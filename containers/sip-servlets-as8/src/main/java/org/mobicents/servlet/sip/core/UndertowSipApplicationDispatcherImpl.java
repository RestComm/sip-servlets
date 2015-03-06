package org.mobicents.servlet.sip.core;

import javax.management.MBeanRegistration;

import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingListener;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.UndertowSipFactoryImpl;

public class UndertowSipApplicationDispatcherImpl extends SipApplicationDispatcherImpl implements
        SipApplicationDispatcher, SipApplicationDispatcherImplMBean, MBeanRegistration,
        LoadBalancerHeartBeatingListener {

    private SipFactoryImpl sipFactoryImpl = null;

    public UndertowSipApplicationDispatcherImpl() {
        super();
        sipFactoryImpl = new UndertowSipFactoryImpl(this);
    }

    public SipFactoryImpl getSipFactory() {
        return this.sipFactoryImpl;
    }
}
