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

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ContainerBase;
import org.jboss.cache.CacheException;
import org.jboss.cache.aop.PojoCacheMBean;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.web.tomcat.service.JBossWeb;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManagerDelegate;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

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
public class JBossCacheSipManager extends JBossCacheManager implements
		DistributableSipManager {

	/**
     * The descriptive information about this implementation.
     */
    protected static final String info = "JBossCacheSipManager/1.0";
    
	protected static Logger logger = Logger.getLogger(JBossCacheSipManager.class);
	
	private SipManagerDelegate sipManagerDelegate;

	/**
	 * Informational name for this Catalina component
	 */
	static final String info_ = "JBossCacheSipManager/1.0";

	// -- Class attributes ---------------------------------

	/**
	 * The transaction manager.
	 */
	protected TransactionManager tm;

	/**
	 * Proxy-object for the JBossCacheService
	 */
	private ConvergedJBossCacheService proxy_;

	/**
	 * Id/timestamp of sessions in cache that we haven't loaded
	 */
	private Map unloadedSessions_ = new ConcurrentHashMap();
	
	/**
	 * Id/timestamp of sessions in cache that we haven't loaded
	 */
	private Map unloadedSipSessions_ = new ConcurrentHashMap();
	
	/**
	 * Id/timestamp of sessions in cache that we haven't loaded
	 */
	private Map unloadedSipApplicationSessions_ = new ConcurrentHashMap();

	/** Our TreeCache's ObjectName */
	private String cacheObjectNameString_ = JBossWeb.DEFAULT_CACHE_NAME;

	/**
	 * If set to true, will add a JvmRouteFilter to the request.
	 */
	protected boolean useJK_ = false;

	/** Are we running embedded in JBoss? */
	private boolean embedded_ = false;

	/** Our JMX Server */
	private MBeanServer mserver_ = null;

	/** Our ClusteredSessionValve's snapshot mode. */
	private String snapshotMode_ = null;

	/** Our ClusteredSessionValve's snapshot interval. */
	private int snapshotInterval_ = 0;

	/** String form of invalidateSessionPolicy_ */
	private String replTriggerString_ = null;

	/** String form of replGranularityString_ */
	private String replGranularityString_ = null;

	/**
	 * Whether we use batch mode replication for field level granularity. We
	 * store this in a Boolean rather than a primitive so JBossCacheCluster can
	 * determine if this was set via a <Manager> element.
	 */
	private Boolean replicationFieldBatchMode_;

	/** Class loader for this web app. */
	private ClassLoader tcl_;

	/**
	 * The snapshot manager we are using.
	 */
	private SnapshotManager snapshotManager_;

	/**
	 * Whether we are doing trace level logging
	 */
	private boolean trace;

	private int maxUnreplicatedInterval_ = WebMetaData.DEFAULT_MAX_UNREPLICATED_INTERVAL;
	
	// ---------------------------------------------------------- Constructors

	public JBossCacheSipManager() {
		super();		
	}

	/** 
    * Create a new JBossCacheManager using the given cache. For use in unit testing.
    * 
    * @param pojoCache
    */
   public JBossCacheSipManager(PojoCacheMBean pojoCache)
   {
      super();
      this.proxy_ = new ConvergedJBossCacheService(pojoCache);
   }
	
	/**
	 * Initializes this Manager when running in embedded mode.
	 * <p>
	 * <strong>NOTE:</strong> This method should not be called when running
	 * unembedded.
	 * </p>
	 */
	public void init(String name, WebMetaData webMetaData, boolean useJK,
			boolean useLocalCache) throws ClusteringNotSupportedException {
		replicationGranularity_ = webMetaData.getReplicationGranularity();
		invalidateSessionPolicy_ = webMetaData.getInvalidateSessionPolicy();
		sipManagerDelegate = new ClusteredSipManagerDelegate(replicationGranularity_);
		useLocalCache_ = useLocalCache;
		log_.info("init(): replicationGranularity_ is "
				+ replicationGranularity_ + " and invalidateSessionPolicy is "
				+ invalidateSessionPolicy_);

		try {
			// Give this manager a name
			objectName_ = new ObjectName(
					"jboss.web:service=ClusterManager,WebModule=" + name);
		} catch (Throwable e) {
			log_.error("Could not create ObjectName", e);
			throw new ClusteringNotSupportedException(e.toString());
		}

		this.useJK_ = useJK;
		this.replicationFieldBatchMode_ = webMetaData
				.getReplicationFieldBatchMode() ? Boolean.TRUE : Boolean.FALSE;
		
		Integer maxUnrep = webMetaData.getMaxUnreplicatedInterval();
		if (maxUnrep != null) {
			this.maxUnreplicatedInterval_ = maxUnrep.intValue();
		}

		if (proxy_ == null)
		proxy_ = new ConvergedJBossCacheService(cacheObjectNameString_);

		// Confirm our replication granularity is compatible with the cache
		// Throws ISE if not
		validateFieldMarshalling();

		embedded_ = true;
	}

	// ------------------------------------------------------------- Properties

	/**
	 * Gets the <code>JBossCacheService</code> through which we interact with
	 * the <code>TreeCache</code>.
	 */
	public JBossCacheService getCacheService() {
		return proxy_;
	}

	/**
	 * Gets a String representation of the JMX <code>ObjectName</code> under
	 * which our <code>TreeCache</code> is registered.
	 */
	public String getCacheObjectNameString() {
		return cacheObjectNameString_;
	}

	/**
	 * Sets the JMX <code>ObjectName</code> under which our
	 * <code>TreeCache</code> is registered.
	 */
	public void setCacheObjectNameString(String treeCacheObjectName) {
		this.cacheObjectNameString_ = treeCacheObjectName;
	}

	/**
	 * Gets when sessions are replicated to the other nodes. The default value,
	 * "instant", synchronously replicates changes to the other nodes. In this
	 * case, the "SnapshotInterval" attribute is not used. The "interval" mode,
	 * in association with the "SnapshotInterval" attribute, indicates that
	 * Tomcat will only replicate modified sessions every "SnapshotInterval"
	 * miliseconds at most.
	 * 
	 * @see #getSnapshotInterval()
	 */
	public String getSnapshotMode() {
		return snapshotMode_;
	}

	/**
	 * Sets when sessions are replicated to the other nodes. Valid values are:
	 * <ul>
	 * <li>instant</li>
	 * <li>interval</li>
	 * </ul>
	 */
	public void setSnapshotMode(String snapshotMode) {
		this.snapshotMode_ = snapshotMode;
	}

	/**
	 * Gets how often session changes should be replicated to other nodes. Only
	 * relevant if property {@link #getSnapshotMode() snapshotMode} is set to
	 * <code>interval</code>.
	 * 
	 * @return the number of milliseconds between session replications.
	 */
	public int getSnapshotInterval() {
		return snapshotInterval_;
	}

	/**
	 * Sets how often session changes should be replicated to other nodes.
	 * 
	 * @param snapshotInterval
	 *            the number of milliseconds between session replications.
	 */
	public void setSnapshotInterval(int snapshotInterval) {
		this.snapshotInterval_ = snapshotInterval;
	}

	/**
	 * Gets whether the <code>Engine</code> in which we are running uses
	 * <code>mod_jk</code>.
	 */
	public boolean getUseJK() {
		return useJK_;
	}

	/**
	 * Sets whether the <code>Engine</code> in which we are running uses
	 * <code>mod_jk</code>.
	 */
	public void setUseJK(boolean useJK) {
		this.useJK_ = useJK;
	}

	/**
	 * Returns the replication granularity expressed as an int.
	 * 
	 * @see WebMetaData#REPLICATION_GRANULARITY_ATTRIBUTE
	 * @see WebMetaData#REPLICATION_GRANULARITY_FIELD
	 * @see WebMetaData#REPLICATION_GRANULARITY_SESSION
	 */
	public int getReplicationGranularity() {
		return replicationGranularity_;
	}

	/**
	 * Gets the granularity of session data replicated across the cluster; i.e.
	 * whether the entire session should be replicated when replication is
	 * triggered, only modified attributes, or only modified fields of
	 * attributes.
	 */
	public String getReplicationGranularityString() {
		// Only lazy-set this if we are started;
		// otherwise screws up standalone TC integration!!
		if (started_ && this.replGranularityString_ == null) {
			switch (this.replicationGranularity_) {
			case WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE:
				this.replGranularityString_ = "ATTRIBUTE";
				break;
			case WebMetaData.REPLICATION_GRANULARITY_SESSION:
				this.replGranularityString_ = "SESSION";
				break;
			case WebMetaData.REPLICATION_GRANULARITY_FIELD:
				this.replGranularityString_ = "FIELD";
			}
		}
		return replGranularityString_;
	}

	/**
	 * Sets the granularity of session data replicated across the cluster. Valid
	 * values are:
	 * <ul>
	 * <li>SESSION</li>
	 * <li>ATTRIBUTE</li>
	 * <li>FIELD</li>
	 * </ul>
	 */
	public void setReplicationGranularityString(String granularity) {
		this.replGranularityString_ = granularity;
	}

	/**
	 * Gets the type of operations on a <code>HttpSession</code> that trigger
	 * replication.
	 */
	public String getReplicationTriggerString() {
		// Only lazy-set this if we are started;
		// otherwise screws up standalone TC integration!!
		if (started_ && this.replTriggerString_ == null) {
			switch (this.invalidateSessionPolicy_) {
			case WebMetaData.SESSION_INVALIDATE_SET:
				this.replTriggerString_ = "SET";
				break;
			case WebMetaData.SESSION_INVALIDATE_SET_AND_GET:
				this.replTriggerString_ = "SET_AND_GET";
				break;
			case WebMetaData.SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET:
				this.replTriggerString_ = "SET_AND_NON_PRIMITIVE_GET";
			}
		}
		return this.replTriggerString_;
	}

	/**
	 * Sets the type of operations on a <code>HttpSession</code> that trigger
	 * replication. Valid values are:
	 * <ul>
	 * <li>SET_AND_GET</li>
	 * <li>SET_AND_NON_PRIMITIVE_GET</li>
	 * <li>SET</li>
	 * </ul>
	 */
	public void setReplicationTriggerString(String trigger) {
		this.replTriggerString_ = trigger;
	}

	/**
	 * Gets whether, if replication granularity is set to <code>FIELD</code>,
	 * replication should be done in batch mode. Ignored if field-level
	 * granularity is not used.
	 */
	public Boolean isReplicationFieldBatchMode() {
		return replicationFieldBatchMode_;
	}

	/**
	 * Sets whether, if replication granularity is set to <code>FIELD</code>,
	 * replication should be done in batch mode. Ignored if field-level
	 * granularity is not used.
	 */
	public void setReplicationFieldBatchMode(boolean replicationFieldBatchMode) {
		this.replicationFieldBatchMode_ = Boolean
				.valueOf(replicationFieldBatchMode);
	}

	public void setUseLocalCache(boolean useLocalCache) {
		this.useLocalCache_ = useLocalCache;
	}

	public int getMaxUnreplicatedInterval() {
		return maxUnreplicatedInterval_;
	}

	public void setMaxUnreplicatedInterval(int maxUnreplicatedInterval) {
		this.maxUnreplicatedInterval_ = maxUnreplicatedInterval;
	}
	
	// JBossCacheManagerMBean-methods -------------------------------------

	public void expireSession(String sessionId) {
		Session session = findSession(sessionId);
		if (session != null)
			session.expire();
	}

	public String getLastAccessedTime(String sessionId) {
		Session session = findSession(sessionId);
		if (session == null) {
			log_.debug("getLastAccessedTime(): Session " + sessionId
					+ " not found");
			return "";
		}
		return new Date(session.getLastAccessedTime()).toString();
	}

	public Object getSessionAttribute(String sessionId, String key) {
		ClusteredSession session = (ClusteredSession) findSession(sessionId);
		return (session == null) ? null : session.getAttribute(key);
	}

	public String getSessionAttributeString(String sessionId, String key) {
		Object attr = getSessionAttribute(sessionId, key);
		return (attr == null) ? null : attr.toString();
	}

	public String listLocalSessionIds() {
		return reportSessionIds(sessions_.keySet());
	}

	public String listSessionIds() {
		Set ids = new HashSet(sessions_.keySet());
		ids.addAll(unloadedSessions_.keySet());
		return reportSessionIds(ids);
	}

	private String reportSessionIds(Set ids) {
		StringBuffer sb = new StringBuffer();
		boolean added = false;
		for (Iterator it = ids.iterator(); it.hasNext();) {
			if (added) {
				sb.append(',');
			} else {
				added = true;
			}

			sb.append(it.next());
		}
		return sb.toString();
	}

	// Manager-methods -------------------------------------

	/**
	 * Start this Manager
	 * 
	 * @throws org.apache.catalina.LifecycleException
	 * 
	 */
	public void start() throws LifecycleException {
		if (embedded_) {
			startEmbedded();
		} else {
			startUnembedded();
		}
	}

	public void stop() throws LifecycleException {
		if (!started_) {
			throw new IllegalStateException("Manager not started");
		}

		log_.debug("Stopping");

		resetStats();

		// Notify our interested LifecycleListeners
		lifecycle_.fireLifecycleEvent(BEFORE_STOP_EVENT, this);

		clearSessions();

		// Don't leak the classloader
		tcl_ = null;

		proxy_.stop();
		tm = null;

		snapshotManager_.stop();

		started_ = false;

		// Notify our interested LifecycleListeners
		lifecycle_.fireLifecycleEvent(AFTER_STOP_EVENT, this);

		try {
			unregisterMBeans();
		} catch (Exception e) {
			log_.error("Could not unregister ManagerMBean from MBeanServer", e);
		}
	}

	/**
	 * Clear the underlying cache store and also pojo that has the observers.
	 */
	protected void clearSessions() {
		// First, the sessions we have actively loaded
		ClusteredSession[] sessions = findLocalSessions();
		for (int i = 0; i < sessions.length; i++) {
			ClusteredSession ses = sessions[i];
			if (log_.isDebugEnabled()) {
				log_
						.debug("clearSessions(): clear session by expiring: "
								+ ses);
			}
			boolean notify = true;
			boolean localCall = true;
			boolean localOnly = true;
			try {
				ses.expire(notify, localCall, localOnly);
			} catch (Throwable t) {
				log_.warn("clearSessions(): Caught exception expiring session "
						+ ses.getIdInternal(), t);
			} finally {
				// Guard against leaking memory if anything is holding a
				// ref to the session by clearing its internal state
				ses.recycle();
			}
		}

		// Next, the local copy of the distributed cache
		Map unloaded = new HashMap(unloadedSessions_);
		Set keys = unloaded.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			String realId = (String) it.next();
			proxy_.removeSessionLocal(realId);
			unloadedSessions_.remove(realId);
		}
	}
	
	/**
	 * Clear the underlying cache store and also pojo that has the observers.
	 */
	protected void clearSipApplicationSessions() {
		// First, the sessions we have actively loaded
		ClusteredSession[] sessions = findLocalSessions();
		for (int i = 0; i < sessions.length; i++) {
			ClusteredSession ses = sessions[i];
			if (log_.isDebugEnabled()) {
				log_
						.debug("clearSessions(): clear session by expiring: "
								+ ses);
			}
			boolean notify = true;
			boolean localCall = true;
			boolean localOnly = true;
			try {
				ses.expire(notify, localCall, localOnly);
			} catch (Throwable t) {
				log_.warn("clearSessions(): Caught exception expiring session "
						+ ses.getIdInternal(), t);
			} finally {
				// Guard against leaking memory if anything is holding a
				// ref to the session by clearing its internal state
				ses.recycle();
			}
		}

		// Next, the local copy of the distributed cache
		Map unloaded = new HashMap(unloadedSipSessions_);
		Set keys = unloaded.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			String realId = (String) it.next();
			proxy_.removeSipApplicationSessionLocal(realId);
			unloadedSipSessions_.remove(realId);
		}
	}

	/**
	 * Create a new session with a generated id.
	 */
	public Session createSession() {
		return createSession(null);
	}

	/**
	 * Create a new session.
	 * 
	 * @param sessionId
	 *            the id to use, or <code>null</code> if we should generate a
	 *            new id
	 * 
	 * @return the session
	 * 
	 * @throws IllegalStateException
	 *             if the current number of active sessions exceeds the maximum
	 *             number allowed
	 */
	public Session createSession(String sessionId) {
		// We check here for maxActive instead of in add(). add() gets called
		// when we load an already existing session from the distributed cache
		// (e.g. in a failover) and we don't want to fail in that situation.

		// maxActive_ -1 is unlimited
		if (maxActive_ != -1 && activeCounter_ >= maxActive_) {
			// Exceeds limit. We need to reject it.
			rejectedCounter_++;
			// Catalina api does not specify what happens
			// but we will throw a runtime exception for now.
			String msgEnd = (sessionId == null) ? "" : " id " + sessionId;
			throw new IllegalStateException(
					"JBossCacheManager.add(): number of "
							+ "active sessions exceeds the maximum limit: "
							+ maxActive_ + " when trying to add session"
							+ msgEnd);
		}

		ClusteredSession session = createEmptyClusteredSession();

		session.setNew(true);
		session.setCreationTime(System.currentTimeMillis());
		session.setMaxInactiveInterval(this.maxInactiveInterval_);
		session.setValid(true);

		if (sessionId == null) {
			sessionId = this.getNextId();

			// We are using mod_jk for load balancing. Append the JvmRoute.
			if (useJK_) {
				if (log_.isDebugEnabled()) {
					log_
							.debug("createSession(): useJK is true. Will append JvmRoute: "
									+ this.getJvmRoute());
				}
				sessionId += "." + this.getJvmRoute();
			}
		}

		session.setId(sessionId); // Setting the id leads to a call to add()

		if (log_.isDebugEnabled()) {
			log_.debug("Created a ClusteredSession with id: " + sessionId);
		}

		createdCounter_++;

		// Add this session to the set of those potentially needing replication
		ConvergedSessionReplicationContext.bindSession(session,
				snapshotManager_);

		return session;
	}

	public boolean storeSession(Session baseSession) {
		boolean stored = false;
		if (baseSession != null && started_) {
			ClusteredSession session = (ClusteredSession) baseSession;

			synchronized (session) {
				if (logger.isDebugEnabled()) {
					log_.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getIdInternal());
				}

				if (session.isValid()
						&& (session.isSessionDirty() || session
								.getExceedsMaxUnreplicatedInterval())) {
					String realId = session.getRealId();

					// Notify all session attributes that they get serialized
					// (SRV 7.7.2)
					long begin = System.currentTimeMillis();
					session.passivate();
					long elapsed = System.currentTimeMillis() - begin;
					stats_.updatePassivationStats(realId, elapsed);

					// Do the actual replication
					begin = System.currentTimeMillis();
					processSessionRepl(session);
					elapsed = System.currentTimeMillis() - begin;
					stored = true;
					stats_.updateReplicationStats(realId, elapsed);
				}
			}
		}

		return stored;
	}

	public boolean storeSipSession(SipSession baseSession) {
		boolean stored = false;
		if (baseSession != null && started_) {
			ClusteredSipSession session = (ClusteredSipSession) baseSession;

			synchronized (session) {
				if (logger.isDebugEnabled()) {
					logger.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getId());
				}

				if (session.isValid()
						&& (session.isSessionDirty() || session
								.getExceedsMaxUnreplicatedInterval())) {
					if(logger.isInfoEnabled()) {
						logger.info("replicating following sip session " + session.getId());
					}
					String realId = session.getId();

					// Notify all session attributes that they get serialized
					// (SRV 7.7.2)
					long begin = System.currentTimeMillis();
					session.passivate();
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
			ClusteredSipApplicationSession session = (ClusteredSipApplicationSession) baseSession;

			synchronized (session) {
				if (logger.isDebugEnabled()) {
					log_.debug("check to see if needs to store and replicate "
							+ "session with id " + session.getId());
				}

				if (session.isValid()
						&& (session.isSessionDirty() || session
								.getExceedsMaxUnreplicatedInterval())) {
					if(logger.isInfoEnabled()) {
						logger.info("replicating following sip application session " + session.getId());
					}
					String realId = session.getId();

					// Notify all session attributes that they get serialized
					// (SRV 7.7.2)
					long begin = System.currentTimeMillis();
					session.passivate();
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
	
	public void add(Session session) {
		if (session == null)
			return;

		if (!(session instanceof ClusteredSession)) {
			throw new IllegalArgumentException(
					"You can only add instances of "
							+ "type ClusteredSession to this Manager. Session class name: "
							+ session.getClass().getName());
		}

		// add((ClusteredSession) session, true);
		add((ClusteredSession) session, false);
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
		add((ClusteredSipSession) session, false);
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
		add((ClusteredSipApplicationSession) session, false);
	}

	/**
	 * Adds the given session to the collection of those being managed by this
	 * Manager.
	 * 
	 * @param session
	 *            the session. Cannot be <code>null</code>.
	 * @param replicate
	 *            whether the session should be replicated
	 * 
	 * @throws NullPointerException
	 *             if <code>session</code> is <code>null</code>.
	 */
	private void add(ClusteredSession session, boolean replicate) {
		if (!session.isValid()) {
			log_.error("Cannot add session with id=" + session.getIdInternal()
					+ " because it is invalid");
			return;
		}

		String realId = session.getRealId();
		Object existing = sessions_.put(realId, session);
		unloadedSessions_.remove(realId);

		if (!session.equals(existing)) {
			if (replicate) {
				storeSession(session);
			}

			activeCounter_++;
			if (activeCounter_ > maxActiveCounter_)
				maxActiveCounter_++;

			if (log_.isDebugEnabled()) {
				log_.debug("Session with id=" + session.getIdInternal()
						+ " added. " + "Current active sessions "
						+ activeCounter_);
			}
		}
	}
	
	/**
	 * Adds the given session to the collection of those being managed by this
	 * Manager.
	 * 
	 * @param session
	 *            the session. Cannot be <code>null</code>.
	 * @param replicate
	 *            whether the session should be replicated
	 * 
	 * @throws NullPointerException
	 *             if <code>session</code> is <code>null</code>.
	 */
	private void add(ClusteredSipSession session, boolean replicate) {
		if (!session.isValid()) {
			log_.error("Cannot add session with id=" + session.getId()
					+ " because it is invalid");
			return;
		}

		SipSessionKey key = session.getKey();
		Object existing = ((ClusteredSipManagerDelegate)sipManagerDelegate).putSipSession(key, session);
		unloadedSipSessions_.remove(key.toString());

		if (!session.equals(existing)) {
			if (replicate) {
				storeSipSession(session);
			}

			activeCounter_++;
			if (activeCounter_ > maxActiveCounter_)
				maxActiveCounter_++;

			if (log_.isDebugEnabled()) {
				log_.debug("Session with id=" + session.getId()
						+ " added. " + "Current active sessions "
						+ activeCounter_);
			}
		}
	}
	
	/**
	 * Adds the given session to the collection of those being managed by this
	 * Manager.
	 * 
	 * @param session
	 *            the session. Cannot be <code>null</code>.
	 * @param replicate
	 *            whether the session should be replicated
	 * 
	 * @throws NullPointerException
	 *             if <code>session</code> is <code>null</code>.
	 */
	private void add(ClusteredSipApplicationSession session, boolean replicate) {
		if (!session.isValid()) {
			log_.error("Cannot add session with id=" + session.getId()
					+ " because it is invalid");
			return;
		}

		SipApplicationSessionKey key = session.getKey();
		Object existing = ((ClusteredSipManagerDelegate)sipManagerDelegate).putSipApplicationSession(key, session);
		unloadedSipApplicationSessions_.remove(key.toString());

		if (!session.equals(existing)) {
			if (replicate) {
				storeSipApplicationSession(session);
			}

			activeCounter_++;
			if (activeCounter_ > maxActiveCounter_)
				maxActiveCounter_++;

			if (log_.isDebugEnabled()) {
				log_.debug("Session with id=" + session.getId()
						+ " added. " + "Current active sessions "
						+ activeCounter_);
			}
		}
	}

	/**
	 * Attempts to find the session in the collection of those being managed
	 * locally, and if not found there, in the distributed cache of sessions.
	 * <p>
	 * If a session is found in the distributed cache, it is added to the
	 * collection of those being managed locally.
	 * </p>
	 * 
	 * @param id
	 *            the session id, which may include an appended jvmRoute
	 * 
	 * @return the session, or <code>null</code> if no such session could be
	 *         found
	 */
	public Session findSession(String id) {
		String realId = getRealId(id);
		// Find it from the local store first
		ClusteredSession session = findLocalSession(realId);

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
				&& !ConvergedSessionReplicationContext
						.isSessionBoundAndExpired(realId, snapshotManager_)) {
			if (logger.isDebugEnabled())
				log_.debug("Checking for session " + realId
						+ " in the distributed cache");

			session = loadSession(realId);
			if (session != null) {
				add(session);
				// TODO should we advise of a new session?
				// tellNew();
			}
		} else if (session != null && session.isOutdated()) {
			if (logger.isDebugEnabled())
				log_.debug("Updating session " + realId
						+ " from the distributed cache");

			// Need to update it from the cache
			loadSession(realId);
		}

		if (session != null) {
			// Add this session to the set of those potentially needing
			// replication
			ConvergedSessionReplicationContext.bindSession(session,
					snapshotManager_);
		}

		return session;
	}

	/**
	 * Return the sessions. Note that this will return not only the local
	 * in-memory sessions, but also any sessions that are in the distributed
	 * cache but have not previously been accessed on this server. Invoking this
	 * method will bring all such sessions into local memory and can potentially
	 * be quite expensive.
	 * 
	 * <p>
	 * Note also that when sessions are loaded from the distributed cache, no
	 * check is made as to whether the number of local sessions will thereafter
	 * exceed the maximum number allowed on this server.
	 * </p>
	 * 
	 * @return an array of all the sessions
	 */
	public Session[] findSessions() {
		// Need to load all the unloaded sessions
		if (unloadedSessions_.size() > 0) {
			// Make a thread-safe copy of the new id list to work with
			Set ids = new HashSet(unloadedSessions_.keySet());

			if (log_.isDebugEnabled()) {
				log_
						.debug("findSessions: loading sessions from distributed cache: "
								+ ids);
			}

			for (Iterator it = ids.iterator(); it.hasNext();) {
				loadSession((String) it.next());
			}
		}

		// All sessions are now "local" so just return the local sessions
		return findLocalSessions();
	}

	/**
	 * Returns all the sessions that are being actively managed by this manager.
	 * This includes those that were created on this server, those that were
	 * brought into local management by a call to
	 * {@link #findLocalSession(String)} as well as all sessions brought into
	 * local management by a call to {@link #findSessions()}.
	 */
	public ClusteredSession[] findLocalSessions() {
		Collection coll = sessions_.values();
		ClusteredSession[] sess = new ClusteredSession[coll.size()];
		sess = (ClusteredSession[]) coll.toArray(sess);
		return sess;
	}

	/**
	 * Returns the given session if it is being actively managed by this
	 * manager. An actively managed session is on that was either created on
	 * this server, brought into local management by a call to
	 * {@link #findLocalSession(String)} or brought into local management by a
	 * call to {@link #findSessions()}.
	 * 
	 * @param realId
	 *            the session id, with any trailing jvmRoute removed.
	 * 
	 * @see #getRealId(String)
	 */
	public ClusteredSession findLocalSession(String realId) {
		return (ClusteredSession) sessions_.get(realId);
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
	 * Removes the session from this Manager's collection of actively managed
	 * sessions. Also removes the session from the distributed cache, both on
	 * this server and on all other server to which this one replicates.
	 */
	public void remove(Session session) {
		ClusteredSession clusterSess = (ClusteredSession) session;
		synchronized (clusterSess) {
			String realId = clusterSess.getRealId();
			if (realId == null)
				return;

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from store with id: " + realId);
			}

			try {
				// Ignore any cache notifications that our own work generates
				ConvergedSessionReplicationContext.startCacheActivity();
				clusterSess.removeMyself();
			} finally {
				ConvergedSessionReplicationContext.finishCacheActivity();

				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sessionExpired(clusterSess,
						realId, snapshotManager_);

				sessions_.remove(realId);
				stats_.removeStats(realId);
				activeCounter_--;
			}
		}
	}

	/**
	 * Removes the session from this Manager's collection of actively managed
	 * sessions. Also removes the session from this server's copy of the
	 * distributed cache (but does not remove it from other servers' distributed
	 * cache).
	 */
	public void removeLocal(Session session) {
		ClusteredSession clusterSess = (ClusteredSession) session;
		synchronized (clusterSess) {
			String realId = clusterSess.getRealId();
			if (realId == null)
				return;

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from local store with id: "
						+ realId);
			}

			try {
				// Ignore any cache notifications that our own work generates
				ConvergedSessionReplicationContext.startCacheActivity();
				clusterSess.removeMyselfLocal();
			} finally {
				ConvergedSessionReplicationContext.finishCacheActivity();

				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sessionExpired(clusterSess,
						realId, snapshotManager_);

				sessions_.remove(realId);
				stats_.removeStats(realId);

				// Update counters.
				// It's a bit ad-hoc to do it here. But since we currently call
				// this when session expires ...
				expiredCounter_++;
				activeCounter_--;
			}
		}
	}

	/**
	 * Removes the session from this Manager's collection of actively managed
	 * sessions. Also removes the session from this server's copy of the
	 * distributed cache (but does not remove it from other servers' distributed
	 * cache).
	 */
	public void removeLocal(SipSession session) {
		ClusteredSipSession clusterSess = (ClusteredSipSession) session;
		synchronized (clusterSess) {
			String realId = clusterSess.getId();
			if (realId == null)
				return;

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from local store with id: "
						+ realId);
			}

			try {
				// Ignore any cache notifications that our own work generates
				ConvergedSessionReplicationContext.startSipCacheActivity();
				clusterSess.removeMyselfLocal();
			} finally {
				ConvergedSessionReplicationContext.finishSipCacheActivity();

				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipSessionExpired(
						clusterSess, realId, snapshotManager_);

				sipManagerDelegate.removeSipSession(clusterSess.getKey());
				stats_.removeStats(realId);

				// Update counters.
				// It's a bit ad-hoc to do it here. But since we currently call
				// this when session expires ...
				expiredCounter_++;
				activeCounter_--;
			}
		}
	}

	/**
	 * Removes the session from this Manager's collection of actively managed
	 * sessions. Also removes the session from this server's copy of the
	 * distributed cache (but does not remove it from other servers' distributed
	 * cache).
	 */
	public void removeLocal(SipApplicationSession session) {
		ClusteredSipApplicationSession clusterSess = (ClusteredSipApplicationSession) session;
		synchronized (clusterSess) {
			String realId = clusterSess.getId();
			if (realId == null)
				return;

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from local store with id: "
						+ realId);
			}

			try {
				// Ignore any cache notifications that our own work generates
				ConvergedSessionReplicationContext.startSipCacheActivity();
				clusterSess.removeMyselfLocal();
			} finally {
				ConvergedSessionReplicationContext.finishSipCacheActivity();

				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext
						.sipApplicationSessionExpired(clusterSess, realId,
								snapshotManager_);

				sipManagerDelegate.removeSipApplicationSession(clusterSess.getKey());
				stats_.removeStats(realId);

				// Update counters.
				// It's a bit ad-hoc to do it here. But since we currently call
				// this when session expires ...
				expiredCounter_++;
				activeCounter_--;
			}
		}
	}

	/**
	 * Loads a session from the distributed store. If an existing session with
	 * the id is already under local management, that session's internal state
	 * will be updated from the distributed store. Otherwise a new session will
	 * be created and added to the collection of those sessions under local
	 * management.
	 * 
	 * @param realId
	 *            id of the session-id with any jvmRoute removed
	 * 
	 * @return the session or <code>null</code> if the session cannot be found
	 *         in the distributed store
	 * 
	 *         TODO refactor this into 2 overloaded methods -- one that takes a
	 *         ClusteredSession and populates it and one that takes an id,
	 *         creates the session and calls the first
	 */
	protected ClusteredSession loadSession(String realId) {
		if (realId == null) {
			return null;
		}

		long begin = System.currentTimeMillis();
		boolean mustAdd = false;
		ClusteredSession session = (ClusteredSession) sessions_.get(realId);
		if (session == null) {
			// This is either the first time we've seen this session on this
			// server, or we previously expired it and have since gotten
			// a replication message from another server
			mustAdd = true;
			session = createEmptyClusteredSession();
		}

		synchronized (session) {
			boolean doTx = false;
			try {
				// We need transaction so any data gravitation replication
				// is sent in batch.
				// Don't do anything if there is already transaction context
				// associated with this thread.
				if (tm.getTransaction() == null)
					doTx = true;

				if (doTx)
					tm.begin();

				// Ignore cache notifications we may generate for this
				// session if data gravitation occurs.
				ConvergedSessionReplicationContext.startCacheActivity();

				session = proxy_.loadSession(realId, session);
			} catch (Exception ex) {
				try {
					// if(doTx)
					// Let's set it no matter what.
					tm.setRollbackOnly();
				} catch (Exception exn) {
					log_.error("Problem rolling back session mgmt transaction",
							exn);
				}

				// We will need to alert Tomcat of this exception.
				if (ex instanceof RuntimeException)
					throw (RuntimeException) ex;

				throw new RuntimeException("Failed to load session " + realId,
						ex);
			} finally {
				try {
					if (doTx)
						endTransaction(realId);
				} finally {
					ConvergedSessionReplicationContext.finishCacheActivity();
				}
			}

			if (session != null) {
				// Need to initialize.
				session.initAfterLoad(this);
				if (mustAdd)
					add(session, false); // don't replicate
				long elapsed = System.currentTimeMillis() - begin;
				stats_.updateLoadStats(realId, elapsed);

				if (log_.isDebugEnabled()) {
					log_.debug("loadSession(): id= " + realId + ", session="
							+ session);
				}
			} else if (log_.isDebugEnabled()) {
				log_.debug("loadSession(): session " + realId
						+ " not found in distributed cache");
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
	 * @param realId
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
		ClusteredSipSession session = (ClusteredSipSession) sipManagerDelegate.getSipSession(key, create, sipFactory, sipApplicationSessionImpl);
		ClusteredSipSession newTempSession = session;
		if (session == null && sipApplicationSessionImpl != null) {
			// This is either the first time we've seen this session on this
			// server, or we previously expired it and have since gotten
			// a replication message from another server
			mustAdd = true;
			newTempSession = (ClusteredSipSession) sipManagerDelegate.getSipSession(key, true, sipFactory, sipApplicationSessionImpl);
		}		
		if(newTempSession != null) {
			synchronized (newTempSession) {
				ClusteredSipSession sessionInCache = null;
				boolean doTx = false;
				try {
					// We need transaction so any data gravitation replication
					// is sent in batch.
					// Don't do anything if there is already transaction context
					// associated with this thread.
					if (tm.getTransaction() == null)
						doTx = true;
	
					if (doTx)
						tm.begin();
	
					// Ignore cache notifications we may generate for this
					// session if data gravitation occurs.
					ConvergedSessionReplicationContext.startSipCacheActivity();
	
					sessionInCache = proxy_.loadSipSession(sipApplicationSessionImpl.getId(), key.toString(), newTempSession);
				} catch (Exception ex) {
					try {
						// if(doTx)
						// Let's set it no matter what.
						tm.setRollbackOnly();
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
					try {
						if (doTx)
							endTransaction(key.toString());
					} finally {
						ConvergedSessionReplicationContext.finishSipCacheActivity();
					}
				}
	
				if (sessionInCache != null) {
					// Need to initialize.
					sessionInCache.initAfterLoad(this);
					if (mustAdd)
						add(sessionInCache, false); // don't replicate
					long elapsed = System.currentTimeMillis() - begin;
					stats_.updateLoadStats(key.toString(), elapsed);
	
					if (log_.isDebugEnabled()) {
						log_.debug("loadSession(): id= " + key.toString() + ", session="
								+ newTempSession);
					}
					return sessionInCache;
				} else if (log_.isDebugEnabled()) {
					log_.debug("loadSession(): session " + key.toString()
							+ " not found in distributed cache");
				}
			}
		}
		if(session != null) {
			ConvergedSessionReplicationContext.bindSipSession(session,
				snapshotManager_);
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
	 * @param realId
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
		ClusteredSipApplicationSession session = (ClusteredSipApplicationSession) sipManagerDelegate.getSipApplicationSession(key, create);
		if (session == null) {			
			// This is either the first time we've seen this session on this
			// server, or we previously expired it and have since gotten
			// a replication message from another server
			mustAdd = true;
			session = (ClusteredSipApplicationSession) sipManagerDelegate.getSipApplicationSession(key, true);
		}		
		synchronized (session) {
			ClusteredSipApplicationSession sessionInCache = null;
			boolean doTx = false;
			try {
				// We need transaction so any data gravitation replication
				// is sent in batch.
				// Don't do anything if there is already transaction context
				// associated with this thread.
				if (tm.getTransaction() == null)
					doTx = true;

				if (doTx)
					tm.begin();

				// Ignore cache notifications we may generate for this
				// session if data gravitation occurs.
				ConvergedSessionReplicationContext.startSipCacheActivity();

				sessionInCache = proxy_.loadSipApplicationSession(key.toString(), session);
			} catch (Exception ex) {
				try {
					// if(doTx)
					// Let's set it no matter what.
					tm.setRollbackOnly();
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
				try {
					if (doTx)
						endTransaction(key.toString());
				} finally {
					ConvergedSessionReplicationContext.finishSipCacheActivity();
				}
			}

			if (sessionInCache != null) {
				// Need to initialize.
				sessionInCache.initAfterLoad(this);
				if (mustAdd)
					add(sessionInCache, false); // don't replicate
				long elapsed = System.currentTimeMillis() - begin;
				stats_.updateLoadStats(key.toString(), elapsed);

				if (log_.isDebugEnabled()) {
					log_.debug("loadSession(): id= " + key.toString() + ", session="
							+ sessionInCache);
				}
				return sessionInCache;
			} else if (log_.isDebugEnabled()) {
				log_.debug("loadSession(): session " + key.toString()
						+ " not found in distributed cache");				
			}
		}
		ConvergedSessionReplicationContext.bindSipApplicationSession(session,
				snapshotManager_);
		return session;
	}

	/**
	 * Places the current session contents in the distributed cache and
	 * replicates them to the cluster
	 * 
	 * @param session
	 *            the session. Cannot be <code>null</code>.
	 */
	protected void processSessionRepl(ClusteredSession session) {
		// If we are using SESSION granularity, we don't want to initiate a TX
		// for a single put
		boolean notSession = (replicationGranularity_ != WebMetaData.REPLICATION_GRANULARITY_SESSION);
		boolean doTx = false;
		try {
			// We need transaction so all the replication are sent in batch.
			// Don't do anything if there is already transaction context
			// associated with this thread.
			if (notSession && tm.getTransaction() == null)
				doTx = true;

			if (doTx)
				tm.begin();

			// Tell the proxy to ignore cache notifications we are about
			// to generate for this session. We have to do this
			// at this level because we don't want to resume handling
			// notifications until any compensating changes resulting
			// from a tx rollback are done.
			ConvergedSessionReplicationContext.startCacheActivity();

			session.processSessionRepl();
		} catch (Exception ex) {
			if (log_.isDebugEnabled())
				log_.debug("processSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					tm.setRollbackOnly();
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
			try {
				if (doTx)
					endTransaction(session.getId());
			} finally {
				ConvergedSessionReplicationContext.finishCacheActivity();
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
	protected void processSipSessionRepl(ClusteredSipSession session) {
		// If we are using SESSION granularity, we don't want to initiate a TX
		// for a single put
		boolean notSession = (replicationGranularity_ != WebMetaData.REPLICATION_GRANULARITY_SESSION);
		boolean doTx = false;
		try {
			// We need transaction so all the replication are sent in batch.
			// Don't do anything if there is already transaction context
			// associated with this thread.
			if (notSession && tm.getTransaction() == null)
				doTx = true;

			if (doTx)
				tm.begin();

			// Tell the proxy to ignore cache notifications we are about
			// to generate for this session. We have to do this
			// at this level because we don't want to resume handling
			// notifications until any compensating changes resulting
			// from a tx rollback are done.
			ConvergedSessionReplicationContext.startSipCacheActivity();

			session.processSessionRepl();
		} catch (Exception ex) {
			if (log_.isDebugEnabled())
				log_.debug("processSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					tm.setRollbackOnly();
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
			try {
				if (doTx)
					endTransaction(session.getId());
			} finally {
				ConvergedSessionReplicationContext.finishCacheActivity();
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
		// If we are using SESSION granularity, we don't want to initiate a TX
		// for a single put
		boolean notSession = (replicationGranularity_ != WebMetaData.REPLICATION_GRANULARITY_SESSION);
		boolean doTx = false;
		try {
			// We need transaction so all the replication are sent in batch.
			// Don't do anything if there is already transaction context
			// associated with this thread.
			if (notSession && tm.getTransaction() == null)
				doTx = true;

			if (doTx)
				tm.begin();

			// Tell the proxy to ignore cache notifications we are about
			// to generate for this session. We have to do this
			// at this level because we don't want to resume handling
			// notifications until any compensating changes resulting
			// from a tx rollback are done.
			ConvergedSessionReplicationContext.startSipCacheActivity();

			session.processSessionRepl();
		} catch (Exception ex) {
			if (log_.isDebugEnabled())
				log_.debug("processSessionRepl(): failed with exception", ex);

			try {
				// if(doTx)
				// Let's setRollbackOnly no matter what.
				// (except if there's no tx due to SESSION (JBAS-3840))
				if (notSession)
					tm.setRollbackOnly();
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
			try {
				if (doTx)
					endTransaction(session.getId());
			} finally {
				ConvergedSessionReplicationContext.finishCacheActivity();
			}
		}
	}

	protected void endTransaction(String id) {
		if (tm == null) {
			log_.warn("JBossCacheManager.endTransaction(): tm is null for id: "
					+ id);
			return;
		}

		try {
			if (tm.getTransaction().getStatus() != Status.STATUS_MARKED_ROLLBACK) {
				tm.commit();
			} else {
				log_
						.info("JBossCacheManager.endTransaction(): rolling back tx for id: "
								+ id);
				tm.rollback();
			}
		} catch (RollbackException re) {
			// Do nothing here since cache may rollback automatically.
			log_
					.warn("JBossCacheManager.endTransaction(): rolling back transaction with exception: "
							+ re);
		} catch (Exception e) {
			throw new RuntimeException(
					"JBossCacheManager.endTransaction(): Exception for id: "
							+ id, e);
		}
	}

	/**
	 * Gets the classloader of the webapp we are managing.
	 */
	protected ClassLoader getWebappClassLoader() {
		return tcl_;
	}

	/**
	 * Overrides the superclass version to add a check as to whether we should
	 * do trace level logging.
	 */
	@Override
	public void backgroundProcess() {
		trace = log_.isTraceEnabled();

		// Called from Catalina StandardEngine for every 60 seconds.

		long start = System.currentTimeMillis();

		processExpires();

		long elapsed = System.currentTimeMillis() - start;

		processingTime_ += elapsed;
	}

	/**
	 * Goes through all sessions and look if they have expired. Note this
	 * overrides the method in JBossManager.
	 */
	protected void processExpires() {
		if (maxInactiveInterval_ < 0) {
			return;
		}

		if (log_.isDebugEnabled()) {
			log_.debug("Looking for sessions that have expired ...");
		}

		try {
			// First, handle the sessions we are actively managing
			Session sessions[] = findLocalSessions();
			for (int i = 0; i < sessions.length; ++i) {
				try {
					ClusteredSession session = (ClusteredSession) sessions[i];
					if (session == null) {
						log_
								.warn("processExpires(): processing null session at index "
										+ i);
						continue;
					}

					// JBAS-2403. Check for outdated sessions where we think
					// the local copy has timed out. If found, refresh the
					// session from the cache in case that might change the
					// timeout
					if (session.isOutdated() && !(session.isValid(false))) {
						// JBAS-2792 don't assign the result of loadSession to
						// session
						// just update the object from the cache or fall through
						// if
						// the session has been removed from the cache
						loadSession(session.getRealId());
					}

					// Do a normal invalidation check that will expire any
					// sessions that have timed out
					// DON'T SYNCHRONIZE on session here -- isValid() and
					// expire() are meant to be multi-threaded and synchronize
					// properly internally; synchronizing externally can lead
					// to deadlocks!!
					if (!session.isValid())
						continue;
				} catch (Exception ex) {
					log_.error("processExpires(): failed expiring "
							+ sessions[i].getIdInternal() + " with exception: "
							+ ex, ex);
				}
			}

			// Next, handle any unloaded sessions that are stale

			long now = System.currentTimeMillis();
			Map unloaded = new HashMap(unloadedSessions_);
			Set entries = unloaded.entrySet();
			// We may have not gotten replication of a timestamp for requests 
			// that occurred w/in maxUnreplicatedInterval_ of the previous
			// request. So we add a grace period to avoid flushing a session
			// early
			// and permanently losing part of its node structure in JBoss Cache.
			long maxUnrep = maxUnreplicatedInterval_ < 0 ? 60
					: maxUnreplicatedInterval_;
			long maxUnused = maxInactiveInterval_ + maxUnrep;
			for (Iterator it = entries.iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				OwnedSessionUpdate osu = (OwnedSessionUpdate) entry.getValue();
				int elapsed = (int) ((now - osu.updateTime) / 1000L);
				if (elapsed >= maxUnused) {
					String realId = (String) entry.getKey();
					try {
						proxy_.removeSessionLocal(realId, osu.owner);
						unloadedSessions_.remove(realId);
					} catch (Exception ex) {
						log_
								.error(
										"processExpire(): failed removing unloaded session "
												+ realId + " with exception: "
												+ ex, ex);
					}
				}
			}
		} catch (Exception ex) {
			log_.error("processExpires: failed with exception: " + ex, ex);
		}
	}

	public void processRemoteAttributeRemoval(String realId, String attrKey) {

		ClusteredSession session = findLocalSession(realId);
		if (session != null) {
			boolean localCall = false; // call is due to remote event
			boolean localOnly = true; // don't call back into cache
			boolean notify = false; // SRV.10.7 gives us leeway
			// not to notify listeners,
			// which is safer

			// Ensure the correct TCL is in place
			ClassLoader prevTcl = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(tcl_);
				synchronized (session) {
					session.removeAttributeInternal(attrKey, localCall,
							localOnly, notify);
				}
				if (logger.isDebugEnabled())
					log_
							.debug("processRemoteAttributeRemoval: removed attribute "
									+ attrKey + " from " + realId);
			} finally {
				Thread.currentThread().setContextClassLoader(prevTcl);
			}
		}
	}
	
	public void processRemoteSipApplicationSessionAttributeRemoval(String realId, String attrKey) {
		SipApplicationSessionKey sipApplicationSessionKey;
		try {
			sipApplicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(realId);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip application session key " + realId, e);
			return;
		}
		ClusteredSipApplicationSession session = findLocalSipApplicationSession(sipApplicationSessionKey, false);
		if (session != null) {
			boolean localCall = false; // call is due to remote event
			boolean localOnly = true; // don't call back into cache
			boolean notify = false; // SRV.10.7 gives us leeway
			// not to notify listeners,
			// which is safer

			// Ensure the correct TCL is in place
			ClassLoader prevTcl = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(tcl_);
				synchronized (session) {
					session.removeAttributeInternal(attrKey, localCall,
							localOnly, notify);
				}
				if (logger.isDebugEnabled())
					log_
							.debug("processRemoteAttributeRemoval: removed attribute "
									+ attrKey + " from " + realId);
			} finally {
				Thread.currentThread().setContextClassLoader(prevTcl);
			}
		}
	}

	public void processRemoteSipSessionAttributeRemoval(String realId, String attrKey) {
		SipSessionKey sipSessionKey;
		try {
			sipSessionKey = SessionManagerUtil.parseSipSessionKey(realId);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + realId, e);
			return;
		}
		ClusteredSipSession session = findLocalSipSession(sipSessionKey, false, null);
		if (session != null) {
			boolean localCall = false; // call is due to remote event
			boolean localOnly = true; // don't call back into cache
			boolean notify = false; // SRV.10.7 gives us leeway
			// not to notify listeners,
			// which is safer

			// Ensure the correct TCL is in place
			ClassLoader prevTcl = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(tcl_);
				synchronized (session) {
					session.removeAttributeInternal(attrKey, localCall,
							localOnly, notify);
				}
				if (logger.isDebugEnabled())
					log_
							.debug("processRemoteAttributeRemoval: removed attribute "
									+ attrKey + " from " + realId);
			} finally {
				Thread.currentThread().setContextClassLoader(prevTcl);
			}
		}
	}
	
	public void processRemoteInvalidation(String realId) {
		// Remove the session from our local map
		ClusteredSession session = (ClusteredSession) sessions_.remove(realId);
		if (session == null) {
			// We weren't managing the session anyway. But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSessions_.remove(realId) != null) {
				if (logger.isDebugEnabled())
					log_.debug("Removed entry for session " + realId
							+ " from unloaded session map");
			}
		} else {
			// Expire the session
			// DON'T SYNCHRONIZE ON SESSION HERE -- isValid() and
			// expire() are meant to be multi-threaded and synchronize
			// properly internally; synchronizing externally can lead
			// to deadlocks!!
			boolean notify = false; // Don't notify listeners. SRV.10.7
			// allows this, and sending notifications
			// leads to all sorts of issues; e.g.
			// circular calls with ClusteredSSO
			boolean localCall = false; // this call originated from the cache;
			// we have already removed session
			boolean localOnly = true; // Don't pass attr removals to cache

			// Ensure the correct TCL is in place
			ClassLoader prevTcl = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(tcl_);
				session.expire(notify, localCall, localOnly);
			} finally {
				Thread.currentThread().setContextClassLoader(prevTcl);
			}

			// Remove any stats for this session
			stats_.removeStats(realId);

			// Update counter.
			activeCounter_--;
		}
	}

	public void processRemoteSipApplicationSessionInvalidation(String realId) {
		// Remove the session from our local map
		SipApplicationSessionKey sipApplicationSessionKey;
		try {
			sipApplicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(realId);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip application session key " + realId, e);
			return;
		}
		ClusteredSipApplicationSession session = (ClusteredSipApplicationSession) 
			sipManagerDelegate.removeSipApplicationSession(sipApplicationSessionKey);
		if (session == null) {
			// We weren't managing the session anyway. But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSipApplicationSessions_.remove(realId) != null) {
				if (logger.isDebugEnabled())
					log_.debug("Removed entry for session " + realId
							+ " from unloaded session map");
			}
		} else {
			// Expire the session
			// DON'T SYNCHRONIZE ON SESSION HERE -- isValid() and
			// expire() are meant to be multi-threaded and synchronize
			// properly internally; synchronizing externally can lead
			// to deadlocks!!
			boolean notify = false; // Don't notify listeners. SRV.10.7
			// allows this, and sending notifications
			// leads to all sorts of issues; e.g.
			// circular calls with ClusteredSSO
			boolean localCall = false; // this call originated from the cache;
			// we have already removed session
			boolean localOnly = true; // Don't pass attr removals to cache

			// Ensure the correct TCL is in place
			ClassLoader prevTcl = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(tcl_);
				session.expire(notify, localCall, localOnly);
			} finally {
				Thread.currentThread().setContextClassLoader(prevTcl);
			}

			// Remove any stats for this session
			stats_.removeStats(realId);

			// Update counter.
			activeCounter_--;
		}
	}
	
	public void processRemoteSipSessionInvalidation(String realId) {
		// Remove the session from our local map
		SipSessionKey sipSessionKey;
		try {
			sipSessionKey = SessionManagerUtil.parseSipSessionKey(realId);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + realId, e);
			return;
		}
		ClusteredSipSession session = (ClusteredSipSession) sipManagerDelegate.removeSipSession(sipSessionKey);
		if (session == null) {
			// We weren't managing the session anyway. But remove it
			// from the list of cached sessions we haven't loaded
			if (unloadedSipSessions_.remove(realId) != null) {
				if (logger.isDebugEnabled())
					log_.debug("Removed entry for session " + realId
							+ " from unloaded session map");
			}
		} else {
			//remove the dialog from the local stacks 
			session.getSessionCreatingDialog().delete();
			
			// Expire the session
			// DON'T SYNCHRONIZE ON SESSION HERE -- isValid() and
			// expire() are meant to be multi-threaded and synchronize
			// properly internally; synchronizing externally can lead
			// to deadlocks!!
			boolean notify = false; // Don't notify listeners. SRV.10.7
			// allows this, and sending notifications
			// leads to all sorts of issues; e.g.
			// circular calls with ClusteredSSO
			boolean localCall = false; // this call originated from the cache;
			// we have already removed session
			boolean localOnly = true; // Don't pass attr removals to cache

			// Ensure the correct TCL is in place
			ClassLoader prevTcl = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(tcl_);
				session.expire(notify, localCall, localOnly);
			} finally {
				Thread.currentThread().setContextClassLoader(prevTcl);
			}

			// Remove any stats for this session
			stats_.removeStats(realId);

			// Update counter.
			activeCounter_--;
		}
	}

	public void processLocalPojoModification(String realId) {
		ClusteredSession session = findLocalSession(realId);
		if (session != null) {
			if (logger.isDebugEnabled()) {
				log_.debug("Marking attributes of session " + realId
						+ " dirty due to POJO modification");
			}
			session.sessionAttributesDirty();
		}
	}
	
	public void processSipApplicationSessionLocalPojoModification(String realId) {
		SipApplicationSessionKey sipApplicationSessionKey;
		try {
			sipApplicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(realId);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip application session key " + realId, e);
			return;
		}
		ClusteredSipApplicationSession session = findLocalSipApplicationSession(sipApplicationSessionKey, false);
		if (session != null) {
			if (logger.isDebugEnabled()) {
				log_.debug("Marking attributes of session " + realId
						+ " dirty due to POJO modification");
			}
			session.sessionAttributesDirty();
		}
	}
	
	public void processSipSessionLocalPojoModification(String realId) {
		SipSessionKey sipSessionKey;
		try {
			sipSessionKey = SessionManagerUtil.parseSipSessionKey(realId);
		} catch (ParseException e) {
			logger.error("Unexpected exception while parsing the following sip session key " + realId, e);
			return;
		}
		ClusteredSipSession session = findLocalSipSession(sipSessionKey, false, null);
		if (session != null) {
			if (logger.isDebugEnabled()) {
				log_.debug("Marking attributes of session " + realId
						+ " dirty due to POJO modification");
			}
			session.sessionAttributesDirty();
		}
	}

	/**
	 * Gets the session id with any jvmRoute removed.
	 * 
	 * @param id
	 *            a session id with or without an appended jvmRoute. Cannot be
	 *            <code>null</code>.
	 */
	protected String getRealId(String id) {
		return (useJK_ ? Util.getRealId(id) : id);
	}

	/**
	 * Callback from the CacheListener to notify us that a session we haven't
	 * loaded has been changed.
	 * 
	 * @param realId
	 *            the session id, without any trailing jvmRoute
	 * @param dataOwner
	 *            the owner of the session. Can be <code>null</code> if the
	 *            owner is unknown.
	 */
	protected void unloadedSessionChanged(String realId, String dataOwner) {
		Object obj = unloadedSessions_.put(realId, new OwnedSessionUpdate(
				dataOwner, System.currentTimeMillis()));
		if (logger.isDebugEnabled()) {
			if (obj == null) {
				log_.debug("New session " + realId
						+ " added to unloaded session map");
			} else {
				log_.debug("Updated timestamp for unloaded session " + realId);
			}
		}
	}
	
	/**
	 * Callback from the CacheListener to notify us that a session we haven't
	 * loaded has been changed.
	 * 
	 * @param realId
	 *            the session id, without any trailing jvmRoute
	 * @param dataOwner
	 *            the owner of the session. Can be <code>null</code> if the
	 *            owner is unknown.
	 */
	protected void unloadedSipSessionChanged(String realId, String dataOwner) {
		Object obj = unloadedSipSessions_.put(realId, new OwnedSessionUpdate(
				dataOwner, System.currentTimeMillis()));
		if (logger.isDebugEnabled()) {
			if (obj == null) {
				log_.debug("New session " + realId
						+ " added to unloaded session map");
			} else {
				log_.debug("Updated timestamp for unloaded session " + realId);
			}
		}
	}
	
	/**
	 * Callback from the CacheListener to notify us that a session we haven't
	 * loaded has been changed.
	 * 
	 * @param realId
	 *            the session id, without any trailing jvmRoute
	 * @param dataOwner
	 *            the owner of the session. Can be <code>null</code> if the
	 *            owner is unknown.
	 */
	protected void unloadedSipApplicationSessionChanged(String realId, String dataOwner) {
		Object obj = unloadedSessions_.put(realId, new OwnedSessionUpdate(
				dataOwner, System.currentTimeMillis()));
		if (logger.isDebugEnabled()) {
			if (obj == null) {
				log_.debug("New session " + realId
						+ " added to unloaded session map");
			} else {
				log_.debug("Updated timestamp for unloaded session " + realId);
			}
		}
	}

	// ---------------------------------------------------- Lifecyle Unembedded

	/**
	 * Start this Manager when running embedded in JBoss AS.
	 * 
	 * @throws org.apache.catalina.LifecycleException
	 */
	private void startEmbedded() throws LifecycleException {
		startManager();

		// Start the JBossCacheService
		// Will need to pass the classloader that is associated with this
		// web app so de-serialization will work correctly.
		tcl_ = super.getContainer().getLoader().getClassLoader();

		proxy_.start(tcl_, this);

		tm = proxy_.getTransactionManager();
		if (tm == null) {
			throw new LifecycleException(
					"JBossCacheManager.start(): Obtain null tm");
		}

		try {
			initializeUnloadedSessions();
//			initializeUnloadedSipSessions();
			initializeUnloadedSipApplicationSessions();

			// Setup our SnapshotManager
			initSnapshotManager();

			// Add SnapshotValve and, if needed, JvmRouteValve and batch repl
			// valve
			installValves();

			log_.debug("start(): JBossCacheService started");
		} catch (Exception e) {
			log_.error("Unable to start manager.", e);
			throw new LifecycleException(e);
		}
	}

	// ----------------------------------------------- Lifecyle When Unembedded

	/**
	 * Start this Manager when running in standalone Tomcat.
	 */
	private void startUnembedded() throws LifecycleException {
		if (started_) {
			return;
		}

		if (log_.isInfoEnabled()) {
			log_.info("Manager is about to start");
		}

		// Notify our interested LifecycleListeners
		lifecycle_.fireLifecycleEvent(BEFORE_START_EVENT, this);

		if (snapshotMode_ == null) {
			// We were not instantiated by a JBossCacheCluster, so we need to
			// find one and let it configure our cluster-wide properties
			try {
				JBossCacheCluster cluster = (JBossCacheCluster) container_
						.getCluster();
				cluster.configureManager(this);
			} catch (ClassCastException e) {
				String msg = "Cluster is not an instance of JBossCacheCluster";
				log_.error(msg, e);
				throw new LifecycleException(msg, e);
			}
		}

		// Validate attributes

		if ("SET_AND_GET".equalsIgnoreCase(replTriggerString_))
			this.invalidateSessionPolicy_ = WebMetaData.SESSION_INVALIDATE_SET_AND_GET;
		else if ("SET_AND_NON_PRIMITIVE_GET"
				.equalsIgnoreCase(replTriggerString_))
			this.invalidateSessionPolicy_ = WebMetaData.SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET;
		else if ("SET".equalsIgnoreCase(replTriggerString_))
			this.invalidateSessionPolicy_ = WebMetaData.SESSION_INVALIDATE_SET;
		else
			throw new LifecycleException("replication-trigger value set to a "
					+ "non-valid value: '" + replTriggerString_
					+ "' (should be ['SET_AND_GET', "
					+ "'SET_AND_NON_PRIMITIVE_GET', 'SET'])");

		if ("SESSION".equalsIgnoreCase(replGranularityString_))
			this.replicationGranularity_ = WebMetaData.REPLICATION_GRANULARITY_SESSION;
		else if ("ATTRIBUTE".equalsIgnoreCase(replGranularityString_))
			this.replicationGranularity_ = WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE;
		else if ("FIELD".equalsIgnoreCase(replGranularityString_))
			this.replicationGranularity_ = WebMetaData.REPLICATION_GRANULARITY_FIELD;
		else
			throw new LifecycleException(
					"replication-granularity value set to "
							+ "a non-valid value: '" + replGranularityString_
							+ "' (should be ['SESSION', "
							+ "'ATTRIBUTE' or 'FIELD'])");

		// Create the JBossCacheService
		try {
			proxy_ = new ConvergedJBossCacheService(cacheObjectNameString_);

			// Confirm our replication granularity is compatible with the cache
			// Throws ISE if not
			validateFieldMarshalling();

			// We need to pass the classloader that is associated with this
			// web app so de-serialization will work correctly.
			tcl_ = container_.getLoader().getClassLoader();
			proxy_.start(tcl_, this);
		} catch (Throwable t) {
			String str = "Problem starting JBossCacheService for Tomcat clustering";
			log_.error(str, t);
			throw new LifecycleException(str, t);
		}

		tm = proxy_.getTransactionManager();
		if (tm == null) {
			throw new LifecycleException(
					"JBossCacheManager.start(): Obtain null tm");
		}

		try {
			initializeUnloadedSessions();
//			initializeUnloadedSipSessions();
			initializeUnloadedSipApplicationSessions();

			// Add SnapshotValve and, if needed, JvmRouteValve and batch repl
			// valve
			installValves();

			started_ = true;

			// Notify our interested LifecycleListeners
			lifecycle_.fireLifecycleEvent(AFTER_START_EVENT, this);

			if (log_.isDebugEnabled()) {
				log_.debug("start(): JBossCacheService started");
			}
		} catch (Exception e) {
			log_.error("Unable to start manager.", e);
			throw new LifecycleException(e);
		}

		try {
			registerMBeans();
		} catch (Exception e) {
			log_.error("Could not register ManagerMBean with MBeanServer", e);
		}
	}

	/**
	 * Register this Manager with JMX.
	 */
	private void registerMBeans() {
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
					+ ":service=ClusterManager,WebModule=//" + hostName
					+ ((Context) container_).getPath());

			if (server.isRegistered(clusterName)) {
				log_.warn("MBean " + clusterName + " already registered");
				return;
			}

			objectName_ = clusterName;
			server.registerMBean(this, clusterName);

		} catch (Exception ex) {
			log_.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Unregister this Manager from the JMX server.
	 */
	private void unregisterMBeans() {
		if (mserver_ != null) {
			try {
				mserver_.unregisterMBean(objectName_);
			} catch (Exception e) {
				log_.error(e);
			}
		}
	}

	/**
	 * Get the current MBean Server.
	 * 
	 * @return
	 * @throws Exception
	 */
	private MBeanServer getMBeanServer() throws Exception {
		if (mserver_ == null) {
			mserver_ = MBeanServerLocator.locateJBoss();
		}
		return (mserver_);
	}

	/**
	 * Gets the ids of all sessions in the distributed cache and adds them to
	 * the unloaded sessions map, with the current time as the last replication
	 * time. This means these sessions may not be evicted from the cache for a
	 * period well beyond when they would normally expire, but this is a
	 * necessary tradeoff to avoid deserializing them all to check their
	 * lastAccessedTime.
	 */
	private void initializeUnloadedSessions() throws CacheException {
		Map sessions = proxy_.getSessionIds();
		if (sessions != null) {
			long now = System.currentTimeMillis();
			for (Iterator it = sessions.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Entry) it.next();
				unloadedSessions_.put(entry.getKey(), new OwnedSessionUpdate(
						(String) entry.getValue(), now));
			}
		}
	}
	
	/**
	 * Gets the ids of all sessions in the distributed cache and adds them to
	 * the unloaded sessions map, with the current time as the last replication
	 * time. This means these sessions may not be evicted from the cache for a
	 * period well beyond when they would normally expire, but this is a
	 * necessary tradeoff to avoid deserializing them all to check their
	 * lastAccessedTime.
	 */
//	private void initializeUnloadedSipSessions() throws CacheException {
//		Map sessions = proxy_.getSipSessionIds();
//		if (sessions != null) {
//			long now = System.currentTimeMillis();
//			for (Iterator it = sessions.entrySet().iterator(); it.hasNext();) {
//				Map.Entry entry = (Entry) it.next();
//				unloadedSipSessions_.put(entry.getKey(), new OwnedSessionUpdate(
//						(String) entry.getValue(), now));
//			}
//		}
//	}
//	
	/**
	 * Gets the ids of all sessions in the distributed cache and adds them to
	 * the unloaded sessions map, with the current time as the last replication
	 * time. This means these sessions may not be evicted from the cache for a
	 * period well beyond when they would normally expire, but this is a
	 * necessary tradeoff to avoid deserializing them all to check their
	 * lastAccessedTime.
	 */
	private void initializeUnloadedSipApplicationSessions() throws CacheException {
		Map sessions = proxy_.getSipApplicationSessionIds();
		if (sessions != null) {
			long now = System.currentTimeMillis();
			for (Iterator it = sessions.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Entry) it.next();
				unloadedSipApplicationSessions_.put(entry.getKey(), new OwnedSessionUpdate(
						(String) entry.getValue(), now));
			}
		}
	}

	/**
	 * Instantiate a SnapshotManager and ClusteredSessionValve and add the valve
	 * to our parent Context's pipeline. Add a JvmRouteValve and
	 * BatchReplicationClusteredSessionValve if needed.
	 * 
	 */
	private void installValves() {
		if (useJK_) {
			log_.info("We are using mod_jk(2) for load-balancing. "
					+ "Will add JvmRouteValve.");

			installContextValve(new ConvergedJvmRouteValve(this));
		}

		// Add batch replication valve if needed.
		// TODO -- should we add this even if not FIELD in case a cross-context
		// call traverses a field-based webapp?
		if (replicationGranularity_ == WebMetaData.REPLICATION_GRANULARITY_FIELD
				&& Boolean.TRUE.equals(replicationFieldBatchMode_)) {
			Valve batchValve = new BatchReplicationClusteredSessionValve(this);
			log_
					.debug("Adding BatchReplicationClusteredSessionValve for batch replication.");
			installContextValve(batchValve);
		}

		// Add clustered session valve
		ConvergedClusteredSessionValve valve = new ConvergedClusteredSessionValve(this);
		installContextValve(valve);
	}

	/**
	 * Create and start a snapshot manager.
	 */
	private void initSnapshotManager() {
		String ctxPath = ((Context) container_).getPath();
		if ("instant".equals(snapshotMode_)
				|| replicationGranularity_ == WebMetaData.REPLICATION_GRANULARITY_FIELD) {
			snapshotManager_ = new InstantConvergedSnapshotManager(this, ctxPath);
		} else if ("interval".equals(snapshotMode_)) {
			snapshotManager_ = new IntervalConvergedSnapshotManager(this, ctxPath,
					snapshotInterval_);
		} else {
			log_.error("Snapshot mode must be 'instant' or 'interval' - "
					+ "using 'instant'");
			snapshotManager_ = new InstantConvergedSnapshotManager(this, ctxPath);
		}

		snapshotManager_.start();
	}

	private void installContextValve(Valve valve) {
		boolean installed = false;

		// In embedded mode, install the valve via JMX to be consistent
		// with the way the overall context is created in TomcatDeployer.
		// We can't do this in unembedded mode because we are called
		// before our Context is registered with the MBean server
		if (embedded_ && getContextObjectName() != null) {
			try {
				getMBeanServer().invoke(getContextObjectName(), "addValve",
						new Object[] { valve },
						new String[] { "org.apache.catalina.Valve" });
				installed = true;
			} catch (Exception e) {
				// JBAS-2422. If the context is restarted via JMX, the above
				// JMX call will fail as the context will not be registered
				// when it's made. So we catch the exception and fall back
				// to adding the valve directly.
				// TODO consider skipping adding via JMX and just do it directly
				log_.debug("Caught exception installing valve to Context", e);
			}
		}

		if (!installed) {
			// If possible install via the ContainerBase.addValve() API.
			if (container_ instanceof ContainerBase) {
				((ContainerBase) container_).addValve(valve);
			} else {
				// No choice; have to add it to the context's pipeline
				container_.getPipeline().addValve(valve);
			}
		}
	}

	/**
	 * If we are using FIELD granularity, checks that the TreeCache supports
	 * marshalling.
	 * 
	 * @throws IllegalStateException
	 *             if not
	 */
	private void validateFieldMarshalling() {
		if (replicationGranularity_ == WebMetaData.REPLICATION_GRANULARITY_FIELD
				&& !proxy_.isMarshallingAvailable()) {
			// BES 16/8/2006 -- throw ISE, not ClusteringNotSupportedException,
			// as a
			// misconfig should be treated differently from the absence of
			// clustering
			// services
			throw new IllegalStateException(
					"replication-granularity value is set to "
							+ "'FIELD' but is not supported by the cache service configuration. "
							+ "Must set 'UseRegionBasedMarshalling' to 'true' in the tc5-cluster.sar jboss-service.xml");
		}
	}

	private ObjectName getContextObjectName() {
		String oname = container_.getObjectName();
		try {
			return (oname == null) ? null : new ObjectName(oname);
		} catch (MalformedObjectNameException e) {
			log_.warn("Error creating object name from string " + oname, e);
			return null;
		}
	}

	private class OwnedSessionUpdate {
		String owner;
		long updateTime;

		OwnedSessionUpdate(String owner, long updateTime) {
			this.owner = owner;
			this.updateTime = updateTime;
		}
	}

	public Session createEmptySession() {
		return createEmptyClusteredSession();
	}

	private ClusteredSession createEmptyClusteredSession() {
		log_.debug("Creating an empty ClusteredSession");

		ClusteredSession session = null;
		switch (replicationGranularity_) {
		case (WebMetaData.REPLICATION_GRANULARITY_ATTRIBUTE): {
			if (super.container_ instanceof SipContext) {
				session = new ConvergedAttributeBasedClusteredSession(this);
			} else {
				session = new AttributeBasedClusteredSession(this);
			}
			break;
		}
		case (WebMetaData.REPLICATION_GRANULARITY_FIELD): {
			if (super.container_ instanceof SipContext) {
				session = new ConvergedFieldBasedClusteredSession(this);
			} else {
				session = new FieldBasedClusteredSession(this);
			}
			break;
		}
		default:
			if (super.container_ instanceof SipContext) {
				session = new ConvergedSessionBasedClusteredSession(this);
			} else {
				session = new SessionBasedClusteredSession(this);
			}
			break;
		}
		return session;
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
	public void setContainer(Container container) {
		container_ = container;
		sipManagerDelegate.setContainer(container);
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipSession removeSipSession(final SipSessionKey key) {
		ClusteredSipSession clusterSess = (ClusteredSipSession) sipManagerDelegate.removeSipSession(key);
		if(clusterSess == null) {
			return null;
		}
		synchronized (clusterSess) {
			String realId = clusterSess.getId();

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from store with id: " + realId);
			}

			try {
				// Ignore any cache notifications that our own work generates
				ConvergedSessionReplicationContext.startSipCacheActivity();
				clusterSess.removeMyself();
			} finally {
				ConvergedSessionReplicationContext.finishSipCacheActivity();

				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipSessionExpired(clusterSess,
						realId, snapshotManager_);

				stats_.removeStats(realId);
				activeCounter_--;
			}
		}
		return clusterSess;
	}

	/**
	 * {@inheritDoc}
	 */
	public MobicentsSipApplicationSession removeSipApplicationSession(
			final SipApplicationSessionKey key) {
		ClusteredSipApplicationSession clusterSess = (ClusteredSipApplicationSession) sipManagerDelegate.removeSipApplicationSession(key);
		if(clusterSess == null) {
			return null;
		}
		synchronized (clusterSess) {
			String realId = clusterSess.getId();

			if (log_.isDebugEnabled()) {
				log_.debug("Removing session from store with id: " + realId);
			}

			try {
				// Ignore any cache notifications that our own work generates
				ConvergedSessionReplicationContext.startSipCacheActivity();
				clusterSess.removeMyself();
			} finally {
				ConvergedSessionReplicationContext.finishSipCacheActivity();

				// We don't want to replicate this session at the end
				// of the request; the removal process took care of that
				ConvergedSessionReplicationContext.sipApplicationSessionExpired(clusterSess,
						realId, snapshotManager_);

				stats_.removeStats(realId);
				activeCounter_--;
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
		ClusteredSipApplicationSession session = findLocalSipApplicationSession(key, false);

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
				&& !ConvergedSessionReplicationContext
						.isSipApplicationSessionBoundAndExpired(key.toString(), snapshotManager_)) {
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
					snapshotManager_);
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
		ClusteredSipSession session = findLocalSipSession(key, false, sipApplicationSessionImpl);

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
		boolean isSipSessionBoundAndExpired = ConvergedSessionReplicationContext
		 	.isSipSessionBoundAndExpired(key.toString(), snapshotManager_);
		if (logger.isDebugEnabled()) {
			logger.debug("sip session " + key
					+ " bound and expired ? " + isSipSessionBoundAndExpired);
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
				&& !isSipSessionBoundAndExpired) {
			if (logger.isDebugEnabled())
				logger.debug("Checking for sip session " + key
						+ " in the distributed cache");

			session = loadSipSession(key, create, sipFactoryImpl, sipApplicationSessionImpl);
			if (session != null) {
				add(session);
				// TODO should we advise of a new session?
				// tellNew();
			}
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
					snapshotManager_);
		}

		return session;
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

	/**
	 * @return the snapshotManager_
	 */
	public SnapshotManager getSnapshotManager() {
		return snapshotManager_;
	}
	
	// JMX Statistics
	/**
	 * Return descriptive information about this Manager implementation and the
	 * corresponding version number, in the format
	 * <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	public String getInfo() {
		return (info);
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
}
