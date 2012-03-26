/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.mobicents.metadata.sip.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.metadata.javaee.spec.MessageDestinationsMetaData;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;
import org.jboss.metadata.parser.ee.MessageDestinationMetaDataParser;
import org.jboss.metadata.parser.ee.ParamValueMetaDataParser;
import org.jboss.metadata.parser.ee.SecurityRoleMetaDataParser;
import org.jboss.metadata.parser.servlet.ListenerMetaDataParser;
import org.jboss.metadata.parser.servlet.LocaleEncodingsMetaDataParser;
import org.jboss.metadata.parser.servlet.ServletMetaDataParser;
import org.jboss.metadata.parser.servlet.SessionConfigMetaDataParser;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.mobicents.metadata.sip.spec.Element;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;

/**
 * @author Remy Maucherat
 * @author josemrecio@gmail.com
 *
 */
public class SipCommonMetaDataParser extends MetaDataElementParser {

    public static boolean parse(XMLStreamReader reader, SipMetaData smd) throws XMLStreamException {
        // Only look at the current element, no iteration
        final Element element = Element.forName(reader.getLocalName());
        switch (element) {
            case APPLICATION_NAME:
                smd.setApplicationName(getElementText(reader));
                break;
            // DescriptionGroup is parsed by SipMetaDataParser
            //case DISPLAY_NAME:
            //    break;
            //case DESCRIPTION:
            //    break;
            case DISTRIBUTABLE:
                // TODO
                throw unexpectedElement(reader);
                // smd.setDistributable(new EmptyMetaData());
                // requireNoContent(reader);
                // break;
            case CONTEXT_PARAM:
                List<ParamValueMetaData> contextParams = smd.getContextParams();
                if (contextParams == null) {
                    contextParams = new ArrayList<ParamValueMetaData>();
                    smd.setContextParams(contextParams);
                }
                contextParams.add(ParamValueMetaDataParser.parse(reader));
                break;
            case LISTENER:
                List<ListenerMetaData> listeners = smd.getListeners();
                if (listeners == null) {
                    listeners = new ArrayList<ListenerMetaData>();
                    smd.setListeners(listeners);
                }
                listeners.add(ListenerMetaDataParser.parse(reader));
                break;
            case SERVLET_SELECTION:
                smd.setServletSelection(SipServletSelectionMetaDataParser.parse(reader));
                break;
            case SERVLET:
                SipServletsMetaData servlets = smd.getSipServlets();
                if (servlets == null) {
                    servlets = new SipServletsMetaData();
                    smd.setSipServlets(servlets);
                }
                servlets.add(ServletMetaDataParser.parse(reader));
                break;
            case PROXY_CONFIG:
                smd.setProxyConfig(ProxyConfigMetaDataParser.parse(reader));
                break;
            case SESSION_CONFIG:
                 if (smd.getSessionConfig() != null)
                     throw new XMLStreamException("Multiple session-config elements detected", reader.getLocation());
                 smd.setSessionConfig(SessionConfigMetaDataParser.parse(reader));
                 break;
            case SECURITY_CONSTRAINT:
                List<SipSecurityConstraintMetaData> sipSecurityConstraints = smd.getSipSecurityConstraints();
                if (sipSecurityConstraints == null) {
                    sipSecurityConstraints = new ArrayList<SipSecurityConstraintMetaData>();
                    smd.setSipSecurityConstraints(sipSecurityConstraints);
                }
                sipSecurityConstraints.add(SipSecurityConstraintMetaDataParser.parse(reader));
                break;
            case LOGIN_CONFIG:
                if (smd.getSipLoginConfig() != null)
                    throw new XMLStreamException("Multiple login-config elements detected", reader.getLocation());
                smd.setSipLoginConfig(SipLoginConfigMetaDataParser.parse(reader));
                break;
            case SECURITY_ROLE:
                SecurityRolesMetaData securityRoles = smd.getSecurityRoles();
                if (securityRoles == null) {
                    securityRoles = new SecurityRolesMetaData();
                    smd.setSecurityRoles(securityRoles);
                }
                securityRoles.add(SecurityRoleMetaDataParser.parse(reader));
                break;
            // javaee:jndiEnvironmentRefsGroup is parsed by SipMetaDataParser
            case MESSAGE_DESTINATION:
                MessageDestinationsMetaData messageDestinations = smd.getMessageDestinations();
                if (messageDestinations == null) {
                    messageDestinations = new MessageDestinationsMetaData();
                    smd.setMessageDestinations(messageDestinations);
                }
                messageDestinations.add(MessageDestinationMetaDataParser.parse(reader));
                break;
            case LOCALE_ENCODING_MAPPING_LIST:
                smd.setLocalEncodings(LocaleEncodingsMetaDataParser.parse(reader));
                break;
            default:
                return false;
        }
        return true;
    }

}
