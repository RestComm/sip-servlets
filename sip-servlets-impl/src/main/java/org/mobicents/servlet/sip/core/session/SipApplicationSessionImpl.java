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
package org.mobicents.servlet.sip.core.session;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSessionActivationListener;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.catalina.security.SecurityUtil;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.utils.JvmRouteUtil;

/**
 * <p>Implementation of the SipApplicationSession interface.
 * An instance of this sip application session can only be retrieved through the Session Manager 
 * (extended class from Tomcat's manager classes implementing the <code>Manager</code> interface)
 * to constrain the creation of sip application session and to make sure that all sessions created
 * can be retrieved only through the session manager<p/> 
 * 
 * <p>
 * As a SipApplicationSession represents a call (that can contain multiple call legs, in the B2BUA case by example),
 * the call id and the app name are used as a unique key for a given SipApplicationSession instance. 
 * </p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 */
public class SipApplicationSessionImpl implements MobicentsSipApplicationSession {

	private static transient Logger logger = Logger.getLogger(SipApplicationSessionImpl.class);

	/**
	 * Timer task that will notify the listeners that the sip application session has expired 
	 * It is an improved timer task that is delayed every time setLastAccessedTime is called on it.
	 * It is delayed of lastAccessedTime + lifetime
	 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
	 */
	public class SipApplicationSessionTimerTask implements Runnable {
		
		public void run() {	
			if(logger.isDebugEnabled()) {
				logger.debug("initial kick off of SipApplicationSessionTimerTask running for sip application session " + getId());
			}
						
			long now = System.currentTimeMillis();
			if(expirationTime > now) {
				// if the session has been accessed since we started it, put it to sleep
				long sleep =  getDelay();
				if(logger.isDebugEnabled()) {
					logger.debug("expirationTime is " + expirationTime + 
							", now is " + now + 
							" sleeping for " + sleep / 1000 + " seconds");
				}
				expirationTimerTask = new SipApplicationSessionTimerTask();					
				expirationTimerFuture = (ScheduledFuture<MobicentsSipApplicationSession>) sipContext.getThreadPoolExecutor().schedule(expirationTimerTask, sleep, TimeUnit.MILLISECONDS);
			} else {
				tryToExpire();
			}
		}

		private void tryToExpire() {
			notifySipApplicationSessionListeners(SipApplicationSessionEventType.EXPIRATION);
			//It is possible that the application grant an extension to the lifetime of the session, thus the sip application
			//should not be treated as expired.
			if(getDelay() <= 0) {
				expired = true;
				if(isValid) {
					sipContext.enterSipApp(null, null, null, true, false);
					try {
						invalidate();
					} finally {
						sipContext.exitSipApp(null, null);
					}
				}
			}
		}				
		
		public long getDelay() {
			return expirationTime - System.currentTimeMillis();
		}							
	} 
	
	protected Map<String, Object> sipApplicationSessionAttributeMap;

	protected transient ConcurrentHashMap<String,SipSessionKey> sipSessions;
	
	protected transient Set<String> httpSessions;
	
	protected SipApplicationSessionKey key;	
	
	protected long lastAccessedTime;
	
	protected long creationTime;
	
	protected long expirationTime;
	
	protected boolean expired;
	
	protected transient SipApplicationSessionTimerTask expirationTimerTask;
	
	protected transient ScheduledFuture<MobicentsSipApplicationSession> expirationTimerFuture;
	
	protected transient ConcurrentHashMap<String, ServletTimer> servletTimers;
	
	protected boolean isValid;
	
	protected boolean invalidateWhenReady = true;
	
	protected boolean readyToInvalidate = false;
	
//	protected transient ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1,
//			90, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	/**
	 * The first sip application for subsequent requests.
	 */
	protected transient SipContext sipContext;
	
	protected String currentRequestHandler;
	
	protected transient Semaphore semaphore;
		
	protected transient MobicentsSipApplicationSessionFacade facade = null;
	
	protected long sipApplicationSessionTimeout = -1;
	
	// Does it need to be synchronized?
	protected Map<String,Object> getAttributeMap() {
		if(sipApplicationSessionAttributeMap == null) {
			sipApplicationSessionAttributeMap = new ConcurrentHashMap<String,Object>() ;
		}
		return sipApplicationSessionAttributeMap;
	}
	
