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
package org.mobicents.servlet.sip.testsuite.publish;

import gov.nist.javax.sip.header.SubscriptionState;

import java.text.ParseException;
import java.util.Iterator;

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
 * This tests the PUBLISH SIP extension see RFC 3903
 *  
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class PublishSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(PublishSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	private static final String[] SUBSCRIPTION_STATES = new String[]{
		SubscriptionState.ACTIVE.toLowerCase() 
	};
	private static final String[] PUBLISH_STATES = new String[]{
		"Initial", "Modify"
	};
	
	TestSipListener watcher;
	ProtocolObjects watcherProtocolObjects;	

	TestSipListener pua;
	ProtocolObjects puaProtocolObjects;	

	
	public PublishSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/publish-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/publish/publish-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		watcherProtocolObjects =new ProtocolObjects(
				"watcher", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		watcher = new TestSipListener(5080, 5070, watcherProtocolObjects, true);
		SipProvider senderProvider = watcher.createProvider();			
		
		senderProvider.addSipListener(watcher);
		
		watcherProtocolObjects.start();
		
		puaProtocolObjects =new ProtocolObjects(
				"pua", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		pua = new TestSipListener(5090, 5070, puaProtocolObjects, true);
		SipProvider puaProvider = pua.createProvider();			
		
		puaProvider.addSipListener(pua);
		
		puaProtocolObjects.start();		
	}
	
	/**
	 * Call flow tested : See RFC 3903 Page 23
	 * in this test, Sip Servlet is acting as PA(ESC) 
	 */
	public void testSipServletReceivesPublish() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "watcher";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = watcherProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = watcherProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		watcher.setPublishEvent("presence");
		watcher.sendSipRequest(Request.SUBSCRIBE, fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertEquals(1, watcher.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present",watcher.getAllSubscriptionState().contains(subscriptionState));	
		}
		watcher.getAllSubscriptionState().clear();
		pua.setPublishEvent("presence");
		pua.setPublishContentMessage("Initial");
		pua.sendSipRequest(Request.PUBLISH, fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertEquals(1, watcher.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present",watcher.getAllSubscriptionState().contains(subscriptionState));	
		}		
		watcher.getAllSubscriptionState().clear();
		pua.setPublishContentMessage(null);
		pua.sendSipRequest(Request.PUBLISH, fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertEquals(0, watcher.getAllSubscriptionState().size());	
		pua.setPublishContentMessage("Modify");
		pua.sendSipRequest(Request.PUBLISH, fromAddress, toAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertEquals(1, watcher.getAllSubscriptionState().size());
		for (String subscriptionState : SUBSCRIPTION_STATES) {
			assertTrue(subscriptionState + " not present",watcher.getAllSubscriptionState().contains(subscriptionState));	
		}
		watcher.getAllSubscriptionState().clear();
	}
	
	/**
	 * Call flow tested : See RFC 3903 Page 23
	 * in this test, Sip Servlet is acting as PUA(EPA) 
	 */
	public void testSipServletSendsPublish() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = watcher.getAllMessagesContent().iterator();
		logger.info("all messages received : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		for (int i = 0; i < PUBLISH_STATES.length; i++) {
			assertTrue(watcher.getAllMessagesContent().contains(PUBLISH_STATES[i]));
		}
	}

	@Override
	protected void tearDown() throws Exception {					
		watcherProtocolObjects.destroy();
		puaProtocolObjects.destroy();	
		logger.info("Test completed");
		super.tearDown();
	}


}
