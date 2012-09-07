/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.mobicents.servlet.sip.catalina.security;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;

import java.lang.reflect.Method;
import java.security.Principal;

import javax.servlet.sip.SipServletResponse;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;

import org.apache.catalina.Realm;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.RealmBase;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.catalina.CatalinaSipContext;
import org.mobicents.servlet.sip.catalina.SipLoginConfig;
import org.mobicents.servlet.sip.catalina.SipSecurityCollection;
import org.mobicents.servlet.sip.catalina.SipSecurityConstraint;
import org.mobicents.servlet.sip.catalina.security.authentication.DigestAuthenticator;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.core.security.SipPrincipal;

public class SipSecurityUtils {
	
	private static final Logger log = Logger.getLogger(SipSecurityUtils.class);
	private SipContext sipStandardContext;
	
	public SipSecurityUtils(SipContext sipContext) {
		this.sipStandardContext = sipContext;
	}
	
	public boolean authenticate(MobicentsSipServletRequest request, SipSecurityConstraint sipConstraint)
	{
		boolean authenticated = false;
		SipLoginConfig loginConfig = (SipLoginConfig) sipStandardContext.getSipLoginConfig();
		try
		{
			if(loginConfig != null) {
				String authMethod = loginConfig.getAuthMethod();
				if(authMethod != null)
				{
					// (1) First check for Proxy Asserted Identity
					String pAssertedIdentitySetting = loginConfig.getIdentitySchemeSettings(MobicentsSipLoginConfig.IDENTITY_SCHEME_P_ASSERTED);
					if(pAssertedIdentitySetting != null) {
						if(request.getHeader(PAssertedIdentityHeader.NAME) != null) {
							String pAssertedHeaderValue = request.getHeader(PAssertedIdentityHeader.NAME);
							
							//If P-Identity is required we must send error message immediately
							if(pAssertedHeaderValue == null &&
									MobicentsSipLoginConfig.IDENTITY_SCHEME_REQUIRED.equals(pAssertedIdentitySetting)) {
								request.createResponse(428, "P-Asserted-Idetity header is required!").send();
								return false;
							}
							javax.sip.address.Address address = 
								sipStandardContext.getSipApplicationDispatcher().getSipFactory().getAddressFactory().createAddress(pAssertedHeaderValue);
							String username = null;
							if(address.getURI().isSipURI()) {
								SipURI sipUri = (SipURI)address.getURI();
								username = sipUri.getUser();
							} else {
								TelURL telUri = (TelURL)address.getURI();
								username = telUri.getPhoneNumber();
							}
							Realm realm = ((CatalinaSipContext)sipStandardContext).getRealm();
							SipPrincipal principal = impersonatePrincipal(username, realm);
							
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
						DigestAuthenticator digestAuthenticator = new DigestAuthenticator(sipStandardContext.getSipApplicationDispatcher().getSipFactory().getHeaderFactory());
						digestAuthenticator.setContext((CatalinaSipContext)sipStandardContext);
						MobicentsSipServletResponse response = createErrorResponse(request, sipConstraint);
						authenticated = digestAuthenticator.authenticate(request, response, loginConfig, ((CatalinaSipContext)sipStandardContext).getSecurityDomain());		
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
	
	private static MobicentsSipServletResponse createErrorResponse(MobicentsSipServletRequest request, SipSecurityConstraint sipConstraint)
	{
		SipServletResponse response = null;
    	if(sipConstraint.isProxyAuthentication()) {
    		response = (MobicentsSipServletResponse) 
    			request.createResponse(MobicentsSipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
    	} else {
    		response = (MobicentsSipServletResponse) 
    			request.createResponse(MobicentsSipServletResponse.SC_UNAUTHORIZED);
    	}
    	return (MobicentsSipServletResponse) response;
	}
	
	public boolean authorize(MobicentsSipServletRequest request)
	{
		boolean allConstrainsSatisfied = true;
		SecurityConstraint[] constraints = ((CatalinaSipContext)sipStandardContext).findConstraints();
		
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
						if(authenticate(request, sipConstraint)) {
							CatalinaSipPrincipal principal = (CatalinaSipPrincipal) request.getUserPrincipal();
							if(principal == null) return false;
							
							for(String assignedRole:constraint.findAuthRoles()) {
								if(principal.isUserInRole(assignedRole)) {
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
	public static SipPrincipal impersonatePrincipal(String username, Realm realm) {
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
			return new CatalinaSipPrincipal(principal);
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
