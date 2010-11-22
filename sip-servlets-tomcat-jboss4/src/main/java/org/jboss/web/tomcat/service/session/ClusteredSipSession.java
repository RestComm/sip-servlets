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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.sip.Dialog;

import org.apache.catalina.Globals;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;
import org.apache.log4j.Logger;
import org.jboss.metadata.WebMetaData;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * Abstract base class for sip session clustering based on SipSessionImpl. Different session
 * replication strategy can be implemented such as session- field- or attribute-based ones.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class ClusteredSipSession extends SipSessionImpl  
	implements Externalizable {

	private static final Logger logger = Logger.getLogger(ClusteredSipSession.class);

	protected static final String B2B_SESSION_MAP = "b2bsm";
	protected static final String B2B_SESSION_SIZE = "b2bss";
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
	protected static final Set replicationExcludes;
	static {
		HashSet set = new HashSet();
		for (int i = 0; i < excludedAttributes.length; i++) {
			set.add(excludedAttributes[i]);
		}
		replicationExcludes = Collections.unmodifiableSet(set);
	}

	protected int invalidationPolicy;

	/**
	 * If true, means the local in-memory session data contains changes that
	 * have not been published to the distributed cache.
	 * 
	 * @deprecated not used
	 */
	protected transient boolean isSessionModifiedSinceLastSave;

	/**
	 * If true, means the local in-memory session data contains metadata changes
	 * that have not been published to the distributed cache.
	 */
	protected transient boolean sessionLastAccessTimeDirty;
	
	/**
	 * If true, means the local in-memory session data contains metadata changes
	 * that have not been published to the distributed cache.
	 */
	protected transient boolean sessionMetadataDirty;

	/**
	 * If true, means the local in-memory session data contains attribute
	 * changes that have not been published to the distributed cache.
	 */
	protected transient boolean sessionAttributesDirty;

	/**
	 * The last version that was passed to {@link #setOutdatedVersion} or
	 * <code>0</code> if <code>setIsOutdated(false)</code> was subsequently
	 * called.
	 */
	protected transient int outdatedVersion;

	/**
	 * The last time {@link #setIsOutdated setIsOutdated(true)} was called or
	 * <code>0</code> if <code>setIsOutdated(false)</code> was subsequently
	 * called.
	 */
	protected transient long outdatedTime;

	/**
	 * Version number to track cache invalidation. If any new version number is
	 * greater than this one, it means the data it holds is newer than this one.
	 */
	protected int version;

	/**
	 * The session's id with any jvmRoute removed.
	 */
//	protected transient String realId;

	/**
	 * Whether JK is being used, in which case our realId will not match our id
	 */
	private transient boolean useJK;
	
	/**
	 * If this session is new
	 */
	protected transient boolean isNew;

	/**
	 * Timestamp when we were last replicated.
	 */
	protected transient long lastReplicated;

	/**
	 * @deprecated Not used
	 */
	protected transient int maxUnreplicatedFactor = 80;

	/**
	 * Maximum number of milliseconds this session should be allowed to go
	 * unreplicated if access to the session doesn't mark it as dirty.
	 */
	protected transient long maxUnreplicatedInterval = -1;
	
	/** True if maxUnreplicatedInterval is 0 or less than maxInactiveInterval */
	protected transient boolean alwaysReplicateMetadata = false;

	/**
	 * Whether any of this session's attributes implement
	 * HttpSessionActivationListener.
	 */
	protected transient Boolean hasActivationListener;

	/**
	 * Has this session only been accessed once?
	 */
	protected transient boolean firstAccess;
	/**
	 * SIP Dialog id
	 */
	protected transient String sessionCreatingDialogId = null;

	// Transient map to store meta data changes for replication.
	protected transient Map<String, Object> metaModifiedMap_ = new ConcurrentHashMap<String, Object>();
	
	private transient String haId;

	protected transient JBossCacheSipManager manager;
	
	/**
	 * The string manager for this package.
	 */
	protected static StringManager sm = StringManager
			.getManager(ClusteredSession.class.getPackage().getName());


	protected ClusteredSipSession(SipSessionKey key, SipFactoryImpl sipFactoryImpl, MobicentsSipApplicationSession mobicentsSipApplicationSession, boolean useJK) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession);
		if(mobicentsSipApplicationSession.getSipContext() != null) {
			manager = ((JBossCacheSipManager)mobicentsSipApplicationSession.getSipContext().getSipManager());
			invalidationPolicy = manager.getInvalidateSessionPolicy();
		}
		this.useJK = useJK;
		this.firstAccess = true;
		isNew = true;
		// it starts with true so that it gets replicated when first created
		sessionMetadataDirty = true;
		checkAlwaysReplicateMetadata();
		haId = SessionManagerUtil.getSipSessionHaKey(key);
	}

	/**
	 * Check to see if the session data is still valid. Outdated here means that
	 * the in-memory data is not in sync with one in the data store.
	 * 
	 * @return
	 */
	public boolean isOutdated() {
		return lastAccessedTime < outdatedTime;
	}

	/**
	 * Marks this session as outdated or up-to-date vis-a-vis the distributed
	 * cache.
	 * 
	 * @param outdated
	 * 
	 * @deprecated use {@link #setOutdatedVersion(int)} and
	 *             {@link #clearOutdated()}
	 */
	public void setIsOutdated(boolean outdated) {
		if (outdated)
			outdatedTime = System.currentTimeMillis();
		else
			clearOutdated();
	}

	public void setOutdatedVersion(int version) {
		this.outdatedVersion = version;
		outdatedTime = System.currentTimeMillis();
	}

	public void clearOutdated() {
		// Only overwrite the access time if access() hasn't been called
		// since setOutdatedVersion() was called
		//FIXME
//		if (outdatedTime > lastAccessedTime) {
//			lastAccessedTime = thisAccessedTime;
//			thisAccessedTime = outdatedTime;
//		}
		outdatedTime = 0;

		// Only overwrite the version if the outdated version is greater
		// Otherwise when we first unmarshal a session that has been
		// replicated many times, we will reset the version to 0
		if (outdatedVersion > version)
			version = outdatedVersion;

		outdatedVersion = 0;
	}

	public void updateAccessTimeFromOutdatedTime() {
		//FIXME
//		if (outdatedTime > thisAccessedTime) {
//			lastAccessedTime = thisAccessedTime;
//			thisAccessedTime = outdatedTime;
//		}
		outdatedTime = 0;
	}

	/**
	 * Gets the session id with any appended jvmRoute info removed.
	 * 
	 * @see #getUseJK()
	 */
