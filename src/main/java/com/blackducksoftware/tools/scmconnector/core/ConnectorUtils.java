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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

/**
 * @author akamen
 * 
 *         All purpose class for convenient utility methods
 */
public class ConnectorUtils {

    private static final Logger log = Logger.getLogger(ConnectorUtils.class);

    private static final String HOSTNAME_WINDOWS = "COMPUTERNAME";
    private static final String HOSTNAME_LINUX = "HOSTNAME";

    public static String getHostFromUri(String uri)
	    throws MalformedURLException {
	URL url = null;
	try {
	    url = new URL(uri);
	} catch (MalformedURLException e) {
	    log.error("Could not convert Server URI to URL", e);
	    throw e;
	}
	return url.getHost();
    }

    public static String getHostName() {
	String hostName = null;
	if (System.getProperty("os.name").startsWith("Windows")) {
	    // Windows will always set the 'COMPUTERNAME' variable
	    hostName = System.getenv(HOSTNAME_WINDOWS.toLowerCase());
	} else {
	    // If it is not Windows then it is most likely a Unix-like operating
	    // system
	    // such as Solaris, AIX, HP-UX, Linux or MacOS.

	    // Most modern shells (such as Bash or derivatives) sets the
	    // HOSTNAME variable so lets try that first.
	    hostName = System.getenv(HOSTNAME_LINUX.toLowerCase());
	    try {
		if (hostName == null) {
		    InetAddress addr = InetAddress.getLocalHost();
		    hostName = addr.getHostName();
		}
	    } catch (UnknownHostException e) {
		e.printStackTrace();
	    }
	}

	log.info("Found host name: " + hostName);

	return hostName;
    }
}
