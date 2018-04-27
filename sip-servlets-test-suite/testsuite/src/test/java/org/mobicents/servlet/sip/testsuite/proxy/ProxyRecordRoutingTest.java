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
 *
 * This file incorporates work covered by the following copyright contributed under the GNU LGPL : Copyright 2007-2011 Red Hat.
 */

package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.Header;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ReasonHeader;

import org.apache.catalina.connector.Connector;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipProtocolHandler;
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
	TestSipListener secondReceiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	ProtocolObjects neutralProto;
	ProtocolObjects secondProto;


	private static final int TIMEOUT = 20000;

	public ProxyRecordRoutingTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
                containerPort = NetworkPortAssigner.retrieveNextPort(); 
		super.setUp();
        }
        public void setupPhones() throws Exception {
		senderProtocolObjects = new ProtocolObjects("proxy-sender",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("proxy-receiver",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		neutralProto = new ProtocolObjects("neutral",
				"gov.nist", ListeningPoint.UDP, AUTODIALOG, null, null, null);
		secondProto = new ProtocolObjects("proxy-second-receiver",
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
		
                int secondReceiverPort = NetworkPortAssigner.retrieveNextPort();
		secondReceiver = new TestSipListener(secondReceiverPort, containerPort, secondProto, false);
		secondReceiver.setRecordRoutingProxyTesting(true);
		SipProvider secondProvider = secondReceiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		neutralProvider.addSipListener(neutral);
		secondProvider.addSipListener(secondReceiver);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
		neutralProto.start();
		secondProto.start();
                
                Map<String,String> params = new HashMap();
                params.put( "servletContainerPort", String.valueOf(containerPort)); 
                params.put( "testPort", String.valueOf(senderPort)); 
                params.put( "receiverPort", String.valueOf(receiverPort));
                params.put( "neutralPort", String.valueOf(neutralPort));
                params.put( "cutmePort", String.valueOf(secondReceiverPort));
                deployApplication(projectHome + 
                        "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp", 
                        params, null);                  
	}

	/*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=164
     */
    public void testCancelProxying() throws Exception {
        setupPhones();
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
        sender.sendCancel(true, "testing text");
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        assertNotNull(receiver.getCancelRequest());
        // https://code.google.com/p/sipservlets/issues/detail?id=272
        ReasonHeader reasonHeader = (ReasonHeader) receiver.getCancelRequest().getHeader(ReasonHeader.NAME);
        assertNotNull(reasonHeader);
        assertEquals("SIP", reasonHeader.getProtocol());
        assertEquals(200, reasonHeader.getCause());
        System.out.println("reasonHeader Text " + reasonHeader.getText());
        assertEquals("testing text", reasonHeader.getText());
        assertTrue(receiver.isCancelReceived());
        assertEquals(487,sender.getFinalResponseStatus());  
        
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        sender.sendCancel(true, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        assertNotNull(receiver.getCancelRequest());
        // https://code.google.com/p/sipservlets/issues/detail?id=272
        reasonHeader = (ReasonHeader) receiver.getCancelRequest().getHeader(ReasonHeader.NAME);
        assertNotNull(reasonHeader);
        assertEquals("SIP", reasonHeader.getProtocol());
        assertEquals(200, reasonHeader.getCause());
        System.out.println("reasonHeader Text " + reasonHeader.getText());
        assertNull(reasonHeader.getText());
        assertTrue(receiver.isCancelReceived());
        assertEquals(487,sender.getFinalResponseStatus());  
    }
    
    /*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=283
     */
    public void testCancelProxying2Locations() throws Exception {
        setupPhones();
        String fromName = "cancel-both-location";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);      
        
        String toSipAddress = "sip-servlets.com";
        String toUser = "proxy-receiver";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);
                                
        receiver.setWaitForCancel(true);
        secondReceiver.setWaitForCancel(true);
        secondReceiver.setWaitBeforeFinalResponse(2000);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        sender.sendCancel(true, "testing text");
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        assertNotNull(receiver.getCancelRequest());
        assertTrue(secondReceiver.isCancelReceived());
        assertNotNull(secondReceiver.getCancelRequest());
        // https://code.google.com/p/sipservlets/issues/detail?id=272
        ReasonHeader reasonHeader = (ReasonHeader) receiver.getCancelRequest().getHeader(ReasonHeader.NAME);
        assertNotNull(reasonHeader);
        assertEquals("SIP", reasonHeader.getProtocol());
        assertEquals(200, reasonHeader.getCause());
        System.out.println("reasonHeader Text " + reasonHeader.getText());
        assertEquals("testing text", reasonHeader.getText());
        assertTrue(receiver.isCancelReceived());
        assertTrue(secondReceiver.isCancelReceived());
        assertEquals(487,sender.getFinalResponseStatus());  
        
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        sender.sendCancel(true, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        assertNotNull(receiver.getCancelRequest());
        // https://code.google.com/p/sipservlets/issues/detail?id=272
        reasonHeader = (ReasonHeader) receiver.getCancelRequest().getHeader(ReasonHeader.NAME);
        assertNotNull(reasonHeader);
        assertEquals("SIP", reasonHeader.getProtocol());
        assertEquals(200, reasonHeader.getCause());
        System.out.println("reasonHeader Text " + reasonHeader.getText());
        assertNull(reasonHeader.getText());
        assertTrue(receiver.isCancelReceived());
        assertTrue(secondReceiver.isCancelReceived());
        assertEquals(487,sender.getFinalResponseStatus());  
    }
    
    /*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=164
     */
    public void testCancelProxyingNon1XX() throws Exception {
        setupPhones();
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
		setupPhones();
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
	 * https://github.com/Mobicents/sip-servlets/issues/63
	 */
	public void testRecordRouteFQDNUriProxying() throws Exception {
		setupPhones();
		String fromName = "record-route-uri";
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
		assertTrue(((RecordRouteHeader)receiver.getInviteRequest().getHeader(RecordRouteHeader.NAME)).getAddress().getURI().toString().contains("localhost"));
	}
	
	/*
	 * 
	 */
	public void testPoppedRouteFQDN() throws Exception {
		setupPhones();
		tomcat.removeConnector(sipConnector);
		Connector udpSipConnector = new Connector(
				SipProtocolHandler.class.getName());
		SipProtocolHandler udpProtocolHandler = (SipProtocolHandler) udpSipConnector
				.getProtocolHandler();
		udpProtocolHandler.setPort(containerPort);
		udpProtocolHandler.setIpAddress(sipIpAddress);
		udpProtocolHandler.setSignalingTransport(listeningPointTransport);		
		udpProtocolHandler.setHostNames("test.mobicents.org");
		tomcat.getSipService().addConnector(udpSipConnector);

		String fromName = "popped-route-uri";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);		
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "proxy-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
						
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[]{RouteHeader.NAME}, new String[]{"<sip:test.mobicents.org:" + containerPort + ";lr;maddr="+ sipIpAddress+">"}, true);		
		Thread.sleep(TIMEOUT);
		assertEquals(200,sender.getFinalResponseStatus());		
		assertTrue(sender.isAckSent());
		assertNull(((RecordRouteHeader)receiver.getInviteRequest().getHeader(RouteHeader.NAME)));
	}
	
	/*
	 * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=2
	 */
	public void testCancelRedirectProxying() throws Exception {
		setupPhones();
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
		setupPhones();
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
        setupPhones();
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
        setupPhones();
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
		secondProto.destroy();
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
