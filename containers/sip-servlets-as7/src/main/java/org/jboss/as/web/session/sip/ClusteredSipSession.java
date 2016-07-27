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

package org.jboss.as.web.session.sip;

import static org.jboss.as.web.WebMessages.MESSAGES;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.SipStack;
import javax.sip.Transaction;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Service;
import org.apache.catalina.util.Enumerator;
import org.apache.log4j.Logger;
import org.jboss.as.clustering.web.DistributedCacheManager;
import org.jboss.as.clustering.web.IncomingDistributableSessionData;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.jboss.as.clustering.web.SessionOwnershipSupport;
import org.jboss.as.clustering.web.sip.DistributableSipSessionMetadata;
import org.jboss.as.clustering.web.sip.DistributedCacheConvergedSipManager;
import org.jboss.as.web.session.ClusteredSession;
import org.jboss.as.web.session.notification.ClusteredSessionManagementStatus;
import org.jboss.as.web.session.notification.ClusteredSessionNotificationCause;
import org.jboss.as.web.session.notification.sip.ClusteredSipSessionNotificationPolicy;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.ha.javax.sip.HASipDialog;
import org.mobicents.ha.javax.sip.ReplicationStrategy;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.B2buaHelperImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

import org.mobicents.servlet.sip.notification.SipSessionActivationEvent;
import org.mobicents.servlet.sip.notification.SessionActivationNotificationCause;

import gov.nist.javax.sip.message.RequestExt;

/**
 * Abstract base class for sip session clustering based on SipSessionImpl. Different session
 * replication strategy can be implemented such as session- field- or attribute-based ones.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * @author posfai.gergely@ext.alerant.hu
 */
public abstract class ClusteredSipSession<O extends OutgoingDistributableSessionData> extends SipSessionImpl {

	private static final Logger logger = Logger.getLogger(ClusteredSipSession.class);

	protected static final String B2B_SESSION_MAP = "b2bsm";
	protected static final String B2B_SESSION_SIZE = "b2bss";
	protected static final String B2B_LINKED_REQUESTS_MAP = "b2blrm";
	protected static final String B2B_LINKED_REQUESTS_SIZE = "b2blrs";
	protected static final String TO_TAG = "tt";
	protected static final String TXS_SIZE = "txm";
	protected static final String TXS_IDS= "txid";
	protected static final String TXS_TYPE= "txt";
	protected static final String ACKS_RECEIVED_SIZE = "ars";
	protected static final String ACKS_RECEIVED_CSEQ = "arc";
	protected static final String ACKS_RECEIVED_VALUE= "arv";
	protected static final String PROXY = "prox";
	protected static final String DIALOG_ID = "did";
	protected static final String READY_TO_INVALIDATE = "rti";
	protected static final String INVALIDATE_WHEN_READY = "iwr";
	protected static final String CSEQ = "cseq";
	protected static final String STATE = "stat";
	protected static final String IS_VALID = "iv";
	protected static final String HANDLER = "hler";
	protected static final String INVALIDATION_POLICY = "ip";
	protected static final String CREATION_TIME = "ct";
	protected static final String TRANSPORT = "tp";
	
	protected static final boolean ACTIVITY_CHECK = 
	      Globals.STRICT_SERVLET_COMPLIANCE
	      || Boolean.valueOf(System.getProperty("org.apache.catalina.session.StandardSession.ACTIVITY_CHECK", "false")).booleanValue();
	
	/**
	 * Descriptive information describing this Session implementation.
	 */
	protected static final String info = "ClusteredSipSession/1.0";

	/**
	 * Set of attribute names which are not allowed to be replicated/persisted.
	 */
	protected static final String[] excludedAttributes = { Globals.SUBJECT_ATTR };

	/**
	 * Set containing all members of {@link #excludedAttributes}.
	 */
	protected static final Set<String> replicationExcludes;
	static {
		Set<String> set = new HashSet<String>();
		for (int i = 0; i < excludedAttributes.length; i++) {
			set.add(excludedAttributes[i]);
		}
		replicationExcludes = Collections.unmodifiableSet(set);
	}
	
	/**
	 * The method signature for the <code>fireContainerEvent</code> method.
	 */
	protected static final Class<?> containerEventTypes[] = { String.class,
			Object.class };

	protected static final Logger log = Logger
			.getLogger(ClusteredSession.class);

	// ----------------------------------------------------- Instance Variables

	/**
	 * The collection of user data attributes associated with this Session.
	 */
	private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>(
			16, 0.75f, 2);

	/**
	 * The authentication type used to authenticate our cached Principal, if
	 * any. NOTE: This value is not included in the serialized version of this
	 * object.
	 */
	private transient String authType = null;

	/**
	 * The <code>java.lang.Method</code> for the
	 * <code>fireContainerEvent()</code> method of the
	 * <code>org.apache.catalina.core.StandardContext</code> method, if our
	 * Context implementation is of this class. This value is computed
	 * dynamically the first time it is needed, or after a session reload (since
	 * it is declared transient).
	 */
	private transient Method containerEventMethod = null;			

	/**
	 * The Manager with which this Session is associated.
	 */
	private transient ClusteredSipSessionManager<O> manager = null;

	/**
	 * Our proxy to the distributed cache.
	 */
	private transient DistributedCacheConvergedSipManager<O> distributedCacheManager;

	/**
	 * The maximum time interval, in seconds, between client requests before the
	 * servlet container may invalidate this session. A negative time indicates
	 * that the session should never time out.
	 */
	private int maxInactiveInterval = -1;

	/**
	 * Flag indicating whether this session is new or not.
	 */
	private boolean isNew = false;
	
	/**
	 * Internal notes associated with this session by Catalina components and
	 * event listeners. <b>IMPLEMENTATION NOTE:</b> This object is <em>not</em>
	 * saved and restored across session serializations!
	 */
	private final transient Map<String, Object> notes = new Hashtable<String, Object>();

	/**
	 * The property change support for this component. NOTE: This value is not
	 * included in the serialized version of this object.
	 */
	 private transient PropertyChangeSupport support =
		 new PropertyChangeSupport(this);
	 
	 /**
	  * The current accessed time for this session.
	  */
	 private volatile long thisAccessedTime = creationTime;


	/**
	 * The access count for this session.
	 */
	private final transient AtomicInteger accessCount;

	/**
	 * Policy controlling whether reading/writing attributes requires
	 * replication.
	 */
	private ReplicationTrigger invalidationPolicy;

	/**
	 * If true, means the local in-memory session data contains metadata changes
	 * that have not been published to the distributed cache.
	 */
	private transient boolean sessionMetadataDirty;

	/**
	 * If true, means the local in-memory session data contains attribute
	 * changes that have not been published to the distributed cache.
	 */
	private transient boolean sessionAttributesDirty;

	/**
	 * Object wrapping thisAccessedTime. Create once and mutate so we can store
	 * it in JBoss Cache w/o concern that a transaction rollback will revert the
	 * cached ref to an older object.
	 */
	private final transient AtomicLong timestamp = new AtomicLong(0);

	/**
	 * Object wrapping other metadata for this session. Create once and mutate
	 * so we can store it in JBoss Cache w/o concern that a transaction rollback
	 * will revert the cached ref to an older object.
	 */
	private volatile transient DistributableSipSessionMetadata metadata;

	/**
	 * The last version that was passed to {@link #setDistributedVersion} or
	 * <code>0</code> if <code>setIsOutdated(false)</code> was subsequently
	 * called.
	 */
	private volatile transient int outdatedVersion;

	/**
	 * The last time {@link #setIsOutdated setIsOutdated(true)} was called or
	 * <code>0</code> if <code>setIsOutdated(false)</code> was subsequently
	 * called.
	 */
	private volatile transient long outdatedTime;

	/**
	 * Version number to track cache invalidation. If any new version number is
	 * greater than this one, it means the data it holds is newer than this one.
	 */
	private final AtomicInteger version = new AtomicInteger(0);

	/**
	 * Whether JK is being used, in which case our realId will not match our id
	 */
	private transient boolean useJK;

	/**
	 * Timestamp when we were last replicated.
	 */
	private volatile transient long lastReplicated;

	/**
	 * Maximum number of milliseconds this session should be allowed to go
	 * unreplicated if access to the session doesn't mark it as dirty.
	 */
	private transient long maxUnreplicatedInterval;

	/** True if maxUnreplicatedInterval is 0 or less than maxInactiveInterval */
	private transient boolean alwaysReplicateTimestamp = false;

	/**
	 * Whether any of this session's attributes implement
	 * HttpSessionActivationListener.
	 */
	private transient Boolean hasActivationListener;

	/**
	 * Has this session only been accessed once?
	 */
	private transient boolean firstAccess;

