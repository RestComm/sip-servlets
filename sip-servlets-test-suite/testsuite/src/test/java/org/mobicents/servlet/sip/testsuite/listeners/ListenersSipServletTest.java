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
package org.mobicents.servlet.sip.testsuite.listeners;

import java.text.ParseException;
import java.util.Iterator;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ListenersSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ListenersSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 1000;
	private static final int TIMEOUT_TIMER = 6000;	
//	private static final int TIMEOUT = 100000000;
	
	private static final String OK = "OK";
	// the order is important here 
	private static final String[] LISTENERS_TO_TEST = new String[]{
		"sipServletInitialized",
		"sipAppSessionCreated",
		//Tested by the tCK
//		"sipAppSessionValueBound", "sipAppSessionValueUnbound", 
		"sipAppSessionAttributeReplaced","sipAppSessionAttributeRemoved", "sipAppSessionAttributeAdded", 
		"sipSessionCreated",
		//Tested by the tCK
//		"sipSessionValueBound", "sipSessionValueUnbound",  
		"sipSessionAttributeReplaced", "sipSessionAttributeRemoved", "sipSessionAttributeAdded"
		};		
		
	private static final String[] LISTENERS_TO_TEST_AFTER = new String[]{
		//tested in TimersSipServletTest 
//		"sipAppSessionExpired",
		//Cannot get this one below because the sipfactory has already been removed when we get the event
		//so we cannot send the message
		"sipAppSessionDestroyed", 
		"sipSessionDestroyed"
	};
	
	private static final String[] LISTENERS_NOT_TESTED = new String[]{
		//difficult to test, leave it for now 
		"noAckReceived", 
		//require the container to support Prack, it currently doesn't
		"noPrackReceived",  
		//require the container to session persistence or distribution, it currently doesn't
		"sipAppSessionPassivated", "sipAppSessionActivated", "sipSessionPassivated", "sipSessionActivated"};
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	
	public ListenersSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/listeners-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/listeners/listeners-sip-servlet-dar.properties";
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
			fail("unexpected exception ");
		}
	}
	
	public void testListeners() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
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
		for (int i = 0; i < LISTENERS_TO_TEST.length; i++) {
			logger.info("Testing following listener " + LISTENERS_TO_TEST[i]);
			sender.sendMessageInDialog(LISTENERS_TO_TEST[i]);
			Thread.sleep(TIMEOUT);
			String content = sender.getLastMessageContent();
			assertNotNull(content);
			if(!OK.equals(content)) {
				fail("following listener " + LISTENERS_TO_TEST[i] + " was not fired");
			}
		}		
		Thread.sleep(TIMEOUT);
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		Thread.sleep(TIMEOUT_TIMER);
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		logger.info("all messages received : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		for (int i = 0; i < LISTENERS_TO_TEST_AFTER.length; i++) {
			assertTrue(sender.getAllMessagesContent().contains(LISTENERS_TO_TEST_AFTER[i]));
		}
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
