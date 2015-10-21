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
package com.blackducksoftware.tools.scmconnector.integrations.mercurial;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.CommandLineExecutor;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;
import com.blackducksoftware.tools.scmconnector.core.ConnectorPropertyRetriever;
import com.blackducksoftware.tools.scmconnector.core.ICommandLineExecutor;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

public class MercurialConnector extends Connector {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private final ICommandLineExecutor commandLineExecutor;
    private static final String EXECUTABLE = "hg";

    private String repositoryURL;

    private String user;
    private String password;

    public MercurialConnector() {
	commandLineExecutor = new CommandLineExecutor();
    }

    public MercurialConnector(ICommandLineExecutor commandLineExecutor) {
	this.commandLineExecutor = commandLineExecutor;
    }

    @Override
    public void init(Properties prps) throws Exception {
	super.init(prps);

	repositoryURL = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);
	user = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_USER);

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(ConnectorPropertyRetriever
			.getProperties());
	password = psw.getPlainText();

	buildURL();

    }

    /**
     * Constructs a valid HTTP url if a user *and* password is provided.
     *
     * @throws Exception
     */
    private void buildURL() throws Exception {
	if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
	    URL url = new URL(repositoryURL);
	    String protocol = url.getProtocol();
	    int port = url.getPort();
	    String host = url.getHost();
	    String path = url.getPath();

	    URIBuilder builder = new URIBuilder();
	    builder.setScheme(protocol);
	    builder.setHost(host);
	    builder.setPort(port);
	    builder.setPath(path);
	    builder.setUserInfo(user, password);

	    repositoryURL = builder.toString();
	    // log.info("Using path: " + repositoryURL); // Reveals password
	}
    }

    @Override
    public String getName() {

	return "Mercurial";
    }

    @Override
    public String getRepositoryPath() {

	return repositoryURL;
    }

    @Override
    public int sync() {

	int exitStatus = -1;

	CommandLine command = CommandLine.parse(EXECUTABLE);

	String sourceDirIncludingModule = addPathComponentToPath(
		getFinalSourceDirectory(), getModuleNameFromURL(repositoryURL));
	File dir = new File(sourceDirIncludingModule);

	if (localWorkingCopyExists(dir, ".hg",
		getModuleNameFromURL(repositoryURL))) {

	    command.addArgument("pull");
	    command.addArgument("-u");

	} else {
	    // Need to adjust the working dir for Mercury checkout: must not
	    // include the module dir part
	    sourceDirIncludingModule = getFinalSourceDirectory();
	    dir = new File(sourceDirIncludingModule);

	    command.addArgument("clone");
	    command.addArgument(repositoryURL);

	}

	try {
	    exitStatus = commandLineExecutor.executeCommand(log, command, dir);
	} catch (Exception e) {
	    log.error("Problem performing command", e);
	}

	return exitStatus;
    }
}
