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

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipSession;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.SipFactory;
import javax.sip.Transaction;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentLanguageHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderAddress;
import javax.sip.header.HeaderFactory;
import javax.sip.header.Parameters;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.ParameterableHeaderImpl;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Implementation of SipServletMessage
 * 
 * @author mranga
 * 
 */
public abstract class SipServletMessageImpl implements SipServletMessage, Serializable {


	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SipServletMessageImpl.class
			.getCanonicalName());
	
	private transient static final String CONTENT_TYPE_TEXT = "text";
//	private transient static final String HCOLON = " : ";
	private transient static final HeaderFactory headerFactory = SipFactories.headerFactory;
	
	protected Message message;
	protected transient SipFactoryImpl sipFactoryImpl;
	protected transient MobicentsSipSession session;

	protected Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
	private Transaction transaction;
	protected transient TransactionApplicationData transactionApplicationData;		

	protected String defaultEncoding = "UTF8";

	protected HeaderForm headerForm = HeaderForm.DEFAULT;
	
	// IP address of the next upstream/downstream hop from which this message
	// was received. Applications can determine the actual IP address of the UA
	// that originated the message from the message Via header fields.
	// But for upstream - thats a proxy stuff, fun with ReqURI, RouteHeader
	protected InetAddress remoteAddr = null;

	protected int remotePort = -1;

	protected String transport = null;

	protected String currentApplicationName = null;
	
	protected transient Principal userPrincipal;
	
	protected boolean isMessageSent;
	
	protected Dialog dialog;
	
	protected String method;
	
	protected SipServletMessageImpl(Message message,
			SipFactoryImpl sipFactoryImpl, Transaction transaction,
			MobicentsSipSession sipSession, Dialog dialog) {
		if (sipFactoryImpl == null)
			throw new NullPointerException("Null factory");
		if (message == null)
			throw new NullPointerException("Null message");
//		if (sipSession == null)
//			throw new NullPointerException("Null session");
		this.sipFactoryImpl = sipFactoryImpl;
		this.message = message;
		this.transaction = transaction;
		this.session = sipSession;
		this.transactionApplicationData = new TransactionApplicationData(this);
		isMessageSent = false;
		this.dialog = dialog;
		
		if(sipSession != null && dialog != null) {
			session.setSessionCreatingDialog(dialog);
			if(dialog.getApplicationData() == null) {
				dialog.setApplicationData(transactionApplicationData);
			}
		}
		
		// good behaviour, lets make some default
		//seems like bad behavior finally 
		//check http://forums.java.net/jive/thread.jspa?messageID=260944
		// => commented out
//		if (this.message.getContentEncoding() == null)
//			try {
//				this.message.addHeader(this.headerFactory
//						.createContentEncodingHeader(this.defaultEncoding));
//			} catch (ParseException e) {
//				logger.debug("Couldnt add deafualt enconding...");
//				e.printStackTrace();
//			}

		if (transaction != null && transaction.getApplicationData() == null) {
			transaction.setApplicationData(transactionApplicationData);
		}
		method = message instanceof Request ? ((Request) message).getMethod()
				: ((CSeqHeader) message.getHeader(CSeqHeader.NAME)).getMethod();
	}

	private void checkCommitted() {
		if(this.isCommitted()) {
			throw new IllegalStateException("This message is in committed state. You can not modify it");
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addAcceptLanguage(java.util.Locale)
	 */
	public void addAcceptLanguage(Locale locale) {
		checkCommitted();
		AcceptLanguageHeader ach = headerFactory
				.createAcceptLanguageHeader(locale);
		message.addHeader(ach);

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addAddressHeader(java.lang.String, javax.servlet.sip.Address, boolean)
	 */
	public void addAddressHeader(String name, Address addr, boolean first)
			throws IllegalArgumentException {

		checkCommitted();
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled()) {
			logger.debug("Adding address header [" + hName + "] as first ["
					+ first + "] value [" + addr + "]");
		}

		//we should test for 
		//This method can be used with headers which are defined to contain one 
		//or more entries matching (name-addr | addr-spec) *(SEMI generic-param) as defined in RFC 3261
//		if (!isAddressTypeHeader(hName)) {
//			logger.error("Header [" + hName + "] is not address type header");
//			throw new IllegalArgumentException("Header[" + hName
//					+ "] is not of an address type");
//		}

		if (isSystemHeader(hName)) {
			logger.error("Error, can't add system header [" + hName + "]");
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

	public void addHeaderInternal(String name, String value, boolean bypassSystemHeaderCheck) {
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Adding header under name [" + hName + "]");

		if (!bypassSystemHeaderCheck && isSystemHeader(hName)) {

			logger.error("Cant add system header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, cant add,cant modify it!!!");
		}

		String nameToAdd = getCorrectHeaderName(hName);
		
		try {
			if(JainSipUtils.multiValueHeaders.contains(name)) {
				List<Header> headers = SipFactory.getInstance().createHeaderFactory().createHeaders(nameToAdd + ":" +
						value);
				for (Header header : headers) {
					this.message.addLast(header);
				}
			} else {
				Header header = SipFactory.getInstance().createHeaderFactory()
					.createHeader(nameToAdd, value);
				this.message.addLast(header);
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied ", ex);
		}
	}
	
	//check if the submitted value is of the form header-value *(COMMA header-value)
//	private boolean isMultipleValue(String value) {
//		StringTokenizer tokenizer = new StringTokenizer(value, ",");
//		tokenizer.nextToken();
//		return tokenizer.hasMoreTokens();
//	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String name, String value) {
		checkCommitted();
		addHeaderInternal(name, value, false);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#addParameterableHeader(java.lang.String, javax.servlet.sip.Parameterable, boolean)
	 */
	public void addParameterableHeader(String name, Parameterable param,
			boolean first) {
		checkCommitted();
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
			throw new IllegalArgumentException("Illegal args supplied", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAcceptLanguage()
	 */
	public Locale getAcceptLanguage() {
		// See section 14.4 of RFC 2616 (HTTP/1.1) for more information about how the Accept-Language header 
		// must interpreted to determine the preferred language of the client.
		Locale preferredLocale = null;
		float q = 0;
		Iterator<Header> it = (Iterator<Header>) this.message
			.getHeaders(AcceptLanguageHeader.NAME);
		while (it.hasNext()) {
			AcceptLanguageHeader alh = (AcceptLanguageHeader) it.next();
			if(preferredLocale == null) {
				preferredLocale = alh.getAcceptLanguage();
				q = alh.getQValue();
			} else {
				if(alh.getQValue() > q) {
					preferredLocale = alh.getAcceptLanguage();
					q = alh.getQValue();
				}
			}
		}
		return preferredLocale;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAcceptLanguages()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAddressHeader(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Address getAddressHeader(String name) throws ServletParseException {
		if (name == null)
			throw new NullPointerException();

		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Fetching address header for name [" + hName + "]");

//		if (!isAddressTypeHeader(hName)) {
//			logger.error("Header of name [" + hName
//					+ "] is not address type header!!!");
//			throw new ServletParseException("Header of type [" + hName
//					+ "] cant be parsed to address, wrong content type!!!");
//		}
		String nameToSearch = getCorrectHeaderName(hName);
		ListIterator<Header> headers = (ListIterator<Header>) this.message
				.getHeaders(nameToSearch);
		ListIterator<Header> lit = headers;

		if (lit != null && lit.hasNext()) {
			Header first = lit.next();
			if (first instanceof HeaderAddress) {
				try {
					if(this.isCommitted()) {
						return new AddressImpl((HeaderAddress) first, false);
					} else {
						return new AddressImpl((HeaderAddress) first, true);
					}
				} catch (ParseException e) {
					throw new ServletParseException("Bad address " + first);
				}
			} else {
				Parameterable parametrable = createParameterable(first, first.getName());
				try {
					if(this.isCommitted()) {
						return new AddressImpl(SipFactories.addressFactory.createAddress(parametrable.getValue()), ((ParameterableHeaderImpl)parametrable).getInternalParameters(), false);
					} else {
						return new AddressImpl(SipFactories.addressFactory.createAddress(parametrable.getValue()), ((ParameterableHeaderImpl)parametrable).getInternalParameters(), false);
					}
				} catch (ParseException e) {
					throw new ServletParseException("Impossible to parse the following header " + name + " as an address.", e);
				}
			}
		}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAddressHeaders(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public ListIterator<Address> getAddressHeaders(String name)
			throws ServletParseException {

		String hName = getFullHeaderName(name);

		// Fix from Thomas Leseney from Nexcom systems
//		if (!isAddressTypeHeader(hName)) {
//			throw new ServletParseException(
//					"Header [" + hName + "] is not address type header");
//		}
		LinkedList<Address> retval = new LinkedList<Address>();
		String nameToSearch = getCorrectHeaderName(hName);

		for (Iterator<Header> it = this.message.getHeaders(nameToSearch); it
				.hasNext();) {
			Header header = (Header) it.next();
			if (header instanceof HeaderAddress) {
				HeaderAddress aph = (HeaderAddress) header;
				try {
					AddressImpl addressImpl = new AddressImpl(
							aph, true);
					retval.add(addressImpl);
				} catch (ParseException ex) {
					throw new ServletParseException("Bad header", ex);
				}
			}  else {
				Parameterable parametrable = createParameterable(header, header.getName());
				try {
					AddressImpl addressImpl = new AddressImpl(SipFactories.addressFactory.createAddress(parametrable.getValue()), ((ParameterableHeaderImpl)parametrable).getInternalParameters(), false);
					retval.add(addressImpl);
				} catch (ParseException e) {
					throw new ServletParseException("Impossible to parse the following header " + name + " as an address.", e);
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
				&& this.session.getSipApplicationSession() != null) {
			return this.session.getSipApplicationSession();
		} else if (create) {						
			//call id not needed anymore since the sipappsessionkey is not a callid anymore but a random uuid
			SipApplicationSessionKey key = SessionManagerUtil.getSipApplicationSessionKey(
					currentApplicationName, 
					null);
			if(this.session == null) {
				if(logger.isDebugEnabled()) {
					logger.debug("Tryin to create a new sip application session with key = " + key);
				}				
				SipContext sipContext = 
					sipFactoryImpl.getSipApplicationDispatcher().findSipApplication(currentApplicationName);				
				MobicentsSipApplicationSession applicationSession = 
					((SipManager)sipContext.getManager()).getSipApplicationSession(key, create);
				SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(key.getId(), currentApplicationName, message, false);
				if(logger.isDebugEnabled()) {
					logger.debug("Tryin to create a new sip session with key = " + sessionKey);
				}
				this.session = ((SipManager)sipContext.getManager()).getSipSession(sessionKey, create,
						sipFactoryImpl, applicationSession);
				this.session.setSessionCreatingTransaction(transaction);				
			} 
//			this.session.setSipApplicationSession(applicationSession);
			return this.session.getSipApplicationSession();			
		}		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		return this.attributes.get(name);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		Vector<String> names = new Vector<String>(this.attributes.keySet());
		return names.elements();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getCallId()
	 */
	public String getCallId() {

		CallIdHeader id = (CallIdHeader) this.message
				.getHeader(getCorrectHeaderName(CallIdHeader.NAME));
		if (id != null)
			return id.getCallId();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {

		if (this.message.getContentEncoding() != null) {
			return this.message.getContentEncoding().getEncoding();
		} else {
			ContentTypeHeader cth = (ContentTypeHeader)
				this.message.getHeader(ContentTypeHeader.NAME);
			if(cth == null) return null;
			return cth.getParameter("charset");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContent()
	 */
	public Object getContent() throws IOException, UnsupportedEncodingException {
		ContentTypeHeader contentTypeHeader = (ContentTypeHeader) 
			this.message.getHeader(ContentTypeHeader.NAME);		
		if(contentTypeHeader!= null && CONTENT_TYPE_TEXT.equals(contentTypeHeader.getContentType())) {
			String content = null;
			if(message.getRawContent() != null) {
				String charset = this.getCharacterEncoding();
				if(charset == null) {
					content = new String(message.getRawContent());	
				} else {
					content = new String(message.getRawContent(), charset);
				}
			} else {
				content = "";
			}
			return content;
		} else {
			return this.message.getRawContent();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContentLanguage()
	 */
	public Locale getContentLanguage() {
		if (this.message.getContentLanguage() != null)
			return this.message.getContentLanguage().getContentLanguage();
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContentLength()
	 */
	public int getContentLength() {
		if (this.message.getContentLength() != null) {
			return this.message.getContentLength().getContentLength();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getContentType()
	 */
	public String getContentType() {
		ContentTypeHeader cth = (ContentTypeHeader) this.message
				.getHeader(getCorrectHeaderName(ContentTypeHeader.NAME));
		if (cth != null)
		{
			String contentType = cth.getContentType();
			String contentSubType = cth.getContentSubType();
			if(contentSubType != null) 
				return contentType + "/" + contentSubType;
			return contentType;
		}
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getExpires()
	 */
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
		AddressImpl address = new AddressImpl(from.getAddress(), AddressImpl.getParameters((Parameters)from), transaction == null ? true : false);
		return address;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {

		String nameToSearch = getCorrectHeaderName(name);
		String value = null;
		if (this.message.getHeader(nameToSearch) != null) {
			value = ((SIPHeader) this.message.getHeader(nameToSearch))
					.getValue();
		}
//		if(logger.isDebugEnabled()) {
//			logger.debug("getHeader "+ name+ ", value="+ value	);
//		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeaderForm()
	 */
	public HeaderForm getHeaderForm() {
		return this.headerForm;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeaderNames()
	 */
	public Iterator<String> getHeaderNames() {
		return this.message.getHeaderNames();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getHeaders(java.lang.String)
	 */
	public ListIterator<String> getHeaders(String name) {
		String nameToSearch = getCorrectHeaderName(name);
		ArrayList<String> result = new ArrayList<String>();

		try {
			ListIterator<Header> list = this.message.getHeaders(nameToSearch);
			while (list != null && list.hasNext()) {
				Header h = list.next();
				result.add(((SIPHeader)h).getHeaderValue());
			}
		} catch (Exception e) {
			logger.fatal("Couldnt fetch headers, original name[" + name
					+ "], name searched[" + nameToSearch + "]", e);
			return result.listIterator();
		}
		return result.listIterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.SipServletMessage#getMethod()
	 */
	public String getMethod() {

		return method;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getParameterableHeader(java.lang.String)
	 */
	public Parameterable getParameterableHeader(String name)
			throws ServletParseException {

		if (name == null)
			throw new NullPointerException(
					"Parametrable header name cant be null!!!");

		String nameToSearch = getCorrectHeaderName(name);

		Header h = this.message.getHeader(nameToSearch);
		
		if(!isParameterable(name)) {
			throw new ServletParseException(name + " header is not parameterable !");
		}
		
		if(h == null) {
			return null;
		}
		
		return createParameterable(h, getFullHeaderName(name));
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getParameterableHeaders(java.lang.String)
	 */
	public ListIterator<Parameterable> getParameterableHeaders(String name)
			throws ServletParseException {

		ListIterator<Header> headers = this.message
				.getHeaders(getCorrectHeaderName(name));

		ArrayList<Parameterable> result = new ArrayList<Parameterable>();

		while (headers != null && headers.hasNext())
			result.add(createParameterable(headers.next(),
					getFullHeaderName(name)));

		if(!isParameterable(name)) {
			throw new ServletParseException(name + " header is not parameterable !");
		}			
		
		return result.listIterator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getProtocol()
	 */
	public String getProtocol() {
		// For this version of the SIP Servlet API this is always "SIP/2.0"
		return "SIP/2.0";
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRawContent()
	 */
	public byte[] getRawContent() throws IOException {		
		if (message != null)
			return message.getRawContent();
		else
			return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getInitialRemoteAddr() {
		return transactionApplicationData.getInitialRemoteHostAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInitialRemotePort() {
		return transactionApplicationData.getInitialRemotePort();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialTransport() {		
		return transactionApplicationData.getInitialRemoteTransport();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		if(getTransaction() != null) {
			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
				return ((SIPTransaction)getTransaction()).getPeerPacketSourceAddress().getHostAddress();
			} else {
				return ((SIPTransaction)getTransaction()).getPeerAddress();
			}
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRemotePort()
	 */
	public int getRemotePort() {
		if(getTransaction() != null) {
			if(((SIPTransaction)getTransaction()).getPeerPacketSourceAddress() != null) {
				return ((SIPTransaction)getTransaction()).getPeerPacketSourcePort();
			} else {
				return ((SIPTransaction)getTransaction()).getPeerPort();
			}
		} else {
			return -1;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getTransport()
	 */
	public String getTransport() {
		if(getTransaction() != null) {
			return ((SIPTransaction)getTransaction()).getTransport();
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getRemoteUser()
	 */
	public String getRemoteUser() {
		// This method returns non-null only if the user is authenticated
		if(this.userPrincipal != null)
			return this.userPrincipal.getName();
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
			MobicentsSipApplicationSession sipApplicationSessionImpl = (MobicentsSipApplicationSession)getApplicationSession(create);
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(sipApplicationSessionImpl.getKey().getId(), currentApplicationName, message, false);
			this.session = ((SipManager)sipApplicationSessionImpl.getSipContext().getManager()).getSipSession(sessionKey, create,
					sipFactoryImpl, sipApplicationSessionImpl);
			this.session.setSessionCreatingTransaction(transaction);
		}
		if(this.session == null) {
			return null;
		} else {
			return this.session.getSession();
		}
	}
	
	/**
	 * Retrieve the sip session implementation
	 * @return the sip session implementation
	 */
	public MobicentsSipSession getSipSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSipSession(MobicentsSipSession session) {
		this.session = session;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getTo()
	 */
	public Address getTo() {
		ToHeader to = (ToHeader) this.message
			.getHeader(getCorrectHeaderName(ToHeader.NAME));
		return new AddressImpl(to.getAddress(), AddressImpl.getParameters((Parameters)to), transaction == null ? true : false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		if(this.userPrincipal == null) {
			if(this.getSipSession() != null) {
				this.userPrincipal = this.getSipSession().getUserPrincipal();
			}
		}
		return this.userPrincipal;
	}
	
	public void setUserPrincipal(Principal principal) {
		this.userPrincipal = principal;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String role) {
		if(this.userPrincipal != null) {
			return ((GenericPrincipal)this.userPrincipal).hasRole(role);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#removeHeader(java.lang.String)
	 */
	public void removeHeader(String name) {
		checkCommitted();
		String hName = getFullHeaderName(name);

		if (isSystemHeader(hName)) {
			throw new IllegalArgumentException("Cant remove system header["
					+ hName + "]");
		}

		String nameToSearch = getCorrectHeaderName(hName);

		this.message.removeHeader(nameToSearch);

	}
	
	public void removeHeaderInternal(String name, boolean bypassSystemHeaderCheck) {
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Removing header under name [" + hName + "]");

		if (!bypassSystemHeaderCheck && isSystemHeader(hName)) {

			logger.error("Cant remove system header [" + hName + "]");

			throw new IllegalArgumentException("Header[" + hName
					+ "] is system header, can't remove it!!!");
		}

		String nameToRemove = getCorrectHeaderName(hName);
		try {
			message.removeHeader(nameToRemove);			
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal args supplied ", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#send()
	 */
	public abstract void send();

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setAcceptLanguage(java.util.Locale)
	 */
	public void setAcceptLanguage(Locale locale) {
		checkCommitted();
		AcceptLanguageHeader alh = headerFactory
				.createAcceptLanguageHeader(locale);

		this.message.setHeader(alh);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setAddressHeader(java.lang.String, javax.servlet.sip.Address)
	 */
	public void setAddressHeader(String name, Address addr) {
		checkCommitted();
		String hName = getFullHeaderName(name);

		if (logger.isDebugEnabled())
			logger.debug("Setting address header [" + name + "] to value ["
					+ addr + "]");

		if (isSystemHeader(hName)) {
			logger.error("Error, cant remove system header [" + hName + "]");
			throw new IllegalArgumentException(
					"Cant set system header, it is maintained by container!!");
		}

//		if (!isAddressTypeHeader(hName)) {
//			logger.error("Error, set header, its not address type header ["
//					+ hName + "]!!");
//			throw new IllegalArgumentException(
//					"This header is not address type header");
//		}
		Header h;
		String headerNameToAdd = getCorrectHeaderName(hName);
		try {
			h = SipFactories.headerFactory.createHeader(headerNameToAdd, addr
					.toString());
			this.message.setHeader(h);
		} catch (ParseException e) {
			logger.error("Parsing problem while setting address header with name "
					+ name + " and address "+ addr, e);			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {
		if (name == null)
			throw new NullPointerException("Attribute name can not be null.");
		this.attributes.put(name, o);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		new String("testEncoding".getBytes(),enc);
		checkCommitted();
		try {			
			this.message.setContentEncoding(SipFactories.headerFactory
					.createContentEncodingHeader(enc));
		} catch (Exception ex) {
			throw new UnsupportedEncodingException(enc);
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContent(java.lang.Object, java.lang.String)
	 */
	public void setContent(Object content, String contentType)
			throws UnsupportedEncodingException {
		checkMessageState();
		checkContentType(contentType);
		checkCommitted();
		
		if(contentType != null && contentType.length() > 0) {
			this.addHeader(ContentTypeHeader.NAME, contentType);
			String charset = this.getCharacterEncoding();
			try {				
				if(content instanceof String  && charset != null) {
					//test for unsupportedencoding exception
					new String("testEncoding".getBytes(charset));
					
					content = new String(((String)content).getBytes());
				}
				ContentTypeHeader contentTypeHeader = (ContentTypeHeader)this.message.getHeader(ContentTypeHeader.NAME);
				this.message.setContent(content, contentTypeHeader);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Parse error reading content type", e);
			}
		}
	}
	
	protected abstract void checkMessageState();

	/**
	 * Check the content type against the list defined by the iana
	 * http://www.iana.org/assignments/media-types/
	 * @param contentType
	 */
	private void checkContentType(String contentType) {
		if(contentType == null) {
			throw new IllegalArgumentException("the content type cannot be null");
		}
		int indexOfSlash = contentType.indexOf("/");
		if(indexOfSlash != -1) { 
			if(!JainSipUtils.ianaAllowedContentTypes.contains(contentType.substring(0, indexOfSlash))) {
				throw new IllegalArgumentException("the given content type " + contentType + " is not allowed");
			}
		} else if(!JainSipUtils.ianaAllowedContentTypes.contains(contentType.toLowerCase())) {
			throw new IllegalArgumentException("the given content type " + contentType + " is not allowed");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContentLanguage(java.util.Locale)
	 */
	public void setContentLanguage(Locale locale) {
		checkCommitted();
		ContentLanguageHeader contentLanguageHeader = 
			SipFactories.headerFactory.createContentLanguageHeader(locale);
		this.message.setContentLanguage(contentLanguageHeader);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContentLength(int)
	 */
	public void setContentLength(int len) {
		checkMessageState();
		checkCommitted();
		try {
			ContentLengthHeader h = headerFactory.createContentLengthHeader(len);
			this.message.setHeader(h);
		} catch (InvalidArgumentException e) {
			throw new IllegalStateException("Impossible to set a content length lower than 0", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setContentType(java.lang.String)
	 */
	public void setContentType(String type) {
		checkContentType(type);
		checkCommitted();
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setExpires(int)
	 */
	public void setExpires(int seconds) {
		try {
			ExpiresHeader expiresHeader = 
				SipFactories.headerFactory.createExpiresHeader(seconds);			
			expiresHeader.setExpires(seconds);
			this.message.setExpires(expiresHeader);
		} catch (Exception e) {
			throw new RuntimeException("Error setting expiration header!", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String name, String value) {
		if(name == null) {
			throw new NullPointerException ("name parameter is null");
		}
		if(value == null) {
			throw new NullPointerException ("value parameter is null");
		}
		if(isSystemHeader(name)) {
			throw new IllegalArgumentException(name + " is a system header !");
		}
		checkCommitted();
		
		try {
			if(JainSipUtils.multiValueHeaders.contains(name)) {
				List<Header> headers = SipFactory.getInstance().createHeaderFactory().createHeaders(name + ":" +
						value);
				for (Header header : headers) {
					this.message.addLast(header);
				}
			} else {
				Header header = SipFactory.getInstance().createHeaderFactory()
					.createHeader(name, value);
				this.message.setHeader(header);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error creating header!", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setHeaderForm(javax.servlet.sip.SipServletMessage.HeaderForm)
	 */
	public void setHeaderForm(HeaderForm form) {

		this.headerForm = form;
		// When form changes to HeaderForm.COMPACT or HeaderForm.LONG - all
		// names must transition, if it is changed to HeaderForm.DEFAULT, no
		// action is performed
		if(form == HeaderForm.DEFAULT)
			return;
		
//		if(form == HeaderForm.COMPACT) {			 
//			for(String fullName : headerFull2CompactNamesMappings.keySet()) {
//				if(message.getHeader(fullName) != null) {
//					try {
						//Handle the case where mutliple headers for the same header name
//						Header header = SipFactories.headerFactory.createHeader(headerCompact2FullNamesMappings.get(fullName), ((SIPHeader)message.getHeader(fullName)).getHeaderValue());
//						message.removeHeader(fullName);
//						message.addHeader(header);
//					} catch (ParseException e) {
//						logger.error("Impossible to parse the header " + fullName + " to its compact form");
//					}
//				}
//			}
//		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#setParameterableHeader(java.lang.String, javax.servlet.sip.Parameterable)
	 */
	public void setParameterableHeader(String name, Parameterable param) {
		checkCommitted();
		if(isSystemHeader(name)) {
			throw new IllegalArgumentException(name + " is a system header !");
		}
		try {
			this.message.setHeader(SipFactories.headerFactory.createHeader(name, param.toString()));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Impossible to set this parameterable header", e);
		}
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

		return JainSipUtils.addressHeadersNames.contains(getFullHeaderName(headerName));

	}

	/**
	 * This method tries to resolve header name - meaning if it is compact - it
	 * returns full name, if its not, it returns passed value.
	 * 
	 * @param headerName
	 * @return
	 */
	protected static String getFullHeaderName(String headerName) {

		String fullName = null;
		if (JainSipUtils.headerCompact2FullNamesMappings.containsKey(headerName)) {
			fullName = JainSipUtils.headerCompact2FullNamesMappings.get(headerName);
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
		if (JainSipUtils.headerCompact2FullNamesMappings.containsKey(headerName)) {
			compactName = JainSipUtils.headerCompact2FullNamesMappings.get(headerName);
		} else {
			// This can be null if there is no mapping!!!
			compactName = JainSipUtils.headerFull2CompactNamesMappings.get(headerName);
		}
		if (logger.isDebugEnabled())
			logger.debug("Fetching compact header name for [" + headerName
					+ "] returning [" + compactName + "]");

		return compactName;

	}

	public String getCorrectHeaderName(String name) {
		return getCorrectHeaderName(name, this.headerForm);

	}

	protected static String getCorrectHeaderName(String name, HeaderForm form) {

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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// FIXME
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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
		if (this.dialog != null) return dialog;
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

	public void setTransport(String transport) {
		this.transport = transport;
	}

	protected static Parameterable createParameterable(Header header, String hName)
			throws ServletParseException {
		String whole = header.toString();
		if (logger.isDebugEnabled())
			logger.debug("Creating parametrable for [" + hName + "] from ["
					+ whole + "]");
		// Remove name
		String stringHeader = whole.substring(whole.indexOf(":") + 1).trim();
//		if (!stringHeader.contains("<") || !stringHeader.contains(">")
//				|| !isParameterable(getFullHeaderName(hName))) {
		
		Map<String, String> paramMap = new HashMap<String, String>();
		String value = stringHeader;
		
		if(stringHeader.trim().indexOf("<") == 0) {
			stringHeader = stringHeader.substring(1);
			String[] split = stringHeader.split(">");
			value = split[0];			
			
			if (split.length > 1 && split[1].contains(";")) {
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
		} else {
			if (value.length() > 1 && value.contains(";")) {
				// repleace first ";" with ""
				String parameters = value.substring(value.indexOf(";") + 1);
				value = value.substring(0, value.indexOf(";"));				
				String[] split = parameters.split(";");
	
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
		}		

		boolean isNotModifiable = JainSipUtils.systemHeaders.contains(header.getName());
		ParameterableHeaderImpl parameterable = new ParameterableHeaderImpl(
				header, value, paramMap, isNotModifiable);
		return parameterable;
	}

	public static boolean isParameterable(String header) {
		if(JainSipUtils.parameterableHeadersNames.contains(header)) {
			return true;
		}
		return false;
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
	

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getLocalAddr()
	 */
	public String getLocalAddr() {
		final SIPTransaction sipTransaction = (SIPTransaction)getTransaction();
		if(sipTransaction != null) {
			return sipTransaction.getHostPort().getHost().getIpAddress();
		} else {
			final String transport = JainSipUtils.findTransport(message);
			final ExtendedListeningPoint listeningPoint = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);		
			return listeningPoint.getHost(true);
		}		
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletMessage#getLocalPort()
	 */
	public int getLocalPort() {
		final SIPTransaction sipTransaction = (SIPTransaction)getTransaction();
		if(sipTransaction != null) {
			return sipTransaction.getPort();
		} else {
			final String transport = JainSipUtils.findTransport(message);
			final ExtendedListeningPoint listeningPoint = sipFactoryImpl.getSipNetworkInterfaceManager().findMatchingListeningPoint(transport, false);		
			return listeningPoint.getPort();
		}
	}
}
