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

package org.mobicents.servlet.sip.testsuite.b2bua;

import java.io.File;
import java.util.ListIterator;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.UserAgentHeader;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Testing if sessionreadytoinvalidate is called twice http://code.google.com/p/mobicents/issues/detail?id=2246
 * 
 * @author Vladimir
 *
 */
public class B2BUASessionCallbackTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(B2BUASessionCallbackTest.class);

	private static final String TRANSPORT_UDP = "udp";
	private static final String TRANSPORT_TCP = "tcp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 15000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public B2BUASessionCallbackTest(String name) {
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
		super.setUp();

		tomcat.addSipConnector(serverName, sipIpAddress, 5070, ListeningPoint.TCP);
		tomcat.startTomcat();
		deployApplication();
		
		senderProtocolObjects = new ProtocolObjects("forward-udp-sender",
				"gov.nist", TRANSPORT_UDP, AUTODIALOG, null, null, null);
		receiverProtocolObjects = new ProtocolObjects("forward-tcp-receiver",
				"gov.nist", TRANSPORT_TCP, AUTODIALOG, null, null, null);
			
	}
	
	public void testCallForwardingCallerSendByeDoubleCallback() throws Exception {
		File testResult = new File("b2buaSessionDoubleCallbackTest");
		testResult.delete();
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-tcp-sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "forward-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {UserAgentHeader.NAME, "extension-header", "DOUBLE"}, new String[] {"TestSipListener UA", "extension-sip-listener","DOUBLE"}, true);		
		Thread.sleep(TIMEOUT+8000);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);
		ListIterator<UserAgentHeader> userAgentHeaderIt = receiver.getInviteRequest().getHeaders(UserAgentHeader.NAME);
		int i = 0; 
		while (userAgentHeaderIt.hasNext()) {
			UserAgentHeader userAgentHeader = (UserAgentHeader) userAgentHeaderIt
					.next();
			assertTrue(userAgentHeader.toString().trim().endsWith("CallForwardingB2BUASipServlet"));
			i++;
		}
		assertEquals(1, i);
		ListIterator<ContactHeader> contactHeaderIt = receiver.getInviteRequest().getHeaders(ContactHeader.NAME);
		i = 0; 
		while (contactHeaderIt.hasNext()) {
			ContactHeader contactHeader = (ContactHeader) contactHeaderIt
					.next();			
			assertTrue(contactHeader.toString().trim().startsWith("Contact: \"callforwardingB2BUA\" <sip:test@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5070;q=0.1;transport=tcp;test>;test"));
			i++;
		}
		assertEquals(1, i);
		ListIterator<Header> extensionHeaderIt = receiver.getInviteRequest().getHeaders("extension-header");
		i = 0; 
		while (extensionHeaderIt.hasNext()) {	
			extensionHeaderIt.next();
			i++;
		}
		assertEquals(2, i);
		userAgentHeaderIt = receiver.getByeRequestReceived().getHeaders(UserAgentHeader.NAME);
		i = 0; 
		while (userAgentHeaderIt.hasNext()) {
			UserAgentHeader userAgentHeader = (UserAgentHeader) userAgentHeaderIt
					.next();
			assertTrue(userAgentHeader.toString().trim().endsWith("CallForwardingB2BUASipServlet"));
			i++;
		}
		assertEquals(1, i);
		contactHeaderIt = receiver.getByeRequestReceived().getHeaders(ContactHeader.NAME);
		i = 0; 
		while (contactHeaderIt.hasNext()) {
			ContactHeader contactHeader = (ContactHeader) contactHeaderIt
					.next();
			i++;
		}
		assertEquals(0, i);
		assertFalse(receiverCallIdHeader.getCallId().equals(senderCallIdHeader.getCallId()));
		extensionHeaderIt = receiver.getByeRequestReceived().getHeaders("extension-header");
		i = 0; 
		while (extensionHeaderIt.hasNext()) {	
			extensionHeaderIt.next();
			i++;
		}
		assertEquals(2, i);
		assertFalse(testResult.exists());
	}
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();	
		sender = null;
		receiver = null;
		logger.info("Test completed");
		super.tearDown();
	}


}
