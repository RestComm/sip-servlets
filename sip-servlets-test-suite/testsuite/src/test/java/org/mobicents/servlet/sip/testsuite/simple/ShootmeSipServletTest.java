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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.AllowHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ServerHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

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
	private static final int DIALOG_TIMEOUT = 40000;
	
	public final static String[] ALLOW_HEADERS = new String[] {"INVITE","ACK","CANCEL","OPTIONS","BYE","SUBSCRIBE","NOTIFY","REFER"};
	
	TestSipListener sender;
	SipProvider senderProvider = null;
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
		senderProvider = sender.createProvider();			
		
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
		// test non regression for Issue 1687 : Contact Header is present in SIP Message where it shouldn't
		Response response = sender.getFinalResponse();
		assertNull(response.getHeader(ContactHeader.NAME));
	}
	
	public void testShootmeSendBye() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "SSsendBye";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setSendBye(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT* 2);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getByeReceived());
		Thread.sleep(TIMEOUT* 4);
	}
	
	// Issue 1042 : Trying to simulate the 2 Invites arriving at the same time
	public void testShootmeRetransTest() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		Request request = sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		senderProvider.sendRequest(request);
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
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"additionalParameterableHeader","nonParameterableHeader"}, new String[] {"none","none"}, true);		
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
	
	/**
	 * non regression test for Issue 1104 http://code.google.com/p/mobicents/issues/detail?id=1104
	 * Cannot find the corresponding sip session to this subsequent request
	 * 
	 * In conflict with Issue 1401 http://code.google.com/p/mobicents/issues/detail?id=1401
	 * ACK request sent by sip client after receiving 488/reINVITE is passed to sip application
	 * Forbidden by SIP Spec "11.2.2 Receiving ACK" :
	 * "Applications are not notified of incoming ACKs for non-2xx final responses to INVITE."
	 * 
	 */
	public void testShootmeErrorResponse() throws Exception {
		String fromName = "testErrorResponse";
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
		assertEquals(486, sender.getFinalResponseStatus());
		assertTrue(!sender.getAllMessagesContent().contains("ackReceived"));
		Thread.sleep(DIALOG_TIMEOUT);
		List<String> allMessagesContent = sender.getAllMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
	}
	
	public void testShootmeRegisterNoContact() throws Exception {
		String fromName = "testRegisterNoContact";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertNull(registerReciever.getRegisterReceived().getHeader(ContactHeader.NAME));
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
	
	public void testShootmeRegisterAuthSavedSession() throws Exception {
		String fromName = "testRegisterSavedSession";
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
		//Issue 1164 non regression test
		ListIterator<AllowHeader> allowHeaders = (ListIterator<AllowHeader>) sender.getFinalResponse().getHeaders(AllowHeader.NAME);
		assertNotNull(allowHeaders);
		List<String> allowHeadersList = new ArrayList<String>();
		while (allowHeaders.hasNext()) {
			allowHeadersList.add(allowHeaders.next().getMethod());
		}
		assertTrue(Arrays.equals(ALLOW_HEADERS, (String[])allowHeadersList.toArray(new String[allowHeadersList.size()])));
	}
	
	// test for http://code.google.com/p/mobicents/issues/detail?id=676
	public void testShootmeToTag() throws Exception {
		String fromName = "TestToTag";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertEquals( 200, sender.getFinalResponseStatus());
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
		assertEquals( 200, sender.getFinalResponseStatus());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());	
	}
	
	// test for http://code.google.com/p/mobicents/issues/detail?id=1010
	public void testFlagParameter() throws Exception {
		String fromName = "testFlagParameter";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertEquals( 200, sender.getFinalResponseStatus());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());	
	}
	
	// test for http://code.google.com/p/mobicents/issues/detail?id=1021
	public void testSessionRetrieval() throws Exception {
		String fromName = "testSessionRetrieval";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		fromAddress.setParameter("fromParam", "whatever");
		
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertEquals( 200, sender.getFinalResponseStatus());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());	
	}
	
	// test for http://code.google.com/p/mobicents/issues/detail?id=1061
	// Also test http://code.google.com/p/mobicents/issues/detail?id=1681
	public void testNoAckReceived() throws Exception {
		String fromName = "noAckReceived";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
				
		sender.setSendAck(false);
		sender.setCountRetrans(true);
		sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertEquals( 200, sender.getFinalResponseStatus());
		assertFalse(sender.isAckSent());
		Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
		// test http://code.google.com/p/mobicents/issues/detail?id=1681
		// Make sure we get the 10 retrans for 200 to INVITE when no ACK is sent
		// corresponding to Timer G
		assertEquals( 10, sender.getNbRetrans());
		List<String> allMessagesContent = sender.getAllMessagesContent();
		assertEquals(1,allMessagesContent.size());
		assertEquals("noAckReceived", allMessagesContent.get(0));
	}

	public void testShootmeServerHeader() throws Exception {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		tomcat.removeConnector(sipConnector);
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-"
				+ sipIpAddress + "-" + 5070);
		sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
				"off");
		sipStackProperties.setProperty(
				"gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE",
				"64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",
				"true");
		sipStackProperties.setProperty("org.mobicents.servlet.sip.SERVER_HEADER",
			"MobicentsSipServletsServer");
		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport, sipStackProperties);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
			
		Response finalResponse = sender.getFinalResponse();
		ServerHeader serverHeader = (ServerHeader) finalResponse.getHeader(ServerHeader.NAME);
		assertNotNull(serverHeader);
		assertTrue(serverHeader.toString().contains("MobicentsSipServletsServer"));
	}
	
	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		registerRecieverProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
		Thread.sleep(4000);
//		FullThreadDump fullThreadDump = new FullThreadDump("127.0.0.1", 1090);
//        fullThreadDump.dump();
	}


}
