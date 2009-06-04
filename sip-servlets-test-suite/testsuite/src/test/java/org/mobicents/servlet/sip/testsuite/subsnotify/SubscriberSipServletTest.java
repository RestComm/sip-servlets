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

import javax.sip.SipProvider;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SubscriberSipServletTest extends SipServletTestCase {
	private static transient Logger logger = Logger.getLogger(SubscriberSipServletTest.class);		
	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	private static final String[] SUBSCRIPTION_STATES = new String[]{
		SubscriptionState.PENDING.toLowerCase(), SubscriptionState.ACTIVE.toLowerCase(), SubscriptionState.TERMINATED.toLowerCase() 
	};
	
	private static final String SESSION_INVALIDATED = new String("sipSessionReadyToBeInvalidated");	
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;
	
	public SubscriberSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/subscriber-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/subsnotify/subscriber-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();			
	}
	
	/*
	 * Test the fact that a sip servlet send a SUBSCRIBE (here 2 subscribes for different event ie subscriptions)
	 * and receive NOTIFYs. 
	 * Check that everything works correctly included the Sip Session Termination upon receiving a NOTIFY
	 * containing Subscription State of Terminated.
	 */
	public void testSipServletSendsSubscribe() throws InterruptedException {
//		receiver.sendInvite();
		Thread.sleep(TIMEOUT*2);
		assertEquals(6, receiver.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present", receiver.getAllSubscriptionState().contains(subscriptionState));	
		}				
		Thread.sleep(TIMEOUT);
		assertEquals(1, receiver.getAllMessagesContent().size());
		assertTrue("session not invalidated after receiving Terminated Subscription State", receiver.getAllMessagesContent().contains(SESSION_INVALIDATED));
		
	}

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}
}