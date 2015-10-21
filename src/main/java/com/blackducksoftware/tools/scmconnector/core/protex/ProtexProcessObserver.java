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

import java.util.Enumeration;
import java.util.Map;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.blackducksoftware.protex.plugin.event.AbstractAnalysisListener;
import com.blackducksoftware.protex.plugin.event.AnalysisEvent;

/**
 * Listener that picks up information from the BDS plugin Observer. Reports on
 * the percent completed for file analysis. Information derived from the
 * bds_tool output.
 *
 * Arbitrary set to a rough counter of "mod 500".
 *
 * @author akamen
 *
 */
public class ProtexProcessObserver extends AbstractAnalysisListener {

    private static final int STATUS_REPORT_INTERVAL = 500;
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final Level level;

    private static final String COMPLETION_PERCENT = "completion_percent";

    private Long counter = new Long(0);
    private boolean loggersCleaned = false;

    public ProtexProcessObserver(Level lvl) {
	level = lvl;
    }

    @Override
    public void analysisInitiated(AnalysisEvent event) {
    }

    @Override
    public void analysisStarted(AnalysisEvent event) {
    }

    @Override
    public void analysisProgressed(AnalysisEvent event) {
	// Because BDSTool appends its own logging appender, we want to get rid
	// of it.
	if (!loggersCleaned) {
	    cleanUpLoggerAppenders();
	}

	// Since it appends the root logger, it also takes over the level. Set
	// it to the level we care about.
	log.setLevel(level);

	if (counter % STATUS_REPORT_INTERVAL == 0) {
	    try {

		Map<String, ?> statusMap = event.status();
		Object compPercent = statusMap.get(COMPLETION_PERCENT);

		if (compPercent instanceof Long) {
		    Long compPercentInt = (Long) compPercent;
		    if (compPercentInt > 0) {
			log.info("Completion percent: " + compPercentInt + "%");
		    }

		}
	    } catch (Throwable e) {
		log.warn("Could not get update" + e.getMessage());
	    }
	}
	counter++;
    }

    @Override
    public void analysisSucceeded(AnalysisEvent event) {
	log.info("Scanning completed.");
    }

    @Override
    public void analysisFailed(AnalysisEvent event) {
	log.info("failed");

    }

    private void cleanUpLoggerAppenders() {
	Enumeration<Appender> appenders = Logger.getRootLogger()
		.getAllAppenders();
	while (appenders.hasMoreElements()) {
	    Appender app = appenders.nextElement();
	    if (app.getName() == null || app.getName().length() == 0) {
		Logger.getRootLogger().removeAppender(app);
		loggersCleaned = true;
	    }
	}
    }
}
