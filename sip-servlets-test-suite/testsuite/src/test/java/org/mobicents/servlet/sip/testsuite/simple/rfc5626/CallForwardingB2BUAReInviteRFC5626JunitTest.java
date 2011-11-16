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

package org.mobicents.servlet.sip.testsuite.simple.rfc5626;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.MaxForwardsHeader;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.RFC5626UseCase;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/*
 * 
 * Added for Issue 2254 http://code.google.com/p/mobicents/issues/detail?id=2254
 * Testing B2BUA Use case not covered by the spec where the UA connects directly to a B2BUA instead of edge proxy 
 */
public class CallForwardingB2BUAReInviteRFC5626JunitTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUAReInviteRFC5626JunitTest.class);

	private static final String TRANSPORT = "tcp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;		
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public CallForwardingB2BUAReInviteRFC5626JunitTest(String name) {
		super(name);
		listeningPointTransport = TRANSPORT;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-b2bua-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();

		senderProtocolObjects = new ProtocolObjects("forward-sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);					
	}
	
	/**
	 * UAC sends INVITE with wrong Via and Contact IP address to make sure that when the subsequent request from the other end is received back
	 * it can be routed back to UAC on the correct IP through the ob parameter support
	 * @throws Exception
	 */
	public void testCallForwardingCallerSendBye() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setSendReinvite(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-tcp-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRFC5626UseCase(RFC5626UseCase.B2BUA);		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isInviteReceived());
		assertTrue(sender.isAckReceived());
		assertNotNull(sender.getInviteRequest().getHeader("ReInvite"));
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) receiver.getInviteRequest().getHeader(MaxForwardsHeader.NAME);
		assertNotNull(maxForwardsHeader);
		// Non Regression test for http://code.google.com/p/mobicents/issues/detail?id=1490
		// B2buaHelper.createRequest does not decrement Max-forwards
		assertEquals(69, maxForwardsHeader.getMaxForwards());
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
		assertEquals(1,sender.bindings); //http://code.google.com/p/mobicents/issues/detail?id=2100
		maxForwardsHeader = (MaxForwardsHeader) receiver.getByeRequestReceived().getHeader(MaxForwardsHeader.NAME);
		assertNotNull(maxForwardsHeader);
		// Non Regression test for http://code.google.com/p/mobicents/issues/detail?id=1490
		// B2buaHelper.createRequest does not decrement Max-forwards
		assertEquals(69, maxForwardsHeader.getMaxForwards());
	}	
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
