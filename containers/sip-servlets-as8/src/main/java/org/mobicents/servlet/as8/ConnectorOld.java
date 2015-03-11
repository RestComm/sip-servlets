package org.mobicents.servlet.as8;

import io.undertow.server.handlers.udp.UdpHandler;

import org.mobicents.servlet.sip.undertow.SipProtocolHandler;

@Deprecated
public class ConnectorOld {
    private SipProtocolHandler protocolHandler;
    private String name;
    private UdpHandler handler;

    public ConnectorOld(String name) {
        this.name = name;
    }

    public SipProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public void setProtocolHandler(SipProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    public void init(){
        try {
            this.protocolHandler.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void start(){
        try {
            this.protocolHandler.start(handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause(){
        this.protocolHandler.resume();
    }

    public void stop(){
        try {
            this.protocolHandler.destroy();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
    public UdpHandler getHandler() {
        return handler;
    }

    public void setHandler(UdpHandler handler) {
        this.handler = handler;
    }
}
