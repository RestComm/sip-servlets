package org.mobicents.servlet.sip.startup.loading;

import javax.servlet.ServletException;

import org.apache.catalina.core.StandardWrapper;

/**
 * Sip implementation of the <b>Wrapper</b> interface that represents
 * an individual servlet definition. This class inherits from the StandardWrapper Tomcat Class.
 * No child Containers are allowed, and the parent Container must be a Context.
 *
 */
public class SipServletImpl extends StandardWrapper {
	public String icon;
	public String servletName;
	public String displayName;
	public String description;
	
	@Override
	public void load() throws ServletException {
		super.load();
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the servletName
	 */
	public String getServletName() {
		return servletName;
	}

	/**
	 * @param servletName the servletName to set
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
