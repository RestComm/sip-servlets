/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.core.session;

import java.io.Serializable;
import java.security.Principal;
import java.text.ParseException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationRoutingRegion;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.Dialog;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.catalina.Container;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

/**
 * 
 * Implementation of the SipSession interface.
 * An instance of this sip session can only be retrieved through the Session Manager
 * to constrain the creation of sip session and to make sure that all sessions created
 * can be retrieved only through the session manager 
 *
 *@author vralev
 *@author mranga
 *
 */
public class SipSessionImpl implements SipSession {
	
	private enum SipSessionEventType {
		CREATION, DELETION;
	}
	
	private transient static final Log logger = LogFactory.getLog(SipSessionImpl.class);
	
	private SipApplicationSessionImpl sipApplicationSession;			
	
	private ProxyBranchImpl proxyBranch;

	private Map<String, Object> sipSessionAttributeMap;
	
	private SipSessionKey key;
	
	private Principal userPrincipal;
	
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
	 * AR state info
	 */
	private Serializable stateInfo;
	
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
	
	protected SipSessionImpl (SipSessionKey key, SipFactoryImpl sipFactoryImpl, SipApplicationSessionImpl sipApplicationSessionImpl) {
		this.key = key;
		setSipApplicationSession(sipApplicationSessionImpl);
		this.sipFactory = sipFactoryImpl;
		this.creationTime = this.lastAccessTime = System.currentTimeMillis();		
		this.state = State.INITIAL;
		this.valid = true;
		this.supervisedMode = true;
		this.sipSessionAttributeMap = new ConcurrentHashMap<String, Object>();
		// the sip context can be null if the AR returned an application that was not deployed
		if(sipApplicationSessionImpl.getSipContext() != null) {
			notifySipSessionListeners(SipSessionEventType.CREATION);
		}
		//FIXME create and start a timer for session expiration
	}
	/**
	 * Notifies the listeners that a lifecycle event occured on that sip session 
	 * @param sipSessionEventType the type of event that happened
	 */
	private void notifySipSessionListeners(SipSessionEventType sipSessionEventType) {
		SipContext sipContext = 
			getSipApplicationSession().getSipContext();		
		if(logger.isDebugEnabled()) {
			logger.debug("notifying sip session listeners of context " + sipContext.getApplicationName() + " of following event " +
					sipSessionEventType);
		}
		List<SipSessionListener> sipSessionListeners = 
			sipContext.getListeners().getSipSessionListeners();
		SipSessionEvent sipSessionEvent = new SipSessionEvent(this);
		for (SipSessionListener sipSessionListener : sipSessionListeners) {
			try {
				if(SipSessionEventType.CREATION.equals(sipSessionEventType)) {
					sipSessionListener.sessionCreated(sipSessionEvent);
				} else if (SipSessionEventType.DELETION.equals(sipSessionEventType)) {
					sipSessionListener.sessionDestroyed(sipSessionEvent);
				}
			} catch (Throwable t) {
				logger.error("SipSessionListener threw exception", t);
			}
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#createRequest(java.lang.String)
	 */
	public SipServletRequest createRequest(final String method) {
		if(method.equals(Request.ACK)
				||method.equals(Request.CANCEL))
			throw new IllegalArgumentException(
					"Can not create ACK or CANCEL requests with this method");				
		if(logger.isDebugEnabled()) {
			logger.debug("dialog associated with this session to create the new request within that dialog "+
					sessionCreatingDialog);
		}
		SipServletRequestImpl sipServletRequest = null;
		if(this.sessionCreatingDialog != null) {			
			try {
				final Request methodRequest = this.sessionCreatingDialog.createRequest(method);
				final String transport = JainSipUtils.findTransport(methodRequest);
				
				ViaHeader viaHeader = JainSipUtils.createViaHeader(sipFactory.getSipProviders(), transport,
						null);			
				viaHeader.setParameter(SipApplicationDispatcherImpl.RR_PARAM_APPLICATION_NAME,
						key.getApplicationName());
				viaHeader.setParameter(SipApplicationDispatcherImpl.RR_PARAM_HANDLER_NAME,
						handlerServlet);				
				methodRequest.setHeader(viaHeader);
				
				//FIXME can it be dialog creating if a SUBSCRIBE is sent for exemple ?
				sipServletRequest = new SipServletRequestImpl(
						methodRequest, this.sipFactory, this, null, null,
						false);
			} catch (SipException e) {
				logger.error("Cannot create the bye request form the dialog",e);
				throw new IllegalArgumentException("Cannot create the bye request",e);
			} catch (ParseException e) {
				logger.error("Cannot add the via header to the bye request form the dialog",e);
				throw new IllegalArgumentException("Cannot create the bye request",e);
			}			
		} else {
			sipServletRequest =(SipServletRequestImpl) sipFactory.createRequest(
				this.sipApplicationSession,
				method,
				this.getLocalParty(),
				this.getRemoteParty());
		}
		//Application Routing :
		//removing the route headers and adding them back again except the one
		//corresponding to the app that is creating the subsequent request
		//avoid going through the same app that created the subsequent request
		final ListIterator<RouteHeader> routeHeaders = sipServletRequest.getMessage().getHeaders(RouteHeader.NAME);
		sipServletRequest.getMessage().removeHeader(RouteHeader.NAME);
		while (routeHeaders.hasNext()) {
			RouteHeader routeHeader = (RouteHeader) routeHeaders
					.next();
			String routeAppName = ((javax.sip.address.SipURI)routeHeader .getAddress().getURI()).
				getParameter(SipApplicationDispatcherImpl.RR_PARAM_APPLICATION_NAME);
			if(routeAppName == null || !routeAppName.equals(getKey().getApplicationName())) {
				sipServletRequest.getMessage().addHeader(routeHeader);
			}
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
		return key.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		return lastAccessTime;
	}

	public void setLastAccessedTime(long lastAccessTime) {
		this.lastAccessTime= lastAccessTime;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getLocalParty()
	 */
	public Address getLocalParty() {
		if(sessionCreatingDialog != null) {
			return new AddressImpl(sessionCreatingDialog.getLocalParty());
		} else if (sessionCreatingTransaction != null){
			try {
				FromHeader fromHeader = (FromHeader)sessionCreatingTransaction.getRequest().getHeader(FromHeader.NAME);
				return new AddressImpl(fromHeader.getAddress());
			} catch(Exception e) {
				throw new RuntimeException("Error creating Address", e);
			}
		} else {
			throw new RuntimeException("Error creating Address, no transaction or dialog have been found");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getRegion()
	 */
	public SipApplicationRoutingRegion getRegion() {
		return routingRegion;
	}

	/**
	 * This method allows the application to set the region that the application 
	 * is in with respect to this SipSession
	 * @param routingRegion the region that the application is in  
	 */
	public void setRoutingRegion(SipApplicationRoutingRegion routingRegion) {
		this.routingRegion = routingRegion;
	}
	
	/**
	 * @return the stateInfo
	 */
	public Serializable getStateInfo() {
		return stateInfo;
	}

	/**
	 * @param stateInfo the stateInfo to set
	 */
	public void setStateInfo(Serializable stateInfo) {
		this.stateInfo = stateInfo;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getRemoteParty()
	 */
	public Address getRemoteParty() {
		if(sessionCreatingDialog != null) {
			return new AddressImpl(sessionCreatingDialog.getRemoteParty());
		} else if (sessionCreatingTransaction != null){
			try {
				ToHeader toHeader = (ToHeader)sessionCreatingTransaction.getRequest().getHeader(ToHeader.NAME);
				return new AddressImpl(toHeader.getAddress());
			} catch(Exception e) {
				throw new RuntimeException("Error creating Address", e);
			}
		} else {
			throw new RuntimeException("Error creating Address, no transaction or dialog have been found");
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
		if(!valid) {
			throw new IllegalStateException("SipSession already invalidated !");
		}
		checkInvalidation();
		
		for (String key : sipSessionAttributeMap.keySet()) {
			removeAttribute(key);
		}
		valid = false;				
		notifySipSessionListeners(SipSessionEventType.DELETION);
		sipFactory.getSessionManager().removeSipSession(key);
	}
	
	/**
	 * 
	 */
	protected void checkInvalidation() {
		if(state.equals(State.CONFIRMED)
				|| state.equals(State.EARLY))
			throw new IllegalStateException("Can not invalidate sip session in " + 
					state.toString() + " state.");
		if(isSupervisedMode() && hasOngoingTransaction()) {
			dumpOngoingTransactions();
			throw new IllegalStateException("Can not invalidate sip session with " +
					ongoingTransactions.size() + " ongoing transactions in supervised mode.");
		}
	}

	private void dumpOngoingTransactions() {
		if(logger.isDebugEnabled()) {
			logger.debug("ongoing transactions in sip the session " + key);
		
			for (Transaction transaction : ongoingTransactions) {
				logger.debug("Transaction " + transaction + " : state = " + transaction.getState());
			}
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#isOngoingTransaction()
	 */
	public boolean hasOngoingTransaction() {
		if(!isSupervisedMode()) {
			return false;
		} else {
			for (Transaction transaction : ongoingTransactions) {
				if(TransactionState.CALLING.equals(transaction.getState()) ||
					TransactionState.TRYING.equals(transaction.getState()) ||
					TransactionState.PROCEEDING.equals(transaction.getState())) {
						return true;
				}
			}
			return false;
		}
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
		
		// Notifying Listeners of attribute removal
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, name);
		SipListenersHolder sipListenersHolder = this.getSipApplicationSession().getSipContext().getListeners();		
		for (SipSessionBindingListener listener : sipListenersHolder.getSipSessionBindingListeners()) {
			try{
				listener.valueUnbound(event);
			} catch (Throwable t) {
				logger.error("SipSessionBindingListener threw exception", t);
			}
			
		}
		for (SipSessionAttributeListener listener : sipListenersHolder.getSipSessionAttributeListeners()) {
			try{
				listener.attributeRemoved(event);
			} catch (Throwable t) {
				logger.error("SipSessionAttributeListener threw exception", t);
			}
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
		
		// Notifying Listeners of attribute addition or modification
		SipSessionBindingEvent event = new SipSessionBindingEvent(this, key);
		SipListenersHolder sipListenersHolder = this.getSipApplicationSession().getSipContext().getListeners();
		if (!this.sipSessionAttributeMap.containsKey(key)) {
			// This is initial, we need to send value bound event
			for (SipSessionBindingListener listener : sipListenersHolder
					.getSipSessionBindingListeners()) {
				try {
					listener.valueBound(event);
				} catch (Throwable t) {
					logger.error("SipSessionBindingListener threw exception", t);
				}
			}
			for (SipSessionAttributeListener listener : sipListenersHolder
					.getSipSessionAttributeListeners()) {
				try {
					listener.attributeAdded(event);
				} catch (Throwable t) {
					logger.error("SipSessionAttributeListener threw exception", t);
				}
			}
		} else {
			for (SipSessionAttributeListener listener : sipListenersHolder
					.getSipSessionAttributeListeners()) {
				try {
					listener.attributeReplaced(event);
				} catch (Throwable t) {
					logger.error("SipSessionAttributeListener threw exception", t);
				}
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

	protected void setSipApplicationSession(
			SipApplicationSessionImpl sipApplicationSession) {
		this.sipApplicationSession = sipApplicationSession;
		if ( sipApplicationSession != null) {
			if(sipApplicationSession.getSipSession(key.toString()) == null) {
				sipApplicationSession.addSipSession(this);
			}
		}
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
		return this.supervisedMode;
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
		if(hasOngoingTransaction()) {
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
	
	/**
	 * Update the sip session state upon sending/receiving a response
	 * Covers JSR 289 Section 6.2.1 along with updateStateOnRequest method
	 * @param response the response received/to send
	 * @param receive true if the response has been received, false if it is to be sent.
	 */
	public void updateStateOnResponse(SipServletResponseImpl response, boolean receive) {
		CSeqHeader cSeqHeader = (CSeqHeader)response.getMessage().getHeader(CSeqHeader.NAME);
		// JSR 289 Section 6.2.1 Point 2 of rules governing the state of SipSession
		// In general, whenever a non-dialog creating request is sent or received, 
		// the SipSession state remains unchanged. Similarly, a response received 
		// for a non-dialog creating request also leaves the SipSession state unchanged. 
		// The exception to the general rule is that it does not apply to requests (e.g. BYE, CANCEL) 
		// that are dialog terminating according to the appropriate RFC rules relating to the kind of dialog.		
		if(!JainSipUtils.dialogCreatingMethods.contains(cSeqHeader.getMethod()) &&
				!JainSipUtils.dialogTerminatingMethods.contains(cSeqHeader.getMethod())) {
			return;
		}
		// Mapping to the sip session state machine (proxy is covered here too)
		if( (State.INITIAL.equals(state) || State.EARLY.equals(state)) && 
				response.getStatus() >= 200 && response.getStatus() < 300 && 
				!JainSipUtils.dialogTerminatingMethods.contains(cSeqHeader.getMethod())) {
			this.setState(State.CONFIRMED);
			if(logger.isDebugEnabled()) {
				logger.debug("the following sip session " + getKey() + " has its state updated to " + getState());
			}
		}
		// Mapping to the sip session state machine
		if( State.INITIAL.equals(state) && response.getStatus() >= 100 && response.getStatus() < 200 ) {
			this.setState(State.EARLY);
			if(logger.isDebugEnabled()) {
				logger.debug("the following sip session " + getKey() + " has its state updated to " + getState());
			}
		}		
		if( (State.INITIAL.equals(state) || State.EARLY.equals(state)) && 
				response.getStatus() >= 300 && response.getStatus() < 700 &&
				JainSipUtils.dialogCreatingMethods.contains(cSeqHeader.getMethod()) && 
				!JainSipUtils.dialogTerminatingMethods.contains(cSeqHeader.getMethod())) {
			// If the servlet acts as a UAC and sends a dialog creating request, 
			// then the SipSession state tracks directly the SIP dialog state except 
			// that non-2XX final responses received in the EARLY or INITIAL states 
			// cause the SipSession state to return to the INITIAL state rather than going to TERMINATED.
			// +
			// If the servlet acts as a proxy for a dialog creating request then 
			// the SipSession state tracks the SIP dialog state except that non-2XX 
			// final responses received from downstream in the EARLY or INITIAL states 
			// cause the SipSession state to return to INITIAL rather than going to TERMINATED.
			if(receive) {
				setState(State.INITIAL);
				if(logger.isDebugEnabled()) {
					logger.debug("the following sip session " + getKey() + " has its state updated to " + getState());
				}
			} 
			// If the servlet acts as a UAS and receives a dialog creating request, 
			// then the SipSession state directly tracks the SIP dialog state. 
			// Unlike a UAC, a non-2XX final response sent by the UAS in the EARLY or INITIAL 
			// states causes the SipSession state to go directly to the TERMINATED state.
			// +
			// This enables proxy servlets to proxy requests to additional destinations 
			// when called by the container in the doResponse() method for a tentative 
			// non-2XX best response. 
			// After all such additional proxy branches have been responded to and after 
			// considering any servlet created responses, the container eventually arrives at 
			// the overall best response and forwards this response upstream. 
			// If this best response is a non-2XX final response, then when the forwarding takes place, 
			// the state of the SipSession object becomes TERMINATED.
			else {
				setState(State.TERMINATED);
				if(logger.isDebugEnabled()) {
					logger.debug("the following sip session " + getKey() + " has its state updated to " + getState());
				}
			}						
		}								 				
	}
	
	/**
	 * Update the sip session state upon sending/receiving a subsequent request
	 * Covers JSR 289 Section 6.2.1 along with updateStateOnResponse method
	 * @param request the subsequent request received/to send
	 * @param receive true if the subsequent request has been received, false if it is to be sent.
	 */
    public void updateStateOnSubsequentRequest(
			SipServletRequestImpl request, boolean receive) {
		if(JainSipUtils.dialogTerminatingMethods.contains(request.getMethod())) {			
			this.setState(State.TERMINATED);
			if(logger.isDebugEnabled()) {
				logger.debug("the following sip session " + getKey() + " has its state updated to " + getState());
			}
		}
		
	}

	/**
	 * @return the key
	 */
	public SipSessionKey getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(SipSessionKey key) {
		this.key = key;
	}

	public ProxyBranchImpl getProxyBranch() {
		return proxyBranch;
	}

	public void setProxyBranch(ProxyBranchImpl proxyBranch) {
		this.proxyBranch = proxyBranch;
	}
	
	/**
     * Perform the internal processing required to passivate
     * this session.
     */
    public void passivate() {
        // Notify ActivationListeners
    	SipSessionEvent event = null;
        Set<String> keySet = sipSessionAttributeMap.keySet();
        for (String key : keySet) {
        	Object attribute = sipSessionAttributeMap.get(key);
            if (attribute instanceof SipSessionActivationListener) {
                if (event == null)
                    event = new SipSessionEvent(this);
                try {
                    ((SipSessionActivationListener)attribute)
                        .sessionWillPassivate(event);
                } catch (Throwable t) {
                    logger.error("SipSessionActivationListener threw exception", t);
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
    	SipSessionEvent event = null;
        Set<String> keySet = sipSessionAttributeMap.keySet();
        for (String key : keySet) {
        	Object attribute = sipSessionAttributeMap.get(key);
            if (attribute instanceof SipSessionActivationListener) {
                if (event == null)
                    event = new SipSessionEvent(this);
                try {
                    ((SipSessionActivationListener)attribute)
                        .sessionDidActivate(event);
                } catch (Throwable t) {
                    logger.error("SipSessionActivationListener threw exception", t);
                }
            }
		}
    }
    
	public Principal getUserPrincipal() {
		return userPrincipal;
	}
	
	public void setUserPrincipal(Principal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}	    
}
