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

import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.EnvironmentRefsGroupMetaData;
import org.jboss.metadata.parser.ee.DescriptionGroupMetaDataParser;
import org.jboss.metadata.parser.ee.EnvironmentRefsGroupMetaDataParser;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.mobicents.metadata.sip.spec.Attribute;
import org.mobicents.metadata.sip.spec.Element;
import org.mobicents.metadata.sip.spec.Sip11MetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;

/**
 * Stax parser for web metadata
 *
 * @author Remy Maucherat
 * @author Thomas.Diesler@jboss.com
 */
public class SipMetaDataParser extends MetaDataElementParser {

    public static SipMetaData parse(XMLStreamReader reader, DTDInfo info) throws XMLStreamException {
        return parse(reader, info, false);
    }

    public static SipMetaData parse(XMLStreamReader reader, DTDInfo info, boolean validation) throws XMLStreamException {
        if (reader == null)
            throw new IllegalArgumentException("Null reader");
        if (info == null)
            throw new IllegalArgumentException("Null info");

        reader.require(START_DOCUMENT, null, null);

        // Read until the first start element
        while (reader.hasNext() && reader.next() != START_ELEMENT) {
        }

        String schemaLocation = readSchemaLocation(reader);

        SipMetaData smd = new Sip11MetaData();

        // Set the publicId / systemId
        if (info != null)
            smd.setDTD(info.getBaseURI(), info.getPublicID(), info.getSystemID());

        // Handle attributes
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String value = reader.getAttributeValue(i);
            if (attributeHasNamespace(reader, i)) {
                continue;
            }
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case VERSION: {
                    smd.setVersion(value);
                    break;
                }
                case METADATA_COMPLETE: {
                    if (smd instanceof Sip11MetaData) {
                        if (Boolean.TRUE.equals(Boolean.valueOf(value))) {
                            ((Sip11MetaData) smd).setMetadataComplete(true);
                        }
                    } else {
                        throw unexpectedAttribute(reader, i);
                    }
                    break;
                }
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }

        DescriptionGroupMetaData descriptionGroup = new DescriptionGroupMetaData();
        EnvironmentRefsGroupMetaData env = new EnvironmentRefsGroupMetaData();
        // Handle elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (EnvironmentRefsGroupMetaDataParser.parse(reader, env)) {
                if (smd.getJndiEnvironmentRefsGroup() == null) {
                    smd.setJndiEnvironmentRefsGroup(env);
                }
                continue;
            }
            if (DescriptionGroupMetaDataParser.parse(reader, descriptionGroup)) {
                if (smd.getDescriptionGroup() == null) {
                    smd.setDescriptionGroup(descriptionGroup);
                }
                continue;
            }
            if (SipCommonMetaDataParser.parse(reader, smd)) {
                continue;
            }
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                default:
                    throw unexpectedElement(reader);
            }
        }

        return smd;
    }

}
