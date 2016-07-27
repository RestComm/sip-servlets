package org.jboss.as.web.session.sip;

import static org.jboss.as.web.WebMessages.MESSAGES;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;
import javax.sip.SipStack;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Service;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.infinispan.Cache;
import org.jboss.as.clustering.web.BatchingManager;
import org.jboss.as.clustering.web.ClusteringNotSupportedException;
import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.as.clustering.web.DistributedCacheManagerFactory;
import org.jboss.as.clustering.web.IncomingDistributableSessionData;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.jboss.as.clustering.web.OutgoingSessionGranularitySessionData;
import org.jboss.as.clustering.web.SessionOwnershipSupport;
import org.jboss.as.clustering.web.infinispan.sip.DistributedCacheManager;
import org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata;
import org.jboss.as.clustering.web.sip.DistributedCacheConvergedSipManager;
import org.jboss.as.clustering.web.sip.LocalDistributableConvergedSessionManager;
import org.jboss.as.web.WebLogger;
import org.jboss.as.web.session.AskSessionOutdatedSessionChecker;
import org.jboss.as.web.session.ClusteredSession;
import org.jboss.as.web.session.DistributableSessionManager;
import org.jboss.as.web.session.LockingValve;
import org.jboss.as.web.session.OutdatedSessionChecker;
import org.jboss.as.web.session.SessionInvalidationTracker;
import org.jboss.as.web.session.SessionReplicationContext;
import org.jboss.as.web.session.SnapshotManager;
import org.jboss.as.web.session.notification.ClusteredSessionNotificationCause;
import org.jboss.as.web.session.notification.sip.ClusteredSipApplicationSessionNotificationCapability;
import org.jboss.as.web.session.notification.sip.ClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.as.web.session.notification.sip.ClusteredSipSessionNotificationCapability;
import org.jboss.as.web.session.notification.sip.ClusteredSipSessionNotificationPolicy;
import org.jboss.as.web.session.notification.sip.IgnoreUndeployLegacyClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.as.web.session.notification.sip.IgnoreUndeployLegacyClusteredSipSessionNotificationPolicy;
import org.jboss.logging.Logger;
import org.jboss.marshalling.ClassResolver;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.util.loading.ContextClassLoaderSwitcher;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;
import org.restcomm.cache.MobicentsCache;
import org.restcomm.cluster.DefaultMobicentsCluster;
import org.restcomm.cluster.MobicentsCluster;
import org.restcomm.cluster.election.DefaultClusterElector;

