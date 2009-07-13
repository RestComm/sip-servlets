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
package org.mobicents.servlet.sip.testsuite.proxy;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

//non regression test for Issue 823
public class SpeedDialJunitTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SpeedDialJunitTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 10000;	
//	private static final int TIMEOUT = 100000000;
	 
	TestSipListener sender;
	TestSipListener receiver;
	ProtocolObjects senderProtocolObjects;
	ProtocolObjects	receiverProtocolObjects;

	public SpeedDialJunitTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	@Override
	public void deployApplication() {

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
	
	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/speeddial-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {		
		super.setUp();

		senderProtocolObjects = new ProtocolObjects("sender",
				"gov.nist", TRANSPORT, AUTODIALOG, "127.0.0.1:5070");
		receiverProtocolObjects = new ProtocolObjects("receiver",
				"gov.nist", TRANSPORT, AUTODIALOG, null);			
	}
	
	public void testSpeedDialCallerSendBye() throws Exception {
		deploySpeedDial("record_route", "false");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		sender.setSendSubsequentRequestsThroughSipProvider(true);
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
				
		String toUser = "9";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
//		assertTrue(sender.getOkToByeReceived());
		assertTrue(receiver.getByeReceived());
	}
	
	public void testSpeedDialErrorResponse() throws Exception {	
		deploySpeedDial("record_route", "false");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		sender.setSendSubsequentRequestsThroughSipProvider(true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, false);
		receiver.setRecordRoutingProxyTesting(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);
		receiver.setRespondWithError(408);
		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender-expect-408";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "9";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isServerErrorReceived());
	}

	public void testSpeedDialCalleeSendBye() throws Exception {
		deploySpeedDial("record_route", "false");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setSendSubsequentRequestsThroughSipProvider(true);
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
				
		String toUser = "9";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
		Thread.sleep(TIMEOUT);
		assertTrue(receiver.getOkToByeReceived());
		assertTrue(sender.getByeReceived());		
	}

	public void testCancelSpeedDial() throws Exception {
		deploySpeedDial("record_route", "false");
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
		sender.setSendSubsequentRequestsThroughSipProvider(true);
		sender.setRecordRoutingProxyTesting(true);
		SipProvider senderProvider = sender.createProvider();

		receiver = new TestSipListener(5090, 5070, receiverProtocolObjects, true);
		receiver.setRecordRoutingProxyTesting(true);
		receiver.setWaitForCancel(true);
		SipProvider receiverProvider = receiver.createProvider();

		receiverProvider.addSipListener(receiver);
		senderProvider.addSipListener(sender);

		senderProtocolObjects.start();
		receiverProtocolObjects.start();

		String fromName = "sender";
		String fromHost = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromHost);
				
		String toUser = "9";
		String toHost = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toHost);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(500);
		sender.sendCancel();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isCancelOkReceived());
		assertTrue(sender.isRequestTerminatedReceived());
		assertTrue(receiver.isCancelReceived());
	}
	
	@Override
	protected void tearDown() throws Exception {	
		senderProtocolObjects.destroy();
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
