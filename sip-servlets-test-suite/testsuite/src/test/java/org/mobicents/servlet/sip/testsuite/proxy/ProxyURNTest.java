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

import java.util.ArrayList;
import java.util.List;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ResponseType;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyURNTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(ProxyRecordRouteUpdateTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	
	private static final String SESSION_READY_TO_INVALIDATE = "sessionReadyToInvalidate";
	private static final String SIP_SESSION_READY_TO_INVALIDATE = "sipSessionReadyToInvalidate";
	
	private static final int TIMEOUT = 20000;

	public ProxyURNTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();				
	}

	//https://code.google.com/p/mobicents/issues/detail?id=2327
	//https://code.google.com/p/mobicents/issues/detail?id=2337
	public void testProxySendInviteToURN() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		URI toURNAddress = senderProtocolObjects.addressFactory.createURI("urn:service:sos");
		
		List<Integer> provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.TRYING);
		receiver.setProvisionalResponsesToSend(provisionalResponsesToSend);
		
		receiver.setRespondWithError(486);
		
		sender.sendSipRequest("INVITE", fromAddress, toURNAddress, null, null, true);
		sender.setUseToURIasRequestUri(true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isServerErrorReceived());
		assertTrue(sender.getAllMessagesContent().contains(SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getAllMessagesContent().contains(SIP_SESSION_READY_TO_INVALIDATE));
	}
	
	public void testProxySendInviteToURNWithoutRoute() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-urn";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		URI toURNAddress = senderProtocolObjects.addressFactory.createURI("urn:service:sos");
		
		sender.sendSipRequest("INVITE", fromAddress, toURNAddress, null, null, true);
		sender.setUseToURIasRequestUri(true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isServerErrorReceived());
		assertTrue(sender.getAllMessagesContent().contains(SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getAllMessagesContent().contains(SIP_SESSION_READY_TO_INVALIDATE));
	}
	
	public void testProxySendInviteToURNWithRoute() throws Exception {
		setupPhones(ListeningPoint.UDP);
		String fromName = "unique-location-urn-route";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		URI toURNAddress = senderProtocolObjects.addressFactory.createURI("urn:service:sos");
		
		sender.setSendBye(true);
		sender.sendSipRequest("INVITE", fromAddress, toURNAddress, null, null, true);
		sender.setUseToURIasRequestUri(true);
		Thread.sleep(TIMEOUT*2);
		assertFalse(sender.isServerErrorReceived());
		assertTrue(sender.getAllMessagesContent().contains(SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getAllMessagesContent().contains(SIP_SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getOkToByeReceived());
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