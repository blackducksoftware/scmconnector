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
import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 *
 * @author akamen updated October 2013
 **/

public abstract class Connector implements Serializable {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    // Location of where Protex expects to find source files.
    private String protexSourceDirectory;
    // Location of where the SCM will dump source into.
    private String connectorSourceDirectory;
    // The number specified by the user within the configuration file.
    private Integer connectorNumber;

    // Status. Zero means success. Set to a negative value on error. Set to a
    // positive value on warning.
    private int status = 0;

    // Status message. Points to an error message if any of the operations fail.
    private String statusMessage = "";

    // A potential combination of the protexSource and connectorSource Dir.
    private String finalSourceDirectory;

    // Keep track of whether prepareSourceLocatoinForSCM() has been called, in
    // case it gets called twice
    private boolean sourceLocationPrepared = false;

    private Properties analysisResults;

    public Properties getAnalysisResults() {
	return analysisResults;
    }

    public void setAnalysisResults(Properties analysisResults) {
	this.analysisResults = analysisResults;
    }

    public int getStatus() {
	return status;
    }

    public void setStatus(int status) {
	this.status = status;
    }

    private void setMessageInResults(String message) {
	if (analysisResults == null) {
	    analysisResults = new Properties();
	}
	analysisResults.setProperty("results.message", message);
    }

    public String getErrorMessage() {
	return statusMessage;
    }

    public void setErrorMessage(String errorMessage) {
	if ((statusMessage != null) && (statusMessage.length() > 0)) {
	    errorMessage = statusMessage + "; " + errorMessage;
	}
	statusMessage = errorMessage;
	setMessageInResults(statusMessage);
    }

