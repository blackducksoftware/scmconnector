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
package com.blackducksoftware.tools.scmconnector.core.protex;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.bom.FileCountType;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.PartialCodeTree;
import com.blackducksoftware.sdk.protex.project.codetree.PartialCodeTreeWithCount;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisInfo;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

// helper class utilized to executed common functions of the Protex SDK
public class ProtexUtils {
    private final ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper;

    public ProtexUtils(
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper) {
	this.protexServerWrapper = protexServerWrapper;
    }

    public AnalysisInfo getLastAnalysisInfo(Project project) throws SdkFault {
	return protexServerWrapper.getInternalApiWrapper().getDiscoveryApi()
		.getLastAnalysisInfo(project.getProjectId());
    }

    // returns the total number of files scanned for a project
    public int getFileCount(Project project) {

	PartialCodeTreeWithCount countTree = null;

	PartialCodeTree tree;
	try {
	    tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper.getInternalApiWrapper()
		    .getCodeTreeApi()
		    .getFileCount(project.getProjectId(), tree);
	} catch (SdkFault e) {
	    return 0;
	}
	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }

    // returns the total number of files with Pending Identification status for
    // a project
    public int getPendingIdFileCount(Project project) {

	PartialCodeTreeWithCount countTree = null;

	PartialCodeTree tree;

	try {
	    tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper
		    .getInternalApiWrapper()
		    .getDiscoveryApi()
		    .getAllDiscoveriesPendingIdFileCount(
			    project.getProjectId(), tree);
	} catch (SdkFault e) {

	    return 0;
	}

	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }

    // returns the total number of files with Pending String Search
    // Identification for a project
    public int getPendingStringSearchFileCount(Project project) {

	PartialCodeTreeWithCount countTree = null;

	PartialCodeTree tree;
	try {
	    tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper
		    .getInternalApiWrapper()
		    .getDiscoveryApi()
		    .getStringSearchPendingIdFileCount(project.getProjectId(),
			    tree);
	} catch (SdkFault e) {
	    return 0;
	}
	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }

    // returns the total number of files with Pending Dependency Identification
    // for a project
    public int getPendingDependenciesIdFileCount(Project project) {

	PartialCodeTreeWithCount countTree = null;
	try {
	    PartialCodeTree tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper
		    .getInternalApiWrapper()
		    .getDiscoveryApi()
		    .getDependenciesPendingIdFileCount(project.getProjectId(),
			    tree);
	} catch (SdkFault e) {
	    return 0;
	}

	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }

    /**
     * Get the Pending Review count for the project.
     *
     * @param project
     * @return
     */
    public int getPendingReviewCount(Project project) {
	PartialCodeTreeWithCount countTree = null;
	try {
	    PartialCodeTree tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper
		    .getInternalApiWrapper()
		    .getBomApi()
		    .getFileCount(project.getProjectId(), tree,
			    FileCountType.PENDING_REVIEW);
	} catch (SdkFault e) {
	    return 0;
	}

	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }

    private PartialCodeTree getCodeTreeForProject(Project project)
	    throws SdkFault {
	return ProtexUtils.getCodeTreeForProject(protexServerWrapper, project);
    }

    static PartialCodeTree getCodeTreeForProject(
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    Project project) throws SdkFault {
	List<CodeTreeNodeType> nodeTypesToInclude = new ArrayList<CodeTreeNodeType>();
	nodeTypesToInclude.add(CodeTreeNodeType.FOLDER);
	nodeTypesToInclude.add(CodeTreeNodeType.EXPANDED_ARCHIVE);
	nodeTypesToInclude.add(CodeTreeNodeType.FILE);
	PartialCodeTree tree = protexServerWrapper
		.getInternalApiWrapper()
		.getCodeTreeApi()
		.getCodeTreeByNodeTypes(project.getProjectId(), "/", 0, true,
			nodeTypesToInclude);

	return tree;
    }

    static PartialCodeTree getCodeTreeForProjectDeep(
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    Project project) throws SdkFault {
	List<CodeTreeNodeType> nodeTypesToInclude = new ArrayList<CodeTreeNodeType>();
	nodeTypesToInclude.add(CodeTreeNodeType.FOLDER);
	nodeTypesToInclude.add(CodeTreeNodeType.EXPANDED_ARCHIVE);
	nodeTypesToInclude.add(CodeTreeNodeType.FILE);
	PartialCodeTree tree = protexServerWrapper
		.getInternalApiWrapper()
		.getCodeTreeApi()
		.getCodeTreeByNodeTypes(project.getProjectId(), "/", -1, true,
			nodeTypesToInclude);

	return tree;
    }

    // returns the total number of files with Pending File Pattern
    // Identification for a project
    public int getPendingFileDiscoveryPatternIdFileCount(Project project) {

	PartialCodeTreeWithCount countTree = null;

	PartialCodeTree tree;
	try {
	    tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper
		    .getInternalApiWrapper()
		    .getDiscoveryApi()
		    .getFileDiscoveryPatternPendingIdFileCount(
			    project.getProjectId(), tree);
	} catch (SdkFault e) {
	    return 0;
	}
	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }

    // returns the total number of files with Pending Code Match Identification
    // for a project
    public int getPendingCodeMatchIdFileCount(Project project) {

	PartialCodeTreeWithCount countTree = null;

	PartialCodeTree tree;
	try {
	    tree = getCodeTreeForProject(project);

	    countTree = protexServerWrapper
		    .getInternalApiWrapper()
		    .getDiscoveryApi()
		    .getCodeMatchPendingIdFileCount(project.getProjectId(),
			    tree);
	} catch (SdkFault e) {
	    return 0;
	}
	if (countTree.getNodes().size() == 0) {
	    return 0;
	}

	return countTree.getNodes().get(0).getCount();
    }
}
