/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.sip.ListeningPoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * This class is responsible for reading up the properties configuration file
 * and starting/stopping tomcat. It delegates to the test case inheriting from it 
 * the deployment of the context and the location of the dar configuration file
 * since it should map to the test case.
 */
public abstract class SipServletTestCase extends TestCase {
	private static transient Logger logger = Logger.getLogger(SipServletTestCase.class);
	protected String tomcatBasePath;
	protected String projectHome;
	protected SipEmbedded tomcat;
	protected String sipIpAddress = "127.0.0.1";
	protected String serviceFullClassName = "org.mobicents.servlet.sip.startup.SipStandardService";
	protected String serverName = "SIP-Servlet-Tomcat-Server";
	protected String listeningPointTransport = ListeningPoint.UDP;
	protected boolean autoDeployOnStartup = true;
	protected boolean initTomcatOnStartup = true;
	protected boolean startTomcatOnStartup = true;
	protected boolean addSipConnectorOnStartup = true;
		
	public SipServletTestCase(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();		
		//Reading properties
		Properties properties = new Properties();
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(
				"org/mobicents/servlet/sip/testsuite/testsuite.properties");		
		try{
			properties.load(inputStream);
		} catch (NullPointerException e) {
			inputStream = getClass().getResourceAsStream(
				"org/mobicents/servlet/sip/testsuite/testsuite.properties");
			properties.load(inputStream);
		}
		
		// First try to use the env variables - useful for shell scripting
		tomcatBasePath = System.getenv("CATALINA_HOME");	
		projectHome = System.getenv("SIP_SERVLETS_HOME");
		
		// Otherwise use the properties
		if(this.tomcatBasePath == null || this.tomcatBasePath.length() <= 0) 
			this.tomcatBasePath = properties.getProperty("tomcat.home");
		if(this.projectHome == null || this.projectHome.length() <= 0)
			this.projectHome = properties.getProperty("project.home");
		logger.info("Tomcat base Path is : " + tomcatBasePath);
		logger.info("Project Home is : " + projectHome);
		//starting tomcat
		tomcat = new SipEmbedded(serverName, serviceFullClassName);
		tomcat.setLoggingFilePath(				
				projectHome + File.separatorChar + "sip-servlets-test-suite" + 
				File.separatorChar + "testsuite" + 
				File.separatorChar + "src" +
				File.separatorChar + "test" + 
				File.separatorChar + "resources" + File.separatorChar);
		logger.info("Log4j path is : " + tomcat.getLoggingFilePath());
		String darConfigurationFile = getDarConfigurationFile();
		tomcat.setDarConfigurationFilePath(darConfigurationFile);
		if(initTomcatOnStartup) {
			tomcat.initTomcat(tomcatBasePath);
			tomcat.addHttpConnector(8080);
			/*
			 * <Connector debugLog="../logs/debuglog.txt" ipAddress="0.0.0.0"
			 * logLevel="DEBUG" port="5070"
			 * protocol="org.mobicents.servlet.sip.startup.SipProtocolHandler"
			 * serverLog="../logs/serverlog.txt" signalingTransport="udp"
			 * sipPathName="gov.nist" sipStackName="SIP-Servlet-Tomcat-Server"/>
			 */
			if(addSipConnectorOnStartup) {
				tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
			}
		}		
		if(startTomcatOnStartup) {
			tomcat.startTomcat();
		}
		if(autoDeployOnStartup) {
			deployApplication();
		}
	}
	
	@Override
	protected void tearDown() throws Exception {	
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
