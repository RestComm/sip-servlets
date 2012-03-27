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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.metadata.parser.servlet.AuthConstraintMetaDataParser;
import org.jboss.metadata.parser.servlet.UserDataConstraintMetaDataParser;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.mobicents.metadata.sip.spec.Attribute;
import org.mobicents.metadata.sip.spec.Element;
import org.mobicents.metadata.sip.spec.SipResourceCollectionsMetaData;
import org.mobicents.metadata.sip.spec.SipSecurityConstraintMetaData;

/**
 * @author Remy Maucherat
 */
public class SipSecurityConstraintMetaDataParser extends MetaDataElementParser {

    public static SipSecurityConstraintMetaData parse(XMLStreamReader reader) throws XMLStreamException {
        SipSecurityConstraintMetaData sipSecurityConstraint = new SipSecurityConstraintMetaData();

        // Handle attributes
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i ++) {
            final String value = reader.getAttributeValue(i);
            if (attributeHasNamespace(reader, i)) {
                continue;
            }
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case ID: {
                    sipSecurityConstraint.setId(value);
                    break;
                }
                default: throw unexpectedAttribute(reader, i);
            }
        }

        // Handle elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case DISPLAY_NAME:
                    sipSecurityConstraint.setDisplayName(getElementText(reader));
                    break;
                case RESOURCE_COLLECTION:
                    SipResourceCollectionsMetaData resourceCollections = sipSecurityConstraint.getResourceCollections();
                    if (resourceCollections == null) {
                        resourceCollections = new SipResourceCollectionsMetaData();
                        sipSecurityConstraint.setResourceCollections(resourceCollections);
                    }
                    resourceCollections.add(SipResourceCollectionsMetaDataParser.parse(reader));
                    break;
                case PROXY_AUTHENTICATION:
                    sipSecurityConstraint.setProxyAuthentication(EmptyMetaDataParser.parse(reader));
                    break;
                case AUTH_CONSTRAINT:
                    sipSecurityConstraint.setAuthConstraint(AuthConstraintMetaDataParser.parse(reader));
                    break;
                case USER_DATA_CONSTRAINT:
                    sipSecurityConstraint.setUserDataConstraint(UserDataConstraintMetaDataParser.parse(reader));
                    break;
                default:
                    throw unexpectedElement(reader);
            }
        }

        return sipSecurityConstraint;
    }

}
