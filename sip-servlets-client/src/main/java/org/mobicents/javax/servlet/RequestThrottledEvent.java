/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.javax.servlet;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

/**
 * When a request that comes in is not passed up to the application because of
 * congestion control.
 * 
 * The event gives details on what triggered the throttling.
 * 
 * The service logic may introduce a SipServletResponse that should be sent back to the originator of
 * the request in the ServletContext as attribute. 
 * 
 * The attribute name should be "org.mobicents.servlet.sip.THROTTLED_RESPONSE".
 * If null, then the container will generate the response
 * automatically.
 */
public class RequestThrottledEvent extends CongestionControlEvent {

    private final SipServletRequest request;
    private SipServletResponse response = null;

    public RequestThrottledEvent(SipServletRequest request, Reason reason, String message) {
        super(ContainerEventType.REQUEST_THROTTLED, reason, message);
        this.request = request;
    }

    public SipServletResponse getResponse() {
        return response;
    }

    public void setResponse(SipServletResponse response) {
        this.response = response;
    }

    /**
     * @return the request that wasn't passed up to the application because
     * of congestion control
     */
    public SipServletRequest getRequest() {
        return request;
    }

}
