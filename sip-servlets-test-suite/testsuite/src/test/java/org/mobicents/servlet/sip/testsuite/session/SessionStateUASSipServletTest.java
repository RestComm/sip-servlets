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

package org.mobicents.servlet.sip.testsuite.session;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipSession;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.TransactionDoesNotExistException;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.SipURI;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SessionStateUASSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SessionStateUASSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final String SEND_1XX_2XX = "send1xx_2xx";
	private static final String SEND_1XX_4XX = "send1xx_4xx";
	private static final String SEND_4XX = "send4xx";
	private static final String SEND_2XX = "send2xx";
	private static final String TEST_TIMEOUT = "test_timeout";
	private static final String STX_408_RECEIVED = "408 received on STX";
	
	private List<String> send_1xx_2xx_sessionStateList;
	private List<String> send_1xx_4xx_sessionStateList;
	private List<String> send_4xx_sessionStateList;
	private List<String> send_2xx_sessionStateList;
	private List<String> send_subsequent_487_sessionStateList;
	private static final int TIMEOUT = 20000;
	private static final int TIMEOUT_EXPIRATION = 70000;
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;	
	
	TestSipListener receiver;
	ProtocolObjects receiverProtocolObjects;	

	public SessionStateUASSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/session-state-uas/src/main/sipapp",
				"sip-test-context",
				"sip-test"));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/session/session-state-uas-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();
		send_1xx_2xx_sessionStateList = new ArrayList<String>();
		send_1xx_2xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_1xx_2xx_sessionStateList.add(SipSession.State.EARLY.toString());
		send_1xx_2xx_sessionStateList.add(SipSession.State.CONFIRMED.toString());
		send_1xx_2xx_sessionStateList.add(SipSession.State.TERMINATED.toString());
		
		send_1xx_4xx_sessionStateList = new ArrayList<String>();
		send_1xx_4xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_1xx_4xx_sessionStateList.add(SipSession.State.EARLY.toString());
		send_1xx_4xx_sessionStateList.add(SipSession.State.TERMINATED.toString());				
		
		send_2xx_sessionStateList = new ArrayList<String>();
		send_2xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_2xx_sessionStateList.add(SipSession.State.CONFIRMED.toString());
		send_2xx_sessionStateList.add(SipSession.State.TERMINATED.toString());
		
		send_4xx_sessionStateList = new ArrayList<String>();
		send_4xx_sessionStateList.add(SipSession.State.INITIAL.toString());		
		send_4xx_sessionStateList.add(SipSession.State.TERMINATED.toString());
		
		send_subsequent_487_sessionStateList = new ArrayList<String>();
		send_subsequent_487_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_subsequent_487_sessionStateList.add(SipSession.State.CONFIRMED.toString());
		send_subsequent_487_sessionStateList.add(SipSession.State.CONFIRMED.toString());
		send_subsequent_487_sessionStateList.add(SipSession.State.TERMINATED.toString());
	}
	
	public void testSessionStateUAS_1xx_2xx() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		createAndExecuteCall(SEND_1XX_2XX, true);
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		assertEquals(send_1xx_2xx_sessionStateList.size(), sender.getAllMessagesContent().size());		
		for (int i = 0; i < send_1xx_2xx_sessionStateList.size(); i++) {
			assertTrue(sender.getAllMessagesContent().contains(send_1xx_2xx_sessionStateList.get(i)));
		}	
	}
	
	public void testSessionStateUAS_1xx_4xx() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		createAndExecuteCall(SEND_1XX_4XX, false);
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		assertEquals(send_1xx_4xx_sessionStateList.size(), sender.getAllMessagesContent().size());		
		for (int i = 0; i < send_1xx_4xx_sessionStateList.size(); i++) {
			assertTrue(sender.getAllMessagesContent().contains(send_1xx_4xx_sessionStateList.get(i)));
		}	
	}
	
	public void testSessionStateUAS_2xx() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		createAndExecuteCall(SEND_2XX, true);
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		assertEquals(send_2xx_sessionStateList.size(), sender.getAllMessagesContent().size());		
		for (int i = 0; i < send_2xx_sessionStateList.size(); i++) {
			assertTrue(sender.getAllMessagesContent().contains(send_2xx_sessionStateList.get(i)));
		}	
	}

	public void testSessionStateUAS_4xx() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		createAndExecuteCall(SEND_4XX, false);
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		assertEquals(send_4xx_sessionStateList.size(), sender.getAllMessagesContent().size());		
		for (int i = 0; i < send_4xx_sessionStateList.size(); i++) {
			assertTrue(sender.getAllMessagesContent().contains(send_4xx_sessionStateList.get(i)));
		}	
	}
	
	/**
	 * http://code.google.com/p/mobicents/issues/detail?id=1438
	 * Sip Session become TERMINATED after receiving 487 response to subsequent request
	 */
	public void testSessionStateUAS_SubsequentRequest_FinalResponse() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		createAndExecuteCall(SEND_2XX, false);
		
		sender.sendInDialogSipRequest(Request.REFER, null, null, null, null, null);	
		
		Thread.sleep(TIMEOUT);
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		int numberOfTerminated = 0; 
		int numberOfConfirmed = 0;
		assertEquals(send_subsequent_487_sessionStateList.size(), sender.getAllMessagesContent().size());		
		for (int i = 0; i < send_subsequent_487_sessionStateList.size(); i++) {
			String messageContent = sender.getAllMessagesContent().get(i);
			assertTrue(sender.getAllMessagesContent().contains(messageContent));
			if(messageContent.equalsIgnoreCase(SipSession.State.CONFIRMED.toString())) {
				numberOfConfirmed++;
			}
			if(messageContent.equalsIgnoreCase(SipSession.State.TERMINATED.toString())) {
				numberOfTerminated++;
			}
		}	
		assertEquals(2, numberOfConfirmed);
		assertEquals(1, numberOfTerminated);
	}
	
	/**
	 * @param sendBye TODO
	 * @throws ParseException
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws InterruptedException
	 * @throws TransactionUnavailableException
	 * @throws TransactionDoesNotExistException
	 */
	private void createAndExecuteCall(String messageContent, boolean sendBye) throws ParseException, SipException,
			InvalidArgumentException, InterruptedException,
			TransactionUnavailableException, TransactionDoesNotExistException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, messageContent, null, false);		
		Thread.sleep(TIMEOUT);				
		if(sendBye) {
			assertTrue(sender.isAckSent());
			Thread.sleep(TIMEOUT);
			sender.sendBye();
			Thread.sleep(TIMEOUT);
			assertTrue(sender.getOkToByeReceived());
		} else {
			assertTrue(sender.isFinalResponseReceived());
		}
	}
	
	// Test for SS spec 11.1.6 transaction timeout notification
	// Also Tests Issue 1618 http://code.google.com/p/mobicents/issues/detail?id=1618
	public void testTransactionTimeoutResponse() throws Exception {		
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setSendAck(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, TEST_TIMEOUT, null, false);
		senderProtocolObjects.destroy();
		
		receiverProtocolObjects =new ProtocolObjects(
				"receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		Thread.sleep(TIMEOUT_EXPIRATION);	
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
			
		}
		assertFalse(receiver.getAllMessagesContent().contains(STX_408_RECEIVED));
		receiverProtocolObjects.destroy();
	}	

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