	/**
	 * Policy that drives whether we issue servlet spec notifications.
	 */
	//FIXME move the notfication policy to a SIP based one and not HTTP
	private transient ClusteredSipSessionNotificationPolicy notificationPolicy;
	private transient ClusteredSessionManagementStatus clusterStatus;

	/**
	 * True if a call to activate() is needed to offset a preceding passivate()
	 * call
	 */
	private transient boolean needsPostReplicateActivation;

	protected transient SipApplicationSessionKey sipAppSessionParentKey;
	
	protected transient String sessionCreatingDialogId = null;
	
	private transient String haId;
	
	/** Coordinate updates from the cluster */
    private transient Lock ownershipLock = new ReentrantLock();
    
	// ------------------------------------------------------------ Constructors

	protected ClusteredSipSession(SipSessionKey key, SipFactoryImpl sipFactoryImpl, MobicentsSipApplicationSession mobicentsSipApplicationSession, boolean useJK) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession);	
		haId = SessionManagerUtil.getSipSessionHaKey(key);
	    this.clusterStatus = new ClusteredSessionManagementStatus(haId, true, null, null);
		if(mobicentsSipApplicationSession.getSipContext() != null) {
			setManager((ClusteredSipSessionManager<O>)mobicentsSipApplicationSession.getSipContext().getSipManager());
			this.invalidationPolicy = this.manager.getReplicationTrigger();
		}		
		this.isNew = true;
		this.useJK = useJK;
		this.firstAccess = true;
		updateThisAccessedTime();		
		accessCount = ACTIVITY_CHECK ? new AtomicInteger() : null;
		// it starts with true so that it gets replicated when first created
		sessionMetadataDirty = true;
		metadata = new DistributableSipSessionMetadata();
		this.metadata.setCreationTime(creationTime);
		this.metadata.setNew(isNew);
		this.metadata.setMaxInactiveInterval(maxInactiveInterval);	
		
