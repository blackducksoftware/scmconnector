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
package com.blackducksoftware.tools.scmconnector.integrations.perforce;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.NoSuchObjectException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.exception.ResourceException;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import com.tek42.perforce.Depot;
import com.tek42.perforce.PerforceException;

/**
 * The perforce connector works a bit differently than others. It expects a
 * client name and then derives the necessary pathing from that client.
 *
 * If a client is not provided, the operation ends.
 *
 * Update: October 2013 This connector went through quite a few changes, please
 * see SVN history for more information.
 *
 * @author akamen
 *
 */
public class PerforceConnector extends Connector {

    private static final long serialVersionUID = 1681612939774624354L;

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private String root; // The path of root dir to which source will be checked
			 // out
    private String port;
    private List<String> viewMappingDepotSpecs = new ArrayList<String>(5);
    private String user;
    private String password;
    // Client will be used to lookup information from the p4 server.
    private String client;
    private String executable;
    private String charset;
    private String fingerPrint;

    private IServer perforceServer;
    private IClient perforceClient;

    private final String P4_PREFIX_STANDARD = "p4java://";
    private final String P4_PREFIX_SSL = "p4javassl://";

    @Override
    public void init(Properties prps) throws Exception {

	// First get the properties, then init

	// Port is actuallly the perforce server + port number combined
	port = prps.getProperty(ConnectorConstants.PERFORCE_PROPERTY_SERVER);
	client = prps.getProperty("client");
	user = prps.getProperty("user");
	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();
	executable = prps.getProperty("executable");
	charset = prps.getProperty("charset");
	fingerPrint = prps.getProperty("fingerprint");

	// Set up the init now.
	prps = initPerforce(prps);
	super.init(prps);

    }

    /**
     * Perforce initialization will determine the root and needs to be run first
     * thing.
     *
     * @param prps
     * @return
     * @throws Exception
     */
    private Properties initPerforce(Properties prps) throws Exception {
	try {

	    // initAdapter(prps);

	    perforceServer = findServer();
	    perforceClient = findClient(perforceServer);
	    prps = getClientInformation(prps);

	} catch (Exception e) {
	    if (perforceServer != null) {
		perforceServer.disconnect();
	    }

	    throw new Exception("Aborting Perforce sync: " + e.getMessage());
	}

	return prps;
    }

    @Override
    public int sync() {

	Depot depot = makeDepot();
	depot.setPort(port);
	depot.setUser(user);
	depot.setPassword(password);
	depot.setClient(client);

	if (charset != null) {
	    depot.setCharset(charset);
	}

	if (executable != null) {
	    depot.setExecutable(executable);
	}

	boolean isValid = false;

	try {
	    isValid = depot.getStatus().isValid();

	} catch (PerforceException e2) {
	    log.error("Connection to Perforce server is not valid", e2);
	    return 1;
	}
	log.info("Perforce connection validity: " + isValid);

	if (isValid == false) {
	    return 1;
	}

	try {

	    log.info("Performing sync of perforce workspace: " + client);

	    for (String view : viewMappingDepotSpecs) {
		StringBuilder builder = depot.getWorkspaces().syncToHead(view,
			true); // Set force to true;
		log.info("response from sync operation: " + builder.toString());
	    }

	    try {
		perforceServer.disconnect();
	    } catch (Exception ignore) {
	    }
	} catch (PerforceException e) {

	    log.error("Error performing sync operation", e);
	    return 1;
	}

	return 0;

    }

    Depot makeDepot() {
	return new Depot();
    }

    private IServer findServer() throws Exception {
	log.info("Connecting to Perforce server to gather settings....");
	IOptionsServer perforceServer = null;
	try {

	    String portAndPrefix = findPrefix();
	    perforceServer = makeServer(portAndPrefix);

	    if (fingerPrint != null) {
		perforceServer.addTrust(fingerPrint);
	    }

	    perforceServer.connect();
	    perforceServer.setUserName(user);

	    if (password != null && password.length() > 0) {
		perforceServer.login(password);
	    }

	} catch (P4JavaException p4e) {
	    throw new Exception("Unable to connect: " + p4e.getMessage());
	} catch (Exception e) {
	    throw new Exception("Unable to connect: " + e.getMessage());
	}

	return perforceServer;
    }

    IOptionsServer makeServer(String portAndPrefix) throws ConnectionException,
	    NoSuchObjectException, ConfigException, ResourceException,
	    URISyntaxException {
	return ServerFactory.getOptionsServer(portAndPrefix, null);
    }

    /**
     * Determines the prefix and returns a fully resolved prefix + port
     *
     * @return
     */
    private String findPrefix() {
	String portAndPrefix = "";
	String[] portParts = port.split(":");

	if (portParts.length == 3) {
	    String prefix = portParts[0];
	    if (P4_PREFIX_SSL.contains(prefix)) {
		// Found ssl request
		// Strip out the prefix (it is indexed at 0, so +1 the length of
		// it)
		String remainderPort = port.substring(prefix.length() + 1,
			port.length());
		portAndPrefix = P4_PREFIX_SSL + remainderPort;
	    }
	} else {
	    portAndPrefix = P4_PREFIX_STANDARD + port;
	}

	log.info("Fully resolved p4 java address: " + portAndPrefix);
	return portAndPrefix;
    }

    private IClient findClient(IServer perforceServer) throws Exception {
	IClient perforceClient = null;
	try {
	    perforceClient = makeClient(client);
	    if (perforceClient == null) {
		throw new Exception("No client exists with name: " + client);
	    }
	} catch (Exception e) {
	    log.error("Unable to get client: " + e.getMessage());
	    displayListOfClients(perforceServer);
	    throw new Exception("Unable to get client: " + e.getMessage());
	}
	return perforceClient;
    }

    IClient makeClient(String client) throws ConnectionException,
	    RequestException, AccessException {
	return perforceServer.getClient(client);
    }

    /**
     * Gets the root and view from the client and loads it into the property
     * file The property file will then be used to setup the root pathing.
     *
     * @param prps
     * @return
     */
    private Properties getClientInformation(Properties prps) {
	try {
	    root = perforceClient.getRoot();

	    log.info("Obtained root and loading into properties: " + root);

	    prps.put(ConnectorConstants.CONNECTOR_PROPERTY_ROOT, root);

	    ClientView clientView = perforceClient.getClientView();

	    int numViewEntries = clientView.getSize();
	    for (int i = 0; i < numViewEntries; i++) {
		IClientViewMapping mapping = clientView.getEntry(i);

		String viewMappingDepotSpec = mapping.getDepotSpec();
		log.info("Obtained view mapping: " + viewMappingDepotSpec);

		viewMappingDepotSpecs.add(viewMappingDepotSpec);
	    }

	} catch (Exception e) {
	    log.error("Unable to get perforce information" + e.getMessage());
	}

	return prps;
    }

    private void displayListOfClients(IServer perforceServer) {
	try {
	    List<IClientSummary> clients = perforceServer.getClients(user, "*",
		    20);
	    log.info("Partial list of available clients: ");
	    for (IClientSummary sum : clients) {
		log.info("Client: " + sum.getName());
	    }
	} catch (Exception e) {
	    // ignore
	}

    }

    @Override
    public String getName() {
	return "Perforce";
    }

    @Override
    public String getRepositoryPath() {
	return root;
    }
}
