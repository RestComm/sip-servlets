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
 * Non regression test for Issue 790 :  
 * 1 SipSession should not be used in 2 different app session (http://code.google.com/p/mobicents/issues/detail?id=790)
 * @author jean.deruelle@gmail.com
 *
 */
public class SameSipSessionB2BUACompositionJunitTest extends SipServletTestCase {
	
	private static final String TO_NAME = "receiver";
	private static final String FROM_NAME = "samesipsession";
	private static final String CANCEL_FROM_NAME = "cancel-samesipsession";
	private static final String ERROR_FROM_NAME = "error-samesipsession";
	
	private static final String FROM_DOMAIN = "sip-servlets.com";
	private static final String TO_DOMAIN = "127.0.0.1:5070";	

	private static transient Logger logger = Logger.getLogger(SameSipSessionB2BUACompositionJunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	ProtocolObjects senderProtocolObjects;

	public SameSipSessionB2BUACompositionJunitTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		deployCallForwarding();
	}

	private void deployCallForwarding() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
				"call-forwarding-b2bua-context", 
				"call-forwarding-b2bua"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/composition/samesipsession-composition-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		autoDeployOnStartup = false;
		super.setUp();

		senderProtocolObjects = new ProtocolObjects(FROM_NAME,
				"gov.nist", TRANSPORT, AUTODIALOG, null);
	}
	
	public void testSameSipSessionCallerSendBye() throws Exception {
		deployCallForwarding();
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();

		String fromName = FROM_NAME;
		String fromSipAddress = FROM_DOMAIN;
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = TO_DOMAIN;
		String toUser = TO_NAME;
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);		
		Thread.sleep(TIMEOUT);
		//checking numbers of ACK received see http://forums.java.net/jive/thread.jspa?messageID=277840
		assertTrue(sender.getOkToByeReceived());
	}

	public void testSameSipSessionCancel() throws Exception {
		deployCallForwarding();
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();

		String fromName = CANCEL_FROM_NAME;
		String fromSipAddress = FROM_DOMAIN;
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = TO_DOMAIN;
		String toUser = TO_NAME;
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(100);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
	}
	
	public void testSameSipSessionErrorResponse() throws Exception {
		deployCallForwarding();
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();

		String fromName = ERROR_FROM_NAME;
		String fromSipAddress = FROM_DOMAIN;
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
		
		String toSipAddress = TO_DOMAIN;
		String toUser = TO_NAME;
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, true);
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isFinalResponseReceived());
		assertEquals(500, sender.getFinalResponseStatus());
	}
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		logger.info("Test completed");
		super.tearDown();
	}


}
