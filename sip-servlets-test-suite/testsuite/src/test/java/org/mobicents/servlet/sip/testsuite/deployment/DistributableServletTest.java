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
package org.mobicents.servlet.sip.testsuite.deployment;

import java.util.HashMap;
import java.util.Map;
import javax.sip.SipProvider;
import javax.sip.address.SipURI;
import static junit.framework.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.testsuite.ProtocolObjects;
import org.mobicents.servlet.sip.testsuite.TestSipListener;

/**
 * This test ensures that a sip application whose the sip.xml dd contains the
 * distributable tag is able to work in a non distributed environment
 *
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableServletTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(DistributableServletTest.class);

    private static final String TRANSPORT = "udp";
    private static final boolean AUTODIALOG = true;
    private static final int TIMEOUT = 10000;
//	private static final int TIMEOUT = 100000000;

    TestSipListener sender;
    ProtocolObjects senderProtocolObjects;
    SipStandardContext sipContext;

    public DistributableServletTest(String name) {
        super(name);
        autoDeployOnStartup=false;
    }

    @Override
    protected void deployApplication() {

    }
    
    public SipStandardContext deployApplication(Map<String, String> params) {
        SipStandardContext ctx = deployApplication(
                projectHome + "/sip-servlets-test-suite/applications/distributable-servlet/src/main/sipapp",
                "sip-tes",
                params,
                null);
        assertTrue(ctx.getAvailable());
        return ctx;
    }    

    @Override
    protected String getDarConfigurationFile() {
        return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/deployment/distributable-servlet-dar.properties";
    }

    @Override
    protected void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();

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
        deployApplication(params);        
    }

    public void testDistributableApp() throws Exception {
        String fromName = "sender";
        String fromSipAddress = "sip-servlets.com";
        SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
                fromName, fromSipAddress);

        String toUser = "receiver";
        String toSipAddress = "sip-servlets.com";
        SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
                toUser, toSipAddress);

        sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);
        Thread.sleep(TIMEOUT);
        assertFalse(sender.isServerErrorReceived());
        assertTrue(sender.isAckSent());
        assertTrue(sender.getOkToByeReceived());
    }

    @Override
    protected void tearDown() throws Exception {
        senderProtocolObjects.destroy();
        super.tearDown();
    }
}
