package org.mobicents.servlet.sip.address;

import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class URIImpl  extends ParameterableImpl implements URI {

	
	private static Log logger = LogFactory.getLog(URIImpl.class
			.getCanonicalName());
	
	javax.sip.address.URI uri;
	
	public URIImpl ( javax.sip.address.URI uri) {
		
		this.uri = uri;
	}
	
	public URIImpl( ) {
		super();
	}
	
	public javax.sip.address.URI getURI() {
		return this.uri;
	}
	
	public URIImpl ( javax.sip.address.TelURL telUrl) {
		this.uri = telUrl;
		super.setParameters( ((gov.nist.javax.sip.address.TelURLImpl)telUrl).getParameters());
	}
	public URIImpl (javax.sip.address.SipURI sipUri) {
		this.uri = sipUri;
		super.setParameters (((gov.nist.javax.sip.address.SipUri)sipUri).getParameters());
		
		
	}

	public String getScheme() {
	
		return uri.getScheme();
	}

	public boolean isSipURI() {
	
		return uri.isSipURI();
	}
	
	
	public String toString()
	{
		return this.uri.toString();
	}
	
	public abstract Object clone();
	
}
