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

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.common.AnalysisStatus;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceRepository;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.scmconnector.core.AnalysisResults;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;
import com.blackducksoftware.tools.scmconnector.core.ConnectorUtils;

/**
 * @author akamen
 *
 *         This class handles project creation for situations when the SDK is
 *         local to the server.
 *
 */
public class LocalProjectCreator extends ProjectCreatorImpl {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final Connector connector;
    private final AnalysisSourceLocation analysisSourceLoc = new AnalysisSourceLocation();

    public LocalProjectCreator(ConnectorConfig config,
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    Connector connector) throws IllegalArgumentException,
	    MalformedURLException {
	super(config, protexServerWrapper);

	this.connector = connector;
	initCreator();
    }

    private void initCreator() throws MalformedURLException {
	// Because this is a local init, we are only interested in the connector
	// location.
	String root = connector.getConnectorSourceDirectory();

	/**
	 * If the root has a leading slash, then remove it, because the SDK is
	 * joined with a trailing slash anyway.
	 */
	if (root.startsWith("/")) {
	    root = root.substring(1);
	}

	analysisSourceLoc.setRepository(AnalysisSourceRepository.REMOTE_SERVER);

	log.info("Using source path: " + root);

	analysisSourceLoc.setSourcePath(root);
	analysisSourceLoc.setHostname(ConnectorUtils.getHostFromUri(getConfig()
		.getServerBean().getServerName()));

    }

    @Override
    public Project cloneProject(String projectName, String cloneProjectName) {
	Project proj = cloneProject(projectName, cloneProjectName,
		analysisSourceLoc);
	return proj;
    }

    @Override
    public Project createNewProject(String projectName) {
	Project proj = createNewProject(projectName, analysisSourceLoc);
	return proj;
    }

    /**
     * Analyzes the project using the SDK
     */
    @Override
    public AnalysisResults analyzeProject(Project project) {
	String localSourceDir = connector.getConnectorSourceDirectory();

	AnalysisResults analysisResults = new AnalysisResults(getConfig());
	try {
	    analysisResults.setPreAnalysisInfo(protexUtils,
		    protexServerWrapper, project);
	} catch (Exception e) {
	    log.error("Error getting pre-analysis info for project "
		    + project.getName() + ": " + e.getMessage());
	    return null;
	}
	int exitValue = analyzeProjectUsingSDK(project, localSourceDir);

	if (exitValue != 0) {
	    return null;
	}

	analysisResults.setPostAnalysisInfo(protexUtils, protexServerWrapper,
		project);

	return analysisResults;
    }

    private int analyzeProjectUsingSDK(Project project, String path) {
	String projectId = project.getProjectId();
	log.info("Starting SDK analysis of project " + projectId);

	try {
	    protexServerWrapper.getInternalApiWrapper().getProjectApi()
		    .startAnalysis(projectId, false);
	} catch (SdkFault e1) {
	    log.error("Cannot start analysis for project with ID " + projectId,
		    e1);
	    return 1;
	}
	AnalysisStatus status = null;
	try {
	    status = protexServerWrapper.getInternalApiWrapper()
		    .getProjectApi().getAnalysisStatus(projectId);

	    while (!status.isFinished()) {
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    // do nothing
		}

		status = protexServerWrapper.getInternalApiWrapper()
			.getProjectApi().getAnalysisStatus(projectId);

	    }
	} catch (SdkFault e) {
	    log.error("Cannot obtain status for project with ID " + projectId,
		    e);
	}

	log.info("Completed SDK analysis of project " + projectId);
	return 0;
    }

}
