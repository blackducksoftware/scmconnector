package com.blackducksoftware.tools.scmconnector.integrations.teamfoundation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.MockCommandLineExecutor;

public class TfsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testWithUsernamePassword() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	Connector connector = new TeamFoundationConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/root");
	props.setProperty("repositoryURL", "testRepoUrl");
	props.setProperty("server", "testServer");
	props.setProperty("collection", "testCollection");
	props.setProperty("view", "testView");
	props.setProperty("workspace", "testWorkspace");
	props.setProperty("executable", "/bin/tfs");
	props.setProperty("user", "testuser");
	props.setProperty("password", "testpassword");
	connector.init(props);
	connector.sync();

	// Verify the sequence of tfs commands that should get executed
	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + "root"));

	assertEquals(
		""
			+ File.separator
			+ "bin"
			+ File.separator
			+ "tfs workspaces -server:testServer/testCollection -login:testuser,testpassword",
		mockExec.getCommandString(0));
	assertEquals(
		""
			+ File.separator
			+ "bin"
			+ File.separator
			+ "tfs workspace -new -collection:testServer/testCollection -login:testuser,testpassword testWorkspace",
		mockExec.getCommandString(1));
	assertTrue(mockExec
		.getCommandString(2)
		.startsWith(
			""
				+ File.separator
				+ "bin"
				+ File.separator
				+ "tfs workfold -map -workspace:testWorkspace -login:testuser,testpassword $testView "));
	assertTrue(mockExec.getCommandString(2).endsWith(
		"" + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator
			+ "root"));

	assertTrue(mockExec.getCommandString(3).startsWith(
		"" + File.separator + "bin" + File.separator
			+ "tfs get -recursive -login:testuser,testpassword "));
	assertTrue(mockExec.getCommandString(3).endsWith(
		"" + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator
			+ "root"));
    }

}
