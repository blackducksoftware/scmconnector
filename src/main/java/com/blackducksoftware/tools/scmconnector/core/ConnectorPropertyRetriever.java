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
/**
 *
 */
package com.blackducksoftware.tools.scmconnector.core;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 *
 * The fact that the data in this class is static means we can't run connectors
 * in parallel. See Jira ticket PROSERVSCM-122.
 *
 * @author akamen
 *
 */
public class ConnectorPropertyRetriever {

    protected static final Logger log = Logger
	    .getLogger(ConnectorPropertyRetriever.class);

    private static Properties properties;

    public ConnectorPropertyRetriever(Properties props) {
	properties = props;
    }

    public static String getPropertyValue(String key) {
	if (properties != null) {
	    String value = properties.getProperty(key);
	    if (value != null && value.length() > 0) {
		return value;
	    } else {
		log.warn("Property is either missing or empty: " + key);
		return value;
	    }
	} else {
	    // Should not happen!
	    log.error("Properties not initialized!");
	    return null;
	}
    }

    public static Properties getProperties() {
	return properties;
    }

}
