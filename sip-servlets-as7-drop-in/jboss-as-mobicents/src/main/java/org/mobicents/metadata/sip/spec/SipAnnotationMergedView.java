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

package org.mobicents.metadata.sip.spec;

import java.util.HashMap;

import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.javaee.spec.EnvironmentRefsGroupMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.javaee.support.AbstractMappedMetaData;
import org.jboss.metadata.javaee.support.IdMetaDataImpl;
import org.jboss.metadata.merge.web.spec.ServletMetaDataMerger;
import org.jboss.metadata.web.spec.ServletMetaData;

/**
 * Create a merged SipMetaData view from an xml + annotation views
 *
 * @author jean.deruelle@gmail.com
 * FIXME: josemrecio - update using org.jboss.metadata.merge.web.spec.AnnotationMergedViewMerger as template
 */
public class SipAnnotationMergedView {

    public static void merge(SipMetaData merged, SipMetaData xml, SipMetaData annotation) {
        // Merge the servlets meta data
        SipServletsMetaData servletsMetaData = new SipServletsMetaData();
        merge(servletsMetaData, xml.getSipServlets(), annotation.getSipServlets());
        merged.setSipServlets(servletsMetaData);

        // Security Roles
        SecurityRolesMetaData securityRolesMetaData = new SecurityRolesMetaData();
        merge(securityRolesMetaData, xml.getSecurityRoles(), annotation.getSecurityRoles());
        merged.setSecurityRoles(securityRolesMetaData);

        // Env
        EnvironmentRefsGroupMetaData environmentRefsGroup = new EnvironmentRefsGroupMetaData();
        Environment xmlEnv = xml != null ? xml.getJndiEnvironmentRefsGroup() : null;
        Environment annEnv = annotation != null ? annotation.getJndiEnvironmentRefsGroup() : null;
        /*
         * FIXME: merge environmentRefsGroup.merge(xmlEnv,annEnv, "", "", false);
         * merged.setJndiEnvironmentRefsGroup(environmentRefsGroup);
         */
        if (merged.getJndiEnvironmentRefsGroup() == null) {
            merged.setJndiEnvironmentRefsGroup(annotation.getJndiEnvironmentRefsGroup());
        }

        // Message Destinations
        /*
         * FIXME: merge MessageDestinationsMetaData messageDestinations = new MessageDestinationsMetaData();
         * messageDestinations.merge(xml.getMessageDestinations(), annotation.getMessageDestinations());
         * merged.setMessageDestinations(messageDestinations);
         */
        if (merged.getMessageDestinations() == null) {
            merged.setMessageDestinations(annotation.getMessageDestinations());
        }

        // merge annotation
        mergeIn(merged, annotation);
        // merge xml override
        mergeIn(merged, xml);
    }

    private static void merge(SipServletsMetaData merged, SipServletsMetaData xml, SipServletsMetaData annotation) {
        HashMap<String, String> servletClassToName = new HashMap<String, String>();
        if (xml != null) {
            if (((IdMetaDataImpl) xml).getId() != null)
                ((IdMetaDataImpl) merged).setId(((IdMetaDataImpl) xml).getId());
            for (ServletMetaData servlet : ((AbstractMappedMetaData<ServletMetaData>) xml)) {
                String className = servlet.getServletName();
                if (className != null) {
                    // Use the unqualified name
                    int dot = className.lastIndexOf('.');
                    if (dot >= 0)
                        className = className.substring(dot + 1);
                    servletClassToName.put(className, servlet.getServletName());
                }
            }
        }

        // First get the annotation beans without an xml entry
        if (annotation != null) {
            for (ServletMetaData servlet : ((AbstractMappedMetaData<ServletMetaData>) annotation)) {
                if (xml != null) {
                    // This is either the servlet-name or the servlet-class simple name
                    String servletName = servlet.getServletName();
                    ServletMetaData match = ((AbstractMappedMetaData<ServletMetaData>) xml).get(servletName);
                    if (match == null) {
                        // Lookup by the unqualified servlet class
                        String xmlServletName = servletClassToName.get(servletName);
                        if (xmlServletName == null)
                            ((AbstractMappedMetaData<ServletMetaData>) merged).add(servlet);
                    }
                } else {
                    ((AbstractMappedMetaData<ServletMetaData>) merged).add(servlet);
                }
            }
        }
        // Now merge the xml and annotations
        if (xml != null) {
            for (ServletMetaData servlet : ((AbstractMappedMetaData<ServletMetaData>) xml)) {
                ServletMetaData annServlet = null;
                if (annotation != null) {
                    String name = servlet.getServletName();
                    annServlet = ((AbstractMappedMetaData<ServletMetaData>) annotation).get(name);
                    if (annServlet == null) {
                        // Lookup by the unqualified servlet class
                        String className = servlet.getServletClass();
                        if (className != null) {
                            // Use the unqualified name
                            int dot = className.lastIndexOf('.');
                            if (dot >= 0)
                                className = className.substring(dot + 1);
                            annServlet = ((AbstractMappedMetaData<ServletMetaData>) annotation).get(className);
                        }
                    }
                }
                // Merge
                ServletMetaData mergedServletMetaData = servlet;
                if (annServlet != null) {
                    mergedServletMetaData = new ServletMetaData();
                    ServletMetaDataMerger.merge(mergedServletMetaData, annServlet, servlet);
                    // mergedServletMetaData.merge(servlet, annServlet);
                }
                ((AbstractMappedMetaData<ServletMetaData>) merged).add(mergedServletMetaData);
            }
        }
    }

    private static void merge(SecurityRolesMetaData merged, SecurityRolesMetaData xml, SecurityRolesMetaData annotation) {
        // FIXME: merge merged.merge(xml, annotation);
    }

    private static void mergeIn(SipMetaData merged, SipMetaData xml) {
        merged.setDTD("", xml.getDtdPublicId(), xml.getDtdSystemId());

        // Sip Specifics

        if (xml.getApplicationName() != null)
            merged.setApplicationName(xml.getApplicationName());

        if (xml.getServletSelection() != null)
            merged.setServletSelection(xml.getServletSelection());

        if (xml.getSipApplicationKeyMethod() != null)
            merged.setSipApplicationKeyMethod(xml.getSipApplicationKeyMethod());

        if (xml.getConcurrencyControlMode() != null)
            merged.setConcurrencyControlMode(xml.getConcurrencyControlMode());

        // Web Specifics

        // Version
        if (xml.getVersion() != null)
            merged.setVersion(xml.getVersion());

        // Description Group
        if (xml.getDescriptionGroup() != null)
            merged.setDescriptionGroup(xml.getDescriptionGroup());

        // Merge the Params
        if (xml.getContextParams() != null)
            merged.setContextParams(xml.getContextParams());

        // Distributable
        if (xml.getDistributable() != null)
            merged.setDistributable(xml.getDistributable());

        // Session Config
        if (xml.getSessionConfig() != null)
            merged.setSessionConfig(xml.getSessionConfig());

        // Listener meta data
        if (xml.getListeners() != null)
            merged.setListeners(xml.getListeners());

        // Login Config
        if (xml.getSipLoginConfig() != null)
            merged.setSipLoginConfig(xml.getSipLoginConfig());

        // Security Constraints
        if (xml.getSipSecurityConstraints() != null)
            merged.setSipSecurityConstraints(xml.getSipSecurityConstraints());

        // Local Encodings
        if (xml.getLocalEncodings() != null)
            merged.setLocalEncodings(xml.getLocalEncodings());
    }
}
