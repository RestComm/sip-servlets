/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.servlet.sip.message;

import java.io.Externalizable;

import gov.nist.javax.sip.header.HeaderFactoryImpl;

import javax.sip.PeerUnavailableException;
import javax.sip.UndertowSipFactory;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
/**
 * @author alerant.appngin@gmail.com
 *
 */
public class UndertowSipFactoryImpl extends SipFactoryImpl implements MobicentsSipFactory, Externalizable {
    private static final Logger logger = Logger.getLogger(UndertowSipFactoryImpl.class.getCanonicalName());
    private static boolean initialized;

    @Override
    public void initialize(String pathName, boolean prettyEncoding) {
        if (!initialized) {
            try {
                System.setProperty("gov.nist.core.STRIP_ADDR_SCOPES", "true");
                sipFactory = UndertowSipFactory.getInstance();
                sipFactory.setPathName(pathName);
                addressFactory = sipFactory.createAddressFactory();
                headerFactory = sipFactory.createHeaderFactory();
                if (prettyEncoding) {
                    ((HeaderFactoryImpl) headerFactory).setPrettyEncoding(prettyEncoding);
                }
                messageFactory = sipFactory.createMessageFactory();
                initialized = true;
            } catch (PeerUnavailableException ex) {
                logger.error("Could not instantiate factories -- exitting", ex);
                throw new IllegalArgumentException("Cannot instantiate factories ", ex);
            }
        }
    }

    public UndertowSipFactoryImpl(SipApplicationDispatcher dispatcher) {
        super(dispatcher);
    }

}
