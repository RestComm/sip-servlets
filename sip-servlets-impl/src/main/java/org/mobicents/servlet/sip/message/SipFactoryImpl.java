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

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.GenericURIImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.security.AuthInfoImpl;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.failover.BalancerDescription;

public class SipFactoryImpl implements Serializable {	

	private static transient Logger logger = Logger.getLogger(SipFactoryImpl.class
			.getCanonicalName());
	private static final String TAG_PARAM = "tag";
	private static final String METHOD_PARAM = "method";
	private static final String MADDR_PARAM = "maddr";
	private static final String TTL_PARAM = "ttl";
	private static final String TRANSPORT_PARAM = "transport";
	private static final String LR_PARAM = "lr";

	private boolean useLoadBalancer = false;
	private BalancerDescription loadBalancerToUse = null;
	
	public static class NamesComparator implements Comparator<String> {
		public int compare(String o1, String o2) {
			return o1.compareToIgnoreCase(o2);
		}
	}
	
	private static final Set<String> forbbidenParams = new HashSet<String>();

	static {
		forbbidenParams.add(TAG_PARAM);
		forbbidenParams.add(METHOD_PARAM);
		forbbidenParams.add(MADDR_PARAM);
		forbbidenParams.add(TTL_PARAM);
		forbbidenParams.add(TRANSPORT_PARAM);
		forbbidenParams.add(LR_PARAM);
	}	

