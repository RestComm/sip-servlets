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

import gov.nist.javax.sip.header.HeaderExt;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.MessageExt;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSession.State;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.UAMode;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.log4j.Logger;
import org.mobicents.ha.javax.sip.SipLoadBalancer;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.b2bua.MobicentsB2BUAHelper;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletMessage;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * Implementation of the B2BUA helper class.
 * 
 * 
 * @author mranga
 * @author Jean Deruelle
 */

public class B2buaHelperImpl implements MobicentsB2BUAHelper, Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(B2buaHelperImpl.class);
	
	protected static final Set<String> B2BUA_SYSTEM_HEADERS = new HashSet<String>();
	static {
		B2BUA_SYSTEM_HEADERS.add(CallIdHeader.NAME);
		B2BUA_SYSTEM_HEADERS.add(CSeqHeader.NAME);
		B2BUA_SYSTEM_HEADERS.add(ViaHeader.NAME);
		// Fix http://code.google.com/p/mobicents/issues/detail?id=3060
		// B2buaHelper.createRequest method throws IllegalArgumentException when "Route" header is included in "headerMap"
//		B2BUA_SYSTEM_HEADERS.add(RouteHeader.NAME);
		B2BUA_SYSTEM_HEADERS.add(RecordRouteHeader.NAME);
		B2BUA_SYSTEM_HEADERS.add(PathHeader.NAME);
	}
	
	// contact parameters not allowed to be modified as per JSR 289 Section 4.1.3
	protected static final Set<String> CONTACT_FORBIDDEN_PARAMETER = new HashSet<String>();
	static {
		CONTACT_FORBIDDEN_PARAMETER.add("method");
		CONTACT_FORBIDDEN_PARAMETER.add("ttl");
		CONTACT_FORBIDDEN_PARAMETER.add("maddr");
		CONTACT_FORBIDDEN_PARAMETER.add("lr");
	}
	
	//Map to handle linked sessions
	private Map<MobicentsSipSessionKey, MobicentsSipSessionKey> sessionMap = null;	
	//Map to handle linked derived sessions
	private Map<String, String> derivedSessionMap = null;

	//Map to handle responses to original request and cancel on original request
	// Issue 1550 http://code.google.com/p/mobicents/issues/detail?id=1550
	// IllegalStateException: Cannot create a response - not a server transaction gov.nist.javax.sip.stack.SIPClientTransaction
	// this map should be able to handle multiple linked requests at the same time
	private transient Map<SipServletRequestImpl, SipServletRequestImpl> originalRequestMap = null;

	private transient SipFactoryImpl sipFactoryImpl;
	
	private transient SipManager sipManager;

	public B2buaHelperImpl() {
		sessionMap = new ConcurrentHashMap<MobicentsSipSessionKey, MobicentsSipSessionKey>();
		derivedSessionMap = new ConcurrentHashMap<String, String>();
		originalRequestMap = new ConcurrentHashMap<SipServletRequestImpl, SipServletRequestImpl>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createRequest(javax.servlet.sip.SipServletRequest, boolean, java.util.Map)
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean linked, Map<String, List<String>> headerMap) throws TooManyHopsException {
		
		if(origRequest == null) {
			throw new NullPointerException("original request cannot be null");
		}
		
		if(origRequest.getMaxForwards() == 0) {
			throw new TooManyHopsException();
		}
		
		try {
			final SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			Request newRequest = (Request) origRequestImpl.message.clone();
			((MessageExt)newRequest).setApplicationData(null);
			//content should be copied too, so commented out
//		 	newRequest.removeContent();				
			//removing the via header from original request
			newRequest.removeHeader(ViaHeader.NAME);	
					
			// Remove the route header ( will point to us ).
			// commented as per issue 649
//			newRequest.removeHeader(RouteHeader.NAME);
//			String tag = Integer.toString((int) (Math.random()*1000));
//			((FromHeader) newRequest.getHeader(FromHeader.NAME)).setParameter("tag", tag);
			
			// Remove the record route headers. This is a new call leg.
			newRequest.removeHeader(RecordRouteHeader.NAME);			
	
			// Issue 1490 : http://code.google.com/p/mobicents/issues/detail?id=1490 
			// B2buaHelper.createRequest does not decrement Max-forwards
			MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) newRequest.getHeader(MaxForwardsHeader.NAME);
			try {
				maxForwardsHeader.setMaxForwards(maxForwardsHeader.getMaxForwards() -1);
			} catch (InvalidArgumentException e) {
				throw new IllegalArgumentException(e);
			}
			
			//Creating new call id
			final Iterator<MobicentsExtendedListeningPoint> listeningPointsIterator = sipFactoryImpl.getSipNetworkInterfaceManager().getExtendedListeningPoints();				
			if(!listeningPointsIterator.hasNext()) {				
				throw new IllegalStateException("There is no SIP connectors available to create the request");
			}
			final MobicentsExtendedListeningPoint extendedListeningPoint = listeningPointsIterator.next();
			final CallIdHeader callIdHeader = sipFactoryImpl.getHeaderFactory().createCallIdHeader(extendedListeningPoint.getSipProvider().getNewCallId().getCallId());
			newRequest.setHeader(callIdHeader);
			
			final List<String> contactHeaderSet = retrieveContactHeaders(headerMap,
					newRequest);	
			final FromHeader newFromHeader = (FromHeader) newRequest.getHeader(FromHeader.NAME);
			newFromHeader.removeParameter("tag");
			((ToHeader) newRequest.getHeader(ToHeader.NAME))
					.removeParameter("tag");
			
			final MobicentsSipSession originalSession = origRequestImpl.getSipSession();
			final MobicentsSipApplicationSession appSession = originalSession
					.getSipApplicationSession();			
			
			newFromHeader.setTag(ApplicationRoutingHeaderComposer.getHash(sipFactoryImpl.getSipApplicationDispatcher(), originalSession.getKey().getApplicationName(), appSession.getKey().getId()));
			
			final MobicentsSipSessionKey key = SessionManagerUtil.getSipSessionKey(appSession.getKey().getId(), originalSession.getKey().getApplicationName(), newRequest, false);
			final MobicentsSipSession session = appSession.getSipContext().getSipManager().getSipSession(key, true, sipFactoryImpl, appSession);			
			session.setHandler(originalSession.getHandler());
		
			
			// cater to http://code.google.com/p/sipservlets/issues/detail?id=31 to be able to set the rport in applications
			final SipApplicationDispatcher sipApplicationDispatcher = sipFactoryImpl.getSipApplicationDispatcher();
			final String branch = JainSipUtils.createBranch(appSession.getKey().getId(),  sipApplicationDispatcher.getHashFromApplicationName(appSession.getKey().getApplicationName()));
			ViaHeader viaHeader = JainSipUtils.createViaHeader(
    				sipFactoryImpl.getSipNetworkInterfaceManager(), newRequest, branch, session.getOutboundInterface());
			newRequest.addHeader(viaHeader);
			
			final SipServletRequestImpl newSipServletRequest = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest(
					newRequest,
					session, 
					null, 
					null, 
					JainSipUtils.DIALOG_CREATING_METHODS.contains(newRequest.getMethod()));			
			//JSR 289 Section 15.1.6	
			newSipServletRequest.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, origRequest);			
			
			final String method = origRequest.getMethod();
			// For non-REGISTER requests, the Contact header field is not copied but is populated by the container as usual but if Contact header is present in the headerMap 
			// then relevant portions of Contact header is to be used in the request created, in accordance with section 4.1.3 of the specification.
			if(Request.REGISTER.equalsIgnoreCase(method)) {
				// Issue 2565 http://code.google.com/p/mobicents/issues/detail?id=2565
				// So if the request is REGISTER the Contact Header field is copied and is used 
				if(contactHeaderSet.size() > 0) {
					// And additional contact from the map are added + stripping the forbidden params
					for (String contactHeaderValue : contactHeaderSet) {
						newSipServletRequest.addHeaderInternal(ContactHeader.NAME, contactHeaderValue, true);						
					}
					ListIterator<ContactHeader> contactHeaders = newSipServletRequest.getMessage().getHeaders(ContactHeader.NAME);
					while (contactHeaders.hasNext()) {
						final URI contactURI = contactHeaders.next().getAddress().getURI();
						// and reset its user part and params accoridng to 4.1.3 The Contact Header Field
						if(contactURI instanceof SipURI) {
							stripForbiddenContactURIParams((SipURI)contactURI);
						}
					}
				}
			} else {
				newRequest.removeHeader(ContactHeader.NAME);	
				//Creating container contact header
				ContactHeader contactHeader = null;
				String fromName = null;
				String diaplayName = newFromHeader.getAddress().getDisplayName();
				if(newFromHeader.getAddress().getURI() instanceof javax.sip.address.SipURI) {
					fromName = ((javax.sip.address.SipURI) newFromHeader.getAddress().getURI()).getUser();
				}										
				// if a sip load balancer is present in front of the server, the contact header is the one from the sip lb
				// so that the subsequent requests can be failed over
				if(sipFactoryImpl.isUseLoadBalancer()) {
					final SipLoadBalancer loadBalancerToUse = sipFactoryImpl.getLoadBalancerToUse();
					javax.sip.address.SipURI sipURI = sipFactoryImpl.getAddressFactory().createSipURI(fromName, loadBalancerToUse.getAddress().getHostAddress());
					sipURI.setHost(loadBalancerToUse.getAddress().getHostAddress());
					sipURI.setPort(loadBalancerToUse.getSipPort());			
					sipURI.setTransportParam(ListeningPoint.UDP);
					javax.sip.address.Address contactAddress = sipFactoryImpl.getAddressFactory().createAddress(sipURI);
					if(diaplayName != null && diaplayName.length() > 0) {
						contactAddress.setDisplayName(diaplayName);
					}
					contactHeader = sipFactoryImpl.getHeaderFactory().createContactHeader(contactAddress);													
				} else {					
					contactHeader = JainSipUtils.createContactHeader(sipFactoryImpl.getSipNetworkInterfaceManager(), newRequest, diaplayName, fromName, session.getOutboundInterface());
				}	
				if(contactHeaderSet.size() > 0) {
					// if the set is not empty then we adjust the values of the set to match the host and port + forbidden params of the container
					setContactHeaders(contactHeaderSet, newSipServletRequest, contactHeader);
				} else if(JainSipUtils.CONTACT_HEADER_METHODS.contains(method)) {
					// otherwise we set the container contact header for allowed methods
					newRequest.setHeader(contactHeader);
				}
			}
			
			originalRequestMap.put(newSipServletRequest, origRequestImpl);
			originalRequestMap.put(origRequestImpl, newSipServletRequest);
			
			if (linked) {
				sessionMap.put(originalSession.getKey(), session.getKey());
				sessionMap.put(session.getKey(), originalSession.getKey());
				dumpLinkedSessions();
			}
			session.setB2buaHelper(this);
			originalSession.setB2buaHelper(this);
			session.setSessionCreatingTransactionRequest(newSipServletRequest);
			
			return newSipServletRequest;
		} catch (ParseException ex) {
			logger.error("Unexpected parse exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg encountered while creating b2bua", ex);
		} catch (ServletException ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Unexpected problem while creating b2bua", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createRequest(javax.servlet.sip.SipSession, javax.servlet.sip.SipServletRequest, java.util.Map)
	 */
	public SipServletRequest createRequest(SipSession session,
			SipServletRequest origRequest, Map<String, List<String>> headerMap) {
		
		if(origRequest == null) {
			throw new NullPointerException("original request cannot be null");
		}
		
		try {
			final SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
			final MobicentsSipSession originalSession = origRequestImpl.getSipSession();
			final MobicentsSipSession sessionImpl = (MobicentsSipSession) session;

			final SipServletRequestImpl newSubsequentServletRequest = (SipServletRequestImpl) session.createRequest(origRequest.getMethod());
						
			//For non-REGISTER requests, the Contact header field is not copied 
			//but is populated by the container as usual
			
			//commented since this is not true in this case since this is a subsequent request
			// this is needed for sending challenge requests
//			if(!Request.REGISTER.equalsIgnoreCase(origRequest.getMethod())) {
//				newSubsequentServletRequest.getMessage().removeHeader(ContactHeader.NAME);
//			}
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			//They will be added later after the sip servlet request has been created
			final List<String> contactHeaderSet = retrieveContactHeaders(headerMap,
					(Request) newSubsequentServletRequest.getMessage());
						
			//If Contact header is present in the headerMap 
			//then relevant portions of Contact header is to be used in the request created, 
			//in accordance with section 4.1.3 of the specification.
			Request subsequentRequest = (Request)newSubsequentServletRequest.getMessage();
			
			// Issue 1490 : http://code.google.com/p/mobicents/issues/detail?id=1490 
			// B2buaHelper.createRequest does not decrement Max-forwards
			MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) subsequentRequest.getHeader(MaxForwardsHeader.NAME);
			try {
				maxForwardsHeader.setMaxForwards(maxForwardsHeader.getMaxForwards() -1);
			} catch (InvalidArgumentException e) {
				throw new IllegalArgumentException(e);
			}
			
			ContactHeader contactHeader = (ContactHeader) subsequentRequest.getHeader(ContactHeader.NAME);
			if(contactHeader != null && contactHeaderSet.size() > 0) {
				subsequentRequest.removeHeader(ContactHeader.NAME);
				setContactHeaders(contactHeaderSet, newSubsequentServletRequest, contactHeader);
			}
			
			//Fix for Issue 585 by alexandre sova
			if(origRequest.getContent() != null && origRequest.getContentType() != null) {
				newSubsequentServletRequest.setContentLength(origRequest.getContentLength());
				newSubsequentServletRequest.setContent(origRequest.getContent(), origRequest.getContentType());
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("newSubsequentServletRequest = " + newSubsequentServletRequest);
			}			
			
			// Added for Issue 1409 http://code.google.com/p/mobicents/issues/detail?id=1409
			copyNonSystemHeaders(origRequestImpl, newSubsequentServletRequest);
			
			originalRequestMap.put(newSubsequentServletRequest, origRequestImpl);
			originalRequestMap.put(origRequestImpl, newSubsequentServletRequest);
			
			sessionMap.put(originalSession.getKey(), sessionImpl.getKey());
			sessionMap.put(sessionImpl.getKey(), originalSession.getKey());
			dumpLinkedSessions();

			sessionImpl.setB2buaHelper(this);
			originalSession.setB2buaHelper(this);
			return newSubsequentServletRequest;
		} catch (Exception ex) {
			logger.error("Unexpected exception ", ex);
			throw new IllegalArgumentException(
					"Illegal arg encountered while creating b2bua", ex);
		}
	}

	/**
	 * Copies all the non system headers from the original request into the new subsequent request
	 * (Not needed for initial requests since a clone of the request is done)
	 * 
	 * Added for Issue 1409 http://code.google.com/p/mobicents/issues/detail?id=1409
	 */
	private void copyNonSystemHeaders(SipServletRequestImpl origRequestImpl,
			SipServletRequestImpl newSubsequentServletRequest) {
		final Message origMessage = origRequestImpl.getMessage();
		final Message subsequentMessage = newSubsequentServletRequest.getMessage();		
		ListIterator<String> headerNames = origMessage.getHeaderNames();
		while (headerNames.hasNext()) {
			String headerName = headerNames.next();
			if(!JainSipUtils.SYSTEM_HEADERS.contains(headerName) && !headerName.equalsIgnoreCase(ContactHeader.NAME)
					&& !headerName.equalsIgnoreCase(FromHeader.NAME) && !headerName.equalsIgnoreCase(ToHeader.NAME)) {
				// Issue 184 : http://code.google.com/p/sipservlets/issues/detail?id=184
				// Not all headers are copied for subsequent requests using B2buaHelper.createRequest(session, request, map)
				// Fix by Alexander Saveliev, iterate through all headers and copy them
                ListIterator<Header> origHeaderIt = origMessage.getHeaders(headerName);
				ListIterator<Header> subsHeaderIt = subsequentMessage.getHeaders(headerName);
                while (origHeaderIt.hasNext()) {
                    HeaderExt origHeader = (HeaderExt) origHeaderIt.next();
                    // Issue http://code.google.com/p/mobicents/issues/detail?id=2094
                    // B2b re-invite for authentication will duplicate Remote-Party-ID header
                    // checking if the subsequent request and original request share the same header name and value
                    // before copying to avoid duplicating the headers
                    boolean headerNameValueAlreadyPresent = false;
                    while (subsHeaderIt.hasNext() && !headerNameValueAlreadyPresent) {
                        HeaderExt subsHeader = (HeaderExt) subsHeaderIt.next();
                        if (origHeader.getValue().equals(subsHeader.getValue())) {
                            headerNameValueAlreadyPresent = true;
                        }
                    }
                    if (!headerNameValueAlreadyPresent) {
                        if (origHeader != null) {
                            subsequentMessage.addHeader(((Header) origHeader.clone()));
                            if (logger.isDebugEnabled()) {
                                logger.debug("original header " + origHeader + " copied in the new subsequent request");
                            }
                        } else {
                            // Issue 2500 http://code.google.com/p/mobicents/issues/detail?id=2500
                            // B2buaHelper.createRequest() throws a NullPointerException if the request contains an empty header
                            if (logger.isDebugEnabled()) {
                                logger.debug("trying to copy original header name " + headerName + " into the new subsequent request with an empty value");
                            }
                            try {
                                subsequentMessage.addHeader(sipFactoryImpl.getHeaderFactory().createHeader(headerName, ""));
                            } catch (ParseException e) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("couldn't copy original header name " + headerName + " into the new subsequent request with an empty value");
                                }
                            }
                        }
                    }
                }
			}
		}
	}

	/**
	 * Issue 1151 :
	 * For Contact header if present only the user part and some parameters are to be used as defined in 4.1.3 The Contact Header Field
	 * 
	 * @param newRequest
	 * @param contactHeaderSet
	 * @param newSipServletRequest
	 * @param contactHeader
	 * @throws ParseException
	 */
	private void setContactHeaders(
			final List<String> contactHeaderSet,
			final SipServletRequestImpl newSipServletRequest,
			ContactHeader contactHeader) throws ParseException {
		// we parse and add the contact headers defined in the map
		Request newRequest = (Request) newSipServletRequest.getMessage();
		for (String contactHeaderValue : contactHeaderSet) {
			newSipServletRequest.addHeaderInternal(ContactHeader.NAME, contactHeaderValue, true);
		}
		// we set up a list of contact headers to be added to the request
		List<ContactHeader> newContactHeaders = new ArrayList<ContactHeader>();
		
		ListIterator<ContactHeader> contactHeaders = newRequest.getHeaders(ContactHeader.NAME);
		while (contactHeaders.hasNext()) {
			// we clone the default Mobicents Sip Servlets Contact Header
			ContactHeader newContactHeader = (ContactHeader) contactHeader.clone();
			
			ContactHeader newRequestContactHeader = contactHeaders.next();
			final URI newURI = newRequestContactHeader.getAddress().getURI();
			newContactHeader.getAddress().setDisplayName(newRequestContactHeader.getAddress().getDisplayName());
			// and reset its user part and params accoridng to 4.1.3 The Contact Header Field
			if(newURI instanceof SipURI) {
				SipURI newSipURI = (SipURI) newURI;
				SipURI newContactSipURI = (SipURI) newContactHeader.getAddress().getURI();
				((SipURI)newContactHeader.getAddress().getURI()).setUser(newSipURI.getUser());								
				Iterator<String> uriParameters = newSipURI.getParameterNames();
				while (uriParameters.hasNext()) {
					String parameter = uriParameters.next();
					if(!CONTACT_FORBIDDEN_PARAMETER.contains(parameter)) {
						String value = newSipURI.getParameter(parameter);
						newContactSipURI.setParameter(parameter, "".equals(value) ? null : value);
					}
				}
			}
			// reset the header params according to 4.1.3 The Contact Header Field
			Iterator<String> headerParameters = newRequestContactHeader.getParameterNames();
			while (headerParameters.hasNext()) {
				String parameter = headerParameters.next();
				String value = newRequestContactHeader.getParameter(parameter);
				newContactHeader.setParameter(parameter, "".equals(value) ? null : value);
			}
			newContactHeaders.add(newContactHeader);
		}
		// we remove the previously added contact headers
		newRequest.removeHeader(ContactHeader.NAME);	
		// and set the new correct ones
		for (ContactHeader newContactHeader : newContactHeaders) {
			newRequest.addHeader(newContactHeader);
		}
	}
	
	private void stripForbiddenContactURIParams(SipURI contactURI) {
		Iterator<String> uriParameters = contactURI.getParameterNames();
		while (uriParameters.hasNext()) {
			String parameter = uriParameters.next();
			if(CONTACT_FORBIDDEN_PARAMETER.contains(parameter)) {
				contactURI.removeParameter(parameter);
				uriParameters = contactURI.getParameterNames();
			}
		}
	}
	
	/**
	 * @param headerMap
	 * @param newRequest
	 * @return
	 * @throws ParseException
	 */
	private final List<String> retrieveContactHeaders(
			Map<String, List<String>> headerMap, Request newRequest)
			throws ParseException {
		List<String> contactHeaderList = new ArrayList<String>();
		if(headerMap != null) {
			for (Entry<String, List<String>> entry : headerMap.entrySet()) {
				final String headerName = entry.getKey();
				if(!headerName.equalsIgnoreCase(ContactHeader.NAME)) {
					if(B2BUA_SYSTEM_HEADERS.contains(headerName)) {
						throw new IllegalArgumentException(headerName + " in the provided map is a system header");
					}
					// Fix for Issue 1002 : The header field map is then used to 
					// override the headers in the newly created request so the header copied from the original request is removed 
					if(entry.getValue().size() > 0)  {						
						newRequest.removeHeader(headerName);
					}
					for (String value : entry.getValue()) {							
						final Header header = sipFactoryImpl.getHeaderFactory().createHeader(
								headerName, value);					
						if(! JainSipUtils.SINGLETON_HEADER_NAMES.contains(header.getName())) {
							newRequest.addHeader(header);
						} else {
							newRequest.setHeader(header);
						}
					}
				} else {
					contactHeaderList = headerMap.get(headerName);
				}
			}
		}
		return contactHeaderList;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createResponseToOriginalRequest(javax.servlet.sip.SipSession, int, java.lang.String)
	 */
	public SipServletResponse createResponseToOriginalRequest(
			SipSession session, int status, String reasonPhrase) {

		if (session == null) {
			throw new NullPointerException("Null arg");
		}
		final MobicentsSipSession sipSession = (MobicentsSipSession) session;
		if(!sipSession.isValidInternal()) {
			throw new IllegalArgumentException("sip session " + sipSession.getId() + " is invalid !");
		}
		final MobicentsSipServletMessage sipServletMessageImpl = sipSession.getSessionCreatingTransactionRequest();
		if(!(sipServletMessageImpl instanceof SipServletRequestImpl)) {
			throw new IllegalStateException("session creating transaction message is not a request !");
		}
		final SipServletRequestImpl sipServletRequestImpl = (SipServletRequestImpl) sipServletMessageImpl;
		if(RoutingState.FINAL_RESPONSE_SENT.equals(sipServletRequestImpl.getRoutingState())) {
			// checked by TCK test com.bea.sipservlet.tck.agents.api.javax_servlet_sip.B2buaHelperTest.testCreateResponseToOriginalRequest101 
			throw new IllegalStateException("subsequent response is inconsistent with an already sent response. a Final response has already been sent ! ");
		}
		if(logger.isDebugEnabled()) {
			logger.debug("creating response to original request " + sipServletRequestImpl + " on session " + session);
		}
		return sipServletRequestImpl.createResponse(status, reasonPhrase);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getLinkedSession(javax.servlet.sip.SipSession)
	 */
	public SipSession getLinkedSession(final SipSession session) {
		if ( session == null) { 
			throw new NullPointerException("the argument is null");
		}
		final MobicentsSipSession mobicentsSipSession = (MobicentsSipSession)session;
		if(!mobicentsSipSession.isValidInternal()) {
			throw new IllegalArgumentException("the session " + mobicentsSipSession + " is invalid");
		}		
		MobicentsSipSessionKey sipSessionKey = this.sessionMap.get(mobicentsSipSession.getKey());
		if(sipSessionKey == null) {
			dumpLinkedSessions();
			if(logger.isDebugEnabled()) {
				logger.debug("No Linked Session found for this session " + session);
			}
			return null;
		}
		if(logger.isDebugEnabled()) {
			logger.debug(" trying to find linked session with key " + sipSessionKey + " for session " + mobicentsSipSession);
		}
		MobicentsSipSession linkedSession = sipManager.getSipSession(sipSessionKey, false, null, mobicentsSipSession.getSipApplicationSession());
		if(logger.isDebugEnabled()) {
			if(linkedSession != null) {
				logger.debug("Linked Session found : " + linkedSession + " for this session " + session);
			} else {
				logger.debug("No Linked Session found for this session " + session);
			}
		}				
		if(mobicentsSipSession.getParentSession() != null) {
			if(logger.isDebugEnabled()) {
				logger.debug(mobicentsSipSession + " has a parent session, it means we need to handle a forked case");
			}
			// Issue 2354 handling of forking
			String linkedDerivedSessionId = derivedSessionMap.get(mobicentsSipSession.getId());
			if(linkedDerivedSessionId == null) {	
				SipServletRequestImpl originalSipServletRequestImpl = (SipServletRequestImpl) linkedSession.getSessionCreatingTransactionRequest();
				
				String newToTag = ApplicationRoutingHeaderComposer.getHash(sipFactoryImpl.getSipApplicationDispatcher(),sipSessionKey.getApplicationName(), sipSessionKey.getApplicationSessionId());
				if(logger.isDebugEnabled()) {
					logger.debug("derived session " + mobicentsSipSession + " has no linked forked session yet, lazily creating one with new ToTag " + newToTag);
				}
				sipSessionKey = new SipSessionKey(sipSessionKey.getFromTag(), newToTag, sipSessionKey.getCallId(), sipSessionKey.getApplicationSessionId(), sipSessionKey.getApplicationName());
				linkedSession = sipManager.getSipSession(sipSessionKey, false, null, mobicentsSipSession.getSipApplicationSession());
				
				// need to clone the original request to create the forked response
				SipServletRequestImpl clonedOriginalRequest = (SipServletRequestImpl)originalSipServletRequestImpl.clone();
				clonedOriginalRequest.setSipSession(linkedSession);
				linkedSession.setSessionCreatingDialog(null);
				linkedSession.setSessionCreatingTransactionRequest(clonedOriginalRequest);
				
				derivedSessionMap.put(mobicentsSipSession.getId(), linkedSession.getId());
				derivedSessionMap.put(linkedSession.getId(), mobicentsSipSession.getId());
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("derived session " + mobicentsSipSession + " has already a linked forked session " + linkedDerivedSessionId + " reusing it");
				}
				try {
					sipSessionKey = SessionManagerUtil.parseSipSessionKey(linkedDerivedSessionId);
					if(logger.isDebugEnabled()) {
						logger.debug(" trying to find derived linked session with key " + sipSessionKey + " for session " + mobicentsSipSession);
					}
					linkedSession = sipManager.getSipSession(sipSessionKey, false, null, mobicentsSipSession.getSipApplicationSession());
				} catch (ParseException e) {
					logger.error("Couldn't parse linkedDerivedSessionId " + linkedDerivedSessionId + " linked to derived session " + mobicentsSipSession, e);
				}				
			}
		}
		
		if(linkedSession != null) {
			return linkedSession.getFacade();
		} else {
			return null;
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getLinkedSipServletRequest(javax.servlet.sip.SipServletRequest)
	 */
	public SipServletRequest getLinkedSipServletRequest(SipServletRequest req) {
		if ( req == null) { 
			throw new NullPointerException("the argument is null");
		}
		return originalRequestMap.get(req);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#getPendingMessages(javax.servlet.sip.SipSession, javax.servlet.sip.UAMode)
	 */
	public List<SipServletMessage> getPendingMessages(SipSession session,
			UAMode mode) {
		final MobicentsSipSession sipSessionImpl = (MobicentsSipSession) session;
		if(!sipSessionImpl.isValidInternal()) {
			throw new IllegalArgumentException("the session " + sipSessionImpl.getId() + " is invalid");
		}	
		
		final List<SipServletMessage> retval = new ArrayList<SipServletMessage> ();
		if (mode.equals(UAMode.UAC)) {
			final Set<Transaction> ongoingTransactions = sipSessionImpl.getOngoingTransactions();
			if(ongoingTransactions != null) {
				for (Transaction transaction: ongoingTransactions) {
					if (transaction instanceof ClientTransaction) {
						final TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
						// Issue1571 http://code.google.com/p/mobicents/issues/detail?id=1571
						// NullPointerException in SipServletResponseImpl.isCommitted 
						// race condition can occur between app code thread and processTxTerminated or Timeout thread
						if(tad != null) {
							final SipServletMessage sipServletMessage = tad.getSipServletMessage();
							if(sipServletMessage != null) {
								//not specified if ACK is a committed message in the spec but it seems not since Proxy api test
								//testCancel101 method adds a header to the ACK and it cannot be on a committed message
								//so we don't want to return ACK as pending messages here. related to TCK test B2BUAHelper.testCreateRequest002
								if (!sipServletMessage.isCommitted() && !Request.ACK.equals(sipServletMessage.getMethod()) && !Request.PRACK.equals(sipServletMessage.getMethod())) {
									retval.add(sipServletMessage);
								}
								final Set<SipServletResponseImpl> sipServletsResponses = tad.getSipServletResponses();
								if(sipServletsResponses != null) {
									for(SipServletResponseImpl sipServletResponseImpl : sipServletsResponses) {
										if (!sipServletResponseImpl.isCommitted()) {
											retval.add(sipServletResponseImpl);
										}
									}
								}
							}
						}
					}
				}
			}
		} else {
			final Set<Transaction> ongoingTransactions = sipSessionImpl.getOngoingTransactions();
			if(ongoingTransactions != null) {
				for (Transaction transaction: ongoingTransactions) {
					if (transaction instanceof ServerTransaction) {
						final TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
						final SipServletMessage sipServletMessage = tad.getSipServletMessage();
						//not specified if ACK is a committed message in the spec but it seems not since Proxy api test
						//testCanacel101 method adds a header to the ACK and it cannot be on a committed message
						//so we don't want to return ACK as pending messages here. related to TCK test B2BUAHelper.testCreateRequest002
						if (!sipServletMessage.isCommitted() && !Request.ACK.equals(sipServletMessage.getMethod())) {
							retval.add(sipServletMessage);
						}
					}
				}
			}
		}
		return retval;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#linkSipSessions(javax.servlet.sip.SipSession, javax.servlet.sip.SipSession)
	 */
	public void linkSipSessions(SipSession session1, SipSession session2) {
		if ( session1 == null) {
			throw new NullPointerException("First argument is null");
		}
		if ( session2 == null) {
			throw new NullPointerException("Second argument is null");
		}
		
		if(!((MobicentsSipSession)session1).isValidInternal() || !((MobicentsSipSession)session2).isValidInternal() || 
				State.TERMINATED.equals(((MobicentsSipSession)session1).getState()) ||
				State.TERMINATED.equals(((MobicentsSipSession)session2).getState()) ||
				!session1.getApplicationSession().equals(session2.getApplicationSession()) ||
				sessionMap.get(((MobicentsSipSession)session1).getKey()) != null ||
				sessionMap.get(((MobicentsSipSession)session2).getKey()) != null) {
			throw new IllegalArgumentException("either of the specified sessions has been terminated " +
					"or the sessions do not belong to the same application session or " +
					"one or both the sessions are already linked with some other session(s)");
		}
		this.sessionMap.put(((MobicentsSipSession)session1).getKey(), ((MobicentsSipSession)session2).getKey());
		this.sessionMap.put(((MobicentsSipSession)session2).getKey(), ((MobicentsSipSession) session1).getKey());
		if(logger.isDebugEnabled()) {
			logger.debug("sipsession " + ((MobicentsSipSession)session1).getKey() + " linked to sip session " + ((MobicentsSipSession)session2).getKey());
		}
		dumpLinkedSessions();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#unlinkSipSessions(javax.servlet.sip.SipSession)
	 */
	public void unlinkSipSessions(SipSession session) {		
		unlinkSipSessionsInternal(session, true);
	}
	
	/**
	 * 
	 * @param session
	 * @param checkSession
	 */
	public void unlinkSipSessionsInternal(SipSession session, boolean checkSession) {		
		if ( session == null) { 
			throw new NullPointerException("the argument is null");
		}
		final MobicentsSipSession key = (MobicentsSipSession) session;
		final MobicentsSipSessionKey sipSessionKey = key.getKey();
		if(checkSession) {
			if(!((MobicentsSipSession)session).isValidInternal() || 
					State.TERMINATED.equals(key.getState()) ||
					sessionMap.get(sipSessionKey) == null) {
				throw new IllegalArgumentException("the session is not currently linked to another session or it has been terminated");
			}		
		}
		final MobicentsSipSessionKey value  = this.sessionMap.remove(sipSessionKey);
		if (value != null) {
			this.sessionMap.remove(value);
			if(logger.isDebugEnabled()) {
				logger.debug("sipsession " + sipSessionKey + " unlinked from sip session " + value);
			}
		} else if(logger.isDebugEnabled()) {
			logger.debug("no sipsession for " + sipSessionKey + " to unlink");			
		}
		final String linkedDerivedSessionId = this.derivedSessionMap.remove(sipSessionKey.toString());
		if (linkedDerivedSessionId != null) {
			this.derivedSessionMap.remove(linkedDerivedSessionId);
			if(logger.isDebugEnabled()) {
				logger.debug("derived sipsession " + sipSessionKey.toString() + " unlinked from derived sip session " + linkedDerivedSessionId);
			}
		} else if(logger.isDebugEnabled()) {
			logger.debug("no derived sipsession for " + sipSessionKey.toString() + " to unlink");			
		}
		unlinkOriginalRequestInternal(sipSessionKey, !checkSession);
		dumpLinkedSessions();
	}
	
	/**
	 * 
	 * @param session
	 * @param checkSession
	 */
	public void unlinkOriginalRequestInternal(MobicentsSipSessionKey sipSessionKey, boolean force) {
		for (Entry<SipServletRequestImpl, SipServletRequestImpl> linkedRequests : originalRequestMap.entrySet()) {
			SipServletRequestImpl request1 = linkedRequests.getKey();
			SipServletRequestImpl request2 = linkedRequests.getValue();
			if(request1 != null && request2 != null) {
				MobicentsSipSessionKey key1 = request1.getSipSessionKey();
				MobicentsSipSessionKey key2 = request2.getSipSessionKey();
				if((key1!=null&&key1.equals(sipSessionKey)) || (key2!=null&&key2.equals(sipSessionKey))) {
					unlinkOriginalRequestInternal(linkedRequests.getKey(), force);
					unlinkOriginalRequestInternal(linkedRequests.getValue(), force);
				}
			}
		}				
	}
	
	/**
	 * 
	 * @param session
	 * @param checkSession
	 */
	public void unlinkOriginalRequestInternal(MobicentsSipServletRequest sipServletRequestImpl, boolean force) {
		if(sipServletRequestImpl != null) {
			SipServletRequestImpl linkedRequest = this.originalRequestMap.get(sipServletRequestImpl);			
			if(sipServletRequestImpl != null) {
				if(linkedRequest != null) {
					// Issue 2419 we unlink only if both tx are in terminated state or transaction is null (which means it has already been cleaned up)
					Transaction transaction = sipServletRequestImpl.getTransaction();
					Transaction linkedTransaction = linkedRequest.getTransaction();
					if(logger.isDebugEnabled()) {
						logger.debug("force " + force);
						logger.debug("transaction " + transaction);
						logger.debug("Linkedtransaction " + linkedTransaction);
						if(transaction != null) {
							logger.debug("transaction state " + transaction.getState());
						}
						if(linkedTransaction != null) {
							logger.debug("linked transaction state " + linkedTransaction.getState());
						}
					}
					if(force || ((transaction == null || TransactionState.TERMINATED.equals(transaction.getState())) &&
							(linkedTransaction == null || TransactionState.TERMINATED.equals(linkedTransaction.getState())))) {
						this.originalRequestMap.remove(sipServletRequestImpl);	
						this.originalRequestMap.remove(linkedRequest);
						if(logger.isDebugEnabled()) {
							logger.debug("following linked request " + linkedRequest + " unlinked from " + sipServletRequestImpl);
						}
					}
				}
			}
		}
		// Makes TCK B2buaHelperTest.testLinkUnlinkSipSessions001 && B2buaHelperTest.testB2buaHelper 
		// fails because it cleans up the tx and the response cannot thus be created
//		if(sipServletRequestImpl != null){
//			sipServletRequestImpl.cleanUp();
//		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.B2buaHelper#createRequest(javax.servlet.sip.SipServletRequest)
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest) {
		final SipServletRequestImpl newSipServletRequest = (SipServletRequestImpl) sipFactoryImpl.createRequest(origRequest, false);
		final SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
		
		final MobicentsSipSession originalSession = origRequestImpl.getSipSession();
		final MobicentsSipSession session = newSipServletRequest.getSipSession();
			
		sessionMap.put(originalSession.getKey(), session.getKey());
		sessionMap.put(session.getKey(), originalSession.getKey());	
		dumpLinkedSessions();

		originalRequestMap.put(newSipServletRequest, origRequestImpl);
		originalRequestMap.put(origRequestImpl, newSipServletRequest);
		
		session.setB2buaHelper(this);
		originalSession.setB2buaHelper(this);
		
		return newSipServletRequest;		
	}

	/**
	 * {@inheritDoc}
	 */
	public SipServletRequest createCancel(SipSession session) {
		if(session == null) throw new NullPointerException("The session for createCancel cannot be null");
		for (SipServletRequestImpl linkedRequest : originalRequestMap.keySet()) {
			if(linkedRequest.getSipSessionKey().equals(((MobicentsSipSession) session).getKey()) && 
					linkedRequest.getMethod().equalsIgnoreCase(Request.INVITE) && 
					!linkedRequest.isFinalResponseGenerated() &&
					// Fix for Issue http://code.google.com/p/mobicents/issues/detail?id=2114
					// 	In B2b servlet, after re-INVITE, and try to create CANCEL will get "final response already sent!" exception.
					linkedRequest.getLastFinalResponse() == null) {
				final SipServletRequestImpl sipServletRequestImpl = (SipServletRequestImpl)linkedRequest.createCancel();
				((MobicentsSipSession)sipServletRequestImpl.getSession()).setB2buaHelper(this);
				return sipServletRequestImpl;
			}
		}
		return null;
	}

	/**
	 * @return the sipFactoryImpl
	 */
	public SipFactoryImpl getSipFactoryImpl() {
		return sipFactoryImpl;
	}

	/**
	 * @param sipFactoryImpl the sipFactoryImpl to set
	 */
	public void setMobicentsSipFactory(MobicentsSipFactory sipFactoryImpl) {
		this.sipFactoryImpl = (SipFactoryImpl) sipFactoryImpl;
	}

	/**
	 * @return the sipManager
	 */
	public SipManager getSipManager() {
		return sipManager;
	}

	/**
	 * @param sipManager the sipManager to set
	 */
	public void setSipManager(SipManager sipManager) {
		this.sipManager = sipManager;
	}
	
	private void dumpLinkedSessions() {
		if(logger.isDebugEnabled()) {
			for (MobicentsSipSessionKey key : sessionMap.keySet()) {
				logger.debug(key + " tied to session " + sessionMap.get(key));
			}
			for (String key : derivedSessionMap.keySet()) {
				logger.debug("forked " + key + " tied to forked session " + derivedSessionMap.get(key));
			}
		}
	}

	/**
	 * @param sessionMap the sessionMap to set
	 */
	public void setSessionMap(Map<MobicentsSipSessionKey, MobicentsSipSessionKey> sessionMap) {
		this.sessionMap = sessionMap;
	}

	/**
	 * @return the sessionMap
	 */
	public Map<MobicentsSipSessionKey, MobicentsSipSessionKey> getSessionMap() {
		return sessionMap;
	}

	/**
	 * @return the originalRequestMap
	 */
	public Map<SipServletRequestImpl, SipServletRequestImpl> getOriginalRequestMap() {
		return originalRequestMap;
	}
	
	/**
	 * @param originalRequestsMap the originalRequests to set
	 */
	public void setOriginalRequestMap(Map<SipServletRequestImpl, SipServletRequestImpl> originalRequestsMap) {
		this.originalRequestMap =  originalRequestsMap;
	}
}
