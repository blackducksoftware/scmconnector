/*******************************************************************************
 * Copyright (C) 2015 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.tools.scmconnector.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisInfo;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.common.ProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.email.EmailTriggerRule;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.scmconnector.core.protex.ProtexIdentifications;
import com.blackducksoftware.tools.scmconnector.core.protex.ProtexUtils;

/**
 * Manages analysis results. Stores pre-analysis results and post-analysis
 * results. Use isNotificationWorthy() to determine whether or not the results
 * should be sent to user via email. Use populateEmailContentMap to fill in the
 * values in an EmailContentMap. Use getPropertiesForConnectorAnalysisResults()
 * to get the Properties object that gets stored in the connector
 * analysisResults field.
 *
 * This code is still pretty messy, but at least it's encapsulated in one class
 * now.
 *
 * @author sbillings
 *
 */
public class AnalysisResults {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final ConnectorConfig config;
    private final ProjectInfoPOJO projectInfoPojo;

    private enum RuleId {
	ALWAYS, ANY_DELTA, NEW_PENDING_ID, NEW_RAPID_ID, NEW_FILE, NEVER;

	private static RuleId nameToId(String name) {
	    return Enum.valueOf(AnalysisResults.RuleId.class, name);
	}
    }

    public AnalysisResults(ConnectorConfig config) {
	this.config = config;
	projectInfoPojo = new ProjectInfoPOJO();
    }

    public AnalysisResults(ConnectorConfig config,
	    ProjectInfoPOJO projectInfoPojo) {
	this.config = config;
	this.projectInfoPojo = projectInfoPojo;
    }

    /**
     * To create/get, from this object, a basic ProjectPojo object
     *
     * @return
     */
    public ProjectPojo getProjectPojo() {
	ProjectPojo basicPojo = new ProtexProjectPojo(projectInfoPojo
		.getProject().getProjectId(), projectInfoPojo.getProject()
		.getName());
	return basicPojo;
    }

    /**
     * Calculate the state (triggers email / does not trigger email) of the
     * email trigger rules. If there are no rules, manufacture one "ALWAYS"
     * rule, to create default behavior.
     *
     * @return
     */
    public List<EmailTriggerRule> calculateNotificationTriggerRuleStates() {

	List<EmailTriggerRule> ruleStates = config
		.getNotificationRulesConfiguration().getRules();
	if (ruleStates == null) {
	    ruleStates = new ArrayList<EmailTriggerRule>(1);
	}
	if (ruleStates.size() == 0) {
	    EmailTriggerRule defaultAlwaysRule = new EmailTriggerRule("ALWAYS");
	    defaultAlwaysRule.setRuleTriggered(true);
	    ruleStates.add(defaultAlwaysRule);
	    return ruleStates;
	}

	for (EmailTriggerRule ruleState : ruleStates) {
	    ruleState.setRuleTriggered(false); // assume not triggered until
					       // shown otherwise
	    String ruleName = ruleState.getRuleName();
	    RuleId ruleId = RuleId.nameToId(ruleName);
	    switch (ruleId) {
	    case ALWAYS:
		ruleState.setRuleTriggered(true);
		break;
	    case ANY_DELTA:
		if (isAnyDeltas(projectInfoPojo)) {
		    ruleState.setRuleTriggered(true);
		}
		break;
	    case NEW_PENDING_ID:
		if (newPendingId(projectInfoPojo)) {
		    ruleState.setRuleTriggered(true);
		}
		break;
	    case NEW_RAPID_ID:
		if (newRapidId(projectInfoPojo)) {
		    ruleState.setRuleTriggered(true);
		}
		break;
	    case NEW_FILE:
		if (newFile(projectInfoPojo)) {
		    ruleState.setRuleTriggered(true);
		}
		break;
	    case NEVER:
		setAllRules(ruleStates, false); // override all rules
		return ruleStates;
	    }

	}

	return ruleStates;
    }

    private void setAllRules(List<EmailTriggerRule> rules, boolean value) {
	for (EmailTriggerRule rule : rules) {
	    rule.setRuleTriggered(value);
	}
    }

