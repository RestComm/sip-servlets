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

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.metadata.javaee.spec.JavaEEMetaDataConstants;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Reuse the JBoss50WebMetaData class from JBoss 5 to provide support for converged http/sip applications jboss-web metadata parsing
 * 
 * @author jean.deruelle@gmail.com
 */
@XmlRootElement(name="jboss-web", namespace=JavaEEMetaDataConstants.JBOSS_NS)
@JBossXmlSchema(
      xmlns={@XmlNs(namespaceURI = JavaEEMetaDataConstants.JAVAEE_NS, prefix = "jee")},
      ignoreUnresolvedFieldOrClass=false,
      namespace=JavaEEMetaDataConstants.JBOSS_NS,
      elementFormDefault=XmlNsForm.QUALIFIED)
@XmlType(name="jboss-webType", namespace=JavaEEMetaDataConstants.JBOSS_NS, propOrder={"classLoading", "securityDomain",
      "jaccAllStoreRole", "contextRoot",
      "virtualHosts", "useSessionCookies", "replicationConfig", "environmentRefsGroup", "securityRoles", "messageDestinations",
      "webserviceDescriptions", "depends", "servlets", "maxActiveSessions", "passivationConfig"})
public class JBoss50ConvergedSipMetaData extends JBossConvergedSipMetaData
{
   private static final long serialVersionUID = 1;
}
