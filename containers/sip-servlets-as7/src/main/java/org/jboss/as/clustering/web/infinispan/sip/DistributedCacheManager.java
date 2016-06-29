/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUV ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.clustering.web.infinispan.sip;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.infinispan.Cache;
import org.infinispan.affinity.KeyAffinityService;
import org.infinispan.affinity.KeyGenerator;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.context.Flag;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryActivatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.remoting.transport.Address;
import org.jboss.as.clustering.infinispan.affinity.KeyAffinityServiceFactory;
import org.jboss.as.clustering.infinispan.invoker.CacheInvoker;
import org.jboss.as.clustering.lock.SharedLocalYieldingClusterLockManager;
import org.jboss.as.clustering.registry.Registry;
import org.jboss.as.clustering.web.BatchingManager;
import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.as.clustering.web.IncomingDistributableSessionData;
import org.jboss.as.clustering.web.LocalDistributableSessionManager;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.jboss.as.clustering.web.OutgoingSessionGranularitySessionData;
import org.jboss.as.clustering.web.SessionAttributeMarshaller;
import org.jboss.as.clustering.web.SessionOwnershipSupport;
import org.jboss.as.clustering.web.impl.IncomingDistributableSessionDataImpl;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import static org.jboss.as.clustering.web.infinispan.sip.InfinispanSipLogger.ROOT_LOGGER;
import static org.jboss.as.clustering.web.infinispan.sip.InfinispanSipMessages.MESSAGES;
import org.jboss.as.clustering.infinispan.invoker.CacheInvoker;
import org.jboss.as.clustering.web.infinispan.SessionAttributeStorage;
import org.jboss.as.clustering.web.sip.DistributableSipApplicationSessionMetadata;
import org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata;
import org.jboss.as.clustering.web.sip.DistributedCacheConvergedSipManager;
import org.jboss.as.clustering.web.sip.LocalDistributableConvergedSessionManager;
import org.jboss.as.clustering.web.sip.OutgoingDistributableSipApplicationSessionData;
import org.jboss.as.clustering.web.sip.OutgoingDistributableSipSessionData;
import org.jboss.as.clustering.web.SessionAttributeMarshaller;

/**
 * Distributed cache manager implementation using Infinispan.
 *
 * @author Paul Ferraro
 */
