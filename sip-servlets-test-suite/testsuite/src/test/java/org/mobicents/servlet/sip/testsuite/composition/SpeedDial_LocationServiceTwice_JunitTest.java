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

package org.mobicents.servlet.sip.testsuite.composition;

import java.util.ListIterator;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.Header;
import javax.sip.header.ReasonHeader;
import javax.sip.header.RecordRouteHeader;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SpeedDial_LocationServiceTwice_JunitTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SpeedDial_LocationServiceTwice_JunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
//	private static final int TIMEOUT = 100000000;
	 
	TestSipListener sender;
	TestSipListener receiver;
	TestSipListener receiver2;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	ProtocolObjects	receiver2ProtocolObjects;

	public SpeedDial_LocationServiceTwice_JunitTest(String name) {
		super(name);
	}

	@Override
	protected void deployApplication() {
		
	}
	
	public void deployBothApplication(boolean isSpeedDialRecordRoute) {
		deploySpeedDial("record_route", "" + isSpeedDialRecordRoute);
		deployLocationService();
	}

	public void deploySpeedDial(String name, String value) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/speed-dial-servlet/src/main/sipapp");
		context.setName("speed-dial-context");
		context.setPath("speed-dial");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		ApplicationParameter applicationParameter = new ApplicationParameter();
		applicationParameter.setName(name);
		applicationParameter.setValue(value);
		context.addApplicationParameter(applicationParameter);
		assertTrue(tomcat.deployContext(context));
	}		
	
	private void deployLocationService() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
				"location-service-context", 
				"location-service"));
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/composition/speeddial-locationservice-twice-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();

		senderProtocolObjects = new ProtocolObjects("sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);			
		receiver2ProtocolObjects = new ProtocolObjects("receiver2",
				"gov.nist", TRANSPORT, AUTODIALOG, null, null, null);			
	}
	
	/*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=273
	 */
	public void testSpeedDialLocationServicePRACKCalleeSendBye() throws Exception {
		deployBothApplication(true);
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, true);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "7";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		String[] headerNames = new String[]{"require"};
		String[] headerValues = new String[]{"100rel"};
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, headerNames, headerValues, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());
		int numberOfPrack = 0;
		ListIterator<Header> listHeaderIt =  receiver.getPrackRequestReceived().getHeaders("X-Seen");
		while (listHeaderIt.hasNext()) {
			listHeaderIt.next();
			numberOfPrack++;
		}
		assertEquals(2,numberOfPrack);
	}
	
	/*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=274
	 */
	public void testSpeedDialLocationServiceRecordRouteReInviteCallerSendBye() throws Exception {
		deployBothApplication(true);
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "7";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.setSendReinvite(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);			
		assertTrue(receiver.isInviteReceived());
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
		int numberOfRRouteHeaders = 0;
		ListIterator<Header> listHeaderIt =  receiver.getInviteRequest().getHeaders(RecordRouteHeader.NAME);
		while (listHeaderIt.hasNext()) {
			listHeaderIt.next();
			numberOfRRouteHeaders++;
		}
		assertEquals(3,numberOfRRouteHeaders);
	}
	
	/*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=275
	 */
	public void testSpeedDialLocationServiceRecordRouteReInviteCalleeSendReInvite() throws Exception {
		deployBothApplication(true);
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "7";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		receiver.setSendReinvite(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);			
		assertTrue(sender.isInviteReceived());
		URI requestUri = sender.getInviteRequest().getRequestURI();
		assertEquals("sip:sender@127.0.0.1:5080;transport=udp;lr", requestUri.toString().trim());
		sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getByeReceived());
		assertTrue(sender.getOkToByeReceived());
		int numberOfRRouteHeaders = 0;
		ListIterator<Header> listHeaderIt =  receiver.getInviteRequest().getHeaders(RecordRouteHeader.NAME);
		while (listHeaderIt.hasNext()) {
			listHeaderIt.next();
			numberOfRRouteHeaders++;
		}
		assertEquals(3,numberOfRRouteHeaders);
	}
	
	/*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=164
     */
    public void testCancelProxying() throws Exception {
    	deployBothApplication(false);
    	sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
		receiver2 = new TestSipListener(5091, 5070, receiver2ProtocolObjects, false);
		receiver2.setRecordRoutingProxyTesting(true);
		SipProvider receiver2Provider = receiver2.createProvider();

		receiverProvider.addSipListener(receiver);
		receiver2Provider.addSipListener(receiver2);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();
		receiver2ProtocolObjects.start();
    	
        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);      
        
        String toSipAddress = "sip-servlets.com";
        String toUser = "8";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);
                                
        receiver2.setWaitForCancel(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);     
        Thread.sleep(TIMEOUT);
        assertTrue(receiver2.isCancelReceived());
        assertTrue(sender.isFinalResponseReceived());
        // https://code.google.com/p/sipservlets/issues/detail?id=272
    }
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
