package org.mobicents.as8;

import javax.sip.CreateListeningPointResult;

import gov.nist.javax.sip.stack.UndertowMessageProcessor;
import io.undertow.server.handlers.udp.RootUdpHandler;
import io.undertow.server.handlers.udp.UdpHandler;
import io.undertow.servlet.handlers.ConvergedApplicationMessageProcessorHandler;
import io.undertow.servlet.handlers.ConvergedApplicationUdpHandler;

import org.mobicents.servlet.sip.undertow.SipProtocolHandler;
import org.wildfly.extension.undertow.UdpListenerService;

public class SipUdpListener {
    
    private SipProtocolHandler protocolHandler;
    private UdpListenerService udpListenerService;

    public SipUdpListener(SipProtocolHandler protocolHandler, UdpListenerService udpListenerService) {
        
        this.udpListenerService = udpListenerService;
        this.protocolHandler = protocolHandler;
        
    }

    public SipProtocolHandler getProtocolHandler() {
        return protocolHandler;
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
            RootUdpHandler rootHandler = this.udpListenerService.getServerService().getValue().getRootUdpHandler();
            CreateListeningPointResult result = this.protocolHandler.start(rootHandler);
            
            ConvergedApplicationMessageProcessorHandler messageProcessorHandler = new ConvergedApplicationMessageProcessorHandler();
            messageProcessorHandler.init(rootHandler.getChannel());
            messageProcessorHandler.addMessageProcessor((UndertowMessageProcessor)result.getMessageProcessor());
            
            ConvergedApplicationUdpHandler caHandler = new ConvergedApplicationUdpHandler();
            caHandler.init(rootHandler.getChannel());
            caHandler.addMessageProcessor((UndertowMessageProcessor)result.getMessageProcessor());

            rootHandler.wrapNextHandler(messageProcessorHandler).wrapNextHandler(caHandler);
            
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
        return this.udpListenerService.getServerService().getValue().getRootUdpHandler();
    }

    public void wrapHandler(UdpHandler handler) {
        this.udpListenerService.getServerService().getValue().getRootUdpHandler().wrapNextHandler(handler);
    }

    public UdpListenerService getUdpListenerService() {
        return udpListenerService;
    }
}
