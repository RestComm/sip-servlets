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
package org.mobicents.servlet.sip.testsuite.session;

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
 * This test is aimed to test the JSR 289 Section 6.2.6 The SipSession Handler 
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SessionHandlerSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SessionHandlerSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 2000;
	private static final int TIMEOUT_BYE = 6000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;	
	
	public SessionHandlerSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/handler-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/session/handler-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender1Chat1", "gov.nist", TRANSPORT, AUTODIALOG, null);
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();			
		senderProvider.addSipListener(sender);
		senderProtocolObjects.start();					
	}

	/**
	 * This test check to see if the correct handler handled the correct SIP request or response.
	 * 
	 * 
	 * @throws InterruptedException
	 * @throws SipException
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 */
	public void testSessionHandler() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
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
		Thread.sleep(TIMEOUT_BYE);
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		logger.info("all messages received from sender : ");
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);			
		}
		assertEquals(4, sender.getAllMessagesContent().size());
		assertTrue(sender.getAllMessagesContent().contains("SecondaryHandler : Subsequent request received"));
		assertTrue(sender.getAllMessagesContent().contains("SecondaryHandler : response OK"));
		assertTrue(sender.getAllMessagesContent().contains("SecondaryHandler : testing MainHandler"));
		assertTrue(sender.getAllMessagesContent().contains("MainHandler : response OK"));
	}
	
	/**
	 * This test check that this is not possible to pas a bad handler name in the setHandler method
	 * @throws InterruptedException
	 * @throws SipException
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 */
	public void testBadSessionHandler() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "badHandler";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isFinalResponseReceived());
		assertTrue(!sender.isAckSent());
	}
	
	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
