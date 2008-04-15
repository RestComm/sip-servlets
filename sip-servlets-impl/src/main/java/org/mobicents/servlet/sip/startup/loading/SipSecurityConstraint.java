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
package org.mobicents.servlet.sip.startup.loading;

import org.apache.catalina.deploy.SecurityConstraint;

/**
 * Sip Security 
 *
 */
public class SipSecurityConstraint extends SecurityConstraint {	
	public boolean proxyAuthentication;

	/**
	 * @return the proxyAuthentication
	 */
	public boolean isProxyAuthentication() {
		return proxyAuthentication;
	}

	/**
	 * @param proxyAuthentication the proxyAuthentication to set
	 */
	public void setProxyAuthentication(boolean proxyAuthentication) {
		this.proxyAuthentication = proxyAuthentication;
	}
	
	
	public void addCollection(SipSecurityCollection sipSecurityCollection) {
		super.addCollection(sipSecurityCollection);
	}
	
	public void removeCollection(SipSecurityCollection sipSecurityCollection) {
		super.removeCollection(sipSecurityCollection);
	}
}
