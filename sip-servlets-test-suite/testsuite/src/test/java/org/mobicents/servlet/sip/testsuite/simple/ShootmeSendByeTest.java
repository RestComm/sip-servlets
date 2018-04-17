package org.mobicents.servlet.sip.testsuite.simple;

import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.AllowHeader;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ServerHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootmeSendByeTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootmeSipServletTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000;
    private static final int TIMEOUT_CSEQ_INCREASE = 100000;
    private static final int DIALOG_TIMEOUT = 40000;

    public final static String[] ALLOW_HEADERS = new String[]{"INVITE", "ACK", "CANCEL", "OPTIONS", "BYE", "SUBSCRIBE", "NOTIFY", "REFER"};

    TestSipListener sender;
    SipProvider senderProvider = null;
    TestSipListener registerReciever;
    SipContext sipContext;

    ProtocolObjects senderProtocolObjects;
    ProtocolObjects registerRecieverProtocolObjects;

    public ShootmeSendByeTest(String name) {
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


    public void testShootmeSendByeOnExpire() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "byeOnExpire";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setSendBye(false);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(5000);
        sender.sendInDialogSipRequest("INVITE", null, null, null, null, null);
        Thread.sleep(70000);
        assertTrue(sender.isAckSent());
        assertTrue(sender.getByeReceived());
    }


    public void testShootmeSendBye() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "SSsendBye";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setSendBye(false);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT * 2);
        assertTrue(sender.isAckSent());
        assertTrue(sender.getByeReceived());
        Thread.sleep(TIMEOUT * 4);
    }

    /*
	 * https://code.google.com/p/sipservlets/issues/detail?id=194
     */
    public void testShootmeSendBye407Terminated() throws InterruptedException, SipException, ParseException, InvalidArgumentException {
        String fromName = "SSsendBye";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.setByeResponse(407);
        sender.setSendBye(false);
        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT * 2);
        assertTrue(sender.isAckSent());
        assertTrue(sender.getByeReceived());
        Thread.sleep(TIMEOUT);
        List<String> allMessagesContent = sender.getAllMessagesContent();
        assertEquals(1, allMessagesContent.size());
        assertEquals("CONFIRMED", allMessagesContent.get(0));
    }


    @Override
    @After
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        registerRecieverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
        Thread.sleep(4000);
//		FullThreadDump fullThreadDump = new FullThreadDump("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "", 1090);
//        fullThreadDump.dump();
    }

}

