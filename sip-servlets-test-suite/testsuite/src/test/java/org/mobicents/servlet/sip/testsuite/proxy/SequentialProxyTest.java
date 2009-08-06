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

import org.mobicents.servlet.sip.SipServletTestCase;

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
		this.shootist = new Shootist(false);
		shootist.setOutboundProxy(false);
		this.shootme = new Shootme(5057);
		this.cutme = new Cutme();
	}

	public void testFirstTargetTimeout() {
		this.shootme.init("stackName");
		this.cutme.init();
		this.shootist.init("sequential", false);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
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
	
	// Here we want to test if the seq proxy will continue to check next branches
	public void testFirstTargetResponds() {
		this.shootme.init("stackName");
		this.cutme.init();
		this.shootist.init("sequential-reverse", false);
		for (int q = 0; q < 20; q++) {
			if (shootist.ended == false && cutme.canceled == false)
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
	
	@Override
	public void tearDown() throws Exception {
		shootist.destroy();
		shootme.destroy();
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
