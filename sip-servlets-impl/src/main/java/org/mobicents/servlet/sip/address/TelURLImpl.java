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

import gov.nist.javax.sip.parser.Lexer;

import java.text.ParseException;
import java.util.Iterator;

import javax.servlet.sip.TelURL;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipFactories;

/**
 * Wrapper class for servlet TelURL. Would be nice if the javax.sip and servlet
 * spec adopted the same convention.
 * 
 * @author M. Ranganathan
 * 
 */
public class TelURLImpl extends URIImpl implements TelURL {

	private static Logger logger = Logger.getLogger(TelURLImpl.class
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
		String phoneNumber = number;
		if(number.startsWith("+")) {			
			phoneNumber = phoneNumber.substring(1);
		}
		try {
			base_phone_number(phoneNumber);		
			telUrl.setPhoneNumber(phoneNumber);
		} catch (ParseException ex) {
			logger.error("Error setting phone number " + number);
			throw new java.lang.IllegalArgumentException("phone number " + number + " is invalid", ex);
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
		super.removeParameter(name);
		telUrl.removeParameter(name);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#setParameter(java.lang.String, java.lang.String)
	 */
	public void setParameter(String name, String value) {
		super.setParameter(name, value);
		try {
			telUrl.setParameter(name, value);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Problem setting parameter", e);
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

	/**
	 * {@inheritDoc}
	 */
	public String getPhoneContext() {
		return telUrl.getPhoneContext();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPhoneNumber(String number, String phoneContext) {
		setPhoneNumber(number);
		try {
			telUrl.setPhoneContext(phoneContext);
		} catch (ParseException ex) {
			logger.error("Error setting phone number " + number);
			throw new java.lang.IllegalArgumentException("phone number " + number + " is invalid", ex);
		}
	}
	
	// there might be a bug in the jsip impl in URLParser class need to check with ranga after TCK is done
	// the part in comment 
	private String base_phone_number(String number) throws ParseException {
		StringBuffer s = new StringBuffer();
		Lexer lexer = new Lexer("sip_urlLexer", number);
		
		int lc = 0;
		while (lexer.hasMoreChars()) {
			char w = lexer.lookAhead(0);
			if (Lexer.isDigit(w)
				|| w == '-'
				|| w == '.'
				|| w == '('
				|| w == ')') {
				lexer.consume(1);
				s.append(w);
				lc++;
			}
//				else if (lc > 0)
//					break;
			else
				throw new IllegalArgumentException("unexpected " + w + " in the phone number");
		}
		return s.toString();

	}
	
	
}