	protected SipApplicationSessionImpl(SipApplicationSessionKey key, SipContext sipContext) {
		sipSessions = new ConcurrentHashMap<String,SipSessionKey>();
		if(sipContext != null && !ConcurrencyControlMode.None.equals(sipContext.getConcurrencyControlMode())) {
			semaphore = new Semaphore(1);
		}
		this.key = key;
		this.sipContext = sipContext;
		this.currentRequestHandler = sipContext.getMainServlet();
		lastAccessedTime = creationTime = System.currentTimeMillis();
		expired = false;		
		isValid = true;		
		// the sip context can be null if the AR returned an application that was not deployed
		if(sipContext != null) {
			//scheduling the timer for session expiration
			if(sipContext.getSipApplicationSessionTimeout() > 0) {				
				sipApplicationSessionTimeout = sipContext.getSipApplicationSessionTimeout() * 60 * 1000;				
				expirationTimerTask = new SipApplicationSessionTimerTask();
				if(logger.isDebugEnabled()) {
					logger.debug("Scheduling sip application session "+ key +" to expire in " + (sipApplicationSessionTimeout / 1000 / 60) + " minutes");
				}
				expirationTimerFuture = (ScheduledFuture<MobicentsSipApplicationSession>) sipContext.getThreadPoolExecutor().schedule(expirationTimerTask, sipApplicationSessionTimeout, TimeUnit.MILLISECONDS);
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("The sip application session "+ key +" will never expire ");
				}
				// If the session timeout value is 0 or less, then an application session timer 
				// never starts for the SipApplicationSession object and the container does 
				// not consider the object to ever have expired
//				expirationTime = -1;
			}
			notifySipApplicationSessionListeners(SipApplicationSessionEventType.CREATION);
		}
	}
	
	/**
	 * Notifies the listeners that a lifecycle event occured on that sip application session 
	 * @param sipApplicationSessionEventType the type of event that happened
	 */
	public void notifySipApplicationSessionListeners(SipApplicationSessionEventType sipApplicationSessionEventType) {						
		List<SipApplicationSessionListener> listeners = 
			sipContext.getListeners().getSipApplicationSessionListeners();
		if(listeners.size() > 0) {
			ClassLoader oldLoader = java.lang.Thread.currentThread().getContextClassLoader();
			java.lang.Thread.currentThread().setContextClassLoader(sipContext.getLoader().getClassLoader());
			SipApplicationSessionEvent event = new SipApplicationSessionEvent(this.getSession());
			if(logger.isDebugEnabled()) {
				logger.debug("notifying sip application session listeners of context " + 
						key.getApplicationName() + " of following event " + sipApplicationSessionEventType);
			}
			for (SipApplicationSessionListener sipApplicationSessionListener : listeners) {
				try {
					if(SipApplicationSessionEventType.CREATION.equals(sipApplicationSessionEventType)) {
						sipApplicationSessionListener.sessionCreated(event);
					} else if (SipApplicationSessionEventType.DELETION.equals(sipApplicationSessionEventType)) {
						sipApplicationSessionListener.sessionDestroyed(event);
					} else if (SipApplicationSessionEventType.EXPIRATION.equals(sipApplicationSessionEventType)) {
						sipApplicationSessionListener.sessionExpired(event);
					} else if (SipApplicationSessionEventType.READYTOINVALIDATE.equals(sipApplicationSessionEventType)) {
						sipApplicationSessionListener.sessionReadyToInvalidate(event);
					}
					
				} catch (Throwable t) {
					logger.error("SipApplicationSessionListener threw exception", t);
				}
			}
			java.lang.Thread.currentThread().setContextClassLoader(oldLoader);
		}		
	}
	
	public void addSipSession(MobicentsSipSession mobicentsSipSession) {
		this.sipSessions.putIfAbsent(mobicentsSipSession.getKey().toString(), mobicentsSipSession.getKey());
		readyToInvalidate = false;
//		sipSessionImpl.setSipApplicationSession(this);
	}
	
	public SipSessionKey removeSipSession (MobicentsSipSession mobicentsSipSession) {
		if(sipSessions != null) {
			return this.sipSessions.remove(mobicentsSipSession.getKey().toString());
		} 
		return null;
	}
	
	public void addHttpSession(HttpSession httpSession) {
		if(httpSessions == null) {
			httpSessions = new CopyOnWriteArraySet<String>();
		}
		this.httpSessions.add(JvmRouteUtil.removeJvmRoute(httpSession.getId()));
		readyToInvalidate = false;
		// TODO: We assume that there is only one HTTP session in the app session. In this case
		// we are safe to only assign jvmRoute once here. When we support multiple http sessions
		// we will need something more sophisticated.
		setJvmRoute(JvmRouteUtil.extractJvmRoute(httpSession.getId()));
	}
	
	public boolean removeHttpSession(HttpSession httpSession) {
		if(httpSessions != null) {
			return this.httpSessions.remove(JvmRouteUtil.removeJvmRoute(httpSession.getId()));
		}
		return false;
	}
	
	public HttpSession findHttpSession (String sessionId) {
		String id = JvmRouteUtil.removeJvmRoute(sessionId);
		if(httpSessions != null) {
			if(httpSessions.contains(id)) {
				try {
					return (HttpSession)sipContext.getSipManager().findSession(id);
				} catch (IOException e) {
					logger.error("An Unexpected exception happened while retrieving the http session " + id, e);				
				}
			}
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void encodeURI(URI uri) {
		uri.setParameter(SIP_APPLICATION_KEY_PARAM_NAME, RFC2396UrlDecoder.encode(getId()));
	}

	/**
	 * {@inheritDoc}
	 * Adds a get parameter to the URL like this:
	 * http://hostname/link -> http://hostname/link?org.mobicents.servlet.sip.ApplicationSessionKey=0
	 * http://hostname/link?something=1 -> http://hostname/link?something=1&org.mobicents.servlet.sip.ApplicationSessionKey=0
	 */
	public URL encodeURL(URL url) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		String urlStr = url.toExternalForm();
		try {
			URL ret;
			if (urlStr.contains("?")) {
				ret = new URL(url + "&" + SIP_APPLICATION_KEY_PARAM_NAME + "="
						+ getId().toString());
			} else {
				ret = new URL(url + "?" + SIP_APPLICATION_KEY_PARAM_NAME + "="
						+ getId().toString());
			}
			return ret;
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed encoding URL : " + url, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getAttribute(String name) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		return this.getAttributeMap().get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getAttributeNames()
	 */
	public Iterator<String> getAttributeNames() {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		return this.getAttributeMap().keySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getCreationTime()
	 */
	public long getCreationTime() {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		return creationTime;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getExpirationTime()
	 */
	public long getExpirationTime() {
		if(!isValid) {
			throw new IllegalStateException("this sip application session is not valid anymore");
		}
		if(expirationTimerTask == null) {
			return 0;
		}
		long expirationTime = expirationTimerTask.getDelay();
		if(expirationTime <= 0) {
			return 0;
		}
		if(expired) {
			return Long.MIN_VALUE;
		}
		return expirationTime;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return key.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		return lastAccessedTime;
	}

	
	private void setLastAccessedTime(long lastAccessTime) {
		this.lastAccessedTime = lastAccessTime;
		//JSR 289 Section 6.3 : starting the sip app session expiry timer anew 
		if(sipApplicationSessionTimeout > 0) {
			expirationTime = lastAccessedTime + sipApplicationSessionTimeout;			
		}
	}
	
	/**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
	//TODO : Section 6.3 : Whenever the last accessed time for a SipApplicationSession is updated, it is considered refreshed i.e.,
	//the expiry timer for that SipApplicationSession starts anew.
	// this method should be called as soon as there is any modifications to the Sip Application Session
	public void access() {
		setLastAccessedTime(System.currentTimeMillis());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getSessions()
	 */
	public Iterator<?> getSessions() {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		return sipSessions.values().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getSessions(java.lang.String)
	 */
	public Iterator<?> getSessions(String protocol) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		if(protocol == null) {
			throw new NullPointerException("protocol given in argument is null");
		}
		if("SIP".equalsIgnoreCase(protocol)) {
			return getSipSessions().iterator();
		} else if("HTTP".equalsIgnoreCase(protocol)) {			
			return getHttpSessions().iterator();
		} else {
			throw new IllegalArgumentException(protocol + " sessions are not handled by this container");
		}
	}
	
	protected Set<MobicentsSipSession> getSipSessions() {
		Set<MobicentsSipSession> retSipSessions = new HashSet<MobicentsSipSession>();
		if(sipSessions != null) {
			for(SipSessionKey sipSessionKey : sipSessions.values()) {
				MobicentsSipSession sipSession = sipContext.getSipManager().getSipSession(sipSessionKey, false, null, this);
				if(sipSession != null) {
					retSipSessions.add(sipSession);
				}
			}
		}
		return retSipSessions;
	}
	
	protected Set<HttpSession> getHttpSessions() {
		Set<HttpSession> retHttpSessions = new HashSet<HttpSession>();
		if(httpSessions != null) {
			for(String id : httpSessions) {
				try {
					HttpSession httpSession = (HttpSession)sipContext.getSipManager().findSession(id);
					if(httpSession != null) {
						retHttpSessions.add(httpSession);
					}
				} catch (IOException e) {
					logger.error("An Unexpected exception happened while retrieving the http session " + id, e);			
				}			
			}
		}
		return retHttpSessions;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getSipSession(java.lang.String)
	 */
	public SipSession getSipSession(String id) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Trying to find a session with the id " + id);
			dumpSipSessions();
		}
		SipSessionKey sipSessionKey = sipSessions.get(id);
		if(sipSessionKey != null) {
			return sipContext.getSipManager().getSipSession(sipSessionKey, false, null, this);
		} else {
			return null;
		}
	}

	private void dumpSipSessions() {
		if(logger.isDebugEnabled()) {
			logger.debug("sessions contained in the following app session " + key);
			for (String sessionKey : sipSessions.keySet()) {
				logger.debug("session key " + sessionKey);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getTimers()
	 */
	public Collection<ServletTimer> getTimers() {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		if(servletTimers != null) {
			return servletTimers.values();
		}
		return new HashMap<String, ServletTimer>().values();
	}

	/**
	 * Add a servlet timer to this application session
	 * @param servletTimer the servlet timer to add
	 */
	public void addServletTimer(ServletTimer servletTimer){
		if(servletTimers == null) {
			servletTimers = new ConcurrentHashMap<String, ServletTimer>();
		}
		servletTimers.putIfAbsent(servletTimer.getId(), servletTimer);
	}
	/**
	 * Remove a servlet timer from this application session
	 * @param servletTimer the servlet timer to remove
	 */
	public void removeServletTimer(ServletTimer servletTimer){
		if(servletTimers != null) {
			servletTimers.remove(servletTimer.getId());
		}
		updateReadyToInvalidateState();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#invalidate()
	 */
	public void invalidate() {
		//JSR 289 Section 6.1.2.2.1
		//When the IllegalStateException is thrown, the application is guaranteed 
		//that the state of the SipApplicationSession object will be unchanged from its state prior to the invalidate() 
		//method call. Even session objects that were eligible for invalidation will not have been invalidated.
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		if(logger.isInfoEnabled()) {
			logger.info("Invalidating the following sip application session " + key);
		}
		
		//doing the invalidation
		for(MobicentsSipSession session: getSipSessions()) {
			if(session.isValid()) {
				session.invalidate();
			}
		}
		for(HttpSession session: getHttpSessions()) {
			if(session instanceof ConvergedSession) {
				ConvergedSession convergedSession = (ConvergedSession) session;
				if(convergedSession.isValid()) {
					convergedSession.invalidate();
				}
			} else {
				try {
					session.invalidate();
				} catch(IllegalStateException ignore) {
					//we ignore this exception, ugly but the only way to test for validity of the session since we cannot cast to catalina Session
					//See Issue 523 http://code.google.com/p/mobicents/issues/detail?id=523
				}
			}
		}
		if(this.sipApplicationSessionAttributeMap != null) {
			for (String key : getAttributeMap().keySet()) {
				removeAttribute(key);
			}
		}
		notifySipApplicationSessionListeners(SipApplicationSessionEventType.DELETION);
		
		isValid = false;	
		//cancelling the timers
		if(servletTimers != null) {
			for (Map.Entry<String, ServletTimer> servletTimerEntry : servletTimers.entrySet()) {
				ServletTimer timerEntry = servletTimerEntry.getValue();
				if(timerEntry != null) {
					timerEntry.cancel();				
				}
			}		
		}
		if(!expired && expirationTimerTask != null) {
			cancelExpirationTimer();
		}
		
		/*
         * Compute how long this session has been alive, and update
         * session manager's related properties accordingly
         */
        long timeNow = System.currentTimeMillis();
        int timeAlive = (int) ((timeNow - creationTime)/1000);
        SipManager manager = sipContext.getSipManager();
        synchronized (manager) {
            if (timeAlive > manager.getSipApplicationSessionMaxAliveTime()) {
                manager.setSipApplicationSessionMaxAliveTime(timeAlive);
            }
            int numExpired = manager.getExpiredSipApplicationSessions();
            numExpired++;
            manager.setExpiredSipApplicationSessions(numExpired);
            int average = manager.getSipApplicationSessionAverageAliveTime();
            average = ((average * (numExpired-1)) + timeAlive)/numExpired;
            manager.setSipApplicationSessionAverageAliveTime(average);
        }
		
		sipContext.getSipManager().removeSipApplicationSession(key);
		sipContext.getSipSessionsUtil().removeCorrespondingSipApplicationSession(key);
		expirationTimerTask = null;
		expirationTimerFuture = null;
		if(httpSessions != null) {
			httpSessions.clear();
		}
//		key = null;
		if(servletTimers != null) {
			servletTimers.clear();
		}
		if(sipApplicationSessionAttributeMap != null) {
			sipApplicationSessionAttributeMap.clear();
		}
		if(sipSessions != null) {
			sipSessions.clear();
		}		
//		executorService.shutdown();
//		executorService = null;		
		httpSessions = null;
		sipSessions = null;
		sipApplicationSessionAttributeMap = null;
		servletTimers = null;			
		if(logger.isInfoEnabled()) {
			logger.info("The following sip application session " + key + " has been invalidated");
		}
		currentRequestHandler = null;
		if(semaphore != null) {
			semaphore.release();
			semaphore = null;
		}
		facade = null;
	}

    // Counts the number of cancelled tasks
    private static int numCancelled = 0;

	private void cancelExpirationTimer() {
		//CANCEL needs to remove the shceduled timer see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6602600
		//to improve perf
//		expirationTimerTask.cancel();
		boolean removed = sipContext.getThreadPoolExecutor().remove(expirationTimerTask);
		if(logger.isDebugEnabled()) {
			logger.debug("expiration timer on sip application session " + key + " removed : " + removed);
		}		
		boolean cancelled = expirationTimerFuture.cancel(true);
		if(logger.isDebugEnabled()) {
			logger.debug("expiration timer on sip application session " + key + " Cancelled : " + cancelled);
		}

        // Purge is expensive when called frequently, only call it every now and then.
        // We do not sync the numCancelled variable. We dont care about correctness of
        // the number, and we will still call purge rought once on every 25 cancels.
        numCancelled++;
        if(numCancelled % 25 == 0) {
            sipContext.getThreadPoolExecutor().purge();
        }
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#isValid()
	 */
	public boolean isValid() {
		return isValid;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {

		if (!isValid)
			throw new IllegalStateException(
					"Can not bind object to session that has been invalidated!!");

		if (name == null)
			// throw new NullPointerException("Name of attribute to bind cant be
			// null!!!");
			return;

		SipApplicationSessionBindingEvent event = null;
		
        Object value = this.getAttributeMap().remove(name);

        // Call the valueUnbound() method if necessary
        if (value != null && value instanceof SipApplicationSessionBindingListener) {
        	event = new SipApplicationSessionBindingEvent(this, name);
            ((SipApplicationSessionBindingListener) value).valueUnbound(event);
        }
        
		SipListenersHolder listeners = sipContext.getListeners();
		List<SipApplicationSessionAttributeListener> listenersList = listeners.getSipApplicationSessionAttributeListeners();
		if(listenersList.size() > 0) {		
			if(event == null) {
				event = new SipApplicationSessionBindingEvent(this, name);
			}
			for (SipApplicationSessionAttributeListener listener : listenersList) {
				if(logger.isDebugEnabled()) {
					logger.debug("notifying SipApplicationSessionAttributeListener " + listener.getClass().getCanonicalName() + " of attribute removed on key "+ key);
				}
				try {
					listener.attributeRemoved(event);
				} catch (Throwable t) {
					logger.error("SipApplicationSessionAttributeListener threw exception", t);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String key, Object attribute) {

		if (!isValid)
			throw new IllegalStateException(
					"Can not bind object to session that has been invalidated!!");

		if (key == null)
			throw new NullPointerException(
					"Name of attribute to bind cant be null!!!");
		if (attribute == null)
			throw new NullPointerException(
					"Attribute that is to be bound cant be null!!!");

		// Construct an event with the new value
		SipApplicationSessionBindingEvent event = null;

        // Call the valueBound() method if necessary
        if (attribute instanceof SipApplicationSessionBindingListener) {
            // Don't call any notification if replacing with the same value
            Object oldValue = getAttributeMap().get(key);
            if (attribute != oldValue) {
            	event = new SipApplicationSessionBindingEvent(this, key);
                try {
                    ((SipApplicationSessionBindingListener) attribute).valueBound(event);
                } catch (Throwable t){
                	logger.error("SipSessionBindingListener threw exception", t); 
                }
            }
        }
		
		Object previousValue = this.getAttributeMap().put(key, attribute);
		
		if (previousValue != null && previousValue != attribute &&
	            previousValue instanceof SipApplicationSessionBindingListener) {
            try {
                ((SipApplicationSessionBindingListener) previousValue).valueUnbound
                    (new SipApplicationSessionBindingEvent(this, key));
            } catch (Throwable t) {
            	logger.error("SipSessionBindingListener threw exception", t);
            }
        }

		SipListenersHolder listeners = sipContext.getListeners();
		List<SipApplicationSessionAttributeListener> listenersList = listeners.getSipApplicationSessionAttributeListeners();
		if(listenersList.size() > 0) {
			if(event == null) {
				event = new SipApplicationSessionBindingEvent(this, key);
			}
			if (previousValue == null) {				
				for (SipApplicationSessionAttributeListener listener : listenersList) {
					if(logger.isDebugEnabled()) {
						logger.debug("notifying SipApplicationSessionAttributeListener " + listener.getClass().getCanonicalName() + " of attribute added on key "+ key);
					}
					try {
						listener.attributeAdded(event);
					} catch (Throwable t) {
						logger.error("SipApplicationSessionAttributeListener threw exception", t);
					}
				}
			} else {				
				for (SipApplicationSessionAttributeListener listener : listenersList) {
					if(logger.isDebugEnabled()) {
						logger.debug("notifying SipApplicationSessionAttributeListener " + listener.getClass().getCanonicalName() + " of attribute replaced on key "+ key);
					}
					try {
						listener.attributeReplaced(event);
					} catch (Throwable t) {
						logger.error("SipApplicationSessionAttributeListener threw exception", t);
					}
				}
			}	
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#setExpires(int)
	 */
	public int setExpires(int deltaMinutes) {
		if(!isValid) {
			throw new IllegalStateException("Impossible to change the sip application " +
					"session timeout when it has been invalidated !");
		}
		expired = false;
		if(logger.isDebugEnabled()) {
			logger.debug("Postponing the expiratin of the sip application session " 
					+ key +" to expire in " + deltaMinutes + " minutes.");
		}
		if(deltaMinutes <= 0) {
			if(logger.isDebugEnabled()) {
				logger.debug("The sip application session "+ key +" won't expire anymore ");
			}
			// If the session timeout value is 0 or less, then an application session timer 
			// never starts for the SipApplicationSession object and the container 
			// does not consider the object to ever have expired
//			this.expirationTime = -1;
			if(expirationTimerTask != null) {
				cancelExpirationTimer();
				expirationTimerTask = null;				
			}		
			return Integer.MAX_VALUE;
		} else {
			long deltaMilliseconds = 0;
//			if(expirationTimerTask != null) {
				//extending the app session life time
//				expirationTime = expirationTimerFuture.getDelay(TimeUnit.MILLISECONDS) + deltaMinutes * 1000 * 60;
				deltaMilliseconds = deltaMinutes * 1000L * 60;
//			} else {
				//the app session was scheduled to never expire and now an expiration time is set
//				deltaMilliseconds = deltaMinutes * 1000L * 60;
//			}
			if(expirationTimerTask != null) {				
				expirationTime = System.currentTimeMillis() + deltaMilliseconds;
				if(logger.isDebugEnabled()) {
					logger.debug("Re-Scheduling sip application session "+ key +" to expire in " + deltaMinutes + " minutes");
					logger.debug("Re-Scheduling sip application session "+ key +" will expires in " + deltaMilliseconds + " milliseconds");
					logger.debug("sip application session "+ key +" will expires at " + expirationTime);
				}
				cancelExpirationTimer();
				expirationTimerTask = null;
				expirationTimerFuture = null;
			}
			expirationTimerTask = new SipApplicationSessionTimerTask();
			expirationTimerFuture = (ScheduledFuture<MobicentsSipApplicationSession>) sipContext.getThreadPoolExecutor().schedule(expirationTimerTask, deltaMilliseconds, TimeUnit.MILLISECONDS);

			return deltaMinutes;
		}				
	}

	public boolean hasTimerListener() {
		return this.sipContext.getListeners().getTimerListener() != null;
	}	

	public SipContext getSipContext() {
		return sipContext;
	}
	
	void expirationTimerFired() {
		notifySipApplicationSessionListeners(SipApplicationSessionEventType.EXPIRATION);
	}

	/**
	 * @return the key
	 */
	public SipApplicationSessionKey getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(SipApplicationSessionKey key) {
		this.key = key;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getApplicationName()
	 */
	public String getApplicationName() {		
		return key.getApplicationName();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getTimer(java.lang.String)
	 */
	public ServletTimer getTimer(String id) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		if(servletTimers != null) {
			return servletTimers.get(id);
		} 
		return null;
	}
	
	/**
     * Perform the internal processing required to passivate
     * this session.
     */
    public void passivate() {
        // Notify ActivationListeners
    	SipApplicationSessionEvent event = null;
    	if(this.sipApplicationSessionAttributeMap != null) {
	        Set<String> keySet = getAttributeMap().keySet();
	        for (String key : keySet) {
	        	Object attribute = getAttributeMap().get(key);
	            if (attribute instanceof SipApplicationSessionActivationListener) {
	                if (event == null)
	                    event = new SipApplicationSessionEvent(this);
	                try {
	                    ((SipApplicationSessionActivationListener)attribute)
	                        .sessionWillPassivate(event);
	                } catch (Throwable t) {
	                    logger.error("SipApplicationSessionActivationListener threw exception", t);
	                }
	            }
			}
	    }
    }
    
    /**
     * Perform internal processing required to activate this
     * session.
     */
    public void activate() {        
        // Notify ActivationListeners
    	SipApplicationSessionEvent event = null;
    	if(sipApplicationSessionAttributeMap != null) {
	        Set<String> keySet = sipApplicationSessionAttributeMap.keySet();
	        for (String key : keySet) {
	        	Object attribute = sipApplicationSessionAttributeMap.get(key);
	            if (attribute instanceof SipApplicationSessionActivationListener) {
	                if (event == null)
	                    event = new SipApplicationSessionEvent(this);
	                try {
	                    ((SipApplicationSessionActivationListener)attribute)
	                        .sessionDidActivate(event);
	                } catch (Throwable t) {
	                    logger.error("SipApplicationSessionActivationListener threw exception", t);
	                }
	            }
			}
	    }
    }

	public boolean getInvalidateWhenReady() {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		return invalidateWhenReady;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getSession(String id, Protocol protocol) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		if(id == null) {
			throw new NullPointerException("id is null");
		}
		if(protocol == null) {
			throw new NullPointerException("protocol is null");
		}
		switch (protocol) {
			case SIP :
				return getSipSession(id);
				
			case HTTP :
				return findHttpSession(id);
		}
		return null;
	}

	public boolean isReadyToInvalidate() {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		updateReadyToInvalidateState();
		return readyToInvalidate;
	}

	public void setInvalidateWhenReady(boolean invalidateWhenReady) {
		if(!isValid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		this.invalidateWhenReady = invalidateWhenReady;
	}
	
	public void onSipSessionReadyToInvalidate(MobicentsSipSession mobicentsSipSession) {
		removeSipSession(mobicentsSipSession);
		updateReadyToInvalidateState();
	}
	
	private void updateReadyToInvalidateState() {
		if(isValid && !readyToInvalidate) {
			for(MobicentsSipSession sipSession : getSipSessions()) {
				if(sipSession.isValid() && !sipSession.isReadyToInvalidate()) {
					if(logger.isDebugEnabled()) {
						logger.debug("Sip Session not ready to be invalidated : " + sipSession.getKey());
					}
					return;
				}
			}
			// Fix for Issue 813 : SipApplicationSession invalidates too soon when HttpSession are present
			// (http://code.google.com/p/mobicents/issues/detail?id=813)
			for(HttpSession httpSession: getHttpSessions()) {				
				if(httpSession instanceof ConvergedSession) {
					ConvergedSession convergedSession = (ConvergedSession) httpSession;
					if(convergedSession.isValid()) {
						if(logger.isDebugEnabled()) {
							logger.debug("Http Session not ready to be invalidated : " + convergedSession.getId());
						}
						return;
					}
				} 
			}
			
			if(servletTimers == null || this.servletTimers.size() <= 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("All sip sessions and http session are ready to be invalidated, no timers alive, can invalidate this application session " + key);
				}
				this.readyToInvalidate = true;
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug(servletTimers.size() + " Timers still alive, cannot invalidate this application session " + key);
				}
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Sip application session already invalidated "+ key);
			}
			this.readyToInvalidate = true;
		}
	}
	
	public void tryToInvalidate() {
		if(isValid && invalidateWhenReady) {
			notifySipApplicationSessionListeners(SipApplicationSessionEventType.READYTOINVALIDATE);
			if(readyToInvalidate) {
				boolean allSipSessionsInvalidated = true;
				for(MobicentsSipSession sipSession : getSipSessions()) {
					if(sipSession.isValid()) {
						allSipSessionsInvalidated = false;
						break;
					}
				}
				boolean allHttpSessionsInvalidated = true;
				for(HttpSession httpSession : getHttpSessions()) {
					ConvergedSession convergedSession = (ConvergedSession) httpSession;
					if(convergedSession.isValid()) {
						allHttpSessionsInvalidated = false;
						break;
					}
				}
				if(allSipSessionsInvalidated && allHttpSessionsInvalidated) {
					this.invalidate();
				}
			}
		}
		
	}

	/**
	 * @return the expired
	 */
	public boolean isExpired() {
		return expired;
	}

	/**
	 * @param currentServletHandler the currentServletHandler to set
	 */
	public void setCurrentRequestHandler(String currentRequestHandler) {
		this.currentRequestHandler = currentRequestHandler;
	}

	/**
	 * @return the currentServletHandler
	 */
	public String getCurrentRequestHandler() {
		return currentRequestHandler;
	}

//	public ThreadPoolExecutor getExecutorService() {
//		return executorService;
//	}

	/**
	 * @return the semaphore
	 */
	public Semaphore getSemaphore() {
		return semaphore;
	}
	
	public MobicentsSipApplicationSessionFacade getSession() {		

        if (facade == null){
            if (SecurityUtil.isPackageProtectionEnabled()){
                final MobicentsSipApplicationSession fsession = this;
                facade = (MobicentsSipApplicationSessionFacade)AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        return new MobicentsSipApplicationSessionFacade(fsession);
                    }
                });
            } else {
                facade = new MobicentsSipApplicationSessionFacade(this);
            }
        }
        return (facade);	  
	}

	protected String jvmRoute;
	
	public String getJvmRoute() {
		return this.jvmRoute;
	}

	public void setJvmRoute(String jvmRoute) {
		// Assign the new jvmRoute
		this.jvmRoute = jvmRoute;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MobicentsSipApplicationSession) {
			return ((MobicentsSipApplicationSession)obj).getKey().equals(getKey());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public String toString() {
		return getId();
	}
}