    public void init(Properties prps) throws Exception {
	// Calling ConnectorPropertyRetriever's constructor modifies it's static
	// data
	// See See Jira ticket PROSERVSCM-122.
	new ConnectorPropertyRetriever(prps);

	connectorSourceDirectory = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_ROOT);
	protexSourceDirectory = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.PROTEX_SOURCE_DIRECTORY);
	prepareSourceLocationForSCM();
    }

    /**
     * This method should return the name of the SCM your connector connects to
     * (e.g. "Git", "Perforce", etc.).
     *
     * @return
     */
    public abstract String getName();

    /**
     * This method should return the full repository URL (possibly modified to
     * include things like username, source code project/module name or path,
     * etc.).
     *
     * @return
     */
    public abstract String getRepositoryPath(); // path designating what we are
						// checking out from the server

    /**
     * Returns the fully qualified location of where the SCMs will place their
     * sources Including the user specified connector path *and* the Protex
     * source directory (if available).
     *
     * @return
     */
    public String getFinalSourceDirectory() {
	return finalSourceDirectory;
    }

    /**
     * Returns the sub-location of where the SCMs will place their sources.
     *
     * @return
     */
    public String getConnectorSourceDirectory() {
	return connectorSourceDirectory;
    }

    /**
     * Perform the checkout or update operation to get the latest source code
     * from the SCM and put it in the root directory.
     *
     * @return
     */
    public abstract int sync();

    /**
     * Prepares the location of where the SCM will dump files into. If the
     * location exists, then moves on, otherwise creates it.
     *
     */
    protected void prepareSourceLocationForSCM() {
	if (sourceLocationPrepared) {
	    // This method used to be called from each Connector's sync, but is
	    // now called
	    // from Connector.init(). Just in case someone didn't get the memo,
	    // check to
	    // be sure it isn't executed twice.
	    return;
	}
	// We can safely place a file separator in between, because extra
	// slashes do not compromise directory creation.
	String finalDir = deriveFinalDir(protexSourceDirectory,
		connectorSourceDirectory);

	doesProtexSourceDirExist();

	File dir = new File(finalDir);

	if (!dir.exists()) {
	    boolean success = dir.mkdirs();
	    if (success) {
		log.info("Created target directory: " + dir.getAbsolutePath());
	    } else {
		log.warn("Unable to create path:" + dir);
	    }

	}

	finalSourceDirectory = dir.getAbsolutePath();

	log.info("Final source destination: " + finalSourceDirectory);

	try {
	    checkDiskPermissions();
	} catch (Exception e) {
	    log.warn(e.getMessage());
	}
	sourceLocationPrepared = true;
    }

    private String deriveFinalDir(String protexSourceDirectory,
	    String connectorSourceDirectory) {
	String finalDir;
	if ((protexSourceDirectory != null)
		&& (protexSourceDirectory.length() > 0)) {
	    // We're running on the server, so finalDir is the following
	    // concatenation
	    finalDir = protexSourceDirectory + File.separator
		    + connectorSourceDirectory;
	} else {
	    // We're running remotely, so finalDir is simply the
	    // connectorSourceDirectory specified in .properties file
	    finalDir = connectorSourceDirectory;
	}
	return finalDir;
    }

    private void doesProtexSourceDirExist() {
	if (protexSourceDirectory != null && protexSourceDirectory.length() > 1) {
	    File protexDir = new File(protexSourceDirectory);
	    if (!protexDir.exists()) {
		log.warn("** Protex source directory that you specified, does not exist!! **");
	    }
	}
    }

    /**
     * Validates the root, to make sure that the user can write to it.
     *
     * @throws Exception
     */
    protected void checkDiskPermissions() throws Exception {
	File finalSrcDir = new File(finalSourceDirectory);
	try {
	    boolean canWrite = verifyDirectoryIsWriteable(finalSrcDir);
	    if (!canWrite) {
		throw new Exception("Unable to write to the location: "
			+ finalSrcDir);
	    }
	} catch (Exception e) {
	    throw new Exception("Permission check fail: " + e.getMessage());
	}

    }

    /**
     * This is a sure way to verify the permissions of a directory. While java
     * natively provides such checks they appear to be entirely unreliable and
     * provide no consistency across various systems.
     *
     * @param protexHome
     * @return
     */
    private boolean verifyDirectoryIsWriteable(File path) {
	FileOutputStream fos = null;
	File tempFile = null;
	try {
	    tempFile = new File(path + File.separator + "temp.txt");
	    File rootDir = new File(tempFile.getParent());
	    rootDir.mkdirs();

	    fos = new FileOutputStream(tempFile);

	    fos.write(1);

	    fos.close();
	    fos.flush();

	    tempFile.delete();
	    return true;
	} catch (Exception e) {
	    log.warn("Encountered error during permission check: "
		    + e.getMessage());
	    return false;
	} finally {
	    try {
		if (fos != null) {
		    fos.close();
		}

		tempFile.deleteOnExit();
	    } catch (IOException ignore) {
	    }

	}
    }

    /**
     * This performs a check to ensure that whatever executable is being
     * invoked, actually exists.
     *
     * @throws Exception
     */
    protected void validateExecutableInstance(String EXECUTABLE)
	    throws Exception {
	log.info("Verifying installation");

	CommandLine command = CommandLine.parse(EXECUTABLE);
	command.addArgument("--version");

	String workingDir = System.getProperty("user.dir");

	if (workingDir != null) {
	    int returnCode = (new CommandLineExecutor()).executeCommand(log,
		    command, new File(workingDir));
	    if (returnCode == 0) {
		log.info("Found installation of: " + EXECUTABLE);
	    } else {
		log.warn("Unable to determine location of: " + EXECUTABLE);
		log.warn("Please install and add to path.");
	    }
	}

    }

    public Integer getConnectorNumber() {
	return connectorNumber;
    }

    public void setConnectorNumber(Integer connectorNumber) {
	this.connectorNumber = connectorNumber;
    }

    protected boolean localWorkingCopyExists(File targetDir, String scmDirName,
	    String moduleName) {
	if (isScmModuleDirectory(targetDir, scmDirName, moduleName)) {
	    return true;
	}
	return false;
    }

    /**
     * Pull the module name from the last part of the URL. Has package access
     * only to make it testable.
     *
     * @param url
     *            the SCM url that contains the module name in the final
     *            component
     * @return the module name extracted from the URL
     */
    public static String getModuleNameFromURL(String url) {
	String[] urlParts = url.split("/");
	return urlParts[urlParts.length - 1];
    }

    private boolean isScmModuleDirectory(File targetDir, String scmDirName,
	    String moduleName) {
	if (!targetDir.getName().equals(moduleName)) {
	    return false;
	}
	File[] dirFiles = targetDir.listFiles();
	if (dirFiles == null) {
	    return false;
	}
	for (File dirFile : dirFiles) {
	    if (dirFile.isDirectory() && (scmDirName.equals(dirFile.getName()))) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Add the given path component (dir name or filename) to the given path.
     * For example: Given "/Temp" and "mysubdir", return "/Temp/mysubdir". Be
     * smart about file separators (number and direction). Giving this package
     * access so it's testable
     *
     * @param path
     *            the path to add to
     * @param pathComponent
     *            the dir name or filename to add to it
     * @return the resulting path
     */
    public static String addPathComponentToPath(String path,
	    String pathComponent) {
	char lastChar = path.charAt(path.length() - 1);

	if (File.separatorChar != lastChar) {
	    path += File.separatorChar;
	}
	return path + pathComponent;
    }
}
