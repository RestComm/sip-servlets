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
package org.mobicents.servlet.sip.testsuite.subsnotify;

import gov.nist.javax.sip.header.SubscriptionState;

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

public class InDialogNotifierSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(InDialogNotifierSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	private static final String[] SUBSCRIPTION_STATES = new String[]{
		SubscriptionState.PENDING.toLowerCase(), SubscriptionState.ACTIVE.toLowerCase(), SubscriptionState.TERMINATED.toLowerCase() 
	};
	
	private static final String SESSION_INVALIDATED = new String("sipSessionReadyToBeInvalidated");	
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	
	public InDialogNotifierSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/notifier-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/subsnotify/indialog-notifier-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() {
		try {
			super.setUp();						
			
			senderProtocolObjects =new ProtocolObjects(
					"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
						
			sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
			SipProvider senderProvider = sender.createProvider();			
			
			senderProvider.addSipListener(sender);
			
			senderProtocolObjects.start();			
		} catch (Exception ex) {
			ex.printStackTrace();
			fail("unexpected exception ");
		}
	}
	
	/*
	 * Test the fact that a sip servlet receive SUBSCRIBE and sends NOTIFYs in response in an existing dialog (INVITE call). 
	 * Check that everything works correctly included the Sip Session Termination.
	 * Session Termination should occur only when the last NOTIFY comes in is since the NOTIFY
	 * containing Subscription State of Terminated is sent after the BYE
	 */
	public void testInDialogSubscriptionByeSentBeforeTerminatingNotify() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
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
		sender.sendInDialogSipRequest(Request.SUBSCRIBE, null, null, null, null);		
		Thread.sleep(TIMEOUT*2);
		assertTrue(sender.getOkToByeReceived());
		for (String subscriptionState : sender.getAllSubscriptionState()) {
			logger.info("Subscription state :" + subscriptionState);	
		}
		assertEquals(3, sender.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present",sender.getAllSubscriptionState().contains(subscriptionState));	
		}				
		Thread.sleep(TIMEOUT);
		assertEquals(1, sender.getAllMessagesContent().size());
		assertTrue("session not invalidated after receiving Terminated Subscription State", sender.getAllMessagesContent().contains(SESSION_INVALIDATED));		
		
	}
	
	/*
	 * Test the fact that a sip servlet receive SUBSCRIBE and sends NOTIFYs in response in an existing dialog (INVITE call). 
	 * Check that everything works correctly included the Sip Session Termination.
	 * Session Termination should occur only when the BYE is sent since the NOTIFY
	 * containing Subscription State of Terminated is sent before the BYE
	 */
	public void testInDialogSubscriptionByeSentAfterTerminatingNotify() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
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
		
		sender.setSendByeAfterTerminatingNotify(true);
		sender.sendInDialogSipRequest(Request.SUBSCRIBE, null, null, null, null);		
		Thread.sleep(TIMEOUT*2);
		assertTrue(sender.getOkToByeReceived());
		for (String subscriptionState : sender.getAllSubscriptionState()) {
			logger.info("Subscription state :" + subscriptionState);	
		}
		assertEquals(3, sender.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present",sender.getAllSubscriptionState().contains(subscriptionState));	
		}				
		Thread.sleep(TIMEOUT);
		assertEquals(1, sender.getAllMessagesContent().size());
		assertTrue("session not invalidated after receiving Terminated Subscription State", sender.getAllMessagesContent().contains(SESSION_INVALIDATED));		
		
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
