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
package org.mobicents.servlet.sip.testsuite.proxy;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.NetworkPortAssigner;
import org.mobicents.servlet.sip.SipServletTestCase;

public class ParallelProxyWithRecordRouteUseHostNameTest extends SipServletTestCase {

    private static transient Logger logger = Logger.getLogger(ParallelProxyWithRecordRouteUseHostNameTest.class);

    protected Shootist shootist;

    protected Shootme shootme;

    protected Cutme cutme;

    protected Hashtable providerTable = new Hashtable();

    private static final int timeout = 20000;

    private static final int receiversCount = 1;

    public ParallelProxyWithRecordRouteUseHostNameTest(String name) {
        super(name);
        this.sipIpAddress = "0.0.0.0";
        autoDeployOnStartup = false;
    }

    @Override
    public void setUp() throws Exception {
        containerPort = NetworkPortAssigner.retrieveNextPort();
        super.setUp();

        int shootistPort = NetworkPortAssigner.retrieveNextPort();
        this.shootist = new Shootist(false, shootistPort, String.valueOf(containerPort));
        int shootmePort = NetworkPortAssigner.retrieveNextPort();
        this.shootme = new Shootme(shootmePort);
        int cutmePort = NetworkPortAssigner.retrieveNextPort();
        this.cutme = new Cutme(cutmePort);

        Map<String, String> params = new HashMap();
        params.put("servletContainerPort", String.valueOf(containerPort));
        params.put("testPort", String.valueOf(shootistPort));
        params.put("receiverPort", String.valueOf(shootmePort));
        params.put("cutmePort", String.valueOf(cutmePort));
        deployApplication(projectHome
                + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                params, null);
    }

    public void testProxy() {
        this.shootme.init("stackName", null);
        this.cutme.init(null);
        this.shootist.init();
        for (int q = 0; q < 20; q++) {
            if (shootme.ended && cutme.canceled) {
                //conditions met
                break;
            } else {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        assertTrue("Conversation not complete!", shootme.ended);
        assertTrue("The party that was supposed to be cancelled didn't cancel.", cutme.canceled);
    }

    @Override
    public void tearDown() throws Exception {
        shootist.destroy();
        shootme.destroy();
        cutme.destroy();
        super.tearDown();
    }

    @Override
    public void deployApplication() {
        assertTrue(tomcat
                .deployContext(
                        projectHome
                        + "/sip-servlets-test-suite/applications/proxy-sip-servlet/src/main/sipapp",
                        "sip-test-context", "sip-test"));
    }

    @Override
    protected String getDarConfigurationFile() {
        return "file:///"
                + projectHome
                + "/sip-servlets-test-suite/testsuite/src/test/resources/"
                + "org/mobicents/servlet/sip/testsuite/proxy/simple-sip-servlet-dar.properties";
    }

    public void init() {
        // setupPhones();
    }

}
