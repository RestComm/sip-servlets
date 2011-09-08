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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipSession;
import javax.sip.SipProvider;
import javax.sip.message.Response;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SessionStateUACSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SessionStateUACSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	
	private List<String> send_1xx_4xx_sessionStateList;
	private List<String> send_subsequent_487_sessionStateList;

	private static final int TIMEOUT = 35000;
	private static final int TIMEOUT_EXPIRATION = 70000;
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;	

	public SessionStateUACSipServletTest(String name) {		
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	protected void deployApplication() {
		deployApplication(null, null, ConcurrencyControlMode.None);
	}
	
	public void deployApplication(String name, String value, ConcurrencyControlMode concurrencyControlMode) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/session-state-uac/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		context.setConcurrencyControlMode(concurrencyControlMode);
		if(name != null) {
			ApplicationParameter applicationParameter = new ApplicationParameter();
			applicationParameter.setName(name);
			applicationParameter.setValue(value);
			context.addApplicationParameter(applicationParameter);
		}		
		assertTrue(tomcat.deployContext(context));			
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/session/session-state-uac-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		send_1xx_4xx_sessionStateList = new ArrayList<String>();
		send_1xx_4xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_1xx_4xx_sessionStateList.add(SipSession.State.EARLY.toString());
		send_1xx_4xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		
		send_subsequent_487_sessionStateList = new ArrayList<String>();
		send_subsequent_487_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_subsequent_487_sessionStateList.add(SipSession.State.EARLY.toString());		
		send_subsequent_487_sessionStateList.add(SipSession.State.CONFIRMED.toString());
		send_subsequent_487_sessionStateList.add(SipSession.State.CONFIRMED.toString());
		send_subsequent_487_sessionStateList.add(SipSession.State.TERMINATED.toString());				
		
		super.setUp();																							
	}
	
	public void testSessionStateUAC_1xx_4xx() throws Exception {	
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		List<Integer> provisionalResponsesToSend = receiver.getProvisionalResponsesToSend();
		provisionalResponsesToSend.clear();
		provisionalResponsesToSend.add(Response.RINGING);	
		receiver.setFinalResponseToSend(Response.FORBIDDEN);
		
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		deployApplication();
		
		Thread.sleep(TIMEOUT);
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		assertEquals(send_1xx_4xx_sessionStateList.size(), receiver.getAllMessagesContent().size());		
		for (int i = 0; i < send_1xx_4xx_sessionStateList.size(); i++) {
			assertTrue(receiver.getAllMessagesContent().contains(send_1xx_4xx_sessionStateList.get(i)));
		}	
	}
	
	/**
	 * http://code.google.com/p/mobicents/issues/detail?id=1438
	 * Sip Session become TERMINATED after receiving 487 response to subsequent request
	 */
	public void testSessionStateUAC_Subsequent_487() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"Subsequent_487", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		List<Integer> provisionalResponsesToSend = receiver.getProvisionalResponsesToSend();
		provisionalResponsesToSend.clear();
		provisionalResponsesToSend.add(Response.RINGING);	
		receiver.setFinalResponseToSend(Response.OK);
		receiver.setReferResponseToSend(487);
		receiver.setSendNotifyForRefer(false);
		
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		deployApplication();
		
		Thread.sleep(TIMEOUT);	
		receiver.sendBye();
		Thread.sleep(5000);
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		int numberOfTerminated = 0; 
		int numberOfConfirmed = 0;
		assertEquals(send_subsequent_487_sessionStateList.size(), receiver.getAllMessagesContent().size());		
		for (int i = 0; i < send_subsequent_487_sessionStateList.size(); i++) {
			String messageContent = receiver.getAllMessagesContent().get(i);
			assertTrue(receiver.getAllMessagesContent().contains(messageContent));
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
	
	// Test for SS spec 11.1.6 transaction timeout notification
	// Also Tests Issue 1470 http://code.google.com/p/mobicents/issues/detail?id=1470
	// Also Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693
	public void testTransactionTimeoutResponse() throws Exception {
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		List<Integer> provisionalResponsesToSend = receiver.getProvisionalResponsesToSend();
		provisionalResponsesToSend.clear();
		provisionalResponsesToSend.add(Response.RINGING);	
		receiver.setFinalResponseToSend(Response.FORBIDDEN);
		
		SipProvider receiverProvider = receiver.createProvider();			
		receiverProvider.addSipListener(receiver);
		receiverProtocolObjects.start();
		
		deployApplication("testTimeout", "true", ConcurrencyControlMode.SipSession);
		
		Thread.sleep(TIMEOUT);
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
			
		}
		assertTrue(receiver.getAllMessagesContent().contains("sipSessionReadyToInvalidate"));
		assertTrue(receiver.getAllMessagesContent().contains("sipAppSessionReadyToInvalidate"));
		assertTrue(receiver.txTimeoutReceived);
	}	

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
