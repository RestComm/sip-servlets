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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ContainerBase;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.SnapshotMode;
import org.jboss.util.loading.ContextClassLoaderSwitcher;
import org.jboss.web.tomcat.service.session.distributedcache.spi.BatchingManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactory;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManagerFactoryFactory;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableConvergedSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingAttributeGranularitySessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingSessionGranularitySessionData;
import org.jboss.web.tomcat.service.session.notification.ClusteredSessionNotificationCause;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipApplicationSessionNotificationCapability;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipSessionNotificationCapability;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipSessionNotificationPolicy;
import org.jboss.web.tomcat.service.session.notification.IgnoreUndeployLegacyClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.web.tomcat.service.session.notification.IgnoreUndeployLegacyClusteredSipSessionNotificationPolicy;
import org.mobicents.cache.MobicentsCache;
import org.mobicents.cluster.DefaultMobicentsCluster;
import org.mobicents.cluster.MobicentsCluster;
import org.mobicents.cluster.election.DefaultClusterElector;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * Implementation of a converged clustered session manager for
 * catalina using JBossCache replication.
 * 
 * Based on JbossCacheManager JBOSS AS 5.1.0.GA Tag 
 * 
 * TODO Some parts have been copied over but the request for change to protected was made http://www.jboss.org/index.html?module=bb&op=viewtopic&p=4231452#4231452 
 * review this for the GA version
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class JBossCacheSipManager<O extends OutgoingDistributableSessionData> extends JBossCacheManager implements
		ClusteredSipManager<O>, LocalDistributableConvergedSessionManager, JBossCacheSipManagerMBean {

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
	protected Map<ClusteredSipSessionKey, OwnedSessionUpdate> unloadedSipSessions_ = new ConcurrentHashMap<ClusteredSipSessionKey, OwnedSessionUpdate>();
	
	/**
	 * Id/timestamp of sessions in cache that we haven't loaded
	 */
	protected Map<String, OwnedSessionUpdate> unloadedSipApplicationSessions_ = new ConcurrentHashMap<String, OwnedSessionUpdate>();

	/**
	 * Id/timestamp of sessions in distributedcache that we haven't loaded
	 * locally
	 */
	private Map<String, OwnedSessionUpdate> unloadedSessions_ = new ConcurrentHashMap<String, OwnedSessionUpdate>();
	
	/** 
	 * Sessions that have been created but not yet loaded. Used to ensure
	 * concurrent threads trying to load the same session
	 */
	private final ConcurrentMap<String, ClusteredSession<? extends OutgoingDistributableSessionData>> embryonicSessions = 
		new ConcurrentHashMap<String, ClusteredSession<? extends OutgoingDistributableSessionData>>();
	
	/** number of sessions rejected because the number active sip sessions exceeds maxActive */
	protected AtomicInteger rejectedSipSessionCounter_ = new AtomicInteger();
	/** number of sessions rejected because the number active sip application sessions exceeds maxActive */
	protected AtomicInteger rejectedSipApplicationSessionCounter_ = new AtomicInteger();
	/** Number of passivated sip sessions */
	private AtomicInteger passivatedSipSessionCount_ = new AtomicInteger();
	/** Maximum number of concurrently passivated sip sessions */
	private AtomicInteger maxPassivatedSipSessionCount_ = new AtomicInteger();
	/** Number of passivated sip applicationsessions */
	private AtomicInteger passivatedSipApplicationSessionCount_ = new AtomicInteger();
	/** Maximum number of concurrently passivated sip application sessions */
	private AtomicInteger maxPassivatedSipApplicationSessionCount_ = new AtomicInteger();
	/** Number of active sip sessions */
	protected AtomicInteger localActiveSipSessionCounter_ = new AtomicInteger();
	/** Number of active sip application sessions */
	protected AtomicInteger localActiveSipApplicationSessionCounter_ = new AtomicInteger();
	/** Maximum number of concurrently locally active sip sessions */
	protected AtomicInteger maxLocalActiveSipSessionCounter_ = new AtomicInteger();
	/** Maximum number of concurrently locally active sip application sessions */
	protected AtomicInteger maxLocalActiveSipApplicationSessionCounter_ = new AtomicInteger();
	/** Maximum number of active sip sessions seen so far */
	protected AtomicInteger maxActiveSipSessionCounter_ = new AtomicInteger();
	/** Maximum number of active sip application sessions seen so far */
	protected AtomicInteger maxActiveSipApplicationSessionCounter_ = new AtomicInteger();
	/** Number of sip sessions that have been active locally that are now expired. */
	protected AtomicInteger expiredSipSessionCounter_ = new AtomicInteger();
	/** Number of sip application sessions that have been active locally that are now expired. */
	protected AtomicInteger expiredSipApplicationSessionCounter_ = new AtomicInteger();
		
	private String sipSessionNotificationPolicyClass_;
	private String sipApplicationSessionNotificationPolicyClass_;
	
	private ClusteredSipApplicationSessionNotificationPolicy sipApplicationSessionNotificationPolicy_;
	private ClusteredSipSessionNotificationPolicy sipSessionNotificationPolicy_;				
	
	/** Number of passivated sessions */
	private AtomicInteger passivatedCount_ = new AtomicInteger();
	/** Maximum number of concurrently passivated sessions */
	private AtomicInteger maxPassivatedCount_ = new AtomicInteger();	
	
	private static final int TOTAL_PERMITS = Integer.MAX_VALUE;
	private Semaphore semaphore = new Semaphore(TOTAL_PERMITS, true);
	private Lock valveLock = new SemaphoreLock(this.semaphore);

	private OutdatedSessionChecker outdatedSessionChecker;
	private OutdatedSipSessionChecker outdatedSipSessionChecker;
	private OutdatedSipApplicationSessionChecker outdatedSipApplicationSessionChecker;
	   
	private volatile boolean stopping;
	
	/** Are we running embedded in JBoss? */
	private boolean embedded_ = false;
	private MobicentsCluster mobicentsCluster;
	private MobicentsCache mobicentsCache;
	private String applicationName;
	
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
	   sipManagerDelegate = new ClusteredSipManagerDelegate(ReplicationGranularity.valueOf(getReplicationGranularityString()),getUseJK(), (ClusteredSipManager)this);
	   this.sipApplicationSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
	   this.sipSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
	   embedded_ = true;
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
                     IgnoreUndeployLegacyClusteredSipSessionNotificationPolicy.class.getName());
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
                     IgnoreUndeployLegacyClusteredSipApplicationSessionNotificationPolicy.class.getName());
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
   private static ClusteredSession<? extends OutgoingDistributableSessionData> uncheckedCastSession(Session session)
   {
      return (ClusteredSession) session;
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
   private static <T extends OutgoingDistributableSessionData> JBossCacheSipManager<T> uncheckedCastSipManager(JBossCacheSipManager mgr)
   {
      return mgr;
   }
   
   @SuppressWarnings("unchecked")
   public DistributedCacheConvergedSipManager<? extends OutgoingDistributableSessionData> getDistributedCacheConvergedSipManager()
   {
      return (DistributedCacheConvergedSipManager) getDistributedCacheManager();
   }
   
   protected OutdatedSessionChecker initOutdatedSessionChecker()
   {
      return new AskSessionOutdatedSessionChecker();      
   }
   
   protected OutdatedSipSessionChecker initOutdatedSipSessionChecker()
   {
      return new AskSipSessionOutdatedSessionChecker();      
   }
   
   protected OutdatedSipApplicationSessionChecker initOutdatedSipApplicationSessionChecker()
   {
      return new AskSipApplicationSessionOutdatedSessionChecker();      
   }
   
   // Satisfy the Manager interface.  Internally we use
   // createEmptyClusteredSession to avoid a cast
   public Session createEmptySession()
   {
      Session session = null;
      try
      {
         // [JBAS-7123] Make sure we're either in the call stack where LockingValve has
         // a lock, or that we acquire one ourselves
         boolean inLockingValve = SessionReplicationContext.isLocallyActive();
         if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS))
         {
            try
            {
               if (trace_)
               {
                  log_.trace("Creating an empty ClusteredSession");
               }
               session = createEmptyConvergedClusteredSession();
            }
            finally
            {
               if (!inLockingValve)
               {
                  this.valveLock.unlock();
               }
            }
         }
         else if (trace_)
         {
            log_.trace("createEmptySession(): Manager is not handling requests; returning null");
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
      
      return session;
   }
   
   private ClusteredSession<? extends OutgoingDistributableSessionData> createEmptyConvergedClusteredSession()
   {     

      ClusteredSession<? extends OutgoingDistributableSessionData> session = null;
      switch (getReplicationGranularity())
      {
         case ATTRIBUTE:
            ClusteredSipManager<OutgoingAttributeGranularitySessionData> amgr = uncheckedCastSipManager(this);
            session = new ConvergedAttributeBasedClusteredSession(amgr);
            break;
         case FIELD:
        	ClusteredSipManager<OutgoingDistributableSessionData> fmgr = uncheckedCastSipManager(this);
            session = new ConvergedFieldBasedClusteredSession(fmgr);
            break;
         default:
        	 ClusteredSipManager<OutgoingSessionGranularitySessionData> smgr = uncheckedCastSipManager(this);
            session = new ConvergedSessionBasedClusteredSession(smgr);
            break;
      }
      return session;
   }
   
   /**
    * {@inheritDoc}
    */
   public long getPassivatedSessionCount()
   {
      return passivatedCount_.get();
   }
   
   /**
    * {@inheritDoc}
    */
   public long getMaxPassivatedSessionCount()
   {
      return maxPassivatedCount_.get();
   }

   /**
    * {@inheritDoc}
    */
   public Session createSession(String sessionId)
   {  
      Session session = null;
      try
      {
         // [JBAS-7123] Make sure we're either in the call stack where LockingValve has
         // a lock, or that we acquire one ourselves
         boolean inLockingValve = SessionReplicationContext.isLocallyActive();
         if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS))
         {
            try
            {
               session = createSessionInternal(sessionId);
            }
            finally
            {
               if (!inLockingValve)
               {
                  this.valveLock.unlock();
               }
            }
         }
         else if (trace_)
         {
            log_.trace("createEmptySession(): Manager is not handling requests; returning null");
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
      
      return session;
   }
   
   /**
    * {@inheritDoc}
    */
   private Session createSessionInternal(String sessionId)
   {      
      // First check if we've reached the max allowed sessions, 
      // then try to expire/passivate sessions to free memory
      // maxActiveAllowed_ -1 is unlimited
      // We check here for maxActive instead of in add().  add() gets called
      // when we load an already existing session from the distributed cache
      // (e.g. in a failover) and we don't want to fail in that situation.

      if(maxActiveAllowed_ != -1 && calcActiveSessions() >= maxActiveAllowed_)
      {
         if (trace_)
         {
            log_.trace("createSession(): active sessions = " + calcActiveSessions() +
                       " and max allowed sessions = " + maxActiveAllowed_);
         }
         
         processHttpSessionExpirationPassivation();
         
         if (calcActiveSessions() >= maxActiveAllowed_)
         {
            // Exceeds limit. We need to reject it.
            rejectedCounter_.incrementAndGet();
            // Catalina api does not specify what happens
            // but we will throw a runtime exception for now.
            String msgEnd = (sessionId == null) ? "" : " id " + sessionId;
            throw new IllegalStateException("createSession(): number of " +
                   "active sessions exceeds the maximum limit: " +
                   maxActiveAllowed_ + " when trying to create session" + msgEnd);
         }
      }

      ClusteredSession<? extends OutgoingDistributableSessionData> session = createEmptyConvergedClusteredSession();

      if (session != null)
      {
	      session.setNew(true);
	      session.setCreationTime(System.currentTimeMillis());
	      session.setMaxInactiveInterval(this.maxInactiveInterval_);
	      session.setValid(true);
	
	      String clearInvalidated = null; // see below
	      
	      if (sessionId == null)
	      {
	          sessionId = this.getNextId();
	
	          // We are using mod_jk for load balancing. Append the JvmRoute.
	          if (getUseJK())
	          {
	              if (trace_)
	              {
	                  log_.trace("createSession(): useJK is true. Will append JvmRoute: " + this.getJvmRoute());
	              }
	              sessionId += "." + this.getJvmRoute();
	          }
	      }
	      else
	      {
	         clearInvalidated = sessionId;
	      }
	
	      session.setId(sessionId); // Setting the id leads to a call to add()
	      
	      getDistributedCacheManager().sessionCreated(session.getRealId());
	      
	      session.tellNew(ClusteredSessionNotificationCause.CREATE);
	
	      if (trace_)
	      {
	         log_.trace("Created a ClusteredSession with id: " + sessionId);
	      }
	
	      createdCounter_.incrementAndGet(); // the call to add() handles the other counters 
	      
	      // Add this session to the set of those potentially needing replication
	      ConvergedSessionReplicationContext.bindSession(session, getSnapshotManager());
	      
	      if (clearInvalidated != null)
	      {
	         // We no longer need to track any earlier session w/ same id 
	         // invalidated by this thread
	         SessionInvalidationTracker.clearInvalidatedSession(clearInvalidated, this);
	      }
      }
      
      return session;
   }
   
   /**
    * Loads a session from the distributed store.  If an existing session with
    * the id is already under local management, that session's internal state
    * will be updated from the distributed store.  Otherwise a new session
    * will be created and added to the collection of those sessions under
    * local management.
    *
    * @param realId  id of the session-id with any jvmRoute removed
    *
    * @return the session or <code>null</code> if the session cannot be found
    *         in the distributed store
    */
   private ClusteredSession<? extends OutgoingDistributableSessionData> loadSession(String realId)
   {
      if (realId == null)
      {
         return null;
      }
      ClusteredSession<? extends OutgoingDistributableSessionData> session = null;
      
      try
      {
         // [JBAS-7123] Make sure we're either in the call stack where LockingValve has
         // a lock, or that we acquire one ourselves
         boolean inLockingValve = SessionReplicationContext.isLocallyActive();
         if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS))
         {
            try
            {
               long begin = System.currentTimeMillis();
               boolean mustAdd = false;
               boolean passivated = false;
               
               session = sessions_.get(realId);
               boolean initialLoad = false;
               if (session == null)
               {                 
                  initialLoad = true;
                  // This is either the first time we've seen this session on this
                  // server, or we previously expired it and have since gotten
                  // a replication message from another server
                  mustAdd = true;
                  session = createEmptyConvergedClusteredSession();

                  // JBAS-7379 Ensure concurrent threads trying to load same session id
                  // use the same session
                  ClusteredSession<? extends OutgoingDistributableSessionData> embryo = 
                     this.embryonicSessions.putIfAbsent(realId, session);
                  if (embryo != null)
                  {
                     session = embryo;
                  }
                  
                  OwnedSessionUpdate osu = unloadedSessions_.get(realId);
                  passivated = (osu != null && osu.isPassivated());
               }
  
               synchronized (session)
               {
                  // JBAS-7379 check if we lost the race to the sync block
                  // and another thread has already loaded this session
                  if (initialLoad && session.isOutdated() == false)
                  {
                	  if (log_.isDebugEnabled())
                      {
                         log_.debug("loadSession(): id= " + realId + ", session=" + session + "session.isOutdated() " + session.isOutdated());
                      }
                     // some one else loaded this
                     return session;
                  }

                  ContextClassLoaderSwitcher.SwitchContext switcher = null; 
                  boolean doTx = false; 
                  boolean loadCompleted = false;
                  try
                  {
                     // We need transaction so any data gravitation replication 
                     // is sent in batch.
                     // Don't do anything if there is already transaction context
                     // associated with this thread.
                     if (getDistributedCacheConvergedSipManager().getBatchingManager().isBatchInProgress() == false)
                     {
                    	 getDistributedCacheConvergedSipManager().getBatchingManager().startBatch();
                        doTx = true;
                     }
                     
                     // Tomcat calls Manager.findSession before setting the tccl,
                     // so we need to do it :(
                     switcher = getContextClassLoaderSwitcher().getSwitchContext();
                     switcher.setClassLoader(getApplicationClassLoader());
                                 
                     IncomingDistributableSessionData data = getDistributedCacheManager().getSessionData(realId, initialLoad);
                     if (data != null)
                     {
                        session.update(data);
                     }
                     else
                     {
                    	 if (log_.isDebugEnabled())
                         {
                            log_.debug("loadSession(): id= " + realId + ", session=" + session + " : no data setting to null ");
                         }
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
                     
                     loadCompleted = true;
                  }
                  catch (Exception ex)
                  {
                     try
                     {
  //                  if(doTx)
                        // Let's set it no matter what.
                    	 getDistributedCacheConvergedSipManager().getBatchingManager().setBatchRollbackOnly();
                     }
                     catch (Exception exn)
                     {
                        log_.error("Caught exception rolling back transaction", exn);
                     }
                     // We will need to alert Tomcat of this exception.
                     if (ex instanceof RuntimeException)
                        throw (RuntimeException) ex;
                     
                     throw new RuntimeException("loadSession(): failed to load session " +
                                                realId, ex);
                  }
                  finally
                  {
                     try {
                        if(doTx)
                        {
                           try
                           {
                        	   getDistributedCacheConvergedSipManager().getBatchingManager().endBatch();
                           }
                           catch (Exception e)
                           {
                              if (loadCompleted)
                              {
                                 // We read the data successfully but then failed in commit?
                                 // That indicates a JBC data gravitation where the replication of
                                 // the gravitated data to our buddy failed. We can ignore that
                                 // and count on this request updating the cache.                               // 
                                 log_.warn("Problem ending batch after loading session " + realId + " -- " + e.getLocalizedMessage() + " However session data was successful loaded.");
                                 log_.debug("Failure cause", e);
                              }
                              else
                              {
                                 if (e instanceof RuntimeException)
                                    throw (RuntimeException) e;
                                 
                                 throw new RuntimeException("loadSession(): failed to load session " +
                                                            realId, e);
                              }
                           }
                        }
                     }
                     finally {
                        if (switcher != null)
                        {
                           switcher.reset();
                        }
                     }
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
                     stats_.updateLoadStats(realId, elapsed);
  
                     if (log_.isDebugEnabled())
                     {
                        log_.debug("loadSession(): id= " + realId + ", session=" + session);
                     }
                  }
                  else if (log_.isDebugEnabled())
                  {
                     log_.debug("loadSession(): session " + realId +
                                " not found in distributed cache");
                  }
                  
                  if (initialLoad)
                  {                     
                     // The session is now in the regular map, or the session
                     // doesn't exist in the distributed cache. either way
                     // it's now safe to stop tracking this embryonic session
                     embryonicSessions.remove(realId);
                  }
               }
            }
            finally
            {
               if (!inLockingValve)
               {
                  this.valveLock.unlock();
               }
            }
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
      
      return session;
   }

   /**
    * {@inheritDoc}
    */
   public void add(Session session)
   {
      if (session == null)
         return;

      if (!(session instanceof ClusteredSession))
      {
         throw new IllegalArgumentException("You can only add instances of " +
               "type ClusteredSession to this Manager. Session class name: " +
               session.getClass().getName());
      }

      try
      {
         // [JBAS-7123] Make sure we're either in the call stack where LockingValve has
         // a lock, or that we acquire one ourselves
         boolean inLockingValve = SessionReplicationContext.isLocallyActive();
         if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS))
         {
            try
            {
               add(uncheckedCastSession(session), false); // wait to replicate until req end
            }
            finally
            {
               if (!inLockingValve)
               {
                  this.valveLock.unlock();
               }
            }
         }
         else if (trace_)
         {
            log_.trace("add(): ignoring add -- Manager is not actively handling requests");
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
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
   private void add(ClusteredSession<? extends OutgoingDistributableSessionData> session, boolean replicate)
   {
      // TODO -- why are we doing this check? The request checks session 
      // validity and will expire the session; this seems redundant
//      if (!session.isValid())
//      {
//         // Not an error; this can happen if a failover request pulls in an
//         // outdated session from the distributed cache (see TODO above)
//         log_.debug("Cannot add session with id=" + session.getIdInternal() +
//                    " because it is invalid");
//         return;
//      }

      String realId = session.getRealId();
      Object existing = sessions_.put(realId, session);
      unloadedSessions_.remove(realId);

      if (!session.equals(existing))
      {
         if (replicate)
         {
            storeSession(session);
         }

         // Update counters
         calcActiveSessions();
         
         if (trace_)
         {
            log_.trace("Session with id=" + session.getIdInternal() + " added. " +
                       "Current active sessions " + localActiveCounter_.get());
         }
      }
   }
   
   /**
    * Return the sessions. Note that this will return not only the local
    * in-memory sessions, but also any sessions that are in the distributed
    * cache but have not previously been accessed on this server.  Invoking
    * this method will bring all such sessions into local memory and can
    * potentially be quite expensive.
    *
    * <p>
    * Note also that when sessions are loaded from the distributed cache, no
    * check is made as to whether the number of local sessions will thereafter
    * exceed the maximum number allowed on this server.
    * </p>
    *
    * @return an array of all the sessions
    */
   public Session[] findSessions()
   {
      Session[] sessions = null;
      try
      {
         // [JBAS-7123] Make sure we're either in the call stack where LockingValve has
         // a lock, or that we acquire one ourselves
         boolean inLockingValve = SessionReplicationContext.isLocallyActive();
         if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS))
         {
            try
            {
               // Need to load all the unloaded sessions
               if(unloadedSessions_.size() > 0)
               {
                  // Make a thread-safe copy of the new id list to work with
                  Set<String> ids = new HashSet<String>(unloadedSessions_.keySet());
  
                  if(trace_) {
                     log_.trace("findSessions: loading sessions from distributed cache: " + ids);
                  }
  
                  for(String id :  ids) {
                     loadSession(id);
                  }
               }
  
               // All sessions are now "local" so just return the local sessions
               sessions = findLocalSessions();
            }
            finally
            {
               if (!inLockingValve)
               {
                  this.valveLock.unlock();
               }
            }
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
      
      return sessions;
   }
   
   /**
    * Attempts to find the session in the collection of those being managed
    * locally, and if not found there, in the distributed cache of sessions.
    * <p>
    * If a session is found in the distributed cache, it is added to the
    * collection of those being managed locally.
    * </p>
    *
    * @param id the session id, which may include an appended jvmRoute
    *
    * @return the session, or <code>null</code> if no such session could
    *         be found
    */
   public Session findSession(String id)
   {
      String realId = getRealId(id);
      // Find it from the local store first
      ClusteredSession<? extends OutgoingDistributableSessionData> session = findLocalSession(realId);
      
      // If we didn't find it locally, only check the distributed cache
      // if we haven't previously handled this session id on this request.
      // If we handled it previously but it's no longer local, that means
      // it's been invalidated. If we request an invalidated session from
      // the distributed cache, it will be missing from the local cache but
      // may still exist on other nodes (i.e. if the invalidation hasn't 
      // replicated yet because we are running in a tx). With buddy replication,
      // asking the local cache for the session will cause the out-of-date
      // session from the other nodes to be gravitated, thus resuscitating
      // the session.
      if (session == null 
            && !SessionInvalidationTracker.isSessionInvalidated(realId, this))
      {
         if (log_.isDebugEnabled())
            log_.debug("Checking for session " + realId + " in the distributed cache");
         
         session = loadSession(realId);
//       if (session != null)
//       {
//          add(session);
//          // We now notify, since we've added a policy to allow listeners 
//          // to discriminate. But the default policy will not allow the 
//          // notification to be emitted for FAILOVER, so the standard
//          // behavior is unchanged.
//          session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
//       }
      }
      else if (session != null && this.outdatedSessionChecker.isSessionOutdated(session))
      {
         if (log_.isDebugEnabled())
            log_.debug("Updating session " + realId + " from the distributed cache");
         
         // Need to update it from the cache
         session = loadSession(realId);
         if (session == null)
         {
            // We have a session locally but it's no longer available
            // from the distributed store; i.e. it's been invalidated elsewhere
            // So we need to clean up
            // TODO what about notifications?
            this.sessions_.remove(realId);
         }
      }

      if (session != null)
      {
         // Add this session to the set of those potentially needing replication
         ConvergedSessionReplicationContext.bindSession(session, getSnapshotManager());
         
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
    * Gets the session id with any jvmRoute removed.
    * 
    * @param id a session id with or without an appended jvmRoute.
    *           Cannot be <code>null</code>.
    */
   private String getRealId(String id)
   {
      return (getUseJK() ? Util.getRealId(id) : id);
   }
   
   /**
    * {@inheritDoc}
    * <p>
    * Overrides the superclass version to ensure that the generated id
    * does not duplicate the id of any other session this manager is aware of.
    * </p>
    */
   @Override
   protected String getNextId()
   {
      while (true)
      {
         String id = super.getNextId();
         if (sessions_.containsKey(id) || unloadedSessions_.containsKey(id))
         {
            duplicates_.incrementAndGet();
         }
         else
         {
            return id;
         }
      }
   }

   protected int getTotalActiveSessions()
   {
      return localActiveCounter_.get() + unloadedSessions_.size() - passivatedCount_.get();
   }
   
   protected int getTotalActiveSipApplicationSessions()
   {
//      return localActiveSipApplicationSessionCounter_.get() + unloadedSipApplicationSessions_.size() - passivatedSipApplicationSessionCount_.get();
	   return localActiveSipApplicationSessionCounter_.get() + unloadedSipApplicationSessions_.size();
   }
   
   protected int getTotalActiveSipSessions()
   {
//      return localActiveSipSessionCounter_.get() + unloadedSipSessions_.size() - passivatedSipSessionCount_.get();
      return localActiveSipSessionCounter_.get() + unloadedSipSessions_.size();
   }
   
   @Override
   public void resetStats()
   {
      super.resetStats();
      
      this.maxPassivatedCount_.set(this.passivatedCount_.get());
      this.maxPassivatedSipSessionCount_.set(this.passivatedSipSessionCount_.get());
      this.maxPassivatedSipApplicationSessionCount_.set(this.passivatedSipApplicationSessionCount_.get());
   }
   
   protected Map<String, OwnedSessionUpdate> getUnloadedSessions()
   {
      Map<String, OwnedSessionUpdate> unloaded = new HashMap<String, OwnedSessionUpdate>(unloadedSessions_);
      return unloaded;
   }
   
   protected Map<String, OwnedSessionUpdate> getUnloadedSipApplicationSessions()
   {
      Map<String, OwnedSessionUpdate> unloaded = new HashMap<String, OwnedSessionUpdate>(unloadedSipApplicationSessions_);
      return unloaded;
   }
   
   protected Map<ClusteredSipSessionKey, OwnedSessionUpdate> getUnloadedSipSessions()
   {
      Map<ClusteredSipSessionKey, OwnedSessionUpdate> unloaded = new HashMap<ClusteredSipSessionKey, OwnedSessionUpdate>(unloadedSipSessions_);
      return unloaded;
   }
   
   /**
    * Gets the ids of all sessions in the distributed cache and adds
    * them to the unloaded sessions map, along with their lastAccessedTime
    * and their maxInactiveInterval. Passivates overage or excess sessions.
    */
   protected void initializeUnloadedSessions()
   {      
      Map<String, String> sessions = getDistributedCacheManager().getSessionIds();
      if (sessions != null)
      {
         boolean passivate = isPassivationEnabled();

         long passivationMax = passivationMaxIdleTime_ * 1000L;
         long passivationMin = passivationMinIdleTime_ * 1000L;
         
         for (Map.Entry<String, String> entry : sessions.entrySet())
         {
            String realId = entry.getKey();
            String owner = entry.getValue();

            long ts = -1;
            DistributableSessionMetadata md = null;
            try
            {
               IncomingDistributableSessionData sessionData = getDistributedCacheManager().getSessionData(realId, owner, false);
               ts = sessionData.getTimestamp();
               md = sessionData.getMetadata();
            }
            catch (Exception e)
            {
               // most likely a lock conflict if the session is being updated remotely; 
               // ignore it and use default values for timstamp and maxInactive
               log_.debug("Problem reading metadata for session " + realId + " -- " + e.toString());               
            }
            
            long lastMod = ts == -1 ? System.currentTimeMillis() : ts;
            int maxLife = md == null ? getMaxInactiveInterval() : md.getMaxInactiveInterval();
            
            OwnedSessionUpdate osu = new OwnedSessionUpdate(owner, lastMod, maxLife, false);
            unloadedSessions_.put(realId, osu);
            if (passivate)
            {
               try
               {
                  long elapsed = System.currentTimeMillis() - lastMod;
                  // if maxIdle time configured, means that we need to passivate sessions that have
                  // exceeded the max allowed idle time
                  if (passivationMax >= 0 
                        && elapsed > passivationMax)
                  {
                     if (trace_)
                     {
                        log_.trace("Elapsed time of " + elapsed + " for session "+ 
                              realId + " exceeds max of " + passivationMax + "; passivating");
                     }
                     processUnloadedSessionPassivation(realId, osu);
                  }
                  // If the session didn't exceed the passivationMaxIdleTime_, see   
                  // if the number of sessions managed by this manager greater than the max allowed 
                  // active sessions, passivate the session if it exceed passivationMinIdleTime_ 
                  else if (maxActiveAllowed_ > 0 
                              && passivationMin >= 0 
                              && calcActiveSessions() > maxActiveAllowed_ 
                              && elapsed >= passivationMin)
                  {
                     if (trace_)
                     {
                        log_.trace("Elapsed time of " + elapsed + " for session "+ 
                              realId + " exceeds min of " + passivationMin + "; passivating");
                     }
                     processUnloadedSessionPassivation(realId, osu);
                  }
               }
               catch (Exception e)
               {
                  // most likely a lock conflict if the session is being updated remotely; ignore it
                  log_.debug("Problem passivating session " + realId + " -- " + e.toString());
               }
            }
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public String listSessionIds()
   {
      Set<String> ids = new HashSet<String>(sessions_.keySet());
      ids.addAll(unloadedSessions_.keySet());
      return reportSessionIds(ids);
   }
   
   private String reportSessionIds(Set<String> ids)
   {
      StringBuffer sb = new StringBuffer();
      boolean added = false;
      for (String id : ids)
      {
         if (added)
         {
            sb.append(',');
         }
         else
         {
            added = true;
         }
         
         sb.append(id);
      }
      return sb.toString();
   }   
   
   /**
    * Notifies the manager that a session in the distributed cache has
    * been invalidated
    * 
    * FIXME This method is poorly named, as we will also get this callback
    * when we use buddy replication and our copy of the session in JBC is being
    * removed due to data gravitation.
    * 
    * @param realId the session id excluding any jvmRoute
    */
   public void notifyRemoteInvalidation(String realId)
   {
      // Remove the session from our local map
      ClusteredSession<? extends OutgoingDistributableSessionData> session = sessions_.remove(realId);
      if (session == null)
      {
         // We weren't managing the session anyway.  But remove it
         // from the list of cached sessions we haven't loaded
         if (unloadedSessions_.remove(realId) != null)
         {
            if (trace_)
               log_.trace("Removed entry for session " + realId + " from unloaded session map");
         }
         
         // If session has failed over and has been passivated here,
         // session will be null, but we'll have a TimeStatistic to clean up
         stats_.removeStats(realId);
      }
      else
      {
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
         ContextClassLoaderSwitcher.SwitchContext switcher = null;         
         try
         {
            // Don't track this invalidation is if it were from a request
            SessionInvalidationTracker.suspend();
            
            switcher = getContextClassLoaderSwitcher().getSwitchContext();
            switcher.setClassLoader(getApplicationClassLoader());
            session.expire(notify, localCall, localOnly, ClusteredSessionNotificationCause.INVALIDATE);
         }
         finally
         {
            SessionInvalidationTracker.resume();
            
            // Remove any stats for this session
            stats_.removeStats(realId);
            
            if (switcher != null)
            {
               switcher.reset();
            }
         }
      }
   }
   
   @Override
   protected void processExpirationPassivation() {
	   processHttpSessionExpirationPassivation();
	   processSipSessionExpirationPassivation();
	   processSipApplicationSessionExpirationPassivation();
   }
   
   /**
    * {@inheritDoc}
    */
   protected void processHttpSessionExpirationPassivation()
   {      
      boolean expire = maxInactiveInterval_ >= 0;
      boolean passivate = isPassivationEnabled();
      
      long passivationMax = passivationMaxIdleTime_ * 1000L;
      long passivationMin = passivationMinIdleTime_ * 1000L;

      if (trace_)
      { 
         log_.trace("processExpirationPassivation(): Looking for sessions that have expired ...");
         log_.trace("processExpirationPassivation(): active sessions = " + calcActiveSessions());
         log_.trace("processExpirationPassivation(): expired sessions = " + expiredCounter_);
         if (passivate)
         {
            log_.trace("processExpirationPassivation(): passivated count = " + getPassivatedSessionCount());
         }
      }
      
      // Holder for sessions or OwnedSessionUpdates that survive expiration,
      // sorted by last acccessed time
      TreeSet<PassivationCheck> passivationChecks = new TreeSet<PassivationCheck>();
      
      try
      {
         // Don't track sessions invalidated via this method as if they
         // were going to be re-requested by the thread
         SessionInvalidationTracker.suspend();
         
         // First, handle the sessions we are actively managing
         ClusteredSession<? extends OutgoingDistributableSessionData> sessions[] = findLocalSessions();
         for (int i = 0; i < sessions.length; ++i)
         {
            if (!backgroundProcessAllowed.get())
            {
               return;
            }
            
            try
            {
               ClusteredSession<? extends OutgoingDistributableSessionData> session = sessions[i];
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
                  if (this.outdatedSessionChecker.isSessionOutdated(session) && !(session.isValid(false)))
                  {
                     // FIXME in AS 5 every time we get a notification from the distributed
                     // cache of an update, we get the latest timestamp. So
                     // we shouldn't need to do a full session load here. A load
                     // adds a risk of an unintended data gravitation.
                     
                     // JBAS-2792 don't assign the result of loadSession to session
                     // just update the object from the cache or fall through if
                     // the session has been removed from the cache
                     loadSession(session.getRealId());
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
                  passivationChecks.add(new PassivationCheck(session));
               }
               
            }
            catch (Exception ex)
            {
               log_.error("processExpirationPassivation(): failed handling " + 
                          sessions[i].getIdInternal() + " with exception: " + 
                          ex, ex);
            }
         }
         
         if (!backgroundProcessAllowed.get())
         {
            return;
         }

         // Next, handle any unloaded sessions

         
         // We may have not gotten replication of a timestamp for requests 
         // that occurred w/in maxUnreplicatedInterval_ of the previous
         // request. So we add a grace period to avoid flushing a session early
         // and permanently losing part of its node structure in JBoss Cache.
         long maxUnrep = getMaxUnreplicatedInterval() < 0 ? 60 :  getMaxUnreplicatedInterval();
         
         Map<String, OwnedSessionUpdate> unloaded = getUnloadedSessions();
         for (Map.Entry<String, OwnedSessionUpdate> entry : unloaded.entrySet())
         {            
            if (!backgroundProcessAllowed.get())
            {
               return;
            }
            
            String realId = entry.getKey();
            OwnedSessionUpdate osu = entry.getValue();
            
            long now = System.currentTimeMillis();
            long elapsed = (now - osu.getUpdateTime());
            try
            {
               if (expire && osu.getMaxInactive() >= 1 && elapsed >= (osu.getMaxInactive() + maxUnrep) * 1000L)
               {
                  //if (osu.passivated && osu.owner == null)
                  if (osu.isPassivated())
                  {
                     // Passivated session needs to be expired. A call to 
                     // findSession will bring it out of passivation
                     Session session = findSession(realId);
                     if (session != null)
                     {
                        session.isValid(); // will expire
                        continue;
                     }
                  }
                  
                  // If we get here either !osu.passivated, or we don't own
                  // the session or the session couldn't be reactivated (invalidated by user). 
                  // Either way, do a cleanup
                  getDistributedCacheManager().removeSessionLocal(realId, osu.getOwner());
                  unloadedSessions_.remove(realId);
                  stats_.removeStats(realId);
                  
               }
               else if (passivate && !osu.isPassivated())
               {  
                  // we now have a valid session; store it so we can check later
                  // if we need to passivate it
                  passivationChecks.add(new PassivationCheck(realId, osu));
               }
            } 
            catch (Exception ex)
            {
               log_.error("processExpirationPassivation(): failed handling unloaded session " + 
                       realId, ex);
            }
         }
         
         // Now, passivations
         if (passivate)
         {
            // Iterate through sessions, earliest lastAccessedTime to latest
            for (PassivationCheck passivationCheck : passivationChecks)
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
                        passivationCheck.getRealId(), e);
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
      
      if (trace_)
      { 
         log_.trace("processExpirationPassivation(): Completed ...");
         log_.trace("processExpirationPassivation(): active sessions = " + calcActiveSessions());
         log_.trace("processExpirationPassivation(): expired sessions = " + expiredCounter_);
         if (passivate)
         {
            log_.trace("processExpirationPassivation(): passivated count = " + getPassivatedSessionCount());
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   protected void processSipSessionExpirationPassivation() {      
      boolean expire = maxInactiveInterval_ >= 0;
      boolean passivate = isPassivationEnabled();
      
      long passivationMax = passivationMaxIdleTime_ * 1000L;
      long passivationMin = passivationMinIdleTime_ * 1000L;

      if (trace_) { 
         log_.trace("processSipSessionExpirationPassivation(): Looking for sip sessions that have expired ...");
         log_.trace("processSipSessionExpirationPassivation(): passivation Max = " + passivationMax);
         log_.trace("processSipSessionExpirationPassivation(): passivation Min = " + passivationMin);
         log_.trace("processSipSessionExpirationPassivation(): active sip sessions = " + calcActiveSipSessions());
         log_.trace("processSipSessionExpirationPassivation(): expired sip sessions = " + expiredSipSessionCounter_);
         if (passivate) {
            log_.trace("processSipSessionExpirationPassivation(): passivated count = " + getPassivatedSipSessionCount());
         }
      }
      
      // Holder for sessions or OwnedSessionUpdates that survive expiration,
      // sorted by last acccessed time
      TreeSet<SipSessionPassivationCheck> passivationChecks = new TreeSet<SipSessionPassivationCheck>();
      
      try {
         // Don't track sessions invalidated via this method as if they
         // were going to be re-requested by the thread
         ConvergedSessionInvalidationTracker.suspend();
         
         // First, handle the sessions we are actively managing
         ClusteredSipSession<? extends OutgoingDistributableSessionData> sessions[] = findLocalSipSessions();
         for (ClusteredSipSession<? extends OutgoingDistributableSessionData> session : sessions) {
            if (!backgroundProcessAllowed.get()) {
               return;
            }
            
            try {
               if (expire) {
                  // JBAS-2403. Check for outdated sessions where we think
                  // the local copy has timed out.  If found, refresh the
                  // session from the cache in case that might change the timeout
                  if (this.outdatedSipSessionChecker.isSessionOutdated(session) && session.isValidInternal()) {
                     // FIXME in AS 5 every time we get a notification from the distributed
                     // cache of an update, we get the latest timestamp. So
                     // we shouldn't need to do a full session load here. A load
                     // adds a risk of an unintended data gravitation.
                     
                     // JBAS-2792 don't assign the result of loadSession to session
                     // just update the object from the cache or fall through if
                     // the session has been removed from the cache
                     loadSipSession(session.getKey(), false, sipManagerDelegate.getSipFactoryImpl(), session.getSipApplicationSession());
                  }
                  // Do a normal invalidation check that will expire the
                  // session if it has timed out
                  // DON'T SYNCHRONIZE on session here -- isValid() and
                  // expire() are meant to be multi-threaded and synchronize
                  // properly internally; synchronizing externally can lead
                  // to deadlocks!!
                  if (!session.isValidInternal()) continue;
               }
                
               // we now have a valid session; store it so we can check later
               // if we need to passivate it
               if (passivate) {
                  passivationChecks.add(new SipSessionPassivationCheck(session));
               }
            }
            catch (Exception ex) {
               log_.error("processSipSessionExpirationPassivation(): failed handling " + 
                          session.getKey() + " with exception: " + 
                          ex, ex);
            }
         }
         
         if (!backgroundProcessAllowed.get()) {
            return;
         }

         // Now, passivations
         if (passivate) {
            // Iterate through sessions, earliest lastAccessedTime to latest
            for (SipSessionPassivationCheck passivationCheck : passivationChecks) {               
               try {
                  long timeNow = System.currentTimeMillis();
                  long timeIdle = timeNow - passivationCheck.getLastUpdate();
                  if(trace_) {
                	  log_.trace("Time now " + timeNow + ", Time Idle " + timeIdle);
                  }
                  // if maxIdle time configured, means that we need to passivate sessions that have
                  // exceeded the max allowed idle time
                  if (passivationMax >= 0  && timeIdle > passivationMax) {
                     passivationCheck.passivate();
                  }
                  // If the session didn't exceed the passivationMaxIdleTime_, see   
                  // if the number of sessions managed by this manager greater than the max allowed 
                  // active sessions, passivate the session if it exceed passivationMinIdleTime_ 
                  else if (maxActiveAllowed_ > 0 
                              && passivationMin > 0 
                              && calcActiveSipSessions() >= maxActiveAllowed_ 
                              && timeIdle > passivationMin) {
                     passivationCheck.passivate();
                  } else {
                     // the entries are ordered by lastAccessed, so once
                     // we don't passivate one, we won't passivate any
                     break;
                  }
               }
               catch (Exception e) {
                  String unloadMark = passivationCheck.isUnloaded() ? "unloaded " : "";
                  log_.error("processSipSessionExpirationPassivation(): failed passivating " + unloadMark + "session " + 
                        passivationCheck.getKey(), e);
               }                  
            }
         }
      }
      catch (Exception ex) {
         log_.error("processSipSessionExpirationPassivation(): failed with exception: " + ex, ex);
      } finally {
         ConvergedSessionInvalidationTracker.resume();
      }
      
      if (trace_) { 
         log_.trace("processSipSessionExpirationPassivation(): Completed ...");
         log_.trace("processSipSessionExpirationPassivation(): active sip sessions = " + calcActiveSipSessions());
         log_.trace("processSipSessionExpirationPassivation(): expired sip sessions = " + expiredSipSessionCounter_);
         if (passivate) {
            log_.trace("processSipSessionExpirationPassivation(): passivated count = " + getPassivatedSipSessionCount());
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   protected void processSipApplicationSessionExpirationPassivation()
   {      
      boolean expire = maxInactiveInterval_ >= 0;
      boolean passivate = isPassivationEnabled();
      
      long passivationMax = passivationMaxIdleTime_ * 1000L;
      long passivationMin = passivationMinIdleTime_ * 1000L;

      if (trace_)
      { 
         log_.trace("processSipApplicationSessionExpirationPassivation(): Looking for sip application sessions that have expired ...");
         log_.trace("processSipApplicationSessionExpirationPassivation(): active sip application sessions = " + calcActiveSipApplicationSessions());
         log_.trace("processSipApplicationSessionExpirationPassivation(): expired sip application sessions = " + expiredSipApplicationSessionCounter_);
         if (passivate)
         {
            log_.trace("processSipApplicationSessionExpirationPassivation(): passivated count = " + getPassivatedSipApplicationSessionCount());
         }
      }
      
      // Holder for sessions or OwnedSessionUpdates that survive expiration,
      // sorted by last acccessed time
      TreeSet<SipApplicationSessionPassivationCheck> passivationChecks = new TreeSet<SipApplicationSessionPassivationCheck>();
      
      try
      {
         // Don't track sessions invalidated via this method as if they
         // were going to be re-requested by the thread
         ConvergedSessionInvalidationTracker.suspend();
         
         // First, handle the sessions we are actively managing
         ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> sessions[] = findLocalSipApplicationSessions();
         for (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session : sessions)
         {
            if (!backgroundProcessAllowed.get())
            {
               return;
            }
            
            try
            {               

               if (expire)
               {
                  // JBAS-2403. Check for outdated sessions where we think
                  // the local copy has timed out.  If found, refresh the
                  // session from the cache in case that might change the timeout
                  if (this.outdatedSipApplicationSessionChecker.isSessionOutdated(session) && session.isValidInternal())
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
                  if (!session.isValidInternal()) continue;
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
               log_.error("processSipApplicationSessionExpirationPassivation(): failed handling " + 
                          session.getKey() + " with exception: " + 
                          ex, ex);
            }
         }
         
         if (!backgroundProcessAllowed.get())
         {
            return;
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
                              && calcActiveSipApplicationSessions() >= maxActiveAllowed_ 
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
                  log_.error("processSipApplicationSessionExpirationPassivation(): failed passivating " + unloadMark + "session " + 
                        passivationCheck.getKey(), e);
               }                  
            }
         }
      }
      catch (Exception ex)
      {
         log_.error("processSipApplicationSessionExpirationPassivation(): failed with exception: " + ex, ex);
      }
      finally
      {
         ConvergedSessionInvalidationTracker.resume();
      }
      
      if (trace_)
      { 
         log_.trace("processSipApplicationSessionExpirationPassivation(): Completed ...");
         log_.trace("processSipApplicationSessionExpirationPassivation(): active sessions = " + calcActiveSipApplicationSessions());
         log_.trace("processSipApplicationSessionExpirationPassivation(): expired sessions = " + expiredSipApplicationSessionCounter_);
         if (passivate)
         {
            log_.trace("processSipApplicationSessionExpirationPassivation(): passivated count = " + getPassivatedSipApplicationSessionCount());
         }
      }
   }
   
   /**
    * Session passivation logic for an actively managed session.
    * 
    * @param realId the session id, minus any jvmRoute
    */
   private void processSessionPassivation(String realId)
   {
      // get the session from the local map
      ClusteredSession<? extends OutgoingDistributableSessionData> session = findLocalSession(realId);
      // Remove actively managed session and add to the unloaded sessions
      // if it's already unloaded session (session == null) don't do anything, 
      if (session != null)
      {
         synchronized (session)
         {
            if (trace_)
            {
               log_.trace("Passivating session with id: " + realId);
            }
            
            session.notifyWillPassivate(ClusteredSessionNotificationCause.PASSIVATION);
            getDistributedCacheManager().evictSession(realId);
            sessionPassivated();
            
            // Put the session in the unloadedSessions map. This will
            // expose the session to regular invalidation.
            Object obj = unloadedSessions_.put(realId, 
                  new OwnedSessionUpdate(null, session.getLastAccessedTimeInternal(), session.getMaxInactiveInterval(), true));
            if (trace_)
            {
               if (obj == null)
               {
                  log_.trace("New session " + realId + " added to unloaded session map");
               }
               else
               {
                  log_.trace("Updated timestamp for unloaded session " + realId);
               }
            }
            sessions_.remove(realId);
         }
      }
      else if (trace_)
      {
         log_.trace("processSessionPassivation():  could not find session " + realId);
      }
   }
   
   /**
    * Session passivation logic for sessions only in the distributed store.
    * 
    * @param realId the session id, minus any jvmRoute
    */
   private void processUnloadedSessionPassivation(String realId, OwnedSessionUpdate osu)
   {
      if (trace_)
      {
         log_.trace("Passivating session with id: " + realId);
      }

      getDistributedCacheManager().evictSession(realId, osu.getOwner());
      osu.setPassivated(true);
      sessionPassivated();      
   }
   
   public void sessionActivated()
   {
      int pc = passivatedCount_.decrementAndGet();
      // Correct for drift since we don't know the true passivation
      // count when we started.  We can get activations of sessions
      // we didn't know were passivated.
      // FIXME -- is the above statement still correct? Is this needed?
      if (pc < 0) 
      {
         // Just reverse our decrement.
         passivatedCount_.incrementAndGet();
      }
   }
   
   private void sessionPassivated()
   {
      int pc = passivatedCount_.incrementAndGet();
      int max = maxPassivatedCount_.get();
      while (pc > max)
      {
         if (!maxPassivatedCount_ .compareAndSet(max, pc))
         {
            max = maxPassivatedCount_.get();
         }
      }
   }
   
   /**
    * Clear the underlying cache store.
    */
   private void clearSessions()
   {
      boolean passivation = isPassivationEnabled();
      // First, the sessions we have actively loaded
      ClusteredSession<? extends OutgoingDistributableSessionData>[] sessions = findLocalSessions();
      for(int i=0; i < sessions.length; i++)
      {
         ClusteredSession<? extends OutgoingDistributableSessionData> ses = sessions[i];
         
         if (trace_)
         {
             log_.trace("clearSessions(): clear session by expiring or passivating: " + ses);
         }
         try
         {
            // if session passivation is enabled, passivate sessions instead of expiring them which means
            // they'll be available to the manager for activation after a restart. 
            if(passivation && ses.isValid())
            {               
               processSessionPassivation(ses.getRealId());
            }
            else
            {               
               boolean notify = true;
               //FIXME set to true when problem with cahce on stop is resolved
               boolean localCall = false;
               boolean localOnly = true;
               ses.expire(notify, localCall, localOnly, ClusteredSessionNotificationCause.UNDEPLOY);               
            }
         }
         catch (Throwable t)
         {
            log_.warn("clearSessions(): Caught exception expiring or passivating session " +
                     ses.getIdInternal(), t);
         }
         finally
         {
            // Guard against leaking memory if anything is holding a
            // ref to the session by clearing its internal state
            ses.recycle();
         }
      }      
      
      String action = passivation ? "evicting" : "removing";
      Set<Map.Entry<String, OwnedSessionUpdate>> unloaded = 
               unloadedSessions_.entrySet();
      for (Iterator<Map.Entry<String, OwnedSessionUpdate>> it = unloaded.iterator(); it.hasNext();)
      {
         Map.Entry<String, OwnedSessionUpdate> entry = it.next();
         String realId = entry.getKey();         
         try
         {
            if (passivation)
            {
               OwnedSessionUpdate osu = entry.getValue();
               // Ignore the marker entries for our passivated sessions
               if (!osu.isPassivated())
               {
            	 //FIXME uncomment when problem with cahce on stop is resolved
                  getDistributedCacheManager().evictSession(realId, osu.getOwner());
               }
            }
            else
            {
            	//FIXME uncomment when problem with cahce on stop is resolved
//               getDistributedCacheManager().removeSessionLocal(realId);           
            }
         }
         catch (Exception e)
         {
            // Not as big a problem; we don't own the session
            log_.debug("Problem " + action + " session " + realId + " -- " + e);
         }
         it.remove(); 
      }
   }
   
   /**
    * Callback from the distributed cache to notify us that a session
    * has been modified remotely.
    * 
    * @param realId the session id, without any trailing jvmRoute
    * @param dataOwner  the owner of the session.  Can be <code>null</code> if
    *                   the owner is unknown.
    * @param distributedVersion the session's version per the distributed cache
    * @param timestamp the session's timestamp per the distributed cache
    * @param metadata the session's metadata per the distributed cache
    */
   public boolean sessionChangedInDistributedCache(String realId, 
                                         String dataOwner,
                                         int distributedVersion,
                                         long timestamp, 
                                         DistributableSessionMetadata metadata)
   {
      boolean updated = true;
      
      ClusteredSession<? extends OutgoingDistributableSessionData> session = findLocalSession(realId);
      if (session != null)
      {
         // Need to invalidate the loaded session. We get back whether
         // this an actual version increment
         updated = session.setVersionFromDistributedCache(distributedVersion);
         if (updated && trace_)      
         {            
            log_.trace("session in-memory data is invalidated for id: " + realId + 
                       " new version: " + distributedVersion);
         }         
      }
      else
      {
         int maxLife = metadata == null ? getMaxInactiveInterval() : metadata.getMaxInactiveInterval();
         
         Object existing = unloadedSessions_.put(realId, new OwnedSessionUpdate(dataOwner, timestamp, maxLife, false));
         if (existing == null)
         {
            calcActiveSessions();
            if (trace_)
            {
               log_.trace("New session " + realId + " added to unloaded session map");
            }
         }
         else if (trace_)
         {
            log_.trace("Updated timestamp for unloaded session " + realId);
         }
      }
      
      return updated;
   }
   
   @SuppressWarnings("unchecked")
   private static ContextClassLoaderSwitcher getContextClassLoaderSwitcher()
   {
      return (ContextClassLoaderSwitcher) AccessController.doPrivileged(ContextClassLoaderSwitcher.INSTANTIATOR);
   }
   
   /**
    * Removes the session from this Manager's collection of actively managed
    * sessions.  Also removes the session from the distributed cache, both
    * on this server and on all other server to which this one replicates.
    */
   public void remove(Session session)
   {
      ClusteredSession<? extends OutgoingDistributableSessionData> clusterSess = uncheckedCastSession(session);
      synchronized (clusterSess)
      {
         String realId = clusterSess.getRealId();
         if (realId == null)
            return;

         if (trace_)
         {
            log_.trace("Removing session from store with id: " + realId);
         }

         try {
            clusterSess.removeMyself();
         }
         finally {
            // We don't want to replicate this session at the end
            // of the request; the removal process took care of that
        	 ConvergedSessionReplicationContext.sessionExpired(clusterSess, realId, getSnapshotManager());
            
            // Track this session to prevent reincarnation by this request 
            // from the distributed cache
            SessionInvalidationTracker.sessionInvalidated(realId, this);
            
            sessions_.remove(realId);
            stats_.removeStats(realId);

            // Compute how long this session has been alive, and update
            // our statistics accordingly
            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
            sessionExpired(timeAlive);
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
   
   
	public boolean storeSipSession(ClusteredSipSession session) {
		boolean stored = false;
		if (session != null && started_) {			

			synchronized (session) {
				if (logger.isDebugEnabled()) {
					logger.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getId() + " isValid " + session.isValidInternal() +
							" getMustReplicateTimestamp " + session.getMustReplicateTimestamp() + 
							" session state " + session.getState());
				}

				if (session.isValidInternal()
						&& (session.isSessionDirty() || session.getMustReplicateTimestamp()) && 
								(State.CONFIRMED.equals(session.getState()) ||
										(((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).getReplicationStrategy().equals(ReplicationStrategy.EarlyDialog) && State.EARLY.equals(session.getState())))) {
					final String realId = session.getId();
					if(logger.isDebugEnabled()) {
						logger.debug("replicating following sip session " + session.getId());
					}					

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
	
	public boolean storeSipApplicationSession(ClusteredSipApplicationSession session) {
		boolean stored = false;
		if (session != null && started_) {					
			synchronized (session) {
				if (logger.isDebugEnabled()) {
					log_.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getId());
				}

				if (session.isValidInternal()
						&& (session.isSessionDirty() || session
								.getMustReplicateTimestamp())) {
					final String realId = session.getId();
					if(logger.isDebugEnabled()) {
						logger.debug("replicating following sip application session " + session.getId());
					}

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
      if (!session.isValidInternal())
      {
         // Not an error; this can happen if a failover request pulls in an
         // outdated session from the distributed cache (see TODO above)
         log_.debug("Cannot add session with id=" + session.getKey() +
                    " because it is invalid");
         return;
      }

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
      if (!session.isValidInternal())
      {
         // Not an error; this can happen if a failover request pulls in an
         // outdated session from the distributed cache (see TODO above)
         log_.debug("Cannot add session with id=" + session.getKey() +
                    " because it is invalid");
         return;
      }

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
		SipApplicationSessionKey applicationSessionKey = null;
		if(sipApplicationSessionImpl != null) {
			applicationSessionKey = sipApplicationSessionImpl.getKey();
		} else {
			applicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(key.getApplicationName(), key.getApplicationSessionId());
		}
		if(logger.isDebugEnabled()) {
			logger.debug("load sip session " + key + ", create = " + create + " sip app session = "+ applicationSessionKey);
		}
		SipFactoryImpl sipFactory = sipFactoryImpl;
		if(sipFactory == null) {
			sipFactory = this.getSipFactoryImpl();
		}
		long begin = System.currentTimeMillis();
		boolean mustAdd = false;
		boolean passivated = false;
		 
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipSession) sipManagerDelegate.getSipSession(key, create, sipFactory, sipApplicationSessionImpl);
		boolean initialLoad = false;
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
			if(session == null) {
				initialLoad = true;
			} else {
				session.updateThisAccessedTime();
			}
			// Swap in/out the webapp classloader so we can deserialize
	         // attributes whose classes are only available to the webapp
			ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(getApplicationClassLoader());
			try {
	            IncomingDistributableSessionData data = getDistributedCacheConvergedSipManager().getSipSessionData(applicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key), initialLoad);
	            if (data != null)
	            {
	            	if(logger.isDebugEnabled()) {
	        			logger.debug("data for sip session " + key + " found in the distributed cache");
	        		}
	            	if (session == null) {
	            		if(sipApplicationSessionImpl != null) {
	            			if(logger.isDebugEnabled()) {
	    	        			logger.debug("parent sip application is " + sipApplicationSessionImpl.getId());
	    	        		}	            			
		        			// This is either the first time we've seen this session on this
		        			// server, or we previously expired it and have since gotten
		        			// a replication message from another server
		        			mustAdd = true;
		        			initialLoad = true;
		        			session = (ClusteredSipSession<? extends OutgoingDistributableSessionData>) 
		        					((ClusteredSipManagerDelegate)sipManagerDelegate).getNewMobicentsSipSession(key, sipFactory, sipApplicationSessionImpl, true);
		        			OwnedSessionUpdate osu = unloadedSipSessions_.get(key);
		        	        passivated = (osu != null && osu.passivated);
	            		} else {
	            			if(logger.isDebugEnabled()) {
	    	        			logger.debug("beware null parent sip application for session " + key);
	    	        		}
	            		}
	            	} 
	            	if(session!= null) {
						session.update(data);						
	            	}
	            } else if(logger.isDebugEnabled()) {
	        		logger.debug("no data for sip session " + key + " in the distributed cache");
	            }
			} finally {
				Thread.currentThread().setContextClassLoader(prevTCL);
			}
//            else if(mustAdd)
//            {
//               // Clunky; we set the session variable to null to indicate
//               // no data so move on
//               session = null;
//            }
//            
//            if (session != null)
//            {
//               ClusteredSessionNotificationCause cause = passivated ? ClusteredSessionNotificationCause.ACTIVATION 
//                                                                    : ClusteredSessionNotificationCause.FAILOVER;
//               session.notifyDidActivate(cause);
//            }
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
			if (doTx) {
				batchingManager.endBatch();
			}
		}
		if (session != null) {
			if (mustAdd) {
				unloadedSipSessions_.remove(key);				
				if (!passivated) {
					session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
				}
			}
			long elapsed = System.currentTimeMillis() - begin;
			stats_.updateLoadStats(key.toString(), elapsed);

			if (log_.isDebugEnabled()) {
				log_
						.debug("loadSession(): id= " + key + ", session="
								+ session);
			}
			return session;
		} else if (log_.isDebugEnabled()) {
			log_.debug("loadSession(): session " + key
					+ " not found in distributed cache");
		}
		if(session != null) {
			ConvergedSessionReplicationContext.bindSipSession(session,
				getSnapshotSipManager());
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
	protected ClusteredSipApplicationSession loadSipApplicationSession(
			final SipApplicationSessionKey key, final boolean create) {
		if (key == null) {
			return null;
		}

		long begin = System.currentTimeMillis();
		boolean mustAdd = false;
		boolean passivated = false;
		boolean initialLoad = false;

		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = 
			(ClusteredSipApplicationSession) sipManagerDelegate.getSipApplicationSession(key, create);

	
//		synchronized (session) {
		boolean doTx = false;
		BatchingManager batchingManager = 
			getDistributedCacheConvergedSipManager().getBatchingManager();
		try {
			// We need transaction so any data gravitation replication
			// is sent in batch.
			// Don't do anything if there is already transaction context
			// associated with this thread.
			if (batchingManager.isBatchInProgress() == false) {
				batchingManager.startBatch();
				doTx = true;
			}
			if(session == null) {
				initialLoad = true;
			} else {
				session.updateThisAccessedTime();
			}
			 // Swap in/out the webapp classloader so we can deserialize
	         // attributes whose classes are only available to the webapp
			ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(getApplicationClassLoader());
			try {
				IncomingDistributableSessionData data = 
					getDistributedCacheConvergedSipManager().getSipApplicationSessionData(key.getId(), initialLoad);
				if (data != null) {
					if (session == null) {
						// This is either the first time we've seen this session on
						// this
						// server, or we previously expired it and have since gotten
						// a replication message from another server
						mustAdd = true;
						initialLoad = true;
						//since the session has to be recreated we don't recreate the sas timer since it is fault tolerant and has been failed over as well
						session = (ClusteredSipApplicationSession) 
							((ClusteredSipManagerDelegate)sipManagerDelegate).getNewMobicentsSipApplicationSession(key, ((SipContext)getContainer()), true);
						OwnedSessionUpdate osu = unloadedSipApplicationSessions_.get(key);
						passivated = (osu != null && osu.passivated);
					}
					if(session != null) {					
						session.update(data);					
					}
					if(mustAdd && ((SipContext)getContainer()).getConcurrencyControlMode() == ConcurrencyControlMode.SipApplicationSession) {
						if(logger.isInfoEnabled()) {
							logger.info("SipApplicationSession " + key + " has just been recreated and concurrency control is set to SipApplicationSession, colocating the timers here to ensure no concurrent access across the cluster");
						}
						// if the session has been recreated, reschedule the FT timers locally if they are not local already
						((ClusteredSipApplicationSession) session).rescheduleTimersLocally();
					}
				} else if(logger.isDebugEnabled()) {
        			logger.debug("no data for sip application session " + key + " in the distributed cache");
        		}
			} finally {
				Thread.currentThread().setContextClassLoader(prevTCL);
			}
//			else if (mustAdd) {
//				// Clunky; we set the session variable to null to indicate
//				// no data so move on
//				session = null;
//			}
//
//			if (session != null) {
//				ClusteredSessionNotificationCause cause = passivated ? ClusteredSessionNotificationCause.ACTIVATION
//						: ClusteredSessionNotificationCause.FAILOVER;
//				session.notifyDidActivate(cause);
//			}
		} catch (Exception ex) {
			try {
				// if(doTx)
				// Let's set it no matter what.
				batchingManager.setBatchRollbackOnly();
			} catch (Exception exn) {
				log_.error("Problem rolling back session mgmt transaction",	exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException){
				throw (RuntimeException) ex;
			}
			throw new RuntimeException("Failed to load session " + key, ex);
		} finally {
			if (doTx) {
				batchingManager.endBatch();
			}
		}
		if (session != null) {
			if (mustAdd) {
				unloadedSipApplicationSessions_.remove(key);	
				if (!passivated) {
					session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
				}
			}
			long elapsed = System.currentTimeMillis() - begin;
			stats_.updateLoadStats(key.toString(), elapsed);

			if (log_.isDebugEnabled()) {
				log_.debug("loadSession(): id= " + key.toString()
						+ ", session=" + session);
			}
			return session;
		} else if (log_.isDebugEnabled()) {
			log_.debug("loadSession(): session " + key.toString()
					+ " not found in distributed cache");
		}
		if(session != null) {
			ConvergedSessionReplicationContext.bindSipApplicationSession(session,
				getSnapshotSipManager());
		}
		// }
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
//		boolean notSession = (getReplicationGranularity() != ReplicationGranularity.SESSION);
		boolean notSession = true;
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
//		boolean notSession = (getReplicationGranularity() != ReplicationGranularity.SESSION);
		boolean notSession = true;
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
				log_.debug("processSipApplicationSessionRepl(): failed with exception", ex);

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
					"JBossCacheSipManager.processSipApplicationSessionRepl(): "
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
				if (logger.isDebugEnabled()) {
					logger.debug("Passivating session with id: " + key);
				}
				
				session.passivate();
				getDistributedCacheConvergedSipManager().evictSipSession(session.getSipApplicationSession().getKey().getId(), SessionManagerUtil.getSipSessionHaKey(key));
				sipSessionPassivated();
				sipManagerDelegate.removeSipSession(key);
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("processSipSessionPassivation():  could not find sip session "
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
				if (logger.isDebugEnabled()) {
					logger.debug("Passivating session with id: " + key);
				}
				
				session.passivate();
				getDistributedCacheConvergedSipManager().evictSipApplicationSession(key.getId());
				sipApplicationSessionPassivated();				
				sipManagerDelegate.removeSipApplicationSession(key);
			}
		} else if (logger.isDebugEnabled()) {
			logger.debug("processSipApplicationSessionPassivation():  could not find sip application session "
					+ key);
		}
	}

	/**
	 * Session passivation logic for sessions only in the distributed store.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processUnloadedSipSessionPassivation(ClusteredSipSessionKey key,
			OwnedSessionUpdate osu) {
		if (trace_) {
			log_.trace("Passivating session with id: " + key);
		}

		getDistributedCacheConvergedSipManager().evictSipSession(key.getSipApplicationSessionKey().getId(), SessionManagerUtil.getSipSessionHaKey(key.getSipSessionKey()), osu.owner);
		osu.passivated = true;
		sipSessionPassivated();
	}
	
	/**
	 * Session passivation logic for sessions only in the distributed store.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processUnloadedSipApplicationSessionPassivation(String key,
			OwnedSessionUpdate osu) {
		if (trace_) {
			log_.trace("Passivating session with id: " + key);
		}

		getDistributedCacheConvergedSipManager().evictSipApplicationSession(key, osu.owner);
		osu.passivated = true;
		sipApplicationSessionPassivated();
	}

	private void sipSessionPassivated() {
		int pc = passivatedSipSessionCount_.incrementAndGet();
		int max = maxPassivatedSipSessionCount_.get();
		while (pc > max) {
			if (!maxPassivatedSipSessionCount_.compareAndSet(max, pc)) {
				max = maxPassivatedSipSessionCount_.get();
			}
		}
	}
	
	private void sipApplicationSessionPassivated() {
		int pc = passivatedSipApplicationSessionCount_.incrementAndGet();
		int max = maxPassivatedSipApplicationSessionCount_.get();
		while (pc > max) {
			if (!maxPassivatedSipApplicationSessionCount_.compareAndSet(max, pc)) {
				max = maxPassivatedSipApplicationSessionCount_.get();
			}
		}
	}
	
//	/**
//	 * Gets the ids of all sessions in the distributed cache and adds them to
//	 * the unloaded sessions map, along with their lastAccessedTime and their
//	 * maxInactiveInterval. Passivates overage or excess sessions.
//	 */
//	private void initializeUnloadedSipSessions() {
//		Map<SipSessionKey, String> sessions = getDistributedCacheConvergedSipManager().getSipSessionKeys();
//		if (sessions != null) {
//			boolean passivate = isPassivationEnabled();
//
//			long passivationMax = passivationMaxIdleTime_ * 1000L;
//			long passivationMin = passivationMinIdleTime_ * 1000L;
//
//			for (Map.Entry<SipSessionKey, String> entry : sessions.entrySet()) {
//				SipSessionKey key = entry.getKey();
//				String owner = entry.getValue();
//
//				long ts = -1;
//				DistributableSessionMetadata md = null;
//				try {
//					IncomingDistributableSessionData sessionData = getDistributedCacheConvergedSipManager()
//							.getSessionData(key, owner, false);
//					ts = sessionData.getTimestamp();
//					md = sessionData.getMetadata();
//				} catch (Exception e) {
//					// most likely a lock conflict if the session is being
//					// updated remotely;
//					// ignore it and use default values for timstamp and
//					// maxInactive
//					log_.debug("Problem reading metadata for session " + key
//							+ " -- " + e.toString());
//				}
//
//				long lastMod = ts == -1 ? System.currentTimeMillis() : ts;
//				int maxLife = md == null ? getMaxInactiveInterval() : md
//						.getMaxInactiveInterval();
//
//				OwnedSessionUpdate osu = new OwnedSessionUpdate(owner, lastMod,
//						maxLife, false);
//				unloadedSipSessions_.put(key, osu);
//				if (passivate) {
//					try {
//						long elapsed = System.currentTimeMillis() - lastMod;
//						// if maxIdle time configured, means that we need to
//						// passivate sessions that have
//						// exceeded the max allowed idle time
//						if (passivationMax >= 0 && elapsed > passivationMax) {
//							if (trace_) {
//								log_.trace("Elapsed time of " + elapsed
//										+ " for session " + key
//										+ " exceeds max of " + passivationMax
//										+ "; passivating");
//							}
//							processUnloadedSipSessionPassivation(key, osu);
//						}
//						// If the session didn't exceed the
//						// passivationMaxIdleTime_, see
//						// if the number of sessions managed by this manager
//						// greater than the max allowed
//						// active sessions, passivate the session if it exceed
//						// passivationMinIdleTime_
//						else if (maxActiveAllowed_ > 0 && passivationMin >= 0
//								&& calcActiveSessions() > maxActiveAllowed_
//								&& elapsed >= passivationMin) {
//							if (trace_) {
//								log_.trace("Elapsed time of " + elapsed
//										+ " for session " + key
//										+ " exceeds min of " + passivationMin
//										+ "; passivating");
//							}
//							processUnloadedSipSessionPassivation(key, osu);
//						}
//					} catch (Exception e) {
//						// most likely a lock conflict if the session is being
//						// updated remotely; ignore it
//						log_.debug("Problem passivating session " + key
//								+ " -- " + e.toString());
//					}
//				}
//			}
//		}
//	}
	
	/**
	 * Gets the ids of all sessions in the distributed cache and adds them to
	 * the unloaded sessions map, along with their lastAccessedTime and their
	 * maxInactiveInterval. Passivates overage or excess sessions.
	 */
	private void initializeUnloadedSipApplicationSessions() {
		Map<String, String> sessions = getDistributedCacheConvergedSipManager().getSipApplicationSessionKeys();
		if (sessions != null) {
			boolean passivate = isPassivationEnabled();

			long passivationMax = passivationMaxIdleTime_ * 1000L;
			long passivationMin = passivationMinIdleTime_ * 1000L;

			for (Map.Entry<String, String> entry : sessions.entrySet()) {
				String sipApplicationSessionKey = entry.getKey();
				String owner = entry.getValue();

				long ts = -1;
				DistributableSessionMetadata md = null;
				try {
					IncomingDistributableSessionData sessionData = getDistributedCacheConvergedSipManager()
							.getSipApplicationSessionData(sipApplicationSessionKey, owner, false);
					ts = sessionData.getTimestamp();
					md = sessionData.getMetadata();
				} catch (Exception e) {
					// most likely a lock conflict if the session is being
					// updated remotely;
					// ignore it and use default values for timstamp and
					// maxInactive
					log_.debug("Problem reading metadata for session " + sipApplicationSessionKey
							+ " -- " + e.toString());
				}

				long lastMod = ts == -1 ? System.currentTimeMillis() : ts;
				int maxLife = md == null ? getMaxInactiveInterval() : md
						.getMaxInactiveInterval();

				OwnedSessionUpdate osu = new OwnedSessionUpdate(owner, lastMod,
						maxLife, false);
				unloadedSipApplicationSessions_.put(sipApplicationSessionKey, osu);
				if (passivate) {
					try {
						long elapsed = System.currentTimeMillis() - lastMod;
						// if maxIdle time configured, means that we need to
						// passivate sessions that have
						// exceeded the max allowed idle time
						if (passivationMax >= 0 && elapsed > passivationMax) {
							if (trace_) {
								log_.trace("Elapsed time of " + elapsed
										+ " for session " + sipApplicationSessionKey
										+ " exceeds max of " + passivationMax
										+ "; passivating");
							}
							processUnloadedSipApplicationSessionPassivation(sipApplicationSessionKey, osu);
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
										+ " for session " + sipApplicationSessionKey
										+ " exceeds min of " + passivationMin
										+ "; passivating");
							}
							processUnloadedSipApplicationSessionPassivation(sipApplicationSessionKey, osu);
						}
					} catch (Exception e) {
						// most likely a lock conflict if the session is being
						// updated remotely; ignore it
						log_.debug("Problem passivating session " + sipApplicationSessionKey
								+ " -- " + e.toString());
					}
				}
			}
		}
	}	
	
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
	@Override
	public void setContainer(Container container) {
		super.setContainer(container);		
		sipManagerDelegate.setContainer(container);
		DistributedCacheConvergedSipManager<? extends OutgoingDistributableSessionData> distributedCacheConvergedSipManager = getDistributedCacheConvergedSipManager();
		// Issue 1514: http://code.google.com/p/mobicents/issues/detail?id=1514	
		// only set this up if the application is a SIP or converged application	
		if(container instanceof SipContext) {			
			applicationName = ((SipContext)getContainer()).getApplicationName();
			if(logger.isDebugEnabled()) {
				logger.debug("Setting Application Name " + applicationName + " for Manager " + this + " for context " + container.getName());
			}
			distributedCacheConvergedSipManager.setApplicationName(applicationName);
			distributedCacheConvergedSipManager.setApplicationNameHashed(((SipContext)getContainer()).getApplicationNameHashed());
		}
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
				log_.debug("Removing sip session from store with id: " + realId);
			}
			boolean notSession = true;
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
				clusterSess.removeMyself();
			} catch (Exception ex) {
				if (log_.isDebugEnabled())
					log_.debug("removeSipSession(): failed with exception", ex);

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
						"JBossCacheManager.removeSipSession(): "
								+ "failed to remove session.", ex);
			} finally {
				if (doTx)
					batchingManager.endBatch();
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipSessionExpired(clusterSess,
						key, getSnapshotSipManager());
				
				// Track this session to prevent reincarnation by this request 
	            // from the distributed cache
	            ConvergedSessionInvalidationTracker.sipSessionInvalidated(key, this);

//	            sipManagerDelegate.removeSipSession(key);
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
				log_.debug("Removing sip app session from store with id: " + realId);
			}

			boolean notSession = true;
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
				clusterSess.removeMyself();
			} catch (Exception ex) {
				if (log_.isDebugEnabled())
					log_.debug("removeSipApplicationSession(): failed with exception", ex);

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
						"JBossCacheManager.removeSipApplicationSession(): "
								+ "failed to remove session.", ex);
			} finally {
				if (doTx)
					batchingManager.endBatch();
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipApplicationSessionExpired(clusterSess,
						key, getSnapshotSipManager());

				// Track this session to prevent reincarnation by this request 
	            // from the distributed cache
	            ConvergedSessionInvalidationTracker.sipApplicationSessionInvalidated(key, this);

//	            sipManagerDelegate.removeSipApplicationSession(key);
				stats_.removeStats(realId);
				
				// Compute how long this session has been alive, and update
	            // our statistics accordingly
//	            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
//	            sipApplicationSessionExpired(timeAlive);
			}
		}
		return clusterSess;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManager#getSipApplicationSession(org.mobicents.servlet.sip.core.session.SipApplicationSessionKey, boolean)
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(
			final SipApplicationSessionKey key, final boolean create) {
		return getSipApplicationSession(key, create, false);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.DistributableSipManager#getSipApplicationSession(org.mobicents.servlet.sip.core.session.SipApplicationSessionKey, boolean, boolean)
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(
			final SipApplicationSessionKey key, final boolean create, final boolean localOnly) {
		// Find it from the local store first
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = findLocalSipApplicationSession(key, false);

		if(!localOnly) {
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
					&& !ConvergedSessionInvalidationTracker.isSipApplicationSessionInvalidated(key, this)) {
				if (logger.isDebugEnabled())
					log_.debug("Checking for sip app session " + key
							+ " in the distributed cache");
	
				session = loadSipApplicationSession(key, create);
	//			if (session != null) {
	//				add(session);
	//				Iterator<ClusteredSipSession<OutgoingDistributableSessionData>> sipSessionIt = 
	//					(Iterator<ClusteredSipSession<OutgoingDistributableSessionData>>)
	//						((MobicentsSipApplicationSession)session).getSessions("SIP");
	//				if(logger.isDebugEnabled()) {
	//					logger.debug("loading the underlying sip sessions from the cache");
	//				}
	//				while (sipSessionIt.hasNext()) {						
	//					ClusteredSipSession sipSession = (ClusteredSipSession) sipSessionIt
	//							.next();
	//					if(logger.isDebugEnabled()) {
	//						logger.debug("loading the underlying sip session from the cache " + sipSession.getKey());
	//					}
	//					getSipSession(sipSession.getKey(), false, null, session);
	//				}	
	//				// TODO should we advise of a new session?
	//				// tellNew();
	//			}
			} else if (session != null && session.isOutdated()) {
				if (logger.isDebugEnabled())
					log_.debug("Updating sip app session " + key
							+ " from the distributed cache");
	
				
				// Need to update it from the cache
				loadSipApplicationSession(key, create);
			}
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

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManager#getSipSession(org.mobicents.servlet.sip.core.session.SipSessionKey, boolean, org.mobicents.servlet.sip.message.SipFactoryImpl, org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession)
	 */
	public MobicentsSipSession getSipSession(final SipSessionKey key,
			final boolean create, final SipFactoryImpl sipFactoryImpl,
			final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		return getSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.DistributableSipManager#getSipSession(org.mobicents.servlet.sip.core.session.SipSessionKey, boolean, org.mobicents.servlet.sip.message.SipFactoryImpl, org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession, boolean)
	 */
	public MobicentsSipSession getSipSession(final SipSessionKey key,
			final boolean create, final SipFactoryImpl sipFactoryImpl,
			final MobicentsSipApplicationSession sipApplicationSessionImpl, final boolean localOnly) {
		
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
		
		if(!localOnly) {			
			boolean isSipSessionInvalidated = ConvergedSessionInvalidationTracker.isSipSessionInvalidated(key, this);
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
	//			if (session != null)
	//		    {
	//		          add(session);
	////		          // We now notify, since we've added a policy to allow listeners 
	////		          // to discriminate. But the default policy will not allow the 
	////		          // notification to be emitted for FAILOVER, so the standard
	////		          // behavior is unchanged.
	////		          session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
	//		    }
			} else if (session != null && session.isOutdated()) {
				if (logger.isDebugEnabled())
					logger.debug("Updating sip session " + key
							+ " from the distributed cache");
	
				// Need to update it from the cache
				loadSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl);
			}

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
		SipApplicationSessionKey key = new SipApplicationSessionKey(realId, applicationName);
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
//				session.invalidate(notify, localCall, localOnly,
//						ClusteredSessionNotificationCause.INVALIDATE);
				session.invalidate();
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
	public void notifyRemoteSipSessionInvalidation(String sipAppSessionId,
			String sipSessionId) {
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseHaSipSessionKey(sipSessionId, sipAppSessionId, applicationName);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + sipSessionId, e);
			return ;
		}
		// Remove the session from our local map		
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
			stats_.removeStats(key.toString());
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
//				session.expire(notify, localCall, localOnly,
//						ClusteredSessionNotificationCause.INVALIDATE);
				session.invalidate();
			} finally {
				ConvergedSessionInvalidationTracker.resume();

				// Remove any stats for this session
				stats_.removeStats(key.toString());

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
		SipApplicationSessionKey key = new SipApplicationSessionKey(realId, applicationName);
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
			String sipAppSessionId,
			String sipSessionId) {		
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseHaSipSessionKey(sipSessionId, sipAppSessionId, applicationName);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + sipSessionId, e);
			return ;
		}
		
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipSession) sipManagerDelegate
				.getSipSession(key, false, null, null);
		if (session != null) {
			session.sessionAttributesDirty();
		} else {
			log_.warn("Received local attribute notification for " + key.toString()
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
		SipApplicationSessionKey key = new SipApplicationSessionKey(realId, applicationName);
		
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = findLocalSipApplicationSession(
				key, false);
		if (session != null) {
			// Need to invalidate the loaded session. We get back whether
			// this an actual version increment
			updated = session
					.setVersionFromDistributedCache(distributedVersion);
			if (updated && log_.isDebugEnabled()) {
				log_.debug("sip app session in-memory data is invalidated for id: "
						+ realId + " new version: " + distributedVersion);
			}
		} else {
			log_.debug("sip app session not found locally for : "
					+ key + " new version: " + distributedVersion);
		}
		// unloadedSipApplicationSessions_ is not currently supported
//		else {
//			int maxLife = metadata == null ? getMaxInactiveInterval()
//					: metadata.getMaxInactiveInterval();
//
//			Object existing = unloadedSipApplicationSessions_
//					.put(key.getId(), new OwnedSessionUpdate(dataOwner, timestamp,
//							maxLife, false));
//			if (existing == null) {
//				calcActiveSessions();
//				if (log_.isDebugEnabled()) {
//					log_.debug("New session " + realId
//							+ " added to unloaded session map");
//				}
//			} else if (trace_) {
//				log_.trace("Updated timestamp for unloaded session " + realId);
//			}
//		}

		return updated;
	}
	
	public void sipApplicationSessionActivated() {
		int pc = passivatedSipApplicationSessionCount_.decrementAndGet();
		// Correct for drift since we don't know the true passivation
		// count when we started. We can get activations of sessions
		// we didn't know were passivated.
		// FIXME -- is the above statement still correct? Is this needed?
		if (pc < 0) {
			// Just reverse our decrement.
			passivatedSipApplicationSessionCount_.incrementAndGet();
		}
	}
	
	public void sipSessionActivated() {
		int pc = passivatedSipSessionCount_.decrementAndGet();
		// Correct for drift since we don't know the true passivation
		// count when we started. We can get activations of sessions
		// we didn't know were passivated.
		// FIXME -- is the above statement still correct? Is this needed?
		if (pc < 0) {
			// Just reverse our decrement.
			passivatedSipSessionCount_.incrementAndGet();
		}
	}
	
	/**
	 * Callback from the distributed cache to notify us that a session has been
	 * modified remotely.
	 * 
	 * @param sipSessionId
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
			String sipAppSessionId,
			String sipSessionId, String dataOwner, int distributedVersion,
			long timestamp, DistributableSessionMetadata metadata) {
		
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseHaSipSessionKey(sipSessionId, sipAppSessionId, applicationName);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + sipSessionId, e);
			return false;
		}  
		
		boolean updated = true;
		
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = findLocalSipSession(
				key, false, null);
		if (session != null) {
			// Need to invalidate the loaded session. We get back whether
			// this an actual version increment
			updated = session
					.setVersionFromDistributedCache(distributedVersion);
			if (updated && trace_) {
				log_.trace("session in-memory data is invalidated for id: "
						+ key.toString() + " new version: " + distributedVersion);
			}
		} else {
			log_.debug("sip session not found locally for : "
					+ key + " new version: " + distributedVersion);
		}
		// unloadedSipSessions_ is not currently supported
//		else {
//			int maxLife = metadata == null ? getMaxInactiveInterval()
//					: metadata.getMaxInactiveInterval();
//
//			Object existing = unloadedSipSessions_
//					.put(new ClusteredSipSessionKey(key, sipAppSessionKey), new OwnedSessionUpdate(dataOwner, timestamp,
//							maxLife, false));
//			if (existing == null) {
//				calcActiveSessions();
//				if (trace_) {
//					log_.trace("New session " + sipSessionId
//							+ " added to unloaded session map");
//				}
//			} else if (trace_) {
//				log_.trace("Updated timestamp for unloaded session " + sipSessionId);
//			}
//		}

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
	public void stop() throws LifecycleException {
		// Handle re-entrance
		if (this.semaphore.tryAcquire()) {
			try {
				log_.debug("Closing off LockingValve");

				// Acquire all remaining permits, shutting off locking valve
				this.semaphore.acquire(TOTAL_PERMITS - 1);
			} catch (InterruptedException e) {
				this.semaphore.release();

				throw new LifecycleException(e);
			}
		}
		clearSessions();
		unloadedSessions_.clear();
		passivatedCount_.set(0);
		super.stop();
	}
	
	@Override
	protected void registerManagerMBean() {
		try {
			MBeanServer server = getMBeanServer();

			String domain;
			if (container_ instanceof ContainerBase) {
				domain = ((ContainerBase) container_).getDomain();
			} else {
				domain = server.getDefaultDomain();
			}
			String hostName = ((Host) container_.getParent()).getName();
			hostName = (hostName == null) ? "localhost" : hostName;
			ObjectName clusterName = new ObjectName(domain
					+ ":type=SipManager,host=" + hostName + ",path="
					+ ((Context) container_).getPath());

			if (server.isRegistered(clusterName)) {
				log_.warn("MBean " + clusterName + " already registered");
				return;
			}

			objectName_ = clusterName;
			server.registerMBean(this, clusterName);

		} catch (Exception ex) {
			log_.error("Could not register " + getClass().getSimpleName()
					+ " to MBeanServer", ex);
		}
	}
	
	@Override
	protected void startExtensions() {
		super.startExtensions();		
		
		this.outdatedSessionChecker = initOutdatedSessionChecker();
		this.outdatedSipSessionChecker = initOutdatedSipSessionChecker();
		this.outdatedSipApplicationSessionChecker = initOutdatedSipApplicationSessionChecker();
		mobicentsCache = new MobicentsCache(getDistributedCacheConvergedSipManager().getJBossCache(), null);
		mobicentsCluster = new DefaultMobicentsCluster(mobicentsCache, getDistributedCacheConvergedSipManager().getJBossCache().getConfiguration().getRuntimeConfig().getTransactionManager(), new DefaultClusterElector());
		if(logger.isDebugEnabled()) {
			logger.debug("Mobicents Sip Servlets Default Mobicents Cluster " + mobicentsCluster + " created");
		}
		initializeUnloadedSipApplicationSessions();
//		initializeUnloadedSipSessions();
		initClusteredSipSessionNotificationPolicy();
		initClusteredSipApplicationSessionNotificationPolicy();
		// Handle re-entrance
		if (!this.semaphore.tryAcquire()) {
			log_.debug("Opening up LockingValve");

			// Make all permits available to locking valve
			this.semaphore.release(TOTAL_PERMITS);
		} else {
			// Release the one we just acquired
			this.semaphore.release();
		}
	}
		
	@Override
	protected void stopExtensions() {
		super.stopExtensions();
		
		mobicentsCache.stop();
		mobicentsCache = null;
		mobicentsCluster = null;
		removeAllSessions();
		
		passivatedSipSessionCount_.set(0);
		passivatedSipApplicationSessionCount_.set(0);
		
		unloadedSipApplicationSessions_.clear();
		unloadedSipSessions_.clear();		
	}		
	
	/**
	 * Create and start a snapshot manager.
	 */
	@Override
	protected void initSnapshotManager() {
		String ctxPath = ((Context) container_).getPath();
		if (SnapshotMode.INSTANT == getSnapshotMode()) {
			setSnapshotManager(new InstantConvergedSnapshotManager(this, ctxPath));
		} else if (getSnapshotMode() == null) {
			log_.warn("Snapshot mode must be 'instant' or 'interval' - "
					+ "using 'instant'");
			setSnapshotMode(SnapshotMode.INSTANT);
			setSnapshotManager(new InstantConvergedSnapshotManager(this, ctxPath));
		} else if (ReplicationGranularity.FIELD == getReplicationGranularity()) {
			throw new IllegalStateException("Property snapshotMode must be "
					+ SnapshotMode.INTERVAL + " when FIELD granularity is used");
		} else if (getSnapshotInterval() < 1) {
			log_
					.warn("Snapshot mode set to 'interval' but snapshotInterval is < 1 "
							+ "using 'instant'");
			setSnapshotMode(SnapshotMode.INSTANT);
			setSnapshotManager(new InstantConvergedSnapshotManager(this, ctxPath));
		} else {
			setSnapshotManager(new IntervalConvergedSnapshotManager(this, ctxPath,
					getSnapshotInterval()));
		}

		getSnapshotManager().start();
	}
	
	/**
	 * Instantiate a SnapshotManager and ClusteredSessionValve and add the valve
	 * to our parent Context's pipeline. Add a JvmRouteValve and
	 * BatchReplicationClusteredSessionValve if needed.
	 */
	protected void installValves() {
		log_.debug("Adding LockingValve");
		this.installValve(new LockingValve(this.valveLock));

		// If JK usage wasn't explicitly configured, default to enabling
		// it if jvmRoute is set on our containing Engine
		if (!getUseJK()) {
			setUseJK(Boolean.valueOf(getJvmRoute() != null));
		}

		if (getUseJK()) {
			log_
					.debug("We are using JK for load-balancing. Adding JvmRouteValve.");
			this.installValve(new ConvergedJvmRouteValve(this));
		}

		// Handle batch replication if needed.
		// TODO -- should we add this even if not FIELD in case a cross-context
		// call traverses a field-based webapp?
		BatchingManager valveBM = null;
		if (getReplicationGranularity() == ReplicationGranularity.FIELD
				&& Boolean.TRUE.equals(getReplicationConfig().getReplicationFieldBatchMode())) {
			valveBM = getDistributedCacheConvergedSipManager().getBatchingManager();;
			log_
					.debug("Including transaction manager in ClusteredSessionValve to support batch replication.");
		}

		// Add clustered session valve
		ConvergedClusteredSessionValve valve = new ConvergedClusteredSessionValve(this, valveBM);
		log_.debug("Adding ConvergedClusteredSessionValve");
		this.installValve(valve);
	}
	
	protected void installValve(Valve valve) {
		boolean installed = false;

		// In embedded mode, install the valve via JMX to be consistent
		// with the way the overall context is created in TomcatDeployer.
		// We can't do this in unembedded mode because we are called
		// before our Context is registered with the MBean server
		if (embedded_) {
			ObjectName name = this.getObjectName(this.container_);

			if (name != null) {
				try {
					MBeanServer server = this.getMBeanServer();

					server.invoke(name, "addValve", new Object[] { valve },
							new String[] { Valve.class.getName() });

					installed = true;
				} catch (Exception e) {
					// JBAS-2422. If the context is restarted via JMX, the above
					// JMX call will fail as the context will not be registered
					// when it's made. So we catch the exception and fall back
					// to adding the valve directly.
					// TODO consider skipping adding via JMX and just do it
					// directly
					log_.debug("Caught exception installing valve to Context",
							e);
				}
			}
		}

		if (!installed) {
			// If possible install via the ContainerBase.addValve() API.
			if (this.container_ instanceof ContainerBase) {
				((ContainerBase) this.container_).addValve(valve);
			} else {
				// No choice; have to add it to the context's pipeline
				this.container_.getPipeline().addValve(valve);
			}
		}
	}
	
	private ObjectName getObjectName(Container container) {
		String oname = container.getObjectName();

		try {
			return (oname == null) ? null : new ObjectName(oname);
		} catch (MalformedObjectNameException e) {
			log_.warn("Error creating object name from string " + oname, e);
			return null;
		}
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
	

	public double getNumberOfSipApplicationSessionCreationPerSecond() {
		return sipManagerDelegate.getNumberOfSipApplicationSessionCreationPerSecond();
	}

	public double getNumberOfSipSessionCreationPerSecond() {
		return sipManagerDelegate.getNumberOfSipSessionCreationPerSecond();
	}

	public void updateStats() {
		sipManagerDelegate.updateStats();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getMaxPassivatedSipSessionCount() {
		return maxPassivatedSipSessionCount_.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPassivatedSipSessionCount() {
		return passivatedSipSessionCount_.get();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public long getMaxPassivatedSipApplicationSessionCount() {
		return maxPassivatedSipApplicationSessionCount_.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPassivatedSipApplicationSessionCount() {
		return passivatedSipApplicationSessionCount_.get();
	}

	/**
	 * Calculates the number of active sessions, and updates the max # of local
	 * active sessions and max # of sessions.
	 * <p>
	 * Call this method when a new session is added or when an accurate count of
	 * active sessions is needed.
	 * </p>
	 * 
	 * @return the size of the sessions map + the size of the unloaded sessions
	 *         map - the count of passivated sessions
	 */
	protected int calcActiveSipApplicationSessions() {
		localActiveSipApplicationSessionCounter_.set(sipManagerDelegate.getNumberOfSipApplicationSessions());
		int active = localActiveSipApplicationSessionCounter_.get();
		int maxLocal = maxLocalActiveSipApplicationSessionCounter_.get();
		while (active > maxLocal) {
			if (!maxLocalActiveSipApplicationSessionCounter_.compareAndSet(maxLocal, active)) {
				maxLocal = maxLocalActiveSipApplicationSessionCounter_.get();
			}
		}

		int count = getTotalActiveSipApplicationSessions();
		int max = maxActiveSipApplicationSessionCounter_.get();
		while (count > max) {
			if (!maxActiveSipApplicationSessionCounter_.compareAndSet(max, count)) {
				max = maxActiveSipApplicationSessionCounter_.get();
				// Something changed, so reset our count
				count = getTotalActiveSipApplicationSessions();
			}
		}
		return count;
	}
	
	/**
	 * Calculates the number of active sessions, and updates the max # of local
	 * active sessions and max # of sessions.
	 * <p>
	 * Call this method when a new session is added or when an accurate count of
	 * active sessions is needed.
	 * </p>
	 * 
	 * @return the size of the sessions map + the size of the unloaded sessions
	 *         map - the count of passivated sessions
	 */
	protected int calcActiveSipSessions() {
		localActiveSipSessionCounter_.set(sipManagerDelegate.getNumberOfSipSessions());
		int active = localActiveSipSessionCounter_.get();
		int maxLocal = maxLocalActiveSipSessionCounter_.get();
		while (active > maxLocal) {
			if (!localActiveSipSessionCounter_.compareAndSet(maxLocal, active)) {
				maxLocal = localActiveSipSessionCounter_.get();
			}
		}

		int count = getTotalActiveSipSessions();
		int max = maxActiveSipSessionCounter_.get();
		while (count > max) {
			if (!maxActiveSipSessionCounter_.compareAndSet(max, count)) {
				max = maxActiveSipSessionCounter_.get();
				// Something changed, so reset our count
				count = getTotalActiveSipSessions();
			}
		}
		return count;
	}
	
	private class SipSessionPassivationCheck implements Comparable<SipSessionPassivationCheck>
	   {
		  private final ClusteredSipSessionKey key;
	      private final OwnedSessionUpdate osu;
	      private final ClusteredSipSession<? extends OutgoingDistributableSessionData> session;
	      
	      private SipSessionPassivationCheck(ClusteredSipSessionKey key, OwnedSessionUpdate osu)
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
	         
	         this.key = new ClusteredSipSessionKey(session.getKey(), session.getSipApplicationSession().getKey());
	         this.session = session;
	         this.osu = null;
	      }
	      
	      private long getLastUpdate()
	      {
	         return osu == null ? session.getLastAccessedTime() : osu.updateTime;
	      }
	      
	      private void passivate()
	      {
	         if (osu == null)
	         {
	            JBossCacheSipManager.this.processSipSessionPassivation(key.getSipSessionKey());
	         }
	         else
	         {
	            JBossCacheSipManager.this.processUnloadedSipSessionPassivation(key, osu);
	         }
	      }
	      
	      private ClusteredSipSessionKey getKey()
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
	         return osu == null ? session.getLastAccessedTime() : osu.updateTime;
	      }
	      
	      private void passivate()
	      {
	         if (osu == null)
	         {
	            JBossCacheSipManager.this.processSipApplicationSessionPassivation(key);
	         }
	         else
	         {
	            JBossCacheSipManager.this.processUnloadedSipApplicationSessionPassivation(key.getId(), osu);
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
	      
	      public boolean isPassivated()
	      {
	         return passivated;
	      }

	      void setPassivated(boolean passivated)
	      {
	         this.passivated = passivated;
	      }

	      public String getOwner()
	      {
	         return owner;
	      }

	      public long getUpdateTime()
	      {
	         return updateTime;
	      }

	      public int getMaxInactive()
	      {
	         return maxInactive;
	      }
	   }

	public ClusteredSipApplicationSessionNotificationPolicy getSipApplicationSessionNotificationPolicy() {
		return sipApplicationSessionNotificationPolicy_;
	}

	public ClusteredSipSessionNotificationPolicy getSipSessionNotificationPolicy() {
		return sipSessionNotificationPolicy_;
	}	

	/**
	 * @return the snapshotSipManager_
	 */
	public SnapshotSipManager getSnapshotSipManager() {
		return (SnapshotSipManager)getSnapshotManager();
	}
	
	private static class SemaphoreLock implements Lock
	   {
	      private final Semaphore semaphore;
	      
	      SemaphoreLock(Semaphore semaphore)
	      {
	         this.semaphore = semaphore;
	      }
	      
	      /**
	       * @see java.util.concurrent.locks.Lock#lock()
	       */
	      public void lock()
	      {
	         this.semaphore.acquireUninterruptibly();
	      }

	      /**
	       * @see java.util.concurrent.locks.Lock#lockInterruptibly()
	       */
	      public void lockInterruptibly() throws InterruptedException
	      {
	         this.semaphore.acquire();
	      }

	      /**
	       * @see java.util.concurrent.locks.Lock#newCondition()
	       */
	      public Condition newCondition()
	      {
	         throw new UnsupportedOperationException();
	      }

	      /**
	       * @see java.util.concurrent.locks.Lock#tryLock()
	       */
	      public boolean tryLock()
	      {
	         return this.semaphore.tryAcquire();
	      }

	      /**
	       * @see java.util.concurrent.locks.Lock#tryLock(long, java.util.concurrent.TimeUnit)
	       */
	      public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
	      {
	         return this.semaphore.tryAcquire(timeout, unit);
	      }

	      /**
	       * @see java.util.concurrent.locks.Lock#unlock()
	       */
	      public void unlock()
	      {
	         this.semaphore.release();
	      }
	   }
	
	private class PassivationCheck implements Comparable<PassivationCheck>
	   {
	      private final String realId;
	      private final OwnedSessionUpdate osu;
	      private final ClusteredSession<? extends OutgoingDistributableSessionData> session;
	      
	      private PassivationCheck(String realId, OwnedSessionUpdate osu)
	      {
	         assert osu != null : "osu is null";
	         assert realId != null : "realId is null";
	         
	         this.realId = realId;
	         this.osu = osu;
	         this.session = null;
	      }
	      
	      private PassivationCheck(ClusteredSession<? extends OutgoingDistributableSessionData> session)
	      {
	         assert session != null : "session is null";
	         
	         this.realId = session.getRealId();
	         this.session = session;
	         this.osu = null;
	      }
	      
	      private long getLastUpdate()
	      {
	         return osu == null ? session.getLastAccessedTimeInternal() : osu.getUpdateTime();
	      }
	      
	      private void passivate()
	      {
	         if (osu == null)
	         {
	            JBossCacheSipManager.this.processSessionPassivation(realId);
	         }
	         else
	         {
	            JBossCacheSipManager.this.processUnloadedSessionPassivation(realId, osu);
	         }
	      }
	      
	      private String getRealId()
	      {
	         return realId;
	      }
	      
	      private boolean isUnloaded()
	      {
	         return osu != null;
	      }

	      // This is what causes sorting based on lastAccessed
	      public int compareTo(PassivationCheck o)
	      {
	         long thisVal = getLastUpdate();
	         long anotherVal = o.getLastUpdate();
	         return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
	      }
	   }

	public MobicentsCluster getMobicentsCluster() {		
		return mobicentsCluster;
	}

	public class AlwaysTrueOutdatedSessionChecker implements
			OutdatedSessionChecker {
		public boolean isSessionOutdated(
				ClusteredSession<? extends OutgoingDistributableSessionData> session) {
			return true;
		}

	}

	public class AskSessionOutdatedSessionChecker implements
			OutdatedSessionChecker {
		public boolean isSessionOutdated(
				ClusteredSession<? extends OutgoingDistributableSessionData> session) {
			return session.isOutdated();
		}

	}

	public interface OutdatedSessionChecker {
		boolean isSessionOutdated(
				ClusteredSession<? extends OutgoingDistributableSessionData> session);
	}
	
	public class AlwaysTrueOutdatedSipSessionChecker implements
			OutdatedSipSessionChecker {
		public boolean isSessionOutdated(
				ClusteredSipSession<? extends OutgoingDistributableSessionData> session) {
			return true;
		}

	}

	public class AskSipSessionOutdatedSessionChecker implements
			OutdatedSipSessionChecker {
		public boolean isSessionOutdated(
				ClusteredSipSession<? extends OutgoingDistributableSessionData> session) {
			return session.isOutdated();
		}

	}

	public interface OutdatedSipSessionChecker {
		boolean isSessionOutdated(
				ClusteredSipSession<? extends OutgoingDistributableSessionData> session);
	}

	public class AlwaysTrueOutdatedSipApplicationSessionChecker implements
			OutdatedSipApplicationSessionChecker {
		public boolean isSessionOutdated(
				ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session) {
			return true;
		}

	}

	public class AskSipApplicationSessionOutdatedSessionChecker implements
			OutdatedSipApplicationSessionChecker {
		public boolean isSessionOutdated(
				ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session) {
			return session.isOutdated();
		}

	}

	public interface OutdatedSipApplicationSessionChecker {
		boolean isSessionOutdated(
				ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session);
	}

	public void checkSipApplicationSessionPassivation(SipApplicationSessionKey key) {
		if (maxActiveAllowed_ > 0
				&& calcActiveSipApplicationSessions() >= maxActiveAllowed_) {
			if (trace_) {
				log_.trace("checkSipApplicationSessionPassivation(): active sip application sessions = "
						+ calcActiveSipApplicationSessions() + " and max allowed sessions = "
						+ maxActiveAllowed_);
			}

			processSipApplicationSessionExpirationPassivation();

			if (calcActiveSipApplicationSessions() >= maxActiveAllowed_) {
				// Exceeds limit. We need to reject it.
				rejectedSipApplicationSessionCounter_.incrementAndGet();
				// Catalina api does not specify what happens
				// but we will throw a runtime exception for now.				
//				throw new IllegalStateException("checkSipApplicationSessionPassivation(): number of "
//						+ "active sip application sessions exceeds the maximum limit: "
//						+ maxActiveAllowed_ + " when trying to create sip application session "
//						+ key);
			}
		}		
	}
	
	public void checkSipSessionPassivation(SipSessionKey key) {
		if (maxActiveAllowed_ > 0
				&& calcActiveSipSessions() >= maxActiveAllowed_) {
			if (trace_) {
				log_.trace("checkSipSessionPassivation(): active sip sessions = "
						+ calcActiveSipSessions() + " and max allowed sessions = "
						+ maxActiveAllowed_);
			}

			processSipSessionExpirationPassivation();

			if (calcActiveSipSessions() >= maxActiveAllowed_) {
				// Exceeds limit. We need to reject it.
				rejectedSipSessionCounter_.incrementAndGet();
				// Catalina api does not specify what happens
				// but we will throw a runtime exception for now.
//				throw new IllegalStateException("checkSipSessionPassivation(): number of "
//						+ "active sip sessions exceeds the maximum limit: "
//						+ maxActiveAllowed_ + " when trying to create sip session "
//						+ key);
			}
		}		
	}
}
