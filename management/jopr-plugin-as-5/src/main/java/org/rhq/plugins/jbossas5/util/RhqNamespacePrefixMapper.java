/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.plugins.jbossas5.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * @author Ian Springer
 */
public class RhqNamespacePrefixMapper extends NamespacePrefixMapper {
    public static final String PLUGIN_NAMESPACE = "urn:xmlns:rhq-plugin";
    public static final String CONFIGURATION_NAMESPACE = "urn:xmlns:rhq-configuration";

    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if (namespaceUri.equals(PLUGIN_NAMESPACE)) {
            return "";
        } else if (namespaceUri.equals(CONFIGURATION_NAMESPACE)) {
            return "c";
        } else {
            return null;
        }
    }
}
