package org.mobicents.servlet.sip.testsuite.simple;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.message.Request;
import static junit.framework.Assert.assertEquals;
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

public class Shootme491Test extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootmeSipServletTest.class);

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

    public Shootme491Test(String name) {
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
        int senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
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
        params.put("testPort", String.valueOf(senderPort));
        params.put("registerPort", String.valueOf(registerPort));
        deployApplication(params);
    }

    public void testShootme491() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "normal";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setSendBye(false);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        Thread.sleep(12000);
        assertTrue(sender.numberOf491s > 0);
        assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod(Request.INVITE) > 1);
        assertTrue(tomcat.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode("4XX") > 0);
    }

    public void testShootme491withRetrans() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "normal";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setSendBye(false);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(2500);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        Thread.sleep(6000);
        int current = sender.numberOf491s;
        System.out.println("number of 491 " + current);
        assertTrue(sender.numberOf491s > 2);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        Thread.sleep(1000);
        assertEquals(current, sender.numberOf491s);
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        registerRecieverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

}
