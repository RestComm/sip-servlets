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

import gov.nist.javax.sip.header.HeaderExt;
import gov.nist.javax.sip.message.MessageExt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.Header;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyRecordRouteReInviteTest extends SipServletTestCase {	
	private static transient Logger logger = Logger.getLogger(ProxyRecordRouteReInviteTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;


	private static final int TIMEOUT = 20000;

	public ProxyRecordRouteReInviteTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();				
	}

	public void testProxyCallerSendBye() throws Exception {
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
		receiver.setAckReceived(false);
		sender.setAckSent(false);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		receiver.setAckSent(false);
		sender.setAckSent(false);
		sender.setAckReceived(false);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckSent());
		assertTrue(sender.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	// Test for http://code.google.com/p/sipservlets/issues/detail?id=44
	public void testProxyReinviteAckSeenByApp() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-ack-seen-by-app";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		// non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2359	
		String inviteBranch = ((MessageExt)receiver.getInviteRequest()).getTopmostViaHeader().getBranch();
		String ackBranch = ((MessageExt)receiver.getAckRequest()).getTopmostViaHeader().getBranch();
		assertFalse(inviteBranch.equals(ackBranch));
		receiver.setAckReceived(false);
		sender.setAckSent(false);
		receiver.setFinalResponseToSend(491);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);		
		Thread.sleep(TIMEOUT);			
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertFalse(sender.getAllMessagesContent().contains("ack-seen-by-app"));
	}
	
	public void testProxyCalleeSendBye() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		sender.setAckSent(false);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		sender.setAckSent(false);		
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckSent());
		assertTrue(sender.isAckReceived());
		receiver.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());
		assertTrue(receiver.getOkToByeReceived());		
	}
	
	/*
	 * Non regression test for Issue 1792
	 */
	public void testProxyCancelTCP() throws Exception {
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, ListeningPoint.TCP);
		setupPhones(ListeningPoint.TCP);
		String fromName = "unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.TRYING);
		provisionalResponsesToSend.add(Response.RINGING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		
		receiver.setWaitForCancel(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(1000);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isCancelReceived());
		assertTrue(sender.isCancelOkReceived());		
		assertTrue(sender.isRequestTerminatedReceived());			
	}
	
	// Issue http://code.google.com/p/mobicents/issues/detail?id=1847
	public void testProxyExtraRouteNoRewrite() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Header rh = senderProtocolObjects.headerFactory.createHeader("Route", "sip:extra-route@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5057;lr");
		LinkedList<Header> hh = new LinkedList<Header>();
		hh.add(rh);
		Thread.sleep(TIMEOUT/4);
		sender.sendInDialogSipRequest("INVITE", null, null, null, hh, "udp");
		Thread.sleep(TIMEOUT/2);
		
		assertTrue(receiver.getInviteRequest().getRequestURI().toString().contains("extra-route"));
	}
	
	// Check branchId of the ACK in case the reINVITE has an INFO in between to make sure that
	// the ACK to 200 OK reINVITE is different than the INFO branchId
	public void testProxyReinviteINFOCheckACKBranchId() throws Exception {
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
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived()); 
		receiver.setAckReceived(false);
		sender.setAckSent(false);
		sender.sendInDialogSipRequest("INFO", null, null, null, null, null);		
		Thread.sleep(500);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		receiver.setAckSent(false);
		sender.setAckSent(false);
		sender.setAckReceived(false);
		//non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2359	
		String inviteBranch = ((MessageExt)receiver.getInviteRequest()).getTopmostViaHeader().getBranch();
		String infoBranch = ((MessageExt)receiver.getInfoRequest()).getTopmostViaHeader().getBranch();
		String ackBranch = ((MessageExt)receiver.getAckRequest()).getTopmostViaHeader().getBranch();
		assertFalse(inviteBranch.equals(ackBranch));
		assertFalse(infoBranch.equals(ackBranch));
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	// https://code.google.com/p/sipservlets/issues/detail?id=238
    public void testProxyINFOLeak() throws Exception {
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
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        assertTrue(receiver.isAckReceived()); 
        receiver.setAckReceived(false);
        sender.setAckSent(false);
        sender.sendInDialogSipRequest("INFO", null, null, null, null, null);        
        Thread.sleep(500);
        sender.sendInDialogSipRequest("INFO", null, null, null, null, null);      
        Thread.sleep(500);
        sender.sendInDialogSipRequest("INFO", null, null, null, null, null);
        Thread.sleep(500);
        sender.sendInDialogSipRequest("INFO", null, null, null, null, null);
        Thread.sleep(500);
        sender.sendInDialogSipRequest("INFO", null, null, null, null, null);
        Thread.sleep(TIMEOUT*2);        
        sender.sendBye();
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());        
        assertTrue(sender.getOkToByeReceived());
        assertNotNull(sender.getFinalResponse().getHeader("X-Proxy-Transactions"));
        assertEquals("1",((HeaderExt)sender.getFinalResponse().getHeader("X-Proxy-Transactions")).getValue());
    }

	public void setupPhones(String transport) throws Exception {
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", transport, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", transport, AUTODIALOG, null, null, null);
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
