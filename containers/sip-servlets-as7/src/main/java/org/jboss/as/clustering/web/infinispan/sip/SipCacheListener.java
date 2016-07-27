/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.clustering.web.infinispan.sip;

import static org.jboss.as.clustering.web.infinispan.sip.InfinispanSipLogger.ROOT_LOGGER;

import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryActivated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryActivatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.jboss.as.clustering.web.sip.LocalDistributableConvergedSessionManager;
import org.jboss.logging.Logger;

/**
 * Listens for distributed caches events, notifying the JBossCacheManager
 * of events of interest. 
 * 
 * @author Brian Stansberry
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * @author posfai.gergely@ext.alerant.hu
 */
@Listener
public class SipCacheListener //extends CacheListenerBase
{
	
	private static final Logger logger = Logger.getLogger(SipCacheListener.class);
	
	/*protected static Logger logger = Logger.getLogger(SipCacheListener.class);
   // Element within an FQN that is SIPSESSION
	protected static final int SIPSESSION_FQN_INDEX = 0;
   // Element within an FQN that is the hostname
	protected static final int HOSTNAME_FQN_INDEX = 1;
   // ELEMENT within an FQN this is the sipappname
	protected static final int SIPAPPNAME_FQN_INDEX = 2;
   // Element within an FQN that is the sip app session id
	protected static final int SIPAPPSESSION_ID_FQN_INDEX = 3;
   // Element within an FQN that is the sip session id   
	protected static final int SIPSESSION_ID_FQN_INDEX = 4;
   // Size of an Fqn that points to the root of a session
	protected static final int SIPAPPSESSION_FQN_SIZE = SIPAPPSESSION_ID_FQN_INDEX + 1;
   // Element within an FQN that is the root of a Pojo attribute map
	protected static final int SIPAPPSESSION_POJO_ATTRIBUTE_FQN_INDEX = SIPAPPSESSION_ID_FQN_INDEX + 1;
   // Size of an Fqn that points to the root of a session
	protected static final int SIPSESSION_FQN_SIZE = SIPSESSION_ID_FQN_INDEX + 1;
   // Element within an FQN that is the root of a Pojo attribute map
	protected static final int SIPSESSION_POJO_ATTRIBUTE_FQN_INDEX = SIPSESSION_ID_FQN_INDEX + 1;
   // Element within an FQN that is the root of an individual Pojo attribute
	protected static final int SIPAPPSESSION_POJO_KEY_FQN_INDEX = SIPAPPSESSION_POJO_ATTRIBUTE_FQN_INDEX + 1;
   // Size of an Fqn that points to the root of a session
	protected static final int SIPAPPSESSION_POJO_KEY_FQN_SIZE = SIPAPPSESSION_POJO_KEY_FQN_INDEX + 1;
   // Element within an FQN that is the root of an individual Pojo attribute
	protected static final int SIPSESSION_POJO_KEY_FQN_INDEX = SIPSESSION_POJO_ATTRIBUTE_FQN_INDEX + 1;
   // Size of an Fqn that points to the root of a session
	protected static final int SIPSESSION_POJO_KEY_FQN_SIZE = SIPSESSION_POJO_KEY_FQN_INDEX + 1;
   // The index of the root of a buddy backup subtree
	protected static final int BUDDY_BACKUP_ROOT_OWNER_INDEX = BuddyManager.BUDDY_BACKUP_SUBTREE_FQN.size();
   // The size of the root of a buddy backup subtree (including owner)
	protected static final int BUDDY_BACKUP_ROOT_OWNER_SIZE = BUDDY_BACKUP_ROOT_OWNER_INDEX + 1;
      
   // Element within an FQN that is the root of a session's internal pojo storage area
	protected static final int SIPSESSION_POJO_INTERNAL_FQN_INDEX = SIPSESSION_ID_FQN_INDEX + 1;
   // Minimum size of an FQN that is below the root of a session's internal pojo storage area
	protected static final int SIPSESSION_POJO_INTERNAL_FQN_SIZE = SIPSESSION_POJO_INTERNAL_FQN_INDEX + 1;
   
// Element within an FQN that is the root of a session's internal pojo storage area
   protected static final int SIPAPPSESSION_POJO_INTERNAL_FQN_INDEX = SIPAPPSESSION_ID_FQN_INDEX + 1;
   // Minimum size of an FQN that is below the root of a session's internal pojo storage area
   protected static final int SIPAPPSESSION_POJO_INTERNAL_FQN_SIZE = SIPAPPSESSION_POJO_INTERNAL_FQN_INDEX + 1;
   
//   private static final String TREE_CACHE_CLASS = "org.jboss.cache.TreeCache";
//   private static final String DATA_GRAVITATION_CLEANUP = "_dataGravitationCleanup";
      */
   private String sipApplicationNameHashed;
   private String sipApplicationName;
   //private boolean fieldBased_;
   //private boolean attributeBased_;
   private Cache cache;
   
   private LocalDistributableConvergedSessionManager manager;
   private DistributedCacheManager cacheManager;
   
   SipCacheListener(Cache cache,
		   LocalDistributableConvergedSessionManager manager, //String contextHostPath,
			//ReplicationGranularity granularity, 
			String sipApplicationName, String sipApplicationNameHashed,
			DistributedCacheManager cacheManager) {
		this.manager = manager;
	   //super(manager, contextHostPath);
		//if (granularity == ReplicationGranularity.FIELD)
		//	fieldBased_ = true;
		//else if (granularity == ReplicationGranularity.ATTRIBUTE)
		//	attributeBased_ = true;
		this.sipApplicationName = sipApplicationName;
		this.sipApplicationNameHashed = sipApplicationNameHashed;
		this.cache = cache;
		this.cacheManager = cacheManager;
		if (logger.isDebugEnabled()){
			logger.debug("SipCacheListener constructor - sipApplicationName=" + sipApplicationName + ", sipApplicationNameHashed=" + sipApplicationNameHashed);
		}
	}

