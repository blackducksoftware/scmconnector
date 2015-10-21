package com.blackducksoftware.tools.scmconnector.core;

import java.io.InputStream;
import java.util.Properties;

import com.blackducksoftware.tools.commonframework.core.config.ConfigurationManager;
import com.blackducksoftware.tools.commonframework.core.config.user.CommonUser;

/**
 * Extended test config class for testing purposes only. Since we cannot test an
 * abstract class, we will test its inherited class.
 *
 * This tests the Protex Config Manager only
 *
 * @author akamen
 *
 */
public class TestProtexConfigurationManager extends ConfigurationManager {

    /**
     * Instantiates a new test protex configuration manager.
     *
     * @param configFileLocation
     *            the config file location
     */
    public TestProtexConfigurationManager(String configFileLocation) {
	super(configFileLocation, APPLICATION.PROTEX);
    }

    /**
     * Instantiates a new test protex configuration manager.
     *
     * @param user
     *            the user
     */
    public TestProtexConfigurationManager(CommonUser user) {
	super(user, APPLICATION.PROTEX);
    }

    /**
     * Instantiates a new test protex configuration manager.
     *
     * @param is
     *            the is
     */
    public TestProtexConfigurationManager(InputStream is) {
	super(is, APPLICATION.PROTEX);
    }

    /**
     * Instantiates a new test protex configuration manager.
     *
     * @param props
     *            the props
     */
    public TestProtexConfigurationManager(Properties props) {
	super(props, APPLICATION.PROTEX);
    }
}
