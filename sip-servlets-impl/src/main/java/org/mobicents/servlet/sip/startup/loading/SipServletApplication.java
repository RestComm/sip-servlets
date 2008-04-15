package org.mobicents.servlet.sip.startup.loading;

import java.util.HashMap;

import org.mobicents.servlet.sip.core.session.SipListenersHolder;

public interface SipServletApplication {

	/**
	 * @return the applicationName
	 */
	String getApplicationName();

	/**
	 * @param applicationName the applicationName to set
	 */
	void setApplicationName(String applicationName);

	/**
	 * @return the contextParams
	 */
	HashMap getContextParams();

	/**
	 * @param contextParams the contextParams to set
	 */
	void setContextParams(HashMap contextParams);

	/**
	 * @return the description
	 */
	String getDescription();

	/**
	 * @param description the description to set
	 */
	void setDescription(String description);

	/**
	 * @return the displayName
	 */
	String getDisplayName();

	/**
	 * @param displayName the displayName to set
	 */
	void setDisplayName(String displayName);

	/**
	 * @return the distributable
	 */
	boolean isDistributable();

	/**
	 * @param distributable the distributable to set
	 */
	void setDistributable(boolean distributable);

	/**
	 * @return the ejbLocalRefs
	 */
	HashMap getEjbLocalRefs();

	/**
	 * @param ejbLocalRefs the ejbLocalRefs to set
	 */
	void setEjbLocalRefs(HashMap ejbLocalRefs);

	/**
	 * @return the ejbRefs
	 */
	HashMap getEjbRefs();

	/**
	 * @param ejbRefs the ejbRefs to set
	 */
	void setEjbRefs(HashMap ejbRefs);

	/**
	 * @return the envEntries
	 */
	HashMap getEnvEntries();

	/**
	 * @param envEntries the envEntries to set
	 */
	void setEnvEntries(HashMap envEntries);

	/**
	 * @return the largeIcon
	 */
	String getLargeIcon();

	/**
	 * @param largeIcon the largeIcon to set
	 */
	void setLargeIcon(String largeIcon);

	/**
	 * @return the listeners
	 */
	SipListenersHolder getListeners();

	/**
	 * @param listeners the listeners to set
	 */
	void setListeners(SipListenersHolder listeners);

	/**
	 * @return the loginConfig
	 */
	LoginConfig getLoginConfig();

	/**
	 * @param loginConfig the loginConfig to set
	 */
	void setLoginConfig(LoginConfig loginConfig);

	/**
	 * @return the mainServlet
	 */
	String getMainServlet();

	/**
	 * @param mainServlet the mainServlet to set
	 */
	void setMainServlet(String mainServlet);

	/**
	 * @return the proxyTimeout
	 */
	int getProxyTimeout();

	/**
	 * @param proxyTimeout the proxyTimeout to set
	 */
	void setProxyTimeout(int proxyTimeout);

	/**
	 * @return the resourceEnvRefs
	 */
	HashMap getResourceEnvRefs();

	/**
	 * @param resourceEnvRefs the resourceEnvRefs to set
	 */
	void setResourceEnvRefs(HashMap resourceEnvRefs);

	/**
	 * @return the resourcesRef
	 */
	HashMap getResourcesRef();

	/**
	 * @param resourcesRef the resourcesRef to set
	 */
	void setResourcesRef(HashMap resourcesRef);

	/**
	 * @return the securityConstraints
	 */
	HashMap getSecurityConstraints();

	/**
	 * @param securityConstraints the securityConstraints to set
	 */
	void setSecurityConstraints(HashMap securityConstraints);

	/**
	 * @return the securityRoles
	 */
	HashMap getSecurityRoles();

	/**
	 * @param securityRoles the securityRoles to set
	 */
	void setSecurityRoles(HashMap securityRoles);

	/**
	 * @return the servletMapping
	 */
	HashMap getServletMapping();

	/**
	 * @param servletMapping the servletMapping to set
	 */
	void setServletMapping(HashMap servletMapping);

	/**
	 * @return the servlets
	 */
	HashMap getServlets();

	/**
	 * @param servlets the servlets to set
	 */
	void setServlets(HashMap servlets);

	/**
	 * @return the sessionTimeout
	 */
	int getSessionTimeout();

	/**
	 * @param sessionTimeout the sessionTimeout to set
	 */
	void setSessionTimeout(int sessionTimeout);

	/**
	 * @return the sipApplicationSessionAttributeMap
	 */
	HashMap<String, Object> getSipApplicationSessionAttributeMap();

	/**
	 * @param sipApplicationSessionAttributeMap the sipApplicationSessionAttributeMap to set
	 */
	void setSipApplicationSessionAttributeMap(
			HashMap<String, Object> sipApplicationSessionAttributeMap);

	/**
	 * @return the smallIcon
	 */
	String getSmallIcon();

	/**
	 * @param smallIcon the smallIcon to set
	 */
	void setSmallIcon(String smallIcon);

}