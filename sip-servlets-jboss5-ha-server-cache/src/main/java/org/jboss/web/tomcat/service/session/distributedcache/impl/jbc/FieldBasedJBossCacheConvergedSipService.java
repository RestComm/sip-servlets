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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.pojo.PojoCache;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipApplicationSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSipSessionData;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class FieldBasedJBossCacheConvergedSipService extends
		FieldBasedJBossCacheService implements
		DistributedCacheConvergedSipManager<OutgoingDistributableSessionData> {

	DistributedCacheConvergedSipManagerDelegate<OutgoingDistributableSessionData> delegate;

	private final PojoCache pojoCache_;

	/**
	 * @param localManager
	 * @throws ClusteringNotSupportedException
	 */
	public FieldBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager)
			throws ClusteringNotSupportedException {
		this(localManager, Util.findPojoCache(Util
				.getCacheConfigName(localManager)));
		this.cacheConfigName_ = Util.getCacheConfigName(localManager);
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

	/**
	 * @param localManager
	 * @param cache
	 */
	public FieldBasedJBossCacheConvergedSipService(
			LocalDistributableSessionManager localManager, PojoCache cache) {
		super(localManager, cache);
		this.pojoCache_ = cache;
		delegate = new DistributedCacheConvergedSipManagerDelegate(
				(AbstractJBossCacheService) this, localManager);
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
			OutgoingDistributableSessionData sipApplicationSessionData) {
		delegate
				.storeSipApplicationSessionData((OutgoingDistributableSipApplicationSessionData) sipApplicationSessionData);
	}

	public void storeSipSessionData(
			OutgoingDistributableSessionData sipSessionData) {
		delegate
				.storeSipSessionData((OutgoingDistributableSipSessionData) sipSessionData);
	}

	public void storeSipApplicationSessionAttributes(
			Map<Object, Object> dataMap,
			OutgoingDistributableSessionData sessionData) {
		this.storeSessionAttributes(dataMap, sessionData);
	}

	public void storeSipSessionAttributes(Map<Object, Object> dataMap,
			OutgoingDistributableSessionData sessionData) {
		this.storeSessionAttributes(dataMap, sessionData);
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

	public static Fqn<String> getFieldFqn(String contextHostPath,
			String sipApplicationSessionKey,
			String attributeKey) {
		List<String> list = new ArrayList<String>(5);
		list.add(DistributedCacheConvergedSipManagerDelegate.SIPSESSION);
		list.add(contextHostPath);
		list.add(sipApplicationSessionKey);
		list.add(ATTRIBUTE);
		// Guard against string with delimiter.
		breakKeys(attributeKey, list);
		return Fqn.fromList(list, true);
	}

	public static Fqn<String> getFieldFqn(String contextHostPath,
			String sipApplicationSessionKey,
			String sipSessionKey, String attributeKey) {
		List<String> list = new ArrayList<String>(5);
		list.add(DistributedCacheConvergedSipManagerDelegate.SIPSESSION);
		list.add(contextHostPath);
		list.add(sipApplicationSessionKey);
		list.add(sipSessionKey);
		list.add(ATTRIBUTE);
		// Guard against string with delimiter.
		breakKeys(attributeKey, list);
		return Fqn.fromList(list, true);
	}

	public static Fqn<String> getAttributeFqn(String contextHostPath,
			String sipApplicationSessionKey) {
		String[] objs = new String[] { SESSION, contextHostPath,
				sipApplicationSessionKey, ATTRIBUTE };
		return Fqn.fromList(Arrays.asList(objs), true);
	}

	public static Fqn<String> getAttributeFqn(String contextHostPath,
			String sipApplicationSessionKey,
			String sipSessionKey) {
		String[] objs = new String[] { SESSION, contextHostPath,
				sipApplicationSessionKey,
				sipSessionKey, ATTRIBUTE };
		return Fqn.fromList(Arrays.asList(objs), true);
	}

	private static void breakKeys(String key, List<String> list) {
		StringTokenizer token = new StringTokenizer(key, FQN_DELIMITER);
		while (token.hasMoreTokens()) {
			list.add(token.nextToken());
		}
	}

	public Object getSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				key);
		if (log_.isTraceEnabled()) {
			log_.trace("getPojo(): session id: " + sipApplicationSessionKey
					+ " key: " + key + " fqn: " + fqn);
		}

		try {
			return pojoCache_.find(fqn);
		} catch (CacheException e) {
			throw new RuntimeException(
					"Exception occurred in PojoCache find ... ", e);
		}
	}

	public Set<String> getSipApplicationSessionAttributeKeys(
			String sipApplicationSessionKey) {
		Set<String> keys = null;
		Fqn<String> fqn = getAttributeFqn(combinedPath_,
				sipApplicationSessionKey);
		try {
			keys = getChildrenNames(fqn);
		} catch (CacheException e) {
			log_.error(
					"getAttributeKeys(): Exception getting keys for session "
							+ sipApplicationSessionKey, e);
		}

		return keys;
	}

	public Map<String, Object> getSipApplicationSessionAttributes(
			String sipApplicationSessionKey) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		Set<String> keys = getAttributeKeys(sipApplicationSessionKey);
		for (String key : keys) {
			attrs.put(key, getAttribute(sipApplicationSessionKey, key));
		}
		return attrs;
	}

	public void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			String key, Object value) {
		if (log_.isTraceEnabled()) {
			log_.trace("putAttribute(): session id: "
					+ sipApplicationSessionKey + " key: " + key + " object: "
					+ value.toString());
		}

		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				key);
		try {
			pojoCache_.attach(fqn.toString(), value);
		} catch (CacheException e) {
			throw new RuntimeException(
					"Exception occurred in PojoCache attach ... ", e);
		}
	}

	public void putSipApplicationSessionAttribute(String sipApplicationSessionKey,
			Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			putAttribute(sipApplicationSessionKey, entry.getKey(), entry
					.getValue());
		}
	}

	public Object removeSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		if (log_.isTraceEnabled()) {
			log_.trace("removePojo(): session id: " + sipApplicationSessionKey
					+ " key: " + key);
		}
		// Construct the fqn.
		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				key);
		try {
			return pojoCache_.detach(fqn.toString());
		} catch (CacheException e) {
			throw new RuntimeException(
					"Exception occurred in PojoCache detach ... ", e);
		}
	}

	public void removeSipApplicationSessionAttributeLocal(
			String sipApplicationSessionKey, String key) {
		if (log_.isTraceEnabled()) {
			log_.trace("removePojoLocal(): session id: "
					+ sipApplicationSessionKey + " key: " + key);
		}

		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				key);
		cacheWrapper_.removeLocal(fqn);
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
		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				sipSessionKey, key);
		if (log_.isTraceEnabled()) {
			log_.trace("getPojo(): session id: " + sipSessionKey + " key: "
					+ key + " fqn: " + fqn);
		}

		try {
			return pojoCache_.find(fqn);
		} catch (CacheException e) {
			throw new RuntimeException(
					"Exception occurred in PojoCache find ... ", e);
		}
	}

	public Set<String> getSipSessionAttributeKeys(
			String sipApplicationSessionKey,
			String sipSessionKey) {
		Set<String> keys = null;
		Fqn<String> fqn = getAttributeFqn(combinedPath_,
				sipApplicationSessionKey, sipSessionKey);
		try {
			keys = getChildrenNames(fqn);
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
		Map<String, Object> attrs = new HashMap<String, Object>();
		Set<String> keys = getSipSessionAttributeKeys(sipApplicationSessionKey,
				sipSessionKey);
		for (String key : keys) {
			attrs.put(key, getSipSessionAttribute(sipApplicationSessionKey,
					sipSessionKey, key));
		}
		return attrs;
	}

	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key, Object value) {
		if (log_.isTraceEnabled()) {
			log_.trace("putAttribute(): session id: " + sipSessionKey
					+ " key: " + key + " object: " + value.toString());
		}

		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				sipSessionKey, key);
		try {
			pojoCache_.attach(fqn.toString(), value);
		} catch (CacheException e) {
			throw new RuntimeException(
					"Exception occurred in PojoCache attach ... ", e);
		}
	}

	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			putSipSessionAttribute(sipApplicationSessionKey, sipSessionKey, entry
					.getKey(), entry.getValue());
		}
	}

	public Object removeSipSessionAttribute(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		if (log_.isTraceEnabled()) {
			log_.trace("removePojo(): session id: " + sipSessionKey + " key: "
					+ key);
		}
		// Construct the fqn.
		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				sipSessionKey, key);
		try {
			return pojoCache_.detach(fqn.toString());
		} catch (CacheException e) {
			throw new RuntimeException(
					"Exception occurred in PojoCache detach ... ", e);
		}
	}

	public void removeSipSessionAttributeLocal(
			String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		if (log_.isTraceEnabled()) {
			log_.trace("removePojoLocal(): session id: " + sipSessionKey
					+ " key: " + key);
		}

		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				sipSessionKey, key);
		cacheWrapper_.removeLocal(fqn);
	}
	
	public void setApplicationName(String applicationName) {
		delegate.setApplicationName(applicationName);
	}

	public void setApplicationNameHashed(String applicationNameHashed) {
		delegate.setApplicationNameHashed(applicationNameHashed);
	}
}