	@CacheEntryRemoved
    public void removed(CacheEntryRemovedEvent<String, Map<Object, Object>> event) {
		if(logger.isDebugEnabled()) {
			logger.debug("removed - event.getKey()=" + event.getKey());
		}
		
        if (event.isPre() || event.isOriginLocal()) return;

        String cacheKey = event.getKey();
        
        try {
        	if (DistributedCacheManager.isKeySipAppSessionId(cacheKey)){
        		
        		String sipAppSessionId = DistributedCacheManager.getSipAppSessionIdFromCacheKey(cacheKey);
        		this.manager.notifyRemoteSipApplicationSessionInvalidation(sipAppSessionId);
        		
        	} else if (DistributedCacheManager.isKeySipSessionId(cacheKey)){
        		
        		String sipSessionId = DistributedCacheManager.getSipSessionIdFromCacheKey(cacheKey);
            	String sipAppSessionId = DistributedCacheManager.getSipAppSessionIdFromCacheKey(cacheKey);
            	
        		this.manager.notifyRemoteSipSessionInvalidation(sipAppSessionId, sipSessionId);
        	}
        } catch (Throwable e) {
            ROOT_LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @CacheEntryModified
    public void modified(CacheEntryModifiedEvent<String, Map<Object, Object>> event) {
    	if(logger.isDebugEnabled()) {
			logger.debug("modified - event.getKey()=" + event.getKey());
		}
        if (event.isPre() || event.isOriginLocal()) return;

        String cacheKey = event.getKey();
        
        try {
            Map<Object, Object> map = event.getValue();
            if (!map.isEmpty()) {
            	if(logger.isDebugEnabled()) {
        			logger.debug("modified - event map not empty - event.getKey()=" + event.getKey());
        		}
                Integer version = SipSessionMapEntry.VERSION.get(map);
                Long timestamp = SipSessionMapEntry.TIMESTAMP.get(map);
                
                
                if (DistributedCacheManager.isKeySipAppSessionId(cacheKey)){
                	if(logger.isDebugEnabled()) {
            			logger.debug("modified - cacheKey is sip app session id");
            		}
                	
                	String sipAppSessionId = DistributedCacheManager.getSipAppSessionIdFromCacheKey(cacheKey);
                	
                	if(logger.isDebugEnabled()) {
            			logger.debug("modified - sipAppSessionId=" + sipAppSessionId);
            		}
                	
                    if ((version != null) && (timestamp != null) /*&& (metadata != null)*/) {
                    	if(logger.isDebugEnabled()) {
                			logger.debug("modified - call sipApplicationSessionChangedInDistributedCache");
                		}
                    	
                        boolean updated = this.manager.sipApplicationSessionChangedInDistributedCache(
                        		sipAppSessionId,
                        		null, 
                                version.intValue(), 
                                timestamp.longValue(),
                                null);
                        
                        if(logger.isDebugEnabled()) {
                			logger.debug("modified - call updated=" + updated);
                		}
                        
                        if (!updated) {
                            ROOT_LOGGER.versionIdMismatch(version, cacheKey);
                        }
                    }
                	
                	
                } else if (DistributedCacheManager.isKeySipSessionId(cacheKey)){
                	if(logger.isDebugEnabled()) {
            			logger.debug("modified - cacheKey is sip session id");
            		}
                	
                	String sipSessionId = DistributedCacheManager.getSipSessionIdFromCacheKey(cacheKey);
                	String sipAppSessionId = DistributedCacheManager.getSipAppSessionIdFromCacheKey(cacheKey);
                	
                	if(logger.isDebugEnabled()) {
            			logger.debug("modified - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
            		}
                	
                	//
                	// If loading the sip-related sip session metadata would become necessary once, then it should be done like this:
                	//
                	//org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata sipMetaData = SipSessionMapEntry.METADATA.get(map);
	                //if (logger.isDebugEnabled()){
	        		//	logger.debug("modified - getting metadata map");
	        		//}
	                //sipMetaData.setMetaData(this.cacheManager.loadSipMetaDataMap(map));
                	
	                
                    if ((version != null) && (timestamp != null) /*&& (metadata != null)*/) {
                    	
                    	if(logger.isDebugEnabled()) {
                			logger.debug("modified - call sipSessionChangedInDistributedCache");
                		}
                    	
                        boolean updated = this.manager.sipSessionChangedInDistributedCache(
                        		sipAppSessionId, 
                        		sipSessionId, 
                        		null, 
                                version.intValue(), 
                                timestamp,
                                null);
                        
                        if(logger.isDebugEnabled()) {
                			logger.debug("modified - call updated=" + updated);
                		}
                        
                        if (!updated) {
                            ROOT_LOGGER.versionIdMismatch(version, cacheKey);
                        }
                    }
                	
                	
                }
               
            } else {
            	if(logger.isDebugEnabled()) {
        			logger.debug("modified - event map is empty");
        		}
            }
        } catch (Throwable e) {
            ROOT_LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    @CacheEntryActivated
    public void activated(CacheEntryActivatedEvent<String, Map<Object, Object>> event) {
    	if(logger.isDebugEnabled()) {
			logger.debug("activated");
		}
    	
        // Do nothing, as passivation is not yet supported
    	
    	/*if (event.isPre()) return;

        try {
        	
            this.manager.sipApplicationSessionActivated();
        } catch (Throwable e) {
            ROOT_LOGGER.warn(e.getLocalizedMessage(), e);
        }*/
    }

	
}
