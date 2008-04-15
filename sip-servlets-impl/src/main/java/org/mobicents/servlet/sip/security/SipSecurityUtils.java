package org.mobicents.servlet.sip.security;

import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.security.authentication.AuthenticatorBase;
import org.mobicents.servlet.sip.security.authentication.DigestAuthenticator;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;

public class SipSecurityUtils {
	
	private static Log log = LogFactory.getLog(SipSecurityUtils.class);
	
	public static boolean authenticate(SipStandardContext sipStandardContext, SipServletRequestImpl request)
	{
		boolean authenticated = false;
		SecurityConstraint[] constraints = sipStandardContext.findConstraints();
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
					} else if(authMethod.equalsIgnoreCase("BASIC")) {
						throw new IllegalStateException("Basic authentication not supported in JSR 289")
					}
				}
			}
			else {
				log.debug("No login configuration found in sip.xml. We won't authenticate.");
				return true; // There is no auth config in sip.xml. So don't authenticate.
			}
			/*
			for(SecurityConstraint constraint:constraints)
			{
				if(constraint instanceof SipSecurityConstraint)
				{
					SipSecurityConstraint sipConstraint = 
						(SipSecurityConstraint) constraint;
					SipURI originator = ((SipURI)request.getFrom().getURI());
					String username = originator.getUser();
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return authenticated;
	}
}
