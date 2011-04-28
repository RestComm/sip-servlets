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

package org.mobicents.servlet.sip.testsuite.simple.prack;

import gov.nist.javax.sip.header.SIPHeader;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootmePrackSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ShootmePrackSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;
//	private static final int TIMEOUT = 100000000;
	private static final int TIMEOUT_CSEQ_INCREASE = 100000;
	
	
	TestSipListener sender;
	SipProvider senderProvider = null;
	ProtocolObjects senderProtocolObjects;
	
//	TestSipListener registerReciever;	
//	ProtocolObjects registerRecieverProtocolObjects;	
	
	public ShootmePrackSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, "4", "true");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();		
		
		
//		registerRecieverProtocolObjects =new ProtocolObjects(
//				"registerReciever", "gov.nist", TRANSPORT, AUTODIALOG, null);
//		registerReciever = new TestSipListener(5058, 5070, registerRecieverProtocolObjects, true);
//		SipProvider registerRecieverProvider = registerReciever.createProvider();			
//		registerRecieverProvider.addSipListener(registerReciever);
//		registerRecieverProtocolObjects.start();		
	}
	
	public void testShootmePrack() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "prack";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isPrackSent());
		assertTrue(sender.isOkToPrackReceived());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testShootmePrackAckReceivedTwice() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "prack-test2xACK";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
				
		sender.setSendBye(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, false);
		sender.setTimeToWaitBeforeAck(1000);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isPrackSent());
		assertTrue(sender.isOkToPrackReceived());
		assertTrue(sender.isAckSent());
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender.getAllMessagesContent().size());
//		assertTrue(sender.getOkToByeReceived());		
		
	}
	
	// non regression test for Issue 1552 http://code.google.com/p/mobicents/issues/detail?id=1552
	// Container does not recognise 100rel if there are other extensions on the Require or Supported line
	public void testShootmePrackMultipleRequire() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "prack";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String[] headerNames = new String[]{"Require", "Require"};
		String[] headerValues = new String[]{"timer", "100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isPrackSent());
		assertTrue(sender.isOkToPrackReceived());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	// non regression test for Issue 1564 : http://code.google.com/p/mobicents/issues/detail?id=1564
	// Send reliably can add a duplicate 100rel to the requires line
	public void testShootmePrackRequirePresent() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "prack-require-present";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String[] headerNames = new String[]{"Require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isPrackSent());
		assertTrue(sender.isOkToPrackReceived());
		ListIterator<SIPHeader> iterator = sender.getInformationalResponse().getHeaders("Require");
		int nbRequire = 0;
		while (iterator.hasNext()) {
			SIPHeader sipHeader = iterator.next();
			nbRequire++;
		}
		assertEquals(1, nbRequire);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}		
	
	public void testShootmePrackReinvite() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "prackisendreinvite";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.setSendReinvite(true);
		sender.setSendBye(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isPrackSent());
		assertTrue(sender.isOkToPrackReceived());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void xxxtestNoPrackReceived() throws Exception {
		String fromName = "noAckReceived";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false, headerNames, headerValues, true);
		sender.setSendAck(false);
		Thread.sleep(TIMEOUT);
		assertEquals( 200, sender.getFinalResponseStatus());
		assertFalse(sender.isAckSent());
		Thread.sleep(TIMEOUT_CSEQ_INCREASE);
		List<String> allMessagesContent = sender.getAllMessagesContent();
		assertEquals(1,allMessagesContent.size());
		assertEquals("noAckReceived", allMessagesContent.get(0));
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
//		registerRecieverProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
