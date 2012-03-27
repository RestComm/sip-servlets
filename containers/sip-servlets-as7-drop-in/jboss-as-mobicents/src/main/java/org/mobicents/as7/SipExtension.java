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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.operations.global.WriteAttributeHandlers;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.AttributeAccess.Storage;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;
import org.mobicents.as7.deployment.ServletDeploymentStats;

/**
 * The web extension.
 *
 * @author Emanuel Muckenhuber
 */
public class SipExtension implements Extension {

    private static final Logger log = Logger.getLogger("org.mobicents.as7");

    public static final String NAMESPACE_FOR_TESTING = Namespace.CURRENT.getUriString();
    public static final String SUBSYSTEM_NAME = "sip";
    private static final PathElement connectorPath =  PathElement.pathElement(Constants.CONNECTOR);

    /** {@inheritDoc} */
    @Override
    public void initialize(ExtensionContext context) {

        final boolean registerRuntimeOnly = context.isRuntimeOnlyRegistrationValid();

        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SipSubsystemDescriptionProviders.SUBSYSTEM);
        registration.registerOperationHandler(ADD, SipSubsystemAdd.INSTANCE, SipSubsystemAdd.INSTANCE, false);
        registration.registerOperationHandler(DESCRIBE, SipSubsystemDescribe.INSTANCE, SipSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        registration.registerOperationHandler(REMOVE, ReloadRequiredRemoveStepHandler.INSTANCE, SipSubsystemDescriptionProviders.SUBSYSTEM_REMOVE, false);
        subsystem.registerXMLElementWriter(SipSubsystemParser.getInstance());

        // connectors
        final ManagementResourceRegistration connectors = registration.registerSubModel(connectorPath, SipSubsystemDescriptionProviders.CONNECTOR);
        connectors.registerOperationHandler(ADD, SipConnectorAdd.INSTANCE, SipConnectorAdd.INSTANCE, false);
        connectors.registerOperationHandler(REMOVE, SipConnectorRemove.INSTANCE, SipConnectorRemove.INSTANCE, false);
        if (registerRuntimeOnly) {
            for (final String attributeName : SipConnectorMetrics.ATTRIBUTES) {
                connectors.registerMetric(attributeName, SipConnectorMetrics.INSTANCE);
            }
        }
        connectors.registerReadWriteAttribute(Constants.PROTOCOL, null, new WriteAttributeHandlers.StringLengthValidatingHandler(1, true), Storage.CONFIGURATION);
        connectors.registerReadWriteAttribute(Constants.SCHEME, null, new WriteAttributeHandlers.StringLengthValidatingHandler(1, true), Storage.CONFIGURATION);
        connectors.registerReadWriteAttribute(Constants.SOCKET_BINDING, null, new WriteAttributeHandlers.StringLengthValidatingHandler(1), Storage.CONFIGURATION);
        connectors.registerReadWriteAttribute(Constants.ENABLED, null, new WriteAttributeHandlers.ModelTypeValidatingHandler(ModelType.BOOLEAN, true), Storage.CONFIGURATION);

        if (registerRuntimeOnly) {
            final ManagementResourceRegistration deployments = subsystem.registerDeploymentModel(SipSubsystemDescriptionProviders.DEPLOYMENT);
            final ManagementResourceRegistration servlets = deployments.registerSubModel(PathElement.pathElement("servlet"), SipSubsystemDescriptionProviders.SERVLET);
            ServletDeploymentStats.register(servlets);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, Namespace.SIP_1_0.getUriString(), SipSubsystemParser.getInstance());
    }

}
