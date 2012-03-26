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
package org.mobicents.metadata.sip.merge;

import org.jboss.metadata.javaee.spec.MessageDestinationsMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.merge.javaee.spec.MessageDestinationsMetaDataMerger;
import org.jboss.metadata.merge.javaee.spec.SecurityRolesMetaDataMerger;
import org.jboss.metadata.merge.javaee.support.NamedModuleImplMerger;
import org.mobicents.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.mobicents.metadata.sip.jboss.JBossSipServletsMetaData;
import org.mobicents.metadata.sip.spec.Sip11MetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;

/**
 * The combined web.xml/jboss-web.xml metadata
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 84989 $
 *
 * @author josemrecio
 *
 */
public class JBossSipMetaDataMerger extends NamedModuleImplMerger {

    public static void merge(JBossConvergedSipMetaData dest, JBossConvergedSipMetaData override, SipMetaData original) {
        merge(dest, override, original, null, "sip.xml", false);
    }

    public static void merge(JBossConvergedSipMetaData dest, JBossConvergedSipMetaData override, SipMetaData original, String overrideFile, String overridenFile,
            boolean mustOverride) {

        NamedModuleImplMerger.merge(dest, override, original);

        if (override != null && override.getApplicationName() != null)
            dest.setApplicationName(override.getApplicationName());
        else if (original != null && original.getApplicationName() != null)
            dest.setApplicationName(original.getApplicationName());

        if (override != null && override.getSipServletSelection() != null)
            dest.setSipServletSelection(override.getSipServletSelection());
        else if (original != null && original.getServletSelection() != null)
            dest.setSipServletSelection(original.getServletSelection());

        if (override != null && override.getProxyConfig() != null)
            dest.setProxyConfig(override.getProxyConfig());
        else if (original != null && original.getProxyConfig() != null)
            dest.setProxyConfig(original.getProxyConfig());

        if (override != null && override.getSipSecurityConstraints() != null)
            dest.setSipSecurityConstraints(override.getSipSecurityConstraints());
        else if (original != null && original.getSipSecurityConstraints() != null)
            dest.setSipSecurityConstraints(original.getSipSecurityConstraints());

        if (override != null && override.getSipSessionConfig() != null)
            dest.setSipSessionConfig(override.getSipSessionConfig());
        else if (original != null && original.getSessionConfig() != null)
            dest.setSipSessionConfig(original.getSessionConfig());

        if (override != null && override.getSipLoginConfig() != null)
            dest.setSipLoginConfig(override.getSipLoginConfig());
        else if (original != null && original.getSipLoginConfig() != null)
            dest.setSipLoginConfig(original.getSipLoginConfig());

        if (override != null && override.getSipContextParams() != null)
            dest.setSipContextParams(override.getSipContextParams());
        else if (original != null && original.getContextParams() != null)
            dest.setSipContextParams(original.getContextParams());

        if (override != null && override.getSipListeners() != null)
            dest.setSipListeners(override.getSipListeners());
        else if (original != null && original.getListeners() != null)
            dest.setSipListeners(original.getListeners());

        // FIXME: josemrecio - merge SipServlets data, complete JBossSipServletsMetaDataMerger.merge()
        JBossSipServletsMetaData soverride = null;
        SipServletsMetaData soriginal = null;
        if (override != null)
            soverride = override.getSipServlets();
        if (original != null)
            soriginal = original.getSipServlets();
        dest.setSipServlets(JBossSipServletsMetaDataMerger.merge(soverride, soriginal));

        MessageDestinationsMetaData overrideMsgDests = null;
        MessageDestinationsMetaData originalMsgDests = null;
        if (override != null && override.getSipMessageDestinations() != null)
            overrideMsgDests = override.getSipMessageDestinations();
        if (original != null && original.getMessageDestinations() != null)
            originalMsgDests = original.getMessageDestinations();
        dest.setSipMessageDestinations(MessageDestinationsMetaDataMerger.merge(overrideMsgDests, originalMsgDests, overridenFile,
                overrideFile));

        if (dest.getSipSecurityRoles() == null)
            dest.setSipSecurityRoles(new SecurityRolesMetaData());
        SecurityRolesMetaData overrideRoles = null;
        SecurityRolesMetaData originalRoles = null;
        if (override != null)
            overrideRoles = override.getSipSecurityRoles();
        if (original != null)
            originalRoles = original.getSecurityRoles();
        SecurityRolesMetaDataMerger.merge(dest.getSipSecurityRoles(), overrideRoles, originalRoles);

        if (override != null && override.getSipApplicationKeyMethod() != null)
            dest.setSipApplicationKeyMethod(override.getSipApplicationKeyMethod());
        else if (original != null && original.getSipApplicationKeyMethod() != null)
            dest.setSipApplicationKeyMethod(original.getSipApplicationKeyMethod());

        if (override != null && override.getConcurrencyControlMode() != null)
            dest.setConcurrencyControlMode(override.getConcurrencyControlMode());
        else if (original != null && original.getConcurrencyControlMode() != null)
            dest.setConcurrencyControlMode(original.getConcurrencyControlMode());

        if (override != null && override.isMetadataComplete() != false)
            dest.setMetadataComplete(override.isMetadataComplete());
        if (original instanceof Sip11MetaData) {
            //if either original or override is MD complete the result is MD complete
            Sip11MetaData sip11MD = (Sip11MetaData) original;
            dest.setMetadataComplete(sip11MD.isMetadataComplete());
        }

   }
}