package org.mobicents.servlet.sip.message;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.sip.address.AddressFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;

public class SipFactoryImpl implements SipFactory {
	private static final Log logger = LogFactory.getLog(SipFactoryImpl.class
			.getCanonicalName());


	private static AddressFactory addressFactory = SipFactories.addressFactory;
	

	public Address createAddress(String addr) throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

	public Address createAddress(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public Address createAddress(URI uri, String displayName) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipApplicationSession createApplicationSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public Parameterable createParameterable(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, Address from, Address to) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, String from, String to) throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

	public SipServletRequest createRequest(SipApplicationSession appSession,
			String method, URI from, URI to) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipServletRequest createRequest(SipServletRequest origRequest,
			boolean sameCallId) {
		// TODO Auto-generated method stub
		return null;
	}

	public SipURI createSipURI(String user, String host) {
		// TODO Auto-generated method stub
		return null;
	}

	public URI createURI(String uri) throws ServletParseException {
		// TODO Auto-generated method stub
		return null;
	}

}
