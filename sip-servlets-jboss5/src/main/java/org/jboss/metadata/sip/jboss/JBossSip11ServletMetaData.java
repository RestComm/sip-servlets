/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jboss.metadata.sip.spec.ParamValueMetaData;
import org.jboss.metadata.javaee.spec.RunAsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.javaee.support.MergeableMetaData;
import org.jboss.metadata.javaee.support.NamedMetaDataWithDescriptionGroup;
import org.jboss.metadata.sip.spec.Sip11ParamValueMetaData;
import org.jboss.metadata.sip.spec.SipServletMetaData;

/**
 * sip-app/servlet metadata
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 75201 $
 */
public class JBossSip11ServletMetaData extends SipServletMetaData
{
   private static final long serialVersionUID = 1;
   
   private static final int loadOnStartupDefault = -1;

   private String servletName;
   private String servletClass;
   private String jspFile;
   /** The servlet init-params */
   private List<JBossSip11ParamValueMetaData> initParam;
   private int loadOnStartup = loadOnStartupDefault;
   private RunAsMetaData runAs;
   /** The security role ref */
   private SecurityRoleRefsMetaData securityRoleRefs;

   public String getServletName()
   {
      return servletName;
   }
   public void setServletName(String servletName)
   {
      this.servletName = servletName;
      super.setName(servletName);
   }

   public String getServletClass()
   {
      return servletClass;
   }
   public void setServletClass(String servletClass)
   {
      this.servletClass = servletClass;
   }

   public List<JBossSip11ParamValueMetaData> getInitParam()
   {
      return initParam;
   }
   @Override
   @XmlTransient   
   public void setInitParam(List<? extends ParamValueMetaData> initParam)
   {
      this.initParam =  (List<JBossSip11ParamValueMetaData>)initParam;
   }
   @XmlElement(name="init-param")
   public void setJavaeeInitParam(List<JBossSip11ParamValueMetaData> initParam)
   {
      this.initParam =  initParam;
   }
   
   public String getJspFile()
   {
      return jspFile;
   }
   public void setJspFile(String jspFile)
   {
      this.jspFile = jspFile;
   }
   public int getLoadOnStartup()
   {
      return loadOnStartup;
   }
   public void setLoadOnStartup(int loadOnStartup)
   {
      this.loadOnStartup = loadOnStartup;
   }
   public RunAsMetaData getRunAs()
   {
      return runAs;
   }
   public void setRunAs(RunAsMetaData runAs)
   {
      this.runAs = runAs;
   }
   public SecurityRoleRefsMetaData getSecurityRoleRefs()
   {
      return securityRoleRefs;
   }
   public void setSecurityRoleRefs(SecurityRoleRefsMetaData securityRoleRefs)
   {
      this.securityRoleRefs = securityRoleRefs;
   }
   @Override
   public SipServletMetaData merge(SipServletMetaData original) {
	   SipServletMetaData merged = new JBossSip11ServletMetaData();
	   merged.merge(this, original);
	   return merged;
   }   
}
