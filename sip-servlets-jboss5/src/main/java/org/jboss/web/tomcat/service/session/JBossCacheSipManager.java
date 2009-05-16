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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.SnapshotMode;
import org.jboss.web.tomcat.service.session.distributedcache.spi.BatchingManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactory;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactoryFactory;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.notification.ClusteredSessionNotificationCause;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipApplicationSessionNotificationCapability;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipSessionNotificationCapability;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipSessionNotificationPolicy;
import org.jboss.web.tomcat.service.session.notification.IgnoreUndeployLegacyClusteredSessionNotificationPolicy;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Implementation of a converged clustered session manager for
 * catalina using JBossCache replication.
 * 
 * Based on JbossCacheManager JBOSS AS 4.2.2 Tag 
 * I was forced to copy over most of the code since some things that needed to be adapted
 * were private 
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class JBossCacheSipManager<O extends OutgoingDistributableSessionData> extends JBossCacheManager implements
		ClusteredSipManager<O> {

    protected static final String DISTRIBUTED_CACHE_FACTORY_CLASSNAME = "org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManagerFactoryImpl";
    
	protected static Logger logger = Logger.getLogger(JBossCacheSipManager.class);
	
	protected SipManagerDelegate sipManagerDelegate;
	
	protected DistributedCacheManagerFactory distributedCacheManagerFactory;
	/**
	 * Informational name for this Catalina component
	 */
	private static final String info_ = "JBossCacheSipManager/1.0";

	// -- Class attributes ---------------------------------

		
	/**
	 * Id/timestamp of sessions in cache that we haven't loaded
	 */
	protected Map<SipSessionKey, OwnedSessionUpdate> unloadedSipSessions_ = new ConcurrentHashMap<SipSessionKey, OwnedSessionUpdate>();
	
	/**
	 * Id/timestamp of sessions in cache that we haven't loaded
	 */
	protected Map<SipApplicationSessionKey, OwnedSessionUpdate> unloadedSipApplicationSessions_ = new ConcurrentHashMap<SipApplicationSessionKey, OwnedSessionUpdate>();

	/** Number of passivated sip sessions */
	private AtomicInteger sipSessionPassivatedCount_ = new AtomicInteger();

	/** Maximum number of concurrently passivated sip sessions */
	private AtomicInteger sipSessionMaxPassivatedCount_ = new AtomicInteger();
	
	/** Number of passivated sip applicationsessions */
	private AtomicInteger sipApplicationSessionPassivatedCount_ = new AtomicInteger();

	/** Maximum number of concurrently passivated sip application sessions */
	private AtomicInteger sipApplicationSessionMaxPassivatedCount_ = new AtomicInteger();
		
	private String sipSessionNotificationPolicyClass_;
	
	private String sipApplicationSessionNotificationPolicyClass_;
	
	private ClusteredSipApplicationSessionNotificationPolicy sipApplicationSessionNotificationPolicy_;
	private ClusteredSipSessionNotificationPolicy sipSessionNotificationPolicy_;
	
	/** The snapshot manager we are using. */
	private SnapshotSipManager snapshotSipManager_;
	
	// ---------------------------------------------------------- Constructors

	public JBossCacheSipManager() throws ClusteringNotSupportedException {		
		this(getConvergedDistributedCacheManager());	
	}

	protected static DistributedCacheManagerFactory getConvergedDistributedCacheManager() throws ClusteringNotSupportedException {
		DistributedCacheManagerFactoryFactory distributedCacheManagerFactoryFactory = DistributedCacheManagerFactoryFactory.getInstance();
		distributedCacheManagerFactoryFactory.setFactoryClassName(DISTRIBUTED_CACHE_FACTORY_CLASSNAME);
		return distributedCacheManagerFactoryFactory.getDistributedCacheManagerFactory();
	}

	/** 
    * Create a new JBossCacheManager using the given cache. For use in unit testing.
    * 
    * @param pojoCache
    */
   public JBossCacheSipManager(DistributedCacheManagerFactory distributedManagerFactory)
   {
      super(distributedManagerFactory);
   }
	
   @Override
   public void init(String name, JBossWebMetaData webMetaData) throws ClusteringNotSupportedException {
	   super.init(name, webMetaData);
	   sipManagerDelegate = new ClusteredSipManagerDelegate(ReplicationGranularity.valueOf(getReplicationGranularityString()),getUseJK());
	   this.sipApplicationSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
	   this.sipSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
   }
   
   private void initClusteredSipSessionNotificationPolicy()
   {
      if (this.sipSessionNotificationPolicyClass_ == null || this.sipSessionNotificationPolicyClass_.length() == 0)
      {
         this.sipSessionNotificationPolicyClass_ = AccessController.doPrivileged(new PrivilegedAction<String>()
         {
            public String run()
            {
               return System.getProperty("jboss.web.clustered.session.notification.policy",
                     IgnoreUndeployLegacyClusteredSessionNotificationPolicy.class.getName());
            }
         });
      }
      
      try
      {
         this.sipSessionNotificationPolicy_ = (ClusteredSipSessionNotificationPolicy) Thread.currentThread().getContextClassLoader().loadClass(this.sipSessionNotificationPolicyClass_).newInstance();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to instantiate " + 
               ClusteredSipSessionNotificationPolicy.class.getName() + 
               " " + this.sipSessionNotificationPolicyClass_, e);
      }
      
      this.sipSessionNotificationPolicy_.setClusteredSipSessionNotificationCapability(new ClusteredSipSessionNotificationCapability());      
   }
   
   private void initClusteredSipApplicationSessionNotificationPolicy()
   {
      if (this.sipApplicationSessionNotificationPolicyClass_ == null || this.sipApplicationSessionNotificationPolicyClass_.length() == 0)
      {
         this.sipApplicationSessionNotificationPolicyClass_ = AccessController.doPrivileged(new PrivilegedAction<String>()
         {
            public String run()
            {
               return System.getProperty("jboss.web.clustered.session.notification.policy",
                     IgnoreUndeployLegacyClusteredSessionNotificationPolicy.class.getName());
            }
         });
      }
      
      try
      {
         this.sipApplicationSessionNotificationPolicy_ = (ClusteredSipApplicationSessionNotificationPolicy) Thread.currentThread().getContextClassLoader().loadClass(this.sipApplicationSessionNotificationPolicyClass_).newInstance();
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to instantiate " + 
               ClusteredSipApplicationSessionNotificationPolicy.class.getName() + 
               " " + this.sipApplicationSessionNotificationPolicyClass_, e);
      }
      
      this.sipApplicationSessionNotificationPolicy_.setClusteredSipApplicationSessionNotificationCapability(new ClusteredSipApplicationSessionNotificationCapability());      
   }
   
   @SuppressWarnings("unchecked")
   private static ClusteredSipSession<? extends OutgoingDistributableSessionData> uncheckedCastSipSession(SipSession session)
   {
      return (ClusteredSipSession) session;
   }
   
   @SuppressWarnings("unchecked")
   private static ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> uncheckedCastSipApplicationSession(SipApplicationSession session)
   {
      return (ClusteredSipApplicationSession) session;
   }
   
   @SuppressWarnings("unchecked")
   public DistributedCacheConvergedSipManager<? extends OutgoingDistributableSessionData> getDistributedCacheConvergedSipManager()
   {
      return (DistributedCacheConvergedSipManager) getDistributedCacheManager();
   }
   
   /**
    * {@inheritDoc}
    * <p>
    * Removes the session from this Manager's collection of actively managed
    * sessions.  Also removes the session from this server's copy of the
    * distributed cache (but does not remove it from other servers'
    * distributed cache).
    * </p>
    */
   public void removeLocal(SipSession session)
   {
      ClusteredSipSession<? extends OutgoingDistributableSessionData> clusterSess = uncheckedCastSipSession(session);
      synchronized (clusterSess)
      {
         SipSessionKey key = clusterSess.getKey();
         if (key == null) return;

         if (trace_)
         {
            log_.trace("Removing sip session from local store with id: " + key);
         }

         try {
            clusterSess.removeMyselfLocal();
         }
         finally
         {
            // We don't want to replicate this session at the end
            // of the request; the removal process took care of that
            ConvergedSessionReplicationContext.sipSessionExpired(clusterSess, key, getSnapshotSipManager());
            
            // Track this session to prevent reincarnation by this request 
            // from the distributed cache
            ConvergedSessionInvalidationTracker.sipSessionInvalidated(key, this);
            
            sipManagerDelegate.removeSipSession(key);
            stats_.removeStats(key.toString());

            // Compute how long this session has been alive, and update
            // our statistics accordingly
//            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
//            sipSessionExpired(timeAlive);
         }
      }
   }
   
   /**
    * {@inheritDoc}
    * <p>
    * Removes the session from this Manager's collection of actively managed
    * sessions.  Also removes the session from this server's copy of the
    * distributed cache (but does not remove it from other servers'
    * distributed cache).
    * </p>
    */
   public void removeLocal(SipApplicationSession session)
   {
      ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusterSess = uncheckedCastSipApplicationSession(session);
      synchronized (clusterSess)
      {
    	  SipApplicationSessionKey key = clusterSess.getKey();
         if (key == null) return;

         if (trace_)
         {
            log_.trace("Removing sip application session from local store with id: " + key);
         }

         try {
            clusterSess.removeMyselfLocal();
         }
         finally
         {
            // We don't want to replicate this session at the end
            // of the request; the removal process took care of that
            ConvergedSessionReplicationContext.sipApplicationSessionExpired(clusterSess, key, getSnapshotSipManager());
            
            // Track this session to prevent reincarnation by this request 
            // from the distributed cache
            ConvergedSessionInvalidationTracker.sipApplicationSessionInvalidated(key, this);
            
            sipManagerDelegate.removeSipApplicationSession(key);
            stats_.removeStats(key.toString());

            // Compute how long this session has been alive, and update
            // our statistics accordingly
//            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
//            sipApplicationSessionExpired(timeAlive);
         }
      }
   }
   
   
	public boolean storeSipSession(SipSession baseSession) {
		boolean stored = false;
		if (baseSession != null && started_) {
			ClusteredSipSession<? extends OutgoingDistributableSessionData> session = uncheckedCastSipSession(baseSession);

			synchronized (session) {
				if (logger.isDebugEnabled()) {
					logger.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getId());
				}

				if (session.isValid()
						&& (session.isSessionDirty() || session
								.getMustReplicateTimestamp())) {
					if(logger.isInfoEnabled()) {
						logger.info("replicating following sip session " + session.getId());
					}
					String realId = session.getId();

					// Notify all session attributes that they get serialized
					// (SRV 7.7.2)
					long begin = System.currentTimeMillis();
					session.notifyWillPassivate(ClusteredSessionNotificationCause.REPLICATION);
					long elapsed = System.currentTimeMillis() - begin;
					stats_.updatePassivationStats(realId, elapsed);

					// Do the actual replication
					begin = System.currentTimeMillis();
					processSipSessionRepl(session);
					elapsed = System.currentTimeMillis() - begin;
					stored = true;
					stats_.updateReplicationStats(realId, elapsed);
				}
			}
		}

		return stored;
	}
	
	public boolean storeSipApplicationSession(SipApplicationSession baseSession) {
		boolean stored = false;
		if (baseSession != null && started_) {
			ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = uncheckedCastSipApplicationSession(baseSession);			

			synchronized (session) {
				if (logger.isDebugEnabled()) {
					log_.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getId());
				}

				if (session.isValid()
						&& (session.isSessionDirty() || session
								.getMustReplicateTimestamp())) {
					if(logger.isInfoEnabled()) {
						logger.info("replicating following sip application session " + session.getId());
					}
					String realId = session.getId();

					// Notify all session attributes that they get serialized
					// (SRV 7.7.2)
					long begin = System.currentTimeMillis();
					session.notifyWillPassivate(ClusteredSessionNotificationCause.REPLICATION);
					long elapsed = System.currentTimeMillis() - begin;
					stats_.updatePassivationStats(realId, elapsed);

					// Do the actual replication
					begin = System.currentTimeMillis();
					processSipApplicationSessionRepl(session);
					elapsed = System.currentTimeMillis() - begin;
					stored = true;
					stats_.updateReplicationStats(realId, elapsed);
				}
			}
		}

		return stored;
	}
	
	public void add(SipSession session) {
		if (session == null)
			return;

		if (!(session instanceof ClusteredSipSession)) {
			throw new IllegalArgumentException(
					"You can only add instances of "
							+ "type ClusteredSession to this Manager. Session class name: "
							+ session.getClass().getName());
		}

		// add((ClusteredSession) session, true);
		add(uncheckedCastSipSession(session), false);
	}
	
	public void add(SipApplicationSession session) {
		if (session == null)
			return;

		if (!(session instanceof ClusteredSipApplicationSession)) {
			throw new IllegalArgumentException(
					"You can only add instances of "
							+ "type ClusteredSession to this Manager. Session class name: "
							+ session.getClass().getName());
		}

		// add((ClusteredSession) session, true);
		add(uncheckedCastSipApplicationSession(session), false);
	}
	
	/**
    * Adds the given session to the collection of those being managed by this
    * Manager.
    *
    * @param session   the session. Cannot be <code>null</code>.
    * @param replicate whether the session should be replicated
    *
    * @throws NullPointerException if <code>session</code> is <code>null</code>.
    */
   private void add(ClusteredSipSession<? extends OutgoingDistributableSessionData> session, boolean replicate)
   {
      // TODO -- why are we doing this check? The request checks session 
      // validity and will expire the session; this seems redundant
      if (!session.isValid())
      {
         // Not an error; this can happen if a failover request pulls in an
         // outdated session from the distributed cache (see TODO above)
         log_.debug("Cannot add session with id=" + session.getKey() +
                    " because it is invalid");
         return;
      }

      String realId = session.getRealId();
      SipSessionKey key = session.getKey();
	  Object existing = ((ClusteredSipManagerDelegate)sipManagerDelegate).putSipSession(key, session);
      unloadedSipSessions_.remove(key);

      if (!session.equals(existing))
      {
         if (replicate)
         {
            storeSipSession(session);
         }

         // Update counters
//         calcActiveSipSessions();
         
         if (trace_)
         {
            log_.trace("Session with id=" + session.getKey() + " added. " +
                       "Current active sessions " + localActiveCounter_.get());
         }
      }
   }
   
   /**
    * Adds the given session to the collection of those being managed by this
    * Manager.
    *
    * @param session   the session. Cannot be <code>null</code>.
    * @param replicate whether the session should be replicated
    *
    * @throws NullPointerException if <code>session</code> is <code>null</code>.
    */
   private void add(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session, boolean replicate)
   {
      // TODO -- why are we doing this check? The request checks session 
      // validity and will expire the session; this seems redundant
      if (!session.isValid())
      {
         // Not an error; this can happen if a failover request pulls in an
         // outdated session from the distributed cache (see TODO above)
         log_.debug("Cannot add session with id=" + session.getKey() +
                    " because it is invalid");
         return;
      }

      String realId = session.getRealId();
      SipApplicationSessionKey key = session.getKey();
	  Object existing = ((ClusteredSipManagerDelegate)sipManagerDelegate).putSipApplicationSession(key, session);
      unloadedSipSessions_.remove(key);

      if (!session.equals(existing))
      {
         if (replicate)
         {
            storeSipApplicationSession(session);
         }

         // Update counters
//         calcActiveSipApplicationSessions();
         
         if (trace_)
         {
            log_.trace("Session with id=" + session.getKey() + " added. " +
                       "Current active sessions " + localActiveCounter_.get());
         }
      }
   }	
	
   /**
	 * Returns all the sessions that are being actively managed by this manager.
	 * This includes those that were created on this server, those that were
	 * brought into local management by a call to
	 * {@link #findLocalSession(String)} as well as all sessions brought into
	 * local management by a call to {@link #findSessions()}.
	 */
	protected ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>[] findLocalSipApplicationSessions() {
		Iterator<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>> it = (Iterator) sipManagerDelegate.getAllSipApplicationSessions();
		List<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>> coll = new ArrayList();
		while (it.hasNext()) {
			ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusteredSipApplicationSession = it.next();
			coll.add(clusteredSipApplicationSession);			
		}
		@SuppressWarnings("unchecked")
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>[] sess = new ClusteredSipApplicationSession[coll
				.size()];
		return coll.toArray(sess);
	}
	
	/**
	 * Returns all the sessions that are being actively managed by this manager.
	 * This includes those that were created on this server, those that were
	 * brought into local management by a call to
	 * {@link #findLocalSession(String)} as well as all sessions brought into
	 * local management by a call to {@link #findSessions()}.
	 */
	protected ClusteredSipSession<? extends OutgoingDistributableSessionData>[] findLocalSipSessions() {
		Iterator<ClusteredSipSession<? extends OutgoingDistributableSessionData>> it = (Iterator) sipManagerDelegate.getAllSipSessions();
		List<ClusteredSipSession<? extends OutgoingDistributableSessionData>> coll = new ArrayList();
		while (it.hasNext()) {
			ClusteredSipSession<? extends OutgoingDistributableSessionData> clusteredSipSession = it.next();
			coll.add(clusteredSipSession);			
		}
		@SuppressWarnings("unchecked")
		ClusteredSipSession<? extends OutgoingDistributableSessionData>[] sess = new ClusteredSipSession[coll
				.size()];
		return coll.toArray(sess);
	}
   
	
	/**
	 * Returns the given sip session if it is being actively managed by this
	 * manager. An actively managed sip session is on that was either created on
	 * this server, brought into local management by a call to
	 * {@link #findLocalSipSession(String)} or brought into local management by a
	 * call to {@link #findSipSessions()}.
	 * 
	 * @param key
	 *            the session key, with any trailing jvmRoute removed.
	 */
	public ClusteredSipSession findLocalSipSession(final SipSessionKey key, final boolean create, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		return (ClusteredSipSession) sipManagerDelegate.getSipSession(key, create, sipManagerDelegate.getSipFactoryImpl(), sipApplicationSessionImpl);
	}

	
	/**
	 * Returns the given sip application session if it is being actively managed by this
	 * manager. An actively managed sip application session is on that was either created on
	 * this server, brought into local management by a call to
	 * {@link #findLocalSipApplicationSession(String)} or brought into local management by a
	 * call to {@link #findSipApplicationSessions()}.
	 * 
	 * @param key 
	 *            the session key, with any trailing jvmRoute removed.
	 */
	public ClusteredSipApplicationSession findLocalSipApplicationSession(final SipApplicationSessionKey key, final boolean create) {
		return (ClusteredSipApplicationSession) sipManagerDelegate.getSipApplicationSession(key, create);
	}				
	
	/**
	    * {@inheritDoc}
	    */
	   protected void processExpirationSipSessionPassivation()
	   {      
	      boolean expire = maxInactiveInterval_ >= 0;
	      boolean passivate = isPassivationEnabled();
	      
	      long passivationMax = passivationMaxIdleTime_ * 1000L;
	      long passivationMin = passivationMinIdleTime_ * 1000L;

	      if (trace_)
	      { 
//	         log_.trace("processExpirationPassivation(): Looking for sessions that have expired ...");
//	         log_.trace("processExpirationPassivation(): active sessions = " + calcActiveSipSessions());
//	         log_.trace("processExpirationPassivation(): expired sessions = " + expiredCounter_);
//	         if (passivate)
//	         {
//	            log_.trace("processExpirationPassivation(): passivated count = " + getPassivatedSipSessionCount());
//	         }
	      }
	      
	      // Holder for sessions or OwnedSessionUpdates that survive expiration,
	      // sorted by last acccessed time
	      Set<SipSessionPassivationCheck> passivationChecks = new TreeSet<SipSessionPassivationCheck>();
	      
	      try
	      {
	         // Don't track sessions invalidated via this method as if they
	         // were going to be re-requested by the thread
	         ConvergedSessionInvalidationTracker.suspend();
	         
	         // First, handle the sessions we are actively managing
	         ClusteredSipSession<? extends OutgoingDistributableSessionData> sessions[] = findLocalSipSessions();
	         for (int i = 0; i < sessions.length; ++i)
	         {
	            try
	            {
	               ClusteredSipSession<? extends OutgoingDistributableSessionData> session = sessions[i];
	               if(session == null)
	               {
	                  log_.warn("processExpirationPassivation(): processing null session at index " +i);
	                  continue;
	               }

	               if (expire)
	               {
	                  // JBAS-2403. Check for outdated sessions where we think
	                  // the local copy has timed out.  If found, refresh the
	                  // session from the cache in case that might change the timeout
	                  if (session.isOutdated() && !(session.isValid(false)))
	                  {
	                     // FIXME in AS 5 every time we get a notification from the distributed
	                     // cache of an update, we get the latest timestamp. So
	                     // we shouldn't need to do a full session load here. A load
	                     // adds a risk of an unintended data gravitation.
	                     
	                     // JBAS-2792 don't assign the result of loadSession to session
	                     // just update the object from the cache or fall through if
	                     // the session has been removed from the cache
	                     loadSipSession(session.getKey(), false, null, null);
	                  }
	   
	                  // Do a normal invalidation check that will expire the
	                  // session if it has timed out
	                  // DON'T SYNCHRONIZE on session here -- isValid() and
	                  // expire() are meant to be multi-threaded and synchronize
	                  // properly internally; synchronizing externally can lead
	                  // to deadlocks!!
	                  if (!session.isValid()) continue;
	               }
	                
	               // we now have a valid session; store it so we can check later
	               // if we need to passivate it
	               if (passivate)
	               {
	                  passivationChecks.add(new SipSessionPassivationCheck(session));
	               }
	               
	            }
	            catch (Exception ex)
	            {
	               log_.error("processExpirationPassivation(): failed handling " + 
	                          sessions[i].getKey() + " with exception: " + 
	                          ex, ex);
	            }
	         }

	         // Next, handle any unloaded sessions

	         
	         // We may have not gotten replication of a timestamp for requests 
	         // that occurred w/in maxUnreplicatedInterval_ of the previous
	         // request. So we add a grace period to avoid flushing a session early
	         // and permanently losing part of its node structure in JBoss Cache.
	         long maxUnrep = getMaxUnreplicatedInterval() < 0 ? 60 : getMaxUnreplicatedInterval();
	         
	         Map<SipSessionKey, OwnedSessionUpdate> unloaded = new HashMap<SipSessionKey, OwnedSessionUpdate>(unloadedSipSessions_);
	         for (Map.Entry<SipSessionKey, OwnedSessionUpdate> entry : unloaded.entrySet())
	         {
	        	SipSessionKey key = entry.getKey();
	            OwnedSessionUpdate osu = entry.getValue();
	            
	            long now = System.currentTimeMillis();
	            long elapsed = (now - osu.updateTime);
	            try
	            {
	               if (expire && osu.maxInactive >= 1 && elapsed >= (osu.maxInactive + maxUnrep) * 1000L)
	               {
	                  //if (osu.passivated && osu.owner == null)
	                  if (osu.passivated)
	                  {
	                     // Passivated session needs to be expired. A call to 
	                     // findSession will bring it out of passivation
	                     SipSession session = getSipSession(key, false, null, null);
	                     if (session != null)
	                     {
	                        session.isValid(); // will expire
	                        continue;
	                     }
	                  }
	                  
	                  // If we get here either !osu.passivated, or we don't own
	                  // the session or the session couldn't be reactivated (invalidated by user). 
	                  // Either way, do a cleanup
	                  getDistributedCacheConvergedSipManager().removeSessionLocal(key, osu.owner);
	                  unloadedSipSessions_.remove(key);
	                  stats_.removeStats(key.toString());
	                  
	               }
	               else if (passivate && !osu.passivated)
	               {  
	                  // we now have a valid session; store it so we can check later
	                  // if we need to passivate it
	                  passivationChecks.add(new SipSessionPassivationCheck(key, osu));
	               }
	            } 
	            catch (Exception ex)
	            {
	               log_.error("processExpirationPassivation(): failed handling unloaded session " + 
	                       key, ex);
	            }
	         }
	         
	         // Now, passivations
	         if (passivate)
	         {
	            // Iterate through sessions, earliest lastAccessedTime to latest
	            for (SipSessionPassivationCheck passivationCheck : passivationChecks)
	            {               
	               try
	               {
	                  long timeNow = System.currentTimeMillis();
	                  long timeIdle = timeNow - passivationCheck.getLastUpdate();
	                  // if maxIdle time configured, means that we need to passivate sessions that have
	                  // exceeded the max allowed idle time
	                  if (passivationMax >= 0 
	                        && timeIdle > passivationMax)
	                  {
	                     passivationCheck.passivate();
	                  }
	                  // If the session didn't exceed the passivationMaxIdleTime_, see   
	                  // if the number of sessions managed by this manager greater than the max allowed 
	                  // active sessions, passivate the session if it exceed passivationMinIdleTime_ 
	                  else if (maxActiveAllowed_ > 0 
	                              && passivationMin > 0 
	                              && calcActiveSessions() >= maxActiveAllowed_ 
	                              && timeIdle > passivationMin)
	                  {
	                     passivationCheck.passivate();
	                  }
	                  else
	                  {
	                     // the entries are ordered by lastAccessed, so once
	                     // we don't passivate one, we won't passivate any
	                     break;
	                  }
	               }
	               catch (Exception e)
	               {
	                  String unloadMark = passivationCheck.isUnloaded() ? "unloaded " : "";
	                  log_.error("processExpirationPassivation(): failed passivating " + unloadMark + "session " + 
	                        passivationCheck.getKey(), e);
	               }                  
	            }
	         }
	      }
	      catch (Exception ex)
	      {
	         log_.error("processExpirationPassivation(): failed with exception: " + ex, ex);
	      }
	      finally
	      {
	         SessionInvalidationTracker.resume();
	      }
	      
//	      if (trace_)
//	      { 
//	         log_.trace("processExpirationPassivation(): Completed ...");
//	         log_.trace("processExpirationPassivation(): active sessions = " + calcActiveSipSessions());
//	         log_.trace("processExpirationPassivation(): expired sessions = " + expiredCounter_);
//	         if (passivate)
//	         {
//	            log_.trace("processExpirationPassivation(): passivated count = " + getPassivatedSipSessionCount());
//	         }
//	      }
	   }
	   
	   /**
	    * {@inheritDoc}
	    */
	   protected void processExpirationSipApplicationSessionPassivation()
	   {      
	      boolean expire = maxInactiveInterval_ >= 0;
	      boolean passivate = isPassivationEnabled();
	      
	      long passivationMax = passivationMaxIdleTime_ * 1000L;
	      long passivationMin = passivationMinIdleTime_ * 1000L;

//	      if (trace_)
//	      { 
//	         log_.trace("processExpirationPassivation(): Looking for sessions that have expired ...");
//	         log_.trace("processExpirationPassivation(): active sessions = " + calcActiveSipApplicationSessions());
//	         log_.trace("processExpirationPassivation(): expired sessions = " + expiredCounter_);
//	         if (passivate)
//	         {
//	            log_.trace("processExpirationPassivation(): passivated count = " + getPassivatedSipApplicationSessionCount());
//	         }
//	      }
	      
	      // Holder for sessions or OwnedSessionUpdates that survive expiration,
	      // sorted by last acccessed time
	      Set<SipApplicationSessionPassivationCheck> passivationChecks = new TreeSet<SipApplicationSessionPassivationCheck>();
	      
	      try
	      {
	         // Don't track sessions invalidated via this method as if they
	         // were going to be re-requested by the thread
	         ConvergedSessionInvalidationTracker.suspend();
	         
	         // First, handle the sessions we are actively managing
	         ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> sessions[] = findLocalSipApplicationSessions();
	         for (int i = 0; i < sessions.length; ++i)
	         {
	            try
	            {
	               ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = sessions[i];
	               if(session == null)
	               {
	                  log_.warn("processExpirationPassivation(): processing null session at index " +i);
	                  continue;
	               }

	               if (expire)
	               {
	                  // JBAS-2403. Check for outdated sessions where we think
	                  // the local copy has timed out.  If found, refresh the
	                  // session from the cache in case that might change the timeout
	                  if (session.isOutdated() && !(session.isValid(false)))
	                  {
	                     // FIXME in AS 5 every time we get a notification from the distributed
	                     // cache of an update, we get the latest timestamp. So
	                     // we shouldn't need to do a full session load here. A load
	                     // adds a risk of an unintended data gravitation.
	                     
	                     // JBAS-2792 don't assign the result of loadSession to session
	                     // just update the object from the cache or fall through if
	                     // the session has been removed from the cache
	                     loadSipApplicationSession(session.getKey(), false);
	                  }
	   
	                  // Do a normal invalidation check that will expire the
	                  // session if it has timed out
	                  // DON'T SYNCHRONIZE on session here -- isValid() and
	                  // expire() are meant to be multi-threaded and synchronize
	                  // properly internally; synchronizing externally can lead
	                  // to deadlocks!!
	                  if (!session.isValid()) continue;
	               }
	                
	               // we now have a valid session; store it so we can check later
	               // if we need to passivate it
	               if (passivate)
	               {
	                  passivationChecks.add(new SipApplicationSessionPassivationCheck(session));
	               }
	               
	            }
	            catch (Exception ex)
	            {
	               log_.error("processExpirationPassivation(): failed handling " + 
	                          sessions[i].getKey() + " with exception: " + 
	                          ex, ex);
	            }
	         }

	         // Next, handle any unloaded sessions

	         
	         // We may have not gotten replication of a timestamp for requests 
	         // that occurred w/in maxUnreplicatedInterval_ of the previous
	         // request. So we add a grace period to avoid flushing a session early
	         // and permanently losing part of its node structure in JBoss Cache.
	         long maxUnrep = getMaxUnreplicatedInterval() < 0 ? 60 : getMaxUnreplicatedInterval();
	         
	         Map<SipApplicationSessionKey, OwnedSessionUpdate> unloaded = new HashMap<SipApplicationSessionKey, OwnedSessionUpdate>(unloadedSipApplicationSessions_);
	         for (Map.Entry<SipApplicationSessionKey, OwnedSessionUpdate> entry : unloaded.entrySet())
	         {
	        	SipApplicationSessionKey key = entry.getKey();
	            OwnedSessionUpdate osu = entry.getValue();
	            
	            long now = System.currentTimeMillis();
	            long elapsed = (now - osu.updateTime);
	            try
	            {
	               if (expire && osu.maxInactive >= 1 && elapsed >= (osu.maxInactive + maxUnrep) * 1000L)
	               {
	                  //if (osu.passivated && osu.owner == null)
	                  if (osu.passivated)
	                  {
	                     // Passivated session needs to be expired. A call to 
	                     // findSession will bring it out of passivation
	                     SipApplicationSession session = getSipApplicationSession(key, false);
	                     if (session != null)
	                     {
	                        session.isValid(); // will expire
	                        continue;
	                     }
	                  }
	                  
	                  // If we get here either !osu.passivated, or we don't own
	                  // the session or the session couldn't be reactivated (invalidated by user). 
	                  // Either way, do a cleanup
	                  getDistributedCacheConvergedSipManager().removeSessionLocal(key, osu.owner);
	                  unloadedSipSessions_.remove(key);
	                  stats_.removeStats(key.toString());
	                  
	               }
	               else if (passivate && !osu.passivated)
	               {  
	                  // we now have a valid session; store it so we can check later
	                  // if we need to passivate it
	                  passivationChecks.add(new SipApplicationSessionPassivationCheck(key, osu));
	               }
	            } 
	            catch (Exception ex)
	            {
	               log_.error("processExpirationPassivation(): failed handling unloaded session " + 
	                       key, ex);
	            }
	         }
	         
	         // Now, passivations
	         if (passivate)
	         {
	            // Iterate through sessions, earliest lastAccessedTime to latest
	            for (SipApplicationSessionPassivationCheck passivationCheck : passivationChecks)
	            {               
	               try
	               {
	                  long timeNow = System.currentTimeMillis();
	                  long timeIdle = timeNow - passivationCheck.getLastUpdate();
	                  // if maxIdle time configured, means that we need to passivate sessions that have
	                  // exceeded the max allowed idle time
	                  if (passivationMax >= 0 
	                        && timeIdle > passivationMax)
	                  {
	                     passivationCheck.passivate();
	                  }
	                  // If the session didn't exceed the passivationMaxIdleTime_, see   
	                  // if the number of sessions managed by this manager greater than the max allowed 
	                  // active sessions, passivate the session if it exceed passivationMinIdleTime_ 
	                  else if (maxActiveAllowed_ > 0 
	                              && passivationMin > 0 
	                              && calcActiveSessions() >= maxActiveAllowed_ 
	                              && timeIdle > passivationMin)
	                  {
	                     passivationCheck.passivate();
	                  }
	                  else
	                  {
	                     // the entries are ordered by lastAccessed, so once
	                     // we don't passivate one, we won't passivate any
	                     break;
	                  }
	               }
	               catch (Exception e)
	               {
	                  String unloadMark = passivationCheck.isUnloaded() ? "unloaded " : "";
	                  log_.error("processExpirationPassivation(): failed passivating " + unloadMark + "session " + 
	                        passivationCheck.getKey(), e);
	               }                  
	            }
	         }
	      }
	      catch (Exception ex)
	      {
	         log_.error("processExpirationPassivation(): failed with exception: " + ex, ex);
	      }
	      finally
	      {
	         SessionInvalidationTracker.resume();
	      }
	      
//	      if (trace_)
//	      { 
//	         log_.trace("processExpirationPassivation(): Completed ...");
//	         log_.trace("processExpirationPassivation(): active sessions = " + calcActiveSipApplicationSessions());
//	         log_.trace("processExpirationPassivation(): expired sessions = " + expiredCounter_);
//	         if (passivate)
//	         {
//	            log_.trace("processExpirationPassivation(): passivated count = " + getPassivatedSipApplicationSessionCount());
//	         }
//	      }
	   }
	
	/**
	 * Loads a session from the distributed store. If an existing session with
	 * the id is already under local management, that session's internal state
	 * will be updated from the distributed store. Otherwise a new session will
	 * be created and added to the collection of those sessions under local
	 * management.
	 * 
	 * @param key
	 *            id of the session-id with any jvmRoute removed
	 * 
	 * @return the session or <code>null</code> if the session cannot be found
	 *         in the distributed store
	 * 
	 *         TODO refactor this into 2 overloaded methods -- one that takes a
	 *         ClusteredSession and populates it and one that takes an id,
	 *         creates the session and calls the first
	 */
	protected ClusteredSipSession loadSipSession(final SipSessionKey key, final boolean create, final SipFactoryImpl sipFactoryImpl, final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		if (key == null) {
			return null;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("load sip session " + key + ", create = " + create + " sip app session = "+ sipApplicationSessionImpl);
		}
		SipFactoryImpl sipFactory = sipFactoryImpl;
		if(sipFactory == null) {
			sipFactory = this.getSipFactoryImpl();
		}
		long begin = System.currentTimeMillis();
		boolean mustAdd = false;
		boolean passivated = false;
		 
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipSession) sipManagerDelegate.getSipSession(key, create, sipFactory, sipApplicationSessionImpl);
		ClusteredSipSession<? extends OutgoingDistributableSessionData> newTempSession = session;
		boolean initialLoad = false;
		if (session == null && sipApplicationSessionImpl != null) {
			// This is either the first time we've seen this session on this
			// server, or we previously expired it and have since gotten
			// a replication message from another server
			mustAdd = true;
			initialLoad = true;
			newTempSession = (ClusteredSipSession<? extends OutgoingDistributableSessionData>) sipManagerDelegate.getSipSession(key, true, sipFactory, sipApplicationSessionImpl);
			OwnedSessionUpdate osu = unloadedSipSessions_.get(key);
	        passivated = (osu != null && osu.passivated);
		}		
		if(newTempSession != null) {
			synchronized (newTempSession) {
				ClusteredSipSession sessionInCache = null;
				boolean doTx = false;
				BatchingManager batchingManager = getDistributedCacheConvergedSipManager().getBatchingManager();
				try {
					// We need transaction so any data gravitation replication
					// is sent in batch.
					// Don't do anything if there is already transaction context
					// associated with this thread.
					if (batchingManager.isBatchInProgress() == false)
		            {
		               batchingManager.startBatch();
		               doTx = true;
		            }

		            IncomingDistributableSessionData data = getDistributedCacheConvergedSipManager().getSessionData(key, initialLoad);
		            if (data != null)
		            {
		               session.update(data);
		            }
		            else
		            {
		               // Clunky; we set the session variable to null to indicate
		               // no data so move on
		               session = null;
		            }
		            
		            if (session != null)
		            {
		               ClusteredSessionNotificationCause cause = passivated ? ClusteredSessionNotificationCause.ACTIVATION 
		                                                                    : ClusteredSessionNotificationCause.FAILOVER;
		               session.notifyDidActivate(cause);
		            }
	
//					sessionInCache = proxy_.loadSipSession(sipApplicationSessionImpl.getId(), key.toString(), newTempSession);
				} catch (Exception ex) {
					try {
						// if(doTx)
						// Let's set it no matter what.
						batchingManager.setBatchRollbackOnly();
					} catch (Exception exn) {
						log_.error("Problem rolling back session mgmt transaction",
								exn);
					}
	
					// We will need to alert Tomcat of this exception.
					if (ex instanceof RuntimeException)
						throw (RuntimeException) ex;
	
					throw new RuntimeException("Failed to load session " + key.toString(),
							ex);
				} finally {
					if (doTx)
						batchingManager.endBatch();
				}
	
				if (session != null)
		         {            
		            if (mustAdd)
		            {
		               add(session, false); // don't replicate
		               if (!passivated)
		               {
		                  session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
		               }
		            }
		            long elapsed = System.currentTimeMillis() - begin;
		            stats_.updateLoadStats(key.toString(), elapsed);

		            if (trace_)
		            {
		               log_.trace("loadSession(): id= " + key + ", session=" + session);
		            }
		         }
		         else if (trace_)
		         {
		            log_.trace("loadSession(): session " + key +
		                       " not found in distributed cache");
		         }
//				if (sessionInCache != null) {
//					// Need to initialize.
//					sessionInCache.initAfterLoad(this);
//					if (mustAdd)
//						add(sessionInCache, false); // don't replicate
//					long elapsed = System.currentTimeMillis() - begin;
//					stats_.updateLoadStats(key.toString(), elapsed);
//	
//					if (log_.isDebugEnabled()) {
//						log_.debug("loadSession(): id= " + key.toString() + ", session="
//								+ newTempSession);
//					}
//					return sessionInCache;
//				} else if (log_.isDebugEnabled()) {
//					log_.debug("loadSession(): session " + key.toString()
//							+ " not found in distributed cache");
//				}
			}
		}		
		return session;
	}
	
	/**
	 * Loads a session from the distributed store. If an existing session with
	 * the id is already under local management, that session's internal state
	 * will be updated from the distributed store. Otherwise a new session will
	 * be created and added to the collection of those sessions under local
	 * management.
	 * 
	 * @param key
	 *            id of the session-id with any jvmRoute removed
	 * 
	 * @return the session or <code>null</code> if the session cannot be found
	 *         in the distributed store
	 * 
	 *         TODO refactor this into 2 overloaded methods -- one that takes a
	 *         ClusteredSession and populates it and one that takes an id,
	 *         creates the session and calls the first
	 */
	protected ClusteredSipApplicationSession loadSipApplicationSession(final SipApplicationSessionKey key, final boolean create) {
		if (key == null) {
			return null;
		}

		long begin = System.currentTimeMillis();
		boolean mustAdd = false;
		boolean passivated = false;
		boolean initialLoad = false;
		
		ClusteredSipApplicationSession session = (ClusteredSipApplicationSession) sipManagerDelegate.getSipApplicationSession(key, create);
		if (session == null) {			
			// This is either the first time we've seen this session on this
			// server, or we previously expired it and have since gotten
			// a replication message from another server
			mustAdd = true;
			initialLoad = true;
			session = (ClusteredSipApplicationSession) sipManagerDelegate.getSipApplicationSession(key, true);
			OwnedSessionUpdate osu = unloadedSipApplicationSessions_.get(key);
	        passivated = (osu != null && osu.passivated);
		}		
		synchronized (session) {
//			ClusteredSipApplicationSession sessionInCache = null;
			boolean doTx = false;
			BatchingManager batchingManager = getDistributedCacheConvergedSipManager().getBatchingManager();
			try {
				// We need transaction so any data gravitation replication
				// is sent in batch.
				// Don't do anything if there is already transaction context
				// associated with this thread.
				if (batchingManager.isBatchInProgress() == false)
	            {
	               batchingManager.startBatch();
	               doTx = true;
	            }

				IncomingDistributableSessionData data = getDistributedCacheConvergedSipManager().getSessionData(key, initialLoad);
				if (data != null)
	            {
	               session.update(data);
	            }
	            else
	            {
	               // Clunky; we set the session variable to null to indicate
	               // no data so move on
	               session = null;
	            }
	            
	            if (session != null)
	            {
	               ClusteredSessionNotificationCause cause = passivated ? ClusteredSessionNotificationCause.ACTIVATION 
	                                                                    : ClusteredSessionNotificationCause.FAILOVER;
	               session.notifyDidActivate(cause);
	            }
			} catch (Exception ex) {
				try {
					// if(doTx)
					// Let's set it no matter what.
					batchingManager.setBatchRollbackOnly();
				} catch (Exception exn) {
					log_.error("Problem rolling back session mgmt transaction",
							exn);
				}

				// We will need to alert Tomcat of this exception.
				if (ex instanceof RuntimeException)
					throw (RuntimeException) ex;

				throw new RuntimeException("Failed to load session " + key,
						ex);
			} finally {
				if (doTx)
					batchingManager.endBatch();
			}
			if (session != null)
	         {            
	            if (mustAdd)
	            {
	               add(session, false); // don't replicate
	               if (!passivated)
	               {
	                  session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
	               }
	            }
	            long elapsed = System.currentTimeMillis() - begin;
	            stats_.updateLoadStats(key.toString(), elapsed);

	            if (trace_)
	            {
	               log_.trace("loadSession(): id= " + key.toString() + ", session=" + session);
	            }
	         }
	         else if (trace_)
	         {
	            log_.trace("loadSession(): session " + key.toString() +
	                       " not found in distributed cache");
	         }
//			if (sessionInCache != null) {
//				// Need to initialize.
//				sessionInCache.initAfterLoad(this);
//				if (mustAdd)
//					add(sessionInCache, false); // don't replicate
//				long elapsed = System.currentTimeMillis() - begin;
//				stats_.updateLoadStats(key.toString(), elapsed);
//
//				if (log_.isDebugEnabled()) {
//					log_.debug("loadSession(): id= " + key.toString() + ", session="
//							+ sessionInCache);
//				}
//				return sessionInCache;
//			} else if (log_.isDebugEnabled()) {
//				log_.debug("loadSession(): session " + key.toString()
//						+ " not found in distributed cache");				
//			}
		}
//		ConvergedSessionReplicationContext.bindSipApplicationSession(session,
//				snapshotManager_);
		return session;
	}
	
	/**
	 * Places the current session contents in the distributed cache and
	 * replicates them to the cluster
	 * 
	 * @param session
	 *            the session. Cannot be <code>null</code>.
	 */
	protected void processSipSessionRepl(ClusteredSipSession session) {
		// If we are using SESSION granularity, we don't want to initiate a TX
		// for a single put
		boolean notSession = (getReplicationGranularity() != ReplicationGranularity.SESSION);
		boolean doTx = false;
		BatchingManager batchingManager = getDistributedCacheConvergedSipManager().getBatchingManager();
		try {
			// We need transaction so all the replication are sent in batch.
	         // Don't do anything if there is already transaction context
	         // associated with this thread.
	         if(notSession && batchingManager.isBatchInProgress() == false)
	         {
	            batchingManager.startBatch();
	            doTx = true;
	         }

			 session.processSipSessionReplication();
		} catch (Exception ex) {
			if (log_.isDebugEnabled())
				log_.debug("processSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					batchingManager.setBatchRollbackOnly();
			} catch (Exception exn) {
				log_.error("Caught exception rolling back transaction", exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;

			throw new RuntimeException(
					"JBossCacheManager.processSessionRepl(): "
							+ "failed to replicate session.", ex);
		} finally {
			if (doTx)
				batchingManager.endBatch();
		}
	}
	
	/**
	 * Places the current session contents in the distributed cache and
	 * replicates them to the cluster
	 * 
	 * @param session
	 *            the session. Cannot be <code>null</code>.
	 */
	protected void processSipApplicationSessionRepl(ClusteredSipApplicationSession session) {
		// If we are using SESSION granularity, we don't want to initiate a TX
		// for a single put
		boolean notSession = (getReplicationGranularity() != ReplicationGranularity.SESSION);
		boolean doTx = false;
		BatchingManager batchingManager = getDistributedCacheConvergedSipManager().getBatchingManager();
		try {
			// We need transaction so all the replication are sent in batch.
	         // Don't do anything if there is already transaction context
	         // associated with this thread.
	         if(notSession && batchingManager.isBatchInProgress() == false)
	         {
	            batchingManager.startBatch();
	            doTx = true;
	         }

			 session.processSipApplicationSessionReplication();
		} catch (Exception ex) {
			if (log_.isDebugEnabled())
				log_.debug("processSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					batchingManager.setBatchRollbackOnly();
			} catch (Exception exn) {
				log_.error("Caught exception rolling back transaction", exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;

			throw new RuntimeException(
					"JBossCacheManager.processSessionRepl(): "
							+ "failed to replicate session.", ex);
		} finally {
			if (doTx)
				batchingManager.endBatch();
		}
	}
	
	/**
	 * Session passivation logic for an actively managed session.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processSipSessionPassivation(SipSessionKey key) {
		// get the session from the local map
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = findLocalSipSession(key, false, null);
		// Remove actively managed session and add to the unloaded sessions
		// if it's already unloaded session (session == null) don't do anything,
		if (session != null) {
			synchronized (session) {
				if (trace_) {
					log_.trace("Passivating session with id: " + key);
				}

				session
						.notifyWillPassivate(ClusteredSessionNotificationCause.PASSIVATION);
				getDistributedCacheConvergedSipManager().evictSession(key);
				sipSessionPassivated();

				// Put the session in the unloadedSessions map. This will
				// expose the session to regular invalidation.
				Object obj = unloadedSipSessions_.put(key,
						new OwnedSessionUpdate(null, session
								.getLastAccessedTimeInternal(), session
								.getMaxInactiveInterval(), true));
				if (trace_) {
					if (obj == null) {
						log_.trace("New session " + key
								+ " added to unloaded session map");
					} else {
						log_.trace("Updated timestamp for unloaded session "
								+ key);
					}
				}
				sessions_.remove(key);
			}
		} else if (trace_) {
			log_.trace("processSessionPassivation():  could not find session "
					+ key);
		}
	}
	
	/**
	 * Session passivation logic for an actively managed session.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processSipApplicationSessionPassivation(SipApplicationSessionKey key) {
		// get the session from the local map
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = findLocalSipApplicationSession(key, false);
		// Remove actively managed session and add to the unloaded sessions
		// if it's already unloaded session (session == null) don't do anything,
		if (session != null) {
			synchronized (session) {
				if (trace_) {
					log_.trace("Passivating session with id: " + key);
				}

				session
						.notifyWillPassivate(ClusteredSessionNotificationCause.PASSIVATION);
				getDistributedCacheConvergedSipManager().evictSession(key);
				sipApplicationSessionPassivated();

				// Put the session in the unloadedSessions map. This will
				// expose the session to regular invalidation.
				Object obj = unloadedSipApplicationSessions_.put(key,
						new OwnedSessionUpdate(null, session
								.getLastAccessedTimeInternal(), session
								.getMaxInactiveInterval(), true));
				if (trace_) {
					if (obj == null) {
						log_.trace("New session " + key
								+ " added to unloaded session map");
					} else {
						log_.trace("Updated timestamp for unloaded session "
								+ key);
					}
				}
				sessions_.remove(key);
			}
		} else if (trace_) {
			log_.trace("processSessionPassivation():  could not find session "
					+ key);
		}
	}

	/**
	 * Session passivation logic for sessions only in the distributed store.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processUnloadedSipSessionPassivation(SipSessionKey key,
			OwnedSessionUpdate osu) {
		if (trace_) {
			log_.trace("Passivating session with id: " + key);
		}

		getDistributedCacheConvergedSipManager().evictSession(key, osu.owner);
		osu.passivated = true;
		sipSessionPassivated();
	}
	
	/**
	 * Session passivation logic for sessions only in the distributed store.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processUnloadedSipApplicationSessionPassivation(SipApplicationSessionKey key,
			OwnedSessionUpdate osu) {
		if (trace_) {
			log_.trace("Passivating session with id: " + key);
		}

		getDistributedCacheConvergedSipManager().evictSession(key, osu.owner);
		osu.passivated = true;
		sipApplicationSessionPassivated();
	}

	private void sipSessionPassivated() {
		int pc = sipSessionPassivatedCount_.incrementAndGet();
		int max = sipSessionMaxPassivatedCount_.get();
		while (pc > max) {
			if (!sipSessionMaxPassivatedCount_.compareAndSet(max, pc)) {
				max = sipSessionMaxPassivatedCount_.get();
			}
		}
	}
	
	private void sipApplicationSessionPassivated() {
		int pc = sipApplicationSessionPassivatedCount_.incrementAndGet();
		int max = sipApplicationSessionMaxPassivatedCount_.get();
		while (pc > max) {
			if (!sipApplicationSessionMaxPassivatedCount_.compareAndSet(max, pc)) {
				max = sipApplicationSessionMaxPassivatedCount_.get();
			}
		}
	}
	
	/**
	 * Gets the ids of all sessions in the distributed cache and adds them to
	 * the unloaded sessions map, along with their lastAccessedTime and their
	 * maxInactiveInterval. Passivates overage or excess sessions.
	 */
	private void initializeUnloadedSipSessions() {
		Map<SipSessionKey, String> sessions = getDistributedCacheConvergedSipManager().getSipSessionKeys();
		if (sessions != null) {
			boolean passivate = isPassivationEnabled();

			long passivationMax = passivationMaxIdleTime_ * 1000L;
			long passivationMin = passivationMinIdleTime_ * 1000L;

			for (Map.Entry<SipSessionKey, String> entry : sessions.entrySet()) {
				SipSessionKey key = entry.getKey();
				String owner = entry.getValue();

				long ts = -1;
				DistributableSessionMetadata md = null;
				try {
					IncomingDistributableSessionData sessionData = getDistributedCacheConvergedSipManager()
							.getSessionData(key, owner, false);
					ts = sessionData.getTimestamp();
					md = sessionData.getMetadata();
				} catch (Exception e) {
					// most likely a lock conflict if the session is being
					// updated remotely;
					// ignore it and use default values for timstamp and
					// maxInactive
					log_.debug("Problem reading metadata for session " + key
							+ " -- " + e.toString());
				}

				long lastMod = ts == -1 ? System.currentTimeMillis() : ts;
				int maxLife = md == null ? getMaxInactiveInterval() : md
						.getMaxInactiveInterval();

				OwnedSessionUpdate osu = new OwnedSessionUpdate(owner, lastMod,
						maxLife, false);
				unloadedSipSessions_.put(key, osu);
				if (passivate) {
					try {
						long elapsed = System.currentTimeMillis() - lastMod;
						// if maxIdle time configured, means that we need to
						// passivate sessions that have
						// exceeded the max allowed idle time
						if (passivationMax >= 0 && elapsed > passivationMax) {
							if (trace_) {
								log_.trace("Elapsed time of " + elapsed
										+ " for session " + key
										+ " exceeds max of " + passivationMax
										+ "; passivating");
							}
							processUnloadedSipSessionPassivation(key, osu);
						}
						// If the session didn't exceed the
						// passivationMaxIdleTime_, see
						// if the number of sessions managed by this manager
						// greater than the max allowed
						// active sessions, passivate the session if it exceed
						// passivationMinIdleTime_
						else if (maxActiveAllowed_ > 0 && passivationMin >= 0
								&& calcActiveSessions() > maxActiveAllowed_
								&& elapsed >= passivationMin) {
							if (trace_) {
								log_.trace("Elapsed time of " + elapsed
										+ " for session " + key
										+ " exceeds min of " + passivationMin
										+ "; passivating");
							}
							processUnloadedSipSessionPassivation(key, osu);
						}
					} catch (Exception e) {
						// most likely a lock conflict if the session is being
						// updated remotely; ignore it
						log_.debug("Problem passivating session " + key
								+ " -- " + e.toString());
					}
				}
			}
		}
	}
	
	/**
	 * Gets the ids of all sessions in the distributed cache and adds them to
	 * the unloaded sessions map, along with their lastAccessedTime and their
	 * maxInactiveInterval. Passivates overage or excess sessions.
	 */
	private void initializeUnloadedSipApplicationSessions() {
		Map<SipApplicationSessionKey, String> sessions = getDistributedCacheConvergedSipManager().getSipApplicationSessionKeys();
		if (sessions != null) {
			boolean passivate = isPassivationEnabled();

			long passivationMax = passivationMaxIdleTime_ * 1000L;
			long passivationMin = passivationMinIdleTime_ * 1000L;

			for (Map.Entry<SipApplicationSessionKey, String> entry : sessions.entrySet()) {
				SipApplicationSessionKey key = entry.getKey();
				String owner = entry.getValue();

				long ts = -1;
				DistributableSessionMetadata md = null;
				try {
					IncomingDistributableSessionData sessionData = getDistributedCacheConvergedSipManager()
							.getSessionData(key, owner, false);
					ts = sessionData.getTimestamp();
					md = sessionData.getMetadata();
				} catch (Exception e) {
					// most likely a lock conflict if the session is being
					// updated remotely;
					// ignore it and use default values for timstamp and
					// maxInactive
					log_.debug("Problem reading metadata for session " + key
							+ " -- " + e.toString());
				}

				long lastMod = ts == -1 ? System.currentTimeMillis() : ts;
				int maxLife = md == null ? getMaxInactiveInterval() : md
						.getMaxInactiveInterval();

				OwnedSessionUpdate osu = new OwnedSessionUpdate(owner, lastMod,
						maxLife, false);
				unloadedSipApplicationSessions_.put(key, osu);
				if (passivate) {
					try {
						long elapsed = System.currentTimeMillis() - lastMod;
						// if maxIdle time configured, means that we need to
						// passivate sessions that have
						// exceeded the max allowed idle time
						if (passivationMax >= 0 && elapsed > passivationMax) {
							if (trace_) {
								log_.trace("Elapsed time of " + elapsed
										+ " for session " + key
										+ " exceeds max of " + passivationMax
										+ "; passivating");
							}
							processUnloadedSipApplicationSessionPassivation(key, osu);
						}
						// If the session didn't exceed the
						// passivationMaxIdleTime_, see
						// if the number of sessions managed by this manager
						// greater than the max allowed
						// active sessions, passivate the session if it exceed
						// passivationMinIdleTime_
						else if (maxActiveAllowed_ > 0 && passivationMin >= 0
								&& calcActiveSessions() > maxActiveAllowed_
								&& elapsed >= passivationMin) {
							if (trace_) {
								log_.trace("Elapsed time of " + elapsed
										+ " for session " + key
										+ " exceeds min of " + passivationMin
										+ "; passivating");
							}
							processUnloadedSipApplicationSessionPassivation(key, osu);
						}
					} catch (Exception e) {
						// most likely a lock conflict if the session is being
						// updated remotely; ignore it
						log_.debug("Problem passivating session " + key
								+ " -- " + e.toString());
					}
				}
			}
		}
	}
	
//	/**
//	 * Instantiate a SnapshotManager and ClusteredSessionValve and add the valve
//	 * to our parent Context's pipeline. Add a JvmRouteValve and
//	 * BatchReplicationClusteredSessionValve if needed.
//	 * 
//	 */
//	private void installValves() {
//		if (useJK_) {
//			log_.info("We are using mod_jk(2) for load-balancing. "
//					+ "Will add JvmRouteValve.");
//
//			installContextValve(new ConvergedJvmRouteValve(this));
//		}
//
//		// Add batch replication valve if needed.
//		// TODO -- should we add this even if not FIELD in case a cross-context
//		// call traverses a field-based webapp?
//		if (replicationGranularity_ == WebMetaData.REPLICATION_GRANULARITY_FIELD
//				&& Boolean.TRUE.equals(replicationFieldBatchMode_)) {
//			Valve batchValve = new BatchReplicationClusteredSessionValve(this);
//			log_
//					.debug("Adding BatchReplicationClusteredSessionValve for batch replication.");
//			installContextValve(batchValve);
//		}
//
//		// Add clustered session valve
//		ConvergedClusteredSessionValve valve = new ConvergedClusteredSessionValve(this);
//		installContextValve(valve);
//	}

	
	/**
	 * @return the SipFactoryImpl
	 */
	public SipFactoryImpl getSipFactoryImpl() {
		return sipManagerDelegate.getSipFactoryImpl();
	}

	/**
	 * @param sipFactoryImpl
	 *            the SipFactoryImpl to set
	 */
	public void setSipFactoryImpl(SipFactoryImpl sipFactoryImpl) {
		sipManagerDelegate.setSipFactoryImpl(sipFactoryImpl);
	}

	/**
	 * @return the container
	 */
	public Container getContainer() {
		return sipManagerDelegate.getContainer();
	}

	/**
	 * @param container
	 *            the container to set
	 */
	public void setContainer(Container container) {
		container_ = container;
		sipManagerDelegate.setContainer(container);
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession removeSipSession(final SipSessionKey key) {
		ClusteredSipSession<? extends OutgoingDistributableSessionData> clusterSess = (ClusteredSipSession<? extends OutgoingDistributableSessionData>) sipManagerDelegate.removeSipSession(key);
		if(clusterSess == null) {
			return null;
		}
		synchronized (clusterSess) {
			String realId = clusterSess.getId();

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from store with id: " + realId);
			}

			try {
				clusterSess.removeMyself();
			} finally {
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipSessionExpired(clusterSess,
						key, getSnapshotSipManager());
				
				// Track this session to prevent reincarnation by this request 
	            // from the distributed cache
	            ConvergedSessionInvalidationTracker.sipSessionInvalidated(key, this);

	            sipManagerDelegate.removeSipSession(key);
				stats_.removeStats(realId);
				
				// Compute how long this session has been alive, and update
	            // our statistics accordingly
//	            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
//	            sipSessionExpired(timeAlive);
			}
		}
		return clusterSess;
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession removeSipApplicationSession(
			final SipApplicationSessionKey key) {
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusterSess = (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>) sipManagerDelegate.removeSipApplicationSession(key);
		if(clusterSess == null) {
			return null;
		}
		synchronized (clusterSess) {
			String realId = clusterSess.getId();

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from store with id: " + realId);
			}

			try {
				clusterSess.removeMyself();
			} finally {
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipApplicationSessionExpired(clusterSess,
						key, getSnapshotSipManager());

				// Track this session to prevent reincarnation by this request 
	            // from the distributed cache
	            ConvergedSessionInvalidationTracker.sipApplicationSessionInvalidated(key, this);

	            sipManagerDelegate.removeSipApplicationSession(key);
				stats_.removeStats(realId);
				
				// Compute how long this session has been alive, and update
	            // our statistics accordingly
//	            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
//	            sipApplicationSessionExpired(timeAlive);
			}
		}
		return clusterSess;
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(
			final SipApplicationSessionKey key, final boolean create) {
		// Find it from the local store first
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = findLocalSipApplicationSession(key, false);

		// If we didn't find it locally, only check the distributed cache
		// if we haven't previously handled this session id on this request.
		// If we handled it previously but it's no longer local, that means
		// it's been invalidated. If we request an invalidated session from
		// the distributed cache, it will be missing from the local cache but
		// may still exist on other nodes (i.e. if the invalidation hasn't
		// replicated yet because we are running in a tx). With buddy
		// replication,
		// asking the local cache for the session will cause the out-of-date
		// session from the other nodes to be gravitated, thus resuscitating
		// the session.
		if (session == null
				&& !ConvergedSessionInvalidationTracker.isSessionInvalidated(key.toString(), this)) {
			if (logger.isDebugEnabled())
				log_.debug("Checking for sip app session " + key
						+ " in the distributed cache");

			session = loadSipApplicationSession(key, create);
			if (session != null) {
				add(session);
				// TODO should we advise of a new session?
				// tellNew();
			}
		} else if (session != null && session.isOutdated()) {
			if (logger.isDebugEnabled())
				log_.debug("Updating session " + key
						+ " from the distributed cache");

			// Need to update it from the cache
			loadSipApplicationSession(key, create);
		}

		if (session != null) {
			// Add this session to the set of those potentially needing
			// replication
			ConvergedSessionReplicationContext.bindSipApplicationSession(session,
					getSnapshotSipManager());
			// If we previously called passivate() on the session due to
	         // replication, we need to make an offsetting activate() call
	         if (session.getNeedsPostReplicateActivation())
	         {
	            session.notifyDidActivate(ClusteredSessionNotificationCause.REPLICATION);
	         }
		}

		return session;
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession getSipSession(final SipSessionKey key,
			final boolean create, final SipFactoryImpl sipFactoryImpl,
			final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		
		// Find it from the local store first
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = findLocalSipSession(key, false, sipApplicationSessionImpl);

		if(session == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("sip session " + key
					+ " not found in the local store");
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("sip session " + key
						+ " found in the local store " + session);
			}
		}
		boolean isSipSessionInvalidated = ConvergedSessionInvalidationTracker.isSessionInvalidated(key.toString(), this);
		if (logger.isDebugEnabled()) {
			logger.debug("sip session " + key
					+ " invalidated ? " + isSipSessionInvalidated);
		}
		// If we didn't find it locally, only check the distributed cache
		// if we haven't previously handled this session id on this request.
		// If we handled it previously but it's no longer local, that means
		// it's been invalidated. If we request an invalidated session from
		// the distributed cache, it will be missing from the local cache but
		// may still exist on other nodes (i.e. if the invalidation hasn't
		// replicated yet because we are running in a tx). With buddy
		// replication,
		// asking the local cache for the session will cause the out-of-date
		// session from the other nodes to be gravitated, thus resuscitating
		// the session.
		if (session == null
				&& !isSipSessionInvalidated) {
			if (logger.isDebugEnabled())
				logger.debug("Checking for sip session " + key
						+ " in the distributed cache");

			session = loadSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl);
//       	if (session != null)
//		    {
//		          add(session);
//		          // We now notify, since we've added a policy to allow listeners 
//		          // to discriminate. But the default policy will not allow the 
//		          // notification to be emitted for FAILOVER, so the standard
//		          // behavior is unchanged.
//		          session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
//		    }
		} else if (session != null && session.isOutdated()) {
			if (logger.isDebugEnabled())
				logger.debug("Updating session " + key
						+ " from the distributed cache");

			// Need to update it from the cache
			loadSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl);
		}

		if (session != null) {
			// Add this session to the set of those potentially needing
			// replication
			ConvergedSessionReplicationContext.bindSipSession(session,
					getSnapshotSipManager());
			
			// If we previously called passivate() on the session due to
	         // replication, we need to make an offsetting activate() call
	         if (session.getNeedsPostReplicateActivation())
	         {
	            session.notifyDidActivate(ClusteredSessionNotificationCause.REPLICATION);
	         }
		}

		return session;
	}

	/**
	 * Notifies the manager that a session in the distributed cache has been
	 * invalidated
	 * 
	 * FIXME This method is poorly named, as we will also get this callback when
	 * we use buddy replication and our copy of the session in JBC is being
	 * removed due to data gravitation.
	 * 
	 * @param realId
	 *            the session id excluding any jvmRoute
	 */
	public void notifyRemoteSipApplicationSessionInvalidation(String realId) {
		// Remove the session from our local map
		SipApplicationSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipApplicationSessionKey(realId);
		} catch (ParseException e) {
			//should never happen
			logger.error("An unexpected exception happened on parsing the following sip application session key " + realId, e);
			return;
		}
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipApplicationSession)sipManagerDelegate
				.removeSipApplicationSession(key);
		if (session == null) {
			// We weren't managing the session anyway. But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSipApplicationSessions_.remove(key) != null) {
				if (trace_)
					log_.trace("Removed entry for session " + key
							+ " from unloaded session map");
			}

			// If session has failed over and has been passivated here,
			// session will be null, but we'll have a TimeStatistic to clean up
			stats_.removeStats(realId);
		} else {
			// Expire the session
			// DON'T SYNCHRONIZE ON SESSION HERE -- isValid() and
			// expire() are meant to be multi-threaded and synchronize
			// properly internally; synchronizing externally can lead
			// to deadlocks!!
			boolean notify = false; // Don't notify listeners. SRV.10.7
			// allows this, and sending notifications
			// leads to all sorts of issues; e.g.
			// circular calls with ClusteredSSO and
			// notifying when all that's happening is
			// data gravitation due to random failover
			boolean localCall = false; // this call originated from the cache;
			// we have already removed session
			boolean localOnly = true; // Don't pass attr removals to cache

			// Ensure the correct TCL is in place
			// BES 2008/11/27 Why?
//			ContextClassLoaderSwitcher.SwitchContext switcher = null;
			try {
				// Don't track this invalidation is if it were from a request
				ConvergedSessionInvalidationTracker.suspend();

//				switcher = getContextClassLoaderSwitcher().getSwitchContext();
//				switcher.setClassLoader(tcl_);
				session.expire(notify, localCall, localOnly,
						ClusteredSessionNotificationCause.INVALIDATE);
			} finally {
				ConvergedSessionInvalidationTracker.resume();

				// Remove any stats for this session
				stats_.removeStats(realId);

//				if (switcher != null) {
//					switcher.reset();
//				}
			}
		}
	}
	
	/**
	 * Notifies the manager that a session in the distributed cache has been
	 * invalidated
	 * 
	 * FIXME This method is poorly named, as we will also get this callback when
	 * we use buddy replication and our copy of the session in JBC is being
	 * removed due to data gravitation.
	 * 
	 * @param realId
	 *            the session id excluding any jvmRoute
	 */
	public void notifyRemoteSipSessionInvalidation(String realId) {
		// Remove the session from our local map
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipSessionKey(realId);
		} catch (ParseException e) {
			//should never happen
			logger.error("An unexpected exception happened on parsing the following sip session key " + realId, e);
			return;
		}
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipSession)sipManagerDelegate
				.removeSipSession(key);
		if (session == null) {
			// We weren't managing the session anyway. But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSipSessions_.remove(key) != null) {
				if (trace_)
					log_.trace("Removed entry for session " + key
							+ " from unloaded session map");
			}

			// If session has failed over and has been passivated here,
			// session will be null, but we'll have a TimeStatistic to clean up
			stats_.removeStats(realId);
		} else {
			// Expire the session
			// DON'T SYNCHRONIZE ON SESSION HERE -- isValid() and
			// expire() are meant to be multi-threaded and synchronize
			// properly internally; synchronizing externally can lead
			// to deadlocks!!
			boolean notify = false; // Don't notify listeners. SRV.10.7
			// allows this, and sending notifications
			// leads to all sorts of issues; e.g.
			// circular calls with ClusteredSSO and
			// notifying when all that's happening is
			// data gravitation due to random failover
			boolean localCall = false; // this call originated from the cache;
			// we have already removed session
			boolean localOnly = true; // Don't pass attr removals to cache

			// Ensure the correct TCL is in place
			// BES 2008/11/27 Why?
//			ContextClassLoaderSwitcher.SwitchContext switcher = null;
			try {
				// Don't track this invalidation is if it were from a request
				ConvergedSessionInvalidationTracker.suspend();

//				switcher = getContextClassLoaderSwitcher().getSwitchContext();
//				switcher.setClassLoader(tcl_);
				session.expire(notify, localCall, localOnly,
						ClusteredSessionNotificationCause.INVALIDATE);
			} finally {
				ConvergedSessionInvalidationTracker.resume();

				// Remove any stats for this session
				stats_.removeStats(realId);

//				if (switcher != null) {
//					switcher.reset();
//				}
			}
		}
	}
	   
	/**
	 * Callback from the distributed cache notifying of a local modification to
	 * a session's attributes. Meant for use with FIELD granularity, where the
	 * session may not be aware of modifications.
	 * 
	 * @param realId
	 *            the session id excluding any jvmRoute
	 */
	public void notifySipApplicationSessionLocalAttributeModification(
			String realId) {
		SipApplicationSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipApplicationSessionKey(realId);
		} catch (ParseException e) {
			// should never happen
			logger
					.error(
							"An unexpected exception happened on parsing the following sip application session key "
									+ realId, e);
			return;
		}
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipApplicationSession) sipManagerDelegate
				.getSipApplicationSession(key, false);
		if (session != null) {
			session.sessionAttributesDirty();
		} else {
			log_.warn("Received local attribute notification for " + realId
					+ " but session is not locally active");
		}
	}
	
	/**
	 * Callback from the distributed cache notifying of a local modification to
	 * a session's attributes. Meant for use with FIELD granularity, where the
	 * session may not be aware of modifications.
	 * 
	 * @param realId
	 *            the session id excluding any jvmRoute
	 */
	public void notifySipSessionLocalAttributeModification(
			String realId) {
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipSessionKey(realId);
		} catch (ParseException e) {
			// should never happen
			logger
					.error(
							"An unexpected exception happened on parsing the following sip session key "
									+ realId, e);
			return;
		}
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipSession) sipManagerDelegate
				.getSipSession(key, false, null, null);
		if (session != null) {
			session.sessionAttributesDirty();
		} else {
			log_.warn("Received local attribute notification for " + realId
					+ " but session is not locally active");
		}
	}
	   
	/**
	 * Callback from the distributed cache to notify us that a session has been
	 * modified remotely.
	 * 
	 * @param realId
	 *            the session id, without any trailing jvmRoute
	 * @param dataOwner
	 *            the owner of the session. Can be <code>null</code> if the
	 *            owner is unknown.
	 * @param distributedVersion
	 *            the session's version per the distributed cache
	 * @param timestamp
	 *            the session's timestamp per the distributed cache
	 * @param metadata
	 *            the session's metadata per the distributed cache
	 */
	public boolean sipApplicationSessionChangedInDistributedCache(
			String realId, String dataOwner, int distributedVersion,
			long timestamp, DistributableSessionMetadata metadata) {
		boolean updated = true;
		SipApplicationSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipApplicationSessionKey(realId);
		} catch (ParseException e) {
			// should never happen
			logger
					.error(
							"An unexpected exception happened on parsing the following sip application session key "
									+ realId, e);
			return false;
		}
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = findLocalSipApplicationSession(
				key, false);
		if (session != null) {
			// Need to invalidate the loaded session. We get back whether
			// this an actual version increment
			updated = session
					.setVersionFromDistributedCache(distributedVersion);
			if (updated && trace_) {
				log_.trace("session in-memory data is invalidated for id: "
						+ realId + " new version: " + distributedVersion);
			}
		} else {
			int maxLife = metadata == null ? getMaxInactiveInterval()
					: metadata.getMaxInactiveInterval();

			Object existing = unloadedSipApplicationSessions_
					.put(key, new OwnedSessionUpdate(dataOwner, timestamp,
							maxLife, false));
			if (existing == null) {
				calcActiveSessions();
				if (trace_) {
					log_.trace("New session " + realId
							+ " added to unloaded session map");
				}
			} else if (trace_) {
				log_.trace("Updated timestamp for unloaded session " + realId);
			}
		}

		return updated;
	}
	
	public void sipApplicationSessionActivated() {
		int pc = sipApplicationSessionPassivatedCount_.decrementAndGet();
		// Correct for drift since we don't know the true passivation
		// count when we started. We can get activations of sessions
		// we didn't know were passivated.
		// FIXME -- is the above statement still correct? Is this needed?
		if (pc < 0) {
			// Just reverse our decrement.
			sipApplicationSessionPassivatedCount_.incrementAndGet();
		}
	}
	
	public void sipSessionActivated() {
		int pc = sipSessionPassivatedCount_.decrementAndGet();
		// Correct for drift since we don't know the true passivation
		// count when we started. We can get activations of sessions
		// we didn't know were passivated.
		// FIXME -- is the above statement still correct? Is this needed?
		if (pc < 0) {
			// Just reverse our decrement.
			sipSessionPassivatedCount_.incrementAndGet();
		}
	}
	
	/**
	 * Callback from the distributed cache to notify us that a session has been
	 * modified remotely.
	 * 
	 * @param realId
	 *            the session id, without any trailing jvmRoute
	 * @param dataOwner
	 *            the owner of the session. Can be <code>null</code> if the
	 *            owner is unknown.
	 * @param distributedVersion
	 *            the session's version per the distributed cache
	 * @param timestamp
	 *            the session's timestamp per the distributed cache
	 * @param metadata
	 *            the session's metadata per the distributed cache
	 */
	public boolean sipSessionChangedInDistributedCache(
			String realId, String dataOwner, int distributedVersion,
			long timestamp, DistributableSessionMetadata metadata) {
		boolean updated = true;
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipSessionKey(realId);
		} catch (ParseException e) {
			// should never happen
			logger
					.error(
							"An unexpected exception happened on parsing the following sip application session key "
									+ realId, e);
			return false;
		}
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = findLocalSipSession(
				key, false, null);
		if (session != null) {
			// Need to invalidate the loaded session. We get back whether
			// this an actual version increment
			updated = session
					.setVersionFromDistributedCache(distributedVersion);
			if (updated && trace_) {
				log_.trace("session in-memory data is invalidated for id: "
						+ realId + " new version: " + distributedVersion);
			}
		} else {
			int maxLife = metadata == null ? getMaxInactiveInterval()
					: metadata.getMaxInactiveInterval();

			Object existing = unloadedSipSessions_
					.put(key, new OwnedSessionUpdate(dataOwner, timestamp,
							maxLife, false));
			if (existing == null) {
				calcActiveSessions();
				if (trace_) {
					log_.trace("New session " + realId
							+ " added to unloaded session map");
				}
			} else if (trace_) {
				log_.trace("Updated timestamp for unloaded session " + realId);
			}
		}

		return updated;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession findSipApplicationSession(
			HttpSession httpSession) {
		return sipManagerDelegate.findSipApplicationSession(httpSession);
	}

	/**
	 * 
	 */
	public void dumpSipSessions() {
		sipManagerDelegate.dumpSipSessions();
	}

	/**
	 * 
	 */
	public void dumpSipApplicationSessions() {
		sipManagerDelegate.dumpSipApplicationSessions();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Iterator<MobicentsSipSession> getAllSipSessions() {
		return sipManagerDelegate.getAllSipSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<MobicentsSipApplicationSession> getAllSipApplicationSessions() {
		return sipManagerDelegate.getAllSipApplicationSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAllSessions() {
		sipManagerDelegate.removeAllSessions();
	}			

	@Override
	public void start() throws LifecycleException {
		super.start();
		initializeUnloadedSipApplicationSessions();
		initializeUnloadedSipSessions();
		initClusteredSipSessionNotificationPolicy();
		initClusteredSipApplicationSessionNotificationPolicy();
		initSnapshotSipManager();
	}
	
	@Override
	public void stop() throws LifecycleException {
		super.stop();
		
		removeAllSessions();
		
		sipSessionPassivatedCount_.set(0);
		sipApplicationSessionPassivatedCount_.set(0);
		
		unloadedSipApplicationSessions_.clear();
		unloadedSipSessions_.clear();
		snapshotSipManager_.stop();
	}
	
	/**
	 * Create and start a snapshot manager.
	 */
	private void initSnapshotSipManager() {
		String ctxPath = ((Context) container_).getPath();
		if (SnapshotMode.INSTANT == getSnapshotMode()) {
			snapshotSipManager_ = new InstantConvergedSnapshotManager(this, ctxPath);
		} else if (getSnapshotMode() == null) {
			log_.warn("Snapshot mode must be 'instant' or 'interval' - "
					+ "using 'instant'");
			setSnapshotMode(SnapshotMode.INSTANT);
			snapshotSipManager_ = new InstantConvergedSnapshotManager(this, ctxPath);
		} else if (ReplicationGranularity.FIELD == getReplicationGranularity()) {
			throw new IllegalStateException("Property snapshotMode must be "
					+ SnapshotMode.INTERVAL + " when FIELD granularity is used");
		} else if (getSnapshotInterval() < 1) {
			log_
					.warn("Snapshot mode set to 'interval' but snapshotInterval is < 1 "
							+ "using 'instant'");
			setSnapshotMode(SnapshotMode.INSTANT);
			snapshotSipManager_ = new InstantConvergedSnapshotManager(this, ctxPath);
		} else {
			snapshotSipManager_ = new IntervalConvergedSnapshotManager(this, ctxPath,
					getSnapshotInterval());
		}

		snapshotSipManager_.start();
	}
	
	// JMX Statistics
	/**
	 * Return descriptive information about this Manager implementation and the
	 * corresponding version number, in the format
	 * <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	public String getInfo() {
		return (info_);
	}

	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipSessions() {
		return (this.sipManagerDelegate.getMaxActiveSipSessions());
	}

	/**
	 * Set the maximum number of actives Sip Sessions allowed, or -1 for no
	 * limit.
	 * 
	 * @param max
	 *            The new maximum number of sip sessions
	 */
	public void setMaxActiveSipSessions(int max) {
		this.sipManagerDelegate.setMaxActiveSipSessions(max);		
	}

	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipApplicationSessions() {
		return (this.sipManagerDelegate.getMaxActiveSipApplicationSessions());
	}

	/**
	 * Set the maximum number of actives Sip Application Sessions allowed, or -1
	 * for no limit.
	 * 
	 * @param max
	 *            The new maximum number of sip application sessions
	 */
	public void setMaxActiveSipApplicationSessions(int max) {
		this.sipManagerDelegate.setMaxActiveSipApplicationSessions(max);
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipSessions() {
		return sipManagerDelegate.getRejectedSipSessions();
	}

	public void setRejectedSipSessions(int rejectedSipSessions) {
		this.sipManagerDelegate.setRejectedSipSessions(rejectedSipSessions);
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipApplicationSessions() {
		return sipManagerDelegate.getRejectedSipApplicationSessions();
	}

	public void setRejectedSipApplicationSessions(
			int rejectedSipApplicationSessions) {
		this.sipManagerDelegate.setRejectedSipApplicationSessions(rejectedSipApplicationSessions);
	}

	public void setSipSessionCounter(int sipSessionCounter) {
		this.sipManagerDelegate.setSipSessionCounter(sipSessionCounter);
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipSessionCounter() {
		return sipManagerDelegate.getSipSessionCounter();
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipSessions() {
		return sipManagerDelegate.getNumberOfSipSessions();
	}

	/**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
	public int getSipSessionMaxAliveTime() {
		return sipManagerDelegate.getSipSessionMaxAliveTime();
	}

	/**
	 * Sets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @param sessionMaxAliveTime
	 *            Longest time (in seconds) that an expired session had been
	 *            alive.
	 */
	public void setSipSessionMaxAliveTime(int sipSessionMaxAliveTime) {
		this.sipManagerDelegate.setSipSessionMaxAliveTime(sipSessionMaxAliveTime);
	}

	/**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
	public int getSipSessionAverageAliveTime() {
		return sipManagerDelegate.getSipSessionAverageAliveTime();
	}

	/**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
	public void setSipSessionAverageAliveTime(int sipSessionAverageAliveTime) {
		this.sipManagerDelegate.setSipSessionAverageAliveTime(sipSessionAverageAliveTime);
	}

	public void setSipApplicationSessionCounter(int sipApplicationSessionCounter) {
		this.sipManagerDelegate.setSipApplicationSessionCounter(sipApplicationSessionCounter);
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipApplicationSessionCounter() {
		return sipManagerDelegate.getSipApplicationSessionCounter();
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipApplicationSessions() {
		return sipManagerDelegate.getNumberOfSipApplicationSessions();
	}

	/**
	 * Gets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @return Longest time (in seconds) that an expired session had been alive.
	 */
	public int getSipApplicationSessionMaxAliveTime() {
		return sipManagerDelegate.getSipApplicationSessionMaxAliveTime();
	}

	/**
	 * Sets the longest time (in seconds) that an expired session had been
	 * alive.
	 * 
	 * @param sessionMaxAliveTime
	 *            Longest time (in seconds) that an expired session had been
	 *            alive.
	 */
	public void setSipApplicationSessionMaxAliveTime(
			int sipApplicationSessionMaxAliveTime) {
		this.sipManagerDelegate.setSipApplicationSessionMaxAliveTime(sipApplicationSessionMaxAliveTime);
	}

	/**
	 * Gets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @return Average time (in seconds) that expired sessions had been alive.
	 */
	public int getSipApplicationSessionAverageAliveTime() {
		return sipManagerDelegate.getSipApplicationSessionAverageAliveTime();
	}

	/**
	 * Sets the average time (in seconds) that expired sessions had been alive.
	 * 
	 * @param sessionAverageAliveTime
	 *            Average time (in seconds) that expired sessions had been
	 *            alive.
	 */
	public void setSipApplicationSessionAverageAliveTime(
			int sipApplicationSessionAverageAliveTime) {
		this.sipManagerDelegate.setSipApplicationSessionAverageAliveTime(sipApplicationSessionAverageAliveTime);
	}

	/**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
	public int getExpiredSipSessions() {
		return sipManagerDelegate.getExpiredSipSessions();
	}

	/**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
	public void setExpiredSipSessions(int expiredSipSessions) {
		this.sipManagerDelegate.setExpiredSipSessions(expiredSipSessions);
	}

	/**
	 * Gets the number of sessions that have expired.
	 * 
	 * @return Number of sessions that have expired
	 */
	public int getExpiredSipApplicationSessions() {
		return sipManagerDelegate.getExpiredSipApplicationSessions();
	}

	/**
	 * Sets the number of sessions that have expired.
	 * 
	 * @param expiredSessions
	 *            Number of sessions that have expired
	 */
	public void setExpiredSipApplicationSessions(
			int expiredSipApplicationSessions) {
		this.sipManagerDelegate.setExpiredSipApplicationSessions(expiredSipApplicationSessions);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getMaxPassivatedSipSessionCount() {
		return sipSessionMaxPassivatedCount_.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPassivatedSipSessionCount() {
		return sipSessionPassivatedCount_.get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getMaxPassivatedSipApplicationSessionCount() {
		return sipApplicationSessionMaxPassivatedCount_.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPassivatedSipApplicationSessionCount() {
		return sipApplicationSessionPassivatedCount_.get();
	}
	
	private class SipSessionPassivationCheck implements Comparable<SipSessionPassivationCheck>
	   {
	      private final SipSessionKey key;
	      private final OwnedSessionUpdate osu;
	      private final ClusteredSipSession<? extends OutgoingDistributableSessionData> session;
	      
	      private SipSessionPassivationCheck(SipSessionKey key, OwnedSessionUpdate osu)
	      {
	         assert osu != null : "osu is null";
	         assert key != null : "key is null";
	         
	         this.key = key;
	         this.osu = osu;
	         this.session = null;
	      }
	      
	      private SipSessionPassivationCheck(ClusteredSipSession<? extends OutgoingDistributableSessionData> session)
	      {
	         assert session != null : "session is null";
	         
	         this.key = session.getKey();
	         this.session = session;
	         this.osu = null;
	      }
	      
	      private long getLastUpdate()
	      {
	         return osu == null ? session.getLastAccessedTimeInternal() : osu.updateTime;
	      }
	      
	      private void passivate()
	      {
	         if (osu == null)
	         {
	            JBossCacheSipManager.this.processSipSessionPassivation(key);
	         }
	         else
	         {
	            JBossCacheSipManager.this.processUnloadedSipSessionPassivation(key, osu);
	         }
	      }
	      
	      private SipSessionKey getKey()
	      {
	         return key;
	      }
	      
	      private boolean isUnloaded()
	      {
	         return osu != null;
	      }

	      // This is what causes sorting based on lastAccessed
	      public int compareTo(SipSessionPassivationCheck o)
	      {
	         long thisVal = getLastUpdate();
	         long anotherVal = o.getLastUpdate();
	         return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
	      }
	   }
	
	private class SipApplicationSessionPassivationCheck implements Comparable<SipApplicationSessionPassivationCheck>
	   {
	      private final SipApplicationSessionKey key;
	      private final OwnedSessionUpdate osu;
	      private final ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session;
	      
	      private SipApplicationSessionPassivationCheck(SipApplicationSessionKey key, OwnedSessionUpdate osu)
	      {
	         assert osu != null : "osu is null";
	         assert key != null : "key is null";
	         
	         this.key = key;
	         this.osu = osu;
	         this.session = null;
	      }
	      
	      private SipApplicationSessionPassivationCheck(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session)
	      {
	         assert session != null : "session is null";
	         
	         this.key = session.getKey();
	         this.session = session;
	         this.osu = null;
	      }
	      
	      private long getLastUpdate()
	      {
	         return osu == null ? session.getLastAccessedTimeInternal() : osu.updateTime;
	      }
	      
	      private void passivate()
	      {
	         if (osu == null)
	         {
	            JBossCacheSipManager.this.processSipApplicationSessionPassivation(key);
	         }
	         else
	         {
	            JBossCacheSipManager.this.processUnloadedSipApplicationSessionPassivation(key, osu);
	         }
	      }
	      
	      private SipApplicationSessionKey getKey()
	      {
	         return key;
	      }
	      
	      private boolean isUnloaded()
	      {
	         return osu != null;
	      }

	      // This is what causes sorting based on lastAccessed
	      public int compareTo(SipApplicationSessionPassivationCheck o)
	      {
	         long thisVal = getLastUpdate();
	         long anotherVal = o.getLastUpdate();
	         return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
	      }
	   }
	
	private class OwnedSessionUpdate
	   {
	      String owner;
	      long updateTime;
	      int maxInactive;
	      boolean passivated;
	      
	      OwnedSessionUpdate(String owner, long updateTime, int maxInactive, boolean passivated)
	      {
	         this.owner = owner;
	         this.updateTime = updateTime;
	         this.maxInactive = maxInactive;
	         this.passivated = passivated;
	      }
	   }

	public ClusteredSipApplicationSessionNotificationPolicy getSipApplicationSessionNotificationPolicy() {
		return sipApplicationSessionNotificationPolicy_;
	}

	public ClusteredSipSessionNotificationPolicy getSipSessionNotificationPolicy() {
		return sipSessionNotificationPolicy_;
	}

	/**
	 * @param snapshotSipManager_ the snapshotSipManager_ to set
	 */
	public void setSnapshotSipManager(SnapshotSipManager snapshotSipManager_) {
		this.snapshotSipManager_ = snapshotSipManager_;
	}

	/**
	 * @return the snapshotSipManager_
	 */
	public SnapshotSipManager getSnapshotSipManager() {
		return snapshotSipManager_;
	}
}
