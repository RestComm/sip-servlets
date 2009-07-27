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

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderAddress;
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
public class AddressImpl extends ParameterableImpl implements Address, Serializable {
	
//	private static Log logger = LogFactory.getLog(AddressImpl.class
//			.getCanonicalName());
	
	private static final String Q_PARAM_NAME = "q";
	private static final String EXPIRES_PARAM_NAME = "expires";
	private static final String PARAM_SEPARATOR = ";";
	private static final String PARAM_NAME_VALUE_SEPARATOR = "=";
	private javax.sip.address.Address address;	
	
	private static HeaderFactory headerFactory = SipFactories.headerFactory;
	private static AddressFactory addressFactory = SipFactories.addressFactory;
	
	public javax.sip.address.Address getAddress() {
		return address;
	}
	
	public AddressImpl() {}
	
	@SuppressWarnings("unchecked")
	public AddressImpl (javax.sip.address.Address address, Map<String, String> parameters, boolean isModifiable) {
		super();
		super.isModifiable = isModifiable;
		this.address = address;				
		if(parameters != null) {
			this.parameters = parameters;
		}
		// this isn't right : we shouldn't copy the uri params as address params
		// See Issue 732 : http://code.google.com/p/mobicents/issues/detail?id=732
		// duplicate parameters when using sipFactory.createAddress(uri) with a uri having parameters 
//		if(this.address.getURI() instanceof Parameters) {
//			Parameters uri = (Parameters) this.address.getURI();
//			Iterator<String> parameterNames = uri.getParameterNames();
//			while (parameterNames.hasNext()) {
//				String parameterName = (String) parameterNames.next();
//				String value = uri.getParameter(parameterName);
//				this.parameters.put(parameterName.toLowerCase(), value);		
//			}
//		}
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
	public AddressImpl(HeaderAddress header, boolean modifiable) throws ParseException {
		this.address = header.getAddress();
		if(header instanceof Parameters) {
			super.parameters = getParameters((Parameters)header);
		} else {
			super.parameters = new ConcurrentHashMap<String, String>();
		}
		super.isModifiable = modifiable;

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
		String expires = this.getParameter(EXPIRES_PARAM_NAME);
		if(expires != null) { // This is how the TCK expects to parse it. See AddressingServlet in TCK spec tests.
			return Integer.parseInt(expires);
		} else { // I think this is not needed.
			return ((SipURI)address.getURI()).getParameter(EXPIRES_PARAM_NAME) == null ? -1 : 
				Integer.parseInt(((SipURI)address.getURI()).getParameter(EXPIRES_PARAM_NAME));
		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#getQ()
	 */
	public float getQ() {
		String q = this.getParameter(Q_PARAM_NAME);
		if(q != null) { // This is how the TCK expects to parse it. See AddressingServlet in TCK spec tests.
			return Float.parseFloat(q);
		} else { // I think this is not needed.
			return ((SipURI)address.getURI()).getParameter(Q_PARAM_NAME) == null ? (float) -1.0  : 
				Float.parseFloat(((SipURI)address.getURI()).getParameter(Q_PARAM_NAME));
		}
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
		if(!isModifiable) {
			throw new IllegalStateException("this Address is used in a context where it cannot be modified");
		}
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
			/*
			SipURI sipUri = (SipURI) uri;
			try {
				sipUri.setParameter(EXPIRES_PARAM_NAME, Integer.toString(seconds));
			} catch (ParseException e) {
				throw new IllegalArgumentException("Problem setting parameter",
						e);
			}*/
			if(seconds == -1) {
				this.removeParameter(EXPIRES_PARAM_NAME);
				return;
			}
			this.setParameter(EXPIRES_PARAM_NAME, new Integer(seconds).toString());
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
		if(q == -1.0) {
			this.removeParameter(Q_PARAM_NAME);
			return;
		}
		if(q > 1.0 || q < 0) {
			throw new IllegalArgumentException("the new qvalue isn't between 0.0 and 1.0 (inclusive) and isn't -1.0.");
		}
		this.setParameter(Q_PARAM_NAME, new Float(q).toString());
		/*
		try {
			Parameters uri = (Parameters) this.address.getURI();
			uri.setParameter(Q_PARAM_NAME, Float.toString(q));
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad parameter", ex);
		}*/
	}
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Address#setURI(javax.servlet.sip.URI)
	 */
	public void setURI(URI uri) {
		if(!isModifiable) {
			throw new IllegalStateException("this Address is used in a context where it cannot be modified");
		}
		this.getAddress().setURI(((URIImpl) uri).uri);
	}
	
	public Object clone() {
		AddressImpl retval = new AddressImpl();
		retval.address = (javax.sip.address.Address) address.clone();
		retval.parameters = ParameterableHeaderImpl.cloneParameters(this.parameters);
		return retval;
	}

	public String toString() {
		// Returns the value of this address as a String. The resulting string
		// must be a valid value of a SIP From or To header.
		// To/From are parametrable.
		StringBuffer retval = new StringBuffer();
		retval.append(address.toString());
		//excluding the parameters already present in the address uri
		if ( parameters!= null) {
			for(Map.Entry<java.lang.String, String> entry : parameters.entrySet()) {
				if((!(address.getURI() instanceof Parameters)) || ((Parameters)address.getURI()).getParameter(entry.getKey()) == null) {
					String value = entry.getValue();
					if(value != null && value.length() > 0) {
						retval.append(PARAM_SEPARATOR).append(entry.getKey()).append(PARAM_NAME_VALUE_SEPARATOR).append(value);
					} else {
						retval.append(PARAM_SEPARATOR).append(entry.getKey());
					}
				}
			}
		}
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
			ContactHeader contactHeader = (ContactHeader) headerFactory.createHeader(
					ContactHeader.NAME, value);
			this.address = addressFactory.createAddress(value);
			this.parameters = getParameters(((Parameters) contactHeader));
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
		
		/*
		if(this.address.getURI() instanceof Parameters) {
			Parameters uri = (Parameters) this.address.getURI();
			try {
				uri.setParameter(name, value);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Problem setting parameter",e);
			}
		}*/
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AddressImpl other = (AddressImpl) obj;
		if (address.getURI() == null) {
			if (other.address.getURI() != null)
				return false;
		} else if (!address.getURI().equals(other.address.getURI())) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else {
			for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
				String pname = it.next();
				
				String p1 = parameters.get(pname);
				String p2 = other.parameters.get(pname);
				
				// those present in both must match (case-insensitive)
				if (p1!=null && p2!=null && !RFC2396UrlDecoder.decode(p1).equalsIgnoreCase(RFC2396UrlDecoder.decode(p2))) return false;
			}
		}
		return true;
	}
	
	public static final Map<String, String> getParameters(Parameters headerParams) {
		Map<String, String> params = new HashMap<String, String>();
		Iterator<String> parameterNames = headerParams.getParameterNames();			
		while (parameterNames.hasNext()) {
			String name = (String) parameterNames.next();
			String value = headerParams.getParameter(name);
			params.put(name, value);
		}
		return params;
	}
	
}
