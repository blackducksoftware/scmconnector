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
package com.blackducksoftware.tools.scmconnector.integrations.ftp;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationPassword;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConstants;
import com.blackducksoftware.tools.scmconnector.core.ConnectorPropertyRetriever;

/**
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

public class FTPConnector extends Connector {

    private final Logger log = Logger.getLogger(this.getClass().getName());

    private String server;
    private String path;
    private String user;
    private String password;

    @Override
    public void init(Properties prps) throws Exception {

	super.init(prps);

	server = prps.getProperty("server");
	path = ConnectorPropertyRetriever
		.getPropertyValue(ConnectorConstants.CONNECTOR_PROPERTY_REPO_URL);
	user = prps.getProperty("user");

	ConfigurationPassword psw = ConfigurationPassword
		.createFromPropertyNoPrefix(prps);
	password = psw.getPlainText();
    }

    @Override
    public String getName() {

	return "FTP";
    }

    @Override
    public String getRepositoryPath() {

	return path;
    }

    @Override
    public int sync() {
	try {

	    FTPClient client = new FTPClient();
	    client.connect(server);
	    client.login(user, password);
	    client.changeDirectory(path);
	    FTPFile[] list = client.list();

	    String parentPath = getFinalSourceDirectory();

	    File dir = new File(parentPath);

	    if (!dir.exists()) {
		dir.mkdir();
	    }

	    deleteFileInTargetDirectoryNotExistingInSource(dir, list);

	    for (FTPFile f : list) {

		if (f.getType() == FTPFile.TYPE_FILE) {
		    File targetFile = new File(parentPath, f.getName());

		    if (f.getModifiedDate().getTime() != targetFile
			    .lastModified()) {

			log.info("Obtaining file: " + f.getName());

			client.download(f.getName(), targetFile);
			targetFile.setLastModified(f.getModifiedDate()
				.getTime());
		    }
		} else if (f.getType() == FTPFile.TYPE_DIRECTORY) {
		    log.info("Inspecting directory: " + f.getName());
		    downloadFiles(parentPath, client, f);
		}

	    }

	} catch (Exception e) {
	    log.error("Error performing FTP operation", e);
	    return 1;
	}
	return 0;

    }

    private void downloadFiles(String parentPath, FTPClient client,
	    FTPFile folder) throws IllegalStateException,
	    FileNotFoundException, IOException, FTPIllegalReplyException,
	    FTPException, FTPDataTransferException, FTPAbortedException,
	    FTPListParseException {
	client.changeDirectory(folder.getName());
	File dir = new File(parentPath, folder.getName());
	if (!dir.exists()) {
	    dir.mkdir();
	}

	FTPFile[] list = client.list();

	deleteFileInTargetDirectoryNotExistingInSource(dir, list);

	for (FTPFile f : list) {
	    if (f.getType() == FTPFile.TYPE_FILE) {
		File targetFile = new File(dir.getPath(), f.getName());

		if (f.getModifiedDate().getTime() != targetFile.lastModified()) {
		    log.info("Obtaining file: " + f.getName());

		    client.download(f.getName(), targetFile);
		    targetFile.setLastModified(f.getModifiedDate().getTime());
		}
	    } else if (f.getType() == FTPFile.TYPE_DIRECTORY) {
		log.info("Inspecting directory: " + f.getName());
		downloadFiles(dir.getPath(), client, f);
	    }
	}
	client.changeDirectoryUp();
    }

    private void deleteFileInTargetDirectoryNotExistingInSource(File dir,
	    FTPFile[] list) throws IOException {
	File[] children = dir.listFiles();

	for (File element : children) {
	    boolean found = false;

	    for (FTPFile element2 : list) {
		if (element2.getName().equals(element)) {
		    found = true;
		}

	    }

	    if (!found) {
		log.info("Deleting local file/folder "
			+ element.getAbsolutePath()
			+ " because does not exist anymore in repository");
		FileUtils.forceDelete(element);
	    }
	}

    }

}
