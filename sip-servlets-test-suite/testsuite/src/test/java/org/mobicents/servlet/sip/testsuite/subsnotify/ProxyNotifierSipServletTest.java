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
 *
 * This file incorporates work covered by the following copyright contributed under the GNU LGPL : Copyright 2007-2011 Red Hat.
 */
package org.mobicents.servlet.sip.testsuite.subsnotify;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import static junit.framework.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * http://code.google.com/p/mobicents/issues/detail?id=1481 NPE during 200 OK to
 * SUBSCRIBE in proxy mode
 *
 * @author jean.deruelle@gmail.com
 */
public class ProxyNotifierSipServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ProxyNotifierSipServletTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;

    TestSipListener sender;
    ProtocolObjects senderProtocolObjects;

    TestSipListener receiver;
    ProtocolObjects receiverProtocolObjects;

    public ProxyNotifierSipServletTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
    }

    public SipStandardContext deployApplication(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                "sip-test",
                params,
                null);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/subsnotify/proxy-notifier-sip-servlet-dar.properties";
    }

    @Override
    protected void setUp() {
        try {
            containerPort = NetworkPortAssigner.retrieveNextPort();
            super.setUp();

            senderProtocolObjects = new ProtocolObjects(
                    "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
            int senderPort = NetworkPortAssigner.retrieveNextPort();
            sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
            SipProvider senderProvider = sender.createProvider();
            senderProvider.addSipListener(sender);
            senderProtocolObjects.start();

            receiverProtocolObjects = new ProtocolObjects(
                    "receiver", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
            int receiverPort = NetworkPortAssigner.retrieveNextPort();
            receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
            SipProvider receiverProvider = receiver.createProvider();
            receiverProvider.addSipListener(receiver);
            receiverProtocolObjects.start();
            
            Map<String, String> params = new HashMap();
            params.put("servletContainerPort", String.valueOf(containerPort));
            params.put("testPort", String.valueOf(senderPort));
            params.put("receiverPort", String.valueOf(receiverPort));
            deployApplication(params);              
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    }

    /*
	 * Test the fact that a sip servlet proxies a SUBSCRIBE not in dialog
     */
    public void testNotify() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "recordRoute";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        receiver.setSendNotify(false);
        sender.sendSipRequest(Request.SUBSCRIBE, fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertEquals(202, sender.getFinalResponseStatus());

    }

    /*
	 * https://code.google.com/p/sipservlets/issues/detail?id=275
     */
    public void testNotifyWithLateAnswerToSubscribe() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "recordRouting";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setRecordRoutingProxyTesting(true);
        receiver.setSendNotify(true);
        receiver.setSendNotifyBeforeResponseToSubscribe(true);
        receiver.setWaitBeforeFinalResponse(receiver.getTimeToWaitBetweenSubsNotify() + 1000);
        sender.sendSipRequest(Request.SUBSCRIBE, fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT * 2);
        assertEquals(202, sender.getFinalResponseStatus());
        assertEquals(3, sender.notifyCount);
        // https://github.com/RestComm/sip-servlets/issues/121
        for (Request request : sender.allRequests) {
            if (request.getMethod().equals("NOTIFY")) {
                assertNotNull(request.getHeader(RecordRouteHeader.NAME));
            }
        }
    }

    /*
	 * Test the fact that a sip servlet proxies a SUBSCRIBE in a dialog (INVITE Sent)
     */
    public void testNotifyInDialog() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "recordRoute";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        receiver.setSendNotify(false);
        sender.setRecordRoutingProxyTesting(true);
        sender.setSendByeBeforeTerminatingNotify(true);

        sender.sendSipRequest(Request.INVITE, fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isAckSent());
        sender.sendInDialogSipRequest(Request.SUBSCRIBE, null, null, null, null, null);
        Thread.sleep(TIMEOUT);
        assertEquals(202, sender.getFinalResponseStatus());
    }

    @Override
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

}
