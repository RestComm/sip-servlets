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
package org.mobicents.as8;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.mobicents.as8.SipConnectorDefinition.CONNECTOR_ATTRIBUTES;

import java.util.Date;
import java.util.List;

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
import org.jboss.msc.service.ServiceName;
import org.wildfly.extension.undertow.UdpListenerService;

/**
 * {@code OperationHandler} responsible for adding a sip connector.
 *
 * @author Emanuel Muckenhuber
 * @author alerant.appngin@gmail.com
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
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.get(OP_ADDR));
        final String name = address.getLastElement().getValue();

        ModelNode fullModel = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));
        final String bindingRef = SipConnectorDefinition.SOCKET_BINDING.resolveModelAttribute(context, fullModel)
                .asString();

        final boolean enabled = SipConnectorDefinition.ENABLED.resolveModelAttribute(context, fullModel).asBoolean();
        final String protocol = SipConnectorDefinition.PROTOCOL.resolveModelAttribute(context, fullModel).asString();
        final String scheme = SipConnectorDefinition.SCHEME.resolveModelAttribute(context, fullModel).asString();

        UdpListenerService udpListenerService = null;
        boolean gotInjectedValues = false;
        boolean timeout = false;
        Date start = new Date();
        // FIXME:needs a better solution here:
        while ((udpListenerService == null || gotInjectedValues == false) && timeout == false) {
            Date current = new Date();
            if (current.getTime() - start.getTime() > 10000) {
                timeout = true;
                break;
            }
            List<ServiceName> serviceNames = context.getServiceRegistry(false).getServiceNames();

            for (ServiceName serviceName : serviceNames) {
                ServiceController<?> controller = context.getServiceRegistry(false).getService(serviceName);
                if (controller.getService() instanceof UdpListenerService
                        && name.equalsIgnoreCase(((UdpListenerService) controller.getService()).getName())) {
                    udpListenerService = (UdpListenerService) controller.getService();

                    // check whether values have been already injected by the container:
                    try {
                        if (udpListenerService.getServerService() != null
                                && udpListenerService.getServerService().getValue() != null
                                && udpListenerService.getServerService().getValue().getRootUdpHandler() != null
                                && udpListenerService.getServerService().getValue().getRootUdpHandler().getChannel() != null) {
                            gotInjectedValues = true;
                        }
                    } catch (Exception e) {
                        gotInjectedValues = false;
                    }

                    break;
                }
            }

        }

        if (udpListenerService == null) {
            throw new OperationFailedException("Please add an udp-listener to webserver definition!", operation);
        }

        final SipConnectorService service = new SipConnectorService(protocol, scheme, udpListenerService);

        final ServiceBuilder<SipUdpListener> serviceBuilder = context
                .getServiceTarget()
                .addService(SipSubsystemServices.JBOSS_SIP_CONNECTOR.append(name), service)
                .addDependency(SipSubsystemServices.JBOSS_SIP, SipServer.class, service.getServer())
                .addDependency(SocketBinding.JBOSS_BINDING_NAME.append(bindingRef), SocketBinding.class,
                        service.getBinding());
        serviceBuilder.setInitialMode(enabled ? Mode.ACTIVE : Mode.NEVER);
        if (enabled) {
            serviceBuilder.addListener(verificationHandler);
        }
        final ServiceController<SipUdpListener> serviceController = serviceBuilder.install();
        if (newControllers != null) {
            newControllers.add(serviceController);
        }
    }
}
