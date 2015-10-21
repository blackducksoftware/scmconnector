package com.blackducksoftware.tools.scmconnector.integrations.serena;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.MockCommandLineExecutor;

/**
 * The Serena connector is presumed to need work before it is ready to use, so
 * presumably this test will need work too.
 *
 * @author sbillings
 *
 */
public class SerenaTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws Exception {
	MockCommandLineExecutor mockExec = new MockCommandLineExecutor();
	SerenaConnector connector = new SerenaConnector(mockExec);
	Properties props = new Properties();
	props.setProperty("root", "src/test/resources/root");
	props.setProperty("repositoryURL", "testUrl");
	props.setProperty("executable", "/bin/serena");

	props.setProperty("host", "testHost");
	props.setProperty("dbname", "testDbName");
	props.setProperty("projectspec", "testProjectSpec");

	props.setProperty("user", "testuser");
	props.setProperty("password", "testpassword");
	connector.init(props);
	connector.sync();

	System.out.println(mockExec.toString());

	assertTrue(mockExec.getLastTargetDirPath().endsWith(
		"src" + File.separator + "test" + File.separator + "resources"
			+ File.separator + "root"));
	assertTrue(mockExec
		.getLastCommandString()
		.startsWith(
			File.separator
				+ "bin"
				+ File.separator
				+ "serena testuser testpassword testHost testDbName testProjectSpec "));
	assertTrue(mockExec.getLastCommandString().endsWith("root"));
    }

}
