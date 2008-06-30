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

import gov.nist.core.NameValueList;
import gov.nist.javax.sip.header.AddressParametersHeader;
import gov.nist.javax.sip.header.From;

import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;
import javax.sip.address.SipURI;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.Parameters;

import org.mobicents.servlet.sip.SipFactories;

/**
 * Implementation of Servlet Address specification.
 * 
 * @author mranga
 * @author bartek
 * 
 */
public class AddressImpl  extends ParameterableImpl implements Address {

//	private static Log logger = LogFactory.getLog(AddressImpl.class
//			.getCanonicalName());
	
	private javax.sip.address.Address address;	

	private static HeaderFactory headerFactory = SipFactories.headerFactory;
	
	public javax.sip.address.Address getAddress() {
		return address;
	}
	
	public AddressImpl() {}
	
	@SuppressWarnings("unchecked")
	public AddressImpl (javax.sip.address.Address address) {
		this.address = address;				
		Parameters uri = (Parameters) this.address.getURI();
		Iterator<String> parameterNames = uri.getParameterNames();
		while (parameterNames.hasNext()) {
			String parameterName = (String) parameterNames.next();
			parameters.set(parameterName, uri.getParameter(parameterName));		
		}
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
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#getDisplayName()
	 */
	public String getDisplayName() {
		return address.getDisplayName();
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#getExpires()
	 */
	public int getExpires() {		
		return this.parameters.get("expires") == null ? -1 : 
			Integer.parseInt(this.parameters.get("expires").getValue());		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#getQ()
	 */
	public float getQ() {
		return this.parameters.get("expires") == null ? (float) -1.0  : 
			Float.parseFloat(this.parameters.get("expires").getValue());
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#getURI()
	 */
	public URI getURI() {
		if (getAddress().getURI() instanceof javax.sip.address.SipURI)
			return new SipURIImpl((javax.sip.address.SipURI) getAddress().getURI());
		else if (getAddress().getURI() instanceof javax.sip.address.TelURL)
			return new TelURLImpl((javax.sip.address.TelURL) getAddress().getURI());
		else
			throw new RuntimeException("unsupported operation - unknown scheme");
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#isWildcard()
	 */
	public boolean isWildcard() {
		return getAddress().isWildcard();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#setDisplayName(java.lang.String)
	 */
	public void setDisplayName(String name) {
		try {
			getAddress().setDisplayName(name);
		} catch (ParseException e) {
			throw new IllegalArgumentException("illegal name ", e);
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#setExpires(int)
	 */
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
		} else {
			throw new IllegalArgumentException(
					"Can only set parameter for Sip URI");
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#setQ(float)
	 */
	public void setQ(float q) {
		try {
			Parameters uri = (Parameters) this.address.getURI();
			uri.setParameter("q", Float.toString(q));
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad parameter", ex);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#setURI(javax.servlet.sip.URI)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getValue()
	 */
	public String getValue() {
		return toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#setValue(java.lang.String)
	 */
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
	
	@Override
	public void setParameter(String name, String value) {		
		super.setParameter(name, value);
		Parameters uri = (Parameters) this.address.getURI();
		try {
			uri.setParameter(name, value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Problem setting parameter",e);
		}
	}
}