//	public String getRealId() {
//		return realId;
//	}

//	private void parseRealId(String sessionId) {
//		String newId = null;
//		if (useJK)
//			newId = Util.getRealId(sessionId);
//		else
//			newId = sessionId;
//
//		// realId is used in a lot of map lookups, so only replace it
//		// if the new id is actually different -- preserve object identity
//		if (!newId.equals(realId))
//			realId = newId;
//	}

	/**
	 * This is called specifically for failover case using mod_jk where the new
	 * session has this node name in there. As a result, it is safe to just
	 * replace the id since the backend store is using the "real" id without the
	 * node name.
	 * 
	 * @param id
	 */
	//FIXME we don't need this for SIP since the route is the call id and is handled by the LB,
	//is it worth encoding it in the SIP message
//	public void resetIdWithRouteInfo(String id) {
//		this.id = id;
//		parseRealId(id);
//	}

	public boolean getUseJK() {
		return useJK;
	}

	/**
	 * Check to see if the input version number is greater than I am. If it is,
	 * it means we will need to invalidate the in-memory cache.
	 * 
	 * @param version
	 * @return
	 */
	public boolean isNewData(int version) {
		return (this.version < version);
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * There are couple ways to generate this version number. But we will stick
	 * with the simple one of incrementing for now.
	 * 
	 * @return the new version
	 */
	public int incrementVersion() {
		return version++;
	}

	/**
	 * @deprecated Returns a meaningless value; use
	 *             {@link #setMaxUnreplicatedInterval(int)}
	 */
	public int getMaxUnreplicatedFactor() {
		return 80;
	}

	/**
	 * @deprecated Ignored; use {@link #setMaxUnreplicatedInterval(int)}
	 */
	public void setMaxUnreplicatedFactor(int factor) {
	}
	
	/**
	 * Gets the time {@link #updateLastReplicated()} was last called, or
	 * <code>0</code> if it has never been called.
	 */
	public long getLastReplicated() {
		return lastReplicated;
	}

	/**
	 * Sets the {@link #getLastReplicated() lastReplicated} field to the current
	 * time.
	 */
	public void updateLastReplicated() {
		lastReplicated = System.currentTimeMillis();
	}

	/**
	 * Get the maximum interval between requests, in
	 * <strong>milliseconds</strong>, after which a request will trigger
	 * replication of the session's metadata regardless of whether the request
	 * has otherwise made the session dirty.
	 * <p/>
	 * <strong>NOTE:</strong> This value is in milliseconds while the equivalent
	 * property in JBossCacheManager is in seconds.
	 * 
	 * @return the maximum interval since last replication after which a request
	 *         will trigger session metadata replication. A value of
	 *         <code>0</code> means replicate metadata on every request; a value
	 *         of <code>-1</code> means never replicate metadata unless the
	 *         session is otherwise dirty.
	 */
	public long getMaxUnreplicatedInterval() {
		return maxUnreplicatedInterval;
	}

	/**
	 * Sets the maximum interval between requests, in
	 * <strong>milliseconds</strong>, after which a request will trigger
	 * replication of the session's metadata regardless of whether the request
	 * has otherwise made the session dirty.
	 * <p/>
	 * <strong>NOTE:</strong> This value is in milliseconds while the equivalent
	 * property in JBossCacheManager is in seconds.
	 * 
	 * @param maxUnreplicatedInterval
	 *            the maximum interval since last replication after which a
	 *            request will trigger session metadata replication. A value of
	 *            <code>0</code> means replicate metadata on every request; a
	 *            value of <code>-1</code> means never replicate metadata unless
	 *            the session is otherwise dirty. A value less than
	 *            <code>-1</code> is treated as <code>-1</code>.
	 */
	public void setMaxUnreplicatedInterval(long interval) {
		this.maxUnreplicatedInterval = Math.max(interval, -1);
		checkAlwaysReplicateMetadata();
	}

	public boolean getExceedsMaxUnreplicatedInterval() {
		boolean exceeds = alwaysReplicateMetadata;

		if (!exceeds && maxUnreplicatedInterval >= 0) // -1 means ignore
		{
			long unrepl = System.currentTimeMillis() - lastReplicated;
			exceeds = (unrepl >= maxUnreplicatedInterval);
		}

		return exceeds;
	}

	private void checkAlwaysReplicateMetadata() {
		boolean was = this.alwaysReplicateMetadata;
		this.alwaysReplicateMetadata = maxUnreplicatedInterval == 0;

		if (this.alwaysReplicateMetadata && !was && logger.isTraceEnabled()) {
			logger
					.trace(key
							+ " will always replicate metadata; maxUnreplicatedInterval="
							+ maxUnreplicatedInterval);
		}
	}


	/**
	 * This is called after loading a session to initialize the transient
	 * values.
	 * 
	 * @param manager
	 */
	public abstract void initAfterLoad(JBossCacheSipManager manager);

	/**
	 * Propogate session to the internal store.
	 */
	public abstract void processSessionRepl();

	/**
	 * Remove myself from the internal store.
	 */
	public abstract void removeMyself();

	/**
	 * Remove myself from the <t>local</t> internal store.
	 */
	public abstract void removeMyselfLocal();

	// ----------------------------------------------- Overridden Public Methods

	public void access() {
		super.access();

		// JBAS-3528. If it's not the first access, make sure
		// the 'new' flag is correct
		//SipSession doesn't have the isNew flag
//		if (!firstAccess && isNew) {
//			setNew(false);
//		}

		sessionLastAccessTimeDirty();
		
//		if (invalidationPolicy == WebMetaData.SESSION_INVALIDATE_ACCESS) {
//			this.sessionMetadataDirty();
//		}
	}

//	public void endAccess() {
//		super.endAccess();
//
//		if (firstAccess) {
//			firstAccess = false;
//			// Tomcat marks the session as non new, but that's not really
//			// accurate per SRV.7.2, as the second request hasn't come in yet
//			// So, we fix that
//			isNew = true;
//		}
//	}

	public Object getAttribute(String name) {

		if (!isValid())
			throw new IllegalStateException(sm
					.getString("clusteredSession.getAttribute.ise"));

		return getAttributeInternal(name);
	}

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
		if (!isValid())
			throw new IllegalStateException(sm
					.getString("clusteredSession.setAttribute.ise"));
		if ((getSipApplicationSession().getSipContext().getSipManager() != null) && getSipApplicationSession().getSipContext().getSipManager().getDistributable()
				&& !(canAttributeBeReplicated(value)))
			throw new IllegalArgumentException(sm
					.getString("clusteredSession.setAttribute.iae"));

		// Construct an event with the new value
		SipSessionBindingEvent event = null;

		// Call the valueBound() method if necessary
		if (value instanceof SipSessionBindingListener) {
			event = new SipSessionBindingEvent(this, name);
			try {
				((SipSessionBindingListener) value).valueBound(event);
			} catch (Throwable t) {
				logger.error(
						sm.getString("standardSession.bindingEvent"), t);
			}
		}

		// Replace or add this attribute
		Object unbound = setInternalAttribute(name, value);

		// Call the valueUnbound() method if necessary
		if ((unbound != null) && (unbound != value)
				&& (unbound instanceof SipSessionBindingListener)) {
			try {
				((SipSessionBindingListener) unbound)
						.valueUnbound(new SipSessionBindingEvent(this,
								name));
			} catch (Throwable t) {
				logger.error(
						sm.getString("standardSession.bindingEvent"), t);
			}
		}

		// Notify interested application event listeners
		List<SipSessionAttributeListener> listeners = getSipApplicationSession().getSipContext().getListeners().getSipSessionAttributeListeners();
		if (listeners == null)
			return;
		for (SipSessionAttributeListener listener : listeners) {
			try {
				if (unbound != null) {
//					fireContainerEvent(context,
//							"beforeSessionAttributeReplaced", listener);
					if (event == null) {
						event = new SipSessionBindingEvent(this, name);
					}
					listener.attributeReplaced(event);
//					fireContainerEvent(context,
//							"afterSessionAttributeReplaced", listener);
				} else {
//					fireContainerEvent(context, "beforeSessionAttributeAdded",
//							listener);
					if (event == null) {
						event = new SipSessionBindingEvent(this, name);
					}
					listener.attributeAdded(event);
//					fireContainerEvent(context, "afterSessionAttributeAdded",
//							listener);
				}
			} catch (Throwable t) {
				try {
//					if (unbound != null) {
//						fireContainerEvent(context,
//								"afterSessionAttributeReplaced", listener);
//					} else {
//						fireContainerEvent(context,
//								"afterSessionAttributeAdded", listener);
//					}
				} catch (Exception e) {
					;
				}
				logger.error(
						sm.getString("standardSession.attributeEvent"), t);
			}
		}
	}

	public void removeAttribute(String name, boolean byPassValidCheck) {
		// Name cannot be null
		if (name == null)
			throw new IllegalArgumentException(sm
					.getString("clusteredSession.setAttribute.namenull"));

		// Validate our current state
		if (!byPassValidCheck && !isValid())
			throw new IllegalStateException(sm
					.getString("clusteredSession.setAttribute.ise"));
		
		// Replace or add this attribute
		removeAttributeInternal(name, true);		
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
		Class clazz = attribute.getClass().getComponentType();
		return (clazz != null && clazz.isPrimitive());
	}

	/**
	 * Invalidates this session and unbinds any objects bound to it. Overridden
	 * here to remove across the cluster instead of just expiring.
	 * 
	 * @exception IllegalStateException
	 *                if this method is called on an invalidated session
	 */
