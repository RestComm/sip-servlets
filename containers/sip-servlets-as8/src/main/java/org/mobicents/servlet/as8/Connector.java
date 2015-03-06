package org.mobicents.servlet.as8;

import org.mobicents.servlet.sip.undertow.SipProtocolHandler;

public class Connector {
    private SipProtocolHandler protocolHandler;
    private String name;

    public Connector(String name) {
        this.name = name;
    }

    public SipProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(SipProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

}
