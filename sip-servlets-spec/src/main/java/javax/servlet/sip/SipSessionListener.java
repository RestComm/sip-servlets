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
 * Implementations of this interface are notified of changes to the list of active SipSessions in a SIP servlet application. To recieve notification events, the implementation class must be configured in the deployment descriptor for the SIP application.
 */
public interface SipSessionListener extends java.util.EventListener{
    /**
     * Notification that a SipSession was created.
     */
    void sessionCreated(javax.servlet.sip.SipSessionEvent se);

    /**
     * Notification that a SipSession was destroyed.
     */
    void sessionDestroyed(javax.servlet.sip.SipSessionEvent se);

    /**
     * Notification that a SipSession is in the ready-to-invalidate state. 
     * The container will invalidate this session upon completion of this callback 
     * unless the listener implementation calls SipSessionEvent.getSession().setInvalidateWhenReady(false)
     * @param se the notification event
     */
    void sessionReadyToInvalidate(SipSessionEvent se);
}
