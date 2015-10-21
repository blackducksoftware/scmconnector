package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import net.jmatrix.eproperties.EProperties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConnectorConfigTest {
    private static final String CONFIG_FILE_PATH = "src/test/resources/config_framework.properties";
    private static final String SERVER_URL = "https://se-px01.dc1.lan/";
    private static final String USERNAME = "unitTester@blackducksoftware.com";

    private static final String PASSWORD1_PLAINTEXT = "blackduck";

    private static final String PASSWORD2_ENCRYPTED = "=,9%1@,=Z<lI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_P";
    private static final String PASSWORD2_PLAINTEXT = "lameduck";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	org.apache.xml.security.Init.init();
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
    public void testServerConnectionDetails() throws Exception {
	ConnectorConfig config = new ConnectorConfig(CONFIG_FILE_PATH);

	assertEquals("Server name/URI test", SERVER_URL, config.getServerBean()
		.getServerName());
	assertEquals("Username test", USERNAME, config.getServerBean()
		.getUserName());
	assertEquals("Password test", PASSWORD1_PLAINTEXT, config
		.getServerBean().getPassword());
    }

    /**
     * If the user sets a value like connector.0.new_project_name to a variable
     * reference, and forgets to set that env variable, the value of the
     * property is null. Make sure we don't get a null pointer exception in that
     * scenario (which allows the utility to survive long enough to produce a
     * helpful error message).
     *
     * @throws Exception
     */
    @Test
    public void testNullProperty() throws Exception {
	EProperties props = new EProperties();
	props.setProperty("envvars", "[method://java.lang.System.getenv()]");
	props.setProperty("protex.server.name", "testserver");
	props.setProperty("protex.user.name", "testuser");
	props.setProperty("protex.password", "testpassword");
	props.setProperty("protex.password.isencrypted", "false");
	props.setProperty("connector.0.user", "testuser2");
	props.setProperty("connector.0.new_project_name",
		"${envvars->SomeNotSetVariable}");
	ConnectorConfig config = new ConnectorConfig(props);
	Properties destinationProperties = new Properties();
	config.copyPropertiesWithPrefix(destinationProperties, "connector.0.");
	assertEquals("testuser2", destinationProperties.getProperty("user"));
    }

    @Test
    public void testPropertiesConstructor() throws Exception {
	Properties props = new Properties();
	props.setProperty("protex.server.name", "differentserver");
	props.setProperty("protex.user.name", "differentuser");
	props.setProperty("protex.password", PASSWORD2_ENCRYPTED);
	props.setProperty("protex.password.isencrypted", "true");
	ConnectorConfig config = new ConnectorConfig(props);

	assertEquals("Server name/URI test", "differentserver", config
		.getServerBean().getServerName());
	assertEquals("Username test", "differentuser", config.getServerBean()
		.getUserName());
	assertEquals("Password test", PASSWORD2_PLAINTEXT, config
		.getServerBean().getPassword());
    }

    @Test
    public void testCustomProperties() throws Exception {
	ConnectorConfig config = new ConnectorConfig(CONFIG_FILE_PATH);

	assertEquals("/home/blackduck", config.getProtexHomeDirectory());

	assertFalse(config.isProxyLocal());
	config.setProxyLocal(true);
	;
	assertTrue(config.isProxyLocal());

	config.setProxyLocal(false);
	assertFalse(config.isProxyLocal());
	assertEquals("", config.getProtexHomeDirectory());

	assertTrue(config.isRunRapidId());

	String expectedToString = "Server: " + ConnectorConfigTest.SERVER_URL
		+ "\nUser: " + ConnectorConfigTest.USERNAME
		+ "\nProtex directory: " + "" + "\nRapid ID: true"
		+ "\nLocal Proxy: false\n";
	assertEquals(expectedToString, config.toString());
    }

    @Test
    public void testMaxConnectorIndex() throws Exception {
	Properties props = new Properties();
	props.setProperty("protex.server.name", "differentserver");
	props.setProperty("protex.user.name", "differentuser");
	props.setProperty("protex.password", PASSWORD2_ENCRYPTED);
	props.setProperty("protex.password.isencrypted", "true");
	ConnectorConfig config = new ConnectorConfig(props);
	assertEquals(1000, config.getMaxConnectorIndex());

	props.setProperty("max.connector.index", "123");
	config = new ConnectorConfig(props);
	assertEquals(123, config.getMaxConnectorIndex());
    }

    @Test
    public void testOptional() throws Exception {
	ConnectorConfig config = new ConnectorConfig(CONFIG_FILE_PATH);

	assertEquals(null, config.getOptionalProperty("nonexistent.property"));
    }

    @Test
    public void testGetPropertyWithDefault() throws Exception {
	ConnectorConfig config = new ConnectorConfig(CONFIG_FILE_PATH);

	assertEquals("my default value",
		config.getProperty("nonexistent.property", "my default value"));
    }

    @Test
    public void testGetPropertiesWithPrefix() throws Exception {
	ConnectorConfig config = new ConnectorConfig(CONFIG_FILE_PATH);

	Properties clientProperties = new Properties();
	config.copyPropertiesWithPrefix(clientProperties, "client.prefix.");
	assertEquals("client property 1", clientProperties.getProperty("prop1"));
	assertEquals("client property 2", clientProperties.getProperty("prop2"));
	assertEquals("client property 3", clientProperties.getProperty("prop3"));
    }

}
