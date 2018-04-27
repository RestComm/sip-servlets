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

import gov.nist.javax.sip.message.RequestExt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sip.SipProvider;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipEmbedded;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Non regression test for https://github.com/Mobicents/sip-servlets/issues/50
 *
 * @author jean
 *
 */
public class LyncInteropShootistSipServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(LyncInteropShootistSipServletTest.class);
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
    private static final int DIALOG_TIMEOUT = 40000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener receiver;

    ProtocolObjects receiverProtocolObjects;

    public LyncInteropShootistSipServletTest(String name) {
        super(name);
        initTomcatOnStartup = false;
        createTomcatOnStartup = false;
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
                + "org/mobicents/servlet/sip/testsuite/simple/shootist-sip-servlet-dar.properties";
    }

    @Override
    @Before
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        System.setProperty("javax.net.ssl.keyStore", LyncInteropShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.trustStore", LyncInteropShootistSipServletTest.class.getResource("testkeys").getPath());
        System.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        System.setProperty("javax.net.ssl.keyStoreType", "jks");
        super.setUp();
        createLyncCompliantTomcat();

    }

    /**
     * https://github.com/Mobicents/sip-servlets/issues/50 Test Lync Interop and
     * reduction of tag and call id sizes
     *
     * @throws Exception
     */
    protected void createLyncCompliantTomcat() throws Exception {
        tomcat = new SipEmbedded(serverName, serviceFullClassName, 32, 10);
        tomcat.setLoggingFilePath(
                projectHome + File.separatorChar + "sip-servlets-test-suite"
                + File.separatorChar + "testsuite"
                + File.separatorChar + "src"
                + File.separatorChar + "test"
                + File.separatorChar + "resources" + File.separatorChar);
        logger.info("Log4j path is : " + tomcat.getLoggingFilePath());
        String darConfigurationFile = getDarConfigurationFile();
        tomcat.setDarConfigurationFilePath(darConfigurationFile);
        Properties sipStackProperties = getSipStackProperties();
        tomcat.initTomcat(tomcatBasePath, sipStackProperties);
        tomcat.addHttpConnector(httpIpAddress, httpContainerPort);
        /*
		 * <Connector debugLog="../logs/debuglog.txt" ipAddress="0.0.0.0"
		 * logLevel="DEBUG" port="5070"
		 * protocol="org.mobicents.servlet.sip.startup.SipProtocolHandler"
		 * serverLog="../logs/serverlog.txt" signalingTransport="udp"
		 * sipPathName="gov.nist" sipStackName="SIP-Servlet-Tomcat-Server"/>
         */
        if (addSipConnectorOnStartup) {
            sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, containerPort, listeningPointTransport);
        }
        if (startTomcatOnStartup) {
            tomcat.startTomcat();
        }
        if (autoDeployOnStartup) {
            deployApplication();
        }
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
        assertEquals(32, ((RequestExt) receiver.getInviteRequest()).getCallIdHeader().getCallId().trim().length());
        assertEquals(10, ((RequestExt) receiver.getInviteRequest()).getFromHeader().getTag().trim().length());
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        receiverProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }
}
