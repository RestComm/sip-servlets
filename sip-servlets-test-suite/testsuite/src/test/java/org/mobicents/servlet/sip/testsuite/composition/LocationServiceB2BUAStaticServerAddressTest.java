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
package org.mobicents.servlet.sip.testsuite.composition;

import gov.nist.javax.sip.header.Contact;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.catalina.connector.Connector;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.UDPPacketForwarder;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class LocationServiceB2BUAStaticServerAddressTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(LocationServiceB2BUAStaticServerAddressTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
//	private static final int TIMEOUT = 100000000;
	 
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	UDPPacketForwarder ipBalancer;

	public LocationServiceB2BUAStaticServerAddressTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		addSipConnectorOnStartup =  false;
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {
		deployCallForwarding();
		deployLocationService();
	}

	private void deployCallForwarding() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
				"call-forwarding-b2bua-context", 
				"call-forwarding-b2bua"));
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
				+ "org/mobicents/servlet/sip/testsuite/composition/location-b2bua-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();

		Connector udpSipConnector = null;
		try {
			udpSipConnector = new Connector(
					SipProtocolHandler.class.getName());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SipProtocolHandler udpProtocolHandler = (SipProtocolHandler) udpSipConnector
				.getProtocolHandler();
		try {
			udpProtocolHandler.setPort(5070);
			udpProtocolHandler.setIpAddress("127.0.0.1");

			udpProtocolHandler.setSignalingTransport("udp");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		udpProtocolHandler.setSipStackProperties(null);
		udpProtocolHandler.setUseStaticAddress(true);
		udpProtocolHandler.setStaticServerAddress("127.0.0.1");
		udpProtocolHandler.setStaticServerPort(5005);
		tomcat.getSipService().addConnector(udpSipConnector);
		try {
			tomcat.startTomcat();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		deployApplication();
		senderProtocolObjects = new ProtocolObjects("sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null);
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
	}
	
	public void testLocationServiceCallForwardingCallerSendBye() throws Exception {		
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();
		
		ipBalancer = new UDPPacketForwarder(5005, 5070, "127.0.0.1");
		ipBalancer.start();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "forward-sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "proxy-b2bua";
		String toHost = "127.0.0.1:5070";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
		Contact contact = (Contact) sender.getInviteOkResponse().getHeader(Contact.NAME);
		SipURI sipURI = (SipURI) contact.getAddress().getURI();
		assertTrue(sipURI.getPort() == 5005);
	}

	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();	
		ipBalancer.stop();
		logger.info("Test completed");
		super.tearDown();
	}


}
