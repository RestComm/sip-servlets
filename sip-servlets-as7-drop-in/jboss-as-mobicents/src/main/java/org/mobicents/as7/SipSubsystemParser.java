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

package org.mobicents.as7;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;
import static org.mobicents.as7.Constants.CONNECTOR;
import static org.mobicents.as7.Constants.ENABLED;
import static org.mobicents.as7.Constants.NAME;
import static org.mobicents.as7.Constants.PROTOCOL;
import static org.mobicents.as7.Constants.SCHEME;
import static org.mobicents.as7.Constants.SOCKET_BINDING;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * The web subsystem parser.
 *
 * @author Emanuel Muckenhuber
 * @author Brian Stansberry
 */
class SipSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private static final SipSubsystemParser INSTANCE = new SipSubsystemParser();

    static SipSubsystemParser getInstance() {
        return INSTANCE;
    }

    /** {@inheritDoc} */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {

        context.startSubsystemElement(Namespace.CURRENT.getUriString(), false);

        ModelNode node = context.getModelNode();

        writeAttribute(writer, Attribute.INSTANCE_ID.getLocalName(), node);
        if(node.hasDefined(CONNECTOR)) {
            for(final Property connector : node.get(CONNECTOR).asPropertyList()) {
                final ModelNode config = connector.getValue();
                writer.writeStartElement(Element.CONNECTOR.getLocalName());
                writer.writeAttribute(NAME, connector.getName());
                writeAttribute(writer, Attribute.PROTOCOL.getLocalName(), config);
                writeAttribute(writer, Attribute.SCHEME.getLocalName(), config);
                writeAttribute(writer, Attribute.SOCKET_BINDING.getLocalName(), config);
                writeAttribute(writer, Attribute.ENABLED.getLocalName(), config);

                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    /** {@inheritDoc} */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SipExtension.SUBSYSTEM_NAME);
        address.protect();

        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
            case INSTANCE_ID:
                subsystem.get(attribute.getLocalName()).set(value);
                break;
            default:
                throw unexpectedAttribute(reader, i);
            }
        }
        list.add(subsystem);

        // elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
                case SIP_1_0:{
                    final Element element = Element.forName(reader.getLocalName());
                    switch (element) {
                        case CONNECTOR: {
                            parseConnector(reader,address, list);
                            break;
                        } default: {
                            throw unexpectedElement(reader);
                        }
                    }
                    break;
                } default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    static void parseConnector(XMLExtendedStreamReader reader, ModelNode address, List<ModelNode> list) throws XMLStreamException {
        String name = null;
        String protocol = null;
        String bindingRef = null;
        String scheme = null;
        String enabled = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
            case NAME:
                name = value;
                break;
            case SOCKET_BINDING:
                bindingRef = value;
                break;
            case SCHEME:
                scheme = value;
                break;
            case PROTOCOL:
                protocol = value;
                break;
            case ENABLED:
                enabled = value;
                break;
            default:
                throw unexpectedAttribute(reader, i);
            }
        }
        if (name == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.NAME));
        }
        if (bindingRef == null) {
            throw missingRequired(reader, Collections.singleton(Attribute.SOCKET_BINDING));
        }
        final ModelNode connector = new ModelNode();
        connector.get(OP).set(ADD);
        connector.get(OP_ADDR).set(address).add(CONNECTOR, name);
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            switch (Namespace.forUri(reader.getNamespaceURI())) {
            case SIP_1_0: {
                final Element element = Element.forName(reader.getLocalName());
                switch (element) {
                default:
                    throw unexpectedElement(reader);
                }
                //break;
            }
            default:
                throw unexpectedElement(reader);
            }
        }
        if(protocol != null) connector.get(PROTOCOL).set(protocol);
        connector.get(SOCKET_BINDING).set(bindingRef);
        if(scheme != null) connector.get(SCHEME).set(scheme);
        if(enabled != null) connector.get(ENABLED).set(enabled);
        list.add(connector);
    }

    static void writeAttribute(final XMLExtendedStreamWriter writer, final String name, ModelNode node) throws XMLStreamException {
        if(node.hasDefined(name)) {
            writer.writeAttribute(name, node.get(name).asString());
        }
    }
    private boolean writeAttribute(XMLExtendedStreamWriter writer, String name, ModelNode node, boolean startwritten, String origin) throws XMLStreamException {
        if(node.hasDefined(name)) {
            if (!startwritten) {
                startwritten = true;
                writer.writeStartElement(origin);
            }
            writer.writeAttribute(name, node.get(name).asString());
        }
        return startwritten;
    }


}
