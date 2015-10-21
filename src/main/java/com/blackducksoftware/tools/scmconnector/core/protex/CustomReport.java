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

import java.io.File;

import org.apache.poi.ss.usermodel.Workbook;

import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.connector.protex.report.ReportUtils;
import com.blackducksoftware.tools.commonframework.core.config.ConfigurationManager;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.commonframework.standard.workbook.WorkbookWriter;

public class CustomReport {
    private final ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper;
    private final ConfigurationManager config;
    private final WorkbookWriter workbookWriter;
    private final ReportUtils reportUtils = new ReportUtils();

    public CustomReport(
	    ProtexServerWrapper<ProtexProjectPojo> protexServerWrapper,
	    ConfigurationManager config, WorkbookWriter workbookWriter) {
	this.protexServerWrapper = protexServerWrapper;
	this.config = config;
	this.workbookWriter = workbookWriter;
    }

    public void generateCustomReportFromTemplate(String projectName,
	    String projectId, String reportTemplateFileName) throws Exception {

	Workbook wb = reportUtils.getReportSectionBySection(
		protexServerWrapper, projectName, new File(
			reportTemplateFileName), config);
	workbookWriter.write(wb);
    }
}
