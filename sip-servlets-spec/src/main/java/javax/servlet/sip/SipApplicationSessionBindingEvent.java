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
 * Events of this type are either sent to an object that implements SipApplicationSessionBindingListener when it is bound or unbound from an application session, or to a SipApplicationSessionAttributeListener that has been configured in the deployment descriptor when any attribute is bound, unbound or replaced in an application session.
 * The session binds the object by a call to SipApplicationSession.setAttribute(String, Object) and unbinds the object by a call to SipApplicationSession.removeAttribute(String).
 * @since: 1.1 
 * @see SipApplicationSession, SipApplicationSessionBindingListener, SipApplicationSessionAttributeListener
 */
public class SipApplicationSessionBindingEvent extends java.util.EventObject{
	private String name= null;
    /**
     * Constructs an event that notifies an object that it has been bound to or unbound from an application session. 
     * To receive the event, the object must implement SipApplicationSessionBindingListener.
     * @param session - the application ession to which the object is bound or unboundname - the name with which the object is bound or unbound
     */
    public SipApplicationSessionBindingEvent(javax.servlet.sip.SipApplicationSession session, java.lang.String name){
         super(session);
         this.name = name;
    }

    /**
     * Returns the application session to or from which the object is bound or unbound.
     */
    public javax.servlet.sip.SipApplicationSession getApplicationSession(){
        return (SipApplicationSession)getSource(); 
    }

    /**
     * Returns the name with which the object is bound to or unbound from the application session.
     */
    public java.lang.String getName(){
        return name;
    }

}
