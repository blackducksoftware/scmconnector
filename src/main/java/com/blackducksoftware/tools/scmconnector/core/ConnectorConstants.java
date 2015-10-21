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

/**
 * Constants to keep track of all the properties
 * 
 * @author akamen
 *
 */
public class ConnectorConstants {

    // / Global properties
    public static final String PROTEX_RAPID_ID_FLAG = "protex.run_rapidid";
    // Location of the source that Protex uses when scanning on the same server.
    public static final String PROTEX_SOURCE_DIRECTORY = "protex.home_directory";
    public static final String PROJECT_DESCRIPTION = "Project created using BlackDuck SCM Connector technology";

    // If true, this property tells SCM Connector to only run the scan for the
    // last connector.x
    public static final String PROTEX_SCAN_ONLY_LAST_CONNECTOR = "perform.aggregate.scan";

    // If false, this property tells SCM Connector to skip the Protex scan;
    // defaults to true
    public static final String NO_PROTEX_SCAN = "perform.scan";
    public static final String SKIP_EMAIL_IF_NO_DELTAS = "skip.email.if.no.deltas";
    public static final String EMAIL_CONTENT_FILE_PATH = "email.content.file.path";

    // / General Connector Properties
    public static final String CONNECTOR_PROPERTY_REPO_URL = "repositoryURL";
    public static final String CONNECTOR_PROPERTY_ROOT = "root";
    public static final String CONNECTOR_PROPERTY_PROTEX_PROJECT_NAME = "new_project_name";
    public static final String CONNECTOR_PROPERTY_PROTEX_PROJECT_USERS = "project_users";

    // If true, set CloneOption.ANALYSIS_RESULTS (in addition to COMPLETED_WORK
    // and ASSIGNED_USERS)
    public static final String COPY_ANALYSIS_RESULTS_FROM_TEMPLATE = "copy.analysis.results.from.template";

    public static final String CONNECTOR_PROPERTY_USER = "user";
    public static final String CONNECTOR_PROPERTY_PASSWORD = "password";

    // Perforce Connector
    public static final String PERFORCE_PROPERTY_SERVER = "port";

    // Git connector
    public static final String GIT_PROPERTY_BRANCH = "branch";
    public static final String GIT_PROPERTY_SHA = "sha";
    public static final String GIT_PROPERTY_TAG = "tag";
    public static final String GIT_PROPERTY_DISABLESTRICTHOSTKEYCHECKING = "disablehostcheck";

    // Scan type
    public static final String SCAN_TYPE_BASELINE = "Initial Baseline";
    public static final String SCAN_TYPE_DELTA = "Delta Scan";

    public static final String MAX_CONNECTOR_INDEX_PROPERTY = "max.connector.index";
    public static final int MAX_CONNECTOR_INDEX_DEFAULT = 1000;
}
