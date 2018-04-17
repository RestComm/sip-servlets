package org.mobicents.servlet.sip.testsuite.simple;

import java.util.HashMap;
import java.util.Map;
import javax.sip.ListeningPoint;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

public class ShootistRegisterTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ShootistSipServletTest.class);
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
    private static final int DIALOG_TIMEOUT = 40000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener receiver;

    ProtocolObjects receiverProtocolObjects;

    public ShootistRegisterTest(String name) {
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

    /**
     * non regression test for Issue 1412
     * http://code.google.com/p/mobicents/issues/detail?id=1412 Contact header
     * is added to REGISTER request by container
     */
    public void testShootistRegister() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("method", "REGISTER");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));
    }

    /**
     * non regression test for
     * https://github.com/Mobicents/sip-servlets/issues/36 REGISTER CSeq
     * Increase for UAC use cases using session.createRequest("REGISTER")
     */
    public void testShootistRegisterCSeqIncrease() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("method", "REGISTER");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(DIALOG_TIMEOUT);
        assertNull(receiver.getRegisterReceived().getHeader(ContactHeader.NAME));
        assertTrue(receiver.getLastRegisterCSeqNumber() >= 2);
    }

    /**
     * non regression test for
     * https://github.com/Mobicents/sip-servlets/issues/51 Contact header with
     * gruu is added to REGISTER request
     */
    public void testShootistRegisterGruu() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("method", "REGISTER");
        ctxAtts.put("testGruu", "true");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        ContactHeader contactHeader = (ContactHeader) receiver.getRegisterReceived().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertEquals("Contact: <sip:caller@example.com;gruu;opaque=hdg7777ad7aflzig8sf7>",
                contactHeader.toString().trim());
    }

    /**
     * non regression test for Issue 2269
     * http://code.google.com/p/mobicents/issues/detail?id=2269 Wrong Contact
     * header scheme URI in case TLS call with request URI 'sip:' scheme and
     * contact is uri is secure with "sips"
     */
    public void testShootistRegisterContactNonSecureURITlsTransport() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("transportRURI", "tls");
        ctxAtts.put("method", "REGISTER");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5072, ListeningPoint.TLS);
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        tomcat.startTomcat();
        Thread.sleep(TIMEOUT);
        ContactHeader contactHeader = (ContactHeader) receiver.getRegisterReceived().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().contains("sip:"));
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().contains(String.valueOf(myPort)));
        assertTrue(((SipURI) contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
    }

    /**
     * non regression test for Issue 156
     * http://code.google.com/p/sipservlets/issues/detail?id=156 Contact header
     * in REGISTER overwritten by container
     */
    public void testShootistRegisterSetContact() throws Exception {
        receiverProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);

        int myPort = NetworkPortAssigner.retrieveNextPort();
        Map<String, String> ctxAtts = new HashMap();
        ctxAtts.put("testPort", String.valueOf(myPort));
        ctxAtts.put("servletContainerPort", String.valueOf(containerPort));
        ctxAtts.put("setRandomContact", "true");
        ctxAtts.put("method", "REGISTER");
        receiver = new TestSipListener(myPort, containerPort, receiverProtocolObjects, false);
        SipProvider senderProvider = receiver.createProvider();

        senderProvider.addSipListener(receiver);

        receiverProtocolObjects.start();
        tomcat.startTomcat();
        deployShootist(ctxAtts, null);
        Thread.sleep(TIMEOUT);
        ContactHeader contactHeader = (ContactHeader) receiver.getRegisterReceived().getHeader(ContactHeader.NAME);
        assertNotNull(contactHeader);
        assertEquals(((SipURI) contactHeader.getAddress().getURI()).toString(), "sip:random@172.172.172.172:3289");
    }
}
