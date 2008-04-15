package org.mobicents.servlet.sip.startup.loading;

import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.catalina.core.StandardWrapper;

public class SipServletImpl extends StandardWrapper {
	public String icon;
	public String servletName;
	public String displayName;
	public String description;
	public String servletClass;
	public HashMap initParameters;
	public int loadOnStartup;
	public String runAs;
	public HashMap securityRoleRefs;
	
	@Override
	public void load() throws ServletException {
		super.load();
	}
}
