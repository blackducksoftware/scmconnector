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
/**
 *
 */
package com.blackducksoftware.tools.scmconnector.core.protex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.blackducksoftware.sdk.fault.ErrorCode;
import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.common.CodeLabelOption;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.CloneOption;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.sdk.protex.project.ProjectUpdateRequest;
import com.blackducksoftware.sdk.protex.project.RapidIdentificationMode;
import com.blackducksoftware.sdk.protex.report.Report;
import com.blackducksoftware.sdk.protex.report.ReportFormat;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.workbook.WorkbookWriter;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;

/**
 * @author akamen All common project creation methods will be housed here.
 */
public abstract class ProjectCreatorImpl implements ProjectCreator {
    protected ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper;
    protected ProtexUtils protexUtils;

    protected enum PROJECT_API_CREATION {
	NEW_PROJECT, CLONE_PROJECT
    };

    protected List<ProjectErrorObserver> errorObservers = new ArrayList<ProjectErrorObserver>();

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final ConnectorConfig config;

    public ProjectCreatorImpl(ConnectorConfig config,
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper)
	    throws IllegalArgumentException {
	this.config = config;
	this.protexServerWrapper = protexServerWrapper;
	initProtexApis();
    }

    @Override
    public void addErrorObserver(ProjectErrorObserver o) {
	errorObservers.add(o);
    }

    @Override
    public void removeErrorObserver(ProjectErrorObserver o) {
	errorObservers.remove(o);
    }

    @Override
    public void notifyErrorObservers(Exception e, String message,
	    String projectName) {
	for (ProjectErrorObserver o : errorObservers) {
	    o.reportError(e, message, projectName);
	}
    }

    private void initProtexApis() throws IllegalArgumentException {
	// initializes all the Protex SDK APIs required for this class
	try {
	    protexUtils = new ProtexUtils(protexServerWrapper);
	} catch (Exception e) {
	    throw new IllegalArgumentException(
		    "Unable to initialize Protex API, fatal!: "
			    + e.getMessage(), e);
	}

	log.info("Protex APIs initialized successfully");
    }

    public Project cloneProject(String projectToCloneToName, String projectToCloneFromName,
	    AnalysisSourceLocation analysisSourceLoc) {
	log.info("[Clone] Searching if project name exists: " + projectToCloneToName);
	Project projectToCloneTo = doesProjectExist(projectToCloneToName,
		PROJECT_API_CREATION.NEW_PROJECT);

	if (projectToCloneTo != null) {
	    log.info("Project exists with name: " + projectToCloneTo.getName());
	    return projectToCloneTo;
	}

	Project projectToCloneFrom = doesProjectExist(projectToCloneFromName,
		PROJECT_API_CREATION.CLONE_PROJECT);
	;

	List<CloneOption> cloneOptions = new ArrayList<CloneOption>();

	cloneOptions.add(CloneOption.ASSIGNED_USERS);
	cloneOptions.add(CloneOption.COMPLETED_WORK);
	if (config.isCopyAnalysisResultsFromTemplate()) {
	    log.info("Clone options: adding ANALYSIS_RESULTS");
	    cloneOptions.add(CloneOption.ANALYSIS_RESULTS);
	}

	String projectId = null;

	// we may not know that the project name has already been created by
	// another user
	// because the previously called getProjects() method only returns
	// projects that the current user is assigned to

	log.info("Cloning project " + projectToCloneFromName
		+ " to create project with name " + projectToCloneToName);
	try {

	    projectId = protexServerWrapper
		    .getInternalApiWrapper()
		    .getProjectApi()
		    .cloneProject(projectToCloneFrom.getProjectId(), projectToCloneToName,
			    cloneOptions, false, new ArrayList<String>());
	} catch (SdkFault e) {
	    log.error("Error cloning project: " + e.getMessage());
	    if (e.getFaultInfo().getErrorCode()
		    .equals(ErrorCode.DUPLICATE_PROJECT_NAME)) {
		String message = "Project Name '" + projectToCloneToName
			+ "' Already Exists - Project Creation Failed";
		log.error(message, e);
		notifyErrorObservers(e, message, projectToCloneToName);
	    }

	    return null;

	}

	if (config.isRunRapidId()) {
	    configureRapidID(projectToCloneToName, projectId);
	}

	try {
	    projectToCloneTo = protexServerWrapper.getInternalApiWrapper()
		    .getProjectApi().getProjectById(projectId);
	} catch (SdkFault e) {
	    String message = "Error obtaining Project with ID " + projectId;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectToCloneToName);
	}

	log.info("Updating description and analysis source directory for project with name "
		+ projectToCloneToName);

	projectToCloneTo.setAnalysisSourceLocation(analysisSourceLoc);

