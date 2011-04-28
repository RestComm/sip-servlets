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
 * This is the class representing event notifications for changes to SipApplicationSessions within a SIP servlet application.
 * A SipApplicationSessionListener receiving this notification may attempt to extend the lifetime of the application instance corresponding to the expiring application session by invoking SipApplicationSession.setExpires(int).
 * @since: 1.1 
 */
public class SipApplicationSessionEvent extends java.util.EventObject{
    /**
     * Creates a new SipApplicationSessionEvent object.
     * @param appSession the expired application session
     */
    public SipApplicationSessionEvent(javax.servlet.sip.SipApplicationSession appSession){
    	super(appSession);
    }

    /**
     * Returns the expired session object.
     */
    public javax.servlet.sip.SipApplicationSession getApplicationSession(){
        return (SipApplicationSession)getSource(); 
    }

}
