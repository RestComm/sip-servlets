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
package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.address.TelURL;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipRequestDispatcher;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.security.AuthInfoEntry;
import org.mobicents.servlet.sip.security.AuthInfoImpl;
import org.mobicents.servlet.sip.security.authentication.DigestAuthenticator;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

public class SipServletRequestImpl extends SipServletMessageImpl implements
		SipServletRequest, Cloneable {

	private static final long serialVersionUID = 1L;

	private static transient Logger logger = Logger.getLogger(SipServletRequestImpl.class);
	
	private static final String EXCEPTION_MESSAGE = "The context does not allow you to modify this request !";
	
	public transient static final Set<String> nonInitialSipRequestMethods = new HashSet<String>();
	
	static {
		nonInitialSipRequestMethods.add("CANCEL");
		nonInitialSipRequestMethods.add("BYE");
		nonInitialSipRequestMethods.add("PRACK");
		nonInitialSipRequestMethods.add("ACK");
		nonInitialSipRequestMethods.add("UPDATE");
		nonInitialSipRequestMethods.add("INFO");
	};
	
	/* Linked request (for b2bua) */
	private SipServletRequestImpl linkedRequest;
	
	private boolean createDialog;
	/*
	 * Popped route header - when we are the UAS we pop and keep the route
	 * header
	 */
	private AddressImpl poppedRoute;
	private RouteHeader poppedRouteHeader;

	/* Cache the application routing directive in the record route header */
	private SipApplicationRoutingDirective routingDirective = SipApplicationRoutingDirective.NEW;

	private RoutingState routingState;
	
	private transient SipServletResponse lastFinalResponse;
	
	private transient SipServletResponse lastInformationalResponse;
	
	/**
	 * Routing region.
	 */
	private SipApplicationRoutingRegion routingRegion;

	private transient URI subscriberURI;

	private boolean isInitial;
	
	private boolean isFinalResponseGenerated;
	
	private boolean is1xxResponseGenerated;	
	
	private transient boolean isReadOnly; 
	
	public SipServletRequestImpl(Request request, SipFactoryImpl sipFactoryImpl,
			MobicentsSipSession sipSession, Transaction transaction, Dialog dialog,
			boolean createDialog) {
		super(request, sipFactoryImpl, transaction, sipSession, dialog);
		this.createDialog = createDialog;
		routingState = checkRoutingState(this, dialog);
		if(RoutingState.INITIAL.equals(routingState)) {
			isInitial = true;
		}
		isFinalResponseGenerated = false;
	}

	@Override
	public boolean isSystemHeader(String headerName) {

		String hName = getFullHeaderName(headerName);

		/*
		 * Contact is a system header field in messages other than REGISTER
		 * requests and responses, 3xx and 485 responses, and 200/OPTIONS
		 * responses so it is not contained in system headers and as such
		 * as a special treatment
		 */
		boolean isSystemHeader = JainSipUtils.systemHeaders.contains(hName);

		if (isSystemHeader)
			return isSystemHeader;

		boolean isContactSystem = false;
		Request request = (Request) this.message;

		String method = request.getMethod();
		if (method.equals(Request.REGISTER)) {
			isContactSystem = false;
		} else {
			isContactSystem = true;
		}

		if (isContactSystem && hName.equals(ContactHeader.NAME)) {
			isSystemHeader = true;
		} else {
			isSystemHeader = false;
		}

		return isSystemHeader;
	}

	public SipServletRequest createCancel() {
		checkReadOnly();
		if (!((Request) message).getMethod().equals(Request.INVITE)) {
			throw new IllegalStateException(
					"Cannot create CANCEL for non inivte");
		}
		if (super.getTransaction() == null
				|| super.getTransaction() instanceof ServerTransaction)
			throw new IllegalStateException("No client transaction found!");

		if(RoutingState.FINAL_RESPONSE_SENT.equals(routingState) || lastFinalResponse != null) {
			throw new IllegalStateException("final response already sent!");
		}
		
		try {
			Request request = (Request) super.message;
			String transport = JainSipUtils.findTransport(request);
			SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false).getSipProvider();					
			
			Request cancelRequest = ((ClientTransaction) getTransaction())
					.createCancel();
			ClientTransaction clientTransaction = sipProvider
					.getNewClientTransaction(cancelRequest);
			SipServletRequest newRequest = new SipServletRequestImpl(
					cancelRequest, sipFactoryImpl, super.session,
					clientTransaction, getTransaction().getDialog(), false);

			return newRequest;
		} catch (SipException ex) {
			throw new IllegalStateException("Could not create cancel", ex);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletRequest#createResponse(int)
	 */
	public SipServletResponse createResponse(int statusCode) {
		return createResponse(statusCode, null);
	}
	
	public SipServletResponse createResponse(final int statusCode, final String reasonPhrase) {
		return createResponse(statusCode, reasonPhrase, true);
	}

	public SipServletResponse createResponse(final int statusCode, final String reasonPhrase, boolean validate) {
		checkReadOnly();
		final Transaction transaction = getTransaction();
		if(RoutingState.CANCELLED.equals(routingState)) {
			throw new IllegalStateException("Cannot create a response for the invite, a CANCEL has been received and the INVITE was replied with a 487!");
		}
		if (transaction == null
				|| transaction instanceof ClientTransaction) {
			if(validate) {
				throw new IllegalStateException(
					"Cannot create a response - not a server transaction");
			}
		}
		try {
			final Request request = transaction.getRequest();
			final Response response = SipFactories.messageFactory.createResponse(
					statusCode, request);
			final Dialog dialog = transaction.getDialog();
			if(reasonPhrase!=null) {
				response.setReasonPhrase(reasonPhrase);
			}
			//add a To tag for all responses except Trying (or trying too if it's a subsequent request)
			if((statusCode > Response.TRYING || !isInitial()) && statusCode <= Response.SESSION_NOT_ACCEPTABLE) {
				final ToHeader toHeader = (ToHeader) response
					.getHeader(ToHeader.NAME);
				// If we already have a to tag, dont create new
				if (toHeader.getTag() == null) {					
					// if a dialog has already been created
					// reuse local tag
					if(dialog != null && dialog.getLocalTag() != null && dialog.getLocalTag().length() > 0) {																	
						toHeader.setTag(dialog.getLocalTag());						
					} else if(session != null && session.getSipApplicationSession() != null) {						
						final SipApplicationSessionKey sipAppSessionKey = session.getSipApplicationSession().getKey();
						toHeader.setTag(ApplicationRoutingHeaderComposer.getHash(sipFactoryImpl.getSipApplicationDispatcher(), session.getKey().getApplicationName(), sipAppSessionKey.getId()));
					} else {							
						//if the sessions are null, it means it is a cancel response
						toHeader.setTag(Integer.toString(new Random().nextInt(10000000)));
					}
				}
				if (statusCode == Response.OK ) {
					//Following restrictions in JSR 289 Section 4.1.3 Contact Header Field
					if(!Request.REGISTER.equals(method) && !Request.OPTIONS.equals(method)) { 
					    // Add the contact header for the dialog.					    
					    final ContactHeader contactHeader = JainSipUtils.createContactHeader(
					    		super.sipFactoryImpl.getSipNetworkInterfaceManager(), request, null);
					    if(logger.isDebugEnabled()) {
					    	logger.debug("We're adding this contact header to our new response: '" + contactHeader + ", transport=" + JainSipUtils.findTransport(request));
					    }
					    response.setHeader(contactHeader);
				    }
				}
			}
			// Application Routing : Adding the recorded route headers as route headers
			final ListIterator<RecordRouteHeader> recordRouteHeaders = request.getHeaders(RecordRouteHeader.NAME);
			while (recordRouteHeaders.hasNext()) {
				RecordRouteHeader recordRouteHeader = (RecordRouteHeader) recordRouteHeaders
						.next();
				RouteHeader routeHeader = SipFactories.headerFactory.createRouteHeader(recordRouteHeader.getAddress());
				response.addHeader(routeHeader);
			}
			
			final SipServletResponseImpl newSipServletResponse = new SipServletResponseImpl(response, super.sipFactoryImpl,
					validate ? (ServerTransaction) transaction : transaction, session, getDialog());
			newSipServletResponse.setOriginalRequest(this);
			if(!Request.PRACK.equals(method) && statusCode >= Response.OK && 
					statusCode <= Response.SESSION_NOT_ACCEPTABLE) {	
				isFinalResponseGenerated = true;
			}
			if(statusCode >= Response.TRYING && 
					statusCode < Response.OK) {	
				is1xxResponseGenerated = true;
			}
			return newSipServletResponse;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code" + statusCode,
					ex);
		}		
	}

	public B2buaHelper getB2buaHelper() {
		checkReadOnly();
		if (session.getProxy() != null)
			throw new IllegalStateException("Proxy already present");
		B2buaHelperImpl b2buaHelper = session.getB2buaHelper();
		if (b2buaHelper != null)
			return b2buaHelper;
		try {
			b2buaHelper = new B2buaHelperImpl();
			b2buaHelper.setSipFactoryImpl(sipFactoryImpl);
			b2buaHelper.setSipManager(session.getSipApplicationSession().getSipContext().getSipManager());
			Request request = (Request) super.message;
			if (this.getTransaction() != null
					&& this.getTransaction().getDialog() == null
					&& JainSipUtils.dialogCreatingMethods.contains(request.getMethod())) {
				
				String transport = JainSipUtils.findTransport(request);
				SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(
						transport, false).getSipProvider();
				
				Dialog dialog = sipProvider.getNewDialog(this
						.getTransaction());
				this.session.setSessionCreatingDialog(dialog);
				dialog.setApplicationData( this.transactionApplicationData);				
			}			
			if(JainSipUtils.dialogCreatingMethods.contains(request.getMethod())) {
				this.createDialog = true; // flag that we want to create a dialog for outgoing request.
			}
			session.setB2buaHelper(b2buaHelper);
			return b2buaHelper;
		} catch (SipException ex) {
			throw new IllegalStateException("Cannot get B2BUAHelper", ex);
		}
	}		

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public int getMaxForwards() {
		return ((MaxForwardsHeader) ((Request) message)
				.getHeader(MaxForwardsHeader.NAME)).getMaxForwards();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletRequest#getPoppedRoute()
	 */
	public Address getPoppedRoute() {
		if((this.poppedRoute == null && poppedRouteHeader != null) ||
				(poppedRoute != null && poppedRouteHeader != null && !poppedRoute.getAddress().equals(poppedRouteHeader.getAddress()))) {
			this.poppedRoute = new AddressImpl(poppedRouteHeader.getAddress(), null, getTransaction() == null ? true : false);
		}
		return poppedRoute;
	}
	
	public RouteHeader getPoppedRouteHeader() {
		return poppedRouteHeader;
	}

	/**
	 * Set the popped route
	 * 
	 * @param routeHeader
	 *            the popped route header to set
	 */
	public void setPoppedRoute(RouteHeader routeHeader) {
		this.poppedRouteHeader = routeHeader;
	}

	/**
	 * {@inheritDoc}
	 */
	public Proxy getProxy() throws TooManyHopsException {
		checkReadOnly();
		if (session.getB2buaHelper() != null ) throw new IllegalStateException("Cannot proxy request");
		return getProxy(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Proxy getProxy(boolean create) throws TooManyHopsException {
		checkReadOnly();
		if (session.getB2buaHelper() != null ) throw new IllegalStateException("Cannot proxy request");
		
		MaxForwardsHeader mfHeader = (MaxForwardsHeader)this.message.getHeader(MaxForwardsHeader.NAME);
		if(mfHeader.getMaxForwards()<=0) {
			try {
				this.createResponse(483, "Too many hops").send();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			throw new TooManyHopsException();
		}
		
		// For requests like PUBLISH dialogs(sessions) do not exist, but some clients
		// attempt to send them in sequence as if they support dialogs and when such a subsequent request
		// comes in it gets assigned to the previous request session where the proxy is destroyed.
		// In this case we must create a new proxy. And we recoginze this case by additionally checking
		// if this request is initial. TODO: Consider deleting the session contents too? JSR 289 says
		// the session is keyed against the headers, not against initial/non-initial...
		if (create) { 
			boolean createNewProxy = false;
			if(isInitial() && session.getProxy() != null && session.getProxy().getOriginalRequest() == null)  {
				createNewProxy = true;
			}
			if(session.getProxy() == null || createNewProxy) {
				ProxyImpl proxy = new ProxyImpl(this, super.sipFactoryImpl);
				this.session.setProxy(proxy);
			}
		}
		return this.session.getProxy();
	}

	/**
	 * {@inheritDoc}
	 */
	public BufferedReader getReader() throws IOException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getRequestURI() {
		Request request = (Request) super.message;
		if (request.getRequestURI() instanceof javax.sip.address.SipURI)
			return new SipURIImpl((javax.sip.address.SipURI) request
					.getRequestURI());
		else if (request.getRequestURI() instanceof javax.sip.address.TelURL)
			return new TelURLImpl((javax.sip.address.TelURL) request
					.getRequestURI());
		else
			throw new UnsupportedOperationException("Unsupported scheme");
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInitial() {
		return isInitial; 
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#isCommitted()
	 */
	public boolean isCommitted() {		
		//the message is an incoming request for which a final response has been generated
		if(getTransaction() instanceof ServerTransaction && 
				(RoutingState.FINAL_RESPONSE_SENT.equals(routingState) || isFinalResponseGenerated)) {
			return true;
		}
		//the message is an outgoing request which has been sent
		if(getTransaction() instanceof ClientTransaction && this.isMessageSent) {
			return true;
		}
		/*
		if(Request.ACK.equals((((Request)message).getMethod()))) {
			return true;
		}*/
		return false;
	}
	
	protected void checkMessageState() {
		if(isMessageSent || getTransaction() instanceof ServerTransaction) {
			throw new IllegalStateException("Message already sent or incoming message");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void pushPath(Address uri) {
		checkReadOnly();
		if(!Request.REGISTER.equalsIgnoreCase(((Request)message).getMethod())) {
			throw new IllegalStateException("Cannot push a Path on a non REGISTER request !");
		}
		if(uri.getURI() instanceof TelURL) {
			throw new IllegalArgumentException("Cannot push a TelUrl as a path !");
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Pushing path into message of value [" + uri + "]");

		try {
			javax.sip.header.Header p = SipFactories.headerFactory
					.createHeader(PathHeader.NAME, uri.toString());
			this.message.addFirst(p);
		} catch (Exception e) {
			logger.error("Error while pushing path [" + uri + "]");
			throw new IllegalArgumentException("Error pushing path ", e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void pushRoute(Address address) {
		checkReadOnly();
		if(address.getURI() instanceof TelURL) {
			throw new IllegalArgumentException("Cannot push a TelUrl as a route !");
		}
		
		javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) ((AddressImpl) address)
			.getAddress().getURI();
		pushRoute(sipUri);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#pushRoute(javax.servlet.sip.SipURI)
	 */
	public void pushRoute(SipURI uri) {	
		checkReadOnly();
		javax.sip.address.SipURI sipUri = ((SipURIImpl) uri).getSipURI();
			sipUri.setLrParam();
		pushRoute(sipUri);
	}

	/**
	 * Pushes a route header on initial request based on the jain sip sipUri given on parameter
	 * @param sipUri the jain sip sip uri to use to construct the route header to push on the request
	 */
	private void pushRoute(javax.sip.address.SipURI sipUri) {		
		if(isInitial()) {
			if (logger.isDebugEnabled())
				logger.debug("Pushing route into message of value [" + sipUri
						+ "]");
			
			sipUri.setLrParam();
	
			try {
				javax.sip.header.Header p = SipFactories.headerFactory
						.createRouteHeader(SipFactories.addressFactory
								.createAddress(sipUri));
				this.message.addFirst(p);
			} catch (SipException e) {
				logger.error("Error while pushing route [" + sipUri + "]");
				throw new IllegalArgumentException("Error pushing route ", e);
			}
		} else {
			//as per JSR 289 Section 11.1.3 Pushing Route Header Field Values
			// pushRoute can only be done on the initial requests. 
			// Subsequent requests within a dialog follow the route set. 
			// Any attempt to do a pushRoute on a subsequent request in a dialog 
			// MUST throw and IllegalStateException.
			throw new IllegalStateException("Cannot push route on subsequent requests, only intial ones");
		}
	}
	
	public void setMaxForwards(int n) {
		checkReadOnly();
		MaxForwardsHeader mfh = (MaxForwardsHeader) this.message
				.getHeader(MaxForwardsHeader.NAME);
		try {
			if (mfh != null) {
				mfh.setMaxForwards(n);
			}
		} catch (Exception ex) {
			String s = "Error while setting max forwards";
			logger.error(s, ex);
			throw new IllegalArgumentException(s, ex);
		}

	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#setRequestURI(javax.servlet.sip.URI)
	 */
	public void setRequestURI(URI uri) {
		checkReadOnly();
		Request request = (Request) message;
		URIImpl uriImpl = (URIImpl) uri;
		javax.sip.address.URI wrappedUri = uriImpl.getURI();
		request.setRequestURI(wrappedUri);
		//TODO look through all contacts of the user and change them depending of if STUN is enabled
		//and the request is aimed to the local network or outside
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#setRoutingDirective(javax.servlet.sip.SipApplicationRoutingDirective, javax.servlet.sip.SipServletRequest)
	 */
	public void setRoutingDirective(SipApplicationRoutingDirective directive,
			SipServletRequest origRequest) throws IllegalStateException {
		checkReadOnly();
		SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
		//@jean.deruelle Commenting this out, why origRequestImpl.isCommitted() is needed ?
//			if ((directive == SipApplicationRoutingDirective.REVERSE || directive == SipApplicationRoutingDirective.CONTINUE)
//					&& (!origRequestImpl.isInitial() || origRequestImpl.isCommitted())) {
		// If directive is CONTINUE or REVERSE, the parameter origRequest must be an 
		//initial request dispatched by the container to this application, 
		//i.e. origRequest.isInitial() must be true
		if ((directive == SipApplicationRoutingDirective.REVERSE || 
				directive == SipApplicationRoutingDirective.CONTINUE)){ 
			if(origRequestImpl == null ||
				!origRequestImpl.isInitial()) {
				if(logger.isDebugEnabled()) {
					logger.debug("directive to set : " + directive);
					logger.debug("Original Request Routing State : " + origRequestImpl.getRoutingState());
				}
				throw new IllegalStateException(
						"Bad state -- cannot set routing directive");
			} else {
				//set AR State Info from previous request
				session.setStateInfo(origRequestImpl.getSipSession().getStateInfo());
				//needed for application composition
				currentApplicationName = origRequestImpl.getCurrentApplicationName();
				//linking the requests
				//FIXME one request can be linked to  many more as for B2BUA
				origRequestImpl.setLinkedRequest(this);
				setLinkedRequest(origRequestImpl);
			}
		} else {				
			//This request must be a request created in a new SipSession 
			//or from an initial request, and must not have been sent. 
			//If any one of these preconditions are not met, the method throws an IllegalStateException.
			Set<Transaction> ongoingTransactions = session.getOngoingTransactions();
			if(!State.INITIAL.equals(session.getState()) && ongoingTransactions != null && ongoingTransactions.size() > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("session state : " + session.getState());
					logger.debug("numbers of ongoing transactions : " + ongoingTransactions.size());
				}
				throw new IllegalStateException(
					"Bad state -- cannot set routing directive");
			}
		}
		routingDirective = directive;
		linkedRequest = origRequestImpl;
		//@jean.deruelle Commenting this out, is this really needed ?
//		RecordRouteHeader rrh = (RecordRouteHeader) request
//				.getHeader(RecordRouteHeader.NAME);
//		javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) rrh
//				.getAddress().getURI();
//
//		try {
//			if (directive == SipApplicationRoutingDirective.NEW) {
//				sipUri.setParameter("rd", "NEW");
//			} else if (directive == SipApplicationRoutingDirective.REVERSE) {
//				sipUri.setParameter("rd", "REVERSE");
//			} else if (directive == SipApplicationRoutingDirective.CONTINUE) {
//				sipUri.setParameter("rd", "CONTINUE");
//			}
//			
//		} catch (Exception ex) {
//			String s = "error while setting routing directive";
//			logger.error(s, ex);
//			throw new IllegalArgumentException(s, ex);
//		}							
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		// JSR 289 Section 5.6.1 Parameters :
		// For initial requests where a preloaded Route header specified the application to be invoked, the parameters are those of the SIP or SIPS URI in that Route header.
		// For initial requests where the application is invoked the parameters are those present on the request URI, 
		// if this is a SIP or a SIPS URI. For other URI schemes, the parameter set is undefined.
		// For subsequent requests in a dialog, the parameters presented to the application  are those that the application itself 
		// set on the Record-Route header for the initial request or response (see 10.4 Record-Route Parameters). 
		// These will typically be the URI parameters of the top Route header field but if the upstream SIP element is a 
		// "strict router" they may be returned in the request URI (see RFC 3261). 
		// It is the containers responsibility to recognize whether the upstream element is a strict router and determine the right parameter set accordingly.
		if(this.getPoppedRoute() != null) {
			return this.getPoppedRoute().getURI().getParameter(name);
		} else {
			return this.getRequestURI().getParameter(name);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map<String, String> getParameterMap() {
		// JSR 289 Section 5.6.1 Parameters :
		// For initial requests where a preloaded Route header specified the application to be invoked, the parameters are those of the SIP or SIPS URI in that Route header.
		// For initial requests where the application is invoked the parameters are those present on the request URI, 
		// if this is a SIP or a SIPS URI. For other URI schemes, the parameter set is undefined.
		// For subsequent requests in a dialog, the parameters presented to the application  are those that the application itself 
		// set on the Record-Route header for the initial request or response (see 10.4 Record-Route Parameters). 
		// These will typically be the URI parameters of the top Route header field but if the upstream SIP element is a 
		// "strict router" they may be returned in the request URI (see RFC 3261). 
		// It is the containers responsibility to recognize whether the upstream element is a strict router and determine the right parameter set accordingly.
		HashMap<String, String> retval = new HashMap<String, String>();
		if(this.getPoppedRoute() != null) {
			Iterator<String> parameterNamesIt =  this.getPoppedRoute().getURI().getParameterNames();
			while (parameterNamesIt.hasNext()) {
				String parameterName = parameterNamesIt.next();
				retval.put(parameterName, this.getPoppedRoute().getURI().getParameter(parameterName));			
			}
		} else {
			Iterator<String> parameterNamesIt =  this.getRequestURI().getParameterNames();
			while (parameterNamesIt.hasNext()) {
				String parameterName = parameterNamesIt.next();
				retval.put(parameterName, this.getRequestURI().getParameter(parameterName));			
			}
		}		
		
		return retval;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		// JSR 289 Section 5.6.1 Parameters :
		// For initial requests where a preloaded Route header specified the application to be invoked, the parameters are those of the SIP or SIPS URI in that Route header.
		// For initial requests where the application is invoked the parameters are those present on the request URI, 
		// if this is a SIP or a SIPS URI. For other URI schemes, the parameter set is undefined.
		// For subsequent requests in a dialog, the parameters presented to the application  are those that the application itself 
		// set on the Record-Route header for the initial request or response (see 10.4 Record-Route Parameters). 
		// These will typically be the URI parameters of the top Route header field but if the upstream SIP element is a 
		// "strict router" they may be returned in the request URI (see RFC 3261). 
		// It is the containers responsibility to recognize whether the upstream element is a strict router and determine the right parameter set accordingly.
		Vector<String> retval = new Vector<String>();
		if(this.getPoppedRoute() != null) {
			Iterator<String> parameterNamesIt =  this.getPoppedRoute().getURI().getParameterNames();
			while (parameterNamesIt.hasNext()) {
				String parameterName = parameterNamesIt.next();
				retval.add(parameterName);		
			}
		} else {
			Iterator<String> parameterNamesIt =  this.getRequestURI().getParameterNames();
			while (parameterNamesIt.hasNext()) {
				String parameterName = parameterNamesIt.next();
				retval.add(parameterName);
			}
		}		
		
		return retval.elements();				
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		// JSR 289 Section 5.6.1 Parameters :
		// The getParameterValues method returns an array of String objects containing all the parameter values associated with a parameter name. 
		// The value returned from the getParameter method must always equal the first value in the array of String objects returned by getParameterValues.
		// Note:Support for multi-valued parameters is defined mainly for HTTP because HTML forms may contain multi-valued parameters in form submissions.
		return new String[] {getParameter(name)};
	}

	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public RequestDispatcher getRequestDispatcher(String handler) {
		SipServletImpl sipServletImpl = (SipServletImpl) 
			getSipSession().getSipApplicationSession().getSipContext().getChildrenMap().get(handler);
		if(sipServletImpl == null) {
			throw new IllegalArgumentException(handler + " is not a valid servlet name");
		}
		return new SipRequestDispatcher(sipServletImpl);
	}

	public String getScheme() {
		return ((Request)message).getRequestURI().getScheme();
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @return the routingDirective
	 */
	public SipApplicationRoutingDirective getRoutingDirective() {
		if(!isInitial()) {
			throw new IllegalStateException("the request is not initial");
		}
		return routingDirective;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.servlet.sip.message.SipServletMessageImpl#send()
	 */
	@Override
	public void send() {
		checkReadOnly();
		try {
			final Request request = (Request) super.message;			
			final String transport = JainSipUtils.findTransport(request);
			final ProxyImpl proxy = session.getProxy();
			final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipFactoryImpl.getSipNetworkInterfaceManager();
			ViaHeader viaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
			//Issue 112 fix by folsson
		    if(!method.equalsIgnoreCase(Request.CANCEL) && viaHeader == null) {
		    	boolean addViaHeader = false;
		    	if(proxy == null) {
		    		addViaHeader = true;
		    	} else if(isInitial) {
		    		if(proxy.getRecordRoute()) {
		    			addViaHeader = true;
		    		}
		    	} else if(proxy.getFinalBranchForSubsequentRequests() != null && proxy.getFinalBranchForSubsequentRequests().getRecordRoute()) {
		    		addViaHeader = true;
		    	}
		    	if(addViaHeader) {
		    		if(logger.isDebugEnabled()) {
				    	logger.debug("Adding via Header");
				    }
		    		
		    		viaHeader = JainSipUtils.createViaHeader(
		    				sipNetworkInterfaceManager, request, null);
		    		message.addHeader(viaHeader);
			    }
		    }
		    
		    if(logger.isDebugEnabled()) {
		    	logger.debug("The found transport for sending request is '" + transport + "'");
		    }
			if(Request.ACK.equals(method)) {
				super.session.getSessionCreatingDialog().sendAck(request);
				return;
			}
			//Added for initial requests only (that are not REGISTER) not for subsequent requests 
			if(isInitial() && !Request.REGISTER.equalsIgnoreCase(method)) {
				// Additional checks for https://sip-servlets.dev.java.net/issues/show_bug.cgi?id=29
//				if(session.getProxy() != null &&
//						session.getProxy().getRecordRoute()) // If the app is proxying it already does that
//				{
//					if(logger.isDebugEnabled()) {
//						logger.debug("Add a record route header for app composition ");
//					}
//					getSipSession().getSipApplicationSession().getSipContext().getSipManager().dumpSipSessions();
//					//Add a record route header for app composition		
//					addAppCompositionRRHeader();	
//				}
				final SipApplicationRouterInfo routerInfo = sipFactoryImpl.getNextInterestedApplication(this);
				if(routerInfo.getNextApplicationName() != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("routing back to the container " +
								"since the following app is interested " + routerInfo.getNextApplicationName());
					}
					//add a route header to direct the request back to the container 
					//to check if there is any other apps interested in it
					addInfoForRoutingBackToContainer(routerInfo, session.getSipApplicationSession().getKey().getId(), session.getKey().getApplicationName());
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("routing outside the container " +
								"since no more apps are interested.");
					}
					//Adding Route Header for LB if we are in a HA configuration
					if(sipFactoryImpl.isUseLoadBalancer() && isInitial) {
						sipFactoryImpl.addLoadBalancerRouteHeader(request);
						if(logger.isDebugEnabled()) {
							logger.debug("adding route to Load Balancer since we are in a HA configuration " +
									" and no more apps are interested.");
						}
					}
				}
			}
			if(logger.isDebugEnabled()) {
				getSipSession().getSipApplicationSession().getSipContext().getSipManager().dumpSipSessions();
			}
			if (super.getTransaction() == null) {				

				final SipProvider sipProvider = sipNetworkInterfaceManager.findMatchingListeningPoint(
						transport, false).getSipProvider();
				
				ContactHeader contactHeader = (ContactHeader)request.getHeader(ContactHeader.NAME);
				if(contactHeader == null) {
					final FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
					final javax.sip.address.URI fromUri = fromHeader.getAddress().getURI();
					String fromName = null;
					if(fromUri instanceof javax.sip.address.SipURI) {
						fromName = ((javax.sip.address.SipURI)fromUri).getUser();
					}
					// Create the contact name address.
					contactHeader = 
						JainSipUtils.createContactHeader(sipNetworkInterfaceManager, request, fromName);										
					request.addHeader(contactHeader);
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("Getting new Client Tx for request " + request);
				}
				final ClientTransaction ctx = sipProvider
						.getNewClientTransaction(request);				
				
				super.session.setSessionCreatingTransaction(ctx);
				
				Dialog dialog = ctx.getDialog();
				
				if(super.session.getProxy() != null) dialog = null;
				
				if (dialog == null && this.createDialog) {					
					dialog = sipProvider.getNewDialog(ctx);
					super.session.setSessionCreatingDialog(dialog);
					if(logger.isDebugEnabled()) {
						logger.debug("new Dialog for request " + request + ", ref = " + dialog);
					}
				}

				//Keeping the transactions mapping in application data for CANCEL handling
				if(linkedRequest != null) {
					final Transaction linkedTransaction = linkedRequest.getTransaction();
					final Dialog linkedDialog = linkedRequest.getDialog();
					//keeping the client transaction in the server transaction's application data
					((TransactionApplicationData)linkedTransaction.getApplicationData()).setTransaction(ctx);
					if(linkedDialog != null && linkedDialog.getApplicationData() != null) {
						((TransactionApplicationData)linkedDialog.getApplicationData()).setTransaction(ctx);
					}
					//keeping the server transaction in the client transaction's application data
					this.transactionApplicationData.setTransaction(linkedTransaction);
					if(dialog!= null && dialog.getApplicationData() != null) {
						((TransactionApplicationData)dialog.getApplicationData()).setTransaction(linkedTransaction);
					}
				}
				// Make the dialog point here so that when the dialog event
				// comes in we can find the session quickly.
				if (dialog != null) {
					dialog.setApplicationData(this.transactionApplicationData);
				}
				
				// SIP Request is ALWAYS pointed to by the client tx.
				// Notice that the tx appplication data is cached in the request
				// copied over to the tx so it can be quickly accessed when response
				// arrives.				
				ctx.setApplicationData(this.transactionApplicationData);

				super.setTransaction(ctx);

			} else if (Request.PRACK.equals(request.getMethod())) {
				final SipProvider sipProvider = sipNetworkInterfaceManager.findMatchingListeningPoint(
						transport, false).getSipProvider();				
				final ClientTransaction ctx = sipProvider.getNewClientTransaction(request);
				//Keeping the transactions mapping in application data for CANCEL handling
				if(linkedRequest != null) {
					//keeping the client transaction in the server transaction's application data
					((TransactionApplicationData)linkedRequest.getTransaction().getApplicationData()).setTransaction(ctx);
					if(linkedRequest.getDialog() != null && linkedRequest.getDialog().getApplicationData() != null) {
						((TransactionApplicationData)linkedRequest.getDialog().getApplicationData()).setTransaction(ctx);
					}
					//keeping the server transaction in the client transaction's application data
					this.transactionApplicationData.setTransaction(linkedRequest.getTransaction());
					if(dialog!= null && dialog.getApplicationData() != null) {
						((TransactionApplicationData)dialog.getApplicationData()).setTransaction(linkedRequest.getTransaction());
					}
				}
				// SIP Request is ALWAYS pointed to by the client tx.
				// Notice that the tx appplication data is cached in the request
				// copied over to the tx so it can be quickly accessed when response
				// arrives.				
				ctx.setApplicationData(this.transactionApplicationData);
				
				setTransaction(ctx);
			}

			//tells the application dispatcher to stop routing the linked request
			// (in this case it would be the original request) since it has been relayed
			if(linkedRequest != null && 
					!SipApplicationRoutingDirective.NEW.equals(routingDirective)) {
				if(!RoutingState.PROXIED.equals(linkedRequest.getRoutingState())) {
					linkedRequest.setRoutingState(RoutingState.RELAYED);
				}
			}			
			session.addOngoingTransaction(getTransaction());
			// Update Session state
			super.session.updateStateOnSubsequentRequest(this, false);			
			if(Request.NOTIFY.equals(getMethod())) {
				final SubscriptionStateHeader subscriptionStateHeader = (SubscriptionStateHeader) 
					getMessage().getHeader(SubscriptionStateHeader.NAME);		
			
				// RFC 3265 : If a matching NOTIFY request contains a "Subscription-State" of "active" or "pending", it creates
				// a new subscription and a new dialog (unless they have already been
				// created by a matching response, as described above).
				if (subscriptionStateHeader != null && 
									(SubscriptionStateHeader.ACTIVE.equalsIgnoreCase(subscriptionStateHeader.getState()) ||
									SubscriptionStateHeader.PENDING.equalsIgnoreCase(subscriptionStateHeader.getState()))) {					
					session.addSubscription(this);
				}
				// A subscription is destroyed when a notifier sends a NOTIFY request
				// with a "Subscription-State" of "terminated".
				if (subscriptionStateHeader != null && 
									SubscriptionStateHeader.TERMINATED.equalsIgnoreCase(subscriptionStateHeader.getState())) {
					session.removeSubscription(this);
				}
			}		
			final MobicentsSipApplicationSession sipApplicationSession = session.getSipApplicationSession();		
			viaHeader.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME,
					sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(session.getKey().getApplicationName()));
			viaHeader.setParameter(MessageDispatcher.APP_ID,
					sipApplicationSession.getKey().getId());
			//updating the last accessed times 
			session.access();
			sipApplicationSession.access();
			Dialog dialog = getDialog();
			if(session.getProxy() != null) dialog = null;
			// If dialog does not exist or has no state.
			if (dialog == null || dialog.getState() == null
					|| (dialog.getState() == DialogState.EARLY && !Request.PRACK.equals(method))) {
				if(logger.isInfoEnabled()) {
					logger.info("Sending the request " + request);
				}
				((ClientTransaction) super.getTransaction()).sendRequest();
			} else {
				// This is a subsequent (an in-dialog) request. 
				// we don't redirect it to the container for now
				if(logger.isInfoEnabled()) {
					logger.info("Sending the in dialog request " + request);
				}
				dialog.sendRequest((ClientTransaction) getTransaction());
			}			
			isMessageSent = true;								
		} catch (Exception ex) {			
			throw new IllegalStateException("Error sending request",ex);
		}

	}

	/**
	 * Add a route header to route back to the container
	 * @param applicationName the application name that was chosen by the AR to route the request
	 * @throws ParseException
	 * @throws SipException 
	 * @throws NullPointerException 
	 */
	public void addInfoForRoutingBackToContainer(SipApplicationRouterInfo routerInfo, String applicationSessionId, String applicationName) throws ParseException, SipException {		
		final Request request = (Request) super.message;
		final javax.sip.address.SipURI sipURI = JainSipUtils.createRecordRouteURI(
				sipFactoryImpl.getSipNetworkInterfaceManager(), 
				request);
		sipURI.setLrParam();
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_DIRECTIVE, 
				routingDirective.toString());
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_PREV_APPLICATION_NAME, 
				applicationName);
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_PREV_APP_ID, 
				applicationSessionId);
		final javax.sip.address.Address routeAddress = 
			SipFactories.addressFactory.createAddress(sipURI);
		final RouteHeader routeHeader = 
			SipFactories.headerFactory.createRouteHeader(routeAddress);
		request.addFirst(routeHeader);		
		// adding the application router info to avoid calling the AppRouter twice
		// See Issue 791 : http://code.google.com/p/mobicents/issues/detail?id=791
		session.setNextSipApplicationRouterInfo(routerInfo);
	}

	/**
	 * Add a record route header for app composition
	 * @throws ParseException if anything goes wrong while creating the record route header
	 * @throws SipException 
	 * @throws NullPointerException 
	 */
	public void addAppCompositionRRHeader()
			throws ParseException, SipException {
		Request request = (Request) super.message;
        
        javax.sip.address.SipURI sipURI = JainSipUtils.createRecordRouteURI(
                        sipFactoryImpl.getSipNetworkInterfaceManager(), 
                        request);
        sipURI.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME, session.getKey().getApplicationName());
        
        sipURI.setLrParam();
        javax.sip.address.Address recordRouteAddress = 
                SipFactories.addressFactory.createAddress(sipURI);
        RecordRouteHeader recordRouteHeader = 
                SipFactories.headerFactory.createRecordRouteHeader(recordRouteAddress);
        request.addFirst(recordRouteHeader);
	}

	public void setLinkedRequest(SipServletRequestImpl linkedRequest) {
		this.linkedRequest = linkedRequest;

	}

	public SipServletRequestImpl getLinkedRequest() {
		return this.linkedRequest;
	}	

	/**
	 * @return the routingState
	 */
	public RoutingState getRoutingState() {
		return routingState;
	}

	/**
	 * @param routingState the routingState to set
	 */
	public void setRoutingState(RoutingState routingState) throws IllegalStateException {
		//JSR 289 Section 11.2.3 && 10.2.6
		if(routingState.equals(RoutingState.CANCELLED) && 
				(this.routingState.equals(RoutingState.FINAL_RESPONSE_SENT) || 
						this.routingState.equals(RoutingState.PROXIED))) {
			throw new IllegalStateException("Cannot cancel final response already sent!");
		}
		if((routingState.equals(RoutingState.FINAL_RESPONSE_SENT)|| 
				routingState.equals(RoutingState.PROXIED)) && this.routingState.equals(RoutingState.CANCELLED)) {
			throw new IllegalStateException("Cancel received and already replied with a 487!");
		}
		if(routingState.equals(RoutingState.SUBSEQUENT)) {
			isInitial = false;
		}
		this.routingState = routingState;	
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#addAuthHeader(javax.servlet.sip.SipServletResponse, javax.servlet.sip.AuthInfo)
	 */
	public void addAuthHeader(SipServletResponse challengeResponse,
			AuthInfo authInfo) {
		checkReadOnly();
		AuthInfoImpl authInfoImpl = (AuthInfoImpl) authInfo;
		
		SipServletResponseImpl challengeResponseImpl = 
			(SipServletResponseImpl) challengeResponse;
		
		Response response = (Response) challengeResponseImpl.getMessage();
		// First check for WWWAuthentication headers
		ListIterator authHeaderIterator = 
			response.getHeaders(WWWAuthenticateHeader.NAME);
		while(authHeaderIterator.hasNext()) {
			WWWAuthenticateHeader wwwAuthHeader = 
				(WWWAuthenticateHeader) authHeaderIterator.next();
			String uri = wwwAuthHeader.getParameter("uri");
			AuthInfoEntry authInfoEntry = authInfoImpl.getAuthInfo(wwwAuthHeader.getRealm());
			
			if(authInfoEntry == null) throw new SecurityException(
					"Cannot add authorization header. No credentials for the following realm: " + wwwAuthHeader.getRealm());
			
			addChallengeResponse(wwwAuthHeader,
					authInfoEntry.getUserName(),
					authInfoEntry.getPassword(),
					this.getRequestURI().toString());
		}
		
		// Now check for Proxy-Authentication
		authHeaderIterator = 
			response.getHeaders(ProxyAuthenticateHeader.NAME);
		while(authHeaderIterator.hasNext()) {
			ProxyAuthenticateHeader wwwAuthHeader = 
				(ProxyAuthenticateHeader) authHeaderIterator.next();
			String uri = wwwAuthHeader.getParameter("uri");
			AuthInfoEntry authInfoEntry = authInfoImpl.getAuthInfo(wwwAuthHeader.getRealm());
			
			if(authInfoEntry == null) throw new SecurityException(
					"No credentials for the following realm: " + wwwAuthHeader.getRealm());
			
			addChallengeResponse(wwwAuthHeader,
					authInfoEntry.getUserName(),
					authInfoEntry.getPassword(),
					this.getRequestURI().toString());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#addAuthHeader(javax.servlet.sip.SipServletResponse, java.lang.String, java.lang.String)
	 */
	public void addAuthHeader(SipServletResponse challengeResponse,
			String username, String password) {
		checkReadOnly();
		SipServletResponseImpl challengeResponseImpl = 
			(SipServletResponseImpl) challengeResponse;
		
		Response response = (Response) challengeResponseImpl.getMessage();
		ListIterator authHeaderIterator = 
			response.getHeaders(WWWAuthenticateHeader.NAME);
		
		// First
		while(authHeaderIterator.hasNext()) {
			WWWAuthenticateHeader wwwAuthHeader = 
				(WWWAuthenticateHeader) authHeaderIterator.next();
			String uri = wwwAuthHeader.getParameter("uri");
			addChallengeResponse(wwwAuthHeader, username, password, this.getRequestURI().toString());
		}
		
		
		authHeaderIterator = 
			response.getHeaders(ProxyAuthenticateHeader.NAME);
		
		while(authHeaderIterator.hasNext()) {
			ProxyAuthenticateHeader wwwAuthHeader = 
				(ProxyAuthenticateHeader) authHeaderIterator.next();
			String uri = wwwAuthHeader.getParameter("uri");
			if(uri == null) uri = this.getRequestURI().toString();
			addChallengeResponse(wwwAuthHeader, username, password, uri);
		}
	}
	
	private void addChallengeResponse(
			WWWAuthenticateHeader wwwAuthHeader,
			String username,
			String password,
			String uri) {
		AuthorizationHeader authorization = DigestAuthenticator.getAuthorizationHeader(
				getMethod(),
				uri,
				"", // TODO: What is this entity-body?
				wwwAuthHeader,
				username,
				password);
		
		message.addHeader(authorization);
	}

	/**
	 * @return the finalResponse
	 */
	public SipServletResponse getLastFinalResponse() {
		return lastFinalResponse;
	}

	/**
	 * @param finalResponse the finalResponse to set
	 */
	public void setResponse(SipServletResponse response) {
		if(response.getStatus() >= 200 && 
				(lastFinalResponse == null || lastFinalResponse.getStatus() < response.getStatus())) {
			this.lastFinalResponse = response;
		}
		if((response.getStatus() > 100 && response.getStatus() < 200) && 
				(lastInformationalResponse == null || lastInformationalResponse.getStatus() < response.getStatus())) {
			this.lastInformationalResponse = response;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Address getInitialPoppedRoute() {
		return transactionApplicationData.getInitialPoppedRoute();
	}

	/**
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	public URI getSubscriberURI() {		
		return subscriberURI;
	}	
	
	public void setSubscriberURI(URI uri) {		
		this.subscriberURI = uri;
	}
	
	/**
	 * Method checking whether or not the sip servlet request in parameter is initial
	 * according to algorithm defined in JSR289 Appendix B
	 * @param sipServletRequest the sip servlet request to check
	 * @param dialog the dialog associated with this request
	 * @return true if the request is initial false otherwise
	 */
	private static RoutingState checkRoutingState(SipServletRequestImpl sipServletRequest, Dialog dialog) {
		// 2. Ongoing Transaction Detection - Employ methods of Section 17.2.3 in RFC 3261 
		//to see if the request matches an existing transaction. 
		//If it does, stop. The request is not an initial request.
		if(dialog != null && DialogState.CONFIRMED.equals(dialog.getState())) {
			return RoutingState.SUBSEQUENT;
		}		
		// 3. Examine Request Method. If it is CANCEL, BYE, PRACK or ACK, stop. 
		//The request is not an initial request for which application selection occurs.
		if(nonInitialSipRequestMethods.contains(sipServletRequest.getMethod())) {
			return RoutingState.SUBSEQUENT;
		}
		// 4. Existing Dialog Detection - If the request has a tag in the To header field, 
		// the container computes the dialog identifier (as specified in section 12 of RFC 3261) 
		// corresponding to the request and compares it with existing dialogs. 
		// If it matches an existing dialog, stop. The request is not an initial request. 
		// The request is a subsequent request and must be routed to the application path 
		// associated with the existing dialog. 
		// If the request has a tag in the To header field, 
		// but the dialog identifier does not match any existing dialogs, 
		// the container must reject the request with a 481 (Call/Transaction Does Not Exist). 
		// Note: When this occurs, RFC 3261 says either the UAS has crashed or the request was misrouted. 
		// In the latter case, the misrouted request is best handled by rejecting the request. 
		// For the Sip Servlet environment, a UAS crash may mean either an application crashed 
		// or the container itself crashed. In either case, it is impossible to route the request 
		// as a subsequent request and it is inappropriate to route it as an initial request. 
		// Therefore, the only viable approach is to reject the request.
		if(dialog != null && !DialogState.EARLY.equals(dialog.getState())) {
			return RoutingState.SUBSEQUENT;
		}
		
		// 6. Detection of Requests Sent to Encoded URIs - 
		// Requests may be sent to a container instance addressed to a URI obtained by calling 
		// the encodeURI() method of a SipApplicationSession managed by this container instance. 
		// When a container receives such a request, stop. This request is not an initial request. 
		// Refer to section 15.11.1 Session Targeting and Application Selection 
		//for more information on how a request sent to an encoded URI is handled by the container.
		//This part will be done in routeIntialRequest since this is where the Session Targeting retrieval is done
		
		return RoutingState.INITIAL;		
	}

	/**
	 * @return the is1xxResponseGenerated
	 */
	public boolean is1xxResponseGenerated() {
		return is1xxResponseGenerated;
	}
	
	/**
	 * @return the isFinalResponseGenerated
	 */
	public boolean isFinalResponseGenerated() {
		return isFinalResponseGenerated;
	}

	/**
	 * @return the lastInformationalResponse
	 */
	public SipServletResponse getLastInformationalResponse() {
		return lastInformationalResponse;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}
	
	protected void checkReadOnly() {
		if(isReadOnly) {
			throw new IllegalStateException(EXCEPTION_MESSAGE);
		}
	}
	
	@Override
	public void addAcceptLanguage(Locale locale) {
		checkReadOnly();
		super.addAcceptLanguage(locale);
	}
	
	@Override
	public void addAddressHeader(String name, Address addr, boolean first)
			throws IllegalArgumentException {
		checkReadOnly();
		super.addAddressHeader(name, addr, first);
	}
	
	@Override
	public void addHeader(String name, String value) {
		checkReadOnly();
		super.addHeader(name, value);
	}
	
	@Override
	public void addParameterableHeader(String name, Parameterable param,
			boolean first) {
		checkReadOnly();
		super.addParameterableHeader(name, param, first);
	}
	
	@Override
	public void removeAttribute(String name) {
		checkReadOnly();
		super.removeAttribute(name);
	}
	
	@Override
	public void removeHeader(String name) {
		checkReadOnly();
		super.removeHeader(name);
	}
	
	@Override
	public void setAcceptLanguage(Locale locale) {
		checkReadOnly();
		super.setAcceptLanguage(locale);
	}
	
	@Override
	public void setAddressHeader(String name, Address addr) {
		checkReadOnly();
		super.setAddressHeader(name, addr);
	}
	
	@Override
	public void setAttribute(String name, Object o) {
		checkReadOnly();
		super.setAttribute(name, o);
	}
	
	@Override
	public void setCharacterEncoding(String enc)
			throws UnsupportedEncodingException {
		checkReadOnly();
		super.setCharacterEncoding(enc);
	}
	
	@Override
	public void setContent(Object content, String contentType)
			throws UnsupportedEncodingException {
		checkReadOnly();
		super.setContent(content, contentType);
	}
	
	@Override
	public void setContentLanguage(Locale locale) {
		checkReadOnly();
		super.setContentLanguage(locale);
	}
	
	@Override
	public void setContentType(String type) {
		checkReadOnly();
		super.setContentType(type);
	}
	
	@Override
	public void setExpires(int seconds) {
		checkReadOnly();
		super.setExpires(seconds);
	}
	
	@Override
	public void setHeader(String name, String value) {
		checkReadOnly();
		super.setHeader(name, value);
	}
	
	@Override
	public void setHeaderForm(HeaderForm form) {
		checkReadOnly();
		super.setHeaderForm(form);
	}
	
	@Override
	public void setParameterableHeader(String name, Parameterable param) {
		checkReadOnly();
		super.setParameterableHeader(name, param);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getInitialRemoteAddr() {		
		if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
			return ((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress();
		} else {
			return ((SIPTransaction)getTransaction()).getPeerAddress();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitialRemotePort() {		
		if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
			return ((SIPTransaction)getTransaction()).getPeerPacketSourcePort();
		} else {
			return ((SIPTransaction)getTransaction()).getPeerPort();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialTransport() {		
		return ((SIPTransaction)getTransaction()).getTransport();
	}
}