    private static boolean newRapidId(ProjectInfoPOJO projectInfo) {
	if (getDeltaRapidIdCount(projectInfo) > 0) {
	    return true;
	}
	return false;
    }

    private static boolean newFile(ProjectInfoPOJO projectInfo) {
	if (getDeltaFileCount(projectInfo) > 0) {
	    return true;
	}
	return false;
    }

    private static boolean newPendingId(ProjectInfoPOJO projectInfo) {
	if (getDeltaIdFileCount(projectInfo) > 0) {
	    return true;
	}
	return false;
    }

    private static boolean isAnyDeltas(ProjectInfoPOJO projectInfo) {
	if ((getDeltaFileDiscoveryPatternIdFileCount(projectInfo) != 0)
		|| (getDeltaDependenciesIdFileCount(projectInfo) != 0)
		|| (getDeltaStringSearchIdFileCount(projectInfo) != 0)
		|| (getDeltaCodeMatchIdFileCount(projectInfo) != 0)
		|| (getDeltaIdFileCount(projectInfo) != 0)
		|| (getDeltaFileCount(projectInfo) != 0)
		|| (getDeltaRapidIdCount(projectInfo) != 0)) {
	    return true;
	}
	return false;
    }

    private static int getDeltaPendingReviewFileCount(
	    ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisPendingReviewFileCount()
		- projectInfo.getPreAnalysisPendingReviewFileCount();
    }

    private static int getDeltaRapidIdCount(ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisRapidIdCount()
		- projectInfo.getPreAnalysisRapidIdCount();
    }

    private static int getDeltaFileDiscoveryPatternIdFileCount(
	    ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisFilePatternMatchPendingIdFileCount()
		- projectInfo
			.getPreAnalysisFilePatternMatchPendingIdFileCount();
    }

    private static int getDeltaDependenciesIdFileCount(
	    ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisDependencyPendingIdFileCount()
		- projectInfo.getPreAnalysisDependencyPendingIdFileCount();
    }

    private static int getDeltaStringSearchIdFileCount(
	    ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisStringSearchPendingIdFileCount()
		- projectInfo.getPreAnalysisStringSearchPendingIdFileCount();
    }

    private static int getDeltaCodeMatchIdFileCount(ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisCodeMatchPendingIdFileCount()
		- projectInfo.getPreAnalysisCodeMatchPendingIdFileCount();
    }

    private static int getDeltaIdFileCount(ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisPendingFileCount()
		- projectInfo.getPreAnalysisPendingFileCount();
    }

    private static int getDeltaFileCount(ProjectInfoPOJO projectInfo) {
	return projectInfo.getPostAnalysisFileCount()
		- projectInfo.getPreAnalysisFileCount();
    }

    /**
     * Set the pre-Analysis values from the given Project.
     *
     * @param protexUtils
     * @param protexServerWrapper
     * @param project
     */
    public void setPreAnalysisInfo(ProtexUtils protexUtils,
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    Project project) {

	projectInfoPojo.setPreAnalysisFileCount(protexUtils
		.getFileCount(project));
	projectInfoPojo.setPreAnalysisPendingFileCount(protexUtils
		.getPendingIdFileCount(project));
	projectInfoPojo.setPreAnalysisCodeMatchPendingIdFileCount(protexUtils
		.getPendingCodeMatchIdFileCount(project));
	projectInfoPojo
		.setPreAnalysisStringSearchPendingIdFileCount(protexUtils
			.getPendingStringSearchFileCount(project));
	projectInfoPojo.setPreAnalysisDependencyPendingIdFileCount(protexUtils
		.getPendingDependenciesIdFileCount(project));
	projectInfoPojo
		.setPreAnalysisFilePatternMatchPendingIdFileCount(protexUtils
			.getPendingFileDiscoveryPatternIdFileCount(project));
	projectInfoPojo.setPreAnalysisPendingReviewFileCount(protexUtils
		.getPendingReviewCount(project));

	ProtexIdentifications protexIdentifications = new ProtexIdentifications(
		protexServerWrapper);
	try {
	    projectInfoPojo.setPreAnalysisRapidIdCount(protexIdentifications
		    .getRapidCount(project));
	} catch (Exception e) {
	    log.info("Unable to get pre-analysis ID info for project "
		    + project.getName() + ": " + e.getMessage());
	    projectInfoPojo.setPreAnalysisRapidIdCount(0);
	}
	String scanType = null;
	// to determine whether scan was conducted for the first time
	if (projectInfoPojo.getPreAnalysisFileCount() == 0) {
	    scanType = ConnectorConstants.SCAN_TYPE_BASELINE;
	} else {
	    scanType = ConnectorConstants.SCAN_TYPE_DELTA;
	}

	projectInfoPojo.setScanType(scanType);
    }

