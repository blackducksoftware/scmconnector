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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.standard.email.CFEmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.email.EmailNotifier;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

/**
 *
 * @author akamen
 *
 */
public class ConnectorRunner {
    private static final String CONNECTOR_PROPERTY_PREFIX = "connector";
    private static final String LOG_FILENAME = "connector.log";
    private static boolean logFileConfigured = false;
    private static Logger log;
    private final ConnectorConfig config; // The configuration
    private List<Connector> connectors; // One for each SCM repo we'll sync from
    private ProtexRunner protexRunner; // Handles all the protex stuff

    private String aggregatedRepositoryPath = "";

    /**
     * Args (provide first, first 2, or all 3): config file path output file
     * path log file path
     *
     * @param args
     */
    public static void main(String[] args) {

	if (args.length == 3) {
	    setLogFile(args[2]);
	} else {
	    setLogFile(LOG_FILENAME);
	}

	log = Logger.getLogger(ConnectorRunner.class);

	ConnectorRunner runner = null;
	log.info("SCM Connector utility, standalone mode.");
	if ((args.length == 1) || (args.length == 2) || (args.length == 3)) {
	    // Check to see if the config file exists.
	    File configFile = new File(args[0]);
	    if (configFile.exists()) {
		log.info("Loading configuration file from: " + configFile);
		try {
		    ConnectorConfig config = new ConnectorConfig(
			    configFile.getAbsolutePath());
		    EmailNotifier emailNotif = new CFEmailNotifier(config);
		    runner = new ConnectorRunner(config, new ProtexRunnerImpl(
			    config, emailNotif));
		} catch (Exception e) {
		    log.error("Error creating ConnectorRunner for "
			    + configFile.getAbsolutePath() + ": " + e);
		    System.exit(-1);
		}
	    } else {
		log.error("This location does not exist: "
			+ configFile.getAbsolutePath());
		System.exit(-1);
	    }
	} else {
	    log.error("Arguments (only the first is required): <config_file> <results_file> <log_file>");
	    System.exit(-1);
	}

	runner.processConnectors();

	int returnStatus = determineStatus(runner);
	if (args.length >= 2) {
	    writeResults(runner, args[1], returnStatus);
	}

	System.exit(returnStatus);
    }

    /**
     * Constructor that loads config from a given file.
     *
     * @param configFileLocation
     * @param protexRunner
     * @throws Exception
     */
    public ConnectorRunner(ConnectorConfig config, ProtexRunner protexRunner)
	    throws Exception {
	this.config = config;
	init(config, protexRunner);
    }

    /**
     * Constructor that loads config from a given properties object.
     *
     * @param configProperties
     * @param protexRunner
     * @throws Exception
     */
    public ConnectorRunner(Properties configProperties,
	    ProtexRunner protexRunner) throws Exception {
	config = new ConnectorConfig(configProperties);
	init(config, protexRunner);
    }

    public ConnectorConfig getConfig() {
	return config;
    }

    private void init(ConnectorConfig connectConfig, ProtexRunner protexRunner)
	    throws Exception {
	setLogFile(LOG_FILENAME);
	if (log == null) {
	    log = Logger.getLogger(ConnectorRunner.class);
	}
	try {
	    determineHostLocation();
	} catch (Exception e) {
	    log.error("Error initializing ConnectorRunner: " + e);
	    throw e;
	}
	this.protexRunner = protexRunner;
    }

    private boolean processConnectorDoSync(Connector connector,
	    String projectName) {
	if (projectName == null) {
	    log.info("Performing sync of data");
	} else {
	    log.info("Performing sync of data for project: " + projectName);
	}
	boolean checkoutWorked = connector.sync() == 0;
	return checkoutWorked;
    }

    private void processConnector(Connector connector,
	    Properties connectorProperties, int i, boolean finalConnector,
	    String aggregatedRepositoryPath) throws Exception {

	String propName = CONNECTOR_PROPERTY_PREFIX + "." + i + "."
		+ ConnectorConstants.CONNECTOR_PROPERTY_PROTEX_PROJECT_NAME;
	String projectName = config.getOptionalProperty(propName);
	if (projectName == null) {
	    log.info("Processing connector for connector #: " + i);
	    log.warn("Property "
		    + propName
		    + " was not specified; Will not perform Protex analysis for connector #"
		    + i);
	} else {
	    log.info("Processing connector for project: " + projectName);
	}

	boolean checkoutWorked = processConnectorDoSync(connector, projectName);
	if (!checkoutWorked) {
	    log.error("Skipping creation of project as there was issues checking out from the repository");
	    connector.setStatus(-1);
	    connector.setErrorMessage("Sync from SCM repository failed");
	    return;
	}

	if (shouldDoProtexScan(config, projectName, finalConnector)) {
	    protexRunner.run(connector, i, projectName,
		    aggregatedRepositoryPath);
	}
    }

    private boolean shouldDoProtexScan(ConnectorConfig config,
	    String projectName, boolean finalConnector) {
	if (projectName == null) {
	    // By making the protex project name optional above, and returning
	    // normally if it's omitted,
	    // we are making the protex scan optional
	    // If the user doesn't want a protex scan, they just omit the
	    // new_project_name property
	    return false;
	}

	if (config.isProtexScanOnlyLastConnector() && !finalConnector) {
	    // We're in "only do scan on final Connector" mode, and this is not
	    // the final; we're done
	    return false;
	}
	return true;
    }

