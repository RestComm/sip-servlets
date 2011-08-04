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

package org.mobicents.servlet.sip.notification;

import javax.servlet.sip.SipApplicationSessionEvent;

/**
 * Events of this type are sent to an object that implements SipApplicationSessionActivationListener. since the specification doesn't differentiate between different causes for activation/passivation such as failover, replication, passivation because of non activity this class is added to provide the application for the root cause of the notification allowing finer grained control
 * 
 * @author jean.deruelle@gmail.com
 * @since: 1.6
 */
public class SipApplicationSessionActivationEvent extends SipApplicationSessionEvent {
	private SessionActivationNotificationCause cause = null;
    /**
     * Constructs an event that notifies an object that it has been activated/passivated for a session. To receive the event, the object must implement SipApplicationSessionActivationListener
     * .
     * @param session the session for which the notification is generated
     * @param cause the root cause on why the notification has been generated (failover, replication, ...)
     */
    public SipApplicationSessionActivationEvent(javax.servlet.sip.SipApplicationSession session, SessionActivationNotificationCause cause){
         super(session);
         this.cause = cause;
    }

    /**
     * Returns the cause on why the listener is called for the application session.
     */
    public SessionActivationNotificationCause getCause(){
        return cause;
    }

}
