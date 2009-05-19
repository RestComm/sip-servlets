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
package org.jboss.web.tomcat.service.session;

import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * This method embeds both the sipSessionKey and the corresponding sip application session key for a sip session
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class ClusteredSipSessionKey {
	private SipSessionKey sipSessionKey;
	private SipApplicationSessionKey sipApplicationSessionKey;
	
	/**
	 * @param sipSessionKey
	 * @param sipApplicationSessionKey
	 */
	public ClusteredSipSessionKey(SipSessionKey sipSessionKey,
			SipApplicationSessionKey sipApplicationSessionKey) {
		super();
		this.sipSessionKey = sipSessionKey;
		this.sipApplicationSessionKey = sipApplicationSessionKey;
	}
	
	/**
	 * @param sipSessionKey the sipSessionKey to set
	 */
	public void setSipSessionKey(SipSessionKey sipSessionKey) {
		this.sipSessionKey = sipSessionKey;
	}
	/**
	 * @return the sipSessionKey
	 */
	public SipSessionKey getSipSessionKey() {
		return sipSessionKey;
	}
	/**
	 * @param sipApplicationSessionKey the sipApplicationSessionKey to set
	 */
	public void setSipApplicationSessionKey(SipApplicationSessionKey sipApplicationSessionKey) {
		this.sipApplicationSessionKey = sipApplicationSessionKey;
	}
	/**
	 * @return the sipApplicationSessionKey
	 */
	public SipApplicationSessionKey getSipApplicationSessionKey() {
		return sipApplicationSessionKey;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((sipApplicationSessionKey == null) ? 0
						: sipApplicationSessionKey.hashCode());
		result = prime * result
				+ ((sipSessionKey == null) ? 0 : sipSessionKey.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusteredSipSessionKey other = (ClusteredSipSessionKey) obj;
		if (sipApplicationSessionKey == null) {
			if (other.sipApplicationSessionKey != null)
				return false;
		} else if (!sipApplicationSessionKey
				.equals(other.sipApplicationSessionKey))
			return false;
		if (sipSessionKey == null) {
			if (other.sipSessionKey != null)
				return false;
		} else if (!sipSessionKey.equals(other.sipSessionKey))
			return false;
		return true;
	}
	
	
}
