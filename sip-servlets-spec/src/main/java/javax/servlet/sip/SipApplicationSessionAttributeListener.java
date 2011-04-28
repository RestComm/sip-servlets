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
 * This listener interface can be implemented in order to get notifications of changes to the attribute lists of application sessions.
 * Since: 1.1
 */
public interface SipApplicationSessionAttributeListener extends java.util.EventListener{
    /**
     * Notification that an attribute has been added to an application session. Called after the attribute is added.
     */
    void attributeAdded(javax.servlet.sip.SipApplicationSessionBindingEvent ev);

    /**
     * Notification that an attribute has been removed from an application session. Called after the attribute is removed.
     */
    void attributeRemoved(javax.servlet.sip.SipApplicationSessionBindingEvent ev);

    /**
     * Notification that an attribute has been replaced in an application session. Called after the attribute is replaced.
     */
    void attributeReplaced(javax.servlet.sip.SipApplicationSessionBindingEvent ev);

}
