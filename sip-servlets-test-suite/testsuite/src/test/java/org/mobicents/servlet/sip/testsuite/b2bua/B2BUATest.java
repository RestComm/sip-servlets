package org.mobicents.servlet.sip.testsuite.b2bua;

import org.mobicents.servlet.sip.SipServletTestCase;

public class B2BUATest extends SipServletTestCase {
	
	private B2BUATestCase testCase;
	
	@Override
	public void deployApplication() {
		tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/b2bua-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test");
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/b2bua/b2bua-sip-servlet-dar.properties";
	}
	
	public void init() throws Exception {
	
		this.testCase = new B2BUATestCase();
		this.testCase.setUp();
		
	}
	
	public void testSendInvite()throws Exception {
		init();
		testCase.doTest();
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.testCase.tearDown();
		
	}
	
	

}
