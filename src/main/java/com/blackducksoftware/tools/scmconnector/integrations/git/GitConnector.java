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

package com.blackducksoftware.tools.scmconnector.integrations.git;

import java.io.File;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.CommandLineExecutor;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;
import com.blackducksoftware.tools.scmconnector.core.ConnectorPropertyRetriever;
import com.blackducksoftware.tools.scmconnector.core.ICommandLineExecutor;

public class GitConnector extends Connector {

    private static final long serialVersionUID = 1820938464648790788L;
    private static final String EXECUTABLE = "git";
    private static final String GIT_COMMAND_CLONE = "clone";
    private static final String GIT_COMMAND_STATUS = "status";
    private static final String GIT_COMMAND_PULL = "pull";

    private static final String[] CREDENTIAL_COMPATABLE_PROTOCOLS = new String[] {
	    "ssh://", "svn+ssh://", "http://", "https://" };

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final ICommandLineExecutor commandLineExecutor;

    private String repositoryURL;
    private String username;
    private String password;

    private String branch;
    private String tag;
    private String sha;

    private boolean disableStrictHostKeyChecking = false;

    public GitConnector() {
	commandLineExecutor = new CommandLineExecutor();
    }

    public GitConnector(ICommandLineExecutor commandLineExecutor) {
	this.commandLineExecutor = commandLineExecutor;
    }

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	repositoryURL = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);
	username = prps.getProperty(ConnectorConstants.CONNECTOR_PROPERTY_USER);

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();

	repositoryURL = addUsernamePassword(repositoryURL, username, password);

	// Calling prps.getProperty() (vs.
	// ConnectorPropertyRetriever.getPropertyValue())
	// avoids a warning message when the property is not set
	branch = prps.getProperty(ConnectorConstants.GIT_PROPERTY_BRANCH);
	tag = prps.getProperty(ConnectorConstants.GIT_PROPERTY_TAG);
	sha = prps.getProperty(ConnectorConstants.GIT_PROPERTY_SHA);

	String disableStrictHostKeyCheckingString =
	// ConnectorPropertyRetriever.getPropertyValue(ConnectorConstants.GIT_PROPERTY_DISABLESTRICTHOSTKEYCHECKING);

	prps.getProperty(
		ConnectorConstants.GIT_PROPERTY_DISABLESTRICTHOSTKEYCHECKING,
		"false");
	disableStrictHostKeyChecking = disableStrictHostKeyCheckingString
		.equalsIgnoreCase("true");
	if (disableStrictHostKeyChecking) {
	    log.warn("SSH strict host key checking has been disabled.");
	}
    }

    private String addUsernamePassword(String repositoryURL, String username,
	    String password) {

	if (StringUtils.isNotBlank(username) && StringUtils.isBlank(password)) {
	    return addCredentialStringToGitUrl(repositoryURL, username);
	}
	if (StringUtils.isNotBlank(username)
		&& StringUtils.isNotBlank(password)) {
	    return addCredentialStringToGitUrl(repositoryURL, username + ":"
		    + password);
	}
	return repositoryURL;
    }

    public static String addCredentialStringToGitUrl(String url,
	    String credentialString) {
	for (String protocol : CREDENTIAL_COMPATABLE_PROTOCOLS) {
	    if (url.startsWith(protocol) && !url.contains("@")
		    && StringUtils.isNotBlank(credentialString)) {
		StringBuilder sb = new StringBuilder(protocol);
		/*
		 * Example
		 * 
		 * 1.
		 * ssh://blackduck@mamba-vm.blackducksoftware.com/usr/src/git/
		 * SampleFiles.git 2.
		 * ssh://mamba-vm.blackducksoftware.com/usr/src
		 * /git/SampleFiles.git If we are here then the url would be in
		 * the form of 2 (1 form will never make it here)
		 */
		sb.append(credentialString);
		sb.append('@');
		sb.append(StringUtils.substringAfter(url, protocol));
		return sb.toString();
	    }
	}
	return url;
    }

    /**
     * Checkout or update from GIT.
     */
    @Override
    public int sync() {
	int exitStatus = -1;
	try {

	    CommandLine command = CommandLine.parse(EXECUTABLE);

	    String targetRepoParentPath = getFinalSourceDirectory();
	    String targetRepoPath = addPathComponentToPath(
		    targetRepoParentPath,
		    getModuleNameFromURLGit(repositoryURL));

	    File targetRepoParentDir = new File(targetRepoParentPath);
	    File targetRepoDir = new File(targetRepoPath);

	    boolean cloneExists = doesGitCloneExist(targetRepoDir);

	    if (!cloneExists) {
		command.addArgument(GIT_COMMAND_CLONE);
		command.addArgument(repositoryURL);
		command.addArgument(targetRepoDir.getAbsolutePath());
		exitStatus = commandLineExecutor.executeCommand(log, command,
			targetRepoParentDir, "yes");
	    } else {
		command.addArgument(GIT_COMMAND_PULL);
		exitStatus = commandLineExecutor.executeCommand(log, command,
			targetRepoDir, "yes");
	    }

	    if (branch != null) {
		gitCheckoutBranchShaOrPrefixedTag(branch);
	    } else if (tag != null) {
		gitCheckoutTag(tag);
	    } else if (sha != null) {
		gitCheckoutBranchShaOrPrefixedTag(sha);
	    }

	} catch (Exception e) {
	    log.error("Unable to perform sync: " + e.getMessage(), e);
	    log.info("Cause: " + e.getCause());
	    exitStatus = -1;
	}
	return exitStatus;

    }

    private void gitCheckoutBranchShaOrPrefixedTag(String branchShaOrPrefixedTag)
	    throws Exception {
	CommandLine command = CommandLine.parse(EXECUTABLE);

	String sourceDirIncludingModule = addPathComponentToPath(
		getFinalSourceDirectory(),
		getModuleNameFromURLGit(repositoryURL));

	File targetDir = new File(sourceDirIncludingModule);

	command.addArgument("checkout");
	command.addArgument(branchShaOrPrefixedTag);
	int exitStatus = commandLineExecutor.executeCommand(log, command,
		targetDir);
	if (exitStatus != 0) {
	    throw new Exception("Git checkout " + branchShaOrPrefixedTag
		    + " failed: " + exitStatus);
	}
    }

    private void gitCheckoutTag(String tag) throws Exception {
	gitCheckoutBranchShaOrPrefixedTag("tags/" + tag);
    }

    /**
     * Checks to see if there is an existing git clone
     *
     * @param targetDir
     * @return
     * @throws Exception
     */
    private boolean doesGitCloneExist(File targetDir) throws Exception {
	validateExecutableInstance(EXECUTABLE);

	CommandLine command = CommandLine.parse(EXECUTABLE);

	command.addArgument(GIT_COMMAND_STATUS);
	command.addArgument(repositoryURL);

	// Check to see if there are any files in the target directory
	// If not, return false
	File[] files = targetDir.listFiles();
	if (files == null || files.length == 0) {
	    log.info("Nothing inside the target directory: " + targetDir);
	    log.info("Skipping status check.");
	    return false;
	}

	int exitStatus = commandLineExecutor.executeCommand(log, command,
		targetDir);

	if (exitStatus == 0) {
	    log.info("repository exists in directory: "
		    + targetDir.getAbsolutePath());
	    return true;
	} else {
	    log.info("repository does not exist in directory: "
		    + targetDir.getAbsolutePath());
	    return false;
	}
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
    public static String getModuleNameFromURLGit(String url) {
	String[] urlParts = url.split("/");
	String moduleName = urlParts[urlParts.length - 1];

	moduleName = moduleName.replace(".git", "");
	return moduleName;
    }

    @Override
    public String getName() {
	return "Git";
    }

    @Override
    public String getRepositoryPath() {
	return repositoryURL;
    }

}