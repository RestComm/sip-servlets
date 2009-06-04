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
package org.mobicents.servlet.sip.testsuite.routing;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ExternalApplicationRoutingTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ExternalApplicationRoutingTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	
	ProtocolObjects senderProtocolObjects;	
	ProtocolObjects recieverProtocolObjects;	
	
	public ExternalApplicationRoutingTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
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
	
	protected String getEmptyDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/routing/external/empty-dar.properties";
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
		
		
		recieverProtocolObjects =new ProtocolObjects(
				"registerReciever", "gov.nist", TRANSPORT, AUTODIALOG, null);
		
		receiver = new TestSipListener(5058, 5070, recieverProtocolObjects, false);
		SipProvider registerRecieverProvider = receiver.createProvider();			
		
		registerRecieverProvider.addSipListener(receiver);
		
		recieverProtocolObjects.start();		
	}
	
	
	// If an app is called even if it just send an informational response nothing make sure it is not forwarded outside
	public void testExternalRoutingWithoutFinalResponse() throws Exception {
		tomcat.startTomcat();
		deployApplication();
		
		String fromName = "testExternalRouting";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String r = "requestUri";
		String ra = "127.0.0.1:5058";
		SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
				r, ra);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertFalse(receiver.isInviteReceived());	
	}
	
	// If an app is called even if it does nothing make sure it is not forwarded outside
	public void testExternalRoutingWithoutInfoResponse() throws Exception {
		tomcat.startTomcat();
		deployApplication();
		
		String fromName = "testExternalRoutingNoInfo";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String r = "requestUri";
		String ra = "127.0.0.1:5058";
		SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
				r, ra);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertFalse(receiver.isInviteReceived());	
	}
	
	// If no app is called make sure it is forwarded outside
	public void testExternalRoutingNoAppCalled() throws Exception {
		tomcat.setDarConfigurationFilePath(getEmptyDarConfigurationFile());
		tomcat.startTomcat();		
		
		String fromName = "testExternalRoutingNoAppCalled";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String r = "requestUri";
		String ra = "127.0.0.1:5058";
		SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
				r, ra);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.isInviteReceived());	
		assertTrue(receiver.getByeReceived());
	}
	
	// If no app is called make sure it is forwarded outside even for ReInvite
	public void testExternalRoutingNoAppCalledReInvite() throws Exception {
		tomcat.setDarConfigurationFilePath(getEmptyDarConfigurationFile());
		tomcat.startTomcat();		
		
		String fromName = "testExternalRoutingNoAppCalled";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		String r = "requestUri";
		String ra = "127.0.0.1:5058";
		SipURI requestUri = senderProtocolObjects.addressFactory.createSipURI(
				r, ra);
		
		sender.setSendReinvite(true);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false, null, null, requestUri);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.isInviteReceived());	
		assertTrue(receiver.getByeReceived());
	}
	
	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();	
		recieverProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
