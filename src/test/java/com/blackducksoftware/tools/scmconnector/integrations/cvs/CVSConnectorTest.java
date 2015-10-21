package com.blackducksoftware.tools.scmconnector.integrations.cvs;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.net.SocketFactory;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.commandLine.BasicListener;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.event.EventManager;

public class CVSConnectorTest {

    private static final String ROOT_LEAF = "testConnectorSourceRoot";
    private static final String ROOT_BASE = "src/test/resources/";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_USER = "testUser";
    private static final String TEST_MODULE = "testModule";
    private static final String TEST_URL = "testUrl";
    private static final String TEST_SERVER = "testServer";
    private static final int TEST_PORT = 123;
    private static final String TEST_CONNECTOR_ROOT = ROOT_BASE + ROOT_LEAF;
    private static final String CVS_ROOT = ":pserver:" + TEST_USER + "@"
	    + TEST_SERVER + TEST_URL;

    private EventManager mockEventManager = mock(EventManager.class);
    private CVSRoot mockCVSRoot = mock(CVSRoot.class);
    private CheckoutCommand mockCheckoutCommand = mock(CheckoutCommand.class);
    private Client mockClient = mock(Client.class);
    private PServerConnection mockConnection = mock(PServerConnection.class);
    private GlobalOptions mockGlobalOptions = mock(GlobalOptions.class);
    private CVSConnector cvsConnector = spy(new CVSConnector());

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws Exception {

	// Mock the "make" methods that construct the CVS library objects:
	// CVSRoot, Connection, Client, etc.
	doReturn(mockCVSRoot).when(cvsConnector).makeCVSRoot(CVS_ROOT);
	when(mockCVSRoot.toString()).thenReturn(CVS_ROOT);
	doReturn(mockConnection).when(cvsConnector).makeConnection(
		any(CVSRoot.class), any(SocketFactory.class));
	doReturn(mockClient).when(cvsConnector).makeClient(
		any(Connection.class), any(AdminHandler.class));
	doReturn(mockCheckoutCommand).when(cvsConnector).makeCheckoutCommand();
	doReturn(mockGlobalOptions).when(cvsConnector).makeGlobalOptions();
	when(mockClient.getEventManager()).thenReturn(mockEventManager);

	// Set up properties for the CVSConnector (the code under test)
	Properties props = new Properties();
	props.setProperty("server", TEST_SERVER);
	props.setProperty("port", String.valueOf(TEST_PORT));
	props.setProperty("repositoryURL", TEST_URL);
	props.setProperty("module", TEST_MODULE);
	props.setProperty("user", TEST_USER);
	props.setProperty("password", TEST_PASSWORD);
	props.setProperty("root", TEST_CONNECTOR_ROOT);

	// Exercise CVSConnector
	cvsConnector.init(props);
	cvsConnector.sync();

	// Verify that all the right calls to the CVS library were made

	verify(mockCVSRoot).setPort(TEST_PORT);
	verify(mockEventManager).addCVSListener(any(BasicListener.class));
	verify(mockClient).setLocalPath(Matchers.endsWith(ROOT_LEAF));
	verify(mockCheckoutCommand).setRecursive(true);
	verify(mockCheckoutCommand).setModule(TEST_MODULE);
	verify(mockCheckoutCommand).setPruneDirectories(true);
	verify(mockGlobalOptions).setCVSRoot(CVS_ROOT);
	verify(mockClient).executeCommand(mockCheckoutCommand,
		mockGlobalOptions);
    }
}
