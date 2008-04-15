package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.stack.SIPTransaction;

import java.text.ParseException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;


public class SipFactoryImpl implements SipFactory {
	private static final Log logger = LogFactory.getLog(SipFactoryImpl.class
			.getCanonicalName());


	private static AddressFactory addressFactory = SipFactories.addressFactory;
	private static final SipFactoryImpl instance = new SipFactoryImpl();

	

	public static class NamesComparator implements Comparator<String> {

		public int compare(String o1, String o2) {

			return o1.compareToIgnoreCase(o2);
		}

	}

	private static final TreeSet<String> forbbidenToHeaderParams = new TreeSet<String>(
			new NamesComparator());

	static {
		forbbidenToHeaderParams.add("tag");

	}

	static final TreeSet<String> dialogCreationMethods = new TreeSet<String>(
			new NamesComparator());

	static {
		dialogCreationMethods.add(Request.INVITE);
		dialogCreationMethods.add(Request.SUBSCRIBE);
	}

	// TODO: Initialize those
	// ---- SIP GOV.NIST
	private SipProvider sipProvider = null;

	private MessageFactory sipMessageFactory = null;

	private HeaderFactory sipHeaderFactory = null;

	private AddressFactory sipAddressFactory = null;

	public static synchronized SipFactoryImpl getInstance() {
		return instance;
	}

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

	public Address createAddress(URI uri) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Creating Address fromm URI[" + uri.toString()
						+ "]");
			URIImpl uriImpl = (URIImpl)uri;
			return new AddressImpl(addressFactory.createAddress(uriImpl.getURI()));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public Address createAddress(URI uri, String displayName) {
		try {
			if (logger.isDebugEnabled())
				logger.debug("Creating Address from URI[" + uri.toString()
						+ "] with display name[" + displayName + "]");
			
			
			javax.sip.address.Address address = addressFactory.createAddress( ((URIImpl)uri).getURI());
			address.setDisplayName(displayName);
			return new AddressImpl(address);
			
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public SipApplicationSession createApplicationSession() {

		if (logger.isDebugEnabled())
			logger.debug("Creating new application session");
		return new SipApplicationSessionImpl(UUID.randomUUID().toString());
	}

	public SipApplicationSessionImpl createApplicationSessionImpl(String id) {
		return new SipApplicationSessionImpl(id);
	}

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

	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean sameCallId) {
		if (logger.isDebugEnabled())
			logger.debug("Creating SipServletRequest from original request["
					+ origRequest + "] with same call id[" + sameCallId + "]");
		// TODO Auto-generated method stub
		return null;
	}

	public SipURI createSipURI(String user, String host) {
		if (logger.isDebugEnabled())
			logger.debug("Creating SipURI from USER[" + user + "] HOST[" + host
					+ "]");
		// TODO Auto-generated method stub
		return null;
	}

	public URI createURI(String s) throws ServletParseException {
		if (logger.isDebugEnabled())
			logger.debug("Creating uri from [" + s + "]");
		// TODO Auto-generated method stub
		return null;
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
		Request requestToWrapp = null;

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
		ListeningPoint lp = sipProvider.getListeningPoint("udp");
		String transport = lp.getTransport();
		
		// LETS CREATE OUR HEADERS

		try {
			cseqHeader = sipHeaderFactory.createCSeqHeader(1L, method);

			javax.sip.address.Address fromAddres = sipAddressFactory
					.createAddress(from.getURI().toString());
			fromAddres.setDisplayName(from.getDisplayName());

			javax.sip.address.Address toAddress = sipAddressFactory
					.createAddress(to.getURI().toString());
			toAddress.setDisplayName(to.getDisplayName());

			toHeader = sipHeaderFactory.createToHeader(toAddress, null);
			fromHeader = sipHeaderFactory.createFromHeader(fromAddres, ""
					+ Math.random() * 100000000);
			callIdHeader = sipHeaderFactory
					.createCallIdHeader(((SipApplicationSessionImpl) sipAppSession)
							.getId());
			maxForwardsHeader = sipHeaderFactory.createMaxForwardsHeader(70);
			
			
			// FIXME: ADD ROUTE? HOW?
			// Address routeAddress = sipAddressFactory.createAddress("sip:"
			// + peerAddress + ":" + peerPort);

			// routeHeader = sipHeaderFactory.createRouteHeader(routeAddress);

			javax.sip.address.SipURI requestURI = null;


			requestURI = (javax.sip.address.SipURI) (((javax.sip.address.URI)to.getURI()).clone());
			
		
		

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

			requestToWrapp = sipMessageFactory.createRequest(null);
			
			viaHeader = JainSipUtils.createViaHeader(sipProvider, transport);
		
			// Add all headers
			requestToWrapp.setRequestURI(requestURI);
			requestToWrapp.addHeader(toHeader);
			requestToWrapp.addHeader(fromHeader);
			requestToWrapp.addHeader(callIdHeader);
			requestToWrapp.addHeader(cseqHeader);
			requestToWrapp.addHeader(maxForwardsHeader);
			requestToWrapp.addHeader(viaHeader);
			if (contactHeader != null)
				requestToWrapp.addHeader(contactHeader);
			if (routeHeader != null)
				requestToWrapp.addHeader(routeHeader);
			
		

			SipSessionImpl session = new SipSessionImpl(sipProvider,(SipApplicationSessionImpl) sipAppSession);
		
			SipServletRequest retVal = new SipServletRequestImpl(
					requestToWrapp,
					sipProvider, session, null,null, dialogCreationMethods.contains(method));

			// TODO: Do session association?
			// TODO set the routing directive as defined in 15.2.2
			
			return retVal;
		} catch (Exception  e) {

			e.printStackTrace();
			logger.error("Error creating sipServletRequest" );
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

}
