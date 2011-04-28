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
 * Containers are required to invoke init() on the servlets before the servlets are ready for service. The servlet can only be used after succesful initialization. Since SIP is a peer-to-peer protocol and some servlets may act as UACs, the container is required to let the servlet know when it is succesfully initialized by invoking SipServletListener.
 * Since: 1.1 See Also:SipServletContextEvent
 */
public interface SipServletListener extends java.util.EventListener {
    /**
     * Notification that the servlet was succesfully initialized
     */
    void servletInitialized(javax.servlet.sip.SipServletContextEvent ce);

}
