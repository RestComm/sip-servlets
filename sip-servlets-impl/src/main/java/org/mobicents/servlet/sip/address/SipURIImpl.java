package org.mobicents.servlet.sip.address;

import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.sip.SipURI;
import javax.sip.InvalidArgumentException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;

/**
 * Implementation of the SipURI interface. This is just a thin wrapper on the
 * JSIP URI interface.
 * 
 * @author M. Ranganathan
 * 
 */
public class SipURIImpl extends URIImpl implements SipURI {

	private static Log logger = LogFactory.getLog(SipFactories.class.getCanonicalName());

	public SipURIImpl(javax.sip.address.SipURI sipUri) {
		super(sipUri);
	}

	private javax.sip.address.SipURI getSipURI() {
		return (javax.sip.address.SipURI) super.uri;

	}

	public String getHeader(String name) {

		return ((SipURI) super.uri).getHeader(name);
	}

	@SuppressWarnings("unchecked")
	public Iterator<String> getHeaderNames() {

		return getSipURI().getHeaderNames();
	}

	public String getHost() {
		return getSipURI().getHost();
	}

	public boolean getLrParam() {

		return getSipURI().hasLrParam();
	}

	public int getPort() {
		return getSipURI().getPort();
	}

	public int getTTLParam() {

		return getSipURI().getTTLParam();
	}

	public String getTransportParam() {

		return getSipURI().getTransportParam();
	}

	public String getUser() {
		return getSipURI().getUser();
	}

	public String getUserParam() {
		return getSipURI().getUserParam();
	}

	public String getUserPassword() {
		return getSipURI().getUserPassword();
	}

	public boolean isSecure() {

		return getSipURI().isSecure();
	}

	public void setHeader(String name, String value) {
		try {
			getSipURI().setHeader(name, value);
		} catch (ParseException ex) {
			logger.error("parse exception occured", ex);
			throw new IllegalArgumentException("Bad arg!",ex);
		}
	}

	public void setHost(String host) {
		try {
			getSipURI().setHost(host);
		} catch (ParseException e) {
			logger.error("parse exception occured", e);
			throw new IllegalArgumentException("Bad arg!",e);
		}

	}

	public void setLrParam(boolean flag) {
		if (flag)
			getSipURI().setLrParam();
		else
			getSipURI().removeParameter("lr");

	}

	public void setMAddrParam(String maddr) {
		try {
			getSipURI().setMAddrParam(maddr);
		} catch (ParseException e) {
			logger.error("parse exception occured", e);
		}

	}

	public void setMethodParam(String method) {
		try {
			getSipURI().setMethodParam(method);
		} catch (ParseException e) {
			logger.error("parse exception occured", e);
		}

	}

	public void setPort(int port) {
		getSipURI().setPort(port);
	}

	public void setSecure(boolean b) {
		getSipURI().setSecure(b);
	}

	public void setTTLParam(int ttl) {
		try {
			getSipURI().setTTLParam(ttl);
		} catch (InvalidArgumentException e) {
			logger.error("invalid argument", e);
		}
	}

	public void setTransportParam(String transport) {
		try {
			getSipURI().setTransportParam(transport);
		} catch (ParseException e) {
			logger.error("error setting transport ", e);
		}
	}

	public void setUser(String user) {
		try {
			getSipURI().setUser(user);
		} catch (ParseException e) {
			logger.error("error setting parameter ", e);
		}
	}

	public void setUserParam(String user) {
		try {
			getSipURI().setUserParam(user);
		} catch (ParseException e) {
			logger.error("error setting parameter ", e);
		}
	}

	public void setUserPassword(String password) {
		try {
			getSipURI().setUserPassword(password);
		} catch (ParseException e) {
			logger.error("error setting parameter ", e);
			throw new IllegalArgumentException("Bad arg", e	);
		
		}
	}

	public String getMAddrParam() {
		return getSipURI().getMAddrParam();
	}

	public String getMethodParam() {

		return getSipURI().getMethodParam();
	}

	public String toString() {
		// URI scheme ->
		// sip:alice@atlanta.com?subject=project%20x&priority=urgent
		// But this is madness, when creating uri from uri or address fom uri we
		// would have uri escaped multpile times...
		return getSipURI().toString();
	}


	public void setValue(String value) {
		try {
			this.uri = SipFactories.addressFactory.createURI(value);
			gov.nist.javax.sip.address.SipUri sipUri = (gov.nist.javax.sip.address.SipUri) this.uri;
			super.setParameters(sipUri.getParameters());
		} catch (ParseException ex) {
			logger.error("Bad input arg", ex);
			throw new IllegalArgumentException("Bad input arg", ex);
		}
	}

	public String getValue() {
		return this.uri.toString();
	}

}
