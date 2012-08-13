/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import java.util.ListIterator;

import javax.sip.ListeningPoint;
import javax.sip.header.RecordRouteHeader;

import org.mobicents.servlet.sip.SipServletTestCase;

public class ParallelProxyWithRecordRouteTest extends SipServletTestCase {

	private static final String STACK_NAME = "shootme";

	protected Shootist shootist;

	protected Shootme shootme;
	
	protected Cutme cutme;

	private static final int TIMEOUT = 15000;
	private static final int TIMEOUT_READY_TO_INVALIDATE = 70000;

	public ParallelProxyWithRecordRouteTest(String name) {
		super(name);

		this.sipIpAddress="0.0.0.0";
		startTomcatOnStartup = false;
		autoDeployOnStartup = false;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		tomcat.addSipConnector(serverName, sipIpAddress, 5070, ListeningPoint.TCP);
		tomcat.startTomcat();
		deployApplication();
		this.shootist = new Shootist(false, null);
		shootist.setOutboundProxy(false);
		this.shootme = new Shootme(5057);
		this.cutme = new Cutme();
	}
	
	public void testProxyCalleeSendsBye() {
		this.shootme.init(STACK_NAME, null);		
		this.shootme.callerSendsBye = false;
		this.shootme.setTimeToWaitBeforeAnswer(300);
		try {
		this.cutme.init(null);
		this.cutme.setTimeToWaitBeforeAnswer(300);
		this.shootist.init("useHostName",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		} catch(Exception e) {}
		this.shootme.callerSendsBye = true;
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
		if(shootist.getNumberOfTryingReceived() != 1) {
			fail("We got " + shootist.getNumberOfTryingReceived() +" Trying, we should have received one !");
		}
	}
	

	public void testProxy() {
		this.shootme.init(STACK_NAME, null);
		this.cutme.init(null);
		this.shootist.init("useHostName",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.isRequestTerminatedReceived())
			fail("487 received from other end!");
		if (!shootist.ended)
			fail("Conversation not complete!");
		if (!cutme.canceled)
			fail("The party that was supposed to be cancelled didn't cancel.");
	}
	
	public void testProxyReadyToInvalidate() {
		this.shootme.init(STACK_NAME, null);
		this.cutme.init(null);
		this.shootist.init("check_rti",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT_READY_TO_INVALIDATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
		assertNotNull(shootist.getLastMessageContent());	
		assertEquals("sessionReadyToInvalidate", shootist.getLastMessageContent());
	}
	
	public void testProxyReadyToInvalidateTCP() {
		this.shootme.init(STACK_NAME, "tcp");
		this.cutme.init("tcp");
		this.shootist.init("check_rti",false, "tcp");
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT_READY_TO_INVALIDATE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
		assertNotNull(shootist.getLastMessageContent());	
		assertEquals("sessionReadyToInvalidate", shootist.getLastMessageContent());
	}
	
	// Non regression test for http://code.google.com/p/sipservlets/issues/detail?id=81
	public void testProxyRecordRoutePushRouteTCP() {
		this.shootme.init(STACK_NAME, "tcp");
		this.cutme.init("tcp");
		this.shootist.init("unique-location-urn-route-tcp-uri1",false, "tcp");
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		//proxied to unique location so no cancel from cutme
		if (cutme.canceled == true)
			fail("The party that was supposed to be cancelled didn't cancel.");
		assertNotNull(shootist.getLastMessageContent());
		ListIterator<RecordRouteHeader> recordRouteHeaders = shootme.getInviteRequest().getHeaders(RecordRouteHeader.NAME);
		assertTrue(recordRouteHeaders.hasNext());
		recordRouteHeaders.next();
		//make sure we only have 1 RR Header
		assertFalse(recordRouteHeaders.hasNext());
		assertEquals("sessionReadyToInvalidate", shootist.getLastMessageContent());
	}
	
	/**
	 * Non regression test for  Issue 747 : Non Record Routing Proxy is adding Record Route on informational response
	 * http://code.google.com/p/mobicents/issues/detail?id=747
	 */
	public void testProxyNonRecordRouting() {
		this.shootme.init(STACK_NAME, null);
		this.cutme.init(null);
		this.shootist.init("nonRecordRouting",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
	}
	
	/**
	 * Non regression test for Issue 851 : NPE on handling URI parameters in address headers
	 * http://code.google.com/p/mobicents/issues/detail?id=851
	 */
	public void testProxyURIParams() {
		this.shootme.init(STACK_NAME, null);
		this.cutme.init(null);
		this.shootist.init("check_uri",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");		
	}
	
	/**
	 * Non regression test for Issue 2417 : Two 100 Trying responses sent if Proxy decision is delayed. 
	 * http://code.google.com/p/mobicents/issues/detail?id=2417
	 */
	public void testProxy2Trying() {
		this.shootme.init(STACK_NAME, null);
		this.cutme.init(null);
		this.shootist.init("test_2_trying",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");		
		if(shootist.getNumberOfTryingReceived() > 1) {
			fail("We got " + shootist.getNumberOfTryingReceived() +" Trying, we should have received only one !");
		}
		if(shootist.getNumberOfTryingReceived() != 1) {
			fail("We got " + shootist.getNumberOfTryingReceived() +" Trying, we should have received one !");
		}
	}
	
	/**
	 * Non regression test for Issue 2440 : SipSession.createRequest on proxy SipSession does not throw IllegalStateException
	 * http://code.google.com/p/mobicents/issues/detail?id=2440
	 */
	public void testProxyCreateSubsequent() {
		this.shootme.init(STACK_NAME, null);		
		this.cutme.init(null);
		this.shootist.init("test_create_subsequent_request",false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (shootist.ended == true)
			fail("Conversation complete where it shouldn't be since the createSubsequentRequest should have thrown an IllegalStateException!");				
	}
	
	/**
	 * Non regression test for Issue 2850 : Use Request-URI custom Mobicents parameters to route request for misbehaving agents
	 * http://code.google.com/p/mobicents/issues/detail?id=2850
	 */
	public void testProxyRequestURIMobicentsParam() {
		this.shootme.init(STACK_NAME, null);	
		this.shootist.init("unique-location",false, null);
		this.shootist.moveRouteParamsToRequestURI = true;
		this.cutme.init(null);
		for (int q = 0; q < 20; q++) {
			if (!shootist.ended && !cutme.canceled)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		if (!shootist.ended)
			fail("Conversation not complete");				
	}

	@Override
	public void tearDown() throws Exception {
		shootist.destroy();
		shootme.destroy();
		if(cutme != null)
			cutme.destroy();
		super.tearDown();
	}

	@Override
	public void deployApplication() {
		assertTrue(tomcat
				.deployContext(
						projectHome
								+ "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
						"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///"
				+ projectHome
				+ "/sip-servlets-test-suite/testsuite/src/test/resources/"
				+ "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
	}
}
