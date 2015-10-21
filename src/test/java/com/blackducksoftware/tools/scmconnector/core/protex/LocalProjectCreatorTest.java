package com.blackducksoftware.tools.scmconnector.core.protex;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.sdk.fault.SdkFault;
import com.blackducksoftware.sdk.protex.license.LicenseCategory;
import com.blackducksoftware.sdk.protex.project.AnalysisSourceLocation;
import com.blackducksoftware.sdk.protex.project.Project;
import com.blackducksoftware.sdk.protex.project.ProjectApi;
import com.blackducksoftware.sdk.protex.project.ProjectRequest;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexAPIWrapper;
import com.blackducksoftware.tools.commonframework.connector.protex.ProtexServerWrapper;
import com.blackducksoftware.tools.commonframework.standard.protex.ProtexProjectPojo;
import com.blackducksoftware.tools.scmconnector.core.Connector;
import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;
import com.blackducksoftware.tools.scmconnector.integrations.nop.NOPConnector;

public class LocalProjectCreatorTest {
    private static final String PROTEX_URL = "https://protex.testdomain.com";
    private static final String SOURCE_ROOT_DIR = "src/test/resources/testRoot";
    private static final String NOP_CONNECTOR = "com.blackducksoftware.tools.scmconnector.integrations.nop.NOPConnector";

    private static final String EXISTING_PROJECT_ID = "existingProjectId";
    private static final String EXISTING_PROJECT_NAME = "existingProjectName";

    private static final String NEW_PROJECT_ID = "newProjectId";
    private static final String NEW_PROJECT_NAME = "newProjectName";

    private static final ProtexAPIWrapper mockProtexAPIWrapper = mock(ProtexAPIWrapper.class);
    private static final ProtexServerWrapper<ProtexProjectPojo> mockProtexServerWrapper = mock(ProtexServerWrapper.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	// Mock the behavior of the protexServerWrapper and ProtexAPIWrapper
	when(mockProtexServerWrapper.getInternalApiWrapper()).thenReturn(
		mockProtexAPIWrapper);

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testClone() throws Exception {

	// Create a test-specific mock Project API
	// so we can manipulate it's behavior per test
	ProjectApi mockProjectApi = mock(ProjectApi.class);
	when(mockProtexAPIWrapper.getProjectApi()).thenReturn(mockProjectApi);

	// Mock the behavior of an existing project
	Project mockExistingProject = mock(Project.class);
	when(mockExistingProject.getProjectId())
		.thenReturn(EXISTING_PROJECT_ID);
	when(mockExistingProject.getName()).thenReturn(EXISTING_PROJECT_NAME);
	when(mockProjectApi.getProjectByName(EXISTING_PROJECT_NAME))
		.thenReturn(mockExistingProject);

	// Mock the NONexistence of the cloned to project before cloning
	when(mockProjectApi.getProjectByName(NEW_PROJECT_NAME)).thenThrow(
		new SdkFault("template does not exist"));

	// Mock the behavior of the clone SDK operation
	when(
		mockProjectApi.cloneProject(eq(EXISTING_PROJECT_ID),
			eq(NEW_PROJECT_NAME), any(ArrayList.class), eq(false),
			any(ArrayList.class))).thenReturn(NEW_PROJECT_ID);

	// Mock the existence of the "cloned to" project after cloning
	Project mockNewProject = mock(Project.class);
	when(mockProjectApi.getProjectById(NEW_PROJECT_ID)).thenReturn(
		mockNewProject);
	when(mockNewProject.getProjectId()).thenReturn(NEW_PROJECT_ID);

	// Set up the object under test
	ConnectorConfig config = initConfig();
	Connector connector = new NOPConnector();
	Properties connectorProps = getConnectorProperties();
	connector.init(connectorProps);
	LocalProjectCreator localProjectCreator = new LocalProjectCreator(
		config, mockProtexServerWrapper, connector);

	// Exercise the code under test
	Project newProject = localProjectCreator.cloneProject(NEW_PROJECT_NAME,
		EXISTING_PROJECT_NAME);

	// Verify the expected SDK calls were made
	assertEquals(NEW_PROJECT_ID, newProject.getProjectId());
	verify(mockNewProject).setAnalysisSourceLocation(
		any(AnalysisSourceLocation.class));
	verify(mockProjectApi).updateProject(mockNewProject);
    }

    @Test
    public void testCreateNewProjectProjectDoesNotExist() throws Exception {

	// Create a test-specific mock Project API
	// so we can manipulate it's behavior per test
	ProjectApi mockProjectApi = mock(ProjectApi.class);
	when(mockProtexAPIWrapper.getProjectApi()).thenReturn(mockProjectApi);

	// Mock the behavior of an existing project
	Project mockExistingProject = mock(Project.class);
	when(mockExistingProject.getProjectId())
		.thenReturn(EXISTING_PROJECT_ID);
	when(mockExistingProject.getName()).thenReturn(EXISTING_PROJECT_NAME);
	when(mockProjectApi.getProjectByName(EXISTING_PROJECT_NAME))
		.thenReturn(mockExistingProject);

	// Mock the NONexistence of the new project before we create it
	when(mockProjectApi.getProjectByName(NEW_PROJECT_NAME)).thenThrow(
		new SdkFault("project does not exist"));

	// Mock the behavior of the TBD SDK operation
	when(
		mockProjectApi.createProject(any(ProjectRequest.class),
			eq(LicenseCategory.PROPRIETARY))).thenReturn(
		NEW_PROJECT_ID);

	// Mock the existence of the new project after creation
	Project mockNewProject = mock(Project.class);
	when(mockProjectApi.getProjectById(NEW_PROJECT_ID)).thenReturn(
		mockNewProject);
	when(mockNewProject.getProjectId()).thenReturn(NEW_PROJECT_ID);

	// Set up the object under test
	ConnectorConfig config = initConfig();
	Connector connector = new NOPConnector();
	Properties connectorProps = getConnectorProperties();
	connector.init(connectorProps);
	LocalProjectCreator localProjectCreator = new LocalProjectCreator(
		config, mockProtexServerWrapper, connector);

	// Exercise the code under test
	Project newProject = localProjectCreator
		.createNewProject(NEW_PROJECT_NAME);

	assertEquals(NEW_PROJECT_ID, newProject.getProjectId());
    }

    private static ConnectorConfig initConfig() throws Exception {
	Properties props = new Properties();
	props.setProperty("protex.server.name", PROTEX_URL);
	props.setProperty("protex.user.name", "testProtexUser");
	props.setProperty("protex.password", "testProtexPassword");
	props.setProperty("protex.password.isencrypted", "false");

	props.setProperty("protex.run_rapidid", "true");
	props.setProperty("protex.home_directory", "/home/blackduck");
	props.setProperty("total_connectors", "1");
	props.setProperty("connector.0.class", NOP_CONNECTOR);
	props.setProperty("connector.0.new_project_name", EXISTING_PROJECT_NAME);
	props.setProperty("connector.0.root", SOURCE_ROOT_DIR);

	ConnectorConfig config = new ConnectorConfig(props);
	return config;
    }

    private static Properties getConnectorProperties() {
	Properties props = new Properties();

	props.setProperty("class", NOP_CONNECTOR);
	props.setProperty("new_project_name", EXISTING_PROJECT_NAME);
	props.setProperty("root", SOURCE_ROOT_DIR);

	return props;
    }

}
