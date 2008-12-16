/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.metadata.sip.jboss;

import java.util.ArrayList;
import java.util.List;

import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.sip.spec.ProxyConfigMetaData;
import org.jboss.metadata.sip.spec.ServletSelectionMetaData;
import org.jboss.metadata.sip.spec.SipLoginConfigMetaData;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletsMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class JBossConvergedSipMetaData extends JBossWebMetaData {
	private static final long serialVersionUID = 1;

	private String applicationName;
	private ServletSelectionMetaData servletSelection;
	private ProxyConfigMetaData proxyConfig;	  
	private List<SipSecurityConstraintMetaData> sipSecurityContraints;
	private SessionConfigMetaData sipSessionConfig;
	private SipLoginConfigMetaData sipLoginConfig;     
	private List<ParamValueMetaData> sipContextParams;
	private List<ListenerMetaData> sipListeners;
	private ServletsMetaData sipServlets;
	
	
	public void merge(JBossConvergedSipMetaData override, SipMetaData original)
	{
		this.merge(override, original, "jboss-web.xml", "sip.xml", false);
	}
    public void merge(JBossConvergedSipMetaData override, SipMetaData original,
         String overrideFile, String overridenFile, boolean mustOverride)
    {
      super.merge(override, original);

      if(override != null && override.getApplicationName()!= null)
         setApplicationName(override.getApplicationName());
      else if(original != null && original.getApplicationName() != null)
         setApplicationName(original.getApplicationName());

      if(override != null && override.servletSelection!= null)
         setServletSelection(override.servletSelection);
      else if(original != null && original.getServletSelection() != null)
    	  setServletSelection(original.getServletSelection());

      if(override != null && override.proxyConfig!= null)
         setProxyConfig(override.proxyConfig);
      else if(original != null && original.getProxyConfig() != null)
    	 setProxyConfig(original.getProxyConfig());
      
      if(override != null && override.sipSessionConfig!= null)
          setSipSessionConfig(override.sipSessionConfig);
       else if(original != null && original.getSessionConfig() != null)
          setSipSessionConfig(original.getSessionConfig());
      
      if(override != null && override.sipLoginConfig!= null)
         setSipLoginConfig(override.sipLoginConfig);
      else if(original != null && original.getSipLoginConfig() != null)
         setSipLoginConfig(original.getSipLoginConfig());
      
      if(override != null && override.sipSecurityContraints!= null)
         setSipSecurityContraints(override.sipSecurityContraints);
      else if(original != null && original.getSipSecurityContraints() != null)
         setSipSecurityContraints(original.getSipSecurityContraints());

      if(override != null && override.sipContextParams!= null)
          setSipContextParams(override.sipContextParams);
       else if(original != null && original.getContextParams() != null)
          setSipContextParams(original.getContextParams());
      
      if(override != null && override.sipListeners!= null)
          setSipListeners(override.sipListeners);
       else if(original != null && original.getListeners() != null)
          setSipListeners(original.getListeners());
      
      if(override != null && override.sipServlets!= null)
          setSipServlets(override.sipServlets);
       else if(original != null && original.getServlets() != null)
          setSipServlets(original.getServlets());

      JBossServletsMetaData soverride = null;
      ServletsMetaData soriginal = null;
      if(override != null)
         soverride = override.getServlets();
      if(original != null)
         soriginal = original.getServlets();
      setServlets(JBossServletsMetaData.merge(soverride, soriginal));
      
      List<ParamValueMetaData> mergedContextParams = new ArrayList<ParamValueMetaData>(sipContextParams);
      if(override != null && override.getContextParams() != null) {
    	  mergedContextParams.addAll(override.getContextParams());
      }
      setContextParams(mergedContextParams);
      
      //listeners should not be merged because they have a special treatment when loading the context
   }
	/**
	 * @param applicationName the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}
	/**
	 * @param servletSelection the servletSelection to set
	 */
	public void setServletSelection(ServletSelectionMetaData servletSelection) {
		this.servletSelection = servletSelection;
	}
	/**
	 * @return the servletSelection
	 */
	public ServletSelectionMetaData getServletSelection() {
		return servletSelection;
	}
	/**
	 * @param proxyConfig the proxyConfig to set
	 */
	public void setProxyConfig(ProxyConfigMetaData proxyConfig) {
		this.proxyConfig = proxyConfig;
	}
	/**
	 * @return the proxyConfig
	 */
	public ProxyConfigMetaData getProxyConfig() {
		return proxyConfig;
	}
	/**
	 * @param sipSecurityContraints the sipSecurityContraints to set
	 */
	public void setSipSecurityContraints(List<SipSecurityConstraintMetaData> sipSecurityContraints) {
		this.sipSecurityContraints = sipSecurityContraints;
	}
	/**
	 * @return the sipSecurityContraints
	 */
	public List<SipSecurityConstraintMetaData> getSipSecurityContraints() {
		return sipSecurityContraints;
	}
	/**
	 * @param sipLoginConfig the sipLoginConfig to set
	 */
	public void setSipLoginConfig(SipLoginConfigMetaData sipLoginConfig) {
		this.sipLoginConfig = sipLoginConfig;
	}
	/**
	 * @return the sipLoginConfig
	 */
	public SipLoginConfigMetaData getSipLoginConfig() {
		return sipLoginConfig;
	}
	/**
	 * @param sipContextParams the sipContextParams to set
	 */
	public void setSipContextParams(List<ParamValueMetaData> sipContextParams) {
		this.sipContextParams = sipContextParams;
	}
	/**
	 * @return the sipContextParams
	 */
	public List<ParamValueMetaData> getSipContextParams() {
		return sipContextParams;
	}
	/**
	 * @param sipListeners the sipListeners to set
	 */
	public void setSipListeners(List<ListenerMetaData> sipListeners) {
		this.sipListeners = sipListeners;
	}
	/**
	 * @return the sipListeners
	 */
	public List<ListenerMetaData> getSipListeners() {
		return sipListeners;
	}
	/**
	 * @param sipServlets the sipServlets to set
	 */
	public void setSipServlets(ServletsMetaData sipServlets) {
		this.sipServlets = sipServlets;
	}
	/**
	 * @return the sipServlets
	 */
	public ServletsMetaData getSipServlets() {
		return sipServlets;
	}
	/**
	 * @param sipSessionConfig the sipSessionConfig to set
	 */
	public void setSipSessionConfig(SessionConfigMetaData sipSessionConfig) {
		this.sipSessionConfig = sipSessionConfig;
	}
	/**
	 * @return the sipSessionConfig
	 */
	public SessionConfigMetaData getSipSessionConfig() {
		return sipSessionConfig;
	}
}
