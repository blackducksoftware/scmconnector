package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.commonframework.standard.email.EmailTriggerRule;

public class AnalysisResultsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testIsScanResultEmailWorthyDefault() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);
	assertTrue(anyRuleTriggered(analysisResults));

	projectInfo.setPostAnalysisStringSearchPendingIdFileCount(6);
	assertTrue(anyRuleTriggered(analysisResults));
    }

    private boolean anyRuleTriggered(AnalysisResults analysisResults) {
	List<EmailTriggerRule> rulesState = analysisResults
		.calculateNotificationTriggerRuleStates();
	for (EmailTriggerRule ruleState : rulesState) {
	    if (ruleState.isRuleTriggered()) {
		return true;
	    }
	}
	return false;
    }

    @Test
    public void testIsScanResultEmailWorthyObsoleteSkipProperty()
	    throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("skip.email.if.no.deltas", "true");

	try {
	    new ConnectorConfig(props);
	    fail("Exception expected");
	} catch (IllegalArgumentException e) {
	    // expected
	}
    }

    @Test
    public void testRuleAnyDelta() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules", "ANY_DELTA");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	// no delta = no email
	assertFalse(anyRuleTriggered(analysisResults));

	// Create a delta: Should produce an email
	projectInfo.setPostAnalysisFileCount(6);
	assertTrue(anyRuleTriggered(analysisResults));
    }

    @Test
    public void testRuleNewPendingId() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules", "NEW_PENDING_ID");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	// no delta = no email
	assertFalse(anyRuleTriggered(analysisResults));

	// Create a delta: Should produce an email
	projectInfo.setPostAnalysisPendingFileCount(6);
	assertTrue(anyRuleTriggered(analysisResults));
    }

    @Test
    public void testRuleNewRapidId() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules", "NEW_RAPID_ID");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	// no delta = no email
	assertFalse(anyRuleTriggered(analysisResults));

	// Create a delta: Should produce an email
	projectInfo.setPostAnalysisRapidIdCount(6);
	assertTrue(anyRuleTriggered(analysisResults));
    }

    @Test
    public void testRuleNewFile() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules", "NEW_FILE");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	// no delta = no email
	assertFalse(anyRuleTriggered(analysisResults));

	// Create a delta: Should produce an email
	projectInfo.setPostAnalysisFileCount(6);
	assertTrue(anyRuleTriggered(analysisResults));
    }

    @Test
    public void testRuleNever() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules", "NEVER");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	// NEVER rule = no email
	assertFalse(anyRuleTriggered(analysisResults));

	// Create a delta: Still should be no email
	projectInfo.setPostAnalysisStringSearchPendingIdFileCount(6);
	assertFalse(anyRuleTriggered(analysisResults));
    }

    @Test
    public void testRuleNeverInCombination() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules",
		"ALWAYS,ANY_DELTA,NEW_PENDING_ID,NEW_RAPID_ID,NEW_FILE,NEVER");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	// no delta = no email
	assertFalse(anyRuleTriggered(analysisResults));

	// Change everything: Should not produce an email
	projectInfo.setPostAnalysisFilePatternMatchPendingIdFileCount(6);
	projectInfo.setPostAnalysisDependencyPendingIdFileCount(6);
	projectInfo.setPostAnalysisStringSearchPendingIdFileCount(6);
	projectInfo.setPostAnalysisCodeMatchPendingIdFileCount(6);
	projectInfo.setPostAnalysisPendingFileCount(6);
	projectInfo.setPostAnalysisFileCount(6);
	projectInfo.setPostAnalysisRapidIdCount(6);

	assertFalse(anyRuleTriggered(analysisResults));
    }

    @Test
    public void testBogusRule() throws Exception {

	Properties props = new Properties();
	props.setProperty("protex.user.name", "notused");
	props.setProperty("protex.server.name", "notused");
	props.setProperty("protex.password", "notused");

	props.setProperty("email.trigger.rules",
		"ALWAYS,ANY_DELTA,NEW_PENDING_ID,NEW_RAPID_ID,NEW_FILE,bogus,NEVER");

	ConnectorConfig config = new ConnectorConfig(props);

	ProjectInfoPOJO projectInfo = getNoDeltaProjectInfoPojo();

	AnalysisResults analysisResults = new AnalysisResults(config,
		projectInfo);

	try {
	    analysisResults.calculateNotificationTriggerRuleStates();
	    fail("Expected exception");
	} catch (IllegalArgumentException e) {
	    // expected
	}
    }

    private ProjectInfoPOJO getNoDeltaProjectInfoPojo() {
	ProjectInfoPOJO projectInfo = new ProjectInfoPOJO();

	projectInfo.setPreAnalysisFilePatternMatchPendingIdFileCount(5);
	projectInfo.setPreAnalysisDependencyPendingIdFileCount(5);
	projectInfo.setPreAnalysisStringSearchPendingIdFileCount(5);
	projectInfo.setPreAnalysisCodeMatchPendingIdFileCount(5);
	projectInfo.setPreAnalysisPendingFileCount(5);
	projectInfo.setPreAnalysisFileCount(5);
	projectInfo.setPreAnalysisRapidIdCount(5);

	projectInfo.setPostAnalysisFilePatternMatchPendingIdFileCount(5);
	projectInfo.setPostAnalysisDependencyPendingIdFileCount(5);
	projectInfo.setPostAnalysisStringSearchPendingIdFileCount(5);
	projectInfo.setPostAnalysisCodeMatchPendingIdFileCount(5);
	projectInfo.setPostAnalysisPendingFileCount(5);
	projectInfo.setPostAnalysisFileCount(5);
	projectInfo.setPostAnalysisRapidIdCount(5);
	return projectInfo;
    }
}
