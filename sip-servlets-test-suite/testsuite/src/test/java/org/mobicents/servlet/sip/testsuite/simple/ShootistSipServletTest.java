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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ReasonHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootistSipServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootistSipServletTest.class);
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
    private static final int DIALOG_TIMEOUT = 40000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener receiver;

    ProtocolObjects receiverProtocolObjects;

    public ShootistSipServletTest(String name) {
        super(name);
        startTomcatOnStartup = false;
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
        ctx = tomcat.deployAppContext(
                projectHome + "/sip-servlets-test-suite/applications/shootist-sip-servlet/src/main/sipapp",
                "sip-test-context", "sip-test");
        assertTrue(ctx.getAvailable());
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
    }

    @Override
    @Before
    protected void setUp() throws Exception {

        System.setProperty("javax.net.ssl.keyStore", ShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.trustStore", ShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();
    }

    public void testShootist() throws Exception {

        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        // Non regression test for http://code.google.com/p/sipservlets/issues/detail?id=31
        assertNotNull(((ViaHeader) receiver.getInviteRequest().getHeader(ViaHeader.NAME)).getParameter("rport"));
        assertNotNull(((ViaHeader) receiver.getByeRequestReceived().getHeader(ViaHeader.NAME)).getParameter("rport"));
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.INVITE));
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.ACK));
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.BYE));
        assertEquals(2, tomcat.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode("2XX"));
    }
    // Also Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693

    public void testShootistCancel() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("cancel", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setWaitForCancel(true);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertTrue(allMessagesContent.size() >= 2);
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.INVITE));
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getRequestsSentByMethod(Request.CANCEL));
        assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode("1XX") > 0);
        assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode("2XX") > 0);
        assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getResponsesProcessedByStatusCode("4XX") > 0);
    }

    public void testShootistCancelServletTimerCancelConcurrency() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("cancel", "true");
        ctxAtts.put("servletTimer", "500");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setWaitForCancel(true);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();

        deployShootist(ctxAtts, ConcurrencyControlMode.SipApplicationSession);

        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(2, allMessagesContent.size());
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
    }

    /*
	 * Non regression test for Issue http://code.google.com/p/mobicents/issues/detail?id=2450
     */
    public void testShootistCancelServletTimerConcurrency() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("cancel", "true");
        ctxAtts.put("servletTimer", "0");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setWaitForCancel(true);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();

        deployShootist(ctxAtts, ConcurrencyControlMode.SipApplicationSession);

        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(2, allMessagesContent.size());
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
    }

    public void testShootistEarlyMediaChange() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        LinkedList<Integer> responses = new LinkedList<Integer>();
        responses.add(180);
        responses.add(183);
        responses.add(183);
        responses.add(183);
        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("cancel", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);

        receiver.setProvisionalResponsesToSend(responses);
        receiver.setWaitForCancel(true);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        assertTrue(receiver.isCancelReceived());
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        
        String earlyMedia = receiver.getMessageRequest().getHeader("EarlyMediaResponses").toString();
        assertTrue("earlyMediaIs3", earlyMedia.contains("EarlyMediaResponses: 3"));
        assertTrue(allMessagesContent.size() >= 2);
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
    }

    public void testShootistSetContact() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue((receiver.getInviteRequest().getHeader("Contact").toString().contains("uriparam=urivalue")));
        assertTrue((receiver.getInviteRequest().getHeader("Contact").toString().contains("headerparam1=headervalue1")));
        assertTrue(receiver.getByeReceived());
    }

    /**
     * non regression test for Issue 676
     * http://code.google.com/p/mobicents/issues/detail?id=676 Tags not removed
     * when using SipFactory.createRequest()
     */
    public void testShootistSetToTag() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("toTag", "callernwPort1241042500479");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    /**
     * non regression test for Issue 732
     * http://code.google.com/p/mobicents/issues/detail?id=732 duplicate
     * parameters when using sipFactory.createAddress(uri) with a uri having
     * parameters
     */
    public void testShootistSetToParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("toParam", "http://yaris.research.att.com:23280/vxml/test.jsp");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    /**
     * non regression test for Issue 1105
     * http://code.google.com/p/mobicents/issues/detail?id=1105
     * sipFactory.createRequest(sipApplicationSession, "METHOD", fromString,
     * toString) function does not handle URI parameters properly
     */
    public void testShootistSetToWithParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String userName = "sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist";
        ctxAtts.put("username", userName);
        ctxAtts.put("useStringFactory", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        ToHeader toHeader = (ToHeader) receiver.getInviteRequest().getHeader(ToHeader.NAME);
        assertEquals("To: <sip:+34666666666@" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080;pres-list=mylist>", toHeader.toString().trim());
        assertTrue(receiver.getByeReceived());
    }

    /**
     * non regression test for Issue 145
     * http://code.google.com/p/sipservlets/issues/detail?id=145
     */
    public void testShootistUserNameNull() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String userName = "nullTest";
        ctxAtts.put("username", userName);
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        FromHeader fromHeader = (FromHeader) receiver.getInviteRequest().getHeader(FromHeader.NAME);
        assertEquals("sip:here.com", fromHeader.getAddress().getURI().toString().trim());
        assertTrue(receiver.getByeReceived());
    }

    /**
     * non regression test for Issue 755
     * http://code.google.com/p/mobicents/issues/detail?id=755 SipURI parameters
     * not escaped
     */
    public void testShootistSetEscapedParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        String toParamValue = "http://yaris.research.att.com:23280/vxml/test.jsp?toto=tata";
        ctxAtts.put("toParam", toParamValue);
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();

        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        ToHeader toHeader = (ToHeader) receiver.getInviteRequest().getHeader(ToHeader.NAME);
        String toParam = ((SipURI) toHeader.getAddress().getURI()).getParameter("toParam");
        logger.info(toParam);
        assertEquals(toParamValue, RFC2396UrlDecoder.decode(toParam));
    }

    /**
     * non regression test for Issue 859
     * http://code.google.com/p/mobicents/issues/detail?id=859 JAIN SIP ACK
     * Creation not interoperable with Microsoft OCS
     */
    public void testJainSipAckCreationViaParams() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setTestAckViaParam(true);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    /**
     * non regression test for Issue 1025
     * http://code.google.com/p/mobicents/issues/detail?id=1025
     * sipservletlistner called twice on redeploy
     */
    public void testShootistSipServletListener() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testServletListener", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        SipStandardContext context = deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        tomcat.undeployContext(context);
        Thread.sleep(TIMEOUT);
        context = deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertEquals(2, receiver.getAllMessagesContent().size());
        context.reload();
        Thread.sleep(TIMEOUT);
        assertEquals(3, receiver.getAllMessagesContent().size());
    }

    /**
     * non regression test for Issue 1090
     * http://code.google.com/p/mobicents/issues/detail?id=1090 Content-Type is
     * not mandatory if Content-Length is 0
     */
    public void testShootistContentLength() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testContentLength", "testContentLength");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getMessageRequest());
    }

    public void testShootistCallerSendsBye() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, true);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getOkToByeReceived());
    }

    public void testShootistUserAgentHeader() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.removeConnector(sipConnector);
        tomcat.startTomcat();
        tomcat.stopTomcat();

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
        sipStackProperties.setProperty("org.mobicents.servlet.sip.USER_AGENT_HEADER",
                "MobicentsSipServletsUserAgent");

        tomcat = new SipEmbedded(serverName, serviceFullClassName);
        tomcat.setLoggingFilePath(
                projectHome + File.separatorChar + "sip-servlets-test-suite"
                + File.separatorChar + "testsuite"
                + File.separatorChar + "src"
                + File.separatorChar + "test"
                + File.separatorChar + "resources" + File.separatorChar);
        logger.info("Log4j path is : " + tomcat.getLoggingFilePath());
        String darConfigurationFile = getDarConfigurationFile();
        tomcat.setDarConfigurationFilePath(darConfigurationFile);
        if (initTomcatOnStartup) {
            tomcat.initTomcat(tomcatBasePath, sipStackProperties);
            tomcat.addHttpConnector(httpIpAddress, httpContainerPort);
            if (addSipConnectorOnStartup) {
                sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, containerPort, listeningPointTransport);
            }
        }

        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        Request invite = receiver.getInviteRequest();
        UserAgentHeader userAgentHeader = (UserAgentHeader) invite.getHeader(UserAgentHeader.NAME);
        assertNotNull(userAgentHeader);
        assertTrue(userAgentHeader.toString().contains("MobicentsSipServletsUserAgent"));
    }

    /**
     * non regression test for Issue 1150
     * http://code.google.com/p/mobicents/issues/detail?id=1150 Contact header
     * contains "transport" parameter even when there are two connectors (UDP
     * and TCP)
     */
    public void testShootistContactTransport() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);
        assertFalse(((SipURI) contactHeader.getAddress().getURI()).toString().contains("transport=udp"));
        String contact = contactHeader.getAddress().toString();
        assertTrue(contact.contains("BigGuy@"));
        assertTrue(contact.contains("from display"));
    }

    public void testShootistContactTlsTransport() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("secureRURI", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        int tcpPort = NetworkPortAssigner.retrieveNextPort();
        int tlsPort = NetworkPortAssigner.retrieveNextPort();
        String tlsPortStr = String.valueOf(tlsPort);
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, tcpPort, ListeningPoint.TCP);
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, tlsPort, ListeningPoint.TLS);
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().contains("sips:"));
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().contains(tlsPortStr));
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
        String viaString = receiver.getInviteRequest().getHeader(ViaHeader.NAME).toString();
        assertTrue(viaString.toLowerCase().contains("tls"));
        assertTrue(viaString.toLowerCase().contains(tlsPortStr));
    }

    /**
     * non regression test for Issue 2269
     * http://code.google.com/p/mobicents/issues/detail?id=2269 Wrong Contact
     * header scheme URI in case TLS call with 'sip:' scheme
     */
    public void testShootistContactNonSecureURITlsTransport() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("transportRURI", "tls");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        int tcpPort = NetworkPortAssigner.retrieveNextPort();
        int tlsPort = NetworkPortAssigner.retrieveNextPort();
        String tlsPortStr = String.valueOf(tlsPort);
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, tcpPort, ListeningPoint.TCP);
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, tlsPort, ListeningPoint.TLS);
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().contains("sip:"));
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().contains(tlsPortStr));
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
        String viaString = receiver.getInviteRequest().getHeader(ViaHeader.NAME).toString();
        assertTrue(viaString.toLowerCase().contains("tls"));
        assertTrue(viaString.toLowerCase().contains(tlsPortStr));
    }

    /**
     * non regression test for Issue 1150
     * http://code.google.com/p/mobicents/issues/detail?id=1150 Contact header
     * contains "transport" parameter even when there are two connectors (UDP
     * and TCP)
     */
    public void testShootistOutboundInterfaceTransport() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", ListeningPoint.TCP, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("outboundInterface", "tcp");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        int tcpPort = NetworkPortAssigner.retrieveNextPort();
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, tcpPort, ListeningPoint.TCP);
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);

        assertTrue(receiver.getByeReceived());
        ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);
        assertFalse(((SipURI) contactHeader.getAddress().getURI()).toString().contains("transport=udp"));
    }

    /**
     * non regression test for
     * https://github.com/Mobicents/sip-servlets/issues/51 Contact header with
     * gruu is added to INVITE request
     */
    public void testShootistInviteGruu() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("method", "INVITE");
        ctxAtts.put("testGruu", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertEquals("Contact: <sip:caller@example.com;gruu;opaque=hdg7777ad7aflzig8sf7>",
                contactHeader.toString().trim());
    }

    /**
     * non regression test for
     * http://code.google.com/p/mobicents/issues/detail?id=2288
     * SipServletRequest.send() throws IllegalStateException instead of
     * IOException
     */
    public void testShootistIOException() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testIOException", "djsklfhdfkdfhdk.com");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(1, allMessagesContent.size());
        assertTrue("IOException not thrown", allMessagesContent.contains("IOException thrown"));
    }

    /**
     * non regression test for
     * http://code.google.com/p/mobicents/issues/detail?id=2288
     * SipServletRequest.send() throws IllegalStateException instead of
     * IOException
     */
    public void testShootistIOExceptionTransportChange() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("transportRURI", "tcp");
        ctxAtts.put("testIOException", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.addSipConnector(serverName, sipIpAddress, containerPort, ListeningPoint.TCP);
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(1, allMessagesContent.size());
        assertTrue("IOException not thrown", allMessagesContent.contains("IOException thrown"));
    }

    /**
     * non regression test for Issue 172
     * http://code.google.com/p/sipservlets/issues/detail?id=172 Possible to add
     * params to Contact header
     */
    @Test
    public void testShootistOptionsSetContact() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("setRandomContact", "true");
        ctxAtts.put("method", "OPTIONS");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();

        receiverProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        ContactHeader contactHeader = (ContactHeader) receiver.getOptionsRequest().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertNotNull(contactHeader.getParameter("optionParam"));
        assertEquals(contactHeader.getParameter("optionParam"), "optionValue");
        assertEquals(((SipURI) contactHeader.getAddress().getURI()).getUser(), "optionUser");
    }

    /**
     * non regression test for Github Issue 48
     * https://github.com/Mobicents/sip-servlets/issues/48 Possible to add
     * params to Contact header for Message
     */
    @Test
    public void testShootistMessageSetContact() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("setRandomContact", "true");
        ctxAtts.put("method", "MESSAGE");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();

        receiverProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        ContactHeader contactHeader = (ContactHeader) receiver.getMessageRequest().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertNotNull(contactHeader.getParameter("optionParam"));
        assertEquals(contactHeader.getParameter("optionParam"), "optionValue");
        assertEquals(((SipURI) contactHeader.getAddress().getURI()).getUser(), "optionUser");
    }

    /**
     * non regression test for Issue 1547
     * http://code.google.com/p/mobicents/issues/detail?id=1547 Can't add a
     * Proxy-Authorization using SipServletMessage.addHeader
     */
    public void testShootistProxyAuthorization() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"");
        ctxAtts.put("headerToAdd", "Proxy-Authorization");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getInviteRequest().getHeader(ProxyAuthorizationHeader.NAME));
        assertNotNull(receiver.getInviteRequest().getHeader(ProxyAuthenticateHeader.NAME));
    }

    /**
     * non regression test for Issue 2798
     * http://code.google.com/p/mobicents/issues/detail?id=2798 Can't add an
     * Authorization using SipServletMessage.addHeader
     */
    public void testShootistAuthorization() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"");
        ctxAtts.put("headerToAdd", "Authorization");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertNotNull(receiver.getInviteRequest().getHeader(AuthorizationHeader.NAME));
    }

    // Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693
    public void testShootistErrorResponse() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testErrorResponse", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
        receiver.setFinalResponseToSend(Response.SERVER_INTERNAL_ERROR);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(2, allMessagesContent.size());
        assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
        assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));
    }

    // Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693
    public void testShootistOptionsTimeout() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testErrorResponse", "true");
        ctxAtts.put("method", "OPTIONS");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
        receiver.setWaitForCancel(true);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(DIALOG_TIMEOUT + TIMEOUT);
        List<String> allMessagesContent = receiver.getAllMessagesContent();
        assertEquals(1, allMessagesContent.size());
        assertTrue("408 received", allMessagesContent.contains("408 received"));
    }

    // Tests Issue 143 http://code.google.com/p/mobicents/issues/detail?id=143
    public void testShootist422Response() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testErrorResponse", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
        receiver.setFinalResponseToSend(422);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        receiver.setFinalResponseToSend(200);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.isAckReceived());
    }

    // Test for SS spec 11.1.6 transaction timeout notification
    // Test Issue 2580 http://code.google.com/p/mobicents/issues/detail?id=2580
    public void testTransactionTimeoutResponse() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setDropRequest(true);
        List<Integer> provisionalResponsesToSend = receiver.getProvisionalResponsesToSend();
        provisionalResponsesToSend.clear();

        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();

        tomcat.startTomcat();
        deployShootist(ctxAtts, null);

        Thread.sleep(DIALOG_TIMEOUT);

        Iterator<String> allMessagesIterator = receiver.getAllMessagesContent().iterator();
        while (allMessagesIterator.hasNext()) {
            String message = (String) allMessagesIterator.next();
            logger.info(message);

        }
        assertTrue(receiver.txTimeoutReceived);
    }

    /*
	 * http://code.google.com/p/mobicents/issues/detail?id=2902
     */
    public void testShootistRemoteAddrAndPort() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testRemoteAddrAndPort", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    // https://code.google.com/p/sipservlets/issues/detail?id=169
    public void testShootistMultipartBytes() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testMultipartBytes", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
    }

    // https://code.google.com/p/sipservlets/issues/detail?id=216
    public void testShootistMutlipleReasonHeaders() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testMultipleReasonHeaders", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        receiver.setProvisionalResponsesToSend(new ArrayList<Integer>());
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        ListIterator<Header> listIt = receiver.getInviteRequest().getHeaders(ReasonHeader.NAME);
        int i = 0;
        while (listIt.hasNext()) {
            listIt.next();
            i++;
        }
        assertEquals(2, i);
    }

    /*
     * https://code.google.com/p/sipservlets/issues/detail?id=245
     */
    public void testShootistAddressParam() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("testAddressParam", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider receiverProvider = receiver.createProvider();
        receiverProvider.addSipListener(receiver);
        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertTrue(receiver.getByeReceived());
        assertEquals("00112233", ((MessageExt) receiver.getInviteRequest()).getFromHeader().getParameter("epid"));
        assertEquals("33221100", ((MessageExt) receiver.getInviteRequest()).getToHeader().getParameter("epid"));
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }
}
