package org.mobicents.servlet.sip.testsuite.click2call;


import java.net.URL;
import java.util.Properties;

import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;

public class Click2CallBasicTest extends SipServletTestCase{

	private static final String CLICK2DIAL_URL = "http://127.0.0.1:8080/click2call/call?from=sip:from@127.0.0.1:5056&to=sip:to@127.0.0.1:5057";
	
	private static Log logger = LogFactory.getLog(Click2CallBasicTest.class);
	
	private SipStack[] sipStackReceivers;
	
	private SipPhone[] sipPhoneReceivers;
	
	private static final int timeout = 5000;
	
	private static final int receiversCount = 2;
	
	// Don't restart the server for this set of tests.
	private static boolean firstTime = true;

	public Click2CallBasicTest(String name) {
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
		firstTime = true;
	}
	
	@Override
	public void tearDown()
	{
		try {
			for(SipPhone sp : sipPhoneReceivers) sp.dispose();
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
				projectHome + "/sip-servlets-test-suite/applications/click-to-call-servlet/src/main/sipapp",
				"click2call-context", "/click2call"));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/click2call/click-to-call-dar.properties";
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

	public void testClickToCallNoConvergedSession() throws InterruptedException {
		init();
		try{									
			SipCall[] receiverCalls = new SipCall[receiversCount];
			
			receiverCalls[0] = sipPhoneReceivers[0].createSipCall();
			receiverCalls[1] = sipPhoneReceivers[0].createSipCall();
			
			receiverCalls[0].listenForIncomingCall();
			receiverCalls[1].listenForIncomingCall();
			
			logger.info("Trying to reach url : " + CLICK2DIAL_URL);
			URL url = new URL(CLICK2DIAL_URL);
			url.openConnection().connect();
			
			receiverCalls[0].waitForIncomingCall(timeout);
			
			assertTrue(receiverCalls[0].sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
			assertTrue(receiverCalls[0].sendIncomingCallResponse(Response.OK, "OK", 0));
			
			receiverCalls[1].waitForIncomingCall(timeout);
			
			assertTrue(receiverCalls[1].sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
			assertTrue(receiverCalls[1].sendIncomingCallResponse(Response.OK, "OK", 0));
		} catch (Exception e) {
			fail(e.toString());
			e.printStackTrace();
		}
	}
}
