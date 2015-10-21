package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

/**
 * The test fixture for a test. Holds all the test-specific information (files,
 * etc.)
 *
 * @author sbillings
 *
 */
public class TestFixture {
    private File dir; // The local working dir
    private File secondDir; // Another local working dir used to alter the
			    // remote repository
    private File resultsDir;
    private File localSourceDir;
    private File srcConfigFile;
    private File destConfigFile;
    private Properties properties = null;
    private ConnectorRunner connectorRunner = null;
    private MockProtexRunner protexRunner = null;

    TestFixture(String srcConfigDirPath, String srcConfigFilename,
	    String additionalProperties, int numConnectors) {
	srcConfigFile = new File(srcConfigDirPath + "/" + srcConfigFilename);
	setupDirs();
	File tempResourceDir = deriveTempResourceDirectory(dir); // Prep the
								 // folder where
								 // we're going
								 // to put the
								 // resource
								 // file

	// Copy the source config file to the temp folder, and add the
	// connector.0.root property
	destConfigFile = new File(tempResourceDir.getAbsolutePath() + "/"
		+ srcConfigFilename);
	String additionalPropertiesText = "\n";
	if (additionalProperties != null) {
	    additionalPropertiesText += additionalProperties + "\n";
	}
	copyAndAppendToFile(srcConfigFile, destConfigFile,
		getRootProperties(localSourceDir, numConnectors)
			+ additionalPropertiesText);
	assertTrue("Modified config file exists", destConfigFile.exists());
    }

    public MockProtexRunner getProtexRunner() {
	return protexRunner;
    }

    public void setProtexRunner(MockProtexRunner protexRunner) {
	this.protexRunner = protexRunner;
    }

    private String getRootProperties(File localSourceDir, int numConnectors) {
	StringBuilder s = new StringBuilder();
	s.append("\n");
	for (int i = 0; i < numConnectors; i++) {
	    s.append("connector.");
	    s.append(i);
	    s.append(".root=");
	    s.append(fixFileSeparators(localSourceDir.getAbsolutePath()));
	    s.append("/\n");
	}
	return s.toString();
    }

    TestFixture(Properties configProperties) {
	properties = configProperties;
	setupDirs();
	properties.setProperty("connector.0.root",
		fixFileSeparators(localSourceDir.getAbsolutePath()));
    }

    public Properties getProperties() {
	return properties;
    }

    private void setupDirs() {
	dir = getTempFolder(); // Create a temp folder for the test to work in
	secondDir = getTempFolder(); // Create a second temp folder used when
				     // altering repo on server
	resultsDir = getTempFolder(); // Create a temp folder for results
	localSourceDir = deriveLocalSourceDirectory(dir); // Create a subfolder
							  // for the SCM
							  // checkout
    }

    public File getDir() {
	return dir;
    }

    public File getSecondDir() {
	return secondDir;
    }

    public File getResultsDir() {
	return resultsDir;
    }

    public File getLocalSourceDir() {
	return localSourceDir;
    }

    public File getDestConfigFile() {
	return destConfigFile;
    }

    public ConnectorRunner getConnectorRunner() {
	return connectorRunner;
    }

    public void setConnectorRunner(ConnectorRunner runner) {
	connectorRunner = runner;
    }

    public void cleanUp() {
	try {
	    FileUtils.deleteQuietly(getDir()); // Clean up
	} catch (Exception e) {
	    System.out.println("Error deleting " + getDir().getAbsolutePath()
		    + ": " + e.getMessage());
	}

	try {
	    FileUtils.deleteQuietly(getSecondDir());
	} catch (Exception e) {
	    System.out.println("Error deleting " + getDir().getAbsolutePath()
		    + ": " + e.getMessage());
	}
    }

    private static File getTempFolder() {
	File temp = null;

	try {
	    temp = File.createTempFile("scmtest",
		    Long.toString(System.nanoTime()));
	} catch (IOException e) {
	    fail("Error creating temp dir for test");
	}

	if (!(temp.delete())) {
	    fail("Could not delete temp file: " + temp.getAbsolutePath());
	}

	if (!(temp.mkdir())) {
	    fail("Could not create temp directory: " + temp.getAbsolutePath());
	}

	return (temp);
    }

    private void copyAndAppendToFile(File srcFile, File destFile,
	    String textToAppend) {
	try {
	    FileUtils.copyFile(srcFile, destFile);
	    PrintWriter out = new PrintWriter(new BufferedWriter(
		    new FileWriter(destFile, true)));
	    out.println(textToAppend);
	    out.close();
	} catch (IOException e) {
	    fail("appendTextToFile() failed.");
	}
    }

    private String fixFileSeparators(String path) {
	StringBuilder builtString = new StringBuilder();
	for (int i = 0; i < path.length(); i++) {
	    if (path.charAt(i) == '\\') {
		builtString.append('/');
	    } else {
		builtString.append(path.charAt(i));
	    }
	}
	return builtString.toString();
    }

    private File deriveLocalSourceDirectory(File tempFolder) {
	File localSourceDirectory = new File(tempFolder.getAbsolutePath()
		+ "/scm_files");
	localSourceDirectory.mkdir();
	return localSourceDirectory;
    }

    private File deriveTempResourceDirectory(File tempFolder) {
	String tempResourceDirectoryPath = tempFolder.getAbsolutePath()
		+ "/resources";
	File tempResourceDirectory = new File(tempResourceDirectoryPath);
	tempResourceDirectory.mkdir();
	return tempResourceDirectory;
    }
}
