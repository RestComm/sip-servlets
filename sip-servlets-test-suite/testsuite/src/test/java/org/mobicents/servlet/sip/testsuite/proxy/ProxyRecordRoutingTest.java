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
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.Header;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ToHeader;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ProxyRecordRoutingTest extends SipServletTestCase {

	private static transient Logger logger = Logger.getLogger(ProxyRecordRoutingTest.class);

	private static final String SESSION_EXPIRED = "sessionExpired";
	private static final String SESSION_READY_TO_INVALIDATE = "sessionReadyToInvalidate";
	private static final String SIP_SESSION_READY_TO_INVALIDATE = "sipSessionReadyToInvalidate";
	private static final boolean AUTODIALOG = true;
	TestSipListener sender;
	TestSipListener neutral;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	ProtocolObjects neutralProto;


	private static final int TIMEOUT = 20000;

	public ProxyRecordRoutingTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		neutralProto = new ProtocolObjects("neutral",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5057, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
		neutral = new TestSipListener(5058, 5070, neutralProto, false);
		neutral.setRecordRoutingProxyTesting(true);
		SipProvider neutralProvider = neutral.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		neutralProvider.addSipListener(neutral);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
		neutralProto.start();
	}

	/*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=164
     */
    public void testCancelProxying() throws Exception {
        deployApplication();
        String fromName = "cancel-unique-location";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);      
        
        String toSipAddress = "sip-servlets.com";
        String toUser = "proxy-receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);
                                
        receiver.setWaitForCancel(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        sender.sendCancel();
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        assertEquals(487,sender.getFinalResponseStatus());        
    }
    
    /*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=164
     */
    public void testCancelProxyingNon1XX() throws Exception {
        deployApplication();
        String fromName = "cancel-unique-location";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);      
        
        String toSipAddress = "sip-servlets.com";
        String toUser = "proxy-receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);
        List<Integer> provResponses = new ArrayList<Integer>();
        provResponses.add(100);
        receiver.setProvisionalResponsesToSend(provResponses);            
        receiver.setWaitForCancel(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        sender.sendCancel();
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        assertEquals(487,sender.getFinalResponseStatus());        
    }
	
	/*
	 * https://code.google.com/p/sipservlets/issues/detail?id=2
	 */
	public void testRedirectProxying() throws Exception {
		deployApplication();
		String fromName = "redirect-unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
						
		receiver.setFinalResponseToSend(302);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertEquals(200,sender.getFinalResponseStatus());		
		assertTrue(sender.isAckSent());
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(neutral.getByeReceived());
		assertFalse(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	/*
	 * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=2
	 */
	public void testCancelRedirectProxying() throws Exception {
		deployApplication();
		String fromName = "cancel-unique-location";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
						
		receiver.setFinalResponseToSend(302);
		neutral.setWaitForCancel(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertEquals(487,sender.getFinalResponseStatus());
		assertTrue(neutral.isCancelReceived());
	}
	
	/*
	 * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=266
	 */
	public void testCancel480Proxying() throws Exception {
		deployApplication();
		String fromName = "cancel-480-sequential-2-locations";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
						
		receiver.setFinalResponseToSend(480);
		neutral.setWaitForCancel(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		// We receive 480 and not 487 because of JSR 289 Section 10.2.6 
		// When receiving a CANCEL for a transaction for which a Proxy object exists the server responds
		// to the CANCEL with a 200 and if the original request has not been proxied yet the container responds to it with a 487 final
		// response. otherwise, all branches are cancelled, and response processing continues as usual
		assertEquals(480,sender.getFinalResponseStatus());
		assertTrue(neutral.isCancelReceived());
	}
	
	/*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=154
     */
    public void testCancel480ChangeToUserProxying() throws Exception {
        deployApplication();
        String fromName = "cancel-480-sequential-2-locations-change-to-user";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);      
        
        String toSipAddress = "sip-servlets.com";
        String toUser = "proxy-receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);
                        
        receiver.setFinalResponseToSend(480);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        assertEquals("newuser", ((SipURI)((ToHeader)neutral.getInviteRequest().getHeader(ToHeader.NAME)).getAddress().getURI()).getUser());
        sender.sendBye();
        Thread.sleep(TIMEOUT);
        // We receive 480 and not 487 because of JSR 289 Section 10.2.6 
        // When receiving a CANCEL for a transaction for which a Proxy object exists the server responds
        // to the CANCEL with a 200 and if the original request has not been proxied yet the container responds to it with a 487 final
        // response. otherwise, all branches are cancelled, and response processing continues as usual
        assertEquals(200,sender.getFinalResponseStatus());
        assertTrue(neutral.getByeReceived());
        assertTrue(sender.getOkToByeReceived());
    }
	
	/*
     * https://code.google.com/p/sipservlets/issues/detail?id=22
     */
    public void testUnderscoreToTagFinalResponseProxying() throws Exception {
        deployApplication();
        String fromName = "underscore-to-tag-unique-location";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);      
        
        String toSipAddress = "sip-servlets.com";
        String toUser = "proxy-receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        receiver.setFinalResponseToSend(404);
        receiver.setToTag(Integer.toString(new Random().nextInt(10000000)) + "_Random_Random");                                
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertEquals(404,sender.getFinalResponseStatus());                     
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
