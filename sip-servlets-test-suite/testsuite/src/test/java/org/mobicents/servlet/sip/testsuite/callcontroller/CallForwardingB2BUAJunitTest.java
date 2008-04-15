package org.mobicents.servlet.sip.testsuite.callcontroller;

import javax.sip.SipProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;

public class CallForwardingB2BUAJunitTest extends SipServletTestCase {
	
	private static Log logger = LogFactory.getLog(CallForwardingB2BUAJunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
//	private static final int TIMEOUT = 5000;	
	private static final int TIMEOUT = 100000000;
	
	CallForwardingTestSipListener sender;
	CallForwardingTestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	@Override
	public void deployApplication() {
		tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test");
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-b2bua-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() {
		try {
			super.setUp();						
			
			senderProtocolObjects =new ProtocolObjects(
					"sender", "gov.nist", TRANSPORT, AUTODIALOG);
			receiverProtocolObjects = new ProtocolObjects(
					"receiver" , "gov.nist", TRANSPORT, AUTODIALOG);
						
			sender = new CallForwardingTestSipListener(5080, 5070, senderProtocolObjects);
			SipProvider senderProvider = sender.createProvider();			

			receiver = new CallForwardingTestSipListener(5090, -1, receiverProtocolObjects);
			SipProvider receiverProvider = receiver.createProvider();
			
			receiverProvider.addSipListener(receiver);
			senderProvider.addSipListener(sender);
			
			senderProtocolObjects.start();
			receiverProtocolObjects.start();
		} catch (Exception ex) {
			fail("unexpected exception ");
		}
	}
	
	public void testCallForwarding() throws InterruptedException {
		sender.sendInvite();
		Thread.sleep(TIMEOUT);
	}

	@Override
	protected void tearDown() {					
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");		
	}


}
