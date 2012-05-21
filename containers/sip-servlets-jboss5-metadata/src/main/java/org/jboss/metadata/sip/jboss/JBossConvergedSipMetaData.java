/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.metadata.ejb.jboss.JBossEnvironmentRefsGroupMetaData;
import org.jboss.metadata.javaee.jboss.RunAsIdentityMetaData;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.sip.spec.ListenerMetaData;
import org.jboss.metadata.sip.spec.MessageDestinationsMetaData;
import org.jboss.metadata.sip.spec.ParamValueMetaData;
import org.jboss.metadata.sip.spec.ProxyConfigMetaData;
import org.jboss.metadata.sip.spec.SecurityRolesMetaData;
import org.jboss.metadata.sip.spec.ServletSelectionMetaData;
import org.jboss.metadata.sip.spec.ServletsMetaData;
import org.jboss.metadata.sip.spec.SessionConfigMetaData;
import org.jboss.metadata.sip.spec.Sip11MetaData;
import org.jboss.metadata.sip.spec.SipLoginConfigMetaData;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;

/**
 * Extend the JBossWebMetaData from JBoss 5 to provide support for converged sip/http applications
 * 
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
	private List<? extends ParamValueMetaData> sipContextParams;
	private Set<ListenerMetaData> sipListeners;
	private JBossServletsMetaData sipServlets;
	private MessageDestinationsMetaData messageDestinations;
	private SecurityRolesMetaData securityRoles;
	private Method sipApplicationKeyMethod;
	private ConcurrencyControlMode concurrencyControlMode;		
	
	public void merge(JBossWebMetaData override, SipMetaData original) {
		this.merge(override, original, "jboss-web.xml", "sip.xml", false);
	}

	public void merge(JBossWebMetaData override, SipMetaData original,
         String overrideFile, String overridenFile, boolean mustOverride)
   {
      super.merge(override, original);

      if(override != null && override.getDistributable()!= null)
          setDistributable(override.getDistributable());
       else if(original != null && original.getDistributable() != null)
          setDistributable(original.getDistributable());
      
      if(override != null && override.isMetadataComplete() != false)
          setMetadataComplete(override.isMetadataComplete());
       else if(original != null && (original instanceof Sip11MetaData) )
       {
          Sip11MetaData sip11MD = (Sip11MetaData) original;
          setMetadataComplete(sip11MD.isMetadataComplete());
       } 
       else if(original != null && (original instanceof JBossSip11MetaData) )
       {
    	   JBossSip11MetaData sip11MD = (JBossSip11MetaData) original;
           setMetadataComplete(sip11MD.isMetadataComplete());
        }
      
      if(original != null && original.getContextParams() != null)
          setSipContextParams(original.getContextParams());       
      
      if(override != null && override.getServletVersion()!= null)
          setServletVersion(override.getServletVersion());
       else if(original != null && original.getVersion() != null)
          setServletVersion(original.getVersion());

       if(original != null && original.getSipSessionConfig() != null) {
    	   SessionConfigMetaData sessionConfigMetaData = new SessionConfigMetaData();
    	   sessionConfigMetaData.setSessionTimeout(original.getSipSessionConfig().getSessionTimeout());
    	   setSipSessionConfig(sessionConfigMetaData); 
       }
       
       if(override != null && override.getSessionConfig()!= null)
           setSessionConfig(override.getSessionConfig());        
       
       if(override != null && override.getFilters()!= null)
          setFilters(override.getFilters());
       
       if(override != null && override.getFilterMappings()!= null)
          setFilterMappings(override.getFilterMappings());
       
       if(override != null && override.getErrorPages()!= null)
          setErrorPages(override.getErrorPages());
       
       if(override != null && override.getJspConfig()!= null)
          setJspConfig(override.getJspConfig());       
       
       if(override != null && override.getLoginConfig()!= null)
          setLoginConfig(override.getLoginConfig());
       
       if(override != null && override.getMimeMappings()!= null)
          setMimeMappings(override.getMimeMappings());
       
       if(override != null && override.getServletMappings()!= null)
          setServletMappings(override.getServletMappings());
       
       if(override != null && override.getSecurityContraints()!= null)
          setSecurityContraints(override.getSecurityContraints());
       
       if(override != null && override.getWelcomeFileList()!= null)
          setWelcomeFileList(override.getWelcomeFileList());
       
       if(override != null && override.getLocalEncodings()!= null)
          setLocalEncodings(override.getLocalEncodings());
       
       if(override != null && override.isJaccAllStoreRole())
          setJaccAllStoreRole(override.isJaccAllStoreRole());
       
       if(override != null && override.getVersion()!= null)
          setVersion(override.getVersion());
       else if(original != null && original.getVersion() != null)
          setVersion(original.getVersion());
       
       if(override != null && override.getContextRoot()!= null)
          setContextRoot(override.getContextRoot());
       
       if(override != null && override.getAlternativeDD()!= null)
          setAlternativeDD(override.getAlternativeDD());
       
       if(override != null && override.getSecurityDomain()!= null)
          setSecurityDomain(override.getSecurityDomain());
       
       if(override != null && override.getJaccContextID()!= null)
          setJaccContextID(override.getJaccContextID());
       
       if(override != null && override.getClassLoading()!= null)
          setClassLoading(override.getClassLoading());
       
       if(override != null && override.getDepends()!= null)
          setDepends(override.getDepends());
       
       if(override != null && override.getRunAsIdentity()!= null)
          setRunAsIdentity(override.getRunAsIdentity());

       if(getSipSecurityRoles() == null)
          setSipSecurityRoles(new SecurityRolesMetaData());
       SecurityRolesMetaData overrideRoles = null;
       SecurityRolesMetaData originalRoles = null;
       if(original != null)
          originalRoles = original.getSecurityRoles();
       getSipSecurityRoles().merge(overrideRoles, originalRoles);

       MessageDestinationsMetaData overrideMsgDests = null;
       MessageDestinationsMetaData originalMsgDests = null;
       if(original != null && original.getMessageDestinations() != null)
          originalMsgDests = original.getMessageDestinations();
       setSipMessageDestinations(MessageDestinationsMetaData.merge(overrideMsgDests,
             originalMsgDests, overridenFile, overrideFile));

       if(this.getJndiEnvironmentRefsGroup() == null)
    	   setJndiEnvironmentRefsGroup(new JBossEnvironmentRefsGroupMetaData());
        Environment env = null;
        JBossEnvironmentRefsGroupMetaData jenv = null;
        if( override != null )
           jenv = (JBossEnvironmentRefsGroupMetaData) override.getJndiEnvironmentRefsGroup();
        if(original != null)
           env = original.getJndiEnvironmentRefsGroup();
        ((JBossEnvironmentRefsGroupMetaData)getJndiEnvironmentRefsGroup()).merge(jenv, env, null, overrideFile, overridenFile, mustOverride);
        
       if(override != null && override.getVirtualHosts()!= null)
          setVirtualHosts(override.getVirtualHosts());
       
       if(override != null && override.isFlushOnSessionInvalidation())
          setFlushOnSessionInvalidation(override.isFlushOnSessionInvalidation());
       
       if(override != null && override.isUseSessionCookies())
          setUseSessionCookies(override.isUseSessionCookies());
       
       if(override != null && override.getReplicationConfig()!= null)
          setReplicationConfig(override.getReplicationConfig());
       
       if(override != null && override.getPassivationConfig()!= null)
          setPassivationConfig(override.getPassivationConfig());
       
       if(override != null && override.getWebserviceDescriptions()!= null)
          setWebserviceDescriptions(override.getWebserviceDescriptions());
       
       if(override != null && override.getArbitraryMetadata()!= null)
          setArbitraryMetadata(override.getArbitraryMetadata());
       
       if(override != null && override.getMaxActiveSessions() != null)
          setMaxActiveSessions(override.getMaxActiveSessions());
       
       if(override != null && override.getSessionCookies() != -1)
          setSessionCookies(override.getSessionCookies());       
      
      if(original != null && original.getApplicationName() != null)
         setApplicationName(original.getApplicationName());

      if(original != null && original.getServletSelection() != null)
    	  setServletSelection(original.getServletSelection());

      if(original != null && original.getProxyConfig() != null)
    	 setProxyConfig(original.getProxyConfig());
      
      if(original != null && original.getSipSessionConfig() != null)
          setSipSessionConfig(original.getSipSessionConfig());
      
      if(original != null && original.getSipLoginConfig() != null)
         setSipLoginConfig(original.getSipLoginConfig());
      
      if(original != null && original.getSipSecurityContraints() != null)
         setSipSecurityContraints(original.getSipSecurityContraints());
      
      if(original != null && original.getContextParams() != null)
          setSipContextParams(original.getContextParams());
      
      if(original != null && original.getListeners() != null)
//          setSipListeners(original.getListeners());
    	  mergeSipListeners(original.getListeners());
      
      JBossServletsMetaData soverride = null;
      ServletsMetaData soriginal = null;
      if(original != null) {
    	  if(original instanceof Sip11MetaData) {
    		  soriginal = ((Sip11MetaData)original).getServlets();
    	  }
    	  if(original instanceof JBossSip11MetaData) {
    		  soriginal = ((JBossSip11MetaData)original).getServlets();
    	  }
      }
      sipServlets = JBossServletsMetaData.merge(soverride, soriginal);


//      if(sipContextParams != null) {
//	      List<ParamValueMetaData> mergedContextParams = new ArrayList<ParamValueMetaData>(sipContextParams);
//	      if(override != null && override.getContextParams() != null) {
//	    	  mergedContextParams.addAll(override.getContextParams());
//	      }
//	      setContextParams(mergedContextParams);
//      }      
      
      if(original != null && original.getSipApplicationKeyMethod() != null)
          setSipApplicationKeyMethod(original.getSipApplicationKeyMethod());
      
      if(original != null && original.getConcurrencyControlMode() != null)
          setConcurrencyControlMode(original.getConcurrencyControlMode());
      
      //listeners should not be merged because they have a special treatment when loading the context
      //baranowb: but listeners have been merged few lines above....
   // Update run-as indentity for a run-as-principal
      if(sipServlets != null)
      {
         for(JBossServletMetaData servlet : sipServlets)
         {
            String servletName = servlet.getServletName();
            String principalName = servlet.getRunAsPrincipal();
            // Get the sip.xml run-as primary role
            String sipXmlRunAs = null;
            if(servlet.getRunAs() != null)
               sipXmlRunAs = servlet.getRunAs().getRoleName();
            if (principalName != null)
            {
               // Update the run-as indentity to use the principal name
               if (sipXmlRunAs == null)
               {
                  //Needs to be merged from Annotations
                  sipXmlRunAs = "PLACEHOLDER_FOR_ANNOTATION";
                  //throw new IllegalStateException("run-as-principal: " + principalName + " found in jboss-web.xml but there was no run-as in web.xml");
               }
               // See if there are any additional roles for this principal
               Set<String> extraRoles = getSecurityRoles().getSecurityRoleNamesByPrincipal(principalName);
               RunAsIdentityMetaData runAsId = new RunAsIdentityMetaData(sipXmlRunAs, principalName, extraRoles);
               getRunAsIdentity().put(servletName, runAsId);
            }
            else if (sipXmlRunAs != null)
            {
               RunAsIdentityMetaData runAsId = new RunAsIdentityMetaData(sipXmlRunAs, null);
               getRunAsIdentity().put(servletName, runAsId);
            }
         }
      }
   }
	
	public void merge(JBossConvergedSipMetaData override, SipMetaData original)
	{
		this.merge(override, original, "jboss-web.xml", "sip.xml", false);
	}
    public void merge(JBossConvergedSipMetaData override, SipMetaData original,
         String overrideFile, String overridenFile, boolean mustOverride)
    {
      super.merge(override, original);

      if(override != null && override.getDistributable()!= null)
          setDistributable(override.getDistributable());
       else if(original != null && original.getDistributable() != null)
          setDistributable(original.getDistributable());
      
      if(override != null && override.isMetadataComplete() != false)
          setMetadataComplete(override.isMetadataComplete());
       else if(original != null && (original instanceof Sip11MetaData) )
       {
          Sip11MetaData sip11MD = (Sip11MetaData) original;
          setMetadataComplete(sip11MD.isMetadataComplete());
       } 
       else if(original != null && (original instanceof JBossSip11MetaData) )
       {
    	   JBossSip11MetaData sip11MD = (JBossSip11MetaData) original;
           setMetadataComplete(sip11MD.isMetadataComplete());
        }
      
      if(override != null && override.getSipContextParams()!= null)
          setSipContextParams(override.getSipContextParams());
       else if(original != null && original.getContextParams() != null)
          setSipContextParams(original.getContextParams());       
      
      if(override != null && override.getServletVersion()!= null)
          setServletVersion(override.getServletVersion());
       else if(original != null && original.getVersion() != null)
          setServletVersion(original.getVersion());

       if(override != null && override.getSipSessionConfig()!= null)
          setSipSessionConfig(override.getSipSessionConfig());
       else if(original != null && original.getSipSessionConfig() != null) {
    	   SessionConfigMetaData sessionConfigMetaData = new SessionConfigMetaData();
    	   sessionConfigMetaData.setSessionTimeout(original.getSipSessionConfig().getSessionTimeout());
    	   setSipSessionConfig(sessionConfigMetaData); 
       }
       
       if(override != null && override.getSessionConfig()!= null)
           setSessionConfig(override.getSessionConfig());        
       
       if(override != null && override.getFilters()!= null)
          setFilters(override.getFilters());
       
       if(override != null && override.getFilterMappings()!= null)
          setFilterMappings(override.getFilterMappings());
       
       if(override != null && override.getErrorPages()!= null)
          setErrorPages(override.getErrorPages());
       
       if(override != null && override.getJspConfig()!= null)
          setJspConfig(override.getJspConfig());       
       
       if(override != null && override.getLoginConfig()!= null)
          setLoginConfig(override.getLoginConfig());
       
       if(override != null && override.getMimeMappings()!= null)
          setMimeMappings(override.getMimeMappings());
       
       if(override != null && override.getServletMappings()!= null)
          setServletMappings(override.getServletMappings());
       
       if(override != null && override.getSecurityContraints()!= null)
          setSecurityContraints(override.getSecurityContraints());
       
       if(override != null && override.getWelcomeFileList()!= null)
          setWelcomeFileList(override.getWelcomeFileList());
       
       if(override != null && override.getLocalEncodings()!= null)
          setLocalEncodings(override.getLocalEncodings());
       
       if(override != null && override.isJaccAllStoreRole())
          setJaccAllStoreRole(override.isJaccAllStoreRole());
       
       if(override != null && override.getVersion()!= null)
          setVersion(override.getVersion());
       else if(original != null && original.getVersion() != null)
          setVersion(original.getVersion());
       
       if(override != null && override.getContextRoot()!= null)
          setContextRoot(override.getContextRoot());
       
       if(override != null && override.getAlternativeDD()!= null)
          setAlternativeDD(override.getAlternativeDD());
       
       if(override != null && override.getSecurityDomain()!= null)
          setSecurityDomain(override.getSecurityDomain());
       
       if(override != null && override.getJaccContextID()!= null)
          setJaccContextID(override.getJaccContextID());
       
       if(override != null && override.getClassLoading()!= null)
          setClassLoading(override.getClassLoading());
       
       if(override != null && override.getDepends()!= null)
          setDepends(override.getDepends());
       
       if(override != null && override.getRunAsIdentity()!= null)
          setRunAsIdentity(override.getRunAsIdentity());

       if(getSipSecurityRoles() == null)
          setSipSecurityRoles(new SecurityRolesMetaData());
       SecurityRolesMetaData overrideRoles = null;
       SecurityRolesMetaData originalRoles = null;
       if(override != null)
          overrideRoles = override.getSipSecurityRoles();
       if(original != null)
          originalRoles = original.getSecurityRoles();
       getSipSecurityRoles().merge(overrideRoles, originalRoles);

       MessageDestinationsMetaData overrideMsgDests = null;
       MessageDestinationsMetaData originalMsgDests = null;
       if(override != null && override.getSipMessageDestinations()!= null)
          overrideMsgDests = override.getSipMessageDestinations();
       if(original != null && original.getMessageDestinations() != null)
          originalMsgDests = original.getMessageDestinations();
       setSipMessageDestinations(MessageDestinationsMetaData.merge(overrideMsgDests,
             originalMsgDests, overridenFile, overrideFile));

       if(this.getJndiEnvironmentRefsGroup() == null)
    	   setJndiEnvironmentRefsGroup(new JBossEnvironmentRefsGroupMetaData());
        Environment env = null;
        JBossEnvironmentRefsGroupMetaData jenv = null;
        if( override != null )
           jenv = (JBossEnvironmentRefsGroupMetaData) override.getJndiEnvironmentRefsGroup();
        if(original != null)
           env = original.getJndiEnvironmentRefsGroup();
        ((JBossEnvironmentRefsGroupMetaData)getJndiEnvironmentRefsGroup()).merge(jenv, env, null, overrideFile, overridenFile, mustOverride);
        
       if(override != null && override.getVirtualHosts()!= null)
          setVirtualHosts(override.getVirtualHosts());
       
       if(override != null && override.isFlushOnSessionInvalidation())
          setFlushOnSessionInvalidation(override.isFlushOnSessionInvalidation());
       
       if(override != null && override.isUseSessionCookies())
          setUseSessionCookies(override.isUseSessionCookies());
       
       if(override != null && override.getReplicationConfig()!= null)
          setReplicationConfig(override.getReplicationConfig());
       
       if(override != null && override.getPassivationConfig()!= null)
          setPassivationConfig(override.getPassivationConfig());
       
       if(override != null && override.getWebserviceDescriptions()!= null)
          setWebserviceDescriptions(override.getWebserviceDescriptions());
       
       if(override != null && override.getArbitraryMetadata()!= null)
          setArbitraryMetadata(override.getArbitraryMetadata());
       
       if(override != null && override.getMaxActiveSessions() != null)
          setMaxActiveSessions(override.getMaxActiveSessions());
       
       if(override != null && override.getSessionCookies() != -1)
          setSessionCookies(override.getSessionCookies());       
      
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
       else if(original != null && original.getSipSessionConfig() != null)
          setSipSessionConfig(original.getSipSessionConfig());
      
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
      
      List<ListenerMetaData> mergedListeners = new ArrayList<ListenerMetaData>();
      if(override != null && override.sipListeners!= null)
          //setSipListeners(override.sipListeners);
    	  mergedListeners.addAll(override.sipListeners);
      if(original != null && original.getListeners() != null)
    	  mergedListeners.addAll(original.getListeners());
      this.mergeSipListeners(mergedListeners);
      
      JBossServletsMetaData soverride = null;
      ServletsMetaData soriginal = null;
      if(override != null)
         soverride = override.getSipServlets();
      if(original != null) {
    	  if(original instanceof Sip11MetaData) {
    		  soriginal = ((Sip11MetaData)original).getServlets();
    	  }
    	  if(original instanceof JBossSip11MetaData) {
    		  soriginal = ((JBossSip11MetaData)original).getServlets();
    	  }
      }
      sipServlets = JBossServletsMetaData.merge(soverride, soriginal);


//      if(sipContextParams != null) {
//	      List<ParamValueMetaData> mergedContextParams = new ArrayList<ParamValueMetaData>(sipContextParams);
//	      if(override != null && override.getContextParams() != null) {
//	    	  mergedContextParams.addAll(override.getContextParams());
//	      }
//	      setContextParams(mergedContextParams);
//      }      
      
      if(override != null && override.getSipApplicationKeyMethod()!= null)
          setSipApplicationKeyMethod(override.getSipApplicationKeyMethod());
       else if(original != null && original.getSipApplicationKeyMethod() != null)
          setSipApplicationKeyMethod(original.getSipApplicationKeyMethod());
      
      if(override != null && override.getConcurrencyControlMode()!= null)
          setConcurrencyControlMode(override.getConcurrencyControlMode());
       else if(original != null && original.getConcurrencyControlMode() != null)
          setConcurrencyControlMode(original.getConcurrencyControlMode());
      
      //listeners should not be merged because they have a special treatment when loading the context
      //baranowb: once again, original code merges them few lines above...
   // Update run-as indentity for a run-as-principal
      if(sipServlets != null)
      {
         for(JBossServletMetaData servlet : sipServlets)
         {
            String servletName = servlet.getServletName();
            String principalName = servlet.getRunAsPrincipal();
            // Get the sip.xml run-as primary role
            String sipXmlRunAs = null;
            if(servlet.getRunAs() != null)
               sipXmlRunAs = servlet.getRunAs().getRoleName();
            if (principalName != null)
            {
               // Update the run-as indentity to use the principal name
               if (sipXmlRunAs == null)
               {
                  //Needs to be merged from Annotations
                  sipXmlRunAs = "PLACEHOLDER_FOR_ANNOTATION";
                  //throw new IllegalStateException("run-as-principal: " + principalName + " found in jboss-web.xml but there was no run-as in web.xml");
               }
               // See if there are any additional roles for this principal
               Set<String> extraRoles = getSecurityRoles().getSecurityRoleNamesByPrincipal(principalName);
               RunAsIdentityMetaData runAsId = new RunAsIdentityMetaData(sipXmlRunAs, principalName, extraRoles);
               getRunAsIdentity().put(servletName, runAsId);
            }
            else if (sipXmlRunAs != null)
            {
               RunAsIdentityMetaData runAsId = new RunAsIdentityMetaData(sipXmlRunAs, null);
               getRunAsIdentity().put(servletName, runAsId);
            }
         }
      }
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
	public void setSipContextParams(List<? extends ParamValueMetaData> sipContextParams) {
		this.sipContextParams = sipContextParams;
	}
	/**
	 * @return the sipContextParams
	 */
	public List<? extends ParamValueMetaData> getSipContextParams() {
		return sipContextParams;
	}
	/**
	 * @param sipListeners the sipListeners to set
	 */
	public void setSipListeners(Set<ListenerMetaData> sipListeners) {
		this.sipListeners = sipListeners;
	}
	/**
	 * @return the sipListeners
	 */
	public Set<ListenerMetaData> getSipListeners() {
		return sipListeners;
	}
	/**
	 * @param sipServlets the sipServlets to set
	 */
	public void setSipServlets(JBossServletsMetaData sipServlets) {
		this.sipServlets = sipServlets;
	}
	/**
	 * @return the sipServlets
	 */
	public JBossServletsMetaData getSipServlets() {
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
	
	/**
	 * @param sipApplicationKeyMethod the sipApplicationKeyMethod to set
	 */
	public void setSipApplicationKeyMethod(Method sipApplicationKeyMethod) {
		this.sipApplicationKeyMethod = sipApplicationKeyMethod;
	}
	/**
	 * @return the sipApplicationKeyMethod
	 */
	public Method getSipApplicationKeyMethod() {
		return sipApplicationKeyMethod;
	}
	/**
	 * @param messageDestinations the messageDestinations to set
	 */
	public void setSipMessageDestinations(MessageDestinationsMetaData messageDestinations) {
		this.messageDestinations = messageDestinations;
	}
	/**
	 * @return the messagesDestinations
	 */
	public MessageDestinationsMetaData getSipMessageDestinations() {
		return messageDestinations;
	}
	/**
	 * @param securityRoles the securityRoles to set
	 */
	public void setSipSecurityRoles(SecurityRolesMetaData securityRoles) {
		this.securityRoles = securityRoles;
	}
	/**
	 * @return the securityRoles
	 */
	public SecurityRolesMetaData getSipSecurityRoles() {
		return securityRoles;
	}
	/**
	 * @param concurrencyControlMode the concurrencyControlMode to set
	 */
	public void setConcurrencyControlMode(ConcurrencyControlMode ConcurrencyControlMode) {
		this.concurrencyControlMode = ConcurrencyControlMode;
	}
	/**
	 * @return the concurrencyControlMode
	 */
	public ConcurrencyControlMode getConcurrencyControlMode() {
		return concurrencyControlMode;
	}	
	
    protected void mergeSipListeners(List<ListenerMetaData> lst) {

	    //precaution, silent return?
		if(lst == null)
		{
			return;
		}
		
		if(this.sipListeners == null)
		{
			//NOTE: make separate copy ?
			this.sipListeners = new HashSet<ListenerMetaData>(lst);
		}else
		{
			this.sipListeners.addAll(lst);
		}

}
}
