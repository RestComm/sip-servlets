package org.mobicents.servlet.sip;
import junit.framework.TestCase;


public abstract class SipServletTestCase extends TestCase {
	private static final String TOMCAT_BASE_PATH = "E:\\servers\\apache-tomcat-5.5.20";
	SipEmbedded tomcat;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		tomcat = new SipEmbedded();
		tomcat.setPath(TOMCAT_BASE_PATH);		
		tomcat.setLoggingFilePath(TOMCAT_BASE_PATH + "\\common\\classes\\logging.properties");
		tomcat.setDarConfigurationFilePath("file:///E:/workspaces/sip-servlets/sip-servlets-impl/docs/dar.properties");
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
