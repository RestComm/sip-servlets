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
package org.mobicents.servlet.sip.testsuite.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sip.SipProvider;
import static junit.framework.Assert.assertTrue;

import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.catalina.SipStandardManager;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test starts 1 sip servlets container on port 5070 that has 2 services
 * Shootme and Shootist.
 *
 * Then the shootist sends an INVITE that goes to Shootme.
 *
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class SameContainerRoutingServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(SameContainerRoutingServletTest.class);

    private static final int TIMEOUT = 60000;
    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;

    TestSipListener sender;
    ProtocolObjects senderProtocolObjects;
    
    private int senderPort;

    public SameContainerRoutingServletTest(String name) {
        super(name);
        autoDeployOnStartup = false;
    }

    @Override
    public void deployApplication() {
    }

    public SipStandardContext deployNotifierApplication(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/notifier-servlet/src/main/sipapp",
                "notifier-test",
                params,
                null);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    public SipStandardContext deployShootistApplication(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/subscriber-servlet/src/main/sipapp",
                "subscriber-test",
                params,
                null);
        assertTrue(ctx.getAvailable());
        return ctx;
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/routing/samecontainer-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();
        senderProtocolObjects = new ProtocolObjects(
                "sender", "gov.nist", TRANSPORT, AUTODIALOG, "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + containerPort, null, null);
        senderPort = NetworkPortAssigner.retrieveNextPort();
        sender = new TestSipListener(senderPort, containerPort, senderProtocolObjects, true);
        SipProvider senderProvider = sender.createProvider();
        senderProvider.addSipListener(sender);
        senderProtocolObjects.start();
    }

    public void testSameContainerRouting() throws Exception {
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(senderPort));
        
        deployNotifierApplication(params);        
        
        params.put("requestURI", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + containerPort);
        deployShootistApplication(params);        
        
        Thread.sleep(TIMEOUT);
        assertTrue(sender.getAllMessagesContent().size() > 0);
        boolean dialogCompletedReceived = false;
        for (String messageContent : sender.getAllMessagesContent()) {
            if (messageContent.equalsIgnoreCase("dialogCompleted")) {
                dialogCompletedReceived = true;
            }
        }
        assertTrue(dialogCompletedReceived);
    }

    public void testSameContainerRoutingNo200ToNotify() throws Exception {
        
        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(senderPort));
        
        deployNotifierApplication(params);        
        
        params.put("requestURI", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + containerPort);
        params.put("no200OKToNotify", "true");
        deployShootistApplication(params);
        
        Thread.sleep(TIMEOUT);
        assertTrue(sender.getAllMessagesContent().size() > 0);
        boolean dialogCompletedReceived = false;
        for (String messageContent : sender.getAllMessagesContent()) {
            if (messageContent.equalsIgnoreCase("dialogCompleted")) {
                dialogCompletedReceived = true;
            }
        }
        assertTrue(dialogCompletedReceived);
    }

    @Override
    public void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        logger.info("Test completed");
        super.tearDown();
    }

}
