package org.mobicents.servlet.sip.testsuite.b2bua;

import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.simple.SimpleSipServletTest;

public class B2BUASipUnitTest extends SipServletTestCase {
	private static Log logger = LogFactory.getLog(SimpleSipServletTest.class);
//	private static final int TIMEOUT = 10000;
	private static final int TIMEOUT = 100000000; 
	SipStack sipStackA;
	SipStack sipStackB;
	
	SipPhone sipPhoneA;
	SipPhone sipPhoneB;
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
	
	public void setupPhones() {
		try {
			Properties properties1 = new Properties();
			//properties1.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
			String transport = "udp";
			String peerHostPort1 = "127.0.0.1:5070";
			properties1.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
					+ transport);
			properties1.setProperty("javax.sip.STACK_NAME", "sender");
			properties1.setProperty("sipunit.BINDADDR", "127.0.0.1");
			properties1.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"logs/b2buadebug1.txt");
			properties1.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"logs/b2bualog1.txt");

			Properties properties2 = new Properties();
			// properties2.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
			String peerHostPort2 = "127.0.0.1:5070";
			properties2.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort2 + "/"
					+ transport);
			properties2.setProperty("javax.sip.STACK_NAME", "receiver");
			properties2.setProperty("sipunit.BINDADDR", "127.0.0.1");
			properties2.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"logs/b2buadebug2.txt");
			properties2.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"logs/b2bualog2.txt");

			sipStackA = new SipStack(SipStack.PROTOCOL_UDP , 5058, properties1);
			ListeningPoint lp = sipStackA.getSipProvider().getListeningPoint("udp");
			String stackIPAddress = lp.getIPAddress();
			sipPhoneA = sipStackA.createSipPhone("localhost", SipStack.PROTOCOL_UDP, 5070, "sip:sender@nist.gov");

			sipStackB = new SipStack(SipStack.PROTOCOL_UDP , 5059, properties2);
			sipPhoneB = sipStackB.createSipPhone("localhost", SipStack.PROTOCOL_UDP, 5070, "sip:receiver@nist.gov");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	public void init()
	{
		setupPhones();
		try{
			SipCall callA = sipPhoneA.createSipCall();
			SipCall callB = sipPhoneB.createSipCall();
			
			callB.listenForIncomingCall();Thread.sleep(300);
			callA.initiateOutgoingCall("sip:receiver@nist.gov", null);
			
			assertTrue(callB.waitForIncomingCall(TIMEOUT));
			
			assertTrue(callB.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
			assertTrue(callA.waitOutgoingCallResponse(TIMEOUT));
			
			assertTrue(callB.sendIncomingCallResponse(Response.OK, "OK", 0));
			assertTrue(callA.waitOutgoingCallResponse(TIMEOUT));
			
			assertTrue(callA.sendInviteOkAck());
			
			assertTrue(callB.waitForAck(TIMEOUT));
			
			assertTrue(callA.disconnect());
			assertTrue(callB.respondToDisconnect());
			
			sipPhoneA.dispose();
			sipPhoneB.dispose();
			sipStackA.dispose();
			sipStackB.dispose();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testSimpleSipServlet() throws InterruptedException {
		init();
		Thread.sleep(5000);
	}
}
