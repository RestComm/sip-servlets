package org.mobicents.servlet.sip.security;

import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.security.authentication.AuthenticatorBase;
import org.mobicents.servlet.sip.security.authentication.DigestAuthenticator;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.ResourceCollection;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityCollection;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;

public class SipSecurityUtils {
	
	private static Log log = LogFactory.getLog(SipSecurityUtils.class);
	
	public static boolean authenticate(SipStandardContext sipStandardContext, SipServletRequestImpl request)
	{
		boolean authenticated = false;
		SipLoginConfig loginConfig = sipStandardContext.getSipLoginConfig();
		try
		{
			if(loginConfig != null) {
				String authMethod = loginConfig.getAuthMethod();
				if(authMethod != null)
				{
					if(authMethod.equalsIgnoreCase("DIGEST")) {
						DigestAuthenticator auth = new DigestAuthenticator();
						auth.setContext(sipStandardContext);
						authenticated = auth.authenticate(request, null, loginConfig);
						request.setUserPrincipal(auth.getPrincipal());
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
	
	public static boolean authorize(SipStandardContext sipStandardContext, SipServletRequestImpl request)
	{
		boolean allConstrainsSatisfied = true;
		GenericPrincipal principal = (GenericPrincipal) request.getUserPrincipal();
		SecurityConstraint[] constraints = sipStandardContext.findConstraints();
		
		// If we have no constraints, just authorize the request;
		if(constraints.length == 0) return true;
		
		constraints: for(SecurityConstraint constraint:constraints)
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
						
						// If yes, see if the current user is in a role compatible with the
						// required roles for the resource.
						if(principal == null) return false;
						boolean constraintSatisfied = false;
						for(String assignedRole:constraint.findAuthRoles()) {
							if(principal.hasRole(assignedRole)) {
								constraintSatisfied = true;
								break;
							}
						}
						if(!constraintSatisfied) allConstrainsSatisfied = false;
					}
				}
			}
		}
		return allConstrainsSatisfied;
	}
}
