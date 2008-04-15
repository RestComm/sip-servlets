/**
 * 
 */
package org.mobicents.servlet.sip.message;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Facade object which masks the internal <code>SipFactoryImpl</code>
 * object from the web application.
 *
 * @author Jean Deruelle
 *
 */
public class SipFactoryFacade implements SipFactory {
	private static final Log logger = LogFactory.getLog(SipFactoryFacade.class
			.getName());
	
	private SipFactoryImpl sipFactoryImpl;
	private SipContext sipContext;
	
	public SipFactoryFacade(SipFactoryImpl sipFactoryImpl, SipContext sipContext) {
		this.sipFactoryImpl = sipFactoryImpl;
		this.sipContext = sipContext;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createAddress(java.lang.String)
	 */
	public Address createAddress(String sipAddress) throws ServletParseException {
		return sipFactoryImpl.createAddress(sipAddress);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI)
	 */
	public Address createAddress(URI uri) {
		return sipFactoryImpl.createAddress(uri);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createAddress(javax.servlet.sip.URI, java.lang.String)
	 */
	public Address createAddress(URI uri, String displayName) {
		return sipFactoryImpl.createAddress(uri, displayName);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSession()
	 */
	public SipApplicationSession createApplicationSession() {
		SipApplicationSessionImpl sipApplicationSessionImpl = 
			(SipApplicationSessionImpl)sipFactoryImpl.createApplicationSession(sipContext);		
		return sipApplicationSessionImpl;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createParameterable(java.lang.String)
	 */
	public Parameterable createParameterable(String s) {
		return sipFactoryImpl.createParameterable(s);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession, java.lang.String, javax.servlet.sip.Address, javax.servlet.sip.Address)
	 */
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, Address from, Address to) {
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(appSession, method, from, to);
		checkHandler(sipServletRequest);		
		return sipServletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession, java.lang.String, java.lang.String, java.lang.String)
	 */
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, String from, String to) throws ServletParseException {
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(appSession, method, from, to);
		checkHandler(sipServletRequest);		
		return sipServletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession, java.lang.String, javax.servlet.sip.URI, javax.servlet.sip.URI)
	 */
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, URI from, URI to) {
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(appSession, method, from, to);
		checkHandler(sipServletRequest);		
		return sipServletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipServletRequest, boolean)
	 */
	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean sameCallId) {		
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(origRequest, sameCallId);
		checkHandler(sipServletRequest);		
		return sipServletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createSipURI(java.lang.String, java.lang.String)
	 */
	public SipURI createSipURI(String user, String host) {
		return sipFactoryImpl.createSipURI(user, host);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createURI(java.lang.String)
	 */
	public URI createURI(String uri) throws ServletParseException {
		return sipFactoryImpl.createURI(uri);
	}

	/**
	 * set the handler for this request if none already exists.
	 * The handler will be the main servlet of the sip context 
	 * @param request the session of this request will have its handler set.
	 */
	private void checkHandler(SipServletRequest request) {
		SipSessionImpl sipSessionImpl = (SipSessionImpl)request.getSession();
		if(sipSessionImpl.getHandler() == null) {
			try {
				sipSessionImpl.setHandler(sipContext.getMainServlet());
				((SipApplicationSessionImpl)sipSessionImpl.getApplicationSession()).setSipContext(sipContext);
			} catch (ServletException se) {
				//should never happen
				logger.error("Impossible to set the default handler on the newly created request "+ request.toString(),se);
			} 
		}
	}
}
