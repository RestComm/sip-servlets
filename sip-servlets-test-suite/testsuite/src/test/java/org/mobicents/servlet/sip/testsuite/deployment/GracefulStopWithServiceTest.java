/*
 * TeleStax, Open Source Cloud Communications.
 * Copyright 2012 and individual contributors by the @authors tag. 
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
package org.mobicents.servlet.sip.testsuite.deployment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sip.SipProvider;
import static junit.framework.Assert.assertTrue;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.ContainerEventType;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * Test for additional service specific criteria.
 *
 * @author jean.deruelle@gmail.com
 *
 */
public class GracefulStopWithServiceTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(GracefulStopWithServiceTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 60000;
    private static final long GRACEFUL_INTERVAL = 1000;

    TestSipListener sender;
    ProtocolObjects senderProtocolObjects;

    public GracefulStopWithServiceTest(String name) {
        super(name);
        autoDeployOnStartup = false;
        startTomcatOnStartup = false;
    }

    public SipStandardContext deployApplication(Map<String, String> params) {
        MemoryRealm realm = new MemoryRealm();
        realm
                .setPathname(projectHome
                        + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                        + "org/mobicents/servlet/sip/testsuite/security/tomcat-users.xml");
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/simple-sip-servlet/src/main/sipapp",
                "sip-tes",
                params,
                null,
                realm);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/deployment/blank dir/simple-sip-servlet-dar.properties";
    }

    private SipContext sipContext;

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();

        tomcat.startTomcat();
        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, null, null, null);
        int myPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(myPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();
        senderProvider.addSipListener(sender);
        senderProtocolObjects.start();
        
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(myPort));
        params.put("testContextApplicationParameter", "OK");
        sipContext = deployApplication(params);        
    }

    /**
     * Assert container is stopped only when service logic allows it
     */
    public void testContextGracefulShutdownPositiveValue() throws Exception {
        //wait for the container to be fully started
        Thread.sleep(5000);

        tomcat.getSipService().setGracefulInterval(GRACEFUL_INTERVAL);
        tomcat.getSipService().stopGracefully(TIMEOUT);

        //wait for MESSAGE to arrive
        Thread.sleep(GRACEFUL_INTERVAL);
        List<String> allMessagesContent = sender.getAllMessagesContent();
        assertTrue(ContainerEventType.GRACEFUL_SHUTDOWN_STARTED.toString(),
                allMessagesContent.contains(ContainerEventType.GRACEFUL_SHUTDOWN_STARTED.toString()));

        //wait a bit more than grace interval 
        Thread.sleep(GRACEFUL_INTERVAL * 2);
        allMessagesContent = sender.getAllMessagesContent();
        assertTrue(ContainerEventType.GRACEFUL_SHUTDOWN_CHECK.toString(),
                allMessagesContent.contains(ContainerEventType.GRACEFUL_SHUTDOWN_CHECK.toString()));

        //wait a bit more for the container to finally shutdown
        Thread.sleep(GRACEFUL_INTERVAL * 2);
        // make sure the Graceful Stop was effective
        assertEquals(0, sipContext.getSipApplicationDispatcher().findInstalledSipApplications().length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        tomcat.stopTomcat();
        senderProtocolObjects.destroy();
    }

    @Override
    protected void deployApplication() {

    }

}
