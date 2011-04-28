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

import javax.servlet.sip.ServletTimer;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsServletTimer extends ServletTimer {

	/**
	 * Cancel this timer, possibly by also interrupting the thread (from the
	 * thread pool) running the task. Note that interupting the thread may have
	 * undesired consequences.
	 * 
	 * @param mayInterruptIfRunning 
	 * @param updateAppSessionReadyToInvalidateState boolean to update or not the readyToInvalidateState upon removing a servlet Timer,
	 * this is useful when we process a remoteInvalidation to make sure we don't load and update the sip session from the cache
	 */
	void cancel(boolean mayInterruptIfRunning, boolean updateAppSessionReadyToInvalidateState);
}
