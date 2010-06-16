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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingAttributeGranularitySessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipApplicationSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipSessionData;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class AttributeBasedJBossCacheConvergedSipService extends
		AttributeBasedJBossCacheService
		implements
		DistributedCacheConvergedSipManager<OutgoingAttributeGranularitySessionData> {

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
		delegate = new DistributedCacheConvergedSipManagerDelegate(
				this, localManager);
	}

	/**
	 * @param localManager
	 * @param cache
	 */
	public AttributeBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager,
			Cache<Object, Object> cache) {
		super(localManager, cache);
		delegate = new DistributedCacheConvergedSipManagerDelegate(
				this, localManager);
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
		delegate.removeSipSessionLocal(sipAppSessionKey, key);
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
			OutgoingAttributeGranularitySessionData sipApplicationSessionData) {
		delegate
				.storeSipApplicationSessionData((OutgoingDistributableSipApplicationSessionData) sipApplicationSessionData);
	}

	public void storeSipSessionData(
			OutgoingAttributeGranularitySessionData sipSessionData) {
		delegate
				.storeSipSessionData((OutgoingDistributableSipSessionData) sipSessionData);
	}

	public void sipApplicationSessionCreated(String key) {
		// no-op by default
	}

	public void sipSessionCreated(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		// no-op by default
	}

	public void storeSipApplicationSessionAttributes(
			Fqn<String> fqn, 
			OutgoingAttributeGranularitySessionData sessionData) {
		Map<String, Object> map = sessionData.getModifiedSessionAttributes();		
		if (map != null) {
			// Duplicate the map with marshalled values
			Map<Object, Object> marshalled = new HashMap<Object, Object>(map
					.size());
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				marshalled.put(entry.getKey(), getMarshalledValue(entry
						.getValue()));
			}
			cacheWrapper_.put(fqn, marshalled);
		}

		Set<String> removed = sessionData.getRemovedSessionAttributes();
		if (removed != null) {
			for (String key : removed) {
				cacheWrapper_.remove(fqn, key);
			}
		}
	}

	public void storeSipSessionAttributes(Fqn<String> fqn,
			OutgoingAttributeGranularitySessionData sessionData) {
		Map<String, Object> map = sessionData.getModifiedSessionAttributes();
		if (map != null) {
			// Duplicate the map with marshalled values
			Map<Object, Object> marshalled = new HashMap<Object, Object>(map
					.size());
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				marshalled.put(entry.getKey(), getMarshalledValue(entry
						.getValue()));
			}
			cacheWrapper_.put(fqn, marshalled);
		}

		Set<String> removed = sessionData.getRemovedSessionAttributes();
		if (removed != null) {
			for (String key : removed) {
				cacheWrapper_.remove(fqn, key);
			}
		}
	}

	public Cache getJBossCache() {
		return getCache();
	}

	public Object getSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);
		return getUnMarshalledValue(cacheWrapper_.get(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key));
	}

	public Set<String> getSipApplicationSessionAttributeKeys(
			String sipApplicationSessionKey) {
		Set keys = null;
		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);
		try {
			Node<Object, Object> node = getCache().getRoot().getChild(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY));
			if (node != null) {
				keys = node.getKeys();
				keys.removeAll(INTERNAL_KEYS);
			}
		} catch (CacheException e) {
			log_.error(
					"getAttributeKeys(): Exception getting keys for session "
							+ sipApplicationSessionKey, e);
		}

		return keys;
	}

	public Map<String, Object> getSipApplicationSessionAttributes(
			String sipApplicationSessionKey) {
		if (sipApplicationSessionKey == null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> empty = Collections.EMPTY_MAP;
			return empty;
		}

		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);

		Node<Object, Object> node = getCache().getRoot().getChild(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY));
		Map<Object, Object> rawData = node.getData();

		return getSessionAttributes(rawData);
	}

	/**
	 * Returns the session attributes, possibly using the passed in
	 * <code>distributedCacheData</code> as a source.
	 * 
	 * <strong>Note:</strong> This operation may alter the contents of the
	 * passed in map. If this is unacceptable, pass in a defensive copy.
	 */
	protected Map<String, Object> getSessionAttributes(
			Map<Object, Object> distributedCacheData) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		for (Map.Entry<Object, Object> entry : distributedCacheData.entrySet()) {
			if (entry.getKey() instanceof String) {
				attrs.put((String) entry.getKey(), getUnMarshalledValue(entry
						.getValue()));
			}
		}

		return attrs;
	}

	public void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			String key, Object value) {
		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);
		cacheWrapper_.put(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key, getMarshalledValue(value));
	}

	public void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			Map<String, Object> map) {
		// Duplicate the map with marshalled values
		Map<Object, Object> marshalled = new HashMap<Object, Object>(map.size());
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			marshalled
					.put(entry.getKey(), getMarshalledValue(entry.getValue()));
		}

		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);
		cacheWrapper_.put(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), marshalled);
	}

	public Object removeSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);
		if (log_.isTraceEnabled()) {
			log_.trace("Remove attribute from distributed store. Fqn: " + Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY)
					+ " key: " + key);
		}
		return getUnMarshalledValue(cacheWrapper_.remove(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key));
	}

	public void removeSipApplicationSessionAttributeLocal(
			String sipApplicationSessionKey, String key) {
		Fqn<String> fqn = delegate.getSipApplicationSessionFqn(combinedPath_,
				sipApplicationSessionKey);
		if (log_.isTraceEnabled()) {
			log_.trace("Remove attribute from distributed store. Fqn: " + Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY)
					+ " key: " + key);
		}
		cacheWrapper_.removeLocal(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key);
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
		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		return getUnMarshalledValue(cacheWrapper_.get(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key));
	}

	public Set<String> getSipSessionAttributeKeys(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		Set keys = null;
		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		try {
			Node<Object, Object> node = getCache().getRoot().getChild(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY));
			if (node != null) {
				keys = node.getKeys();
				keys.removeAll(INTERNAL_KEYS);
			}
		} catch (CacheException e) {
			log_.error(
					"getAttributeKeys(): Exception getting keys for session "
							+ sipSessionKey, e);
		}

		return keys;
	}

	public Map<String, Object> getSipSessionAttributes(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		if (sipSessionKey == null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> empty = Collections.EMPTY_MAP;
			return empty;
		}

		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);

		Node<Object, Object> node = getCache().getRoot().getChild(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY));
		Map<Object, Object> rawData = node.getData();

		return getSessionAttributes(rawData);
	}

	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key, Object value) {
		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		cacheWrapper_.put(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key, getMarshalledValue(value));
	}

	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, Map<String, Object> map) {
		// Duplicate the map with marshalled values
		Map<Object, Object> marshalled = new HashMap<Object, Object>(map.size());
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			marshalled
					.put(entry.getKey(), getMarshalledValue(entry.getValue()));
		}

		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		cacheWrapper_.put(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), marshalled);
	}

	public Object removeSipSessionAttribute(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		if (log_.isTraceEnabled()) {
			log_.trace("Remove attribute from distributed store. Fqn: " + Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY)
					+ " key: " + key);
		}
		return getUnMarshalledValue(cacheWrapper_.remove(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key));
	}

	public void removeSipSessionAttributeLocal(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		Fqn<String> fqn = delegate.getSipSessionFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		if (log_.isTraceEnabled()) {
			log_.trace("Remove attribute from distributed store. Fqn: " + Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY)
					+ " key: " + key);
		}
		cacheWrapper_.removeLocal(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), key);
	}

	public void setApplicationName(String applicationName) {
		delegate.setApplicationName(applicationName);
	}

	public void setApplicationNameHashed(String applicationNameHashed) {
		delegate.setApplicationNameHashed(applicationNameHashed);
	}

}
