package com.blackducksoftware.tools.scmconnector.integrations.perforce;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.tek42.perforce.Depot;
import com.tek42.perforce.parse.Status;
import com.tek42.perforce.parse.Workspaces;

public class PerforceConnectorTest {
    private static final String TEST_FINGERPRINT = "testFingerPrint";
    private static final String TEST_DEPOT_SPEC = "testDepotSpec";
    private static final String TEST_EXECUTABLE = "testExecutable";
    private static final String TEST_CHARSET = "testCharset";
    private static final String P4PASSWORD = "blackduck";
    private static final String PERFORCE_WORKSPACE_NAME = "testWorkspaceName";
    private static final String P4PORT = "scm-win:1666";
    private static final String P4USER = P4PASSWORD;
    private static final String TEST_CONNECTOR_ROOT = "src/test/resources/testConnectorSourceRoot";

    private PerforceConnector perforceConnector = spy(new PerforceConnector());
    private Depot mockDepot = mock(Depot.class);
    private Status mockDepotStatus = mock(Status.class);
    private Workspaces mockWorkspaces = mock(Workspaces.class);
    private Server mockServer = mock(Server.class);
    private Client mockClient = mock(Client.class);
    private ClientView mockClientView = mock(ClientView.class);
    private ClientViewMapping mockClientViewMapping = mock(ClientViewMapping.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws Exception {

	// Mock the "make" methods that construct the Perforce library objects:
	// Depot, Server, Client, Workspaces
	doReturn(mockDepot).when(perforceConnector).makeDepot();
	doReturn(mockServer).when(perforceConnector).makeServer(
		"p4java://" + P4PORT);
	doReturn(mockClient).when(perforceConnector).makeClient(
		PERFORCE_WORKSPACE_NAME);

	when(mockDepot.getWorkspaces()).thenReturn(mockWorkspaces);
	when(mockDepot.getStatus()).thenReturn(mockDepotStatus);
	when(mockDepotStatus.isValid()).thenReturn(true);
	when(mockClient.getRoot()).thenReturn(
		"src/test/resources/rootFromClient");
	when(mockClient.getClientView()).thenReturn(mockClientView);
	when(mockClientView.getSize()).thenReturn(1);
	when(mockClientView.getEntry(0)).thenReturn(mockClientViewMapping);
	when(mockClientViewMapping.getDepotSpec()).thenReturn(TEST_DEPOT_SPEC);
	when(mockWorkspaces.syncToHead(TEST_DEPOT_SPEC, true)).thenReturn(
		new StringBuilder("mocked sync succeeded"));

	// Set up properties for the PerforceConnector (the code under test)
	Properties props = new Properties();
	props.setProperty("port", P4PORT);
	props.setProperty("client", PERFORCE_WORKSPACE_NAME);
	props.setProperty("user", P4USER);
	props.setProperty("password", P4PASSWORD);
	props.setProperty("password.isencrypted", "false");
	props.setProperty("new_project_name", "testProject");
	props.setProperty("root", TEST_CONNECTOR_ROOT);

	props.setProperty("charset", TEST_CHARSET);
	props.setProperty("executable", TEST_EXECUTABLE);
	props.setProperty("fingerprint", TEST_FINGERPRINT);

	// Exercise PerforceConnector
	perforceConnector.init(props);
	perforceConnector.sync();

	verify(mockDepot).setUser(P4USER);
	verify(mockDepot).setPassword(P4PASSWORD);
	verify(mockDepot).setClient(PERFORCE_WORKSPACE_NAME);
	verify(mockDepot).setCharset(TEST_CHARSET);
	verify(mockDepot).setExecutable(TEST_EXECUTABLE);

	// verify server connect, setUser, login
	verify(mockServer).connect();
	verify(mockServer).setUserName(P4USER);
	verify(mockServer).login(P4PASSWORD);
	verify(mockServer).addTrust(TEST_FINGERPRINT);

	verify(mockWorkspaces).syncToHead(TEST_DEPOT_SPEC, true);
	verify(mockServer).disconnect();
    }

}
