import junit.framework.TestCase;


public abstract class SipServletTestCase extends TestCase {	
	SipEmbedded tomcat;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		tomcat = new SipEmbedded();
		tomcat.setPath("E:\\servers\\apache-tomcat-5.5.20");
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
