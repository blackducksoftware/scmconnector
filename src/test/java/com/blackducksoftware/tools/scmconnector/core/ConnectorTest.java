package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.Connector;

public class ConnectorTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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
    public void testGetModuleNameFromURL() {
	String moduleName = Connector
		.getModuleNameFromURL("http://repository/web");
	assertEquals("Module name check; No trailing slash", "web", moduleName);

	moduleName = Connector.getModuleNameFromURL("http://repository/web/");
	assertEquals("Module name check; With trailing slash", "web",
		moduleName);
    }

    @Test
    public void testAddPathComponentToPath() {
	String expectedDir = File.separator + "Temp" + File.separator + "base"
		+ File.separator + "web";

	String dir = Connector.addPathComponentToPath(File.separator + "Temp"
		+ File.separator + "base", "web");
	assertEquals("Dir check; No trailing slash", expectedDir, dir);

	dir = Connector.addPathComponentToPath(File.separator + "Temp"
		+ File.separator + "base" + File.separator, "web");
	assertEquals("Dir check; With trailing slash", expectedDir, dir);
    }

}