	try {
	    protexServerWrapper.getInternalApiWrapper().getProjectApi()
		    .updateProject(projectToCloneTo);
	} catch (SdkFault e) {
	    String message = "Error updating configuration for project "
		    + projectToCloneToName;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectToCloneToName);
	}

	return projectToCloneTo;

    }

    public Project createNewProject(String projectName,
	    AnalysisSourceLocation analysisSourceLoc) {
	log.info("[New] Searching if project name exists: " + projectName);
	Project project = doesProjectExist(projectName,
		PROJECT_API_CREATION.NEW_PROJECT);

	if (project != null) {
	    log.info("Project exists with name: " + project.getName());
	    updateProject(project, analysisSourceLoc);
	    return project;
	}

	ProjectRequest projectRequest = getProjectRequest(projectName);
	projectRequest.setAnalysisSourceLocation(analysisSourceLoc);

	project = generateProjectID(projectRequest, projectName);

	return project;

    }

    /**
     * Updates the project with the new analysis source information
     *
     * @param project
     * @param analysisSourceLoc
     */
    private void updateProject(Project project,
	    AnalysisSourceLocation analysisSourceLoc) {
	ProjectUpdateRequest req = new ProjectUpdateRequest();
	req.setAnalysisSourceLocation(analysisSourceLoc);
	req.setProjectId(project.getProjectId());
	try {
	    protexServerWrapper.getInternalApiWrapper().getProjectApi()
		    .updateProject(req);
	    log.info("Updated existing project: " + project.getName());
	} catch (SdkFault e) {
	    log.warn("Unable to update project information:" + e.getMessage());
	}

    }

    @Override
    public void setCodeLabelOption(String projectName, String projectId,
	    String furnishedBy, String sourceCodeUrl) {
	CodeLabelOption codeLabel = new CodeLabelOption();

	if (furnishedBy != null) {
	    codeLabel.setFurnishedBy(furnishedBy);
	}

	if (sourceCodeUrl != null) {
	    codeLabel.setOpenSourceReferenceLocation(sourceCodeUrl);
	}

	try {

	    protexServerWrapper.getInternalApiWrapper().getProjectApi()
		    .updateCodeLabelOption(projectId, codeLabel);
	} catch (SdkFault e) {
	    String message = "Error setting Code Label option for project "
		    + projectName;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectName);
	}
    }

    @Override
    public void generateStandardReportFromTemplate(String projectName,
	    String projectId, String reportTemplateName, String reportFileName) {

	String reportTemplateId = getReportTemplateId(projectName,
		reportTemplateName);
	if (reportTemplateId == null) {
	    return; // error observers have already been notified
	}

	Report report = generateProjectReport(projectName, projectId,
		reportTemplateId, reportFileName);
	if (report == null) {
	    return; // error observers have already been notified
	}

	writeReportToFile(projectName, report, reportFileName);
    }

    private String getReportTemplateId(String projectName,
	    String reportTemplateName) {
	log.info("finding report template by name: " + reportTemplateName);
	String reportTemplateId = null;
	try {
	    reportTemplateId = protexServerWrapper.getInternalApiWrapper()
		    .getReportApi().suggestReportTemplates(reportTemplateName)
		    .get(0).getReportTemplateId();
	} catch (SdkFault e1) {
	    String message = "Template " + reportTemplateName
		    + " is not defined in Protex.";
	    log.error(message, e1);
	    notifyErrorObservers(e1, message, projectName);
	    return null;
	} catch (IndexOutOfBoundsException e2) {
	    Exception templateUndefinedException = new Exception("Template "
		    + reportTemplateName + " is not defined in Protex.");
	    log.error(templateUndefinedException.getMessage(),
		    templateUndefinedException);
	    notifyErrorObservers(templateUndefinedException,
		    templateUndefinedException.getMessage(), projectName);
	    return null;
	}
	return reportTemplateId;
    }

    private Report generateProjectReport(String projectName, String projectId,
	    String reportTemplateId, String reportFileName) {
	Report report = null;
	try {
	    log.info("calling SDK report function to return report by template for project "
		    + projectName);
	    report = protexServerWrapper
		    .getInternalApiWrapper()
		    .getReportApi()
		    .generateProjectReport(projectId, reportTemplateId,
			    getReportFormat(reportFileName));
	} catch (SdkFault e) {
	    String message = "Error generating report for project: "
		    + projectName;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectName);
	    return null;
	}
	return report;
    }

    private void writeReportToFile(String projectName, Report report,
	    String reportFileName) {
	log.info("writing bytestream of report to file " + reportFileName);

	File transferredFile = new File(reportFileName);
	FileOutputStream outStream = null;
	try {

	    outStream = new FileOutputStream(transferredFile);
	    report.getFileContent().writeTo(outStream);
	} catch (IOException e) {
	    String message = "Error writing to filename: " + reportFileName;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectName);
	    return;
	} finally {
	    if (outStream != null) {
		try {
		    outStream.close();
		} catch (IOException e) {
		    String message = "Error closing stream for filename: "
			    + reportFileName;
		    log.error(message, e);
		}
	    }
	}

	log.info("\nReport written to: " + transferredFile.getAbsolutePath());
    }

    @Override
    public void generateCustomReportFromTemplate(String projectName,
	    String projectId, String reportTemplateFileName,
	    WorkbookWriter workbookWriter) throws Exception {
	CustomReport customReport = new CustomReport(protexServerWrapper,
		config, workbookWriter);
	customReport.generateCustomReportFromTemplate(projectName, projectId,
		reportTemplateFileName);
    }

    protected ConnectorConfig getConfig() {
	return config;
    }

    // //////////////////////////
    // Internal helper methods//
    // //////////////////////////

    private Project doesProjectExist(String projectName,
	    PROJECT_API_CREATION creationType) {
	log.info("Searching if project name exists: " + projectName);
	Project project = null;

	try {
	    project = protexServerWrapper.getInternalApiWrapper()
		    .getProjectApi().getProjectByName(projectName);
	    project.setDescription(ConnectorConstants.PROJECT_DESCRIPTION);
	} catch (WebServiceException webServiceException) {
	    notifyErrorObservers(webServiceException,
		    "Please check the server URL", projectName);
	    throw webServiceException;
	} catch (SdkFault exception) {
	    if (creationType == PROJECT_API_CREATION.CLONE_PROJECT) {
		String message = "Could not find project template with name "
			+ projectName + " to clone over for new project";
		log.error(message, exception);
		notifyErrorObservers(exception, message, projectName);
		return null;
	    } else if (creationType == PROJECT_API_CREATION.NEW_PROJECT) {
		// Ignore
	    }
	}

	return project;
    }

    private ProjectRequest getProjectRequest(String projectName) {
	// Create the project Request
	log.info("Creating New Project with name " + projectName);
	ProjectRequest projectRequest = new ProjectRequest();
	projectRequest.setName(projectName);
	projectRequest.setDescription(ConnectorConstants.PROJECT_DESCRIPTION);

	return projectRequest;
    }

    private Project generateProjectID(ProjectRequest projectRequest,
	    String projectName) {
	Project proj = null;
	String projectId = null;

	// we may not know that the project name has already been created by
	// another user
	// because the previously called getProjects() method only returns
	// projects that the current user is assigned to
	try {
	    projectId = protexServerWrapper.getInternalApiWrapper()
		    .getProjectApi()
		    .createProject(projectRequest, LicenseCategory.PROPRIETARY);
	} catch (SdkFault e) {
	    if (e.getFaultInfo().getErrorCode()
		    .equals(ErrorCode.DUPLICATE_PROJECT_NAME)) {
		String message = "Project Name '" + projectName
			+ "' Already Exists - Project Creation Failed";
		log.error(message, e);
		notifyErrorObservers(e, message, projectName);
		return null;
	    } else if (e.getFaultInfo().getErrorCode()
		    .equals(ErrorCode.INVALID_CREDENTIALS)) {
		String message = "Invalid credentials";
		log.error(message, e);
		notifyErrorObservers(e, message, projectName);
		return null;
	    } else {
		String message = e.getMessage();
		log.error(message, e);
		notifyErrorObservers(e, message, projectName);
		return null;
	    }
	}

	if (config.isRunRapidId()) {
	    configureRapidID(projectName, projectId);
	}

	try {
	    proj = protexServerWrapper.getInternalApiWrapper().getProjectApi()
		    .getProjectById(projectId);
	} catch (SdkFault e) {
	    String message = "Error obtaining Project with ID " + projectId;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectName);
	}

	return proj;
    }

    private void configureRapidID(String projectName, String projectId) {

	try {
	    protexServerWrapper
		    .getInternalApiWrapper()
		    .getProjectApi()
		    .updateRapidIdentificationMode(
			    projectId,
			    RapidIdentificationMode.AUTOMATIC_INCLUDE_GLOBAL_CONFIGURATIONS);
	} catch (SdkFault e) {
	    String message = "Error setting Identification tab Automatic Include Global Configurations Property for project "
		    + projectName;
	    log.error(message, e);
	    notifyErrorObservers(e, message, projectName);
	}
    }

    private ReportFormat getReportFormat(String reportFileName) {

	String fileNameExtension = reportFileName.substring(
		reportFileName.lastIndexOf(".") + 1, reportFileName.length());
	if (fileNameExtension.equals("html")) {
	    return ReportFormat.HTML;
	} else if (fileNameExtension.equals("xls")) {
	    return ReportFormat.XLS;
	} else if (fileNameExtension.equals("doc")) {
	    return ReportFormat.MS_WORD;
	} else if (fileNameExtension.equals("odt")) {
	    return ReportFormat.ODF_TEXT;
	} else {
	    return null;
	}
    }
}
