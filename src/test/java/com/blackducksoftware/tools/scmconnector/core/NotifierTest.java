package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisInfo;
import com.blackducksoftware.tools.commonframework.standard.email.CFEmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.email.EmailNotifier;
import com.blackducksoftware.tools.scmconnector.core.AnalysisResults;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;
import com.blackducksoftware.tools.scmconnector.core.Notifier;
import com.blackducksoftware.tools.scmconnector.core.ProjectInfoPOJO;

public class NotifierTest {
    private static final long JUNE_18_2015_15_02_13_EDT = 1434654133003L;
    private final String PROJECT_NAME1 = "JUnit_NotifierTest";
    private static final String SERVER_NAME = "se-px01.dc1.lan";
    private static final String SERVER_PROTOCOL = "https";
    private static final String PROTEX_URL = SERVER_PROTOCOL + "://"
	    + SERVER_NAME + "/";
    private static final String PROTEX_PASSWORD = "blackduck";
    private static final String PROTEX_USERNAME = "unitTester@blackducksoftware.com";

    private static final String EXPECTED_EMAIL_BODY_STANDARD = "<h3>Analysis Powered by: Black Duck&trade; Protex</h3>\n"
	    + "    <p><b>Project Name:</b> testProjectName </p>\n"
	    + "    <p><b>Scan Start Time:</b> Thu Jun 18 15:02:13 EDT 2015 </p>\n"
	    + "    <p><b>Scan Finish Time:</b> Thu Jun 18 15:02:13 EDT 2015 </p>\n"
	    + "    <p><b>Scan Performed By:</b> testAnalyzedBy </p>\n"
	    + "    <p><b>Scan Type:</b> testScanType </p>\n"
	    + "    <p><b>Code Repository Type:</b> testConnector </p>\n"
	    + "    <p><b>Code Repository Path:</b> testRepoPath </p>\n"
	    + "    <table border=\"1\">\n"
	    + "	<tr>\n"
	    + "	<td></td>\n"
	    + "	<td>Total #</td>\n"
	    + "	<td>Total # As Part of Delta Scan</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Files Scanned</td>\n"
	    + "	<td>13</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Pending Identification</td>\n"
	    + "	<td>15</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Pending Code Match Identification</td>\n"
	    + "	<td>11</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Pending String Search Identification</td>\n"
	    + "	<td>17</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Pending Dependency Identification</td>\n"
	    + "	<td>12</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Pending File Pattern Identification</td>\n"
	    + "	<td>14</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Rapid ID Code Matches</td>\n"
	    + "	<td>16</td>\n"
	    + "	<td>10</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	<tr>\n"
	    + "	<td>Pending Review</td>\n"
	    + "	<td>0</td>\n"
	    + "	<td>0</td>\n"
	    + "	</tr>\n"
	    + "	\n"
	    + "	</table>\n"
	    + "	<p><b>BOM URL for project:</b> testBomUrl </p>";

    private static final String EXPECTED_EMAIL_BODY_CUSTOM = "<h3>User-Defined Email</h3>\n"
	    + "    <p>Not much to say</p>";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testRejectObsoleteEmailProperty() throws Exception {
	Properties props = createBasicConfig(PROJECT_NAME1);
	props.setProperty("skip.email.if.no.deltas", "anything");
	try {
	    new ConnectorConfig(props);
	    fail("Should have thrown an exception on the skip.email.if.no.deltas property");
	} catch (IllegalArgumentException e) {
	    System.out
		    .println("As expected, exception thrown on obsolete skip.email.if.no.deltas property: "
			    + e.getMessage());
	}
    }

