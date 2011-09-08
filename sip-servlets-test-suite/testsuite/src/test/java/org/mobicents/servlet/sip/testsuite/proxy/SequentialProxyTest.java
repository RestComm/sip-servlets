/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardService;

public class SequentialProxyTest extends SipServletTestCase {

	protected Shootist shootist;

	protected Shootme shootme;
	
	protected Cutme cutme;

	private static final int TIMEOUT = 10000;

	public SequentialProxyTest(String name) {
		super(name);

		this.sipIpAddress="0.0.0.0";
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.shootist = new Shootist(false, null);
		shootist.setOutboundProxy(false);
		this.shootme = new Shootme(5057);
		this.cutme = new Cutme();
	}
	
	public void testTreePhonesSecondAnswer() {
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.init("sequential-three", false, null);
		for (int q = 0; q < 30; q++) {
			if (shootist.ended == false || cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
	}

	public void testFirstTargetTimeout() {
		new Thread() {
			public void run() {
				shootist.pauseBeforeBye = 20000;
				shootme.init("stackName", null);
				cutme.init(null);
				shootist.init("sequential", false, null);
			}
		}.start();
		for (int q = 0; q < 8; q++) {
			if (shootist.ended == false || cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");


	}
	
	public void testSingleTargetCancel() {
		new Thread() {
			public void run() {
				shootme.init("stackName", null);
				cutme.init(null);
				shootist.init("sequential-cut", false, null);
			}
		}.start();
		for (int q = 0; q < 8; q++) {
			if (cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
	}
	
	public void testTreePhonesCancel() {
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.setSendCancelOn180(true);
		this.shootist.init("sequential-three", false, null);
		for (int q = 0; q < 30; q++) {
			if (cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
//		if (shootist.ended == false)
//			fail("Conversation not complete!");
		if (cutme.canceled == false)
			fail("The party that was supposed to be cancelled didn't cancel.");
		if (shootme.inviteTid != null)
			fail("This party wasn't supposed to be called.");
	}

	// Here we want to test if the seq proxy will continue to check next branches
	public void testFirstTargetResponds() {
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.init("sequential-reverse", false, null);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false || cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == true)
			fail("This party must not ever be contacted");
	}
	
	// Test for http://code.google.com/p/mobicents/issues/detail?id=2740
	public void testOutboundProxySetting() {

		sipService.setOutboundProxy("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5057");
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.init("sequential-reverse", false, null);
		for (int q = 0; q < 10; q++) {
			if (shootist.ended == false || cutme.canceled == false) {
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
		if (cutme.canceled == true)
			fail("This party must not ever be contacted");

	}

	public void testOKRetransmissionsReachApplication() {
		shootme.retrans = 0;
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.pauseBeforeAck = 40;
		shootist.pauseBeforeBye = 10000;
		this.shootist.init("sequential-retransmission", false, null);
		for (int q = 0; q < 2; q++) {
			if (shootist.ended == false || cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
	}
	
	public void testOKRetransmissionsReachApplicationScrambleResponses() {
		shootme.retrans = 0;
		shootme.scrambleResponses = true;
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.pauseBeforeAck = 40;
		shootist.pauseBeforeBye = 10000;
		
		this.shootist.init("sequential-retransmission", false, null);
		for (int q = 0; q < 2; q++) {
			if (shootist.ended == false || cutme.canceled == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (shootist.ended == false)
			fail("Conversation not complete!");
	}
	
	public void testFirstTargetRespondsBusy() {
		this.shootme.inviteResponseCode = 483;
		this.shootme.init("stackName", null);
		this.cutme.init(null);
		this.shootist.init("sequential-reverse-one", false, null);
		for (int q = 0; q <6; q++) {
			if (shootist.ended == false)
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		if (shootist.lastResponse.getStatusCode() != 483)
			fail("We expected 483 here");
	}
	
	@Override
	public void tearDown() throws Exception {
		shootist.destroy();
		shootme.destroy();
		cutme.destroy();
		super.tearDown();
	}

	SipStandardService sipService;
	@Override
	public void deployApplication() {
		sipService = tomcat.getSipService();
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
