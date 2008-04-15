package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.ContentEncoding;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipSession;
import javax.sip.ClientTransaction;
import javax.sip.ListeningPoint;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.AlertInfoHeader;
import javax.sip.header.AllowEventsHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.CallInfoHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentDispositionHeader;
import javax.sip.header.ContentEncodingHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ErrorInfoHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.Parameters;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.ReplyToHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubjectHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.SipFactories;

/**
 * Implementation of SipServletMessage
 * 
 * @author mranga
 * 
 */
public abstract class SipServletMessageImpl implements SipServletMessage {

	protected Request request;
	protected SipProvider provider;
	protected SipSession sipSession;
	protected ClientTransaction clientTransaction;
	protected ServerTransaction serverTransaction;
	protected SipFactoryImpl sipFactory;
	protected Map<String, Object> attributes = new HashMap<String, Object>();

	private static HeaderFactory headerFactory = SipFactories.headerFactory;

	private static Log logger = LogFactory.getLog(SipServletMessageImpl.class
			.getCanonicalName());

	/**
	 * List of headers that ARE system and all times
	 */
	protected static final HashSet<String> systemHeaders = new HashSet<String>();
	static {

		systemHeaders.add(FromHeader.NAME);
		systemHeaders.add(ToHeader.NAME);
		systemHeaders.add(CallIdHeader.NAME);
		systemHeaders.add(CSeqHeader.NAME);
		systemHeaders.add(ViaHeader.NAME);
		systemHeaders.add(RouteHeader.NAME);
		systemHeaders.add(RecordRouteHeader.NAME);
		systemHeaders.add(PathHeader.NAME);
		// This is system in messages other than REGISTER!!! ContactHeader.NAME
		// Contact is a system header field in messages other than REGISTER
		// requests and responses, 3xx and 485 responses, and 200/OPTIONS
		// responses. Additionally, for containers implementing the reliable
		// provisional responses extension, RAck and RSeq are considered system
		// headers also.
	}

	protected static final HashSet<String> addressHeadersNames = new HashSet<String>();

	static {

		// The baseline SIP specification defines the following set of header
		// fields that conform to this grammar: From, To, Contact, Route,
		// Record-Route, Reply-To, Alert-Info, Call-Info, and Error-Info

		addressHeadersNames.add(FromHeader.NAME);
		addressHeadersNames.add(ToHeader.NAME);
		addressHeadersNames.add(ContactHeader.NAME);
		addressHeadersNames.add(RouteHeader.NAME);
		addressHeadersNames.add(RecordRouteHeader.NAME);
		addressHeadersNames.add(ReplyToHeader.NAME);
		addressHeadersNames.add(AlertInfoHeader.NAME);
		addressHeadersNames.add(CallInfoHeader.NAME);
		addressHeadersNames.add(ErrorInfoHeader.NAME);

	}

	protected static final HashMap<String, String> headerCompactNamesMappings = new HashMap<String, String>();

	{ // http://www.iana.org/assignments/sip-parameters
		// Header Name compact Reference
		// ----------------- ------- ---------
		// Call-ID i [RFC3261]
		// From f [RFC3261]
		// To t [RFC3261]
		// Via v [RFC3261]
		// =========== NON SYSTEM HEADERS ========
		// Contact m [RFC3261] <-- Possibly system header
		// Accept-Contact a [RFC3841]
		// Allow-Events u [RFC3265]
		// Content-Encoding e [RFC3261]
		// Content-Length l [RFC3261]
		// Content-Type c [RFC3261]
		// Event o [RFC3265]
		// Identity y [RFC4474]
		// Identity-Info n [RFC4474]
		// Refer-To r [RFC3515]
		// Referred-By b [RFC3892]
		// Reject-Contact j [RFC3841]
		// Request-Disposition d [RFC3841]
		// Session-Expires x [RFC4028]
		// Subject s [RFC3261]
		// Supported k [RFC3261]

		headerCompactNamesMappings.put("i", CallIdHeader.NAME);
		headerCompactNamesMappings.put("f", FromHeader.NAME);
		headerCompactNamesMappings.put("t", ToHeader.NAME);
		headerCompactNamesMappings.put("v", ViaHeader.NAME);
		headerCompactNamesMappings.put("m", ContactHeader.NAME);
		// headerCompactNamesMappings.put("a",); // Where is this header?
		headerCompactNamesMappings.put("u", AllowEventsHeader.NAME);
		headerCompactNamesMappings.put("e", ContentEncodingHeader.NAME);
		headerCompactNamesMappings.put("l", ContentLengthHeader.NAME);
		headerCompactNamesMappings.put("c", ContentTypeHeader.NAME);
		headerCompactNamesMappings.put("o", EventHeader.NAME);
		// headerCompactNamesMappings.put("y", IdentityHeader); // Where is
		// sthis header?
		// headerCompactNamesMappings.put("n",IdentityInfoHeader );
		headerCompactNamesMappings.put("r", ReferToHeader.NAME);
		// headerCompactNamesMappings.put("b", ReferedByHeader);
		// headerCompactNamesMappings.put("j", RejectContactHeader);
		headerCompactNamesMappings.put("d", ContentDispositionHeader.NAME);
		// headerCompactNamesMappings.put("x", SessionExpiresHeader);
		headerCompactNamesMappings.put("s", SubjectHeader.NAME);
		headerCompactNamesMappings.put("k", SupportedHeader.NAME);
	}