/**
 * Implementation of a converged clustered session manager for
 * catalina using JBossCache replication.
 * 
 * Based on DistributableSessionManager JBOSS AS 7.5.0
 * 
 * 
 * @author Ben Wang
 * @author Brian Stansberry
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class DistributableSipSessionManager<O extends OutgoingDistributableSessionData> extends DistributableSessionManager implements ClusteredSipSessionManager<O>, LocalDistributableConvergedSessionManager, DistributableSipSessionManagerMBean, DistributableSipManager {

	protected static final String DISTRIBUTED_CACHE_FACTORY_CLASSNAME = "org.jboss.as.clustering.web.infinispan.sip.DistributedCacheConvergedSipManagerFactoryImpl";

	protected static Logger logger = Logger.getLogger(DistributableSipSessionManager.class);

	protected SipManagerDelegate sipManagerDelegate;

	protected DistributedCacheManagerFactory distributedCacheManagerFactory;
	/**
	 * Informational name for this Catalina component
	 */
	private static final String info_ = "DistributableSipSessionManager/1.0";

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
	private final ConcurrentMap<String, ClusteredSession<O>> embryonicSessions = 
		new ConcurrentHashMap<String, ClusteredSession<O>>();

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
	private final Semaphore semaphore = new Semaphore(TOTAL_PERMITS, true);
	private final Lock valveLock = new SemaphoreLock(this.semaphore);

	private OutdatedSessionChecker outdatedSessionChecker;
	private OutdatedSipSessionChecker outdatedSipSessionChecker;
	private OutdatedSipApplicationSessionChecker outdatedSipApplicationSessionChecker;

	private volatile boolean stopping;

	/** Are we running embedded in JBoss? */
	private boolean embedded_ = false;
	MobicentsCluster mobicentsCluster;
	MobicentsCache mobicentsCache;
	private String applicationName;

	private SnapshotManager snapshotManager;

	// ---------------------------------------------------------- Constructors

	//public DistributableSipSessionManager() throws ClusteringNotSupportedException {		
	//	this(getConvergedDistributedCacheManager());		
	//}

	//protected static DistributedCacheManagerFactory getConvergedDistributedCacheManager() throws ClusteringNotSupportedException {
	// jboss-5ben ez volt (törölni majd ezeket a kikommentezett sorokat): 
	//DistributedCacheManagerFactoryFactory distributedCacheManagerFactoryFactory = DistributedCacheManagerFactoryFactory.getInstance();
	//distributedCacheManagerFactoryFactory.setFactoryClassName(DISTRIBUTED_CACHE_FACTORY_CLASSNAME);
	//return distributedCacheManagerFactoryFactory.getDistributedCacheManagerFactory();

	//	DistributedCacheManagerFactoryService distributedCacheManagerFactoryService = new DistributedCacheManagerFactoryService();
	//	return distributedCacheManagerFactoryService.getValue();
	//}

	/** 
	 * Create a new DistributableSessionManager using the given cache. For use in unit testing.
	 * 
	 * @param pojoCache
	 */
	//public DistributableSipSessionManager(DistributedCacheManagerFactory distributedManagerFactory)
	//{
	//   super(distributedManagerFactory);
	//}


	public DistributableSipSessionManager(DistributedCacheManagerFactory factory, JBossWebMetaData metaData, ClassResolver resolver) throws ClusteringNotSupportedException {
		super(factory, metaData, resolver);
		sipManagerDelegate = new ClusteredSipManagerDelegate(getReplicationGranularity(), getUseJK(), (ClusteredSipSessionManager)this);
		this.sipApplicationSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
		this.sipSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
		if (logger.isDebugEnabled()){
			logger.debug("DistributableSipSessionManager constructor - getReplicationConfig = " + getReplicationConfig().toString() + ", sipSessionNotificationPolicyClass_ = " + sipSessionNotificationPolicyClass_);
		}
		embedded_ = true;
	}

	//@Override
	//public void init(String name, JBossWebMetaData webMetaData) throws ClusteringNotSupportedException {
	//super.init(name, webMetaData);
	//sipManagerDelegate = new ClusteredSipManagerDelegate(ReplicationGranularity.valueOf(getReplicationGranularityString()),getUseJK(), (ClusteredSipManager)this);
	//this.sipApplicationSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
	//this.sipSessionNotificationPolicyClass_ = getReplicationConfig().getSessionNotificationPolicy();
	//embedded_ = true;
	//}

	private void initClusteredSipSessionNotificationPolicy()
	{
		if (logger.isDebugEnabled()){
			logger.debug("initClusteredSipSessionNotificationPolicy");
		}
		
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
			//this.sipSessionNotificationPolicy_ = (ClusteredSipSessionNotificationPolicy) Thread.currentThread().getContextClassLoader().loadClass(this.sipSessionNotificationPolicyClass_).newInstance();
			this.sipSessionNotificationPolicy_ = (ClusteredSipSessionNotificationPolicy) this.getClass().getClassLoader().loadClass(this.sipSessionNotificationPolicyClass_).newInstance();
			
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
		if (logger.isDebugEnabled()){
			logger.debug("initClusteredSipApplicationSessionNotificationPolicy");
		}
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
			//this.sipApplicationSessionNotificationPolicy_ = (ClusteredSipApplicationSessionNotificationPolicy) Thread.currentThread().getContextClassLoader().loadClass(this.sipApplicationSessionNotificationPolicyClass_).newInstance();
			this.sipApplicationSessionNotificationPolicy_ = (ClusteredSipApplicationSessionNotificationPolicy) this.getClass().getClassLoader().loadClass(this.sipApplicationSessionNotificationPolicyClass_).newInstance();
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
		if (logger.isDebugEnabled()){
			logger.debug("uncheckedCastSession");
		}
		return (ClusteredSession) session;
	}

	@SuppressWarnings("unchecked")
	private static ClusteredSipSession<? extends OutgoingDistributableSessionData> uncheckedCastSipSession(SipSession session)
	{
		if (logger.isDebugEnabled()){
			logger.debug("uncheckedCastSipSession");
		}
		return (ClusteredSipSession) session;
	}

	@SuppressWarnings("unchecked")
	private static ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> uncheckedCastSipApplicationSession(SipApplicationSession session)
	{
		if (logger.isDebugEnabled()){
			logger.debug("uncheckedCastSipApplicationSession");
		}
		return (ClusteredSipApplicationSession) session;
	}

	@SuppressWarnings("unchecked")
	private static <T extends OutgoingDistributableSessionData> DistributableSipSessionManager<T> uncheckedCastSipManager(DistributableSipSessionManager mgr)
	{
		if (logger.isDebugEnabled()){
			logger.debug("uncheckedCastSipManager");
		}
		return mgr;
	}

	@SuppressWarnings("unchecked")
	public DistributedCacheConvergedSipManager<? extends OutgoingDistributableSessionData> getDistributedCacheConvergedSipManager()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getDistributedCacheConvergedSipManager");
		}
		return (DistributedCacheConvergedSipManager) getDistributedCacheManager();
	}

	protected OutdatedSessionChecker initOutdatedSessionChecker()
	{
		if (logger.isDebugEnabled()){
			logger.debug("initOutdatedSessionChecker");
		}
		return new AskSessionOutdatedSessionChecker();      
	}

	protected OutdatedSipSessionChecker initOutdatedSipSessionChecker()
	{
		if (logger.isDebugEnabled()){
			logger.debug("initOutdatedSipSessionChecker");
		}
		return new AskSipSessionOutdatedSessionChecker();      
	}

	protected OutdatedSipApplicationSessionChecker initOutdatedSipApplicationSessionChecker()
	{
		if (logger.isDebugEnabled()){
			logger.debug("initOutdatedSipApplicationSessionChecker");
		}
		return new AskSipApplicationSessionOutdatedSessionChecker();      
	}

	// Satisfy the Manager interface.  Internally we use
	// createEmptyClusteredSession to avoid a cast
	public ClusteredSession<O> createEmptySession()
	{
		if (logger.isDebugEnabled()){
			logger.debug("createEmptySession");
		}
		ClusteredSession<O> session = null;
		try
		{
			// [JBAS-7123] Make sure we're either in the call stack where LockingValve has
			// a lock, or that we acquire one ourselves
			boolean inLockingValve = SessionReplicationContext.isLocallyActive();
			if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS))
			{
				try
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Creating an empty ClusteredSession");
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
			else if (logger.isDebugEnabled())
			{
				logger.debug("createEmptySession(): Manager is not handling requests; returning null");
			}
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}

		return session;
	}
	
	@SuppressWarnings("unchecked")
	private ClusteredSession<O> createEmptyConvergedClusteredSession()
	{     
		if (logger.isDebugEnabled()){
			logger.debug("createEmptyConvergedClusteredSession");
		}
		try {
            // [JBAS-7123] Make sure we're either in the call stack where LockingValve has
            // a lock, or that we acquire one ourselves
            boolean inLockingValve = SessionReplicationContext.isLocallyActive();
            if (inLockingValve || this.valveLock.tryLock(0, TimeUnit.SECONDS)) {
                try {
                	// Only session granularity is supported so far
                    //switch (this.getReplicationGranularity()) {
                    //    case ATTRIBUTE:
                    //        return (ClusteredSession<O>) new AttributeBasedClusteredSession((ClusteredSessionManager<OutgoingAttributeGranularitySessionData>) this);
                    //    default:
                			return (ClusteredSession<O>) new ConvergedSessionBasedClusteredSession((ClusteredSipSessionManager<OutgoingSessionGranularitySessionData>) this);
                    //}
                } finally {
                    if (!inLockingValve) {
                        this.valveLock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPassivatedSessionCount()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getPassivatedSessionCount");
		}
		return passivatedCount_.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public long getMaxPassivatedSessionCount()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getMaxPassivatedSessionCount");
		}
		return maxPassivatedCount_.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session createSession(String sessionId, Random random)
	{  
		if (logger.isDebugEnabled()){
			logger.debug("createSession - sessionId=" + sessionId);
		}
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
					session = createSessionInternal(sessionId, random);
				}
				finally
				{
					if (!inLockingValve)
					{
						this.valveLock.unlock();
					}
				}
			}
			else 
			{
				logger.trace("createEmptySession(): Manager is not handling requests; returning null");
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
	private Session createSessionInternal(String sessionId, Random random)
	{      
		if (logger.isDebugEnabled()){
			logger.debug("createSessionInternal - sessionId=" + sessionId);
		}
		// First check if we've reached the max allowed sessions, 
		// then try to expire/passivate sessions to free memory
		// maxActiveAllowed -1 is unlimited
		// We check here for maxActive instead of in add().  add() gets called
		// when we load an already existing session from the distributed cache
		// (e.g. in a failover) and we don't want to fail in that situation.

		if(maxActiveAllowed != -1 && calcActiveSessions() >= maxActiveAllowed)
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("createSession(): active sessions = " + calcActiveSessions() +
						" and max allowed sessions = " + maxActiveAllowed);
			}

			processHttpSessionExpirationPassivation();

			if (calcActiveSessions() >= maxActiveAllowed)
			{
				// Exceeds limit. We need to reject it.
				rejectedCounter.incrementAndGet();
				// Catalina api does not specify what happens
				// but we will throw a runtime exception for now.
				String msgEnd = (sessionId == null) ? "" : " id " + sessionId;
				throw new IllegalStateException("createSession(): number of " +
						"active sessions exceeds the maximum limit: " +
						maxActiveAllowed + " when trying to create session" + msgEnd);
			}
		}

		ClusteredSession<? extends OutgoingDistributableSessionData> session = createEmptyConvergedClusteredSession();

		if (session != null)
		{
			session.setNew(true);
			session.setCreationTime(System.currentTimeMillis());
			session.setMaxInactiveInterval(this.maxInactiveInterval);
			session.setValid(true);

			String clearInvalidated = null; // see below

			if (sessionId == null) {
				sessionId = this.generateSessionId(random);
			} else {
				clearInvalidated = sessionId;
			}

			session.setId(sessionId); // Setting the id leads to a call to add()

			getDistributedCacheManager().sessionCreated(session.getRealId());

			session.tellNew(ClusteredSessionNotificationCause.CREATE);

			if (logger.isDebugEnabled())
			{
				logger.debug("Created a ClusteredSession with id: " + sessionId);
			}

			createdCounter.incrementAndGet(); // the call to add() handles the other counters 

			// Add this session to the set of those potentially needing replication
			ConvergedSessionReplicationContext.bindSession(session, snapshotManager);

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
	private ClusteredSession<O> loadSession(String realId)
	{
		if (logger.isDebugEnabled()){
			logger.debug("loadSession - realId=" + realId);
		}
		if (realId == null)
		{
			return null;
		}
		ClusteredSession<O> session = null;

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

					//session = sessions_.get(realId);
					session = cast(this.sessions.get(realId));
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
						if (logger.isDebugEnabled()){
							logger.debug("loadSession - putIfAbsent with realId=" + realId + ", session.getId()=" + session.getId());
						}
						ClusteredSession<O> embryo = 
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
							if (logger.isDebugEnabled())
							{
								logger.debug("loadSession(): id= " + realId + ", session=" + session + "session.isOutdated() " + session.isOutdated());
							}
							// some one else loaded this
							return session;
						}

						//ContextClassLoaderSwitcher.SwitchContext switcher = null; 
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
								if (logger.isDebugEnabled()){
									logger.debug("loadSession - call startBatch");
								}
								getDistributedCacheConvergedSipManager().getBatchingManager().startBatch();
								doTx = true;
							}

							IncomingDistributableSessionData data = getDistributedCacheManager().getSessionData(realId, initialLoad);
							if (data != null)
							{
								session.update(data);
							}
							else
							{
								if (logger.isDebugEnabled())
								{
									logger.debug("loadSession(): id= " + realId + ", session=" + session + " : no data setting to null ");
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
								logger.error("Caught exception rolling back transaction", exn);
							}
							// We will need to alert Tomcat of this exception.
							if (ex instanceof RuntimeException)
								throw (RuntimeException) ex;

							throw new RuntimeException("loadSession(): failed to load session " +
									realId, ex);
						}
						finally
						{
							//try {
							if(doTx)
							{
								try
								{
									if (logger.isDebugEnabled()){
										logger.debug("loadSession - call endBatch");
									}
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
										logger.warn("Problem ending batch after loading session " + realId + " -- " + e.getLocalizedMessage() + " However session data was successful loaded.");
										logger.debug("Failure cause", e);
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
							//}
							//finally {
							//if (switcher != null)
							//{
							//switcher.reset();
							//}
							//}
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
							this.getReplicationStatistics().updateLoadStats(realId, elapsed);

							if (logger.isDebugEnabled())
							{
								logger.debug("loadSession(): id= " + realId + ", session=" + session);
							}
						}
						else if (logger.isDebugEnabled())
						{
							logger.debug("loadSession(): session " + realId +
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
		if (logger.isDebugEnabled()){
			logger.debug("add - session.getId()=" + session.getId());
		}
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
			else if (logger.isDebugEnabled())
			{
				logger.debug("add(): ignoring add -- Manager is not actively handling requests");
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
		if (logger.isDebugEnabled()){
			logger.debug("add - session.getId()=" + session.getId() + ", replicate=" + replicate);
		}
		// TODO -- why are we doing this check? The request checks session 
		// validity and will expire the session; this seems redundant
		//      if (!session.isValid())
		//      {
		//         // Not an error; this can happen if a failover request pulls in an
		//         // outdated session from the distributed cache (see TODO above)
		//         logger.debug("Cannot add session with id=" + session.getIdInternal() +
		//                    " because it is invalid");
		//         return;
		//      }

		String realId = session.getRealId();
		Object existing = this.sessions.put(realId, session);
		unloadedSessions_.remove(realId);

		if (!session.equals(existing))
		{
			if (replicate)
			{
				storeSession(session);
			}

			// Update counters
			calcActiveSessions();

			if (logger.isDebugEnabled())
			{
				logger.debug("Session with id=" + session.getIdInternal() + " added. " +
						"Current active sessions " + localActiveCounter.get());
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
		if (logger.isDebugEnabled()){
			logger.debug("findSessions");
		}
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

						if(logger.isDebugEnabled()) {
							logger.debug("findSessions: loading sessions from distributed cache: " + ids);
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
	public ClusteredSession<O> findSession(String id)
	{
		if (logger.isDebugEnabled()){
			logger.debug("findSession - id=" + id);
		}

		String realId = this.parse(id).getKey().toString();

		// Find it from the local store first

		ClusteredSession<O> session = cast(this.sessions.get(realId));

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
			if (logger.isDebugEnabled())
				logger.debug("Checking for session " + realId + " in the distributed cache");

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
			if (logger.isDebugEnabled())
				logger.debug("Updating session " + realId + " from the distributed cache");

			// Need to update it from the cache
			session = loadSession(realId);
			if (session == null)
			{
				// We have a session locally but it's no longer available
				// from the distributed store; i.e. it's been invalidated elsewhere
				// So we need to clean up
				// TODO what about notifications?
				this.sessions.remove(realId);
			}
		}

		if (session != null)
		{
			// Add this session to the set of those potentially needing replication
			ConvergedSessionReplicationContext.bindSession(session, snapshotManager);

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
	 * <p>
	 * Overrides the superclass version to ensure that the generated id
	 * does not duplicate the id of any other session this manager is aware of.
	 * </p>
	 */
	/*@Override
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
   }*/

	protected int getTotalActiveSessions()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getTotalActiveSessions");
		}
		return localActiveCounter.get() + unloadedSessions_.size() - passivatedCount_.get();
	}

	protected int getTotalActiveSipApplicationSessions()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getTotalActiveSipApplicationSessions");
		}
		//      return localActiveSipApplicationSessionCounter_.get() + unloadedSipApplicationSessions_.size() - passivatedSipApplicationSessionCount_.get();
		return localActiveSipApplicationSessionCounter_.get() + unloadedSipApplicationSessions_.size();
	}

	protected int getTotalActiveSipSessions()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getTotalActiveSipSessions");
		}
		//      return localActiveSipSessionCounter_.get() + unloadedSipSessions_.size() - passivatedSipSessionCount_.get();
		return localActiveSipSessionCounter_.get() + unloadedSipSessions_.size();
	}

	@Override
	public void resetStats()
	{
		if (logger.isDebugEnabled()){
			logger.debug("resetStats");
		}
		super.resetStats();

		this.maxPassivatedCount_.set(this.passivatedCount_.get());
		this.maxPassivatedSipSessionCount_.set(this.passivatedSipSessionCount_.get());
		this.maxPassivatedSipApplicationSessionCount_.set(this.passivatedSipApplicationSessionCount_.get());
	}

	protected Map<String, OwnedSessionUpdate> getUnloadedSessions()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getUnloadedSessions");
		}
		Map<String, OwnedSessionUpdate> unloaded = new HashMap<String, OwnedSessionUpdate>(unloadedSessions_);
		return unloaded;
	}

	protected Map<String, OwnedSessionUpdate> getUnloadedSipApplicationSessions()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getUnloadedSipApplicationSessions");
		}
		Map<String, OwnedSessionUpdate> unloaded = new HashMap<String, OwnedSessionUpdate>(unloadedSipApplicationSessions_);
		return unloaded;
	}

	protected Map<ClusteredSipSessionKey, OwnedSessionUpdate> getUnloadedSipSessions()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getUnloadedSipSessions");
		}
		Map<ClusteredSipSessionKey, OwnedSessionUpdate> unloaded = new HashMap<ClusteredSipSessionKey, OwnedSessionUpdate>(unloadedSipSessions_);
		return unloaded;
	}

	/**
	 * Gets the ids of all sessions in the distributed cache and adds
	 * them to the unloaded sessions map, along with their lastAccessedTime
	 * and their maxInactiveInterval. Passivates overage or excess sessions.
	 */
	protected void initializeUnloadedSessions() {    
		if (logger.isDebugEnabled()){
			logger.debug("initializeUnloadedSessions");
		}
		Map<String, String> sessions = getDistributedCacheManager().getSessionIds();
		if (sessions != null)
		{
			boolean passivate = isPassivationEnabled();

			long passivationMax = this.getPassivationMaxIdleTime() * 1000L;
			long passivationMin = this.getPassivationMinIdleTime() * 1000L;

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
					logger.debug("Problem reading metadata for session " + realId + " -- " + e.toString());               
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
							if (logger.isDebugEnabled())
							{
								logger.debug("Elapsed time of " + elapsed + " for session "+ 
										realId + " exceeds max of " + passivationMax + "; passivating");
							}
							processUnloadedSessionPassivation(realId, osu);
						}
						// If the session didn't exceed the passivationMaxIdleTime_, see   
						// if the number of sessions managed by this manager greater than the max allowed 
						// active sessions, passivate the session if it exceed passivationMinIdleTime_ 
						else if (maxActiveAllowed > 0 
								&& passivationMin >= 0 
								&& calcActiveSessions() > maxActiveAllowed 
								&& elapsed >= passivationMin)
						{
							if (logger.isDebugEnabled())
							{
								logger.debug("Elapsed time of " + elapsed + " for session "+ 
										realId + " exceeds min of " + passivationMin + "; passivating");
							}
							processUnloadedSessionPassivation(realId, osu);
						}
					}
					catch (Exception e)
					{
						// most likely a lock conflict if the session is being updated remotely; ignore it
						logger.debug("Problem passivating session " + realId + " -- " + e.toString());
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
		if (logger.isDebugEnabled()){
			logger.debug("listSessionIds");
		}
		Set<String> ids = new HashSet<String>(this.sessions.keySet());
		ids.addAll(unloadedSessions_.keySet());
		return reportSessionIds(ids);
	}

	private String reportSessionIds(Set<String> ids)
	{
		if (logger.isDebugEnabled()){
			logger.debug("reportSessionIds");
		}
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
		if (logger.isDebugEnabled()){
			logger.debug("notifyRemoteInvalidation");
		}
		// Remove the session from our local map
		ClusteredSession<O> session = (ClusteredSession<O>)this.sessions.remove(realId);
		if (session == null)
		{
			// We weren't managing the session anyway.  But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSessions_.remove(realId) != null)
			{
				if (logger.isDebugEnabled())
					logger.debug("Removed entry for session " + realId + " from unloaded session map");
			}

			// If session has failed over and has been passivated here,
			// session will be null, but we'll have a TimeStatistic to clean up
			this.getReplicationStatistics().removeStats(realId);
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
			//ContextClassLoaderSwitcher.SwitchContext switcher = null;         
			try
			{
				// Don't track this invalidation is if it were from a request
				SessionInvalidationTracker.suspend();

				//switcher = getContextClassLoaderSwitcher().getSwitchContext();
				//switcher.setClassLoader(getApplicationClassLoader());
				session.expire(notify, localCall, localOnly, ClusteredSessionNotificationCause.INVALIDATE);
			}
			finally
			{
				SessionInvalidationTracker.resume();

				// Remove any stats for this session
				this.getReplicationStatistics().removeStats(realId);

				//if (switcher != null)
				//{
				//   switcher.reset();
				//}
			}
		}
	}

	@Override
	protected void processExpirationPassivation() {
		if (logger.isDebugEnabled()){
			logger.debug("processExpirationPassivation");
		}
		processHttpSessionExpirationPassivation();
		processSipSessionExpirationPassivation();
		processSipApplicationSessionExpirationPassivation();
	}

	void relinquishSessionOwnership(String realId, boolean remove) {
		if (logger.isDebugEnabled()){
			logger.debug("relinquishSessionOwnership");
		}
		
		SessionOwnershipSupport support = this.getDistributedCacheManager().getSessionOwnershipSupport();

		if (support != null) {
			support.relinquishSessionOwnership(realId, remove);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processHttpSessionExpirationPassivation()
	{      
		if (logger.isDebugEnabled()){
			logger.debug("processHttpSessionExpirationPassivation");
		}
		
		boolean expire = maxInactiveInterval >= 0;
		boolean passivate = isPassivationEnabled();

		long passivationMax = getPassivationMaxIdleTime() * 1000L;
		long passivationMin = getPassivationMinIdleTime() * 1000L;

		logger.trace("processExpirationPassivation(): Looking for sessions that have expired ...");
		logger.tracef("processExpirationPassivation(): active sessions = %d", calcActiveSessions());
		logger.tracef("processExpirationPassivation(): expired sessions = %d", expiredCounter.get());
		if (passivate) {
			logger.tracef("processExpirationPassivation(): passivated count = %d", getPassivatedSessionCount());
		}

		// Holder for sessions or OwnedSessionUpdates that survive expiration,
		// sorted by last accessed time
		TreeSet<PassivationCheck> passivationChecks = new TreeSet<PassivationCheck>();

		try {
			// Don't track sessions invalidated via this method as if they
			// were going to be re-requested by the thread
			SessionInvalidationTracker.suspend();

			if (this.valveLock.tryLock(0, TimeUnit.SECONDS)) {
				try {
					// First, handle the sessions we are actively managing
					for (Session s: this.sessions.values()) {

						//if (!backgroundProcessAllowed.get()) {
						//return;
						//}

						if (!this.started) return;

						boolean likelyExpired = false;
						String realId = null;

						try {
							ClusteredSession<O> session = cast(s);

							realId = session.getRealId();
							likelyExpired = expire;

							if (expire) {
								// JBAS-2403. Check for outdated sessions where we think
								// the local copy has timed out. If found, refresh the
								// session from the cache in case that might change the timeout
								likelyExpired = (session.isValid(false) == false);
								if (likelyExpired && this.outdatedSessionChecker.isSessionOutdated(session)) {
									// With JBC, every time we get a notification from the distributed
									// cache of an update, we get the latest timestamp. So
									// we shouldn't need to do a full session load here. A load
									// adds a risk of an unintended data gravitation. However,
									// with a database instead of JBC we don't get notifications

									// JBAS-2792 don't assign the result of loadSession to session
									// just update the object from the cache or fall through if
									// the session has been removed from the cache
									try {
										loadSession(session.getRealId());
									} finally {
										// BZ 990092 loadSession(...) will have started a batch - so we need to end it here
										BatchingManager bm = getDistributedCacheManager().getBatchingManager();
										if (bm.isBatchInProgress()) {
											if (logger.isDebugEnabled()){
												logger.debug("processHttpSessionExpirationPassivation - call setBatchRollbackOnly");
											}
											bm.setBatchRollbackOnly();
											if (logger.isDebugEnabled()){
												logger.debug("processHttpSessionExpirationPassivation - call endBatch");
											}
											bm.endBatch();
										}
									}
								}

								// Do a normal invalidation check that will expire the
								// session if it has timed out
								// DON'T SYNCHRONIZE on session here -- isValid() and
								// expire() are meant to be multi-threaded and synchronize
								// properly internally; synchronizing externally can lead
								// to deadlocks!!
								if (!session.isValid()) {
									// BZ 1039585 - Clustered session memory leaking
									//session.relinquishSessionOwnership(true);
									this.relinquishSessionOwnership(session.getRealId(), true);
									continue;
								}

								likelyExpired = false;
							}

							// we now have a valid session; store it so we can check later
							// if we need to passivate it
							if (passivate) {
								passivationChecks.add(new PassivationCheck(session));
							}

						} catch (Exception e) {
							if (likelyExpired) {
								// JBAS-7397 clean up
								bruteForceCleanup(realId, e);
							} else {
								logger.error(MESSAGES.failToPassivateLoad(realId), e);
							}

						}
					}

					//if (!backgroundProcessAllowed.get()) {
					//return;
					//}

					// Next, handle any unloaded sessions

					// We may have not gotten replication of a timestamp for requests
					// that occurred w/in maxUnreplicatedInterval of the previous
					// request. So we add a grace period to avoid flushing a session early
					// and permanently losing part of its node structure in JBoss Cache.
					long maxUnrep = getMaxUnreplicatedInterval() < 0 ? 60 : getMaxUnreplicatedInterval();

					for (Map.Entry<String, OwnedSessionUpdate> entry : this.unloadedSessions_.entrySet()) {
						//if (!backgroundProcessAllowed.get()) {
						//return;
						//}

						if (!this.started) return;

						String realId = entry.getKey();
						OwnedSessionUpdate osu = entry.getValue();
						boolean likelyExpired = false;

						long now = System.currentTimeMillis();
						long elapsed = (now - osu.getUpdateTime());
						try {
							likelyExpired = expire && osu.getMaxInactive() >= 1 && elapsed >= (osu.getMaxInactive() + maxUnrep) * 1000L;
							if (likelyExpired) {
								// if (osu.passivated && osu.owner == null)
									if (osu.isPassivated()) {
										// Passivated session needs to be expired. A call to
										// findSession will bring it out of passivation
										Session session = null;
										try {
											session = findSession(realId);
										} finally {
											// BZ 993559 loadSession(...) will have started a batch to bring the session out of passivation - so we need to end it here
											BatchingManager bm = getDistributedCacheManager().getBatchingManager();
											if (bm.isBatchInProgress()) {
												if (logger.isDebugEnabled()){
													logger.debug("processHttpSessionExpirationPassivation - call setBatchRollbackOnly");
												}
												bm.setBatchRollbackOnly();
												if (logger.isDebugEnabled()){
													logger.debug("processHttpSessionExpirationPassivation - call endBatch");
												}
												bm.endBatch();
											}
										}
										if (session != null) {
											session.isValid(); // will expire
											continue;
										}
									}

									// If we get here either !osu.passivated, or we don't own
									// the session or the session couldn't be reactivated (invalidated by user).
									// Either way, do a cleanup
									getDistributedCacheManager().removeSessionLocal(realId, osu.getOwner());
									unloadedSessions_.remove(realId);
									this.getReplicationStatistics().removeStats(realId);

							} else if (passivate && !osu.isPassivated()) {
								// we now have a valid session; store it so we can check later
								// if we need to passivate it
								passivationChecks.add(new PassivationCheck(realId, osu));
							}
						} catch (Exception e) {
							// JBAS-7397 Don't try forever
							if (likelyExpired) {
								// JBAS-7397
								bruteForceCleanup(realId, e);
							} else {
								logger.error(MESSAGES.failToPassivateUnloaded(realId), e);
							}
						}
					}

					if (!this.started) return;

					// Now, passivations
					if (passivate) {
						// Iterate through sessions, earliest lastAccessedTime to latest
						for (PassivationCheck passivationCheck : passivationChecks) {
							try {
								long timeNow = System.currentTimeMillis();
								long timeIdle = timeNow - passivationCheck.getLastUpdate();
								// if maxIdle time configured, means that we need to passivate sessions that have
								// exceeded the max allowed idle time
								if (passivationMax >= 0 && timeIdle > passivationMax) {
									passivationCheck.passivate();
								}
								// If the session didn't exceed the passivationMaxIdleTime_, see
								// if the number of sessions managed by this manager greater than the max allowed
								// active sessions, passivate the session if it exceed passivationMinIdleTime_
								else if ((maxActiveAllowed > 0) && (passivationMin > 0) && (calcActiveSessions() >= maxActiveAllowed) && (timeIdle > passivationMin)) {
									passivationCheck.passivate();
								} else {
									// the entries are ordered by lastAccessed, so once
									// we don't passivate one, we won't passivate any
									break;
								}
							} catch (Exception e) {
								logger.error(MESSAGES.failToPassivate(passivationCheck.isUnloaded() ? "unloaded " : "", passivationCheck.getRealId()), e);
							}
						}
					}
				} finally {
					this.valveLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			logger.error(MESSAGES.processExpirationPassivationException(ex.getLocalizedMessage()), ex);
		} finally {
			SessionInvalidationTracker.resume();
		}

		logger.trace("processExpirationPassivation(): Completed ...");
		logger.tracef("processExpirationPassivation(): active sessions = %d", calcActiveSessions());
		logger.tracef("processExpirationPassivation(): expired sessions = %d", expiredCounter.get());
		if (passivate) {
			logger.tracef("processExpirationPassivation(): passivated count = %d", getPassivatedSessionCount());
		}
	}

	private void bruteForceCleanup(String realId, Exception ex) {
		if (logger.isDebugEnabled()){
			logger.debug("bruteForceCleanup");
		}
		
		logger.warn(MESSAGES.bruteForceCleanup(realId, ex.toString()));
		try {
			this.getDistributedCacheManager().removeSessionLocal(realId, null);
		} catch (Exception e) {
			logger.error(MESSAGES.failToBruteForceCleanup(realId), e);
		} finally {
			// Get rid of our refs even if distributed store fails
			unloadedSessions_.remove(realId);
			this.getReplicationStatistics().removeStats(realId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processSipSessionExpirationPassivation() {
		if (logger.isDebugEnabled()){
			logger.debug("processSipSessionExpirationPassivation");
		}
		
		boolean expire = maxInactiveInterval >= 0;
		boolean passivate = isPassivationEnabled();

		long passivationMax = getPassivationMaxIdleTime() * 1000L;
		long passivationMin = getPassivationMinIdleTime() * 1000L;

		logger.trace("processSipSessionExpirationPassivation(): Looking for sip sessions that have expired ...");
		logger.trace("processSipSessionExpirationPassivation(): passivation Max = " + passivationMax);
		logger.trace("processSipSessionExpirationPassivation(): passivation Min = " + passivationMin);
		logger.trace("processSipSessionExpirationPassivation(): active sip sessions = " + calcActiveSipSessions());
		logger.trace("processSipSessionExpirationPassivation(): expired sip sessions = " + expiredSipSessionCounter_);
		if (passivate) {
			logger.trace("processSipSessionExpirationPassivation(): passivated count = " + getPassivatedSipSessionCount());
		}

		// Holder for sessions or OwnedSessionUpdates that survive expiration,
		// sorted by last accessed time
		TreeSet<SipSessionPassivationCheck> passivationChecks = new TreeSet<SipSessionPassivationCheck>();

		try {
			// Don't track sessions invalidated via this method as if they
			// were going to be re-requested by the thread
			ConvergedSessionInvalidationTracker.suspend();

			if (this.valveLock.tryLock(0, TimeUnit.SECONDS)) {
				try {
					// First, handle the sessions we are actively managing
					ClusteredSipSession<? extends OutgoingDistributableSessionData>[] sessions = findLocalSipSessions(); 
					for (ClusteredSipSession<? extends OutgoingDistributableSessionData> session: sessions) {

						//if (!backgroundProcessAllowed.get()) {
						//return;
						//}

						if (!this.started) return;

						boolean likelyExpired = false;
						String realId = null;

						try {
							//ClusteredSipSession<O> session = cast(s);

							likelyExpired = expire;

							if (expire) {
								// JBAS-2403. Check for outdated sessions where we think
								// the local copy has timed out. If found, refresh the
								// session from the cache in case that might change the timeout
								//likelyExpired = (session.isValidInternal(false) == false);
								if (session.isValidInternal() && this.outdatedSipSessionChecker.isSessionOutdated(session)) {
									// With JBC, every time we get a notification from the distributed
									// cache of an update, we get the latest timestamp. So
									// we shouldn't need to do a full session load here. A load
									// adds a risk of an unintended data gravitation. However,
									// with a database instead of JBC we don't get notifications

									// JBAS-2792 don't assign the result of loadSession to session
									// just update the object from the cache or fall through if
									// the session has been removed from the cache
									try {
										//loadSipSession(session.getRealId());
										loadSipSession(session.getKey(), false, sipManagerDelegate.getSipFactoryImpl(), session.getSipApplicationSession());
									} finally {
										// BZ 990092 loadSession(...) will have started a batch - so we need to end it here
										BatchingManager bm = getDistributedCacheManager().getBatchingManager();
										if (bm.isBatchInProgress()) {
											if (logger.isDebugEnabled()){
												logger.debug("processSipSessionExpirationPassivation - call setBatchRollbackOnly");
											}
											bm.setBatchRollbackOnly();
											if (logger.isDebugEnabled()){
												logger.debug("processSipSessionExpirationPassivation - call endBatch");
											}
											bm.endBatch();
										}
									}
								}

								// Do a normal invalidation check that will expire the
								// session if it has timed out
								// DON'T SYNCHRONIZE on session here -- isValid() and
								// expire() are meant to be multi-threaded and synchronize
								// properly internally; synchronizing externally can lead
								// to deadlocks!!
								if (!session.isValidInternal()) {
									// BZ 1039585 - Clustered session memory leaking
									//session.relinquishSessionOwnership(true);
									this.relinquishSessionOwnership(session.getHaId(), true);
									continue;
								}

								likelyExpired = false;
							}

							// we now have a valid session; store it so we can check later
							// if we need to passivate it
							if (passivate) {
								passivationChecks.add(new SipSessionPassivationCheck(session));
							}

						} catch (Exception e) {
							if (likelyExpired) {
								// JBAS-7397 clean up
								bruteForceCleanup(realId, e);
							} else {
								logger.error(MESSAGES.failToPassivateLoad(realId), e);
							}

						}
					}

					//if (!backgroundProcessAllowed.get()) {
					//return;
					//}

					// Now, passivations
					// Passivation is not supported yet
					/*if (passivate) {
                       // Iterate through sessions, earliest lastAccessedTime to latest
                       for (SipSessionPassivationCheck passivationCheck : passivationChecks) {
                           try {
                               long timeNow = System.currentTimeMillis();
                               long timeIdle = timeNow - passivationCheck.getLastUpdate();
                               // if maxIdle time configured, means that we need to passivate sessions that have
                               // exceeded the max allowed idle time
                               if (passivationMax >= 0 && timeIdle > passivationMax) {
                                   passivationCheck.passivate();
                               }
                               // If the session didn't exceed the passivationMaxIdleTime_, see
                               // if the number of sessions managed by this manager greater than the max allowed
                               // active sessions, passivate the session if it exceed passivationMinIdleTime_
                               else if ((maxActiveAllowed > 0) && (passivationMin > 0) && (calcActiveSipSessions() >= maxActiveAllowed) && (timeIdle > passivationMin)) {
                                   passivationCheck.passivate();
                               } else {
                                   // the entries are ordered by lastAccessed, so once
                                   // we don't passivate one, we won't passivate any
                                   break;
                               }
                           } catch (Exception e) {
                               logger.error(MESSAGES.failToPassivate(passivationCheck.isUnloaded() ? "unloaded " : "", passivationCheck.getRealId()), e);
                           }
                       }
                   }*/
				} finally {
					this.valveLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			logger.error(MESSAGES.processExpirationPassivationException(ex.getLocalizedMessage()), ex);
		} finally {
			SessionInvalidationTracker.resume();
		}

		if (logger.isDebugEnabled()) { 
			logger.trace("processSipSessionExpirationPassivation(): Completed ...");
			logger.trace("processSipSessionExpirationPassivation(): active sip sessions = " + calcActiveSipSessions());
			logger.trace("processSipSessionExpirationPassivation(): expired sip sessions = " + expiredSipSessionCounter_);
			if (passivate) {
				logger.trace("processSipSessionExpirationPassivation(): passivated count = " + getPassivatedSipSessionCount());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected void processSipApplicationSessionExpirationPassivation()
	{      
		if (logger.isDebugEnabled()){
			logger.debug("processSipApplicationSessionExpirationPassivation");
		}
		
		boolean expire = maxInactiveInterval >= 0;
		boolean passivate = isPassivationEnabled();

		long passivationMax = getPassivationMaxIdleTime() * 1000L;
		long passivationMin = getPassivationMinIdleTime() * 1000L;

		if (logger.isDebugEnabled())
		{ 
			logger.trace("processSipApplicationSessionExpirationPassivation(): Looking for sip application sessions that have expired ...");
			logger.trace("processSipApplicationSessionExpirationPassivation(): active sip application sessions = " + calcActiveSipApplicationSessions());
			logger.trace("processSipApplicationSessionExpirationPassivation(): expired sip application sessions = " + expiredSipApplicationSessionCounter_);
			if (passivate)
			{
				logger.trace("processSipApplicationSessionExpirationPassivation(): passivated count = " + getPassivatedSipApplicationSessionCount());
			}
		}

		// Holder for sessions or OwnedSessionUpdates that survive expiration,
		// sorted by last accessed time
		TreeSet<SipApplicationSessionPassivationCheck> passivationChecks = new TreeSet<SipApplicationSessionPassivationCheck>();

		try {
			// Don't track sessions invalidated via this method as if they
			// were going to be re-requested by the thread
			SessionInvalidationTracker.suspend();

			if (this.valveLock.tryLock(0, TimeUnit.SECONDS)) {
				try {
					// First, handle the sessions we are actively managing
					ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>[] sessions = findLocalSipApplicationSessions(); 
					for (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session: sessions) {

						//if (!backgroundProcessAllowed.get()) {
						//return;
						//}

						if (!this.started) return;

						boolean likelyExpired = false;
						String realId = null;

						try {
							//ClusteredSipApplicationSession<O> session = cast(s);

							likelyExpired = expire;

							if (expire) {
								// JBAS-2403. Check for outdated sessions where we think
								// the local copy has timed out. If found, refresh the
								// session from the cache in case that might change the timeout

								//likelyExpired = (session.isValid(false) == false);
								if (session.isValidInternal() && this.outdatedSipApplicationSessionChecker.isSessionOutdated(session)) {
									// With JBC, every time we get a notification from the distributed
									// cache of an update, we get the latest timestamp. So
									// we shouldn't need to do a full session load here. A load
									// adds a risk of an unintended data gravitation. However,
									// with a database instead of JBC we don't get notifications

									// JBAS-2792 don't assign the result of loadSession to session
									// just update the object from the cache or fall through if
									// the session has been removed from the cache
									try {
										//loadSession(session.getRealId());
										loadSipApplicationSession(session.getKey(), false);
									} finally {
										// BZ 990092 loadSession(...) will have started a batch - so we need to end it here
										BatchingManager bm = getDistributedCacheManager().getBatchingManager();
										if (bm.isBatchInProgress()) {
											if (logger.isDebugEnabled()){
												logger.debug("processSipApplicationSessionExpirationPassivation - call setBatchRollbackOnly");
											}
											bm.setBatchRollbackOnly();
											if (logger.isDebugEnabled()){
												logger.debug("processSipApplicationSessionExpirationPassivation - call endBatch");
											}
											bm.endBatch();
										}
									}
								}

								// Do a normal invalidation check that will expire the
								// session if it has timed out
								// DON'T SYNCHRONIZE on session here -- isValid() and
								// expire() are meant to be multi-threaded and synchronize
								// properly internally; synchronizing externally can lead
								// to deadlocks!!
								if (!session.isValidInternal()) {
									// BZ 1039585 - Clustered session memory leaking
									//session.relinquishSessionOwnership(true);
									this.relinquishSessionOwnership(session.getHaId(), true);
									continue;
								}

								likelyExpired = false;
							}

							// we now have a valid session; store it so we can check later
							// if we need to passivate it
							if (passivate) {
								passivationChecks.add(new SipApplicationSessionPassivationCheck(session));
							}

						} catch (Exception e) {
							if (likelyExpired) {
								// JBAS-7397 clean up
								bruteForceCleanup(realId, e);
							} else {
								logger.error(MESSAGES.failToPassivateLoad(realId), e);
							}

						}
					}

					//if (!backgroundProcessAllowed.get()) {
					//return;
					//}

					// Now, passivations
					// Passivation is not supported yet
					/*if (passivate) {
                       // Iterate through sessions, earliest lastAccessedTime to latest
                       for (SipApplicationSessionPassivationCheck passivationCheck : passivationChecks) {
                           try {
                               long timeNow = System.currentTimeMillis();
                               long timeIdle = timeNow - passivationCheck.getLastUpdate();
                               // if maxIdle time configured, means that we need to passivate sessions that have
                               // exceeded the max allowed idle time
                               if (passivationMax >= 0 && timeIdle > passivationMax) {
                                   passivationCheck.passivate();
                               }
                               // If the session didn't exceed the passivationMaxIdleTime_, see
                               // if the number of sessions managed by this manager greater than the max allowed
                               // active sessions, passivate the session if it exceed passivationMinIdleTime_
                               else if ((maxActiveAllowed > 0) && (passivationMin > 0) && (calcActiveSipApplicationSessions() >= maxActiveAllowed) && (timeIdle > passivationMin)) {
                                   passivationCheck.passivate();
                               } else {
                                   // the entries are ordered by lastAccessed, so once
                                   // we don't passivate one, we won't passivate any
                                   break;
                               }
                           } catch (Exception e) {
                               logger.error(MESSAGES.failToPassivate(passivationCheck.isUnloaded() ? "unloaded " : "", passivationCheck.getRealId()), e);
                           }
                       }
                   }*/
				} finally {
					this.valveLock.unlock();
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception ex) {
			logger.error(MESSAGES.processExpirationPassivationException(ex.getLocalizedMessage()), ex);
		} finally {
			ConvergedSessionInvalidationTracker.resume();
		}

		logger.trace("processExpirationPassivation(): Completed ...");
		logger.tracef("processExpirationPassivation(): active sessions = %d", calcActiveSessions());
		logger.tracef("processExpirationPassivation(): expired sessions = %d", expiredCounter.get());
		if (passivate) {
			logger.tracef("processExpirationPassivation(): passivated count = %d", getPassivatedSessionCount());
		}
	}

	/**
	 * Session passivation logic for an actively managed session.
	 * 
	 * @param realId the session id, minus any jvmRoute
	 */
	private void processSessionPassivation(String realId)
	{
		if (logger.isDebugEnabled()){
			logger.debug("processSessionPassivation");
		}
		
		// FIXME: Passivation is not yet supported

		// get the session from the local map
		ClusteredSession<O> session = cast(this.sessions.get(realId));;
		// Remove actively managed session and add to the unloaded sessions
		// if it's already unloaded session (session == null) don't do anything, 
		if (session != null)
		{
			synchronized (session)
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("processSessionPassivation - Passivating session with id: " + realId);
				}

				session.notifyWillPassivate(ClusteredSessionNotificationCause.PASSIVATION);
				getDistributedCacheManager().evictSession(realId);
				sessionPassivated();

				// Put the session in the unloadedSessions map. This will
				// expose the session to regular invalidation.
				Object obj = unloadedSessions_.put(realId, 
						new OwnedSessionUpdate(null, session.getLastAccessedTimeInternal(), session.getMaxInactiveInterval(), true));
				if (logger.isDebugEnabled())
				{
					if (obj == null)
					{
						logger.debug("processSessionPassivation - New session with id: " + realId + " added to unloaded session map");
					}
					else
					{
						logger.debug("processSessionPassivation - Updated timestamp for unloaded session with id: " + realId);
					}
				}
				this.sessions.remove(realId);
			}
		}
		else if (logger.isDebugEnabled())
		{
			logger.debug("processSessionPassivation():  could not find session " + realId);
		}
	}

	/**
	 * Session passivation logic for sessions only in the distributed store.
	 * 
	 * @param realId the session id, minus any jvmRoute
	 */
	private void processUnloadedSessionPassivation(String realId, OwnedSessionUpdate osu)
	{
		if (logger.isDebugEnabled()){
			logger.debug("processSessionPassivation");
		}
		
		if (logger.isDebugEnabled())
		{
			logger.debug("Passivating session with id: " + realId);
		}

		getDistributedCacheManager().evictSession(realId, osu.getOwner());
		osu.setPassivated(true);
		sessionPassivated();      
	}

	public void sessionActivated()
	{
		if (logger.isDebugEnabled()){
			logger.debug("sessionActivated");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("sessionPassivated");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("clearSessions");
		}
		
		boolean passivation = isPassivationEnabled();
		// First, the sessions we have actively loaded
		//ClusteredSession<O>[] sessions = findLocalSessions();
		//for(int i=0; i < sessions.length; i++)
		for (Session session: this.sessions.values())
		{
			ClusteredSession<O> ses = cast(session);
			//ClusteredSession<? extends OutgoingDistributableSessionData> ses = sessions[i];

			if (logger.isDebugEnabled())
			{
				logger.debug("clearSessions(): clear session by expiring or passivating: " + ses);
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
				logger.warn("clearSessions(): Caught exception expiring or passivating session " +
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
				logger.debug("Problem " + action + " session " + realId + " -- " + e);
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
		if (logger.isDebugEnabled()){
			logger.debug("sessionChangedInDistributedCache - realId=" + realId + ", dataOwner=" + dataOwner);
		}
		
		boolean updated = true;

		ClusteredSession<O> session = cast(this.sessions.get(realId));
		if (session != null)
		{
			// Need to invalidate the loaded session. We get back whether
			// this an actual version increment
			updated = session.setVersionFromDistributedCache(distributedVersion);
			if (updated && logger.isDebugEnabled())      
			{            
				logger.debug("session in-memory data is invalidated for id: " + realId + 
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
				if (logger.isDebugEnabled())
				{
					logger.debug("sessionChangedInDistributedCache - New session with id: " + realId + " added to unloaded session map");
				}
			}
			else if (logger.isDebugEnabled())
			{
				logger.debug("sessionChangedInDistributedCache - Updated timestamp for unloaded session with id: " + realId);
			}
		}

		return updated;
	}

	@SuppressWarnings("unchecked")
	private static ContextClassLoaderSwitcher getContextClassLoaderSwitcher()
	{
		if (logger.isDebugEnabled()){
			logger.debug("getContextClassLoaderSwitcher");
		}
		return (ContextClassLoaderSwitcher) AccessController.doPrivileged(ContextClassLoaderSwitcher.INSTANTIATOR);
	}

	/**
	 * Removes the session from this Manager's collection of actively managed
	 * sessions.  Also removes the session from the distributed cache, both
	 * on this server and on all other server to which this one replicates.
	 */
	public void remove(Session session)
	{
		if (logger.isDebugEnabled()){
			logger.debug("remove - session.getId()=" + session.getId());
		}
		
		ClusteredSession<? extends OutgoingDistributableSessionData> clusterSess = uncheckedCastSession(session);
		synchronized (clusterSess)
		{
			String realId = clusterSess.getRealId();
			if (realId == null)
				return;

			if (logger.isDebugEnabled())
			{
				logger.debug("Removing session from store with id: " + realId);
			}

			try {
				clusterSess.removeMyself();
			}
			finally {
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sessionExpired(clusterSess, realId, snapshotManager);

				// Track this session to prevent reincarnation by this request 
				// from the distributed cache
				SessionInvalidationTracker.sessionInvalidated(realId, this);

				this.sessions.remove(realId);
				this.getReplicationStatistics().removeStats(realId);

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
		if (logger.isDebugEnabled()){
			logger.debug("removeLocal - session.getId()=" + session.getId());
		}
		
		@SuppressWarnings("unchecked")
		ClusteredSipSession<O> clusterSess = (ClusteredSipSession<O>) uncheckedCastSipSession(session);
		synchronized (clusterSess)
		{
			SipSessionKey key = clusterSess.getKey();
			if (key == null) return;

			if (logger.isDebugEnabled())
			{
				logger.debug("Removing sip session from local store with id: " + key);
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
				this.getReplicationStatistics().removeStats(key.toString());

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
		if (logger.isDebugEnabled()){
			logger.debug("removeLocal (SipApplicationSession) - session.getId()=" + session.getId());
		}
		
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusterSess = uncheckedCastSipApplicationSession(session);
		synchronized (clusterSess)
		{
			SipApplicationSessionKey key = clusterSess.getKey();
			if (key == null) return;

			if (logger.isDebugEnabled())
			{
				logger.debug("Removing sip application session from local store with id: " + key);
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
				this.getReplicationStatistics().removeStats(key.toString());

				// Compute how long this session has been alive, and update
				// our statistics accordingly
				//            int timeAlive = (int) ((System.currentTimeMillis() - clusterSess.getCreationTimeInternal())/1000);
				//            sipApplicationSessionExpired(timeAlive);
			}
		}
	}

	@Override
	public boolean storeSipSession(ClusteredSipSession<? extends OutgoingDistributableSessionData> session) {
		if (logger.isDebugEnabled()){
			logger.debug("storeSipSession - session.getId()=" + session.getId());
		}
		
		boolean stored = false;
		if (session != null && started) {			

			synchronized (session) {				
				if (logger.isDebugEnabled()) {
					boolean isValidInternal = session.isValidInternal();
					String message = "check to see if needs to store and replicate "
						+ "session with id=" + session.getId() + ", isValid=" + isValidInternal; 
					if(isValidInternal) {
						message = message + ", getMustReplicateTimestamp=" + session.getMustReplicateTimestamp() + 
						", session state=" + session.getState() + ", session.isSessionDirty()=" + session.isSessionDirty() + ", replicationStrategy=" + ((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).getReplicationStrategy();
					}
					logger.debug(message);
				}

				if (session.isValidInternal()
							&& (session.isSessionDirty() || session.getMustReplicateTimestamp()) 
							&& 
							(State.CONFIRMED.equals(session.getState())
						||
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
					this.getReplicationStatistics().updatePassivationStats(realId, elapsed);

					// Do the actual replication
					begin = System.currentTimeMillis();
					processSipSessionRepl(session);
					elapsed = System.currentTimeMillis() - begin;
					stored = true;
					if (logger.isDebugEnabled()){
						logger.debug("storeSipSession - calling updateReplicationStats with realId=" + realId + ", elapsed=" + elapsed + " (session.getId()=" + session.getId() + ")");
					}
					this.getReplicationStatistics().updateReplicationStats(realId, elapsed);
				}
			}
		}
		if (logger.isDebugEnabled()){
			logger.debug("storeSipSession - returning with stored=" + stored + " (session.getId()=" + session.getId() + ")");
		}

		return stored;
	}

	@Override
	public boolean storeSipApplicationSession(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session) {
		if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSession - session.getId()=" + session.getId());
		}
		
		boolean stored = false;
		if (session != null && started) {					
			synchronized (session) {
				if (logger.isDebugEnabled()) {
					logger.debug("check to see if needs to store and replicate "
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
					this.getReplicationStatistics().updatePassivationStats(realId, elapsed);

					// Do the actual replication
					begin = System.currentTimeMillis();
					processSipApplicationSessionRepl(session);		
					elapsed = System.currentTimeMillis() - begin;
					stored = true;
					if (logger.isDebugEnabled()){
						logger.debug("storeSipApplicationSession - calling updateReplicationStats with realId=" + realId + ", elapsed=" + elapsed + " (session.getId()=" + session.getId() + ")");
					}
					this.getReplicationStatistics().updateReplicationStats(realId, elapsed);
				}
			}
		}

		if (logger.isDebugEnabled()){
			logger.debug("storeSipApplicationSession - returning with stored=" + stored + " (session.getId()=" + session.getId() + ")");
		}
		return stored;
	}

	public void add(SipSession session) {
		if (logger.isDebugEnabled()){
			logger.debug("add (SipSession) - session.getId()=" + session.getId());
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("add (SipApplicationSession) - session.getId()=" + session.getId());
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("add(ClusteredSipSession) - session.getId()=" + session.getId() + ", replicate=" + replicate);
		}
		
		// TODO -- why are we doing this check? The request checks session 
		// validity and will expire the session; this seems redundant
		if (!session.isValidInternal())
		{
			// Not an error; this can happen if a failover request pulls in an
			// outdated session from the distributed cache (see TODO above)
			logger.debug("Cannot add session with id=" + session.getKey() +
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

			if (logger.isDebugEnabled())
			{
				logger.debug("Session with id=" + session.getKey() + " added. " +
						"Current active sessions " + localActiveCounter.get());
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
		if (logger.isDebugEnabled()){
			logger.debug("add(ClusteredSipApplicationSession) - session.getId()=" + session.getId() + ", replicate=" + replicate);
		}
		
		if (!session.isValidInternal())
		{
			// Not an error; this can happen if a failover request pulls in an
			// outdated session from the distributed cache (see TODO above)
			logger.debug("Cannot add session with id=" + session.getKey() +
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

			if (logger.isDebugEnabled())
			{
				logger.debug("Session with id=" + session.getKey() + " added. " +
						"Current active sessions " + localActiveCounter.get());
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
		if (logger.isDebugEnabled()){
			logger.debug("findLocalSipApplicationSessions");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("findLocalSipSessions");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("findLocalSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", create=" + create);
		}
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
		if (logger.isDebugEnabled()){
			logger.debug("findLocalSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey() + ", create=" + create);
		}
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
		if (logger.isDebugEnabled()){
			if (key != null){
				logger.debug("loadSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", key.getFromTag()=" + key.getFromTag() + ", create=" + create);
			} else {
				logger.debug("loadSipSession - key is null");
			}
		}
		
		if (key == null) {
			return null;
		}

		SipApplicationSessionKey applicationSessionKey = null;
		if(sipApplicationSessionImpl != null) {
			applicationSessionKey = (SipApplicationSessionKey) sipApplicationSessionImpl.getKey();
			if (logger.isDebugEnabled()){
				logger.debug("loadSipSession - sipApplicationSessionImpl not null - applicationSessionKey.getId()=" + applicationSessionKey.getId());
			}
		} else {
			applicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(key.getApplicationName(), key.getApplicationSessionId(),
					null); // TODO: is ok that the third parameter is null? In JBoss5 there were only 2 parameters.
			if (logger.isDebugEnabled()){
				logger.debug("loadSipSession - sipApplicationSessionImpl is null - applicationSessionKey.getId()=" + applicationSessionKey.getId());
			}
			
		}
		if(logger.isDebugEnabled()) {
			logger.debug("load sip session with id: " + key + ", create = " + create + ", sip app session id= "+ applicationSessionKey.getId());
		}
		MobicentsSipFactory sipFactory = sipFactoryImpl;
		if(sipFactory == null) {
			sipFactory = this.getMobicentsSipFactory();
		}
		long begin = System.currentTimeMillis();
		boolean mustAdd = false;
		boolean passivated = false;

		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = (ClusteredSipSession) sipManagerDelegate.getSipSession(key, create, (SipFactoryImpl) sipFactory, sipApplicationSessionImpl);
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
				if (logger.isDebugEnabled()){
					logger.debug("loadSipSession - call startBatch - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", create=" + create);
				}
				batchingManager.startBatch();
				doTx = true;
			}
			if(session == null) {
				if (logger.isDebugEnabled()){
					logger.debug("loadSipSession - setting initialLoad to true");
				}
				initialLoad = true;
			} else {
				session.updateThisAccessedTime();
			}
			
			IncomingDistributableSessionData data = getDistributedCacheConvergedSipManager().getSipSessionData(applicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key), initialLoad);
			if (data != null)
			{
				if(logger.isDebugEnabled()) {
					logger.debug("data for sip session " + key + " found in the distributed cache");
					logger.debug("loadSipSession - data.getMetadata()=" + data.getMetadata());
					if (data.getMetadata() != null && data.getMetadata() instanceof DistributableSipSessionMetadata){
						logger.debug("loadSipSession - metadata is instanceof DistributableSipSessionMetadata");
						if (((DistributableSipSessionMetadata)data.getMetadata()).getMetaData() != null){
							logger.debug("loadSipSession - metadata is instanceof DistributableSipSessionMetadata - metadata map not null");
							for (String tmpKey: ((DistributableSipSessionMetadata)data.getMetadata()).getMetaData().keySet()){
								logger.debug("loadSipSession - metadata map entry: " + tmpKey + "=" + ((DistributableSipSessionMetadata)data.getMetadata()).getMetaData().get(tmpKey));	
							}								
						}
					}
					
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
						((ClusteredSipManagerDelegate)sipManagerDelegate).getNewMobicentsSipSession(key, (SipFactoryImpl)sipFactory, sipApplicationSessionImpl, true);
						OwnedSessionUpdate osu = unloadedSipSessions_.get(key);
						passivated = (osu != null && osu.passivated);
						
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("beware null parent sip application for session " + key);
						}
					}
				} 
				if(session!= null) {
					if (logger.isDebugEnabled()){
						logger.debug("loadSipSession - session not null, calling session.update");
					}
					session.update(data);						
				}
			} else if(logger.isDebugEnabled()) {
				logger.debug("no data for sip session " + key + " in the distributed cache");
			}
			
		} catch (Exception ex) {
			try {
				// if(doTx)
				// Let's set it no matter what.
				if(logger.isDebugEnabled()) {
					logger.debug("loadSipSession - call setBatchRollbackOnly");
				}
				batchingManager.setBatchRollbackOnly();
			} catch (Exception exn) {
				logger.error("Problem rolling back session mgmt transaction",
						exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;

			throw new RuntimeException("Failed to load session " + key.toString(),
					ex);
		} finally {
			if (doTx) {
				if(logger.isDebugEnabled()) {
					logger.debug("loadSipSession - call endBatch");
				}
				batchingManager.endBatch();
			}
		}
		if (session != null) {
			if (logger.isDebugEnabled()){
				logger.debug("loadSipSession - session not null");
			}
			
			if (mustAdd) {
				if (logger.isDebugEnabled()){
					logger.debug("loadSipSession - session not null, mustAdd true");
				}
				
				unloadedSipSessions_.remove(key);				
				if (!passivated) {
					if (logger.isDebugEnabled()){
						logger.debug("loadSipSession - session not null, mustAdd true, passivated false");
					}
					
					session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
				}
			}
			long elapsed = System.currentTimeMillis() - begin;
			this.getReplicationStatistics().updateLoadStats(key.toString(), elapsed);

			if (logger.isDebugEnabled()) {
				logger
				.debug("loadSession(): id= " + key + ", session="
						+ session);
			}
			return session;
		} else if (logger.isDebugEnabled()) {
			logger.debug("loadSession(): session " + key
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
	protected ClusteredSipApplicationSession loadSipApplicationSession(final SipApplicationSessionKey key, final boolean create) {
		if (logger.isDebugEnabled()){
			logger.debug("loadSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey() + ", create=" + create);
		}
		
		if (key == null) {
			return null;
		}

		if(logger.isDebugEnabled()) {
			logger.debug("load sip application session " + key + ", create = " + create);
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
				if (logger.isDebugEnabled()){
					logger.debug("loadSipApplicationSession - call startBatch");
				}
				batchingManager.startBatch();
				doTx = true;
			}
			if(session == null) {
				initialLoad = true;
			} else {
				session.updateThisAccessedTime();
			}
			
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
					OwnedSessionUpdate osu = unloadedSipApplicationSessions_.get(key.getId());
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
				logger.error("Problem rolling back session mgmt transaction",	exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException){
				throw (RuntimeException) ex;
			}
			throw new RuntimeException("Failed to load session " + key, ex);
		} finally {
			if (doTx) {
				if (logger.isDebugEnabled()){
					logger.debug("loadSipApplicationSession - call endBatch");
				}
				batchingManager.endBatch();
			}
		}
		if (session != null) {
			if (mustAdd) {
				unloadedSipApplicationSessions_.remove(key.getId());	
				if (!passivated) {
					session.tellNew(ClusteredSessionNotificationCause.FAILOVER);
				}
			}
			long elapsed = System.currentTimeMillis() - begin;
			this.getReplicationStatistics().updateLoadStats(key.toString(), elapsed);

			if (logger.isDebugEnabled()) {
				logger.debug("loadSipApplicationSession(): id= " + key.toString()
						+ ", session=" + session);
			}
			return session;
		} else if (logger.isDebugEnabled()) {
			logger.debug("loadSipApplicationSession(): session " + key.toString()
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
		if (logger.isDebugEnabled()){
			logger.debug("processSipSessionRepl - session.getId()=" + session.getId());
		}
		
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
				if (logger.isDebugEnabled()){
					logger.debug("processSipSessionRepl - call startBatch");
				}
				batchingManager.startBatch();
				doTx = true;
			}

			session.processSipSessionReplication();
		} catch (Exception ex) {
			if (logger.isDebugEnabled())
				logger.debug("processSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					batchingManager.setBatchRollbackOnly();
			} catch (Exception exn) {
				logger.error("Caught exception rolling back transaction", exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;

			throw new RuntimeException(
					"JBossCacheManager.processSessionRepl(): "
					+ "failed to replicate session.", ex);
		} finally {
			if (doTx){
				if (logger.isDebugEnabled()){
					logger.debug("processSipSessionRepl - call endBatch");
				}
				batchingManager.endBatch();
			}
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
		if (logger.isDebugEnabled()){
			logger.debug("processSipApplicationSessionRepl - session.getId()=" + session.getId());
		}
		
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
				if (logger.isDebugEnabled()){
					logger.debug("processSipApplicationSessionRepl - starting TX (session.getId()=" + session.getId() + ")");
				}
				if (logger.isDebugEnabled()){
					logger.debug("processSipApplicationSessionRepl - call startBatch");
				}
				batchingManager.startBatch();
				doTx = true;
			}

			session.processSipApplicationSessionReplication();
		} catch (Exception ex) {
			if (logger.isDebugEnabled())
				logger.debug("processSipApplicationSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					batchingManager.setBatchRollbackOnly();
			} catch (Exception exn) {
				logger.error("Caught exception rolling back transaction", exn);
			}

			// We will need to alert Tomcat of this exception.
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;

			throw new RuntimeException(
					"JBossCacheSipManager.processSipApplicationSessionRepl(): "
					+ "failed to replicate session.", ex);
		} finally {
			if (logger.isDebugEnabled()){
				logger.debug("processSipApplicationSessionRepl - end (session.getId()=" + session.getId() + ")");
			}
			if (doTx){
				if (logger.isDebugEnabled()){
					logger.debug("processSipApplicationSessionRepl - ending TX (session.getId()=" + session.getId() + ")");
				}
				if (logger.isDebugEnabled()){
					logger.debug("processSipApplicationSessionRepl - call endBatch");
				}
				batchingManager.endBatch();
			}
		}
	}

	/**
	 * Session passivation logic for an actively managed session.
	 * 
	 * @param realId
	 *            the session id, minus any jvmRoute
	 */
	private void processSipSessionPassivation(SipSessionKey key) {
		if (logger.isDebugEnabled()){
			logger.debug("processSipSessionPassivation - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId());
		}
		
		
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
		if (logger.isDebugEnabled()){
			logger.debug("processSipApplicationSessionPassivation - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey());
		}
		
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
		if (logger.isDebugEnabled()){
			
			logger.debug("processUnloadedSipSessionPassivation - " 
					+ " key.getSipApplicationSessionKey().getApplicationName()=" + key.getSipApplicationSessionKey().getApplicationName()
					+ ", key.getSipApplicationSessionKey().getId()=" + key.getSipApplicationSessionKey().getId()
					+ ", key.getSipApplicationSessionKey().getAppGeneratedKey()=" + key.getSipApplicationSessionKey().getAppGeneratedKey()
					+ ", key.getSipSessionKey().getApplicationName()=" + key.getSipSessionKey().getApplicationName()
					+ ", key.getSipSessionKey().getApplicationSessionId()=" + key.getSipSessionKey().getApplicationSessionId()
					+ ", key.getSipSessionKey().getCallId()=" + key.getSipSessionKey().getCallId());
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Passivating session with id: " + key);
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
		if (logger.isDebugEnabled()){
			logger.debug("processUnloadedSipApplicationSessionPassivation - key" + key);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Passivating session with id: " + key);
		}

		getDistributedCacheConvergedSipManager().evictSipApplicationSession(key, osu.owner);
		osu.passivated = true;
		sipApplicationSessionPassivated();
	}

	private void sipSessionPassivated() {
		if (logger.isDebugEnabled()){
			logger.debug("sipSessionPassivated");
		}
		
		int pc = passivatedSipSessionCount_.incrementAndGet();
		int max = maxPassivatedSipSessionCount_.get();
		while (pc > max) {
			if (!maxPassivatedSipSessionCount_.compareAndSet(max, pc)) {
				max = maxPassivatedSipSessionCount_.get();
			}
		}
	}

	private void sipApplicationSessionPassivated() {
		if (logger.isDebugEnabled()){
			logger.debug("sipApplicationSessionPassivated");
		}
		
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
	//					logger.debug("Problem reading metadata for session " + key
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
	//								logger.trace("Elapsed time of " + elapsed
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
	//						else if (maxActiveAllowed > 0 && passivationMin >= 0
	//								&& calcActiveSessions() > maxActiveAllowed
	//								&& elapsed >= passivationMin) {
	//							if (trace_) {
	//								logger.trace("Elapsed time of " + elapsed
	//										+ " for session " + key
	//										+ " exceeds min of " + passivationMin
	//										+ "; passivating");
	//							}
	//							processUnloadedSipSessionPassivation(key, osu);
	//						}
	//					} catch (Exception e) {
	//						// most likely a lock conflict if the session is being
	//						// updated remotely; ignore it
	//						logger.debug("Problem passivating session " + key
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
		if (logger.isDebugEnabled()){
			logger.debug("initializeUnloadedSipApplicationSessions");
		}
		
		Map<String, String> sessions = getDistributedCacheConvergedSipManager().getSipApplicationSessionKeys();
		if (sessions != null) {
			boolean passivate = isPassivationEnabled();

			long passivationMax = getPassivationMaxIdleTime() * 1000L;
			long passivationMin = getPassivationMinIdleTime() * 1000L;

			for (Map.Entry<String, String> entry : sessions.entrySet()) {
				String sipApplicationSessionId = entry.getKey();
				String owner = entry.getValue();

				long ts = -1;
				DistributableSessionMetadata md = null;
				try {
					if (logger.isDebugEnabled()){
						logger.debug("initializeUnloadedSipApplicationSessions - get sip app session data from cache with id=" + sipApplicationSessionId + ", and owner=" + owner);
					}
					
					IncomingDistributableSessionData sessionData = getDistributedCacheConvergedSipManager()
					.getSipApplicationSessionData(sipApplicationSessionId, owner, false);
					ts = sessionData.getTimestamp();
					md = sessionData.getMetadata();
					
					if (logger.isDebugEnabled()){
						if (md == null){
							logger.debug("initializeUnloadedSipApplicationSessions - retrieved metadata is null");	
						} else {
							logger.debug("initializeUnloadedSipApplicationSessions - metadata.getId()=" + md.getId());
						}
					}
				} catch (Exception e) {
					// most likely a lock conflict if the session is being
					// updated remotely;
					// ignore it and use default values for timstamp and
					// maxInactive
					logger.debug("Problem reading metadata for session " + sipApplicationSessionId
							+ " -- " + e.toString());
				}

				long lastMod = ts == -1 ? System.currentTimeMillis() : ts;
				int maxLife = md == null ? getMaxInactiveInterval() : md
						.getMaxInactiveInterval();

				OwnedSessionUpdate osu = new OwnedSessionUpdate(owner, lastMod,
						maxLife, false);
				unloadedSipApplicationSessions_.put(sipApplicationSessionId, osu);
				if (passivate) {
					try {
						long elapsed = System.currentTimeMillis() - lastMod;
						// if maxIdle time configured, means that we need to
						// passivate sessions that have
						// exceeded the max allowed idle time
						if (passivationMax >= 0 && elapsed > passivationMax) {
							if (logger.isDebugEnabled()) {
								logger.debug("Elapsed time of " + elapsed
										+ " for session " + sipApplicationSessionId
										+ " exceeds max of " + passivationMax
										+ "; passivating");
							}
							processUnloadedSipApplicationSessionPassivation(sipApplicationSessionId, osu);
						}
						// If the session didn't exceed the
						// passivationMaxIdleTime_, see
						// if the number of sessions managed by this manager
						// greater than the max allowed
						// active sessions, passivate the session if it exceed
						// passivationMinIdleTime_
						else if (maxActiveAllowed > 0 && passivationMin >= 0
								&& calcActiveSessions() > maxActiveAllowed
								&& elapsed >= passivationMin) {
							if (logger.isDebugEnabled()) {
								logger.debug("Elapsed time of " + elapsed
										+ " for session " + sipApplicationSessionId
										+ " exceeds min of " + passivationMin
										+ "; passivating");
							}
							processUnloadedSipApplicationSessionPassivation(sipApplicationSessionId, osu);
						}
					} catch (Exception e) {
						// most likely a lock conflict if the session is being
						// updated remotely; ignore it
						logger.debug("Problem passivating session " + sipApplicationSessionId
								+ " -- " + e.toString());
					}
				}
			}
		}
	}	

	/**
	 * @return the SipFactoryImpl
	 */
	@Override
	public MobicentsSipFactory getMobicentsSipFactory(){
		if (logger.isDebugEnabled()){
			logger.debug("getMobicentsSipFactory");
		}
		
		return sipManagerDelegate.getSipFactoryImpl();
	}

	/**
	 * @param sipFactoryImpl
	 *            the SipFactoryImpl to set
	 */
	@Override
	public void setMobicentsSipFactory(MobicentsSipFactory sipFactoryImpl){
		if (logger.isDebugEnabled()){
			logger.debug("setMobicentsSipFactory");
		}
		
		sipManagerDelegate.setSipFactoryImpl((SipFactoryImpl)sipFactoryImpl);
	}

	/**
	 * @return the container
	 */
	//public Container getContainer() {
	public SipContext getSipContextContainer() {
		if (logger.isDebugEnabled()){
			logger.debug("getSipContextContainer");
		}
		
		return sipManagerDelegate.getContainer();
	}

	/**
	 * @param container
	 *            the container to set
	 */
	@Override
	public void setContainer(Container container) {
		if (logger.isDebugEnabled()){
			logger.debug("setContainer - container=" + container);
		}
		
		if(container != null && container instanceof SipContext && ((SipContext)container).getSipFactoryFacade() == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("context " + container.getName() + " not yet init-ed so returning");
			}
			return;
		}
		super.setContainer(container);		
		if(container instanceof SipContext) {	
			if(logger.isDebugEnabled()){
				logger.debug("container is SipContext so set it to sipManagerDelegate");
			}
			sipManagerDelegate.setContainer((SipContext) container);
		}
		DistributedCacheConvergedSipManager<? extends OutgoingDistributableSessionData> distributedCacheConvergedSipManager = getDistributedCacheConvergedSipManager();
		// Issue 1514: http://code.google.com/p/mobicents/issues/detail?id=1514	
		// only set this up if the application is a SIP or converged application	
		if(container instanceof SipContext) {			
			applicationName = ((SipContext)getContainer()).getApplicationName();
			if(logger.isDebugEnabled()) {
				logger.debug("Setting Application Name " + applicationName + " for Manager " + this + " for context " + container.getName());
			}
			if(((SipContext)getContainer()).getApplicationName() != null && ((SipContext)getContainer()).getApplicationNameHashed() != null) {
				distributedCacheConvergedSipManager.setApplicationName(applicationName);
				distributedCacheConvergedSipManager.setApplicationNameHashed(((SipContext)getContainer()).getApplicationNameHashed());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MobicentsSipSession removeSipSession(final MobicentsSipSessionKey key) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", key.getFromTag()=" + key.getFromTag());
		}
		
		ClusteredSipSession<? extends OutgoingDistributableSessionData> clusterSess = (ClusteredSipSession<? extends OutgoingDistributableSessionData>) sipManagerDelegate.removeSipSession(key);
		if(clusterSess == null) {
			return null;
		}
		synchronized (clusterSess) {
			String realId = clusterSess.getId();

			if (logger.isDebugEnabled()) {
				logger.debug("Removing sip session from store with id: " + realId);
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
					if (logger.isDebugEnabled()){
						logger.debug("removeSipSession - call startBatch");
					}
					batchingManager.startBatch();
					doTx = true;
				}
				clusterSess.removeMyself();
			} catch (Exception ex) {
				if (logger.isDebugEnabled())
					logger.debug("removeSipSession(): failed with exception", ex);

				try {
					// if(doTx)
					// Let's setRollbackOnly no matter what.
					// (except if there's no tx due to SESSION (JBAS-3840))
					if (notSession)
						batchingManager.setBatchRollbackOnly();
				} catch (Exception exn) {
					logger.error("Caught exception rolling back transaction", exn);
				}

				// We will need to alert Tomcat of this exception.
				if (ex instanceof RuntimeException)
					throw (RuntimeException) ex;

				throw new RuntimeException(
						"JBossCacheManager.removeSipSession(): "
						+ "failed to remove session.", ex);
			} finally {
				if (doTx){
					if (logger.isDebugEnabled()){
						logger.debug("removeSipSession - call endBatch");
					}
					batchingManager.endBatch();
				}
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipSessionExpired(clusterSess,
						(SipSessionKey)key, getSnapshotSipManager());

				// Track this session to prevent reincarnation by this request 
				// from the distributed cache
				ConvergedSessionInvalidationTracker.sipSessionInvalidated(key, this);

				//	            sipManagerDelegate.removeSipSession(key);
				this.getReplicationStatistics().removeStats(realId);

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
	@Override
	public MobicentsSipApplicationSession removeSipApplicationSession(
			final MobicentsSipApplicationSessionKey key) {
		if (logger.isDebugEnabled()){
			logger.debug("removeSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey());
		}
		
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusterSess = (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>) sipManagerDelegate.removeSipApplicationSession(key);
		if(clusterSess == null) {
			return null;
		}
		synchronized (clusterSess) {
			String realId = clusterSess.getId();

			if (logger.isDebugEnabled()) {
				logger.debug("Removing sip app session from store with id: " + realId);
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
					if (logger.isDebugEnabled()){
						logger.debug("removeSipApplicationSession - call startBatch");
					}
					batchingManager.startBatch();
					doTx = true;
				}
				clusterSess.removeMyself();
			} catch (Exception ex) {
				if (logger.isDebugEnabled())
					logger.debug("removeSipApplicationSession(): failed with exception", ex);

				try {
					// if(doTx)
					// Let's setRollbackOnly no matter what.
					// (except if there's no tx due to SESSION (JBAS-3840))
					if (notSession)
						batchingManager.setBatchRollbackOnly();
				} catch (Exception exn) {
					logger.error("Caught exception rolling back transaction", exn);
				}

				// We will need to alert Tomcat of this exception.
				if (ex instanceof RuntimeException)
					throw (RuntimeException) ex;

				throw new RuntimeException(
						"JBossCacheManager.removeSipApplicationSession(): "
						+ "failed to remove session.", ex);
			} finally {
				if (doTx){
					if (logger.isDebugEnabled()){
						logger.debug("removeSipApplicationSession - call endBatch");
					}
					batchingManager.endBatch();
				}
				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipApplicationSessionExpired(clusterSess,
						key, getSnapshotSipManager());

				// Track this session to prevent reincarnation by this request 
				// from the distributed cache
				ConvergedSessionInvalidationTracker.sipApplicationSessionInvalidated(key, this);

				//	            sipManagerDelegate.removeSipApplicationSession(key);
				this.getReplicationStatistics().removeStats(realId);

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
	@Override
	public MobicentsSipApplicationSession getSipApplicationSession(
			final MobicentsSipApplicationSessionKey key, final boolean create) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey() + ", create=" + create);
		}
		
		return getSipApplicationSession(key, create, false);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.DistributableSipManager#getSipApplicationSession(org.mobicents.servlet.sip.core.session.SipApplicationSessionKey, boolean, boolean)
	 */
	@Override
	public MobicentsSipApplicationSession getSipApplicationSession(
			final MobicentsSipApplicationSessionKey key, final boolean create, final boolean localOnly) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipApplicationSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey() + ", create=" + create + ", localOnly=" + localOnly);
		}
		
		// Find it from the local store first
		ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session = findLocalSipApplicationSession((SipApplicationSessionKey)key, false);

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
					logger.debug("Checking for sip app session " + key
							+ " in the distributed cache");

				session = loadSipApplicationSession((SipApplicationSessionKey)key, create);
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
					logger.debug("Updating sip app session " + key
							+ " from the distributed cache");


				// Need to update it from the cache
				loadSipApplicationSession((SipApplicationSessionKey)key, create);
			}
		}
		if (session != null) {			
			// Add this session to the set of those potentially needing
			// replication
			if (logger.isDebugEnabled()){
				logger.debug("getSipApplicationSession - retrieved session is not null");
			}
			ConvergedSessionReplicationContext.bindSipApplicationSession(session,
					getSnapshotSipManager());
			// If we previously called passivate() on the session due to
			// replication, we need to make an offsetting activate() call
			if (session.getNeedsPostReplicateActivation())
			{
				if (logger.isDebugEnabled()){
					logger.debug("getSipApplicationSession - call activate on retrieved session");
				}
				session.notifyDidActivate(ClusteredSessionNotificationCause.REPLICATION);
			}
		} else {
			if (logger.isDebugEnabled()){
				logger.debug("getSipApplicationSession - retrieved session is null");
			}
		}

		return session;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipManager#getSipSession(org.mobicents.servlet.sip.core.session.SipSessionKey, boolean, org.mobicents.servlet.sip.message.SipFactoryImpl, org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession)
	 */
	@Override
	public MobicentsSipSession getSipSession(final MobicentsSipSessionKey key,
			final boolean create, final MobicentsSipFactory sipFactoryImpl,
			final MobicentsSipApplicationSession sipApplicationSessionImpl) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", key.getFromTag()=" + key.getFromTag() + ", create=" + create);
		}
		
		return getSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl, false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.DistributableSipManager#getSipSession(org.mobicents.servlet.sip.core.session.SipSessionKey, boolean, org.mobicents.servlet.sip.message.SipFactoryImpl, org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession, boolean)
	 */
	@Override
	public MobicentsSipSession getSipSession(final MobicentsSipSessionKey key,
			final boolean create, final MobicentsSipFactory sipFactoryImpl,
			final MobicentsSipApplicationSession sipApplicationSessionImpl, final boolean localOnly) {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSession - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId() + ", key.getFromTag()=" + key.getFromTag() + ", create=" + create + ", localOnly=" + localOnly);
		}

		

		// Find it from the local store first
		ClusteredSipSession<? extends OutgoingDistributableSessionData> session = findLocalSipSession((SipSessionKey)key, false, sipApplicationSessionImpl);
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

				session = loadSipSession((SipSessionKey)key, create, (SipFactoryImpl)sipFactoryImpl, sipApplicationSessionImpl);
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
				loadSipSession((SipSessionKey)key, create, (SipFactoryImpl)sipFactoryImpl, sipApplicationSessionImpl);
			}

		}
		if (session != null) {			
			// Add this session to the set of those potentially needing
			// replication
			if (logger.isDebugEnabled()){
				logger.debug("getSipSession - session is not null - Add this session to the set of those potentially needing replication");
			}
			
			ConvergedSessionReplicationContext.bindSipSession(session,
					getSnapshotSipManager());

			// If we previously called passivate() on the session due to
			// replication, we need to make an offsetting activate() call
			if (session.getNeedsPostReplicateActivation())
			{
				if (logger.isDebugEnabled()){
					logger.debug("getSipSession - session is not null - calling notifyDidActivate");
				}
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
		if (logger.isDebugEnabled()){
			logger.debug("notifyRemoteSipApplicationSessionInvalidation - realId=" + realId);
		}
		
		// Remove the session from our local map
		SipApplicationSessionKey key = new SipApplicationSessionKey(realId, applicationName, null); // TODO: is ok that the third parameter is null? In JBoss5 there were only 2 parameters. 
		ClusteredSipApplicationSession<O> session = (ClusteredSipApplicationSession)sipManagerDelegate
		.removeSipApplicationSession(key);
		if (session == null) {
			// We weren't managing the session anyway. But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSipApplicationSessions_.remove(key.getId()) != null) {
				if (logger.isDebugEnabled())
					logger.debug("Removed entry for session " + key
							+ " from unloaded session map");
			}

			// If session has failed over and has been passivated here,
			// session will be null, but we'll have a TimeStatistic to clean up
			this.getReplicationStatistics().removeStats(realId);
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
				logger.info("SipApplicationSession invalidated by remote node " + session);
			} finally {
				ConvergedSessionInvalidationTracker.resume();

				// Remove any stats for this session
				this.getReplicationStatistics().removeStats(realId);

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
		if (logger.isDebugEnabled()){
			logger.debug("notifyRemoteSipSessionInvalidation - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
		}
		
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
				if (logger.isDebugEnabled())
					logger.debug("Removed entry for session " + key
							+ " from unloaded session map");
			}

			// If session has failed over and has been passivated here,
			// session will be null, but we'll have a TimeStatistic to clean up
			this.getReplicationStatistics().removeStats(key.toString());
		} else {
			//remove the dialog from the local stacks 
			if(session.getSessionCreatingDialog() != null) {
				final String sessionCreatingDialogId = session.getSessionCreatingDialog().getDialogId();
				if(sessionCreatingDialogId != null && sessionCreatingDialogId.length() > 0) {
					Container context = getContainer();
					Container container = context.getParent().getParent();
					if(container instanceof Engine) {
						if (logger.isDebugEnabled()) {
							logger.debug("container is instanceof Engine");
						}
						
						Service service = ((Engine)container).getService();
						if(service instanceof SipService) {
							if (logger.isDebugEnabled()) {
								logger.debug("service is instanceof SipService");
							}
							
							Connector[] connectors = service.findConnectors();
							for (Connector connector : connectors) {
								SipStack sipStack = (SipStack)
								connector.getProtocolHandler().getAttribute(SipStack.class.getSimpleName());
								if(sipStack != null) {
									if (logger.isDebugEnabled()) {
										logger.debug("deleting local dialog " + sessionCreatingDialogId + " on remote invalidation of the sip session " + key);
									}
									((ClusteredSipStack)sipStack).remoteDialogRemoval(sessionCreatingDialogId); 									
								}
							}
						}
					}
				}				
			}

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
				//session.invalidate();
				logger.info("SipSession invalidated by remote node (forcing local invalidation without triggering events)" + session);
			} finally {
				ConvergedSessionInvalidationTracker.resume();

				// Remove any stats for this session
				this.getReplicationStatistics().removeStats(key.toString());

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
		if (logger.isDebugEnabled()){
			logger.debug("notifySipApplicationSessionLocalAttributeModification - realId=" + realId);
		}
		
		SipApplicationSessionKey key = new SipApplicationSessionKey(realId, applicationName, null); // TODO: is ok that the third parameter is null? In JBoss5 there were only 2 parameters.
		ClusteredSipApplicationSession<O> session = (ClusteredSipApplicationSession) sipManagerDelegate
		.getSipApplicationSession(key, false);
		if (session != null) {
			session.sessionAttributesDirty();
		} else {
			logger.warn("Received local attribute notification for " + realId
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
		if (logger.isDebugEnabled()){
			logger.debug("notifySipSessionLocalAttributeModification - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId);
		}
		
		SipSessionKey key = null;
		try {
			key = SessionManagerUtil.parseHaSipSessionKey(sipSessionId, sipAppSessionId, applicationName);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + sipSessionId, e);
			return ;
		}

		ClusteredSipSession<O> session = (ClusteredSipSession) sipManagerDelegate
		.getSipSession(key, false, null, null);
		if (session != null) {
			session.sessionAttributesDirty();
		} else {
			logger.warn("Received local attribute notification for " + key.toString()
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
		if (logger.isDebugEnabled()){
			logger.debug("sipApplicationSessionChangedInDistributedCache - realId=" + realId + ", dataOwner=" + dataOwner);
		}
		
		boolean updated = true;
		SipApplicationSessionKey key = new SipApplicationSessionKey(realId, applicationName, null);// TODO: is ok that the third parameter is null? In JBoss5 there were only 2 parameters.

		ClusteredSipApplicationSession<O> session = findLocalSipApplicationSession(
				key, false);
		if (session != null) {
			// Need to invalidate the loaded session. We get back whether
			// this an actual version increment
			updated = session
			.setVersionFromDistributedCache(distributedVersion);
			if (updated && logger.isDebugEnabled()) {
				logger.debug("sip app session in-memory data is invalidated for id: "
						+ realId + " new version: " + distributedVersion);
			}
		} else {
			logger.debug("sip app session not found locally for : "
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
		//				if (logger.isDebugEnabled()) {
		//					logger.debug("New session " + realId
		//							+ " added to unloaded session map");
		//				}
		//			} else if (trace_) {
		//				logger.trace("Updated timestamp for unloaded session " + realId);
		//			}
		//		}

		return updated;
	}

	public void sipApplicationSessionActivated() {
		if (logger.isDebugEnabled()){
			logger.debug("sipApplicationSessionActivated");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("sipSessionActivated");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("sipSessionChangedInDistributedCache - sipAppSessionId=" + sipAppSessionId + ", sipSessionId=" + sipSessionId + ", metadata=" + metadata);
		}
		
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
			if (updated && logger.isDebugEnabled()) {
				logger.debug("session in-memory data is invalidated for id: "
						+ key.toString() + " new version: " + distributedVersion);
			}
		} else {
			logger.debug("sip session not found locally for : "
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
		//					logger.trace("New session " + sipSessionId
		//							+ " added to unloaded session map");
		//				}
		//			} else if (trace_) {
		//				logger.trace("Updated timestamp for unloaded session " + sipSessionId);
		//			}
		//		}

		return updated;
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession findSipApplicationSession(
			HttpSession httpSession) {
		if (logger.isDebugEnabled()){
			logger.debug("findSipApplicationSession - httpSession.getId()=" + httpSession.getId());
		}
		
		return sipManagerDelegate.findSipApplicationSession(httpSession);
	}

	/**
	 * 
	 */
	public void dumpSipSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("dumpSipSessions");
		}
		sipManagerDelegate.dumpSipSessions();
	}

	/**
	 * 
	 */
	public void dumpSipApplicationSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("dumpSipApplicationSessions");
		}
		sipManagerDelegate.dumpSipApplicationSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<MobicentsSipSession> getAllSipSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("getAllSipSessions");
		}
		return sipManagerDelegate.getAllSipSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<MobicentsSipApplicationSession> getAllSipApplicationSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("getAllSipApplicationSessions");
		}
		return sipManagerDelegate.getAllSipApplicationSessions();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAllSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("removeAllSessions");
		}
		sipManagerDelegate.removeAllSessions();
	}			

	@Override
	public void stop() throws LifecycleException {
		if (logger.isDebugEnabled()){
			logger.debug("stop");
		}
		
		// Handle re-entrance
		if (this.semaphore.tryAcquire()) {
			try {
				logger.debug("Closing off LockingValve");

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

		stopExtensions();

		super.stop();
	}

	@Override
	public synchronized void start() throws LifecycleException {
		if (logger.isDebugEnabled()){
			logger.debug("start");
		}
		
		super.start();
		startExtensions();
	}

	private void startExtensions() {
		if (logger.isDebugEnabled()){
			logger.debug("startExtensions");
		}

		this.outdatedSessionChecker = initOutdatedSessionChecker();
		this.outdatedSipSessionChecker = initOutdatedSipSessionChecker();
		
		this.outdatedSipApplicationSessionChecker = initOutdatedSipApplicationSessionChecker();
		
		
				
		mobicentsCache = new MobicentsCache(getDistributedCacheConvergedSipManager().getClusteredCache());
		mobicentsCluster = new DefaultMobicentsCluster(mobicentsCache, getDistributedCacheConvergedSipManager().getClusteredCache().getAdvancedCache().getTransactionManager(), new DefaultClusterElector());
				
		
		if(logger.isDebugEnabled()) {
			logger.debug("Mobicents Sip Servlets Default Mobicents Cluster " + mobicentsCluster + " created");
		}
		
		if(!mobicentsCluster.isStarted()){
			mobicentsCluster.startCluster();
			
			if(logger.isDebugEnabled()) {
				logger.debug("Mobicents Sip Servlets Default Mobicents Cluster " + mobicentsCluster + " started");
			}
			
		}
		
		
		initializeUnloadedSipApplicationSessions();
		//		initializeUnloadedSipSessions();
		initClusteredSipSessionNotificationPolicy();
		initClusteredSipApplicationSessionNotificationPolicy();
		// Handle re-entrance
		if (!this.semaphore.tryAcquire()) {
			logger.debug("Opening up LockingValve");

			// Make all permits available to locking valve
			this.semaphore.release(TOTAL_PERMITS);
		} else {
			// Release the one we just acquired
			this.semaphore.release();
		}
	}

	private void stopExtensions() {
		if (logger.isDebugEnabled()){
			logger.debug("stopExtensions");
		}
		
		if(mobicentsCache != null){
			mobicentsCache.stopCache();
			mobicentsCache = null;
			mobicentsCluster = null;
		}
		

		removeAllSessions();

		passivatedSipSessionCount_.set(0);
		passivatedSipApplicationSessionCount_.set(0);

		unloadedSipApplicationSessions_.clear();
		unloadedSipSessions_.clear();		
	}		

	/**
	 * Instantiate a SnapshotManager and ClusteredSessionValve and add the valve
	 * to our parent Context's pipeline. Add a JvmRouteValve and
	 * BatchReplicationClusteredSessionValve if needed.
	 */
	@Override
	protected void installValves() {
		if (logger.isDebugEnabled()){
			logger.debug("installValves");
		}
		
		logger.debug("Adding LockingValve");
		this.installContextValve(new LockingValve(this.valveLock));

		// If JK usage wasn't explicitly configured, default to enabling
		// it if jvmRoute is set on our containing Engine
		if (!getUseJK()) {
			getReplicationConfig().setUseJK(Boolean.valueOf(getJvmRoute() != null));
			//setUseJK(Boolean.valueOf(getJvmRoute() != null));
		}

		if (getUseJK()) {
			logger
			.debug("We are using JK for load-balancing. Adding JvmRouteValve.");
			this.installContextValve(new ConvergedJvmRouteValve(this));
		}

		// Add clustered session valve
		ConvergedClusteredSessionValve valve = new ConvergedClusteredSessionValve(this, null);
		logger.debug("Adding ConvergedClusteredSessionValve");
		this.installContextValve(valve);
	}

	private void installContextValve(Valve valve) {
		if (logger.isDebugEnabled()){
			logger.debug("installContextValve");
		}
		
		if (this.container instanceof Pipeline) {
			((Pipeline) this.container).addValve(valve);
		} else {
			// No choice; have to add it to the context's pipeline
			this.container.getPipeline().addValve(valve);
		}
	}

	//TODO: torolni ezt a metodust, jboss7-ben mar installContextValve van
	/*protected void installValve(Valve valve) {
		boolean installed = false;

		// In embedded mode, install the valve via JMX to be consistent
		// with the way the overall context is created in TomcatDeployer.
		// We can't do this in unembedded mode because we are called
		// before our Context is registered with the MBean server

		// Even on startup due to refactoring from http://code.google.com/p/mobicents/issues/detail?id=2794
		// the below JMX call will fail as
		// the context will not be registered when it's made so installing it directly
//		if (embedded_) {
//			ObjectName name = this.getObjectName(this.container_);
//
//			if (name != null) {
//				try {
//					MBeanServer server = this.getMBeanServer();
//
//					server.invoke(name, "addValve", new Object[] { valve },
//							new String[] { Valve.class.getName() });
//
//					installed = true;
//				} catch (Exception e) {
//					// JBAS-2422. If the context is restarted via JMX, the above
//					// JMX call will fail as the context will not be registered
//					// when it's made. So we catch the exception and fall back
//					// to adding the valve directly.
//					// TODO consider skipping adding via JMX and just do it
//					// directly
//					logger.debug("Caught exception installing valve to Context",
//							e);
//				}
//			}
//		}

		if (!installed) {
			// If possible install via the ContainerBase.addValve() API.
			if (this.container_ instanceof ContainerBase) {
				((ContainerBase) this.container_).addValve(valve);
			} else {
				// No choice; have to add it to the context's pipeline
				this.container_.getPipeline().addValve(valve);
			}
		}
	}*/

	private ObjectName getObjectName(Container container) {
		if (logger.isDebugEnabled()){
			logger.debug("getObjectName");
		}
		
		String oname = container.getObjectName();

		try {
			return (oname == null) ? null : new ObjectName(oname);
		} catch (MalformedObjectNameException e) {
			logger.warn("Error creating object name from string " + oname, e);
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
		if (logger.isDebugEnabled()){
			logger.debug("getMaxActiveSipSessions");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("setMaxActiveSipSessions");
		}
		
		this.sipManagerDelegate.setMaxActiveSipSessions(max);		
	}

	/**
	 * Return the maximum number of active Sessions allowed, or -1 for no limit.
	 */
	public int getMaxActiveSipApplicationSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("getMaxActiveSipApplicationSessions");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("setMaxActiveSipApplicationSessions");
		}
		
		this.sipManagerDelegate.setMaxActiveSipApplicationSessions(max);
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("getRejectedSipSessions");
		}
		
		return sipManagerDelegate.getRejectedSipSessions();
	}

	public void setRejectedSipSessions(int rejectedSipSessions) {
		if (logger.isDebugEnabled()){
			logger.debug("setRejectedSipSessions");
		}
		
		this.sipManagerDelegate.setRejectedSipSessions(rejectedSipSessions);
	}

	/**
	 * Number of sip session creations that failed due to maxActiveSipSessions
	 * 
	 * @return The count
	 */
	public int getRejectedSipApplicationSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("getRejectedSipApplicationSessions");
		}
		
		return sipManagerDelegate.getRejectedSipApplicationSessions();
	}

	public void setRejectedSipApplicationSessions(
			int rejectedSipApplicationSessions) {
		if (logger.isDebugEnabled()){
			logger.debug("setRejectedSipApplicationSessions");
		}
		
		this.sipManagerDelegate.setRejectedSipApplicationSessions(rejectedSipApplicationSessions);
	}

	public void setSipSessionCounter(int sipSessionCounter) {
		if (logger.isDebugEnabled()){
			logger.debug("setSipSessionCounter");
		}
		
		this.sipManagerDelegate.setSipSessionCounter(sipSessionCounter);
	}

	/**
	 * Total sessions created by this manager.
	 * 
	 * @return sessions created
	 */
	public int getSipSessionCounter() {
		if (logger.isDebugEnabled()){
			logger.debug("getSipSessionCounter");
		}
		
		return sipManagerDelegate.getSipSessionCounter();
	}

	/**
	 * Returns the number of active sessions
	 * 
	 * @return number of sessions active
	 */
	public int getActiveSipSessions() {
		if (logger.isDebugEnabled()){
			logger.debug("getActiveSipSessions");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("calcActiveSipApplicationSessions");
		}
		
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
		if (logger.isDebugEnabled()){
			logger.debug("calcActiveSipApplicationSessions - return=" + count);
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
		if (logger.isDebugEnabled()){
			logger.debug("calcActiveSipSessions");
		}
		
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
		
		if (logger.isDebugEnabled()){
			logger.debug("calcActiveSipSessions - return=" + count);
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

			this.key = new ClusteredSipSessionKey(session.getKey(), (SipApplicationSessionKey)session.getSipApplicationSession().getKey());
			this.session = session;
			this.osu = null;
		}

		private long getLastUpdate()
		{
			return osu == null ? session.getLastAccessedTime() : osu.updateTime;
		}

		private void passivate()
		{
			if (logger.isDebugEnabled()){
				logger.debug("SipSessionPassivationCheck.passivate");
			}
			
			if (osu == null)
			{
				DistributableSipSessionManager.this.processSipSessionPassivation(key.getSipSessionKey());
			}
			else
			{
				DistributableSipSessionManager.this.processUnloadedSipSessionPassivation(key, osu);
			}
		}

		private ClusteredSipSessionKey getKey()
		{
			return key;
		}

		private boolean isUnloaded()
		{
			if (logger.isDebugEnabled()){
				logger.debug("SipSessionPassivationCheck.isUnloaded = " + (osu != null));
			}
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
			if (logger.isDebugEnabled()){
				logger.debug("SipApplicationSessionPassivationCheck.passivate");
			}
			
			if (osu == null)
			{
				DistributableSipSessionManager.this.processSipApplicationSessionPassivation(key);
			}
			else
			{
				DistributableSipSessionManager.this.processUnloadedSipApplicationSessionPassivation(key.getId(), osu);
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
			if (logger.isDebugEnabled()){
				logger.debug("OwnedSessionUpdate.isPassivated = " + passivated);
			}
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
		return (SnapshotSipManager)snapshotManager;
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
				DistributableSipSessionManager.this.processSessionPassivation(realId);
			}
			else
			{
				DistributableSipSessionManager.this.processUnloadedSessionPassivation(realId, osu);
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

	
	
	// For EAP Branch it has to stay commented 

	//	public class AlwaysTrueOutdatedSessionChecker implements
	//			OutdatedSessionChecker {
	//		public boolean isSessionOutdated(
	//				ClusteredSession<? extends OutgoingDistributableSessionData> session) {
	//			return true;
	//		}
	//
	//	}
	//
	//	public class AskSessionOutdatedSessionChecker implements
	//			OutdatedSessionChecker {
	//		public boolean isSessionOutdated(
	//				ClusteredSession<? extends OutgoingDistributableSessionData> session) {
	//			return session.isOutdated();
	//		}
	//
	//	}
	//
	//	public interface OutdatedSessionChecker {
	//		boolean isSessionOutdated(
	//				ClusteredSession<? extends OutgoingDistributableSessionData> session);
	//	}

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
		if (logger.isDebugEnabled()){
			logger.debug("checkSipApplicationSessionPassivation - key.getApplicationName()=" + key.getApplicationName() + ", key.getId()=" + key.getId() + ", key.getAppGeneratedKey()=" + key.getAppGeneratedKey());
		}
		
		if (maxActiveAllowed > 0
				&& calcActiveSipApplicationSessions() >= maxActiveAllowed) {
			if (logger.isDebugEnabled()) {
				logger.debug("checkSipApplicationSessionPassivation(): active sip application sessions = "
						+ calcActiveSipApplicationSessions() + " and max allowed sessions = "
						+ maxActiveAllowed);
			}

			processSipApplicationSessionExpirationPassivation();

			if (calcActiveSipApplicationSessions() >= maxActiveAllowed) {
				// Exceeds limit. We need to reject it.
				rejectedSipApplicationSessionCounter_.incrementAndGet();
				// Catalina api does not specify what happens
				// but we will throw a runtime exception for now.				
				//				throw new IllegalStateException("checkSipApplicationSessionPassivation(): number of "
				//						+ "active sip application sessions exceeds the maximum limit: "
				//						+ maxActiveAllowed + " when trying to create sip application session "
				//						+ key);
			}
		}		
	}

	public void checkSipSessionPassivation(SipSessionKey key) {
		if (logger.isDebugEnabled()){
			logger.debug("checkSipSessionPassivation - key.getApplicationName()=" + key.getApplicationName() + ", key.getApplicationSessionId()=" + key.getApplicationSessionId() + ", key.getCallId()=" + key.getCallId());
		}
		
		if (maxActiveAllowed > 0
				&& calcActiveSipSessions() >= maxActiveAllowed) {
			if (logger.isDebugEnabled()) {
				logger.debug("checkSipSessionPassivation(): active sip sessions = "
						+ calcActiveSipSessions() + " and max allowed sessions = "
						+ maxActiveAllowed);
			}

			processSipSessionExpirationPassivation();

			if (calcActiveSipSessions() >= maxActiveAllowed) {
				// Exceeds limit. We need to reject it.
				rejectedSipSessionCounter_.incrementAndGet();
				// Catalina api does not specify what happens
				// but we will throw a runtime exception for now.
				//				throw new IllegalStateException("checkSipSessionPassivation(): number of "
				//						+ "active sip sessions exceeds the maximum limit: "
				//						+ maxActiveAllowed + " when trying to create sip session "
				//						+ key);
			}
		}		
	}

	private ClusteredSession<O> cast(Session session) {
		if (logger.isDebugEnabled()){
			logger.debug("cast");
		}
		
		if (session == null) return null;
		if (!(session instanceof ClusteredSession)) {
			throw MESSAGES.invalidSession(getClass().getName());
		}
		return (ClusteredSession<O>) session;
	}

	@Override
	protected SnapshotManager createSnapshotManager() {
		if (logger.isDebugEnabled()){
			logger.debug("createSnapshotManager");
		}
		
		String ctxPath = ((Context) this.container).getPath();
		switch (this.getSnapshotMode()) {
		case INTERVAL: {
			int interval = this.getSnapshotInterval();
			if (interval > 0) {
				//return new IntervalSnapshotManager(this, ctxPath, interval);
				snapshotManager = new IntervalConvergedSnapshotManager(this, ctxPath, interval);
				return snapshotManager;
			}
			WebLogger.WEB_SESSION_LOGGER.invalidSnapshotInterval();
		}
		case INSTANT: {
			//return new InstantSnapshotManager(this, ctxPath);
			snapshotManager = new InstantConvergedSnapshotManager(this, ctxPath);
			return snapshotManager;
		}
		default: {
			throw MESSAGES.invalidSnapshotMode();
		}
		}
	}

	protected Session[] findLocalSessions(){
		if (logger.isDebugEnabled()){
			logger.debug("findLocalSessions");
		}
		
		return this.sessions.values().toArray(new Session[this.sessions.values().size()]);
	}

}

