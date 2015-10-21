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

import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.tools.commonframework.standard.workbook.WorkbookWriter;
import com.blackducksoftware.tools.scmconnector.core.AnalysisResults;

/**
 * @author akamen
 *
 *         Basic interface that governs all project creations
 */
public interface ProjectCreator {

    AnalysisResults analyzeProject(Project project);

    Project cloneProject(String projectName, String cloneProjectName);

    Project createNewProject(String projectName);

    void setCodeLabelOption(String projectName, String projectId,
	    String furnishedBy, String sourceCodeUrl);

    void generateStandardReportFromTemplate(String projectName,
	    String projectId, String reportTemplateName, String reportFileName);

    void generateCustomReportFromTemplate(String projectName, String projectId,
	    String reportTemplateFileName, WorkbookWriter workbookWriter)
	    throws Exception;

    void addErrorObserver(ProjectErrorObserver o);

    void removeErrorObserver(ProjectErrorObserver o);

    void notifyErrorObservers(Exception e, String message, String projectName);
}
