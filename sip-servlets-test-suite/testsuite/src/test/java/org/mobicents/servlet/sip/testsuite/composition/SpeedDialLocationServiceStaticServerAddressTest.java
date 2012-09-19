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

package org.mobicents.servlet.sip.testsuite.composition;

import java.util.ListIterator;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.connector.Connector;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.UDPPacketForwarder;
import org.mobicents.servlet.sip.catalina.SipProtocolHandler;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SpeedDialLocationServiceStaticServerAddressTest extends SipServletTestCase {
	
	private static final int IPLB_ADDRESS_EXTERNAL = 5005;
	private static final int IPLB_ADDRESS_INTERNAL = 5115;

	private static transient Logger logger = Logger.getLogger(SpeedDialLocationServiceStaticServerAddressTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;	
//	private static final int TIMEOUT = 100000000;
	 
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	
	UDPPacketForwarder ipBalancerExternal;
	UDPPacketForwarder ipBalancerInternal;

	public SpeedDialLocationServiceStaticServerAddressTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		addSipConnectorOnStartup =  false;
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
		deploySpeedDial();
		deployLocationService();
	}

	private void deploySpeedDial() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/speed-dial-servlet/src/main/sipapp",
				"speed-dial-context", 
				"speed-dial"));
	}
	
	private void deployLocationService() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
				"location-service-context", 
				"location-service"));
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/composition/speeddial-locationservice-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();

		Connector udpSipConnector = null;
		try {
			udpSipConnector = new Connector(
					SipProtocolHandler.class.getName());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SipProtocolHandler udpProtocolHandler = (SipProtocolHandler) udpSipConnector
				.getProtocolHandler();
		try {
			udpProtocolHandler.setPort(5070);
			udpProtocolHandler.setIpAddress("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");

			udpProtocolHandler.setSignalingTransport("udp");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tomcat.getSipService().setSipStackProperties(null);
		udpProtocolHandler.setUseStaticAddress(true);
		udpProtocolHandler.setStaticServerAddress("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		udpProtocolHandler.setStaticServerPort(IPLB_ADDRESS_EXTERNAL);
		tomcat.getSipService().addConnector(udpSipConnector);
		tomcat.getSipService().setOutboundProxy(System.getProperty("org.mobicents.testsuite.testhostaddr")+":"+IPLB_ADDRESS_INTERNAL);
		//tomcat.getSipService().setOutboundProxy(outboundProxy)
		try {
			tomcat.startTomcat();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		deployApplication();

		senderProtocolObjects = new ProtocolObjects("sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);			
	}
	
	private void startLoadBalancer() {	
		ipBalancerExternal = new UDPPacketForwarder(IPLB_ADDRESS_EXTERNAL, 5070, "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		ipBalancerExternal.start();
		ipBalancerInternal = new UDPPacketForwarder(IPLB_ADDRESS_INTERNAL, 5090, "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
		ipBalancerInternal.start();
	}
	
	public void testSpeedDialLocationServiceCallerSendBye() throws Exception {		
		sender = new TestSipListener(5080, 5005, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5005, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		startLoadBalancer();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		assertTrue(ipBalancerInternal.sipMessageWithoutRetrans.size()>0);
	}
	
	public void testSpeedDialLocationServiceErrorResponse() throws Exception {		
		sender = new TestSipListener(5080, 5005, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5005, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
		startLoadBalancer();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		receiver.setRespondWithError(408);
		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender-expect-408";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isServerErrorReceived());
	}

	public void testSpeedDialLocationServiceCalleeSendBye() throws Exception {
		sender = new TestSipListener(5080, 5005, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5005, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		receiver.setWaitBeforeFinalResponse(2000);
		SipProvider receiverProvider = receiver.createProvider();
		
		startLoadBalancer();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(7000);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(7000);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(7000);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(7000);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(7000);
		receiver.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(7000);
		int count = 0;
		ListIterator viaHeaders = sender.getInviteRequest().getHeaders(ViaHeader.NAME);
		while(viaHeaders.hasNext()) {viaHeaders.next(); count++;}
		

		assertEquals(3, count); // must see exactly 3 via headers in the callee->caller direction
		//TODO: review this assertTrue(ipBalancerExternal.sipMessageWithoutRetrans.size()<=27); // More than 26 messages means the something that should be bypassing is going through it
		assertTrue(receiver.isAckReceived()); // is the ACK working in the callee->caller direction
		assertTrue(sender.isAckReceived()); // is the ACK working in the caller->callee direction
		assertTrue(ipBalancerExternal.sipMessageWithoutRetrans.size()>0);
		assertTrue(ipBalancerInternal.sipMessageWithoutRetrans.size()>0);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());	

		for(Request r : receiver.allRequests) {
			ViaHeader via = (ViaHeader) r.getHeader(ViaHeader.NAME);
			assertEquals(IPLB_ADDRESS_EXTERNAL, via.getPort());
		}
		for(Request r : sender.allRequests) {
			ViaHeader via = (ViaHeader) r.getHeader(ViaHeader.NAME);
			assertEquals(IPLB_ADDRESS_EXTERNAL, via.getPort());
		}

		for(Response message : sender.allResponses) {
			if(message.getStatusCode()>200) {
				fail("We don't expect errors. This is error: " + message);
			}
		}
		for(Response message : receiver.allResponses) {
			if(message.getStatusCode()>200) {
				fail("We don't expect errors. This is error: " + message);
			}
		}
	}

	public void testCancelSpeedDialLocationService() throws Exception {
		sender = new TestSipListener(5080, 5005, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();
		
		startLoadBalancer();

		receiver = new TestSipListener(5090, 5005, receiverProtocolObjects, true);
		receiver.setRecordRoutingProxyTesting(true);
		receiver.setWaitForCancel(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
		assertTrue(ipBalancerInternal.sipMessageWithoutRetrans.size()>0);
		assertTrue(ipBalancerExternal.sipMessageWithoutRetrans.size()>0);
	}

	public void testRemoteAddrPortAndTransport() throws Exception {		
		startLoadBalancer();
		sender = new TestSipListener(5080, 5005, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();
		
		

		receiver = new TestSipListener(5090, 5005, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		receiver.setWaitBeforeFinalResponse(2000);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "remote";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();	
		ipBalancerExternal.stop();
		ipBalancerInternal.stop();
		logger.info("Test completed");
		super.tearDown();
	}


}