    /**
     * Set the post-Analysis values from the given Project.
     *
     * @param project
     * @param projectInfo
     * @throws Exception
     */
    public void setPostAnalysisInfo(ProtexUtils protexUtils,
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    Project project) {
	AnalysisInfo analysisInfo = null;
	try {
	    analysisInfo = protexUtils.getLastAnalysisInfo(project);
	} catch (SdkFault e) {
	    String message = "Error obtaining post analysis info for project with name: "
		    + project.getName();
	    log.error(message, e);
	    return;
	}

	projectInfoPojo.setAnalysisInfo(analysisInfo);

	projectInfoPojo.setProject(project);

	projectInfoPojo.setPostAnalysisFileCount((protexUtils
		.getFileCount(project)));
	projectInfoPojo.setPostAnalysisPendingFileCount(protexUtils
		.getPendingIdFileCount(project));
	projectInfoPojo.setPostAnalysisCodeMatchPendingIdFileCount(protexUtils
		.getPendingCodeMatchIdFileCount(project));
	projectInfoPojo
		.setPostAnalysisStringSearchPendingIdFileCount(protexUtils
			.getPendingStringSearchFileCount(project));
	projectInfoPojo.setPostAnalysisDependencyPendingIdFileCount(protexUtils
		.getPendingDependenciesIdFileCount(project));
	projectInfoPojo
		.setPostAnalysisFilePatternMatchPendingIdFileCount(protexUtils
			.getPendingFileDiscoveryPatternIdFileCount(project));
	projectInfoPojo.setPostAnalysisPendingReviewFileCount(protexUtils
		.getPendingReviewCount(project));

	int rapidCount = 0;
	ProtexIdentifications protexIdentifications = new ProtexIdentifications(
		protexServerWrapper);
	try {
	    rapidCount = protexIdentifications.getRapidCount(project);
	} catch (Exception e) {
	    log.warn("Unable to obtain rapid count for project with name: "
		    + project.getName());
	}
	projectInfoPojo.setPostAnalysisRapidIdCount(rapidCount);

	String bomUrl = config.getServerBean().getServerName()
		+ "/protex/ProtexIPIdentifyFolderBillOfMaterialsContainer?isAtTop=true&ProtexIPProjectId="
		+ project.getProjectId()
		+ "&ProtexIPIdentifyFileViewLevel=folder&ProtexIPIdentifyFileId=-1";
	projectInfoPojo.setBomUrl(bomUrl);

    }

