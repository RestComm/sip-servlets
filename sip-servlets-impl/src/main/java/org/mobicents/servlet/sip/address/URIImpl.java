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
