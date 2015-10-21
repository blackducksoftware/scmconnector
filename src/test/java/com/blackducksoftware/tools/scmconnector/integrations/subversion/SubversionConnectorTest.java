package com.blackducksoftware.tools.scmconnector.integrations.subversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.CommandResults;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.MockCommandLineExecutor;

public class SubversionConnectorTest {

    private static final String ROOT_DIR_NAME = "root";
    private static final String SVN_CHECKOUT_CMD = "svn co";
    private static final String TEST_REPO_URL = "testRepoUrl";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_USER = "testuser";
    private static final String CMD_LINE_USERNAME_PASSWORD = "--username "
	    + TEST_USER + " " + "--password" + " " + TEST_PASSWORD;
    private static final String FLAGS_PROPERTY_VALUE = "flag1,flag2";
    private static final String CMD_LINE_FLAGS = "--non-interactive flag1 flag2";
    private static final String SVN_UPDATE_CMD = "svn up";
    private static final String SVN_STATUS_CMD = "svn status";

    /**
     * This mock command executor will fail the first time a command is
     * executed, and succeed thereafter. This mimicks
     * "the repo does not exist locally" scenario.
     *
     * @author sbillings
     *
     */
    private class FirstCommandFailsCommandLineExecutor extends
	    MockCommandLineExecutor {
	private int executeCount = 0;

	@Override
	public int executeCommand(Logger log, CommandLine command,
		File targetDir) throws Exception {
	    setLastLog(log);
	    addCommand(command);
	    setLastTargetDir(targetDir);

	    if (executeCount++ == 0) {
		return 1;
	    } else {
		return 0;
	    }
	}

	@Override
	public int executeCommand(Logger log, CommandLine command,
		File targetDir, String promptResponse) throws Exception {
	    setLastLog(log);
	    addCommand(command);
	    setLastTargetDir(targetDir);

	    if (executeCount++ == 0) {
		return 1;
	    } else {
		return 0;
	    }
	}

	@Override
	public CommandResults executeCommandForOutput(Logger log,
		CommandLine command, File targetDir) throws Exception {
	    CommandResults commandResults;
	    setLastLog(log);
	    addCommand(command);
	    setLastTargetDir(targetDir);

	    System.out.println("Mock Executing: " + command + " in "
		    + targetDir.getAbsolutePath());

	    if (executeCount++ == 0) {
		commandResults = new CommandResults(1, "failed");
		return commandResults;
	    } else {
		commandResults = new CommandResults(0, "succeeded");
		return commandResults;
	    }
	}

    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testUpdate() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	Connector connector = new SubversionConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/" + ROOT_DIR_NAME);
	props.setProperty("repositoryURL", TEST_REPO_URL);
	props.setProperty("user", TEST_USER);
	props.setProperty("flags", FLAGS_PROPERTY_VALUE);
	props.setProperty("password", TEST_PASSWORD);

	connector.init(props);
	connector.sync();

	// Verify the sequence of tfs commands that should get executed
	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + ROOT_DIR_NAME));

	assertEquals(2, mockExec.size());
	assertEquals(SVN_STATUS_CMD, mockExec.getCommandString(0));
	assertEquals(SVN_UPDATE_CMD + " " + CMD_LINE_USERNAME_PASSWORD + " "
		+ CMD_LINE_FLAGS, mockExec.getCommandString(1));

    }

    @Test
    public void testInitialCheckout() throws Exception {
	// Using this special mock command exec, the "does this
	// repo already exist locally" command (the first command) will fail,
	// simulating an initial checkout scenario.
	MockCommandLineExecutor mockExec = new FirstCommandFailsCommandLineExecutor();
	Connector connector = new SubversionConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/" + ROOT_DIR_NAME);
	props.setProperty("repositoryURL", TEST_REPO_URL);
	props.setProperty("user", TEST_USER);
	props.setProperty("flags", FLAGS_PROPERTY_VALUE);
	props.setProperty("password", TEST_PASSWORD);

	connector.init(props);
	connector.sync();

	// Verify the sequence of tfs commands that should get executed
	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + ROOT_DIR_NAME));

	assertEquals(2, mockExec.size());
	assertEquals(SVN_STATUS_CMD, mockExec.getCommandString(0));
	assertEquals(SVN_CHECKOUT_CMD + " " + TEST_REPO_URL + " "
		+ CMD_LINE_USERNAME_PASSWORD + " " + CMD_LINE_FLAGS,
		mockExec.getCommandString(1));

    }

}
