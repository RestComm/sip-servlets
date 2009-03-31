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
package org.mobicents.servlet.sip.testsuite.b2bua;

import java.util.Properties;

import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;

public class B2BUASipUnitTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(B2BUASipUnitTest.class);
	private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000; 
	SipStack sipStackA;
	SipStack sipStackB;
	
	SipPhone sipPhoneA;
	SipPhone sipPhoneB;
	
	public B2BUASipUnitTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/b2bua-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/b2bua/b2bua-sip-servlet-dar.properties";
	}
	
	public void setupPhones() throws Exception {
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
		properties1.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
			"32");

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
		properties2.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
			"32");

		sipStackA = new SipStack(SipStack.PROTOCOL_UDP , 5058, properties1);					
		sipPhoneA = sipStackA.createSipPhone("localhost", SipStack.PROTOCOL_UDP, 5070, "sip:sender@nist.gov");

		sipStackB = new SipStack(SipStack.PROTOCOL_UDP , 5059, properties2);
		sipPhoneB = sipStackB.createSipPhone("localhost", SipStack.PROTOCOL_UDP, 5070, "sip:aa@nist.gov");
	}
		
	public void init() throws Exception
	{
		setupPhones();
			
		SipCall callA = sipPhoneA.createSipCall();
		SipCall callB = sipPhoneB.createSipCall();
		
		callB.listenForIncomingCall();
		Thread.sleep(300);
		callA.initiateOutgoingCall("sip:receiver@nist.gov", null);
		
		assertTrue(callB.waitForIncomingCall(TIMEOUT));
		
		assertTrue(callB.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
		assertTrue(callA.waitOutgoingCallResponse(TIMEOUT));
		
		assertTrue(callB.sendIncomingCallResponse(Response.OK, "OK", 0));
		assertTrue(callB.waitForAck(TIMEOUT));
		
		assertTrue(callA.waitOutgoingCallResponse(TIMEOUT));
		assertNotNull(callA.findMostRecentResponse(Response.OK));
		//sipunit doesn't succeed to send the ACK since it tries to do it with 
		//Dialog.createRequest(Request.ACK)
		//assertTrue(callA.sendInviteOkAck());
		callA.sendInviteOkAck();				
		
		assertTrue(callA.disconnect());			
		assertTrue(callB.waitForDisconnect(TIMEOUT));
		assertTrue(callB.respondToDisconnect());
		
		sipPhoneA.dispose();
		sipPhoneB.dispose();
		sipStackA.dispose();
		sipStackB.dispose();
	}

	public void testB2BUASipUnit() throws Exception {
		init();
		Thread.sleep(TIMEOUT);
	}
}