	public void addAcceptLanguage(Locale locale) {
		AcceptLanguageHeader ach = headerFactory
				.createAcceptLanguageHeader(locale);
		request.addHeader(ach);

	}

	public void addAddressHeader(String name, Address addr, boolean first)
			throws IllegalArgumentException {

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Adding address header [" + hName + "] as first ["
					+ first + "] value [" + addr + "]");

		if (!isAddressTypeHeader(hName)) {

			logger.error("Header [" + hName + "] is nto address type header");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is not of an address type");
		}

		if (isSystemHeader(hName)) {

			logger.error("Error, cant add ssytem header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, cant add, modify it!!!");
		}

		try {
			Header h = headerFactory.createHeader(hName, addr.toString());
			if (first) {
				this.request.addFirst(h);
			} else {
				this.request.addLast(h);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addHeader(String name, String value) {

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Adding header under name [" + hName + "]");

		if (isSystemHeader(hName)) {

			logger.error("Cant add system header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, cant add, modify it!!!");
		}

		try {
			Header header = SipFactories.headerFactory.createHeader(hName,
					value);
			this.request.addLast(header);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied ", ex);
		}

	}

	public void addParameterableHeader(String name, Parameterable param,
			boolean first) {
		try {
			String hName = getFullHeaderName(name);

			if (logger.isDebugEnabled())
				logger.debug("Adding parametrable header under name [" + hName
						+ "] as first [" + first + "] value [" + param + "]");

			String body = param.toString();
			Header header = SipFactories.headerFactory
					.createHeader(hName, body);
			if (first)
				this.request.addFirst(header);
			else
				this.request.addLast(header);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied");
		}

	}

	public Locale getAcceptLanguage() {
		AcceptLanguageHeader alh = (AcceptLanguageHeader) this.request
				.getHeader(AcceptLanguageHeader.NAME);
		if (alh == null)
			return null;
		else
			return alh.getAcceptLanguage();

	}

	public Iterator<Locale> getAcceptLanguages() {
		LinkedList<Locale> ll = new LinkedList<Locale>();
		Iterator<Header> it = (Iterator<Header>) this.request.getHeaders(AcceptLanguageHeader.NAME);
		while (it.hasNext()) {
			AcceptLanguageHeader alh = (AcceptLanguageHeader) it.next();
			ll.add(alh.getAcceptLanguage());
		}
		return ll.iterator();
	}

	@SuppressWarnings("unchecked")
	public Address getAddressHeader(String name) throws ServletParseException {
		if (name == null)
			throw new NullPointerException();

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Fetching address header for name [" + hName + "]");

		if (!isAddressTypeHeader(hName)) {
			logger.error("Header of name [" + hName
					+ "] is not address type header!!!");
			throw new ServletParseException("Header of type [" + hName
					+ "] cant be parsed to address, wrong content type!!!");
		}

		ListIterator<Header> headers = (ListIterator<Header>) this.request
				.getHeaders(hName);
		ListIterator<Header> lit = headers;

		if (lit != null) {
			Header first = lit.next();
			if (first instanceof AddressParametersHeader) {
				try {

					return new AddressImpl((AddressParametersHeader) first);
				} catch (ParseException e) {
					throw new ServletParseException("Bad address " + first);
				}
			}
		}
		return null;

	}

	@SuppressWarnings("unchecked")
	public ListIterator<Address> getAddressHeaders(String name)
			throws ServletParseException {

		if (isAddressTypeHeader(name)) {
			throw new IllegalArgumentException(
					"Header is not address type header");
		}
		LinkedList<Address> retval = new LinkedList<Address>();
		for (Iterator<Header> it = this.request.getHeaders(name); it.hasNext();) {
			Header header = (Header) it.next();
			if (header instanceof AddressParametersHeader) {
				AddressParametersHeader aph = (AddressParametersHeader) header;
				try {
					AddressImpl addressImpl = new AddressImpl(
							(AddressParametersHeader) aph);
					retval.add(addressImpl);
				} catch (ParseException ex) {
					throw new ServletParseException("Bad header", ex);
				}

			}
		}
		return retval.listIterator();
	}

	public SipApplicationSession getApplicationSession() {
		return this.sipSession.getApplicationSession();
	}

	public SipApplicationSession getApplicationSession(boolean create) {
		if ( this.sipSession.getApplicationSession() == null && create) {
			SipApplicationSessionImpl applSession = new SipApplicationSessionImpl();
			
		}
		return null;
	}

	public Object getAttribute(String name) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		return this.attributes.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCallId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getContent() throws IOException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getContent(Class[] classes) throws IOException,
			UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getContentLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getContentLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getExpires() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Address getFrom() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public HeaderForm getHeaderForm() {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public ListIterator<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public Parameterable getParameterableHeader(String name)
			throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

	public ListIterator<Parameterable> getParameterableHeaders(String name)
			throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getRawContent() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public SipSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public SipSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	public Address getTo() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTransport() {
		// TODO Auto-generated method stub
		return null;
	}

	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeHeader(String name) {
		// TODO Auto-generated method stub

	}

	public void send() throws IOException {
		// TODO Auto-generated method stub

	}

	public void setAcceptLanguage(Locale locale) {
		// TODO Auto-generated method stub

	}

	public void setAddressHeader(String name, Address addr) {
		// TODO Auto-generated method stub

	}

	public void setAttribute(String name, Object o) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		this.attributes.put(name, o);
	}

	public void setCharacterEncoding(String enc)
			throws UnsupportedEncodingException {
		this.request.setContentEncoding(new ContentEncoding(enc));

	}

	public void setContent(Object content, String contentType)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	public void setContentLanguage(Locale locale) {
		// TODO Auto-generated method stub

	}

	public void setContentLength(int len) {
		// TODO Auto-generated method stub

	}

	public void setContentType(String type) {
		// TODO Auto-generated method stub

	}

	public void setExpires(int seconds) {
		// TODO Auto-generated method stub

	}

	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub

	}

	public void setHeaderForm(HeaderForm form) {
		// TODO Auto-generated method stub

	}

	public void setParameterableHeader(String name, Parameterable param) {
		// TODO Auto-generated method stub

	}

	/**
	 * Applications must not add, delete, or modify so-called "system" headers.
	 * These are header fields that the servlet container manages: From, To,
	 * Call-ID, CSeq, Via, Route (except through pushRoute), Record-Route.
	 * Contact is a system header field in messages other than REGISTER requests
	 * and responses, 3xx and 485 responses, and 200/OPTIONS responses.
	 * Additionally, for containers implementing the reliable provisional
	 * responses extension, RAck and RSeq are considered system headers also.
	 * 
	 * This method should return true if passed name - full or compact is name
	 * of system header in context of this message. Each subclass has to
	 * implement it in the manner that it conforms to semantics of wrapping
	 * class
	 * 
	 * @param headerName
	 * @return
	 */
	public abstract boolean isSystemHeader(String headerName);

	/**
	 * This method checks if passed name is name of address type header -
	 * according to rfc 3261
	 * 
	 * @param headerName -
	 *            name of header - either full or compact
	 * @return
	 */
	public static boolean isAddressTypeHeader(String headerName) {

		return addressHeadersNames.contains(getFullHeaderName(headerName));

	}

	/**
	 * This method tries to resolve header name - meaning if it is compact - it
	 * returns full name, if its not, it returns passed value.
	 * 
	 * @param headerName
	 * @return
	 */
	public static String getFullHeaderName(String headerName) {

		String fullName = null;
		if (headerCompactNamesMappings.containsKey(headerName)) {
			fullName = headerCompactNamesMappings.get(headerName);
		} else {
			fullName = headerName;
		}
		if (logger.isDebugEnabled())
			logger.debug("Fetching full header name for [" + headerName
					+ "] returning [" + fullName + "]");

		return fullName;
	}
	 
	public SIPTransaction getTransaction()
	{
		if(clientTransaction != null) 
			return (SIPClientTransaction) clientTransaction;
		else 
			return (SIPServerTransaction) serverTransaction;
	}

	public SipSession getSipSession() {
		return sipSession;
	}

	public void setSipSession(SipSession sipSession) {
		this.sipSession = sipSession;
	}
	

}
