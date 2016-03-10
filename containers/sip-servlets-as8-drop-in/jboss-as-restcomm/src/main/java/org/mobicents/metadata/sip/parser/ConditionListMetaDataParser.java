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

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.mobicents.metadata.sip.spec.Attribute;
import org.mobicents.metadata.sip.spec.ConditionMetaData;
import org.mobicents.metadata.sip.spec.Element;

/**
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.metadata.sip.parser package from jboss-as7-mobicents project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class ConditionListMetaDataParser extends MetaDataElementParser {

    public static List<ConditionMetaData> parse(XMLStreamReader reader) throws XMLStreamException {

        List<ConditionMetaData> listConditionMetaData = new ArrayList<ConditionMetaData>();

        // Handle attributes
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String value = reader.getAttributeValue(i);
            if (attributeHasNamespace(reader, i)) {
                continue;
            }
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }

        // Handle elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case AND:
                    listConditionMetaData.add(AndMetaDataParser.parse(reader));
                    break;
                case OR:
                    listConditionMetaData.add(OrMetaDataParser.parse(reader));
                    break;
                case NOT:
                    listConditionMetaData.add(NotMetaDataParser.parse(reader));
                    break;
                case EQUAL:
                    listConditionMetaData.add(EqualMetaDataParser.parse(reader));
                    break;
                case CONTAINS:
                    listConditionMetaData.add(ContainsMetaDataParser.parse(reader));
                    break;
                case EXISTS:
                    listConditionMetaData.add(ExistsMetaDataParser.parse(reader));
                    break;
                default:
                    throw unexpectedElement(reader);
            }
        }

        return listConditionMetaData;
    }

}
