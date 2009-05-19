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
package org.jboss.web.tomcat.service.session.distributedcache.impl.jbc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingAttributeGranularitySessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipApplicationSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipSessionData;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class AttributeBasedJBossCacheConvergedSipService extends
		AttributeBasedJBossCacheService implements DistributedCacheConvergedSipManager<OutgoingAttributeGranularitySessionData> {

	public static final String SIPSESSION = "SIPSESSION";
	
	DistributedCacheConvergedSipManagerDelegate<OutgoingAttributeGranularitySessionData> delegate;
	
	/**
	 * @param localManager
	 * @throws ClusteringNotSupportedException
	 */
	public AttributeBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager)
			throws ClusteringNotSupportedException {
		super(localManager);
		delegate = new DistributedCacheConvergedSipManagerDelegate((AbstractJBossCacheService)this, localManager);
	}

	/**
	 * @param localManager
	 * @param cache
	 */
	public AttributeBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager,
			Cache<Object, Object> cache) {
		super(localManager, cache);
		delegate = new DistributedCacheConvergedSipManagerDelegate((AbstractJBossCacheService)this, localManager);
	}
	
	@Override
	public void start() {	
		super.start();
		delegate.start();
	}
	
	@Override
	public void stop() {			
		delegate.stop();
		super.stop();
	}

	public void evictSession(SipApplicationSessionKey sipAppSessionKey, SipSessionKey key) {
		delegate.evictSession(sipAppSessionKey, key);      
	}

	public void evictSession(SipApplicationSessionKey key) {
		delegate.evictSession(key);         
	}

	public void evictSession(SipApplicationSessionKey sipAppSessionKey, SipSessionKey key, String dataOwner) {
		delegate.evictSession(sipAppSessionKey, key, dataOwner);
	}

	public void evictSession(SipApplicationSessionKey key, String dataOwner) {
		delegate.evictSession(key, dataOwner);
	}

	public IncomingDistributableSessionData getSessionData(SipApplicationSessionKey sipAppSessionKey, SipSessionKey key,
			boolean initialLoad) {
		return delegate.getSessionData(sipAppSessionKey, key, initialLoad);		
	}

	public IncomingDistributableSessionData getSessionData(SipApplicationSessionKey sipAppSessionKey, SipSessionKey key,
			String dataOwner, boolean includeAttributes) {
		return delegate.getSessionData(sipAppSessionKey, key, dataOwner, includeAttributes);
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, boolean initialLoad) {
		return delegate.getSessionData(key, initialLoad);		
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, String dataOwner,
			boolean includeAttributes) {
		return delegate.getSessionData(key, dataOwner, includeAttributes);
	}

	public Map<SipApplicationSessionKey, String> getSipApplicationSessionKeys() {
		return delegate.getSipApplicationSessionKeys();
	}
		
	public Map<SipSessionKey, String> getSipSessionKeys() {
		return delegate.getSipSessionKeys();
	}

	public void removeSessionLocal(SipApplicationSessionKey key) {
		delegate.removeSessionLocal(key);
	}

	public void removeSessionLocal(SipApplicationSessionKey sipAppSessionKey, SipSessionKey key) {
		delegate.removeSessionLocal(sipAppSessionKey, key);
	}
	
	public void removeSessionLocal(SipApplicationSessionKey key,
			String dataOwner) {
		delegate.removeSessionLocal(key, dataOwner);
	}

	public void removeSessionLocal(SipApplicationSessionKey sipAppSessionKey, SipSessionKey key, String dataOwner) {
		delegate.removeSessionLocal(sipAppSessionKey, key, dataOwner);
	}

	public void storeSipApplicationSessionData(
			OutgoingAttributeGranularitySessionData sipApplicationSessionData) {
		delegate.storeSipApplicationSessionData((OutgoingDistributableSipApplicationSessionData)sipApplicationSessionData);
	}

	public void storeSipSessionData(
			OutgoingAttributeGranularitySessionData sipSessionData) {
		delegate.storeSipSessionData((OutgoingDistributableSipSessionData)sipSessionData);
	}
	
	public void sipApplicationSessionCreated(SipApplicationSessionKey key) {
		// no-op by default    
	}

	public void sipSessionCreated(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey) {
		// no-op by default    
	}

	public void storeSipApplicationSessionAttributes(
			Map<Object, Object> dataMap,
			OutgoingAttributeGranularitySessionData sessionData) {
		Fqn<String> fqn = null;
		Map<String, Object> map = sessionData.getModifiedSessionAttributes();
		if (map != null) {
			// Duplicate the map with marshalled values
			Map<Object, Object> marshalled = new HashMap<Object, Object>(map
					.size());
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				marshalled.put(entry.getKey(), getMarshalledValue(entry
						.getValue()));
			}
			fqn = delegate.getSipApplicationSessionFqn(combinedPath_, sessionData.getRealId());
			cacheWrapper_.put(fqn, marshalled);
		}

		Set<String> removed = sessionData.getRemovedSessionAttributes();
		if (removed != null) {
			if (fqn == null) {
				fqn = delegate.getSipApplicationSessionFqn(combinedPath_, sessionData.getRealId());
			}
			for (String key : removed) {
				cacheWrapper_.remove(fqn, key);
			}
		}
	}
	
	public void storeSipSessionAttributes(
			Map<Object, Object> dataMap,
			OutgoingAttributeGranularitySessionData sessionData) {
		Fqn<String> fqn = null;
		Map<String, Object> map = sessionData.getModifiedSessionAttributes();
		if (map != null) {
			// Duplicate the map with marshalled values
			Map<Object, Object> marshalled = new HashMap<Object, Object>(map
					.size());
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				marshalled.put(entry.getKey(), getMarshalledValue(entry
						.getValue()));
			}
			
			fqn = delegate.getSipSessionFqn(combinedPath_, ((OutgoingDistributableSipSessionData) sessionData).getSipApplicationSessionKey().toString(),  ((OutgoingDistributableSipSessionData) sessionData).getSipSessionKey().toString());
			cacheWrapper_.put(fqn, marshalled);
		}

		Set<String> removed = sessionData.getRemovedSessionAttributes();
		if (removed != null) {
			if (fqn == null) {
				fqn = delegate.getSipSessionFqn(combinedPath_, ((OutgoingDistributableSipSessionData) sessionData).getSipApplicationSessionKey().toString(),  ((OutgoingDistributableSipSessionData) sessionData).getSipSessionKey().toString());
			}
			for (String key : removed) {
				cacheWrapper_.remove(fqn, key);
			}
		}
	}
}
