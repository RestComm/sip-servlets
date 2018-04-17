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
package org.mobicents.servlet.sip.testsuite.join;

import java.util.HashMap;
import java.util.Map;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import static junit.framework.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Test the behavior of Mobicents Sip Servlets with regard to Join (RFC 3911)
 * Support
 *
 * @author jean.deruelle@gmail.com
 *
 */
public class JoinSipServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(JoinSipServletTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 20000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener sender;
    TestSipListener receiver;
    ProtocolObjects senderProtocolObjects;
    ProtocolObjects receiverProtocolObjects;

    public JoinSipServletTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
    }

    public SipStandardContext deployApplication(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/join-sip-servlet/src/main/sipapp",
                "join-dial",
                params,
                null);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/join/join-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();

        senderProtocolObjects = new ProtocolObjects("sender",
                "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        receiverProtocolObjects = new ProtocolObjects("receiver",
                "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
    }

    public void testSipServletSendsJoin() throws Exception {
        int senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
        sender.setRecordRoutingProxyTesting(true);
        SipProvider senderProvider = sender.createProvider();

        int receiverPort = NetworkPortAssigner.retrieveNextPort();
        receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
        receiver.setRecordRoutingProxyTesting(true);
        receiver.setWaitBeforeFinalResponse(2000);
        SipProvider receiverProvider = receiver.createProvider();

        receiverProvider.addSipListener(receiver);
        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        receiverProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(receiverPort));
        params.put("senderPort", String.valueOf(senderPort));        
        deployApplication(params);        

        String fromName = "sender";
        String fromHost = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromHost);

        String toUser = "join";
        String toHost = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toHost);

        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isJoinRequestReceived());
        assertTrue(receiver.getOkToByeReceived());
        assertTrue(sender.getByeReceived());
    }

    public void testSipServletReceivesJoin() throws Exception {
        int senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
        sender.setRecordRoutingProxyTesting(true);
        SipProvider senderProvider = sender.createProvider();

        int receiverPort = NetworkPortAssigner.retrieveNextPort();
        receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
        receiver.setRecordRoutingProxyTesting(true);
        SipProvider receiverProvider = receiver.createProvider();        
       
        receiverProvider.addSipListener(receiver);
        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        receiverProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(receiverPort));
        params.put("senderPort", String.valueOf(senderPort));        
        deployApplication(params);              

        String fromName = "sender";
        String fromHost = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromHost);

        String toUser = "join-receiver";
        String toHost = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toHost);

        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getLastMessageContent());
        assertFalse(receiver.isServerErrorReceived());
        assertTrue(receiver.getByeReceived());
        assertTrue(sender.getByeReceived());
    }

    @Override
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

}
