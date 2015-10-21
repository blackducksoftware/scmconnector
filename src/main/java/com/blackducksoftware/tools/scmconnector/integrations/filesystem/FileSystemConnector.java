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
package com.blackducksoftware.tools.scmconnector.integrations.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;
import com.blackducksoftware.tools.scmconnector.core.ConnectorPropertyRetriever;

/**
 * Connector that
 *
 * @author akamen
 *
 */
public class FileSystemConnector extends Connector {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    // Location of where the source resides.
    String repoURL;

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	repoURL = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);

    }

    @Override
    public String getName() {
	return "File System";
    }

    @Override
    public String getRepositoryPath() {
	String repoPath = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);
	return repoPath;
    }

    @Override
    public int sync() {
	if (repoURL != null) {
	    File sourceLocation = new File(repoURL);
	    File targetLocation = new File(getFinalSourceDirectory());
	    if (!sourceLocation.exists()) {
		log.error("Unable to find repository location: " + repoURL);
		return -1;
	    }

	    boolean created = createTargetLocation(targetLocation);
	    if (!created) {
		return -1;
	    }

	    try {
		log.info("Copying source: " + sourceLocation);
		log.info("To destination: " + targetLocation);

		FileUtils.copyDirectoryToDirectory(sourceLocation,
			targetLocation);
		log.info("Finished copying");
	    } catch (IOException ioe) {
		log.error("Unable to copy directory: " + ioe.getMessage());
		return -1;

	    }

	    // return copyDirectory(targetLocation, destinationLocation));
	}
	return 0;
    }

    private boolean createTargetLocation(File targetLocation) {
	boolean createdTarget = true;
	if (!targetLocation.exists()) {
	    log.info("Creating directory " + targetLocation);
	    boolean success = targetLocation.mkdir();
	    if (!success) {
		log.warn("Unable to create directory: " + targetLocation);
		log.warn("Please create the directory manually.");
		return false;
	    }
	}

	return createdTarget;
    }
}
