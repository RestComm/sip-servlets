/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.message;

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
	private ThreadLocal<HttpSession> threadLocalHttpSession;
	
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
		SipApplicationSessionImpl sipApplicationSessionImpl = 
			(SipApplicationSessionImpl)sipFactoryImpl.createApplicationSession(sipContext);
		associateHttpSession(sipApplicationSessionImpl);
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
			SipApplicationSessionImpl sipApplicationSessionImpl) {
		
		HttpSession httpSession = threadLocalHttpSession.get();
		if(httpSession != null) {
			sipApplicationSessionImpl.addHttpSession(httpSession);
		}
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
//				((SipApplicationSessionImpl)sipSessionImpl.getApplicationSession()).setSipContext(sipContext);
			} catch (ServletException se) {
				//should never happen
				logger.error("Impossible to set the default handler on the newly created request "+ request.toString(),se);
			} 
		}
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSessionByAppName(java.lang.String)
	 */
	public SipApplicationSession createApplicationSessionByAppName(
			String sipAppName) {
		SipApplicationSessionImpl sipApplicationSessionImpl = (SipApplicationSessionImpl)
			sipFactoryImpl.createApplicationSessionByAppName(sipAppName);
		associateHttpSession(sipApplicationSessionImpl);
		return sipApplicationSessionImpl;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createApplicationSessionByKey(java.lang.String)
	 */
	public SipApplicationSession createApplicationSessionByKey(
			String sipApplicationKey) {
		SipApplicationSessionImpl sipApplicationSessionImpl = (SipApplicationSessionImpl)
			sipFactoryImpl.createApplicationSessionByKey(sipApplicationKey);
		associateHttpSession(sipApplicationSessionImpl);
		return sipApplicationSessionImpl;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipFactory#createAuthInfo()
	 */
	public AuthInfo createAuthInfo() {		
		return sipFactoryImpl.createAuthInfo();
	}
}
