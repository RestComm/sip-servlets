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
package org.mobicents.servlet.sip.testsuite.composition;

import gov.nist.javax.sip.header.ims.PPreferredServiceHeader;
import java.util.HashMap;

import java.util.ListIterator;
import java.util.Map;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.Header;

import javax.sip.header.RecordRouteHeader;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class SpeedDial_LocationServiceTwice_JunitTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(SpeedDial_LocationServiceTwice_JunitTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener sender;
    TestSipListener receiver;
    TestSipListener receiver2;
    ProtocolObjects senderProtocolObjects;
    ProtocolObjects receiverProtocolObjects;
    ProtocolObjects receiver2ProtocolObjects;

    public SpeedDial_LocationServiceTwice_JunitTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    protected void deployApplication() {

    }

    public void deployBothApplication(Map<String, String> params) {
        deploySpeedDial(params);//, "" + isSpeedDialRecordRoute);
        deployLocationService(params);
    }

    private void deploySpeedDial(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/speed-dial-servlet/src/main/sipapp",
                "speed-dial",
                params, null);
        assertTrue(ctx.getAvailable());
    }

    private void deployLocationService(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/location-service-servlet/src/main/sipapp",
                "location-service",
                params, null);
        assertTrue(ctx.getAvailable());
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/composition/speeddial-locationservice-twice-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();

        senderProtocolObjects = new ProtocolObjects("sender",
                "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        receiverProtocolObjects = new ProtocolObjects("receiver",
                "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        receiver2ProtocolObjects = new ProtocolObjects("receiver2",
                "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
    }

    /*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=273
     */
    public void testSpeedDialLocationServicePRACKCalleeSendBye() throws Exception {
        int senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
        sender.setRecordRoutingProxyTesting(true);
        SipProvider senderProvider = sender.createProvider();

        int receiverPort = NetworkPortAssigner.retrieveNextPort();
        receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, true);
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
        params.put("record_route", "true");        
        deployBothApplication(params);        

        String fromName = "sender";
        String fromHost = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromHost);

        String toUser = "7";
        String toHost = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toHost);

        String[] headerNames = new String[]{"require", "P-Preferred-Service"};
        String[] headerValues = new String[]{"100rel", "urn:urn-7:3gpp-service.ims.icsi.mmtel.gsma.ipcall"};
        String sdpContent = "\n"
                + "v=0\n"
                + "o=- 1 1 IN IP4 127.0.0.1\n"
                + "s=helium\n"
                + "c=IN IP4 127.0.0.1\n"
                + "t=0 0\n"
                + "a=sendrecv\n"
                + "m=audio 30010 RTP/AVP 8\n"
                + "a=rtpmap:8 PCMA/8000";

        sender.sendSipRequest("INVITE", fromAddress, toAddress, sdpContent, null, false, headerNames, headerValues, true);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getOkToByeReceived());
        assertTrue(sender.getByeReceived());
        assertNotNull(receiver.getInviteRequest().getHeader(PPreferredServiceHeader.NAME));
        System.out.println("received request content " + receiver.getInviteRequest().getContent());
        assertEquals(sdpContent, new String(receiver.getInviteRequest().getRawContent(), "UTF-8"));

        int numberOfPrack = 0;
        ListIterator<Header> listHeaderIt = receiver.getPrackRequestReceived().getHeaders("X-Seen");
        while (listHeaderIt.hasNext()) {
            listHeaderIt.next();
            numberOfPrack++;
        }
        assertEquals(2, numberOfPrack);
    }

    /*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=274
     */
    public void testSpeedDialLocationServiceRecordRouteReInviteCallerSendBye() throws Exception {
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
        params.put("record_route", "true");        
        deployBothApplication(params);           

        String fromName = "sender";
        String fromHost = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromHost);

        String toUser = "7";
        String toHost = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toHost);

        sender.setSendReinvite(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isInviteReceived());
        sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        assertTrue(sender.getOkToByeReceived());
        int numberOfRRouteHeaders = 0;
        ListIterator<Header> listHeaderIt = receiver.getInviteRequest().getHeaders(RecordRouteHeader.NAME);
        while (listHeaderIt.hasNext()) {
            listHeaderIt.next();
            numberOfRRouteHeaders++;
        }
        assertEquals(3, numberOfRRouteHeaders);
    }

    /*
	 * Non regression test for https://code.google.com/p/sipservlets/issues/detail?id=275
     */
    public void testSpeedDialLocationServiceRecordRouteReInviteCalleeSendReInvite() throws Exception {
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
        params.put("record_route", "true");        
        deployBothApplication(params);           

        String fromName = "sender";
        String fromHost = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromHost);

        String toUser = "7";
        String toHost = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toHost);

        receiver.setSendReinvite(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isInviteReceived());
        URI requestUri = sender.getInviteRequest().getRequestURI();
        assertEquals("sip:sender@127.0.0.1:" + senderPort + ";transport=udp;lr", requestUri.toString().trim());
        sender.sendInDialogSipRequest("BYE", null, null, null, null, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        assertTrue(sender.getOkToByeReceived());
        int numberOfRRouteHeaders = 0;
        ListIterator<Header> listHeaderIt = receiver.getInviteRequest().getHeaders(RecordRouteHeader.NAME);
        while (listHeaderIt.hasNext()) {
            listHeaderIt.next();
            numberOfRRouteHeaders++;
        }
        assertEquals(3, numberOfRRouteHeaders);
    }

    /*
     * Non Regression test for https://code.google.com/p/sipservlets/issues/detail?id=164
     */
    public void testCancelProxying() throws Exception {
        int senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, false);
        sender.setRecordRoutingProxyTesting(true);
        SipProvider senderProvider = sender.createProvider();

        int receiverPort = NetworkPortAssigner.retrieveNextPort();
        receiver = new TestSipListener(receiverPort, containerPort, receiverProtocolObjects, false);
        receiver.setRecordRoutingProxyTesting(true);
        SipProvider receiverProvider = receiver.createProvider();

        int receiver2Port = NetworkPortAssigner.retrieveNextPort();
        receiver2 = new TestSipListener(receiver2Port, containerPort, receiver2ProtocolObjects, false);
        receiver2.setRecordRoutingProxyTesting(true);
        SipProvider receiver2Provider = receiver2.createProvider();

        receiverProvider.addSipListener(receiver);
        receiver2Provider.addSipListener(receiver2);
        senderProvider.addSipListener(sender);

        senderProtocolObjects.start();
        receiverProtocolObjects.start();
        receiver2ProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(receiverPort));
        params.put("senderPort", String.valueOf(senderPort));
        params.put("receiver2Port", String.valueOf(receiver2Port));
        params.put("record_route", "false");        
        deployBothApplication(params);           

        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toSipAddress = "sip-servlets.com";
        String toUser = "8";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        receiver2.setWaitForCancel(true);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver2.isCancelReceived());
        assertTrue(sender.isFinalResponseReceived());
        // https://code.google.com/p/sipservlets/issues/detail?id=272
    }

    @Override
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

}
