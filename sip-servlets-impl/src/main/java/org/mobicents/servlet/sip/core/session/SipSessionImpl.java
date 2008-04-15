package org.mobicents.servlet.sip.core.session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationRoutingRegion;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingEvent;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

public class SipSessionImpl implements SipSession {

	private ArrayList<SipSessionAttributeListener> sipSessionAttributeListeners;
	private ArrayList<SipSessionBindingListener> sipSessionBindingListeners;
	private ArrayList<SipSessionListener> sipSessionListeners;

	private HashMap<String, Object> _sipSessionAttributeMap;

	public ArrayList<SipSessionAttributeListener> getSipSessionAttributeListeners() {
		return sipSessionAttributeListeners;
	}

	public void setSipSessionAttributeListeners(
			ArrayList<SipSessionAttributeListener> sipSessionAttributeListeners) {
		this.sipSessionAttributeListeners = sipSessionAttributeListeners;
	}

	public ArrayList<SipSessionBindingListener> getSipSessionBindingListeners() {
		return sipSessionBindingListeners;
	}

	public void setSipSessionBindingListeners(
			ArrayList<SipSessionBindingListener> sipSessionBindingListeners) {
		this.sipSessionBindingListeners = sipSessionBindingListeners;
	}

	public ArrayList<SipSessionListener> getSipSessionListeners() {
		return sipSessionListeners;
	}

	public void setSipSessionListeners(
			ArrayList<SipSessionListener> sipSessionListeners) {
		this.sipSessionListeners = sipSessionListeners;
	}

	public SipServletRequest createRequest(String method) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipApplicationSession getApplicationSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCallId() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getCreationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Address getLocalParty() {
		// TODO Auto-generated method stub
		return null;
	}

	public SipApplicationRoutingRegion getRegion() {
		// TODO Auto-generated method stub
		return null;
	}

	public Address getRemoteParty() {
		// TODO Auto-generated method stub
		return null;
	}

	public State getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public URI getSubscriberURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public void invalidate() {
		// TODO Auto-generated method stub

	}

	public boolean isOngoingTransaction() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeAttribute(String name) {

		if(!isValid())
			throw new IllegalStateException("Can not bind object to session that has been invalidated!!");
		
		if(name==null)
		//	throw new NullPointerException("Name of attribute to bind cant be null!!!");
			return;
		
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, name);

		for (SipSessionBindingListener l : this.getSipSessionBindingListeners()) {
			l.valueUnbound(event);

		}

		for (SipSessionAttributeListener l : this
				.getSipSessionAttributeListeners()) {
			l.attributeRemoved(event);
		}

		this._sipSessionAttributeMap.remove(name);
	}

	public void setAttribute(String key, Object attribute) {

		
		if(!isValid())
			throw new IllegalStateException("Can not bind object to session that has been invalidated!!");
		
		if(key==null)
			throw new NullPointerException("Name of attribute to bind cant be null!!!");
		if(attribute==null)
			throw new NullPointerException("Attribute that is to be bound cant be null!!!");
		
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, key);
		if (this._sipSessionAttributeMap.containsKey(key)) {
			// This is initial, we need to send value bound event

			for (SipSessionBindingListener l : this
					.getSipSessionBindingListeners()) {
				l.valueBound(event);

			}

			for (SipSessionAttributeListener l : this
					.getSipSessionAttributeListeners()) {
				l.attributeAdded(event);
			}

		} else {

			for (SipSessionAttributeListener l : this
					.getSipSessionAttributeListeners()) {
				l.attributeReplaced(event);
			}

		}

		this._sipSessionAttributeMap.put(key, attribute);

	}

	public void setHandler(String name) throws ServletException {
		// TODO Auto-generated method stub

	}

	public void setOutboundInterface(SipURI uri) {
		// TODO Auto-generated method stub

	}

}
