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

package javax.servlet.sip;
/**
 * Implementations of this interface can receive notifications about invalidated and/or activated SipApplicationSession objects in the SIP application they are part of. To receive notification events, the implementation class must be configured in the deployment descriptor for the servlet application.
 */
public interface SipApplicationSessionListener extends java.util.EventListener{
    /**
     * Notification that a session was created.
     */
    void sessionCreated(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that a session was invalidated. Either it timed out or it was explicitly invalidated. It is not possible to extend the application sessions lifetime.
     */
    void sessionDestroyed(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that an application session has expired. The application may request an extension of the lifetime of the application session by invoking
     * .
     */
    void sessionExpired(javax.servlet.sip.SipApplicationSessionEvent ev);
    
    /**
     * Notification that a SipApplicationSession is in the ready-to-invalidate state. 
     * The container will invalidate this session upon completion of this callback 
     * unless the listener implementation calls 
     * SipApplicationSessionEvent.getApplicationSession().setInvalidateWhenReady(false)
     * 
     * @param ev the notification event
     * @since 1.1
     */
    void sessionReadyToInvalidate(SipApplicationSessionEvent ev);
}
