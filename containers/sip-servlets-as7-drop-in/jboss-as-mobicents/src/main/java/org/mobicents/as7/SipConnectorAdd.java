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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.mobicents.as7.Constants.STATIC_SERVER_ADDRESS;
import static org.mobicents.as7.Constants.STATIC_SERVER_PORT;
import static org.mobicents.as7.Constants.STUN_SERVER_ADDRESS;
import static org.mobicents.as7.Constants.STUN_SERVER_PORT;
import static org.mobicents.as7.SipConnectorDefinition.CONNECTOR_ATTRIBUTES;

import java.util.List;

import org.apache.catalina.connector.Connector;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

/**
 * {@code OperationHandler} responsible for adding a sip connector.
 *
 * @author Emanuel Muckenhuber
 */
class SipConnectorAdd extends AbstractAddStepHandler {

    static final SipConnectorAdd INSTANCE = new SipConnectorAdd();

    private SipConnectorAdd() {
        //
    }

    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        model.get(SipConnectorDefinition.NAME.getName()).set(address.getLastElement().getValue());

        for (SimpleAttributeDefinition def : CONNECTOR_ATTRIBUTES) {
            def.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
    	final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        final String name = address.getLastElement().getValue();

        ModelNode fullModel = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        final String bindingRef = SipConnectorDefinition.SOCKET_BINDING.resolveModelAttribute(context, fullModel).asString();

        final boolean enabled = SipConnectorDefinition.ENABLED.resolveModelAttribute(context, fullModel).asBoolean();
        final String protocol = SipConnectorDefinition.PROTOCOL.resolveModelAttribute(context, fullModel).asString();
        final String scheme = SipConnectorDefinition.SCHEME.resolveModelAttribute(context, fullModel).asString();

        final boolean useStaticAddress = SipConnectorDefinition.USE_STATIC_ADDRESS.resolveModelAttribute(context, fullModel).asBoolean();        
        final String staticServerAddress = operation.hasDefined(STATIC_SERVER_ADDRESS) ? SipConnectorDefinition.STATIC_SERVER_ADDRESS.resolveModelAttribute(context, fullModel).asString() : null;
        final int staticServerPort = operation.hasDefined(STATIC_SERVER_PORT) ? SipConnectorDefinition.STATIC_SERVER_PORT.resolveModelAttribute(context, fullModel).asInt() : -1;

        final boolean useStun = SipConnectorDefinition.USE_STUN.resolveModelAttribute(context, fullModel).asBoolean();        
        final String stunServerAddress = operation.hasDefined(STUN_SERVER_ADDRESS) ? SipConnectorDefinition.STUN_SERVER_ADDRESS.resolveModelAttribute(context, fullModel).asString() : null;
        final int stunServerPort = operation.hasDefined(STUN_SERVER_PORT) ? SipConnectorDefinition.STUN_SERVER_PORT.resolveModelAttribute(context, fullModel).asInt() : -1;

        final SipConnectorService service = new SipConnectorService(protocol, scheme, useStaticAddress, staticServerAddress, staticServerPort, useStun, stunServerAddress, stunServerPort);
        final ServiceBuilder<Connector> serviceBuilder = context.getServiceTarget().addService(SipSubsystemServices.JBOSS_SIP_CONNECTOR.append(name), service)
                .addDependency(SipSubsystemServices.JBOSS_SIP, SipServer.class, service.getServer())
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(bindingRef), SocketBinding.class, service.getBinding());
        serviceBuilder.setInitialMode(enabled ? Mode.ACTIVE : Mode.NEVER);
        if (enabled) {
            serviceBuilder.addListener(verificationHandler);
        }
        final ServiceController<Connector> serviceController = serviceBuilder.install();
        if (newControllers != null) {
            newControllers.add(serviceController);
        }
    }
}
