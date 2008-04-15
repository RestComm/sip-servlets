package org.mobicents.servlet.sip.message;

import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.ContentLanguage;
import gov.nist.javax.sip.header.ContentType;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PathHeader;
import gov.nist.javax.sip.message.SIPMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipSession;
import javax.sip.Dialog;
import javax.sip.SipFactory;
import javax.sip.Transaction;
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
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.ParameterableHeaderImpl;
import org.mobicents.servlet.sip.core.session.SessionManager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * Implementation of SipServletMessage
 * 
 * @author mranga
 * 
 */
public abstract class SipServletMessageImpl implements SipServletMessage {

	private static Log logger = LogFactory.getLog(SipServletMessageImpl.class
			.getCanonicalName());
	
	protected Message message;
	protected SipFactoryImpl sipFactoryImpl;
	protected SipSessionImpl session;

	protected Map<String, Object> attributes = new HashMap<String, Object>();
	private Transaction transaction;
	protected TransactionApplicationData transactionApplicationData;	

	private static HeaderFactory headerFactory = SipFactories.headerFactory;

	protected String _encDefault = "UTF8";

	protected HeaderForm _headerForm = HeaderForm.DEFAULT;

	protected InetAddress localAddr = null;

	protected int localPort = -1;

	// IP address of the next upstream/downstream hop from which this message
	// was received. Applications can determine the actual IP address of the UA
	// that originated the message from the message Via header fields.
	// But for upstream - thats a proxy stuff, fun with ReqURI, RouteHeader
	protected InetAddress remoteAddr = null;

	protected int remotePort = -1;

	protected String transport = null;

	protected String currentApplicationName = null;

