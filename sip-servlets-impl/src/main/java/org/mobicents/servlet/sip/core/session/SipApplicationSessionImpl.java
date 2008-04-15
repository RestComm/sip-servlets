package org.mobicents.servlet.sip.core.session;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.URI;

import org.mobicents.servlet.sip.core.timers.ServletTimerImpl;


public class SipApplicationSessionImpl implements SipApplicationSession {

	private static long DEFAULT_LIFETIME = 1000*60*60;
	
	private SipListenersHolder listeners;
	
	private Map<String, Object> sipApplicationSessionAttributeMap = new ConcurrentHashMap<String,Object>() ;

	private Map<String,SipSessionImpl> sipSessions = new ConcurrentHashMap<String,SipSessionImpl>();
	
	private String applicationName;
	
	private String id;
	
	private long lastAccessTime;
	
	private long creationTime;
	
	private long expirationTime;
	
	private Set<ServletTimer> servletTimers;
	
	private boolean valid;
	
	private TimerListener agregatingListener;
	private ArrayList<ServletTimer> runningTimers;
	
	/**
	 * Passed as info object into Servelt timer that ticks for this sip app
	 * session as expiration timer
	 */
	private Serializable endObject;
	private ServletTimerImpl expirationTimer;
	
	/**
	 * Lock for this application session
	 */
	private Object _APP_LOCK=new Object();
	
	public SipApplicationSessionImpl() {
		this.id = UUID.randomUUID().toString();
		lastAccessTime = creationTime = System.currentTimeMillis();
		expirationTime = lastAccessTime + DEFAULT_LIFETIME;
	}
	
	public SipApplicationSessionImpl(String id ) {
		this.id = id;
		lastAccessTime = creationTime = System.currentTimeMillis();
		expirationTime = lastAccessTime + DEFAULT_LIFETIME;
	}
	
	public void addSipSession( SipSessionImpl sipSessionImpl) {
		this.sipSessions.put(sipSessionImpl.getId(), sipSessionImpl);
		sipSessionImpl.setApplicationSession(this);
	}
	
	
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
						url + "&org.mobicents.servlet.sip.ApplicationSessionKey="
							+ getId().toString());
			}
			else
			{
				ret = new URL(
						url + "?org.mobicents.servlet.sip.ApplicationSessionKey="
							+ getId().toString());
			}
			return ret;
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed encoding URL", e);
		}
	}

	public Object getAttribute(String name) {
		return this.sipApplicationSessionAttributeMap.get(name);
	}

	public Iterator<String> getAttributeNames() {
		return this.sipApplicationSessionAttributeMap.keySet().iterator();
	}

	public long getCreationTime() {
		return creationTime;
	}

	public long getExpirationTime() {
		return expirationTime;
	}

	public String getId() {
		return id;
	}

	public long getLastAccessedTime() {
		return lastAccessTime;
	}

	public Iterator<?> getSessions() {
		return sipSessions.entrySet().iterator();
	}

	public Iterator<?> getSessions(String protocol) {
		if(protocol.equals("SIP"))
			return sipSessions.values().iterator();
		else 
			return null;
	}

	public SipSession getSipSession(String id) {
		return sipSessions.get(id);
	}

	public Collection<ServletTimer> getTimers() {
		return servletTimers;
	}

	public void invalidate() {
		for(SipSessionImpl session: sipSessions.values())
		{
			if(session.isValid())
				throw new IllegalStateException("All SIP " +
						"and HTTP sessions must be invalidated" +
						" before invalidating the application session.");
		}
		valid = false;
	}

	public boolean isValid() {
		return valid;
	}

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

		for (SipApplicationSessionBindingListener l : this.listeners
				.getSipApplicationSessionBindingListeners()) {
			l.valueUnbound(event);

		}

		for (SipApplicationSessionAttributeListener l : this.listeners
				.getSipApplicationSessionAttributeListeners()) {
			l.attributeRemoved(event);
		}

		this.sipApplicationSessionAttributeMap.remove(name);
	}

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
		if (sipApplicationSessionAttributeMap.containsKey(key)) {
			// This is initial, we need to send value bound event

			for (SipApplicationSessionBindingListener l : this.listeners
					.getSipApplicationSessionBindingListeners()) {
				l.valueBound(event);

			}

			for (SipApplicationSessionAttributeListener l : this.listeners
					.getSipApplicationSessionAttributeListeners()) {
				l.attributeAdded(event);
			}

		} else {

			for (SipApplicationSessionAttributeListener l : this.listeners
					.getSipApplicationSessionAttributeListeners()) {
				l.attributeReplaced(event);
			}

		}

		this.sipApplicationSessionAttributeMap.put(key, attribute);

	}

	public int setExpires(int deltaMinutes) {
		if(deltaMinutes == 0)
			this.expirationTime = Long.MAX_VALUE;
		else
			this.expirationTime = System.currentTimeMillis() + deltaMinutes*1000*60;
		return 0;
	}

	public SipListenersHolder getListeners() {
		return listeners;
	}

	public void setListeners(SipListenersHolder listeners) {
		this.listeners = listeners;
	}

	public boolean hasTimerListeners() {
		return this.listeners.getTimerListeners().size() > 0;
	}

	public void timerCanceled(ServletTimer st) {
	}

	public TimerListener getAgregatingListener() {
		return agregatingListener;
	}

	public void setAgregatingListener(TimerListener agregatingListener) {
		this.agregatingListener = agregatingListener;
	}

	Serializable getEndObject() {
		if (this.endObject == null)
			this.endObject = new Serializable() {
			};

		return this.endObject;
	}
	
	void expirationTimerFired()
	{
		
	}
	
	public void timerScheduled(ServletTimerImpl st) {
		
		this.runningTimers.add(st);

	}

}
