/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.servlet.sip.testsuite.simple;

import gov.nist.javax.sip.message.MessageExt;

import java.util.HashMap;
import java.util.Map;
import javax.sip.SipProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class QuotedParameterSipServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(QuotedParameterSipServletTest.class);
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
    private static final int DIALOG_TIMEOUT = 40000;
//    private static final int TIMEOUT = 100000000;

    TestSipListener receiver;

    ProtocolObjects receiverProtocolObjects;

    public QuotedParameterSipServletTest(String name) {
        super(name);
        startTomcatOnStartup = false;
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat.deployContext(
                projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp",
                "sip-test-context", "sip-test"));
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/simple/simple-sip-servlet-dar.properties";
    }

    @Override
    @Before
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        System.setProperty("javax.net.ssl.keyStore", ShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.trustStore", ShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        super.setUp();
    }

    public void testQuotedFromHeaderParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String userName = "sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist";
        ctxAtts.put("username", userName);
        ctxAtts.put("testFromHeader", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        // qparam1 and qparam3 are being defined in sip.xml as the parameters which its value need to be quoted.
        assertTrue(((MessageExt) receiver.getInviteRequest()).getFromHeader().toString().contains("param0=value0;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getFromHeader().toString().contains(";qparam1=\"value1\";"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getFromHeader().toString().contains(";param2=value2;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getFromHeader().toString().contains(";qparam3=\"value3\""));
    }

    public void testQuotedToHeaderParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String userName = "sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist";
        ctxAtts.put("username", userName);
        ctxAtts.put("testToHeader", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        // qparam1 and qparam3 are being defined in sip.xml as the parameters which its value need to be quoted.
        assertTrue(((MessageExt) receiver.getInviteRequest()).getToHeader().toString().contains("param0=value0;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getToHeader().toString().contains(";qparam1=\"value1\";"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getToHeader().toString().contains(";param2=value2;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getToHeader().toString().contains(";qparam3=\"value3\""));
    }

    public void testQuotedContactHeaderParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String userName = "sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist";
        ctxAtts.put("username", userName);
        ctxAtts.put("testContactHeader", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        // qparam1 and qparam3 are being defined in sip.xml as the parameters which its value need to be quoted.
        assertTrue(((MessageExt) receiver.getInviteRequest()).getHeader("Contact").toString().contains("param0=value0;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getHeader("Contact").toString().contains(";qparam1=\"value1\";"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getHeader("Contact").toString().contains(";param2=value2;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getHeader("Contact").toString().contains(";qparam3=\"value3\""));
    }

    public void testQuotedViaHeaderParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String userName = "sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist";
        ctxAtts.put("username", userName);
        ctxAtts.put("testViaHeader", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        // qparam1 and qparam3 are being defined in sip.xml as the parameters which its value need to be quoted.
        assertTrue(((MessageExt) receiver.getInviteRequest()).getTopmostViaHeader().toString().contains("param0=value0;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getTopmostViaHeader().toString().contains(";qparam1=\"value1\";"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getTopmostViaHeader().toString().contains(";param2=value2;"));
        assertTrue(((MessageExt) receiver.getInviteRequest()).getTopmostViaHeader().toString().contains(";qparam3=\"value3\""));
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }
}
