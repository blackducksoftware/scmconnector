/**
Copyright (C)2014 Black Duck Software Inc.
http://www.blackducksoftware.com/
All rights reserved. **/

package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNode;
import com.blackducksoftware.sdk.protex.project.codetree.PartialCodeTree;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.CodeMatchDiscovery;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.connector.protex.identification.ProtexIdUtils;
import com.blackducksoftware.tools.commonframework.core.config.ConfigConstants.APPLICATION;
import com.blackducksoftware.tools.commonframework.core.config.server.ServerBean;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;

public class ProtexTestUtils {
    private static ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper = null;

    static void init(ProtexServerWrapper<ProtexProjectPojo> wrapper) {
	protexServerWrapper = wrapper;
    }

    static void init(String server, String username, String password,
	    boolean pswIsPlainText) throws Exception {
	Properties props = new Properties();
	props.setProperty("protex.server.name", server);
	props.setProperty("protex.user.name", username);
	props.setProperty("protex.password", password);
	if (pswIsPlainText) {
	    props.setProperty("protex.password.isencrypted", "false");
	} else {
	    props.setProperty("protex.password.isencrypted", "true");
	}
	ConnectorConfig minimalConfig = new ConnectorConfig(props);

	ServerBean serverBean = new ServerBean();
	serverBean.setApplication(APPLICATION.PROTEX);
	serverBean.setServerName(server);
	serverBean.setUserName(username);
	serverBean.setPassword(password);

	protexServerWrapper = new ProtexServerWrapper<>(serverBean,
		minimalConfig, true);
    }

    public static String getProjectAnalysisSourceLocation(String projectName)
	    throws Exception {
	if (protexServerWrapper == null) {
	    throw new Exception("Need to call ProtexUtils.init() method first");
	}
	return getProjectAnalysisSourceLocation(protexServerWrapper,
		projectName);
    }

    private static String getProjectAnalysisSourceLocation(
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    String projectName) throws Exception {
	try {
	    Project project = protexServerWrapper.getInternalApiWrapper()
		    .getProjectApi().getProjectByName(projectName);
	    return project.getAnalysisSourceLocation().getSourcePath();
	} catch (Exception e) {
	    System.out
		    .println("Error getting analysis source location for project "
			    + projectName + ": " + e.getMessage());
	    throw e;
	}
    }

    // Identification methods

    static void makeIdentifications(ProtexIdUtils codeMatchUtils)
	    throws Exception {
	String path = "/";
	PartialCodeTree fullTreeFiles = codeMatchUtils
		.getAllCodeTreeFiles(path);
	makeACodeMatchDiscoveryId(codeMatchUtils, path, fullTreeFiles);
	codeMatchUtils.refreshBom();
    }

    private static void makeACodeMatchDiscoveryId(ProtexIdUtils codeMatchUtils,
	    String path, PartialCodeTree fullTreeFiles) throws SdkFault {
	List<CodeTreeNode> fileNodes = fullTreeFiles.getNodes();
	CodeMatchDiscovery codeMatchDiscovery = getACodeMatchDiscovery(
		codeMatchUtils, path, fileNodes);
	codeMatchUtils.makeId(codeMatchDiscovery.getFilePath(),
		codeMatchDiscovery);
    }

    private static CodeMatchDiscovery getACodeMatchDiscovery(
	    ProtexIdUtils codeMatchUtils, String path,
	    List<CodeTreeNode> fileNodes) throws SdkFault {

	List<CodeMatchDiscovery> codeMatchDiscoveries = codeMatchUtils
		.getCodeMatchDiscoveries(path, fileNodes);
	assertTrue(codeMatchDiscoveries.size() > 0);
	return codeMatchDiscoveries.get(0);
    }

    // End Identifying methods

}
