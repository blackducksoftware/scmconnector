package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractTest {
    // Any/all tests can add their testFixtures to this list, which will be
    // cleaned up at the end
    protected static final List<TestFixture> testFixtureCleanUpList = new ArrayList<TestFixture>();
    protected static final List<File> dirCleanUpList = new ArrayList<File>();

    private static Date yesterdayDate = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	// There are some cases where temp dirs are not removed because
	// of left-over locks (something isn't being closed down completely
	// before the test exists). This is a crude way of ensuring those don't
	// build up over time
	deleteOldTempDirs();
    }

    private static void deleteOldTempDirs() throws Exception {
	File tempDirLocation = getTempDirLocation();

	File[] files = tempDirLocation.listFiles();
	for (File file : files) {
	    if (file.isDirectory()
		    && (file.getName()
			    .matches("scmtest[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]+"))) {
		if (!FileUtils.isFileNewer(file, yesterday())) {
		    FileUtils.deleteQuietly(file);
		}
	    }
	}
    }

    private static Date yesterday() throws Exception {
	if (yesterdayDate == null) {
	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    yesterdayDate = cal.getTime();
	}
	return yesterdayDate;
    }

    /**
     * Clean up all test fixtures from all tests
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
	boolean debugMode = false;
	if (!debugMode) {
	    for (TestFixture testFixture : testFixtureCleanUpList) {
		testFixture.cleanUp();
	    }
	    for (File dir : dirCleanUpList) {
		try {
		    FileUtils.deleteQuietly(dir);
		} catch (Exception e) {
		    System.out.println("Error deleting dir "
			    + dir.getAbsolutePath() + ": " + e.getMessage());
		}
	    }
	}
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Setup and run the test.
     *
     * @param configFilename
     *            The configuration file (.properties)
     * @return The test fixture for the test.
     */
    public TestFixture doTest(String configFilename) throws Exception {
	return doTest(configFilename, null, 1);
    }

    public TestFixture doTest(String configFilename, int numConnectors)
	    throws Exception {
	return doTest(configFilename, null, numConnectors);
    }

    public TestFixture doTest(String configFilename, String additionalProperties)
	    throws Exception {
	return doTest(configFilename, additionalProperties, 1);
    }

    /**
     * Setup and run the test. If provided, append some additioanl properties to
     * the end of the properties file.
     *
     * @param configFilename
     *            The configuration file (.properties)
     * @return The test fixture for the test.
     */
    public TestFixture doTest(String configFilename,
	    String additionalProperties, int numConnectors) throws Exception {
	TestFixture testFixture = new TestFixture("src/test/resources",
		configFilename, additionalProperties, numConnectors);
	testFixtureCleanUpList.add(testFixture);
	ConnectorRunner runner = initRunnerConfigFile(testFixture);
	run(runner);
	return testFixture;
    }

    /**
     * Setup and run the test. If provided, append some additioanl properties to
     * the end of the properties file.
     *
     * @param configFilename
     *            The configuration file (.properties)
     * @return The test fixture for the test.
     */
    public static TestFixture doTest(Properties configProperties)
	    throws Exception {
	TestFixture testFixture = new TestFixture(configProperties);
	testFixtureCleanUpList.add(testFixture);
	ConnectorRunner runner = initRunnerConfigProperties(testFixture);
	run(runner);
	return testFixture;
    }

    /**
     * Setup and run the test. If provided, append some additioanl properties to
     * the end of the properties file.
     *
     * @param configFilename
     *            The configuration file (.properties)
     * @return The test fixture for the test.
     */
    protected TestFixture doTestNopConnector(Properties configProperties,
	    File sourceDir, boolean expectSuccess) throws Exception {
	TestFixture testFixture = new TestFixture(configProperties);
	testFixtureCleanUpList.add(testFixture);

	// For the NOP Connector, we must ensure the source is already at the
	// root folder
	// since there is no SCM checkout to get it there.
	File scanRootDir = testFixture.getLocalSourceDir();
	FileUtils.copyDirectory(sourceDir, scanRootDir);

	ConnectorRunner runner = initRunnerConfigProperties(testFixture);
	if (expectSuccess) {
	    run(runner);
	} else {
	    runExpectingFailure(runner);
	}
	return testFixture;
    }

    private static MockProtexRunner getProtexRunner(ConnectorConfig config) {
	MockProtexRunner protexRunner = new MockProtexRunner(config);
	return protexRunner;
    }

    private ConnectorRunner initRunnerConfigFile(TestFixture testFixture)
	    throws Exception {
	ConnectorConfig config = new ConnectorConfig(testFixture
		.getDestConfigFile().getAbsolutePath());
	MockProtexRunner protexRunner = getProtexRunner(config);
	ConnectorRunner runner = new ConnectorRunner(config, protexRunner);
	testFixture.setConnectorRunner(runner);
	testFixture.setProtexRunner(protexRunner);
	return runner;
    }

    private static ConnectorRunner initRunnerConfigProperties(
	    TestFixture testFixture) throws Exception {
	ConnectorConfig config = new ConnectorConfig(
		testFixture.getProperties());
	MockProtexRunner protexRunner = getProtexRunner(config);
	ConnectorRunner runner = new ConnectorRunner(
		testFixture.getProperties(), protexRunner);
	testFixture.setConnectorRunner(runner);
	testFixture.setProtexRunner(protexRunner);
	return runner;
    }

    private static void run(ConnectorRunner runner) throws Exception {
	runner.processConnectors();
	assertTrue("All connectors succeeded check", runner.noErrors());
    }

    private void runExpectingFailure(ConnectorRunner runner) {
	runner.processConnectors();
	assertFalse("Connector failure check", runner.noErrors());
    }

    protected static File getTempFolder() {
	File temp = null;

	try {
	    temp = File.createTempFile("scmtest",
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

    private static File getTempDirLocation() throws IOException {
	File tempDir = getTempFolder();
	File tempDirLocation = tempDir.getParentFile();
	tempDir.delete();
	return tempDirLocation;
    }

    protected static File getTestFileFsysRepo(String workDirPath) {
	StringBuilder testFilePath = new StringBuilder(workDirPath);
	testFilePath.append(File.separator);
	testFilePath.append("filesysrepo");
	testFilePath.append(File.separator);
	testFilePath.append("testFile.txt");
	File testFile = new File(testFilePath.toString());
	return testFile;
    }

    protected static File getTestFileNOP(String workDirPath) {
	StringBuilder testFilePath = new StringBuilder(workDirPath);
	testFilePath.append(File.separator);
	testFilePath.append("testFile.txt");
	File testFile = new File(testFilePath.toString());
	return testFile;
    }

    protected static File getTestFileWeb(String workDirPath) {
	StringBuilder testFilePath = new StringBuilder(workDirPath);
	testFilePath.append(File.separator);
	testFilePath.append("web");
	testFilePath.append(File.separator);
	testFilePath.append("css");
	testFilePath.append(File.separator);
	testFilePath.append("template_head.html");
	File testFile = new File(testFilePath.toString());
	return testFile;
    }

    protected static File getTestFileJMeter(String workDirPath) {
	StringBuilder testFilePath = new StringBuilder(workDirPath);
	testFilePath.append(File.separator);
	testFilePath.append("src");
	testFilePath.append(File.separator);
	testFilePath.append("i18nedit.properties");
	File testFile = new File(testFilePath.toString());
	return testFile;
    }

    protected static File getTestFileSampleFiles(String workDirPath) {
	StringBuilder testFilePath = new StringBuilder(workDirPath);
	testFilePath.append(File.separator);
	testFilePath.append("SampleFiles");
	testFilePath.append(File.separator);
	testFilePath.append("ASP_simple.asp");
	File testFile = new File(testFilePath.toString());
	return testFile;
    }

    /**
     * Recursively count non-hidden files.
     *
     * @param dir
     *            The top of the dir tree to scan/count
     * @return number of files found in the dir tree
     */
    public static int countFiles(File dir) {
	int fileCount = 0;
	File[] files = dir.listFiles();
	for (File file : files) {
	    if (file.getName().charAt(0) == '.') {
		continue;
	    }
	    if (file.isDirectory()) {
		fileCount += countFiles(file);
	    } else {
		fileCount++;
	    }
	}
	return fileCount;
    }

    /**
     * Recursively purge files/folders except within hidden folders.
     *
     * @param dir
     *            The top of the dir tree to scan/count
     * @return number of files found in the dir tree
     */
    protected static void purgeDir(File dir) {
	File[] files = dir.listFiles();
	for (File file : files) {
	    if (file.getName().charAt(0) == '.') {
		continue;
	    }
	    if (file.isDirectory()) {
		purgeDir(file);
		file.delete();
	    } else {
		file.delete();
	    }
	}
    }

    protected static String getModuleNameFromURL(String url) {
	String[] urlParts = url.split("/");
	return urlParts[urlParts.length - 1];
    }

    protected static boolean onWindows() {
	return SystemUtils.IS_OS_WINDOWS;

    }

    protected void dumpProperties(Properties properties) {
	System.out.println("Results:");
	for (String key : properties.stringPropertyNames()) {
	    String value = properties.getProperty(key);
	    System.out.println(key + " => " + value);
	}
    }

    public static File getSelfCleaningTempDir() {
	File tempDir = getTempFolder();
	dirCleanUpList.add(tempDir);
	return tempDir;
    }
}
