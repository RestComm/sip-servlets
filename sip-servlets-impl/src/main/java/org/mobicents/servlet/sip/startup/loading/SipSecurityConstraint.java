package org.mobicents.servlet.sip.startup.loading;

import org.apache.catalina.deploy.SecurityCollection;
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
