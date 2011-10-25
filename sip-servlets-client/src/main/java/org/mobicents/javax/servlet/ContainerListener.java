/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
 * Causes applications to be notified of various events occuring the Container.
 */
public interface ContainerListener extends java.util.EventListener{
	
	 /**
     * As soon as congestion starts in the underlying source, it calls this
     * method to notify about it. Notification is only one-time till the
     * congestion abates in which case
     * {@link CongestionListener#onCongestionControlStopped(CongestionEvent)} is called
     * 
     * @param source
     *            The underlying source which is facing congestion
     */
    public void onCongestionControlStarted(CongestionControlEvent event);

    /**
     * As soon as congestion abates in the underlying source, it calls this
     * method to notify about it. Notification is only one-time till the
     * congestion starts again in which case
     * {@link CongestionListener#onCongestionStart(String)} is called
     * 
     * @param source
     *            The underlying source
     */
    public void onCongestionControlStopped(CongestionControlEvent event);
    
    /**
     * When a request that comes in is not passed up to the application because of congestion control.
     * 
     * @param request the request that wasn't passed up to the application because of congestion control
     * @param event giving details on what triggered the throttling
     * @return a SipServletResponse that should be sent back to the originator of the request. If null, then 
     * the container will generate the response automatically
     */
    public SipServletResponse onRequestThrottled(SipServletRequest request, CongestionControlEvent event);    
}
