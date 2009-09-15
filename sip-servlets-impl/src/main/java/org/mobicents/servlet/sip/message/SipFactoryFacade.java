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

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.ConvergedSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Facade object which masks the internal <code>SipFactoryImpl</code>
 * object from the web application.
 *
 * @author Jean Deruelle
 *
 */
public class SipFactoryFacade implements SipFactory, Serializable {
	
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SipFactoryFacade.class
			.getName());
	
	private SipFactoryImpl sipFactoryImpl;
	private transient SipContext sipContext;
	private transient ThreadLocal<HttpSession> threadLocalHttpSession;
	
	public SipFactoryFacade(SipFactoryImpl sipFactoryImpl, SipContext sipContext) {
		this.sipFactoryImpl = sipFactoryImpl;
		this.sipContext = sipContext;
		threadLocalHttpSession = new ThreadLocal<HttpSession>();
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
		MobicentsSipApplicationSession sipApplicationSessionImpl = null; 
		HttpSession httpSession = threadLocalHttpSession.get();
		// make sure we don't create a new sip app session if the http session has already one associated
		if(httpSession != null) {
			ConvergedSession convergedSession = (ConvergedSession) httpSession;
			sipApplicationSessionImpl = convergedSession.getApplicationSession(false);
		}
		if(sipApplicationSessionImpl == null) {
			sipApplicationSessionImpl =
				(MobicentsSipApplicationSession)sipFactoryImpl.createApplicationSessionByAppName(sipContext.getApplicationName());
			associateHttpSession(sipApplicationSessionImpl);
		}
		return sipApplicationSessionImpl;
	}

	/**
	 * A servlet wants to create a sip application
	 * session, we can retrieve its http session in the thread local data 
	 * if it has a sip http session (sip servlet only won't have any http sessions)  
	 * and associate it with the sip app session.
	 * @param sipApplicationSessionImpl the app session to assocaiate the http session with
	 */
	private void associateHttpSession(
			MobicentsSipApplicationSession sipApplicationSessionImpl) {
		
		HttpSession httpSession = threadLocalHttpSession.get();
		if(httpSession != null) {
			sipApplicationSessionImpl.addHttpSession(httpSession);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createParameterable(java.lang.String)
	 */
	public Parameterable createParameterable(String s) throws ServletParseException {
		return sipFactoryImpl.createParameterable(s);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession, java.lang.String, javax.servlet.sip.Address, javax.servlet.sip.Address)
	 */
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, Address from, Address to) {
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(appSession, method, from, to, ((MobicentsSipApplicationSession)appSession).getCurrentRequestHandler());
		return sipServletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession, java.lang.String, java.lang.String, java.lang.String)
	 */
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, String from, String to) throws ServletParseException {
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(appSession, method, from, to, ((MobicentsSipApplicationSession)appSession).getCurrentRequestHandler());
		return sipServletRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createRequest(javax.servlet.sip.SipApplicationSession, java.lang.String, javax.servlet.sip.URI, javax.servlet.sip.URI)
	 */
	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, URI from, URI to) {
		SipServletRequest sipServletRequest = sipFactoryImpl.createRequest(appSession, method, from, to, ((MobicentsSipApplicationSession)appSession).getCurrentRequestHandler());
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

	/**
	 * set the handler for this request if none already exists.
	 * The handler will be the main servlet of the sip context 
	 * @param request the session of this request will have its handler set.
	 */
	private void checkHandler(SipServletRequest request) {
		MobicentsSipSession sipSessionImpl = (MobicentsSipSession)request.getSession();
		if(sipSessionImpl.getHandler() == null) {
			try {
				sipSessionImpl.setHandler(sipContext.getMainServlet());
//				((SipApplicationSessionImpl)sipSessionImpl.getApplicationSession()).setSipContext(sipContext);
			} catch (ServletException se) {
				//should never happen
				logger.error("Impossible to set the default handler on the newly created request "+ request.toString(),se);
			} 
		}
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
	 * Store the http session in the sip Factory's thread local 
	 * @param httpSession the http session to store for later retrieval
	 */
	public void storeHttpSession(HttpSession httpSession) {
		threadLocalHttpSession.set(httpSession);
	}
	
	/**
	 * Retrieve the http session previously stored in the sip Factory's thread local
	 * @return the http session previously stored in the sip Factory's thread local
	 */
	public HttpSession retrieveHttpSession() {
		return threadLocalHttpSession.get();
	}

	/**
	 * {@inheritDoc}
	 */
	public SipApplicationSession createApplicationSessionByKey(
			String sipApplicationKey) {
		MobicentsSipApplicationSession sipApplicationSessionImpl = null;
		// make sure we don't create a new sip app session if the http session has already one associated
		HttpSession httpSession = threadLocalHttpSession.get();
		if(httpSession != null) {
			ConvergedSession convergedSession = (ConvergedSession) httpSession;
			sipApplicationSessionImpl = convergedSession.getApplicationSession(false);
		}
		if(sipApplicationSessionImpl == null) {
			sipApplicationSessionImpl = (MobicentsSipApplicationSession)
				sipFactoryImpl.createApplicationSessionByKey(sipApplicationKey);
			associateHttpSession(sipApplicationSessionImpl);
		}
		return sipApplicationSessionImpl;
	}

	/**
	 * {@inheritDoc}
	 */
	public AuthInfo createAuthInfo() {		
		return sipFactoryImpl.createAuthInfo();
	}

	/**
	 * {@inheritDoc}
	 */
	public SipApplicationSession createApplicationSessionByAppName(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
