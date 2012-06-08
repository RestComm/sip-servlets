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

package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.IllegalTransactionStateException;
import gov.nist.javax.sip.stack.IllegalTransactionStateException.Reason;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.address.Hop;
import javax.sip.address.TelURL;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSAwareRouter;
import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.address.GenericURIImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.MobicentsSipServlet;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.b2bua.MobicentsB2BUAHelper;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxy;
import org.mobicents.servlet.sip.core.security.MobicentsAuthInfoEntry;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SipRequestDispatcher;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.security.AuthInfoEntry;
import org.mobicents.servlet.sip.security.AuthInfoImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

public abstract class SipServletRequestImpl extends SipServletMessageImpl implements
		MobicentsSipServletRequest {

	public static final String STALE = "stale";

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(SipServletRequestImpl.class);
	
	private static final String EXCEPTION_MESSAGE = "The context does not allow you to modify this request !";
	
	public static final Set<String> NON_INITIAL_SIP_REQUEST_METHODS = new HashSet<String>();
	
	static {
		NON_INITIAL_SIP_REQUEST_METHODS.add("CANCEL");
		NON_INITIAL_SIP_REQUEST_METHODS.add("BYE");
		NON_INITIAL_SIP_REQUEST_METHODS.add("PRACK");
		NON_INITIAL_SIP_REQUEST_METHODS.add("ACK");
		NON_INITIAL_SIP_REQUEST_METHODS.add("UPDATE");
		NON_INITIAL_SIP_REQUEST_METHODS.add("INFO");		
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
	
	// This field is only used in CANCEL requests where we need the INVITe transaction
	private transient Transaction inviteTransactionToCancel;
	
	private boolean orphanRequest;
	
	// needed for externalizable
	public SipServletRequestImpl () {}
	
	protected SipServletRequestImpl(Request request, SipFactoryImpl sipFactoryImpl,
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
	public ModifiableRule getModifiableRule(String headerName) {

		String hName = getFullHeaderName(headerName);

		/*
		 * Contact is a system header field in messages other than REGISTER
		 * requests and responses, 3xx and 485 responses, and 200/OPTIONS
		 * responses so it is not contained in system headers and as such
		 * as a special treatment
		 */
		boolean isSystemHeader = JainSipUtils.SYSTEM_HEADERS.contains(hName);

		if (isSystemHeader) {
			return ModifiableRule.NotModifiable;
		}
		// Issue http://code.google.com/p/mobicents/issues/detail?id=2467
		// From and to Header are modifiable except the Tag parameter in a request that is not committed
		if(headerName.equalsIgnoreCase(FromHeader.NAME)) {
			if(!isCommitted()) {
				return ModifiableRule.From;
			} else {
				return ModifiableRule.NotModifiable;
			}
		}
		if(headerName.equalsIgnoreCase(ToHeader.NAME)) {
			if (!isCommitted()) {
				return ModifiableRule.To;
			} else {
				return ModifiableRule.NotModifiable;
			}
		}
		
		if(hName.equals(ContactHeader.NAME)) {
			Request request = (Request) this.message;
	
			String method = request.getMethod();
			if (method.equals(Request.REGISTER)) {
				return ModifiableRule.ContactNotSystem;
			} else {
				return ModifiableRule.ContactSystem;
			}				
		} else {
			return ModifiableRule.Modifiable;
		}
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
			if(lastFinalResponse != null) {
				throw new IllegalStateException("final response already sent : " + lastFinalResponse);
			} else {
				throw new IllegalStateException("final response already sent!");
			}
		}
		
		try {			
			Request cancelRequest = ((ClientTransaction) getTransaction())
					.createCancel();
			SipServletRequestImpl newRequest = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest(
					cancelRequest, getSipSession(),
					null, getTransaction().getDialog(), false);
			newRequest.inviteTransactionToCancel = super.getTransaction();
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
		return createResponse(statusCode, null, true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#createResponse(int, java.lang.String)
	 */
	public SipServletResponse createResponse(final int statusCode, final String reasonPhrase) {
		return createResponse(statusCode, reasonPhrase, true);
	}		
	
	public SipServletResponse createResponse(final int statusCode, final String reasonPhrase, boolean validate) {
		checkReadOnly();
		final Transaction transaction = getTransaction();
		if(RoutingState.CANCELLED.equals(routingState)) {
			throw new IllegalStateException("Cannot create a response for the invite, a CANCEL has been received and the INVITE was replied with a 487!");
		}
		if(validate) {
			if (transaction == null) {
				throw new IllegalStateException(
					"Cannot create a response for request " + message + " transaction is null, a final error response has probably already been sent");
			}
			if(transaction instanceof ClientTransaction) {
				throw new IllegalStateException(
					"Cannot create a response for request " + message + " not a server transaction " + transaction);
			}
		}
		try {
			final Request request = (Request) this.getMessage();
			final Response response = SipFactoryImpl.messageFactory.createResponse(
					statusCode, request);			
			if(reasonPhrase!=null) {
				response.setReasonPhrase(reasonPhrase);
			}
			final MobicentsSipSession session = getSipSession();
			final String requestMethod = getMethod();
			//add a To tag for all responses except Trying (or trying too if it's a subsequent request)
			if((statusCode > Response.TRYING || !isInitial()) && statusCode <= Response.SESSION_NOT_ACCEPTABLE) {
				final ToHeader toHeader = (ToHeader) response
					.getHeader(ToHeader.NAME);
				// If we already have a to tag, dont create new
				if (toHeader.getTag() == null) {					
					// if a dialog has already been created
					// reuse local tag
					final Dialog dialog = transaction.getDialog();
					if(session != null && dialog != null && dialog.getLocalTag() != null && dialog.getLocalTag().length() > 0
							&& session.getKey().getToTag() != null && session.getKey().getToTag().length() >0) {
						if(!dialog.getLocalTag().equals(session.getKey().getToTag())) {
							// Issue 2354 : if the dialog to tag is different than the  session to tag use the session to tag
							// so that we send the forked response out with the correct to tag
							if(logger.isDebugEnabled()) {
						    	logger.debug("setting session ToTag: " + session.getKey().getToTag());
						    }
							toHeader.setTag(session.getKey().getToTag());
						} else {
							if(logger.isDebugEnabled()) {
						    	logger.debug("setting dialog LocalTag: " + dialog.getLocalTag());
						    }
							toHeader.setTag(dialog.getLocalTag());
						}
					} else if(session != null && session.getSipApplicationSession() != null) {						
						final MobicentsSipApplicationSessionKey sipAppSessionKey = session.getSipApplicationSession().getKey();
						final MobicentsSipSessionKey sipSessionKey = session.getKey();
						// Fix for Issue 1044 : javax.sip.SipException: Tag mismatch dialogTag during process B2B response
						// make sure not to generate a new tag
						synchronized (this) {
							String toTag = sipSessionKey.getToTag();
							if(logger.isDebugEnabled()) {
						    	logger.debug("sipSessionKey ToTag : " + toTag);
						    }
							if(toTag == null) {
								toTag = ApplicationRoutingHeaderComposer.getHash(sipFactoryImpl.getSipApplicationDispatcher(),sipSessionKey.getApplicationName(), sipAppSessionKey.getId());
								//need to stay false for TCK Test com.bea.sipservlet.tck.agents.api.javax_servlet_sip.SipSessionListenerTest.testSessionDestroyed001
								session.getKey().setToTag(toTag, false);
							}
							if(logger.isDebugEnabled()) {
						    	logger.debug("setting ToTag: " + toTag);
						    }
							toHeader.setTag(toTag);	
						}						
					} else {							
						//if the sessions are null, it means it is a cancel response
						toHeader.setTag(Integer.toString(new Random().nextInt(10000000)));
					}
				}
				// Following restrictions in JSR 289 Section 4.1.3 Contact Header Field
				// + constraints from Issue 1687 : Contact Header is present in SIP Message where it shouldn't
				boolean setContactHeader = true;
				if ((statusCode >= 300 && statusCode < 400) || statusCode == 485 
						|| Request.REGISTER.equals(requestMethod) || Request.OPTIONS.equals(requestMethod)
						|| Request.BYE.equals(requestMethod) || Request.CANCEL.equals(requestMethod)
						|| Request.PRACK.equals(requestMethod) || Request.MESSAGE.equals(requestMethod)
						|| Request.PUBLISH.equals(requestMethod)) {
					// don't set the contact header in those case
					setContactHeader = false;					
				} 				
				if(setContactHeader) {	
					String outboundInterface = null;
					if(session != null) {
						outboundInterface = session.getOutboundInterface();
					}		
				    // Add the contact header for the dialog.				    
				    ContactHeader contactHeader = JainSipUtils.createContactHeader(
			    			super.sipFactoryImpl.getSipNetworkInterfaceManager(), request, null, null, outboundInterface);
				    String transport = "udp";
				    if(session != null && session.getTransport() != null) transport = session.getTransport();
					SipConnector sipConnector = StaticServiceHolder.sipStandardService.findSipConnector(transport);
					if(sipConnector != null && sipConnector.isUseStaticAddress()) {
						if(session != null && session.getProxy() == null) {
							boolean sipURI = contactHeader.getAddress().getURI().isSipURI();
							if(sipURI) {
								javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) contactHeader.getAddress().getURI();
								sipUri.setHost(sipConnector.getStaticServerAddress());
								sipUri.setPort(sipConnector.getStaticServerPort());
							}
						}
					}
				    if(logger.isDebugEnabled()) {
				    	logger.debug("We're adding this contact header to our new response: '" + contactHeader + ", transport=" + JainSipUtils.findTransport(request));
				    }
				    response.setHeader(contactHeader);
			    }
			}
			// Issue 1355 http://code.google.com/p/mobicents/issues/detail?id=1355 Not RFC compliant :
			// Application Routing : Adding the recorded route headers as route headers
//			final ListIterator<RecordRouteHeader> recordRouteHeaders = request.getHeaders(RecordRouteHeader.NAME);
//			while (recordRouteHeaders.hasNext()) {
//				RecordRouteHeader recordRouteHeader = (RecordRouteHeader) recordRouteHeaders
//						.next();
//				RouteHeader routeHeader = SipFactoryImpl.headerFactory.createRouteHeader(recordRouteHeader.getAddress());
//				response.addHeader(routeHeader);
//			}
			
			if(session != null && session.getCopyRecordRouteHeadersOnSubsequentResponses() && !isInitial() && Request.INVITE.equals(requestMethod)) {
				// Miss Record-Route in Response for non compliant Server in reINVITE http://code.google.com/p/mobicents/issues/detail?id=2066
				final ListIterator<RecordRouteHeader> recordRouteHeaders = request.getHeaders(RecordRouteHeader.NAME);
				while (recordRouteHeaders.hasNext()) {
					RecordRouteHeader recordRouteHeader = (RecordRouteHeader) recordRouteHeaders
							.next();
					response.addHeader(recordRouteHeader);
				}
			}
			
			final SipServletResponseImpl newSipServletResponse = (SipServletResponseImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletResponse(
					response, 
					validate ? (ServerTransaction) transaction : transaction, session, getDialog(), false, false);
			newSipServletResponse.setOriginalRequest(this);
			if(!Request.PRACK.equals(requestMethod) && statusCode >= Response.OK && 
					statusCode <= Response.SESSION_NOT_ACCEPTABLE) {	
				isFinalResponseGenerated = true;
			}
			if(statusCode >= Response.TRYING && 
					statusCode < Response.OK) {	
				is1xxResponseGenerated = true;
			}
			return newSipServletResponse;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code " + statusCode,
					ex);
		}		
	}

	public B2buaHelper getB2buaHelper() {
		checkReadOnly();
		final MobicentsSipSession session = getSipSession();
		if (session.getProxy() != null)
			throw new IllegalStateException("Proxy already present");
		MobicentsB2BUAHelper b2buaHelper = session.getB2buaHelper();
		if (b2buaHelper != null)
			return b2buaHelper;
		b2buaHelper = new B2buaHelperImpl();
		b2buaHelper.setMobicentsSipFactory(sipFactoryImpl);
		b2buaHelper.setSipManager(session.getSipApplicationSession().getSipContext().getSipManager());
		if(JainSipUtils.DIALOG_CREATING_METHODS.contains(getMethod())) {
			this.createDialog = true; // flag that we want to create a dialog for outgoing request.
		}
		session.setB2buaHelper(b2buaHelper);
		return b2buaHelper;
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
			this.poppedRoute = new AddressImpl(poppedRouteHeader.getAddress(), null, getTransaction() == null ? ModifiableRule.Modifiable : ModifiableRule.NotModifiable);
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
		final MobicentsSipSession session = getSipSession();
		if (session.getB2buaHelper() != null ) throw new IllegalStateException("Cannot proxy request");
		return getProxy(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Proxy getProxy(boolean create) throws TooManyHopsException {
		checkReadOnly();
		final MobicentsSipSession session = getSipSession();
		if (session.getB2buaHelper() != null ) throw new IllegalStateException("Cannot proxy request");
		
		final MaxForwardsHeader mfHeader = (MaxForwardsHeader)this.message.getHeader(MaxForwardsHeader.NAME);
		if(mfHeader.getMaxForwards()<=0) {
			try {
				this.createResponse(Response.TOO_MANY_HOPS, "Too many hops").send();
			} catch (IOException e) {
				throw new RuntimeException("could not send the Too many hops response out !", e);
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
			MobicentsProxy proxy = session.getProxy();
			boolean createNewProxy = false;
			if(isInitial() && proxy != null && proxy.getOriginalRequest() == null)  {
				createNewProxy = true;
			}
			if(proxy == null || createNewProxy) {				
				session.setProxy(new ProxyImpl(this, super.sipFactoryImpl));
			}
		}
		return session.getProxy();
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
					.getRequestURI(), ModifiableRule.Modifiable);
		else if (request.getRequestURI() instanceof javax.sip.address.TelURL)
			return new TelURLImpl((javax.sip.address.TelURL) request
					.getRequestURI());
		else 
			// From horacimacias : Fix for Issue 2115 MSS unable to handle GenericURI URIs
			return new GenericURIImpl(request.getRequestURI());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInitial() {
		return isInitial && !isOrphan(); 
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
			javax.sip.header.Header p = SipFactoryImpl.headerFactory
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
		if(!isInitial() && getSipSession().getProxy() == null) {
			//as per JSR 289 Section 11.1.3 Pushing Route Header Field Values
			// pushRoute can only be done on the initial requests. 
			// Subsequent requests within a dialog follow the route set. 
			// Any attempt to do a pushRoute on a subsequent request in a dialog 
			// MUST throw and IllegalStateException.
			throw new IllegalStateException("Cannot push route on subsequent requests, only intial ones");
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Pushing route into message of value [" + sipUri + "]");
			
			sipUri.setLrParam();
			try {
				javax.sip.header.Header p = SipFactoryImpl.headerFactory
						.createRouteHeader(SipFactoryImpl.addressFactory
								.createAddress(sipUri));
				this.message.addFirst(p);
			} catch (SipException e) {
				logger.error("Error while pushing route [" + sipUri + "]");
				throw new IllegalArgumentException("Error pushing route ", e);
			}
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
		final MobicentsSipSession session = getSipSession();
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

	public Enumeration<Locale> getLocales() {
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
		if(getTransaction() != null) {
			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null &&
					((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress() != null) {
				return ((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress();
			} else {
				return ((SIPTransaction)getTransaction()).getPeerAddress();
			}
		} else {
			ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
			if(via == null) {
				return null;
			} else {
				return via.getHost();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public javax.servlet.RequestDispatcher getRequestDispatcher(String handler) {
		MobicentsSipServlet sipServletImpl = (MobicentsSipServlet)
			getSipSession().getSipApplicationSession().getSipContext().findSipServletByName(handler);
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
	 * @see org.mobicents.servlet.sip.message.SipServletMessageImpl#send()
	 */
	@Override
	public void send() throws IOException {
		checkReadOnly();
		final Request request = (Request) super.message;
		final String requestMethod = getMethod();
		final SipApplicationDispatcher sipApplicationDispatcher = sipFactoryImpl.getSipApplicationDispatcher();
		final MobicentsSipSession session = getSipSession();
		final DNSServerLocator dnsServerLocator = sipApplicationDispatcher.getDNSServerLocator();
		Hop hop = null;
		// RFC 3263 support
		if(dnsServerLocator != null) {
			if(Request.CANCEL.equals(requestMethod)) {
				// RFC 3263 Section 4 : a CANCEL for a particular SIP request MUST be sent to the same SIP 
				// server that the SIP request was delivered to.
				TransactionApplicationData inviteTxAppData = ((TransactionApplicationData)inviteTransactionToCancel.getApplicationData());
				if(inviteTxAppData != null && inviteTxAppData.getHops() != null) {
					hop = inviteTxAppData.getHops().peek();
				}
			} else {
				javax.sip.address.URI uriToResolve =  request.getRequestURI();
				RouteHeader routeHeader = (RouteHeader) request.getHeader(RouteHeader.NAME);
				if(routeHeader != null) {
					uriToResolve = routeHeader.getAddress().getURI();
				} else {
					// RFC5626 - see if we are to find a flow for this request.
					// Note: we should do this even if the "uriToResolve" is coming
					// from a route header but since we currently have not implemented
					// the correct things for a proxy scenario, only do it for UAS
					// scenarios. At least this will minimize the potential for messing
					// up right now...
					uriToResolve = resolveSipOutbound(uriToResolve);
				}

				if(session.getProxy() == null && session.getTransport() != null && uriToResolve.isSipURI() && ((javax.sip.address.SipURI)uriToResolve).getTransportParam() == null &&
						// no need to modify the Request URI for UDP which is the default transport
						!session.getTransport().equalsIgnoreCase(ListeningPoint.UDP)) {					
					try {
						((javax.sip.address.SipURI)uriToResolve).setTransportParam(session.getTransport());
					} catch (ParseException e) {
						// nothing to do here, will never happen
					}
				}
				Queue<Hop> hops = dnsServerLocator.locateHops(uriToResolve);
				if(hops.size() > 0) {
					// RFC 3263 support don't remove the current hop, it will be the one to reuse for CANCEL and ACK to non 2xx transactions
					hop = hops.peek();
					transactionApplicationData.setHops(hops);				
				}
			}
		}
		send(hop);
	}
	
	
	/**
	 * Check to see if the uri to resolve contains a "ob" parameter and if so, try
	 * and locate a "flow" for this uri.
	 * 
	 * This is part of the RFC5626 implementation
	 * 
	 * @param uriToResolve
	 * @return the flow uri or if no flow was found, the same uri 
	 * that was passed into the method
	 */
	private javax.sip.address.URI resolveSipOutbound(final javax.sip.address.URI uriToResolve) {
		if (!uriToResolve.isSipURI()) {
			return uriToResolve;
		}

		final javax.sip.address.SipURI sipURI = (javax.sip.address.SipURI) uriToResolve;
		if (sipURI.getParameter(MessageDispatcher.SIP_OUTBOUND_PARAM_OB) == null) {
			// no ob parameter, return
			return uriToResolve;
		}
		final MobicentsSipSession session = getSipSession();
		final javax.sip.address.SipURI flow = session.getFlow();
		if (flow != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found a flow \"" + flow + "\" for the original uri \"" + uriToResolve + "\"");
			}
			return flow;
		}

		return uriToResolve;
	}
	
	public void send(Hop hop) throws IOException {
		final Request request = (Request) super.message;
		final String requestMethod = getMethod();
		final MobicentsSipSession session = getSipSession();
		final String sessionTransport = session.getTransport();
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipFactoryImpl.getSipNetworkInterfaceManager();
		final MobicentsSipApplicationSession sipApplicationSession = session.getSipApplicationSession();
		
		try {
			if(logger.isDebugEnabled()) {
		    	logger.debug("session transport is " + sessionTransport);
		    }
			// Because proxy decides transport in different way, it allows inbound and outbound transport to be different.
			if(session != null && session.getProxy() == null) {
				((MessageExt)message).setApplicationData(session.getTransport());			
			}		
			
			if(Request.CANCEL.equals(requestMethod)) {
				getSipSession().setRequestsPending(0);
	    		Transaction tx = inviteTransactionToCancel;
    			if(tx != null) {
    				// we rely on the transaction state to know if we send the cancel or not
    				if(tx.getState() == null || tx.getState().equals(TransactionState.CALLING) || tx.getState().equals(TransactionState.TRYING)) {
	    				logger.debug("Can not send CANCEL. Will try to STOP retransmissions " + tx + " tx state " + tx.getState());
	    				// We still haven't received any response on this call, so we can not send CANCEL,
	    				// we will just stop the retransmissions
	    				StaticServiceHolder.disableRetransmissionTimer.invoke(tx);
	    				if(tx.getApplicationData() instanceof TransactionApplicationData) {
	    					TransactionApplicationData tad = (TransactionApplicationData) tx.getApplicationData();
	    					tad.setCanceled(true);
	    				}
	    				return;
    				}
    			} else {
    				logger.debug("Can not send CANCEL because no responses arrived. " +
    						"Can not stop retransmissions. The transaction is null");
    			}
	    	} 						
			
			if(logger.isDebugEnabled()) {
				logger.debug("hop " + hop );
			}
			//Issue http://code.google.com/p/mobicents/issues/detail?id=3144
			//Store the transport of the resolved hop to be used later
			if(hop != null) {
				((MessageExt)message).setApplicationData(hop.getTransport());
			}
			
			// adding via header and update via branch if null
			checkViaHeaderAddition();
						
			final String transport = JainSipUtils.findTransport(request);
			if(sessionTransport == null) {
				session.setTransport(transport);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("The found transport for sending request is '" + transport + "'");
			}

			SipConnector sipConnector = StaticServiceHolder.sipStandardService.findSipConnector(transport);
			MobicentsExtendedListeningPoint matchingListeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(
					transport, false);
			final SipProvider sipProvider = matchingListeningPoint.getSipProvider();


			//we update the via header after the sip connector has been found for the correct transport
			checkViaHeaderUpdateForStaticExternalAddressUsage(sipConnector);
			
			if(Request.ACK.equals(requestMethod)) {
				// DNS Route need to be added for ACK
				// See RFC 3263 : "Because the ACK request for 2xx responses to INVITE constitutes a
				// different transaction, there is no requirement that it be delivered
				// to the same server that received the original request"
				addDnsRoute(hop, request);
				sendAck(transport, sipConnector, matchingListeningPoint);
				return;
			}						
			boolean addDNSRoute = true;
			//Added for initial requests only (that are not REGISTER) not for subsequent requests 
			if(isInitial() && !Request.REGISTER.equalsIgnoreCase(requestMethod)) {
				final SipApplicationRouterInfo routerInfo = sipFactoryImpl.getNextInterestedApplication(this);
				if(routerInfo.getNextApplicationName() != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("routing back to the container " +
								"since the following app is interested " + routerInfo.getNextApplicationName());
					}
					//add a route header to direct the request back to the container 
					//to check if there is any other apps interested in it
					addInfoForRoutingBackToContainer(routerInfo, session.getSipApplicationSession().getKey().getId(), session.getKey().getApplicationName());
					addDNSRoute = false;
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("routing outside the container " +
								"since no more apps are interested.");
					}
					//Adding Route Header for LB if we are in a HA configuration
					if(isInitial) {
						if(sipFactoryImpl.isUseLoadBalancer()) {
							sipFactoryImpl.addLoadBalancerRouteHeader(request);
							addDNSRoute = false;
							if(logger.isDebugEnabled()) {
								logger.debug("adding route to Load Balancer since we are in a HA configuration " +
								" and no more apps are interested.");
							}
						} else if(StaticServiceHolder.sipStandardService.getOutboundProxy() != null) {
							sipFactoryImpl.addLoadBalancerRouteHeader(request);
							addDNSRoute = false;
							if(logger.isDebugEnabled()) {
								logger.debug("adding route to outbound proxy (no load balancer set) since we have outboundProxy configured " +
								" and no more apps are interested.");
							}
						}
					}
				}
			}
			
			if(addDNSRoute) {
				// we add the DNS Route only if we don't redirect back to the container or if there is no outgoing load balancer defined
				addDnsRoute(hop, request);
			}
			
			if(logger.isDebugEnabled()) {
				getSipSession().getSipApplicationSession().getSipContext().getSipManager().dumpSipSessions();
			}
			if (super.getTransaction() == null) {				
				setSystemContactHeader(sipConnector, matchingListeningPoint, transport); 
				/* 
				 * If we the next hop is in this container we optimize the traffic by directing it here instead of going through load balancers.
				 * This is must be done before creating the transaction, otherwise it will go to the host/port specified prior to the changes here.
				 */
				if(!isInitial() && sipConnector != null && // Initial requests already use local address in RouteHeader.
						sipConnector.isUseStaticAddress()) {
					JainSipUtils.optimizeRouteHeaderAddressForInternalRoutingrequest(sipConnector, request, session, sipFactoryImpl, transport);
				}								
				if(logger.isDebugEnabled()) {
					logger.debug("Getting new Client Tx for request " + request);
				}
				final ClientTransaction ctx = sipProvider.getNewClientTransaction(request);				
				JainSipUtils.setTransactionTimers((TransactionExt) ctx, sipFactoryImpl.getSipApplicationDispatcher());
				Dialog dialog = null;
				if(session.getProxy() != null) {
					// take care of the RRH
					if(isInitial()) {
						if(session.getProxy().getRecordRoute()) {
							ListIterator li = request.getHeaders(RecordRouteHeader.NAME);
							while(li.hasNext()) {
								RecordRouteHeader rrh = (RecordRouteHeader) li.next();
								if(rrh == null) {
									if(logger.isDebugEnabled()) {
										logger.debug("Unexpected RRH = null for this request" + request);
									}
								} else {
									javax.sip.address.URI uri = rrh.getAddress().getURI();
									if(uri.isSipURI()) {
										
										javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) uri;
										String nextApp = sipUri.getParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME);
										final MobicentsSipApplicationSessionKey sipAppKey = getSipSession().getSipApplicationSession().getKey();
										final String thisApp = sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(sipAppKey.getApplicationName());
										if(thisApp.equals(nextApp)) {
											if(sipConnector != null && sipConnector.isUseStaticAddress()) {
												sipUri.setHost(sipConnector.getStaticServerAddress());
												sipUri.setPort(sipConnector.getStaticServerPort());
											}
											//sipUri.setTransportParam(transport);
											if(logger.isDebugEnabled()) {
												logger.debug("Updated the RRH with static server address " + sipUri);
											}
										}
									}
								}
							}
						}
					}
				} else {
					// no dialogs in proxy
					dialog = ctx.getDialog();
				}
				
				if (dialog == null && this.createDialog && JainSipUtils.DIALOG_CREATING_METHODS.contains(getMethod())) {					
					dialog = sipProvider.getNewDialog(ctx);
					((DialogExt)dialog).disableSequenceNumberValidation();
					session.setSessionCreatingDialog(dialog);
					if(logger.isDebugEnabled()) {
						logger.debug("new Dialog for request " + request + ", ref = " + dialog);
					}
				}

				if(linkedRequest != null) {
					updateLinkedRequestAppDataMapping(ctx, dialog);
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
				session.setSessionCreatingTransactionRequest(this);

			} else if (Request.PRACK.equals(request.getMethod())) {			
				final ClientTransaction ctx = sipProvider.getNewClientTransaction(request);
				JainSipUtils.setTransactionTimers((TransactionExt) ctx, sipFactoryImpl.getSipApplicationDispatcher());
				
				if(linkedRequest != null) {
					updateLinkedRequestAppDataMapping(ctx, dialog);
				}
				// SIP Request is ALWAYS pointed to by the client tx.
				// Notice that the tx appplication data is cached in the request
				// copied over to the tx so it can be quickly accessed when response
				// arrives.				
				ctx.setApplicationData(this.transactionApplicationData);
				setTransaction(ctx);
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("Transaction is not null, where was it created? " + getTransaction());
				}
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
			session.updateStateOnSubsequentRequest(this, false);
			// Issue 1481 http://code.google.com/p/mobicents/issues/detail?id=1481
			// proxy should not add or remove subscription since there is no dialog associated with it
			checkSubscriptionStateHeaders(session);								

			//updating the last accessed times 
			session.access();
			sipApplicationSession.access();
			Dialog dialog = getDialog();
			if(session.getProxy() != null) dialog = null;
			if(request.getMethod().equals(Request.CANCEL)) dialog = null;
						
			// Issue 1791 : using a different classloader created outside the application loader 
			// to avoid leaks on startup/shutdown
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				final ClassLoader cl = sipApplicationSession.getSipContext().getClass().getClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				// If dialog does not exist or has no state.
				if (dialog == null || dialog.getState() == null
						|| (dialog.getState() == DialogState.EARLY && !Request.PRACK.equals(requestMethod)) || Request.CANCEL.equals(requestMethod)) {
					if(logger.isDebugEnabled()) {
						logger.debug("Sending the request " + request);
					}
					((ClientTransaction) super.getTransaction()).sendRequest();
				} else {
					// This is a subsequent (an in-dialog) request. 
					// we don't redirect it to the container for now
					if(logger.isDebugEnabled()) {
						logger.debug("Sending the in dialog request " + request);
					}
					dialog.sendRequest((ClientTransaction) getTransaction());
				}			
				isMessageSent = true;
				
				if(method.equals(Request.INVITE)) {
					session.setRequestsPending(session.getRequestsPending()+1);
				}
			} finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}
		} catch (Exception ex) {			
			// The second condition for SipExcpetion is to cover com.bea.sipservlet.tck.agents.spec.ProxyBranchTest.testCreatingBranchParallel() where they send a request twice, the second
			// time it does a "Request already sent" jsip exception but the tx is going on and must not be destryed
			boolean skipTxTermination = false;
			Transaction tx = getTransaction();
			if((ex instanceof IllegalTransactionStateException && ((IllegalTransactionStateException)ex).getReason().equals(Reason.RequestAlreadySent)) || (tx != null && !(tx instanceof ClientTransaction))) {
				skipTxTermination = true;
			}
			if(!skipTxTermination) {				
				JainSipUtils.terminateTransaction(tx);
				// cleaning up the request to make sure it can be resent with some modifications in case of exception
				if(transactionApplicationData.getHops() != null && transactionApplicationData.getHops().size() > 0) {
					request.removeFirst(RouteHeader.NAME);
				}
				request.removeFirst(ViaHeader.NAME);
				request.removeFirst(ContactHeader.NAME);
				setTransaction(null);
				message = (Request) request.clone();
				if(ex.getCause() != null && ex.getCause() instanceof IOException) {				
					throw (IOException) ex.getCause();
				}
			}
			throw new IllegalStateException("Error sending request " + request,ex);
		} 
	}

	/**
	 * 
	 * @param hop
	 * @param request	
	 */
	private void addDnsRoute(Hop hop, final Request request)
			throws ParseException, SipException {
		if(hop != null && sipFactoryImpl.getSipApplicationDispatcher().isExternal(hop.getHost(), hop.getPort(), hop.getTransport())) {	
			javax.sip.address.SipURI nextHopUri = SipFactoryImpl.addressFactory.createSipURI(null, hop.getHost());
			nextHopUri.setLrParam();
			nextHopUri.setPort(hop.getPort());
			if(hop.getTransport() != null) {
				nextHopUri.setTransportParam(hop.getTransport());
			}
			// Deal with http://code.google.com/p/mobicents/issues/detail?id=2346
			nextHopUri.setParameter(DNSAwareRouter.DNS_ROUTE, Boolean.TRUE.toString());
			final javax.sip.address.Address nextHopRouteAddress = 
				SipFactoryImpl.addressFactory.createAddress(nextHopUri);
			final RouteHeader nextHopRouteHeader = 
				SipFactoryImpl.headerFactory.createRouteHeader(nextHopRouteAddress);
			if(logger.isDebugEnabled()) {
				logger.debug("Adding next hop found by RFC 3263 lookups as route header" + nextHopRouteHeader);			    	
			}
	
			request.addFirst(nextHopRouteHeader);
		}
	}

	/**
	 * 
	 */
	private void checkViaHeaderAddition() throws ParseException {
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipFactoryImpl.getSipNetworkInterfaceManager();		
		final Request request = (Request) super.message;
		final MobicentsSipSession session = getSipSession();
		final MobicentsSipApplicationSession sipApplicationSession = session.getSipApplicationSession();
		final MobicentsProxy proxy = session.getProxy();
		ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
		
		if(!request.getMethod().equals(Request.CANCEL) && viaHeader == null) {
			//Issue 112 fix by folsson
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
				// Issue 					
				viaHeader = JainSipUtils.createViaHeader(
						sipNetworkInterfaceManager, request, null, session.getOutboundInterface());
				message.addHeader(viaHeader);
				if(logger.isDebugEnabled()) {
			    	logger.debug("Added via Header" + viaHeader);
			    }
		    }
		}
		if(viaHeader.getBranch() == null) {
			final String branch = JainSipUtils.createBranch(sipApplicationSession.getKey().getId(),  sipFactoryImpl.getSipApplicationDispatcher().getHashFromApplicationName(session.getKey().getApplicationName()));			
			viaHeader.setBranch(branch);
		}
	}

	/**
	 * 
	 * @param sipConnector
	 * @throws ParseException
	 * @throws InvalidArgumentException
	 */
	private void checkViaHeaderUpdateForStaticExternalAddressUsage(final SipConnector sipConnector)
			throws ParseException, InvalidArgumentException {
		
		final Request request = (Request) super.message;	
		ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
				
		if(sipConnector != null && sipConnector.isUseStaticAddress()) {
			javax.sip.address.URI uri = request.getRequestURI();
			RouteHeader route = (RouteHeader) request.getHeader(RouteHeader.NAME);
			if(route != null) {
				uri = route.getAddress().getURI();
			}
			if(uri.isSipURI()) {
				javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) uri;
				String host = sipUri.getHost();
				int port = sipUri.getPort();
				if(sipFactoryImpl.getSipApplicationDispatcher().isExternal(host, port, transport)) {
					viaHeader.setHost(sipConnector.getStaticServerAddress());
					viaHeader.setPort(sipConnector.getStaticServerPort());
				}
			}
		}		
	}
	
	/**
	 * Keeping the transactions mapping in application data for CANCEL handling
	 * 
	 * @param ctx
	 * @param dialog
	 */
	private void updateLinkedRequestAppDataMapping(final ClientTransaction ctx,
			Dialog dialog) {
		final Transaction linkedTransaction = linkedRequest.getTransaction();
		final Dialog linkedDialog = linkedRequest.getDialog();
		//keeping the client transaction in the server transaction's application data
		if(linkedTransaction != null && linkedTransaction.getApplicationData() != null) {
			((TransactionApplicationData)linkedTransaction.getApplicationData()).setTransaction(ctx);
		}
		if(linkedDialog != null && linkedDialog.getApplicationData() != null) {
			((TransactionApplicationData)linkedDialog.getApplicationData()).setTransaction(ctx);
		}
		//keeping the server transaction in the client transaction's application data
		this.transactionApplicationData.setTransaction(linkedTransaction);
		if(dialog!= null && dialog.getApplicationData() != null) {
			((TransactionApplicationData)dialog.getApplicationData()).setTransaction(linkedTransaction);
		}
	}

	/**
	 * @param session
	 * @throws SipException
	 */
	private void checkSubscriptionStateHeaders(final MobicentsSipSession session)
			throws SipException {
		if(Request.NOTIFY.equals(getMethod()) && session.getProxy() == null) {
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
	}

	/**
	 * @param request
	 * @param requestMethod
	 * @param session
	 * @param proxy
	 * @param sipNetworkInterfaceManager
	 * @param transport
	 * @param sipConnector
	 * @param matchingListeningPoint
	 * @throws ParseException
	 */
	private void setSystemContactHeader(final SipConnector sipConnector,
			final MobicentsExtendedListeningPoint matchingListeningPoint, String transportForRequest)
			throws ParseException {
		
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipFactoryImpl.getSipNetworkInterfaceManager();
		final Request request = (Request) super.message;
		final String requestMethod = getMethod();
		final MobicentsSipSession session = getSipSession();
		final MobicentsProxy proxy = session.getProxy();
		
		ContactHeader contactHeader = (ContactHeader)request.getHeader(ContactHeader.NAME);
		if(contactHeader == null && !Request.REGISTER.equalsIgnoreCase(requestMethod) && JainSipUtils.CONTACT_HEADER_METHODS.contains(requestMethod) && proxy == null) {
			final FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
			final javax.sip.address.URI fromUri = fromHeader.getAddress().getURI();
			String fromName = null;
			String displayName = fromHeader.getAddress().getDisplayName();
			if(fromUri instanceof javax.sip.address.SipURI) {
				fromName = ((javax.sip.address.SipURI)fromUri).getUser();
			}
			// Create the contact name address.						
			contactHeader = 
				JainSipUtils.createContactHeader(sipNetworkInterfaceManager, request, displayName, fromName, session.getOutboundInterface());	
			request.addHeader(contactHeader);
		}

		if(proxy == null && contactHeader != null) {
			boolean sipURI = contactHeader.getAddress().getURI().isSipURI();
			if(sipURI) {
				javax.sip.address.SipURI contactSipUri = (javax.sip.address.SipURI) contactHeader.getAddress().getURI();
				if(sipConnector != null && sipConnector.isUseStaticAddress()) {
					contactSipUri.setHost(sipConnector.getStaticServerAddress());
					contactSipUri.setPort(sipConnector.getStaticServerPort());
					contactSipUri.setUser(null);
				} else {
					boolean usePublicAddress = JainSipUtils.findUsePublicAddress(
							sipNetworkInterfaceManager, request, matchingListeningPoint);
					contactSipUri.setHost(matchingListeningPoint.getIpAddress(usePublicAddress));
					contactSipUri.setPort(matchingListeningPoint.getPort());
					
				}
				// http://code.google.com/p/mobicents/issues/detail?id=1150 only set transport if not udp
				if(transportForRequest != null) {
					if(!ListeningPoint.UDP.equalsIgnoreCase(transportForRequest)) {
						contactSipUri.setTransportParam(transportForRequest);
					}						
					
					// If the transport for the request was changed in the last moment we need to update the header
					String transportParam = contactSipUri.getTransportParam();
					if(transportParam != null && !transportParam.equalsIgnoreCase(transportForRequest)) {
						contactSipUri.setTransportParam(transportForRequest);
					}

					if(ListeningPoint.TLS.equalsIgnoreCase(transportForRequest)) {
						final javax.sip.address.URI requestURI = request.getRequestURI();
						// make the contact uri secure only if the request uri is secure to cope with issue http://code.google.com/p/mobicents/issues/detail?id=2269
						// Wrong Contact header scheme URI in case TLS call with 'sip:' scheme
						if(requestURI.isSipURI() && ((javax.sip.address.SipURI)requestURI).isSecure()) {
							contactSipUri.setSecure(true);
						} else if(requestURI.isSipURI() && !((javax.sip.address.SipURI)requestURI).isSecure() && contactSipUri.isSecure()) {
							// if the Request URI is non secure but the contact is make sure to move it back to non secure to handle Microsoft OCS interop 
							contactSipUri.setSecure(false);
						}
					}
				}
			} 
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean visitNextHop() {
		if(transactionApplicationData != null) {
			Queue<Hop> nextHops = transactionApplicationData.getHops();
			if(sipFactoryImpl.getSipApplicationDispatcher().getDNSServerLocator() != null && nextHops != null && nextHops.size() > 1) {
				nextHops.remove();
				Hop nextHop = nextHops.peek();
				if(nextHop != null) {
					// If a failure occurs, the client SHOULD create a new request, 
					// which is identical to the previous, but
					getMessage().removeFirst(RouteHeader.NAME);
					// has a different value of the Via branch ID than the previous (and
					// therefore constitutes a new SIP transaction).  
					ViaHeader viaHeader = (ViaHeader) getMessage().getHeader(ViaHeader.NAME);
					viaHeader.removeParameter("branch");
					message = (Message) message.clone();
					if(logger.isDebugEnabled()) {
						logger.debug("sending request " + getMessage() + " to next hop " + nextHop + " discovered through RFC3263 mechanisms.");
					}
					setTransaction(null);
					// That request is sent to the next element in the list as specified by RFC 2782.
					try {
						send(nextHop);
					} catch (IOException e) {
						logger.error("unexpected IOException on sending " + getMessage() + " to next hop " + nextHop + "discovered through RFC3263 mechanisms.");
						return false;
					}
					return true;
				}
			}
		}
		return false;
		
	}

	/**
	 * @param request
	 * @param session
	 * @param viaHeader
	 * @param transport
	 * @param sipConnector
	 * @param sipApplicationSession
	 * @param matchingListeningPoint
	 * @throws ParseException
	 * @throws SipException
	 */
	private void sendAck(final String transport, final SipConnector sipConnector,			
			final MobicentsExtendedListeningPoint matchingListeningPoint)
			throws ParseException, SipException {
		
		final Request request = (Request) super.message;			
		final MobicentsSipSession session = getSipSession();
		final MobicentsSipApplicationSession sipApplicationSession = session.getSipApplicationSession();
		
		// Issue 1791 : using a different classloader created outside the application loader 
		// to avoid leaks on startup/shutdown
		final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			final ClassLoader cl = sipApplicationSession.getSipContext().getClass().getClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			if(sipConnector != null && // Initial requests already use local address in RouteHeader.
					sipConnector.isUseStaticAddress()) {
				JainSipUtils.optimizeRouteHeaderAddressForInternalRoutingrequest(sipConnector, request, session, sipFactoryImpl, transport);
			}
			//Issue 2588 : updating the last accessed times for ACK as well 
			session.access();
			sipApplicationSession.access();
			if(logger.isDebugEnabled()) {
				logger.debug("Sending the ACK request " + request);
			}
			session.getSessionCreatingDialog().sendAck(request);
			session.setRequestsPending(session.getRequestsPending()-1);
			final Transaction transaction = getTransaction();
			// transaction can be null in case of forking
			if(transaction != null) {
				final TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
				final MobicentsB2BUAHelper b2buaHelperImpl = sipSession.getB2buaHelper();
				if(b2buaHelperImpl != null && tad != null) {
					// we unlink the originalRequest early to avoid keeping the messages in mem for too long
					b2buaHelperImpl.unlinkOriginalRequestInternal((SipServletRequestImpl)tad.getSipServletMessage(), false);
				}				
				session.removeOngoingTransaction(transaction);					
				if(tad != null) {
					tad.cleanUp();
				}
			}
			final SipProvider sipProvider = matchingListeningPoint.getSipProvider();	
			// Issue 1468 : to handle forking, we shouldn't cleanup the app data since it is needed for the forked responses
			if(((SipStackImpl)sipProvider.getSipStack()).getMaxForkTime() == 0 && transaction != null) {
				transaction.setApplicationData(null);
			}
			return;
		} finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
	}

	/**
	 * Add a route header to route back to the container
	 * @param applicationName the application name that was chosen by the AR to route the request
	 * @throws ParseException
	 * @throws SipException 
	 * @throws NullPointerException 
	 */
	private void addInfoForRoutingBackToContainer(SipApplicationRouterInfo routerInfo, String applicationSessionId, String applicationName) throws ParseException, SipException {		
		final Request request = (Request) super.message;
		final javax.sip.address.SipURI sipURI = JainSipUtils.createRecordRouteURI(
				sipFactoryImpl.getSipNetworkInterfaceManager(), 
				request);
		sipURI.setLrParam();
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_DIRECTIVE, 
				routingDirective.toString());
		if(getSipSession().getRegionInternal() != null) {
			sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_REGION_LABEL, 
					getSipSession().getRegionInternal().getLabel());
			sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_REGION_TYPE, 
					getSipSession().getRegionInternal().getType().toString());
		}
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_PREV_APPLICATION_NAME, 
				applicationName);
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_PREV_APP_ID, 
				applicationSessionId);
		final javax.sip.address.Address routeAddress = 
			SipFactoryImpl.addressFactory.createAddress(sipURI);
		final RouteHeader routeHeader = 
			SipFactoryImpl.headerFactory.createRouteHeader(routeAddress);
		request.addFirst(routeHeader);		
		// adding the application router info to avoid calling the AppRouter twice
		// See Issue 791 : http://code.google.com/p/mobicents/issues/detail?id=791
		final MobicentsSipSession session = getSipSession();
		session.setNextSipApplicationRouterInfo(routerInfo);
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
		if(logger.isDebugEnabled()) {
			logger.debug("setting routing state to " + routingState);
		}
		this.routingState = routingState;	
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#addAuthHeader(javax.servlet.sip.SipServletResponse, javax.servlet.sip.AuthInfo)
	 */
	public void addAuthHeader(SipServletResponse challengeResponse,
			AuthInfo authInfo) {
		addAuthHeader(challengeResponse, authInfo, false);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#addAuthHeader(javax.servlet.sip.SipServletResponse, java.lang.String, java.lang.String)
	 */
	public void addAuthHeader(SipServletResponse challengeResponse,
			String username, String password) {
		addAuthHeader(challengeResponse, username, password, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.SipServletRequestExt#addAuthHeader(javax.servlet.sip.SipServletResponse, javax.servlet.sip.AuthInfo, boolean)
	 */
	public void addAuthHeader(SipServletResponse challengeResponse,
			AuthInfo authInfo, boolean cacheCredentials) {
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
			// Fix for Issue 1832 : http://code.google.com/p/mobicents/issues/detail?id=1832 
			// Authorization header is growing when nonce become stale, don't take into account stale headers
			// in the challenge request
			removeStaleAuthHeaders(wwwAuthHeader);
//			String uri = wwwAuthHeader.getParameter("uri");
			AuthInfoEntry authInfoEntry = authInfoImpl.getAuthInfo(wwwAuthHeader.getRealm());
			
			if(authInfoEntry == null) throw new SecurityException(
					"Cannot add authorization header. No credentials for the following realm: " + wwwAuthHeader.getRealm());
			
			addChallengeResponse(wwwAuthHeader,
					authInfoEntry.getUserName(),
					authInfoEntry.getPassword(),
					this.getRequestURI().toString());
			
			if(cacheCredentials) {
				getSipSession().getSipSessionSecurity().addCachedAuthInfo(wwwAuthHeader.getRealm(), authInfoEntry);
			}
		}
		
		// Now check for Proxy-Authentication
		authHeaderIterator = 
			response.getHeaders(ProxyAuthenticateHeader.NAME);
		while(authHeaderIterator.hasNext()) {
			ProxyAuthenticateHeader proxyAuthHeader = 
				(ProxyAuthenticateHeader) authHeaderIterator.next();
			// Fix for Issue 1832 : http://code.google.com/p/mobicents/issues/detail?id=1832 
			// Authorization header is growing when nonce become stale, don't take into account stale headers
			// in the challenge request
			removeStaleAuthHeaders(proxyAuthHeader);
//			String uri = wwwAuthHeader.getParameter("uri");
			AuthInfoEntry authInfoEntry = authInfoImpl.getAuthInfo(proxyAuthHeader.getRealm());
			
			if(authInfoEntry == null) throw new SecurityException(
					"No credentials for the following realm: " + proxyAuthHeader.getRealm());
			
			addChallengeResponse(proxyAuthHeader,
					authInfoEntry.getUserName(),
					authInfoEntry.getPassword(),
					this.getRequestURI().toString());
			
			if(cacheCredentials) {
				getSipSession().getSipSessionSecurity().addCachedAuthInfo(proxyAuthHeader.getRealm(), authInfoEntry);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.javax.servlet.sip.SipServletRequestExt#addAuthHeader(javax.servlet.sip.SipServletResponse, java.lang.String, java.lang.String, boolean)
	 */
	public void addAuthHeader(SipServletResponse challengeResponse,
			String username, String password, boolean cacheCredentials) {
		checkReadOnly();
		SipServletResponseImpl challengeResponseImpl = 
			(SipServletResponseImpl) challengeResponse;
		
		Response response = (Response) challengeResponseImpl.getMessage();
		ListIterator<Header> authHeaderIterator = 
			response.getHeaders(WWWAuthenticateHeader.NAME);
		
		// First
		while(authHeaderIterator.hasNext()) {
			WWWAuthenticateHeader wwwAuthHeader = 
				(WWWAuthenticateHeader) authHeaderIterator.next();
			// Fix for Issue 1832 : http://code.google.com/p/mobicents/issues/detail?id=1832 
			// Authorization header is growing when nonce become stale, don't take into account stale headers
			// in the challenge request
			removeStaleAuthHeaders(wwwAuthHeader);
//			String uri = wwwAuthHeader.getParameter("uri");
			addChallengeResponse(wwwAuthHeader, username, password, this.getRequestURI().toString());
			
			if(cacheCredentials) {
				getSipSession().getSipSessionSecurity().addCachedAuthInfo(wwwAuthHeader.getRealm(), new AuthInfoEntry(response.getStatusCode(), username, password));
			}
		}
		
		
		authHeaderIterator = 
			response.getHeaders(ProxyAuthenticateHeader.NAME);
		
		while(authHeaderIterator.hasNext()) {
			ProxyAuthenticateHeader proxyAuthHeader = 
				(ProxyAuthenticateHeader) authHeaderIterator.next();
			// Fix for Issue 1832 : http://code.google.com/p/mobicents/issues/detail?id=1832 
			// Authorization header is growing when nonce become stale, don't take into account stale headers
			// in the challenge request
			removeStaleAuthHeaders(proxyAuthHeader);
			String uri = proxyAuthHeader.getParameter("uri");
			if(uri == null) uri = this.getRequestURI().toString();
			addChallengeResponse(proxyAuthHeader, username, password, uri);
			
			if(cacheCredentials) {
				getSipSession().getSipSessionSecurity().addCachedAuthInfo(proxyAuthHeader.getRealm(), new AuthInfoEntry(response.getStatusCode(), username, password));
			}
		}
	}
	
	/*
	 * Fix for Issue 1832 : http://code.google.com/p/mobicents/issues/detail?id=1832
	 * Authorization header is growing when nonce become stale, don't take into account stale headers
	 * in the subsequent request
	 * 
	 * From RFC 2617 : stale
     * A flag, indicating that the previous request from the client was
     * rejected because the nonce value was stale. If stale is TRUE
     * (case-insensitive), the client may wish to simply retry the request
     * with a new encrypted response, without reprompting the user for a
     * new username and password. The server should only set stale to TRUE
     * if it receives a request for which the nonce is invalid but with a
     * valid digest for that nonce (indicating that the client knows the
     * correct username/password). If stale is FALSE, or anything other
     * than TRUE, or the stale directive is not present, the username
     * and/or password are invalid, and new values must be obtained.
	 */
	protected void removeStaleAuthHeaders(WWWAuthenticateHeader responseAuthHeader) {
		String realm = responseAuthHeader.getRealm();
		
		ListIterator<Header> authHeaderIterator = 
			message.getHeaders(AuthorizationHeader.NAME);
		if(authHeaderIterator.hasNext()) {
			message.removeHeader(AuthorizationHeader.NAME);
			while(authHeaderIterator.hasNext()) {
				AuthorizationHeader wwwAuthHeader = 
					(AuthorizationHeader) authHeaderIterator.next();
				if(realm != null && !realm.equalsIgnoreCase(wwwAuthHeader.getRealm())) {
					message.addHeader(wwwAuthHeader);
				}
			}
		}
		
		authHeaderIterator = 
			message.getHeaders(ProxyAuthorizationHeader.NAME);
		if(authHeaderIterator.hasNext()) {
			message.removeHeader(ProxyAuthorizationHeader.NAME);
			while(authHeaderIterator.hasNext()) {
				ProxyAuthorizationHeader proxyAuthHeader = 
					(ProxyAuthorizationHeader) authHeaderIterator.next();
				if(realm != null && !realm.equalsIgnoreCase(proxyAuthHeader.getRealm())) {
					message.addHeader(proxyAuthHeader);
				}
			}	
		}
	}
	
	
	private void addChallengeResponse(
			WWWAuthenticateHeader wwwAuthHeader,
			String username,
			String password,
			String uri) {
		
		int nc = generateNcFromMessage(message);

		AuthorizationHeader authorization = getSipSession().getSipApplicationSession().getSipContext().getDigestAuthenticator().getAuthorizationHeader(
				getMethod(),
				uri,
				"", // TODO: What is this entity-body?
				wwwAuthHeader,
				username,
				password,
				wwwAuthHeader.getNonce(),
				nc);
		
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
	public void setResponse(SipServletResponseImpl response) {		
		if(response.getStatus() >= 200 && 
				(lastFinalResponse == null || lastFinalResponse.getStatus() < response.getStatus())) {
			if(logger.isDebugEnabled()) {
				logger.debug("last final response " + response + " set on " + this);
			}
			this.lastFinalResponse = response;
		}
		// we keep the last informational response for noPrackReceived only
		if(containsRel100(response.getMessage()) && (response.getStatus() > 100 && response.getStatus() < 200) && 
				(lastInformationalResponse == null || lastInformationalResponse.getStatus() < response.getStatus())) {
			if(logger.isDebugEnabled()) {
				logger.debug("last informational response " + lastInformationalResponse + " set on " + this);
			}
			this.lastInformationalResponse = response;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#getInitialPoppedRoute()
	 */
	public Address getInitialPoppedRoute() {
		return transactionApplicationData.getInitialPoppedRoute();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#getRegion()
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
		if(NON_INITIAL_SIP_REQUEST_METHODS.contains(sipServletRequest.getMethod())) {
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
		if(message == null || ((SIPRequest)message).getRemoteAddress() == null) {
			return null;
		}
		return ((SIPRequest)message).getRemoteAddress().getHostAddress();
		// replaced because wasn't giving correct info for ACK
//		if(getTransaction() != null) {
//			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
//				return ((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress();
//			} else {
//				return ((SIPTransaction)getTransaction()).getPeerAddress();
//			}
//		} else {
//			ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
//			if(via == null) {
//				return null;
//			} else {
//				return via.getHost();
//			}
//		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitialRemotePort() {
		if(message == null ) {
			return -1;
		}
		return ((SIPRequest)message).getRemotePort();
		// replaced because wasn't giving correct info for ACK
//		if(getTransaction() != null) {
//			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
//				return ((SIPTransaction)getTransaction()).getPeerPacketSourcePort();
//			} else {
//				return ((SIPTransaction)getTransaction()).getPeerPort();
//			}
//		}else {
//			ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
//			if(via == null) {
//				return -1;
//			} else {
//				return via.getPort()<=0 ? 5060 : via.getPort();
//			}
//		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialTransport() {		
		if(getTransaction() != null) {
			return ((SIPTransaction)getTransaction()).getTransport();
		} else {
			ViaHeader via = (ViaHeader) message.getHeader(ViaHeader.NAME);
			if(via == null) {
				return null;
			} else {
				return via.getTransport();
			}
		}
	}
	
	public void cleanUp() {
//		super.cleanUp();
		if(transactionApplicationData != null) {
			transactionApplicationData.cleanUp();
			transactionApplicationData = null;
		}
		setTransaction(null);
		poppedRoute =null;
		poppedRouteHeader = null;
		routingDirective =null;
		routingRegion = null;
		routingState = null;
		subscriberURI = null;
//		lastFinalResponse = null;
//		lastInformationalResponse = null;		
		linkedRequest = null;		
	}
	
	public void cleanUpLastResponses() {
		if(logger.isDebugEnabled()) {
			logger.debug("cleaning up last responses on " + this);
		}
		lastFinalResponse = null;
		lastInformationalResponse = null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		String messageString = in.readUTF();
		try {
			message = SipFactoryImpl.messageFactory.createRequest(messageString);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Message " + messageString + " previously serialized could not be reparsed", e);
		}
		boolean isLinkedRequestSerialized = in.readBoolean();
		if (isLinkedRequestSerialized) {
			linkedRequest = (SipServletRequestImpl) in.readObject();
		}
		createDialog = in.readBoolean();
		String routingDirectiveString = in.readUTF();
		if(!routingDirectiveString.equals("")) {
			routingDirective = SipApplicationRoutingDirective.valueOf(routingDirectiveString);
		}
		String routingStateString = in.readUTF();
		if(!routingStateString.equals("")) {
			routingState = RoutingState.valueOf(routingStateString);
		}
		boolean isRoutingRegionSet = in.readBoolean();
		if(isRoutingRegionSet) {
			routingRegion = (SipApplicationRoutingRegion) in.readObject();
		}
		isInitial = in.readBoolean();
		isFinalResponseGenerated = in.readBoolean();
		is1xxResponseGenerated = in.readBoolean();		
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		if(linkedRequest == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeObject(linkedRequest);
		}
		out.writeBoolean(createDialog);
		if(routingDirective != null) {
			out.writeUTF(routingDirective.toString());
		} else {
			out.writeUTF("");
		}
		if(routingState != null) {
			out.writeUTF(routingState.toString());
		} else {
			out.writeUTF("");
		}
		if(routingRegion != null) {
			out.writeBoolean(true);
			out.writeObject(routingRegion);
		} else {
			out.writeBoolean(false);
		}
		out.writeBoolean(isInitial);
		out.writeBoolean(isFinalResponseGenerated);
		out.writeBoolean(is1xxResponseGenerated);	
	}

	private static int generateNcFromMessage(Message message) {
		int nc = 1;
		CSeqHeader cseq = (CSeqHeader) message.getHeader(CSeqHeader.NAME);
		if (cseq != null) {
			nc = (int) (cseq.getSeqNumber() % Integer.MAX_VALUE);
		}
		return nc;
	}

	/**
	 * Added for Issue 2173 http://code.google.com/p/mobicents/issues/detail?id=2173
	 * Handle Header [Authentication-Info: nextnonce="xyz"] in sip authorization responses
	 */
	public void updateAuthorizationHeaders(boolean useNextNonce) {
		if(logger.isDebugEnabled()) {
			logger.debug("Updating authorization headers with nextnonce " + getSipSession().getSipSessionSecurity().getNextNonce());
		}
		int nc = -1; 
			
		List<Header> authorizationHeaders = new ArrayList<Header>();
		
		// First check for WWWAuthentication headers
		ListIterator authHeaderIterator = 
			message.getHeaders(AuthorizationHeader.NAME);
		while(authHeaderIterator.hasNext()) {
			AuthorizationHeader wwwAuthHeader = 
				(AuthorizationHeader) authHeaderIterator.next();
			MobicentsAuthInfoEntry authInfoEntry = getSipSession().getSipSessionSecurity().getCachedAuthInfos().get(wwwAuthHeader.getRealm());
			
			if(authInfoEntry != null) {
				
				if(nc < 0) {
					nc = generateNcFromMessage(message);
				}
				String nextNonce = null;
				if(useNextNonce) {
					nextNonce = getSipSession().getSipSessionSecurity().getNextNonce();
				} else {
					nextNonce = wwwAuthHeader.getNonce();
				}
				
				AuthorizationHeader authorization = getSipSession().getSipApplicationSession().getSipContext().getDigestAuthenticator().getAuthorizationHeader(
						getMethod(),
						this.getRequestURI().toString(),
						"", // TODO: What is this entity-body?
						wwwAuthHeader,
						authInfoEntry.getUserName(),
						authInfoEntry.getPassword(),
						nextNonce,
						nc);

				authorizationHeaders.add(authorization);
			} else {
				authorizationHeaders.add(wwwAuthHeader);
			}
		}
		
		// Now check for Proxy-Authentication
		authHeaderIterator = 
			message.getHeaders(ProxyAuthorizationHeader.NAME);
		while(authHeaderIterator.hasNext()) {
			ProxyAuthorizationHeader proxyAuthHeader = 
				(ProxyAuthorizationHeader) authHeaderIterator.next();
//			String uri = wwwAuthHeader.getParameter("uri");
			MobicentsAuthInfoEntry authInfoEntry = getSipSession().getSipSessionSecurity().getCachedAuthInfos().get(proxyAuthHeader.getRealm());
			
			if(authInfoEntry != null) { 
				
				if(nc < 0) {
					nc = generateNcFromMessage(message);
				}
				String nextNonce = null;
				if(useNextNonce) {
					nextNonce = getSipSession().getSipSessionSecurity().getNextNonce();
				} else {
					nextNonce = proxyAuthHeader.getNonce();
				}
				AuthorizationHeader authorization = getSipSession().getSipApplicationSession().getSipContext().getDigestAuthenticator().getAuthorizationHeader(
						getMethod(),
						this.getRequestURI().toString(),
						"", // TODO: What is this entity-body?
						proxyAuthHeader,
						authInfoEntry.getUserName(),
						authInfoEntry.getPassword(),
						nextNonce,
						nc);

				authorizationHeaders.add(authorization);
			} else {
				authorizationHeaders.add(proxyAuthHeader);
			}
		}
		
		message.removeHeader(AuthorizationHeader.NAME);
		message.removeHeader(ProxyAuthorizationHeader.NAME);
		
		for(Header header : authorizationHeaders) {
			message.addHeader(header);
		}
	}

	// Issue 2354 : need to clone the original request to create the forked response
	public Object clone() {
		SipServletRequestImpl sipServletRequestImpl = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest((Request)message, sipSession, getTransaction(), null, createDialog);
		sipServletRequestImpl.setLinkedRequest(linkedRequest);
		sipServletRequestImpl.setPoppedRoute(poppedRouteHeader);
		sipServletRequestImpl.setSubscriberURI(subscriberURI);
		sipServletRequestImpl.setAttributeMap(getAttributeMap());
		return sipServletRequestImpl;
	}	
}