    @Test
    public void testUserSuppliedFile() throws Exception {
	Properties props = createBasicConfig(PROJECT_NAME1);
	props.setProperty("email.content.file.path",
		"src/test/resources/user_supplied_email.xml");
	ConnectorConfig config = new ConnectorConfig(props);

	EmailNotifier emailNotif = new CFEmailNotifier(config);
	Notifier notifier = new Notifier(config, emailNotif,
		"src/main/resources/notifications.xml");

	ProjectInfoPOJO projectInfoPojo = new ProjectInfoPOJO();
	Project project = new Project();
	project.setName("testProjectName");
	projectInfoPojo.setProject(project);
	projectInfoPojo.setBomUrl("testBomUrl");
	projectInfoPojo.setScanType("testScanType");

	projectInfoPojo.setPreAnalysisCodeMatchPendingIdFileCount(1);
	projectInfoPojo.setPreAnalysisDependencyPendingIdFileCount(2);
	projectInfoPojo.setPreAnalysisFileCount(3);
	projectInfoPojo.setPreAnalysisFilePatternMatchPendingIdFileCount(4);
	projectInfoPojo.setPreAnalysisPendingFileCount(5);
	projectInfoPojo.setPreAnalysisRapidIdCount(6);
	projectInfoPojo.setPreAnalysisStringSearchPendingIdFileCount(7);

	projectInfoPojo.setPostAnalysisCodeMatchPendingIdFileCount(11);
	projectInfoPojo.setPostAnalysisDependencyPendingIdFileCount(12);
	projectInfoPojo.setPostAnalysisFileCount(13);
	projectInfoPojo.setPostAnalysisFilePatternMatchPendingIdFileCount(14);
	projectInfoPojo.setPostAnalysisPendingFileCount(15);
	projectInfoPojo.setPostAnalysisRapidIdCount(16);
	projectInfoPojo.setPostAnalysisStringSearchPendingIdFileCount(17);

	AnalysisInfo analysisInfo = new AnalysisInfo();
	Date date = new Date(JUNE_18_2015_15_02_13_EDT);
	analysisInfo.setAnalysisFinishedDate(date);
	analysisInfo.setAnalysisStartedDate(date);
	analysisInfo.setAnalyzedBy("testAnalyzedBy");
	projectInfoPojo.setAnalysisInfo(analysisInfo);
	AnalysisResults projectAnalysisResults = new AnalysisResults(config,
		projectInfoPojo);

	String emailBody = notifier.sendNotificationEmail("testConnector",
		"testRepoPath", "testAggregatedRepoPath",
		projectAnalysisResults);

	assertEquals(EXPECTED_EMAIL_BODY_CUSTOM, emailBody);
    }

    @Test
    public void testStandardEmailFile() throws Exception {
	Properties props = createBasicConfig(PROJECT_NAME1);
	ConnectorConfig config = new ConnectorConfig(props);

	EmailNotifier emailNotif = new CFEmailNotifier(config);
	Notifier notifier = new Notifier(config, emailNotif,
		"src/main/resources/notifications.xml");

	ProjectInfoPOJO projectInfoPojo = new ProjectInfoPOJO();
	Project project = new Project();
	project.setName("testProjectName");
	projectInfoPojo.setProject(project);
	projectInfoPojo.setBomUrl("testBomUrl");
	projectInfoPojo.setScanType("testScanType");

	projectInfoPojo.setPreAnalysisCodeMatchPendingIdFileCount(1);
	projectInfoPojo.setPreAnalysisDependencyPendingIdFileCount(2);
	projectInfoPojo.setPreAnalysisFileCount(3);
	projectInfoPojo.setPreAnalysisFilePatternMatchPendingIdFileCount(4);
	projectInfoPojo.setPreAnalysisPendingFileCount(5);
	projectInfoPojo.setPreAnalysisRapidIdCount(6);
	projectInfoPojo.setPreAnalysisStringSearchPendingIdFileCount(7);

	projectInfoPojo.setPostAnalysisCodeMatchPendingIdFileCount(11);
	projectInfoPojo.setPostAnalysisDependencyPendingIdFileCount(12);
	projectInfoPojo.setPostAnalysisFileCount(13);
	projectInfoPojo.setPostAnalysisFilePatternMatchPendingIdFileCount(14);
	projectInfoPojo.setPostAnalysisPendingFileCount(15);
	projectInfoPojo.setPostAnalysisRapidIdCount(16);
	projectInfoPojo.setPostAnalysisStringSearchPendingIdFileCount(17);

	AnalysisInfo analysisInfo = new AnalysisInfo();
	Date date = new Date(JUNE_18_2015_15_02_13_EDT);
	analysisInfo.setAnalysisFinishedDate(date);
	analysisInfo.setAnalysisStartedDate(date);
	analysisInfo.setAnalyzedBy("testAnalyzedBy");
	projectInfoPojo.setAnalysisInfo(analysisInfo);
	AnalysisResults projectAnalysisResults = new AnalysisResults(config,
		projectInfoPojo);

	String emailBody = notifier.sendNotificationEmail("testConnector",
		"testRepoPath", "testAggregatedRepoPath",
		projectAnalysisResults);

	assertEquals(EXPECTED_EMAIL_BODY_STANDARD, emailBody);
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
	props.setProperty("connector.0.repositoryURL", "notused");
	props.setProperty("connector.0.user", "notused");
	props.setProperty("connector.0.password", "notused");
	props.setProperty("connector.0.new_project_name", projectName);

	// The existence of these properties will cause email body generation
	// happen
	props.setProperty("smtp.address", "bogus.blackducksoftware.com");
	props.setProperty("smtp.from", "bogus@blackducksoftware.com");
	props.setProperty("smtp.to", "bogus@blackducksoftware.com");

	return props;
    }

}