	private transient SipApplicationDispatcher sipApplicationDispatcher = null;
	/**
	 * Dafault constructor
	 * @param sipApplicationDispatcher 
	 */
	public SipFactoryImpl(SipApplicationDispatcher sipApplicationDispatcher) {		
		this.sipApplicationDispatcher = sipApplicationDispatcher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(java.lang.String)
	 */
	public Address createAddress(String sipAddress)
			throws ServletParseException {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating Address from [" + sipAddress + "]");
			}

			AddressImpl retval = new AddressImpl();
			retval.setValue(sipAddress);
			return retval;
		} catch (IllegalArgumentException e) {
			throw new ServletParseException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI)
	 */
	public Address createAddress(URI uri) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating Address fromm URI[" + uri.toString()
					+ "]");
		}
		URIImpl uriImpl = (URIImpl) uri;
		return new AddressImpl(SipFactories.addressFactory
				.createAddress(uriImpl.getURI()), null, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI,
	 *      java.lang.String)
	 */
	public Address createAddress(URI uri, String displayName) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating Address from URI[" + uri.toString()
						+ "] with display name[" + displayName + "]");
			}

			javax.sip.address.Address address = SipFactories.addressFactory
					.createAddress(((URIImpl) uri).getURI());
			address.setDisplayName(displayName);
			return new AddressImpl(address, null, true);

		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createApplicationSession()
	 */
	public SipApplicationSession createApplicationSession() {
		throw new UnsupportedOperationException("use createApplicationSession(SipContext sipContext) instead !");
	}
	
	/**
	 * Creates an application session associated with the context
	 * @param sipContext
	 * @return
	 */
	public SipApplicationSession createApplicationSession(SipContext sipContext) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new application session for sip context "+ sipContext.getApplicationName());
		}
		//call id not needed anymore since the sipappsessionkey is not a callid anymore but a random uuid
		SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
				sipContext.getApplicationName(), 
				null,
				false);		
		MobicentsSipApplicationSession sipApplicationSession = ((SipManager)sipContext.getManager()).getSipApplicationSession(
				sipApplicationSessionKey, true);
			
		return sipApplicationSession.getSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, javax.servlet.sip.Address,
	 *      javax.servlet.sip.Address)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, Address from, Address to, String handler) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM_A[" + from + "] TO_A[" + to + "]");
		}

		validateCreation(method, sipAppSession);

		try { 
			//javadoc specifies that a copy of the address should be done hence the clone
			return createSipServletRequest(sipAppSession, method, (Address)from.clone(), (Address)to.clone(), handler);
		} catch (ServletParseException e) {
			logger.error("Error creating sipServletRequest", e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, javax.servlet.sip.URI, javax.servlet.sip.URI)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, URI from, URI to, String handler) {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM_URI[" + from + "] TO_URI[" + to + "]");
		}

		validateCreation(method, sipAppSession);

		//javadoc specifies that a copy of the uri should be done hence the clone
		Address toA = this.createAddress(to.clone());
		Address fromA = this.createAddress(from.clone());

		try {
			return createSipServletRequest(sipAppSession, method, fromA, toA, handler);
		} catch (ServletParseException e) {
			logger.error("Error creating sipServletRequest", e);
			return null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, String from, String to, String handler) throws ServletParseException {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM["
							+ from + "] TO[" + to + "]");
		}

		validateCreation(method, sipAppSession);

		Address toA = this.createAddress(to);
		Address fromA = this.createAddress(from);

		return createSipServletRequest(sipAppSession, method, fromA, toA, handler);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipServletRequest,
	 *      boolean)
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean sameCallId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating SipServletRequest from original request["
					+ origRequest + "] with same call id[" + sameCallId + "]");
		}

	    return origRequest.getB2buaHelper().createRequest(origRequest);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createSipURI(java.lang.String,
	 *      java.lang.String)
	 */
	public SipURI createSipURI(String user, String host) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating SipURI from USER[" + user + "] HOST[" + host
					+ "]");
		}
		try {
			return new SipURIImpl(SipFactories.addressFactory.createSipURI(
					user, host));
		} catch (ParseException e) {
			logger.error("couldn't parse the SipURI from USER[" + user
					+ "] HOST[" + host + "]", e);
			throw new IllegalArgumentException("Could not create SIP URI user = " + user + " host = " + host);
		}
	}

	public URI createURI(String uri) throws ServletParseException {
//		if(!checkScheme(uri)) {
//			// testCreateProxyBranches101 needs this to be IllegalArgumentExcpetion, but the test is wrong
//			throw new ServletParseException("The uri " + uri + " is not valid");
//		}
		try {
			javax.sip.address.URI jainUri = SipFactories.addressFactory
					.createURI(uri);
			if (jainUri instanceof javax.sip.address.SipURI) {
				return new SipURIImpl(
						(javax.sip.address.SipURI) jainUri);
			} else if (jainUri instanceof javax.sip.address.TelURL) {
				return new TelURLImpl(
						(javax.sip.address.TelURL) jainUri);
			} else {
				return new GenericURIImpl(jainUri);
			}
		} catch (ParseException ex) {
			throw new ServletParseException("Bad param " + uri, ex);
		}
	}

	// ------------ HELPER METHODS
	// -------------------- createRequest
	/**
	 * Does basic check for illegal methods, wrong state, if it finds, it throws
	 * exception
	 * 
	 */
	private static void validateCreation(String method, SipApplicationSession app) {

		if (method.equals(Request.ACK)) {
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.ACK + "]!");
		}
		if (method.equals(Request.PRACK)) {
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.PRACK + "]!");
		}
		if (method.equals(Request.CANCEL)) {
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.CANCEL
							+ "]!");
		}
		if (!app.isValid()) {
			throw new IllegalArgumentException(
					"Cant associate request with invalidaded sip session application!");
		}

	}

	/**
	 * This method actually does create javax.sip.message.Request, dialog(if
	 * method is INVITE or SUBSCRIBE), ctx and wraps this in new sipsession
	 * 
	 * @param sipAppSession
	 * @param method
	 * @param from
	 * @param to
	 * @return
	 */
	private SipServletRequest createSipServletRequest(
			SipApplicationSession sipAppSession, String method, Address from,
			Address to, String handler) throws ServletParseException {
		
		MobicentsSipApplicationSession mobicentsSipApplicationSession = (MobicentsSipApplicationSession) sipAppSession;
		
		// the request object with method, request URI, and From, To, Call-ID,
		// CSeq, Route headers filled in.
		Request requestToWrap = null;

		ContactHeader contactHeader = null;
		ToHeader toHeader = null;
		FromHeader fromHeader = null;
		CSeqHeader cseqHeader = null;
		CallIdHeader callIdHeader = null;
		MaxForwardsHeader maxForwardsHeader = null;		

		// FIXME: Is this nough?
		// We need address from which this will be sent, also this one will be
		// default for contact and via
		String transport = ListeningPoint.UDP;

		// LETS CREATE OUR HEADERS			
		javax.sip.address.Address fromAddress = null;
		try {
			// Issue 676 : Any component of the from and to URIs not allowed in the context of
			// SIP From and To headers are removed from the copies [refer Table 1, Section
			// 19.1.1, RFC3261]
			for(String param : forbbidenParams) {
				from.getURI().removeParameter(param);	
			}
			
			// Issue 676 : from tags not removed so removing the tag
			from.removeParameter(TAG_PARAM);
			
			fromAddress = SipFactories.addressFactory
					.createAddress(from.getURI().toString());
			fromAddress.setDisplayName(from.getDisplayName());		
			
			fromHeader = SipFactories.headerFactory.createFromHeader(fromAddress, null);			
		} catch (Exception pe) {
			throw new ServletParseException("Impossoible to parse the given From " + from.toString(), pe);
		}
		javax.sip.address.Address toAddress = null; 
		try{
			// Issue 676 : Any component of the from and to URIs not allowed in the context of
			// SIP From and To headers are removed from the copies [refer Table 1, Section
			// 19.1.1, RFC3261]
			for(String param : forbbidenParams) {
				to.getURI().removeParameter(param);	
			}
			// Issue 676 : to tags not removed so removing the tag
			to.removeParameter(TAG_PARAM);
			
			toAddress = SipFactories.addressFactory
				.createAddress(to.getURI().toString());
			
			toAddress.setDisplayName(to.getDisplayName());

			toHeader = SipFactories.headerFactory.createToHeader(toAddress, null);										
		} catch (Exception pe) {
			throw new ServletParseException("Impossoible to parse the given To " + to.toString(), pe);
		}
		
		try {
			cseqHeader = SipFactories.headerFactory.createCSeqHeader(1L, method);
			// Fix provided by Hauke D. Issue 411
			SipApplicationSessionKey sipApplicationSessionKey = mobicentsSipApplicationSession.getKey();
//			if(sipApplicationSessionKey.isAppGeneratedKey()) {				
				callIdHeader = SipFactories.headerFactory.createCallIdHeader(
						getSipNetworkInterfaceManager().getExtendedListeningPoints().next().getSipProvider().getNewCallId().getCallId());	
//			} else {
//				callIdHeader = SipFactories.headerFactory.createCallIdHeader(
//						sipApplicationSessionKey.getId());
//			}
			maxForwardsHeader = SipFactories.headerFactory
					.createMaxForwardsHeader(JainSipUtils.MAX_FORWARD_HEADER_VALUE);
						URIImpl requestURI = (URIImpl)to.getURI().clone();

			// copying address params into headers.
			Iterator<String> keys = to.getParameterNames();

			while (keys.hasNext()) {
				String key = keys.next();				
				toHeader.setParameter(key, to.getParameter(key));
			}

			keys = from.getParameterNames();

			while (keys.hasNext()) {
				String key = keys.next();				
				fromHeader.setParameter(key, from.getParameter(key));
			}
			//Issue 112 by folsson : no via header to add will be added when the request will be sent out
			List<Header> viaHeaders = new ArrayList<Header>();
						 			
			requestToWrap = SipFactories.messageFactory.createRequest(
					requestURI.getURI(), 
					method, 
					callIdHeader, 
					cseqHeader, 
					fromHeader, 
					toHeader, 
					viaHeaders, 
					maxForwardsHeader);

			//Adding default contact header for register
			if(Request.REGISTER.equalsIgnoreCase(method)) {				
				String fromName = null;
				if(fromHeader.getAddress().getURI() instanceof javax.sip.address.SipURI) {
					fromName = ((javax.sip.address.SipURI)fromHeader.getAddress().getURI()).getUser();
				}										
				// Create the contact name address.
				contactHeader = null;
				// if a sip load balancer is present in front of the server, the contact header is the one from the sip lb
				// so that the subsequent requests can be failed over
				if(useLoadBalancer) {
					javax.sip.address.SipURI sipURI = SipFactories.addressFactory.createSipURI(fromName, loadBalancerToUse.getAddress().getHostAddress());
					sipURI.setHost(loadBalancerToUse.getAddress().getHostAddress());
					sipURI.setPort(loadBalancerToUse.getSipPort());			
					sipURI.setTransportParam(transport);
					javax.sip.address.Address contactAddress = SipFactories.addressFactory.createAddress(sipURI);
					if(fromName != null && fromName.length() > 0) {
						contactAddress.setDisplayName(fromName);
					}
					contactHeader = SipFactories.headerFactory.createContactHeader(contactAddress);													
				} else {
					contactHeader = JainSipUtils.createContactHeader(getSipNetworkInterfaceManager(), requestToWrap, fromName);
				}
			}
			// Add all headers		
			if(contactHeader != null) {
				requestToWrap.addHeader(contactHeader);
			}
			
			SipApplicationDispatcher dispatcher = 
				mobicentsSipApplicationSession.getSipContext().getSipApplicationDispatcher();
			ApplicationRoutingHeaderComposer stack = new ApplicationRoutingHeaderComposer(
					dispatcher);			
			stack.setApplicationName(sipAppSession.getApplicationName());
//			if(sipApplicationSessionKey.isAppGeneratedKey()) {
				stack.setAppGeneratedApplicationSessionId(sipApplicationSessionKey.getId());
//			}
			fromHeader.setTag(stack.toString());
			
			SipSessionKey key = SessionManagerUtil.getSipSessionKey(
					mobicentsSipApplicationSession.getKey().getApplicationName(), requestToWrap, false);
			MobicentsSipSession session = ((SipManager)mobicentsSipApplicationSession.getSipContext().getManager()).
				getSipSession(key, true, this, mobicentsSipApplicationSession);
			session.setHandler(handler);
			session.setLocalParty(new AddressImpl(fromAddress, null, false));
			session.setRemoteParty(new AddressImpl(toAddress, null, false));
			
			SipServletRequest retVal = new SipServletRequestImpl(
					requestToWrap, this, session, null, null,
					JainSipUtils.dialogCreatingMethods.contains(method));						
			
			return retVal;
		} catch (Exception e) {
			logger.error("Error creating sipServletRequest", e);
		}
 
		return null;

	}

	/**
	 * {@inheritDoc}
	 */
	public Parameterable createParameterable(String value) throws ServletParseException {
		try {			 
			Header header = SipFactories.headerFactory.createHeader(ContactHeader.NAME, value);
			return SipServletMessageImpl.createParameterable(header, SipServletMessageImpl.getFullHeaderName(header.getName()));
		} catch (ParseException e) {
			try {
				Header header = SipFactories.headerFactory.createHeader(ContentTypeHeader.NAME, value);
				return SipServletMessageImpl.createParameterable(header, SipServletMessageImpl.getFullHeaderName(header.getName()));
			} catch (ParseException pe) {
				throw new ServletParseException("Impossible to parse the following parameterable "+ value , pe);
			}
		} 		
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public SipApplicationRouterInfo getNextInterestedApplication(SipServletRequestImpl sipServletRequestImpl) {
		return sipApplicationDispatcher.getNextInterestedApplication(sipServletRequestImpl);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSessionByAppName(java.lang.String)
	 */
	public SipApplicationSession createApplicationSessionByAppName(
			String sipAppName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new application session for application name " + sipAppName);
		}
		SipContext sipContext = sipApplicationDispatcher.findSipApplication(sipAppName);
		if(sipContext == null) {
			throw new IllegalArgumentException("The specified application "+sipAppName+" is not currently deployed");
		}
		return createApplicationSession(sipContext);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSessionByKey(java.lang.String)
	 */
	public SipApplicationSession createApplicationSessionByKey(
			String sipApplicationKey) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating new application session by following key " + sipApplicationKey);
		}
		SipApplicationSessionKey sipApplicationSessionKey = null;
		try {
			sipApplicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(
					sipApplicationKey);
		} catch (ParseException e) {
			throw new IllegalArgumentException(sipApplicationKey + " is not a valid sip application session key", e);
		}		
		SipContext sipContext = sipApplicationDispatcher.findSipApplication(sipApplicationSessionKey.getApplicationName());
		if(sipContext == null) {
			throw new IllegalArgumentException("The specified application "+sipApplicationSessionKey.getApplicationName()+" is not currently deployed");
		}
		MobicentsSipApplicationSession sipApplicationSession = ((SipManager)sipContext.getManager()).getSipApplicationSession(
				sipApplicationSessionKey, true);		
		return sipApplicationSession.getSession();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createAuthInfo()
	 */
	public AuthInfo createAuthInfo() {
		return new AuthInfoImpl();
	}

	/**
	 * @return the sipApplicationDispatcher
	 */
	public SipApplicationDispatcher getSipApplicationDispatcher() {
		return sipApplicationDispatcher;
	}

	/**
	 * @param sipApplicationDispatcher the sipApplicationDispatcher to set
	 */
	public void setSipApplicationDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {
		this.sipApplicationDispatcher = sipApplicationDispatcher;
	}
	
	/**
	 * Retrieve the manager for the sip network interfaces
	 * @return the manager for the sip network interfaces
	 */
	public SipNetworkInterfaceManager getSipNetworkInterfaceManager() {
		return sipApplicationDispatcher.getSipNetworkInterfaceManager();
	}

	/**
	 * @return the loadBalancerToUse
	 */
	public BalancerDescription getLoadBalancerToUse() {
		return loadBalancerToUse;
	}

	/**
	 * @param loadBalancerToUse the loadBalancerToUse to set
	 */
	public void setLoadBalancerToUse(BalancerDescription loadBalancerToUse) {
		if(loadBalancerToUse == null) {
			useLoadBalancer = false;
		} else {
			useLoadBalancer = true;
		}
		this.loadBalancerToUse = loadBalancerToUse;
	}

	/**
	 * @return the useLoadBalancer
	 */
	public boolean isUseLoadBalancer() {
		return useLoadBalancer;
	}

	/**
	 * 
	 * @param request
	 * @throws ParseException
	 */
	public void addLoadBalancerRouteHeader(Request request) {
		try {
			javax.sip.address.SipURI sipUri = SipFactories.addressFactory.createSipURI(null, loadBalancerToUse.getAddress().getHostAddress());
			sipUri.setPort(loadBalancerToUse.getSipPort());
			sipUri.setLrParam();
			String transport = JainSipUtils.findTransport(request);
			sipUri.setTransportParam(transport);
			ExtendedListeningPoint listeningPoint = 
				getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);
			sipUri.setParameter(MessageDispatcher.ROUTE_PARAM_NODE_HOST, 
					listeningPoint.getHost(JainSipUtils.findUsePublicAddress(getSipNetworkInterfaceManager(), request, listeningPoint)));
			sipUri.setParameter(MessageDispatcher.ROUTE_PARAM_NODE_PORT, 
					"" + listeningPoint.getPort());
			javax.sip.address.Address routeAddress = 
				SipFactories.addressFactory.createAddress(sipUri);
			RouteHeader routeHeader = 
				SipFactories.headerFactory.createRouteHeader(routeAddress);
			request.addFirst(routeHeader);			
		} catch (ParseException e) {
			//this should never happen
			throw new IllegalArgumentException("Impossible to set the Load Balancer Route Header !", e);
		} catch (SipException e) {
			//this should never happen
			throw new IllegalArgumentException("Impossible to set the Load Balancer Route Header !", e);
		}
	}
}
