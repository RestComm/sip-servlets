package org.mobicents.servlet.sip.address;

import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.From;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;

/**
 * Implementation of Servlet Address specification.
 * 
 * @author mranga
 * @author bartek
 * 
 */
public class AddressImpl  extends ParameterableImpl implements Address{

	private static Log logger = LogFactory.getLog(AddressImpl.class
			.getCanonicalName());

	
	private javax.sip.address.Address address;

	private static AddressFactory addressFactory = SipFactories.addressFactory;

	private static HeaderFactory headerFactory = SipFactories.headerFactory;

	

	private SipURI getUriAsSipUri() {

		if (this.address.getURI() instanceof SipURI) {
			return (SipURI) this.address.getURI();
			
		} else {
			throw new UnsupportedOperationException("Cannot return as SipURI");
		}

	}
	
	public javax.sip.address.Address getAddress() {
		return address;
	}
	
	
	public AddressImpl() {
		
		
	}
	
	public AddressImpl (javax.sip.address.Address address) {
		this.address = address;
		
	}

	/**
	 * Creates instance of Address object. This object is valid representation
	 * of SIP AddressHeader, like To, From, Contact. Input to create this object
	 * HAS to be string that conforms to sip(s) uri schema. Methods exposed by
	 * this object act on header part of address. Parameter methods modify
	 * header parameters, not uri parameters.
	 * 
	 * @param addressString -
	 *            string representing header, it has to conform to rfc 3261
	 *            sip(s) uri scheme, see chapter 19 and specification of To/From
	 *            header. <br>
	 *            Passed values must look like followin: <b>
	 *            <ul>
	 *            <li>&ltsip:ala@ma.kota&gt</li>
	 *            <li>sip:ala@ma.kota</li>
	 *            <li>&ltsip:ala@ma.kota;somePName=somePValue;someP2Name=V2&gt;headerParam1=Value1;headerParam2=V2</li>
	 *            <li>&ltsip:ala@ma.kota&gt;headerParam1=Value1;headerParam2=V2</li>
	 *            <li>"Kazik Staszewski"
	 *            &ltsip:ala@ma.kota;somePName=somePValue;someP2Name=V2&gt;headerParam1=Value1;headerParam2=V2</li>
	 *            <li>"Kazik Staszewski"
	 *            &ltsip:ala@ma.kota&gt;headerParam1=Value1;headerParam2=V2</li>
	 *            </ul>
	 *            </b>
	 * @throws ParseException
	 */
	

	public AddressImpl(AddressParametersHeader header) throws ParseException {
		this.address = header.getAddress();
		super.parameters = header.getParameters();

	}

	public String getDisplayName() {

		return address.getDisplayName();
	}

	public int getExpires() {

		javax.sip.address.URI uri = this.address.getURI();
		if (uri instanceof SipURI) {
			SipURI sipUri = (SipURI) uri;
			return sipUri.getParameter("expires") == null ? -1 : Integer
					.parseInt(sipUri.getParameter("expires"));
		} else
			throw new UnsupportedOperationException("Illegal operation");

	}

	public float getQ() {
		SipURI sipUri = this.getUriAsSipUri();
		return sipUri.getParameter("q") == null ? (float) -1.0 : Float
				.parseFloat(sipUri.getParameter("q"));
	}

	public URI getURI() {
		if (getAddress().getURI() instanceof javax.sip.address.SipURI)
			return new SipURIImpl((javax.sip.address.SipURI) getAddress().getURI());
		else if (getAddress().getURI() instanceof javax.sip.address.TelURL)
			return new TelURLImpl((javax.sip.address.TelURL) getAddress().getURI());
		else
			throw new RuntimeException("unsupported operation - unknown scheme");
	}

	public boolean isWildcard() {

		return getAddress().isWildcard();
	}

	public void setDisplayName(String name) {
		try {
			getAddress().setDisplayName(name);
		} catch (ParseException e) {
			throw new IllegalArgumentException("illegal name ", e);
		}

	}

	public void setExpires(int seconds) throws IllegalArgumentException {
		javax.sip.address.URI uri = this.getAddress().getURI();

		if (uri instanceof SipURI) {
			SipURI sipUri = (SipURI) uri;
			try {
				sipUri.setParameter("expires", Integer.toString(seconds));
			} catch (ParseException e) {
				throw new IllegalArgumentException("Problem setting parameter",
						e);
			}
		} else
			throw new IllegalArgumentException(
					"Can only set parameter for Sip URI");
	}

	public void setQ(float q) {
		try {
			SipURI sipUri = getUriAsSipUri();
			sipUri.setParameter("q", Float.toString(q));
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad parameter", ex);
		}
	}

	public void setURI(URI uri) {
		this.getAddress().setURI(((URIImpl) uri).uri);

	}

	public Object clone() {

		AddressImpl retval = new AddressImpl();
		retval.parameters = (NameValueList) this.parameters.clone();
		return retval;
		
	}

	public String toString() {
		// Returns the value of this address as a String. The resulting string
		// must be a valid value of a SIP From or To header.
		// To/From are parametrable.
		StringBuffer retval = new StringBuffer();
		retval.append(address.toString());
		if ( parameters.size() != 0 ) retval.append(";").append(parameters.toString());
		return retval.toString();

	}

	
	public String getValue() {
		return toString();
	}

	public void removeParameter(String name) {
		this.parameters.remove(name);

	}

	public void setParameter(String name, String value) {
		this.parameters.put(name, new NameValue(name,value));

	}

	public void setValue(String value) {

		try {

			FromHeader fromHeader = (FromHeader) headerFactory.createHeader(
					FromHeader.NAME, value);
			this.address = fromHeader.getAddress();
			this.parameters = ((From) fromHeader).getParameters();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Illegal argument", ex);
		}
	}

	public void setAddress(javax.sip.address.Address address) {
		this.address = address;
	}

}
