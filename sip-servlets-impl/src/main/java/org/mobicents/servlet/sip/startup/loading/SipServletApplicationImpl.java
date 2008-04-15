package org.mobicents.servlet.sip.startup.loading;

import java.util.HashMap;

import org.mobicents.servlet.sip.core.session.SipListenersHolder;

public class SipServletApplicationImpl implements SipServletApplication {
	public String applicationName;
	public String smallIcon;
	public String largeIcon;
	public String displayName;
	public String description;
	public boolean distributable;
	public HashMap contextParams;
	public SipListenersHolder listeners;
	public String mainServlet;
	public HashMap servlets;
	private HashMap servletMapping;
	public int proxyTimeout;
	public int sessionTimeout;
	public HashMap resourceEnvRefs;
	public HashMap resourcesRef;
	public HashMap securityConstraints;
	public LoginConfig loginConfig;
	public HashMap securityRoles;
	public HashMap envEntries;
	public HashMap ejbRefs;
	public HashMap ejbLocalRefs;
	public HashMap<String,Object> sipApplicationSessionAttributeMap;
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getApplicationName()
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setApplicationName(java.lang.String)
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getContextParams()
	 */
	public HashMap getContextParams() {
		return contextParams;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setContextParams(java.util.HashMap)
	 */
	public void setContextParams(HashMap contextParams) {
		this.contextParams = contextParams;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getDisplayName()
	 */
	public String getDisplayName() {
		return displayName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setDisplayName(java.lang.String)
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#isDistributable()
	 */
	public boolean isDistributable() {
		return distributable;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setDistributable(boolean)
	 */
	public void setDistributable(boolean distributable) {
		this.distributable = distributable;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getEjbLocalRefs()
	 */
	public HashMap getEjbLocalRefs() {
		return ejbLocalRefs;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setEjbLocalRefs(java.util.HashMap)
	 */
	public void setEjbLocalRefs(HashMap ejbLocalRefs) {
		this.ejbLocalRefs = ejbLocalRefs;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getEjbRefs()
	 */
	public HashMap getEjbRefs() {
		return ejbRefs;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setEjbRefs(java.util.HashMap)
	 */
	public void setEjbRefs(HashMap ejbRefs) {
		this.ejbRefs = ejbRefs;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getEnvEntries()
	 */
	public HashMap getEnvEntries() {
		return envEntries;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setEnvEntries(java.util.HashMap)
	 */
	public void setEnvEntries(HashMap envEntries) {
		this.envEntries = envEntries;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getLargeIcon()
	 */
	public String getLargeIcon() {
		return largeIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setLargeIcon(java.lang.String)
	 */
	public void setLargeIcon(String largeIcon) {
		this.largeIcon = largeIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getListeners()
	 */
	public SipListenersHolder getListeners() {
		return listeners;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setListeners(org.mobicents.servlet.sip.core.session.SipListenersHolder)
	 */
	public void setListeners(SipListenersHolder listeners) {
		this.listeners = listeners;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getLoginConfig()
	 */
	public LoginConfig getLoginConfig() {
		return loginConfig;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setLoginConfig(org.mobicents.servlet.sip.startup.loading.LoginConfig)
	 */
	public void setLoginConfig(LoginConfig loginConfig) {
		this.loginConfig = loginConfig;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getMainServlet()
	 */
	public String getMainServlet() {
		return mainServlet;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setMainServlet(java.lang.String)
	 */
	public void setMainServlet(String mainServlet) {
		this.mainServlet = mainServlet;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getProxyTimeout()
	 */
	public int getProxyTimeout() {
		return proxyTimeout;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setProxyTimeout(int)
	 */
	public void setProxyTimeout(int proxyTimeout) {
		this.proxyTimeout = proxyTimeout;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getResourceEnvRefs()
	 */
	public HashMap getResourceEnvRefs() {
		return resourceEnvRefs;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setResourceEnvRefs(java.util.HashMap)
	 */
	public void setResourceEnvRefs(HashMap resourceEnvRefs) {
		this.resourceEnvRefs = resourceEnvRefs;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getResourcesRef()
	 */
	public HashMap getResourcesRef() {
		return resourcesRef;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setResourcesRef(java.util.HashMap)
	 */
	public void setResourcesRef(HashMap resourcesRef) {
		this.resourcesRef = resourcesRef;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSecurityConstraints()
	 */
	public HashMap getSecurityConstraints() {
		return securityConstraints;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSecurityConstraints(java.util.HashMap)
	 */
	public void setSecurityConstraints(HashMap securityConstraints) {
		this.securityConstraints = securityConstraints;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSecurityRoles()
	 */
	public HashMap getSecurityRoles() {
		return securityRoles;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSecurityRoles(java.util.HashMap)
	 */
	public void setSecurityRoles(HashMap securityRoles) {
		this.securityRoles = securityRoles;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getServletMapping()
	 */
	public HashMap getServletMapping() {
		return servletMapping;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setServletMapping(java.util.HashMap)
	 */
	public void setServletMapping(HashMap servletMapping) {
		this.servletMapping = servletMapping;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getServlets()
	 */
	public HashMap getServlets() {
		return servlets;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setServlets(java.util.HashMap)
	 */
	public void setServlets(HashMap servlets) {
		this.servlets = servlets;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSessionTimeout()
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSessionTimeout(int)
	 */
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSipApplicationSessionAttributeMap()
	 */
	public HashMap<String, Object> getSipApplicationSessionAttributeMap() {
		return sipApplicationSessionAttributeMap;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSipApplicationSessionAttributeMap(java.util.HashMap)
	 */
	public void setSipApplicationSessionAttributeMap(
			HashMap<String, Object> sipApplicationSessionAttributeMap) {
		this.sipApplicationSessionAttributeMap = sipApplicationSessionAttributeMap;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSmallIcon()
	 */
	public String getSmallIcon() {
		return smallIcon;
	}
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSmallIcon(java.lang.String)
	 */
	public void setSmallIcon(String smallIcon) {
		this.smallIcon = smallIcon;
	}
}
