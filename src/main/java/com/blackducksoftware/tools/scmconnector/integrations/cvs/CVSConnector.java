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
package com.blackducksoftware.tools.scmconnector.integrations.cvs;

import java.util.Properties;

import javax.net.SocketFactory;

import org.apache.log4j.Logger;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

public class CVSConnector extends Connector {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private String server;
    private String port;
    private String repositoryPath;
    private String module;

    private String user;
    private String password;

    private String root;

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	server = prps.getProperty("server");
	port = prps.getProperty("port");
	repositoryPath = prps
		.getProperty(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);
	module = prps.getProperty("module");
	root = getFinalSourceDirectory();
	user = prps.getProperty("user");

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();

    }

    @Override
    public String getName() {

	return "CVS";
    }

    @Override
    public String getRepositoryPath() {

	return repositoryPath;
    }

    @Override
    public int sync() {

	CVSRoot cvsRoot = makeCVSRoot(":pserver:" + user + "@" + server
		+ repositoryPath);
	cvsRoot.setPort(Integer.parseInt(port));

	PServerConnection aConn;
	try {
	    log.info("Opening CVS Connection");
	    aConn = openCVSConnection(cvsRoot);
	} catch (CommandAbortedException e) {
	    log.error("Failure opening CVS Connection", e);
	    return 1;
	} catch (AuthenticationException e) {
	    log.error("Failure authenticating to the CVS Server", e);
	    return 1;
	}

	return cvsCheckOut(aConn, cvsRoot);

    }

    CVSRoot makeCVSRoot(String connectionString) {
	return CVSRoot.parse(connectionString);
    }

    PServerConnection makeConnection(CVSRoot cvsRoot,
	    SocketFactory socketFactory) {
	return new PServerConnection(cvsRoot, socketFactory);
    }

    Client makeClient(Connection aConn, AdminHandler adminHandler) {
	return new Client(aConn, adminHandler);
    }

    CheckoutCommand makeCheckoutCommand() {
	return new CheckoutCommand();
    }

    GlobalOptions makeGlobalOptions() {
	return new GlobalOptions();
    }

    private int cvsCheckOut(PServerConnection aConn, CVSRoot cvsRoot) {

	Client client = makeClient(aConn, new StandardAdminHandler());
	client.getEventManager().addCVSListener(new BasicListener());
	client.setLocalPath(root);

	CheckoutCommand command = makeCheckoutCommand();
	command.setRecursive(true);
	command.setModule(module);
	command.setPruneDirectories(true);
	GlobalOptions globalOptions = makeGlobalOptions();
	globalOptions.setCVSRoot(cvsRoot.toString());

	boolean success = false;

	try {
	    log.info("Checking out from CVS Connection");
	    success = client.executeCommand(command, globalOptions);
	} catch (CommandAbortedException e) {
	    log.error("Failure checking out from the CVS Server", e);
	    return 1;
	} catch (CommandException e) {
	    log.error("Failure checking out from the CVS Server", e);
	    return 1;
	} catch (AuthenticationException e) {
	    log.error("Failure checking out from the CVS Server", e);
	    return 1;
	}

	if (success) {
	    return 0;
	} else {
	    return 1;
	}
    }

    private PServerConnection openCVSConnection(CVSRoot cvsRoot)
	    throws CommandAbortedException, AuthenticationException {
	PServerConnection aConn = makeConnection(cvsRoot,
		SocketFactory.getDefault());

	aConn.setEncodedPassword(StandardScrambler.getInstance().scramble(
		password));

	aConn.open();

	return aConn;
    }

    class BasicListener extends CVSAdapter {

	/**
	 * Stores a tagged line
	 */
	private final StringBuffer taggedLine = new StringBuffer();

	/**
	 * Called when the server wants to send a message to be displayed to the
	 * user. The message is only for information purposes and clients can
	 * choose to ignore these messages if they wish.
	 *
	 * @param e
	 *            the event
	 */
	@Override
	public void messageSent(MessageEvent e) {
	    String line = e.getMessage();

	    if (e.isTagged()) {

		String message = MessageEvent.parseTaggedMessage(taggedLine,
			line);
		// if we get back a non-null line, we have something
		// to output. Otherwise, there is more to come and we
		// should do nothing yet.
		if (message != null) {
		    if (e.isError()) {
			log.error(message);
		    } else {
			log.info(message);
		    }
		}
	    } else {
		if (e.isError()) {
		    log.error(line);
		} else {
		    log.info(line);
		}
	    }
	}
    }
}
