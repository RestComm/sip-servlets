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

import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.SIPDialog;

import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Principal;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.SipStack;
import javax.sip.address.URI;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;
import org.apache.log4j.Logger;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSipSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheConvergedSipManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributedCacheManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.IncomingDistributableSessionData;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.jboss.web.tomcat.service.session.notification.ClusteredSessionManagementStatus;
import org.jboss.web.tomcat.service.session.notification.ClusteredSessionNotificationCause;
import org.jboss.web.tomcat.service.session.notification.ClusteredSipSessionNotificationPolicy;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.B2buaHelperImpl;
import org.mobicents.servlet.sip.message.MobicentsSipSessionFacade;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.startup.SipService;

/**
 * Abstract base class for sip session clustering based on SipSessionImpl. Different session
 * replication strategy can be implemented such as session- field- or attribute-based ones.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class ClusteredSipSession<O extends OutgoingDistributableSessionData> extends SipSessionImpl {

	private static transient final Logger logger = Logger.getLogger(ClusteredSipSession.class);

	
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

	/**
	 * The string manager for this package.
	 */
	protected static final StringManager sm = StringManager
			.getManager(ClusteredSession.class.getPackage().getName());

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
	 * The time this session was created, in milliseconds since midnight,
	 * January 1, 1970 GMT.
	 */
	private long creationTime = 0L;

	/**
	 * We are currently processing a session expiration, so bypass certain
	 * IllegalStateException tests. NOTE: This value is not included in the
	 * serialized version of this object.
	 */
	private volatile transient boolean expiring = false;

	/**
	 * The facade associated with this session. NOTE: This value is not included
	 * in the serialized version of this object.
	 */
	private transient MobicentsSipSessionFacade facade = null;

	/**
	 * The session identifier of this Session.
	 */
	private String id = null;

	/**
	 * The last accessed time for this Session.
	 */
	private volatile long lastAccessedTime = creationTime;

	/**
	 * The Manager with which this Session is associated.
	 */
	private transient ClusteredSipManager<O> manager = null;

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
	 * Flag indicating whether this session is valid or not.
	 */
	private volatile boolean isValid = false;

	/**
	 * Internal notes associated with this session by Catalina components and
	 * event listeners. <b>IMPLEMENTATION NOTE:</b> This object is <em>not</em>
	 * saved and restored across session serializations!
	 */
	private final transient Map<String, Object> notes = new Hashtable<String, Object>();

	/**
	 * The authenticated Principal associated with this session, if any.
	 * <b>IMPLEMENTATION NOTE:</b> This object is <i>not</i> saved and restored
	 * across session serializations!
	 */
	private transient Principal principal = null;

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
	private volatile transient DistributableSipSessionMetadata metadata = new DistributableSipSessionMetadata();

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
	 * The session's id with any jvmRoute removed.
	 */
	private transient String realId;

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
	private transient boolean alwaysReplicateTimestamp = true;

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
	
	// ------------------------------------------------------------ Constructors

	protected ClusteredSipSession(SipSessionKey key, SipFactoryImpl sipFactoryImpl, MobicentsSipApplicationSession mobicentsSipApplicationSession, boolean useJK) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession);
		this.invalidationPolicy = this.manager.getReplicationTrigger();
		this.useJK = useJK;
		this.firstAccess = true;
		accessCount = ACTIVITY_CHECK ? new AtomicInteger() : null;
		// it starts with true so that it gets replicated when first created
		sessionMetadataDirty = true;
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

	public long getCreationTime() {
		if (!isValidInternal())
			throw new IllegalStateException(sm
					.getString("clusteredSession.getCreationTime.ise"));

		return (this.creationTime);
	}

	/**
	 * Set the creation time for this session. This method is called by the
	 * Manager when an existing Session instance is reused.
	 * 
	 * @param time
	 *            The new creation time
	 */
	public void setCreationTime(long time) {
		this.creationTime = time;
		this.lastAccessedTime = time;
		this.thisAccessedTime = time;
		sessionMetadataDirty();
	}

	
	public long getLastAccessedTime() {
		if (!isValidInternal()) {
			throw new IllegalStateException(sm
					.getString("clusteredSession.getLastAccessedTime.ise"));
		}

		return (this.lastAccessedTime);
	}

	public long getLastAccessedTimeInternal() {
		return (this.lastAccessedTime);
	}

	public SipManager getManager() {
		return (this.manager);
	}

	public void setManager(SipManager manager) {
		if ((manager instanceof ClusteredSipManager) == false)
			throw new IllegalArgumentException(
					"manager must implement ClusteredSipManager");
		@SuppressWarnings("unchecked")
		ClusteredSipManager<O> unchecked = (ClusteredSipManager) manager;
		this.manager = unchecked;

		this.invalidationPolicy = this.manager.getReplicationTrigger();
		this.useJK = this.manager.getUseJK();

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
		if (isValid && interval == 0) {
			expire();
		}
		checkAlwaysReplicateTimestamp();
		sessionMetadataDirty();
	}

	public Principal getPrincipal() {
		return (this.principal);
	}

	/**
	 * Set the authenticated Principal that is associated with this Session.
	 * This provides an <code>Authenticator</code> with a means to cache a
	 * previously authenticated Principal, and avoid potentially expensive
	 * <code>Realm.authenticate()</code> calls on every request.
	 * 
	 * @param principal
	 *            The new Principal, or <code>null</code> if none
	 */
	public void setPrincipal(Principal principal) {
		Principal oldPrincipal = this.principal;
		this.principal = principal;
		support.firePropertyChange("principal", oldPrincipal, this.principal);

		if ((oldPrincipal != null && !oldPrincipal.equals(principal))
				|| (oldPrincipal == null && principal != null))
			sessionMetadataDirty();
	}

	public void access() {
		this.lastAccessedTime = this.thisAccessedTime;
		this.thisAccessedTime = System.currentTimeMillis();

		if (ACTIVITY_CHECK) {
			accessCount.incrementAndGet();
		}

		// JBAS-3528. If it's not the first access, make sure
		// the 'new' flag is correct
		if (!firstAccess && isNew) {
			setNew(false);
		}
	}

	public void endAccess() {
		isNew = false;

		if (ACTIVITY_CHECK) {
			accessCount.decrementAndGet();
		}

		this.lastAccessedTime = this.thisAccessedTime;

		if (firstAccess) {
			firstAccess = false;
			// Tomcat marks the session as non new, but that's not really
			// accurate per SRV.7.2, as the second request hasn't come in yet
			// So, we fix that
			isNew = true;
		}
	}

	public boolean isNew() {
		if (!isValidInternal())
			throw new IllegalStateException(sm
					.getString("clusteredSession.isNew.ise"));

		return (this.isNew);
	}

	public void setNew(boolean isNew) {
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
	 * Overrides the {@link StandardSession#isValid() superclass method} to call @
	 * #isValid(boolean) isValid(true)} .
	 */
	public boolean isValid() {
		return isValid(true);
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
		sessionMetadataDirty();
	}

	/**
	 * Invalidates this session and unbinds any objects bound to it. Overridden
	 * here to remove across the cluster instead of just expiring.
	 * 
	 * @exception IllegalStateException
	 *                if this method is called on an invalidated session
	 */
	public void invalidate() {
		if (!isValid())
			throw new IllegalStateException(sm
					.getString("clusteredSession.invalidate.ise"));

		// Cause this session to expire globally
		boolean notify = true;
		boolean localCall = true;
		boolean localOnly = false;
		expire(notify, localCall, localOnly,
				ClusteredSessionNotificationCause.INVALIDATE);
	}

	public void expire() {
		expire(true);
	}

	/**
	 * Expires the session, but in such a way that other cluster nodes are
	 * unaware of the expiration.
	 * 
	 * @param notify
	 */
	public void expire(boolean notify) {
		boolean localCall = true;
		boolean localOnly = true;
		expire(notify, localCall, localOnly,
				ClusteredSessionNotificationCause.TIMEOUT);
	}

	public Object getNote(String name) {
		return (notes.get(name));
	}

	@SuppressWarnings("unchecked")
	public Iterator getNoteNames() {
		return (notes.keySet().iterator());
	}

	public void setNote(String name, Object value) {
		notes.put(name, value);
	}

	public void removeNote(String name) {
		notes.remove(name);
	}

	
	// ------------------------------------------------------------ HttpSession

	public ServletContext getServletContext() {
		if (manager == null)
			return (null);
		Context context = (Context) manager.getContainer();
		if (context == null)
			return (null);
		else
			return (context.getServletContext());
	}

	public Object getAttribute(String name) {
		if (!isValid())
			throw new IllegalStateException(sm
					.getString("clusteredSession.getAttribute.ise"));

		return getAttributeInternal(name);
	}

	@SuppressWarnings("unchecked")
	public Enumeration getAttributeNames() {
		if (!isValid())
			throw new IllegalStateException(sm
					.getString("clusteredSession.getAttributeNames.ise"));

		return (new Enumerator(getAttributesInternal().keySet(), true));
	}

	public void setAttribute(String name, Object value) {
		// Name cannot be null
		if (name == null)
			throw new IllegalArgumentException(sm
					.getString("clusteredSession.setAttribute.namenull"));

		// Null value is the same as removeAttribute()
		if (value == null) {
			removeAttribute(name);
			return;
		}

		// Validate our current state
		if (!isValidInternal()) {
			throw new IllegalStateException(sm
					.getString("clusteredSession.setAttribute.ise"));
		}

		if (canAttributeBeReplicated(value) == false) {
			throw new IllegalArgumentException(sm
					.getString("clusteredSession.setAttribute.iae"));
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
			event = new SipSessionBindingEvent(getSession(), name);
			try {
				((SipSessionBindingListener) value).valueBound(event);
			} catch (Throwable t) {
				manager.getContainer().getLogger().error(
						sm.getString("clusteredSession.bindingEvent"), t);
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
						.valueUnbound(new SipSessionBindingEvent(getSession(),
								name));
			} catch (Throwable t) {
				manager.getContainer().getLogger().error(
						sm.getString("clusteredSession.bindingEvent"), t);
			}
		}

		// Notify interested application event listeners
		if (notificationPolicy.isSipSessionAttributeListenerInvocationAllowed(
				this.clusterStatus, ClusteredSessionNotificationCause.MODIFY,
				name, true)) {
			Context context = (Context) manager.getContainer();
			Object lifecycleListeners[] = context
					.getApplicationEventListeners();
			if (lifecycleListeners == null)
				return;
			for (int i = 0; i < lifecycleListeners.length; i++) {
				if (!(lifecycleListeners[i] instanceof SipSessionAttributeListener))
					continue;
				SipSessionAttributeListener listener = (SipSessionAttributeListener) lifecycleListeners[i];
				try {
					if (unbound != null) {
						fireContainerEvent(context,
								"beforeSessionAttributeReplaced", listener);
						if (event == null) {
							event = new SipSessionBindingEvent(getSession(),
									name);
						}
						listener.attributeReplaced(event);
						fireContainerEvent(context,
								"afterSessionAttributeReplaced", listener);
					} else {
						fireContainerEvent(context,
								"beforeSessionAttributeAdded", listener);
						if (event == null) {
							event = new SipSessionBindingEvent(getSession(),
									name);
						}
						listener.attributeAdded(event);
						fireContainerEvent(context,
								"afterSessionAttributeAdded", listener);
					}
				} catch (Throwable t) {
					try {
						if (unbound != null) {
							fireContainerEvent(context,
									"afterSessionAttributeReplaced", listener);
						} else {
							fireContainerEvent(context,
									"afterSessionAttributeAdded", listener);
						}
					} catch (Exception e) {
						;
					}
					manager.getContainer().getLogger().error(
							sm.getString("clusteredSession.attributeEvent"), t);
				}
			}
		}
	}

	public void removeAttribute(String name) {
		// Validate our current state
		if (!isValidInternal())
			throw new IllegalStateException(sm
					.getString("clusteredSession.removeAttribute.ise"));

		final boolean localCall = true;
		final boolean localOnly = false;
		final boolean notify = true;
		removeAttributeInternal(name, localCall, localOnly, notify,
				ClusteredSessionNotificationCause.MODIFY);
	}

	public Object getValue(String name) {
		return (getAttribute(name));
	}

	public String[] getValueNames() {
		if (!isValidInternal())
			throw new IllegalStateException(sm
					.getString("clusteredSession.getValueNames.ise"));

		return (keys());
	}

	public void putValue(String name, Object value) {
		setAttribute(name, value);
	}

	public void removeValue(String name) {
		removeAttribute(name);
	}

	// ---------------------------------------------------- DistributableSession

	/**
	 * Gets the session id with any appended jvmRoute info removed.
	 * 
	 * @see #getUseJK()
	 */
	public String getRealId() {
		return realId;
	}

	protected int getVersion() {
		return version.get();
	}

	public boolean getMustReplicateTimestamp() {
		// If the access times are the same, access() was never called
		// on the session
		boolean touched = this.thisAccessedTime != this.lastAccessedTime;
		boolean exceeds = alwaysReplicateTimestamp && touched;

		if (!exceeds && touched && maxUnreplicatedInterval > 0) // -1 means
																// ignore
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

	protected boolean isSessionMetadataDirty() {
		return sessionMetadataDirty;
	}

	protected DistributableSessionMetadata getSessionMetadata() {
		this.metadata.setId(id);
		this.metadata.setCreationTime(creationTime);
		this.metadata.setMaxInactiveInterval(maxInactiveInterval);
		this.metadata.setNew(isNew);
		this.metadata.setValid(isValid);
		
		getSipSessionMetadata();
		
		return this.metadata;
	}

	protected void getSipSessionMetadata() {
		this.metadata.setSipApplicationSessionKey(sipApplicationSession.getKey());
		this.metadata.setSipSessionKey(key);
		this.metadata.setB2buaHelper(b2buaHelper);
		this.metadata.setProxy(proxy);
		this.metadata.setHandlerServlet(handlerServlet);
		this.metadata.setInvalidateWhenReady(invalidateWhenReady);
		this.metadata.setReadyToInvalidate(readyToInvalidate);
		this.metadata.setRoutingRegion(routingRegion);
		this.metadata.setSipDialog((SIPDialog)sessionCreatingDialog);
		if(subscriberURI != null) {
			this.metadata.setSubscriberURI(subscriberURI.toString());
		}
	}

	protected boolean isSessionAttributeMapDirty() {
		return sessionAttributesDirty;
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(IncomingDistributableSessionData sessionData) {
		assert sessionData != null : "sessionData is null";

		this.version.set(sessionData.getVersion());

		long ts = sessionData.getTimestamp();
		this.lastAccessedTime = this.thisAccessedTime = ts;
		this.timestamp.set(ts);

		DistributableSipSessionMetadata md = (DistributableSipSessionMetadata)sessionData.getMetadata();
		// TODO -- get rid of these field and delegate to metadata
		this.id = md.getId();
		this.creationTime = md.getCreationTime();
		this.maxInactiveInterval = md.getMaxInactiveInterval();
		this.isNew = md.isNew();
		this.isValid = md.isValid();
		this.metadata = md;
		
		updateSipSession(md);
		
		// Get our id without any jvmRoute appended
//		parseRealId(id);

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

		this.clusterStatus = new ClusteredSessionManagementStatus(this.realId,
				true, null, null);

		checkAlwaysReplicateTimestamp();

		populateAttributes(sessionData.getSessionAttributes());

		// TODO uncomment when work on JBAS-1900 is completed
		// // Session notes -- for FORM auth apps, allow replicated session
		// // to be used without requiring a new login
		// // We use the superclass set/removeNote calls here to bypass
		// // the custom logic we've added
		// String username = (String) in.readObject();
		// if (username != null)
		// {
		// super.setNote(Constants.SESS_USERNAME_NOTE, username);
		// }
		// else
		// {
		// super.removeNote(Constants.SESS_USERNAME_NOTE);
		// }
		// String password = (String) in.readObject();
		// if (password != null)
		// {
		// super.setNote(Constants.SESS_PASSWORD_NOTE, password);
		// }
		// else
		// {
		// super.removeNote(Constants.SESS_PASSWORD_NOTE);
		// }

		// We are no longer outdated vis a vis distributed cache
		clearOutdated();
	}

	protected void updateSipSession(DistributableSipSessionMetadata md) {
		//From SipSession
		key = md.getSipSessionKey();
		sipAppSessionParentKey = md.getSipApplicationSessionKey();
		routingRegion = (SipApplicationRoutingRegion) md.getRoutingRegion();
		handlerServlet = md.getHandlerServlet();
		if(logger.isDebugEnabled()) {
			logger.debug("reading handlerServlet "+ handlerServlet);
		}
		String subscriberURIStringified = md.getSubscriberURI();
		if(subscriberURIStringified != null) {
			try {
				URI jsipSubscriberUri = SipFactories.addressFactory.createURI(subscriberURIStringified);				
				if(jsipSubscriberUri instanceof javax.sip.address.SipURI) {
					subscriberURI = new SipURIImpl((javax.sip.address.SipURI)jsipSubscriberUri);
				} else if (jsipSubscriberUri instanceof javax.sip.address.TelURL) {
					subscriberURI = new TelURLImpl((javax.sip.address.TelURL)jsipSubscriberUri);
				}
			} catch (ParseException pe) {
				logger.error("Impossible to parse the subscriber URI " 
						+ subscriberURIStringified, pe);
			}
		}
		sessionCreatingDialog = (SIPDialog) md.getSipDialog();
		if(logger.isDebugEnabled()) {
			logger.debug("deserialized dialog for the sip session "+ sessionCreatingDialog);
		}
		invalidateWhenReady = md.isInvalidateWhenReady();
		readyToInvalidate = md.isReadyToInvalidate();
		proxy = (ProxyImpl) md.getProxy();
		b2buaHelper = (B2buaHelperImpl) md.getB2buaHelper();
		if(proxy != null) {
			proxy.setSipFactoryImpl(sipFactory);
		}
		if(b2buaHelper != null) {
			b2buaHelper.setSipFactoryImpl(sipFactory);
			b2buaHelper.setSipManager(manager);
		}
		//inject the dialog into the available sip stacks
		if(logger.isDebugEnabled()) {
			logger.debug("dialog to inject " + sessionCreatingDialog);
		}
		Container context = manager.getContainer();
		Container container = context.getParent().getParent();
		if(container instanceof Engine) {
			Service service = ((Engine)container).getService();
			if(service instanceof SipService) {
				Connector[] connectors = service.findConnectors();
				for (Connector connector : connectors) {
					SipStack sipStack = (SipStack)
						connector.getProtocolHandler().getAttribute(SipStack.class.getSimpleName());
					if(sipStack != null && sessionCreatingDialog!= null && ((SipStackImpl)sipStack).getDialog(sessionCreatingDialog.getDialogId()) == null && sipStack.getSipProviders().hasNext()) {
						((SIPDialog)sessionCreatingDialog).setSipProvider((SipProviderImpl)sipStack.getSipProviders().next());
						((SipStackImpl)sipStack).putDialog((SIPDialog)sessionCreatingDialog);
						if(logger.isDebugEnabled()) {
							logger.debug("dialog injected " + sessionCreatingDialog);
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
		// Replicate the session.
		if (log.isTraceEnabled()) {
			log
					.trace("processSessionReplication(): session is dirty. Will increment "
							+ "version from: "
							+ getVersion()
							+ " and replicate.");
		}
		version.incrementAndGet();

		O outgoingData = getOutgoingSipSessionData();
		distributedCacheManager.storeSipSessionData(outgoingData);

		sessionAttributesDirty = false;
		sessionMetadataDirty = false;

		lastReplicated = System.currentTimeMillis();
	}

	protected abstract O getOutgoingSipSessionData();

	/**
	 * Remove myself from the distributed cache.
	 */
	public void removeMyself() {
		getDistributedCacheManager().removeSession(getRealId());
	}

	/**
	 * Remove myself from the <t>local</t> instance of the distributed cache.
	 */
	public void removeMyselfLocal() {
		getDistributedCacheManager().removeSessionLocal(getRealId());
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
	 * This is called specifically for failover case using mod_jk where the new
	 * session has this node name in there. As a result, it is safe to just
	 * replace the id since the backend store is using the "real" id without the
	 * node name.
	 * 
	 * @param id
	 */
	public void resetIdWithRouteInfo(String id) {
		this.id = id;
		parseRealId(id);
	}

	/**
	 * Gets whether the session expects to be accessible via mod_jk, mod_proxy,
	 * mod_cluster or other such AJP-based load balancers.
	 */
	public boolean getUseJK() {
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
		return thisAccessedTime < outdatedTime;
	}

	public boolean isSessionDirty() {
		return sessionAttributesDirty || sessionMetadataDirty;
	}

	/** Inform any SipSessionListener of the creation of this session */
	public void tellNew(ClusteredSessionNotificationCause cause) {
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
						manager.getContainer().getLogger().error(
								sm.getString("clusteredSession.sessionEvent"),
								t);
					}
				}
			}
		}
	}

	/**
	 * Returns whether the current session is still valid, but only calls
	 * {@link #expire(boolean)} for timed-out sessions if
	 * <code>expireIfInvalid</code> is <code>true</code>.
	 * 
	 * @param expireIfInvalid
	 *            <code>true</code> if sessions that have been timed out should
	 *            be expired
	 */
	public boolean isValid(boolean expireIfInvalid) {
		if (this.expiring) {
			return true;
		}

		if (!this.isValid) {
			return false;
		}

		if (ACTIVITY_CHECK && accessCount.get() > 0) {
			return true;
		}

		if (maxInactiveInterval >= 0) {
			long timeNow = System.currentTimeMillis();
			int timeIdle = (int) ((timeNow - thisAccessedTime) / 1000L);
			if (timeIdle >= maxInactiveInterval) {
				if (expireIfInvalid)
					expire(true);
				else
					return false;
			}
		}

		return (this.isValid);

	}

	/**
	 * Expires the session, notifying listeners and possibly the manager.
	 * <p>
	 * <strong>NOTE:</strong> The manager will only be notified of the
	 * expiration if <code>localCall</code> is <code>true</code>; otherwise it
	 * is the responsibility of the caller to notify the manager that the
	 * session is expired. (In the case of JBossCacheManager, it is the manager
	 * itself that makes such a call, so it of course is aware).
	 * </p>
	 * 
	 * @param notify
	 *            whether servlet spec listeners should be notified
	 * @param localCall
	 *            <code>true</code> if this call originated due to local
	 *            activity (such as a session invalidation in user code or an
	 *            expiration by the local background processing thread);
	 *            <code>false</code> if the expiration originated due to some
	 *            kind of event notification from the cluster.
	 * @param localOnly
	 *            <code>true</code> if the expiration should not be announced to
	 *            the cluster, <code>false</code> if other cluster nodes should
	 *            be made aware of the expiration. Only meaningful if
	 *            <code>localCall</code> is <code>true</code>.
	 * @param cause
	 *            the cause of the expiration
	 */
	public void expire(boolean notify, boolean localCall, boolean localOnly,
			ClusteredSessionNotificationCause cause) {
		if (log.isTraceEnabled()) {
			log.trace("The session has expired with id: " + id
					+ " -- is expiration local? " + localOnly);
		}

		// If another thread is already doing this, stop
		if (expiring)
			return;

		synchronized (this) {
			// If we had a race to this sync block, another thread may
			// have already completed expiration. If so, don't do it again
			if (!isValid)
				return;

			if (manager == null)
				return;

			expiring = true;

			// Notify interested application event listeners
			// FIXME - Assumes we call listeners in reverse order
			Context context = (Context) manager.getContainer();
			Object lifecycleListeners[] = context
					.getApplicationLifecycleListeners();
			if (notify
					&& (lifecycleListeners != null)
					&& notificationPolicy
							.isSipSessionListenerInvocationAllowed(
									this.clusterStatus, cause, localCall)) {
				SipSessionEvent event = new SipSessionEvent(getSession());
				for (int i = 0; i < lifecycleListeners.length; i++) {
					int j = (lifecycleListeners.length - 1) - i;
					if (!(lifecycleListeners[j] instanceof SipSessionListener))
						continue;
					SipSessionListener listener = (SipSessionListener) lifecycleListeners[j];
					try {
						fireContainerEvent(context, "beforeSessionDestroyed",
								listener);
						listener.sessionDestroyed(event);
						fireContainerEvent(context, "afterSessionDestroyed",
								listener);
					} catch (Throwable t) {
						try {
							fireContainerEvent(context,
									"afterSessionDestroyed", listener);
						} catch (Exception e) {
							;
						}
						manager.getContainer().getLogger().error(
								sm.getString("clusteredSession.sessionEvent"),
								t);
					}
				}
			}

			if (ACTIVITY_CHECK) {
				accessCount.set(0);
			}

			// Notify interested session event listeners.
//			if (notify) {
//				fireSessionEvent(Session.SESSION_DESTROYED_EVENT, null);
//			}

			// JBAS-1360 -- Unbind any objects associated with this session
			String keys[] = keys();
			for (int i = 0; i < keys.length; i++) {
				removeAttributeInternal(keys[i], localCall, localOnly, notify,
						cause);
			}

			// Remove this session from our manager's active sessions
			// If !localCall, this expire call came from the manager,
			// so don't recurse
			if (localCall) {
				removeFromManager(localOnly);
			}

			// We have completed expire of this session
			setValid(false);
			expiring = false;
		}

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
		// Notify interested session event listeners
//		fireSessionEvent(Session.SESSION_PASSIVATED_EVENT, null);

		if (hasActivationListener != Boolean.FALSE) {
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
						if (event == null)
							event = new SipSessionEvent(this);

						try {
							((SipSessionActivationListener) attribute)
									.sessionWillPassivate(event);
						} catch (Throwable t) {
							manager
									.getContainer()
									.getLogger()
									.error(
											sm
													.getString("clusteredSession.attributeEvent"),
											t);
						}
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
						if (event == null)
							event = new SipSessionEvent(getSession());
						try {
							((SipSessionActivationListener) attribute)
									.sessionDidActivate(event);
						} catch (Throwable t) {
							manager
									.getContainer()
									.getLogger()
									.error(
											sm
													.getString("clusteredSession.attributeEvent"),
											t);
						}
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
		return needsPostReplicateActivation;
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append('[')
				.append("id: ").append(id).append(" lastAccessedTime: ")
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
		Map<String, Object> existing = getAttributesInternal();
		Map<String, Object> excluded = removeExcludedAttributes(existing);

		existing.clear();

		existing.putAll(distributedCacheAttributes);
		if (excluded != null)
			existing.putAll(excluded);
	}

	protected final Map<String, Object> getAttributesInternal() {
		return attributes;
	}

	protected final ClusteredSipManager<O> getManagerInternal() {
		return manager;
	}

	protected final DistributedCacheManager<O> getDistributedCacheManager() {
		return distributedCacheManager;
	}

	protected final void setDistributedCacheManager(
			DistributedCacheConvergedSipManager<O> distributedCacheManager) {
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
		if (!sessionAttributesDirty && log.isTraceEnabled())
			log.trace("Marking session attributes dirty " + id);

		sessionAttributesDirty = true;
	}

	protected final void setHasActivationListener(boolean hasListener) {
		this.hasActivationListener = Boolean.valueOf(hasListener);
	}

	// ----------------------------------------------------------------- Private

	private void checkAlwaysReplicateTimestamp() {
		this.alwaysReplicateTimestamp = (maxUnreplicatedInterval == 0 || (maxUnreplicatedInterval > 0
				&& maxInactiveInterval >= 0 && maxUnreplicatedInterval > (maxInactiveInterval * 1000)));
	}

	private void parseRealId(String sessionId) {
		String newId = null;
		if (useJK)
			newId = Util.getRealId(sessionId);
		else
			newId = sessionId;

		// realId is used in a lot of map lookups, so only replace it
		// if the new id is actually different -- preserve object identity
		if (!newId.equals(realId))
			realId = newId;
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
			event = new SipSessionBindingEvent(getSession(), name);
			((SipSessionBindingListener) value).valueUnbound(event);
		}

		// Notify interested application event listeners
		if (notificationPolicy.isSipSessionAttributeListenerInvocationAllowed(
				this.clusterStatus, cause, name, localCall)) {
			Context context = (Context) manager.getContainer();
			Object lifecycleListeners[] = context
					.getApplicationEventListeners();
			if (lifecycleListeners == null)
				return;
			for (int i = 0; i < lifecycleListeners.length; i++) {
				if (!(lifecycleListeners[i] instanceof SipSessionAttributeListener))
					continue;
				SipSessionAttributeListener listener = (SipSessionAttributeListener) lifecycleListeners[i];
				try {
					fireContainerEvent(context,
							"beforeSessionAttributeRemoved", listener);
					if (event == null) {
						event = new SipSessionBindingEvent(getSession(), name);
					}
					listener.attributeRemoved(event);
					fireContainerEvent(context, "afterSessionAttributeRemoved",
							listener);
				} catch (Throwable t) {
					try {
						fireContainerEvent(context,
								"afterSessionAttributeRemoved", listener);
					} catch (Exception e) {
						;
					}
					manager.getContainer().getLogger().error(
							sm.getString("clusteredSession.attributeEvent"), t);
				}
			}
		}
	}

	private String[] keys() {
		Set<String> keySet = getAttributesInternal().keySet();
		return ((String[]) keySet.toArray(new String[keySet.size()]));
	}

	/**
	 * Return the <code>isValid</code> flag for this session without any
	 * expiration check.
	 */
	private boolean isValidInternal() {
		return (this.isValid || this.expiring);
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
		if (!sessionMetadataDirty && !isNew && log.isTraceEnabled())
			log.trace("Marking session metadata dirty " + id);
		sessionMetadataDirty = true;
	}

	/**
	 * Advise our manager to remove this expired session.
	 * 
	 * @param localOnly
	 *            whether the rest of the cluster should be made aware of the
	 *            removal
	 */
	private void removeFromManager(boolean localOnly) {
		if (localOnly) {
			manager.removeLocal(this);
		} else {
			manager.removeSipSession(key);
		}
	}

	private final void clearOutdated() {
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
}
