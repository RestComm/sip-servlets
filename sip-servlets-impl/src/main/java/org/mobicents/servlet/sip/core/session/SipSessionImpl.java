package org.mobicents.servlet.sip.core.session;

import gov.nist.javax.sip.stack.SIPTransaction;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;

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
import javax.sip.ClientTransaction;
import javax.sip.Dialog;

public class SipSessionImpl implements SipSession {

	private SipApplicationSessionImpl sipApplicationSession;
	
	private ArrayList<SipSessionAttributeListener> sipSessionAttributeListeners;
	private ArrayList<SipSessionBindingListener> sipSessionBindingListeners;
	private ArrayList<SipSessionListener> sipSessionListeners;

	private HashMap<String, Object> sipSessionAttributeMap;
	
	/**
	 * Unique ID for this session.
	 */
	private UUID uuid;
	
	/**
	 * Creation time.
	 */
	private long creationTime;
	
	/**
	 * Last access time.
	 */
	private long lastAccessTime;
	
	/**
	 * Routing region per session/dialog.
	 */
	private SipApplicationRoutingRegion routingRegion;
	
	/**
	 * Current state of the session, one of INTITIAL, EARLY, ESTABLISHED and TERMINATED.
	 */
	private State state;
	
	/**
	 * Is the session valid.
	 */
	private boolean valid;
	
	/**
	 * The name of the servlet withing this same app to handle all subsequent requests.
	 */
	private String handlerServlet;
	
	
	// === THESE ARE THE OBJECTS A SIP SESSION CAN BE ASSIGNED TO ===
	// TODO: Refactor this into two Session classes to avoid nulls
	// and branching on nulls
	/**
	 * We use this for dialog-related requests. In this case the dialog
	 * directly corresponds to the session.
	 */
	private Dialog sessionCreatingDialog;
	
	/**
	 * We use this for REGISTER, where a dialog doesn't exist to carry the session info.
	 * In this case the session only spans a single transaction.
	 */
	private SIPTransaction sessionCreatingTransaction;
	// =============================================================
	
	private Set<SIPTransaction> ongoingTransactions = new TreeSet<SIPTransaction>();
	
	private boolean supervisedMode;
	
	public SipSessionImpl ( Dialog dialog, SIPTransaction transaction, SipApplicationSessionImpl sipApp) {
		this.sessionCreatingDialog = dialog;
		this.sessionCreatingTransaction = transaction;
		this.ongoingTransactions.add(transaction);
		this.sipApplicationSession = sipApp;
		this.creationTime = this.lastAccessTime = System.currentTimeMillis();
		this.uuid = UUID.randomUUID();
		this.state = State.INITIAL;
		this.valid = true;
		this.supervisedMode = true;
	}
	
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#createRequest(java.lang.String)
	 */
	public SipServletRequest createRequest(String method) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipApplicationSession getApplicationSession() {
		return this.sipApplicationSession;
	}

	public Object getAttribute(String name) {
		return sipSessionAttributeMap.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		Vector<String> names = new Vector<String>(sipSessionAttributeMap.keySet());
		return names.elements();
	}

	public String getCallId() {
		if(this.sessionCreatingDialog != null)
			return this.sessionCreatingDialog.getCallId().getCallId();
		else
			return this.sessionCreatingTransaction.getOriginalRequest().getCallId().getCallId();
	}

	public long getCreationTime() {
		return creationTime;
	}

	public String getId() {
		return uuid.toString();
	}

	public long getLastAccessedTime() {
		return lastAccessTime;
	}

	public Address getLocalParty() {
		// TODO Auto-generated method stub
		return null;
	}

	public SipApplicationRoutingRegion getRegion() {
		return routingRegion;
	}

	public Address getRemoteParty() {
		// TODO Auto-generated method stub
		return null;
	}

	public State getState() {
		return this.state;
	}

	public URI getSubscriberURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public void invalidate() {
		if(state.equals(State.CONFIRMED)
				|| state.equals(State.EARLY))
			throw new IllegalStateException("Can not invalidate sessions in" + 
					state.toString() + " state.");
		if(isSupervisedMode() && ongoingTransactions.size()>0)
			throw new IllegalStateException("Can not invalidate session with " +
					"ongoing transactions in supervised mode.");
		valid = false;
	}

	public boolean isOngoingTransaction() {
		if(!isSupervisedMode())
			return false;
		else
			return ongoingTransactions.size()>0;
	}

	public boolean isValid() {
		return this.valid;
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

		this.sipSessionAttributeMap.remove(name);
	}

	public void setAttribute(String key, Object attribute) {

		
		if(!isValid())
			throw new IllegalStateException("Can not bind object to session that has been invalidated!!");
		
		if(key==null)
			throw new NullPointerException("Name of attribute to bind cant be null!!!");
		if(attribute==null)
			throw new NullPointerException("Attribute that is to be bound cant be null!!!");
		
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, key);
		if (this.sipSessionAttributeMap.containsKey(key)) {
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

		this.sipSessionAttributeMap.put(key, attribute);

	}

	public void setHandler(String name) throws ServletException {
		// TODO Auto-generated method stub

	}

	public void setOutboundInterface(SipURI uri) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param sipApplicationSession the sipApplicationSession to set
	 */
	public void setApplicationSession(SipApplicationSessionImpl sipApplicationSession) {
		this.sipApplicationSession = sipApplicationSession;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setSessionCreatingDialog(Dialog dialog) {
		this.sessionCreatingDialog = dialog;
	}

	/**
	 * @return the dialog
	 */
	public Dialog getSessionCreatingDialog() {
		return sessionCreatingDialog;
	}

	public SipApplicationSessionImpl getSipApplicationSession() {
		return sipApplicationSession;
	}

	public void setSipApplicationSession(
			SipApplicationSessionImpl sipApplicationSession) {
		this.sipApplicationSession = sipApplicationSession;
	}

	public SIPTransaction getSessionCreatingTransaction() {
		return sessionCreatingTransaction;
	}

	public void setSessionCreatingTransaction(SIPTransaction initialTransaction) {
		this.sessionCreatingTransaction = initialTransaction;
	}

	public boolean isSupervisedMode() {
		return supervisedMode;
	}

	public void setSupervisedMode(boolean supervisedMode) {
		this.supervisedMode = supervisedMode;
	}
}
