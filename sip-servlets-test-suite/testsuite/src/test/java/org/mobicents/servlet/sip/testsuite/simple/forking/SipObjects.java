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

package org.mobicents.servlet.sip.testsuite.simple.forking;

import gov.nist.javax.sip.SipStackImpl;

import java.util.Properties;

import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipStack;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

// import org.sipfoundry.commons.log4j.SipFoundryAppender;
// import org.sipfoundry.commons.log4j.SipFoundryLayout;
// import org.sipfoundry.commons.log4j.SipFoundryLogRecordFactory;

public class SipObjects {
    SipStack sipStack;
    HeaderFactory headerFactory;
    AddressFactory addressFactory;
    MessageFactory messageFactory;

    public SipObjects(int myPort, String stackName, String automaticDialog, boolean reentrant) {
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        String stackname = stackName + myPort;
        properties.setProperty("javax.sip.STACK_NAME", stackname);

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.

        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", automaticDialog);

       /* properties.setProperty("gov.nist.javax.sip.LOG_FACTORY", SipFoundryLogRecordFactory.class
                .getName()); */

        // Set to 0 in your production code for max speed.
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.

        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        String logFile = "logs/" + stackname + ".txt";
        String msgLogFile = "logs/msg-" + stackname + ".xml";
        properties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "32");

        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", logFile);
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", msgLogFile);
        properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "true");
        
        if(reentrant) {
        	properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "4");
    		properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
        }

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            String logFileDirectory = "logs/";

            /* SipFoundryAppender sfa = new SipFoundryAppender(new SipFoundryLayout(),
                    logFileDirectory + "sip" + stackname + ".log");

            ((SipStackImpl) sipStack).addLogAppender(sfa);*/
            System.out.println("createSipStack " + sipStack);
        } catch (Exception e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            throw new RuntimeException("Stack failed to initialize");
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
        } catch (SipException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
