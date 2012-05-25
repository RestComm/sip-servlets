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

package org.mobicents.servlet.sip.testsuite.proxy;

import gov.nist.javax.sip.message.MessageExt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyRecordRouteUpdateTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ProxyRecordRouteUpdateTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	
	private static final int TIMEOUT = 20000;

	public ProxyRecordRouteUpdateTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();				
	}

	//https://code.google.com/p/mobicents/issues/detail?id=2264
	//Call flow replicated the RFC3311 example but within the context of a record-route proxy
	//No PRACK involved, UPDATE is used to refresh the session-timer
	public void testProxySendUpdateBeforeFinalResponse() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-update";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitBeforeFinalResponse(10000);
		
		sender.setSendUpdateOn180(true);
		receiver.setSendUpdateAfterUpdate(true);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT*2);		
		assertTrue(sender.isSendUpdate());
		assertTrue(receiver.isUpdateReceived());
		assertFalse(receiver.isSendUpdateAfterUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isUpdateReceived());
		assertTrue(receiver.isSendUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	// Check branchId of the ACK in case the INVITE has an UPDATE in between to make sure that
	// the ACK to 200 OK INVITE is different than the UPDATE branchId
	public void testProxySendUpdateBeforeFinalResponseCheckACKBranchId() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-update";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitBeforeFinalResponse(2000);
		
		sender.setSendUpdateOn180(true);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT*2);		
		assertTrue(sender.isSendUpdate());
		assertTrue(receiver.isUpdateReceived());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		String inviteBranch = ((MessageExt)receiver.getInviteRequest()).getTopmostViaHeader().getBranch();
		String updateBranch = ((MessageExt)receiver.getUpdateRequest()).getTopmostViaHeader().getBranch();
		String ackBranch = ((MessageExt)receiver.getAckRequest()).getTopmostViaHeader().getBranch();
		assertFalse(inviteBranch.equals(ackBranch));
		assertFalse(updateBranch.equals(ackBranch));
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	//https://code.google.com/p/mobicents/issues/detail?id=2264
	//Only the receiver sends UPDATE
	public void testProxyReceiverSendUpdateBeforeFinalResponse() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-update";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitBeforeFinalResponse(15000);
		
		receiver.setSendUpdateAfterProvisionalResponses(true);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT*2);		
		assertTrue(receiver.isSendUpdate());
		assertTrue(sender.isUpdateReceived());
		assertFalse(receiver.isSendUpdateAfterUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
	}
	
	//https://code.google.com/p/mobicents/issues/detail?id=2264
	//Call flow replicated the RFC3311 example but within the context of a record-route proxy
	//PRACK involved
	public void testProxySendUpdateBeforeFinalResponseWithPRACK() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-udpate";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver-update";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		receiver.setWaitForCancel(true);
		
		sender.setSendUpdateAfterPrack(true);
		receiver.setSendUpdateAfterUpdate(true);
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);	
		Thread.sleep(TIMEOUT*2);		
		assertTrue(sender.isSendUpdate());
		assertTrue(receiver.isUpdateReceived());
//		assertFalse(receiver.isSendUpdateAfterUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isUpdateReceived());
		assertTrue(receiver.isSendUpdate());
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());	
	}
	
	// Non regression test for http://code.google.com/p/sipservlets/issues/detail?id=41
	public void testProxyTestUpdateInDialog() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		// part of non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2359
		// allow to check if ACK retrans keep the same different branch id
		sender.setTimeToWaitBeforeAck(2000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		// non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2359	
		String inviteBranch = ((MessageExt)receiver.getInviteRequest()).getTopmostViaHeader().getBranch();
		String ackBranch = ((MessageExt)receiver.getAckRequest()).getTopmostViaHeader().getBranch();
		assertFalse(inviteBranch.equals(ackBranch));
		receiver.setFinalResponse(null);
		receiver.setFinalResponseStatus(-1);
		sender.sendInDialogSipRequest("UPDATE", null, null, null, null, null);		
		Thread.sleep(TIMEOUT);
		receiver.setFinalResponse(null);
		receiver.setFinalResponseStatus(-1);
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
		

		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertFalse(sender.getAllMessagesContent().contains("FINAL"));
	}
	
	public void setupPhones(String transport) throws Exception {
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", transport, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", transport, AUTODIALOG, null, "3", "true");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5057, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
	}
	
	@Override
	public void tearDown() throws Exception {
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
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
}