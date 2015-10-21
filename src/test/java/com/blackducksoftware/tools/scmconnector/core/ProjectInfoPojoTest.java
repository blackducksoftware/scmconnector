package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.AnalysisInfo;
import com.blackducksoftware.tools.scmconnector.core.ProjectInfoPOJO;

public class ProjectInfoPojoTest {
    private static ProjectInfoPOJO pojo;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	pojo = new ProjectInfoPOJO();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetBomUrl() {
	pojo.setBomUrl("test url");
	assertEquals("test url", pojo.getBomUrl());
    }

    @Test
    public void testGetAnalysisInfo() {
	AnalysisInfo analysisInfo = new AnalysisInfo();
	analysisInfo.setAnalyzedBy("test analyzed by value");
	pojo.setAnalysisInfo(analysisInfo);
	assertEquals("test analyzed by value", pojo.getAnalysisInfo()
		.getAnalyzedBy());
    }

    @Test
    public void testGetPreAnalysisFileCount() {
	pojo.setPreAnalysisFileCount(1);
	assertEquals(1, pojo.getPreAnalysisFileCount());
    }

    @Test
    public void testGetPreAnalysisPendingFileCount() {
	pojo.setPreAnalysisPendingFileCount(2);
	assertEquals(2, pojo.getPreAnalysisPendingFileCount());
    }

    @Test
    public void testGetPreAnalysisCodeMatchPendingIdFileCount() {
	pojo.setPreAnalysisCodeMatchPendingIdFileCount(3);
	assertEquals(3, pojo.getPreAnalysisCodeMatchPendingIdFileCount());
    }

    @Test
    public void testGetPreAnalysisStringSearchPendingIdFileCount() {
	pojo.setPreAnalysisStringSearchPendingIdFileCount(4);
	assertEquals(4, pojo.getPreAnalysisStringSearchPendingIdFileCount());
    }

    @Test
    public void testGetPreAnalysisDependencyPendingIdFileCount() {
	pojo.setPreAnalysisDependencyPendingIdFileCount(5);
	assertEquals(5, pojo.getPreAnalysisDependencyPendingIdFileCount());
    }

    @Test
    public void testGetPreAnalysisFilePatternMatchPendingIdFileCount() {
	pojo.setPreAnalysisFilePatternMatchPendingIdFileCount(6);
	assertEquals(6, pojo.getPreAnalysisFilePatternMatchPendingIdFileCount());
    }

    @Test
    public void testGetPostAnalysisFileCount() {
	pojo.setPostAnalysisFileCount(7);
	assertEquals(7, pojo.getPostAnalysisFileCount());
    }

    @Test
    public void testGetPostAnalysisPendingFileCount() {
	pojo.setPostAnalysisPendingFileCount(8);
	assertEquals(8, pojo.getPostAnalysisPendingFileCount());
    }

    @Test
    public void testGetPostAnalysisCodeMatchPendingIdFileCount() {
	pojo.setPostAnalysisCodeMatchPendingIdFileCount(9);
	assertEquals(9, pojo.getPostAnalysisCodeMatchPendingIdFileCount());
    }

    @Test
    public void testGetPostAnalysisStringSearchPendingIdFileCount() {
	pojo.setPostAnalysisStringSearchPendingIdFileCount(10);
	assertEquals(10, pojo.getPostAnalysisStringSearchPendingIdFileCount());
    }

    @Test
    public void testGetPostAnalysisDependencyPendingIdFileCount() {
	pojo.setPostAnalysisDependencyPendingIdFileCount(11);
	assertEquals(11, pojo.getPostAnalysisDependencyPendingIdFileCount());
    }

    @Test
    public void testGetPostAnalysisFilePatternMatchPendingIdFileCount() {
	pojo.setPostAnalysisFilePatternMatchPendingIdFileCount(12);
	assertEquals(12,
		pojo.getPostAnalysisFilePatternMatchPendingIdFileCount());
    }

    @Test
    public void testGetScanType() {
	pojo.setScanType("test scan type");
	assertEquals("test scan type", pojo.getScanType());
    }

    @Test
    public void testGetProject() {
	Project testProject = new Project();
	pojo.setProject(testProject);
	assertEquals(testProject, pojo.getProject());
    }

}
