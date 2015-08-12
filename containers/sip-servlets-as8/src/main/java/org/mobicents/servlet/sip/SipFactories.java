/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
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
package org.mobicents.servlet.sip;

import gov.nist.javax.sip.header.HeaderFactoryImpl;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.apache.log4j.Logger;

/**
 *
 * This class is based on org.mobicents.servlet.sip.SipFactories class from sip-servlet-as7 project, re-implemented for jboss
 * as8 (wildfly) by:
 *
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipFactories {
    private static final Logger logger = Logger.getLogger(SipFactories.class.getCanonicalName());

    private static boolean initialized;

    public static AddressFactory addressFactory;

    public static HeaderFactory headerFactory;

    public static SipFactory sipFactory;

    public static MessageFactory messageFactory;

    public static void initialize(String pathName, boolean prettyEncoding) {
        if (!initialized) {
            try {
                System.setProperty("gov.nist.core.STRIP_ADDR_SCOPES", "true");
                sipFactory = SipFactory.getInstance();
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
}
