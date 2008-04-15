package org.mobicents.servlet.sip.testsuite.simple;

import javax.sip.SipProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootmeSipServletTest extends SipServletTestCase {
	
	private static Log logger = LogFactory.getLog(ShootmeSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() {
		try {
			super.setUp();						
			
			senderProtocolObjects =new ProtocolObjects(
					"sender", "gov.nist", TRANSPORT, AUTODIALOG);
						
			sender = new TestSipListener(5080, 5070, senderProtocolObjects);
			SipProvider senderProvider = sender.createProvider();			
			
			senderProvider.addSipListener(sender);
			
			senderProtocolObjects.start();			
		} catch (Exception ex) {
			fail("unexpected exception ");
		}
	}
	
	public void testCallForwarding() throws InterruptedException {
		sender.sendInvite();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());		
	}

	@Override
	protected void tearDown() {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");		
	}


}
