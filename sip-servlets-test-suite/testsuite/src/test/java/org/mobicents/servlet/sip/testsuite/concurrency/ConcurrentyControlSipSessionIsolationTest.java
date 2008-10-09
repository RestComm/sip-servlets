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
import java.util.Iterator;

import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ConcurrentyControlSipSessionIsolationTest extends SipServletTestCase {
	
	private static Log logger = LogFactory.getLog(ConcurrentyControlSipSessionIsolationTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final int TIMEOUT = 5000;	
//	private static final int TIMEOUT = 100000000;
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	
	public ConcurrentyControlSipSessionIsolationTest(String name) {
		super(name);
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
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();						
		
		senderProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG);
					
		sender = new TestSipListener(5080, 5070, senderProtocolObjects, true);
		SipProvider senderProvider = sender.createProvider();			
		
		senderProvider.addSipListener(sender);
		
		senderProtocolObjects.start();			
	}
	
	public void testElapsedTime() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		long startTime = System.currentTimeMillis();
		sender.setSendBye(false);
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
		Thread.sleep(3000);
		sender.sendInDialogSipRequest("INFO", "1", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "2", "text", "plain", null);
		sender.sendInDialogSipRequest("INFO", "3", "text", "plain", null);
		Thread.sleep(30000);
		sender.sendBye();
		Thread.sleep(3000);
		long elapsedTime = sender.getLastInfoResponseTime() - startTime;
		assertTrue(elapsedTime<15000);
		assertTrue(!sender.isServerErrorReceived());
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
