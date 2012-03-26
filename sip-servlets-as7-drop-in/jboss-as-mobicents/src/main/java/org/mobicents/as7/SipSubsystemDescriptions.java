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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DEFAULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HEAD_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAMESPACE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NILLABLE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REPLY_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TAIL_COMMENT_ALLOWED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * The web subsystem description providers.
 *
 * @author Emanuel Muckenhuber
 * @author Jean-Frederic Clere
 */
class SipSubsystemDescriptions {

    static final String RESOURCE_NAME = SipSubsystemDescriptions.class.getPackage().getName() + ".LocalDescriptions";

    static ModelNode getSubsystemDescription(final Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();

        node.get(DESCRIPTION).set(bundle.getString("sip"));
        node.get(HEAD_COMMENT_ALLOWED).set(true);
        node.get(TAIL_COMMENT_ALLOWED).set(true);
        node.get(NAMESPACE).set(Namespace.SIP_1_0.getUriString());

        node.get(ATTRIBUTES, Constants.INSTANCE_ID, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, Constants.INSTANCE_ID, DESCRIPTION).set(bundle.getString("sip.instance-id"));
        node.get(ATTRIBUTES, Constants.INSTANCE_ID, REQUIRED).set(false);

        node.get(CHILDREN, Constants.CONNECTOR, DESCRIPTION).set(bundle.getString("sip.connector"));
        node.get(CHILDREN, Constants.CONNECTOR, MODEL_DESCRIPTION);

        return node;
    }

    static ModelNode getSubsystemAddDescription(final Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(ADD);
        node.get(DESCRIPTION).set(bundle.getString("sip.add"));

        node.get(REQUEST_PROPERTIES, Constants.INSTANCE_ID, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, Constants.INSTANCE_ID, DESCRIPTION).set(bundle.getString("sip.instance-id"));
        node.get(REQUEST_PROPERTIES, Constants.INSTANCE_ID, REQUIRED).set(false);

//        // Add configuration, connectors etc
//        ModelNode configuration = node.get(REQUEST_PROPERTIES, Constants.CONTAINER_CONFIG);
//        getConfigurationCommonDescription(configuration, "value-type", bundle);
//        configuration.get(TYPE).set(ModelType.OBJECT);
//        configuration.get(REQUIRED).set(false);

        ModelNode connector = node.get(REQUEST_PROPERTIES, Constants.CONNECTOR);
        getConnectorCommonDescription(connector, "value-type", bundle);
        connector.get(TYPE).set(ModelType.OBJECT);
        connector.get(REQUIRED).set(false);

        return node;
    }

