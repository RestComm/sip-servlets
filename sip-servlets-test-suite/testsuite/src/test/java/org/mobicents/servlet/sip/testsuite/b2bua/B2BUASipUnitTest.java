/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class B2BUASipUnitTest extends SipServletTestCase {
	private static final Logger logger = Logger.getLogger(B2BUASipUnitTest.class);
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000; 
	SipStack sipStackA;
	SipStack sipStackB;
	
	SipPhone sipPhoneA;
	SipPhone sipPhoneB;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	
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
	
	public void setupPhones(String a, String b) throws Exception {
		Properties properties1 = new Properties();
		//properties1.setProperty("javax.sip.IP_ADDRESS", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		String transport = "udp";
		String peerHostPort1 = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070";
		properties1.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ transport);
		properties1.setProperty("javax.sip.STACK_NAME", "sender");
		properties1.setProperty("sipunit.BINDADDR", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		properties1.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"logs/b2buadebug1.txt");
		properties1.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"logs/b2bualog1.xml");
		properties1.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
			"32");

		Properties properties2 = new Properties();
		// properties2.setProperty("javax.sip.IP_ADDRESS", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		String peerHostPort2 = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070";
		properties2.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort2 + "/"
				+ transport);
		properties2.setProperty("javax.sip.STACK_NAME", "receiver");
		properties2.setProperty("sipunit.BINDADDR", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		properties2.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			"logs/b2buadebug2.txt");
		properties2.setProperty("gov.nist.javax.sip.SERVER_LOG",
			"logs/b2bualog2.xml");
		properties2.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
			"32");

		sipStackA = new SipStack(SipStack.PROTOCOL_UDP , 5058, properties1);					
		sipPhoneA = sipStackA.createSipPhone("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "", SipStack.PROTOCOL_UDP, 5070, a);

		sipStackB = new SipStack(SipStack.PROTOCOL_UDP , 5059, properties2);
		sipPhoneB = sipStackB.createSipPhone("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "", SipStack.PROTOCOL_UDP, 5070, b);
	}			

	public void testB2BUASipUnit() throws Exception {
		setupPhones("sip:sender@nist.gov", "sip:aa@nist.gov");
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
		Thread.sleep(TIMEOUT);
	}
	
	public void testB2BUASipUnitCancelNoResponse() throws Exception {
		setupPhones("sip:sender@nist.gov", "sip:aa@nist.gov");
		SipCall callA = sipPhoneA.createSipCall();
		SipCall callB = sipPhoneB.createSipCall();
		
		callB.listenForIncomingCall();
		Thread.sleep(300);
		callA.initiateOutgoingCall("sip:cancel-no-response@nist.gov", null);
		//sipunit doesn't succeed to send the ACK since it tries to do it with 
		//Dialog.createRequest(Request.ACK)
		//assertTrue(callA.sendInviteOkAck());
		callA.sendInviteOkAck();				
		
		sipPhoneA.dispose();
		sipPhoneB.dispose();
		sipStackA.dispose();
		sipStackB.dispose();
		Thread.sleep(TIMEOUT);
	}
	
	public void testB2BUASipUnitGenerateResponses() throws Exception {
		senderProtocolObjects = new ProtocolObjects("generateResponses",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("aa",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);								
		
		String fromName = "generateResponses";
		String fromSipAddress = "nist.gov";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "nist.gov";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		sender.setTimeToWaitBeforeBye(TIMEOUT);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5059, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		//checking numbers of ACK received see http://forums.java.net/jive/thread.jspa?messageID=277840
		assertEquals(1,receiver.ackCount);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
	}
}
