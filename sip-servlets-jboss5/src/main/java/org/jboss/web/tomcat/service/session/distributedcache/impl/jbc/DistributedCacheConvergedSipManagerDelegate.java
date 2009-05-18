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

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.Context;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.pojo.impl.InternalConstant;
import org.jboss.logging.Logger;
import org.jboss.web.tomcat.service.session.ClusteredManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributedCacheConvergedSipManagerDelegate {
	
	protected Logger log_ = Logger.getLogger(getClass());
	public static final String SIPSESSION = "SIPSESSION";
	protected String sipApplicationName; 
	private SipCacheListener sipCacheListener_;
	private SipPassivationListener sipPassivationListener_;
	
	AbstractJBossCacheService<OutgoingDistributableSessionData> jBossCacheService;
	
	public DistributedCacheConvergedSipManagerDelegate(AbstractJBossCacheService<OutgoingDistributableSessionData> jBossCacheService) {
		this.jBossCacheService = jBossCacheService;
	}
	
	public void start() {
		Context webapp = (Context) ((ClusteredManager<OutgoingDistributableSessionData>) jBossCacheService
				.getManager()).getContainer();
		if (webapp instanceof SipContext) {
			SipContext sipApp = (SipContext) webapp;
			// As per JSR 289, application name should be unique
			sipApplicationName = sipApp.getApplicationName();

			sipCacheListener_ = new SipCacheListener(
					jBossCacheService.cacheWrapper_, jBossCacheService
							.getManager(), jBossCacheService.combinedPath_,
					Util.getReplicationGranularity(jBossCacheService
							.getManager()), sipApplicationName);
			jBossCacheService.getCache().addCacheListener(sipCacheListener_);

			if (jBossCacheService.getManager().isPassivationEnabled()) {
				log_.debug("Passivation is enabled");
				sipPassivationListener_ = new SipPassivationListener(
						jBossCacheService.getManager(),
						jBossCacheService.combinedPath_, sipApplicationName);
				jBossCacheService.getCache().addCacheListener(
						sipPassivationListener_);
			}
		}
	}
	
	public void stop() {
		if (sipCacheListener_ != null) {
			jBossCacheService.getCache().removeCacheListener(sipCacheListener_);
			if (sipPassivationListener_ != null) {
				jBossCacheService.getCache().removeCacheListener(
						sipPassivationListener_);
			}
		}
	}
	
	public void evictSession(SipSessionKey key) {
		evictSession(key, null);      
	}

	public void evictSession(SipApplicationSessionKey key) {
		evictSession(key, null);      
	}

	public void evictSession(SipSessionKey key, String dataOwner) {
		Fqn<String> fqn = dataOwner == null ? getSipSessionFqn(jBossCacheService.combinedPath_,
				null, key.toString()) : getBuddyBackupSipSessionFqn(dataOwner, jBossCacheService.combinedPath_,
				null, key.toString());
		if (log_.isTraceEnabled()) {
			log_
					.trace("evictSession(): evicting session from my distributed store. Fqn: "
							+ fqn);
		}
		jBossCacheService.cacheWrapper_.evictSubtree(fqn);
	}

	public void evictSession(SipApplicationSessionKey key, String dataOwner) {
		Fqn<String> fqn = dataOwner == null ? getSipApplicationSessionFqn(jBossCacheService.combinedPath_,
				key.toString()) : getBuddyBackupSipApplicationSessionFqn(dataOwner, jBossCacheService.combinedPath_,
						key.toString());
		if (log_.isTraceEnabled()) {
			log_
					.trace("evictSession(): evicting session from my distributed store. Fqn: "
							+ fqn);
		}
		jBossCacheService.cacheWrapper_.evictSubtree(fqn);
	}

	public IncomingDistributableSessionData getSessionData(SipSessionKey key,
			boolean initialLoad) {
		if (key == null) {
			throw new IllegalArgumentException("Null key");
		}

		Fqn<String> fqn = getSipSessionFqn(jBossCacheService.combinedPath_, null, key.toString());
		Map<Object, Object> sessionData = jBossCacheService.cacheWrapper_.getData(fqn, true);

		if (sessionData == null) {
			// Requested session is no longer in the cache; return null
			return null;
		}

		if (initialLoad) {
			jBossCacheService.setupSessionRegion(fqn);
		}

		IncomingDistributableSessionData dsd = null;

		try {
			dsd = jBossCacheService.getDistributableSessionData(key.toString(), sessionData, true);
		} catch (Exception e) {
			log_.warn("Problem accessing session data : " + e.getClass() + " "
					+ e.getLocalizedMessage());
			// Clean up
			removeSessionLocal(key);
			return null;
		}

		return dsd;
	}

	public IncomingDistributableSessionData getSessionData(SipSessionKey key,
			String dataOwner, boolean includeAttributes) {
		Fqn<String> fqn = dataOwner == null ? getSipSessionFqn(jBossCacheService.combinedPath_,
				null, key.toString()) : getBuddyBackupSipSessionFqn(dataOwner, jBossCacheService.combinedPath_,
						null, key.toString());
		Map<Object, Object> distributedCacheData = jBossCacheService.cacheWrapper_.getData(fqn,
				false);
		return jBossCacheService.getDistributableSessionData(key.toString(), distributedCacheData,
				includeAttributes);
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, boolean initialLoad) {
		if (key == null) {
			throw new IllegalArgumentException("Null key");
		}

		Fqn<String> fqn = getSipApplicationSessionFqn(jBossCacheService.combinedPath_, key.toString());
		Map<Object, Object> sessionData = jBossCacheService.cacheWrapper_.getData(fqn, true);

		if (sessionData == null) {
			// Requested session is no longer in the cache; return null
			return null;
		}

		if (initialLoad) {
			jBossCacheService.setupSessionRegion(fqn);
		}

		IncomingDistributableSessionData dsd = null;

		try {
			dsd = jBossCacheService.getDistributableSessionData(key.toString(), sessionData, true);
		} catch (Exception e) {
			log_.warn("Problem accessing session data : " + e.getClass() + " "
					+ e.getLocalizedMessage());
			// Clean up
			removeSessionLocal(key);
			return null;
		}

		return dsd;
	}

	public IncomingDistributableSessionData getSessionData(
			SipApplicationSessionKey key, String dataOwner,
			boolean includeAttributes) {
		Fqn<String> fqn = dataOwner == null ? getSipApplicationSessionFqn(jBossCacheService.combinedPath_,
				key.toString()) : getBuddyBackupSipApplicationSessionFqn(dataOwner, jBossCacheService.combinedPath_,
				key.toString());
		Map<Object, Object> distributedCacheData = jBossCacheService.cacheWrapper_.getData(fqn,
				false);
		return jBossCacheService.getDistributableSessionData(key.toString(), distributedCacheData,
				includeAttributes);
	}

	public Map<SipApplicationSessionKey, String> getSipApplicationSessionKeys() {
		Map<SipApplicationSessionKey, String> result = new HashMap<SipApplicationSessionKey, String>();

		Fqn<String> sipappFqn = getSipappFqn();

		Node<Object, Object> bbRoot = jBossCacheService.getCache().getRoot()
				.getChild(jBossCacheService.BUDDY_BACKUP_FQN);
		if (bbRoot != null) {
			Set<Node<Object, Object>> owners = bbRoot.getChildren();
			if (owners != null) {
				for (Node<Object, Object> owner : owners) {
					@SuppressWarnings("unchecked")
					Node sipRoot = owner.getChild(sipappFqn);
					if (sipRoot != null) {
						@SuppressWarnings("unchecked")
						Set<String> ids = sipRoot.getChildrenNames();
						storeSipApplicationSessionOwners(ids, (String) owner.getFqn()
								.getLastElement(), result);
					}
				}
			}
		}

		storeSipApplicationSessionOwners(jBossCacheService.getChildrenNames(sipappFqn), null,
				result);

		return result;
	}

	
	protected Fqn<String> getSipappFqn() {
		// /SIPSESSION/sipAppPath_hostName
		String[] objs = new String[] { SIPSESSION, jBossCacheService.combinedPath_ };
		return Fqn.fromList(Arrays.asList(objs), true);
	}

	private void storeSipSessionOwners(Set<String> ids, String owner,
			Map<SipSessionKey, String> map) {
		if (ids != null) {
			for (String id : ids) {
				if (!InternalConstant.JBOSS_INTERNAL_STRING.equals(id)) {
					SipSessionKey key = null;
					try {
						key = SessionManagerUtil.parseSipSessionKey(id);
					} catch (ParseException e) {
						//should never happen
						log_.error("An unexpected exception happened on parsing the following sip session key " + id, e);
						return;
					}
					map.put(key, owner);
				}
			}
		}
	}
	
	private void storeSipApplicationSessionOwners(Set<String> ids, String owner,
			Map<SipApplicationSessionKey, String> map) {
		if (ids != null) {
			for (String id : ids) {
				if (!InternalConstant.JBOSS_INTERNAL_STRING.equals(id)) {
					SipApplicationSessionKey key = null;
					try {
						key = SessionManagerUtil.parseSipApplicationSessionKey(id);
					} catch (ParseException e) {
						//should never happen
						log_.error("An unexpected exception happened on parsing the following sip application session key " + id, e);
						return;
					}
					map.put(key, owner);
				}
			}
		}
	}
	
	public Map<SipSessionKey, String> getSipSessionKeys() {
		Map<SipSessionKey, String> result = new HashMap<SipSessionKey, String>();

		Fqn<String> sipappFqn = getSipappFqn();

		Node<Object, Object> bbRoot = jBossCacheService.getCache().getRoot()
				.getChild(jBossCacheService.BUDDY_BACKUP_FQN);
		if (bbRoot != null) {
			Set<Node<Object, Object>> owners = bbRoot.getChildren();
			if (owners != null) {
				for (Node<Object, Object> owner : owners) {
					@SuppressWarnings("unchecked")
					Node sipRoot = owner.getChild(sipappFqn);
					if (sipRoot != null) {
						@SuppressWarnings("unchecked")
						Set<String> ids = sipRoot.getChildrenNames();
						storeSipSessionOwners(ids, (String) owner.getFqn()
								.getLastElement(), result);
					}
				}
			}
		}

		storeSipSessionOwners(jBossCacheService.getChildrenNames(sipappFqn), null,
				result);

		return result;
	}

	public void removeSessionLocal(SipApplicationSessionKey key) {
		Fqn<String> fqn = getSipApplicationSessionFqn(
				jBossCacheService.combinedPath_, key.toString());
		if (log_.isTraceEnabled()) {
			log_
					.trace("Remove session from my own distributed store only. Fqn: "
							+ fqn);
		}

		jBossCacheService.cacheWrapper_.removeLocal(fqn);

		jBossCacheService.removeSessionRegion(key.toString(), fqn);
	}

	public void removeSessionLocal(SipSessionKey key) {
		Fqn<String> fqn = getSipSessionFqn(
				jBossCacheService.combinedPath_, null, key.toString());
		if (log_.isTraceEnabled()) {
			log_
					.trace("Remove session from my own distributed store only. Fqn: "
							+ fqn);
		}

		jBossCacheService.cacheWrapper_.removeLocal(fqn);

		jBossCacheService.removeSessionRegion(key.toString(), fqn);
	}
	
	public void removeSessionLocal(SipApplicationSessionKey key,
			String dataOwner) {
		if (dataOwner == null) {
			removeSessionLocal(key);
		} else {
			Fqn<String> fqn = getBuddyBackupSipApplicationSessionFqn(dataOwner,
					jBossCacheService.combinedPath_, key.toString());
			if (log_.isTraceEnabled()) {
				log_
						.trace("Remove session from my own distributed store only. Fqn: "
								+ fqn);
			}
			jBossCacheService.cacheWrapper_.removeLocal(fqn);
		}
	}

	public void removeSessionLocal(SipSessionKey key, String dataOwner) {
		if (dataOwner == null) {
			removeSessionLocal(key);
		} else {
			Fqn<String> fqn = getBuddyBackupSipSessionFqn(dataOwner,
					jBossCacheService.combinedPath_, null, key.toString());
			if (log_.isTraceEnabled()) {
				log_
						.trace("Remove session from my own distributed store only. Fqn: "
								+ fqn);
			}
			jBossCacheService.cacheWrapper_.removeLocal(fqn);
		}
	}
	
	public Fqn<String> getSipSessionFqn(String contextHostPath,
			String appSessionId, String sessionId) {
		// /SIPSESSION/contextHostPath/sipApplicationName/id/sessionId
		String[] objs = new String[] { SIPSESSION, contextHostPath, sipApplicationName, appSessionId, sessionId };
		return Fqn.fromList(Arrays.asList(objs), true);
	}
	
	public Fqn<String> getSipApplicationSessionFqn(String contextHostPath,
			String sessionId) {
		// /SIPSESSION/contextHostPath/sipApplicationName/id
		String[] objs = new String[] { SIPSESSION, contextHostPath, sipApplicationName, sessionId };
		return Fqn.fromList(Arrays.asList(objs), true);
	}	
	
	public Fqn<String> getBuddyBackupSipSessionFqn(String dataOwner,
			String contextHostPath, String appSessionId, String sessionId) {
		String[] objs = new String[] { jBossCacheService.BUDDY_BACKUP,
				dataOwner, SIPSESSION, contextHostPath,
				sipApplicationName, appSessionId, sessionId };
		return Fqn.fromList(Arrays.asList(objs), true);
	}
	
	public Fqn<String> getBuddyBackupSipApplicationSessionFqn(String dataOwner,
			String contextHostPath, String sessionId) {
		String[] objs = new String[] { jBossCacheService.BUDDY_BACKUP,
				dataOwner, SIPSESSION, contextHostPath,
				sipApplicationName, sessionId };
		return Fqn.fromList(Arrays.asList(objs), true);
	} 
}
