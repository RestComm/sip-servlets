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

package org.mobicents.servlet.sip.core.timers;

import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;

/**
 * Base interface for the sip application session expiration timer task responsible
 * to expire the session
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface SipApplicationSessionTimerTask extends Runnable {
	/**
	 * Retrieve the sip application Session associated with the timer task
	 * @return the sip application session
	 */
	MobicentsSipApplicationSession getSipApplicationSession();
	
	/**
	 * Set the sip application Session associated with the timer task
	 * @param sipApplicationSession the sip application session to set
	 */
	void setSipApplicationSession(MobicentsSipApplicationSession sipApplicationSession);

	/**
	 * Returns the delay in milliseconds before the session is due to expire or a negative value
	 * if it has already passed the time it should have expired 
	 * @return
	 */
	long getDelay();

}
