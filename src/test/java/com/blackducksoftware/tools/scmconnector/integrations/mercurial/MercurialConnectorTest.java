package com.blackducksoftware.tools.scmconnector.integrations.mercurial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.MockCommandLineExecutor;

public class MercurialConnectorTest {

    private static final String ROOT_DIR_NAME = "root";
    private static final String MODULE_NAME = "SampleFiles";
    private static final String SERVER_PORT_MODULEPATH = "mamba-vm:8000/usr/src/hg/repos/"
	    + MODULE_NAME;
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_USER = "testuser";
    private static final String HG_CLONE_COMMAND = "hg clone";
    private static final String HG_UPDATE_COMMAND = "hg pull -u";
    private static final String URL_PROTOCOL_PREFIX = "http://";
    private static final String TEST_REPO_URL = URL_PROTOCOL_PREFIX
	    + SERVER_PORT_MODULEPATH;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testCheckout() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	Connector connector = new MercurialConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/" + ROOT_DIR_NAME);
	props.setProperty("repositoryURL", TEST_REPO_URL);
	props.setProperty("user", TEST_USER);
	props.setProperty("password", TEST_PASSWORD);

	connector.init(props);
	connector.sync();

	// Verify the sequence of tfs commands that should get executed
	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + ROOT_DIR_NAME));

	assertEquals(1, mockExec.size());
	assertEquals(HG_CLONE_COMMAND + " " + URL_PROTOCOL_PREFIX + TEST_USER
		+ ":" + TEST_PASSWORD + "@" + SERVER_PORT_MODULEPATH,
		mockExec.getCommandString(0));

    }

    @Test
    public void testUpdate() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	Connector connector = new MercurialConnector(mockExec);

	File rootDir = getTempFolder();

	File moduleDir = new File(rootDir.getAbsoluteFile() + File.separator
		+ MODULE_NAME);
	boolean created = moduleDir.mkdir();

	File hgDir = new File(moduleDir.getAbsoluteFile() + File.separator
		+ ".hg");
	created = hgDir.mkdir();

	Properties props = new Properties();
	props.setProperty("root", rootDir.getAbsolutePath());
	props.setProperty("repositoryURL", TEST_REPO_URL);
	props.setProperty("user", TEST_USER);
	props.setProperty("password", TEST_PASSWORD);

	connector.init(props);
	connector.sync();

	// Verify the sequence of tfs commands that should get executed
	assertEquals(1, mockExec.size());
	assertEquals(HG_UPDATE_COMMAND, mockExec.getCommandString(0));

    }

    protected static File getTempFolder() {
	File temp = null;

	try {
	    temp = File.createTempFile("scm_hg_test",
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

}
