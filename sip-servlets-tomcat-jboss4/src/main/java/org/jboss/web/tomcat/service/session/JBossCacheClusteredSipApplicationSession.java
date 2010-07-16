/*
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

import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.startup.SipContext;


/**
 * Common superclass of ClusteredSipApplicationSession types that use JBossCache
 * as their distributed cache.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class JBossCacheClusteredSipApplicationSession extends ClusteredSipApplicationSession {
	private static transient final Logger logger = Logger.getLogger(JBossCacheClusteredSipApplicationSession.class);

	/**
	 * Our proxy to the cache.
	 */
	protected transient ConvergedJBossCacheService proxy_;
	   
	protected JBossCacheClusteredSipApplicationSession(SipApplicationSessionKey key,
			SipContext sipContext) {
		super(key, sipContext, ((JBossCacheSipManager)sipContext.getSipManager()).getUseJK());
		int maxUnrep = ((JBossCacheSipManager)sipContext.getSipManager()).getMaxUnreplicatedInterval() * 1000;
	    setMaxUnreplicatedInterval(maxUnrep);
		establishProxy();
	}
	
	/**
	 * Initialize fields marked as transient after loading this session from the
	 * distributed store
	 * 
	 * @param manager
	 *            the manager for this session
	 */
	public void initAfterLoad(JBossCacheSipManager manager) {		
		sipContext = (SipContext) manager.getContainer();		
		
		establishProxy();

		populateMetaData();
		// Since attribute map may be transient, we may need to populate it
		// from the underlying store.
		populateAttributes();

		// Notify all attributes of type SipApplicationSessionActivationListener 
		this.activate();

		// We are no longer outdated vis a vis distributed cache
		clearOutdated();
	}

	/**
	 * Gets a reference to the JBossCacheService.
	 */
	protected void establishProxy() {
		if (proxy_ == null) {
			proxy_ = (ConvergedJBossCacheService)((JBossCacheSipManager) getSipContext().getSipManager()).getCacheService();

			// still null???
			if (proxy_ == null) {
				throw new RuntimeException(
						"JBossCacheClusteredSession: Cache service is null.");
			}
		}
	}

	protected abstract void populateAttributes();

	/**
	 * Override the superclass to additionally reset this class' fields.
	 * <p>
	 * <strong>NOTE:</strong> It is not anticipated that this method will be
	 * called on a ClusteredSession, but we are overriding the method to be
	 * thorough.
	 * </p>
	 */
//	public void recycle() {
//		super.recycle();
//
//		proxy_ = null;
//	}
	
	protected void populateMetaData() {
		final String sipAppSessionId = getHaId();
		Long ct = (Long) proxy_.getSipApplicationSessionMetaData(sipAppSessionId, CREATION_TIME);
		if(ct != null) {
			creationTime = ct;
		}
		Integer ip = (Integer) proxy_.getSipApplicationSessionMetaData(sipAppSessionId, INVALIDATION_POLICY);
		if(ip != null) {
			invalidationPolicy = ip;
		}
		Boolean valid = (Boolean) proxy_.getSipApplicationSessionMetaData(sipAppSessionId, IS_VALID);
		if(valid != null) {
			setValid(valid);
		}
		sipSessions.clear();
		SipSessionKey[] sipSessionKeys = (SipSessionKey[]) proxy_.getSipApplicationSessionMetaData(sipAppSessionId, SIP_SESSIONS);
		if(sipSessionKeys != null && sipSessionKeys.length > 0) {
			for (SipSessionKey sipSessionKey : sipSessionKeys) {
				sipSessionKey.setApplicationName(proxy_.getSipApplicationName());
				sipSessionKey.setApplicationName(sipAppSessionId);
				sipSessionKey.computeToString();
				sipSessions.add(sipSessionKey);
			}		
		}
		String[] httpSessionIds = (String[]) proxy_.getSipApplicationSessionMetaData(sipAppSessionId, HTTP_SESSIONS);
		if(httpSessionIds != null && httpSessionIds.length > 0) {
			if(httpSessions == null) {
				httpSessions = new CopyOnWriteArraySet<String>();
			} else {
				httpSessions.clear();
			}
			for (String httpSessionId : httpSessionIds) {
				httpSessions.add(httpSessionId);
			}
		}
		isNew = false;
	}

	/**
	 * Increment our version and place ourself in the cache.
	 */
	public synchronized void processSessionRepl() {
		// Replicate the session.
		if (logger.isDebugEnabled()) {
			logger.debug("processSessionRepl(): session is dirty. Will increment "
					+ "version from: " + getVersion() + " and replicate.");
		}
		final String sipAppSessionKey = getHaId();
		if (logger.isDebugEnabled()) {
			logger.debug("processSessionRepl(): replicating sip app session " + sipAppSessionKey);
		}
		if(isNew) {
			proxy_.putSipApplicationSessionMetaData(sipAppSessionKey, CREATION_TIME, creationTime);
			proxy_.putSipApplicationSessionMetaData(sipAppSessionKey, INVALIDATION_POLICY, invalidationPolicy);
			isNew = false;
		}
		if(sessionMetadataDirty) {			
			for (Entry<String, Object> entry : metaDataModifiedMap.entrySet()) {
				proxy_.putSipApplicationSessionMetaData(sipAppSessionKey, entry.getKey(), entry.getValue());
			}			
			metaDataModifiedMap.clear();			
		}		
		if(sipSessionsMapModified) {					
			proxy_.putSipApplicationSessionMetaData(sipAppSessionKey, SIP_SESSIONS, sipSessions.toArray(new SipSessionKey[sipSessions.size()]));
			sipSessionsMapModified = false;
		}		
		if(httpSessionsMapModified) {			
			proxy_.putSipApplicationSessionMetaData(sipAppSessionKey, HTTP_SESSIONS, httpSessions.toArray(new String[httpSessions.size()]));
			httpSessionsMapModified = false;
		}
		this.incrementVersion();
		proxy_.putSipApplicationSession(sipAppSessionKey, this);

		sessionMetadataDirty = false;
		sessionAttributesDirty = false;		
		sessionLastAccessTimeDirty = false;

		updateLastReplicated();
	}

	/**
	 * Overrides the superclass impl by doing nothing if <code>localCall</code>
	 * is <code>false</code>. The JBossCacheManager will already be aware of a
	 * remote invalidation and will handle removal itself.
	 */
	protected void removeFromManager(boolean localCall, boolean localOnly) {
		if (localCall) {
			super.removeFromManager(localCall, localOnly);
		}
	}

	protected Object removeAttributeInternal(String name, boolean localCall,
			boolean localOnly) {
		return removeJBossInternalAttribute(name, localCall, localOnly);
	}

	protected Object removeJBossInternalAttribute(String name) {
		throw new UnsupportedOperationException(
				"removeJBossInternalAttribute(String) "
						+ "is not supported by JBossCacheClusteredSession; use "
						+ "removeJBossInternalAttribute(String, boolean, boolean");
	}

	protected abstract Object removeJBossInternalAttribute(String name,
			boolean localCall, boolean localOnly);

}
