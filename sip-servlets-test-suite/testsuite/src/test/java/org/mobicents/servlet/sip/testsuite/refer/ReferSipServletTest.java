/*
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
package org.mobicents.servlet.sip.testsuite.refer;

import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This tests the REFER SIP extension see RFC 3515
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ReferSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ReferSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	private static final String[] NOTIFICATIONS = new String[]{
		"SIP/2.0 100 Trying", "SIP/2.0 200 OK" 
	};
	
	private static final String[] INDIALOG_NOTIFICATIONS = new String[]{
		"SIP/2.0 100 Subsequent" 
	};
	
	private static final String SESSION_INVALIDATED = new String("sipSessionReadyToBeInvalidated");	
	
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;	

	TestSipListener referTo;
	ProtocolObjects referToProtocolObjects;	

	
	public ReferSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/refer-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/refer/refer-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();
		
		referToProtocolObjects =new ProtocolObjects(
				"referTo", "gov.nist", TRANSPORT, AUTODIALOG, null);
		referTo = new TestSipListener(5090, 5070, referToProtocolObjects, true);
		SipProvider referToProvider = referTo.createProvider();			
		referToProvider.addSipListener(referTo);
		referToProtocolObjects.start();
		
	}
	
	/**
	 * Call flow tested : See RFC 3515 Page 11-13
	 * in this test, Sip Servlet is receiving the REFERs (Acts as Agent B in the RFC) 
	 */
	public void testSipServletsReceivesReferOutOfDialog() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest(Request.REFER, fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT*2);		
		assertTrue(referTo.isAckReceived());
		assertEquals(3, sender.getAllMessagesContent().size());
		for (String subscriptionState : NOTIFICATIONS) {
			assertTrue(subscriptionState + " not present",sender.getAllMessagesContent().contains(subscriptionState));	
		}
		assertTrue(referTo.getOkToByeReceived());
		assertTrue("session not invalidated after receiving Terminated Subscription State", sender.getAllMessagesContent().contains(SESSION_INVALIDATED));
		
	}
	
	/**
	 * Call flow tested : See RFC 3515 Page 11-13
	 * in this test, Sip Servlet is sending the REFERs (Acts as Agent A in the RFC) 
	 */
	public void testSipServletsSendsReferOutOfDialog() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		
		Thread.sleep(TIMEOUT * 2);		
		assertEquals(NOTIFICATIONS.length + 1, sender.getAllMessagesContent().size());
		for (String subscriptionState : NOTIFICATIONS) {
			assertTrue(subscriptionState + " not present",sender.getAllMessagesContent().contains(subscriptionState));	
		}
		assertTrue("session not invalidated after receiving Terminated Subscription State", sender.getAllMessagesContent().contains(SESSION_INVALIDATED));
		
	}
	
	/**
	 * Call flow tested : See RFC 3515 Page 13-15
	 * in this test, Sip Servlet is receiving the REFERs (Acts as Agent B in the RFC) 
	 */
	public void testSipServletsReceivesReferInDialog() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest(Request.INVITE, fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		
		sender.sendInDialogSipRequest(Request.REFER, null, null, null, null);		
		Thread.sleep(TIMEOUT);		
		assertTrue(referTo.isAckReceived());
		assertEquals(NOTIFICATIONS.length, sender.getAllMessagesContent().size());
		for (String subscriptionState : NOTIFICATIONS) {
			assertTrue(subscriptionState + " not present",sender.getAllMessagesContent().contains(subscriptionState));	
		}
		assertTrue(referTo.getOkToByeReceived());
		sender.getAllMessagesContent().clear();
		sender.sendInDialogSipRequest(Request.REFER, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertEquals(INDIALOG_NOTIFICATIONS.length, sender.getAllMessagesContent().size());
		for (String subscriptionState : INDIALOG_NOTIFICATIONS) {
			assertTrue(subscriptionState + " not present",sender.getAllMessagesContent().contains(subscriptionState));	
		}
		sender.sendBye();
		Thread.sleep(TIMEOUT);	
		assertTrue(sender.getOkToByeReceived());
		assertTrue("session not invalidated after receiving Terminated Subscription State", sender.getAllMessagesContent().contains(SESSION_INVALIDATED));
	}
	
	/**
	 * Call flow tested : See RFC 3515 Page 13-15
	 * in this test, Sip Servlet is receiving the REFERs (Acts as Agent B in the RFC) 
	 */
	public void testSipServletsReceivesReferInDialogByeSentBeforeTermSubscription() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest(Request.INVITE, fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		
		sender.setSendByeBeforeTerminatingNotify(true);
		sender.sendInDialogSipRequest(Request.REFER, null, null, null, null);		
		Thread.sleep(TIMEOUT*2);		
		assertTrue(referTo.isAckReceived());
		assertEquals(3, sender.getAllMessagesContent().size());
		for (String subscriptionState : NOTIFICATIONS) {
			assertTrue(subscriptionState + " not present",sender.getAllMessagesContent().contains(subscriptionState));	
		}						
		assertTrue(sender.getOkToByeReceived());
		assertTrue("session not invalidated after receiving Terminated Subscription State", sender.getAllMessagesContent().contains(SESSION_INVALIDATED));
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		referToProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
