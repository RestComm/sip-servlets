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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ServerHeader;
import javax.sip.message.Response;
import static junit.framework.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootmeServerHeaderTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootmeServerHeaderTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;

    public final static String[] ALLOW_HEADERS = new String[]{"INVITE", "ACK", "CANCEL", "OPTIONS", "BYE", "SUBSCRIBE", "NOTIFY", "REFER"};

    TestSipListener sender;
    SipProvider senderProvider = null;
    TestSipListener registerReciever;
    SipContext sipContext;

    ProtocolObjects senderProtocolObjects;
    ProtocolObjects registerRecieverProtocolObjects;

    public ShootmeServerHeaderTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
    }

    public void deployApplication(Map<String, String> params) {
        SipStandardContext context = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
                "sip-test",
                params,
                null,
                1,
                null);
        assertTrue(context.getAvailable());
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
        super.setUp();

        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        senderProvider = sender.createProvider();
        senderProvider.addSipListener(sender);
        senderProtocolObjects.start();

        registerRecieverProtocolObjects = new ProtocolObjects(
                "registerReciever", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        int registerPort = NetworkPortAssigner.retrieveNextPort();
        registerReciever = new TestSipListener(registerPort, containerPort, registerRecieverProtocolObjects, true);
        SipProvider registerRecieverProvider = registerReciever.createProvider();
        registerRecieverProvider.addSipListener(registerReciever);
        registerRecieverProtocolObjects.start();

        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(myPort));
        params.put("registerPort", String.valueOf(registerPort));
        deployApplication(params);
    }

    @Override
    protected Properties getSipStackProperties() {
        Properties sipStackProperties = new Properties();
        sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-"
                + sipIpAddress + "-" + containerPort);
        sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
                "off");
        sipStackProperties.setProperty(
                "gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE",
                "64");
        sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",
                "true");
        sipStackProperties.setProperty("org.mobicents.servlet.sip.SERVER_HEADER",
                "MobicentsSipServletsServer");
        return sipStackProperties;
    }

    public void testShootmeServerHeader() throws Exception {
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
        assertTrue(sender.getOkToByeReceived());

        Response finalResponse = sender.getFinalResponse();
        ServerHeader serverHeader = (ServerHeader) finalResponse.getHeader(ServerHeader.NAME);
        assertNotNull(serverHeader);
        assertTrue(serverHeader.toString().contains("MobicentsSipServletsServer"));
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        registerRecieverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
        Thread.sleep(4000);
    }

}
