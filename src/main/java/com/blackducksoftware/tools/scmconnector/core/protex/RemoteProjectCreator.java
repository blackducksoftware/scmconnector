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
package com.blackducksoftware.tools.scmconnector.core.protex;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.blackducksoftware.protex.plugin.BlackDuckCommand;
import com.blackducksoftware.protex.plugin.BlackDuckCommand.State;
import com.blackducksoftware.protex.plugin.BlackDuckCommandBuilder;
import com.blackducksoftware.protex.plugin.BlackDuckCommandBuilder.AnalyzeCommandBuilder;
import com.blackducksoftware.protex.plugin.BuildToolIntegrationException;
import com.blackducksoftware.protex.plugin.ProtexServer;
import com.blackducksoftware.protex.plugin.event.AnalysisListener;
import com.blackducksoftware.protex.plugin.event.ProgressObserver;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceRepository;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.scmconnector.core.AnalysisResults;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;

/**
 * Implementation of all remote project creation
 *
 * This is invoked in situations where the SDK is remote to the server.
 *
 * @author akamen
 *
 */
public class RemoteProjectCreator extends ProjectCreatorImpl {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final AnalysisSourceLocation analysisSourceLoc;
    private final Connector connector;

    public RemoteProjectCreator(ConnectorConfig config,
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    Connector connector) throws IllegalArgumentException {
	super(config, protexServerWrapper);
	this.connector = connector;
	analysisSourceLoc = new AnalysisSourceLocation();
	/**
	 * This enum may look weird, but it means that the SDK is local to the
	 * code. Not local to the server. From the javadoc: Project source
	 * resides on the client machine, for example when analyzed with bdstool
	 * or the Protex client
	 */
	analysisSourceLoc.setRepository(AnalysisSourceRepository.LOCAL_PROXY);

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
     * Sets up all the analysis parameters and invokes the BDS scan using the
     * Protex Integration Plugin.
     *
     * @param project
     * @param path
     * @return
     */
    @Override
    public AnalysisResults analyzeProject(Project project) {
	String finalSourceLocation = connector.getFinalSourceDirectory();

	AnalysisResults analysisResults = new AnalysisResults(getConfig());
	try {
	    analysisResults.setPreAnalysisInfo(protexUtils,
		    protexServerWrapper, project);
	} catch (Exception e) {
	    log.error("Error getting pre-analysis info for project "
		    + project.getName() + ": " + e.getMessage());
	    return null;
	}

	int exitValue = performBDSAnalysisScan(project, finalSourceLocation);

	if (exitValue != 0) {
	    return null;
	}

	analysisResults.setPostAnalysisInfo(protexUtils, protexServerWrapper,
		project);

	return analysisResults;
    }

    private int performBDSAnalysisScan(Project proj, String root) {

	log.info("Starting BDS analysis; root: " + root);

	File rootDir = new File(root);

	AnalyzeCommandBuilder cmdBuilder = BlackDuckCommandBuilder.analyze();

	ProtexServer server = new ProtexServer(getConfig().getServerBean()
		.getPassword());
	server.setServerUrl(getConfig().getServerBean().getServerName());
	server.setUsername(getConfig().getServerBean().getUserName());

	cmdBuilder.connectedTo(server);
	cmdBuilder.directory(rootDir);

	String projectId = proj.getProjectId();
	if (projectId == null) {
	    log.error("Unable to determine project ID!  Fatal.");
	    return -1;
	}

	cmdBuilder.projectId(projectId);

	try {
	    ClassLoader cl = server.getClientLoader();
	    Thread.currentThread().setContextClassLoader(cl);
	} catch (BuildToolIntegrationException e1) {

	}

	// Setup console listeners
	Level lvl = log.getLevel();
	if (lvl == null) {
	    Logger rootLogger = Logger.getRootLogger();
	    lvl = rootLogger.getLevel();
	}

	// ProtexAnalysisListener listener = new ProtexAnalysisListener(log,
	// lvl);
	ArrayList<AnalysisListener> list = new ArrayList<AnalysisListener>();
	list.add(new ProtexProcessObserver(lvl));
	ProgressObserver po = new ProgressObserver(list);

	cmdBuilder.observingProgress(po);

	BlackDuckCommand command = null;

	log.info("Remotely getting bdstool JAR file from Protex server and linking.");
	try {
	    command = cmdBuilder.build();
	    log.info("Running bdstool command analyze on " + root);
	    command.run();
	} catch (BuildToolIntegrationException e) {
	    log.error("Error running bdstool", e);

	    return 1;
	}

	if (command.state() == State.FAILED) {
	    log.error("BDSTool Failed in Analyzing New Project, please check .bdstool.log file");
	    return 1;

	}

	log.info("Finished BDS analysis.");

	return 0;

    }

}
