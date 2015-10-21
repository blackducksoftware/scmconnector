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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationManager;
import com.blackducksoftware.tools.commonframework.core.config.EmailBean;

public class ConnectorConfig extends ConfigurationManager {
    private final Logger log = Logger.getLogger(this.getClass().getName());

    // Default both to false.
    // isLocal will only be set to true programmatically by matching the
    // hostname.
    // Determines whether the Proxy is local. If proxy is local we get to use
    // the SDK, otherwise the BDS tool is invoked.
    private boolean isProxyLocal = false;
    private boolean runRapidId = false;

    // Protex location that is only used when isLocal = true;
    private String protexHomeDirectory = "";

    private boolean doProtexScan = true;
    private boolean protexScanOnlyLastConnector = false;
    private boolean copyAnalysisResultsFromTemplate = false;
    private int maxConnectorIndex = ConnectorConstants.MAX_CONNECTOR_INDEX_DEFAULT;
    private String emailContentFilePath;

    private final Map<String, String> notificationSubstitutionValues = new HashMap<String, String>();

    public ConnectorConfig() {
	super();
    }

    public ConnectorConfig(String propFilePath) throws Exception {
	super(propFilePath, APPLICATION.PROTEX);
	init();
    }

    public ConnectorConfig(Properties configProperties) throws Exception {
	super(configProperties, APPLICATION.PROTEX);
	init();
    }

    private void init() throws Exception {
	initGlobals();
	initProtexHomeDirectory();
	initProtexScanning();
	initEmail();
	initNotificationSubstitutions();
    }

    private void initEmail() {

	checkForUnsupportedProperty(
		ConnectorConstants.SKIP_EMAIL_IF_NO_DELTAS,
		"The new way to configure email notification is explained in the Email Notification section of the README file");

	String propValue = getOptionalProperty(ConnectorConstants.EMAIL_CONTENT_FILE_PATH);
	if (propValue != null) {
	    emailContentFilePath = propValue;
	}

	// CF's ConfigurationManager loads email.smtp.*, where-as
	// SCM Connector supports smtp.*. Would be nice to move SCM Connector
	// to the CF standard property names, but that will require all users
	// to change their config files.
	EmailBean emailConfiguration = super.getEmailConfiguration();
	emailConfiguration.setSmtpAddress(getOptionalProperty("smtp.address"));
	emailConfiguration.setSmtpTo(getOptionalProperty("smtp.to"));
	emailConfiguration.setSmtpFrom(getOptionalProperty("smtp.from"));
	emailConfiguration.setUseAuth(getOptionalProperty("smtp.use_auth",
		false, Boolean.class));
	emailConfiguration.setAuthPassword(getOptionalProperty("smtp.password",
		"", String.class));
	emailConfiguration.setAuthUserName(getOptionalProperty("smtp.username",
		"", String.class));
	emailConfiguration.setEmailProtocol(getOptionalProperty(
		"smtp.protocol", "smtp", String.class));
	emailConfiguration.setSmtpPort(getOptionalProperty("smtp.port", 25,
		Integer.class));
    }

    /**
     * Read properties used for substituting user content into email
     * notifications. They look like:
     * notification.substitution.0.variable=myvariable
     * notification.substitution.0.value=myvalue
     */
    private void initNotificationSubstitutions() {
	for (int i = 0;; i++) {
	    String variableName = getOptionalProperty("notification.substitution"
		    + "." + i + "." + "variable");
	    if ((variableName == null) || (variableName.length() == 0)) {
		break;
	    }
	    String variableValue = getOptionalProperty("notification.substitution"
		    + "." + i + "." + "value");
	    notificationSubstitutionValues.put(variableName, variableValue);
	}
    }

    private void initProtexScanning() {

	doProtexScan = true;
	String propValue = getOptionalProperty(ConnectorConstants.NO_PROTEX_SCAN);
	if ((propValue != null) && ("false".equals(propValue))) {
	    doProtexScan = false;
	}

	propValue = getOptionalProperty(ConnectorConstants.PROTEX_SCAN_ONLY_LAST_CONNECTOR);
	protexScanOnlyLastConnector = "true".equals(propValue);

	propValue = getOptionalProperty(ConnectorConstants.COPY_ANALYSIS_RESULTS_FROM_TEMPLATE);
	copyAnalysisResultsFromTemplate = "true".equals(propValue);
    }

