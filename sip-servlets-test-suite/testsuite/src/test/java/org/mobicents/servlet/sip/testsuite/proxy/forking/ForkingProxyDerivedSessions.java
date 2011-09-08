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

package org.mobicents.servlet.sip.testsuite.proxy.forking;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.testsuite.proxy.Shootist;
import org.mobicents.servlet.sip.testsuite.proxy.Shootme;

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
		this.shootist = new Shootist(true, null);
		this.ua1 = new Shootme(5057);
		this.ua2 = new Shootme(5056);
	}

	public void testProxy() {
		this.ua1.init("ua1stackName", null);
		this.ua2.init("ua2stackName", null);
		this.shootist.init("useHostName", false, null);
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
	
	@Override
	protected Properties getSipStackProperties() {
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
		"true");
		sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
				"32");
		sipStackProperties.setProperty(SipStandardService.DEBUG_LOG_STACK_PROP, 
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-debug.txt");
		sipStackProperties.setProperty(SipStandardService.SERVER_LOG_STACK_PROP,
				tomcatBasePath + "/" + "mss-jsip-" + getName() +"-messages.xml");
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
		sipStackProperties.setProperty(SipStandardService.AUTOMATIC_DIALOG_SUPPORT_STACK_PROP, "off");		
		sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "1");
		sipStackProperties.setProperty(SipStandardService.LOOSE_DIALOG_VALIDATION, "true");
		sipStackProperties.setProperty(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
		return sipStackProperties;
	}
}
