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
package com.blackducksoftware.tools.scmconnector.integrations.serena;

import java.io.File;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.CommandLineExecutor;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ICommandLineExecutor;

/**
 * The Serena connector is presumed to need work before it is ready to use. This
 * class is here as a starting point for that work. The Serena connector is
 * currently not mentioned in the documentation.
 *
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 *
 **/

public class SerenaConnector extends Connector {

    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final ICommandLineExecutor commandLineExecutor;
    private String host;

    private String user;
    private String password;

    private String dbname;

    private String dsn;

    private String projectspec;

    private String executable;

    public SerenaConnector() {
	commandLineExecutor = new CommandLineExecutor();
    }

    public SerenaConnector(ICommandLineExecutor commandLineExecutor) {
	this.commandLineExecutor = commandLineExecutor;
    }

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	host = prps.getProperty("host");
	user = prps.getProperty("user");

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();
	dbname = prps.getProperty("dbname");
	dsn = prps.getProperty("dsn");
	projectspec = prps.getProperty("projectspec");
	executable = prps.getProperty("executable");

    }

    @Override
    public String getName() {
	return "Serena";
    }

    @Override
    public String getRepositoryPath() {
	return projectspec;
    }

    @Override
    public int sync() {
	CommandLine command = CommandLine.parse(executable);

	int exitStatus = 1;

	String[] arguments = { user, password, host, dbname, dsn, projectspec,
		(getFinalSourceDirectory()).replaceAll("/", "@/") };

	command.addArguments(arguments);

	try {
	    exitStatus = commandLineExecutor.executeCommand(log, command,
		    new File(getFinalSourceDirectory()));
	} catch (Exception e) {
	    log.error("Failure executing Serena Command", e);
	    return -1;
	}

	log.info("Exit Status=" + exitStatus);

	return exitStatus;
    }

}
