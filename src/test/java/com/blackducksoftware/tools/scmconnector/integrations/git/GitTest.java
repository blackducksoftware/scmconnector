package com.blackducksoftware.tools.scmconnector.integrations.git;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.MockCommandLineExecutor;

public class GitTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testBasic() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	GitConnector gitConnector = new GitConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/root");
	props.setProperty("repositoryURL", "https://github.com/testrepo/test");
	gitConnector.init(props);
	gitConnector.sync();

	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + "root"));
	assertTrue(mockExec.getLastCommandString().startsWith(
		"git clone https://github.com/testrepo/test "));
	assertTrue(mockExec.getLastCommandString().endsWith(
		"" + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator
			+ "root" + File.separator + "test"));
    }

    @Test
    public void testWithUsername() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	GitConnector gitConnector = new GitConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/root");
	props.setProperty("repositoryURL", "https://github.com/testrepo/test");
	props.setProperty("user", "testusername");
	gitConnector.init(props);
	gitConnector.sync();

	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + "root"));
	assertTrue(mockExec.getLastCommandString().startsWith(
		"git clone https://testusername@github.com/testrepo/test "));
	assertTrue(mockExec.getLastCommandString().endsWith(
		"" + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator
			+ "root" + File.separator + "test"));
    }

    @Test
    public void testWithUsernamePassword() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	GitConnector gitConnector = new GitConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/root");
	props.setProperty("repositoryURL", "https://github.com/testrepo/test");
	props.setProperty("user", "testusername");
	props.setProperty("password", "testpassword");
	gitConnector.init(props);
	gitConnector.sync();

	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + "root"));
	assertTrue(mockExec
		.getLastCommandString()
		.startsWith(
			"git clone https://testusername:testpassword@github.com/testrepo/test "));
	assertTrue(mockExec.getLastCommandString().endsWith(
		"" + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator
			+ "root" + File.separator + "test"));
    }
}
