package com.blackducksoftware.tools.scmconnector.core;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.junit.Test;

import com.blackducksoftware.tools.scmconnector.core.ConnectorConfig;

/**
 * Password encryption sanity check. Password encryption is thoroughly tested by
 * CF ConfigurationFileTest. Also, the SCM integration tests use a mixture of
 * password scenarios to provide further test coverage. This does does simple
 * test to make sure SCM Connector is taking advantage of the Common Framework
 * ConfigurationFile / ConfigurationPassword capabilities related to updating
 * config files with encrypted passwords.
 * 
 * @author sbillings
 *
 */
public class ConnectorConfigEncryptionTest {

    @Test
    public void testLegacyProtexPasswordPlainTextIsplaintextNotSet()
	    throws Exception {

	// Setup
	Properties props = new Properties();
	props.setProperty("protex.server.name", "https://se-px01.dc1.lan/");
	props.setProperty("protex.user.name",
		"unitTester@blackducksoftware.com");
	props.setProperty("protex.password", "blackduck");
	File propFile = File.createTempFile(this.getClass().getName(),
		"properties1");
	OutputStream origPropsOutputStream = new FileOutputStream(propFile);
	props.store(origPropsOutputStream, "test config file");

	// Construct config option: should update config file with encrypted
	// password
	new ConnectorConfig(propFile.getAbsolutePath());

	// Verify
	Properties updatedProps = new Properties();
	InputStream updatedPropsInputStream = new FileInputStream(propFile);
	updatedProps.load(updatedPropsInputStream);
	assertEquals("https://se-px01.dc1.lan/",
		updatedProps.getProperty("protex.server.name"));
	assertEquals(
		"_=ZTu,6$3,7u>Ji3SHP(lI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_P",
		updatedProps.getProperty("protex.password"));
	assertEquals("true",
		updatedProps.getProperty("protex.password.isencrypted"));
    }

    @Test
    public void testScmPassword() throws Exception {

	// Setup
	Properties props = new Properties();
	props.setProperty("protex.server.name", "https://se-px01.dc1.lan/");
	props.setProperty("protex.user.name",
		"unitTester@blackducksoftware.com");
	props.setProperty("protex.password", "blackduck");

	props.setProperty("connector.0.password", "blackduck");
	props.setProperty("connector.0.password.isencrypted", "false");

	props.setProperty("connector.1.password", "blackduck");

	File propFile = File.createTempFile(this.getClass().getName(),
		"properties2");
	OutputStream origPropsOutputStream = new FileOutputStream(propFile);
	props.store(origPropsOutputStream, "test config file");

	// Construct config option: should update config file with encrypted
	// password
	new ConnectorConfig(propFile.getAbsolutePath());

	// Verify
	Properties updatedProps = new Properties();
	InputStream updatedPropsInputStream = new FileInputStream(propFile);
	updatedProps.load(updatedPropsInputStream);

	assertEquals("https://se-px01.dc1.lan/",
		updatedProps.getProperty("protex.server.name"));
	assertEquals(
		"_=ZTu,6$3,7u>Ji3SHP(lI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_P",
		updatedProps.getProperty("protex.password"));
	assertEquals("true",
		updatedProps.getProperty("protex.password.isencrypted"));

	assertEquals("blackduck",
		updatedProps.getProperty("connector.0.password"));
	assertEquals("false",
		updatedProps.getProperty("connector.0.password.isencrypted"));

	assertEquals(
		"_=ZTu,6$3,7u>Ji3SHP(lI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_PlI'nKT:u_P",
		updatedProps.getProperty("connector.1.password"));
	assertEquals("true",
		updatedProps.getProperty("connector.1.password.isencrypted"));
    }
}
