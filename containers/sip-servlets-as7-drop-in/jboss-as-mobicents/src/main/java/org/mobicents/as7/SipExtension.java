/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

/**
 * The sip extension.
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 */
public class SipExtension implements Extension {
    public static final String SUBSYSTEM_NAME = "sip";
    protected static final PathElement CONNECTOR_PATH = PathElement.pathElement(Constants.CONNECTOR);

    private static final String RESOURCE_NAME = SipExtension.class.getPackage().getName() + ".LocalDescriptions";

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, SipExtension.class.getClassLoader(), true, false);
    }

    private static final int MANAGEMENT_API_MAJOR_VERSION = 1;
    private static final int MANAGEMENT_API_MINOR_VERSION = 1;

    /** {@inheritDoc} */
    @Override
    public void initialize(ExtensionContext context) {

        //final boolean registerRuntimeOnly = context.isRuntimeOnlyRegistrationValid();

        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, MANAGEMENT_API_MAJOR_VERSION, MANAGEMENT_API_MINOR_VERSION);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SipDefinition.INSTANCE);
        registration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE, GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        //final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SipSubsystemDescriptionProviders.SUBSYSTEM);
        //registration.registerOperationHandler(ADD, SipSubsystemAdd.INSTANCE, SipSubsystemAdd.INSTANCE, false);
        //registration.registerOperationHandler(DESCRIBE, SipSubsystemDescribe.INSTANCE, SipSubsystemDescribe.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        //registration.registerOperationHandler(REMOVE, ReloadRequiredRemoveStepHandler.INSTANCE, SipSubsystemDescriptionProviders.SUBSYSTEM_REMOVE, false);
        subsystem.registerXMLElementWriter(SipSubsystemParser.getInstance());

        // connectors
        final ManagementResourceRegistration connectors = registration.registerSubModel(SipConnectorDefinition.INSTANCE);
//        final ManagementResourceRegistration connectors = registration.registerSubModel(CONNECTOR_PATH, SipSubsystemDescriptionProviders.CONNECTOR);
//        connectors.registerOperationHandler(ADD, SipConnectorAdd.INSTANCE, SipConnectorAdd.INSTANCE, false);
//        connectors.registerOperationHandler(REMOVE, SipConnectorRemove.INSTANCE, SipConnectorRemove.INSTANCE, false);
//        if (registerRuntimeOnly) {
//            for (final String attributeName : SipConnectorMetrics.ATTRIBUTES) {
//                connectors.registerMetric(attributeName, SipConnectorMetrics.INSTANCE);
//            }
//        }
//        connectors.registerReadWriteAttribute(Constants.PROTOCOL, null, new WriteAttributeHandlers.StringLengthValidatingHandler(1, true), Storage.CONFIGURATION);
//        connectors.registerReadWriteAttribute(Constants.SCHEME, null, new WriteAttributeHandlers.StringLengthValidatingHandler(1, true), Storage.CONFIGURATION);
//        connectors.registerReadWriteAttribute(Constants.SOCKET_BINDING, null, new WriteAttributeHandlers.StringLengthValidatingHandler(1), Storage.CONFIGURATION);
//        connectors.registerReadWriteAttribute(Constants.ENABLED, null, new WriteAttributeHandlers.ModelTypeValidatingHandler(ModelType.BOOLEAN, true), Storage.CONFIGURATION);

        //deployment
        final ManagementResourceRegistration deployments = subsystem.registerDeploymentModel(SipDeploymentDefinition.INSTANCE);
        deployments.registerSubModel(SipDeploymentServletDefinition.INSTANCE);

//        if (registerRuntimeOnly) {
//            final ManagementResourceRegistration deployments = subsystem.registerDeploymentModel(SipSubsystemDescriptionProviders.DEPLOYMENT);
//            final ManagementResourceRegistration servlets = deployments.registerSubModel(PathElement.pathElement("servlet"), SipSubsystemDescriptionProviders.SERVLET);
//            ServletDeploymentStats.register(servlets);
//        }
    }

    /** {@inheritDoc} */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, Namespace.SIP_1_0.getUriString(), SipSubsystemParser.getInstance());
    }

}
