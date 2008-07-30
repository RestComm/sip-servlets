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

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
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
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipRequestDispatcher;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.security.AuthInfoEntry;
import org.mobicents.servlet.sip.security.AuthInfoImpl;
import org.mobicents.servlet.sip.security.authentication.DigestAuthenticator;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

public class SipServletRequestImpl extends SipServletMessageImpl implements
		SipServletRequest, Cloneable {
	private static Log logger = LogFactory.getLog(SipServletRequestImpl.class);
		
	public static final Set<String> nonInitialSipRequestMethods = new HashSet<String>();
	
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
	private RouteHeader popedRouteHeader;

	/* Cache the application routing directive in the record route header */
	private SipApplicationRoutingDirective routingDirective = SipApplicationRoutingDirective.NEW;

	private RoutingState routingState;
	
	private B2buaHelper b2buahelper;
	
	private SipServletResponse lastFinalResponse;
	
	/**
	 * Routing region.
	 */
	private SipApplicationRoutingRegion routingRegion;

	private URI subscriberURI;

	private Address initialPoppedRoute;
	
	private boolean isInitial;
	
	public SipServletRequestImpl(Request request, SipFactoryImpl sipFactoryImpl,
			MobicentsSipSession sipSession, Transaction transaction, Dialog dialog,
			boolean createDialog) {
		super(request, sipFactoryImpl, transaction, sipSession, dialog);
		this.createDialog = createDialog;
		routingState = checkRoutingState(this, dialog);
		if(RoutingState.INITIAL.equals(routingState)) {
			isInitial = true;
		}
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
		boolean isSystemHeader = systemHeaders.contains(hName);

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

		if (!((Request) message).getMethod().equals(Request.INVITE)) {
			throw new IllegalStateException(
					"Cannot create CANCEL for non inivte");
		}
		if (super.getTransaction() == null
				|| super.getTransaction() instanceof ServerTransaction)
			throw new IllegalStateException("No client transaction found!");

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

	public SipServletResponse createResponse(int statusCode, String reasonPhrase) {
		if (super.getTransaction() == null
				|| super.getTransaction() instanceof ClientTransaction) {
			throw new IllegalStateException(
					"Cannot create a response - not a server transaction");
		}
		try {
			Request request = this.getTransaction().getRequest();
			Response response = SipFactories.messageFactory.createResponse(
					statusCode, request);
			if(reasonPhrase!=null) {
				response.setReasonPhrase(reasonPhrase);
			}
			//add a To tag for all responses except Trying (or trying too if it's a subsequent request)
			if((statusCode > Response.TRYING || !isInitial()) && statusCode <= Response.SESSION_NOT_ACCEPTABLE) {
				ToHeader toHeader = (ToHeader) response
					.getHeader(ToHeader.NAME);
				// If we already have a to tag, dont create new
				if (toHeader.getTag() == null) {
					String localTag = null;					
					if(getTransaction().getDialog() != null) {
						localTag = getTransaction().getDialog().getLocalTag();
						// if a dialog has already been created
						// reuse local tag
						if(localTag != null && localTag.length() > 0) {
							toHeader.setTag(localTag);
						} else {
							toHeader.setTag(Integer.toString((int) (Math.random()*10000000)));
						}
					} else {
						toHeader.setTag(Integer.toString((int) (Math.random()*10000000)));
					}
				}
				if (statusCode == Response.OK ) {
					//Following restrictions in JSR 289 Section 4.1.3 Contact Header Field
					if(!Request.REGISTER.equals(request.getMethod()) && !Request.OPTIONS.equals(request.getMethod())) { 
					    // Add the contact header for the dialog.
					    String transport = ((ViaHeader) request
						    .getHeader(ViaHeader.NAME)).getTransport();					
					    ContactHeader contactHeader = JainSipUtils
					    .createContactHeader(super.sipFactoryImpl.getSipNetworkInterfaceManager(), transport, null);
					    if(logger.isDebugEnabled()) {
					    	logger.debug("We're adding this contact header to our new response: '" + contactHeader + ", transport=" + transport);
					    }
					    response.setHeader(contactHeader);
				    }
				}
			}
			// Application Routing : Adding the recorded route headers as route headers
			ListIterator<RecordRouteHeader> recordRouteHeaders = request.getHeaders(RecordRouteHeader.NAME);
			while (recordRouteHeaders.hasNext()) {
				RecordRouteHeader recordRouteHeader = (RecordRouteHeader) recordRouteHeaders
						.next();
				RouteHeader routeHeader = SipFactories.headerFactory.createRouteHeader(recordRouteHeader.getAddress());
				response.addHeader(routeHeader);
			}
			
			SipServletResponseImpl newSipServletResponse = new SipServletResponseImpl(response, super.sipFactoryImpl,
					(ServerTransaction) getTransaction(), session, getDialog());
			newSipServletResponse.setOriginalRequest(this);
			return newSipServletResponse;
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code" + statusCode,
					ex);
		}		
	}

	public B2buaHelper getB2buaHelper() {
		if (this.transactionApplicationData.getProxy() != null)
			throw new IllegalStateException("Proxy already present");
		if (this.b2buahelper != null)
			return this.b2buahelper;
		try {
			if (this.getTransaction() != null
					&& this.getTransaction().getDialog() == null) {
				Request request = (Request) super.message;
				String transport = JainSipUtils.findTransport(request);
				SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(
						transport, false).getSipProvider();
				
				Dialog dialog = sipProvider.getNewDialog(this
						.getTransaction());
				this.session.setSessionCreatingDialog(dialog);
				dialog.setApplicationData( this.transactionApplicationData);				
			}
			this.b2buahelper = new B2buaHelperImpl(this);
			this.createDialog = true; // flag that we want to create a dialog for outgoing request.
			return this.b2buahelper;
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
		AddressImpl addressImpl = null;
		if (this.popedRouteHeader != null) {
			addressImpl = new AddressImpl(this.popedRouteHeader.getAddress());			
		}
		return addressImpl;
	}

	/**
	 * Set the popped route
	 * 
	 * @param routeHeader
	 *            the popped route header to set
	 */
	public void setPoppedRoute(RouteHeader routeHeader) {
		this.popedRouteHeader = routeHeader;
	}

	/**
	 * {@inheritDoc}
	 * @TODO -- deal with maxforwards header.
	 */
	public Proxy getProxy() throws TooManyHopsException {
		if ( this.b2buahelper != null ) throw new IllegalStateException("Cannot proxy request");
		return getProxy(true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Proxy getProxy(boolean create) throws TooManyHopsException {
		if ( this.b2buahelper != null ) throw new IllegalStateException("Cannot proxy request");
		
		if (create && transactionApplicationData.getProxy() == null) {
			ProxyImpl proxy = new ProxyImpl(this, super.sipFactoryImpl);
			this.transactionApplicationData.setProxy(proxy);
			getSipSession().setSupervisedMode(proxy.getSupervised());
		}
		return this.transactionApplicationData.getProxy();
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

	/**
	 * {@inheritDoc}
	 */
	public void pushPath(Address uri) {

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

	public void setRequestURI(URI uri) {
		Request request = (Request) message;
		URIImpl uriImpl = (URIImpl) uri;
		javax.sip.address.URI wrappedUri = uriImpl.getURI();
		request.setRequestURI(wrappedUri);

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#setRoutingDirective(javax.servlet.sip.SipApplicationRoutingDirective, javax.servlet.sip.SipServletRequest)
	 */
	public void setRoutingDirective(SipApplicationRoutingDirective directive,
			SipServletRequest origRequest) throws IllegalStateException {
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
			if(!State.INITIAL.equals(session.getState()) && session.getOngoingTransactions().size() > 0) {
				if(logger.isDebugEnabled()) {
					logger.debug("session state : " + session.getState());
					logger.debug("numbers of ongoing transactions : " + session.getOngoingTransactions().size());
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
		RecordRouteHeader rrh = (RecordRouteHeader) message
				.getHeader(RecordRouteHeader.NAME);
		return rrh.getParameter(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map<String, String> getParameterMap() {
		RecordRouteHeader rrh = (RecordRouteHeader) message
				.getHeader(RecordRouteHeader.NAME);
		HashMap<String, String> retval = new HashMap<String, String>();
		Iterator<String> parameterNamesIt = rrh.getParameterNames();
		while (parameterNamesIt.hasNext()) {
			String parameterName = (String) parameterNamesIt.next();
			retval.put(parameterName, rrh.getParameter(parameterName));			
		}
		return retval;
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		RecordRouteHeader rrh = (RecordRouteHeader) message
				.getHeader(RecordRouteHeader.NAME);
		Vector<String> retval = new Vector<String>();
		Iterator<String> parameterNamesIt = rrh.getParameterNames();
		while (parameterNamesIt.hasNext()) {
			String parameterName = (String) parameterNamesIt.next();
			retval.add(parameterName);
		}		
		return retval.elements();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the routingDirective
	 */
	public SipApplicationRoutingDirective getRoutingDirective() {
		return routingDirective;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.servlet.sip.message.SipServletMessageImpl#send()
	 */
	@Override
	public void send() {

		try {
			Request request = (Request) super.message;
			String transport = JainSipUtils.findTransport(request);
			//Issue 112 fix by folsson
		    if(!request.getMethod().equalsIgnoreCase(Request.CANCEL) && (getSipSession().getProxyBranch() == null
			    || (transactionApplicationData.getProxy() != null && transactionApplicationData.getProxy().getRecordRoute()))){

		    	if(message.getHeader(ViaHeader.NAME) == null) {
		    		ViaHeader viaHeader = JainSipUtils.createViaHeader(
		    				sipFactoryImpl.getSipNetworkInterfaceManager(), transport, null);
		    		message.addHeader(viaHeader);
		    	}
		    }
		    
		    if(logger.isDebugEnabled()) {
		    	logger.debug("The found transport for sending request is '" + transport + "'");
		    }
			if(Request.ACK.equals(request.getMethod())) {
				super.session.getSessionCreatingDialog().sendAck(request);
				return;
			}
			//Added for initial requests only (that are not REGISTER) not for subsequent requests 
			if(isInitial() && !Request.REGISTER.equalsIgnoreCase(request.getMethod())) {
				// Additional checks for https://sip-servlets.dev.java.net/issues/show_bug.cgi?id=29
				if(transactionApplicationData.getProxy() != null &&
						transactionApplicationData.getProxy().getRecordRoute()) // If the app is proxying it already does that
				{
					if(logger.isDebugEnabled()) {
						logger.debug("Add a record route header for app composition ");
					}
					((SipStandardManager)getSipSession().getSipApplicationSession().getSipContext().getSipManager()).dumpSipSessions();
					//Add a record route header for app composition		
					addAppCompositionRRHeader();	
				}
				SipApplicationRouterInfo routerInfo = sipFactoryImpl.getNextInterestedApplication(this);
				if(routerInfo.getNextApplicationName() != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("routing back to the container " +
								"since the following app is interested " + routerInfo.getNextApplicationName());
					}
					//add a route header to direct the request back to the container 
					//to check if there is any other apps interested in it
					addInfoForRoutingBackToContainer(session.getKey().getApplicationName());
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("routing outside the container " +
								"since no more apps are is interested.");
					}
				}
			}
			((SipStandardManager)getSipSession().getSipApplicationSession().getSipContext().getSipManager()).dumpSipSessions();
			if (super.getTransaction() == null) {

				SipProvider sipProvider = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(
						transport, false).getSipProvider();
				
				ContactHeader contactHeader = (ContactHeader)request.getHeader(ContactHeader.NAME);
				if(contactHeader == null) {
					FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
					String fromName = null;
					if(fromHeader.getAddress().getURI() instanceof javax.sip.address.SipURI) {
						fromName = ((javax.sip.address.SipURI)fromHeader.getAddress().getURI()).getUser();
					}
					// Create the contact name address.
					contactHeader = 
						JainSipUtils.createContactHeader(sipFactoryImpl.getSipNetworkInterfaceManager(), transport, fromName);										
					request.addHeader(contactHeader);
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("Getting new Client Tx for request " + request);
				}
				ClientTransaction ctx = sipProvider
						.getNewClientTransaction(request);				
				
				super.session.setSessionCreatingTransaction(ctx);
				
				Dialog dialog = ctx.getDialog();
				if (dialog == null && this.createDialog) {					
					dialog = sipProvider.getNewDialog(ctx);
					super.session.setSessionCreatingDialog(dialog);
					if(logger.isDebugEnabled()) {
						logger.debug("new Dialog for request " + request + ", ref = " + dialog);
					}
				}

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

			}

			//tells the application dispatcher to stop routing the linked request
			// (in this case it would be the original request) since it has been relayed
			if(linkedRequest != null && 
					!SipApplicationRoutingDirective.NEW.equals(routingDirective)) {
				if(!RoutingState.PROXIED.equals(linkedRequest.getRoutingState())) {
					linkedRequest.setRoutingState(RoutingState.RELAYED);
				}
			}
			// Update Session state
			super.session.updateStateOnSubsequentRequest(this, false);
			
			ViaHeader viaHeader = (ViaHeader) message.getHeader(ViaHeader.NAME);
			viaHeader.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME,
					session.getKey().getApplicationName());
			if(session.getHandler() != null && session.getHandler().length() > 0) {
				viaHeader.setParameter(MessageDispatcher.RR_PARAM_HANDLER_NAME,
					session.getHandler());
			} else {
				viaHeader.setParameter(MessageDispatcher.RR_PARAM_HANDLER_NAME,
						session.getSipApplicationSession().getSipContext().getMainServlet());
			}
			
			((SipStandardManager)getSipSession().getSipApplicationSession().getSipContext().getSipManager()).dumpSipSessions();
			// If dialog does not exist or has no state.
			if (getDialog() == null || getDialog().getState() == null
					|| getDialog().getState() == DialogState.EARLY) {
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
				getDialog().sendRequest((ClientTransaction) getTransaction());
			}			
//			super.session.addOngoingTransaction(getTransaction());
			//updating the last accessed times 
			getSipSession().access();
			getSipSession().getSipApplicationSession().access();			
		} catch (Exception ex) {			
			throw new IllegalStateException("Error sending request",ex);
		}

	}

	/**
	 * Add a route header to route back to the container
	 * @param applicationName the application name that was chosen by the AR to route the request
	 * @throws ParseException
	 */
	public void addInfoForRoutingBackToContainer(String applicationName) throws ParseException {		
		Request request = (Request) super.message;
		javax.sip.address.SipURI sipURI = JainSipUtils.createRecordRouteURI(
				sipFactoryImpl.getSipNetworkInterfaceManager(), 
				JainSipUtils.findTransport(request));
		sipURI.setLrParam();
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_DIRECTIVE, 
				routingDirective.toString());
		sipURI.setParameter(MessageDispatcher.ROUTE_PARAM_PREV_APPLICATION_NAME, 
				applicationName);
		javax.sip.address.Address routeAddress = 
			SipFactories.addressFactory.createAddress(sipURI);
		RouteHeader routeHeader = 
			SipFactories.headerFactory.createRouteHeader(routeAddress);
		request.addHeader(routeHeader);						
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
                        JainSipUtils.findTransport(request));
        sipURI.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME, session.getKey().getApplicationName());
        sipURI.setParameter(MessageDispatcher.RR_PARAM_HANDLER_NAME, session.getHandler());
        
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
	public void setLastFinalResponse(SipServletResponse finalResponse) {
		if(finalResponse.getStatus() >= 200 && 
				(lastFinalResponse == null || lastFinalResponse.getStatus() < finalResponse.getStatus())) {
			this.lastFinalResponse = finalResponse;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Address getInitialPoppedRoute() {
		return initialPoppedRoute;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setInitialPoppedRoute(Address address) {
		this.initialPoppedRoute = address;;
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
}
