/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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

import java.io.Serializable;

import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.timers.PeriodicScheduleStrategy;
import org.mobicents.timers.TimerTaskData;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class TimerServiceTaskData extends TimerTaskData implements Serializable {
	
	private Serializable data;
	
	private SipApplicationSessionKey key;
	
	private long delay;
	
	public TimerServiceTaskData(Serializable id, long startTime, long period,
			PeriodicScheduleStrategy periodicScheduleStrategy) {
		super(id, startTime, period, periodicScheduleStrategy);
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Serializable data) {
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public Serializable getData() {
		return data;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(SipApplicationSessionKey key) {
		this.key = key;
	}

	/**
	 * @return the key
	 */
	public SipApplicationSessionKey getKey() {
		return key;
	}

	/**
	 * @param delay the delay to set
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * @return the delay
	 */
	public long getDelay() {
		return delay;
	}

}