    /**
     * Collects a list of connectors Then iterates through each one, processing
     * them one by one in order.
     */
    public void processConnectors() {
	connectors = initializeConnectors();
	int connectorListIndex = 0;
	for (Connector connector : connectors) {
	    int connectorNumber = connector.getConnectorNumber();

	    boolean finalConnector = (++connectorListIndex) == connectors
		    .size();

	    Properties connectorProperties = new Properties();

	    // home directory will need to be supplied to individual connectors
	    // in order to determine full path for server-side scans
	    connectorProperties.put(ConnectorConstants.PROTEX_SOURCE_DIRECTORY,
		    config.getProtexHomeDirectory());
	    config.copyPropertiesWithPrefix(connectorProperties,
		    CONNECTOR_PROPERTY_PREFIX + "." + connectorNumber + ".");
	    try {
		connector.init(connectorProperties);
		if (config.isProtexScanOnlyLastConnector()) {
		    appendToAggregatedRepositoryPath(connector
			    .getRepositoryPath() + "; ");
		}
		processConnector(connector, connectorProperties,
			connectorNumber, finalConnector,
			getAggregatedRepositoryPath());
	    } catch (Exception e) {
		log.error("Error: " + e.getMessage(), e);
		connector.setStatus(-1);
		connector.setErrorMessage(e.getMessage());
	    }

	}
	log.info("Finished processing all connectors in the configuration file");
    }

    public String getAggregatedRepositoryPath() {
	return aggregatedRepositoryPath;
    }

    public void appendToAggregatedRepositoryPath(String aggregatedRepositoryPath) {
	this.aggregatedRepositoryPath += aggregatedRepositoryPath;
    }

    public boolean noErrors() {
	for (Connector connector : connectors) {
	    if (connector.getStatus() < 0) { // positive status = warning;
					     // negative = error
		return false;
	    }
	}
	return true;
    }

    public List<Connector> getConnectors() {
	return connectors;
    }

    /**
     * Finds a number of connectors (up to 1000 by default, but this maximum can
     * be changed in the config file) and returns a list of them. This
     * eliminates the need to keep track and index starting at zero.
     *
     * @return
     */
    private List<Connector> initializeConnectors() {
	List<Connector> connectors = new ArrayList<Connector>();
	int counter = 0;

	while (counter < config.getMaxConnectorIndex()) {
	    // this is a slightly convoluted way to get the property, but it
	    // avoids the warning when absent
	    String connectorClass = config.getProps().getProperty(
		    "connector." + counter + ".class");
	    Connector connector = null;

	    if (connectorClass != null) {
		try {
		    Class<?> theClass = Class.forName(connectorClass);
		    connector = (Connector) theClass.newInstance();
		    connector.setConnectorNumber(counter);
		    log.info("Found connector: " + connector.getName());

		    connectors.add(connector);

		} catch (ClassNotFoundException e) {
		    log.error("No class exists with name: " + connectorClass, e);
		} catch (InstantiationException e) {
		    log.error("Error instantiating object for class: "
			    + connectorClass, e);
		} catch (IllegalAccessException e) {
		    log.error("Error getting data access for object of class: "
			    + connectorClass, e);
		}
	    }

	    counter++;
	}

	return connectors;
    }

    /**
     * This will look at the configuration object then determine whether or not
     * we are on the same server and set the local flag.
     */
    private void determineHostLocation() {
	String uri = config.getServerBean().getServerName();
	String resolvedHostName = ConnectorUtils.getHostName();

	// Only if the host name resolves and matches do we flip the switch
	if (resolvedHostName != null) {
	    resolvedHostName = resolvedHostName.toLowerCase();
	    if (uri.contains(resolvedHostName)) {
		log.info("Executing connectors on the same machine as Protex");
		config.setProxyLocal(true);
		return;
	    }
	}

	// Otherwise assume all is remote.
	log.info("Executing connnectors on: " + resolvedHostName);
	config.setProxyLocal(false);

    }

    static void setLogFile(String logFilePath) {
	// Once the log file is configured, don't change it
	if (logFileConfigured) {
	    return;
	}
	logFileConfigured = true;
	Enumeration<Appender> appenders = Logger.getRootLogger()
		.getAllAppenders();
	while (appenders.hasMoreElements()) {
	    Appender appender = appenders.nextElement();
	    if (appender instanceof FileAppender) {
		((FileAppender) appender).setFile(logFilePath);
		((FileAppender) appender).activateOptions();
	    }
	}
    }

    public static void writeResults(ConnectorRunner runner,
	    String resultsFilePath, int returnStatus) {
	Properties mergedResults = new Properties();

	for (int i = 0; i < runner.connectors.size(); i++) {
	    Connector connector = runner.connectors.get(i);
	    Properties connectorResults = connector.getAnalysisResults();
	    if (connectorResults != null) {
		mergedResults.putAll(connectorResults);
	    }
	}

	// Since an error could have occurred even before a Connector got
	// called,
	// the caller is in the best position to know if there was an error or
	// not, so the caller
	// provides the status value
	mergedResults.setProperty("results.status",
		Integer.toString(returnStatus));

	try {
	    File outputFile = new File(resultsFilePath);
	    OutputStream os = new FileOutputStream(outputFile);
	    mergedResults.store(os, "SCM Connector results");
	    os.close();
	} catch (IOException e) {
	    log.error("Warning: error writing results to file "
		    + resultsFilePath + ": " + e.getMessage());
	    return;
	}
    }

    private static int determineStatus(ConnectorRunner runner) {
	if (runner.noErrors()) {
	    return 0;
	}
	for (Connector connector : runner.getConnectors()) {
	    int connectorStatus = connector.getStatus();
	    if (connectorStatus < 0) {
		return connectorStatus;
	    }
	}
	return 0;
    }

}
