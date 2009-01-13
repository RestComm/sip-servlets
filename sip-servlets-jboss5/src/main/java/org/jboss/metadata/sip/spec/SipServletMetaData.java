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
package org.jboss.metadata.sip.spec;

import java.util.List;

import org.jboss.metadata.javaee.spec.RunAsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.javaee.support.MergeableMetaData;
import org.jboss.metadata.javaee.support.NamedMetaDataWithDescriptionGroup;

/**
 * sip-app/servlet metadata
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 75201 $
 */
public abstract class SipServletMetaData extends NamedMetaDataWithDescriptionGroup
   implements MergeableMetaData<SipServletMetaData>
{   
	
	private static final int loadOnStartupDefault = -1;
	
   public abstract String getServletName();
   public abstract void setServletName(String servletName);
   public abstract String getServletClass();
   public abstract void setServletClass(String servletClass);
   public abstract List<? extends ParamValueMetaData> getInitParam();
   public abstract void setInitParam(List<? extends ParamValueMetaData> initParam);
   public abstract String getJspFile();
   public abstract void setJspFile(String jspFile);
   public abstract int getLoadOnStartup();
   public abstract void setLoadOnStartup(int loadOnStartup);
   public abstract RunAsMetaData getRunAs();
   public abstract void setRunAs(RunAsMetaData runAs);
   public abstract SecurityRoleRefsMetaData getSecurityRoleRefs();
   public abstract void setSecurityRoleRefs(SecurityRoleRefsMetaData securityRoleRefs);
   public abstract SipServletMetaData merge(SipServletMetaData original);
   
   public void merge(SipServletMetaData override, SipServletMetaData original)
   {
      super.merge(override, original);
      if(override != null && override.getServletName() != null)
          setServletName(override.getServletName());
       else if(original != null && original.getServletName() != null)
          setServletName(original.getServletName());
      if(override != null && override.getServletClass() != null)
         setServletClass(override.getServletClass());
      else if(original != null && original.getServletClass() != null)
         setServletClass(original.getServletClass());
      if(override != null && override.getJspFile() != null)
         setJspFile(override.getJspFile());
      else if(original != null && original.getJspFile() != null)
         setJspFile(original.getJspFile());
      if(override != null && override.getInitParam() != null)
         setInitParam(override.getInitParam());
      else if(original != null && original.getInitParam() != null)
         setInitParam(original.getInitParam());
      if(override != null && override.getLoadOnStartup() != loadOnStartupDefault)
         setLoadOnStartup(override.getLoadOnStartup());
      else if(original != null && original.getLoadOnStartup() != loadOnStartupDefault)
         setLoadOnStartup(original.getLoadOnStartup());
      if(override != null && override.getRunAs() != null)
         setRunAs(override.getRunAs());
      else if(original != null && original.getRunAs() != null)
         setRunAs(original.getRunAs());
      if(override != null && override.getSecurityRoleRefs() != null)
         setSecurityRoleRefs(override.getSecurityRoleRefs());
      else if(original != null && original.getSecurityRoleRefs() != null)
         setSecurityRoleRefs(original.getSecurityRoleRefs());
   }

   public String toString()
   {
      StringBuilder tmp = new StringBuilder("ServletMetaData(id=");
      tmp.append(getId());
      tmp.append(",servletClass=");
      tmp.append(getServletClass());
      tmp.append(",jspFile=");
      tmp.append(getJspFile());
      tmp.append(",loadOnStartup=");
      tmp.append(getLoadOnStartup());
      tmp.append(",runAs=");
      tmp.append(getRunAs());
      tmp.append(')');
      return tmp.toString();
   }
}
