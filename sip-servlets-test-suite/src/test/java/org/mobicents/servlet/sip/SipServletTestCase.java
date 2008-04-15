package org.mobicents.servlet.sip;
import java.io.*;
import java.util.Properties;

import junit.framework.TestCase;


public abstract class SipServletTestCase extends TestCase {
	private static  String TOMCAT_BASE_PATH = "E:\\servers\\apache-tomcat-5.5.20";
	SipEmbedded tomcat;
	protected String projectHome;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
Properties properties = new Properties();
		
		properties.load(new FileInputStream("tomcat.properties"));
		
		TOMCAT_BASE_PATH = properties.getProperty("tomcat.home");
		
		projectHome = properties.getProperty("project.home");
		
		tomcat = new SipEmbedded();
		tomcat.setPath(TOMCAT_BASE_PATH);		
		tomcat.setLoggingFilePath(TOMCAT_BASE_PATH + "/common/classes/logging.properties");
		tomcat.setDarConfigurationFilePath("file:///" + projectHome + "/sip-servlets-impl/docs/dar.properties");
		tomcat.startTomcat();
		deployApplication();
	}
	
	@Override
	protected void tearDown() throws Exception {
		tomcat.stopTomcat();
		super.tearDown();
	}

	public abstract void deployApplication();
}
