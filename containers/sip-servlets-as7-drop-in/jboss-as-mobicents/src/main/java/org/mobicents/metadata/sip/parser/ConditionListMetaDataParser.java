/*
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
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.mobicents.metadata.sip.spec.Attribute;
import org.mobicents.metadata.sip.spec.ConditionMetaData;
import org.mobicents.metadata.sip.spec.Element;

/**
 * @author josemrecio@gmail.com
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
