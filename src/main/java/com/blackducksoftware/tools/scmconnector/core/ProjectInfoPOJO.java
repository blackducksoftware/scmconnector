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

import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisInfo;

/**
 * Analysis results details. Should no longer be accessed directly. Use
 * AnalysisResults instead. AnalysisResults still uses this class to store the
 * details.
 *
 * @author jatoui
 * @title Solutions Architect
 * @email jatoui@blackducksoftware.com
 * @company Black Duck Software
 * @year 2012
 **/

public class ProjectInfoPOJO {
    private AnalysisInfo analysisInfo;

    private int preAnalysisFileCount;
    private int preAnalysisPendingFileCount;
    private int preAnalysisCodeMatchPendingIdFileCount;
    private int preAnalysisStringSearchPendingIdFileCount;
    private int preAnalysisDependencyPendingIdFileCount;
    private int preAnalysisFilePatternMatchPendingIdFileCount;
    private int preAnalysisRapidIdCount;
    private int preAnalysisPendingReviewFileCount;

    private int postAnalysisFileCount;
    private int postAnalysisPendingFileCount;
    private int postAnalysisCodeMatchPendingIdFileCount;
    private int postAnalysisStringSearchPendingIdFileCount;
    private int postAnalysisDependencyPendingIdFileCount;
    private int postAnalysisFilePatternMatchPendingIdFileCount;
    private int postAnalysisRapidIdCount;
    private int postAnalysisPendingReviewFileCount;

    private String scanType;
    private Project project;
    private String bomUrl;

    public String getBomUrl() {
	return bomUrl;
    }

    public void setBomUrl(String bomUrl) {
	this.bomUrl = bomUrl;
    }

    public AnalysisInfo getAnalysisInfo() {
	return analysisInfo;
    }

    public void setAnalysisInfo(AnalysisInfo analysisInfo) {
	this.analysisInfo = analysisInfo;
    }

    public int getPreAnalysisFileCount() {
	return preAnalysisFileCount;
    }

    public void setPreAnalysisFileCount(int preAnalysisFileCount) {
	this.preAnalysisFileCount = preAnalysisFileCount;
    }

    public int getPreAnalysisPendingFileCount() {
	return preAnalysisPendingFileCount;
    }

    public void setPreAnalysisPendingFileCount(int preAnalysisPendingFileCount) {
	this.preAnalysisPendingFileCount = preAnalysisPendingFileCount;
    }

    public int getPreAnalysisCodeMatchPendingIdFileCount() {
	return preAnalysisCodeMatchPendingIdFileCount;
    }

    public void setPreAnalysisCodeMatchPendingIdFileCount(
	    int preAnalysisCodeMatchPendingIdFileCount) {
	this.preAnalysisCodeMatchPendingIdFileCount = preAnalysisCodeMatchPendingIdFileCount;
    }

    public int getPreAnalysisStringSearchPendingIdFileCount() {
	return preAnalysisStringSearchPendingIdFileCount;
    }

    public void setPreAnalysisStringSearchPendingIdFileCount(
	    int preAnalysisStringSearchPendingIdFileCount) {
	this.preAnalysisStringSearchPendingIdFileCount = preAnalysisStringSearchPendingIdFileCount;
    }

    public int getPreAnalysisDependencyPendingIdFileCount() {
	return preAnalysisDependencyPendingIdFileCount;
    }

    public void setPreAnalysisDependencyPendingIdFileCount(
	    int preAnalysisDependencyPendingIdFileCount) {
	this.preAnalysisDependencyPendingIdFileCount = preAnalysisDependencyPendingIdFileCount;
    }

    public int getPreAnalysisFilePatternMatchPendingIdFileCount() {
	return preAnalysisFilePatternMatchPendingIdFileCount;
    }

    public void setPreAnalysisFilePatternMatchPendingIdFileCount(
	    int preAnalysisFilePatternMatchPendingIdFileCount) {
	this.preAnalysisFilePatternMatchPendingIdFileCount = preAnalysisFilePatternMatchPendingIdFileCount;
    }

    public int getPostAnalysisFileCount() {
	return postAnalysisFileCount;
    }

    public void setPostAnalysisFileCount(int postAnalysisFileCount) {
	this.postAnalysisFileCount = postAnalysisFileCount;
    }

    public int getPostAnalysisPendingFileCount() {
	return postAnalysisPendingFileCount;
    }

    public void setPostAnalysisPendingFileCount(int postAnalysisPendingFileCount) {
	this.postAnalysisPendingFileCount = postAnalysisPendingFileCount;
    }

    public int getPostAnalysisCodeMatchPendingIdFileCount() {
	return postAnalysisCodeMatchPendingIdFileCount;
    }

    public void setPostAnalysisCodeMatchPendingIdFileCount(
	    int postAnalysisCodeMatchPendingIdFileCount) {
	this.postAnalysisCodeMatchPendingIdFileCount = postAnalysisCodeMatchPendingIdFileCount;
    }

    public int getPostAnalysisStringSearchPendingIdFileCount() {
	return postAnalysisStringSearchPendingIdFileCount;
    }

    public void setPostAnalysisStringSearchPendingIdFileCount(
	    int postAnalysisStringSearchPendingIdFileCount) {
	this.postAnalysisStringSearchPendingIdFileCount = postAnalysisStringSearchPendingIdFileCount;
    }

    public int getPostAnalysisDependencyPendingIdFileCount() {
	return postAnalysisDependencyPendingIdFileCount;
    }

    public void setPostAnalysisDependencyPendingIdFileCount(
	    int postAnalysisDependencyPendingIdFileCount) {
	this.postAnalysisDependencyPendingIdFileCount = postAnalysisDependencyPendingIdFileCount;
    }

    public int getPostAnalysisFilePatternMatchPendingIdFileCount() {
	return postAnalysisFilePatternMatchPendingIdFileCount;
    }

    public void setPostAnalysisFilePatternMatchPendingIdFileCount(
	    int postAnalysisFilePatternMatchPendingIdFileCount) {
	this.postAnalysisFilePatternMatchPendingIdFileCount = postAnalysisFilePatternMatchPendingIdFileCount;
    }

    public int getPreAnalysisRapidIdCount() {
	return preAnalysisRapidIdCount;
    }

    public void setPreAnalysisRapidIdCount(int preRapidIdCount) {
	preAnalysisRapidIdCount = preRapidIdCount;
    }

    public int getPostAnalysisRapidIdCount() {
	return postAnalysisRapidIdCount;
    }

    public void setPostAnalysisRapidIdCount(int postRapidIdCount) {
	postAnalysisRapidIdCount = postRapidIdCount;
    }

    public String getScanType() {
	return scanType;
    }

    public void setScanType(String scanType) {
	this.scanType = scanType;
    }

    public Project getProject() {
	return project;
    }

    public void setProject(Project project) {
	this.project = project;
    }

    public int getPreAnalysisPendingReviewFileCount() {
	return preAnalysisPendingReviewFileCount;
    }

    public void setPreAnalysisPendingReviewFileCount(
	    int preAnalysisPendingReviewFileCount) {
	this.preAnalysisPendingReviewFileCount = preAnalysisPendingReviewFileCount;
    }

    public int getPostAnalysisPendingReviewFileCount() {
	return postAnalysisPendingReviewFileCount;
    }

    public void setPostAnalysisPendingReviewFileCount(
	    int postAnalysisPendingReviewFileCount) {
	this.postAnalysisPendingReviewFileCount = postAnalysisPendingReviewFileCount;
    }

}
