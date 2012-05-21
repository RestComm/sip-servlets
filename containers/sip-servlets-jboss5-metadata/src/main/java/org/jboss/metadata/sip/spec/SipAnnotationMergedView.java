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

package org.jboss.metadata.sip.spec;

import java.util.HashMap;

import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentRefsGroupMetaData;
import org.jboss.metadata.sip.spec.MessageDestinationsMetaData;
import org.jboss.metadata.sip.spec.SecurityRolesMetaData;
import org.jboss.metadata.javaee.support.AbstractMappedMetaData;
import org.jboss.metadata.javaee.support.IdMetaDataImpl;

/**
 * Create a merged SipMetaData view from an xml + annotation views
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipAnnotationMergedView {

	public static void merge(SipMetaData merged, SipMetaData xml, SipMetaData annotation)
	   {
	      //Merge the servlets meta data
		ServletsMetaData servletsMetaData = new ServletsMetaData();
		merge(servletsMetaData, xml.getServlets(), annotation.getServlets());
		merged.setServlets(servletsMetaData);  
	      
	      //Security Roles
	      SecurityRolesMetaData securityRolesMetaData = new SecurityRolesMetaData();
	      merge(securityRolesMetaData, xml.getSecurityRoles(), annotation.getSecurityRoles());
	      merged.setSecurityRoles(securityRolesMetaData);
	      
	      //Env
	      EnvironmentRefsGroupMetaData environmentRefsGroup = new EnvironmentRefsGroupMetaData();
	      Environment xmlEnv = xml != null ? xml.getJndiEnvironmentRefsGroup() : null;
	      Environment annEnv = annotation != null ? annotation.getJndiEnvironmentRefsGroup() : null;
	      environmentRefsGroup.merge(xmlEnv,annEnv, "", "", false);
	      merged.setJndiEnvironmentRefsGroup(environmentRefsGroup);
	      
	      //Message Destinations
	      MessageDestinationsMetaData messageDestinations = new MessageDestinationsMetaData();
	      messageDestinations.merge(xml.getMessageDestinations(), annotation.getMessageDestinations());
	      merged.setMessageDestinations(messageDestinations);
	      
	      //merge annotation
	      mergeIn(merged,annotation);
	      //merge xml override
	      mergeIn(merged,xml);
	   }
	   
	   private static void merge(ServletsMetaData merged, ServletsMetaData xml,
	         ServletsMetaData annotation)
	   {
	      HashMap<String,String> servletClassToName = new HashMap<String,String>();
	      if(xml != null)
	      {
	         if(((IdMetaDataImpl)xml).getId() != null)
	        	 ((IdMetaDataImpl)merged).setId(((IdMetaDataImpl)xml).getId());
	         for(ServletMetaData servlet : ((AbstractMappedMetaData<ServletMetaData>)xml))
	         {
	            String className = servlet.getServletName();
	            if(className != null)
	            {
	               // Use the unqualified name
	               int dot = className.lastIndexOf('.');
	               if(dot >= 0)
	                  className = className.substring(dot+1);
	               servletClassToName.put(className, servlet.getServletName()); 
	            }
	         }         
	      }
	      
	      // First get the annotation beans without an xml entry
	      if(annotation != null)
	      {
	         for(ServletMetaData servlet : ((AbstractMappedMetaData<ServletMetaData>)annotation))
	         {
	            if(xml != null)
	            {
	               // This is either the servlet-name or the servlet-class simple name
	               String servletName = servlet.getServletName();
	               ServletMetaData match = ((AbstractMappedMetaData<ServletMetaData>)xml).get(servletName);
	               if(match == null)
	               {
	                  // Lookup by the unqualified servlet class
	                  String xmlServletName = servletClassToName.get(servletName);
	                  if(xmlServletName == null)
	                	  ((AbstractMappedMetaData<ServletMetaData>)merged).add(servlet);
	               }
	            }
	            else
	            {
	            	((AbstractMappedMetaData<ServletMetaData>)merged).add(servlet);
	            }
	         }
	      }
	      // Now merge the xml and annotations
	      if(xml != null)
	      {
	         for(ServletMetaData servlet : ((AbstractMappedMetaData<ServletMetaData>)xml))
	         {
	            ServletMetaData annServlet = null;
	            if(annotation != null)
	            {
	               String name = servlet.getServletName();
	               annServlet = ((AbstractMappedMetaData<ServletMetaData>)annotation).get(name);
	               if(annServlet == null)
	               {
	                  // Lookup by the unqualified servlet class
	                  String className = servlet.getServletClass();
	                  if(className != null)
	                  {
	                     // Use the unqualified name
	                     int dot = className.lastIndexOf('.');
	                     if(dot >= 0)
	                        className = className.substring(dot+1);
	                     annServlet = ((AbstractMappedMetaData<ServletMetaData>)annotation).get(className);
	                  }
	               }
	            }
	            // Merge
	            ServletMetaData mergedServletMetaData = servlet;
	            if(annServlet != null)
	            {
	               mergedServletMetaData = new ServletMetaData();
	               mergedServletMetaData.merge(servlet, annServlet);
	            }
	            ((AbstractMappedMetaData<ServletMetaData>)merged).add(mergedServletMetaData);
	         }
	      } 
	   }
	   
	   private static void merge(SecurityRolesMetaData merged, SecurityRolesMetaData xml,
	         SecurityRolesMetaData annotation)
	   {
	      merged.merge(xml, annotation); 
	   }
	   /**
	    * This method mimics MetaData.merge(override, value) in two step way. It works like:<br>
	    * merged.setX(override.getX), more or less, in distinction to original method of MetaData.
	    * 
	    * 
	    * @param merged
	    * @param override
	    */
	   private static void mergeIn(SipMetaData merged, SipMetaData override)
	   {
	      merged.setDTD("", override.getDtdPublicId(), override.getDtdSystemId());
	      
	      //FIXME: check if we can extend "merge" method to mvoe this code.
	      //Sip Specifics
	    
	      if(override.getApplicationName() != null)
		         merged.setApplicationName(override.getApplicationName());
	      
	      if(override.getServletSelection() != null)
		         merged.setServletSelection(override.getServletSelection());
	      
	      if(override.getSipApplicationKeyMethod() != null)
		         merged.setSipApplicationKeyMethod(override.getSipApplicationKeyMethod());
	      
	      if(override.getConcurrencyControlMode() != null)
		         merged.setConcurrencyControlMode(override.getConcurrencyControlMode());
	      
	      //Web Specifics
	      
	      //Version
	      if(override.getVersion() != null)
	         merged.setVersion(override.getVersion());
	      
	      //Description Group
	      if(override.getDescriptionGroup() != null)
	         merged.setDescriptionGroup(override.getDescriptionGroup());

	      //Merge the Params
	      if(override.getContextParams() != null)
	         //merged.mergeContextParameters(override.getContextParams());
	    	  merged.setContextParams(override.getContextParams());
	      
	      //Distributable
	      if(override.getDistributable() != null)
	         merged.setDistributable(override.getDistributable());
	      
	      //Session Config
	      if(override.getSipSessionConfig() != null)
	         merged.setSipSessionConfig(override.getSipSessionConfig());
	      
	      //Listener meta data
	      if(override.getListeners() != null)
	         merged.mergeListeners(override.getListeners());
	      
	      //Login Config
	      if(override.getSipLoginConfig() != null)
	         merged.setSipLoginConfig(override.getSipLoginConfig());
	      
	      //Security Constraints
	      if(override.getSipSecurityContraints() != null)
	         //merged.mergeSipSecurityContraints(override.getSipSecurityContraints());
	    	  merged.setSipSecurityContraints(override.getSipSecurityContraints());
	      
	      //Local Encodings
	      if(override.getLocalEncodings() != null)
	         merged.setLocalEncodings(override.getLocalEncodings());
	   }
}
