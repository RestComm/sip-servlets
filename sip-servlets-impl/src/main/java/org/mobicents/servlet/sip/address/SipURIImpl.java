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
package org.mobicents.servlet.sip.address;

import gov.nist.javax.sip.address.SipURIExt;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.SipURI;
import javax.sip.InvalidArgumentException;
import javax.sip.header.Parameters;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipFactories;

/**
 * Implementation of the SipURI interface. This is just a thin wrapper on the
 * JSIP URI interface.
 * 
 * @author M. Ranganathan
 * 
 */
public class SipURIImpl extends URIImpl implements SipURI, Serializable {

	private static final String LR_PARAM = "lr";
	private static final String MADDR = "maddr";
	protected static final String METHOD = "method";
	protected static final String TTL = "ttl";
	protected static final String USER = "user";
	protected static final String TRANSPORT = "transport";
	private static Logger logger = Logger.getLogger(SipURIImpl.class.getCanonicalName());

	public SipURIImpl(javax.sip.address.SipURI sipUri) {
		super(sipUri);
	}

	public javax.sip.address.SipURI getSipURI() {
		return (javax.sip.address.SipURI) super.uri;

	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {
		if(name == null) {
			throw new NullPointerException("header name is null");
		}
		try {
			return RFC2396UrlDecoder.decode(((javax.sip.address.SipURI) super.uri).getHeader(name));
		} catch (NullPointerException e) {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getHeaderNames()
	 */
	@SuppressWarnings("unchecked")
	public Iterator<String> getHeaderNames() {
		return getSipURI().getHeaderNames();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getHost()
	 */
	public String getHost() {
		return getSipURI().getHost();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getLrParam()
	 */
	public boolean getLrParam() {
		return getSipURI().hasLrParam();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getPort()
	 */
	public int getPort() {
		return getSipURI().getPort();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getTTLParam()
	 */
	public int getTTLParam() {
		return getSipURI().getTTLParam();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getTransportParam()
	 */
	public String getTransportParam() {
		return getSipURI().getTransportParam();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getUser()
	 */
	public String getUser() {
		return RFC2396UrlDecoder.decode(getSipURI().getUser());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getUserParam()
	 */
	public String getUserParam() {
		return RFC2396UrlDecoder.decode(getSipURI().getUserParam());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getUserPassword()
	 */
	public String getUserPassword() {
		if(getSipURI().getUserPassword() == null) return null;
		return RFC2396UrlDecoder.decode(getSipURI().getUserPassword());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#isSecure()
	 */
	public boolean isSecure() {
		return getSipURI().isSecure();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String name, String value) {
		try {
			getSipURI().setHeader(name, value);
		} catch (ParseException ex) {
			logger.error("parse exception occured", ex);
			throw new IllegalArgumentException("Bad arg!",ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setHost(java.lang.String)
	 */
	public void setHost(String host) {
		try {
			getSipURI().setHost(host);
		} catch (ParseException e) {
			logger.error("parse exception occured", e);
			throw new IllegalArgumentException("Bad arg!",e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setLrParam(boolean)
	 */
	public void setLrParam(boolean flag) {
		if (flag) {
			getSipURI().setLrParam();
			super.parameters.put(LR_PARAM, "");
		} else {
			getSipURI().removeParameter(LR_PARAM);
			super.parameters.remove(LR_PARAM);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setMAddrParam(java.lang.String)
	 */
	public void setMAddrParam(String maddr) {
		try {
			getSipURI().setMAddrParam(maddr);
			super.parameters.put(MADDR, maddr);
		} catch (ParseException e) {
			logger.error("parse exception occured", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setMethodParam(java.lang.String)
	 */
	public void setMethodParam(String method) {
		try {
			getSipURI().setMethodParam(method);
			super.parameters.put(METHOD, method);
		} catch (ParseException e) {
			logger.error("parse exception occured", e);
			throw new IllegalArgumentException("Bad arg " + method, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setPort(int)
	 */
	public void setPort(int port) {
		getSipURI().setPort(port);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setSecure(boolean)
	 */
	public void setSecure(boolean b) {
		getSipURI().setSecure(b);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setTTLParam(int)
	 */
	public void setTTLParam(int ttl) {
		try {
			getSipURI().setTTLParam(ttl);
			super.parameters.put(TTL, "" + ttl);
		} catch (InvalidArgumentException e) {
			logger.error("invalid argument", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setTransportParam(java.lang.String)
	 */
	public void setTransportParam(String transport) {
		try {
			getSipURI().setParameter(TRANSPORT, transport);
			super.parameters.put(TRANSPORT, transport);
		} catch (ParseException e) {
			logger.error("error setting transport ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setUser(java.lang.String)
	 */
	public void setUser(String user) {
		try {
			getSipURI().setUser(user);			
		} catch (ParseException e) {
			logger.error("error setting parameter ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setUserParam(java.lang.String)
	 */
	public void setUserParam(String user) {
		try {
			getSipURI().setUserParam(user);
			super.parameters.put(USER, user);
		} catch (ParseException e) {
			logger.error("error setting parameter ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#setUserPassword(java.lang.String)
	 */
	public void setUserPassword(String password) {
		try {
			getSipURI().setUserPassword(password);
		} catch (ParseException e) {
			logger.error("error setting parameter ", e);
			throw new IllegalArgumentException("Bad arg", e	);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getMAddrParam()
	 */
	public String getMAddrParam() {
		return getSipURI().getMAddrParam();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#getMethodParam()
	 */
	public String getMethodParam() {
		return RFC2396UrlDecoder.decode(getSipURI().getMethodParam());
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.URIImpl#toString()
	 */
	public String toString() {
		return RFC2396UrlDecoder.decode(getSipURI().toString());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		try {
			this.uri = SipFactories.addressFactory.createURI(value);
			if(this.uri instanceof Parameters) {
				super.setParameters(AddressImpl.getParameters((Parameters)uri));
			} else {
				super.setParameters(new ConcurrentHashMap<String, String>());
			}
		} catch (ParseException ex) {
			logger.error("Bad input arg", ex);
			throw new IllegalArgumentException("Bad input arg", ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getValue()
	 */
	public String getValue() {
		return RFC2396UrlDecoder.decode(this.uri.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.URIImpl#clone()
	 */
	@Override
	public SipURI clone() {
		return new SipURIImpl((javax.sip.address.SipURI) this.getSipURI().clone());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipURI#removeHeader(java.lang.String)
	 */
	public void removeHeader(String name) {
		//FIXME it's using jain sip RI implementation classes, it should not
		((SipURIExt)getSipURI()).removeHeader(name);
	}

	@Override
	public void setParameter(String name, String value) {
		//Special case to pass Addressing spec test from 289 TCK
		if(LR_PARAM.equals(name)) {
			if(value == null || value.length()<1) {
				this.setLrParam(true);
				return;
			}
		}
		super.setParameter(name, value);
		try {
			((Parameters)getSipURI()).setParameter(name, value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Problem setting parameter",e);
		}
	}
	
	@Override
	public void removeParameter(String name) {
		super.removeParameter(name);
		((Parameters)getSipURI()).removeParameter(name);
	}
}
