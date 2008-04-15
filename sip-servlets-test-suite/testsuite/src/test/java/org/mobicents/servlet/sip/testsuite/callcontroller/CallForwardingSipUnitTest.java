package org.mobicents.servlet.sip.testsuite.callcontroller;

import java.util.Properties;

import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;

public class CallForwardingSipUnitTest extends SipServletTestCase {

	private static Log logger = LogFactory.getLog(CallForwardingSipUnitTest.class);

	private SipStack sipStackSender;
	private SipPhone sipPhoneSender;	
	
	private SipStack sipStackReceiver;
	private SipPhone sipPhoneReceiver;

//	private static final int TIMEOUT = 5000;	
	private static final int TIMEOUT = 1000000;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		SipStack.setTraceEnabled(true);
	}

	@Override
	public void tearDown() throws Exception {		
		sipPhoneSender.dispose();		
		sipStackSender.dispose();		
		sipPhoneReceiver.dispose();		
		sipStackReceiver.dispose();
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test");
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-servlet-dar.properties";
	}

	public SipStack makeStack(String transport, int port) throws Exception {
		Properties properties = new Properties();
//		String peerHostPort1 = "127.0.0.1:5070";
//		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
//				+ "udp");
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + transport + "_"
				+ port);
		properties.setProperty("sipunit.BINDADDR", "127.0.0.1");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/simplesipservlettest_debug_port" + port + ".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/simplesipservlettest_log_port" + port + ".txt");
		
		return new SipStack(transport, port, properties);		
	}

	public void setupPhone() throws Exception {
			sipStackSender = makeStack(SipStack.PROTOCOL_UDP, 5080);					
			sipPhoneSender = sipStackSender.createSipPhone("localhost",
					SipStack.PROTOCOL_UDP, 5070, "sip:sender@nist.gov");		
			sipStackReceiver = makeStack(SipStack.PROTOCOL_UDP, 5090);					
			sipPhoneReceiver = sipStackReceiver.createSipPhone("localhost",
					SipStack.PROTOCOL_UDP, 5070, "sip:forward-receiver@nist.gov");
	}

	public void init() throws Exception {
		setupPhone();		
	}

	// Check if we receive a FORBIDDEN response for our invite
	public void testCallForwarding() throws Exception {
		init();
		
		SipCall receiver = sipPhoneReceiver.createSipCall();
		receiver.listenForIncomingCall();
		SipCall sender = sipPhoneSender.makeCall("sip:receiver@nist.gov", null);
		assertNotNull(sender);		
		assertTrue(receiver.waitForIncomingCall(TIMEOUT));			
		assertTrue(receiver.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
		assertTrue(sender.waitOutgoingCallResponse(TIMEOUT));
		assertTrue(receiver.sendIncomingCallResponse(Response.OK, "Answer", 0));
		assertTrue(sender.waitOutgoingCallResponse(TIMEOUT));
		assertTrue(sender.sendInviteOkAck());
		assertTrue(receiver.waitForAck(TIMEOUT));
		assertTrue(sender.disconnect());
		assertTrue(receiver.respondToDisconnect());
	}
}
