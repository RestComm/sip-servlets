/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import gov.nist.javax.sip.ServerTransactionExt;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.message.MessageExt;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingEvent;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionEvent;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Parameters;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.mobicents.ha.javax.sip.SipLoadBalancer;
import org.mobicents.javax.servlet.sip.SipSessionAsynchronousWork;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.MobicentsSipServlet;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipListeners;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.b2bua.MobicentsB2BUAHelper;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletMessage;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxy;
import org.mobicents.servlet.sip.core.security.MobicentsSipSessionSecurity;
import org.mobicents.servlet.sip.core.security.SipPrincipal;
import org.mobicents.servlet.sip.message.B2buaHelperImpl;
import org.mobicents.servlet.sip.message.MobicentsSipSessionFacade;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.notification.SessionActivationNotificationCause;
import org.mobicents.servlet.sip.notification.SipSessionActivationEvent;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * 
 * <p>Implementation of the SipSession interface.
 * An instance of this sip session can only be retrieved through the Session Manager
 * (extended class from Tomcat's manager classes implementing the <code>Manager</code> interface)
 * to constrain the creation of sip session and to make sure that all sessions created
 * can be retrieved only through the session manager</p> 
 *
 * <p>
 * As a SipApplicationSession represents a dialog, 
 * the call id and from header URI, from tag, to Header (and to Tag to identify forked requests) 
 * are used as a unique key for a given SipSession instance. 
 * </p>
 *
 * @author vralev
 * @author mranga
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class SipSessionImpl implements MobicentsSipSession {
		
	private static final Logger logger = Logger.getLogger(SipSessionImpl.class);
	
	protected transient MobicentsSipApplicationSessionKey sipApplicationSessionKey;			
	//lazy loaded and not serialized
	protected transient MobicentsSipApplicationSession sipApplicationSession;
	
	protected ProxyImpl proxy;
	
	protected B2buaHelperImpl b2buaHelper;
	
	protected transient int requestsPending;

	volatile protected Map<String, Object> sipSessionAttributeMap;
	
	protected transient SipSessionKey key;
	
	protected transient SipPrincipal userPrincipal;
	
	protected long cseq = -1;
	
	protected String transport;
	
//	protected transient ThreadPoolExecutor executorService = new ThreadPoolExecutor(1, 1, 90, TimeUnit.SECONDS,
//			new LinkedBlockingQueue<Runnable>());
	
	/**
	 * Creation time.
	 */
	protected long creationTime;
	
	/**
	 * Last access time.
	 */
	protected long lastAccessedTime;
	
	/**
	 * Routing region per session/dialog.
	 */
	protected transient SipApplicationRoutingRegion routingRegion;
	
	/**
	 * AR state info
	 */
	protected transient Serializable stateInfo;
	
	/**
	 * AR router info for the next app in chain
	 */
	protected transient SipApplicationRouterInfo nextSipApplicationRouterInfo;
	
	/**
	 * Current state of the session, one of INTITIAL, EARLY, ESTABLISHED and TERMINATED.
	 */
	protected State state;
	
	/**
	 * Is the session valid.
	 */
	protected AtomicBoolean isValidInternal;
	
	protected transient boolean isValid;
	
	/**
	 * The name of the servlet withing this same app to handle all subsequent requests.
	 */
	protected String handlerServlet;
		
	/**
	 * Subscriber URI should be set for outbound sessions, from requests created in the container.
	 */
	protected transient String subscriberURI;
	
	/**
	 * Outbound interface is one of the allowed values in the Servlet Context attribute
	 * "javax.servlet.ip.outboundinterfaces"
	 * This one is not serialized, it has to be reset by the app on sessionActivated listener method
	 */
	protected transient String outboundInterface;
	
	
	// === THESE ARE THE OBJECTS A SIP SESSION CAN BE ASSIGNED TO ===
	// TODO: Refactor this into two Session classes to avoid nulls
	// and branching on nulls
	/**
	 * We use this for dialog-related requests. In this case the dialog
	 * directly corresponds to the session.
	 */
	protected transient Dialog sessionCreatingDialog;
	
	/**
	 * We use this for REGISTER or MESSAGE, where a dialog doesn't exist to carry the session info.
	 * In this case the session only spans a single transaction.
	 */
	protected transient SipServletRequestImpl sessionCreatingTransactionRequest;
	protected transient boolean isSessionCreatingTransactionServer;
	// =============================================================
		
	// TODO : Can be optimized into separate server tx and client tx to speed up some parts of the code
	protected transient Set<Transaction> ongoingTransactions;
	
	volatile protected transient ConcurrentHashMap<String, MobicentsSipSession> derivedSipSessions;

	/*
	 * The almighty provider
	 */
	protected transient SipFactoryImpl sipFactory;
	
	protected boolean invalidateWhenReady = true;
	
	protected boolean readyToInvalidate = false;

	/*
	 * If this is a derived session, have a pointer to the parent session.
	 */
	protected transient MobicentsSipSession parentSession = null;
	
	//parties used when this is an outgoing request that has not yet been sent 
	protected transient Address localParty = null;
	protected transient Address remoteParty = null;
	
	//Subscriptions used for RFC 3265 compliance to be able to determine when the session can be invalidated
	//  A subscription is destroyed when a notifier sends a NOTIFY request with a "Subscription-State" of "terminated".
	// If a subscription's destruction leaves no other application state associated with the dialog, the dialog terminates
	volatile protected transient Set<EventHeader> subscriptions = null;
	//original transaction that started this session is stored so that we know if the session should end when all subscriptions have terminated or when the BYE has come
	protected transient String originalMethod = null;
	protected transient boolean okToByeSentOrReceived = false;
	// Issue 2066 Miss Record-Route in Response To Subsequent Requests for non RFC3261 compliant servers
	protected transient boolean copyRecordRouteHeadersOnSubsequentResponses = false;
	
	protected transient Semaphore semaphore;
	
	protected transient MobicentsSipSessionFacade facade = null;
	
	protected transient ConcurrentHashMap<Long, Boolean> acksReceived = new ConcurrentHashMap<Long, Boolean>(2);
	// Added for Issue 2173 http://code.google.com/p/mobicents/issues/detail?id=2173
    // Handle Header [Authentication-Info: nextnonce="xyz"] in sip authorization responses
	protected transient MobicentsSipSessionSecurity sipSessionSecurity;
	
	/**
	 * This is the flow for this session, i.e., this uri represents the 
	 * remote destination from where the request actually was received on.
	 * We will use this this "flow" whenever we send out a subsequent
	 * request to this destination. See RFC5626 for details.
	 * 
	 * TODO: in a fail-over scenario, should we keep this uri? If e.g. this
	 * flow is a TCP connection, that connection will of course not be
	 * failed over to the other server, which then would cause us to not (depending
	 * on the NAT) be able to connect to the remote client anyway. On the other hand
	 * if this is a nice NAT we will be able to get through so at least we have
	 * a chance of reconnecting as opposed to having the complete wrong address.
	 * So for now, I'll keep the flow around.
	 */
	private javax.sip.address.SipURI flow;

	
	protected SipSessionImpl (SipSessionKey key, SipFactoryImpl sipFactoryImpl, MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		this.key = key;
		setSipApplicationSession(mobicentsSipApplicationSession);
		this.sipFactory = sipFactoryImpl;
		this.creationTime = this.lastAccessedTime = System.currentTimeMillis();		
		this.state = State.INITIAL;
		this.isValidInternal = new AtomicBoolean(true);
		this.isValid = true;
		this.ongoingTransactions = new CopyOnWriteArraySet<Transaction>();
		if(mobicentsSipApplicationSession.getSipContext() != null && ConcurrencyControlMode.SipSession.equals(mobicentsSipApplicationSession.getSipContext().getConcurrencyControlMode())) {
			semaphore = new Semaphore(1);		
		}		
	}
	/**
	 * Notifies the listeners that a lifecycle event occured on that sip session 
	 * @param sipSessionEventType the type of event that happened
	 */
	public void notifySipSessionListeners(SipSessionEventType sipSessionEventType) {
		MobicentsSipApplicationSession sipApplicationSession = getSipApplicationSession();
		if(sipApplicationSession != null) {
			SipContext sipContext = sipApplicationSession.getSipContext(); 							
			List<SipSessionListener> sipSessionListeners = 
				sipContext.getListeners().getSipSessionListeners();		
			if(sipSessionListeners.size() > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("notifying sip session listeners of context " + sipContext.getApplicationName() + " of following event " +
							sipSessionEventType);
				}
				ClassLoader oldLoader = java.lang.Thread.currentThread().getContextClassLoader();
				java.lang.Thread.currentThread().setContextClassLoader(sipContext.getSipContextClassLoader());
				// http://code.google.com/p/sipservlets/issues/detail?id=135
				sipContext.bindThreadBindingListener();
				SipSessionEvent sipSessionEvent = new SipSessionEvent(this.getFacade());
				for (SipSessionListener sipSessionListener : sipSessionListeners) {
					try {
						if(logger.isDebugEnabled()) {
							logger.debug("notifying sip session listener " + sipSessionListener.getClass().getName() + " of context " + 
									key.getApplicationName() + " of following event " + sipSessionEventType);
						}
						if(SipSessionEventType.CREATION.equals(sipSessionEventType)) {
							sipSessionListener.sessionCreated(sipSessionEvent);
						} else if (SipSessionEventType.DELETION.equals(sipSessionEventType)) {
							sipSessionListener.sessionDestroyed(sipSessionEvent);
						} else if (SipSessionEventType.READYTOINVALIDATE.equals(sipSessionEventType)) {
							sipSessionListener.sessionReadyToInvalidate(sipSessionEvent);
						}
					} catch (Throwable t) {
						logger.error("SipSessionListener threw exception", t);
					}
				}
				// http://code.google.com/p/sipservlets/issues/detail?id=135
				sipContext.unbindThreadBindingListener();
				java.lang.Thread.currentThread().setContextClassLoader(oldLoader);
			}		
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#createRequest(java.lang.String)
	 */
	public SipServletRequest createRequest(final String method) {
		if(method.equalsIgnoreCase(Request.ACK) || method.equalsIgnoreCase(Request.PRACK)
				|| method.equalsIgnoreCase(Request.CANCEL)) {
			throw new IllegalArgumentException(
					"Can not create ACK, PRACK or CANCEL requests with this method");
		}
		if(!isValid()) {
			throw new IllegalStateException("cannot create a subsequent request " + method + " because the session " + key + " is invalid");
		}
		if(State.TERMINATED.equals(state)) {
			throw new IllegalStateException("cannot create a subsequent request " + method + " because the session " + key + " is in TERMINATED state");
		}
		// Issue 2440 : http://code.google.com/p/mobicents/issues/detail?id=2440
		// SipSession.createRequest on proxy SipSession does not throw IllegalStateException
		if(proxy != null) {
			throw new IllegalStateException("cannot create a subsequent request " + method + " because the session " + key + " has been used for proxying. See JSR289 Section 6.2.2.1.");
		}
//		if((State.INITIAL.equals(state) && hasOngoingTransaction())) {
//			throw new IllegalStateException("cannot create a request because the session is in INITIAL state with ongoing transactions");
//		}
		if(logger.isDebugEnabled()) {
			logger.debug("dialog associated with this session to create the new request " + method + " within that dialog "+
					sessionCreatingDialog);
			if(sessionCreatingDialog != null) {
				logger.debug("dialog state " + sessionCreatingDialog.getState() + " for that dialog "+ sessionCreatingDialog);
			}
		}
		SipServletRequestImpl sipServletRequest = null;
		// Fix for Issue http://code.google.com/p/mobicents/issues/detail?id=2230 BYE is routed to unexpected IP
		// MSS should throw an IllegalStateException when a subsequent request is being created on a TERMINATED dialog
		// don't do it on the BYE method as a 408 within a dialog could have make the dialog TERMINATED and MSS should allow the app to create the subsequent BYE from any thread
		if(sessionCreatingDialog != null && DialogState.TERMINATED.equals(sessionCreatingDialog.getState()) && !method.equalsIgnoreCase(Request.BYE)) {
			// don't do it for authentication as the dialog will go back to TERMINATED state, so we should allow to create challenge requests
			if(sessionCreatingTransactionRequest == null || sessionCreatingTransactionRequest.getLastFinalResponse() == null ||
					(sessionCreatingTransactionRequest.getLastFinalResponse().getStatus() != 401 && sessionCreatingTransactionRequest.getLastFinalResponse().getStatus() != 407)) {				
				throw new IllegalStateException("cannot create a subsequent request " + method + " because the dialog " + sessionCreatingDialog + " for session " + key + " is in TERMINATED state");
			}
		}
		// Issue http://code.google.com/p/mobicents/issues/detail?id=2230 BYE is routed to unexpected IP BYE can be sent on a TERMINATED Dialog
		if(this.sessionCreatingDialog != null && 
				(!DialogState.TERMINATED.equals(sessionCreatingDialog.getState()) || 
						(DialogState.TERMINATED.equals(sessionCreatingDialog.getState()) && method.equalsIgnoreCase(Request.BYE)))) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("dialog " + sessionCreatingDialog + " used to create the new request " + method);			
			}
			try {
				final Request methodRequest = this.sessionCreatingDialog.createRequest(method);

				if(methodRequest.getHeader(ContactHeader.NAME) != null) {
					// if a sip load balancer is present in front of the server, the contact header is the one from the sip lb
					// so that the subsequent requests can be failed over
					try {
						ContactHeader contactHeader = null;
						FromHeader from = (FromHeader) methodRequest.getHeader(FromHeader.NAME);
						String displayName = from.getAddress().getDisplayName();
						String userName = null;
						javax.sip.address.URI uri = from.getAddress().getURI();
						if(uri.isSipURI()) {
							userName = ((javax.sip.address.SipURI)uri).getUser();
						}
						if(sipFactory.isUseLoadBalancer()) {
							SipLoadBalancer loadBalancerToUse = sipFactory.getLoadBalancerToUse();
							javax.sip.address.SipURI sipURI = SipFactoryImpl.addressFactory.createSipURI(userName, loadBalancerToUse.getAddress().getHostAddress());
							sipURI.setHost(loadBalancerToUse.getAddress().getHostAddress());
							sipURI.setPort(loadBalancerToUse.getSipPort());

							// TODO: Is this enough or we must specify the transport somewhere?
							// We can leave it like this. It will be updated if needed in the send() method
							sipURI.setTransportParam(ListeningPoint.UDP);
							
							javax.sip.address.Address contactAddress = SipFactoryImpl.addressFactory.createAddress(sipURI);
							contactHeader = SipFactoryImpl.headerFactory.createContactHeader(contactAddress);													
						} else {											

							contactHeader = JainSipUtils.createContactHeader(sipFactory.getSipNetworkInterfaceManager(), methodRequest, displayName, userName, outboundInterface);
						}
						methodRequest.setHeader(contactHeader);
					} catch (Exception e) {
						logger.error("Can not create contact header for subsequent request " + method + " for session " + key, e);
					}
				}
				// Fix for Issue 1130 (http://code.google.com/p/mobicents/issues/detail?id=1130) : 
				// NullPointerException when sending request to client which support both UDP and TCP transport
				// before removing the via header we store the transport into its app data
				ListIterator<ViaHeader> viaHeaders = methodRequest.getHeaders(ViaHeader.NAME);				
				if(viaHeaders != null && viaHeaders.hasNext()) {
					ViaHeader viaHeader = viaHeaders.next();
					((MessageExt)methodRequest).setApplicationData(viaHeader.getTransport());
				}
				//Issue 112 fix by folsson
				methodRequest.removeHeader(ViaHeader.NAME);
				
				//if a SUBSCRIBE or BYE is sent for exemple, it will reuse the prexisiting dialog
				sipServletRequest = (SipServletRequestImpl) sipFactory.getMobicentsSipServletMessageFactory().createSipServletRequest(
						methodRequest, this, null, sessionCreatingDialog,
						false);
			} catch (SipException e) {
				logger.error("Cannot create the " + method + " request from the dialog " + sessionCreatingDialog,e);
				throw new IllegalArgumentException("Cannot create the " + method + " request from the dialog " + sessionCreatingDialog + " for sip session " + key,e);
			} 	
		} else {			
			//case where other requests are sent with the same session like REGISTER or for challenge requests
			if(sessionCreatingTransactionRequest != null) {
				if(!isSessionCreatingTransactionServer) {
					if(logger.isDebugEnabled()) {
						logger.debug("orignal tx for creating susbequent request " + method + " on session " + key +" was a Client Tx");
					}
					Request request = (Request) sessionCreatingTransactionRequest.getMessage().clone();
					// Issue 1524 :	Caused by: java.text.ParseException: CSEQ method mismatch with Request-Line
					javax.sip.address.URI requestUri = (javax.sip.address.URI) request.getRequestURI().clone();					
					try {
						request.setMethod(method);
					} catch (ParseException e) {
						throw new IllegalArgumentException("Unexpected exception happened on setting method " + method, e);
					}
					request.setRequestURI(requestUri);
					((MessageExt)request).setApplicationData(null);
					
					final CSeqHeader cSeqHeader = (CSeqHeader) request.getHeader((CSeqHeader.NAME));					
					try {
						cSeqHeader.setSeqNumber(cSeqHeader.getSeqNumber() + 1l);
						cSeqHeader.setMethod(method);
					} catch (InvalidArgumentException e) {
						logger.error("Cannot increment the Cseq header to the new " + method + " on the susbequent request to create on session " + key,e);
						throw new IllegalArgumentException("Cannot create the " + method + " on the susbequent request to create on session " + key,e);				
					} catch (ParseException e) {
						throw new IllegalArgumentException("Cannot set the " + method + " on the susbequent request to create on session " + key,e);		
					}
					// Fix for Issue 1130 (http://code.google.com/p/mobicents/issues/detail?id=1130) : 
					// NullPointerException when sending request to client which support both UDP and TCP transport
					// before removing the ViaHeader we store the transport into its app data
					ListIterator<ViaHeader> viaHeaders = request.getHeaders(ViaHeader.NAME);				
					if(viaHeaders != null && viaHeaders.hasNext()) {
						ViaHeader viaHeader = viaHeaders.next();
						((MessageExt)request).setApplicationData(viaHeader.getTransport());
					}
					request.removeHeader(ViaHeader.NAME);
					
					final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipFactory.getSipNetworkInterfaceManager();
					final SipProvider sipProvider = sipNetworkInterfaceManager.findMatchingListeningPoint(
							JainSipUtils.findTransport(request), false).getSipProvider();
					final SipApplicationDispatcher sipApplicationDispatcher = sipFactory.getSipApplicationDispatcher();				
					final String branch = JainSipUtils.createBranch(getSipApplicationSession().getKey().getId(),  sipApplicationDispatcher.getHashFromApplicationName(getKey().getApplicationName()));
										
					ViaHeader viaHeader = JainSipUtils.createViaHeader(
		    				sipNetworkInterfaceManager, request, branch, outboundInterface);
		    		request.addHeader(viaHeader);
					
					sipServletRequest = (SipServletRequestImpl) sipFactory.getMobicentsSipServletMessageFactory().createSipServletRequest(
							request, this, null, sessionCreatingDialog,
							true);
					
					// Fix for Issue http://code.google.com/p/mobicents/issues/detail?id=2230 BYE is routed to unexpected IP
					if(sessionCreatingDialog != null && sessionCreatingDialog.getRemoteTarget() != null) {
						SipUri sipUri = (SipUri) sessionCreatingDialog.getRemoteTarget().getURI().clone();
						sipUri.clearUriParms();
						if(logger.isDebugEnabled()) {
							logger.debug("setting request uri to " + sipUri);
						}
						request.setRequestURI(sipUri);
					}
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("orignal tx for creating susbequent request " + method + " on session " + key +" was a Server Tx");
					}
					try {
						// copying original params and call id
						final Request originalRequest = (Request) sessionCreatingTransactionRequest.getMessage();
						final FromHeader fromHeader = (FromHeader) originalRequest.getHeader(FromHeader.NAME);
						final ToHeader toHeader = (ToHeader) originalRequest.getHeader(ToHeader.NAME);
						final AddressImpl currentLocalParty = (AddressImpl)this.getLocalParty().clone();
						final AddressImpl currentRemoteParty = (AddressImpl)this.getRemoteParty().clone();
						((Parameters)currentRemoteParty .getAddress().getURI()).removeParameter("tag");
						((Parameters)currentLocalParty .getAddress().getURI()).removeParameter("tag");
						final String originalCallId = ((CallIdHeader)originalRequest.getHeader(CallIdHeader.NAME)).getCallId();
						sipServletRequest =(SipServletRequestImpl) sipFactory.createRequest(
							getSipApplicationSession(),
							method,
							currentLocalParty,
							currentRemoteParty,
							handlerServlet,
							originalCallId,
							fromHeader.getTag());						
						final Request request = ((Request)sipServletRequest.getMessage());
						sipServletRequest.getSipSession().setCseq(((CSeqHeader)request.getHeader(CSeqHeader.NAME)).getSeqNumber());
						final Map<String, String> fromParameters = new HashMap<String, String>();
						final Iterator<String> fromParameterNames = fromHeader.getParameterNames();
						while (fromParameterNames.hasNext()) {
							String parameterName = (String) fromParameterNames.next();
							if(sessionCreatingDialog != null || !SipFactoryImpl.FORBIDDEN_PARAMS.contains(parameterName)) {
								fromParameters.put(parameterName, fromHeader.getParameter(parameterName));
							}
						}
						final Map<String, String> toParameters = new HashMap<String, String>();
						final Iterator<String> toParameterNames = toHeader.getParameterNames();
						while (toParameterNames.hasNext()) {
							String parameterName = (String) toParameterNames.next();
							if(sessionCreatingDialog != null || !SipFactoryImpl.FORBIDDEN_PARAMS.contains(parameterName)) {
								toParameters.put(parameterName, toHeader.getParameter(parameterName));
							}
						}			
					
						final ToHeader newTo = (ToHeader) request.getHeader(ToHeader.NAME);
						for (Entry<String, String> fromParameter : fromParameters.entrySet()) {
							String value = fromParameter.getValue();
							if(value == null) {
								value = "";
							}
							newTo.setParameter(fromParameter.getKey(),  value);
						}
						final FromHeader newFrom = (FromHeader) request.getHeader(FromHeader.NAME);
						for (Entry<String, String> toParameter : toParameters.entrySet()) {
							String value = toParameter.getValue();
							if(value == null) {
								value = "";
							}
							newFrom.setParameter(toParameter.getKey(),  value);
						}	
						// Fix for Issue http://code.google.com/p/mobicents/issues/detail?id=2230 BYE is routed to unexpected IP
						if(sessionCreatingDialog != null && sessionCreatingDialog.getRemoteTarget() != null) {
							SipUri sipUri = (SipUri) sessionCreatingDialog.getRemoteTarget().getURI().clone();
							sipUri.clearUriParms();
							if(logger.isDebugEnabled()) {
								logger.debug("setting request uri to " + sipUri);
							}
							request.setRequestURI(sipUri);
						}
					} catch (ParseException e) {
						throw new IllegalArgumentException("Problem setting param on the newly created susbequent request " + sipServletRequest,e);
					}					
				}

				if(sipSessionSecurity != null && sipSessionSecurity.getNextNonce() != null) {
					sipServletRequest.updateAuthorizationHeaders(true);
				}	
				else if (sipSessionSecurity != null) {
					sipServletRequest.updateAuthorizationHeaders(false);
				}
				
				return sipServletRequest;
			} else {
				String errorMessage = "Couldn't create the subsequent request " + method + " for this session " + key + ", isValid " + isValid() + ", session state " + state + " , sessionCreatingDialog = " + sessionCreatingDialog;
				if(sessionCreatingDialog != null) {
					errorMessage += " , dialog state " + sessionCreatingDialog.getState();
				}
				errorMessage += " , sessionCreatingTransactionRequest = " + sessionCreatingTransactionRequest;							
				throw new IllegalStateException(errorMessage);			
			}
		}
		//Application Routing :
		//removing the route headers and adding them back again except the one
		//corresponding to the app that is creating the subsequent request
		//avoid going through the same app that created the subsequent request
				
		Request request = (Request) sipServletRequest.getMessage();
		final ListIterator<RouteHeader> routeHeaders = request.getHeaders(RouteHeader.NAME);
		request.removeHeader(RouteHeader.NAME);
		while (routeHeaders.hasNext()) {
			RouteHeader routeHeader = routeHeaders.next();
			String routeAppNameHashed = ((javax.sip.address.SipURI)routeHeader .getAddress().getURI()).
				getParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME);
			String routeAppName = null;
			if(routeAppNameHashed != null) {
				routeAppName = sipFactory.getSipApplicationDispatcher().getApplicationNameFromHash(routeAppNameHashed);
			}
			if(routeAppName == null || !routeAppName.equals(getKey().getApplicationName())) {
				request.addHeader(routeHeader);
			}
		}
		
		if(sipSessionSecurity != null && sipSessionSecurity.getNextNonce() != null) {
			sipServletRequest.updateAuthorizationHeaders(true);
		}	
		else if (sipSessionSecurity != null) {
			sipServletRequest.updateAuthorizationHeaders(false);
		}	
		
		return sipServletRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {
		MobicentsSipApplicationSession sipApplicationSession = getSipApplicationSession();
		if(sipApplicationSession == null) {
			return null;
		} else {
			return sipApplicationSession.getFacade();
		}
	}
	
	// Does it need to be synchronized?
	protected Map<String, Object> getAttributeMap() {
		if(this.sipSessionAttributeMap == null) {
			this.sipSessionAttributeMap = new ConcurrentHashMap<String, Object>();
		}
		return this.sipSessionAttributeMap;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		if(!isValid()) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		return getAttributeMap().get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		if(!isValid()) {
			throw new IllegalStateException("SipApplicationSession already invalidated !");
		}
		Vector<String> names = new Vector<String>(getAttributeMap().keySet());
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
			return ((CallIdHeader)this.sessionCreatingTransactionRequest.getMessage().getHeader(CallIdHeader.NAME)).getCallId();
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
		return lastAccessedTime;
	}

	private void setLastAccessedTime(long lastAccessedTime) {
		this.lastAccessedTime= lastAccessedTime;
	}
	
	/**
     * Update the accessed time information for this session.  This method
     * should be called by the context when a request comes in for a particular
     * session, even if the application does not reference it.
     */
	public void access() {
		setLastAccessedTime(System.currentTimeMillis());
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getLocalParty()
	 */
	public Address getLocalParty() {
		if(sessionCreatingDialog != null) {
			return new AddressImpl(sessionCreatingDialog.getLocalParty(), null, ModifiableRule.NotModifiable);
		} else if (sessionCreatingTransactionRequest != null){
			if(isSessionCreatingTransactionServer) {
				ToHeader toHeader = (ToHeader) sessionCreatingTransactionRequest.getMessage().getHeader(ToHeader.NAME);
				return new AddressImpl(toHeader.getAddress(), AddressImpl.getParameters((Parameters)toHeader),  ModifiableRule.NotModifiable);
			} else {
				FromHeader fromHeader = (FromHeader)sessionCreatingTransactionRequest.getMessage().getHeader(FromHeader.NAME);
				return new AddressImpl(fromHeader.getAddress(), AddressImpl.getParameters((Parameters)fromHeader),  ModifiableRule.NotModifiable);
			}			
		} else {
			return localParty;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SipApplicationRoutingRegion getRegion() {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		if(routingRegion == null) {
			throw new IllegalStateException("This methos can be called only on initial requests");
		}
		return routingRegion;
	}

	/**
	 * {@inheritDoc}
	 */
	public SipApplicationRoutingRegion getRegionInternal() {		
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
			return new AddressImpl(sessionCreatingDialog.getRemoteParty(), null, ModifiableRule.NotModifiable);
		} else if (sessionCreatingTransactionRequest != null){
			try {
				if(!isSessionCreatingTransactionServer) {
					ToHeader toHeader = (ToHeader)sessionCreatingTransactionRequest.getMessage().getHeader(ToHeader.NAME);
					return new AddressImpl(toHeader.getAddress(), AddressImpl.getParameters((Parameters)toHeader),  ModifiableRule.NotModifiable);
				} else {
					FromHeader fromHeader = (FromHeader)sessionCreatingTransactionRequest.getMessage().getHeader(FromHeader.NAME);
					return new AddressImpl(fromHeader.getAddress(), AddressImpl.getParameters((Parameters)fromHeader),  ModifiableRule.NotModifiable);
				}
			} catch(Exception e) {
				throw new IllegalArgumentException("Error creating Address", e);
			}
		} else {
			return remoteParty;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#getState()
	 */
	public State getState() {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		return this.state;
	}

//	public ThreadPoolExecutor getExecutorService() {
//		return executorService;
//	}
	/**
	 * {@inheritDoc}
	 */
	public URI getSubscriberURI() {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		if (this.subscriberURI == null)
			throw new IllegalStateException("Subscriber URI is only available for outbound sessions.");
		else {		
			try {
				return sipFactory.createURI(subscriberURI);
			} catch (ServletParseException e) {
				throw new IllegalArgumentException("couldn't parse the outbound interface " + subscriberURI, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#invalidate()
	 */
	public void invalidate() {
		invalidate(false);
	}
	
	public void invalidate(boolean bypassCheck) {						
		boolean wasValid = isValidInternal.compareAndSet(true, false);
		if(!wasValid) {
			if(!bypassCheck) {
				throw new IllegalStateException("SipSession " + key + " already invalidated !");
			} else {
				if(logger.isInfoEnabled()) {
					logger.info("SipSession " + key + " already invalidated, doing nothing");					
				}
				return;
			}
		}
		
		if(logger.isInfoEnabled()) {
			logger.info("Invalidating the sip session " + key);
		}
				
		// No need for checks after JSR 289 PFD spec
		//checkInvalidation();
		if(sipSessionAttributeMap != null) {
			for (String key : sipSessionAttributeMap.keySet()) {
				removeAttribute(key, true);
			}
		}
		
		final MobicentsSipApplicationSession sipApplicationSession = getSipApplicationSession();
        SipManager manager = sipApplicationSession.getSipContext().getSipManager();
        // http://code.google.com/p/mobicents/issues/detail?id=2885
        // FQN Memory Leak in HA mode with PESSIMISTIC locking
        // remove it before the DELETION notification to avoid the sip application session to be destroyed before 
        // and leaking in the JBoss Cache		
		manager.removeSipSession(key);		
		sipApplicationSession.getSipContext().getSipSessionsUtil().removeCorrespondingSipSession(key);
		
		/*
         * Compute how long this session has been alive, and update
         * session manager's related properties accordingly
         */
        long timeNow = System.currentTimeMillis();
        int timeAlive = (int) ((timeNow - creationTime)/1000);        
        synchronized (manager) {
            if (timeAlive > manager.getSipSessionMaxAliveTime()) {
                manager.setSipSessionMaxAliveTime(timeAlive);
            }
            int numExpired = manager.getExpiredSipSessions();
            numExpired++;
            manager.setExpiredSipSessions(numExpired);
            int average = manager.getSipSessionAverageAliveTime();
            average = ((average * (numExpired-1)) + timeAlive)/numExpired;
            manager.setSipSessionAverageAliveTime(average);
        }
        
		notifySipSessionListeners(SipSessionEventType.DELETION);			
		
		isValid = false;
		
		if(derivedSipSessions != null) {
			for (MobicentsSipSession derivedMobicentsSipSession : derivedSipSessions.values()) {
				derivedMobicentsSipSession.invalidate();
			}		
			derivedSipSessions.clear();
		}	
		
		sipApplicationSession.onSipSessionReadyToInvalidate(this);
		if(ongoingTransactions != null) {
			if(logger.isDebugEnabled()) {
				logger.debug(ongoingTransactions.size() + " ongoing transactions still present in the following sip session " + key + " on invalidation");
			}
			for(Transaction transaction : ongoingTransactions) {
				if(!TransactionState.TERMINATED.equals(transaction.getState())) {
					if(transaction.getApplicationData() != null) {
						((TransactionApplicationData)transaction.getApplicationData()).cleanUp();
					}
					try {
						transaction.terminate();
					} catch (ObjectInUseException e) {
						// no worries about this one, we just try to eagerly  terminate the tx is the sip session has been forcefully invalidated
					}
				}
			}
			ongoingTransactions.clear();
		}
		if(subscriptions != null) {
			subscriptions.clear();
		}
		if(acksReceived != null) {
			acksReceived.clear();
		}
		if(sipSessionSecurity != null) {
			sipSessionSecurity.getCachedAuthInfos().clear();
		}
//		executorService.shutdown();
		parentSession = null;
		userPrincipal = null;		
//		executorService = null;
		// If the sip app session is nullified com.bea.sipservlet.tck.agents.api.javax_servlet_sip.B2buaHelperTest.testCreateResponseToOriginalRequest102 will fail
		// because it will try to get the B2BUAHelper after the session has been invalidated	
//		sipApplicationSession = null;
		manager = null;
		if(b2buaHelper != null) {
			b2buaHelper.unlinkSipSessionsInternal(this, false);
			b2buaHelper= null;	
		}
		derivedSipSessions = null;
		// not collecting it here to avoid race condition from 
		// http://code.google.com/p/mobicents/issues/detail?id=2130#c19
//		handlerServlet = null;
		localParty = null;
		ongoingTransactions = null;
		originalMethod = null;
		outboundInterface = null;
		sipSessionAttributeMap = null;
//		key = null;
		if(sessionCreatingDialog != null) {
			// terminating dialog to make sure there is not retention, if the app didn't send a BYE for invite tx by example
			if(!DialogState.TERMINATED.equals(sessionCreatingDialog.getState())) {
				sessionCreatingDialog.delete();
			}
//			sessionCreatingDialog.setApplicationData(null);
			sessionCreatingDialog = null;
		}
		if(sessionCreatingTransactionRequest != null) {
//			sessionCreatingTransaction.setApplicationData(null);
			Transaction sessionCreatingTransaction = sessionCreatingTransactionRequest.getTransaction();
			if(sessionCreatingTransaction != null) {
				// terminating transaction to make sure there is not retention
				if(!TransactionState.TERMINATED.equals(sessionCreatingTransaction.getState())) {
					try {
						sessionCreatingTransaction.terminate();
					} catch (ObjectInUseException e) {
						// never thrown by jain sip and anyway there is nothing we can do about it
					}
				}
			}
			sessionCreatingTransactionRequest.cleanUp();
			sessionCreatingTransactionRequest = null;
		}
		if(proxy != null && !proxy.getAckReceived()) {
			try {
				proxy.cancel();
			} catch (Exception e) {
				logger.debug("Problem cancelling proxy. We just try our best. This is not a critical error.", e);
			}
			proxy.getTransactionMap().clear();
			proxy.getProxyBranchesMap().clear();
			proxy = null;
		}
		remoteParty = null;
		routingRegion = null;
		sipFactory = null;
		state = null;
		stateInfo = null;
		subscriberURI = null;
		subscriptions = null;
		acksReceived = null;
		sipSessionSecurity = null;
		// don't release or nullify the semaphore, it should be done externally
		// see Issue http://code.google.com/p/mobicents/issues/detail?id=1294
//		if(semaphore != null) {
//			semaphore.release();
//			semaphore = null;
//		}
		facade = null;				
	}
	
	/**
	 * Not needed anymore after PFD JSR 289 spec
	 */
//	protected void checkInvalidation() {
//		if(state.equals(State.CONFIRMED)
//				|| state.equals(State.EARLY))
//			throw new IllegalStateException("Can not invalidate sip session in " + 
//					state.toString() + " state.");
//		if(isSupervisedMode() && hasOngoingTransaction()) {
//			dumpOngoingTransactions();
//			throw new IllegalStateException("Can not invalidate sip session with " +
//					ongoingTransactions.size() + " ongoing transactions in supervised mode.");
//		}
//	}

//	private void dumpOngoingTransactions() {
//		if(logger.isDebugEnabled()) {
//			logger.debug("ongoing transactions in sip the session " + key);
//		
//			for (Transaction transaction : ongoingTransactions) {
//				logger.debug("Transaction " + transaction + " : state = " + transaction.getState());
//			}
//		}
//	}

	/**
	 * Removed from the interface in PFD stage
	 * so making it protected
	 */
	protected boolean hasOngoingTransaction() {
		if(!isSupervisedMode()) {
			return false;
		} else {
			if(ongoingTransactions != null) {
				for (Transaction transaction : ongoingTransactions) {
					if(TransactionState.CALLING.equals(transaction.getState()) ||
						TransactionState.TRYING.equals(transaction.getState()) ||
						TransactionState.PROCEEDING.equals(transaction.getState()) ||
						TransactionState.COMPLETED.equals(transaction.getState()) ||
						TransactionState.CONFIRMED.equals(transaction.getState())) {
							return true;
					}
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
		return this.isValid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#isValidInternal()
	 */
	public boolean isValidInternal() {
		return isValidInternal.get();
	}
	
	/**
	 * @param isValid the isValid to set
	 */
	public void setValid(boolean isValid) {
		this.isValidInternal.set(isValid);
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		removeAttribute(name, false);
	}
	
	public void removeAttribute(String name, boolean byPassValidCheck) {

		if(!byPassValidCheck && !isValid())
			throw new IllegalStateException("Can not bind object to session that has been invalidated!!");
		
		if(name==null)
		//	throw new NullPointerException("Name of attribute to bind cant be null!!!");
			return;
		
		SipSessionBindingEvent event = null;
		Object value = getAttributeMap().get(name);
		// Call the valueUnbound() method if necessary
        if (value != null && value instanceof SipSessionBindingListener) {
        	event = new SipSessionBindingEvent(this, name);
            ((SipSessionBindingListener) value).valueUnbound(event);
        }
		
		this.getAttributeMap().remove(name);
		
		// Notifying Listeners of attribute removal	
		SipListeners sipListenersHolder = this.getSipApplicationSession().getSipContext().getListeners();		
		List<SipSessionAttributeListener> listenersList = sipListenersHolder.getSipSessionAttributeListeners();
		if(listenersList.size() > 0) {
			if(event == null) {
				event = new SipSessionBindingEvent(this, name);
			}
			for (SipSessionAttributeListener listener : listenersList) {
				if(logger.isDebugEnabled()) {
					logger.debug("notifying SipSessionAttributeListener " + listener.getClass().getCanonicalName() + " of attribute removed on key "+ key);
				}
				try{
					listener.attributeRemoved(event);
				} catch (Throwable t) {
					logger.error("SipSessionAttributeListener threw exception", t);
				}
			}
		}
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
		
		// Construct an event with the new value
		SipSessionBindingEvent event = null;

        // Call the valueBound() method if necessary
        if (attribute instanceof SipSessionBindingListener) {        	
            // Don't call any notification if replacing with the same value
            Object oldValue = getAttributeMap().get(key);
            if (attribute != oldValue) {
            	event = new SipSessionBindingEvent(this, key);
                try {
                    ((SipSessionBindingListener) attribute).valueBound(event);
                } catch (Throwable t){
                	logger.error("SipSessionBindingListener threw exception", t); 
                }
            }
        }
		
		Object previousValue = this.getAttributeMap().put(key, attribute);
		
		if (previousValue != null && previousValue != attribute &&
	            previousValue instanceof SipSessionBindingListener) {
            try {
                ((SipSessionBindingListener) previousValue).valueUnbound
                    (new SipSessionBindingEvent(this, key));
            } catch (Throwable t) {
            	logger.error("SipSessionBindingListener threw exception", t);
            }
        }
		
		// Notifying Listeners of attribute addition or modification		
		SipListeners sipListenersHolder = this.getSipApplicationSession().getSipContext().getListeners();
		List<SipSessionAttributeListener> listenersList = sipListenersHolder.getSipSessionAttributeListeners();
		if(listenersList.size() > 0) {
			if(event == null) {
				event = new SipSessionBindingEvent(this, key);	
			}
			if (previousValue == null) {
				// This is initial, we need to send value bound event
				for (SipSessionAttributeListener listener : listenersList) {
					if(logger.isDebugEnabled()) {
						logger.debug("notifying SipSessionAttributeListener " + listener.getClass().getCanonicalName() + " of attribute added on key "+ key);
					}
					try {
						listener.attributeAdded(event);
					} catch (Throwable t) {
						logger.error("SipSessionAttributeListener threw exception", t);
					}
				}
			} else {
				for (SipSessionAttributeListener listener : listenersList) {
					if(logger.isDebugEnabled()) {
						logger.debug("notifying SipSessionAttributeListener " + listener.getClass().getCanonicalName() + " of attribute replaced on key "+ key);
					}
					try {
						listener.attributeReplaced(event);
					} catch (Throwable t) {
						logger.error("SipSessionAttributeListener threw exception", t);
					}
				}
			}		
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#setHandler(java.lang.String)
	 */
	public void setHandler(String name) throws ServletException {
		if(!isValid()) {
			throw new IllegalStateException("the session has already been invalidated, no handler can be set on it anymore !");
		}
		if(name != null && name.equals(handlerServlet)) {
			return ;
		}
		SipContext sipContext = getSipApplicationSession().getSipContext();
		MobicentsSipServlet container = sipContext.findSipServletByName(name);		
		
		if(container == null && sipContext.getSipRubyController() == null) {
			throw new ServletException("the sip servlet with the name "+ name + 
					" doesn't exist in the sip application " + sipContext.getApplicationName());
		}		
		this.handlerServlet = name;
		getSipApplicationSession().setCurrentRequestHandler(handlerServlet);
		if(logger.isDebugEnabled()) {
			if(name !=null) {
				logger.debug("Session Handler for application " + getKey().getApplicationName() + " set to " + handlerServlet + " on sip session " + key);
			} else {
				logger.debug("Session Handler for application " + getKey().getApplicationName() + " set to " + sipContext.getSipRubyController() + " on sip session " + key);
			}
		}
	}
	
	/**
	 * Retrieve the handler associated with this sip session
	 * @return the handler associated with this sip session
	 */
	public String getHandler() {
		return handlerServlet;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setSessionCreatingDialog(Dialog dialog) {
		if(proxy == null) {
			this.sessionCreatingDialog = dialog;
			if(logger.isDebugEnabled()) {
				logger.debug("setting session creating dialog for this session to " + dialog);
				if(dialog != null) {
					logger.debug("session creating dialog dialogId " + dialog.getDialogId());
				}
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("not setting session creating dialog for this session to " + dialog + " since this is a proxy application");
				if(dialog != null) {
					logger.debug("not setting session creating dialog with dialogId " + dialog.getDialogId() + " since this is a proxy application");
				}
			}
		}
	}

	/**
	 * @return the dialog
	 */
	public Dialog getSessionCreatingDialog() {
		return sessionCreatingDialog;
	}

	public MobicentsSipApplicationSession getSipApplicationSession() {
		if(sipApplicationSession == null) {
			final String applicationName = key.getApplicationName(); 
			final SipContext sipContext = sipFactory.getSipApplicationDispatcher().findSipApplication(applicationName);
			if(sipContext != null) {
				sipApplicationSession = sipContext.getSipManager().getSipApplicationSession(sipApplicationSessionKey, false);
			}
		} 
		return sipApplicationSession;
	}

	protected void setSipApplicationSession(
			MobicentsSipApplicationSession sipApplicationSession) {		
		if (sipApplicationSession != null) {			
			this.sipApplicationSessionKey = sipApplicationSession.getKey();
			sipApplicationSession.addSipSession(this);			
		}
	}

	public SipServletRequestImpl getSessionCreatingTransactionRequest() {
		return sessionCreatingTransactionRequest;
	}	

	/**
	 * @param sessionCreatingTransaction the sessionCreatingTransaction to set
	 */
	public void setSessionCreatingTransactionRequest(MobicentsSipServletMessage message) {		
		if(message != null) {
			if(message instanceof SipServletRequestImpl) {		
				this.sessionCreatingTransactionRequest = (SipServletRequestImpl) message;
				this.isSessionCreatingTransactionServer = message.getTransaction() instanceof ServerTransaction;
			} else if(message.getTransaction() != null && message.getTransaction().getApplicationData() != null) {
				SipServletMessageImpl sipServletMessageImpl = ((TransactionApplicationData)message.getTransaction().getApplicationData()).getSipServletMessage();
				if(sipServletMessageImpl != null && sipServletMessageImpl instanceof SipServletRequestImpl) {		
					this.sessionCreatingTransactionRequest = (SipServletRequestImpl) sipServletMessageImpl;
					this.isSessionCreatingTransactionServer = message.getTransaction() instanceof ServerTransaction;
				}
			}
		}
		if(sessionCreatingTransactionRequest != null) {
			if(originalMethod == null) {
				originalMethod = sessionCreatingTransactionRequest.getMethod();
			}		
			addOngoingTransaction(sessionCreatingTransactionRequest.getTransaction());
			// Issue 906 : CSeq is not increased correctly for REGISTER requests if registrar requires authentication.
			// http://code.google.com/p/mobicents/issues/detail?id=906
			// we update the parent session for the REGISTER so that the CSeq is correctly increased
			// if the session is stored
			if(parentSession != null && Request.REGISTER.equals(originalMethod)) {
				if(!parentSession.equals(this)) {
					parentSession.setSessionCreatingTransactionRequest(message);
				}
			}
		}
	}
	
	public boolean isSupervisedMode() {
		if(proxy == null) {
			return true;
		} else {
			return this.proxy.getSupervised();
		}
	}

	public void setSipSubscriberURI(String subscriberURI) {
		this.subscriberURI = subscriberURI;
	}
	
	public String getSipSubscriberURI() {
		return subscriberURI;
	}

	public String getOutboundInterface() {
		return outboundInterface;
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
	
	public void onTerminatedState() {
		if(isValidInternal()) {
			onReadyToInvalidate();
			if(this.parentSession != null) {
				Iterator<MobicentsSipSession> derivedSessionsIterator = parentSession.getDerivedSipSessions();
				while (derivedSessionsIterator.hasNext()) {
					MobicentsSipSession mobicentsSipSession = (MobicentsSipSession) derivedSessionsIterator
							.next();
					if(mobicentsSipSession.isValidInternal() && !mobicentsSipSession.isReadyToInvalidate()) {
						return;
					}
				}					
				this.parentSession.onReadyToInvalidate();
			}
		}
	}

	/**
	 * Add an ongoing tx to the session.
	 */
	public void addOngoingTransaction(Transaction transaction) {
		
		if(transaction != null && ongoingTransactions != null  && !isReadyToInvalidate() ) { 
			boolean added = this.ongoingTransactions.add(transaction);
			if(added) {
				if(logger.isDebugEnabled()) {
					logger.debug("transaction "+ transaction +" has been added to sip session's ongoingTransactions" );
				}
				setReadyToInvalidate(false);
			}
		}
	}
	
	/**
	 * Remove an ongoing tx to the session.
	 */
	public void removeOngoingTransaction(Transaction transaction) {

		boolean removed = false;
		if(this.ongoingTransactions != null) {
			removed = this.ongoingTransactions.remove(transaction);
		}
		
//		if(sessionCreatingTransactionRequest != null && sessionCreatingTransactionRequest.getMessage() != null && JainSipUtils.DIALOG_CREATING_METHODS.contains(sessionCreatingTransactionRequest.getMethod())) {
//			sessionCreatingTransactionRequest = null;
//		}
				
		if(sessionCreatingTransactionRequest != null && 
				sessionCreatingTransactionRequest.getTransaction()!= null && 
				sessionCreatingTransactionRequest.getTransaction().equals(transaction)) {
			sessionCreatingTransactionRequest.cleanUp();
		}
			
		if(logger.isDebugEnabled()) {
			logger.debug("transaction "+ transaction +" has been removed from sip session's ongoingTransactions ? " + removed );
		}	
		
		updateReadyToInvalidate(transaction);
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
	public void updateStateOnResponse(MobicentsSipServletResponse response, boolean receive) {
		final String method = response.getMethod();
		
		if(sipSessionSecurity != null && response.getStatus() >= 200 && response.getStatus() < 300) {
			// Issue 2173 http://code.google.com/p/mobicents/issues/detail?id=2173
			// it means some credentials were cached need to check if we need to store the nextnonce if the response have one
			AuthenticationInfoHeader authenticationInfoHeader = (AuthenticationInfoHeader)response.getMessage().getHeader(AuthenticationInfoHeader.NAME);
			if(authenticationInfoHeader != null) {
				String nextNonce = authenticationInfoHeader.getNextNonce();
				if(logger.isDebugEnabled()) {
					logger.debug("Storing nextNonce " + nextNonce + " for session " + key);
				}
				sipSessionSecurity.setNextNonce(nextNonce);			
			}
			
		}
		// JSR 289 Section 6.2.1 Point 2 of rules governing the state of SipSession
		// In general, whenever a non-dialog creating request is sent or received, 
		// the SipSession state remains unchanged. Similarly, a response received 
		// for a non-dialog creating request also leaves the SipSession state unchanged. 
		// The exception to the general rule is that it does not apply to requests (e.g. BYE, CANCEL) 
		// that are dialog terminating according to the appropriate RFC rules relating to the kind of dialog.		
		if(!JainSipUtils.DIALOG_CREATING_METHODS.contains(method) &&
				!JainSipUtils.DIALOG_TERMINATING_METHODS.contains(method)) {
			if(getSessionCreatingDialog() == null && proxy == null) {
				// Fix for issue http://code.google.com/p/mobicents/issues/detail?id=2116
				// avoid creating derived sessions for non dialogcreating requests
				if(logger.isDebugEnabled()) {
					logger.debug("resetting the to tag since a response to a non dialog creating and terminating method has been received for non proxy session with no dialog in state " + state);
				}
				key.setToTag(null, false);
			}
			return;
		}
		// Mapping to the sip session state machine (proxy is covered here too)
		if( (State.INITIAL.equals(state) || State.EARLY.equals(state)) && 
				response.getStatus() >= 200 && response.getStatus() < 300 && 
				!JainSipUtils.DIALOG_TERMINATING_METHODS.contains(method)) {
			this.setState(State.CONFIRMED);			
			if(this.proxy != null && response.getProxyBranch() != null && !response.getProxyBranch().getRecordRoute()) {
				// Section 6.2.4.1.2 Invalidate When Ready Mechanism :
		    	// "The container determines the SipSession to be in the ready-to-invalidate state under any of the following conditions:
		    	// 2. A SipSession transitions to the CONFIRMED state when it is acting as a non-record-routing proxy."
				setReadyToInvalidate(true);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
			}
		}
		// Mapping to the sip session state machine
		// We will transition from INITIAL to EARLY here for 100 Trying (not clear from the spec)
		// Figure 6-1 The SIP Dialog State Machine
		// and Figure 6-2 The SipSession State Machine
		if( State.INITIAL.equals(state) && response.getStatus() >= 100 && response.getStatus() < 200 ) {
			this.setState(State.EARLY);
			if(logger.isDebugEnabled()) {
				logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
			}
		}		
		if( (State.INITIAL.equals(state) || State.EARLY.equals(state)) && 
				response.getStatus() >= 300 && response.getStatus() < 700 &&
				JainSipUtils.DIALOG_CREATING_METHODS.contains(method) && 
				!JainSipUtils.DIALOG_TERMINATING_METHODS.contains(method)) {
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
				if(proxy == null) {
					// Fix for issue http://code.google.com/p/mobicents/issues/detail?id=2083
					if(logger.isDebugEnabled()) {
						logger.debug("resetting the to tag since a non 2xx response has been received for non proxy session in state " + state);
					}
					key.setToTag(null, false);
				}
				setState(State.INITIAL);				
//				readyToInvalidate = true; 
				if(logger.isDebugEnabled()) {
					logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
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
				setReadyToInvalidate(true);
				if(logger.isDebugEnabled()) {
					logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
				}
			}						
		}
		if(((State.CONFIRMED.equals(state) || State.TERMINATED.equals(state)) && response.getStatus() >= 200 && Request.BYE.equals(method))
				// http://code.google.com/p/mobicents/issues/detail?id=1438
				// Sip Session become TERMINATED after receiving 487 response to subsequent request => !confirmed clause added
				|| (!State.CONFIRMED.equals(state) && response.getStatus() == 487)) {
			boolean hasOngoingSubscriptions = false;
			if(subscriptions != null) {				
				if(subscriptions.size() > 0) {
					hasOngoingSubscriptions = true;
				}
				if(logger.isDebugEnabled()) {
					logger.debug("the following sip session " + getKey() + " has " + subscriptions.size() + " subscriptions");
				}
				if(!hasOngoingSubscriptions) {
					if(sessionCreatingDialog != null) {
						sessionCreatingDialog.delete();
					}
				}
			}				
			if(!hasOngoingSubscriptions) {
				if(getProxy() == null || response.getStatus() != 487) {
					setState(State.TERMINATED);
					setReadyToInvalidate(true);
					if(logger.isDebugEnabled()) {
						logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
						logger.debug("the following sip session " + getKey() + " is ready to be invalidated ");
					}
				}
			}
			if(logger.isDebugEnabled()) {
				logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
			}
			okToByeSentOrReceived = true;						
		}
		// we send the CANCEL only for 1xx responses
		if(response.getTransactionApplicationData().isCanceled() && response.getStatus() < 200 && !response.getMethod().equals(Request.CANCEL)) {
			SipServletRequestImpl request = (SipServletRequestImpl) response.getTransactionApplicationData().getSipServletMessage();
			if(logger.isDebugEnabled()) {
				logger.debug("request to cancel " + request + " routingstate " + request.getRoutingState() + 
						" requestCseq " + ((MessageExt)request.getMessage()).getCSeqHeader().getSeqNumber() + 
						" responseCseq " + ((MessageExt)response.getMessage()).getCSeqHeader().getSeqNumber());
			}
			if(!request.getRoutingState().equals(RoutingState.CANCELLED) && 
					((MessageExt)request.getMessage()).getCSeqHeader().getSeqNumber() == 
					((MessageExt)response.getMessage()).getCSeqHeader().getSeqNumber()) {
				if(response.getStatus() > 100) {
					request.setRoutingState(RoutingState.CANCELLED);
				}
				try {
					request.createCancel().send();					
				} catch (IOException e) {
					if(logger.isEnabledFor(Priority.WARN)) {
					logger.warn("Couldn't send CANCEL for a transaction that has been CANCELLED but " +
							"CANCEL was not sent because there was no response from the other side. We" +
							" just stopped the retransmissions." + response + "\nThe transaction" + 
							response.getTransaction(), e);
					}
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
			MobicentsSipServletRequest request, boolean receive) {

		//state updated to TERMINATED for CANCEL only if no final response had been received on the inviteTransaction
		if(((Request.CANCEL.equalsIgnoreCase(request.getMethod())))) {
			if(!(request.getTransaction() instanceof ServerTransactionExt)) {
				return;
			}
			final Transaction inviteTransaction = ((ServerTransactionExt) request.getTransaction()).getCanceledInviteTransaction();
			TransactionApplicationData inviteAppData = (TransactionApplicationData) 
				inviteTransaction.getApplicationData();			
			SipServletRequestImpl inviteRequest = (SipServletRequestImpl)inviteAppData.getSipServletMessage();
			// Issue 1484 : http://code.google.com/p/mobicents/issues/detail?id=1484
			// we terminate the session only for initial requests
			if((inviteRequest != null && inviteRequest.isInitial() && inviteRequest.getLastFinalResponse() == null) || 
						(proxy != null && proxy.getBestResponse() == null))  {
				this.setState(State.TERMINATED);
				if(logger.isDebugEnabled()) {
					logger.debug("the following sip session " + getKey() + " has its state updated to " + state);
				}
			}
		}
		
		if(Request.ACK.equalsIgnoreCase(request.getMethod())) {
			if(sessionCreatingTransactionRequest != null) {
				sessionCreatingTransactionRequest.cleanUpLastResponses();
			}
		}
		
	}
    
    
    private void updateReadyToInvalidate(Transaction transaction) {
    	// Section 6.2.4.1.2 Invalidate When Ready Mechanism :
    	// "The container determines the SipSession to be in the ready-to-invalidate state under any of the following conditions:
    	// 3. A SipSession acting as a UAC transitions from the EARLY state back to 
    	// the INITIAL state on account of receiving a non-2xx final response (6.2.1 Relationship to SIP Dialogs, point 4)
    	// and has not initiated any new requests (does not have any pending transactions)."
    	if(!readyToInvalidate && (ongoingTransactions == null || ongoingTransactions.isEmpty()) && 
    			transaction instanceof ClientTransaction && getProxy() == null && 
    			state != null && state.equals(State.INITIAL) && 
    			// Fix for Issue 1734
    			sessionCreatingTransactionRequest != null && 
    			sessionCreatingTransactionRequest.getLastFinalResponse() != null && 
    			sessionCreatingTransactionRequest.getLastFinalResponse().getStatus() >= 300) {
    		setReadyToInvalidate(true);
    	}
	}
    
    /**
     * This method is called immediately when the conditions for read to invalidate
     * session are met
     */
    public void onReadyToInvalidate() {
    	this.setReadyToInvalidate(true);
    	
    	if(logger.isDebugEnabled()) {
    		logger.debug("invalidateWhenReady flag is set to " + invalidateWhenReady);
    	}
    	
    	if(isValid() && this.invalidateWhenReady) {
    		this.notifySipSessionListeners(SipSessionEventType.READYTOINVALIDATE);
    		//If the application does not explicitly invalidate the session in the callback or has not defined a listener, 
        	//the container will invalidate the session. 
        	if(isValid()) {
        		invalidate(true);
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

	/**
	 * {@inheritDoc}
	 */
	public ProxyImpl getProxy() {
		return proxy;
	}
	/**
	 * {@inheritDoc}
	 */
	public void setProxy(MobicentsProxy proxy) {
		this.proxy = (ProxyImpl) proxy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setB2buaHelper(MobicentsB2BUAHelper helperImpl) {
		this.b2buaHelper = (B2buaHelperImpl) helperImpl;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public B2buaHelperImpl getB2buaHelper() {
		return this.b2buaHelper;
	}
	
	/**
     * Perform the internal processing required to passivate
     * this session.
     */
    public void passivate() {
        // Notify ActivationListeners
    	SipSessionEvent event = null;
    	if(this.sipSessionAttributeMap != null) {
	        Set<String> keySet = getAttributeMap().keySet();
	        for (String key : keySet) {
	        	Object attribute = getAttributeMap().get(key);
	            if (attribute instanceof SipSessionActivationListener) {
	                if (event == null)
	                	event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.PASSIVATION);
	                try {
	                    ((SipSessionActivationListener)attribute)
	                        .sessionWillPassivate(event);
	                } catch (Throwable t) {
	                    logger.error("SipSessionActivationListener threw exception", t);
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
    	SipSessionEvent event = null;
    	if(sipSessionAttributeMap != null) {
	        Set<String> keySet = getAttributeMap().keySet();
	        for (String key : keySet) {
	        	Object attribute = getAttributeMap().get(key);
	            if (attribute instanceof SipSessionActivationListener) {
	                if (event == null)
	                	event = new SipSessionActivationEvent(this, SessionActivationNotificationCause.ACTIVATION);
	                try {
	                    ((SipSessionActivationListener)attribute)
	                        .sessionDidActivate(event);
	                } catch (Throwable t) {
	                    logger.error("SipSessionActivationListener threw exception", t);
	                }
	            }
			}
	    }
    }
    
	public SipPrincipal getUserPrincipal() {
		return userPrincipal;
	}
	
	public void setUserPrincipal(SipPrincipal userPrincipal) {
		this.userPrincipal = userPrincipal;
	}
	
	public boolean getInvalidateWhenReady() {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		return invalidateWhenReady;
	}
	
	public boolean isReadyToInvalidate() {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		return readyToInvalidate;
	}
	
	/**
	 * @param readyToInvalidate the readyToInvalidate to set
	 */
	public void setReadyToInvalidate(boolean readyToInvalidate) {
		if(logger.isDebugEnabled()) {
    		logger.debug("readyToInvalidate flag is set to " + readyToInvalidate);
    	}
		this.readyToInvalidate = readyToInvalidate;
	}

	public boolean isReadyToInvalidateInternal() {
		return readyToInvalidate;
	}
	
	public void setInvalidateWhenReady(boolean arg0) {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		invalidateWhenReady = arg0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#setOutboundInterface(java.net.InetAddress)
	 */
	public void setOutboundInterface(InetAddress inetAddress) {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		if(inetAddress == null) {
			throw new NullPointerException("parameter is null");
		}		
		String address = inetAddress.getHostAddress();
		List<SipURI> list = sipFactory.getSipNetworkInterfaceManager().getOutboundInterfaces();
		SipURI networkInterface = null;
		for(SipURI networkInterfaceURI : list) {
			if(networkInterfaceURI.toString().contains(address)) {
				networkInterface = networkInterfaceURI;
				break;
			}
		}
		
		if(networkInterface == null) throw new IllegalArgumentException("Network interface for " +
				address + " not found");		
		try {
			outboundInterface = new SipURIImpl(SipFactoryImpl.addressFactory.createSipURI(null, address), ModifiableRule.NotModifiable).toString();
		} catch (ParseException e) {
			logger.error("couldn't parse the SipURI from USER[" + null
					+ "] HOST[" + address + "]", e);
			throw new IllegalArgumentException("Could not create SIP URI user = " + null + " host = " + address);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipSession#setOutboundInterface(java.net.InetSocketAddress)
	 */
	public void setOutboundInterface(InetSocketAddress inetSocketAddress) {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		if(inetSocketAddress == null) {
			throw new NullPointerException("parameter is null");
		}		
		String address = inetSocketAddress.getAddress().getHostAddress() + ":" + inetSocketAddress.getPort();
		List<SipURI> list = sipFactory.getSipNetworkInterfaceManager().getOutboundInterfaces();
		SipURI networkInterface = null;
		for(SipURI networkInterfaceURI : list) {
			if(networkInterfaceURI.toString().contains(address)) {
				networkInterface = networkInterfaceURI;
				break;
			}
		}
		
		if(networkInterface == null) throw new IllegalArgumentException("Network interface for " +
				address + " not found");
		try {
			outboundInterface = new SipURIImpl(SipFactoryImpl.addressFactory.createSipURI(null, address), ModifiableRule.NotModifiable).toString();
		} catch (ParseException e) {
			logger.error("couldn't parse the SipURI from USER[" + null
					+ "] HOST[" + address + "]", e);
			throw new IllegalArgumentException("Could not create SIP URI user = " + null + " host = " + address);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.SipSessionExt#setOutboundInterface(javax.servlet.sip.SipURI)
	 */
	public void setOutboundInterface(SipURI outboundInterface) {
		if(!isValid()) {
			throw new IllegalStateException("the session has been invalidated");
		}
		if(outboundInterface == null) {
			throw new NullPointerException("parameter is null");
		}				
		List<SipURI> list = sipFactory.getSipNetworkInterfaceManager().getOutboundInterfaces();
		SipURI networkInterface = null;
		for(SipURI networkInterfaceURI : list) {
			if(networkInterfaceURI.equals(outboundInterface)) {
				networkInterface = networkInterfaceURI;
				break;
			}
		}
		
		if(networkInterface == null) throw new IllegalArgumentException("Network interface for " +
				outboundInterface + " not found");
		this.outboundInterface = outboundInterface.toString();
		if(outboundInterface.getTransportParam() != null) {
			this.transport = outboundInterface.getTransportParam();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ServletContext getServletContext() {
		return getSipApplicationSession().getSipContext().getServletContext();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#removeDerivedSipSession(java.lang.String)
	 */
	public MobicentsSipSession removeDerivedSipSession(String toTag) {
		return derivedSipSessions.remove(toTag);
	}
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#findDerivedSipSession(java.lang.String)
	 */
	public MobicentsSipSession findDerivedSipSession(String toTag) {
		if(derivedSipSessions != null) {
			return derivedSipSessions.get(toTag);
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#getDerivedSipSessions()
	 */
	public Iterator<MobicentsSipSession> getDerivedSipSessions() {
		if(derivedSipSessions != null) {
			return derivedSipSessions.values().iterator();
		}
		return new HashMap<String, MobicentsSipSession>().values().iterator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#setParentSession(org.mobicents.servlet.sip.core.session.MobicentsSipSession)
	 */
	public void setParentSession(MobicentsSipSession mobicentsSipSession) {
		parentSession = mobicentsSipSession;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#getParentSession()
	 */
	public MobicentsSipSession getParentSession() {
		return parentSession;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#setSipSessionAttributeMap(java.util.Map)
	 */
	public void setSipSessionAttributeMap(
			Map<String, Object> sipSessionAttributeMap) {
		this.sipSessionAttributeMap = sipSessionAttributeMap;
	}
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#addDerivedSipSessions(org.mobicents.servlet.sip.core.session.MobicentsSipSession)
	 */
	public void addDerivedSipSessions(MobicentsSipSession derivedSession) {
		if(derivedSipSessions == null) {
			this.derivedSipSessions = new ConcurrentHashMap<String, MobicentsSipSession>();
		}
		derivedSipSessions.putIfAbsent(derivedSession.getKey().getToTag(), derivedSession);
	}
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#getSipSessionAttributeMap()
	 */
	public Map<String, Object> getSipSessionAttributeMap() {
		return getAttributeMap();
	}
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#setLocalParty(javax.servlet.sip.Address)
	 */
	public void setLocalParty(Address localParty) {
		this.localParty = localParty;
	}
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#setRemoteParty(javax.servlet.sip.Address)
	 */
	public void setRemoteParty(Address remoteParty) {
		this.remoteParty = remoteParty;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addSubscription(MobicentsSipServletMessage sipServletMessageImpl) throws SipException {
		EventHeader eventHeader = null;
		if(sipServletMessageImpl instanceof SipServletResponseImpl) {
			eventHeader =  (EventHeader) ((SipServletRequestImpl)((SipServletResponseImpl)sipServletMessageImpl).getRequest()).getMessage().getHeader(EventHeader.NAME);			
		} else {
			eventHeader =  (EventHeader) sipServletMessageImpl.getMessage().getHeader(EventHeader.NAME);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("adding subscription " + eventHeader + " to sip session " + getId());
		}
		if(subscriptions == null) {
			this.subscriptions = new CopyOnWriteArraySet<EventHeader>();
		}
		subscriptions.add(eventHeader);	
				
		if(logger.isDebugEnabled()) {
			logger.debug("Request from Original Transaction is " + originalMethod);
			logger.debug("Dialog is " + sessionCreatingDialog);
		}
		if(subscriptions.size() < 2 && Request.INVITE.equals(originalMethod)) {
			sessionCreatingDialog.terminateOnBye(false);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void removeSubscription(MobicentsSipServletMessage sipServletMessageImpl) {
		EventHeader eventHeader =  (EventHeader) sipServletMessageImpl.getMessage().getHeader(EventHeader.NAME);
		if(logger.isDebugEnabled()) {
			logger.debug("removing subscription " + eventHeader + " to sip session " + getId());
		}
		boolean hasOngoingSubscriptions = false;
		if(subscriptions != null) {
			subscriptions.remove(eventHeader);
			if(subscriptions.size() > 0) {
				hasOngoingSubscriptions = true;
			}
			if(!hasOngoingSubscriptions) {		
				if(subscriptions.size() < 1) {
					if((originalMethod != null && okToByeSentOrReceived) || !Request.INVITE.equals(originalMethod) ) {
						setReadyToInvalidate(true);
						setState(State.TERMINATED);
					}
				}			
			}
		}
		if(isReadyToInvalidateInternal()) {
			if(logger.isDebugEnabled()) {
				logger.debug("no more subscriptions in session " + getId());
			}
			if(sessionCreatingDialog != null) {
				sessionCreatingDialog.delete();
			}
		}
	}
	/**
	 * @return the semaphore
	 */
	public Semaphore getSemaphore() {
		return semaphore;
	}
	
	public MobicentsSipSessionFacade getFacade() {		
		MobicentsSipApplicationSession sipApplicationSession = getSipApplicationSession();			
        if (facade == null && sipApplicationSession != null){
        	SipContext sipContext = sipApplicationSession.getSipContext(); 	
            if (sipContext.isPackageProtectionEnabled()){
                final MobicentsSipSession fsession = this;
                facade = (MobicentsSipSessionFacade)AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        return new MobicentsSipSessionFacade(fsession);
                    }
                });
            } else {
                facade = new MobicentsSipSessionFacade(this);
            }
        }
        return (facade);	  
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MobicentsSipSession) {
			MobicentsSipSession sipSession = (MobicentsSipSession)obj;
			if(sipSession.getKey().equals(getKey())) {
				// Issue 2365 : Derived Sessions should be equal only if their to tag is equal
				if(sipSession.getKey().getToTag() == null && getKey().getToTag() == null) {
					return true;
				}				
				if(sipSession.getKey().getToTag() != null && getKey().getToTag() != null && sipSession.getKey().getToTag().equals(getKey().getToTag())) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return false;
	}
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	@Override
	public String toString() {
		return getKey().toString();
	}
	public SipApplicationRouterInfo getNextSipApplicationRouterInfo() {
		return nextSipApplicationRouterInfo;
	}
	public void setNextSipApplicationRouterInfo(
			SipApplicationRouterInfo routerInfo) {
		this.nextSipApplicationRouterInfo = routerInfo;
	}
	
	/**
	 * Setting ackReceived for CSeq to specified value in second param.
	 * if the second param is true it will try to cleanup earlier cseq as well to save on memory
	 * @param cSeq cseq to set the ackReceived
	 * @param ackReceived whether or not the ack has been received for this cseq
	 */
	public void setAckReceived(long cSeq, boolean ackReceived) {
		if(logger.isDebugEnabled()) {
			logger.debug("setting AckReceived to : " + ackReceived + " for CSeq " + cSeq);
		}
		acksReceived.put(cSeq, ackReceived);
		if(ackReceived) {
			cleanupAcksReceived(cSeq);
		}
	}
	
	/**
	 * check if the ack has been received for the cseq in param
	 * it may happen that the ackReceived has been removed already if that's the case it will return true
	 * @param cSeq CSeq number to check if the ack has already been received
	 * @return
	 */
	protected boolean isAckReceived(long cSeq) {
		Boolean ackReceived = acksReceived.get(cSeq);
		if(logger.isDebugEnabled()) {
			logger.debug("isAckReceived for CSeq " + cSeq +" : " + ackReceived);
		}
		if(ackReceived == null) {
			// if there is no value for it it means that it is a retransmission 
			return true;
		}
		return ackReceived;
	}
	
	/**
	 * We clean up the stored acks received when the remoteCSeq in param is greater and
	 * that the ackReceived is true
	 * @param remoteCSeq remoteCSeq the basis CSeq for cleaning up earlier (lower CSeq) stored ackReceived
	 */
	protected void cleanupAcksReceived(long remoteCSeq) {
		List<Long> toBeRemoved = new ArrayList<Long>();
		final Iterator<Entry<Long, Boolean>> cSeqs = acksReceived.entrySet().iterator();
		while (cSeqs.hasNext()) {
			final Entry<Long, Boolean> entry = cSeqs.next();
			final long cSeq = entry.getKey();
			final boolean ackReceived = entry.getValue();
			if(ackReceived && cSeq < remoteCSeq) {
				toBeRemoved.add(cSeq);
			}
		}
		for(Long cSeq: toBeRemoved) {			
			acksReceived.remove(cSeq);
			if(logger.isDebugEnabled()) {
				logger.debug("removed ackReceived for CSeq " + cSeq);
			}
		}
	}
	
	public long getCseq() {
		return cseq;
	}
	public void setCseq(long cseq) {
		this.cseq = cseq;		
	}
	//CSeq validation should only be done for non proxy applications
	public boolean validateCSeq(MobicentsSipServletRequest sipServletRequest) {
		final Request request = (Request) sipServletRequest.getMessage();
		final long localCseq = cseq;		
		final long remoteCSeq =  ((CSeqHeader) request.getHeader(CSeqHeader.NAME)).getSeqNumber();
		final String method = request.getMethod();
		final boolean isAck = Request.ACK.equalsIgnoreCase(method);
		final boolean isPrackCancel= Request.PRACK.equalsIgnoreCase(method) || Request.CANCEL.equalsIgnoreCase(method);
		boolean resetLocalCSeq = true;
				
		if(isAck && isAckReceived(remoteCSeq)) {
			// Filter out ACK retransmissions for JSIP patch for http://code.google.com/p/mobicents/issues/detail?id=766
			logger.debug("ACK filtered out as a retransmission. This Sip Session already has been ACKed.");
			return false;
		}
		if(isAck) {
			if(logger.isDebugEnabled()) {
				logger.debug("localCSeq : " + localCseq + ", remoteCSeq : " + remoteCSeq);
			}
			setAckReceived(remoteCSeq, true);			
		} 	
		if(localCseq == remoteCSeq && !isAck) {
			logger.debug("dropping retransmission " + request + " since it matches the current sip session cseq " + localCseq);
			return false;
		}		
		if(localCseq > remoteCSeq) {				
			if(!isAck && !isPrackCancel) {
				logger.error("CSeq out of order for the following request " + sipServletRequest);
				if(Request.INVITE.equalsIgnoreCase(method)) {
					setAckReceived(remoteCSeq, false);
				}
				final SipServletResponse response = sipServletRequest.createResponse(Response.SERVER_INTERNAL_ERROR, "CSeq out of order");
				try {
					response.send();
				} catch (IOException e) {
					logger.error("Can not send error response", e);
				}
				return false;
			} else {				
				// Issue 1714 : if the local cseq is greater then the remote one don't reset the local cseq
				resetLocalCSeq= false;
			}
		}		
		if(logger.isDebugEnabled()) {
			logger.debug("resetLocalCSeq : " + resetLocalCSeq);
		}
		if(resetLocalCSeq) {
			setCseq(remoteCSeq);
			if(Request.INVITE.equalsIgnoreCase(method)) {
				setAckReceived(remoteCSeq, false);
			}
		}		
		return true;
	}
	
	public String getTransport() {
		return transport;
	}
	public void setTransport(String transport) {
		this.transport = transport;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.SipSessionExt#scheduleAsynchronousWork(org.mobicents.javax.servlet.sip.SipSessionAsynchronousWork)
	 */
	public void scheduleAsynchronousWork(SipSessionAsynchronousWork work) {
		sipFactory.getSipApplicationDispatcher().getAsynchronousExecutor().execute(new SipSessionAsyncTask(key, work, sipFactory));
	}
	public int getRequestsPending() {
		return requestsPending;
	}
	public void setRequestsPending(int requests) {
		// Sometimes the count might not match due to retransmissing of ACK after OK is missing or CANCEL, 
		// we should never go negative here
		if(requests < 0) requests = 0;
		requestsPending = requests;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.SipSessionExt#setCopyRecordRouteHeadersOnSubsequentResponses(boolean)
	 */
	public void setCopyRecordRouteHeadersOnSubsequentResponses(
			boolean copyRecordRouteHeadersOnSubsequentResponses) {
		this.copyRecordRouteHeadersOnSubsequentResponses = copyRecordRouteHeadersOnSubsequentResponses;
	}
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.SipSessionExt#getCopyRecordRouteHeadersOnSubsequentResponses()
	 */
	public boolean getCopyRecordRouteHeadersOnSubsequentResponses() {
		return copyRecordRouteHeadersOnSubsequentResponses;
	}
	/**
	 * @param sipSessionSecurity the sipSessionSecurity to set
	 */
	public void setSipSessionSecurity(MobicentsSipSessionSecurity sipSessionSecurity) {
		this.sipSessionSecurity = sipSessionSecurity;
	}
	/**
	 * @return the sipSessionSecurity
	 */
	public MobicentsSipSessionSecurity getSipSessionSecurity() {
		if(sipSessionSecurity == null) {
			sipSessionSecurity = new SipSessionSecurity();
		}
		return sipSessionSecurity;
	}
	
	public void acquire() {
		if(semaphore != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Before semaphore acquire for sipSession=" + this + " semaphore=" + semaphore);
			}
			try {
				while(!semaphore.tryAcquire(30000, TimeUnit.MILLISECONDS)){
					logger.warn("Failed to acquire session semaphore " + 
							semaphore + " for 30 secs. We will unlock the " +
							"semaphore no matter what because the " +
							"transaction is about to timeout. THIS " +
							"MIGHT ALSO BE CONCURRENCY CONTROL RISK." +						 
							" sip Session is" + this);
					semaphore.release();
				}
			} catch (InterruptedException e) {
				logger.error("Problem acquiring semaphore on sip session " + this, e);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("After semaphore acquire for sipSession=" + this + " semaphore=" + semaphore);
			}
		}
	}
	
	public void release() {
		if(semaphore != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Before Semaphore released for sipSession=" + this + " semaphore=" + semaphore);
			}
			//equalize the semaphore permits to the expected number for binary semaphore
			if(semaphore.availablePermits()>0) {
				logger.warn("About to release semaphore but we expected permits = 0. We will adjust to normal "
						+ semaphore + " sip session=" + this);
				while(semaphore.availablePermits()>0) {
					try {
						semaphore.acquire();
					} catch (Exception e) {
					}
				}
			}
			if(semaphore.availablePermits()<0) {
				logger.warn("About to release semaphore but we expected permits = 0. We will adjust to normal " 
						+ semaphore + " sip session=" + this);
				while(semaphore.availablePermits()<0) {
					try {
						semaphore.release();
					} catch (Exception e) {
					}
				}
			}	
		
			semaphore.release();
			if(logger.isDebugEnabled()) {
				logger.debug("After Semaphore released for sipSession=" + this + " semaphore=" + semaphore);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#setFlow(javax.sip.address.SipURI)
	 */
	public void setFlow(final javax.sip.address.SipURI flow) {
		this.flow = flow;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.MobicentsSipSession#getFlow()
	 */
	public javax.sip.address.SipURI getFlow() {
		return flow;
	}

	protected boolean orphan = false;

	public boolean isOrphan() {
		return orphan;
	}

	public void setOrphan(boolean orphan) {
		this.orphan = orphan;
	}
}
