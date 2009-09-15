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

import java.text.ParseException;

import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipFactories;

/**
 * URI wrapper for any scheme URI implementation
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class GenericURIImpl extends URIImpl {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(GenericURIImpl.class.getCanonicalName());

	public GenericURIImpl(javax.sip.address.URI uri) {
		super(uri);
	}

	@Override
	public URI clone() {		
		return new GenericURIImpl((javax.sip.address.URI)uri.clone());
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.Parameterable#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		try {
			this.uri = SipFactories.addressFactory.createURI(value);
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

	
}
