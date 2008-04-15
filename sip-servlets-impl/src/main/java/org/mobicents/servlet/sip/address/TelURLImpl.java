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

	private TelURLImpl() {
		super();
	}

	public TelURLImpl(javax.sip.address.TelURL telUrl) {
		super(telUrl);
		this.telUrl = telUrl;
	}

	public String getPhoneNumber() {

		return telUrl.getPhoneNumber();
	}

	public boolean isGlobal() {

		return telUrl.isGlobal();
	}

	public void setPhoneNumber(String number) {
		try {
			telUrl.setPhoneNumber(number);
		} catch (ParseException ex) {
			logger.error("Error setting phone number " + number);
			throw new java.lang.IllegalArgumentException(ex);
		}
	}

	public String getParameter(String key) {
		return telUrl.getParameter(key);
	}

	@SuppressWarnings("unchecked")
	public Iterator<String> getParameterNames() {
		return telUrl.getParameterNames();
	}

	public String getScheme() {
		return telUrl.getScheme();
	}

	public boolean isSipURI() {
		return false;
	}

	public void removeParameter(String name) {
		telUrl.removeParameter(name);

	}

	public void setParameter(String name, String value) {

		try {
			telUrl.setParameter(name, value);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}

	}

	public Object clone() {
		return new TelURLImpl((javax.sip.address.TelURL) this.telUrl.clone());

	}

	public String toString() {
		return this.telUrl.toString();
	}

	public String getValue() {
		return this.telUrl.toString();
	}

	public void setValue(String value) {
		try {
			this.telUrl = SipFactories.addressFactory.createTelURL(value);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Bad url string " + value);
		}

	}
}