@Listener
public class DistributedCacheManager<V extends OutgoingDistributableSessionData>
	extends org.jboss.as.clustering.web.infinispan.DistributedCacheManager<V> 
	implements DistributedCacheConvergedSipManager<V> {
	
	private static Logger logger = Logger.getLogger(DistributedCacheManager.class);

	private static UnsupportedOperationException UNSUPPORTED = 
	      new UnsupportedOperationException("Attribute operations not supported with SESSION replication granularity.");
	
    //DistributedCacheConvergedSipManagerDelegate<OutgoingSessionGranularitySessionData> delegate;
	final SessionAttributeStorage<V> attributeStorage;
    private final Cache<String, Map<Object, Object>> cache;
    private final ForceSynchronousCacheInvoker invoker;
    private final CacheInvoker txInvoker;
    private final SharedLocalYieldingClusterLockManager lockManager;
    
    
    // delegate stuff
    public static final String SIP_SESSION_KEY_PREFIX = "SIP_SESSION/";
	public static final String SIP_APP_SESSION_KEY_PREFIX = "SIP_APP_SESSION/";
	
	
	protected String sipApplicationNameHashed; 
	protected String sipApplicationName;
	private SipCacheListener sipCacheListener_;
	private final SessionAttributeMarshaller marshaller;
	
	LocalDistributableSessionManager manager;
	
	static String getSipSessionCacheKey(String sipAppSessionKey, String sipSessionId){
		return SIP_SESSION_KEY_PREFIX + sipAppSessionKey + "/" + sipSessionId;
	}
	static String getSipAppSessionCacheKey(String sipAppSessionId){
		return SIP_APP_SESSION_KEY_PREFIX + sipAppSessionId;
	}
	static boolean isKeySipSessionId(String cacheKey){
		return cacheKey.startsWith(SIP_SESSION_KEY_PREFIX);
	}
	static boolean isKeySipAppSessionId(String cacheKey){
		return cacheKey.startsWith(SIP_APP_SESSION_KEY_PREFIX);
	}
	static String getSipSessionIdFromCacheKey(String cacheKey){
		if (isKeySipSessionId(cacheKey)){
			return cacheKey.substring(SIP_SESSION_KEY_PREFIX.length());
		} else if (isKeySipAppSessionId(cacheKey)){
			String tmp = cacheKey.substring(SIP_SESSION_KEY_PREFIX.length());
			return tmp.substring(tmp.indexOf('/'));
		} else {
			return null;
		}
	}
	static String getSipAppSessionIdFromCacheKey(String cacheKey){
		if (isKeySipAppSessionId(cacheKey)){
			return cacheKey.substring(SIP_APP_SESSION_KEY_PREFIX.length());
		} else if (isKeySipSessionId(cacheKey)){
			String tmp = cacheKey.substring(SIP_SESSION_KEY_PREFIX.length());
			return tmp.substring(0, tmp.indexOf('/'));
		}else {
			return null;
		}
	}
	
	
	
    
    public DistributedCacheManager(LocalDistributableSessionManager manager,
            Cache<String, Map<Object, Object>> cache, 
            Registry<String, Void> registry,
            SharedLocalYieldingClusterLockManager lockManager, 
            SessionAttributeStorage<V> attributeStorage,
            BatchingManager batchingManager, 
            CacheInvoker invoker, 
            CacheInvoker txInvoker, 
            KeyAffinityServiceFactory affinityFactory,
            SessionAttributeMarshaller marshaller) {
    	super(manager, 
    			cache, 
    			registry, 
    			lockManager, 
    			attributeStorage, 
    			batchingManager, 
    			invoker,
    			txInvoker, 
    			affinityFactory);
    	this.lockManager = lockManager;
    	this.cache = cache;
    	this.attributeStorage = attributeStorage;
    	this.invoker = new ForceSynchronousCacheInvoker(invoker);
    	this.txInvoker = txInvoker;
    	//delegate = new DistributedCacheConvergedSipManagerDelegate(
    	//		this,
    	//		manager,
    	//		marshaller);
    	this.manager = manager;
		this.marshaller = marshaller;
    }
    
    public Cache<String, Map<Object, Object>> getCache() {
		return cache;
	}
    
	private ForceSynchronousCacheInvoker getInvoker() {
		return invoker;
	}

	public CacheInvoker getTxInvoker() {
		return txInvoker;
	}

	@Override
	public void start() {
		if (logger.isDebugEnabled()){
			logger.debug("start");
		}
		super.start();
		//this.trace("startingDelegate(%s)", delegate);
		//delegate.start();
		trace("DistributedCacheManager.start() : sipApplicationName %s sipApplicationNameHashed %s", sipApplicationName, sipApplicationNameHashed);
		if(sipApplicationName != null) {
			createCacheListeners();
		}
	}
	
	private void createCacheListeners() {
		if (logger.isDebugEnabled()){
			logger.debug("createCacheListeners");
		}
		
		// do here the creation of the root node for holding sip session data, cause it may fail if concurrently created on demand (storing sessions) 
		//infinispanCacheService.getCache().getRoot().addChild(getSipApplicationSessionParentFqn(infinispanCacheService.combinedPath_));
		
		sipCacheListener_ = new SipCacheListener(
				getCache(), (LocalDistributableConvergedSessionManager)manager, 
				//infinispanCacheService.combinedPath_,
				//Util.getReplicationGranularity(manager), 
				sipApplicationName, sipApplicationNameHashed);
		
		//if (log_.isDebugEnabled()) {
		//	log_.debug("DistributedCacheConvergedSipManagerDelegate.start() : sipCacheListener " + sipCacheListener_);
		//}
		trace("DistributedCacheManager.createCacheListeners()");
		
		getCache().addListener(sipCacheListener_);

		/*if (manager.isPassivationEnabled()) {
			log_.debug("Passivation is enabled");
			sipPassivationListener_ = new SipPassivationListener(
					manager,
					infinispanCacheService.combinedPath_, sipApplicationNameHashed);
			infinispanCacheService.getCache().addCacheListener(
					sipPassivationListener_);
		}*/
	}

	@Override
	public void stop() {
		if (logger.isDebugEnabled()){
			logger.debug("stop");
		}
		//delegate.stop();
		if (sipCacheListener_ != null) {
			getCache().removeListener(sipCacheListener_);
			/*if (sipPassivationListener_ != null) {
				infinispanCacheService.getCache().removeCacheListener(
						sipPassivationListener_);
			}*/
		}
		super.stop();
	}

    void trace(String message, Object... args) {
        ROOT_LOGGER.tracef(message, args);
    }

	@Override
	public void removeSipApplicationSession(String sipAppSessionId) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSession - sipAppSessionId=" + sipAppSessionId);
		}
		
		//delegate.removeSipApplicationSession(sipApplicationSessionKey);
		
		trace("removeSipApplicationSession(%s)", sipAppSessionId);
		
		this.removeSipApplicationSession(sipAppSessionId, false);
	}
	
	private void removeSipApplicationSession(final String sipAppSessionId, final boolean local) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSession - sipAppSessionId=" + sipAppSessionId + ", local=" + local);
		}
		trace("removeSipApplicationSession(%s, %s)", sipAppSessionId, local);
		
		final String sipAppSessionKey = getSipAppSessionCacheKey(sipAppSessionId);
		
		Operation<Void> operation = new Operation<Void>() {
            @Override
            public Void invoke(Cache<String, Map<Object, Object>> cache) {
                cache.remove(sipAppSessionKey);
                // todo: also remove sip sessions of app session?
                return null;
            }
        };
        getInvoker().invoke(getCache(), operation, Flag.SKIP_CACHE_LOAD, local ? Flag.CACHE_MODE_LOCAL : Flag.SKIP_REMOTE_LOOKUP);
    }

	@Override
	public void removeSipSession(String sipAppSessionId, String sipSessionId) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSession - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
		}
		//delegate.removeSipSession(sipApplicationSessionKey, sipSessionKey);
		
		trace("removeSipSession(%s, %s)", sipAppSessionId, sipSessionId);
		
		this.removeSipSession(sipAppSessionId, sipSessionId, false);
	}

	private void removeSipSession(final String sipAppSessionId, final String sipSessionId, final boolean local) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSession - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", local=" + local);
		}
		trace("removeSipSession(%s, %s, %s)", sipAppSessionId, sipSessionId, local);
		
		final String sipSessionKey = getSipSessionCacheKey(sipAppSessionId, sipSessionId);
		
		Operation<Void> operation = new Operation<Void>() {
            @Override
            public Void invoke(Cache<String, Map<Object, Object>> cache) {
            	cache.remove(sipSessionKey);
                
                return null;
            }
        };
        getInvoker().invoke(getCache(), operation, Flag.SKIP_CACHE_LOAD, local ? Flag.CACHE_MODE_LOCAL : Flag.SKIP_REMOTE_LOOKUP);
    }
	
	@Override
	public void removeSipApplicationSessionLocal(String sipAppSessionId) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSessionLocal - sipAppSessionId=" + sipAppSessionId);
		}
		// delegate.removeSipApplicationSessionLocal(key);
		trace("removeSipApplicationSessionLocal(%s)", sipAppSessionId);
		
        this.removeSipApplicationSession(sipAppSessionId, true);
	}
	
	@Override
	public void removeSipApplicationSessionLocal(String sipAppSessionId, String dataOwner) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSessionLocal - sipAppSessionId=" + sipAppSessionId + ", dataOwner=" + dataOwner);
		}
		
		// delegate.removeSipApplicationSessionLocal(key, dataOwner);
		trace("removeSipApplicationSessionLocal(%s, %s)", sipAppSessionId, dataOwner);
		
		if (dataOwner == null) {
            this.removeSipApplicationSession(sipAppSessionId, true);
        }
	}

	@Override
	public void removeSipSessionLocal(String sipAppSessionId, String sipSessionId) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSessionLocal - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
		}
		
		// delegate.removeSipSessionLocal(sipAppSessionKey, key);
		
		trace("removeSipSessionLocal(%s, %s)", sipAppSessionId, sipSessionId);
		
		this.removeSipSession(sipAppSessionId, sipSessionId, true);
	}
	
	@Override
	public void removeSipSessionLocal(String sipAppSessionId, String sipSessionId, String dataOwner) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSessionLocal - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", dataOwner=" + dataOwner);
		}
		// delegate.removeSipSessionLocal(sipAppSessionKey, key, dataOwner);
		
		trace("removeSipSessionLocal(%s, %s, %s)", sipAppSessionId, sipSessionId, dataOwner);
		
		if (dataOwner == null) {
            this.removeSipSession(sipAppSessionId, sipSessionId, true);
        }
		
	}

	@Override
	public void storeSipSessionData(final V sipSessionData) {
		if (logger.isDebugEnabled()){
			logger.debug("storeSipSessionData - sipSessionData.getRealId()=" + sipSessionData.getRealId());
		}
		// delegate.storeSipSessionData((OutgoingDistributableSipSessionData) sipSessionData);
		
		final OutgoingDistributableSipSessionData sessionData = (OutgoingDistributableSipSessionData) sipSessionData;
		
		
		/*String sipApplicationSessionKey = sipSessionData.getSipApplicationSessionKey();
		String sessionKey = sipSessionData.getSipSessionKey();

		if (log_.isDebugEnabled()) {
			log_.debug("storeSipSessionData(): putting sip session " + sessionKey.toString());
		}

		Fqn<String> fqn = getSipSessionFqn(infinispanCacheService.combinedPath_, sipApplicationSessionKey, sessionKey);

		// Swap in/out the webapp classloader so we can deserialize
        // attributes whose classes are only available to the webapp
		ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(manager.getApplicationClassLoader());
		try {
			storeSipSessionMetaData(fqn, sipSessionData);
			((DistributedCacheConvergedSipManager)infinispanCacheService).storeSipSessionAttributes(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), sipSessionData);
		} finally {
			Thread.currentThread().setContextClassLoader(prevTCL);
		}*/
		
		// todo: swap?
		final String sipApplicationSessionKey = sessionData.getSipApplicationSessionKey();
		final String sipSessionId = sessionData.getSipSessionKey();
		final String sipSessionKey = getSipSessionCacheKey(sipApplicationSessionKey, sipSessionId);
		if (logger.isDebugEnabled()){
			logger.debug("storeSipSessionData - sipSessionKey=" + sipSessionKey + "--> cache key");
			logger.debug("storeSipSessionData - sipSessionId=" + sipSessionId);
			logger.debug("storeSipSessionData - sipApplicationSessionKey=" + sipApplicationSessionKey);
		}
		
		Operation<Void> operation = new Operation<Void>() {
            @Override
            public Void invoke(Cache<String, Map<Object, Object>> cache) {
            	Map<Object, Object> map = cache.putIfAbsent(sipSessionKey, null);
            	if (logger.isDebugEnabled()){
        			logger.debug("storeSipSessionData after putIfAbsent - cache.getAdvancedCache().getStatus()=" + cache.getAdvancedCache().getStatus());
        			logger.debug("storeSipSessionData after putIfAbsent - cache.getAdvancedCache().getStats()=" + cache.getAdvancedCache().getStats());
        		}
            	try {
            		
	                SipSessionMapEntry.VERSION.put(map, Integer.valueOf(sessionData.getVersion()));
	                DistributableSipSessionMetadata dsm = (DistributableSipSessionMetadata)(sessionData.getMetadata());
	                if (dsm != null && dsm.isNew() && sessionData.isSessionMetaDataDirty()){
		                if (dsm.isNew()){
		                	SipSessionMapEntry.METADATA.put(map, sessionData.getMetadata());
		                }
		                
		                if (dsm.getMetaData() != null){
		                	SipSessionMapEntry.SIP_SERVLETS_METADATA.put(map, DistributedCacheManager.this.marshaller.marshal(dsm.getMetaData()));
		                	/*try {
		                        DistributedCacheManager.this.infinispanCacheService.sipServletsMetaDataStorage.store(map, sipApplicationSessionData);
		                    } catch (IOException e) {
		                        throw MESSAGES.failedToStoreSessionAttributes(e, sessionId);
		                    }*/
		                }
		                
	                }   
	                if (sessionData.getTimestamp() != null){
	                	SipSessionMapEntry.TIMESTAMP.put(map, sessionData.getTimestamp());
	                }
                
                    DistributedCacheManager.this.attributeStorage.store(map, sipSessionData);
                } catch (IOException e) {
                    throw MESSAGES.failedToStoreSessionAttributes(e, sipSessionId);
                }
                return null;
            }
        };

        getInvoker().invoke(getCache(), operation);

	}

	@Override
	public void storeSipApplicationSessionData(final V sipApplicationSessionData) {
		if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSessionData - sipApplicationSessionData.getRealId()=" + sipApplicationSessionData.getRealId());
		}
		// delegate.storeSipApplicationSessionData((OutgoingDistributableSipApplicationSessionData) sipApplicationSessionData);
		
		final OutgoingDistributableSipApplicationSessionData sipAppSessionData = (OutgoingDistributableSipApplicationSessionData) sipApplicationSessionData;
		
		/*String fqnId = sipApplicationSessionData.getSipApplicationSessionKey();

		if (log_.isTraceEnabled()) {
			log_.trace("putSipSession(): putting sip session " + fqnId);
		}

		Fqn<String> fqn = getSipApplicationSessionFqn(infinispanCacheService.combinedPath_, fqnId);

		Map<Object, Object> map = new HashMap<Object, Object>();
		
		// Swap in/out the webapp classloader so we can deserialize
        // attributes whose classes are only available to the webapp
		ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(manager.getApplicationClassLoader());
		try {
			storeSipApplicationSessionMetaData(fqn, sipApplicationSessionData);
			((DistributedCacheConvergedSipManager)infinispanCacheService).storeSipApplicationSessionAttributes(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), sipApplicationSessionData);
		} finally {
			Thread.currentThread().setContextClassLoader(prevTCL);
		}*/
		
		// todo: swap?
		
		final String sipAppSessionId = sipAppSessionData.getSipApplicationSessionKey();
		final String sipAppSessionKey = getSipAppSessionCacheKey(sipAppSessionId);
		if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSessionData - sipAppSessionId=" + sipAppSessionId);
			logger.debug("storeSipApplicationSessionData - sipAppSessionKey=" + sipAppSessionKey + "--> cache key");
			logger.debug("storeSipApplicationSessionData - sipAppSessionData.getRealId()=" + sipAppSessionData.getRealId());
		}
		
		//this.trace("storeSipApplicationSessionData(%s)", sipAppSessionId);

        Operation<Void> operation = new Operation<Void>() {
            @Override
            public Void invoke(Cache<String, Map<Object, Object>> cache) {
            	if (logger.isDebugEnabled()){
        			logger.debug("storeSipApplicationSessionData - cache.getName()=" + cache.getName());
        		}
            	
            	Map<Object, Object> map = cache.putIfAbsent(sipAppSessionKey, null);
            	if (logger.isDebugEnabled()){
        			logger.debug("storeSipApplicationSessionData after putIfAbsent - cache.getAdvancedCache().getStatus()=" + cache.getAdvancedCache().getStatus());
        			logger.debug("storeSipApplicationSessionData after putIfAbsent - cache.getAdvancedCache().getStats().getCurrentNumberOfEntries()=" + cache.getAdvancedCache().getStats().getCurrentNumberOfEntries());
        		}
            	try {
            		if (logger.isDebugEnabled()){
            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.VERSION.put");
            		}
	            	SipSessionMapEntry.VERSION.put(map, Integer.valueOf(sipAppSessionData.getVersion()));
	                
	                DistributableSipApplicationSessionMetadata dsm = (DistributableSipApplicationSessionMetadata)sipAppSessionData.getMetadata();
	                if (dsm != null && dsm.isNew() && sipAppSessionData.isSessionMetaDataDirty()){
		                if (dsm.isNew()){
		                	
		                	// TODO: ezt az id-beallitast csak beletettem kezzel h ne legyen null, mert akkor elszallt, nem kene mar "jonnie" az id-nak vhonnan?
		                	sipAppSessionData.getMetadata().setId(sipAppSessionId);
		                	if (logger.isDebugEnabled()){
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.METADATA.put");
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.METADATA.put: id=" + sipAppSessionData.getMetadata().getId());
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.METADATA.put: creationTime=" + sipAppSessionData.getMetadata().getCreationTime());
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.METADATA.put: maxInactiveInterval=" + sipAppSessionData.getMetadata().getMaxInactiveInterval());
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.METADATA.put: isNew=" + sipAppSessionData.getMetadata().isNew());
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.METADATA.put: isValid=" + sipAppSessionData.getMetadata().isValid());
		            		}
		                	SipSessionMapEntry.METADATA.put(map, sipAppSessionData.getMetadata());
		                }
		                
		                if(dsm.getMetaData() != null) {
		                	if (logger.isDebugEnabled()){
		            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.SIP_SERVLETS_METADATA.put");
		            			if (dsm.getMetaData() != null){
		            				logger.debug("storeSipApplicationSessionData - sip servlets metadata keys:");
		            				for (String metadataKey: dsm.getMetaData().keySet()){
		            					logger.debug("storeSipApplicationSessionData - metadata key: " + metadataKey);	
		            				}
		            			} else {
		            				logger.debug("getOutgoingSipApplicationSessionData - metadata.getMetaData() is null");
		            			}
		            		}
		                	SipSessionMapEntry.SIP_SERVLETS_METADATA.put(map, DistributedCacheManager.this.marshaller.marshal(dsm.getMetaData()));
		                	
		                	
		                	// TODO ? store sip session data too?
		                	
		                }
	                }
	                
	                if (sipAppSessionData.getTimestamp() != null){
	                	if (logger.isDebugEnabled()){
	            			logger.debug("storeSipApplicationSessionData - SipSessionMapEntry.TIMESTAMP.put");
	            		}
	                	SipSessionMapEntry.TIMESTAMP.put(map, sipAppSessionData.getTimestamp());
	                }
                
	                if (logger.isDebugEnabled()){
            			logger.debug("storeSipApplicationSessionData - DistributedCacheManager.this.attributeStorage.store");
            		}
                    DistributedCacheManager.this.attributeStorage.store(map, sipApplicationSessionData);
                } catch (IOException e) {
                    throw MESSAGES.failedToStoreSessionAttributes(e, sipAppSessionId);
                }
                if (logger.isDebugEnabled()){
        			logger.debug("storeSipApplicationSessionData - return from operation");
        		}
                return null;
            }
        };

        if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSessionData - invoke operation");
		}
        getInvoker().invoke(getCache(), operation);
        if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSessionData - operation invoked, continue");
		}
	}

	@Override
	public void storeSipSessionAttributes(String sipAppSessionId, String sipSessionId, V sipSessionData) {
		if (logger.isDebugEnabled()){
			logger.debug("storeSipSessionAttributes - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
		}
		
		// TODO ? jobss5-ben volt ilyen, de ugy tunik, jboss7-ben mar nincs
		// (szoval valszeg inkabb torolni kene innen es az if-bol is)
		
		//if(sipSessionData.getSessionAttributes() != null) {
		//	cacheWrapper_.put(fqn, ATTRIBUTE_KEY.toString(), getMarshalledValue(sessionData.getSessionAttributes()));
		//}
		
	}

	@Override
	public void storeSipApplicationSessionAttributes(String sipAppSessionId, V sipApplicationSessionData) {
		if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSessionAttributes - sipAppSessionId=" + sipAppSessionId);
		}
		// todo? jobss5-ben volt ilyen, de ugy tunik, jboss7-ben mar nincs
		// (szoval valszeg inkabb torolni kene innen es az if-bol is)
		
		//if(sessionData.getSessionAttributes() != null) {
		//	cacheWrapper_.put(fqn, ATTRIBUTE_KEY.toString(), getMarshalledValue(sessionData.getSessionAttributes()));
		//}
		
	}

	@Override
	public IncomingDistributableSessionData getSipSessionData(String sipAppSessionId, String sipSessionId, boolean initialLoad) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionData - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", initialLoad=" + initialLoad);
		}
		
		//return delegate.getSipSessionData(sipAppSessionKey, key, initialLoad);
		
		trace("getSipSessionData(%s, %s, %s)", sipAppSessionId, sipSessionId, initialLoad);
		
		if (sipAppSessionId == null || sipSessionId == null) {
			throw new IllegalArgumentException("Null key");
		}
		
		return this.getSipSessionDataImpl(sipAppSessionId, sipSessionId, true);
	}

	@Override
	public IncomingDistributableSessionData getSipSessionData(String sipAppSessionId, String sipSessionId, String dataOwner, boolean includeAttributes) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionData - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", dataOwner=" + dataOwner + ", includeAttributes=" + includeAttributes);
		}
		//return delegate.getSipSessionData(sipAppSessionKey, key, dataOwner, includeAttributes);
		trace("getSipSessionData(%s, %s, %s, %s)", sipAppSessionId, sipSessionId, dataOwner, includeAttributes);

        return (dataOwner == null) ? this.getSipSessionDataImpl(sipAppSessionId, sipSessionId, includeAttributes) : null;
	}

	@Override
	public IncomingDistributableSessionData getSipApplicationSessionData(String sipAppSessionId, boolean initialLoad) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionData - sipAppSessionId=" + sipAppSessionId + ", initialLoad=" + initialLoad);
		}
		// return delegate.getSipApplicationSessionData(key, initialLoad);
		trace("getSipApplicationSessionData(%s, %s)", sipAppSessionId, initialLoad);
		
		if (sipAppSessionId == null) {
			throw new IllegalArgumentException("Null key");
		}

		return this.getSipApplicationSessionDataImpl(sipAppSessionId, true);
	}

	@Override
	public IncomingDistributableSessionData getSipApplicationSessionData(String sipAppSessionId, String dataOwner, boolean includeAttributes) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionData - sipAppSessionId=" + sipAppSessionId + ", dataOwner=" + dataOwner + ", includeAttributes=" + includeAttributes);
		}
		// return delegate.getSipApplicationSessionData(key, dataOwner, includeAttributes);
		
		trace("getSipApplicationSessionData(%s, %s, %s)", sipAppSessionId, dataOwner, includeAttributes);
		
		return (dataOwner == null) ? this.getSipApplicationSessionDataImpl(sipAppSessionId, includeAttributes) : null;
	}

	@Override
	public void evictSipSession(String sipAppSessionId, String sipSessionId) {
		if (logger.isDebugEnabled()){
			logger.debug("evictSipSession - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
		}
		
		//delegate.evictSipSession(sipAppSessionKey, key);
		trace("evictSipSession(%s, %s)", sipAppSessionId, sipSessionId);
		final String sipSessionKey = getSipSessionCacheKey(sipAppSessionId, sipSessionId);
		
		Operation<Void> operation = new Operation<Void>() {
            @Override
            public Void invoke(Cache<String, Map<Object, Object>> cache) {
                if (cache.getAdvancedCache().lock(sipSessionKey)) {
                    cache.evict(sipSessionKey);
                }
                return null;
            }
        };
        try {
        	getTxInvoker().invoke(getCache(), operation, Flag.FAIL_SILENTLY);
        } catch (Throwable e) {
            ROOT_LOGGER.debugf(e, "Failed to evict sip session %s/%s", sipAppSessionId, sipSessionId);
        }
	}

	@Override
	public void evictSipApplicationSession(String sipAppSessionId) {
		if (logger.isDebugEnabled()){
			logger.debug("evictSipApplicationSession - sipAppSessionId=" + sipAppSessionId);
		}
		
		//delegate.evictSipApplicationSession(key);
		trace("evictSipApplicationSession(%s)", sipAppSessionId);
		final String sipAppSessionKey = getSipAppSessionCacheKey(sipAppSessionId);
		
        Operation<Void> operation = new Operation<Void>() {
            @Override
            public Void invoke(Cache<String, Map<Object, Object>> cache) {
                if (cache.getAdvancedCache().lock(sipAppSessionKey)) {
                    cache.evict(sipAppSessionKey);
                }
                return null;
            }
        };
        try {
        	getTxInvoker().invoke(getCache(), operation, Flag.FAIL_SILENTLY);
        } catch (Throwable e) {
            ROOT_LOGGER.debugf(e, "Failed to evict sip app session %s", sipAppSessionId);
        }
        
        // todo: also evict sip sessions of app session??
	}

	@Override
	public void evictSipSession(String sipAppSessionId, String sipSessionId, String dataOwner) {
		if (logger.isDebugEnabled()){
			logger.debug("evictSipSession - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", dataOwner=" + dataOwner);
		}
		
		//delegate.evictSipSession(sipAppSessionKey, key, dataOwner);
		
		//if (log_.isDebugEnabled()) {
		//	log_
		//			.debug("evictSession(): evicting sip session from my distributed store. sipAppSessionKey/sipSessionKey: "
		//					+ sipAppSessionKey + "/" + key);
		//}
		
		trace("evictSipSession(%s, %s, %s)", sipAppSessionId, sipSessionId, dataOwner);

        if (dataOwner == null) {
            this.evictSession(sipAppSessionId, sipSessionId);
        }
	}

	@Override
	public void evictSipApplicationSession(String sipAppSessionId, String dataOwner) {
		if (logger.isDebugEnabled()){
			logger.debug("evictSipApplicationSession - sipAppSessionId=" + sipAppSessionId + ", dataOwner=" + dataOwner);
		}
		//delegate.evictSipApplicationSession(key, dataOwner);
		
		//if (log_.isDebugEnabled()) {
		//	log_
		//			.debug("evictSession(): evicting sip application session from my distributed store. Fqn: "
		//					+ fqn);
		//}
		trace("evictSipApplicationSession(%s, %s)", sipAppSessionId, dataOwner);

		if (dataOwner == null){
			this.evictSipApplicationSession(sipAppSessionId);
		}
	}

	@Override
	public Map<String, String> getSipSessionKeys() {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionKeys");
		}
		//return delegate.getSipSessionKeys();
		
		Map<String, String> result = new HashMap<String, String>();
        Operation<Set<String>> operation = new Operation<Set<String>>() {
            @Override
            public Set<String> invoke(Cache<String, Map<Object, Object>> cache) {
                //return cache.keySet();
            	Set<String> sessionIds = new HashSet<String>();
            	for (String key: cache.keySet()){
            		if (key.startsWith(SIP_SESSION_KEY_PREFIX)){
            			sessionIds.add(key);
            		}
            	}
            	return sessionIds;
            }
        };
        for (String sessionId: getInvoker().invoke(this.cache, operation, Flag.SKIP_LOCKING, Flag.SKIP_REMOTE_LOOKUP, Flag.SKIP_CACHE_LOAD)) {
            result.put(sessionId, null);
        }
        return result;
	}

	@Override
	public Map<String, String> getSipApplicationSessionKeys() {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionKeys");
		}
		
		// return delegate.getSipApplicationSessionKeys();
		
		Map<String, String> result = new HashMap<String, String>();
        Operation<Set<String>> operation = new Operation<Set<String>>() {
            @Override
            public Set<String> invoke(Cache<String, Map<Object, Object>> cache) {
                //return cache.keySet();
            	Set<String> sessionIds = new HashSet<String>();
            	for (String key: cache.keySet()){
            		if (key.startsWith(SIP_APP_SESSION_KEY_PREFIX)){
            			sessionIds.add(key);
            		}
            	}
            	return sessionIds;
            }
        };
        for (String sessionId: getInvoker().invoke(this.cache, operation, Flag.SKIP_LOCKING, Flag.SKIP_REMOTE_LOOKUP, Flag.SKIP_CACHE_LOAD)) {
            result.put(sessionId, null);
        }
        return result;
	}

	@Override
	public void sipSessionCreated(String sipApplicationSessionKey,
			String sipSessionKey) {
		if (logger.isDebugEnabled()){
			logger.debug("sipSessionCreated - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey);
		}
		// no-op by default
	}

	@Override
	public void sipApplicationSessionCreated(String key) {
		if (logger.isDebugEnabled()){
			logger.debug("sipSessionCreated - key=" + key);
		}
		// no-op by default
	}

	@Override
	public Object getSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionAttribute - sipApplicationSessionKey=" + sipApplicationSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
	}

	@Override
	public void putSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key, Object value) {
		if (logger.isDebugEnabled()){
			logger.debug("putSipApplicationSessionAttribute with Object - sipApplicationSessionKey=" + sipApplicationSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
		
	}

	@Override
	public void putSipApplicationSessionAttribute(
			String sipApplicationSessionKey, Map<String, Object> map) {
		if (logger.isDebugEnabled()){
			logger.debug("putSipApplicationSessionAttribute with Map - sipApplicationSessionKey=" + sipApplicationSessionKey);
		}
		throw UNSUPPORTED;
		
	}

	@Override
	public Object removeSipApplicationSessionAttribute(
			String sipApplicationSessionKey, String key) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSessionAttribute - sipApplicationSessionKey=" + sipApplicationSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
	}

	@Override
	public void removeSipApplicationSessionAttributeLocal(
			String sipApplicationSessionKey, String key) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSessionAttributeLocal - sipApplicationSessionKey=" + sipApplicationSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
	}

	@Override
	public Set<String> getSipApplicationSessionAttributeKeys(
			String sipApplicationSessionKey) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionAttributeKeys - sipApplicationSessionKey=" + sipApplicationSessionKey);
		}
		throw UNSUPPORTED;
	}

	@Override
	public Map<String, Object> getSipApplicationSessionAttributes(
			String sipApplicationSessionKey) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionAttributes - sipApplicationSessionKey=" + sipApplicationSessionKey);
		}
		throw UNSUPPORTED;
	}

	@Override
	public Map<String, Object> getConvergedSessionAttributes(String realId,
			Map<Object, Object> distributedCacheData) {
		if (logger.isDebugEnabled()){
			logger.debug("getConvergedSessionAttributes - realId=" + realId);
		}
		
		/*Map<String, Object> result = null;
		if(distributedCacheData != null) {
			result = (Map<String, Object>) getUnMarshalledValue(distributedCacheData.get(ATTRIBUTE_KEY.toString()));
		}
		
	    return result == null ? Collections.EMPTY_MAP : result;
	    */
		
		// todo? is this method even necessary?
		return null;
	}

	@Override
	public Object getSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionAttribute - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
	}

	@Override
	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key, Object value) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionAttribute with Object - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
		
	}

	@Override
	public void putSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, Map<String, Object> map) {
		if (logger.isDebugEnabled()){
			logger.debug("putSipSessionAttribute with Map - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey);
		}
		throw UNSUPPORTED;
		
	}

	@Override
	public Object removeSipSessionAttribute(String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSessionAttribute - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
	}

	@Override
	public void removeSipSessionAttributeLocal(String sipApplicationSessionKey,
			String sipSessionKey, String key) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSessionAttributeLocal - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey + ", key=" + key);
		}
		throw UNSUPPORTED;
		
	}

	@Override
	public Set<String> getSipSessionAttributeKeys(
			String sipApplicationSessionKey, String sipSessionKey) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionAttributeKeys - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey);
		}
		throw UNSUPPORTED;
	}

	@Override
	public Map<String, Object> getSipSessionAttributes(
			String sipApplicationSessionKey, String sipSessionKey) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionAttributes - sipApplicationSessionKey=" + sipApplicationSessionKey + ", sipSessionKey=" + sipSessionKey);
		}
		throw UNSUPPORTED;
	}

	@Override
	public Cache getInfinispanCache() {
		return this.cache;
	}

	@Override
	public void setApplicationName(String applicationName) {
		if (logger.isDebugEnabled()){
			logger.debug("setApplicationName - applicationName=" + applicationName);
		}
		//delegate.setApplicationName(applicationName);
		sipApplicationName = applicationName;
	}

	@Override
	public void setApplicationNameHashed(String applicationNameHashed) {
		if (logger.isDebugEnabled()){
			logger.debug("setApplicationNameHashed - applicationNameHashed=" + applicationNameHashed);
		}
		
		//delegate.setApplicationNameHashed(applicationNameHashed);
		sipApplicationNameHashed = applicationNameHashed;
		trace("DistributedCacheManager.setApplicationNameHashed() : sipApplicationName " + sipApplicationName + " sipApplicationNameHashed " + sipApplicationNameHashed);
		//if (log_.isDebugEnabled()) {
		//	log_.debug("DistributedCacheConvergedSipManagerDelegate.setApplicationNameHashed() : sipApplicationName " + sipApplicationName + " sipApplicationNameHashed " + sipApplicationNameHashed);
		//}
		if(sipCacheListener_ == null && sipApplicationNameHashed != null) {
			createCacheListeners();
		}
	}	

	@Override
    public Map<String, String> getSessionIds() {
		if (logger.isDebugEnabled()){
			logger.debug("getSessionIds");
		}
		Map<String, String> result = new HashMap<String, String>();
        Operation<Set<String>> operation = new Operation<Set<String>>() {
            @Override
            public Set<String> invoke(Cache<String, Map<Object, Object>> cache) {
                //return cache.keySet();
            	// todo? also return sip session and sip app session ids??
            	Set<String> sessionIds = new HashSet<String>();
            	for (String key: cache.keySet()){
            		if (!key.startsWith(SIP_SESSION_KEY_PREFIX) && !key.startsWith(SIP_APP_SESSION_KEY_PREFIX)){
            			sessionIds.add(key);
            		}
            	}
            	return sessionIds;
            }
        };
        for (String sessionId: getInvoker().invoke(this.cache, operation, Flag.SKIP_LOCKING, Flag.SKIP_REMOTE_LOOKUP, Flag.SKIP_CACHE_LOAD)) {
            result.put(sessionId, null);
        }
        return result;
    }

	/*
	@Override
    public boolean isPassivationEnabled() {
        // todo?
    }

    
    @Override
    public boolean isPersistenceEnabled() {
        // todo?
    }

	@Override
    public void setForceSynchronous(boolean forceSynchronous) {
        // todo?
    }
    
    @Override
    public SessionOwnershipSupport getSessionOwnershipSupport() {
        // todo?
    }
    
	@Override
    public boolean isLocal(String sessionId) {
        // todo?
    }

    @Override
    public String locate(String sessionId) {
        // todo?
    }

    @Override
    public String createSessionId() {
        // todo?
    }

    @Override
    public String getKey() {
        // todo?
    }
	 */
	
	// Simplified CacheInvoker.Operation using assigned key/value types
    abstract class Operation<R> implements CacheInvoker.Operation<String, Map<Object, Object>, R> {
    }
    
    private IncomingDistributableSessionData getSipApplicationSessionDataImpl(final String sipAppSessionId, final boolean includeAttributes) {
    	if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSessionDataImpl - sipAppSessionId=" + sipAppSessionId + ", includeAttributes=" + includeAttributes);
		}
    	
		final String sipAppSessionKey = getSipAppSessionCacheKey(sipAppSessionId);
		
		Operation<IncomingDistributableSessionData> operation = new Operation<IncomingDistributableSessionData>() {
            @Override
            public IncomingDistributableSessionData invoke(Cache<String, Map<Object, Object>> cache) {
            	if (sipAppSessionId == null){
            		return null;
            	}
                
                Map<Object, Object> map = cache.get(getSipAppSessionCacheKey(sipAppSessionId));

                // If requested sip app session or sip session is no longer in the cache; return null
            	if (map == null) return null;
            	
                /*Map<Object, Object> sessionMetaData = (Map<Object, Object>) SipSessionMapEntry.SIP_SERVLETS_METADATA.get(map);
                //Map<Object, Object> sessionMetaData = (Map<Object, Object>) sessionData.get(SIP_SERVLETS_METADATA_KEY);
        		Map<String, Object> sipSessionMetaData = new java.util.concurrent.ConcurrentHashMap<String, Object>();
        		if(sessionMetaData != null) {
        			for(Entry<Object, Object> entry : sessionMetaData.entrySet()) {
        				sipSessionMetaData.put((String)entry.getKey(), entry.getValue());
        			}
        		}*/
        		
        		// --
        		/*if (includeAttributes) {
                    try {
                        result.setSessionAttributes(infinispanCacheService.attributeStorage.load(map));
                    } catch (Exception e) {
                        throw MESSAGES.failedToLoadSessionAttributes(e, sessionId);
                    }
                }*/
        		//Map<Object, Object> sessionAttributesData = infinispanCacheService.cacheWrapper_.getData(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), true);
        		
        		
            	try {
	        		// --	
	        		Integer version = SipSessionMapEntry.VERSION.get(map);
	        		// Map<Object, Object> versionData = jBossCacheService.cacheWrapper_.getData(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.VERSION_KEY), true);
	        		
	        		Long timestamp = SipSessionMapEntry.TIMESTAMP.get(map);
	                org.jboss.as.clustering.web.DistributableSessionMetadata metadata = SipSessionMapEntry.METADATA.get(map);
	                
	                DistributableSipApplicationSessionMetadata sipMetaData = new DistributableSipApplicationSessionMetadata();
	                sipMetaData.setId(metadata.getId());
	                sipMetaData.setCreationTime(metadata.getCreationTime());
	                sipMetaData.setMaxInactiveInterval(metadata.getMaxInactiveInterval());
	                sipMetaData.setNew(metadata.isNew());
	                sipMetaData.setValid(metadata.isValid());
	                sipMetaData.setMetaData(loadSipServletsMetaData(map));
	                
	                IncomingDistributableSessionDataImpl result = null;
                
                	result = new IncomingDistributableSessionDataImpl(version, timestamp, sipMetaData);
                	if (includeAttributes) {
                        result.setSessionAttributes(attributeStorage.load(map));
                    }
                	
                	// todo?
                	// get sip sessions of sip app session
                	//for (String key: sipMetaData.getMetaData().keySet()){
                	//	if (isKeySipSessionId(key)){
                	//		String sipSessionId = getSipSessionIdFromKey(key);
                	//		getSipSessionData(sipAppSessionId, sipSessionId, includeAttributes);
                	//	}
                	//}
                	return result;
                } catch (Exception e) {
        			ROOT_LOGGER.debugf("Problem accessing sip app session " + sipAppSessionId + " data : " + e.getClass() + " "
        					+ e, e);
        			// Clean up
        			// removeSipApplicationSessionLocal(sipAppSessionKey);
        			throw MESSAGES.failedToLoadSessionAttributes(e, sipAppSessionId);
        		}
            }

        };

        try {
            return getInvoker().invoke(this.cache, operation);
        } catch (Exception e) {
            ROOT_LOGGER.sessionLoadFailed(e, sipAppSessionId);

            // Clean up
            this.removeSipApplicationSessionLocal(sipAppSessionId);
            
            return null;
        }
    }
		
	public Map<String, Object> loadSipServletsMetaData(Map<Object, Object> map) throws IOException, ClassNotFoundException {
		if (logger.isDebugEnabled()){
			logger.debug("loadSipServletsMetaData");
		}
        return (Map<String, Object>) this.marshaller.unmarshal(SipSessionMapEntry.SIP_SERVLETS_METADATA.get(map));
    }
	
	private IncomingDistributableSessionData getSipSessionDataImpl(final String sipAppSessionId, final String sipSessionId, final boolean includeAttributes) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionDataImpl - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", includeAttributes=" + includeAttributes);
		}
		
        Operation<IncomingDistributableSessionData> operation = new Operation<IncomingDistributableSessionData>() {
            @Override
            public IncomingDistributableSessionData invoke(Cache<String, Map<Object, Object>> cache) {
            	if (sipAppSessionId == null || sipSessionId == null){
            		return null;
            	}

            	Map<Object, Object> map = cache.get(getSipSessionCacheKey(sipAppSessionId, sipSessionId));

                // If requested sip app session or sip session is no longer in the cache; return null
            	if (map == null) return null;
                
                /*Map<Object, Object> sessionMetaData = (Map<Object, Object>) SipSessionMapEntry.SIP_SERVLETS_METADATA.get(map);
                //Map<Object, Object> sessionMetaData = (Map<Object, Object>) sessionData.get(SIP_SERVLETS_METADATA_KEY);
        		Map<String, Object> sipSessionMetaData = new java.util.concurrent.ConcurrentHashMap<String, Object>();
        		if(sessionMetaData != null) {
        			for(Entry<Object, Object> entry : sessionMetaData.entrySet()) {
        				sipSessionMetaData.put((String)entry.getKey(), entry.getValue());
        			}
        		}*/
        		
        		// --
        		/*if (includeAttributes) {
                    try {
                        result.setSessionAttributes(infinispanCacheService.attributeStorage.load(map));
                    } catch (Exception e) {
                        throw MESSAGES.failedToLoadSessionAttributes(e, sessionId);
                    }
                }*/
        		//Map<Object, Object> sessionAttributesData = infinispanCacheService.cacheWrapper_.getData(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.ATTRIBUTE_KEY), true);
        		
        		
            	try {
	        		// --	
	        		Integer version = SipSessionMapEntry.VERSION.get(map);
	        		// Map<Object, Object> versionData = jBossCacheService.cacheWrapper_.getData(Fqn.fromString(fqn.toString() + "/" + AbstractJBossCacheService.VERSION_KEY), true);
	        		
	        		Long timestamp = SipSessionMapEntry.TIMESTAMP.get(map);
	                org.jboss.as.clustering.web.DistributableSessionMetadata metadata = SipSessionMapEntry.METADATA.get(map);
	                
	                DistributableSipSessionMetadata sipMetaData = new DistributableSipSessionMetadata();
	                sipMetaData.setId(metadata.getId());
	                sipMetaData.setCreationTime(metadata.getCreationTime());
	                sipMetaData.setMaxInactiveInterval(metadata.getMaxInactiveInterval());
	                sipMetaData.setNew(metadata.isNew());
	                sipMetaData.setValid(metadata.isValid());
	                sipMetaData.setMetaData(loadSipServletsMetaData(map));
	                
	                IncomingDistributableSessionDataImpl result = null;
	                
                	result = new IncomingDistributableSessionDataImpl(version, timestamp, sipMetaData);
                	if (includeAttributes) {
                        result.setSessionAttributes(attributeStorage.load(map));
                    }
                	return result;
                } catch (Exception e) {
        			ROOT_LOGGER.debugf("Problem accessing sip session " + sipAppSessionId + "/" + sipSessionId + " data : " + e.getClass() + " "
        					+ e, e);
        			// Clean up
        			// removeSipSessionLocal(sipAppSessionId, sipSessionId);
        			throw MESSAGES.failedToLoadSessionAttributes(e, sipAppSessionId + "/" + sipSessionId);
        		}
            }
        };

        try {
            return getInvoker().invoke(this.cache, operation);
        } catch (Exception e) {
            ROOT_LOGGER.sessionLoadFailed(e, sipAppSessionId + "/" + sipSessionId);

            // Clean up
            this.removeSipSessionLocal(sipAppSessionId, sipSessionId);
            
            return null;
        }
    }
	
	static class ForceSynchronousCacheInvoker implements CacheInvoker {
        private static final ThreadLocal<Boolean> forceThreadSynchronous = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        private final CacheInvoker invoker;
        private volatile boolean forceSynchronous = false;

        ForceSynchronousCacheInvoker(CacheInvoker invoker) {
            this.invoker = invoker;
        }

        void setForceSynchronous(boolean forceSynchronous) {
            this.forceSynchronous = forceSynchronous;
        }

        void forceThreadSynchronous() {
            forceThreadSynchronous.set(Boolean.TRUE);
        }

        @Override
        public <K, V, R> R invoke(Cache<K, V> cache, Operation<K, V, R> operation, Flag... flags) {
            return this.invoker.invoke(cache, operation, this.forceSynchronous || forceThreadSynchronous.get().booleanValue() ? EnumSet.of(Flag.FORCE_SYNCHRONOUS, flags).toArray(new Flag[0]) : flags);
        }
    }

	/*@Override
	public LockResult acquireSipSessionOwnership(String sipApplicationSessionId, String sipSessionId, boolean newLock) throws TimeoutException, InterruptedException {
		this.trace("acquireSipSessionOwnership(%s, %s, %s)", sipApplicationSessionId, sipSessionId, newLock);

		return acquireSessionOwnership(createSipLockKey(sipApplicationSessionId, sipSessionId), newLock);
	}
	
	@Override
	public void relinquishSipSessionOwnership(String sipApplicationSessionId, String sipSessionId, boolean remove) {
		this.trace("relinquishSipSessionOwnership(%s, %s, %s)", sipApplicationSessionId, sipSessionId, remove);

		relinquishSessionOwnership(createSipLockKey(sipApplicationSessionId, sipSessionId), remove);
	}
	
	@Override
	public LockResult acquireSipApplicationSessionOwnership(String sipApplicationSessionId, boolean newLock) throws TimeoutException, InterruptedException {
		this.trace("acquireSipApplicationSessionOwnership(%s, %s)", sipApplicationSessionId, newLock);
		
		return acquireSessionOwnership(createSipApplicationLockKey(sipApplicationSessionId), newLock);
	}
	
	@Override
	public void relinquishSipApplicationSessionOwnership(String sipApplicationSessionId, boolean remove) {
		this.trace("relinquishSipApplicationSessionOwnership(%s, %s)", sipApplicationSessionId, remove);

		relinquishSessionOwnership(createSipApplicationLockKey(sipApplicationSessionId), remove);
	}
	
	private String createSipLockKey(String sipApplicationSessionId, String sipSessionId) {
        return "SIP/" + sipApplicationSessionId + "/" + sipSessionId;
    }
	
	private String createSipApplicationLockKey(String sipApplicationSessionId) {
        return "SIPAPP/" + sipApplicationSessionId;
    }
	
	@Override
    public SipSessionOwnershipSupport getSipSessionOwnershipSupport() {
        return (this.lockManager != null) ? this : null;
    }*/
	
}
