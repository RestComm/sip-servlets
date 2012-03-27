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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.jboss.metadata.parser.util.MetaDataElementParser;
import org.mobicents.metadata.sip.spec.Attribute;
import org.mobicents.metadata.sip.spec.Element;
import org.mobicents.metadata.sip.spec.SubdomainOfMetaData;

/**
 * @author josemrecio@gmail.com
 *
 */
public class SubdomainOfMetaDataParser extends MetaDataElementParser {

    public static SubdomainOfMetaData parse(XMLStreamReader reader) throws XMLStreamException {
        SubdomainOfMetaData subdomainOfMetaData = new SubdomainOfMetaData();

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
                case VAR:
                    if (subdomainOfMetaData.getVar() != null) {
                        throw unexpectedElement(reader);
                    }
                    subdomainOfMetaData.setFromVarMetaData(VarMetaDataParser.parse(reader));
                    break;
                case VALUE:
                    if (subdomainOfMetaData.getValue() != null) {
                        throw unexpectedElement(reader);
                    }
                    subdomainOfMetaData.setValue(reader.getElementText());
                    break;
                default:
                    throw unexpectedElement(reader);
            }
        }

        return subdomainOfMetaData;
    }

}
