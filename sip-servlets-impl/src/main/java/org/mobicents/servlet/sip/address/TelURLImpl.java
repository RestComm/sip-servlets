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
package org.mobicents.servlet.sip.address;

import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.sip.TelURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;

/**
 * Wrapper class for servlet TelURL. Would be nice if the javax.sip and servlet
 * spec adopted the same convention.
 * 
 * @author M. Ranganathan
 * 
 */
public class TelURLImpl extends URIImpl implements TelURL {

	private static Log logger = LogFactory.getLog(TelURLImpl.class
			.getCanonicalName());

	private javax.sip.address.TelURL telUrl;		

	public TelURLImpl(javax.sip.address.TelURL telUrl) {
		super(telUrl);
		this.telUrl = telUrl;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TelURL#getPhoneNumber()
	 */
	public String getPhoneNumber() {
		return telUrl.getPhoneNumber();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TelURL#isGlobal()
	 */
	public boolean isGlobal() {
		return telUrl.isGlobal();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.TelURL#setPhoneNumber(java.lang.String)
	 */
	public void setPhoneNumber(String number) {
		try {
			telUrl.setPhoneNumber(number);
		} catch (ParseException ex) {
			logger.error("Error setting phone number " + number);
			throw new java.lang.IllegalArgumentException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#getParameter(java.lang.String)
	 */
	public String getParameter(String key) {
		return telUrl.getParameter(key);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#getParameterNames()
	 */
	@SuppressWarnings("unchecked")
	public Iterator<String> getParameterNames() {
		return telUrl.getParameterNames();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.URIImpl#getScheme()
	 */
	public String getScheme() {
		return telUrl.getScheme();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.URIImpl#isSipURI()
	 */
	public boolean isSipURI() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#removeParameter(java.lang.String)
	 */
	public void removeParameter(String name) {
		telUrl.removeParameter(name);

	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		try {
			telUrl.setParameter(name, value);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.URIImpl#clone()
	 */
	public TelURL clone() {
		return new TelURLImpl((javax.sip.address.TelURL) this.telUrl.clone());
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.URIImpl#toString()
	 */
	public String toString() {
		return this.telUrl.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#getValue()
	 */
	public String getValue() {
		return this.telUrl.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		try {
			this.telUrl = SipFactories.addressFactory.createTelURL(value);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad url string " + value);
		}
	}
}
