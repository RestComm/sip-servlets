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
package org.mobicents.servlet.sip.testsuite.security;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import static junit.framework.Assert.assertTrue;

import org.apache.catalina.realm.MemoryRealm;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class PAssertedIdentityAuthTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootmeSipServletAuthTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 5000;
//	 private static final int TIMEOUT = 100000000;

    TestSipListener sender;

    ProtocolObjects senderProtocolObjects;

    public PAssertedIdentityAuthTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
    }

    public SipStandardContext deployApplication(Map<String, String> params) {

        MemoryRealm realm = new MemoryRealm();
        realm
                .setPathname(projectHome
                        + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                        + "org/mobicents/servlet/sip/testsuite/security/tomcat-users.xml");
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/shootme-sip-servlet-auth/src/main/sipapp",
                "sip-test",
                params,
                null,
                realm);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/security/shootme-sip-servlet-auth-dar.properties";
    }

    @Override
    protected void setUp() {
        try {
            containerPort = NetworkPortAssigner.retrieveNextPort();
            super.setUp();

            senderProtocolObjects = new ProtocolObjects("sender", "gov.nist",
                    TRANSPORT, AUTODIALOG, null, null, null);
            int senderPort = NetworkPortAssigner.retrieveNextPort();
            sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects,
                    true);
            sender.setRecordRoutingProxyTesting(true);
            SipProvider senderProvider = sender.createProvider();
            senderProvider.addSipListener(sender);
            senderProtocolObjects.start();

            Map<String, String> params = new HashMap();
            params.put("servletContainerPort", String.valueOf(containerPort));
            params.put("testPort", String.valueOf(senderPort));
            params.put("testContextApplicationParameter", "OK");
            deployApplication(params);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    }

    public void testShootme() throws InterruptedException, SipException,
            ParseException, InvalidArgumentException {
        String fromName = "p-asserted-user";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        String[] headerNames = new String[]{"P-Asserted-Identity"};
        String[] headerContents = new String[]{"\"User One\" <sip:user@localhost.com>"};

        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null,
                false, headerNames, headerContents, true);

        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        assertTrue(sender.getOkToByeReceived());
        assertTrue(!sender.isAuthenticationErrorReceived());
    }

    @Override
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

}
