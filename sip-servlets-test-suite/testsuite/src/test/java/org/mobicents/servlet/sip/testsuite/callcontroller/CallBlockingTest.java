package org.mobicents.servlet.sip.testsuite.callcontroller;

import java.util.Properties;

import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipUnitServletTestCase;

public class CallBlockingTest extends SipUnitServletTestCase {

	private static Log logger = LogFactory.getLog(CallBlockingTest.class);

	private SipStack sipStackSender;
	private SipPhone sipPhoneSender;	

	private static final int timeout = 10000;	

	public CallBlockingTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {		
		sipPhoneSender.dispose();		
		sipStackSender.dispose();		
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-blocking-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-blocking-servlet-dar.properties";
	}

	public SipStack makeStack(String transport, int port) throws Exception {
		Properties properties = new Properties();
		String peerHostPort1 = "127.0.0.1:5070";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ "udp");
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
					SipStack.PROTOCOL_UDP, 5070, "sip:blocked-sender@sip-servlets.com");		
	}

	public void init() throws Exception {
		setupPhone();
	}

	// Check if we receive a FORBIDDEN response for our invite
	public void testCallBlockingInvite() throws Exception {
		init();
		SipCall sender = sipPhoneSender.createSipCall();
		assertTrue(sender.initiateOutgoingCall("sip:receiver@sip-servlets.com", null));
		assertTrue(sender.waitOutgoingCallResponse(timeout));	
		assertResponseReceived(Response.FORBIDDEN, sender);
	}
}
