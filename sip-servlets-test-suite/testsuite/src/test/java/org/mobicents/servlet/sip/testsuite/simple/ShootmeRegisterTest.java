package org.mobicents.servlet.sip.testsuite.simple;

import java.util.HashMap;
import java.util.Map;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
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

/**
 *
 * @author jaime
 */
public class ShootmeRegisterTest extends SipServletTestCase {

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

    public ShootmeRegisterTest(String name) {
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

    public void testShootmeRegister() throws Exception {
        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        sender.sendSipRequest("REGISTER", fromAddress, fromAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isFinalResponseReceived());
        assertEquals(200, sender.getFinalResponseStatus());
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getRequestsProcessedByMethod(Request.REGISTER));
        assertEquals(1, tomcat.getSipService().getSipApplicationDispatcher().getResponsesSentByStatusCode("2XX"));
    }

    /**
     * non regression test for Issue 172
     * http://code.google.com/p/sipservlets/issues/detail?id=172 Possible to add
     * contact Header to 200 OK to OPTIONS
     */
    public void testShootmeOptions() throws Exception {
        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        sender.sendSipRequest("OPTIONS", fromAddress, fromAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertTrue(sender.isFinalResponseReceived());
        assertEquals(200, sender.getFinalResponseStatus());
        ContactHeader contactHeader = (ContactHeader) sender.getFinalResponse().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertEquals(((SipURI) contactHeader.getAddress().getURI()).toString(), "sip:random@172.172.172.172:3289");
    }

    public void testShootmeRegisterNoContact() throws Exception {
        String fromName = "testRegisterNoContact";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertNull(registerReciever.getRegisterReceived().getHeader(ContactHeader.NAME));
    }

    public void testShootmeRegisterCSeqIncrease() throws Exception {
        String fromName = "testRegisterCSeq";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);
        Thread.sleep(TIMEOUT_CSEQ_INCREASE);
        assertEquals(4, registerReciever.getLastRegisterCSeqNumber());
        assertTrue(sender.isFinalResponseReceived());
    }

    public void testShootmeRegisterCSeqIncreaseAuth() throws Exception {
        String fromName = "testRegisterCSeq";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);
        registerReciever.setChallengeRequests(true);
        sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);
        Thread.sleep(TIMEOUT_CSEQ_INCREASE);
        assertEquals(4, registerReciever.getLastRegisterCSeqNumber());
        assertTrue(sender.isFinalResponseReceived());
    }

    public void testShootmeRegisterAuthSavedSession() throws Exception {
        String fromName = "testRegisterSavedSession";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);
        registerReciever.setChallengeRequests(true);
        registerReciever.setSendBye(false);
        sender.setSendBye(false);
        sender.sendSipRequest("INVITE", fromAddress, fromAddress, null, null, false);
        Thread.sleep(TIMEOUT_CSEQ_INCREASE);
        assertTrue(registerReciever.getLastRegisterCSeqNumber() == 4);
        assertTrue(sender.isFinalResponseReceived());
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
