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

import gov.nist.javax.sip.header.ims.PathHeader;

import java.util.HashMap;
import java.util.Map;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.RFC5626UseCase;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyEdgeRecordRouteTest extends SipServletTestCase {	
	private static transient Logger logger = Logger.getLogger(ProxyEdgeRecordRouteTest.class);
	private static final boolean AUTODIALOG = true;
	private static final String TRANSPORT = "tcp";
	private static final int TIMEOUT = 20000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;	

	public ProxyEdgeRecordRouteTest(String name) {
		super(name);
		listeningPointTransport = TRANSPORT;
                autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
		containerPort = NetworkPortAssigner.retrieveNextPort();
                super.setUp();

                
                senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
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
                params.put( "containerPort", String.valueOf(containerPort)); 
                params.put( "testPort", String.valueOf(senderPort)); 
                params.put( "receiverPort", String.valueOf(receiverPort));
                deployApplication(projectHome + 
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", 
                        params, null);
	}

	public void testProxyProcessOutgoingInitialRequests() throws Exception {
		String fromName = "register-outbound";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRFC5626UseCase(RFC5626UseCase.Proxy);
		sender.sendSipRequest("REGISTER", fromAddress, toAddress, null, null, false, new String[]{"Supported"}, new String[]{"outbound"}, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(200, sender.getFinalResponseStatus());
		PathHeader pathHeader = (PathHeader) sender.getFinalResponse().getHeader(PathHeader.NAME);
		assertNotNull(pathHeader);
		String flow = ((SipURI)pathHeader.getAddress().getURI()).getUser();
		assertNotNull(flow);
		assertNotNull(((SipURI)pathHeader.getAddress().getURI()).getParameter("ob"));
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null,(SipURI) pathHeader.getAddress().getURI(), false, null, null, true);
		Thread.sleep(TIMEOUT);		
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		receiver.setAckSent(false);
		sender.setAckSent(false);
		sender.setAckReceived(false);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testProxyProcessOutgoingInitialRequests403() throws Exception {
		String fromName = "register-outbound";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRFC5626UseCase(RFC5626UseCase.Proxy);
		sender.sendSipRequest("REGISTER", fromAddress, toAddress, null, null, false, new String[]{"Supported"}, new String[]{"outbound"}, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(200, sender.getFinalResponseStatus());
		PathHeader pathHeader = (PathHeader) sender.getFinalResponse().getHeader(PathHeader.NAME);
		assertNotNull(pathHeader);
		String flow = ((SipURI)pathHeader.getAddress().getURI()).getUser();
		assertNotNull(flow);
		assertNotNull(((SipURI)pathHeader.getAddress().getURI()).getParameter("ob"));
		((SipURI)pathHeader.getAddress().getURI()).setUser("77+977+977+977+9AXvvv71C77+9Qu+/vTlt3ITvv70677+977+9Zi9UQ1BfMTI3LjAuMC4xXzUwNzBfMTI3LjAuMC4xXzQ2MTYy");
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null,(SipURI) pathHeader.getAddress().getURI(), false, null, null, true);
		Thread.sleep(TIMEOUT);		
		assertEquals(403,sender.getFinalResponseStatus());					
	}
	
	public void testProxyProcessIncomingInitialRequests() throws Exception {
		String fromName = "invite-inbound";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRFC5626UseCase(RFC5626UseCase.Proxy);
		sender.sendSipRequest("REGISTER", fromAddress, toAddress, null, null, false, new String[]{"Supported"}, new String[]{"outbound"}, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(200, sender.getFinalResponseStatus());
		PathHeader pathHeader = (PathHeader) sender.getFinalResponse().getHeader(PathHeader.NAME);
		assertNotNull(pathHeader);
		String flow = ((SipURI)pathHeader.getAddress().getURI()).getUser();
		assertNotNull(flow);
		assertNotNull(((SipURI)pathHeader.getAddress().getURI()).getParameter("ob"));
		receiver.sendSipRequest("INVITE", fromAddress, toAddress, null,(SipURI) pathHeader.getAddress().getURI(), false, null, null, true);
		Thread.sleep(TIMEOUT);		
		assertTrue(receiver.isAckSent());
		assertTrue(sender.isAckReceived());
		receiver.setAckReceived(false);
		receiver.setAckSent(false);
		sender.setAckSent(false);
		sender.setAckReceived(false);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckSent());
		assertTrue(sender.isAckReceived());
		receiver.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());
		assertTrue(receiver.getOkToByeReceived());		
	}
	
	public void testProxyProcessOutgoingAndIncomingInitialRequests() throws Exception {
		String fromName = "register-outbound";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setRFC5626UseCase(RFC5626UseCase.Proxy);
		sender.sendSipRequest("REGISTER", fromAddress, toAddress, null, null, false, new String[]{"Supported"}, new String[]{"outbound"}, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(200, sender.getFinalResponseStatus());
		PathHeader pathHeader = (PathHeader) sender.getFinalResponse().getHeader(PathHeader.NAME);
		assertNotNull(pathHeader);
		String flow = ((SipURI)pathHeader.getAddress().getURI()).getUser();
		assertNotNull(flow);
		assertNotNull(((SipURI)pathHeader.getAddress().getURI()).getParameter("ob"));
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null,(SipURI) pathHeader.getAddress().getURI(), false, null, null, true);
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

	@Override
	public void tearDown() throws Exception {
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		ctx = tomcat.deployAppContext(
				projectHome + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test");
                assertTrue(ctx.getAvailable());                
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}	
}