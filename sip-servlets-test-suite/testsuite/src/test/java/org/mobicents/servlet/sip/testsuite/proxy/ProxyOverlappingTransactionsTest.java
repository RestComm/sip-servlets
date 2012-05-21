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
import java.util.HashSet;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyOverlappingTransactionsTest extends SipServletTestCase {	
	private static transient Logger logger = Logger.getLogger(ProxyOverlappingTransactionsTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;


	private static final int TIMEOUT = 20000;

	public ProxyOverlappingTransactionsTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();				
	}
	
	public void testProxyOverlapReinvites() throws Exception {
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
		Thread.sleep(TIMEOUT/2);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		sender.setAckSent(false);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		receiver.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.numberOf491s>0);
		assertTrue(receiver.numberOf491s>0);
		receiver.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());
		assertTrue(receiver.getOkToByeReceived());		
	}
	
	//http://code.google.com/p/sipservlets/issues/detail?id=99
	public void testProxyOverlapReinviteInfo() throws Exception {
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
		
		Thread.sleep(TIMEOUT/2);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		receiver.setAckReceived(false);
		sender.setAckSent(false);
		sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
		Thread.sleep(1);
		
		sender.sendInDialogSipRequest("INFO", null, null, null, null, null);
		
		Thread.sleep(TIMEOUT);
		//assertTrue(sender.numberOf491s>0);
		//assertTrue(receiver.numberOf491s>0);
		receiver.sendBye();
		HashMap<String,Request> ackBranches = new HashMap<String,Request>();
		for(Request request:receiver.allRequests) {
			if(request.getMethod().equals("ACK")) {
				ViaHeader via = (ViaHeader) request.getHeader("VIa");
				String branch = via.getBranch();
				ackBranches.put(branch, request);
			}
		}
		for(Request request:receiver.allRequests) {
			if(request.getMethod().equals("INFO")) {
				ViaHeader via = (ViaHeader) request.getHeader("VIa");
				String branch = via.getBranch();
				if(ackBranches.containsKey(branch)) 
					fail("INFO and ACK have the same branch id for " + request + " and " + ackBranches.get(branch));;
			}
		}
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getByeReceived());
		assertTrue(receiver.getOkToByeReceived());		
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
