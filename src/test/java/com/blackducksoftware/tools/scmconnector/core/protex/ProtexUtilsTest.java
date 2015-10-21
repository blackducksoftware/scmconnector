package com.blackducksoftware.tools.scmconnector.core.protex;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.bom.BomApi;
import com.blackducksoftware.sdk.protex.project.bom.FileCountType;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeApi;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeType;
import com.blackducksoftware.sdk.protex.project.codetree.CodeTreeNodeWithCount;
import com.blackducksoftware.sdk.protex.project.codetree.PartialCodeTree;
import com.blackducksoftware.sdk.protex.project.codetree.PartialCodeTreeWithCount;
import com.blackducksoftware.sdk.protex.project.codetree.discovery.DiscoveryApi;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexAPIWrapper;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;

public class ProtexUtilsTest {

    private static final String TEST_PROJECT_ID = "testProjectId";
    private static final int NUM_NODES = 123;

    private static final ProtexAPIWrapper mockProtexAPIWrapper = mock(ProtexAPIWrapper.class);
    private static final ProtexServerWrapper<ProtexProjectPojo> mockProtexServerWrapper = mock(ProtexServerWrapper.class);
    private static final BomApi mockBomApi = mock(BomApi.class);
    private static final CodeTreeApi mockCodeTreeApi = mock(CodeTreeApi.class);
    private static final DiscoveryApi mockDiscoveryApi = mock(DiscoveryApi.class);
    private static final PartialCodeTree mockPartialCodeTree = mock(PartialCodeTree.class);
    private static final PartialCodeTreeWithCount mockPartialCodeTreeWithCount = mock(PartialCodeTreeWithCount.class);
    private static final Project mockProject = mock(Project.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	when(mockProject.getProjectId()).thenReturn(TEST_PROJECT_ID);

	when(mockProtexServerWrapper.getInternalApiWrapper()).thenReturn(
		mockProtexAPIWrapper);
	when(mockProtexAPIWrapper.getBomApi()).thenReturn(mockBomApi);
	when(mockProtexAPIWrapper.getCodeTreeApi()).thenReturn(mockCodeTreeApi);
	when(mockProtexAPIWrapper.getDiscoveryApi()).thenReturn(
		mockDiscoveryApi);

	List<CodeTreeNodeWithCount> testNodes = new ArrayList<>();
	CodeTreeNodeWithCount codeTreeNodeWithCount = new CodeTreeNodeWithCount();
	codeTreeNodeWithCount.setCount(NUM_NODES);
	codeTreeNodeWithCount.setName("testcodeTreeNodeWithCountName");
	codeTreeNodeWithCount.setNodeType(CodeTreeNodeType.FILE);
	testNodes.add(codeTreeNodeWithCount);

	when(mockPartialCodeTreeWithCount.getNodes()).thenReturn(testNodes);

	List<CodeTreeNodeType> nodeTypesToInclude = new ArrayList<CodeTreeNodeType>();
	nodeTypesToInclude.add(CodeTreeNodeType.FOLDER);
	nodeTypesToInclude.add(CodeTreeNodeType.EXPANDED_ARCHIVE);
	nodeTypesToInclude.add(CodeTreeNodeType.FILE);

	when(
		mockCodeTreeApi.getCodeTreeByNodeTypes(TEST_PROJECT_ID, "/", 0,
			true, nodeTypesToInclude)).thenReturn(
		mockPartialCodeTree);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testgetFileCount() throws SdkFault {

	when(mockCodeTreeApi.getFileCount(TEST_PROJECT_ID, mockPartialCodeTree))
		.thenReturn(mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES, protexUtils.getFileCount(mockProject));
    }

    @Test
    public void testGetPendingIdFileCount() throws SdkFault {
	when(
		mockDiscoveryApi.getAllDiscoveriesPendingIdFileCount(
			TEST_PROJECT_ID, mockPartialCodeTree)).thenReturn(
		mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES, protexUtils.getPendingIdFileCount(mockProject));
    }

    @Test
    public void testGetPendingStringSearchFileCount() throws SdkFault {

	when(
		mockDiscoveryApi.getStringSearchPendingIdFileCount(
			TEST_PROJECT_ID, mockPartialCodeTree)).thenReturn(
		mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES,
		protexUtils.getPendingStringSearchFileCount(mockProject));
    }

    @Test
    public void testGetPendingDependenciesIdFileCount() throws SdkFault {

	when(
		mockDiscoveryApi.getDependenciesPendingIdFileCount(
			TEST_PROJECT_ID, mockPartialCodeTree)).thenReturn(
		mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES,
		protexUtils.getPendingDependenciesIdFileCount(mockProject));
    }

    @Test
    public void testGetPendingReviewCount() throws SdkFault {

	when(
		mockBomApi.getFileCount(TEST_PROJECT_ID, mockPartialCodeTree,
			FileCountType.PENDING_REVIEW)).thenReturn(
		mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES, protexUtils.getPendingReviewCount(mockProject));
    }

    @Test
    public void testGetPendingFileDiscoveryPatternIdFileCount() throws SdkFault {

	when(
		mockDiscoveryApi.getFileDiscoveryPatternPendingIdFileCount(
			TEST_PROJECT_ID, mockPartialCodeTree)).thenReturn(
		mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES,
		protexUtils
			.getPendingFileDiscoveryPatternIdFileCount(mockProject));
    }

    @Test
    public void testGetPendingCodeMatchIdFileCount() throws SdkFault {

	when(
		mockDiscoveryApi.getCodeMatchPendingIdFileCount(
			TEST_PROJECT_ID, mockPartialCodeTree)).thenReturn(
		mockPartialCodeTreeWithCount);

	ProtexUtils protexUtils = new ProtexUtils(mockProtexServerWrapper);
	assertEquals(NUM_NODES,
		protexUtils.getPendingCodeMatchIdFileCount(mockProject));
    }
}
