package org.mobicents.servlet.sip.startup;

import java.util.Map;

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

	void addConstraint(org.apache.catalina.deploy.SecurityConstraint securityConstraint);
	
	void removeConstraint(org.apache.catalina.deploy.SecurityConstraint securityConstraint);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSecurityRoles()
	 */
	Map getSecurityRoles();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSecurityRoles(java.util.HashMap)
	 */
	void setSecurityRoles(Map securityRoles);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSipApplicationSessionAttributeMap()
	 */
	Map<String, Object> getSipApplicationSessionAttributeMap();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSipApplicationSessionAttributeMap(java.util.Map)
	 */
	void setSipApplicationSessionAttributeMap(
			Map<String, Object> sipApplicationSessionAttributeMap);

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#getSmallIcon()
	 */
	String getSmallIcon();

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.startup.loading.SipServletApplication#setSmallIcon(java.lang.String)
	 */
	void setSmallIcon(String smallIcon);

}