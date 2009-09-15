/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.Properties;

import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;

public class ProxyBranchTimeoutTest extends SipServletTestCase {

//	private static transient Logger logger = Logger.getLogger(ProxyBranchTimeoutTest.class);

	private SipStack sipStackSender;
	private SipStack[] sipStackReceivers;

	private SipPhone sipPhoneSender;
	private SipPhone[] sipPhoneReceivers;

	private static final int TIMEOUT = 5000;

	private static final int receiversCount = 1;

	// Don't restart the server for this set of tests.
	private static boolean firstTime = true;

	public ProxyBranchTimeoutTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		if (firstTime) {
			super.setUp();
		}
		firstTime = true;
	}

	@Override
	public void tearDown() throws Exception {
		sipPhoneSender.dispose();
		for (SipPhone sp : sipPhoneReceivers)
			sp.dispose();
		sipStackSender.dispose();
		for (SipStack ss : sipStackReceivers)
			ss.dispose();
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat
				.deployContext(
						projectHome
								+ "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
						"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
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

	public void setupPhones() throws Exception {
		sipStackReceivers = new SipStack[receiversCount];
		sipPhoneReceivers = new SipPhone[receiversCount];
		
		sipStackSender = makeStack(SipStack.PROTOCOL_UDP, 5058);
//		ListeningPoint lp = sipStackSender.getSipProvider()
//				.getListeningPoint("udp");
//		String stackIPAddress = lp.getIPAddress();
		sipPhoneSender = sipStackSender.createSipPhone("localhost",
				SipStack.PROTOCOL_UDP, 5070, "sip:sender@nist.gov");

		for (int q = 0; q < receiversCount; q++) {
			int port = 5058 - 1 - q;
			sipStackReceivers[q] = makeStack(SipStack.PROTOCOL_UDP, port);
			sipPhoneReceivers[q] = sipStackReceivers[q].createSipPhone(
					"localhost", SipStack.PROTOCOL_UDP, 5070,
					"sip:receiver@nist.gov");
		}		
	}

	public void init() throws Exception {
		setupPhones();
	}

	// For some reason we can't intercept CANCEL with SipUnit
	public void testProxyBranchTimeoutRequestWithoutCancel()
			throws Exception {
		init();
		
		SipCall sender = sipPhoneSender.createSipCall();
		SipCall[] receiverCalls = new SipCall[receiversCount];

		receiverCalls[0] = sipPhoneReceivers[0].createSipCall();

		receiverCalls[0].listenForIncomingCall();

		sender.initiateOutgoingCall("sip:receiver@nist.gov", null);

		receiverCalls[0].waitForIncomingCall(TIMEOUT);

		receiverCalls[0].sendIncomingCallResponse(Response.RINGING,
				"Ringing", 0);
		assertTrue(sender.waitOutgoingCallResponse(TIMEOUT));

		Thread.sleep(11000); // The built-in response wait doesnt work.
		receiverCalls[0].waitForDisconnect(TIMEOUT);

		boolean code408received = false;
		for (int q = 0; q < 5; q++) {
			assertTrue(sender.waitOutgoingCallResponse(TIMEOUT));
			int responseCode = sender.getLastReceivedResponse()
					.getStatusCode();
			if (responseCode == 408) {
				code408received = true;
				break;
			}
		}
		assertTrue(code408received);
	}
}
