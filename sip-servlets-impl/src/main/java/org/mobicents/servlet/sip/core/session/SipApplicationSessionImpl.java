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

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionActivationListener;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipApplicationSessionEvent;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * 
 * Implementation of the SipApplicationSession interface.
 * An instance of this sip application session can only be retrieved through the Session Manager
 * to constrain the creation of sip application session and to make sure that all sessions created
 * can be retrieved only through the session manager 
 *
 */
public class SipApplicationSessionImpl implements SipApplicationSession {
	private transient static final Log logger = LogFactory.getLog(SipSessionImpl.class);

	private enum SipApplicationSessionEventType {
		CREATION, DELETION, EXPIRATION;
	}
	/**
	 * Timer task that will notify the listeners that the sip application session has expired 
	 * @author Jean Deruelle
	 */
	private class SipApplicationSessionTimerTask extends TimerTask {		
		private SipApplicationSessionImpl sipApplicationSessionImpl;
		
		/**
		 * Default Constructor
		 * @param sipApplicationSessionImpl the sip application session that will expires
		 */
		public SipApplicationSessionTimerTask(SipApplicationSessionImpl sipApplicationSessionImpl) {
			this.sipApplicationSessionImpl = sipApplicationSessionImpl;
		}
		
		@Override
		public void run() {
			sipApplicationSessionImpl.notifySipApplicationSessionListeners(SipApplicationSessionEventType.EXPIRATION);
			sipApplicationSessionImpl.expired = true;
		}
		
	}
	
	public static final String SIP_APPLICATION_KEY_PARAM_NAME = "org.mobicents.servlet.sip.ApplicationSessionKey"; 
	
	private Map<String, Object> sipApplicationSessionAttributeMap;

	private Map<SipSessionKey,SipSessionImpl> sipSessions;
	
	private Map<String, HttpSession> httpSessions;
	
	private SipApplicationSessionKey key;	
	
	private long lastAccessTime;
	
	private long creationTime;
	
	private long expirationTime;
	
	private boolean expired;
	
	private Timer expirationTimer;
	
	private TimerTask expirationTimerTask;
	
	private Map<String, ServletTimer> servletTimers;
	
	private boolean valid;

