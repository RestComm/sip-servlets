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

import io.undertow.server.handlers.MetricsHandler;
import io.undertow.servlet.api.DeploymentInfo;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.wildfly.extension.undertow.DeploymentDefinition;
import org.wildfly.extension.undertow.UndertowService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;
import org.wildfly.extension.undertow.deployment.UndertowMetricsCollector;

/**
 * @author Tomaz Cerar
 * @created 23.2.12 18:35
 * @author josemrecio@gmail.com
 */
public class SipDeploymentServletDefinition extends SimpleResourceDefinition {
    public static final SipDeploymentServletDefinition INSTANCE = new SipDeploymentServletDefinition();

    protected static final SimpleAttributeDefinition LOAD_TIME = new SimpleAttributeDefinitionBuilder(Constants.LOAD_TIME, ModelType.LONG, true).build();
    protected static final SimpleAttributeDefinition MAX_TIME = new SimpleAttributeDefinitionBuilder(Constants.MAX_TIME, ModelType.LONG, true).build();
    protected static final SimpleAttributeDefinition MIN_TIME = new SimpleAttributeDefinitionBuilder(Constants.MIN_TIME, ModelType.LONG, true).build();
    protected static final SimpleAttributeDefinition PROCESSING_TIME = new SimpleAttributeDefinitionBuilder(Constants.PROCESSING_TIME, ModelType.LONG, true).build();
    protected static final SimpleAttributeDefinition REQUEST_COUNT = new SimpleAttributeDefinitionBuilder(Constants.REQUEST_COUNT, ModelType.INT, true).build();


    private SipDeploymentServletDefinition() {
        super(PathElement.pathElement("servlet"),
                SipExtension.getResourceDescriptionResolver("deployment.servlet"));
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration registration) {
        registration.registerMetric(LOAD_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult) {
                //FIXME:response.set(metricResult.getLoadTime());
            }
        });
        registration.registerMetric(MAX_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult) {
                response.set(metricResult.getMaxRequestTime());
            }
        });
        registration.registerMetric(MIN_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult) {
                response.set(metricResult.getMinRequestTime());
            }
        });
        registration.registerMetric(PROCESSING_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult) {
                response.set(metricResult.getTotalRequestTime());
            }
        });
        registration.registerMetric(REQUEST_COUNT, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult) {
                response.set(metricResult.getTotalRequests());
            }
        });
    }

    abstract static class AbstractMetricsHandler implements OperationStepHandler {

        abstract void handle(ModelNode response, String name, MetricsHandler.MetricResult metricResult);

        @Override
        public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
            final PathAddress address = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));

            final Resource web = context.readResourceFromRoot(address.subAddress(0, address.size() - 1), false);
            final ModelNode subModel = web.getModel();

            final String host = DeploymentDefinition.VIRTUAL_HOST.resolveModelAttribute(context, subModel).asString();
            final String path = DeploymentDefinition.CONTEXT_ROOT.resolveModelAttribute(context, subModel).asString();
            final String server = DeploymentDefinition.SERVER.resolveModelAttribute(context, subModel).asString();

            final ServiceController<?> controller = context.getServiceRegistry(false).getService(UndertowService.deploymentServiceName(server, host, path));
            final UndertowDeploymentService deploymentService = (UndertowDeploymentService) controller.getService();
            final DeploymentInfo deploymentInfo = deploymentService.getDeploymentInfoInjectedValue().getValue();
            final UndertowMetricsCollector collector = (UndertowMetricsCollector)deploymentInfo.getMetricsCollector();
            context.addStep(new OperationStepHandler() {
                @Override
                public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {

                    if (controller != null) {
                        final String name = address.getLastElement().getValue();
                        final ModelNode response = new ModelNode();
                        MetricsHandler.MetricResult result = collector != null ? collector.getMetrics(name) : null;
                        if (result == null) {
                            response.set(0);
                        } else {
                            handle(response, name, result);
                        }
                        context.getResult().set(response);
                    }
                    context.stepCompleted();
                }
            }, OperationContext.Stage.RUNTIME);
            context.stepCompleted();
        }
    }

}
