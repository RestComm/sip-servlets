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
package org.mobicents.servlet.sip.tck;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardService;

/**
 * This test class just allows to debug TCK test cases in your IDE
 * @author
 */
public class AppRouterTCKTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(AppRouterTCKTest.class);

    public AppRouterTCKTest(String name) {
        super(name);
        startTomcatOnStartup = true;
        autoDeployOnStartup = true;
        addSipConnectorOnStartup = true;
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat.deployContext(
                projectHome + "/build/jsr289-tck/tck/dist/ar_continue.sar",
                "sip-test-context-continue", "sip-test-continue"));
        assertTrue(tomcat.deployContext(
                projectHome + "/build/jsr289-tck/tck/dist/ar_internalroutemodifier.sar",
                "sip-test-context-int-mod", "sip-test-int-mod"));
        assertTrue(tomcat.deployContext(
                projectHome + "/build/jsr289-tck/tck/dist/ar_reverse.sar",
                "sip-test-context-reverse", "sip-test-reverse"));
        assertTrue(tomcat.deployContext(
                projectHome + "/build/jsr289-tck/tck/dist/ar_suburiregion.sar",
                "sip-test-context-suburiregion", "sip-test-suburiregion"));
        assertTrue(tomcat.deployContext(
                projectHome + "/build/jsr289-tck/tck/dist/ar_success.sar",
                "sip-test-context-ar_success", "sip-test-ar_success"));
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-b2bua-servlet-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        //changes approuter to TCK one
        System.setProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider", "com.bea.sipservlet.tck.ar.TckApplicationRouterProvider");
        containerPort = 5080;
        super.setUp();
    }

    @Ignore("Place breakpoint here and run TCK test case")
    public void testAppRouterTCKApp() throws Exception {
        assertTrue(true);

    }

    @Override
    protected Properties getSipStackProperties() {
        Properties sipStackProperties = new Properties();
        sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
                "32");
        sipStackProperties.setProperty(SipStandardService.DEBUG_LOG_STACK_PROP,
                tomcatBasePath + "/" + "mss-jsip-" + getName() + "-debug.txt");
        sipStackProperties.setProperty(SipStandardService.SERVER_LOG_STACK_PROP,
                tomcatBasePath + "/" + "mss-jsip-" + getName() + "-messages.xml");
        sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
        sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
        sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
        sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "5");
        sipStackProperties.setProperty(SipStandardService.LOOSE_DIALOG_VALIDATION, "true");
        sipStackProperties.setProperty(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
        return sipStackProperties;
    }

    @Override
    protected void tearDown() throws Exception {
        logger.info("Test completed");
        super.tearDown();
    }
}
