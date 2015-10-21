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

package com.blackducksoftware.tools.scmconnector.integrations.subversion;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.CommandLineExecutor;
import com.blackducksoftware.tools.scmconnector.core.CommandResults;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;
import com.blackducksoftware.tools.scmconnector.core.ConnectorPropertyRetriever;
import com.blackducksoftware.tools.scmconnector.core.ICommandLineExecutor;

/**
 * SVN connector, similar to GIT connector: Uses the locally installed SVN
 * client and calls it as an external process.
 *
 * @author akamen
 *
 */
public class SubversionConnector extends Connector {

    private static final long serialVersionUID = 1820938464648790788L;

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final ICommandLineExecutor commandLineExecutor;
    private static final String EXECUTABLE = "svn";

    private String repositoryURL;
    private String user;
    private String password;
    private String svnFlags;

    private List<String> svnFlagsList = new ArrayList<String>();

    public SubversionConnector() {
	commandLineExecutor = new CommandLineExecutor();
    }

    public SubversionConnector(ICommandLineExecutor commandLineExecutor) {
	this.commandLineExecutor = commandLineExecutor;
    }

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	repositoryURL = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);
	user = prps.getProperty("user");

	svnFlags = prps.getProperty("flags");

	if (svnFlags != null && !svnFlags.isEmpty()) {
	    svnFlagsList = Arrays.asList(svnFlags.split(","));
	}

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();
    }

    private boolean repoExists(File targetDir) throws Exception {
	validateExecutableInstance(EXECUTABLE);

	CommandLine command = CommandLine.parse(EXECUTABLE);

	command.addArgument("status");

	CommandResults commandResults;
	try {
	    commandResults = commandLineExecutor.executeCommandForOutput(log,
		    command, targetDir);
	} catch (Exception e) {
	    log.error("Failure executing SVN Command", e);
	    commandResults = null;
	}

	if (commandResults != null && commandResults.getStatus() == 0) {

	    // warning message of form "svn: warning: W155007: 'C:\SVNFiles' is
	    // not a working copy" only
	    // printed when repository is not checked out to directory
	    if (commandResults.getOutput().trim().contains("warning")) {
		log.info("repository does not exist in directory: "
			+ targetDir.getAbsolutePath());
		return false;
	    }

	    log.info("repository exists in directory: "
		    + targetDir.getAbsolutePath());

	    return true;
	} else {
	    log.info("repository does not exist in directory: "
		    + targetDir.getAbsolutePath());
	    return false;
	}
    }

    @Override
    public String getName() {
	return "Subversion";
    }

    @Override
    public String getRepositoryPath() {
	return repositoryURL;
    }

    @Override
    public int sync() {

	int exitStatus = -1;

	try {
	    File targetDir = new File(getFinalSourceDirectory());

	    CommandLine command = CommandLine.parse(EXECUTABLE);

	    if (!repoExists(targetDir)) {
		command.addArgument("co");
		command.addArgument(repositoryURL);
	    } else {
		command.addArgument("up");
	    }

	    // now add destination
	    // command.addArgument(targetDir.getAbsolutePath());
	    if (user != null && !user.isEmpty()) {
		command.addArgument("--username");
		command.addArgument(user);
		if (!password.isEmpty()) {
		    command.addArgument("--password");
		    command.addArgument(password);
		}
	    }

	    command.addArgument("--non-interactive");

	    for (String svnFlag : svnFlagsList) {
		command.addArgument(svnFlag);
	    }

	    exitStatus = commandLineExecutor.executeCommand(log, command,
		    targetDir);

	} catch (Exception e) {
	    log.error("Unable to perform sync: " + e.getMessage());
	}
	return exitStatus;
    }
}
