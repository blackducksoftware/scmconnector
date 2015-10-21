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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.codetree.PartialCodeTree;
import com.blackducksoftware.sdk.protex.project.codetree.identification.CodeTreeIdentificationInfo;
import com.blackducksoftware.sdk.protex.project.codetree.identification.Identification;
import com.blackducksoftware.sdk.protex.project.codetree.identification.IdentificationMode;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;

public class ProtexIdentifications {
    private final ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper;
    private final Map<Project, List<Identification>> rapidIdLists = new HashMap<Project, List<Identification>>();
    private final Map<Project, List<Identification>> manualIdLists = new HashMap<Project, List<Identification>>();

    public ProtexIdentifications(
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper) {
	this.protexServerWrapper = protexServerWrapper;
    }

    public int getManualCount(Project project) throws Exception {
	populateIdLists(project);
	List<Identification> manualIdList = manualIdLists.get(project);
	return manualIdList.size();
    }

    public int getRapidCount(Project project) throws Exception {
	populateIdLists(project);
	List<Identification> rapidIdList = rapidIdLists.get(project);
	return rapidIdList.size();
    }

    /**
     * Populate and cache ID lists for this project.
     *
     * @param project
     * @throws Exception
     */
    private void populateIdLists(Project project) throws Exception {

	if (manualIdLists.get(project) != null) {
	    return;
	}

	List<Identification> rapidIds = new ArrayList<Identification>();
	rapidIdLists.put(project, rapidIds);

	List<Identification> manualIds = new ArrayList<Identification>();
	manualIdLists.put(project, manualIds);

	PartialCodeTree nodes;
	List<CodeTreeIdentificationInfo> codeTreeIdentificationInfoList;
	try {
	    nodes = ProtexUtils.getCodeTreeForProjectDeep(protexServerWrapper,
		    project);
	    codeTreeIdentificationInfoList = protexServerWrapper
		    .getInternalApiWrapper().getIdentificationApi()
		    .getAppliedIdentifications(project.getProjectId(), nodes);
	} catch (SdkFault e) {
	    throw new Exception("Error getting identifications for project "
		    + project.getName() + ": " + e.getMessage());
	}

	if (codeTreeIdentificationInfoList == null) {
	    return;
	}
	for (CodeTreeIdentificationInfo info : codeTreeIdentificationInfoList) {
	    List<Identification> ids = info.getIdentifications();
	    if (ids == null) {
		continue;
	    }
	    for (Identification id : ids) {
		IdentificationMode mode = id.getMode();
		switch (mode) {
		case RAPID_ID:
		    rapidIds.add(id);
		    break;
		case MANUAL:
		    manualIds.add(id);
		    break;
		default:
		    throw new Exception("Unexpected IdentificationMode: "
			    + mode.toString());
		}
	    }
	}
    }
}
