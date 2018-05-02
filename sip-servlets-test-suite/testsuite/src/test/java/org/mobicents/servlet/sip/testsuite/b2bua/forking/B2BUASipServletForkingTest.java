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
package org.mobicents.servlet.sip.testsuite.b2bua.forking;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.sip.SipProvider;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.proxy.Shootist;
import org.mobicents.servlet.sip.testsuite.simple.forking.Proxy;
import org.mobicents.servlet.sip.testsuite.simple.forking.Shootme;

public class B2BUASipServletForkingTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(B2BUASipServletForkingTest.class);
    private static final int TIMEOUT = 40000;
//	private static final int TIMEOUT = 100000000;

    public B2BUASipServletForkingTest(String name) {
        super(name);
        startTomcatOnStartup = false;
        autoDeployOnStartup = false;
        addSipConnectorOnStartup = false;
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat.deployContext(
                projectHome + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                "sip-test-context", "sip-test"));
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/callcontroller/call-forwarding-b2bua-servlet-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();
    }

    // non regression test for Issue https://telestax.atlassian.net/browse/RES-4
    public void testB2BUAForkingCrossed180() throws Exception {

        // ShootMe-1                        : 5091
        // ShootMe-2                        : 5092
        // Proxy (forks to Shootme clients) : 5090
        // Container running B2BUA app      : 5060
        // Shootist (initiates the call)    : 5089


        int shootme1Port = NetworkPortAssigner.retrieveNextPort();
        //force ringing to come after shootme2,but 200ok before shootme 2
        Shootme shootme1 = new Shootme(shootme1Port, true, 1000,1500);
        SipProvider shootmeProvider = shootme1.createProvider();
        shootmeProvider.addSipListener(shootme1);

        int shootme2Port = NetworkPortAssigner.retrieveNextPort();
        //send 180 inmediately,but 200 ok after shootme1
        Shootme shootme2 = new Shootme(shootme2Port, true, 2500);
        SipProvider shootme2Provider = shootme2.createProvider();
        shootme2Provider.addSipListener(shootme2);

        int proxyPort = NetworkPortAssigner.retrieveNextPort();
        Proxy proxy = new Proxy(proxyPort, new int[]{shootme1Port, shootme2Port});
        SipProvider provider = proxy.createSipProvider();
        provider.addSipListener(proxy);

        int shootistPort = NetworkPortAssigner.retrieveNextPort();
        int listeningPort = NetworkPortAssigner.retrieveNextPort();
        Shootist shootist = new Shootist(true, shootistPort, String.valueOf(listeningPort));
        shootist.pauseBeforeBye = 20000;
        shootist.setFromHost("sip-servlets.com");

        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, listeningPort, listeningPointTransport);
        tomcat.startTomcat();
        Map<String, String> params = new HashMap<String, String>();
        params.put("timeToWaitForBye", "20000");
        params.put("dontSetRURI", "true");
        params.put("servletContainerPort", String.valueOf(proxyPort));
        params.put("testPort", String.valueOf(shootme1Port));
        params.put("senderPort", String.valueOf(shootistPort));
        SipStandardContext sipContext = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                params, null);

        shootist.init("forward-sender-forking-pending", false, null);
        Thread.sleep(TIMEOUT);
        proxy.stop();
        shootme1.stop();
        shootme2.stop();
        shootist.stop();
        assertTrue(shootme1.isAckSeen());
        assertTrue(shootme1.checkBye());
        assertTrue(shootme2.isAckSeen());
        assertTrue(shootme2.checkBye());

        Iterator sipSessionsIter = sipContext.getSipManager().getAllSipSessions();
        while (sipSessionsIter.hasNext()) {
            MobicentsSipSession sipSession = (MobicentsSipSession) sipSessionsIter.next();
            String msg = String.format("SipSession in memory, key [%s], state [%s], hasParent [%s], hasDerivedSessions [%s]", sipSession.getKey(), sipSession.getState(), sipSession.getParentSession() != null, sipSession.getDerivedSipSessions().hasNext());
            logger.error(msg);
            Iterator<MobicentsSipSession> iter = sipSession.getDerivedSipSessions();
            while (iter.hasNext()) {
                MobicentsSipSession derivedSipSession = iter.next();
                msg = String.format("Derived sip session [%s], state [%s}",derivedSipSession.getKey(), derivedSipSession.getState());
                logger.error(msg);
            }
        }

        assertEquals(0, sipContext.getSipManager().getActiveSipSessions());
        assertEquals(0, sipContext.getSipManager().getActiveSipApplicationSessions());

    }

    // non regression test for Issue 2354 http://code.google.com/p/mobicents/issues/detail?id=2354
    public void testB2BUAForking() throws Exception {
        int shootme1Port = NetworkPortAssigner.retrieveNextPort();
        //force shootme1 to send 180 first
        Shootme shootme1 = new Shootme(shootme1Port, true,100, 1500);
        SipProvider shootmeProvider = shootme1.createProvider();
        shootmeProvider.addSipListener(shootme1);
        int shootme2Port = NetworkPortAssigner.retrieveNextPort();
        //force shootme2 to send 180 second
        Shootme shootme2 = new Shootme(shootme2Port, true, 300, 2500);
        SipProvider shootme2Provider = shootme2.createProvider();
        shootme2Provider.addSipListener(shootme2);
        int proxyPort = NetworkPortAssigner.retrieveNextPort();
        Proxy proxy = new Proxy(proxyPort, new int[]{shootme1Port, shootme2Port});
        SipProvider provider = proxy.createSipProvider();
        provider.addSipListener(proxy);
        int shootistPort = NetworkPortAssigner.retrieveNextPort();
        int listeningPort = NetworkPortAssigner.retrieveNextPort();
        Shootist shootist = new Shootist(true, shootistPort, String.valueOf(listeningPort));
        shootist.pauseBeforeBye = 20000;
        shootist.setFromHost("sip-servlets.com");

        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, listeningPort, listeningPointTransport);
        tomcat.startTomcat();
        Map<String, String> params = new HashMap<String, String>();
        params.put("timeToWaitForBye", "20000");
        params.put("dontSetRURI", "true");
        params.put("servletContainerPort", String.valueOf(proxyPort));
        params.put("testPort", String.valueOf(shootme1Port));
        params.put("senderPort", String.valueOf(shootistPort));
        SipStandardContext sipContext = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                params, null);

        shootist.init("forward-sender-forking-pending", false, null);
        Thread.sleep(TIMEOUT);
        proxy.stop();
        shootme1.stop();
        shootme2.stop();
        shootist.stop();
        assertTrue(shootme1.isAckSeen());
        assertTrue(shootme1.checkBye());
        assertTrue(shootme2.isAckSeen());
        assertTrue(shootme2.checkBye());
        assertEquals(0, sipContext.getSipManager().getActiveSipSessions());
        assertEquals(0, sipContext.getSipManager().getActiveSipApplicationSessions());

    }

    public void testB2BUAForkingWithCANCEL() throws Exception {
        int shootme1Port = NetworkPortAssigner.retrieveNextPort();
        Shootme shootme1 = new Shootme(shootme1Port, true, 2500);
        SipProvider shootmeProvider = shootme1.createProvider();
        shootmeProvider.addSipListener(shootme1);
        int shootme2Port = NetworkPortAssigner.retrieveNextPort();
        Shootme shootme2 = new Shootme(shootme2Port, true, 1500);
        SipProvider shootme2Provider = shootme2.createProvider();
        shootme2Provider.addSipListener(shootme2);
        shootme2.setWaitForCancel(true);
        int proxyPort = NetworkPortAssigner.retrieveNextPort();
        Proxy proxy = new Proxy(proxyPort, new int[]{shootme1Port, shootme2Port});
        SipProvider provider = proxy.createSipProvider();
        provider.addSipListener(proxy);
        int shootistPort = NetworkPortAssigner.retrieveNextPort();
        int listeningPort = NetworkPortAssigner.retrieveNextPort();
        Shootist shootist = new Shootist(true, shootistPort, String.valueOf(listeningPort));
        shootist.pauseBeforeBye = 20000;
        shootist.setFromHost("sip-servlets.com");

        sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, listeningPort, listeningPointTransport);
        tomcat.startTomcat();
        Map<String, String> params = new HashMap<String, String>();
        params.put("timeToWaitForBye", "20000");
        params.put("dontSetRURI", "true");
        params.put("servletContainerPort", String.valueOf(proxyPort));
        params.put("testPort", String.valueOf(shootme1Port));
        params.put("senderPort", String.valueOf(shootistPort));
        SipStandardContext sipContext = deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/call-forwarding-b2bua-servlet/src/main/sipapp",
                params, null);

        shootist.init("forward-sender-forking-pending", false, null);
        Thread.sleep(TIMEOUT);
        proxy.stop();
        shootme1.stop();
        shootme2.stop();
        shootist.stop();
