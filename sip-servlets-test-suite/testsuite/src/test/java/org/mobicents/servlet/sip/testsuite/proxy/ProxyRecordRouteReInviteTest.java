/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.testsuite.proxy;

import gov.nist.javax.sip.header.HeaderExt;
import gov.nist.javax.sip.message.MessageExt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
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
                autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
                containerPort = NetworkPortAssigner.retrieveNextPort();
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
		logger.info("RequestsProcessedByMethod " + tomcat.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod());
		logger.info("RequestsSentByMethod " + tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod());
		logger.info("ResponsesProcessedByStatusCode " + tomcat.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode());
		logger.info("ResponsesSentByStatusCode " + tomcat.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode());
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod(Request.INVITE)>1);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod(Request.ACK)>1);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod(Request.BYE)>0);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode("2XX")>1);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.INVITE)>1);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.ACK)>1);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.BYE)>0);
		assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode("2XX")>1);
	}
	
	/*
	 * https://code.google.com/p/sipservlets/issues/detail?id=21
	 */
	public void testProxyCallerFinalResponseOnSubsequentRequest() throws Exception {
        setupPhones(ListeningPoint.UDP);
        String fromName = "unique-location-final-response-subsequent";
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
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);      
        Thread.sleep(TIMEOUT);
        assertEquals(491, sender.getFinalResponseStatus());
        assertFalse(receiver.isAckReceived());
        receiver.setAckReceived(false);
        receiver.setAckSent(false);
        sender.setAckSent(false);
        sender.setAckReceived(false);
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
		tomcat.addSipConnector(serverName, sipIpAddress, containerPort, ListeningPoint.TCP);
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
		Header rh = senderProtocolObjects.headerFactory.createHeader("Route", "sip:extra-route@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + ctx.getServletContext().getInitParameter("receiverPort") + ";lr");
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

    /*
     * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=202
     */
    public void testProxyModifySDP() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "modify-SDP";
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
        assertEquals("SDP modified successfully", new String(sender.getFinalResponse().getRawContent(), "UTF-8"));
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
		assertEquals("SDP modified successfully", new String(sender.getFinalResponse().getRawContent(), "UTF-8"));
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckSent());
		assertTrue(sender.isAckReceived());
		assertEquals("SDP modified successfully", new String(receiver.getFinalResponse().getRawContent(), "UTF-8"));
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());		
	}
    
	public void setupPhones(String transport) throws Exception {
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", transport, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", transport, AUTODIALOG, null, null, null);
                
                int senderPort = NetworkPortAssigner.retrieveNextPort(); 
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
                
                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort)); 
                params.put( "testPort", String.valueOf(senderPort)); 
                params.put( "receiverPort", String.valueOf(receiverPort));               
                deployApplication(projectHome + 
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", 
                        params, null);                      
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
