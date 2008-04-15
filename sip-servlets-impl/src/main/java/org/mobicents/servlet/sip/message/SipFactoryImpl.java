package org.mobicents.servlet.sip.message;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.SipProvider;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.session.SessionManager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

public class SipFactoryImpl implements SipFactory {
	private static final Log logger = LogFactory.getLog(SipFactoryImpl.class
			.getCanonicalName());

	public static class NamesComparator implements Comparator<String> {

		public int compare(String o1, String o2) {

			return o1.compareToIgnoreCase(o2);
		}

	}

	private static final TreeSet<String> forbbidenToHeaderParams = new TreeSet<String>(
			new NamesComparator());

	private static final TreeSet<String> dialogCreationMethods = new TreeSet<String>(
			new NamesComparator());

	static {
		forbbidenToHeaderParams.add("tag");
		dialogCreationMethods.add(Request.INVITE);
		dialogCreationMethods.add(Request.SUBSCRIBE);
	}

	private Set<SipProvider> sipProviders = null;

	private SessionManager sessionManager = null;
	
	/**
	 * Dafault constructor
	 * @param sessionManager 
	 */
	public SipFactoryImpl(SessionManager sessionManager) {
		this.sipProviders = Collections.synchronizedSet(new HashSet<SipProvider>());
		this.sessionManager = sessionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(java.lang.String)
	 */
	public Address createAddress(String sipAddress)
			throws ServletParseException {

		try {
			if (logger.isDebugEnabled())
				logger.debug("Creating Address from [" + sipAddress + "]");

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
		try {
			if (logger.isDebugEnabled())
				logger.debug("Creating Address fromm URI[" + uri.toString()
						+ "]");
			URIImpl uriImpl = (URIImpl) uri;
			return new AddressImpl(SipFactories.addressFactory
					.createAddress(uriImpl.getURI()));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI,
	 *      java.lang.String)
	 */
	public Address createAddress(URI uri, String displayName) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Creating Address from URI[" + uri.toString()
						+ "] with display name[" + displayName + "]");

			javax.sip.address.Address address = SipFactories.addressFactory
					.createAddress(((URIImpl) uri).getURI());
			address.setDisplayName(displayName);
			return new AddressImpl(address);

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

		if (logger.isDebugEnabled())
			logger.debug("Creating new application session");
		return new SipApplicationSessionImpl(UUID.randomUUID().toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, javax.servlet.sip.Address,
	 *      javax.servlet.sip.Address)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, Address from, Address to) {
		if (logger.isDebugEnabled())
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM_A[" + from + "] TO_A[" + to + "]");

		validateCreation(method, sipAppSession);

		return createSipServletRequest(sipAppSession, method, from, to);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, javax.servlet.sip.URI, javax.servlet.sip.URI)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, URI from, URI to) {
		if (logger.isDebugEnabled())
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM_URI[" + from + "] TO_URI[" + to + "]");

		validateCreation(method, sipAppSession);

		Address toA = this.createAddress(to);
		Address fromA = this.createAddress(from);

		return createSipServletRequest(sipAppSession, method, fromA, toA);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public SipServletRequest createRequest(SipApplicationSession sipAppSession,
			String method, String from, String to) throws ServletParseException {
		if (logger.isDebugEnabled())
			logger
					.debug("Creating new SipServletRequest for SipApplicationSession["
							+ sipAppSession
							+ "] METHOD["
							+ method
							+ "] FROM["
							+ from + "] TO[" + to + "]");

		validateCreation(method, sipAppSession);

		Address toA = this.createAddress(to);
		Address fromA = this.createAddress(from);

		return createSipServletRequest(sipAppSession, method, fromA, toA);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipServletRequest,
	 *      boolean)
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean sameCallId) {
		if (logger.isDebugEnabled())
			logger.debug("Creating SipServletRequest from original request["
					+ origRequest + "] with same call id[" + sameCallId + "]");

	    return origRequest.getB2buaHelper().createRequest(origRequest, true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipFactory#createSipURI(java.lang.String,
	 *      java.lang.String)
	 */
	public SipURI createSipURI(String user, String host) {
		if (logger.isDebugEnabled())
			logger.debug("Creating SipURI from USER[" + user + "] HOST[" + host
					+ "]");
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
		try {
			javax.sip.address.URI jainUri = SipFactories.addressFactory
					.createURI(uri);
			if (jainUri instanceof javax.sip.address.SipURI) {
				return new SipURIImpl(
						(gov.nist.javax.sip.address.SipUri) jainUri);
			} else if (jainUri instanceof javax.sip.address.TelURL) {
				return new TelURLImpl(
						(gov.nist.javax.sip.address.TelURLImpl) jainUri);

			}
		} catch (Exception ex) {
			throw new ServletParseException("Bad param " + uri, ex);
		}
		throw new IllegalArgumentException("Unsupported Scheme : " + uri);
	}

	// ------------ HELPER METHODS
	// -------------------- createRequest
	/**
	 * Does basic check for illegal methods, wrong state, if it finds, it throws
	 * exception
	 * 
	 */
	private void validateCreation(String method, SipApplicationSession app)
			throws IllegalArgumentException {

		if (method.equals(Request.ACK))
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.ACK + "]!");
		if (method.equals(Request.CANCEL))
			throw new IllegalArgumentException(
					"Wrong method to create request with[" + Request.CANCEL
							+ "]!");
		if (!app.isValid())
			throw new IllegalArgumentException(
					"Cant associate request with invalidaded sip session application!");

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
			Address to) {
		
		// the request object with method, request URI, and From, To, Call-ID,
		// CSeq, Route headers filled in.
		Request requestToWrap = null;

		ContactHeader contactHeader = null;
		ToHeader toHeader = null;
		FromHeader fromHeader = null;
		CSeqHeader cseqHeader = null;
		ViaHeader viaHeader = null;
		CallIdHeader callIdHeader = null;
		MaxForwardsHeader maxForwardsHeader = null;
		RouteHeader routeHeader = null;

		// FIXME: Is this nough?
		// We need address from which this will be sent, also this one will be
		// default for contact and via
		String transport = "udp";

		// LETS CREATE OUR HEADERS

		try {
			cseqHeader = SipFactories.headerFactory
					.createCSeqHeader(1L, method);

			javax.sip.address.Address fromAddres = SipFactories.addressFactory
					.createAddress(from.getURI().toString());
			fromAddres.setDisplayName(from.getDisplayName());

			javax.sip.address.Address toAddress = SipFactories.addressFactory
					.createAddress(to.getURI().toString());
			toAddress.setDisplayName(to.getDisplayName());


			toHeader = SipFactories.headerFactory.createToHeader(toAddress, null);
			fromHeader = SipFactories.headerFactory.createFromHeader(fromAddres, ""
					+ new Random().nextInt() );
			callIdHeader = SipFactories.headerFactory
					.createCallIdHeader(((SipApplicationSessionImpl) sipAppSession)
							.getId());
			maxForwardsHeader = SipFactories.headerFactory
					.createMaxForwardsHeader(70);

			// FIXME: ADD ROUTE? HOW?
			// Address routeAddress = sipAddressFactory.createAddress("sip:"
			// + peerAddress + ":" + peerPort);

			// routeHeader = sipHeaderFactory.createRouteHeader(routeAddress);

			SipURIImpl requestURI = (SipURIImpl) ((SipURIImpl) to.getURI())
					.clone();

			// now lets put header params into headers.
			Iterator<String> keys = to.getParameterNames();

			while (keys.hasNext()) {
				String key = keys.next();
				if (forbbidenToHeaderParams.contains(key)) {
					continue;
				}
				toHeader.setParameter(key, to.getParameter(key));
			}

			keys = from.getParameterNames();

			while (keys.hasNext()) {
				String key = keys.next();

				toHeader.setParameter(key, from.getParameter(key));
			}

			viaHeader = JainSipUtils.createViaHeader(sipProviders, transport,
					null);
			
			List<Header> viaHeaders = new ArrayList<Header>();
			viaHeaders.add(viaHeader);
			
			requestToWrap = SipFactories.messageFactory.createRequest(
					requestURI.getSipURI(), 
					method, 
					callIdHeader, 
					cseqHeader, 
					fromHeader, 
					toHeader, 
					viaHeaders, 
					maxForwardsHeader);

			
			// Add all headers
			if (contactHeader != null)
				requestToWrap.addHeader(contactHeader);
			if (routeHeader != null)
				requestToWrap.addHeader(routeHeader);
			
			if(((SipApplicationSessionImpl)sipAppSession).getKey() == null) {
				SipApplicationSessionKey sipApplicationSessionKey = SessionManager.getSipApplicationSessionKey(
					((SipApplicationSessionImpl)sipAppSession).getSipContext().getApplicationName(), requestToWrap);
				((SipApplicationSessionImpl)sipAppSession).setKey(sipApplicationSessionKey);
			}
			SipSessionKey key = SessionManager.getSipSessionKey(
					((SipApplicationSessionImpl)sipAppSession).getKey().getApplicationName(), requestToWrap);
			SipSessionImpl session = sessionManager.getSipSession(key, true, this);
			session.setSipApplicationSession((SipApplicationSessionImpl)sipAppSession);
//			((SipApplicationSessionImpl)sipAppSession).addSipSession(session);
//			SipSessionImpl session = new SipSessionImpl(this,
//					(SipApplicationSessionImpl) sipAppSession);
			
			SipServletRequest retVal = new SipServletRequestImpl(
					requestToWrap, this, session, null, null,
					dialogCreationMethods.contains(method));						
			
			return retVal;
		} catch (Exception e) {
			logger.error("Error creating sipServletRequest", e);
		}

		return null;

	}

	/**
	 * {@inheritDoc}
	 */
	public Parameterable createParameterable(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Add a sip Provider to the current set of sip providers
	 * 
	 * @param sipProvider
	 *            the sip provider to add
	 */
	public void addSipProvider(SipProvider sipProvider) {
		sipProviders.add(sipProvider);
	}

	/**
	 * remove the sip provider form the current set of sip providers
	 * 
	 * @param sipProvider
	 *            the sip provider to remove
	 */
	public void removeSipProvider(SipProvider sipProvider) {
		sipProviders.remove(sipProvider);
	}

	/**
	 * This method returns a read only set of the sip providers
	 * 
	 * @return read only set of the sip providers
	 */
	public Set<SipProvider> getSipProviders() {
		return Collections.unmodifiableSet(sipProviders);
	}

	/**
	 * @return the sessionManager
	 */
	public SessionManager getSessionManager() {
		return sessionManager;
	}

}
