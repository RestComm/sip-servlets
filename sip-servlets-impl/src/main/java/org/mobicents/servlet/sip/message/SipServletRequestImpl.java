package org.mobicents.servlet.sip.message;

import gov.nist.core.NameValue;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.ims.PathHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.sip.Address;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.header.ContactHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.address.URIImpl;

public class SipServletRequestImpl extends SipServletMessageImpl implements
		SipServletRequest {

	private SipServletRequestImpl nextRequest;
	private RouteHeader popedRouteHeader;
	private SipApplicationRoutingDirective routingDirective = SipApplicationRoutingDirective.NEW;
	
	private boolean isInitial = true;

	private static Log logger = LogFactory.getLog(SipServletRequestImpl.class);

	public SipServletRequestImpl(SipProvider provider, SipSession sipSession,
			Transaction clientTransaction, Dialog dialog) {
		super(clientTransaction.getRequest(), provider, clientTransaction,
				sipSession, dialog);

	}

	@Override
	public boolean isSystemHeader(String headerName) {

		String hName = getFullHeaderName(headerName);

		/*
		 * Contact is a system header field in messages other than REGISTER
		 * requests and responses, 3xx and 485 responses, and 200/OPTIONS
		 * responses.
		 */

		// This doesnt contain contact!!!!
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
		if (super.transaction == null
				|| super.transaction instanceof ServerTransaction)
			throw new IllegalStateException("No client transaction found!");

		try {
			Request cancelRequest = ((ClientTransaction) transaction)
					.createCancel();
			ClientTransaction clientTransaction = super.provider
					.getNewClientTransaction(cancelRequest);
			SipServletRequest newRequest = new SipServletRequestImpl(
					super.provider, super.sipSession, clientTransaction,
					transaction.getDialog());

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
		if (super.transaction == null
				|| super.transaction instanceof ClientTransaction) {
			throw new IllegalStateException(
					"Cannot create a response - not a server transaction");
		}
		try {
			Request request = this.transaction.getRequest();
			Response response = SipFactories.messageFactory.createResponse(
					statusCode, request);

			return new SipServletResponseImpl(response, provider,
					(ServerTransaction) transaction, sipSession, dialog);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code" + statusCode);
		}
	}

	public SipServletResponse createResponse(int statusCode, String reasonPhrase) {
		if (super.transaction == null
				|| super.transaction instanceof ClientTransaction) {
			throw new IllegalStateException(
					"Cannot create a response - not a server transaction");
		}
		try {
			Request request = this.transaction.getRequest();
			Response response = SipFactories.messageFactory.createResponse(
					statusCode, request);
			response.setReasonPhrase(reasonPhrase);

			return new SipServletResponseImpl(response, provider,
					(ServerTransaction) transaction, sipSession, dialog);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code" + statusCode);
		}

	}

	public B2buaHelper getB2buaHelper() {
		// TODO Auto-generated method stub
		return null;
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
	 * @see javax.servlet.sip.SipServletRequest#getPoppedRoute()
	 */
	public Address getPoppedRoute() {

		if(this.popedRouteHeader != null) {
			return new AddressImpl(this.popedRouteHeader.getAddress());
		}		
		return null;
	}

	/**
	 * Set the popped route
	 * @param routeHeader the popped route header to set
	 */
	public void setPoppedRoute(RouteHeader routeHeader) {
		this.popedRouteHeader = routeHeader;
	}
	
	public Proxy getProxy() throws TooManyHopsException {
		// TODO Auto-generated method stub
		return null;
	}

	public Proxy getProxy(boolean create) throws TooManyHopsException {
		// TODO Auto-generated method stub
		return null;
	}

	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletRequest#getRequestURI()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletRequest#isInitial()
	 */
	public boolean isInitial() {

		return this.isInitial;

	}

	public void setInitial(boolean isInitial) {
		this.isInitial = isInitial;
	}

	public void pushPath(Address uri) {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletRequest#pushRoute(javax.servlet.sip.Address)
	 */
	public void pushRoute(Address address) {
		if (logger.isDebugEnabled())
			logger.debug("Pushing route into message of value [" + address
					+ "]");

		try {
			javax.sip.header.Header p = SipFactories.headerFactory
					.createHeader(RouteHeader.NAME, address.toString());
			this.message.addFirst(p);
		} catch (Exception e) {
			logger.error("Error while pushing route [" + address + "]");
			throw new IllegalArgumentException("Error pushing route ", e);
		}

	}

	public void pushRoute(SipURI uri) {
		if (logger.isDebugEnabled())
			logger.debug("Pushing route into message of value [" + uri + "]");

		javax.sip.address.SipURI sipUri = ((SipURIImpl) uri).getSipURI();

		try {
			javax.sip.address.Address address = SipFactories.addressFactory
					.createAddress(sipUri);
			javax.sip.header.Header routeHeader = SipFactories.headerFactory
					.createRouteHeader(address);
			this.message.addFirst(routeHeader);
		} catch (Exception e) {
			logger.error("Error while pushing route [" + uri + "]");
			throw new IllegalArgumentException("Error pushing route ", e);
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

	public void setRoutingDirective(SipApplicationRoutingDirective directive,
			SipServletRequest origRequest) throws IllegalStateException {
			Request request = (Request) message;
		javax.sip.address.URI uri = request.getRequestURI();
		SipServletRequestImpl origRequestImpl  = (SipServletRequestImpl) origRequest;
		if ( (directive == SipApplicationRoutingDirective.REVERSE || directive == SipApplicationRoutingDirective.CONTINUE)
				&& ( !origRequestImpl.isInitial || origRequestImpl.isCommitted()) ) {
			throw new IllegalStateException("Bad state -- cannot set routing directive");
		}
		
		RecordRouteHeader rrh   = (RecordRouteHeader) request.getHeader(RecordRouteHeader.NAME);
		javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) rrh.getAddress().getURI();
		
		try {
				if (directive == SipApplicationRoutingDirective.NEW) {
					sipUri.setParameter("rd", "NEW");
				} else if ( directive == SipApplicationRoutingDirective.REVERSE) {
					sipUri.setParameter("rd", "REVERSE");					
				} else if ( directive == SipApplicationRoutingDirective.CONTINUE) {
					sipUri.setParameter("rd", "CONTINUE");
				}
				routingDirective = directive;			
		} catch (Exception ex) {
			String s = "error while setting routing directive";
			logger.error(s, ex);
			throw new IllegalArgumentException(s, ex);
		}
		origRequestImpl.setNextRequest(this);

	}

	/*
	 * (non-Javadoc)
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
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		RecordRouteHeader rrh = (RecordRouteHeader) message.getHeader(RecordRouteHeader.NAME);
		return rrh.getParameter(name);
	}

	public Map getParameterMap() {
		RecordRoute rrh = (RecordRoute) message.getHeader(RecordRouteHeader.NAME);
		HashMap retval  = new HashMap ();
		Collection<NameValue> params = rrh.getParameters().values();
		for ( NameValue nv : params) {
			retval.put(nv.getName(), nv.getValue());
		}
		
		return null;
	}

	public Enumeration getParameterNames() {
		RecordRoute rrh = (RecordRoute) message.getHeader(RecordRouteHeader.NAME);
		Vector<String> retval = new Vector<String> ();
		Set<String> keys = rrh.getParameters().keySet();
		retval.addAll(keys);
		return retval.elements();
	
	}

	/*
	 * (non-Javadoc)
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

	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
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
	 * @param nextRequest the nextRequest to set
	 */
	private void setNextRequest(SipServletRequestImpl nextRequest) {
		this.nextRequest = nextRequest;
	}

	/**
	 * @return the nextRequest
	 */
	private SipServletRequestImpl getNextRequest() {
		return nextRequest;
	}

	/**
	 * @return the routingDirective
	 */
	public SipApplicationRoutingDirective getRoutingDirective() {
		return routingDirective;
	}

}
