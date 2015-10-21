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
package com.blackducksoftware.tools.scmconnector.integrations.nop;

import java.util.Properties;

import com.blackducksoftware.tools.scmconnector.core.Connector;

/**
 * The "no operation" (= do nothing) connector. Use this connector when you just
 * need to (Protex) scan a set of source code that is already sitting on the
 * local file system. In other words, you don't need the SCM checkout part,
 * since the source is already available.
 * 
 * @author akamen
 *
 */
public class NOPConnector extends Connector {

    // Location of where the source resides.
    String repoURL;

    @Override
    public void init(Properties prps) throws Exception {
	super.init(prps);
	repoURL = getRepositoryPath();
    }

    @Override
    public String getName() {
	return "NOP (no operation)";
    }

    @Override
    public String getRepositoryPath() {
	return "<not applicable>";
    }

    /**
     * Sync = do nothing.
     */
    @Override
    public int sync() {
	return 0;
    }
}
