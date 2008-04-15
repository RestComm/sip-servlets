package org.mobicents.servlet.sip.startup;

import java.util.HashMap;

import org.apache.catalina.Context;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;

public interface SipContext extends Context {

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getApplicationName()
	 */
	String getApplicationName();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setApplicationName(java.lang.String)
	 */
	void setApplicationName(String applicationName);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getDescription()
	 */
	String getDescription();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setDescription(java.lang.String)
	 */
	void setDescription(String description);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getLargeIcon()
	 */
	String getLargeIcon();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setLargeIcon(java.lang.String)
	 */
	void setLargeIcon(String largeIcon);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getListeners()
	 */
	SipListenersHolder getListeners();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setListeners(org.mobicents.servlet.sip.core.session.SipListenersHolder)
	 */
	void setListeners(SipListenersHolder listeners);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getMainServlet()
	 */
	String getMainServlet();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setMainServlet(java.lang.String)
	 */
	void setMainServlet(String mainServlet);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getProxyTimeout()
	 */
	int getProxyTimeout();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setProxyTimeout(int)
	 */
	void setProxyTimeout(int proxyTimeout);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSecurityConstraints()
	 */
	HashMap getSecurityConstraints();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSecurityConstraints(java.util.HashMap)
	 */
	void setSecurityConstraints(HashMap securityConstraints);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSecurityRoles()
	 */
	HashMap getSecurityRoles();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSecurityRoles(java.util.HashMap)
	 */
	void setSecurityRoles(HashMap securityRoles);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getServlets()
	 */
	HashMap getSipServlets();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setServlets(java.util.HashMap)
	 */
	void setSipServlets(HashMap sipServlets);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSessionTimeout()
	 */
	int getSessionTimeout();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSessionTimeout(int)
	 */
	void setSessionTimeout(int sessionTimeout);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSipApplicationSessionAttributeMap()
	 */
	HashMap<String, Object> getSipApplicationSessionAttributeMap();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSipApplicationSessionAttributeMap(java.util.HashMap)
	 */
	void setSipApplicationSessionAttributeMap(
			HashMap<String, Object> sipApplicationSessionAttributeMap);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSmallIcon()
	 */
	String getSmallIcon();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSmallIcon(java.lang.String)
	 */
	void setSmallIcon(String smallIcon);

}