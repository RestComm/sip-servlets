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

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class NoApplicationDeployedJunitTest extends SipServletTestCase {

	private static transient Logger logger = Logger.getLogger(NoApplicationDeployedJunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;
	
	/**
	 * @param name
	 */
	public NoApplicationDeployedJunitTest(String name) {
		super(name);
		startTomcatOnStartup = false;
		initTomcatOnStartup = false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		senderProtocolObjects = new ProtocolObjects("sender",
				"gov.nist", TRANSPORT, AUTODIALOG, null);
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);
	}
	
	@Override
	protected void deployApplication() {
		// test to deploy nothing
		
	}
	
	@Override
	protected String getDarConfigurationFile() {
		return null;
	}
	
	public void testNoMatchingAppDeployedCallerSendBye() throws Exception {
		System.setProperty("javax.servlet.sip.dar", "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/composition/dummy-dar.properties");
		super.tomcat.initTomcat(tomcatBasePath);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
		super.tomcat.startTomcat();
		
		sender = new TestSipListener(5080, 5090, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		SipURI routeAddress = senderProtocolObjects.addressFactory.createSipURI(
				null, "127.0.0.1");
		routeAddress.setPort(5070);
		routeAddress.setLrParam();
		routeAddress.setTransportParam(senderProtocolObjects.transport);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, routeAddress, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	public void testNoAppDeployedCallerSendBye() throws Exception {
		System.setProperty("javax.servlet.sip.dar", "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/composition/empty-dar.properties");
		super.tomcat.initTomcat(tomcatBasePath);
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, listeningPointTransport);
		super.tomcat.startTomcat();
		
		sender = new TestSipListener(5080, 5090, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "1";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		SipURI routeAddress = senderProtocolObjects.addressFactory.createSipURI(
				null, "127.0.0.1");
		routeAddress.setPort(5070);
		routeAddress.setLrParam();
		routeAddress.setTransportParam(senderProtocolObjects.transport);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, routeAddress, false);		
		Thread.sleep(TIMEOUT);
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
