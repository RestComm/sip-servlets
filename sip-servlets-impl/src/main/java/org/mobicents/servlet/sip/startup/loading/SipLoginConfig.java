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
package org.mobicents.servlet.sip.startup.loading;

import java.util.HashMap;

import org.apache.catalina.deploy.LoginConfig;
/**
 * 
 *
 */
public class SipLoginConfig extends LoginConfig {	
	private static final long serialVersionUID = 1L;
	public static final String BASIC_AUTHENTICATION_METHOD = "BASIC";
	public static final String DIGEST_AUTHENTICATION_METHOD = "DIGEST";
	public static final String CLIENT_CERT_AUTHENTICATION_METHOD = "CLIENT_CERT";
	
	public static final String IDENTITY_SCHEME_SUPPORTED = "SUPPORTED";
	public static final String IDENTITY_SCHEME_REQUIRED = "REQUIRED";
	
	public static final String IDENTITY_SCHEME_P_ASSERTED = "P-Asserted-Identity";
	public static final String IDENTITY_SCHEME_IDENTITY = "Identity";
	
	private HashMap<String, String> identityAssertionSchemes = new HashMap<String, String>();
	
	public void addIdentityAssertion(String scheme, String support) {
		identityAssertionSchemes.put(scheme, support);
	}
	
	public String getIdetitySchemeSettings(String scheme) {
		return identityAssertionSchemes.get(scheme);
	}
}
