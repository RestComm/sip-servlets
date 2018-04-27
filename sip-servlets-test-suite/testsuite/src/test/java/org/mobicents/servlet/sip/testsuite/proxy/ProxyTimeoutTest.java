/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ResponseType;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyTimeoutTest extends SipServletTestCase {	
	private static final String SESSION_EXPIRED = "sessionExpired";
	private static final String SESSION_READY_TO_INVALIDATE = "sessionReadyToInvalidate";
	private static final String SIP_SESSION_READY_TO_INVALIDATE = "sipSessionReadyToInvalidate";
	private static transient Logger logger = Logger.getLogger(ProxyTimeoutTest.class);
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener neutral;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	ProtocolObjects neutralProto;


	private static final int TIMEOUT = 20000;

	public ProxyTimeoutTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
	}
        
        public void setupPhones(Map<String,String> params) throws Exception{
                containerPort = NetworkPortAssigner.retrieveNextPort();             
		super.setUp();
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		neutralProto = new ProtocolObjects("neutral",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
                int senderPort = NetworkPortAssigner.retrieveNextPort();
		sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

                int receiverPort = NetworkPortAssigner.retrieveNextPort();
		receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
                int neutralPort = NetworkPortAssigner.retrieveNextPort();
		neutral = new TestSipListener(neutralPort, containerPort, neutralProto, false);
		neutral.setRecordRoutingProxyTesting(true);
		SipProvider neutralProvider = neutral.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		neutralProvider.addSipListener(neutral);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
		neutralProto.start();

                params.put( "servletContainerPort", String.valueOf(containerPort)); 
                params.put( "testPort", String.valueOf(senderPort)); 
                params.put( "receiverPort", String.valueOf(receiverPort));                
                deployApplication(projectHome + 
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", 
                        params, null);
        }

	/**
	 * It will proxy to 2 locations, one will send a trying that will stop the 1xx timer but the final response timer should fire
	 * and the other is not alive so will no send any response so the 1xx timer should fire
	 * @throws Exception
	 */
	public void testProxy1xxResponseTimeout() throws Exception {
		setupPhones(new HashMap());
		String fromName = "sequential-1xxResponseTimeout";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
		receiver.setWaitForCancel(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT*3);
		assertEquals(0,receiver.getFinalResponseStatus());
		assertTrue(!sender.isAckSent());
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue(sender.getAllMessagesContent().size() >= 4);
		assertTrue(sender.getAllMessagesContent().contains(ResponseType.INFORMATIONAL.toString()));
		assertTrue(sender.getAllMessagesContent().contains(ResponseType.FINAL.toString()));
		assertTrue(sender.getAllMessagesContent().contains(SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getAllMessagesContent().contains(SIP_SESSION_READY_TO_INVALIDATE));
	}
	
	/**
	 * It will proxy to 1 locations that will send a final response that will stop the 1xx and final response timer 
	 * @throws Exception
	 */
	public void testProxyNoTimeout() throws Exception {
		setupPhones(new HashMap());
		String fromName = "sequential-reverse-NoResponseTimeout";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);		
		assertTrue(sender.isAckSent());
		assertEquals(0, sender.getAllMessagesContent().size());		
	}
	
	/**
	 * Test Issue 1678 : SipApplicationSession.setExpires() doesn't work sometimes
	 * Test Issue 1676 : SipApplicationSession.getExpirationTime() is incorrect
	 */
	public void testProxySipApplicationSessionTimeout() throws Exception {
                Map<String,String> map = new HashMap();
                map.put("sipApplicationSessionTimeout", "1");
                setupPhones(map);
		String fromName = "sipApplicationSessionTimeout";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());		
		sender.setSendBye(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(70000);		
		assertTrue(sender.isAckSent());
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue(sender.getAllMessagesContent().size() >= 3);
		assertTrue(sender.getAllMessagesContent().contains(SESSION_EXPIRED));
		assertTrue(sender.getAllMessagesContent().contains(SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getAllMessagesContent().contains(SIP_SESSION_READY_TO_INVALIDATE));
			
	}	
	
	public void testNonExistLegTimeout() throws Exception {
		setupPhones(new HashMap());;
		String fromName = "sequential-nonexist";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(50000);		
		assertTrue(sender.getMessageRequest() != null);		
	}
	
	/*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=263
	 */
	public void testNonExistLeg1xxTimeout() throws Exception {
                setupPhones(new HashMap());
		String fromName = "sequential-nonexist-1xx";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(50000);		
		assertTrue(sender.getMessageRequest() != null);				
		assertTrue(sender.getAllMessagesContent().contains("doBranchTimeoutReceived"));	
		assertTrue(sender.getAllMessagesContent().contains(SESSION_READY_TO_INVALIDATE));
		assertTrue(sender.getAllMessagesContent().contains(SIP_SESSION_READY_TO_INVALIDATE));
	}

	@Override
	public void tearDown() throws Exception {
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();		
		neutralProto.destroy();
		logger.info("Test completed");
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat
				.deployContext(
						projectHome
								+ "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
						"sip-test-context", "sip-test"));
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}	
}