//		checkAlwaysReplicateMetadata();
	}

	// ---------------------------------------------------------------- Session

	public abstract String getInfo();

	public String getAuthType() {
		return (this.authType);
	}

	public void setAuthType(String authType) {
		String oldAuthType = this.authType;
		this.authType = authType;
		support.firePropertyChange("authType", oldAuthType, this.authType);
	}					

	public SipManager getManager() {
		return (this.manager);
	}

	public void setManager(SipManager manager) {
		if ((manager instanceof ClusteredSipSessionManager) == false)
			throw MESSAGES.invalidManager();
		
		if(logger.isDebugEnabled()){
			logger.debug("setManager with : "  + manager);
		}
		
		@SuppressWarnings("unchecked")
		ClusteredSipSessionManager<O> unchecked = (ClusteredSipSessionManager<O>) manager;
		this.manager = unchecked;

		this.invalidationPolicy = this.manager.getReplicationTrigger();
		//TODO: Is the next row necessary? It does not exist anymore in the sources of JBoss7.
		//this.useJK = ((DistributableSessionManager)this.manager).getUseJK();

		int maxUnrep = this.manager.getMaxUnreplicatedInterval() * 1000;
		setMaxUnreplicatedInterval(maxUnrep);
		this.notificationPolicy = this.manager.getSipSessionNotificationPolicy();
		establishDistributedCacheManager();
	}

	public int getMaxInactiveInterval() {
		return (this.maxInactiveInterval);
	}

	/**
	 * Overrides the superclass to calculate
	 * {@link #getMaxUnreplicatedInterval() maxUnreplicatedInterval}.
	 */
	public void setMaxInactiveInterval(int interval) {
		this.maxInactiveInterval = interval;
		if (isValid() && interval == 0) {
			invalidate();
		}
		checkAlwaysReplicateTimestamp();
		sessionMetadataDirty();
	}	
	
	@Override
	public void access() {
		if (logger.isDebugEnabled()){
			logger.debug("access - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		try {
            this.acquireSessionOwnership();
        } catch (TimeoutException e) {
            // May be contending with the session expiration thread of another node, so retry once.
            try {
                this.acquireSessionOwnership();
            } catch (TimeoutException te) {
                throw MESSAGES.failAcquiringOwnership(getHaId(), te);
            }
        }
		
		this.lastAccessedTime = this.thisAccessedTime;
		this.thisAccessedTime = System.currentTimeMillis();
		ConvergedSessionReplicationContext.bindSipSession(this, manager.getSnapshotSipManager());

		if (ACTIVITY_CHECK) {
			accessCount.incrementAndGet();
		}

		// JBAS-3528. If it's not the first access, make sure
		// the 'new' flag is correct
		if (!firstAccess && isNew) {
			setNew(false);
		}
	}
	
	private void acquireSessionOwnership() throws TimeoutException {
		if (logger.isDebugEnabled()){
			logger.debug("acquireSessionOwnership - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
        SessionOwnershipSupport support = this.distributedCacheManager.getSessionOwnershipSupport();

        if (support != null) {
            try {
                this.ownershipLock.lockInterruptibly();

                try {
                    if (support.acquireSessionOwnership(getHaId(), needNewLock()) == SessionOwnershipSupport.LockResult.ACQUIRED_FROM_CLUSTER) {
                        IncomingDistributableSessionData data = this.distributedCacheManager.getSipSessionData(this.key.getApplicationSessionId(), getHaId(), false);
                        if (data != null) {
                            // We may be out of date re: the distributed cache
                            update(data);
                        }
                    }
                } finally {
                    this.ownershipLock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw MESSAGES.interruptedAcquiringOwnership(getHaId(), e);
            }
        }
    }
	
	private boolean needNewLock() {
		if (logger.isDebugEnabled()){
			logger.debug("needNewLock - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
        return firstAccess && isNew;
    }

	public void setNew(boolean isNew) {
		if (logger.isDebugEnabled()){
			logger.debug("setNew - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - isNew=" + isNew);
		}
		
		this.isNew = isNew;

		// Don't replicate metadata just 'cause its the second request
		// The only effect of this is if someone besides a request
		// deserializes metadata from the distributed cache, this
		// field may be out of date.
		// If a request accesses the session, the access() call will
		// set isNew=false, so the request will see the correct value
		// sessionMetadataDirty();
	}


	/**
	 * Invalidates this session and unbinds any objects bound to it. Overridden
	 * here to remove across the cluster instead of just expiring.
	 * 
	 * @exception IllegalStateException
	 *                if this method is called on an invalidated session
	 */
	@Override
	public void invalidate() {
		if (logger.isDebugEnabled()){
			logger.debug("invalidate - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
//		if (!isValid())
//			throw new IllegalStateException(sm
//					.getString("clusteredSession.invalidate.ise"));
//
//		// Cause this session to expire globally
//		boolean notify = true;
//		boolean localCall = true;
//		boolean localOnly = false;
//		invalidate(notify, localCall, localOnly,
//				ClusteredSessionNotificationCause.INVALIDATE);
		super.invalidate();
	}

	
//	/**
//	 * Expires the session, notifying listeners and possibly the manager.
//	 * <p>
//	 * <strong>NOTE:</strong> The manager will only be notified of the
//	 * expiration if <code>localCall</code> is <code>true</code>; otherwise it
//	 * is the responsibility of the caller to notify the manager that the
//	 * session is expired. (In the case of JBossCacheManager, it is the manager
//	 * itself that makes such a call, so it of course is aware).
//	 * </p>
//	 * 
//	 * @param notify
//	 *            whether servlet spec listeners should be notified
//	 * @param localCall
//	 *            <code>true</code> if this call originated due to local
//	 *            activity (such as a session invalidation in user code or an
//	 *            expiration by the local background processing thread);
//	 *            <code>false</code> if the expiration originated due to some
//	 *            kind of event notification from the cluster.
//	 * @param localOnly
//	 *            <code>true</code> if the expiration should not be announced to
//	 *            the cluster, <code>false</code> if other cluster nodes should
//	 *            be made aware of the expiration. Only meaningful if
//	 *            <code>localCall</code> is <code>true</code>.
//	 * @param cause
//	 *            the cause of the expiration
//	 */
//	public void invalidate(boolean notify, boolean localCall,
//			boolean localOnly, ClusteredSessionNotificationCause cause) {
//		if (log.isTraceEnabled()) {
//			log.trace("The session has been invalidated with key: " + key
//					+ " -- is invalidation local? " + localOnly);
//		}		
//
//		synchronized (this) {
//			// If we had a race to this sync block, another thread may
//			// have already completed expiration. If so, don't do it again
//			if (!isValid)
//				return;
//
//			if (manager == null)
//				return;
//			
//			// Notify interested application event listeners
//			// FIXME - Assumes we call listeners in reverse order
//			Context context = (Context) manager.getContainer();
//			Object lifecycleListeners[] = context
//					.getApplicationLifecycleListeners();
//			if (notify
//					&& (lifecycleListeners != null)
//					&& notificationPolicy
//							.isSipSessionListenerInvocationAllowed(
//									this.clusterStatus, cause, localCall)) {
//				SipSessionEvent event = new SipSessionEvent(getSession());
//				for (int i = 0; i < lifecycleListeners.length; i++) {
//					int j = (lifecycleListeners.length - 1) - i;
//					if (!(lifecycleListeners[j] instanceof SipSessionListener))
//						continue;
//					SipSessionListener listener = (SipSessionListener) lifecycleListeners[j];
//					try {
//						fireContainerEvent(context, "beforeSessionDestroyed",
//								listener);
//						listener.sessionDestroyed(event);
//						fireContainerEvent(context, "afterSessionDestroyed",
//								listener);
//					} catch (Throwable t) {
//						try {
//							fireContainerEvent(context,
//									"afterSessionDestroyed", listener);
//						} catch (Exception e) {
//							;
//						}
//						manager.getContainer().getLogger().error(
//								sm.getString("clusteredSession.sessionEvent"),
//								t);
//					}
//				}
//			}
//
//			if (ACTIVITY_CHECK) {
//				accessCount.set(0);
//			}
//
//			// Notify interested session event listeners.
////			if (notify) {
////				fireSessionEvent(Session.SESSION_DESTROYED_EVENT, null);
////			}
//
//			// JBAS-1360 -- Unbind any objects associated with this session
//			String keys[] = keys();
//			for (int i = 0; i < keys.length; i++) {
//				removeAttributeInternal(keys[i], localCall, localOnly, notify,
//						cause);
//			}
//
//			// Remove this session from our manager's active sessions
//			// If !localCall, this expire call came from the manager,
//			// so don't recurse
//			if (localCall) {
//				removeFromManager(localOnly);
//			}
//				
//		}
//
//	}
	
	public Object getNote(String name) {
		if (logger.isDebugEnabled()){
			logger.debug("getNote - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		return (notes.get(name));
	}

	@SuppressWarnings("unchecked")
	public Iterator getNoteNames() {
		if (logger.isDebugEnabled()){
			logger.debug("getNoteNames - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		return (notes.keySet().iterator());
	}

	public void setNote(String name, Object value) {
		if (logger.isDebugEnabled()){
			logger.debug("setNote - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		notes.put(name, value);
	}

	public void removeNote(String name) {
		if (logger.isDebugEnabled()){
			logger.debug("removeNote - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		notes.remove(name);
	}

	
	
	
	// ------------------------------------------------------------ HttpSession

	public ServletContext getServletContext() {
		if (logger.isDebugEnabled()){
			logger.debug("getServletContext - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		if (manager == null)
			return (null);
		Context context = (Context) manager.getContainer();
		if (context == null)
			return (null);
		else
			return (context.getServletContext());
	}

	public Object getAttribute(String name) {
		if (logger.isDebugEnabled()){
			logger.debug("getAttribute - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		if (!isValid())
			throw MESSAGES.expiredSession();
			
		return getAttributeInternal(name);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		if (logger.isDebugEnabled()){
			logger.debug("getAttributeNames - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		if (!isValid())
			throw MESSAGES.expiredSession();
			
		return (new Enumerator(getAttributesInternal().keySet(), true));
	}

	public void setAttribute(String name, Object value) {
		if (logger.isDebugEnabled()){
			logger.debug("setAttribute - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		// Name cannot be null
		if (name == null)
			throw MESSAGES.expiredSession();
			
		// Null value is the same as removeAttribute()
		if (value == null) {
			removeAttribute(name);
			return;
		}

		// Validate our current state
		if (!isValid()) {
			throw MESSAGES.expiredSession();
		}

		if (canAttributeBeReplicated(value) == false) {
			throw MESSAGES.failToReplicateAttribute(name, value.getClass().getCanonicalName());
		}

		// Construct an event with the new value
		SipSessionBindingEvent event = null;

		// Call the valueBound() method if necessary
		if (value instanceof SipSessionBindingListener
				&& notificationPolicy
						.isSipSessionBindingListenerInvocationAllowed(
								this.clusterStatus,
								ClusteredSessionNotificationCause.MODIFY, name,
								true)) {
			event = new SipSessionBindingEvent(this, name);
			try {
				((SipSessionBindingListener) value).valueBound(event);
			} catch (Throwable t) {
				manager.getContainer().getLogger().error(MESSAGES.errorValueBoundEvent(t));
			}
		}

		if (value instanceof SipSessionActivationListener)
			hasActivationListener = Boolean.TRUE;

		// Replace or add this attribute
		Object unbound = setAttributeInternal(name, value);

		// Call the valueUnbound() method if necessary
		if ((unbound != null)
				&& (unbound != value)
				&& (unbound instanceof SipSessionBindingListener)
				&& notificationPolicy
						.isSipSessionBindingListenerInvocationAllowed(
								this.clusterStatus,
								ClusteredSessionNotificationCause.MODIFY, name,
								true)) {
			try {
				((SipSessionBindingListener) unbound)
						.valueUnbound(new SipSessionBindingEvent(this,
								name));
			} catch (Throwable t) {
				manager.getContainer().getLogger().error(MESSAGES.errorValueUnboundEvent(t));
			}
		}

		// Notify interested application event listeners
		if (notificationPolicy.isSipSessionAttributeListenerInvocationAllowed(
				this.clusterStatus, ClusteredSessionNotificationCause.MODIFY,
				name, true)) {
			List<SipSessionAttributeListener> listeners = getSipApplicationSession().getSipContext().getListeners().getSipSessionAttributeListeners();
			if (listeners == null)
				return;
			for (SipSessionAttributeListener listener : listeners) {
				try {
					if (unbound != null) {
//						fireContainerEvent(context,
//								"beforeSessionAttributeReplaced", listener);
						if (event == null) {
							event = new SipSessionBindingEvent(this, name);
						}
						listener.attributeReplaced(event);
//						fireContainerEvent(context,
//								"afterSessionAttributeReplaced", listener);
					} else {
//						fireContainerEvent(context, "beforeSessionAttributeAdded",
//								listener);
						if (event == null) {
							event = new SipSessionBindingEvent(this, name);
						}
						listener.attributeAdded(event);
//						fireContainerEvent(context, "afterSessionAttributeAdded",
//								listener);
					}
				} catch (Throwable t) {
					try {
//						if (unbound != null) {
//							fireContainerEvent(context,
//									"afterSessionAttributeReplaced", listener);
//						} else {
//							fireContainerEvent(context,
//									"afterSessionAttributeAdded", listener);
//						}
					} catch (Exception e) {
						;
					}
					manager.getContainer().getLogger().error(MESSAGES.errorSessionAttributeEvent(t));
				}
			}
		}
	}

	@Override
	public void removeAttribute(String name, boolean byPassValidCheck) {
		if (logger.isDebugEnabled()){
			logger.debug("removeAttribute - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		// Validate our current state
		if (!byPassValidCheck && !isValid())
			throw MESSAGES.expiredSession();

		final boolean localCall = true;
		final boolean localOnly = false;
		final boolean notify = true;
		removeAttributeInternal(name, localCall, localOnly, notify,
				ClusteredSessionNotificationCause.MODIFY);
	}

	public Object getValue(String name) {
		if (logger.isDebugEnabled()){
			logger.debug("getValue - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name + ", return=" + getAttribute(name));
		}
		
		return (getAttribute(name));
	}	

	public void putValue(String name, Object value) {
		if (logger.isDebugEnabled()){
			logger.debug("putValue - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name + ", value=" + value);
		}
		
		setAttribute(name, value);
	}

	public void removeValue(String name) {
		if (logger.isDebugEnabled()){
			logger.debug("removeValue - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		removeAttribute(name);
	}

	// ---------------------------------------------------- DistributableSession
	
	protected int getVersion() {
		if (logger.isDebugEnabled()){
			logger.debug("getVersion - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + version.get());
		}
		
		return version.get();
	}

	public boolean getMustReplicateTimestamp() {
		if (logger.isDebugEnabled()){
			logger.debug("getMustReplicateTimestamp - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		boolean exceeds = alwaysReplicateTimestamp;

		if (!exceeds && maxUnreplicatedInterval > 0) // -1 means ignore
		{
			long unrepl = System.currentTimeMillis() - lastReplicated;
			exceeds = (unrepl >= maxUnreplicatedInterval);
		}

		return exceeds;
	}

	protected long getSessionTimestamp() {
		this.timestamp.set(this.thisAccessedTime);
		return this.timestamp.get();
	}
	
	public void updateThisAccessedTime() {
		if (logger.isDebugEnabled()){
			logger.debug("updateThisAccessedTime - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		thisAccessedTime = System.currentTimeMillis();
	}

	protected boolean isSessionMetadataDirty() {
		if (logger.isDebugEnabled()){
			logger.debug("isSessionMetadataDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + sessionMetadataDirty);
		}
		
		return sessionMetadataDirty;
	}

	protected DistributableSipSessionMetadata getSessionMetadata() {						
		if (logger.isDebugEnabled()){
			logger.debug("getSessionMetadata - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		return this.metadata;
	}

	protected boolean isSessionAttributeMapDirty() {
		if (logger.isDebugEnabled()){
			logger.debug("isSessionAttributeMapDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + sessionAttributesDirty);
		}
		return sessionAttributesDirty;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(IncomingDistributableSessionData sessionData) {
		if (logger.isDebugEnabled()){
			logger.debug("update - sessionData=" + sessionData + ", this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		assert sessionData != null : "sessionData is null";

		this.version.set(sessionData.getVersion());

		long ts = sessionData.getTimestamp();
		this.lastAccessedTime = this.thisAccessedTime = ts;
		this.timestamp.set(ts);

		this.metadata = (DistributableSipSessionMetadata)sessionData.getMetadata();
		if(logger.isDebugEnabled()) {
			logger.debug("update - this.metadata=" + this.metadata);
			if (this.metadata != null){
				logger.debug("update - this.metadata.getMetaData()" + this.metadata.getMetaData());
				if (this.metadata.getMetaData() != null){
					for (String tmpKey: this.metadata.getMetaData().keySet()){
						logger.debug("update - this.metadata.getMetaData() entry:" + tmpKey + "=" + this.metadata.getMetaData().get(tmpKey));	
					}
				}
			}
		}
		
		// TODO -- get rid of these field and delegate to metadata
		this.creationTime = metadata.getCreationTime();
		this.maxInactiveInterval = metadata.getMaxInactiveInterval();					
		
		updateSipSession(metadata);
		
		// We no longer know if we have an activationListener
		hasActivationListener = null;

		// If the session has been replicated, any subsequent
		// access cannot be the first.
		this.firstAccess = false;

		// We don't know when we last replicated our timestamp. We may be
		// getting called due to activation, not deserialization after
		// replication, so this.timestamp may be after the last replication.
		// So use the creation time as a conservative guesstimate. Only downside
		// is we may replicate a timestamp earlier than we need to, which is not
		// a heavy cost.
		this.lastReplicated = this.creationTime;

		this.clusterStatus = new ClusteredSessionManagementStatus(haId,
				true, null, null);

		checkAlwaysReplicateTimestamp();

		populateAttributes(sessionData.getSessionAttributes());

		isNew = false;
		// We are no longer outdated vis a vis distributed cache
		clearOutdated();
		// make sure we don't write back to the cache on a remote session refresh
		sessionAttributesDirty = false;
		sessionMetadataDirty = false;
	}

	protected void updateSipSession(DistributableSipSessionMetadata md) {
		if (logger.isDebugEnabled()){
			logger.debug("updateSipSession - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		//From SipSession
		final Map<String, Object> metaData = md.getMetaData();
		if (logger.isDebugEnabled()){
			logger.debug("updateSipSession - handlerServlet original value=" + handlerServlet
					+", new value=" + (String) metaData.get(HANDLER)
					+ ", this.getId()=" + this.getId()
					+ ", this.getHaId()=" + this.getHaId());
		}
		handlerServlet = (String) metaData.get(HANDLER);
		Boolean valid = (Boolean)metaData.get(IS_VALID);
		if(valid != null) {
			// call to super very important to avoid setting the session dirty on reload and rewrite to the cache
			super.setValid(valid);
		} 
		state = (State)metaData.get(STATE);
		Long cSeq = (Long) metaData.get(CSEQ);
		if(cSeq != null) {
			cseq = cSeq;
		} 
		Boolean iwr = (Boolean) metaData.get(INVALIDATE_WHEN_READY);		
		if(iwr != null) {
			invalidateWhenReady = iwr;
		} 
		Boolean rti = (Boolean) metaData.get(READY_TO_INVALIDATE);		
		if(rti != null) {
			readyToInvalidate = rti;
		} 
		sessionCreatingDialogId = (String) metaData.get(DIALOG_ID);
		proxy = (ProxyImpl) metaData.get(PROXY);
		if(proxy != null) {
			proxy.setMobicentsSipFactory(getManager().getMobicentsSipFactory());
		}
		
		transport = (String) metaData.get(TRANSPORT);
		
		Integer size = (Integer) metaData.get(B2B_SESSION_SIZE);
		String[][] sessionArray = (String[][])metaData.get(B2B_SESSION_MAP);
		if(logger.isDebugEnabled()) {
			logger.debug("b2bua session array size = " + size + ", value = " + sessionArray);
		}
		if(size != null && sessionArray != null) {
			Map<MobicentsSipSessionKey, MobicentsSipSessionKey> sessionMap = new ConcurrentHashMap<MobicentsSipSessionKey, MobicentsSipSessionKey>();
			for (int i = 0; i < size; i++) {
				String key = sessionArray[0][i];
				String value = sessionArray[1][i];
				try {
					SipSessionKey sipSessionKeyKey = SessionManagerUtil.parseSipSessionKey(key);
					SipSessionKey sipSessionKeyValue = SessionManagerUtil.parseSipSessionKey(value);
					sessionMap.put(sipSessionKeyKey, sipSessionKeyValue);
				} catch (ParseException e) {
					logger.warn("couldn't parse a deserialized sip session key from the B2BUA", e);
				}
			}
			if(b2buaHelper == null) {
				b2buaHelper = new B2buaHelperImpl();
				b2buaHelper.setMobicentsSipFactory(getManager().getMobicentsSipFactory());
				b2buaHelper.setSipManager(getManager());
			}
			b2buaHelper.setSessionMap(sessionMap);
		}
		if(((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).getReplicationStrategy() == ReplicationStrategy.EarlyDialog) {
			Integer txsSize = (Integer) metaData.get(TXS_SIZE);
			String[] txIds = (String[])metaData.get(TXS_IDS);
			Boolean[] txTypes = (Boolean[])metaData.get(TXS_TYPE);
			if(logger.isDebugEnabled()) {
				logger.debug("tx array size = " + txsSize + ", value = " + txIds);
			}
			if(txsSize != null && txIds != null) {
				for (int i = 0; i < txsSize; i++) {
					String txId = txIds[i];
					Boolean txType = txTypes[i];
					if(logger.isDebugEnabled()) {
						logger.debug("trying to find tx with id = " + txId + ", type = " + txType + " locally or in the distributed cache");
					}
					Transaction transaction = ((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).findTransaction(txId, txType);					
					addOngoingTransaction(transaction);
					if(transaction != null) {
						if(transaction instanceof ServerTransaction) { 
							acksReceived.put(((RequestExt)transaction.getRequest()).getCSeqHeader().getSeqNumber(), false);								
						} else if(proxy != null) {
							final TransactionApplicationData transactionApplicationData = (TransactionApplicationData) transaction.getApplicationData();
							if(logger.isDebugEnabled()) {
								logger.debug("transaction application data from tx id " + txId + " is " + transactionApplicationData);
							}
							if(transactionApplicationData != null) {
								if(logger.isDebugEnabled()) {
									logger.debug("populating proxy from the transaction application data");
								}
								proxy.getTransactionMap().put(txId, transactionApplicationData);
								ProxyBranchImpl proxyBranchImpl = transactionApplicationData.getProxyBranch();
								if(proxyBranchImpl != null) {
									if(logger.isDebugEnabled()) {
										logger.debug("populating branch from the transaction application data");
									}
									proxyBranchImpl.setOutgoingRequest((SipServletRequestImpl)transactionApplicationData.getSipServletMessage());
									proxyBranchImpl.setOriginalRequest((SipServletRequestImpl)proxy.getOriginalRequest());
									proxyBranchImpl.setProxy(proxy);
									// The original (~JBoss5) code: proxy.addProxyBranch(proxyBranchImpl);
									// Is replaced by the following:
									SipFactory sipFactory = (SipFactory) getServletContext().getAttribute("javax.servlet.sip.SipFactory");
									try {
										// TODO: is this the right way to do it?
										// TODO: proxy.getProxyBranchesMap() == null check? probably not neccessary?
										proxy.getProxyBranchesMap().put(sipFactory.createURI(proxyBranchImpl.getTargetURI()), proxyBranchImpl);
									} catch (ServletParseException e) {
										logger.error("Error while adding proxy branch to proxy. Could not parse URI: " + e);
									}
								}
							}
						}
					}
				}				
			}
			String toTag = (String) metaData.get(TO_TAG);
			if(logger.isDebugEnabled()) {
				logger.debug("sessionkey replicated totag to " + toTag);
			}
			if(key.getToTag() == null && toTag != null && toTag.trim().length() > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("setting sessionkey totag to " + toTag);
				}
				key.setToTag(toTag, false);
			}
			// Fix for Issue 2739 : Null returned for B2BUAHelperImpl.getLinkedSipServletRequest() in Early Dailog Failover
			size = (Integer) metaData.get(B2B_LINKED_REQUESTS_SIZE);
			SipServletRequestImpl[][] linkedRequests = (SipServletRequestImpl[][])metaData.get(B2B_LINKED_REQUESTS_MAP);
			if(logger.isDebugEnabled()) {
				logger.debug("b2bua linked requests array size = " + size + ", value = " + linkedRequests);
			}
			if(size != null && linkedRequests != null) {
				Map<SipServletRequestImpl, SipServletRequestImpl> linkedRequestsMap = new ConcurrentHashMap<SipServletRequestImpl, SipServletRequestImpl>();
				for (int i = 0; i < size; i++) {
					SipServletRequestImpl key = linkedRequests[0][i];
					SipServletRequestImpl value = linkedRequests[1][i];
					linkedRequestsMap.put(key, value);						
				}
				if(b2buaHelper == null) {
					b2buaHelper = new B2buaHelperImpl();
					b2buaHelper.setMobicentsSipFactory(getManager().getMobicentsSipFactory());
					b2buaHelper.setSipManager(getManager());
				}
				b2buaHelper.setOriginalRequestMap(linkedRequestsMap);
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("dialog id to inject " + sessionCreatingDialogId + ", this.getId()=" + this.getId());
		}
		if(sessionCreatingDialogId != null && sessionCreatingDialogId.length() > 0) {
			//Container context = getManager().getContainer();
			Container context = this.manager.getContainer();
			if (logger.isDebugEnabled()){
				logger.debug("updateSipSession - context=" + context);
			}
			
			Container container = context.getParent().getParent();
			if (logger.isDebugEnabled()){
				logger.debug("updateSipSession - container=" + container);
			}
			if(container instanceof Engine) {
				if(logger.isDebugEnabled()){
					logger.debug("Engine: " + container);
					logger.debug("Engine: " + container.getClass().getName());
				}
				Service service = ((Engine)container).getService();
				if(logger.isDebugEnabled()){
					logger.debug("Service: " + service);
					logger.debug("Service: " + service.getClass().getName());
				}
				if(service instanceof SipService) {
					SipService sipService = (SipService) service;
					SipStack sipStack = sipService.getSipStack();
					if (logger.isDebugEnabled()){
						logger.debug("updateSipSession - service is instanceof SipService - sipStack=" + sipStack);
					}
					if(sipStack != null) {
						sessionCreatingDialog = ((ClusteredSipStack)sipStack).getDialog(sessionCreatingDialogId); 
						if(logger.isDebugEnabled()) {
							logger.debug("dialog injected " + sessionCreatingDialog + ", this.getId()=" + this.getId());
						}
					}
				}else{
					if(logger.isDebugEnabled()) {
						logger.debug("updateSipSession - retrieved service is not SipService --> workaround: getting the SipService from static holder - sessionCreatingDialog=" + sessionCreatingDialog);
					}
					SipService sipService = StaticServiceHolder.sipStandardService;
					SipStack sipStack = sipService.getSipStack();					
					if(sipStack != null) {
						if (logger.isDebugEnabled()){
							logger.debug("updateSipSession - sipStack.getClass().getName()=" + sipStack.getClass().getName());
						}
						
						sessionCreatingDialog = ((ClusteredSipStack)sipStack).getDialog(sessionCreatingDialogId); 
						if(logger.isDebugEnabled()) {
							logger.debug("dialog injected " + sessionCreatingDialog);
						}
					}else{
						if(logger.isDebugEnabled()) {
							logger.debug("updateSipSession - could not inject dialog, sip stack is still null");
						}
					}
					
				}
			}
		}
	}

	// ------------------------------------------------------------------ Public

	/**
	 * Increment our version and propagate ourself to the distributed cache.
	 */
	public synchronized void processSipSessionReplication() {
		if (logger.isDebugEnabled()){
			logger.debug("processSipSessionReplication - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		// Replicate the session.
		if (logger.isDebugEnabled()) {
			logger
					.debug("processSipSessionReplication(): session is dirty. Will increment "
							+ "version from: "
							+ getVersion()
							+ " and replicate.");
		}
		version.incrementAndGet();

		O outgoingData = getOutgoingSipSessionData();
		if(sessionMetadataDirty) {
			Map<String, Object> metaData = ((DistributableSipSessionMetadata)outgoingData.getMetadata()).getMetaData();
			if(proxy != null) {											
				metaData.put(PROXY, proxy);
			}
			
			if(b2buaHelper != null) {
				final Map<MobicentsSipSessionKey, MobicentsSipSessionKey> sessionMap = b2buaHelper.getSessionMap();
				final int size = sessionMap.size();
				final String[][] sessionArray = new String[2][size];
				int i = 0;
				for (Entry<MobicentsSipSessionKey, MobicentsSipSessionKey> entry : sessionMap.entrySet()) {
					sessionArray [0][i] = entry.getKey().toString(); 
					sessionArray [1][i] = entry.getValue().toString();
					i++;
				}
				metaData.put(B2B_SESSION_SIZE, size);
				if(logger.isDebugEnabled()) {
					logger.debug("storing b2bua session array " + sessionArray);
				}
				metaData.put(B2B_SESSION_MAP, sessionArray);
			}	
			if(((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).getReplicationStrategy() == ReplicationStrategy.EarlyDialog) {
				final Set<Transaction> ongoingTransactions = getOngoingTransactions();
				final int size = ongoingTransactions.size();
				final String[] txIdArray = new String[size];
				final Boolean[] txTypeArray = new Boolean[size];
				int i = 0;
				for (Transaction transaction : ongoingTransactions) {					
					txIdArray[i] = transaction.getBranchId(); 
					txTypeArray[i] = transaction instanceof ServerTransaction ? Boolean.TRUE : Boolean.FALSE;
					i++;
				}
				metaData.put(TXS_SIZE, size);
				if(logger.isDebugEnabled()) {
					logger.debug("storing transaction ids array " + txIdArray);
				}
				metaData.put(TXS_IDS, txIdArray);
				if(logger.isDebugEnabled()) {
					logger.debug("storing transaction type array " + txTypeArray);
				}
				metaData.put(TXS_TYPE, txTypeArray);
				String toTag = key.getToTag();
				if(toTag == null) {
					toTag = "";
				}
				if(logger.isDebugEnabled()) {
					logger.debug("storing sessionkey to tag " + toTag);
				}
				metaData.put(TO_TAG, toTag);
				if(b2buaHelper != null) {
					// Fix for Issue 2739 : Null returned for B2BUAHelperImpl.getLinkedSipServletRequest() in Early Dailog Failover
					final Map<SipServletRequestImpl, SipServletRequestImpl> linkedRequestsMap = b2buaHelper.getOriginalRequestMap();
					final int linkedRequestsMapSize = linkedRequestsMap.size();
					final SipServletRequestImpl[][] linkedRequestsArray = new SipServletRequestImpl[2][linkedRequestsMapSize];
					int j = 0;
					for (Entry<SipServletRequestImpl, SipServletRequestImpl> entry : linkedRequestsMap.entrySet()) {
						linkedRequestsArray [0][j] = entry.getKey(); 
						linkedRequestsArray [1][j] = entry.getValue();
						j++;
					}
					metaData.put(B2B_LINKED_REQUESTS_SIZE, linkedRequestsMapSize);
					if(logger.isDebugEnabled()) {
						logger.debug("storing b2bua linked requests array " + linkedRequestsArray);
					}
					metaData.put(B2B_LINKED_REQUESTS_MAP, linkedRequestsArray);
				}	
			}
		}
		distributedCacheManager.storeSipSessionData(outgoingData);
		
		sessionAttributesDirty = false;
		sessionMetadataDirty = false;
		isNew = false;
		metadata.setNew(isNew);
		metadata.getMetaData().clear();
		lastReplicated = System.currentTimeMillis();
	}

	protected abstract O getOutgoingSipSessionData();

	/**
	 * Remove myself from the distributed cache.
	 */
	public void removeMyself() {
		if (logger.isDebugEnabled()){
			logger.debug("removeMyself - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		((DistributedCacheConvergedSipManager)getDistributedCacheManager()).removeSipSession(sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key));
	}

	/**
	 * Remove myself from the <t>local</t> instance of the distributed cache.
	 */
	public void removeMyselfLocal() {
		if (logger.isDebugEnabled()){
			logger.debug("removeMyselfLocal - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		((DistributedCacheConvergedSipManager)getDistributedCacheManager()).removeSipSessionLocal(sipApplicationSessionKey.getId(), SessionManagerUtil.getSipSessionHaKey(key), null);
	}

	/**
	 * Gets the sessions creation time, skipping any validity check.
	 * 
	 * @return the creation time
	 */
	public long getCreationTimeInternal() {
		return creationTime;
	}

	/**
	 * Gets the time {@link #processSessionReplication()} was last called, or
	 * <code>0</code> if it has never been called.
	 */
	public long getLastReplicated() {
		return lastReplicated;
	}

	/**
	 * Gets the maximum period in ms after which a request accessing this
	 * session will trigger replication of its timestamp, even if the request
	 * doesn't otherwise modify the session. A value of -1 means no limit.
	 */
	public long getMaxUnreplicatedInterval() {
		return maxUnreplicatedInterval;
	}

	/**
	 * Sets the maximum period in ms after which a request accessing this
	 * session will trigger replication of its timestamp, even if the request
	 * doesn't otherwise modify the session. A value of -1 means no limit.
	 */
	public void setMaxUnreplicatedInterval(long interval) {
		this.maxUnreplicatedInterval = Math.max(interval, -1);
		checkAlwaysReplicateTimestamp();
	}	

	/**
	 * Gets whether the session expects to be accessible via mod_jk, mod_proxy,
	 * mod_cluster or other such AJP-based load balancers.
	 */
	public boolean getUseJK() {
		if (logger.isDebugEnabled()){
			logger.debug("getUseJK - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + useJK);
		}
		
		return useJK;
	}

	/**
	 * Update our version due to changes in the distributed cache.
	 * 
	 * @param version
	 *            the distributed cache version
	 * @return <code>true</code>
	 */
	public boolean setVersionFromDistributedCache(int version) {
		if (logger.isDebugEnabled()){
			logger.debug("setVersionFromDistributedCache - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - version=" + version);
		}
		
		boolean outdated = getVersion() < version;
		if (outdated) {
			this.outdatedVersion = version;
			outdatedTime = System.currentTimeMillis();
		}
		return outdated;
	}

	/**
	 * Check to see if the session data is still valid. Outdated here means that
	 * the in-memory data is not in sync with one in the data store.
	 * 
	 * @return
	 */
	public boolean isOutdated() {
		if (logger.isDebugEnabled()){
			logger.debug("isOutdated - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + (thisAccessedTime < outdatedTime));
		}
		
		return thisAccessedTime < outdatedTime;
	}

	public boolean isSessionDirty() {
		if (logger.isDebugEnabled()){
			logger.debug("isSessionDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + (sessionAttributesDirty || sessionMetadataDirty));
		}
		
		return sessionAttributesDirty || sessionMetadataDirty;
	}

	/** Inform any SipSessionListener of the creation of this session */
	public void tellNew(ClusteredSessionNotificationCause cause) {
		if (logger.isDebugEnabled()){
			logger.debug("tellNew - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		// Notify interested session event listeners
//		fireSessionEvent(Session.SESSION_CREATED_EVENT, null);

		// Notify interested application event listeners
		if (notificationPolicy.isSipSessionListenerInvocationAllowed(
				this.clusterStatus, cause, true)) {
			Context context = (Context) manager.getContainer();
			Object lifecycleListeners[] = context
					.getApplicationLifecycleListeners();
			if (lifecycleListeners != null) {
				SipSessionEvent event = new SipSessionEvent(this);
				for (int i = 0; i < lifecycleListeners.length; i++) {
					if (!(lifecycleListeners[i] instanceof SipSessionListener))
						continue;
					SipSessionListener listener = (SipSessionListener) lifecycleListeners[i];
					try {
						fireContainerEvent(context, "beforeSessionCreated",
								listener);
						listener.sessionCreated(event);
						fireContainerEvent(context, "afterSessionCreated",
								listener);
					} catch (Throwable t) {
						try {
							fireContainerEvent(context, "afterSessionCreated",
									listener);
						} catch (Exception e) {
							;
						}
						manager.getContainer().getLogger().error(MESSAGES.errorSessionEvent(t));
					}
				}
			}
		}
	}	

	private String[] keys() {
		Set<String> keySet = getAttributesInternal().keySet();
		return ((String[]) keySet.toArray(new String[keySet.size()]));
	}

	/**
	 * Inform any HttpSessionActivationListener that the session will passivate.
	 * 
	 * @param cause
	 *            cause of the notification (e.g.
	 *            {@link ClusteredSessionNotificationCause#REPLICATION} or
	 *            {@link ClusteredSessionNotificationCause#PASSIVATION}
	 */
	public void notifyWillPassivate(ClusteredSessionNotificationCause cause) {
		if (logger.isDebugEnabled()){
			logger.debug("notifyWillPassivate - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - cause=" + cause);
		}
		
		// Notify interested session event listeners
//		fireSessionEvent(Session.SESSION_PASSIVATED_EVENT, null);

		if (hasActivationListener != null && hasActivationListener != Boolean.FALSE) {
			boolean hasListener = false;

			// Notify ActivationListeners
			SipSessionEvent event = null;
			String keys[] = keys();
			Map<String, Object> attrs = getAttributesInternal();
			for (int i = 0; i < keys.length; i++) {
				Object attribute = attrs.get(keys[i]);
				if (attribute instanceof SipSessionActivationListener) {
					hasListener = true;

					if (notificationPolicy
							.isSipSessionActivationListenerInvocationAllowed(
									this.clusterStatus, cause, keys[i])) {
						if (event == null){
							if(cause == ClusteredSessionNotificationCause.REPLICATION){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.REPLICATION);
							}else if(cause == ClusteredSessionNotificationCause.PASSIVATION){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.PASSIVATION);
							}else if(cause == ClusteredSessionNotificationCause.FAILOVER){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.FAILOVER);
							}else if(cause == ClusteredSessionNotificationCause.FAILAWAY){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.FAILAWAY);
							}else if(cause == ClusteredSessionNotificationCause.ACTIVATION){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.ACTIVATION);
							}else{
								throw new IllegalStateException("can not translate clustered session notification cause to session activation notification cause");
							}
						}

						ClassLoader oldLoader = java.lang.Thread.currentThread().getContextClassLoader();
						   
						java.lang.Thread.currentThread().setContextClassLoader(getSipApplicationSession().getSipContext().getSipContextClassLoader());
						
						try {
							if(logger.isDebugEnabled()){
								logger.debug(attribute);
							}
							((SipSessionActivationListener) attribute)
									.sessionWillPassivate(event);
						} catch (Throwable t) {
							logger.error(MESSAGES.errorSessionActivationEvent(t));
						}
						
						java.lang.Thread.currentThread().setContextClassLoader(oldLoader);
						
					}
				}
			}

			hasActivationListener = hasListener ? Boolean.TRUE : Boolean.FALSE;
		}

		if (cause != ClusteredSessionNotificationCause.PASSIVATION) {
			this.needsPostReplicateActivation = true;
		}
	}

	/**
	 * Inform any HttpSessionActivationListener that the session has been
	 * activated.
	 * 
	 * @param cause
	 *            cause of the notification (e.g.
	 *            {@link ClusteredSessionNotificationCause#REPLICATION} or
	 *            {@link ClusteredSessionNotificationCause#PASSIVATION}
	 */
	public void notifyDidActivate(ClusteredSessionNotificationCause cause) {
		if (logger.isDebugEnabled()){
			logger.debug("notifyDidActivate - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - cause=" + cause);
		}
		
		if (cause == ClusteredSessionNotificationCause.ACTIVATION) {
			this.needsPostReplicateActivation = true;
		}

		// Notify interested session event listeners
//		fireSessionEvent(Session.SESSION_ACTIVATED_EVENT, null);

		if (hasActivationListener != Boolean.FALSE) {
			// Notify ActivationListeners

			boolean hasListener = false;

			SipSessionEvent event = null;
			String keys[] = keys();
			Map<String, Object> attrs = getAttributesInternal();
			for (int i = 0; i < keys.length; i++) {
				Object attribute = attrs.get(keys[i]);
				if (attribute instanceof SipSessionActivationListener) {
					hasListener = true;

					if (notificationPolicy
							.isSipSessionActivationListenerInvocationAllowed(
									this.clusterStatus, cause, keys[i])) {
						
						if (event == null){
							if(cause == ClusteredSessionNotificationCause.REPLICATION){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.REPLICATION);
							}else if(cause == ClusteredSessionNotificationCause.PASSIVATION){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.PASSIVATION);
							}else if(cause == ClusteredSessionNotificationCause.FAILOVER){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.FAILOVER);
							}else if(cause == ClusteredSessionNotificationCause.FAILAWAY){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.FAILAWAY);
							}else if(cause == ClusteredSessionNotificationCause.ACTIVATION){
								event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.ACTIVATION);
							}else{
								throw new IllegalStateException("can not translate clustered session notification cause to session activation notification cause");
							}
						}

						ClassLoader oldLoader = java.lang.Thread.currentThread().getContextClassLoader();
						   
						java.lang.Thread.currentThread().setContextClassLoader(getSipApplicationSession().getSipContext().getSipContextClassLoader());
						
						try {
							((SipSessionActivationListener) attribute)
									.sessionDidActivate(event);
						} catch (Throwable t) {
							logger.error(MESSAGES.errorSessionActivationEvent(t));
						}
						
						java.lang.Thread.currentThread().setContextClassLoader(oldLoader);
						
					}
				}
			}

			hasActivationListener = hasListener ? Boolean.TRUE : Boolean.FALSE;
		}

		if (cause != ClusteredSessionNotificationCause.ACTIVATION) {
			this.needsPostReplicateActivation = false;
		}
	}

	/**
	 * Gets whether the session needs to notify HttpSessionActivationListeners
	 * that it has been activated following replication.
	 */
	public boolean getNeedsPostReplicateActivation() {
		if (logger.isDebugEnabled()){
			logger.debug("getNeedsPostReplicateActivation - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - return=" + needsPostReplicateActivation);
		}
		
		return needsPostReplicateActivation;
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append('[')
				.append("id: ").append(haId).append(" lastAccessedTime: ")
				.append(lastAccessedTime).append(" version: ").append(version)
				.append(" lastOutdated: ").append(outdatedTime).append(']')
				.toString();
	}

	// ----------------------------------------------------- Protected Methods

	protected abstract Object setAttributeInternal(String name, Object value);

	protected abstract Object removeAttributeInternal(String name,
			boolean localCall, boolean localOnly);

	protected Object getAttributeInternal(String name) {
		Object result = getAttributesInternal().get(name);

		// Do dirty check even if result is null, as w/ SET_AND_GET null
		// still makes us dirty (ensures timely replication w/o using ACCESS)
		if (isGetDirty(result)) {
			sessionAttributesDirty();
		}

		return result;
	}

	/**
	 * Extension point for subclasses to load the attribute map from the
	 * distributed cache.
	 */
	protected void populateAttributes(
			Map<String, Object> distributedCacheAttributes) {
		if (logger.isDebugEnabled()){
			logger.debug("populateAttributes - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		Map<String, Object> existing = getAttributesInternal();
		Map<String, Object> excluded = removeExcludedAttributes(existing);

		existing.clear();

		if(logger.isDebugEnabled()) {
			logger.debug("putting following attributes " + distributedCacheAttributes + " in the sip session " + key);
		}
		
		existing.putAll(distributedCacheAttributes);
		if (excluded != null)
			existing.putAll(excluded);
	}

	protected final Map<String, Object> getAttributesInternal() {
		return attributes;
	}

	protected final ClusteredSipSessionManager<O> getManagerInternal() {
		return manager;
	}

	protected final DistributedCacheManager<O> getDistributedCacheManager() {
		if (logger.isDebugEnabled()){
			logger.debug("getDistributedCacheManager - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		return distributedCacheManager;
	}

	protected final void setDistributedCacheManager(
			DistributedCacheConvergedSipManager<O> distributedCacheManager) {
		if (logger.isDebugEnabled()){
			logger.debug("setDistributedCacheManager - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		this.distributedCacheManager = distributedCacheManager;
	}

	/**
	 * Returns whether the attribute's type is one that can be replicated.
	 * 
	 * @param attribute
	 *            the attribute
	 * @return <code>true</code> if <code>attribute</code> is <code>null</code>,
	 *         <code>Serializable</code> or an array of primitives.
	 */
	protected boolean canAttributeBeReplicated(Object attribute) {
		if (attribute instanceof Serializable || attribute == null)
			return true;
		Class<?> clazz = attribute.getClass().getComponentType();
		return (clazz != null && clazz.isPrimitive());
	}

	/**
	 * Removes any attribute whose name is found in {@link #excludedAttributes}
	 * from <code>attributes</code> and returns a Map of all such attributes.
	 * 
	 * @param attributes
	 *            source map from which excluded attributes are to be removed.
	 * 
	 * @return Map that contains any attributes removed from
	 *         <code>attributes</code>, or <code>null</code> if no attributes
	 *         were removed.
	 */
	protected final Map<String, Object> removeExcludedAttributes(
			Map<String, Object> attributes) {
		if (logger.isDebugEnabled()){
			logger.debug("removeExcludedAttributes - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		Map<String, Object> excluded = null;
		for (int i = 0; i < excludedAttributes.length; i++) {
			Object attr = attributes.remove(excludedAttributes[i]);
			if (attr != null) {
				if (log.isTraceEnabled()) {
					log.trace("Excluding attribute " + excludedAttributes[i]
							+ " from replication");
				}
				if (excluded == null) {
					excluded = new HashMap<String, Object>();
				}
				excluded.put(excludedAttributes[i], attr);
			}
		}

		return excluded;
	}

	protected final boolean isGetDirty(Object attribute) {
		if (logger.isDebugEnabled()){
			logger.debug("isGetDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		boolean result = false;
		switch (invalidationPolicy) {
		case SET_AND_GET:
			result = true;
			break;
		case SET_AND_NON_PRIMITIVE_GET:
			result = isMutable(attribute);
			break;
		default:
			// result is false
		}
		return result;
	}

	protected boolean isMutable(Object attribute) {
		return attribute != null
				&& !(attribute instanceof String || attribute instanceof Number
						|| attribute instanceof Character || attribute instanceof Boolean);
	}

	/**
	 * Gets a reference to the JBossCacheService.
	 */
	protected void establishDistributedCacheManager() {
		if (logger.isDebugEnabled()){
			logger.debug("establishDistributedCacheManager - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		if (distributedCacheManager == null) {
			distributedCacheManager = getManagerInternal()
					.getDistributedCacheConvergedSipManager();

			// still null???
			if (distributedCacheManager == null) {
				throw new RuntimeException("DistributedCacheManager is null.");
			}
		}
	}

	protected final void sessionAttributesDirty() {
		if (logger.isDebugEnabled()){
			logger.debug("sessionAttributesDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		if (!sessionAttributesDirty && logger.isDebugEnabled()) {
			logger.debug("Marking session attributes dirty " + haId);
		}

		sessionAttributesDirty = true;
		ConvergedSessionReplicationContext.bindSipSession(this, manager.getSnapshotSipManager());
	}

	protected final void setHasActivationListener(boolean hasListener) {
		if (logger.isDebugEnabled()){
			logger.debug("sessionAttributesDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - hasListener=" + hasListener);
		}
		
		this.hasActivationListener = Boolean.valueOf(hasListener);
	}

	// ----------------------------------------------------------------- Private

	private void checkAlwaysReplicateTimestamp() {
		this.alwaysReplicateTimestamp = (maxUnreplicatedInterval == 0 || (maxUnreplicatedInterval > 0
				&& maxInactiveInterval >= 0 && maxUnreplicatedInterval > (maxInactiveInterval * 1000)));
	}	

	/**
	 * Remove the attribute from the local cache and possibly the distributed
	 * cache, plus notify any listeners
	 * 
	 * @param name
	 *            the attribute name
	 * @param localCall
	 *            <code>true</code> if this call originated from local activity
	 *            (e.g. a removeAttribute() in the webapp or a local session
	 *            invalidation/expiration), <code>false</code> if it originated
	 *            due to an remote event in the distributed cache.
	 * @param localOnly
	 *            <code>true</code> if the removal should not be replicated
	 *            around the cluster
	 * @param notify
	 *            <code>true</code> if listeners should be notified
	 * @param cause
	 *            the cause of the removal
	 */
	private void removeAttributeInternal(String name, boolean localCall,
			boolean localOnly, boolean notify,
			ClusteredSessionNotificationCause cause) {
		if (logger.isDebugEnabled()){
			logger.debug("removeAttributeInternal - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		// Remove this attribute from our collection
		Object value = removeAttributeInternal(name, localCall, localOnly);

		// Do we need to do valueUnbound() and attributeRemoved() notification?
		if (!notify || (value == null)) {
			return;
		}

		// Call the valueUnbound() method if necessary
		SipSessionBindingEvent event = null;
		if (value instanceof SipSessionBindingListener
				&& notificationPolicy
						.isSipSessionBindingListenerInvocationAllowed(
								this.clusterStatus, cause, name, localCall)) {
			event = new SipSessionBindingEvent(this, name);
			((SipSessionBindingListener) value).valueUnbound(event);
		}

		// Notify interested application event listeners
		if (notificationPolicy.isSipSessionAttributeListenerInvocationAllowed(
				this.clusterStatus, cause, name, localCall)) {
			// Notify interested application event listeners
			List<SipSessionAttributeListener> listeners = getSipApplicationSession().getSipContext().getListeners().getSipSessionAttributeListeners();
			if (listeners == null)
				return;
			for (SipSessionAttributeListener listener : listeners) {
				try {
//					fireContainerEvent(context, "beforeSessionAttributeRemoved",
//							listener);
					if (event == null) {
						event = new SipSessionBindingEvent(this, name);
					}
					listener.attributeRemoved(event);
//					fireContainerEvent(context, "afterSessionAttributeRemoved",
//							listener);
				} catch (Throwable t) {
//					try {
//						fireContainerEvent(context, "afterSessionAttributeRemoved",
//								listener);
//					} catch (Exception e) {
//						;
//					}
					manager.getContainer().getLogger().error(MESSAGES.errorSessionAttributeEvent(t));
				}
			}
		}
	}

	
	/**
	 * Fire container events if the Context implementation is the
	 * <code>org.apache.catalina.core.StandardContext</code>.
	 * 
	 * @param context
	 *            Context for which to fire events
	 * @param type
	 *            Event type
	 * @param data
	 *            Event data
	 * 
	 * @exception Exception
	 *                occurred during event firing
	 */
	private void fireContainerEvent(Context context, String type, Object data)
			throws Exception {
		if (logger.isDebugEnabled()){
			logger.debug("fireContainerEvent - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - type=" + type);
		}

		if (!"org.apache.catalina.core.StandardContext".equals(context
				.getClass().getName())) {
			return; // Container events are not supported
		}
		// NOTE: Race condition is harmless, so do not synchronize
		if (containerEventMethod == null) {
			containerEventMethod = context.getClass().getMethod(
					"fireContainerEvent", containerEventTypes);
		}
		Object containerEventParams[] = new Object[2];
		containerEventParams[0] = type;
		containerEventParams[1] = data;
		containerEventMethod.invoke(context, containerEventParams);

	}	

	private void sessionMetadataDirty() {
		if (logger.isDebugEnabled()){
			logger.debug("sessionMetadataDirty - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
//		if (!sessionMetadataDirty && !isNew && log.isTraceEnabled())
		if(logger.isDebugEnabled()) {
			logger.debug("Marking session metadata dirty " + key);
		}
		sessionMetadataDirty = true;
		ConvergedSessionReplicationContext.bindSipSession(this, manager.getSnapshotSipManager());
	}	
	
	/**
	 * Advise our manager to remove this expired session.
	 * 
	 * @param localOnly
	 *            whether the rest of the cluster should be made aware of the
	 *            removal
	 */
	private void removeFromManager(boolean localOnly) {
		if (logger.isDebugEnabled()){
			logger.debug("removeFromManager - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - localOnly=" + localOnly);
		}
		
		if (localOnly) {
			manager.removeLocal(this);
		} else {
			manager.removeSipSession(key);
		}
	}

	public final void clearOutdated() {
		if (logger.isDebugEnabled()){
			logger.debug("clearOutdated - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		// Only overwrite the access time if access() hasn't been called
		// since setOutdatedVersion() was called
		if (outdatedTime > thisAccessedTime) {
			lastAccessedTime = thisAccessedTime;
			thisAccessedTime = outdatedTime;
		}
		outdatedTime = 0;

		// Only overwrite the version if the outdated version is greater
		// Otherwise when we first unmarshall a session that has been
		// replicated many times, we will reset the version to 0
		if (outdatedVersion > version.get())
			version.set(outdatedVersion);

		outdatedVersion = 0;
	}

	@Override
	public void setState(State state) {
		if (logger.isDebugEnabled()){
			logger.debug("setState - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - state=" + state);
		}
		
		super.setState(state);
		sessionMetadataDirty();
		metadata.getMetaData().put(STATE, state);
	}
	
	@Override
	public void setCseq(long cseq) {
		if (logger.isDebugEnabled()){
			logger.debug("setCseq - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - cseq=" + cseq);
		}
		
		long oldCSeq = getCseq();
		if (logger.isDebugEnabled()){
			logger.debug("setCseq - oldCSeq=" + oldCSeq + ", cseq=" + cseq);
		}
		super.setCseq(cseq);
		if(oldCSeq != cseq) {
			sessionMetadataDirty();
			metadata.getMetaData().put(CSEQ, cseq);
		}
	}
	
	@Override
	public void setHandler(String name) throws ServletException {
		if (logger.isDebugEnabled()){
			logger.debug("setHandler - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - name=" + name);
		}
		
		super.setHandler(name);
		sessionMetadataDirty();
		metadata.getMetaData().put(HANDLER, name);
	}
	
	@Override
	public String getHandler() {
		if (logger.isDebugEnabled()){
			logger.debug("getHandler - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + ", return=" + super.getHandler());
			logger.debug("getHandler - this.getHaId()=" + this.getHaId());
			logger.debug("getHandler - this.getId()=" + this.getId());
		}
		
		return super.getHandler();
	}
	
	@Override
	public void setInvalidateWhenReady(boolean arg0) {
		if (logger.isDebugEnabled()){
			logger.debug("setInvalidateWhenReady - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - arg0=" + arg0);
		}
		
		super.setInvalidateWhenReady(arg0);
		sessionMetadataDirty();		
		metadata.getMetaData().put(INVALIDATE_WHEN_READY, arg0);
	}

	@Override
	public void setReadyToInvalidate(boolean readyToInvalidate) {
		if (logger.isDebugEnabled()){
			logger.debug("setReadyToInvalidate - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - readyToInvalidate=" + readyToInvalidate);
		}
		
		boolean oldReadyToInvalidate = this.readyToInvalidate;
		super.setReadyToInvalidate(readyToInvalidate);
		if(oldReadyToInvalidate != readyToInvalidate) {
			sessionMetadataDirty();
			metadata.getMetaData().put(READY_TO_INVALIDATE, readyToInvalidate);
		}
	}
	
	@Override
	public void setValid(boolean isValid) {
		if (logger.isDebugEnabled()){
			logger.debug("setValid - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - isValid=" + isValid);
		}
		
		super.setValid(isValid);
		sessionMetadataDirty();
		metadata.getMetaData().put(IS_VALID, isValid);
	}
	
	@Override
	public void setSessionCreatingDialog(Dialog dialog) {
		if (logger.isDebugEnabled()){
			logger.debug("setSessionCreatingDialog - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		if(logger.isDebugEnabled()) {			
			if(super.sessionCreatingDialog != null) {
				logger.debug(" oldDialogId " + sessionCreatingDialogId);
			}
		}
		super.setSessionCreatingDialog(dialog);
		if(logger.isDebugEnabled()) {
			logger.debug(" dialog " + dialog);
			if(dialog != null) {
				logger.debug(" dialogId " + dialog.getDialogId());
			}			
		}
		if(dialog != null && dialog.getDialogId() != null && !dialog.getDialogId().equals(sessionCreatingDialogId)) {			
			if(dialog != null) {
				if (logger.isDebugEnabled()){
					logger.debug("DialogId set to " + dialog.getDialogId());
				}
			}
			sessionMetadataDirty();
			metadata.getMetaData().put(DIALOG_ID, dialog.getDialogId() );
			sessionCreatingDialogId = dialog.getDialogId();
		}
	}
	
	@Override
	public void setTransport(String transport) {
		if (logger.isDebugEnabled()){
			logger.debug("setTransport - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId() + " - transport=" + transport);
		}
		
		super.setTransport(transport);
		sessionMetadataDirty();
		metadata.getMetaData().put(TRANSPORT, transport );
	}
	
	public String getHaId() {
		return haId;
	}
	
	@Override
	public void passivate() {
		if (logger.isDebugEnabled()){
			logger.debug("passivate - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		notifyWillPassivate(ClusteredSessionNotificationCause.PASSIVATION);
		processDialogPassivation();
		sipApplicationSession = null;
	}

	public void processDialogPassivation() {	
		if (logger.isDebugEnabled()){
			logger.debug("processDialogPassivation - this.getHaId()=" + this.getHaId() + ", this.getId()=" + this.getId());
		}
		
		if(sessionCreatingDialog != null) {
			((ClusteredSipStack)StaticServiceHolder.sipStandardService.getSipStack()).passivateDialog((HASipDialog)sessionCreatingDialog);
			TransactionApplicationData  applicationData = ((TransactionApplicationData)sessionCreatingDialog.getApplicationData());
			if(applicationData != null) {
				applicationData.cleanUp();
				applicationData.getSipServletMessage().cleanUp();
				applicationData.getSipServletMessage().setSipSession(null);
				sessionCreatingDialog.setApplicationData(null);
			}
		}
		sessionCreatingDialog = null;
	}
	
}
