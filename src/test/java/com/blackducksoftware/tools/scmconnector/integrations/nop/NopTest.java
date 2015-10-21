package com.blackducksoftware.tools.scmconnector.integrations.nop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.AbstractTest;
import com.blackducksoftware.tools.scmconnector.core.ConnectorRunner;
import com.blackducksoftware.tools.scmconnector.core.MockProtexRunner;
import com.blackducksoftware.tools.scmconnector.core.TestFixture;

/**
 * Test the NOP (no operation = does nothing) Connector.
 *
 * @author sbillings
 *
 */
public class NopTest extends AbstractTest {
    private static final String PROTEX_PASSWORD = "testpassword";
    private static final String PROTEX_USERNAME = "testuser@blackducksoftware.com";
    private static final String SRC_DIR = "src/test/resources/filesysrepo";
    private static final String SERVER_NAME = "server.domain.com";
    private static final String SERVER_PROTOCOL = "https";
    private static final String PROTEX_URL = SERVER_PROTOCOL + "://"
	    + SERVER_NAME + "/";
    private static final String PROJECT_NAME1 = "testproject";
    private static final int RETURN_STATUS = 123;

    @BeforeClass
    public static void setUp2() throws Exception {
    }

    @AfterClass
    public static void cleanUp() throws Exception {

    }

    /**
     * Test NOP connector. Verify that it sees the files already in the root
     * location.
     */
    @Test
    public void test() throws Exception {
	Properties props = createBasicConfig(PROJECT_NAME1);

	TestFixture testFixture = doTestNopConnector(props, new File(SRC_DIR),
		true);

	// Verify that the ProtexRunner was called
	MockProtexRunner mockProtexRunner = testFixture.getProtexRunner();
	assertEquals(1, mockProtexRunner.getRunCount());
	assertEquals(1, mockProtexRunner.getScanCount());
	assertEquals(PROJECT_NAME1, mockProtexRunner.getLastProjectName());

	assertEquals("Expected file count after checkout", 3,
		countFiles(testFixture.getLocalSourceDir()));

	// Pick a random file and make sure it's there
	File testFile = getTestFileNOP(testFixture.getLocalSourceDir()
		.getAbsolutePath());
	assertTrue(testFile.exists());
	assertEquals(15, testFile.length());

	// Write results to a properties file
	String resultsFilePath = testFixture.getResultsDir().getAbsolutePath()
		+ File.separator + "results.properties";
	ConnectorRunner.writeResults(testFixture.getConnectorRunner(),
		resultsFilePath, RETURN_STATUS);

	// Read and verify results from properties file
	Properties resultsProps = new Properties();
	InputStream resultsInputStream = new FileInputStream(resultsFilePath);
	resultsProps.load(resultsInputStream);

	dumpProperties(resultsProps);
	assertEquals(RETURN_STATUS,
		Integer.parseInt(resultsProps.getProperty("results.status")));
    }

    private Properties createBasicConfig(String projectName) {
	Properties props = new Properties();
	props.setProperty("protex.server.name", PROTEX_URL);
	props.setProperty("protex.user.name", PROTEX_USERNAME);
	props.setProperty("protex.password", PROTEX_PASSWORD);
	props.setProperty("protex.password.isencrypted", "false");
	props.setProperty("protex.run_rapidid", "false");
	props.setProperty("protex.home_directory", "/home/blackduck");
	props.setProperty("total_connectors", "1");
	props.setProperty("connector.0.class",
		"com.blackducksoftware.tools.scmconnector.integrations.nop.NOPConnector");
	props.setProperty("connector.0.new_project_name", projectName);

	return props;
    }
}
