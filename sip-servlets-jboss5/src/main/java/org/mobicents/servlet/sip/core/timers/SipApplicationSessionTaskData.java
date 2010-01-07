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

import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.timers.TimerTaskData;

/**
 * Data to be saved so that the fault tolerant sip application session timer canbe restored and fired if a node crash
 * and fail over occurs
 * 
 * We just save the sip application session key so that we are able to retrieve it from the cache 
 * and do the expiration process on failover
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipApplicationSessionTaskData extends TimerTaskData implements Serializable {
	
	private SipApplicationSessionKey key;
	
	public SipApplicationSessionTaskData(MobicentsSipApplicationSession sipApplicationSession) {
		super(sipApplicationSession.getId(), -1, -1, null);
		this.key = sipApplicationSession.getKey();
	}
	/**
	 * @return the data
	 */
	public SipApplicationSessionKey getKey() {
		return key;
	}
}
