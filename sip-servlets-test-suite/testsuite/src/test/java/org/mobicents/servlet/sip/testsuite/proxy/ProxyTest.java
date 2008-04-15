package org.mobicents.servlet.sip.testsuite.proxy;


import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;

public class ProxyTest extends SipServletTestCase{

	private static Log logger = LogFactory.getLog(ProxyTest.class);
	
	private SipStack sipStackSender;
	private SipStack[] sipStackReceivers;
	
	private SipPhone sipPhoneSender;
	private SipPhone[] sipPhoneReceivers;
	
	private static final int timeout = 5000;
	
	private static final int receiversCount = 1;
	
	// Don't restart the server for this set of tests.
	private static boolean firstTime = true;

	public ProxyTest(String name) {
		super(name);
	}
	
	@Override
	public void setUp()
	{
		if(firstTime)
		{
			try {
				super.setUp();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		firstTime = false;
	}
	
	@Override
	public void tearDown()
	{
		try {
			sipPhoneSender.dispose();
			for(SipPhone sp : sipPhoneReceivers) sp.dispose();
			sipStackSender.dispose();
			for(SipStack ss : sipStackReceivers) ss.dispose();
			super.tearDown();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}
	
	public SipStack makeStack(String transport, int port)
	{
		Properties properties = new Properties();
		String peerHostPort1 = "127.0.0.1:5070";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ "udp");
		properties.setProperty("javax.sip.STACK_NAME", "UAC_"+transport+"_"+port);
		properties.setProperty("sipunit.BINDADDR", "127.0.0.1");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
		"logs/simplesipservlettest_debug_port"+port+".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
		"logs/simplesipservlettest_log_port"+port+".txt");
		try {
			return new SipStack(transport, port, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void setupPhones() {
		sipStackReceivers = new SipStack[receiversCount];
		sipPhoneReceivers = new SipPhone[receiversCount];
		try {
			sipStackSender = makeStack(SipStack.PROTOCOL_UDP , 5058);
			ListeningPoint lp = sipStackSender.getSipProvider().getListeningPoint("udp");
			String stackIPAddress = lp.getIPAddress();
			sipPhoneSender = sipStackSender.createSipPhone("localhost",
					SipStack.PROTOCOL_UDP, 5070, "sip:sender@nist.gov");

			for(int q=0; q<receiversCount; q++)
			{
				int port = 5058 - 1 - q;
				sipStackReceivers[q] = makeStack(SipStack.PROTOCOL_UDP, port);
				sipPhoneReceivers[q] = sipStackReceivers[q].createSipPhone(
						"localhost", SipStack.PROTOCOL_UDP, 5070, "sip:receiver@nist.gov");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	public void init()
	{
		setupPhones();
	}

	// Tests a complete call.
	public void testCallProxying() throws InterruptedException {
		init();
		try{
			SipCall sender = sipPhoneSender.createSipCall();
			SipCall[] receiverCalls = new SipCall[receiversCount];
			for(int q=0; q<receiversCount; q++)
				receiverCalls[q] = sipPhoneReceivers[q].createSipCall();
			
			for(SipCall call:receiverCalls)
				call.listenForIncomingCall();
			
			sender.initiateOutgoingCall("sip:receiver@nist.gov", null);
			
			for(SipCall call:receiverCalls)
				assertTrue(call.waitForIncomingCall(timeout));
			
			for(SipCall call:receiverCalls)
				assertTrue(call.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
			assertTrue(sender.waitOutgoingCallResponse(timeout));
			
			for(SipCall call:receiverCalls)
				assertTrue(call.sendIncomingCallResponse(Response.OK, "Answer", 0));
			assertTrue(sender.waitOutgoingCallResponse(timeout));
			
			assertTrue(sender.sendInviteOkAck());
			
			for(SipCall call:receiverCalls)
				assertTrue(call.waitForAck(timeout));
			
			assertTrue(sender.disconnect());
			
			for(SipCall call:receiverCalls)
				assertTrue(call.respondToDisconnect());
			
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}
	
	// This just checks if the INVITEs are getting through
	public void testInviteProxying() throws InterruptedException {
		init();
		try{
			SipCall sender = sipPhoneSender.createSipCall();
			SipCall[] receiverCalls = new SipCall[receiversCount];
			for(int q=0; q<receiversCount; q++)
				receiverCalls[q] = sipPhoneReceivers[q].createSipCall();
			
			for(SipCall call:receiverCalls)
				call.listenForIncomingCall();
			
			sender.initiateOutgoingCall("sip:receiver@nist.gov", null);
			
			for(SipCall call:receiverCalls)
				assertTrue(call.waitForIncomingCall(timeout));
			
			for(SipCall call:receiverCalls)
				assertTrue(call.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
			assertTrue(sender.waitOutgoingCallResponse(timeout));
			
			for(SipCall call:receiverCalls)
				assertTrue(call.sendIncomingCallResponse(Response.OK, "Answer", 0));
			assertTrue(sender.waitOutgoingCallResponse(timeout));

		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}
}
