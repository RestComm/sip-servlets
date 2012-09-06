/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.servlet.sip.testsuite.callcontroller;

import gov.nist.javax.sip.message.MessageExt;

import java.util.Iterator;
import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Request;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class CallForwardingB2BUAJunitTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUAJunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;		
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public CallForwardingB2BUAJunitTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName("checkOriginalRequestMapSize");
		applicationParameter.setValue("true");
		context.addApplicationParameter(applicationParameter);	
		assertTrue(tomcat.deployContext(context));
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-b2bua-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();

		senderProtocolObjects = new ProtocolObjects("forward-sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null, "32", "true");
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
			
	}
	
	public void testCallForwardingCallerSendBye() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckReceived());
		Thread.sleep(TIMEOUT*3);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender.getAllMessagesContent().size());
		assertTrue(sender.getAllMessagesContent().contains("sipApplicationSessionReadyToBeInvalidated"));
	}
	
	public void testCallForwardingToItself() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-myself";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		Thread.sleep(TIMEOUT*2);
		assertTrue(receiver.isAckReceived());
		Thread.sleep(TIMEOUT*4);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(2, sender.getAllMessagesContent().size());
		assertTrue(sender.getAllMessagesContent().contains("sipApplicationSessionReadyToBeInvalidated"));
	}
	
	/*
	 * non regression test for Issue 2419 
	 */
	public void testCallForwardingCallerSendByeLinkedRequest() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "useLinkedRequest";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckReceived());
		Thread.sleep(TIMEOUT*3);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender.getAllMessagesContent().size());
		assertTrue(sender.getAllMessagesContent().contains("sipApplicationSessionReadyToBeInvalidated"));
	}
	
	public void testCallForwardingCallerSendByeUseSameCallID() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender-factory-same-callID";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckReceived());
		Thread.sleep(TIMEOUT*3);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender.getAllMessagesContent().size());
		assertTrue(sender.getAllMessagesContent().contains("sipApplicationSessionReadyToBeInvalidated"));
		assertEquals(((MessageExt)sender.getInviteRequest()).getCallIdHeader().getCallId(), ((MessageExt)receiver.getInviteRequest()).getCallIdHeader().getCallId());
	}
	
	// Non regression test for http://code.google.com/p/sipservlets/issues/detail?id=126
	public void testCallForwardingCallerSendByeUseSameCallIDOriginalSessionTerminated() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender-factory-same-callID-kill-original-session";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		//receiver.setTimeToWaitBeforeBye(TIMEOUT*2);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isAckReceived());
		Thread.sleep(TIMEOUT*2);
		assertTrue(receiver.getOkToByeReceived());
		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(1, sender.getAllMessagesContent().size());
		assertTrue(sender.getAllMessagesContent().contains("sipApplicationSessionReadyToBeInvalidated"));
		assertEquals(((MessageExt)sender.getInviteRequest()).getCallIdHeader().getCallId(), ((MessageExt)receiver.getInviteRequest()).getCallIdHeader().getCallId());
	}

	public void testCallForwardingCalleeSendBye() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setTimeToWaitBeforeBye(TIMEOUT*2);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT*4);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());		
	}
	
	public void testCallForwardingCheckContact() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "checkContact";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"Contact"}, new String[] {"<sip:" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5056>;+sip.instance=\"<urn:uuid:some-xxxx>\""}, true);		
		Thread.sleep(TIMEOUT*3);
		assertEquals(200, sender.getFinalResponseStatus());		
	}

	public void testCancelCallForwarding() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
	}
	
	public void testCallForwardingCallerSendByePendingMessages() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-pending-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.setTimeToWaitBeforeAck(6000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT*4);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	public void testCallForwardingCallerUseSipFactory() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "factory-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		sender.setTimeToWaitBeforeAck(6000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT*4);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	public void testCallForwardingUpdateCallerSendBye() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "testSipAppSessionReadyToBeInvalidated";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT * 3);
		sender.sendInDialogSipRequest(Request.UPDATE, null, null, null, null, null);		
		sender.sendInDialogSipRequest(Request.UPDATE, null, null, null, null, null);
		Thread.sleep(TIMEOUT * 4);
		sender.sendInDialogSipRequest(Request.BYE, null, null, null, null, null);
		Thread.sleep(TIMEOUT * 6);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());		
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertEquals(0, sender.getAllMessagesContent().size());		
	}
	
	// non regression test for Issue 1550 http://code.google.com/p/mobicents/issues/detail?id=1550
	// IllegalStateException: Cannot create a response - not a server transaction gov.nist.javax.sip.stack.SIPClientTransaction
	public void testCallForwardingLinkedRequestCallerSendBye() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "useLinkedRequest-pending";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT * 3);
		assertEquals(200, sender.getFinalResponseStatus());
		sender.setFinalResponse(null);
		sender.setFinalResponseStatus(-1);
		sender.sendInDialogSipRequest(Request.UPDATE, null, null, null, null, null);
		receiver.sendInDialogSipRequest(Request.UPDATE, null, null, null, null, null);
		Thread.sleep(TIMEOUT * 4);
		assertEquals(200, receiver.getFinalResponseStatus());
		assertEquals(200, sender.getFinalResponseStatus());
		sender.sendInDialogSipRequest(Request.BYE, null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());					
	}
	
	// non regression test for issue 1716 http://code.google.com/p/mobicents/issues/detail?id=1716
	// The same response is routed to SipServlet twice
	public void testCallForwardingCallerCheckRetransmissionIsNotAForkedResponse() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "factory-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		sender.setTimeToWaitBeforeAck(4000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {"Require"}, new String[] {"Nothing"}, false);		
		Thread.sleep(TIMEOUT*4);
		assertTrue(sender.isAckSent());
		assertTrue(receiver.isAckReceived());
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	public void testCallForwardingCaller2ConnectorsPortIssue() throws Exception {
		
		senderProtocolObjects = new ProtocolObjects("forward-sender",
				"gov.nist", ListeningPoint.TCP, AUTODIALOG, null, "32", "true");
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", ListeningPoint.TCP, AUTODIALOG, null, null, null);
		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5071, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		tomcat.removeConnector(sipConnector);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, ListeningPoint.TCP);
		tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
		
		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "2-connectors-port-issue-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "2-connectors-port-issue-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
				
		sender.sendSipRequest("REGISTER", fromAddress, fromAddress, null, null, false);
		Thread.sleep(TIMEOUT);
		assertEquals(200, sender.getFinalResponseStatus());
		
		receiver.sendSipRequest("REGISTER", toAddress, toAddress, null, null, false);
		sender.setTimeToWaitBeforeBye(TIMEOUT*2);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckReceived());
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
	}
	
	@Override
	protected Properties getSipStackProperties() {
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
		"true");
		sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
				"32");
		sipStackProperties.setProperty(SipStandardService.DEBUG_LOG_STACK_PROP, 
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-debug.txt");
		sipStackProperties.setProperty(SipStandardService.SERVER_LOG_STACK_PROP,
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-messages.xml");
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
		sipStackProperties.setProperty(SipStandardService.AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");		
		sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "1");
		sipStackProperties.setProperty(SipStandardService.LOOSE_DIALOG_VALIDATION, "true");
		sipStackProperties.setProperty(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
		return sipStackProperties;
	}
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
