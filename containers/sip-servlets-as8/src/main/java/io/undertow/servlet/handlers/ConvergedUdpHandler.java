package io.undertow.servlet.handlers;

import gov.nist.javax.sip.stack.UndertowMessageProcessor;
import io.undertow.server.handlers.udp.UdpHandler;

public interface ConvergedUdpHandler extends UdpHandler{

    void addMessageProcessor(UndertowMessageProcessor messageProcessor);
}
