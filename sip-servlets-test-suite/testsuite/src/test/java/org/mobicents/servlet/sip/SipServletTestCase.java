package org.mobicents.servlet.sip;

import java.io.InputStream;
import java.util.Properties;

import org.cafesip.sipunit.SipTestCase;

/**
 * This class is responsible for reading up the properties configuration file
 * and starting/stopping tomcat. It delegates to the test case inheriting from it 
 * the deployment of the context and the location of the dar configuration file
 * since it should map to the test case.
 */
public abstract class SipServletTestCase extends SipTestCase {
	protected String tomcatBasePath;
	protected String projectHome;
	protected SipEmbedded tomcat;
	protected boolean autoDeployOnStartup = true;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		//Reading properties
		Properties properties = new Properties();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"org/mobicents/servlet/sip/testsuite/testsuite.properties");
		properties.load(inputStream);		
		tomcatBasePath = properties.getProperty("tomcat.home");		
		projectHome = properties.getProperty("project.home");
		//starting tomcat
		tomcat = new SipEmbedded();
		tomcat.setPath(tomcatBasePath);		
		tomcat.setLoggingFilePath("file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/");
		String darConfigurationFile = getDarConfigurationFile();
		tomcat.setDarConfigurationFilePath(darConfigurationFile);		
		tomcat.startTomcat();
		if(autoDeployOnStartup) {
			deployApplication();
		}
	}
	
	@Override
	public void tearDown() throws Exception {	
		tomcat.stopTomcat();
		super.tearDown();
	}

	/**
	 * Delegates the choice of the application to deploy to the test case 
	 */
	protected abstract void deployApplication();
	
	/**
	 * Delegates the choice of the default application router 
	 * configuration file to use to the test case
	 */
	protected abstract String getDarConfigurationFile();
}
