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
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

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

	public void evictSession(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key) {
		delegate.evictSession(sipAppSessionKey, key);
	}

	public void evictSession(SipApplicationSessionKey key) {
		delegate.evictSession(key);
	}

	public void evictSession(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key, String dataOwner) {
		delegate.evictSession(sipAppSessionKey, key, dataOwner);
	}

	public void evictSession(SipApplicationSessionKey key, String dataOwner) {
		delegate.evictSession(key, dataOwner);
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey sipAppSessionKey, SipSessionKey key,
			boolean initialLoad) {
		return delegate.getSessionData(sipAppSessionKey, key, initialLoad);
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey sipAppSessionKey, SipSessionKey key,
			String dataOwner, boolean includeAttributes) {
		return delegate.getSessionData(sipAppSessionKey, key, dataOwner,
				includeAttributes);
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

	public void removeSessionLocal(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key) {
		delegate.removeSessionLocal(sipAppSessionKey, key);
	}

	public void removeSessionLocal(SipApplicationSessionKey key,
			String dataOwner) {
		delegate.removeSessionLocal(key, dataOwner);
	}

	public void removeSessionLocal(SipApplicationSessionKey sipAppSessionKey,
			SipSessionKey key, String dataOwner) {
		delegate.removeSessionLocal(sipAppSessionKey, key, dataOwner);
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

	public void sipApplicationSessionCreated(SipApplicationSessionKey key) {
		// no-op by default
	}

	public void sipSessionCreated(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey) {
		// no-op by default
	}

	public Cache getJBossCache() {
		return getCache();
	}

	public static Fqn<String> getFieldFqn(String contextHostPath,
			SipApplicationSessionKey sipApplicationSessionKey,
			String attributeKey) {
		List<String> list = new ArrayList<String>(5);
		list.add(DistributedCacheConvergedSipManagerDelegate.SIPSESSION);
		list.add(contextHostPath);
		list.add(sipApplicationSessionKey.getId());
		list.add(ATTRIBUTE);
		// Guard against string with delimiter.
		breakKeys(attributeKey, list);
		return Fqn.fromList(list, true);
	}

	public static Fqn<String> getFieldFqn(String contextHostPath,
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey, String attributeKey) {
		List<String> list = new ArrayList<String>(5);
		list.add(DistributedCacheConvergedSipManagerDelegate.SIPSESSION);
		list.add(contextHostPath);
		list.add(sipApplicationSessionKey.getId());
		list.add(SessionManagerUtil.getSipSessionHaKey(sipSessionKey));
		list.add(ATTRIBUTE);
		// Guard against string with delimiter.
		breakKeys(attributeKey, list);
		return Fqn.fromList(list, true);
	}

	public static Fqn<String> getAttributeFqn(String contextHostPath,
			SipApplicationSessionKey sipApplicationSessionKey) {
		String[] objs = new String[] { SESSION, contextHostPath,
				sipApplicationSessionKey.getId(), ATTRIBUTE };
		return Fqn.fromList(Arrays.asList(objs), true);
	}

	public static Fqn<String> getAttributeFqn(String contextHostPath,
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey) {
		String[] objs = new String[] { SESSION, contextHostPath,
				sipApplicationSessionKey.getId(),
				SessionManagerUtil.getSipSessionHaKey(sipSessionKey), ATTRIBUTE };
		return Fqn.fromList(Arrays.asList(objs), true);
	}

	private static void breakKeys(String key, List<String> list) {
		StringTokenizer token = new StringTokenizer(key, FQN_DELIMITER);
		while (token.hasMoreTokens()) {
			list.add(token.nextToken());
		}
	}

	public Object getAttribute(
			SipApplicationSessionKey sipApplicationSessionKey, String key) {
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

	public Set<String> getAttributeKeys(
			SipApplicationSessionKey sipApplicationSessionKey) {
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

	public Map<String, Object> getAttributes(
			SipApplicationSessionKey sipApplicationSessionKey) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		Set<String> keys = getAttributeKeys(sipApplicationSessionKey);
		for (String key : keys) {
			attrs.put(key, getAttribute(sipApplicationSessionKey, key));
		}
		return attrs;
	}

	public void putAttribute(SipApplicationSessionKey sipApplicationSessionKey,
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

	public void putAttribute(SipApplicationSessionKey sipApplicationSessionKey,
			Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			putAttribute(sipApplicationSessionKey, entry.getKey(), entry
					.getValue());
		}
	}

	public Object removeAttribute(
			SipApplicationSessionKey sipApplicationSessionKey, String key) {
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

	public void removeAttributeLocal(
			SipApplicationSessionKey sipApplicationSessionKey, String key) {
		if (log_.isTraceEnabled()) {
			log_.trace("removePojoLocal(): session id: "
					+ sipApplicationSessionKey + " key: " + key);
		}

		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				key);
		cacheWrapper_.removeLocal(fqn);
	}

	public void removeSession(SipApplicationSessionKey sipApplicationSessionKey) {
		delegate.removeSession(sipApplicationSessionKey);
	}

	public void removeSession(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey) {
		delegate.removeSession(sipApplicationSessionKey, sipSessionKey);
	}

	public Object getAttribute(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey, String key) {
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

	public Set<String> getAttributeKeys(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey) {
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

	public Map<String, Object> getAttributes(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		Set<String> keys = getAttributeKeys(sipApplicationSessionKey,
				sipSessionKey);
		for (String key : keys) {
			attrs.put(key, getAttribute(sipApplicationSessionKey,
					sipSessionKey, key));
		}
		return attrs;
	}

	public void putAttribute(SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey, String key, Object value) {
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

	public void putAttribute(SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey, Map<String, Object> map) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			putAttribute(sipApplicationSessionKey, sipSessionKey, entry
					.getKey(), entry.getValue());
		}
	}

	public Object removeAttribute(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey, String key) {
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

	public void removeAttributeLocal(
			SipApplicationSessionKey sipApplicationSessionKey,
			SipSessionKey sipSessionKey, String key) {
		if (log_.isTraceEnabled()) {
			log_.trace("removePojoLocal(): session id: " + sipSessionKey
					+ " key: " + key);
		}

		Fqn<String> fqn = getFieldFqn(combinedPath_, sipApplicationSessionKey,
				sipSessionKey, key);
		cacheWrapper_.removeLocal(fqn);
	}
}
