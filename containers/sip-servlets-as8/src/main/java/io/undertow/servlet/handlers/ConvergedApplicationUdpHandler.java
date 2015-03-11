package io.undertow.servlet.handlers;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import gov.nist.javax.sip.stack.UndertowMessageProcessor;
import io.undertow.server.handlers.udp.AbstractUdpHandler;
import io.undertow.server.protocol.udp.UdpMessage;

public class ConvergedApplicationUdpHandler extends AbstractUdpHandler implements ConvergedUdpHandler{
    private UndertowMessageProcessor messageProcessor;
    
    @Override
    public void addMessageProcessor(UndertowMessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void handleRequest(UdpMessage message) {
        DatagramPacket packet = new DatagramPacket(message.getBufferedData().array(),
                message.getBufferedData().array().length);
        InetSocketAddress sourceAddress = (InetSocketAddress) message.getAddressBuffer().getSourceAddress();
        packet.setAddress(sourceAddress.getAddress());
        packet.setSocketAddress(sourceAddress);

        messageProcessor.addNewMessage(packet);
    } 

}
