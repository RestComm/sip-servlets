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

package org.mobicents.servlet.sip.testsuite.security;

import java.util.ListIterator;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.Header;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class CallForwardingB2BUAAuthTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUAAuthTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public CallForwardingB2BUAAuthTest(String name) {
		super(name);
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
	
	public void testCallForwardingAuth() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setChallengeRequests(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"Remote-Party-ID"}, new String[] {"<sip:test@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080>;screen=yes;privacy=off;party=calling;-call-initiator=5016;-call-initiator-location=int;-redirected-by;-int-ext=5016;-ent-name=Acro;-direction=ext;-call-id=55665"}, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		// Non Regression test for http://code.google.com/p/mobicents/issues/detail?id=2094
		// B2b re-invite for authentication will duplicate Remote-Party-ID header
		ListIterator<Header> it = receiver.getInviteRequest().getHeaders("Remote-Party-ID");
		int nbHeaders= 0;
		while (it.hasNext()) {
			Header header = it.next();
			nbHeaders++;
		}
		assertEquals(1, nbHeaders);
	}
	
	// Non regression test for issues 19 http://code.google.com/p/sipservlets/issues/detail?id=19
	public void testCallForwardingShootmeAuthEarlyDialog() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.sendProvisionalResponseBeforeChallenge(true);
		receiver.setChallengeRequests(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender-auth-early-dialog";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "forward-receiver-auth-early-dialog";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"Remote-Party-ID"}, new String[] {"<sip:test@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080>;screen=yes;privacy=off;party=calling;-call-initiator=5016;-call-initiator-location=int;-redirected-by;-int-ext=5016;-ent-name=Acro;-direction=ext;-call-id=55665"}, true);		
		Thread.sleep(TIMEOUT*2);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		// Non Regression test for http://code.google.com/p/mobicents/issues/detail?id=2094
		// B2b re-invite for authentication will duplicate Remote-Party-ID header
		ListIterator<Header> it = receiver.getInviteRequest().getHeaders("Remote-Party-ID");
		int nbHeaders= 0;
		while (it.hasNext()) {
			Header header = it.next();
			nbHeaders++;
		}
		assertEquals(1, nbHeaders);
	}
	
	/*
	 * Non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2114
	 * In B2b servlet, after re-INVITE, and try to create CANCEL will get "final response already sent!" exception.
	 */
	public void testCallForwardingAuthCancel() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		sender.setSendCancelOn1xx(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setChallengeRequests(true);
		receiver.setWaitForCancel(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"Remote-Party-ID"}, new String[] {"<sip:test@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080>;screen=yes;privacy=off;party=calling;-call-initiator=5016;-call-initiator-location=int;-redirected-by;-int-ext=5016;-ent-name=Acro;-direction=ext;-call-id=55665"}, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
	}
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
