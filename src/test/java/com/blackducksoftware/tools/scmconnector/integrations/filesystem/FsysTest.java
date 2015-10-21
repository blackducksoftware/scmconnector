package com.blackducksoftware.tools.scmconnector.integrations.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Properties;

import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.AbstractTest;
import com.blackducksoftware.tools.scmconnector.core.TestFixture;

public class FsysTest extends AbstractTest {

    /**
     * Test file system connector. Checkout and verify (check the file count,
     * and the size of one of the files). Remove the local files. Update, and
     * verify that the removed files were restored.
     */
    @Test
    public void testFsysRemote() throws Exception {

	TestFixture testFixture = doTest("config_fsys.properties");
	assertEquals("Expected file count after checkout", 3,
		countFiles(testFixture.getLocalSourceDir()));

	// Pick a random file and make sure it's there
	File testFile = getTestFileFsysRepo(testFixture.getLocalSourceDir()
		.getAbsolutePath());
	assertTrue(testFile.exists());
	assertEquals(15, testFile.length());

	// Delete all files locally; update should bring 'em all back
	purgeDir(testFixture.getLocalSourceDir());
	assertEquals("Expected file count after purge", 0,
		countFiles(testFixture.getLocalSourceDir()));

	// Second time: update: should bring deleted files back
	testFixture.getConnectorRunner().processConnectors();

	// The files should have been restored from SCM
	assertEquals("Expected file count after update", 3,
		countFiles(testFixture.getLocalSourceDir()));

	// Check that specific file again too
	assertTrue(testFile.exists());
	assertEquals(15, testFile.length());
    }

    /**
     * Test file system connector. Checkout and verify (check the file count,
     * and the size of one of the files). Remove the local files. Update, and
     * verify that the removed files were restored. This variant passes an input
     * stream that has the config rather than the path to a config file.
     */
    @Test
    public void testFsysRemoteProps() throws Exception {
	Properties props = new Properties();
	props.setProperty("protex.server.name", "https://server.domain.com/");
	props.setProperty("protex.user.name", "user@domain.com");
	props.setProperty("protex.password", "testpassword");
	props.setProperty("protex.password.isencrypted", "false");
	props.setProperty("protex.run_rapidid", "false");

	props.setProperty("total_connectors", "1");
	props.setProperty(
		"connector.0.class",
		"com.blackducksoftware.tools.scmconnector.integrations.filesystem.FileSystemConnector");
	props.setProperty("connector.0.repositoryURL",
		"src/test/resources/filesysrepo");
	props.setProperty("connector.0.user", "blackduck");
	props.setProperty("connector.0.password", "testpassword");

	TestFixture testFixture = doTest(props);
	assertEquals("Expected file count after checkout", 3,
		countFiles(testFixture.getLocalSourceDir()));

	// Pick a random file and make sure it's there
	File testFile = getTestFileFsysRepo(testFixture.getLocalSourceDir()
		.getAbsolutePath());
	assertTrue(testFile.exists());
	assertEquals(15, testFile.length());

	// Delete all files locally; update should bring 'em all back
	purgeDir(testFixture.getLocalSourceDir());
	assertEquals("Expected file count after purge", 0,
		countFiles(testFixture.getLocalSourceDir()));

	// Second time: update: should bring deleted files back
	testFixture.getConnectorRunner().processConnectors();

	// The files should have been restored from SCM
	assertEquals("Expected file count after update", 3,
		countFiles(testFixture.getLocalSourceDir()));

	// Check that specific file again too
	assertTrue(testFile.exists());
	assertEquals(15, testFile.length());
    }
}
