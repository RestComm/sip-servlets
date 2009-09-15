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

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.sip.URI;
import javax.sip.header.Parameters;

public abstract class URIImpl extends ParameterableImpl implements URI {
	private static final long serialVersionUID = 1L;
//	private static Log logger = LogFactory.getLog(URIImpl.class
//			.getCanonicalName());

	javax.sip.address.URI uri;

	public URIImpl(javax.sip.address.URI uri) {
		this.uri = uri;
	}

	public URIImpl(javax.sip.address.TelURL telUrl) {
		this.uri = telUrl;
		if(this.uri instanceof Parameters) {
			super.setParameters(AddressImpl.getParameters((Parameters)this.uri));
		} else {
			super.setParameters(new ConcurrentHashMap<String, String>());
		}	
	}

	public URIImpl(javax.sip.address.SipURI sipUri) {
		this.uri = sipUri;
		if(this.uri instanceof Parameters) {
			super.setParameters(AddressImpl.getParameters((Parameters)this.uri));
		} else {
			super.setParameters(new ConcurrentHashMap<String, String>());
		}
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	//Added by Thomas Leseney from Nexcom Systems 
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	//Added by Thomas Leseney from Nexcom Systems 
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final URIImpl other = (URIImpl) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

}
