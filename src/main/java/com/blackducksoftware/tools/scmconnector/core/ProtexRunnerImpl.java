/*******************************************************************************
 * Copyright (C) 2015 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.tools.scmconnector.core;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.tools.commonframework.standard.email.EmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.workbook.CsvWriter;
import com.blackducksoftware.tools.commonframework.standard.workbook.ExcelWriter;
import com.blackducksoftware.tools.commonframework.standard.workbook.WorkbookWriter;
import com.blackducksoftware.tools.scmconnector.core.protex.ConnectorProjectErrorObserver;
import com.blackducksoftware.tools.scmconnector.core.protex.ProjectCreator;
import com.blackducksoftware.tools.scmconnector.core.protex.ProjectCreatorFactory;
import com.blackducksoftware.tools.scmconnector.core.protex.ProjectErrorObserver;

public class ProtexRunnerImpl implements ProtexRunner {
    private static final String NOTIFICATION_EMAIL_FILENAME = "notifications.xml";
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final ConnectorConfig config;
    private final EmailNotifier emailNotif;
    private final Notifier notifier;
    private final String notificationEmailFilePath;

    public ProtexRunnerImpl(ConnectorConfig config, EmailNotifier emailNotif) {
	this.config = config;
	this.emailNotif = emailNotif;
	notificationEmailFilePath = this.getClass().getClassLoader()
		.getResource(NOTIFICATION_EMAIL_FILENAME).getFile();
	notifier = new Notifier(config, emailNotif, notificationEmailFilePath);
    }

    @Override
    public void run(Connector connector, int i, String projectName,
	    String aggregatedRepositoryPath) throws Exception {
	//
	// Project Creation
	//
	log.info("Creating or finding project: " + projectName);
	ProjectCreator projCreator;
	try {
	    projCreator = ProjectCreatorFactory.getCreator(config, connector);
	} catch (MalformedURLException e) {
	    log.error("Invalid protex server URL");
	    return;
	}
	ProjectErrorObserver errorObserver = new ConnectorProjectErrorObserver(
		connector, emailNotif);
	projCreator.addErrorObserver(errorObserver);
	Project project = createProtexProject(config, connector, i,
		projectName, projCreator);
	if (project == null) {
	    log.error("Issues creating project: " + projectName);
	    return;
	}

	if (!config.isDoProtexScan()) {
	    return; // configured for no scanning
	}

	String furnishedBy = config.getOptionalProperty("connector." + i
		+ ".furnished_by");
	String sourceCodeUrl = config.getOptionalProperty("connector." + i
		+ ".source_code_url");
	projCreator.setCodeLabelOption(projectName, project.getProjectId(),
		furnishedBy, sourceCodeUrl);

	log.info("Analyzing project with name " + projectName);
	AnalysisResults projectAnalysisResults = projCreator
		.analyzeProject(project);
	if (projectAnalysisResults == null) {
	    log.error("Skipping project with name " + projectName
		    + " as there was issues getting information from Protex");
	    return;
	}

	log.info("Analysis of project with name " + projectName + " completed");
	connector.setAnalysisResults(projectAnalysisResults
		.getPropertiesForConnectorAnalysisResults(i));

	notifier.sendNotificationEmail(connector.getName(),
		connector.getRepositoryPath(), aggregatedRepositoryPath,
		projectAnalysisResults);

	generateStandardReport(config, i, projectName, projCreator, project);
	generateCustomReport(config, i, projectName, projCreator, project);
    }

    private Project createProtexProject(ConnectorConfig config,
	    Connector connector, int i, String projectName,
	    ProjectCreator projCreator) {

	String cloneProjectName = config.getOptionalProperty("connector." + i
		+ ".project_template_name");
	Project project;
	if (cloneProjectName == null) {
	    project = projCreator.createNewProject(projectName);
	} else {
	    project = projCreator.cloneProject(projectName, cloneProjectName);
	}
	return project;
    }

    /**
     * Generate a "standard" report (template defined in Protex: Tools > Policy
     * Manager > Reports).
     *
     * @param connectorIndex
     * @param projectName
     * @param projCreator
     * @param project
     */
    private void generateStandardReport(ConnectorConfig config,
	    int connectorIndex, String projectName, ProjectCreator projCreator,
	    Project project) {
	String propName = "connector." + connectorIndex
		+ ".report_template_name";
	String reportTemplateName = config.getOptionalProperty(propName);

	propName = "connector." + connectorIndex + ".report_filename";
	String reportFileName = config.getOptionalProperty(propName);

	if (reportTemplateName != null && reportFileName != null) {
	    log.info("Generating standard report using template: "
		    + reportTemplateName + " to output " + reportFileName);
	    projCreator.generateStandardReportFromTemplate(projectName,
		    project.getProjectId(), reportTemplateName, reportFileName);
	}
    }

    /**
     * Generate a "custom" report (template defined in the given Excel .xlsx
     * file).
     *
     * @param connectorIndex
     * @param projectName
     * @param projCreator
     * @param project
     */
    private void generateCustomReport(ConnectorConfig config,
	    int connectorIndex, String projectName, ProjectCreator projCreator,
	    Project project) throws Exception {

	String propName = "connector." + connectorIndex
		+ ".custom_report_template_filename";
	String reportTemplateFilename = config.getOptionalProperty(propName);

	propName = "connector." + connectorIndex + ".custom_report_filename";
	String reportFilePath = config.getOptionalProperty(propName);

	if (reportTemplateFilename != null && reportFilePath != null) {
	    log.info("Generating custom report using template: "
		    + reportTemplateFilename + " to output " + reportFilePath);

	    projCreator.generateCustomReportFromTemplate(projectName,
		    project.getProjectId(), reportTemplateFilename,
		    getWorkbookWriter(reportFilePath));
	}
    }

    private WorkbookWriter getWorkbookWriter(String reportFilePath)
	    throws IOException {

	if (reportFilePath.endsWith(".xlsx")) {
	    log.info("Output (report) file extension is .xlsx, so the generated file format will be Microsoft Excel (2007 or later)");
	    return new ExcelWriter(reportFilePath);
	} else {
	    log.info("Output (report) file extension is not .xlsx, so the generated file format will be Comma Separated Values (CSV)");
	    return new CsvWriter(reportFilePath);
	}
    }

}
