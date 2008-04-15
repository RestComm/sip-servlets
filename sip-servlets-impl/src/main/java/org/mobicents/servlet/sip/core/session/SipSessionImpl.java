package org.mobicents.servlet.sip.core.session;


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
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;

import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;


/**
 * 
 * Implementation of the SipSession interface.
 * 
 *
 *@author vralev
 *@author mranga
 *
 */
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
	
	/**
	 * The first sip application for subsequent requests.
	 */
	private SipContext sipContext;
	
	/**
	 * Subscriber URI should be set for outbound sessions, from requests created in the container.
	 */
	private URI subscriberURI;
	
	/**
	 * Outbound interface is onle of the allowed values in the Servlet COntext attribute
	 * "javax.servlet.ip.outboundinterfaces"
	 */
	private SipURI outboundInterface;
	
	
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
	private Transaction sessionCreatingTransaction;
	// =============================================================
	
	private Set<Transaction> ongoingTransactions = new TreeSet<Transaction>();
	
	private boolean supervisedMode;


	/*
	 * The almighty provider
	 */
	private SipProvider provider;
	
	public SipSessionImpl (SipProvider provider,  SipApplicationSessionImpl sipApp) {
		this.provider = provider;
		this.sipApplicationSession = sipApp;
		this.creationTime = this.lastAccessTime = System.currentTimeMillis();
		this.uuid = UUID.randomUUID();
		this.state = State.INITIAL;
		this.valid = true;
		this.supervisedMode = true;
		if ( sipApp != null) sipApp.addSipSession(this);
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
		if(method.equals(Request.ACK)
				||method.equals(Request.CANCEL))
			throw new IllegalArgumentException(
					"Can not create ACK or CANCEL requests with this method");
		
		return SipFactoryImpl.getInstance().createRequest(
				this.sipApplicationSession,
				method,
				this.getLocalParty(),
				this.getRemoteParty());
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
			return ((CallIdHeader)this.sessionCreatingTransaction.getRequest().getHeader(CallIdHeader.NAME)).getCallId();
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
		if(sessionCreatingDialog != null)
			return new AddressImpl(sessionCreatingDialog.getLocalParty());
		else
		{
			try
			{
				FromHeader fromHeader = (FromHeader)sessionCreatingTransaction.getRequest().getHeader(FromHeader.NAME);
				return new AddressImpl(fromHeader.getAddress());
			}
			catch(Exception e)
			{
				throw new RuntimeException("Error creating Address", e);
			}
		}
	}

	public SipApplicationRoutingRegion getRegion() {
		return routingRegion;
	}

	public Address getRemoteParty() {
		if(sessionCreatingDialog != null)
			return new AddressImpl(sessionCreatingDialog.getLocalParty());
		else
		{
			try
			{
				ToHeader toHeader = (ToHeader)sessionCreatingTransaction.getRequest().getHeader(ToHeader.NAME);
				return new AddressImpl(toHeader.getAddress());
			}
			catch(Exception e)
			{
				throw new RuntimeException("Error creating Address", e);
			}
		}
	}

	public State getState() {
		return this.state;
	}

	public URI getSubscriberURI() {
		if (this.subscriberURI == null)
			throw new IllegalStateException("Subscriber URI is only available for outbound sessions.");
		else 
			return this.subscriberURI;
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
		if(!valid) {
			throw new IllegalStateException("the session has already been invalidated, no handler can be set on it anymore !");
		}
		//TODO throw a ServletException if no servlet with the specified name exists in this application
		// this implies that the sipsession knows all the servlet of the application, the constructor should refactored for that
		this.handlerServlet = name;
	}
	
	public String getHandler()
	{
		return handlerServlet;
	}

	public void setOutboundInterface(SipURI uri) {
		// TODO: validate from the list in servlet context
		this.outboundInterface = uri;
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

	public Transaction getSessionCreatingTransaction() {
		return sessionCreatingTransaction;
	}

	public void setSessionCreatingTransaction(Transaction initialTransaction) {
		this.sessionCreatingTransaction = initialTransaction;
		this.ongoingTransactions.add(initialTransaction);
	}

	public boolean isSupervisedMode() {
		return supervisedMode;
	}

	public void setSupervisedMode(boolean supervisedMode) {
		this.supervisedMode = supervisedMode;
	}

	public void setSubscriberURI(URI subscriberURI) {
		this.subscriberURI = subscriberURI;
	}

	public SipURI getOutboundInterface() {
		return outboundInterface;
	}

	public SipContext getSipContext() {
		return sipContext;
	}

	public void setSipContext(SipContext sipContext) {
		this.sipContext = sipContext;
	}

	public SipProvider getProvider() {
		
		return this.provider;
	}

	
	public void onTransactionTimeout(Transaction transaction)
	{
		this.ongoingTransactions.remove(transaction);
	}
	
	public void onDialogTimeout(Dialog dialog)
	{
		if(this.ongoingTransactions.size()>0)
		{
			throw new IllegalStateException("Dialog timed out, but there are active transactions.");
		}
		this.state = State.TERMINATED;
	}

	public void setState(State state) {
		this.state = state;
	}



	
}
