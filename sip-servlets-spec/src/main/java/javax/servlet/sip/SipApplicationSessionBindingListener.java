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
 * Causes an object to be notified when it is bound to or unbound from a SipApplicationSession. The object is notified by an SipApplicationSessionBindingEvent object. This may be as a result of a servlet programmer explicitly unbinding an attribute from an application session, due to an application session being invalidated, or due to an application session timing out.
 * Since: 1.1 See Also:SipApplicationSession, SipApplicationSessionBindingEvent
 */
public interface SipApplicationSessionBindingListener extends java.util.EventListener{
    /**
     * Notifies the object that it is being bound to an application session and identifies the application session.
     */
    void valueBound(javax.servlet.sip.SipApplicationSessionBindingEvent event);

    /**
     * Notifies the object that it is being unbound from an application session and identifies the application session.
     */
    void valueUnbound(javax.servlet.sip.SipApplicationSessionBindingEvent event);

}
