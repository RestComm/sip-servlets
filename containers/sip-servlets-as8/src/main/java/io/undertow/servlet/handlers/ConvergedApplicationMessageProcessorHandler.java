package io.undertow.servlet.handlers;

import gov.nist.javax.sip.stack.UndertowMessageProcessor;
import io.undertow.server.handlers.udp.AbstractUdpHandler;
import io.undertow.server.protocol.udp.UdpMessage;

public class ConvergedApplicationMessageProcessorHandler extends AbstractUdpHandler implements ConvergedUdpHandler{

    private UndertowMessageProcessor messageProcessor;

    
    @Override
    public void handleRequest(UdpMessage message) {
        if(messageProcessor!=null  && next!=null){
            if(next instanceof ConvergedUdpHandler){
                ((ConvergedUdpHandler) next).addMessageProcessor(messageProcessor);
            }
            next.handleRequest(message);
        }else{
            //TODO
        }
    }
    
    @Override
    public void addMessageProcessor(UndertowMessageProcessor messageProcessor){
        this.messageProcessor = messageProcessor;
    }

}