	/**
	 * List of headers that ARE system at all times
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

	protected static final HashMap<String, String> headerCompact2FullNamesMappings = new HashMap<String, String>();

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

		headerCompact2FullNamesMappings.put("i", CallIdHeader.NAME);
		headerCompact2FullNamesMappings.put("f", FromHeader.NAME);
		headerCompact2FullNamesMappings.put("t", ToHeader.NAME);
		headerCompact2FullNamesMappings.put("v", ViaHeader.NAME);
		headerCompact2FullNamesMappings.put("m", ContactHeader.NAME);
		// headerCompact2FullNamesMappings.put("a",); // Where is this header?
		headerCompact2FullNamesMappings.put("u", AllowEventsHeader.NAME);
		headerCompact2FullNamesMappings.put("e", ContentEncodingHeader.NAME);
		headerCompact2FullNamesMappings.put("l", ContentLengthHeader.NAME);
		headerCompact2FullNamesMappings.put("c", ContentTypeHeader.NAME);
		headerCompact2FullNamesMappings.put("o", EventHeader.NAME);
		// headerCompact2FullNamesMappings.put("y", IdentityHeader); // Where is
		// sthis header?
		// headerCompact2FullNamesMappings.put("n",IdentityInfoHeader );
		headerCompact2FullNamesMappings.put("r", ReferToHeader.NAME);
		// headerCompact2FullNamesMappings.put("b", ReferedByHeader);
		// headerCompact2FullNamesMappings.put("j", RejectContactHeader);
		headerCompact2FullNamesMappings.put("d", ContentDispositionHeader.NAME);
		// headerCompact2FullNamesMappings.put("x", SessionExpiresHeader);
		headerCompact2FullNamesMappings.put("s", SubjectHeader.NAME);
		headerCompact2FullNamesMappings.put("k", SupportedHeader.NAME);
	}

	protected static final HashMap<String, String> headerFull2CompactNamesMappings = new HashMap<String, String>();

	static { // http://www.iana.org/assignments/sip-parameters
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

		headerFull2CompactNamesMappings.put(CallIdHeader.NAME, "i");
		headerFull2CompactNamesMappings.put(FromHeader.NAME, "f");
		headerFull2CompactNamesMappings.put(ToHeader.NAME, "t");
		headerFull2CompactNamesMappings.put(ViaHeader.NAME, "v");
		headerFull2CompactNamesMappings.put(ContactHeader.NAME, "m");
		// headerFull2CompactNamesMappings.put(,"a"); // Where is this header?
		headerFull2CompactNamesMappings.put(AllowEventsHeader.NAME, "u");
		headerFull2CompactNamesMappings.put(ContentEncodingHeader.NAME, "e");
		headerFull2CompactNamesMappings.put(ContentLengthHeader.NAME, "l");
		headerFull2CompactNamesMappings.put(ContentTypeHeader.NAME, "c");
		headerFull2CompactNamesMappings.put(EventHeader.NAME, "o");
		// headerCompact2FullNamesMappings.put(IdentityHeader,"y"); // Where is
		// sthis header?
		// headerCompact2FullNamesMappings.put(IdentityInfoHeader ,"n");
		headerFull2CompactNamesMappings.put(ReferToHeader.NAME, "r");
		// headerCompact2FullNamesMappings.put(ReferedByHeader,"b");
		// headerCompact2FullNamesMappings.put(RejectContactHeader,"j");
		headerFull2CompactNamesMappings.put(ContentDispositionHeader.NAME, "d");
		// headerCompact2FullNamesMappings.put(SessionExpiresHeader,"x");
		headerFull2CompactNamesMappings.put(SubjectHeader.NAME, "s");
		headerFull2CompactNamesMappings.put(SupportedHeader.NAME, "k");
	}

	protected SipServletMessageImpl(Message message,
			SipFactoryImpl sipFactoryImpl, Transaction transaction,
			SipSession sipSession, Dialog dialog) {
		if (sipFactoryImpl == null)
			throw new NullPointerException("Null factory");
		if (message == null)
			throw new NullPointerException("Null message");
//		if (sipSession == null)
//			throw new NullPointerException("Null session");
		this.sipFactoryImpl = sipFactoryImpl;
		this.message = message;
		this.transaction = transaction;
		this.session = (SipSessionImpl) sipSession;
		this.transactionApplicationData = new TransactionApplicationData(this);

		// good behaviour, lets make some default
		if (this.message.getContentEncoding() == null)
			try {
				this.message.addHeader(this.headerFactory
						.createContentEncodingHeader(this._encDefault));
			} catch (ParseException e) {
				logger.debug("Couldnt add deafualt enconding...");
				e.printStackTrace();
			}

		if (transaction != null && transaction.getApplicationData() == null)
			transaction.setApplicationData(transactionApplicationData);

	}

	public void addAcceptLanguage(Locale locale) {
		AcceptLanguageHeader ach = headerFactory
				.createAcceptLanguageHeader(locale);
		message.addHeader(ach);

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

			String nameToAdd = getCorrectHeaderName(hName);
			Header h = headerFactory.createHeader(nameToAdd, addr.toString());

			if (first) {
				this.message.addFirst(h);
			} else {
				this.message.addLast(h);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("Error adding header", e);
		}

	}

	public void addHeader(String name, String value) {

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Adding header under name [" + hName + "]");

		if (isSystemHeader(hName)) {

			logger.error("Cant add system header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, cant add,cant modify it!!!");
		}

		String nameToAdd = getCorrectHeaderName(hName);
		try {
			Header header = SipFactories.headerFactory.createHeader(nameToAdd,
					value);
			this.message.addLast(header);
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

			String nameToAdd = getCorrectHeaderName(hName);

			Header header = SipFactories.headerFactory.createHeader(nameToAdd,
					body);
			if (first)
				this.message.addFirst(header);
			else
				this.message.addLast(header);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied");
		}

	}

	public Locale getAcceptLanguage() {
		AcceptLanguageHeader alh = (AcceptLanguageHeader) this.message
				.getHeader(AcceptLanguageHeader.NAME);
		if (alh == null)
			return null;
		else
			return alh.getAcceptLanguage();

	}

	public Iterator<Locale> getAcceptLanguages() {
		LinkedList<Locale> ll = new LinkedList<Locale>();
		Iterator<Header> it = (Iterator<Header>) this.message
				.getHeaders(AcceptLanguageHeader.NAME);
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
		String nameToSearch = getCorrectHeaderName(hName);
		ListIterator<Header> headers = (ListIterator<Header>) this.message
				.getHeaders(nameToSearch);
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

		String hName = getFullHeaderName(name);

		if (isAddressTypeHeader(hName)) {
			throw new IllegalArgumentException(
					"Header is not address type header");
		}
		LinkedList<Address> retval = new LinkedList<Address>();
		String nameToSearch = getCorrectHeaderName(hName);

		for (Iterator<Header> it = this.message.getHeaders(nameToSearch); it
				.hasNext();) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {
		return getApplicationSession(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getApplicationSession(boolean)
	 */
	public SipApplicationSession getApplicationSession(boolean create) {
		if (this.session != null
				&& this.session.getApplicationSession() != null) {
			return this.session.getApplicationSession();
		} else if (create) {			
			SipApplicationSessionKey key = SessionManager.getSipApplicationSessionKey(currentApplicationName, message);
			SipApplicationSessionImpl applicationSession = 
				sipFactoryImpl.getSessionManager().getSipApplicationSession(key, create);
			if(this.session == null) {
				SipSessionKey sessionKey = SessionManager.getSipSessionKey(currentApplicationName, message);
				this.session = sipFactoryImpl.getSessionManager().getSipSession(sessionKey, create,
						sipFactoryImpl);
				this.session.setSessionCreatingTransaction(transaction);				
			} 
			this.session.setSipApplicationSession(applicationSession);
			return this.session.getApplicationSession();			
		}		
		return null;
	}