    /**
     * Populate the given EmailContentMap with analysis results values.
     *
     * @param connectorName
     * @param repositoryPath
     * @param aggregatedRepositoryPath
     * @param values
     */
    public void populateEmailContentMap(String connectorName,
	    String repositoryPath, String aggregatedRepositoryPath,
	    EmailContentMap values) {

	values.put("projectName", projectInfoPojo.getProject().getName());
	values.put(
		"analysisStartDate",
		getString(projectInfoPojo.getAnalysisInfo()
			.getAnalysisStartedDate(), "Analysis Started Date"));
	values.put(
		"analysisFinishDate",
		getString(projectInfoPojo.getAnalysisInfo()
			.getAnalysisFinishedDate(), "Analysis Finished Date"));
	values.put("analyzedBy", projectInfoPojo.getAnalysisInfo()
		.getAnalyzedBy());
	values.put("scanType", projectInfoPojo.getScanType());
	values.put("connectorName", connectorName);
	if (config.isProtexScanOnlyLastConnector()) {
	    values.put("connectorRepositoryPath", aggregatedRepositoryPath);
	} else {
	    values.put("connectorRepositoryPath", repositoryPath);
	}
	values.put("fileCount",
		Integer.toString(projectInfoPojo.getPostAnalysisFileCount()));
	values.put("deltaFileCount",
		Integer.toString(getDeltaFileCount(projectInfoPojo)));
	values.put("pendingIdFileCount", Integer.toString(projectInfoPojo
		.getPostAnalysisPendingFileCount()));
	values.put("deltaIdFileCount",
		Integer.toString(getDeltaIdFileCount(projectInfoPojo)));
	values.put("pendingCodeMatchIdFileCount", Integer
		.toString(projectInfoPojo
			.getPostAnalysisCodeMatchPendingIdFileCount()));
	values.put("deltaCodeMatchIdFileCount",
		Integer.toString(getDeltaCodeMatchIdFileCount(projectInfoPojo)));
	values.put("pendingStringSearchIdFileCount", Integer
		.toString(projectInfoPojo
			.getPostAnalysisStringSearchPendingIdFileCount()));
	values.put("deltaStringSearchIdFileCount", Integer
		.toString(getDeltaStringSearchIdFileCount(projectInfoPojo)));
	values.put("pendingDependenciesIdFileCount", Integer
		.toString(projectInfoPojo
			.getPostAnalysisDependencyPendingIdFileCount()));
	values.put("deltaDependenciesIdFileCount", Integer
		.toString(getDeltaDependenciesIdFileCount(projectInfoPojo)));
	values.put("pendingFileDiscoveryPatternIdFileCount", Integer
		.toString(projectInfoPojo
			.getPostAnalysisFilePatternMatchPendingIdFileCount()));
	values.put(
		"deltaFileDiscoveryPatternIdFileCount",
		Integer.toString(getDeltaFileDiscoveryPatternIdFileCount(projectInfoPojo)));

	values.put("pendingReviewFileCount", Integer.toString(projectInfoPojo
		.getPostAnalysisPendingReviewFileCount()));
	values.put("deltaPendingReviewFileCount", Integer
		.toString(getDeltaPendingReviewFileCount(projectInfoPojo)));

	values.put("bomUrl", projectInfoPojo.getBomUrl());

	values.put("rapidIdCount",
		Integer.toString(projectInfoPojo.getPostAnalysisRapidIdCount()));
	values.put("deltaRapidIdCount",
		Integer.toString(getDeltaRapidIdCount(projectInfoPojo)));

	values.putAll(config.getNotificationSubstitutionValues());
    }

    private String getString(Object o, String name) {
	if (o == null) {
	    log.warn(name + " is null");
	    return "<null>";
	}
	return o.toString();
    }

