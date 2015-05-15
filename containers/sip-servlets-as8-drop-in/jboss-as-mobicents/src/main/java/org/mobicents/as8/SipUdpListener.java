/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.as8;

import javax.sip.CreateListeningPointResult;

import gov.nist.javax.sip.stack.UndertowMessageProcessor;
import io.undertow.server.handlers.udp.RootUdpHandler;
import io.undertow.server.handlers.udp.UdpHandler;
import io.undertow.servlet.handlers.ConvergedApplicationMessageProcessorHandler;
import io.undertow.servlet.handlers.ConvergedApplicationUdpHandler;

import org.mobicents.servlet.sip.undertow.SipProtocolHandler;
import org.wildfly.extension.undertow.UdpListenerService;

/**
 * @author alerant.appngin@gmail.com
 */
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
