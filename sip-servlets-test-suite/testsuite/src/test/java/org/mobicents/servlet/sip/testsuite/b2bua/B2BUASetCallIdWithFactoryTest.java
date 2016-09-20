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

package org.mobicents.servlet.sip.testsuite.b2bua;

import java.util.ListIterator;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.AllowHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.UserAgentHeader;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Testing system header modification, in particular callId.
 * 
 * @author Filip Olsson 
 *
 */
public class B2BUASetCallIdWithFactoryTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(B2BUASetCallIdWithFactoryTest.class);

	private static final String TRANSPORT_UDP = "udp";
	private static final String TRANSPORT_TCP = "tcp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 15000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	SipStandardManager sipStandardManager = null;

	public B2BUASetCallIdWithFactoryTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
	    sipStandardManager = new SipStandardManager();
	    SipStandardContext context = new SipStandardContext();
        context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp");
        context.setName("sip-test-context");
        context.setPath("/sip-test");       
        context.addLifecycleListener(new SipContextConfig());
        context.setManager(sipStandardManager);
        boolean success = tomcat.deployContext(
                context);
		assertTrue(success);
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
        
        private static final String B2BUACALL_ID="f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com";
	
	public void testSetCallIdWithFactory() throws Exception {
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-tcp-sender-factory";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = "sip-servlets.com";
		String toUser = "forward-receiver";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, new String[] {UserAgentHeader.NAME, "extension-header", AllowHeader.NAME, "P-SetCallId"}, new String[] {"TestSipListener UA", "extension-sip-listener", "INVITE, CANCEL, BYE, ACK, OPTIONS", B2BUACALL_ID}, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		CallIdHeader receiverCallIdHeader = (CallIdHeader)receiver.getInviteRequest().getHeader(CallIdHeader.NAME);
		CallIdHeader senderCallIdHeader = (CallIdHeader)sender.getInviteRequest().getHeader(CallIdHeader.NAME);

		assertFalse(receiverCallIdHeader.getCallId().equals(senderCallIdHeader.getCallId()));
                assertEquals(B2BUACALL_ID, receiverCallIdHeader.getCallId());

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
