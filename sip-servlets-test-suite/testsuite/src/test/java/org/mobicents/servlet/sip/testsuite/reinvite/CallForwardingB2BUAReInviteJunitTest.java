/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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

package org.mobicents.servlet.sip.testsuite.reinvite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.AllowHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/*
 * 
 * Added for Issue 1409 http://code.google.com/p/mobicents/issues/detail?id=1409
 */
public class CallForwardingB2BUAReInviteJunitTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUAReInviteJunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;		
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public CallForwardingB2BUAReInviteJunitTest(String name) {
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

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
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

	// Issue 2500 http://code.google.com/p/mobicents/issues/detail?id=2500
	// B2buaHelper.createRequest() throws a NullPointerException if the request contains an empty header
	public void testCallForwardingCallerSendReInviteSendBye() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
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
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {AllowHeader.NAME}, new String[] {"INVITE, CANCEL, BYE, ACK, OPTIONS"}, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) receiver.getInviteRequest().getHeader(MaxForwardsHeader.NAME);
		assertNotNull(maxForwardsHeader);
		// Non Regression test for http://code.google.com/p/mobicents/issues/detail?id=1490
		// B2buaHelper.createRequest does not decrement Max-forwards
		assertEquals(69, maxForwardsHeader.getMaxForwards());
		
		ListIterator<AllowHeader> allowHeaderIt = receiver.getInviteRequest().getHeaders(AllowHeader.NAME);
		int i = 0; 
		while (allowHeaderIt.hasNext()) {	
			allowHeaderIt.next();
			i++;
		}
		assertEquals(5, i);

		Header methodHeader = receiverProtocolObjects.headerFactory.createHeader("Supported", "");
		// Non Regression for Issue 184 http://code.google.com/p/sipservlets/issues/detail?id=184
		Header allowHeader = receiverProtocolObjects.headerFactory.createAllowHeader("INVITE, CANCEL, BYE, ACK, OPTIONS");
		List<Header> headers = new ArrayList<Header>();
		headers.add(methodHeader);
		headers.add(allowHeader);
		
		sender.sendInDialogSipRequest("INVITE", null, null, null, headers, null);
		receiver.setInviteReceived(false);
		receiver.setAckReceived(false);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		allowHeaderIt = receiver.getInviteRequest().getHeaders(AllowHeader.NAME);
		i = 0; 
		while (allowHeaderIt.hasNext()) {	
			allowHeaderIt.next();
			i++;
		}
		assertEquals(5, i);
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	/**
	 * Non Regression test for 
	 * http://code.google.com/p/mobicents/issues/detail?id=2230
	 * BYE is routed to unexpected IP
	 */
	public void testCallForwardingCallerSendBye408onReinvite() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender-408";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) receiver.getInviteRequest().getHeader(MaxForwardsHeader.NAME);
		assertNotNull(maxForwardsHeader);
		receiver.setFinalResponseToSend(Response.REQUEST_TIMEOUT);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		
		// Non Regression test for http://code.google.com/p/mobicents/issues/detail?id=1490
		// B2buaHelper.createRequest does not decrement Max-forwards
		assertEquals(69, maxForwardsHeader.getMaxForwards());
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	/**
	 * Non Regression test for 
	 * http://code.google.com/p/mobicents/issues/detail?id=2230
	 * 	BYE is routed to unexpected IP
	 */
	public void testCallForwardingCallerSendBye408onReinviteBYENewThread() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender-408-new-thread";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) receiver.getInviteRequest().getHeader(MaxForwardsHeader.NAME);
		assertNotNull(maxForwardsHeader);
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
		receiver.setFinalResponseToSend(Response.REQUEST_TIMEOUT);
		Header methodHeader = receiverProtocolObjects.headerFactory.createHeader("Method", "BYE");
		List<Header> headers = new ArrayList<Header>();
		headers.add(methodHeader);
		sender.sendInDialogSipRequest("INVITE", null, null, null, headers, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		
		receiver.sendInDialogSipRequest("UPDATE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	/**
	 * Non Regression test for 
	 * http://code.google.com/p/mobicents/issues/detail?id=2230
	 * 	BYE is routed to unexpected IP
	 */
	public void testCallForwardingCallerSendBye408onReinviteUPDATENewThread() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender-408-new-thread";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) receiver.getInviteRequest().getHeader(MaxForwardsHeader.NAME);
		assertNotNull(maxForwardsHeader);
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
		receiver.setFinalResponseToSend(Response.REQUEST_TIMEOUT);
		Header methodHeader = receiverProtocolObjects.headerFactory.createHeader("Method", "UPDATE");
		List<Header> headers = new ArrayList<Header>();
		headers.add(methodHeader);
		sender.sendInDialogSipRequest("INVITE", null, null, null, headers, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		
		receiver.sendInDialogSipRequest("UPDATE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isInviteReceived());
		assertTrue(receiver.isAckReceived());
		
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);		
		assertTrue(sender.getOkToByeReceived());
	
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue(sender.getAllMessagesContent().contains("IllegalStateException"));
	}
	
	/**
	 * Non Regression test for 
	 * http://code.google.com/p/mobicents/issues/detail?id=1445
	 * "Bad Request Method. CANCEL" during sending CANCEL to re-INVITE
	 */
	public void testCallForwardingCallerReInviteCancel() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);		
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
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());		
		receiver.setWaitForCancel(true);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(500);
		sender.sendCancel();		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
		sender.setCancelOkReceived(false);
		sender.setRequestTerminatedReceived(false);
		receiver.setCancelReceived(false);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(500);
		sender.sendCancel();		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
	}
	
	/**
	 * Non Regression test for 
	 * http://code.google.com/p/mobicents/issues/detail?id=1837
	 * ACK was received by JAIN-SIP but was not routed to application 
	 */
	public void testCallForwardingCallerReInviteAckRaceInfo() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setDisableSequenceNumberValidation(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		receiverProtocolObjects.start();

		String fromName = "forward-pending-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setTimeToWaitBeforeAck(9000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(8000);
		receiver.sendInDialogSipRequest("UPDATE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckReceived());			
	}		
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
