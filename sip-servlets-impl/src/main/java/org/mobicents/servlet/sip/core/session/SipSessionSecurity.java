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

package org.mobicents.servlet.sip.core.session;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.security.MobicentsAuthInfoEntry;
import org.mobicents.servlet.sip.core.security.MobicentsSipSessionSecurity;

/**
 * Added to cache the credentials and store the nextnonce for Issue 2173 http://code.google.com/p/mobicents/issues/detail?id=2173
 * Handle Header [Authentication-Info: nextnonce="xyz"] in sip authorization responses
 * @author jean.deruelle@gmail.com
 *
 */
public class SipSessionSecurity implements MobicentsSipSessionSecurity {
	private static final Logger logger = Logger.getLogger(SipSessionSecurity.class);
	
	private ConcurrentHashMap<String, MobicentsAuthInfoEntry> cachedAuthInfos = new ConcurrentHashMap<String, MobicentsAuthInfoEntry>(2);
	private String nextNonce;
	
	public void removeCachedAuthInfo(String realm) {
		if(logger.isDebugEnabled()) {
			logger.debug("Removing authInfo for realm " + realm);
		}
		getCachedAuthInfos().remove(realm);
	}
	
	public void addCachedAuthInfo(String realm, MobicentsAuthInfoEntry authInfoEntry) {
		if(logger.isDebugEnabled()) {
			logger.debug("Caching authInfo " + authInfoEntry + " for realm " + realm);
		}
		getCachedAuthInfos().put(realm, authInfoEntry);
	}
	
	/**
	 * @return the cachedAuthInfos
	 */
	public ConcurrentHashMap<String, MobicentsAuthInfoEntry> getCachedAuthInfos() {		
		return cachedAuthInfos;
	}

	/**
	 * @param nextNonce the nextNonce to set
	 */
	public void setNextNonce(String nextNonce) {
		this.nextNonce = nextNonce;
	}

	/**
	 * @return the nextNonce
	 */
	public String getNextNonce() {
		return nextNonce;
	}
	
	
}
