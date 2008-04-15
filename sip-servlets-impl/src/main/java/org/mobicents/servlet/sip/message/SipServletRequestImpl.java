package org.mobicents.servlet.sip.message;

import gov.nist.core.NameValue;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.ims.PathHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.ContactHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
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
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

public class SipServletRequestImpl extends SipServletMessageImpl implements
		SipServletRequest, Cloneable {

	/* Linked request (for b2bua) */
	private SipServletRequestImpl linkedRequest;
	private SipServletRequestImpl nextRequest;

	private boolean createDialog;
	/*
	 * Popped route header - when we are the UAS we pop and keep the route
	 * header
	 */
	private RouteHeader popedRouteHeader;

	/* Cache the application routing directive in the record route header */
	private SipApplicationRoutingDirective routingDirective = SipApplicationRoutingDirective.NEW;

	private boolean isInitial = true;
	private B2buaHelper b2buahelper;

	private static Log logger = LogFactory.getLog(SipServletRequestImpl.class);

	public SipServletRequestImpl(Request request, SipProvider provider,
			SipSession sipSession, Transaction transaction, Dialog dialog,
			boolean createDialog) {
		super(request, provider, transaction, sipSession, dialog);
		this.createDialog = createDialog;

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
		if (super.getTransaction() == null
				|| super.getTransaction() instanceof ServerTransaction)
			throw new IllegalStateException("No client transaction found!");

		try {
			Request cancelRequest = ((ClientTransaction) getTransaction())
					.createCancel();
			ClientTransaction clientTransaction = super.provider
					.getNewClientTransaction(cancelRequest);
			SipServletRequest newRequest = new SipServletRequestImpl(
					cancelRequest, super.provider, super.session,
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
		if (super.getTransaction() == null
				|| super.getTransaction() instanceof ClientTransaction) {
			throw new IllegalStateException(
					"Cannot create a response - not a server transaction");
		}
		try {
			Request request = this.getTransaction().getRequest();
			Response response = SipFactories.messageFactory.createResponse(
					statusCode, request);
			if (statusCode == Response.OK) {
				ToHeader toHeader = (ToHeader) response
						.getHeader(ToHeader.NAME);
				if (toHeader.getTag() == null) // If we already have a to tag
				// dont create new
				{
					toHeader.setTag(java.util.UUID.randomUUID().toString());
					// Add the contact header for the dialog.
					String transport = ((ViaHeader) request
							.getHeader(ViaHeader.NAME)).getTransport();
					ContactHeader contactHeader = JainSipUtils
							.createContactForProvider(provider, transport);
					response.setHeader(contactHeader);
				}

				// response.addHeader(SipFactories.headerFactory.createHeader(RouteHeader.NAME,
				// "org.mobicents.servlet.sip.example.SimpleSipServlet_SimpleSipServlet"));
			}
			return new SipServletResponseImpl(response, provider,
					(ServerTransaction) getTransaction(), session, getDialog());
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code" + statusCode,
					ex);
		}
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
			response.setReasonPhrase(reasonPhrase);

			return new SipServletResponseImpl(response, provider,
					(ServerTransaction) getTransaction(), session, getDialog());
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad status code" + statusCode);
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
				Dialog dialog = this.provider.getNewDialog(this
						.getTransaction());
				dialog.setApplicationData( this.transactionApplicationData.getSipSession());
				this.session.setSessionCreatingDialog(dialog);

			}
			this.b2buahelper = new B2buaHelperImpl(this);
			this.createDialog = true; // flag that we want to create a dialog for outgoing request.
			return this.b2buahelper;
		} catch (SipException ex) {
			throw new IllegalStateException("Cannot get B2BUAHelper");
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

		if (this.popedRouteHeader != null) {
			return new AddressImpl(this.popedRouteHeader.getAddress());
		}
		return null;
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
	 * 
	 * @TODO -- deal with maxforwards header.
	 */
	public Proxy getProxy() throws TooManyHopsException {
		if ( this.b2buahelper != null ) throw new IllegalStateException("Cannot proxy request");
		return transactionApplicationData.getProxy();
	}

	public Proxy getProxy(boolean create) throws TooManyHopsException {
		if ( this.b2buahelper != null ) throw new IllegalStateException("Cannot proxy request");
		
		if (create && transactionApplicationData.getProxy() == null) {
			ProxyImpl proxy = new ProxyImpl(this, provider);
			this.transactionApplicationData.setProxy(proxy);
		}
		
		

		return getProxy();
	}

	/*
	 * (non-Javadoc)
	 * 
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

		javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) ((AddressImpl) address)
				.getAddress().getURI();
		sipUri.setLrParam();

		try {
			javax.sip.header.Header p = SipFactories.headerFactory
					.createRouteHeader(SipFactories.addressFactory
							.createAddress(sipUri));
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
		sipUri.setLrParam();

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
		SipServletRequestImpl origRequestImpl = (SipServletRequestImpl) origRequest;
		if ((directive == SipApplicationRoutingDirective.REVERSE || directive == SipApplicationRoutingDirective.CONTINUE)
				&& (!origRequestImpl.isInitial || origRequestImpl.isCommitted())) {
			throw new IllegalStateException(
					"Bad state -- cannot set routing directive");
		}

		RecordRouteHeader rrh = (RecordRouteHeader) request
				.getHeader(RecordRouteHeader.NAME);
		javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) rrh
				.getAddress().getURI();

		try {
			if (directive == SipApplicationRoutingDirective.NEW) {
				sipUri.setParameter("rd", "NEW");
			} else if (directive == SipApplicationRoutingDirective.REVERSE) {
				sipUri.setParameter("rd", "REVERSE");
			} else if (directive == SipApplicationRoutingDirective.CONTINUE) {
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

	public Map getParameterMap() {
		RecordRoute rrh = (RecordRoute) message
				.getHeader(RecordRouteHeader.NAME);
		HashMap retval = new HashMap();
		Collection<NameValue> params = rrh.getParameters().values();
		for (NameValue nv : params) {
			retval.put(nv.getName(), nv.getValue());
		}

		return null;
	}

	public Enumeration getParameterNames() {
		RecordRoute rrh = (RecordRoute) message
				.getHeader(RecordRouteHeader.NAME);
		Vector<String> retval = new Vector<String>();
		Set<String> keys = rrh.getParameters().keySet();
		retval.addAll(keys);
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
	 * @param nextRequest
	 *            the nextRequest to set
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.servlet.sip.message.SipServletMessageImpl#send()
	 */
	@Override
	public void send() {

		try {
			Request request = (Request) super.message;

			String transport = "udp";

			String transportParam = ((javax.sip.address.SipURI) request
					.getRequestURI()).getTransportParam();

			if (transportParam != null
					&& transportParam.equalsIgnoreCase("tls")) {
				transport = "tls";
			} else if (request.getContentLength().getContentLength() > 4096) {
				transport = "tcp";
			}

			if (super.getTransaction() == null) {

				ViaHeader viaHeader = JainSipUtils.createViaHeader(provider,
						transport);
				request.setHeader(viaHeader);

				ClientTransaction ctx = provider
						.getNewClientTransaction(request);

				Dialog dialog = ctx.getDialog();
				if (dialog == null && this.createDialog) {
					dialog = this.provider.getNewDialog(ctx);
				}

				// Make the dialog point here so that when the dialog event
				// comes in we can find the session quickly.
				if (dialog != null)
					dialog.setApplicationData(super.getSession());

				// SIP Request is ALWAYS pointed to by the client tx.
				// Notice that the tx appplication data is cached in the request
				// copied over to the tx so it can be quickly accessed when response
				// arrives.
				ctx.setApplicationData(this.transactionApplicationData);

				super.setTransaction(ctx);

			}
			// If dialog does not exist or has no state.
			if (getDialog() == null || getDialog().getState() == null
					|| getDialog().getState() == DialogState.EARLY) {
				((ClientTransaction) super.getTransaction()).sendRequest();
			} else {
				// This is an in-dialog request.
				getDialog().sendRequest((ClientTransaction) getTransaction());
			}
		} catch (Exception ex) {
			throw new IllegalStateException("Error sending reuqest");
		}

	}

	public void setLinkedRequest(SipServletRequestImpl linkedRequest) {
		this.linkedRequest = linkedRequest;

	}

	public SipServletRequestImpl getLinkedRequest() {
		return this.linkedRequest;
	}

}
