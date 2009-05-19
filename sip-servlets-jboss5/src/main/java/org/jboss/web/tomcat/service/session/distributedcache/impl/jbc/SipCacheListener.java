/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.util.Map;

import org.jboss.cache.Fqn;
import org.jboss.cache.buddyreplication.BuddyManager;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodeRemovedEvent;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.web.tomcat.service.session.ClusteredSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;

/**
 * Listens for distributed caches events, notifying the JBossCacheManager
 * of events of interest. 
 * 
 * @author Brian Stansberry
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
@org.jboss.cache.notifications.annotation.CacheListener
public class SipCacheListener extends CacheListenerBase
{
	protected static Logger logger = Logger.getLogger(SipCacheListener.class);
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
   
   private static Logger log_ = Logger.getLogger(SipCacheListener.class);
   private String sipApplicationName;
   private boolean fieldBased_;
   private boolean attributeBased_;
   
   SipCacheListener(JBossCacheWrapper wrapper,
			LocalDistributableSessionManager manager, String contextHostPath,
			ReplicationGranularity granularity, String sipApplicationName) {
		super(manager, contextHostPath);
		if (granularity == ReplicationGranularity.FIELD)
			fieldBased_ = true;
		else if (granularity == ReplicationGranularity.ATTRIBUTE)
			attributeBased_ = true;
		this.sipApplicationName = sipApplicationName;
	}

   protected boolean isFqnForOurSipapp(Fqn<String> fqn, boolean isBuddy)
   {   
      try
      {
         if (sipApplicationName.equals(fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + SIPAPPNAME_FQN_INDEX : SIPAPPNAME_FQN_INDEX))
               && AbstractJBossCacheService.SESSION.equals(fqn.get(isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + SIPSESSION_FQN_INDEX : SIPSESSION_FQN_INDEX)))
            return true;
      }
      catch (IndexOutOfBoundsException e)
      {
         // can't be ours; too small; just fall through
      }
   
      return false;
   }
   
// --------------- CacheListener methods ------------------------------------

   @NodeRemoved
   public void nodeRemoved(NodeRemovedEvent event)
   {      
      if (event.isPre())
         return;
      
      boolean local = event.isOriginLocal();
      if (!fieldBased_ && local)
         return;
      
      @SuppressWarnings("unchecked")
      Fqn<String> fqn = event.getFqn();
      boolean isBuddy = isBuddyFqn(fqn);
      
      if (!local 
            && isFqnSessionRootSized(fqn.size(), isBuddy) 
            && isFqnForOurSipapp(fqn, isBuddy))
      {
    	  	// A session has been invalidated from another node;
    	  	// need to inform manager
    	  	String sessId = null;
	      	if(isFqnSipApplicationSessionRootSized(fqn.size(), isBuddy)) {
	      		sessId = getSipApplicationSessionIdFromFqn(fqn, isBuddy);
	      		((ClusteredSipManager)manager_).notifyRemoteSipApplicationSessionInvalidation(sessId);
	      	} else {
	      		sessId = getSipSessionIdFromFqn(fqn, isBuddy);
	      		((ClusteredSipManager)manager_).notifyRemoteSipSessionInvalidation(sessId);
	      	}          
      }
      else if (local && !isBuddy
                  && (isPossibleSipApplicationSessionInternalPojoFqn(fqn) || isPossibleSipSessionInternalPojoFqn(fqn)) 
                  && isFqnForOurWebapp(fqn, isBuddy))
      {
    	  	String sessId = null;
			if (isFqnSipApplicationSessionRootSized(fqn.size(), isBuddy)) {
				sessId = getSipApplicationSessionIdFromFqn(fqn, isBuddy);
				((ClusteredSipManager) manager_)
						.notifySipApplicationSessionLocalAttributeModification(sessId);
			} else {
				sessId = getSipSessionIdFromFqn(fqn, isBuddy);
				((ClusteredSipManager) manager_)
						.notifySipSessionLocalAttributeModification(sessId);
			}                   
      }
   }
   
   @NodeModified
   public void nodeModified(NodeModifiedEvent event)
   {      
      if (event.isPre())
         return;
      
      boolean local = event.isOriginLocal();
      if (!fieldBased_ && local)
         return;
      
      @SuppressWarnings("unchecked")
      Fqn<String> fqn = event.getFqn();
      boolean isBuddy = isBuddyFqn(fqn);      
      
      if (!local 
             &&isFqnSessionRootSized(fqn.size(), isBuddy)
             &&isFqnForOurSipapp(fqn, isBuddy))
      {
         // Query if we have version value in the distributed cache. 
         // If we have a version value, compare the version and invalidate if necessary.
         @SuppressWarnings("unchecked")
         Map<Object, Object> data = event.getData();
         Integer version = (Integer) data.get(AbstractJBossCacheService.VERSION_KEY);
         if(version != null)
         {
        	 String sipAppSessionId = getSipApplicationSessionIdFromFqn(fqn, isBuddy);
        	 String sipSessionId = null;
        	 boolean isSipApplicationSession = true;
 	      	if(!isFqnSipApplicationSessionRootSized(fqn.size(), isBuddy)) {
 	      		sipSessionId = getSipSessionIdFromFqn(fqn, isBuddy);
 	      		isSipApplicationSession = false;
 	      	}              
            String owner = isBuddy ? getBuddyOwner(fqn) : null;
            Long timestamp = (Long) data.get(AbstractJBossCacheService.TIMESTAMP_KEY);
            if (timestamp == null)
            {
               log_.warn("No timestamp attribute found in " + fqn);
            }
            else
            {
            	boolean updated = false;
            	if(isSipApplicationSession) {
            		// Notify the manager that a session has been updated
            		updated = ((ClusteredSipManager) manager_).sipApplicationSessionChangedInDistributedCache(sipAppSessionId, owner, 
                                                  version.intValue(), 
                                                  timestamp.longValue(), 
                                                  (DistributableSessionMetadata) data.get(AbstractJBossCacheService.METADATA_KEY));
            	} else {
            		// Notify the manager that a session has been updated
            		updated = ((ClusteredSipManager) manager_).sipSessionChangedInDistributedCache(sipAppSessionId, sipSessionId, owner, 
                                                  version.intValue(), 
                                                  timestamp.longValue(), 
                                                  (DistributableSessionMetadata) data.get(AbstractJBossCacheService.METADATA_KEY));
            	}
               if (!updated && !isBuddy)
               {
                  log_.warn("Possible concurrency problem: Replicated version id " + 
                            version + " is less than or equal to in-memory version for session app id " + sipAppSessionId + " and session id " + sipSessionId); 
               }
               /*else 
               {
                  We have a local session but got a modification for the buddy tree.
                  This means another node is in the process of taking over the session;
                  we don't worry about it
               }
                */
            }
         }
         else if (!attributeBased_) // other granularities can modify attributes only
         {
            log_.warn("No version attribute found in " + fqn);
         }
      }
      else if (local && !isBuddy
            && (isPossibleSipApplicationSessionInternalPojoFqn(fqn) || isPossibleSipSessionInternalPojoFqn(fqn)) 
            && isFqnForOurSipapp(fqn, isBuddy))
      {
         // One of our sessions' pojos is modified; need to inform
         // the manager so it can mark the session dirty
			String sessId = null;
			if (isFqnSipApplicationSessionRootSized(fqn.size(), isBuddy)) {
				sessId = getSipApplicationSessionIdFromFqn(fqn, isBuddy);
				((ClusteredSipManager) manager_)
						.notifySipApplicationSessionLocalAttributeModification(sessId);
			} else {
				sessId = getSipSessionIdFromFqn(fqn, isBuddy);
				((ClusteredSipManager) manager_)
						.notifySipSessionLocalAttributeModification(sessId);
			}  
      }
   }       
   
   protected static boolean isFqnSessionRootSized(int size, boolean isBuddy)
   {
      return size == (isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + SIPAPPSESSION_FQN_SIZE : SIPAPPSESSION_FQN_SIZE) || 
      	size == (isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE + SIPSESSION_FQN_SIZE : SIPSESSION_FQN_SIZE);
   }
   
   protected static boolean isFqnSipApplicationSessionRootSized(int size,
			boolean isBuddy) {
		return size == (isBuddy ? BUDDY_BACKUP_ROOT_OWNER_SIZE
				+ SIPAPPSESSION_FQN_SIZE : SIPAPPSESSION_FQN_SIZE);
	}

   protected static String getSipApplicationSessionIdFromFqn(Fqn fqn,
			boolean isBuddy) {
		if (isBuddy) {
			return (String) fqn.get(BUDDY_BACKUP_ROOT_OWNER_SIZE
					+ SIPAPPSESSION_ID_FQN_INDEX);
		} else {
			return (String) fqn.get(SIPAPPSESSION_ID_FQN_INDEX);
		}
	}

   protected static String getSipSessionIdFromFqn(Fqn fqn, boolean isBuddy) {
		if (isBuddy) {
			return (String) fqn.get(BUDDY_BACKUP_ROOT_OWNER_SIZE
					+ SIPSESSION_ID_FQN_INDEX);
		} else {
			return (String) fqn.get(SIPSESSION_ID_FQN_INDEX);
		}
	}

	/**
	 * Check if the fqn is big enough to be in the internal pojo area but isn't
	 * in the regular attribute area.
	 * 
	 * Structure in the cache is:
	 * 
	 * /SIPSESSION 
	 * ++ /contextPath_hostname
	 * ++++ /sipApplicationName
	 * ++++++ /id 
	 * ++++++++ /sipsessionid 
	 * ++++++++++ /ATTRIBUTE
	 * ++++++++++ /_JBossInternal_ 
	 * ++++++++++++ etc etc
	 * 
	 * If the Fqn size is big enough to be "etc etc" or lower, but the 4th level
	 * is not "ATTRIBUTE", it must be under _JBossInternal_. We discriminate
	 * based on != ATTRIBUTE to avoid having to code to the internal PojoCache
	 * _JBossInternal_ name.
	 * 
	 * @param fqn
	 * @return
	 */
	public static boolean isPossibleSipSessionInternalPojoFqn(Fqn<String> fqn) {
		return (fqn.size() > SIPSESSION_POJO_INTERNAL_FQN_SIZE && FieldBasedJBossCacheService.ATTRIBUTE
				.equals(fqn.get(SIPSESSION_POJO_INTERNAL_FQN_INDEX)) == false);
	}
	
	/**
	 * Check if the fqn is big enough to be in the internal pojo area but isn't
	 * in the regular attribute area.
	 * 
	 * Structure in the cache is:
	 * 
	 * /SIPSESSION 
	 * ++ /contextPath_hostname
	 * ++++ /sipApplicationName
	 * ++++++ /id 
	 * ++++++++ /ATTRIBUTE
	 * ++++++++ /_JBossInternal_ 
	 * ++++++++++ etc etc
	 * 
	 * If the Fqn size is big enough to be "etc etc" or lower, but the 4th level
	 * is not "ATTRIBUTE", it must be under _JBossInternal_. We discriminate
	 * based on != ATTRIBUTE to avoid having to code to the internal PojoCache
	 * _JBossInternal_ name.
	 * 
	 * @param fqn
	 * @return
	 */
	public static boolean isPossibleSipApplicationSessionInternalPojoFqn(Fqn<String> fqn) {
		return (fqn.size() > SIPAPPSESSION_POJO_INTERNAL_FQN_SIZE && FieldBasedJBossCacheService.ATTRIBUTE
				.equals(fqn.get(SIPAPPSESSION_POJO_INTERNAL_FQN_INDEX)) == false);
	}
}
