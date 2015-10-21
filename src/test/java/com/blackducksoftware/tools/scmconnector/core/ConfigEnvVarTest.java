package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.assertEquals;

import java.io.File;

import net.jmatrix.eproperties.EProperties;

import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigEnvVarTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws Exception {

	String testEnvVar;
	String propsFilename;
	if (SystemUtils.IS_OS_WINDOWS) {
	    testEnvVar = "TEMP"; // on linux, make this PATH, and adjust the
				 // references below: envvar->PATH
	    propsFilename = "src/test/resources/env_windows.properties";
	} else {
	    testEnvVar = "USER";
	    propsFilename = "src/test/resources/env_nix.properties";
	}

	String expectedValue = System.getenv(testEnvVar);
	System.out.println("Value of env var: " + expectedValue);

	EProperties props = new EProperties();
	File propsFile = new File(propsFilename);
	props.load(propsFile);

	System.out.println("About to construct ConnnectorConfig");
	ConnectorConfig config = new ConnectorConfig(props);
	System.out.println("ConnnectorConfig constructed");

	assertEquals(expectedValue, config.getProperty("smtp.address"));
	assertEquals(expectedValue, config.getProperty("smtp.from"));
	assertEquals(expectedValue, config.getProperty("smtp.to"));
	assertEquals(expectedValue, config.getProperty("protex.server.name"));
	assertEquals(expectedValue, config.getProperty("protex.user.name"));
	assertEquals(expectedValue, config.getProperty("protex.password"));
	assertEquals(expectedValue, config.getProperty("protex.run_rapidid"));
	assertEquals(expectedValue, config.getProperty("protex.home_directory"));
	assertEquals(expectedValue, config.getProperty("connector.0.class"));
	assertEquals(expectedValue,
		config.getProperty("connector.0.repositoryURL"));
	assertEquals(expectedValue, config.getProperty("connector.0.user"));
	assertEquals(expectedValue, config.getProperty("connector.0.password"));
	assertEquals(expectedValue,
		config.getProperty("connector.0.new_project_name"));
	assertEquals(expectedValue, config.getProperty("connector.0.root"));

    }

}