//	public void invalidate() {
//		if (!isValid())
//			throw new IllegalStateException(sm
//					.getString("clusteredSession.invalidate.ise"));

		// Cause this session to expire globally
//		boolean notify = true;
//		boolean localCall = true;
//		boolean localOnly = false;
//		expire(notify, localCall, localOnly);
//	}

	/**
	 * Overrides the {@link StandardSession#isValid() superclass method} to call @
	 * #isValid(boolean) isValid(true)} .
	 */
//	public boolean isValid() {
//		return isValid(true);
//	}

	/**
	 * Returns whether the current session is still valid, but only calls
	 * {@link #expire(boolean)} for timed-out sessions if
	 * <code>expireIfInvalid</code> is <code>true</code>.
	 * 
	 * @param expireIfInvalid
	 *            <code>true</code> if sessions that have been timed out should
	 *            be expired
	 */
//	public boolean isValid(boolean expireIfInvalid) {
//		if (this.expiring) {
//			return true;
//		}
//		return super.isValid();
//		if (!isValid) {
//			return false;
//		}
//
//		if (ACTIVITY_CHECK && accessCount.get() > 0) {
//			return true;
//		}
//
//		if (maxInactiveInterval >= 0) {
//			long timeNow = System.currentTimeMillis();
//			int timeIdle = (int) ((timeNow - thisAccessedTime) / 1000L);
//			if (timeIdle >= maxInactiveInterval) {
//				if (expireIfInvalid)
//					expire(true);
//				else
//					return false;
//			}
//		}
//
//		return isValid;
//
//	}

	/**
	 * Expires the session, but in such a way that other cluster nodes are
	 * unaware of the expiration.
	 * 
	 * @param notify
	 */
	public void expire(boolean notify) {
		boolean localCall = true;
		boolean localOnly = true;
		expire(notify, localCall, localOnly);
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
	 */
	public void expire(boolean notify, boolean localCall, boolean localOnly) {
		if (logger.isDebugEnabled()) {
			logger.debug("The session has expired with id: " + key
					+ " -- is it local? " + localOnly);
		}

		// If another thread is already doing this, stop
//		if (expiring)
//			return;

//		synchronized (this) {
			// If we had a race to this sync block, another thread may
			// have already completed expiration. If so, don't do it again
			if (!isValid())
				return;

			if (getSipApplicationSession().getSipContext().getSipManager() == null)
				return;

//			expiring = true;

			// Notify interested application event listeners
			// FIXME - Assumes we call listeners in reverse order
			List<SipSessionListener> listeners = getSipApplicationSession().getSipContext().getListeners().getSipSessionListeners();
			if (notify && (listeners != null)) {
				SipSessionEvent event = new SipSessionEvent(this);
				for (SipSessionListener listener : listeners) {
					try {
//						fireContainerEvent(context, "beforeSessionDestroyed",
//								listener);
						listener.sessionDestroyed(event);
//						fireContainerEvent(context, "afterSessionDestroyed",
//								listener);
					} catch (Throwable t) {
//						try {
//							fireContainerEvent(context,
//									"afterSessionDestroyed", listener);
//						} catch (Exception e) {
//							;
//						}
						getSipApplicationSession().getSipContext().getLogger()
								.error(sm.getString("standardSession.sessionEvent"),t);
					}
				}
			}
//			if (ACTIVITY_CHECK) {
//				accessCount.set(0);
//			}

			// Notify interested session event listeners.
//			if (notify) {
//				fireSessionEvent(Session.SESSION_DESTROYED_EVENT, null);
//			}

			// JBAS-1360 -- Unbind any objects associated with this session
			String keys[] = keys();
			for (int i = 0; i < keys.length; i++)
				removeAttributeInternal(keys[i], localCall, localOnly, notify);

			// Remove this session from our manager's active sessions
			removeFromManager(localCall, localOnly);

			// We have completed expire of this session
//			setValid(false);
//			expiring = false;
//		}

	}

	/**
	 * Advise our manager to remove this expired session.
	 * 
	 * @param localCall
	 *            whether this call originated from local activity or from a
	 *            remote invalidation. In this default implementation, this
	 *            parameter is ignored.
	 * @param localOnly
	 *            whether the rest of the cluster should be made aware of the
	 *            removal
	 */
	protected void removeFromManager(boolean localCall, boolean localOnly) {
		if (localOnly) {
			((JBossCacheSipManager) getSipApplicationSession().getSipContext().getSipManager()).removeLocal(this);
		} else {
			getSipApplicationSession().getSipContext().getSipManager().removeSipSession(key);
		}
	}

	public void passivate() {
		// Notify interested session event listeners
//		fireSessionEvent(Session.SESSION_PASSIVATED_EVENT, null);

		if (hasActivationListener != null && hasActivationListener) {
			boolean hasListener = false;

			// Notify ActivationListeners
			SipSessionEvent event = null;
			String keys[] = keys();
			Map attrs = getAttributesInternal();
			for (int i = 0; i < keys.length; i++) {
				Object attribute = attrs.get(keys[i]);
				if (attribute instanceof SipSessionActivationListener) {
					hasListener = true;

					if (event == null)
						event = new SipSessionEvent(this);
					try {
						((SipSessionActivationListener) attribute)
								.sessionWillPassivate(event);
					} catch (Throwable t) {
						getSipApplicationSession().getSipContext().getSipManager()
								.getContainer()
								.getLogger()
								.error(
										sm
												.getString("clusteredSession.attributeEvent"),
										t);
					}
				}
			}

			hasActivationListener = hasListener ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	public void activate() {
		// Notify interested session event listeners
//		fireSessionEvent(Session.SESSION_ACTIVATED_EVENT, null);

		if (hasActivationListener != null && hasActivationListener) {
			// Notify ActivationListeners

			boolean hasListener = false;

			SipSessionEvent event = null;
			String keys[] = keys();
			Map attrs = getAttributesInternal();
			for (int i = 0; i < keys.length; i++) {
				Object attribute = attrs.get(keys[i]);
				if (attribute instanceof SipSessionActivationListener) {
					hasListener = true;
					if (event == null)
						event = new SipSessionEvent(this);
					try {
						((SipSessionActivationListener) attribute)
								.sessionDidActivate(event);
					} catch (Throwable t) {
						getSipApplicationSession().getSipContext().getSipManager()
								.getContainer()
								.getLogger()
								.error(
										sm
												.getString("clusteredSession.attributeEvent"),
										t);
					}
				}
			}

			hasActivationListener = hasListener ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	// TODO uncomment when work on JBAS-1900 is completed
	// public void removeNote(String name)
	// {
	// // FormAuthenticator removes the username and password because
	// // it assumes they are not needed if the Principal is cached,
	// // but they are needed if the session fails over, so ignore
	// // the removal request.
	// // TODO discuss this on Tomcat dev list to see if a better
	// // way of handling this can be found
	// if (Constants.SESS_USERNAME_NOTE.equals(name)
	// || Constants.SESS_PASSWORD_NOTE.equals(name))
	// {
	// if (logger.isDebugEnabled())
	// {
	// logger.debug("removeNote(): ignoring removal of note " + name);
	// }
	// }
	// else
	// {
	// super.removeNote(name);
	// }
	//	      
	// }

	// TODO uncomment when work on JBAS-1900 is completed
	// public void setNote(String name, Object value)
	// {
	// super.setNote(name, value);
	//	      
	// if (Constants.SESS_USERNAME_NOTE.equals(name)
	// || Constants.SESS_PASSWORD_NOTE.equals(name))
	// {
	// sessionIsDirty();
	// }
	// }

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
//		// Fields that the superclass isn't clearing
//		listeners.clear();
//		support = new PropertyChangeSupport(this);
//
//		invalidationPolicy = 0;
//		outdatedTime = 0;
//		outdatedVersion = 0;
//		sessionAttributesDirty = false;
//		sessionMetadataDirty = false;
//		realId = null;
//		useJK = false;
//		version = 0;
//		hasActivationListener = null;
//		lastReplicated = 0;
//		maxUnreplicatedInterval = 0;
//      this.alwaysReplicateMetadata = true;
//	}

	/**
	 * Set the creation time for this session. This method is called by the
	 * Manager when an existing Session instance is reused.
	 * 
	 * @param time
	 *            The new creation time
	 */
//	public void setCreationTime(long time) {
//		super.setCreationTime(time);
//		sessionMetadataDirty();
//	}

	/**
	 * Overrides the superclass method to also set the {@link #getRealId()
	 * realId} property.
	 */
//	public void setId(String id) {
//		// Parse the real id first, as super.setId() calls add(),
//		// which depends on having the real id
//		parseRealId(id);
//		super.setId(id);
//	}

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

		Principal oldPrincipal = this.userPrincipal;
		this.userPrincipal = principal;
//		support.firePropertyChange("principal", oldPrincipal, this.principal);

		if ((oldPrincipal != null && !oldPrincipal.equals(principal))
				|| (oldPrincipal == null && principal != null))
			sessionMetadataDirty();

	}

//	public void setNew(boolean isNew) {
//		super.setNew(isNew);
//		// Don't replicate metadata just 'cause its the second request
//		// The only effect of this is if someone besides a request
//		// deserializes metadata from the distributed cache, this
//		// field may be out of date.
//		// If a request accesses the session, the access() call will
//		// set isNew=false, so the request will see the correct value
//		// sessionMetadataDirty();
//	}

//	public void setValid(boolean isValid) {
//		super.setValid(isValid);
//		sessionMetadataDirty();
//	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("key: " + key)
				.append(" lastAccessedTime: " + lastAccessedTime).append(
						" version: " + version).append(
						" lastOutdated: " + outdatedTime);

		return buf.toString();
	}

	// --------------------------------------------------------- Externalizable

	/**
	 * Reads all non-transient state from the ObjectOutput <i>except the
	 * attribute map</i>. Subclasses that wish the attribute map to be read
	 * should override this method and {@link #writeExternal(ObjectOutput)
	 * writeExternal()}.
	 * 
	 * <p>
	 * This method is deliberately public so it can be used to reset the
	 * internal state of a session object using serialized contents replicated
	 * from another JVM via JBossCache.
	 * </p>
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		if(logger.isDebugEnabled()) {
			logger.debug("reading sip session from the cache ");
		}
//		synchronized (this) {
			// From SipSessionImpl
			lastAccessedTime = in.readLong();
			// From ClusteredSession
			version = in.readInt();
			
			transport = in.readUTF();

			// Get our id without any jvmRoute appended
//			parseRealId(id);

			// We no longer know if we have an activationListener
			hasActivationListener = null;

			// If the session has been replicated, any subsequent
			// access cannot be the first.
			this.firstAccess = false;
			sessionMetadataDirty = false;
			sessionAttributesDirty = false;
			sessionLastAccessTimeDirty = false;			
			
			// Assume deserialization means replication and use thisAccessedTime
			// as a proxy for when replication occurred
			updateLastReplicated();

			checkAlwaysReplicateMetadata();

			if(logger.isDebugEnabled()) {
				logger.debug("sip session has just been read from the cache ");				
			}
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
//		}
	}

	/**
	 * Writes all non-transient state to the ObjectOutput <i>except the
	 * attribute map</i>. Subclasses that wish the attribute map to be written
	 * should override this method and append it.
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
//		synchronized (this) {
			// From SipSessionimpl
			out.writeLong(lastAccessedTime);			
			
			// From ClusteredSession			
			out.writeInt(version);
			
			out.writeUTF(transport);

			// TODO uncomment when work on JBAS-1900 is completed
			// // Session notes -- for FORM auth apps, allow replicated session
			// // to be used without requiring a new login
			// String username = (String) getNote(Constants.
			// SESS_USERNAME_NOTE);
			// logger.debug(Constants.SESS_USERNAME_NOTE + " = " + username);
			// out.writeObject(username);
			// String password = (String) getNote(Constants.SESS_PASSWORD_NOTE);
			// logger.debug(Constants.SESS_PASSWORD_NOTE + " = " + password);
			// out.writeObject(password);
//		}
	}

	// ----------------------------------------------------- Protected Methods

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
	protected static Map removeExcludedAttributes(Map attributes) {
		Map excluded = null;
		for (int i = 0; i < excludedAttributes.length; i++) {
			Object attr = attributes.remove(excludedAttributes[i]);
			if (attr != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("Excluding attribute " + excludedAttributes[i]
							+ " from replication");
				}
				if (excluded == null) {
					excluded = new HashMap();
				}
				excluded.put(excludedAttributes[i], attr);
			}
		}

		return excluded;
	}

	// -------------------------------------- Internal protected method override

	/**
	 * Method inherited from Tomcat. Return zero-length based string if not
	 * found.
	 */
	protected String[] keys() {
		return ((String[]) getAttributesInternal().keySet()
				.toArray(new String[0]));
	}

	/**
	 * Called by super.removeAttribute().
	 * 
	 * @param name
	 *            the attribute name
	 * @param notify
	 *            <code>true</code> if listeners should be notified
	 */
	protected void removeAttributeInternal(String name, boolean notify) {
		boolean localCall = true;
		boolean localOnly = false;
		removeAttributeInternal(name, localCall, localOnly, notify);
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
	 */
	protected void removeAttributeInternal(String name, boolean localCall,
			boolean localOnly, boolean notify) {

		// Remove this attribute from our collection
		Object value = removeJBossInternalAttribute(name, localCall, localOnly);

		// Do we need to do valueUnbound() and attributeRemoved() notification?
		if (!notify || (value == null)) {
			return;
		}

		// Call the valueUnbound() method if necessary
		SipSessionBindingEvent event = null;
		if (value instanceof SipSessionBindingListener) {
			event = new SipSessionBindingEvent(this, name);
			((SipSessionBindingListener) value).valueUnbound(event);
		}

		// Notify interested application event listeners
		List<SipSessionAttributeListener> listeners = getSipApplicationSession().getSipContext().getListeners().getSipSessionAttributeListeners();
		if (listeners == null)
			return;
		for (SipSessionAttributeListener listener : listeners) {
			try {
//				fireContainerEvent(context, "beforeSessionAttributeRemoved",
//						listener);
				if (event == null) {
					event = new SipSessionBindingEvent(this, name);
				}
				listener.attributeRemoved(event);
//				fireContainerEvent(context, "afterSessionAttributeRemoved",
//						listener);
			} catch (Throwable t) {
//				try {
//					fireContainerEvent(context, "afterSessionAttributeRemoved",
//							listener);
//				} catch (Exception e) {
//					;
//				}
				logger.error(
						sm.getString("standardSession.attributeEvent"), t);
			}
		}

	}

	protected Object getAttributeInternal(String name) {
		return getJBossInternalAttribute(name);
	}

	protected Map getAttributesInternal() {
		return getJBossInternalAttributes();
	}

	protected Object setInternalAttribute(String name, Object value) {
		if (value instanceof SipSessionActivationListener)
			hasActivationListener = Boolean.TRUE;

		return setJBossInternalAttribute(name, value);
	}

	// ------------------------------------------ JBoss internal abstract method

	protected abstract Object getJBossInternalAttribute(String name);
	
	protected abstract Object removeJBossInternalAttribute(String name, boolean localCall, boolean localOnly);

	protected abstract Map getJBossInternalAttributes();

	protected abstract Object setJBossInternalAttribute(String name,
			Object value);

	// ------------------------------------------------ Session Package Methods

	protected void sessionAttributesDirty() {
		sessionAttributesDirty = true;
		ConvergedSessionReplicationContext.bindSipSession(this, manager.getSnapshotManager());
	}

	protected boolean getSessionAttributesDirty() {
		return sessionAttributesDirty;
	}

	protected void sessionMetadataDirty() {
		sessionMetadataDirty = true;
		ConvergedSessionReplicationContext.bindSipSession(this, manager.getSnapshotManager());
	}		

	protected boolean getSessionMetadataDirty() {
		return sessionMetadataDirty;
	}

	protected void sessionLastAccessTimeDirty() {
		sessionLastAccessTimeDirty = true;
		ConvergedSessionReplicationContext.bindSipSession(this, manager.getSnapshotManager());
	}
	
	protected boolean getSessionLastAccessTimeDirty() {
		return sessionLastAccessTimeDirty;
	}
	
	/**
	 * Calls {@link #sessionAttributesDirty()} and
	 * {@link #sessionMetadataDirty()}.
	 * 
	 * @deprecated use one of the more fine-grained methods.
	 */
	protected void sessionDirty() {
		sessionAttributesDirty();
		sessionMetadataDirty();
	}

	public boolean isSessionDirty() {
		return sessionAttributesDirty || sessionMetadataDirty || sessionLastAccessTimeDirty;
	}

	public boolean getReplicateSessionBody() {
		return sessionMetadataDirty || getExceedsMaxUnreplicatedInterval() || 
			(maxUnreplicatedInterval == -1 && sessionAttributesDirty);
	}

	protected boolean isGetDirty(Object attribute) {
		boolean result = false;
		switch (invalidationPolicy) {
		case (WebMetaData.SESSION_INVALIDATE_SET_AND_GET):
			result = true;
			break;
		case (WebMetaData.SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET):
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
	
	@Override
	public void setState(State state) {
		super.setState(state);
		sessionMetadataDirty();
		metaModifiedMap_.put(STATE, state);
	}
	
	@Override
	public void setCseq(long cseq) {
		super.setCseq(cseq);
		sessionMetadataDirty();
		metaModifiedMap_.put(CSEQ, cseq);
	}
	
	@Override
	public void setHandler(String name) throws ServletException {
		super.setHandler(name);
		sessionMetadataDirty();
		metaModifiedMap_.put(HANDLER, name);
	}
	
	@Override
	public void setInvalidateWhenReady(boolean arg0) {
		super.setInvalidateWhenReady(arg0);
		sessionMetadataDirty();		
		metaModifiedMap_.put(INVALIDATE_WHEN_READY, arg0);
	}

	@Override
	public void setReadyToInvalidate(boolean readyToInvalidate) {
		super.setReadyToInvalidate(readyToInvalidate);
		sessionMetadataDirty();
		metaModifiedMap_.put(READY_TO_INVALIDATE, readyToInvalidate);
	}
	
	@Override
	public void setValid(boolean isValid) {
		super.setValid(isValid);
		sessionMetadataDirty();
		metaModifiedMap_.put(IS_VALID, isValid);
	}
	
	@Override
	public void setSessionCreatingDialog(Dialog dialog) {
		super.setSessionCreatingDialog(dialog);
		if(dialog != null && dialog.getDialogId() != null) {
			sessionMetadataDirty();
			metaModifiedMap_.put(DIALOG_ID, dialog.getDialogId() );
		}
	}
	
	public String getHaId() {
		return haId;
	}
	
	public void setReadyToInvalidateInternal(boolean readyToInvalidate) {
		super.setReadyToInvalidate(readyToInvalidate);
	}

	public void setValidInternal(boolean isValid) {
		super.setValid(isValid);
	}
}
