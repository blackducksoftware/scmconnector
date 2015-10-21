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

import org.apache.log4j.Logger;

import com.blackducksoftware.tools.commonframework.core.exception.CommonFrameworkException;
import com.blackducksoftware.tools.commonframework.standard.email.EmailContentMap;
import com.blackducksoftware.tools.commonframework.standard.email.EmailNotifier;
import com.blackducksoftware.tools.commonframework.standard.email.EmailTemplate;

/**
 * Handles email notification decisions, and location of the email content file.
 * Delegates decision making (send / don't send) to AnalysisResults class.
 * Delegates email generation, and sending to Common Framework.
 *
 * @author sbillings
 *
 */
public class Notifier {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final EmailNotifier emailNotif;
    private final String notificationEmailFilePath;

    public Notifier(ConnectorConfig config, EmailNotifier emailNotif,
	    String defaultNotificationEmailFilePath) {
	this.emailNotif = emailNotif;
	String userEmailContentFilePath = config.getEmailContentFilePath();
	if (userEmailContentFilePath != null) {
	    notificationEmailFilePath = userEmailContentFilePath;
	} else {
	    notificationEmailFilePath = defaultNotificationEmailFilePath;
	}
    }

    /**
     * Sends an analysis summary email notification, if appropriate.
     *
     * @param connectorName
     * @param repositoryPath
     * @param aggregatedRepositoryPath
     * @param projectAnalysisResults
     * @return email body, or null if no email was sent.
     * @throws CommonFrameworkException
     */
    public String sendNotificationEmail(String connectorName,
	    String repositoryPath, String aggregatedRepositoryPath,
	    AnalysisResults projectAnalysisResults)
	    throws CommonFrameworkException {

	EmailContentMap values = emailNotif
		.configureContentMap(notificationEmailFilePath);
	projectAnalysisResults.populateEmailContentMap(connectorName,
		repositoryPath, aggregatedRepositoryPath, values);

	log.info("Sending email regarding project: "
		+ projectAnalysisResults.getProjectPojo().getProjectName());
	EmailTemplate emailTemplate = emailNotif
		.sendEmail(projectAnalysisResults.getProjectPojo(), values,
			projectAnalysisResults
				.calculateNotificationTriggerRuleStates());

	if (emailTemplate == null) {
	    return null;
	}
	String emailBody = emailTemplate.getBody();
	return emailBody;
    }

}