    public static ModelNode getSubsystemRemoveDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);
        final ModelNode op = new ModelNode();
        op.get(OPERATION_NAME).set(REMOVE);
        op.get(DESCRIPTION).set(bundle.getString("sip.remove"));
        op.get(REPLY_PROPERTIES).setEmptyObject();
        op.get(REQUEST_PROPERTIES).setEmptyObject();
        return op;
    }

    static ModelNode getConnectorDescription(final Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        ModelNode node = new ModelNode();
        node.get(HEAD_COMMENT_ALLOWED).set(true);
        node.get(TAIL_COMMENT_ALLOWED).set(true);

        return getConnectorCommonDescription(node, ATTRIBUTES, bundle);
    }

    static ModelNode getConnectorCommonDescription(final ModelNode node, final String type, final ResourceBundle bundle) {

        node.get(DESCRIPTION).set(bundle.getString("sip.connector"));

        node.get(type, Constants.NAME, TYPE).set(ModelType.STRING);
        node.get(type, Constants.NAME, DESCRIPTION).set(bundle.getString("sip.connector.name"));
        node.get(type, Constants.NAME, REQUIRED).set(false); // TODO should be true.
        node.get(type, Constants.NAME, NILLABLE).set(false);

        node.get(type, Constants.PROTOCOL, TYPE).set(ModelType.STRING);
        node.get(type, Constants.PROTOCOL, DESCRIPTION).set(bundle.getString("sip.connector.protocol"));
        node.get(type, Constants.PROTOCOL, REQUIRED).set(true);

        node.get(type, Constants.SOCKET_BINDING, TYPE).set(ModelType.STRING);
        node.get(type, Constants.SOCKET_BINDING, DESCRIPTION).set(bundle.getString("sip.connector.socket-binding"));
        node.get(type, Constants.SOCKET_BINDING, REQUIRED).set(true);
        node.get(type, Constants.SOCKET_BINDING, NILLABLE).set(false);

        node.get(type, Constants.SCHEME, TYPE).set(ModelType.STRING);
        node.get(type, Constants.SCHEME, DESCRIPTION).set(bundle.getString("sip.connector.scheme"));
        node.get(type, Constants.SCHEME, REQUIRED).set(false);
        node.get(type, Constants.SCHEME, DEFAULT).set("http");

        node.get(type, Constants.ENABLED, TYPE).set(ModelType.BOOLEAN);
        node.get(type, Constants.ENABLED, DESCRIPTION).set(bundle.getString("sip.connector.enabled"));
        node.get(type, Constants.ENABLED, REQUIRED).set(false);
        node.get(type, Constants.ENABLED, DEFAULT).set(true);

        if (ATTRIBUTES.equals(type)) {
            /* add the stats descriptions */

            node.get(type, Constants.BYTES_SENT, TYPE).set(ModelType.INT);
            node.get(type, Constants.BYTES_SENT, DESCRIPTION).set(bundle.getString("sip.connector.stats.bytes-sent"));
            node.get(type, Constants.BYTES_RECEIVED, TYPE).set(ModelType.INT);
            node.get(type, Constants.BYTES_RECEIVED, DESCRIPTION).set(bundle.getString("sip.connector.stats.bytes-received"));
            node.get(type, Constants.PROCESSING_TIME, TYPE).set(ModelType.INT);
            node.get(type, Constants.PROCESSING_TIME, DESCRIPTION).set(bundle.getString("sip.connector.stats.processing-time"));
            node.get(type, Constants.ERROR_COUNT, TYPE).set(ModelType.INT);
            node.get(type, Constants.ERROR_COUNT, DESCRIPTION).set(bundle.getString("sip.connector.stats.error-count"));
            node.get(type, Constants.MAX_TIME, TYPE).set(ModelType.INT);
            node.get(type, Constants.MAX_TIME, DESCRIPTION).set(bundle.getString("sip.connector.stats.max-time"));
            node.get(type, Constants.REQUEST_COUNT, TYPE).set(ModelType.INT);
            node.get(type, Constants.REQUEST_COUNT, DESCRIPTION).set(bundle.getString("sip.connector.request-count"));

        }

        return node;
    }

    static ModelNode getConnectorAdd(final Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(ADD);
        node.get(DESCRIPTION).set(bundle.getString("sip.connector.add"));

        getConnectorCommonDescription(node, REQUEST_PROPERTIES, bundle);

        return node;
    }

    static ModelNode getConnectorRemove(final Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);
        final ModelNode node = new ModelNode();
        node.get(OPERATION_NAME).set(REMOVE);
        node.get(DESCRIPTION).set(bundle.getString("sip.connector.remove"));
        return node;
    }

    static ModelNode getDeploymentRuntimeDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();

        node.get(DESCRIPTION).set(bundle.getString("sip.deployment"));
        node.get(ATTRIBUTES).setEmptyObject();
        node.get(OPERATIONS); // placeholder

        node.get(CHILDREN, "servlet", DESCRIPTION).set(bundle.getString("sip.deployment.servlet"));
        node.get(CHILDREN, "servlet", MIN_OCCURS).set(0);
        node.get(CHILDREN, "servlet", MODEL_DESCRIPTION);

        return node;
    }

    static ModelNode getDeploymentServletDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();

        node.get(DESCRIPTION).set(bundle.getString("sip.deployment.servlet"));

        node.get(ATTRIBUTES, "load-time", DESCRIPTION).set(bundle.getString("sip.deployment.servlet.load-time"));
        node.get(ATTRIBUTES, "load-time", TYPE).set(ModelType.LONG);
        node.get(ATTRIBUTES, "max-time", DESCRIPTION).set(bundle.getString("sip.deployment.servlet.max-time"));
        node.get(ATTRIBUTES, "max-time", TYPE).set(ModelType.LONG);
        node.get(ATTRIBUTES, "min-time", DESCRIPTION).set(bundle.getString("sip.deployment.servlet.min-time"));
        node.get(ATTRIBUTES, "min-time", TYPE).set(ModelType.LONG);
        node.get(ATTRIBUTES, "processing-time", DESCRIPTION).set(bundle.getString("sip.deployment.servlet.processing-time"));
        node.get(ATTRIBUTES, "processing-time", TYPE).set(ModelType.LONG);
        node.get(ATTRIBUTES, "request-count", DESCRIPTION).set(bundle.getString("sip.deployment.servlet.request-count"));
        node.get(ATTRIBUTES, "request-count", TYPE).set(ModelType.INT);

        node.get(OPERATIONS); // placeholder

        node.get(CHILDREN).setEmptyObject();

        return node;
    }

    private static ResourceBundle getResourceBundle(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return ResourceBundle.getBundle(RESOURCE_NAME, locale);
    }

    public static ModelNode getConfigurationDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);
        final ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set(bundle.getString("sip.configuration"));

        return node;
    }

}