	/**
	 * The first sip application for subsequent requests.
	 */
	private SipContext sipContext;
	
	
	public SipApplicationSessionImpl(SipApplicationSessionKey key, SipContext sipContext) {
		sipApplicationSessionAttributeMap = new ConcurrentHashMap<String,Object>() ;
		sipSessions = new ConcurrentHashMap<SipSessionKey,SipSessionImpl>();
		httpSessions = new ConcurrentHashMap<String,HttpSession>();
		servletTimers = new ConcurrentHashMap<String, ServletTimer>();
		this.key = key;
		this.sipContext = sipContext;
		lastAccessTime = creationTime = System.currentTimeMillis();
		expired = false;
		expirationTimer =  new Timer();
		valid = true;
		// the sip context can be null if the AR returned an application that was not deployed
		if(sipContext != null) {
			//scheduling the timer for session expiration
			if(sipContext.getSipApplicationSessionTimeout() > 0) {
				expirationTime = sipContext.getSipApplicationSessionTimeout() * 60 * 1000;				
				expirationTimerTask = new SipApplicationSessionTimerTask(this);
				if(logger.isDebugEnabled()) {
					logger.debug("Scheduling sip application session "+ key +" to expire in " + (expirationTime / 1000 / 60) + " minutes");
				}
				expirationTimer.schedule(expirationTimerTask, expirationTime);
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("The sip application session "+ key +" will never expire ");
				}
				// If the session timeout value is 0 or less, then an application session timer 
				// never starts for the SipApplicationSession object and the container does 
				// not consider the object to ever have expired
				expirationTime = -1;
			}
			notifySipApplicationSessionListeners(SipApplicationSessionEventType.CREATION);
		}
		//FIXME create and start a timer for session expiration
	}
	
	/**
	 * Notifies the listeners that a lifecycle event occured on that sip application session 
	 * @param sipApplicationSessionEventType the type of event that happened
	 */
	private void notifySipApplicationSessionListeners(SipApplicationSessionEventType sipApplicationSessionEventType) {				
		SipApplicationSessionEvent event = new SipApplicationSessionEvent(this);
		if(logger.isDebugEnabled()) {
			logger.debug("notifying sip application session listeners of context " + 
					key.getApplicationName() + " of following event " + sipApplicationSessionEventType);
		}
		List<SipApplicationSessionListener> listeners = 
			sipContext.getListeners().getSipApplicationSessionListeners();
		for (SipApplicationSessionListener sipApplicationSessionListener : listeners) {
			try {
				if(SipApplicationSessionEventType.CREATION.equals(sipApplicationSessionEventType)) {
					sipApplicationSessionListener.sessionCreated(event);
				} else if (SipApplicationSessionEventType.DELETION.equals(sipApplicationSessionEventType)) {
					sipApplicationSessionListener.sessionDestroyed(event);
				} else if (SipApplicationSessionEventType.EXPIRATION.equals(sipApplicationSessionEventType)) {
					sipApplicationSessionListener.sessionExpired(event);
				}
				
			} catch (Throwable t) {
				logger.error("SipApplicationSessionListener threw exception", t);
			}
		}		
	}
	
	protected void addSipSession( SipSessionImpl sipSessionImpl) {
		this.sipSessions.put(sipSessionImpl.getKey(), sipSessionImpl);
//		sipSessionImpl.setSipApplicationSession(this);
	}
	
	protected SipSessionImpl removeSipSession (SipSessionImpl sipSessionImpl) {
		return this.sipSessions.remove(sipSessionImpl.getKey());
	}
	
	public void addHttpSession( HttpSession httpSession) {
		this.httpSessions.put(httpSession.getId(), httpSession);
	}
	
	public HttpSession removeHttpSession (HttpSession httpSession) {
		return this.httpSessions.remove(httpSession.getId());
	}
	
	public HttpSession findHttpSession (HttpSession httpSession) {
		return this.httpSessions.get(httpSession.getId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#encodeURI(javax.servlet.sip.URI)
	 */
	public void encodeURI(URI uri) {
		uri.setParameter("org.mobicents.servlet.sip.ApplicationSessionKey", getId());
	}

	/**
	 * Adds a get parameter to the URL like this:
	 * http://hostname/link -> http://hostname/link?org.mobicents.servlet.sip.ApplicationSessionKey=0
	 * http://hostname/link?something=1 -> http://hostname/link?something=1&org.mobicents.servlet.sip.ApplicationSessionKey=0
	 */
	public URL encodeURL(URL url) {
		String urlStr = url.toExternalForm();
		try
		{
			URL ret;
			if(urlStr.contains("?"))
			{
				ret = new URL(
						url + "&"+ SIP_APPLICATION_KEY_PARAM_NAME + "="
							+ getId().toString());
			}
			else
			{
				ret = new URL(
						url + "?"+ SIP_APPLICATION_KEY_PARAM_NAME + "="
							+ getId().toString());
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed encoding URL", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return this.sipApplicationSessionAttributeMap.get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getAttributeNames()
	 */
	public Iterator<String> getAttributeNames() {
		return this.sipApplicationSessionAttributeMap.keySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getCreationTime()
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getExpirationTime()
	 */
	public long getExpirationTime() {
		if(expirationTime <= 0) {
			return 0;
		}
		if(expired) {
			return Long.MIN_VALUE;
		}
		return expirationTime;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getId()
	 */
	public String getId() {
		return key.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		return lastAccessTime;
	}

	public void setLastAccessedTime(long lastAccessTime) {
		this.lastAccessTime= lastAccessTime;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getSessions()
	 */
	public Iterator<?> getSessions() {
		return sipSessions.entrySet().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getSessions(java.lang.String)
	 */
	public Iterator<?> getSessions(String protocol) {
		if("SIP".equalsIgnoreCase(protocol))
			return sipSessions.values().iterator();
		else 
			//sipContext.getManager().findSessions()
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getSipSession(java.lang.String)
	 */
	public SipSession getSipSession(String id) {
		return sipSessions.get(id);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#getTimers()
	 */
	public Collection<ServletTimer> getTimers() {
		return servletTimers.values();
	}

	/**
	 * Add a servlet timer to this application session
	 * @param servletTimer the servlet timer to add
	 */
	public void addServletTimer(ServletTimer servletTimer){
		servletTimers.put(servletTimer.getId(), servletTimer);
	}
	/**
	 * Remove a servlet timer from this application session
	 * @param servletTimer the servlet timer to remove
	 */
	public void removeServletTimer(ServletTimer servletTimer){
		servletTimers.remove(servletTimer);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#invalidate()
	 */
	public void invalidate() {
		if(!valid) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		//JSR 289 Section 6.1.2.2.1
		//When the IllegalStateException is thrown, the application is guaranteed 
		//that the state of the SipApplicationSession object will be unchanged from its state prior to the invalidate() 
		//method call. Even session objects that were eligible for invalidation will not have been invalidated.
		
		//need to check before doing the real invalidation if they are eligible 
		//for invalidation
		for(SipSessionImpl session: sipSessions.values()) {
			if(session.isValid()) {
				try {
					session.checkInvalidation();
				} catch (IllegalStateException e) {
					throw new IllegalStateException("All SIP " +
						"and HTTP sessions must be invalidated" +
						" before invalidating the application session.", e);
				}
			}					
		}
		//doing the invalidation
		for(SipSessionImpl session: sipSessions.values()) {
			if(session.isValid()) {
				session.invalidate();
			}
		}
		for(HttpSession session: httpSessions.values()) {
			session.invalidate();
		}
		valid = false;	
		//cancelling the timers
		for (Map.Entry<String, ServletTimer> servletTimerEntry : servletTimers.entrySet()) {
			servletTimerEntry.getValue().cancel();
		}
		notifySipApplicationSessionListeners(SipApplicationSessionEventType.DELETION);
		// FIXME get rid of the reference in the session manager (refactor session manager
		// to map to tomcat session management)
//		sipContext.getSessionManager().removeSipApplicationSession(key);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#isValid()
	 */
	public boolean isValid() {
		return valid;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {

		if (!isValid())
			throw new IllegalStateException(
					"Can not bind object to session that has been invalidated!!");

		if (name == null)
			// throw new NullPointerException("Name of attribute to bind cant be
			// null!!!");
			return;

		SipApplicationSessionBindingEvent event = new SipApplicationSessionBindingEvent(
				this, name);
		SipListenersHolder listeners = sipContext.getListeners();
		if(logger.isDebugEnabled()) {
			logger.debug("notifying SipApplicationSessionBindingListeners of value unbound on key "+ key);
		}
		for (SipApplicationSessionBindingListener listener : listeners
				.getSipApplicationSessionBindingListeners()) {
			try {
				listener.valueUnbound(event);
			} catch (Throwable t) {
				logger.error("SipApplicationSessionBindingListener threw exception", t);
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("notifying SipApplicationSessionAttributeListener of attribute removed on key "+ key);
		}
		for (SipApplicationSessionAttributeListener listener : listeners
				.getSipApplicationSessionAttributeListeners()) {
			try {
				listener.attributeRemoved(event);
			} catch (Throwable t) {
				logger.error("SipApplicationSessionAttributeListener threw exception", t);
			}
		}

		this.sipApplicationSessionAttributeMap.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String key, Object attribute) {

		if (!isValid())
			throw new IllegalStateException(
					"Can not bind object to session that has been invalidated!!");

		if (key == null)
			throw new NullPointerException(
					"Name of attribute to bind cant be null!!!");
		if (attribute == null)
			throw new NullPointerException(
					"Attribute that is to be bound cant be null!!!");

		SipApplicationSessionBindingEvent event = new SipApplicationSessionBindingEvent(
				this, key);
		SipListenersHolder listeners = sipContext.getListeners();
		if (!sipApplicationSessionAttributeMap.containsKey(key)) {
			// This is initial, we need to send value bound event
			if(logger.isDebugEnabled()) {
				logger.debug("notifying SipApplicationSessionBindingListeners of value bound on key "+ key);
			}
			for (SipApplicationSessionBindingListener listener : listeners
					.getSipApplicationSessionBindingListeners()) {
				try {					
					listener.valueBound(event);
				} catch (Throwable t) {
					logger.error("SipApplicationSessionBindingListener threw exception", t);
				}				
			}
			if(logger.isDebugEnabled()) {
				logger.debug("notifying SipApplicationSessionAttributeListener of attribute added on key "+ key);
			}
			for (SipApplicationSessionAttributeListener listener : listeners
					.getSipApplicationSessionAttributeListeners()) {
				try {
					listener.attributeAdded(event);
				} catch (Throwable t) {
					logger.error("SipApplicationSessionAttributeListener threw exception", t);
				}
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("notifying SipApplicationSessionAttributeListener of attribute replaced on key "+ key);
			}
			for (SipApplicationSessionAttributeListener listener : listeners
					.getSipApplicationSessionAttributeListeners()) {
				try {
					listener.attributeReplaced(event);
				} catch (Throwable t) {
					logger.error("SipApplicationSessionAttributeListener threw exception", t);
				}
			}
		}
		this.sipApplicationSessionAttributeMap.put(key, attribute);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipApplicationSession#setExpires(int)
	 */
	public int setExpires(int deltaMinutes) {
		if(!isValid()) {
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
			this.expirationTime = -1;
			if(expirationTimerTask != null) {
				expirationTimerTask.cancel();								
			}		
			return Integer.MAX_VALUE;
		} else {
			this.expirationTime = (expirationTimerTask.scheduledExecutionTime() - expirationTime) + deltaMinutes * 1000 * 60;
			if(expirationTimerTask != null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Re-Scheduling sip application session "+ key +" to expire in " + (expirationTime / 1000 / 60)+ " minutes");
				}
				expirationTimerTask.cancel();
				expirationTimerTask = new SipApplicationSessionTimerTask(this);
				expirationTimer.schedule(expirationTimerTask, expirationTime);
			}
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
		return servletTimers.get(id);
	}
	
	/**
     * Perform the internal processing required to passivate
     * this session.
     */
    public void passivate() {
        // Notify ActivationListeners
    	SipApplicationSessionEvent event = null;
        Set<String> keySet = sipApplicationSessionAttributeMap.keySet();
        for (String key : keySet) {
        	Object attribute = sipApplicationSessionAttributeMap.get(key);
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
    
    /**
     * Perform internal processing required to activate this
     * session.
     */
    public void activate() {        
        // Notify ActivationListeners
    	SipApplicationSessionEvent event = null;
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
