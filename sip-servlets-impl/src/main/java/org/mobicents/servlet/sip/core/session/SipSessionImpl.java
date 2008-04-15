package org.mobicents.servlet.sip.core.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationRoutingRegion;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.SipSession.State;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;

import org.apache.catalina.Container;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

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
	private static final Log logger = LogFactory.getLog(SipSessionImpl.class);
	
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
		
	private Set<Transaction> ongoingTransactions = 
		Collections.synchronizedSet(new HashSet<Transaction>());
	
	private boolean supervisedMode;


	/*
	 * The almighty provider
	 */
	private SipFactoryImpl sipFactory;
	
	public SipSessionImpl (SipFactoryImpl sipFactoryImpl,  SipApplicationSessionImpl sipApp) {
		this.sipFactory = sipFactoryImpl;
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
		SipServletRequest sipServletRequest = null;
		if(Request.BYE.equalsIgnoreCase(method)) {			
			try {
				Request byeRequest = this.sessionCreatingDialog.createRequest(Request.BYE);
				sipServletRequest = new SipServletRequestImpl(
						byeRequest, this.sipFactory, this, null, null,
						false);
			} catch (SipException e) {
				logger.error("Cannot create the bye request form the dialog",e);
				throw new IllegalArgumentException("Cannot create the bye request");
			}			
		} else {
			sipServletRequest = sipFactory.createRequest(
				this.sipApplicationSession,
				method,
				this.getLocalParty(),
				this.getRemoteParty());
		}
		return sipServletRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {
		return this.sipApplicationSession;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		return sipSessionAttributeMap.get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		Vector<String> names = new Vector<String>(sipSessionAttributeMap.keySet());
		return names.elements();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getCallId()
	 */
	public String getCallId() {
		if(this.sessionCreatingDialog != null)
			return this.sessionCreatingDialog.getCallId().getCallId();
		else
			return ((CallIdHeader)this.sessionCreatingTransaction.getRequest().getHeader(CallIdHeader.NAME)).getCallId();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getCreationTime()
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getId()
	 */
	public String getId() {
		return uuid.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		return lastAccessTime;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getLocalParty()
	 */
	public Address getLocalParty() {
		if(sessionCreatingDialog != null) {
			return new AddressImpl(sessionCreatingDialog.getLocalParty());
		} else {
			try {
				FromHeader fromHeader = (FromHeader)sessionCreatingTransaction.getRequest().getHeader(FromHeader.NAME);
				return new AddressImpl(fromHeader.getAddress());
			} catch(Exception e) {
				throw new RuntimeException("Error creating Address", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getRegion()
	 */
	public SipApplicationRoutingRegion getRegion() {
		return routingRegion;
	}

	public void setRoutingRegion(SipApplicationRoutingRegion routingRegion) {
		this.routingRegion = routingRegion;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getRemoteParty()
	 */
	public Address getRemoteParty() {
		if(sessionCreatingDialog != null) {
			return new AddressImpl(sessionCreatingDialog.getRemoteParty());
		} else {
			try {
				ToHeader toHeader = (ToHeader)sessionCreatingTransaction.getRequest().getHeader(ToHeader.NAME);
				return new AddressImpl(toHeader.getAddress());
			} catch(Exception e) {
				throw new RuntimeException("Error creating Address", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getState()
	 */
	public State getState() {
		return this.state;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getSubscriberURI()
	 */
	public URI getSubscriberURI() {
		if (this.subscriberURI == null)
			throw new IllegalStateException("Subscriber URI is only available for outbound sessions.");
		else 
			return this.subscriberURI;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#invalidate()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#isOngoingTransaction()
	 */
	public boolean isOngoingTransaction() {
		if(!isSupervisedMode())
			return false;
		else
			return ongoingTransactions.size()>0;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#isValid()
	 */
	public boolean isValid() {
		return this.valid;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {

		if(!isValid())
			throw new IllegalStateException("Can not bind object to session that has been invalidated!!");
		
		if(name==null)
		//	throw new NullPointerException("Name of attribute to bind cant be null!!!");
			return;
		
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, name);

		for (SipSessionBindingListener listener : this.getSipSessionBindingListeners()) {
			listener.valueUnbound(event);
		}
		for (SipSessionAttributeListener listener : this
				.getSipSessionAttributeListeners()) {
			listener.attributeRemoved(event);
		}

		this.sipSessionAttributeMap.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String key, Object attribute) {
		if(!isValid()) {
			throw new IllegalStateException("Can not bind object to session that has been invalidated!!");
		}
		if(key == null) {
			throw new NullPointerException("Name of attribute to bind cant be null!!!");
		}
		if(attribute == null) {
			throw new NullPointerException("Attribute that is to be bound cant be null!!!");
		}
		
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, key);
		if (!this.sipSessionAttributeMap.containsKey(key)) {
			// This is initial, we need to send value bound event
			for (SipSessionBindingListener listener : this
					.getSipSessionBindingListeners()) {
				listener.valueBound(event);

			}
			for (SipSessionAttributeListener listener : this
					.getSipSessionAttributeListeners()) {
				listener.attributeAdded(event);
			}
		} else {
			for (SipSessionAttributeListener listener : this
					.getSipSessionAttributeListeners()) {
				listener.attributeReplaced(event);
			}
		}

		this.sipSessionAttributeMap.put(key, attribute);

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#setHandler(java.lang.String)
	 */
	public void setHandler(String name) throws ServletException {
		if(!valid) {
			throw new IllegalStateException("the session has already been invalidated, no handler can be set on it anymore !");
		}
		SipContext sipContext = getSipApplicationSession().getSipContext();
		Container[] containers = sipContext.findChildren();
		boolean isServletExists = false;
		int i = 0;
		while (i < containers.length && !isServletExists) {
			if(containers[i] instanceof SipServletImpl) {
				final SipServletImpl sipServletImpl = (SipServletImpl)containers[i];
				if(sipServletImpl.getServletName().equals(name)) {
					isServletExists = true;
				}
			}
			i++;
		}
		if(!isServletExists) {
			throw new ServletException("the sip servlet with the name "+ name + 
					" doesn't exist in the sip application " + sipContext.getApplicationName());
		}
		this.handlerServlet = name;
	}
	
	/**
	 * Retrieve the handler associated with this sip session
	 * @return the handler associated with this sip session
	 */
	public String getHandler() {
		return handlerServlet;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#setOutboundInterface(javax.servlet.sip.SipURI)
	 */
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

	/**
	 * @param sessionCreatingTransaction the sessionCreatingTransaction to set
	 */
	public void setSessionCreatingTransaction(Transaction sessionCreatingTransaction) {
		this.sessionCreatingTransaction = sessionCreatingTransaction;
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

	public Set<SipProvider> getProviders() {		
		return sipFactory.getSipProviders();
	}
	
	public void onDialogTimeout(Dialog dialog) {
		if(this.ongoingTransactions.size()>0) {
			throw new IllegalStateException("Dialog timed out, but there are active transactions.");
		}
		this.state = State.TERMINATED;
	}

	public void setState(State state) {
		this.state = state;
	}

	/**
	 * Add an ongoing tx to the session.
	 */
	public void addOngoingTransaction(Transaction transaction) {
		this.ongoingTransactions.add(transaction);
		if(logger.isDebugEnabled()) {
			logger.debug("transaction "+ transaction +" has been added to sip session's ongoingTransactions" );
		}
	}
	
	/**
	 * Remove an ongoing tx to the session.
	 */
	public void removeOngoingTransaction(Transaction transaction) {
		this.ongoingTransactions.remove(transaction);
		if(logger.isDebugEnabled()) {
			logger.debug("transaction "+ transaction +" has been removed from sip session's ongoingTransactions" );
		}		
	}
	
	public Set<Transaction> getOngoingTransactions() {
		return this.ongoingTransactions;
	}	
	
	public void updateStateOnResponse(SipServletResponseImpl response)
	{
		if( response.getStatus()>=200 && response.getStatus()<300 )
			this.setState(State.CONFIRMED);
		if( response.getStatus()>=100 && response.getStatus()<200 )
			this.setState(State.EARLY);
	}
}
