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
package org.mobicents.servlet.sip.security;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;

import java.lang.reflect.Method;
import java.security.Principal;

import javax.servlet.sip.SipServletResponse;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;

import org.apache.catalina.Realm;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.security.authentication.DigestAuthenticator;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityCollection;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;

public class SipSecurityUtils {
	
	private static transient Logger log = Logger.getLogger(SipSecurityUtils.class);
	
	public static boolean authenticate(SipContext sipStandardContext,
			SipServletRequestImpl request,
			SipSecurityConstraint sipConstraint)
	{
		boolean authenticated = false;
		SipLoginConfig loginConfig = sipStandardContext.getSipLoginConfig();
		try
		{
			if(loginConfig != null) {
				String authMethod = loginConfig.getAuthMethod();
				if(authMethod != null)
				{
					// (1) First check for Proxy Asserted Identity
					String pAssertedIdentitySetting = loginConfig.getIdetitySchemeSettings(SipLoginConfig.IDENTITY_SCHEME_P_ASSERTED);
					if(pAssertedIdentitySetting != null) {
						if(request.getHeader(PAssertedIdentityHeader.NAME) != null) {
							String pAssertedHeaderValue = request.getHeader(PAssertedIdentityHeader.NAME);
							
							//If P-Identity is required we must send error message immediately
							if(pAssertedHeaderValue == null &&
									SipLoginConfig.IDENTITY_SCHEME_REQUIRED.equals(pAssertedIdentitySetting)) {
								request.createResponse(428, "P-Asserted-Idetity header is required!").send();
								return false;
							}
							javax.sip.address.Address address = 
								SipFactories.addressFactory.createAddress(pAssertedHeaderValue);
							String username = null;
							if(address.getURI().isSipURI()) {
								SipURI sipUri = (SipURI)address.getURI();
								username = sipUri.getUser();
							} else {
								TelURL telUri = (TelURL)address.getURI();
								username = telUri.getPhoneNumber();
							}
							Realm realm = sipStandardContext.getRealm();
							Principal principal = impersonatePrincipal(username, realm);
							
							if(principal != null) {
								authenticated = true;
								request.setUserPrincipal(principal);
								request.getSipSession().setUserPrincipal(principal);
								log.debug("P-Asserted-Identity authetication successful for user: " + username);
							}
						}
					}
					// (2) Then if P-Identity has failed and is not required attempt DIGEST auth
					if(!authenticated && authMethod.equalsIgnoreCase("DIGEST")) {
						DigestAuthenticator digestAuthenticator = new DigestAuthenticator();
						digestAuthenticator.setContext(sipStandardContext);
						SipServletResponseImpl response = createErrorResponse(request, sipConstraint);
						authenticated = digestAuthenticator.authenticate(request, response, loginConfig);		
						request.setUserPrincipal(digestAuthenticator.getPrincipal());
					} else if(authMethod.equalsIgnoreCase("BASIC")) {
						throw new IllegalStateException("Basic authentication not supported in JSR 289");
					}
				}
			}
			else {
				log.debug("No login configuration found in sip.xml. We won't authenticate.");
				return true; // There is no auth config in sip.xml. So don't authenticate.
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return authenticated;
	}
	
	private static SipServletResponseImpl createErrorResponse(SipServletRequestImpl request, SipSecurityConstraint sipConstraint)
	{
		SipServletResponse response = null;
    	if(sipConstraint.isProxyAuthentication()) {
    		response = (SipServletResponseImpl) 
    			request.createResponse(SipServletResponseImpl.SC_PROXY_AUTHENTICATION_REQUIRED);
    	} else {
    		response = (SipServletResponseImpl) 
    			request.createResponse(SipServletResponseImpl.SC_UNAUTHORIZED);
    	}
    	return (SipServletResponseImpl) response;
	}
	
	public static boolean authorize(SipContext sipStandardContext, SipServletRequestImpl request)
	{
		boolean allConstrainsSatisfied = true;
		SecurityConstraint[] constraints = sipStandardContext.findConstraints();
		
		// If we have no constraints, just authorize the request;
		if(constraints.length == 0) {
			return true;
		}
		
		for(SecurityConstraint constraint:constraints)
		{
			if(constraint instanceof SipSecurityConstraint)
			{
				SipSecurityConstraint sipConstraint = (SipSecurityConstraint) constraint;
				for(org.apache.catalina.deploy.SecurityCollection security:sipConstraint.findCollections()) {
					
					// For each secured resource see if it's bound to the current 
					// request method and servlet name.
					SipSecurityCollection sipSecurity = (SipSecurityCollection)security;
					String servletName = request.getSipSession().getHandler();
					if(sipSecurity.findMethod(request.getMethod())
							&& sipSecurity.findServletName(servletName)) {
						boolean constraintSatisfied = false;
						// If yes, see if the current user is in a role compatible with the
						// required roles for the resource.
						if(authenticate(sipStandardContext, request, sipConstraint)) {
							GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
							if(principal == null) return false;
							
							for(String assignedRole:constraint.findAuthRoles()) {
								if(principal.hasRole(assignedRole)) {
									constraintSatisfied = true;
									break;
								}
							}
						}
						if(!constraintSatisfied) {
							allConstrainsSatisfied = false;
							log.error("Constraint \"" + constraint.getDisplayName() + "\" not satifsied");
						}
					}
				}
			}
		}
		return allConstrainsSatisfied;
	}
	
	/*
	 *  This method attempts to obtain the Principal of a user from a realm without having to
	 *  authenticate with a password or certificate. Not all realms support this, but we can use
	 *  reflection to peek into Realms that extend RealmBase.
	 */
	public static Principal impersonatePrincipal(String username, Realm realm) {
		Method getPrincipalMethod = null;
		Class clazz = realm.getClass();
		try {
			if(!(realm instanceof RealmBase)) {
				throw new RuntimeException("Only Realms extending RealmBase are supported. Report this error. Current realm class is " +
						realm.getClass().getCanonicalName());
			}
			while(getPrincipalMethod == null) {
				try {
					getPrincipalMethod = clazz.getDeclaredMethod("getPrincipal", String.class);
				} catch (NoSuchMethodException e) {
					log.warn("unexpected exception while impersonatePrincipal", e);
				}
				clazz = clazz.getSuperclass();
				if(clazz == null) break;
			}
			getPrincipalMethod.setAccessible(true);
			Principal principal =  (Principal) getPrincipalMethod.invoke(realm, username);
			return principal;
		} catch (Throwable t) {
			log.error("Could not impersonate user " + username, t);
		} finally {
			if(getPrincipalMethod != null) {
				getPrincipalMethod.setAccessible(false);
			}
		}
		return null;
		
	}
}
