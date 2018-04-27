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

import java.util.HashMap;
import java.util.Map;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.Request;
import javax.sip.header.Header;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Testing Proxying bridge between udp and tcp transports
 * 
 * All tests here are for http://code.google.com/p/mobicents/issues/detail?id=2623
 * 
 * @author Vladimir Ralev
 *
 */
public class ProxyUdpTcpTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ProxyUdpTcpTest.class);

	private static final String TRANSPORT_UDP = "udp";
	private static final String TRANSPORT_TCP = "tcp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public ProxyUdpTcpTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
				"sip-test-context", 
				"sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}	
	
	@Override
	protected void setUp() throws Exception {
                containerPort = NetworkPortAssigner.retrieveNextPort();            
		super.setUp();

		tomcat.addSipConnector(serverName, sipIpAddress, containerPort, ListeningPoint.TCP);
		tomcat.startTomcat();
	}
	
	public void testCallForwardingCallerSendBye() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);

		String fromName = "proxy-tcp";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "forward-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {UserAgentHeader.NAME, "extension-header"}, new String[] {"TestSipListener UA", "extension-sip-listener"}, addSipConnectorOnStartup);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		

		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());

	}

	public void testCallForwardingCalleeSendByeTCPSender() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
//		receiver.setTransport(false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-udp";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);		
		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());
	}
	
	public void testCallForwardingCalleeSendByeTCPSenderExtraRoutes() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
//		receiver.setTransport(false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-udp";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		SipURI route = senderProtocolObjects.addressFactory.createSipURI(
				toUser, "127.0.0.1:" + containerPort);
		
		sender.extraRoute = true;
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, route, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);		
		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());
		for(Request r : receiver.allRequests) {
			if(r.getMethod().equalsIgnoreCase("ACK")) {
				Header h = r.getHeader("Route");
				if(h == null) fail("We expected extra Route header.");
			}
		}
	}
	
	public void testUnspecifiedCallForwardingCalleeSendByeTCPSender() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
//		receiver.setTransport(false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-unspecified";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.setTransport(false);
		sender.setUseDefaultRoute(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);		
		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());
	}
	
	public void testCallForwardingCallerSendByeOrphan() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-udp-receiver",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-orphan";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "forward-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {UserAgentHeader.NAME, "extension-header"}, new String[] {"TestSipListener UA", "extension-sip-listener"}, addSipConnectorOnStartup);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		

		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());

	}
	
	public void testCallForwardingCallerSendByeOrphan2() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-udp-receiver",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();
                sender.setSendBye(false);

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-orphan";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "forward-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {UserAgentHeader.NAME, "extension-header"}, new String[] {"TestSipListener UA", "extension-sip-listener"}, addSipConnectorOnStartup);	
		
		Thread.sleep(TIMEOUT);
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
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		

		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());

	}

	
	public void testTCPCallForwardingCalleeSendByeTCPSender() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
//		receiver.setTransport(false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-tcp";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);		
		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());
	}
	
	
	public void testCallForwardingCalleeSendBye() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, listeningPointTransport, listeningPointTransport);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
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
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", params, null);                

		String fromName = "proxy-tcp";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);		
		assertEquals(receiverCallIdHeader.getCallId(),senderCallIdHeader.getCallId());
	}

	
	
	@Override
	protected void tearDown() throws Exception {	
		try{
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();	
		} catch(Exception e) {
			e.printStackTrace();
		}
		logger.info("Test completed");
		super.tearDown();
	}


}
