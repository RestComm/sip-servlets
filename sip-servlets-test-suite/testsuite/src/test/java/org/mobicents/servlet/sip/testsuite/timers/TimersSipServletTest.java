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

package org.mobicents.servlet.sip.testsuite.timers;

import java.text.ParseException;
import java.util.Iterator;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.catalina.LifecycleException;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Checks that applicaiton session expiration is working
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class TimersSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(TimersSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	//1 sec
	private static final int TIMEOUT = 5000;
	//1 minute and 10 sec
	private static final int APP_SESSION_TIMEOUT = 80000;	
//	private static final int TIMEOUT = 100000000;
	
	// the order is important here 
	private static final String[] TIMERS_TO_TEST = new String[]{
		"recurringTimerExpired",
		"timerExpired",
		"sipAppSessionExpired",
		"sipAppSessionReadyToBeInvalidated"
	};				
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	SipStandardContext context = null; 
		
	public TimersSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/timers-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		//setting the sip application timeout to 1 minutes instead of the regular 3 minutes
		context.setSipApplicationSessionTimeout(1);
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());		
		assertTrue(tomcat.deployContext(context));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/timers/timers-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() {
		try {
			super.setUp();						
			
			senderProtocolObjects =new ProtocolObjects(
					"sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
						
			sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
			SipProvider senderProvider = sender.createProvider();			
			
			senderProvider.addSipListener(sender);
			
			senderProtocolObjects.start();			
		} catch (Exception ex) {
			fail("unexpected exception ");
		}
	}
	
//	public void testTimers() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
//		String fromName = "sender";
//		String fromSipAddress = "sip-servlets.com";
//		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
//				fromName, fromSipAddress);
//				
//		String toUser = "receiver";
//		String toSipAddress = "sip-servlets.com";
//		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
//				toUser, toSipAddress);
//		
//		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
//		Thread.sleep(TIMEOUT);
//		assertTrue(sender.isAckSent());			
//		Thread.sleep(APP_SESSION_TIMEOUT);
//		sender.sendBye();
//		Thread.sleep(TIMEOUT);
//		assertTrue(sender.getOkToByeReceived());
//		Thread.sleep(APP_SESSION_TIMEOUT);
//		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
//		while (allMessagesIterator.hasNext()) {
//			String message = (String) allMessagesIterator.next();
//			logger.info(message);
//		}
//		for (int i = 0; i < TIMERS_TO_TEST.length; i++) {
//			assertTrue(sender.getAllMessagesContent().contains(TIMERS_TO_TEST[i]));
//		}	
//	}
	
	// Test Issue 1698 : http://code.google.com/p/mobicents/issues/detail?id=1698
	// SipApplicationSession Expiration Timer is not reset and so does not fire if 
	// an indialog request is sent from within sessionExpired callback
	// 
	// Issue 1773 : http://code.google.com/p/mobicents/issues/detail?id=1773
	// This has been commented out because of JSR 289, Section 6.1.2 SipApplicationSession Lifetime :
	// "Servlets can register for application session timeout notifications using the SipApplicationSessionListener interface. 
	// In the sessionExpired() callback method, the application may request an extension of the application session lifetime 
	// by invoking setExpires() on the timed out SipApplicationSession giving as an argument the number of minutes until the session expires again"
	// Even sending a message out will not start the expiration timer anew indirectly otherwise it makes some TCK tests fail
	public void testTimerExpirationExtensionByInDialogRequest() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "expExtInDialog";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());			
		Thread.sleep(APP_SESSION_TIMEOUT);
		assertTrue(sender.isInviteReceived());	
		assertFalse(sender.getAllMessagesContent().contains("sipAppSessionExpired"));
		Thread.sleep(APP_SESSION_TIMEOUT);
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue(sender.getAllMessagesContent().isEmpty());	
	}
	
	// Issue 1478 : http://code.google.com/p/mobicents/issues/detail?id=1478
	// Attempting to use the TimerService after reloading a servlet throws a RejectedExecutionException
	public void testTimerReloading() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "checkReload";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());	
		assertTrue(sender.getAllMessagesContent().contains("timerExpired"));
		
//		tomcat.undeployContext(context);
//		tomcat.deployContext(context);
		try {
			context.stop();
			context.start();
		} catch (LifecycleException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sender.getAllMessagesContent().clear();
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());	
		assertTrue(sender.getAllMessagesContent().contains("timerExpired"));
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
