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
import org.mobicents.metadata.sip.spec.SipServletSelectionMetaData;

/**
 * @author josemrecio@gmail.com
 *
 */
public class SipServletSelectionMetaDataParser extends MetaDataElementParser {

    public static SipServletSelectionMetaData parse(XMLStreamReader reader) throws XMLStreamException {
        SipServletSelectionMetaData servletSelection = new SipServletSelectionMetaData();

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
                case MAIN_SERVLET:
                    if (servletSelection.getMainServlet() != null) {
                        throw duplicateNamedElement(reader, Element.MAIN_SERVLET.name());
                    }
                    servletSelection.setMainServlet(getElementText(reader));
                    break;
                case SERVLET_MAPPING:
                    servletSelection.addToSipServletMappings(SipServletMappingMetaDataParser.parse(reader));
                    break;
                default:
                    throw unexpectedElement(reader);
            }
        }

        return servletSelection;
    }

}
