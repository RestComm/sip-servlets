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
 * The sip subsystem description providers.
 *
 * @author Emanuel Muckenhuber
 * @author Jean-Frederic Clere
 * @author josemrecio@gmail.com
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

        node.get(ATTRIBUTES, Constants.APPLICATION_ROUTER, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, Constants.APPLICATION_ROUTER, DESCRIPTION).set(bundle.getString("sip.application-router"));
        node.get(ATTRIBUTES, Constants.APPLICATION_ROUTER, REQUIRED).set(true);

        node.get(ATTRIBUTES, Constants.SIP_STACK_PROPS, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, Constants.SIP_STACK_PROPS, DESCRIPTION).set(bundle.getString("sip.stack-properties"));
        node.get(ATTRIBUTES, Constants.SIP_STACK_PROPS, REQUIRED).set(true);

        node.get(ATTRIBUTES, Constants.SIP_PATH_NAME, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, Constants.SIP_PATH_NAME, DESCRIPTION).set(bundle.getString("sip.path-name"));
        node.get(ATTRIBUTES, Constants.SIP_PATH_NAME, REQUIRED).set(false);

        node.get(ATTRIBUTES, Constants.SIP_APP_DISPATCHER_CLASS, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, Constants.SIP_APP_DISPATCHER_CLASS, DESCRIPTION).set(bundle.getString("sip.app-dispatcher-class"));
        node.get(ATTRIBUTES, Constants.SIP_APP_DISPATCHER_CLASS, REQUIRED).set(true);

        node.get(ATTRIBUTES, Constants.CONCURRENCY_CONTROL_MODE, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, Constants.CONCURRENCY_CONTROL_MODE, DESCRIPTION).set(bundle.getString("sip.concurrency-control-mode"));
        node.get(ATTRIBUTES, Constants.CONCURRENCY_CONTROL_MODE, REQUIRED).set(false);
        //node.get(ATTRIBUTES, Constants.CONCURRENCY_CONTROL_MODE, DEFAULT).set("None");

        node.get(ATTRIBUTES, Constants.CONGESTION_CONTROL_INTERVAL, TYPE).set(ModelType.INT);
        node.get(ATTRIBUTES, Constants.CONGESTION_CONTROL_INTERVAL, DESCRIPTION).set(bundle.getString("sip.congestion-control-interval"));
        node.get(ATTRIBUTES, Constants.CONGESTION_CONTROL_INTERVAL, REQUIRED).set(false);

        node.get(ATTRIBUTES, Constants.USE_PRETTY_ENCODING, TYPE).set(ModelType.BOOLEAN);
        node.get(ATTRIBUTES, Constants.USE_PRETTY_ENCODING, DESCRIPTION).set(bundle.getString("sip.use-pretty-encoding"));
        node.get(ATTRIBUTES, Constants.USE_PRETTY_ENCODING, REQUIRED).set(false);
        node.get(ATTRIBUTES, Constants.USE_PRETTY_ENCODING, DEFAULT).set(true);

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

        node.get(REQUEST_PROPERTIES, Constants.APPLICATION_ROUTER, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, Constants.APPLICATION_ROUTER, DESCRIPTION).set(bundle.getString("sip.application-router"));
        node.get(REQUEST_PROPERTIES, Constants.APPLICATION_ROUTER, REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, Constants.SIP_STACK_PROPS, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, Constants.SIP_STACK_PROPS, DESCRIPTION).set(bundle.getString("sip.stack-properties"));
        node.get(REQUEST_PROPERTIES, Constants.SIP_STACK_PROPS, REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, Constants.SIP_PATH_NAME, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, Constants.SIP_PATH_NAME, DESCRIPTION).set(bundle.getString("sip.path-name"));
        node.get(REQUEST_PROPERTIES, Constants.SIP_PATH_NAME, REQUIRED).set(false);

        node.get(REQUEST_PROPERTIES, Constants.SIP_APP_DISPATCHER_CLASS, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, Constants.SIP_APP_DISPATCHER_CLASS, DESCRIPTION).set(bundle.getString("sip.app-dispatcher-class"));
        node.get(REQUEST_PROPERTIES, Constants.SIP_APP_DISPATCHER_CLASS, REQUIRED).set(true);

        node.get(REQUEST_PROPERTIES, Constants.CONCURRENCY_CONTROL_MODE, TYPE).set(ModelType.STRING);
        node.get(REQUEST_PROPERTIES, Constants.CONCURRENCY_CONTROL_MODE, DESCRIPTION).set(bundle.getString("sip.concurrency-control-mode"));
        node.get(REQUEST_PROPERTIES, Constants.CONCURRENCY_CONTROL_MODE, REQUIRED).set(false);

        node.get(REQUEST_PROPERTIES, Constants.CONGESTION_CONTROL_INTERVAL, TYPE).set(ModelType.INT);
        node.get(REQUEST_PROPERTIES, Constants.CONGESTION_CONTROL_INTERVAL, DESCRIPTION).set(bundle.getString("sip.congestion-control-interval"));
        node.get(REQUEST_PROPERTIES, Constants.CONGESTION_CONTROL_INTERVAL, REQUIRED).set(false);

        node.get(REQUEST_PROPERTIES, Constants.USE_PRETTY_ENCODING, TYPE).set(ModelType.BOOLEAN);
        node.get(REQUEST_PROPERTIES, Constants.USE_PRETTY_ENCODING, DESCRIPTION).set(bundle.getString("sip.use-pretty-encoding"));
        node.get(REQUEST_PROPERTIES, Constants.USE_PRETTY_ENCODING, REQUIRED).set(false);
        node.get(REQUEST_PROPERTIES, Constants.USE_PRETTY_ENCODING, DEFAULT).set(true);

        // Add configuration, connectors etc
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
        node.get(type, Constants.NAME, REQUIRED).set(true);
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

        node.get(type, Constants.USE_STATIC_ADDRESS, TYPE).set(ModelType.BOOLEAN);
        node.get(type, Constants.USE_STATIC_ADDRESS, DESCRIPTION).set(bundle.getString("sip.connector.use-static-address"));
        node.get(type, Constants.USE_STATIC_ADDRESS, REQUIRED).set(false);
        node.get(type, Constants.USE_STATIC_ADDRESS, DEFAULT).set(false);

        node.get(type, Constants.STATIC_SERVER_ADDRESS, TYPE).set(ModelType.STRING);
        node.get(type, Constants.STATIC_SERVER_ADDRESS, DESCRIPTION).set(bundle.getString("sip.connector.static-server-address"));
        node.get(type, Constants.STATIC_SERVER_ADDRESS, REQUIRED).set(false);

        node.get(type, Constants.STATIC_SERVER_PORT, TYPE).set(ModelType.INT);
        node.get(type, Constants.STATIC_SERVER_PORT, DESCRIPTION).set(bundle.getString("sip.connector.static-server-port"));
        node.get(type, Constants.STATIC_SERVER_PORT, REQUIRED).set(false);

        node.get(type, Constants.USE_STUN, TYPE).set(ModelType.BOOLEAN);
        node.get(type, Constants.USE_STUN, DESCRIPTION).set(bundle.getString("sip.connector.use-stun"));
        node.get(type, Constants.USE_STUN, REQUIRED).set(false);
        node.get(type, Constants.USE_STUN, DEFAULT).set(false);

        node.get(type, Constants.STUN_SERVER_ADDRESS, TYPE).set(ModelType.STRING);
        node.get(type, Constants.STUN_SERVER_ADDRESS, DESCRIPTION).set(bundle.getString("sip.connector.stun-server-address"));
        node.get(type, Constants.STUN_SERVER_ADDRESS, REQUIRED).set(false);

        node.get(type, Constants.STUN_SERVER_PORT, TYPE).set(ModelType.INT);
        node.get(type, Constants.STUN_SERVER_PORT, DESCRIPTION).set(bundle.getString("sip.connector.stun-server-port"));
        node.get(type, Constants.STUN_SERVER_PORT, REQUIRED).set(false);

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

}
