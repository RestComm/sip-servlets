/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.metadata.sip.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.metadata.parser.servlet.AuthConstraintMetaDataParser;
import org.jboss.metadata.parser.servlet.UserDataConstraintMetaDataParser;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.jboss.metadata.property.PropertyReplacers;
import org.mobicents.metadata.sip.spec.Attribute;
import org.mobicents.metadata.sip.spec.Element;
import org.mobicents.metadata.sip.spec.SipResourceCollectionsMetaData;
import org.mobicents.metadata.sip.spec.SipSecurityConstraintMetaData;

/**
 * @author Remy Maucherat
 *
 *         This class is based on the contents of org.mobicents.metadata.sip.parser package from jboss-as7-mobicents project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class SipSecurityConstraintMetaDataParser extends MetaDataElementParser {

    public static SipSecurityConstraintMetaData parse(XMLStreamReader reader) throws XMLStreamException {
        SipSecurityConstraintMetaData sipSecurityConstraint = new SipSecurityConstraintMetaData();

        // Handle attributes
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
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
                default:
                    throw unexpectedAttribute(reader, i);
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
                    // FIXME: 7.1.2.Final - setup proper ProperlyReplacer
                    sipSecurityConstraint
                            .setAuthConstraint(AuthConstraintMetaDataParser.parse(reader, PropertyReplacers.noop()));
                    break;
                case USER_DATA_CONSTRAINT:
                    // FIXME: 7.1.2.Final - setup proper ProperlyReplacer
                    sipSecurityConstraint.setUserDataConstraint(UserDataConstraintMetaDataParser.parse(reader,
                            PropertyReplacers.noop()));
                    break;
                default:
                    throw unexpectedElement(reader);
            }
        }

        return sipSecurityConstraint;
    }

}
