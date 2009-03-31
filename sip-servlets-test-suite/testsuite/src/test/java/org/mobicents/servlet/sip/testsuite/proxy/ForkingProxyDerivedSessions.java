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

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;

/**
 * This tests aims to fork to 2 differents destinations each one returning a 200 OK
 * tuhs creating a derived session and check the behavior
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ForkingProxyDerivedSessions extends SipServletTestCase {

	private static transient Logger logger = Logger.getLogger(ForkingProxyDerivedSessions.class);

	protected Shootist shootist;

	protected Shootme ua1;
	
	protected Shootme ua2;

	private static final int TIMEOUT = 5000;

	private static final int receiversCount = 1;

	public ForkingProxyDerivedSessions(String name) {
		super(name);

		this.sipIpAddress="0.0.0.0";
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.shootist = new Shootist(true);
		this.ua1 = new Shootme(5057);
		this.ua2 = new Shootme(5056);
	}

	public void testProxy() {
		this.ua1.init("ua1stackName");
		this.ua2.init("ua2stackName");
		this.shootist.init("useHostName", false);
		for (int q = 0; q < 20; q++) {
			if (!shootist.ended && !ua1.ended) {
				logger.info("loop number " + q);
				try {
					Thread.sleep(TIMEOUT);
				} catch (InterruptedException e) {}
			}
		}
		assertTrue("Conversation not complete!", shootist.ended);
		assertTrue("Conversation not complete!", ua1.ended || ua1.cancelled);
		assertTrue("Conversation not complete!", ua2.ended || ua2.cancelled);
	}

	@Override
	public void tearDown() throws Exception {
		shootist.destroy();
		ua1.destroy();
		ua2.destroy();
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
