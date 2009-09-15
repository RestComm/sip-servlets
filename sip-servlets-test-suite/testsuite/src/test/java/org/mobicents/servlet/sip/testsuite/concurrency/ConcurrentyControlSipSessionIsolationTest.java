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
package org.mobicents.servlet.sip.testsuite.concurrency;

import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ConcurrentyControlSipSessionIsolationTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(ConcurrentyControlSipSessionIsolationTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
//	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	
	public ConcurrentyControlSipSessionIsolationTest(String name) {
		super(name);
		autoDeployOnStartup = false;
	}

	public void deployApplication() {
		
	}
	
	public void deployApplication(ConcurrencyControlMode concurrencyControlMode) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome
				+ "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		context.setConcurrencyControlMode(concurrencyControlMode);
		assertTrue(tomcat.deployContext(context));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
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
	}
	
	public void testElapsedTimeAndSessionOverlapping() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		deployApplication(ConcurrencyControlMode.SipSession);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setSendBye(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(2000);
//		long startTime = System.currentTimeMillis();
		sender.sendInDialogSipRequest("INFO", "1", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "2", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "3", "text", "plain", null);
		sender.sendBye();
		Thread.sleep(20000);
//		long elapsedTime = sender.getLastInfoResponseTime() - startTime;
//		assertTrue(elapsedTime>15000);
		assertTrue(!sender.isServerErrorReceived());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testElapsedTimeAndSipApplicationSessionOverlapping() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		deployApplication(ConcurrencyControlMode.SipApplicationSession);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setSendBye(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(2000);
//		long startTime = System.currentTimeMillis();
		sender.sendInDialogSipRequest("INFO", "1", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "2", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "3", "text", "plain", null);
		sender.sendBye();
		Thread.sleep(20000);
//		long elapsedTime = sender.getLastInfoResponseTime() - startTime;
//		assertTrue(elapsedTime>15000);
		assertTrue(!sender.isServerErrorReceived());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	public void testElapsedTimeAndSessionOverlappingWithNoConcurrencyControl() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		deployApplication(ConcurrencyControlMode.None);
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.setSendBye(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(3000);
//		long startTime = System.currentTimeMillis();
		sender.sendInDialogSipRequest("INFO", "1", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "2", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "3", "text", "plain", null);
		sender.sendBye();
		Thread.sleep(10000);
//		long elapsedTime = sender.getLastInfoResponseTime() - startTime;
//		assertTrue(elapsedTime<7000);
		assertTrue(sender.isServerErrorReceived());
		assertTrue(sender.isAckSent());
		assertTrue(sender.getOkToByeReceived());		
	}
	
	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
