package org.mobicents.servlet.sip.startup.loading;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.deploy.SecurityCollection;

public class SipSecurityCollection extends SecurityCollection {
	public List<String> servletNames = new ArrayList<String>();
	public List<String> sipMethods = new ArrayList<String>();
		
	
	/**
	 * 
	 */
	public SipSecurityCollection() {
		super();	
	}
	
	/**
	 * 
	 * @param name
	 * @param description
	 */
	public SipSecurityCollection(String name, String description) {
		super(name, description);
	}
	/**
	 * 
	 * @param name
	 */
	public SipSecurityCollection(String name) {
		super(name);
	}
	
	/**
	 * 
	 * @param servletName
	 */
	public void addServletName(String servletName) {
		if(servletName == null) {
			return;
		}
		servletNames.add(servletName);
	}
	
	/**
	 * 
	 * @param servletName
	 */
	public void removeServletName(String servletName) {
		if(servletName == null) {
			return;
		}
		servletNames.remove(servletName);
	}
	
	/**
	 * 
	 * @param sipMethod
	 */
	public void addSipMethod(String sipMethod) {
		if(sipMethod == null) {
			return;
		}
		sipMethods.add(sipMethod);
	}
	
	/**
	 * 
	 * @param sipMethod
	 */
	public void removeSipMethod(String sipMethod) {
		if(sipMethod == null) {
			return;
		}
		sipMethods.remove(sipMethod);
	}
}
