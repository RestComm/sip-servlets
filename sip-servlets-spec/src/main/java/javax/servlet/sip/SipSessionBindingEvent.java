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
 * Events of this type are either sent to an object that implements SipSessionBindingListener when it is bound or unbound from a session, or to a SipSessionAttributeListener that has been configured in the deployment descriptor when any attribute is bound, unbound or replaced in a session.
 * The session binds the object by a call to SipSession.setAttribute and unbinds the object by a call to SipSession.removeAttribute.
 * @see SipSession, SipSessionBindingListener, SipSessionAttributeListener
 */
public class SipSessionBindingEvent extends java.util.EventObject{
	private String name;
    /**
     * Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the event, the object must implement
     * .
     * @param session the session to which the object is bound or unboundname - the name with which the object is bound or unbound
     */
    public SipSessionBindingEvent(javax.servlet.sip.SipSession session, java.lang.String name){
         super(session);
         this.name = name;
    }

    /**
     * Returns the name with which the object is bound to or unbound from the session.
     */
    public java.lang.String getName(){
        return name; 
    }

    /**
     * Returns the session to or from which the object is bound or unbound.
     */
    public javax.servlet.sip.SipSession getSession(){
        return (SipSession)getSource(); 
    }

}