    public Map<String, String> getNotificationSubstitutionValues() {
	return notificationSubstitutionValues;
    }

    public boolean isProtexScanOnlyLastConnector() {
	return protexScanOnlyLastConnector;
    }

    public boolean isDoProtexScan() {
	return doProtexScan;
    }

    public void setDoProtexScan(boolean doProtexScan) {
	this.doProtexScan = doProtexScan;
    }

    public boolean isProxyLocal() {
	return isProxyLocal;
    }

    public boolean isCopyAnalysisResultsFromTemplate() {
	return copyAnalysisResultsFromTemplate;
    }

    public void setCopyAnalysisResultsFromTemplate(
	    boolean copyAnalysisResultsFromTemplate) {
	this.copyAnalysisResultsFromTemplate = copyAnalysisResultsFromTemplate;
    }

    /**
     * Also blanks out the protex location
     *
     * @param isLocal
     */
    public void setProxyLocal(boolean isLocal) {
	isProxyLocal = isLocal;
	if (!isLocal) {
	    protexHomeDirectory = "";
	    log.info("Setting proxy to false and nulling out the protex directory");
	}
    }

    public boolean isRunRapidId() {
	return runRapidId;
    }

    public void setRunRapidId(boolean runRapidId) {
	this.runRapidId = runRapidId;
    }

    public String getProtexHomeDirectory() {
	return protexHomeDirectory;
    }

    public void setProtexHomeDirectory(String protexHomeDirectory) {
	this.protexHomeDirectory = protexHomeDirectory;
    }

    public int getMaxConnectorIndex() {
	return maxConnectorIndex;
    }

    public void setMaxConnectorIndex(int maxConnectorIndex) {
	this.maxConnectorIndex = maxConnectorIndex;
    }

    public String getEmailContentFilePath() {
	return emailContentFilePath;
    }

    /**
     * Get property value or return default if it's not set.
     *
     * @param propertyKey
     *            The property's key
     * @param defaultValue
     *            The default value to return if it's not set
     * @return The value, or the default value if it's not set
     */
    public String getProperty(String propertyKey, String defaultValue) {
	String value = getOptionalProperty(propertyKey);
	if (value == null) {
	    value = defaultValue;
	}

	return value;
    }

    private void initGlobals() {
	// denotes whether Rapid ID should be automatically run for the project
	String runRapidIdString = this
		.getOptionalProperty(ConnectorConstants.PROTEX_RAPID_ID_FLAG);
	boolean runRapidId = "true".equals(runRapidIdString);
	setRunRapidId(runRapidId);

	// limit on how many connectors to check for
	String maxConnectorIndexString = this
		.getOptionalProperty(ConnectorConstants.MAX_CONNECTOR_INDEX_PROPERTY);
	if (maxConnectorIndexString != null) {
	    int maxConnectorIndex = ConnectorConstants.MAX_CONNECTOR_INDEX_DEFAULT;
	    try {
		maxConnectorIndex = Integer.parseInt(maxConnectorIndexString);
		setMaxConnectorIndex(maxConnectorIndex);
	    } catch (Exception e) {
		// ignore invalid values
	    }
	}
    }

    public void copyPropertiesWithPrefix(Properties destinationProperties,
	    String prefix) {

	Enumeration<Object> keys = getProps().keys();
	while (keys.hasMoreElements()) {
	    String propertyName = (String) keys.nextElement();
	    String propertyValue = getProps().getProperty(propertyName);
	    if (propertyName.startsWith(prefix) && (propertyValue != null)) {
		destinationProperties.put(
			propertyName.substring(prefix.length()), propertyValue);
	    }

	}
    }

    private void initProtexHomeDirectory() {
	String protexHomeDirectory = this
		.getOptionalProperty(ConnectorConstants.PROTEX_SOURCE_DIRECTORY);
	setProtexHomeDirectory(protexHomeDirectory);
    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();

	buf.append("Server: " + getServerBean().getServerName());
	buf.append("\n");
	buf.append("User: " + getServerBean().getUserName());
	buf.append("\n");
	buf.append("Protex directory: " + getProtexHomeDirectory());
	buf.append("\n");
	buf.append("Rapid ID: " + isRunRapidId());
	buf.append("\n");
	buf.append("Local Proxy: " + isProxyLocal());
	buf.append("\n");
	return buf.toString();
    }

}
