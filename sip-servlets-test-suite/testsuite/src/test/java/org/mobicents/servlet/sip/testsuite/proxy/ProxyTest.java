package org.mobicents.servlet.sip.testsuite.proxy;


import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ListeningPoint;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.simple.SimpleSipServletTest;

public class ProxyTest extends SipServletTestCase{
	private static Log logger = LogFactory.getLog(SimpleSipServletTest.class);
	SipStack sipStackA;
	SipStack sipStackB;
	
	SipPhone sipPhoneA;
	SipPhone sipPhoneB;
	@Override
	public void deployApplication() {
		tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test");
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
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
			properties1.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"logs/simplesipservlettest_debug.txt");
			properties1.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"logs/simplesipservlettest_log.txt");

			Properties properties2 = new Properties();
			// properties2.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
			String peerHostPort2 = "127.0.0.1:5070";
			properties2.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort2 + "/"
					+ transport);
			properties2.setProperty("javax.sip.STACK_NAME", "receiver");
			properties2.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"logs/simplesipservlettest_debug.txt");
			properties2.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"logs/simplesipservlettest_log.txt");

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
			
			assertTrue(callB.waitForIncomingCall(10000));
			
			assertTrue(callB.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
			assertTrue(callA.waitOutgoingCallResponse(10000));
			
			assertTrue(callB.sendIncomingCallResponse(Response.OK, "Answer", 0));
			assertTrue(callA.waitOutgoingCallResponse(10000));
			
			assertTrue(callA.sendInviteOkAck());
			
			assertTrue(callB.waitForAck(10000));
			
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
