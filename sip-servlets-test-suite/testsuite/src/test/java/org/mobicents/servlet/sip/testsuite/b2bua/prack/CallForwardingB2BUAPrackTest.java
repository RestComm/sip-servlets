/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.testsuite.b2bua.prack;

import gov.nist.javax.sip.message.RequestExt;
import java.util.HashMap;
import java.util.Map;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class CallForwardingB2BUAPrackTest extends SipServletTestCase {

	private static transient Logger logger = Logger.getLogger(CallForwardingB2BUAPrackTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 20000;
//	private static final int TIMEOUT = 100000000;

	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public CallForwardingB2BUAPrackTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
				"sip-test-context",
				"sip-test"));
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
                containerPort = NetworkPortAssigner.retrieveNextPort();
		super.setUp();

		senderProtocolObjects = new ProtocolObjects("forward-sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

	}

	public void testCallForwardingCallerSendBye() throws Exception {
		tomcat.startTomcat();

                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort));
                params.put( "testPort", String.valueOf(receiverPort));
                params.put( "senderPort", String.valueOf(senderPort));
            SipStandardContext deployApplication = deployApplication(projectHome +
                    "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                    params
                    , null);


		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);

		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};

		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}

	// non regression test for https://github.com/Mobicents/sip-servlets/issues/66
	public void testCallForwardingCallerPrackUpdateSendBye() throws Exception {
		tomcat.startTomcat();

               int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort));
                params.put( "testPort", String.valueOf(receiverPort));
                params.put( "senderPort", String.valueOf(senderPort));
            SipStandardContext deployApplication = deployApplication(projectHome +
                    "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                    params
                    , null);

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);

		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};

//		sender.setSendUpdateOn180(true);
//		receiver.setTimeToWaitBeforeAck(5000);
		sender.setSendUpdateAfterPrack(true);
		sender.setTimeToWaitBeforeBye(1000);
		receiver.setSendUpdateAfterPrack(true);
		receiver.setWaitBeforeFinalResponse(3000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		assertEquals(4, ((RequestExt)receiver.getByeRequestReceived()).getCSeqHeader().getSeqNumber());
	}

	// non regression test for https://github.com/RestComm/sip-servlets/issues/362
	public void testCallForwardingCallerPrackUpdateFromBPartySendBye() throws Exception {
		tomcat.startTomcat();

               int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort));
                params.put( "testPort", String.valueOf(receiverPort));
                params.put( "senderPort", String.valueOf(senderPort));
            SipStandardContext deployApplication = deployApplication(projectHome +
                    "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                    params
                    , null);

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);

		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};

//		sender.setSendUpdateOn180(true);
//		receiver.setTimeToWaitBeforeAck(5000);
		sender.setSendUpdateAfterPrack(true);
		sender.setTimeToWaitBeforeBye(1000);
		receiver.setSendUpdateAfterPrack(true);
                //this will actually cuase the issue, by forcing receiver to send counter UPDATE
                receiver.setSendUpdateAfterUpdate(true);
		receiver.setWaitBeforeFinalResponse(3000);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);
		Thread.sleep(TIMEOUT);
                assertTrue(sender.isUpdateReceived());
                assertTrue(receiver.isUpdateReceived());
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		assertEquals(4, ((RequestExt)receiver.getByeRequestReceived()).getCSeqHeader().getSeqNumber());
	}

	public void testCallForwardingCalleeSendBye() throws Exception {
		tomcat.startTomcat();

               int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, true);
                receiver.setTimeToWaitBetweenProvisionnalResponse(1000);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort));
                params.put( "testPort", String.valueOf(receiverPort));
                params.put( "senderPort", String.valueOf(senderPort));
            SipStandardContext deployApplication = deployApplication(projectHome +
                    "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                    params
                    , null);

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);

		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};

		for (int i = 0; i < 3; i++) {
			sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);
		}
		Thread.sleep(TIMEOUT * 5);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());

		if(sender.getAllMessagesContent() != null) {
			assertFalse(sender.getAllMessagesContent().contains("KO"));
		}
	}

	public void testCallForwardingCallerSendByeAnyLocalAddress() throws Exception {
		tomcat.removeConnector(sipConnector);
		sipIpAddress = "0.0.0.0";
		tomcat.addSipConnector(serverName, sipIpAddress, containerPort, listeningPointTransport);
		tomcat.startTomcat();

               int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort));
                params.put( "testPort", String.valueOf(receiverPort));
                params.put( "senderPort", String.valueOf(senderPort));
            SipStandardContext deployApplication = deployApplication(projectHome +
                    "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                    params
                    , null);

		String fromName = "forward-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);

		String toSipAddress = "sip-servlets.com";
		String toUser = "receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);

		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};

		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.isPrackReceived());
		Request prackReceived = receiver.getPrackRequestReceived();
		assertNotNull(prackReceived);
		ViaHeader via= ((RequestExt)prackReceived).getTopmostViaHeader();
		assertNotNull(via);
		assertFalse(via.getHost().equals("0.0.0.0"));
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}

	@Override
	protected void tearDown() throws Exception {
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