    /**
     * Get a Properties object full of analysis results values for the
     * connector.
     *
     * @param connectorIndex
     * @return
     */
    public Properties getPropertiesForConnectorAnalysisResults(
	    int connectorIndex) {
	Properties props = new Properties();

	Project project = projectInfoPojo.getProject();
	AnalysisInfo analysisInfo = projectInfoPojo.getAnalysisInfo();

	String scanType = null;
	if (projectInfoPojo.getPreAnalysisFileCount() == 0) {
	    scanType = ConnectorConstants.SCAN_TYPE_BASELINE;
	} else {
	    scanType = ConnectorConstants.SCAN_TYPE_DELTA;
	}

	// setResultInProperties(props, connectorIndex, i, String name, String
	// description, String value)
	int i = 0;
	setResultInProperties(props, connectorIndex, i++, "projectName",
		"Project Name", project.getName());
	setResultInProperties(props, connectorIndex, i++, "scanType",
		"Scan Type", scanType);
	setResultInProperties(props, connectorIndex, i++, "analysisStartDate",
		"Analysis Start Date", analysisInfo.getAnalysisStartedDate()
			.toString());
	setResultInProperties(props, connectorIndex, i++, "analysisFinishDate",
		"Analysis Finish Date", analysisInfo.getAnalysisFinishedDate()
			.toString());
	setResultInProperties(props, connectorIndex, i++, "analyzedBy",
		"Analyzed By", analysisInfo.getAnalyzedBy());
	setResultInProperties(props, connectorIndex, i++, "fileCount",
		"File Count",
		Integer.toString(projectInfoPojo.getPostAnalysisFileCount()));
	setResultInProperties(
		props,
		connectorIndex,
		i++,
		"deltaFileCount",
		"Delta File Count",
		Integer.toString(projectInfoPojo.getPostAnalysisFileCount()
			- projectInfoPojo.getPreAnalysisFileCount()));
	setResultInProperties(props, connectorIndex, i++, "pendingIdFileCount",
		"Pending ID File Count", Integer.toString(projectInfoPojo
			.getPostAnalysisPendingFileCount()));
	setResultInProperties(props, connectorIndex, i++, "deltaIdFileCount",
		"Delta ID File Count", Integer.toString(projectInfoPojo
			.getPostAnalysisPendingFileCount()
			- projectInfoPojo.getPreAnalysisPendingFileCount()));
	setResultInProperties(props, connectorIndex, i++,
		"pendingCodeMatchIdFileCount",
		"Pending Code Match ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisCodeMatchPendingIdFileCount()));
	setResultInProperties(props, connectorIndex, i++,
		"deltaCodeMatchIdFileCount", "Delta Code Match ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisCodeMatchPendingIdFileCount()
			- projectInfoPojo
				.getPreAnalysisCodeMatchPendingIdFileCount()));

	setResultInProperties(props, connectorIndex, i++,
		"pendingStringSearchIdFileCount",
		"Pending String Search ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisStringSearchPendingIdFileCount()));
	setResultInProperties(
		props,
		connectorIndex,
		i++,
		"deltaStringSearchIdFileCount",
		"Delta String Search ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisStringSearchPendingIdFileCount()
			- projectInfoPojo
				.getPreAnalysisStringSearchPendingIdFileCount()));

	setResultInProperties(props, connectorIndex, i++,
		"pendingDependenciesIdFileCount",
		"Pending Dependencies ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisDependencyPendingIdFileCount()));
	setResultInProperties(props, connectorIndex, i++,
		"deltaDependenciesIdFileCount",
		"Delta Dependencies ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisDependencyPendingIdFileCount()
			- projectInfoPojo
				.getPreAnalysisDependencyPendingIdFileCount()));

	setResultInProperties(props, connectorIndex, i++,
		"pendingFileDiscoveryPatternIdFileCount",
		"Pending File Discovery Pattern ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisFilePatternMatchPendingIdFileCount()));
	setResultInProperties(
		props,
		connectorIndex,
		i++,
		"deltaFileDiscoveryPatternIdFileCount",
		"Delta File Discovery Pattern ID File Count",
		Integer.toString(projectInfoPojo
			.getPostAnalysisFilePatternMatchPendingIdFileCount()
			- projectInfoPojo
				.getPreAnalysisFilePatternMatchPendingIdFileCount()));

	setResultInProperties(props, connectorIndex, i++, "rapidIdCount",
		"Rapid Identification Count",
		Integer.toString(projectInfoPojo.getPostAnalysisRapidIdCount()));
	setResultInProperties(
		props,
		connectorIndex,
		i++,
		"deltaRapidIdCount",
		"Delta Rapid ID Count",
		Integer.toString(projectInfoPojo.getPostAnalysisRapidIdCount()
			- projectInfoPojo.getPreAnalysisRapidIdCount()));

	setResultInProperties(props, connectorIndex, i++, "bomUrl", "BOM URL",
		projectInfoPojo.getBomUrl());

	return props;
    }

    private static void setResultInProperties(Properties props,
	    int connectorIndex, int i, String name, String description,
	    String value) {
	String propertyKeyPrefix = "connector." + connectorIndex + ".";

	// for iterating through all names/descriptions/values
	props.put(propertyKeyPrefix + "result." + i + "." + "name", name);
	props.put(propertyKeyPrefix + "result." + i + "." + "description",
		description);
	props.put(propertyKeyPrefix + "result." + i + "." + "value", "" + value);

	// for fetching by name
	props.put(propertyKeyPrefix + "result." + name, value);
    }
}
