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
package org.mobicents.servlet.sip.testsuite.timers;

import java.text.ParseException;
import java.util.Iterator;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Checks that applicaiton session expiration is working
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class TimersSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(TimersSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	//1 sec
	private static final int TIMEOUT = 2000;
	//1 minute and 10 sec
	private static final int APP_SESSION_TIMEOUT = 80000;	
//	private static final int TIMEOUT = 100000000;
	
	// the order is important here 
	private static final String[] TIMERS_TO_TEST = new String[]{
		"recurringTimerExpired",
		"timerExpired",
		"sipAppSessionExpired",
		"sipAppSessionReadyToBeInvalidated"
		};				
	
	TestSipListener sender;
	
	ProtocolObjects senderProtocolObjects;	

	
	public TimersSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(projectHome + "/sip-servlets-test-suite/applications/timers-sip-servlet/src/main/sipapp");
		context.setName("sip-test-context");
		context.setPath("sip-test");
		//setting the sip application timeout to 1 minutes instead of the regular 3 minutes
		context.setSipApplicationSessionTimeout(1);
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());		
		assertTrue(tomcat.deployContext(context));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/timers/timers-sip-servlet-dar.properties";
	}
	
	@Override
	protected void setUp() {
		try {
			super.setUp();						
			
			senderProtocolObjects =new ProtocolObjects(
					"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
						
			sender = new TestSipListener(5080, 5070, senderProtocolObjects, false);
			SipProvider senderProvider = sender.createProvider();			
			
			senderProvider.addSipListener(sender);
			
			senderProtocolObjects.start();			
		} catch (Exception ex) {
			fail("unexpected exception ");
		}
	}
	
	public void testTimers() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
		String fromName = "sender";
		String fromSipAddress = "sip-servlets.com";
		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
				fromName, fromSipAddress);
				
		String toUser = "receiver";
		String toSipAddress = "sip-servlets.com";
		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
				toUser, toSipAddress);
		
		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);	
		Thread.sleep(TIMEOUT);
		assertTrue(sender.isAckSent());			
		Thread.sleep(APP_SESSION_TIMEOUT);
		sender.sendBye();
		Thread.sleep(TIMEOUT);
		assertTrue(sender.getOkToByeReceived());
		Thread.sleep(APP_SESSION_TIMEOUT);
		Iterator<String> allMessagesIterator = sender.getAllMessagesContent().iterator();
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		for (int i = 0; i < TIMERS_TO_TEST.length; i++) {
			assertTrue(sender.getAllMessagesContent().contains(TIMERS_TO_TEST[i]));
		}	
	}

	@Override
	protected void tearDown() throws Exception {					
		senderProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
