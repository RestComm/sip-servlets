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
package org.mobicents.servlet.sip.testsuite.session;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.SipSession;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SessionStateUACSipServletTest extends SipServletTestCase {
	
	private static transient Logger logger = Logger.getLogger(SessionStateUACSipServletTest.class);

	private static final String TRANSPORT = "udp";
	private static final boolean AUTODIALOG = true;
	private static final String SEND_1XX_2XX = "send1xx_2xx";
	private static final String SEND_1XX_4XX = "send1xx_4xx";
	private static final String SEND_4XX = "send4xx";
	private static final String SEND_2XX = "send2xx";
	
	private List<String> send_1xx_2xx_sessionStateList;
	private List<String> send_1xx_4xx_sessionStateList;
	private List<String> send_4xx_sessionStateList;
	private List<String> send_2xx_sessionStateList;

	private static final int TIMEOUT = 35000;
	
	TestSipListener receiver;
	
	ProtocolObjects receiverProtocolObjects;	

	public SessionStateUACSipServletTest(String name) {
		super(name);
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/session-state-uac/src/main/sipapp",
				"sip-test-context",
				"sip-test"));		
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/session/session-state-uac-dar.properties";
	}
	
	@Override
	protected void setUp() throws Exception {
		send_1xx_4xx_sessionStateList = new ArrayList<String>();
		send_1xx_4xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		send_1xx_4xx_sessionStateList.add(SipSession.State.EARLY.toString());
		send_1xx_4xx_sessionStateList.add(SipSession.State.INITIAL.toString());
		
		receiverProtocolObjects =new ProtocolObjects(
				"sender", "gov.nist", TRANSPORT, AUTODIALOG, null);
					
		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
		List<Integer> provisionalResponsesToSend = receiver.getProvisionalResponsesToSend();
		provisionalResponsesToSend.clear();
		provisionalResponsesToSend.add(Response.RINGING);	
		receiver.setFinalResponseToSend(Response.FORBIDDEN);
		
		SipProvider senderProvider = receiver.createProvider();			
		
		senderProvider.addSipListener(receiver);
		
		receiverProtocolObjects.start();
		
		super.setUp();																							
	}
	
	public void testSessionStateUAC_1xx_4xx() throws InterruptedException, SipException, ParseException, InvalidArgumentException {		
		Thread.sleep(TIMEOUT);
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}		
		
		assertEquals(send_1xx_4xx_sessionStateList.size(), receiver.getAllMessagesContent().size());		
		for (int i = 0; i < send_1xx_4xx_sessionStateList.size(); i++) {
			assertTrue(receiver.getAllMessagesContent().contains(send_1xx_4xx_sessionStateList.get(i)));
		}	
	}	
	
	// Test for SS spec 11.1.6 transaction timeout notification
	public void testTransactionTimeoutResponse() throws InterruptedException, SipException, ParseException, InvalidArgumentException {		
		Thread.sleep(TIMEOUT);
		
		Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
		}
		assertTrue(receiver.txTimeoutReceived);
	}	

	@Override
	protected void tearDown() throws Exception {					
		receiverProtocolObjects.destroy();			
		logger.info("Test completed");
		super.tearDown();
	}


}
