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

import java.util.EventListener;

/**
 * Objects that are bound to a SipApplicationSession may listen to container events 
 * notifying them when the application session to which they are bound will 
 * be passivated or activated. 
 * A container that migrates application sessions between VMs or persists 
 * them is required to notify all attributes implementing this listener and 
 * that are bound to those application sessions of the events.
 */
public interface SipApplicationSessionActivationListener extends EventListener {
	/**
	 * Notification that the application session is about to be passivated.
	 * @param se event identifying the application session about to be persisted
	 * @since 1.1
	 */
	void sessionWillPassivate(SipApplicationSessionEvent se);
	/**
	 * Notification that the application session has just been activated.
	 * @param se event identifying the activated application session
	 * @since 1.1
	 */
	void sessionDidActivate(SipApplicationSessionEvent se);
}
