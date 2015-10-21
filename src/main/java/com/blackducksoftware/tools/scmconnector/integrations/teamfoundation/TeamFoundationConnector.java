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
package com.blackducksoftware.tools.scmconnector.integrations.teamfoundation;

import java.io.File;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.CommandLineExecutor;
import com.blackducksoftware.tools.scmconnector.core.CommandResults;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ICommandLineExecutor;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

public class TeamFoundationConnector extends Connector {

    private static final long serialVersionUID = 1L;

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final ICommandLineExecutor commandLineExecutor;

    private String server;
    private String collection;
    private String view;
    private String user;
    private String password;
    private String workspace;
    private String executable;

    private boolean workspaceExists = false;

    public TeamFoundationConnector() {
	commandLineExecutor = new CommandLineExecutor();
    }

    public TeamFoundationConnector(ICommandLineExecutor commandLineExecutor) {
	this.commandLineExecutor = commandLineExecutor;
    }

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	server = prps.getProperty("server");
	collection = prps.getProperty("collection");
	view = prps.getProperty("view");
	workspace = prps.getProperty("workspace");

	executable = prps.getProperty("executable");
	user = prps.getProperty("user");

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();
    }

    @Override
    public String getName() {

	return "Team Foundation Server 2010";
    }

    @Override
    public String getRepositoryPath() {

	return server + "/" + collection + view;
    }

    @Override
    public int sync() {

	workspaceExists();

	if (!workspaceExists) {

	    if (createWorkpace() != 0) {
		return 1;
	    }

	    if (mapWorkpace() != 0) {
		return 1;
	    }
	}

	CommandLine command = CommandLine.parse(executable);

	String[] arguments = new String[] { "get", "-recursive",
		"-login:" + user + "," + password, getFinalSourceDirectory() };
	command.addArguments(arguments, false);

	int exitStatus = 1;

	try {
	    exitStatus = commandLineExecutor.executeCommand(log, command,
		    new File(getFinalSourceDirectory()));
	} catch (Exception e) {
	    log.error("Failure executing TF Command", e);
	}

	return exitStatus;

    }

    private int createWorkpace() {
	CommandLine command = CommandLine.parse(executable);

	String[] arguments = new String[] { "workspace", "-new",
		"-collection:" + server + "/" + collection,
		"-login:" + user + "," + password, workspace };

	command.addArguments(arguments, false);

	int exitStatus = 1;

	try {
	    exitStatus = commandLineExecutor.executeCommand(log, command,
		    new File(getFinalSourceDirectory()));

	} catch (Exception e) {

	    log.error("Failure executing TF Command", e);
	}

	return exitStatus;
    }

    private int mapWorkpace() {
	CommandLine command = CommandLine.parse(executable);

	command.addArgument("workfold", false);
	command.addArgument("-map", false);
	command.addArgument("-workspace:" + workspace, false);
	command.addArgument("-login:" + user + "," + password, false);
	command.addArgument("$" + view, false); // Don't mess with quoting
	command.addArgument(getFinalSourceDirectory(), false);

	int exitStatus = 1;

	try {
	    exitStatus = commandLineExecutor.executeCommand(log, command,
		    new File(getFinalSourceDirectory()));

	} catch (Exception e) {

	    log.error("Failure executing TF Command", e);
	}
	return exitStatus;

    }

    private int workspaceExists() {
	CommandLine command = CommandLine.parse(executable);

	command.addArgument("workspaces", false);
	command.addArgument("-server:" + server + "/" + collection, false);
	command.addArgument("-login:" + user + "," + password, false);

	int commandReturnStatus = 1;
	String commandOutput = null;

	try {
	    CommandResults results = commandLineExecutor
		    .executeCommandForOutput(log, command, new File(
			    getFinalSourceDirectory()));
	    commandReturnStatus = results.getStatus();
	    commandOutput = results.getOutput();

	    if (commandOutput.contains(workspace)) {
		workspaceExists = true;
		log.info("The workspace exists");
	    } else {
		workspaceExists = false;
		log.info("The workspace does not exist");
	    }

	} catch (Exception e) {

	    log.error("Failure executing TF Command: " + command.toString()
		    + "; output: " + commandOutput);
	    workspaceExists = false;
	}

	return commandReturnStatus;
    }
}
