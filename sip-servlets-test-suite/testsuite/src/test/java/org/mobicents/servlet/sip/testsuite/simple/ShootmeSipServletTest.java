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
package org.mobicents.servlet.sip.testsuite.simple;

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

public class ShootmeSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ShootmeSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000;
	private static final int TIMEOUT_CSEQ_INCREASE = 100000;
	
	
	TestSipListener sender;
	TestSipListener registerReciever;
	
	ProtocolObjects senderProtocolObjects;	
	ProtocolObjects registerRecieverProtocolObjects;	
	
	public ShootmeSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();		
		
		
		registerRecieverProtocolObjects =new ProtocolObjects(
				"registerReciever", "gov.nist", TRANSPORT, AUTODIALOG, null);
		
		registerReciever = new TestSipListener(5058, 5070, registerRecieverProtocolObjects, true);
		SipProvider registerRecieverProvider = registerReciever.createProvider();			
		
		registerRecieverProvider.addSipListener(registerReciever);
		
		registerRecieverProtocolObjects.start();		
	}
	
	public void testShootme() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
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
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testShootmeParameterable() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"additionalParameterableHeader","nonParameterableHeader"}, new String[] {"none","none"});		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testShootmeRegister() throws Exception {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("REGISTER", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isFinalResponseReceived());
		assertEquals(200, sender.getFinalResponseStatus());
	}
	
	public void testShootmeRegisterCSeqIncrease() throws Exception {
		String fromName = "testRegisterCSeq";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT_CSEQ_INCREASE);
		assertTrue(registerReciever.getLastRegisterCSeqNumber() == 4);
		assertTrue(sender.isFinalResponseReceived());
	}
	
	public void testShootmeRegisterCSeqIncreaseAuth() throws Exception {
		String fromName = "testRegisterCSeq";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		registerReciever.setChallengeRequests(true);		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT_CSEQ_INCREASE);
		assertTrue(registerReciever.getLastRegisterCSeqNumber() == 4);
		assertTrue(sender.isFinalResponseReceived());
	}	
	
	public void testShootmeCancel() throws Exception {
		String fromName = "cancel";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());			
		Thread.sleep(TIMEOUT);
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue(sender.getAllMessagesContent().contains("cancelReceived"));
	}
	
	public void testShootmeMultipleValueHeaders() throws Exception {
		String fromName = "TestAllowHeader";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isFinalResponseReceived());
		assertEquals(405, sender.getFinalResponseStatus());
	}
	
	// test for http://code.google.com/p/mobicents/issues/detail?id=676
	public void testShootmeToTag() throws Exception {
		String fromName = "TestToTag";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());	
	}
	
	// test for http://code.google.com/p/mobicents/issues/detail?id=695
	public void testSubscriberURI() throws Exception {
		String fromName = "testSubscriberUri";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());	
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		registerRecieverProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
