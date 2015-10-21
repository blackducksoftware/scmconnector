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
package com.blackducksoftware.tools.scmconnector.core.protex;

import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;

/**
 * @author akamen Determines which project implementation is returned
 */
public class ProjectCreatorFactory {

    public static ProjectCreator getCreator(ConnectorConfig config,
	    Connector connector) throws Exception {
	ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper = new ProtexServerWrapper<>(
		config.getServerBean(), config, true);
	ProjectCreator project = null;
	boolean isProxyLocal = config.isProxyLocal();
	if (isProxyLocal) {
	    project = new LocalProjectCreator(config, protexServerWrapper,
		    connector);
	} else {
	    project = new RemoteProjectCreator(config, protexServerWrapper,
		    connector);
	}

	return project;
    }
}
