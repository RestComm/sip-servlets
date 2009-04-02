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

import javax.xml.bind.annotation.XmlElement;

import org.jboss.metadata.javaee.spec.RunAsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.javaee.support.MergeableMetaData;
import org.jboss.metadata.javaee.support.NamedMetaDataWithDescriptionGroup;
import org.jboss.xb.annotations.JBossXmlNsPrefix;

/**
 * sip-app/servlet metadata
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 75201 $
 */
public class ServletMetaData extends
		NamedMetaDataWithDescriptionGroup implements
		MergeableMetaData<ServletMetaData> {

	private static final long serialVersionUID = 1;

	private static final int loadOnStartupDefault = -1;

	private String servletName;
	private String servletClass;
	private String jspFile;
	/** The servlet init-params */
	private List<ParamValueMetaData> initParam;
	private int loadOnStartup = loadOnStartupDefault;
	private RunAsMetaData runAs;
	/** The security role ref */
	private SecurityRoleRefsMetaData securityRoleRefs;

	public String getServletName() {
		return servletName;
	}

	@XmlElement(name = "servlet-name")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setServletName(String servletName) {
		this.servletName = servletName;
		super.setName(servletName);
	}

	public String getServletClass() {
		return servletClass;
	}

	@XmlElement(name = "servlet-class")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setServletClass(String servletClass) {
		this.servletClass = servletClass;
	}

	public List<ParamValueMetaData> getInitParam() {
		return initParam;
	}

	@XmlElement(name = "init-param")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setInitParam(List<ParamValueMetaData> initParam) {
		this.initParam = initParam;
	}

	public String getJspFile() {
		return jspFile;
	}

	@XmlElement(name = "jsp-file")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setJspFile(String jspFile) {
		this.jspFile = jspFile;
	}

	public int getLoadOnStartup() {
		return loadOnStartup;
	}

	@XmlElement(name = "load-on-startup")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setLoadOnStartup(int loadOnStartup) {
		this.loadOnStartup = loadOnStartup;
	}

	public RunAsMetaData getRunAs() {
		return runAs;
	}

	@XmlElement(name = "run-as")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setRunAs(RunAsMetaData runAs) {
		this.runAs = runAs;
	}

	public SecurityRoleRefsMetaData getSecurityRoleRefs() {
		return securityRoleRefs;
	}

	@XmlElement(name = "security-role-ref")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setSecurityRoleRefs(SecurityRoleRefsMetaData securityRoleRefs) {
		this.securityRoleRefs = securityRoleRefs;
	}

	public ServletMetaData merge(ServletMetaData original) {
		ServletMetaData merged = new ServletMetaData();
		merged.merge(this, original);
		return merged;
	}

	public void merge(ServletMetaData override, ServletMetaData original) {
		super.merge(override, original);
		if (override != null && override.getServletName() != null)
			setServletName(override.getServletName());
		else if (original != null && original.getServletName() != null)
			setServletName(original.getServletName());
		if (override != null && override.getServletClass() != null)
			setServletClass(override.getServletClass());
		else if (original != null && original.getServletClass() != null)
			setServletClass(original.getServletClass());
		if (override != null && override.getJspFile() != null)
			setJspFile(override.getJspFile());
		else if (original != null && original.getJspFile() != null)
			setJspFile(original.getJspFile());
		if (override != null && override.getInitParam() != null)
			setInitParam(override.getInitParam());
		else if (original != null && original.getInitParam() != null)
			setInitParam(original.getInitParam());
		if (override != null
				&& override.getLoadOnStartup() != loadOnStartupDefault)
			setLoadOnStartup(override.getLoadOnStartup());
		else if (original != null
				&& original.getLoadOnStartup() != loadOnStartupDefault)
			setLoadOnStartup(original.getLoadOnStartup());
		if (override != null && override.getRunAs() != null)
			setRunAs(override.getRunAs());
		else if (original != null && original.getRunAs() != null)
			setRunAs(original.getRunAs());
		if (override != null && override.getSecurityRoleRefs() != null)
			setSecurityRoleRefs(override.getSecurityRoleRefs());
		else if (original != null && original.getSecurityRoleRefs() != null)
			setSecurityRoleRefs(original.getSecurityRoleRefs());
	}

	public String toString() {
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
