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

import javax.servlet.sip.URI;

public abstract class URIImpl extends ParameterableImpl implements URI {

//	private static Log logger = LogFactory.getLog(URIImpl.class
//			.getCanonicalName());

	javax.sip.address.URI uri;

	public URIImpl(javax.sip.address.URI uri) {
		this.uri = uri;
	}

	public URIImpl(javax.sip.address.TelURL telUrl) {
		this.uri = telUrl;
		super.setParameters(((gov.nist.javax.sip.address.TelURLImpl) telUrl)
				.getParameters());
	}

	public URIImpl(javax.sip.address.SipURI sipUri) {
		this.uri = sipUri;
		super.setParameters(((gov.nist.javax.sip.address.SipUri) sipUri)
				.getParameters());
	}

	public javax.sip.address.URI getURI() {
		return this.uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.URI#getScheme()
	 */
	public String getScheme() {
		return uri.getScheme();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.sip.URI#isSipURI()
	 */
	public boolean isSipURI() {
		return uri.isSipURI();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#toString()
	 */
	public String toString() {
		return this.uri.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.address.ParameterableImpl#clone()
	 */
	public abstract URI clone();

}
