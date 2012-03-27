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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Sip application spec metadata.
 *
 * @author jean.deruelle@gmail.com
 */
@XmlRootElement(name="sip-app", namespace="http://www.jcp.org/xml/ns/sipservlet")
@JBossXmlSchema(
      xmlns={@XmlNs(namespaceURI = "http://www.jcp.org/xml/ns/sipservlet", prefix = "sipservlet"),
    		  @XmlNs(namespaceURI = "http://java.sun.com/xml/ns/javaee", prefix = "javaee")
      },      
      ignoreUnresolvedFieldOrClass=false,
      namespace="http://www.jcp.org/xml/ns/sipservlet",
      elementFormDefault=XmlNsForm.QUALIFIED,
      normalizeSpace=true,
      strict=false)
@XmlType(name="sip-appType",
      namespace="http://www.jcp.org/xml/ns/sipservlet")
public class Sip11MetaData extends SipMetaData
{
   private static final long serialVersionUID = 1;
	private boolean metadataComplete;

	public boolean isMetadataComplete() {
		return metadataComplete;
	}

	@XmlAttribute(name = "metadata-complete")
	public void setMetadataComplete(boolean metadataComplete) {
		this.metadataComplete = metadataComplete;
	}  
}