//		assertTrue(shootme1.isAckSeen());
        assertTrue(shootme1.checkBye());
//		assertTrue(shootme2.isAckSeen());
//		assertTrue(shootme2.checkBye());
        assertEquals(0, sipContext.getSipManager().getActiveSipSessions());
        assertEquals(0, sipContext.getSipManager().getActiveSipApplicationSessions());

    }

    @Override
    protected Properties getSipStackProperties() {
        Properties sipStackProperties = new Properties();
        sipStackProperties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT",
                "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
                "32");
        sipStackProperties.setProperty(SipStandardService.DEBUG_LOG_STACK_PROP,
                tomcatBasePath + "/" + "mss-jsip-" + getName() + "-debug.txt");
        sipStackProperties.setProperty(SipStandardService.SERVER_LOG_STACK_PROP,
                tomcatBasePath + "/" + "mss-jsip-" + getName() + "-messages.xml");
        sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-" + getName());
        sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
        sipStackProperties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "64");
        sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
        sipStackProperties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "5");
        sipStackProperties.setProperty(SipStandardService.LOOSE_DIALOG_VALIDATION, "true");
        sipStackProperties.setProperty(SipStandardService.PASS_INVITE_NON_2XX_ACK_TO_LISTENER, "true");
        return sipStackProperties;
    }

    @Override
    protected void tearDown() throws Exception {
        logger.info("Test completed");
        super.tearDown();
    }
}