	public Object getAttribute(String name) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		return this.attributes.get(name);
	}

	public Enumeration<String> getAttributeNames() {
		Vector<String> names = new Vector<String>(this.attributes.keySet());
		return names.elements();
	}

	public String getCallId() {

		CallIdHeader id = (CallIdHeader) this.message
				.getHeader(getCorrectHeaderName(CallIdHeader.NAME));
		if (id != null)
			return id.getCallId();
		else
			return null;
	}

	public String getCharacterEncoding() {

		if (this.message.getContentEncoding() != null)
			return this.message.getContentEncoding().getEncoding();
		else
			return null;
	}

	public Object getContent() throws IOException, UnsupportedEncodingException {
		return this.message.getContent();
	}

	public Object getContent(Class[] classes) throws IOException,
			UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return null;
	}

	public Locale getContentLanguage() {
		if (this.message.getContentLanguage() != null)
			return this.message.getContentLanguage().getContentLanguage();
		else
			return null;
	}

	public int getContentLength() {
		if (this.message.getContent() != null
				&& this.message.getContentLength() != null)
			return this.message.getContentLength().getContentLength();
		else
			return 0;
	}

	public String getContentType() {
		ContentTypeHeader cth = (ContentTypeHeader) this.message
				.getHeader(getCorrectHeaderName(ContentTypeHeader.NAME));
		if (cth != null)
			return cth.getContentType();
		else
			return null;
	}

	public int getExpires() {
		if (this.message.getExpires() != null)
			return this.message.getExpires().getExpires();
		else
			return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getFrom()
	 */
	public Address getFrom() {
		// AddressImpl enforces immutability!!
		FromHeader from = (FromHeader) this.message
				.getHeader(getCorrectHeaderName(FromHeader.NAME));
		AddressImpl address = new AddressImpl(from.getAddress());
		return address;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {

		String nameToSearch = getCorrectHeaderName(name);

		if (this.message.getHeader(nameToSearch) != null)
			return ((SIPHeader) this.message.getHeader(nameToSearch))
					.getHeaderValue();
		else
			return null;
	}

	public HeaderForm getHeaderForm() {

		return this._headerForm;
	}

	public Iterator<String> getHeaderNames() {
		return this.message.getHeaderNames();
	}

	public ListIterator<String> getHeaders(String name) {

		String nameToSearch = getCorrectHeaderName(name);
		ArrayList<String> result = new ArrayList<String>();

		try {
			ListIterator<Header> list = this.message.getHeaders(nameToSearch);
			while (list.hasNext()) {
				Header h = list.next();
				result.add(h.toString());
			}
		} catch (Exception e) {
			logger.fatal("Couldnt fetch headers, original name[" + name
					+ "], name searched[" + nameToSearch + "]", e);
			return result.listIterator();
		}

		return result.listIterator();
	}

	public String getLocalAddr() {
		if (this.localAddr == null)
			return null;
		else
			return this.localAddr.getHostAddress();
	}

	public int getLocalPort() {

		return this.localPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getMethod()
	 */
	public String getMethod() {

		return message instanceof Request ? ((Request) message).getMethod()
				: ((CSeqHeader) message.getHeader(CSeqHeader.NAME)).getMethod();
	}

	public Parameterable getParameterableHeader(String name)
			throws ServletParseException {

		// FIXME:javadoc does not say that this object has to imply
		// immutability, thats wierds
		// as it suggests that its just a copy.... arghhhhhhhhhhh
		if (name == null)
			throw new NullPointerException(
					"Parametrable header name cant be null!!!");

		String nameToSearch = getCorrectHeaderName(name);

		Header h = this.message.getHeader(nameToSearch);

		return createParameterable(h.toString(), getFullHeaderName(name));
	}

	public ListIterator<Parameterable> getParameterableHeaders(String name)
			throws ServletParseException {

		ListIterator<Header> headers = this.message
				.getHeaders(getCorrectHeaderName(name));

		ArrayList<Parameterable> result = new ArrayList<Parameterable>();

		while (headers.hasNext())
			result.add(createParameterable(headers.next().toString(),
					getFullHeaderName(name)));

		return result.listIterator();
	}

	public String getProtocol() {
		// For this version of the SIP Servlet API this is always "SIP/2.0"
		return "SIP/2.0";
	}

	public byte[] getRawContent() throws IOException {
		SIPMessage message = (SIPMessage) this.message;
		if (message != null)
			return message.getRawContent();
		else
			return null;
	}

	public String getRemoteAddr() {

		// FIXME: Ambigious description!!!!
		// But: that originated the message from the message Via header fields.
		// or we have to play in proxy stuff - but thats insane!!!! - we will
		// stick to Via only... ;/
		// So for reqeust it will be top via
		// For response Via ontop of local host ?

		if (this.remoteAddr == null) {
			// If its null it means that transport level info has not been
			// set... ;/
			if (this.message instanceof Response) {

				// FIXME:....
				return null;

			} else {
				// isntanceof Reqeust
				ListIterator<ViaHeader> vias = this.message
						.getHeaders(getCorrectHeaderName(ViaHeader.NAME));
				if (vias.hasNext()) {
					ViaHeader via = vias.next();
					return via.getHost();
				} else {
					// those ethods
					return null;
				}

			}
		} else {
			return this.remoteAddr.getHostAddress();
		}

	}

	public int getRemotePort() {
		return this.remotePort;
	}

	public String getRemoteUser() {
		if (this.transaction != null
				&& this.transaction.getDialog() != null
				&& (javax.sip.address.SipURI) this.transaction.getDialog()
						.getRemoteParty() != null) {

			javax.sip.address.SipURI sipUri = (javax.sip.address.SipURI) (this.transaction
					.getDialog().getRemoteParty().getURI());
			return sipUri.getUser();
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getSession()
	 */
	public SipSession getSession() {
		return getSession(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getSession(boolean)
	 */
	public SipSession getSession(boolean create) {
		if (this.session == null && create) {
			SipSessionKey sessionKey = SessionManager.getSipSessionKey(currentApplicationName, message);
			this.session = sipFactoryImpl.getSessionManager().getSipSession(sessionKey, create,
					sipFactoryImpl);
			this.session.setSessionCreatingTransaction(transaction);
			session.setSipApplicationSession((SipApplicationSessionImpl)getApplicationSession(create));
		}
		return this.session;
	}
	
	public SipSessionImpl getSipSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSipSession(SipSessionImpl session) {
		this.session = session;
	}

	public Address getTo() {
		return new AddressImpl(((ToHeader) this.message
				.getHeader(getCorrectHeaderName(ToHeader.NAME))).getAddress());
	}

	public String getTransport() {

		return this.transport;
	}

	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isCommitted() {
		return this.transaction.getState() != null;
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

		String hName = getFullHeaderName(name);

		if (isSystemHeader(hName)) {
			throw new IllegalArgumentException("Cant remove system header["
					+ hName + "]");
		}

		String nameToSearch = getCorrectHeaderName(hName);

		this.message.removeHeader(nameToSearch);

	}

	public abstract void send();

	public void setAcceptLanguage(Locale locale) {

		AcceptLanguageHeader alh = headerFactory
				.createAcceptLanguageHeader(locale);

		this.message.addHeader(alh);

	}

	public void setAddressHeader(String name, Address addr) {

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Setting address header [" + name + "] to value ["
					+ addr + "]");

		if (isSystemHeader(hName)) {

			logger.error("Error, cant remove system header [" + hName + "]");

			throw new IllegalArgumentException(
					"Cant set system header, it is maintained by container!!");
		}

		if (!isAddressTypeHeader(hName)) {

			logger.error("Error, set header, its not address type header ["
					+ hName + "]!!");

			throw new IllegalArgumentException(
					"This header is not address type header");
		}
		Header h;
		String headerNameToAdd = getCorrectHeaderName(hName);
		try {
			h = SipFactories.headerFactory.createHeader(headerNameToAdd, addr
					.toString());
			this.message.setHeader(h);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setAttribute(String name, Object o) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		this.attributes.put(name, o);
	}

	public void setCharacterEncoding(String enc) {
		try {
			this.message.setContentEncoding(SipFactories.headerFactory
					.createContentEncodingHeader(ContentEncodingHeader.NAME
							+ ":" + enc));
		} catch (Exception ex) {
			throw new IllegalArgumentException("Unsupported encoding", ex);
		}

	}

	public void setContent(Object content, String contentType)
			throws UnsupportedEncodingException {
		String type = contentType.split("/")[0];
		String subtype = contentType.split("/")[1];
		try {
			this.message.setContent(content, new ContentType(type, subtype));
		} catch (Exception e) {
			throw new RuntimeException("Parse error reading content type", e);
		}

	}

	public void setContentLanguage(Locale locale) {
		this.message.setContentLanguage(new ContentLanguage(locale
				.getLanguage()));

	}

	public void setContentLength(int len) {

		// this.message.setContentLength(new ContentLength(len));
		String name = getCorrectHeaderName(ContentLengthHeader.NAME);

		this.message.removeHeader(name);
		try {
			Header h = headerFactory.createHeader(name, len + "");
			this.message.addHeader(h);
		} catch (ParseException e) {
			// Shouldnt happen ?
			logger.error("Error while setting content length header !!!", e);
		}

	}

	public void setContentType(String type) {

		String name = getCorrectHeaderName(ContentTypeHeader.NAME);
		try {
			Header h = headerFactory.createHeader(name, type);
			this.message
					.removeHeader(getCorrectHeaderName(ContentTypeHeader.NAME));
			this.message.addHeader(h);
		} catch (ParseException e) {
			logger.error("Error while setting content type header !!!", e);
		}

	}

	public void setExpires(int seconds) {
		try {
			Expires expiresHeader = new Expires();
			expiresHeader.setExpires(seconds);
			this.message.setExpires(expiresHeader);
		} catch (Exception e) {
			throw new RuntimeException("Error setting expiration!", e);
		}

	}

	public void setHeader(String name, String value) {
		try {
			Header header = SipFactory.getInstance().createHeaderFactory()
					.createHeader(name, value);
			this.message.setHeader(header);
		} catch (Exception e) {
			throw new RuntimeException("Error creating header!", e);
		}

	}

	public void setHeaderForm(HeaderForm form) {

		// When form changes to HeaderForm.COMPACT or HeaderForm.LONG - all
		// names must transition, if it is changed to HeaderForm.DEFAULT, no
		// action is performed
		if(form==HeaderForm.DEFAULT)
			return;
		
		if(form==HeaderForm.COMPACT)
		{
			//If header has compact name - we must change name to compact, if not, its left as it is.
			
		}

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
	 * @param headerName -
	 *            either long or compact header name
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
		if (headerCompact2FullNamesMappings.containsKey(headerName)) {
			fullName = headerCompact2FullNamesMappings.get(headerName);
		} else {
			fullName = headerName;
		}
		if (logger.isDebugEnabled())
			logger.debug("Fetching full header name for [" + headerName
					+ "] returning [" + fullName + "]");

		return fullName;
	}

	/**
	 * This method tries to determine compact header name - if passed value is
	 * compact form it is returned, otherwise method tries to find compact name -
	 * if it is found, string rpresenting compact name is returned, otherwise
	 * null!!!
	 * 
	 * @param headerName
	 * @return
	 */
	public static String getCompactName(String headerName) {

		String compactName = null;
		if (headerCompact2FullNamesMappings.containsKey(headerName)) {
			compactName = headerCompact2FullNamesMappings.get(headerName);
		} else {
			// This can be null if there is no mapping!!!
			compactName = headerFull2CompactNamesMappings.get(headerName);
		}
		if (logger.isDebugEnabled())
			logger.debug("Fetching compact header name for [" + headerName
					+ "] returning [" + compactName + "]");

		return compactName;

	}

	public String getCorrectHeaderName(String name) {
		// Mostly irritating - why this doesnt work?
		// switch(this._headerForm)
		// {
		// case HeaderForm.DEFAULT:
		// return name;
		// break;;
		// }
		return this.getCorrectHeaderName(name, this._headerForm);

	}

	public String getCorrectHeaderName(String name, HeaderForm form) {

		if (form == HeaderForm.DEFAULT) {
			return name;
		} else if (form == HeaderForm.COMPACT) {

			String compact = getCompactName(name);
			if (compact != null)
				return compact;
			else
				return name;
		} else if (form == HeaderForm.LONG) {
			return getFullHeaderName(name);
		} else {
			// ERROR ? - this shouldnt happen
			throw new IllegalStateException(
					"No default form of a header set!!!");
		}

	}

	public Transaction getTransaction() {
		return this.transaction;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// FIXME
		return super.clone();
	}

	@Override
	public String toString() {

		return this.message.toString();
	}

	protected void setTransactionApplicationData(
			TransactionApplicationData applicationData) {
		this.transactionApplicationData = applicationData;
	}

	public TransactionApplicationData getTransactionApplicationData() {
		return this.transactionApplicationData;
	}

	public Message getMessage() {
		return message;
	}

	public Dialog getDialog() {
		if (this.transaction != null)
			return this.transaction.getDialog();
		else
			return null;
	}

	/**
	 * @param transaction
	 *            the transaction to set
	 */
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	// TRANSPORT LEVEL, TO BE SET WHEN THIS IS RECEIVED
	public void setLocalAddr(InetAddress localAddr) {
		this.localAddr = localAddr;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public void setRemoteAddr(InetAddress remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public void setTransport(String transport) {
		this.transport = transport;
	}

	private Parameterable createParameterable(String whole, String hName)
			throws ServletParseException {
		// FIXME: Not sure if this is correct ;/ Ranga?
		if (logger.isDebugEnabled())
			logger.debug("Creating parametrable for [" + hName + "] from ["
					+ whole + "]");
		// Remove name
		String stringHeader = whole.substring(whole.indexOf(":") + 1).trim();
		if (!stringHeader.contains("<") || !stringHeader.contains(">")
				|| !isParameterable(getFullHeaderName(hName))) {
			logger
					.error("Cant create parametrable - argument doesnt contain uri part, which it has to have!!!");
			throw new ServletParseException("Header[" + hName
					+ "] is not parametrable");
		}

		// FIXME: This can be wrong ;/ Couldnt find list of parameterable
		// headers
		stringHeader.replace("<", "");
		String[] split = stringHeader.split(">");
		String value = split[0];
		HashMap paramMap = new HashMap();

		if (split[1].contains(";")) {
			// repleace first ";" with ""
			split[1] = split[1].replaceFirst(";", "");
			split = split[1].split(";");

			for (String pair : split) {
				String[] vals = pair.split("=");
				if (vals.length != 2) {
					logger
							.error("Wrong parameter format, expected value and name, got ["
									+ pair + "]");
					throw new ServletParseException(
							"Wrong parameter format, expected value or name["
									+ pair + "]");
				}
				paramMap.put(vals[0], vals[1]);
			}
		}

		ParameterableHeaderImpl parameterable = new ParameterableHeaderImpl(
				value, paramMap);
		return parameterable;
	}

	public static boolean isParameterable(String hName) {
		// FIXME: Add cehck?
		return true;

	}

	/**
	 * @return the currentApplicationName
	 */
	public String getCurrentApplicationName() {
		return currentApplicationName;
	}

	/**
	 * @param currentApplicationName the currentApplicationName to set
	 */
	public void setCurrentApplicationName(String currentApplicationName) {
		this.currentApplicationName = currentApplicationName;
	}
}
