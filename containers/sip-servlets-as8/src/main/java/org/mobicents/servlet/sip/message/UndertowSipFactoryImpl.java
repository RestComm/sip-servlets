package org.mobicents.servlet.sip.message;

import java.io.Externalizable;

import gov.nist.javax.sip.header.HeaderFactoryImpl;

import javax.sip.PeerUnavailableException;
import javax.sip.UndertowSipFactory;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

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
