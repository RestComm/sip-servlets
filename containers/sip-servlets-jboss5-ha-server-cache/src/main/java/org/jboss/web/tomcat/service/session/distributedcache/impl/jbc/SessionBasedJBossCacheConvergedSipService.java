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

package org.jboss.web.tomcat.service.session.distributedcache.impl.jbc;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipApplicationSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class SessionBasedJBossCacheConvergedSipService extends
		SessionBasedJBossCacheService
		implements
		DistributedCacheConvergedSipManager<OutgoingSessionGranularitySessionData> {

	private static UnsupportedOperationException UNSUPPORTED = 
	      new UnsupportedOperationException("Attribute operations not supported " +
	            "with ReplicationGranularity " + ReplicationGranularity.SESSION);
	
	DistributedCacheConvergedSipManagerDelegate<OutgoingSessionGranularitySessionData> delegate;

	public SessionBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager)
			throws ClusteringNotSupportedException {
		super(localManager);
		delegate = new DistributedCacheConvergedSipManagerDelegate(
				(AbstractJBossCacheService) this, localManager);
	}

	public SessionBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager,
			Cache<Object, Object> plainCache) {
		super(localManager, plainCache);
		delegate = new DistributedCacheConvergedSipManagerDelegate(
				(AbstractJBossCacheService) this, localManager);
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

	public void evictSipSession(String sipAppSessionKey,
			String key) {
		delegate.evictSipSession(sipAppSessionKey, key);
	}

	public void evictSipApplicationSession(String key) {
		delegate.evictSipApplicationSession(key);
	}

	public void evictSipSession(String sipAppSessionKey,
			String key, String dataOwner) {
		delegate.evictSipSession(sipAppSessionKey, key, dataOwner);
	}

	public void evictSipApplicationSession(String key, String dataOwner) {
		delegate.evictSipApplicationSession(key, dataOwner);
	}

	public IncomingDistributableSessionData getSipSessionData(
			String sipAppSessionKey, String key,
			boolean initialLoad) {
		return delegate.getSipSessionData(sipAppSessionKey, key, initialLoad);
	}

	public IncomingDistributableSessionData getSipSessionData(
			String sipAppSessionKey, String key,
			String dataOwner, boolean includeAttributes) {
		return delegate.getSipSessionData(sipAppSessionKey, key, dataOwner,
				includeAttributes);
	}

	public IncomingDistributableSessionData getSipApplicationSessionData(
			String key, boolean initialLoad) {
		return delegate.getSipApplicationSessionData(key, initialLoad);
	}

	public IncomingDistributableSessionData getSipApplicationSessionData(
			String key, String dataOwner,
			boolean includeAttributes) {
		return delegate.getSipApplicationSessionData(key, dataOwner, includeAttributes);
	}

	public Map<String, String> getSipApplicationSessionKeys() {
		return delegate.getSipApplicationSessionKeys();
	}
	
	public Map<String, String> getSipSessionKeys() {
		return delegate.getSipSessionKeys();
	}	

	public void removeSipApplicationSessionLocal(String key) {
		delegate.removeSipApplicationSessionLocal(key);
	}

	public void removeSipSessionLocal(String sipAppSessionKey,
			String key) {
		delegate.removeSipApplicationSessionLocal(sipAppSessionKey, key);
	}

	public void removeSipApplicationSessionLocal(String key,
			String dataOwner) {
		delegate.removeSipApplicationSessionLocal(key, dataOwner);
	}

	public void removeSipSessionLocal(String sipAppSessionKey,
			String key, String dataOwner) {
		delegate.removeSipSessionLocal(sipAppSessionKey, key, dataOwner);
	}

	public void storeSipApplicationSessionData(
			OutgoingSessionGranularitySessionData sipApplicationSessionData) {
		delegate
				.storeSipApplicationSessionData((OutgoingDistributableSipApplicationSessionData) sipApplicationSessionData);
	}

	public void storeSipSessionData(
			OutgoingSessionGranularitySessionData sipSessionData) {
		delegate
				.storeSipSessionData((OutgoingDistributableSipSessionData) sipSessionData);
	}

	public void storeSipApplicationSessionAttributes(Fqn<String> fqn,
			OutgoingSessionGranularitySessionData sessionData) {
		if(sessionData.getSessionAttributes() != null) {
			if (log_.isDebugEnabled()) {
				log_
					.debug("storeSipApplicationSessionAttributes(): putting sip app session attributes "
							+ sessionData.getSessionAttributes());
				String attribLog = "\n";
				for(String name : sessionData.getSessionAttributes().keySet()) {
					attribLog += "ATTRIBUTE(" + name + ") -> " + sessionData.getSessionAttributes().get(name) + "\n";
				}
				log_.debug(attribLog);
			}
			cacheWrapper_.put(fqn, ATTRIBUTE_KEY.toString(), getMarshalledValue(sessionData.getSessionAttributes()));
		}
	}

	public void storeSipSessionAttributes(Fqn<String> fqn,
			OutgoingSessionGranularitySessionData sessionData) {
		if(sessionData.getSessionAttributes() != null) {
			if (log_.isDebugEnabled()) {
				log_.debug("storeSipSessionAttributes(): putting sip session attributes "
								+ sessionData.getSessionAttributes());
				String attribLog = "\n";
				for(String name : sessionData.getSessionAttributes().keySet()) {
					attribLog += "ATTRIBUTE(" + name + ") -> " + sessionData.getSessionAttributes().get(name) + "\n";
				}
				log_.debug(attribLog);
			}
			cacheWrapper_.put(fqn, ATTRIBUTE_KEY.toString(), getMarshalledValue(sessionData.getSessionAttributes()));
		}
	}

	public void sipApplicationSessionCreated(String key) {
		// no-op by default
	}

	public void sipSessionCreated(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		// no-op by default
	}

	public Cache getJBossCache() {
		return getCache();
	}

	public Object getSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		throw UNSUPPORTED;
	}

	public Set<String> getSipApplicationSessionAttributeKeys(
			String sipApplicationSessionKey) {
		throw UNSUPPORTED;
	}

	public Map<String, Object> getSipApplicationSessionAttributes(
			String sipApplicationSessionKey) {
		throw UNSUPPORTED;
	}

	public void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			String key, Object value) {
		throw UNSUPPORTED;
	}

	public void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			Map<String, Object> map) {
		throw UNSUPPORTED;		
	}

	public Object removeSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		throw UNSUPPORTED;
	}

	public void removeSipApplicationSessionAttributeLocal(
			String sipApplicationSessionKey, String key) {
		throw UNSUPPORTED;
	}

	public void removeSipApplicationSession(String sipApplicationSessionKey) {
		delegate.removeSipApplicationSession(sipApplicationSessionKey);
	}

	public void removeSipSession(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		delegate.removeSipSession(sipApplicationSessionKey, sipSessionKey);
	}

	public Object getSipSessionAttribute(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		throw UNSUPPORTED;
	}

	public Set<String> getSipSessionAttributeKeys(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		throw UNSUPPORTED;
	}

	public Map<String, Object> getSipSessionAttributes(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		throw UNSUPPORTED;
	}

	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key, Object value) {
		throw UNSUPPORTED;
	}

	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, Map<String, Object> map) {
		throw UNSUPPORTED;
	}

	public Object removeSipSessionAttribute(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		throw UNSUPPORTED;
	}

	public void removeSipSessionAttributeLocal(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		throw UNSUPPORTED;
	}
	
	public void setApplicationName(String applicationName) {
		delegate.setApplicationName(applicationName);
	}

	public void setApplicationNameHashed(String applicationNameHashed) {
		delegate.setApplicationNameHashed(applicationNameHashed);
	}
	
	public Map<String, Object> getConvergedSessionAttributes(String realId,
			Map<Object, Object> distributedCacheData) {
		Map<String, Object> result = null;
		if(distributedCacheData != null) {
			result = (Map<String, Object>) getUnMarshalledValue(distributedCacheData.get(ATTRIBUTE_KEY.toString()));
		}
	    return result == null ? Collections.EMPTY_MAP : result;
	}
	
	@Override
	protected void storeSessionAttributes(Map<Object, Object> dataMap,
			OutgoingSessionGranularitySessionData sessionData) {
		if(log_.isTraceEnabled()) {
			Map<String, Object> attrs = sessionData.getSessionAttributes();
			if(attrs != null) {
				for (Entry<String, Object> entry : attrs.entrySet()) {
					log_.trace("attribute key " + entry.getKey() + ", value=" + entry.getValue());
				}
			} else {
				log_.trace("attributes null, not replicating");
			}
		}		
		super.storeSessionAttributes(dataMap, sessionData);
	}
}
