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
package org.mobicents.servlet.sip.testsuite.targeting;

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

/**
 * This test is aimed to test the @SipApplicationKey annotation and check that this is compliant with JSR 289 
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SessionKeyTargetingSipServletTest extends SipServletTestCase {
		

	private static transient Logger logger = Logger.getLogger(SessionKeyTargetingSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;
	private static final int TIMEOUT_TIMER = 6000;
	private static final int CONTAINER_PORT = 5070;
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender1Chat1;
	TestSipListener sender2Chat1;
	TestSipListener sender3Chat1;
	TestSipListener sender1Chat2;
	
	
	ProtocolObjects sender1Chat1ProtocolObjects;	
	ProtocolObjects sender2Chat1ProtocolObjects;
	ProtocolObjects sender3Chat1ProtocolObjects;
	ProtocolObjects sender1Chat2ProtocolObjects;

	
	public SessionKeyTargetingSipServletTest(String name) {
		super(name);
//		startTomcatOnStartup = false;
//		autoDeployOnStartup = false;
//		initTomcatOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/chatroom-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/targeting/chatroom-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		sender1Chat1ProtocolObjects =new ProtocolObjects(
				"sender1Chat1", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender2Chat1ProtocolObjects =new ProtocolObjects(
				"sender2Chat1", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender3Chat1ProtocolObjects =new ProtocolObjects(
				"sender3Chat1", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender1Chat2ProtocolObjects =new ProtocolObjects(
				"sender1Chat2", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		sender1Chat1 = new TestSipListener(5071, CONTAINER_PORT, sender1Chat1ProtocolObjects, false);
		SipProvider senderProvider = sender1Chat1.createProvider();			
		senderProvider.addSipListener(sender1Chat1);
		sender1Chat1ProtocolObjects.start();			
		
		sender2Chat1 = new TestSipListener(5072, CONTAINER_PORT, sender2Chat1ProtocolObjects, false);
		SipProvider sender2Provider = sender2Chat1.createProvider();			
		sender2Provider.addSipListener(sender2Chat1);
		sender2Chat1ProtocolObjects.start();
		
		sender3Chat1 = new TestSipListener(5073, CONTAINER_PORT, sender3Chat1ProtocolObjects, false);
		SipProvider sender3Provider = sender3Chat1.createProvider();			
		sender3Provider.addSipListener(sender3Chat1);
		sender3Chat1ProtocolObjects.start();
		
		sender1Chat2 = new TestSipListener(5074, CONTAINER_PORT, sender1Chat2ProtocolObjects, false);
		SipProvider sender4Provider = sender1Chat2.createProvider();			
		sender4Provider.addSipListener(sender1Chat2);
		sender1Chat2ProtocolObjects.start();
	}

	/**
	 * This test check to see if all requests with the same request URI
	 * "sip:mychatroom1@example.com" will be handled by the same application
	 * session. (The sip servlet will return the same key for the same request
	 * URI)
	 * 
	 * So it starts 4 sip clients. 3 of them will connect to the same request
	 * Uri and the last one to another request uri. The sip servlet application
	 * will send back a MESSAGE containing the number of clients connected to
	 * the same application session upon ACK reception.
	 * 
	 * The 3 first clients should receive respectively 1, 2 and 3. And the 4th
	 * client should receive 1.
	 * 
	 * @throws InterruptedException
	 * @throws SipException
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 */
	public void testSipApplicationSessionTargeting() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender1Chat1";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = sender1Chat1ProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "chatroom1";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = sender1Chat1ProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender1Chat1.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender1Chat1.isAckSent());
		fromName = "sender2Chat1";
		fromAddress = sender2Chat1ProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		sender2Chat1.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender2Chat1.isAckSent());
		fromName = "sender3Chat1";
		fromAddress = sender3Chat1ProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		sender3Chat1.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender3Chat1.isAckSent());
				
		toUser = "chatroom2";
		toAddress = sender1Chat2ProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		fromName = "sender1Chat2";
		fromAddress = sender1Chat2ProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		sender1Chat2.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender1Chat2.isAckSent());
		
		Thread.sleep(TIMEOUT);
		sender1Chat1.sendBye();
		sender2Chat1.sendBye();
		sender3Chat1.sendBye();
		sender1Chat2.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender1Chat1.getOkToByeReceived());
		assertTrue(sender2Chat1.getOkToByeReceived());
		assertTrue(sender3Chat1.getOkToByeReceived());
		assertTrue(sender1Chat2.getOkToByeReceived());
		Thread.sleep(TIMEOUT_TIMER);
		Iterator<String> allMessagesIterator = sender1Chat1.getAllMessagesContent().iterator();
		logger.info("all messages received from sender1Chat1 : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);			
		}
		assertEquals(1, sender1Chat1.getAllMessagesContent().size());
		assertTrue(sender1Chat1.getAllMessagesContent().contains("1"));
		
		allMessagesIterator = sender2Chat1.getAllMessagesContent().iterator();
		logger.info("all messages received from sender2Chat1 : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender1Chat1.getAllMessagesContent().size());
		assertTrue(sender2Chat1.getAllMessagesContent().contains("2"));
		
		allMessagesIterator = sender3Chat1.getAllMessagesContent().iterator();
		logger.info("all messages received from sender3Chat1 : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender1Chat1.getAllMessagesContent().size());
		assertTrue(sender3Chat1.getAllMessagesContent().contains("3"));
		
		allMessagesIterator = sender1Chat2.getAllMessagesContent().iterator();
		logger.info("all messages received from sender1Chat2 : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		assertEquals(1, sender1Chat1.getAllMessagesContent().size());
		assertTrue(sender1Chat2.getAllMessagesContent().contains("1"));
	}
	
	/**
	 * This test check that this is not possible to modify the request in the @SipApplicationKey annotated method
	 * @throws InterruptedException
	 * @throws SipException
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 */
	public void testSipApplicationKeyStaticField() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "staticField";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = sender1Chat1ProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "chatroom";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = sender1Chat1ProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender1Chat1.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		sender1Chat1.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender1Chat1.getOkToByeReceived());
	}
	
	/**
	 * This test check that this is not possible to modify the request in the @SipApplicationKey annotated method
	 * @throws InterruptedException
	 * @throws SipException
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 */
	public void testSipApplicationKeyModifyingSipServletRequest() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "modifyRequest";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = sender1Chat1ProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "chatroom";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = sender1Chat1ProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender1Chat1.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender1Chat1.isFinalResponseReceived());
		assertTrue(!sender1Chat1.isAckSent());
	}

	@Override
	protected void tearDown() throws Exception {					
		sender1Chat1ProtocolObjects.destroy();
		if(sender2Chat1ProtocolObjects != null){
			sender2Chat1ProtocolObjects.destroy();
		}
		if(sender3Chat1ProtocolObjects != null){
			sender3Chat1ProtocolObjects.destroy();
		}
		if(sender1Chat2ProtocolObjects != null){
			sender1Chat2ProtocolObjects.destroy();
		}
		logger.info("Test completed");
		super.tearDown();
	}


}
