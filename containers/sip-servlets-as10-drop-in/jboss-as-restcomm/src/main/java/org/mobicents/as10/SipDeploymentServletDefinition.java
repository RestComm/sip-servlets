/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.as10;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleListAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.server.logging.ServerLogger;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.wildfly.extension.undertow.DeploymentDefinition;
import org.wildfly.extension.undertow.UndertowService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;
import org.wildfly.extension.undertow.deployment.UndertowMetricsCollector;

import io.undertow.server.handlers.MetricsHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;

/**
 * @author Tomaz Cerar
 * @created 23.2.12 18:35
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.as7 package from jboss-as7-mobicents project, re-implemented for
 *         jboss as10 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class SipDeploymentServletDefinition extends SimpleResourceDefinition {
    public static final SipDeploymentServletDefinition INSTANCE = new SipDeploymentServletDefinition();

    static final SimpleAttributeDefinition SERVLET_NAME = new SimpleAttributeDefinitionBuilder("servlet-name", ModelType.STRING, false).setStorageRuntime().build();
    static final SimpleAttributeDefinition SERVLET_CLASS = new SimpleAttributeDefinitionBuilder("servlet-class", ModelType.STRING, false).setStorageRuntime().build();
    static final SimpleAttributeDefinition LOAD_ON_STARTUP = new SimpleAttributeDefinitionBuilder("load-on-startup", ModelType.STRING, true).setStorageRuntime().build();
    static final SimpleAttributeDefinition MAX_REQUEST_TIME = new SimpleAttributeDefinitionBuilder("max-request-time", ModelType.LONG, true).setStorageRuntime().build();
    static final SimpleAttributeDefinition MIN_REQUEST_TIME = new SimpleAttributeDefinitionBuilder("min-request-time", ModelType.LONG, true).setStorageRuntime().build();
    static final SimpleAttributeDefinition TOTAL_REQUEST_TIME = new SimpleAttributeDefinitionBuilder("total-request-time", ModelType.LONG, true).setStorageRuntime().build();
    static final SimpleAttributeDefinition REQUEST_COUNT = new SimpleAttributeDefinitionBuilder("request-count", ModelType.LONG, true).setStorageRuntime().build();
    static final SimpleListAttributeDefinition SERVLET_MAPPINGS = new SimpleListAttributeDefinition.Builder("mappings", new SimpleAttributeDefinitionBuilder("mapping", ModelType.STRING).setAllowNull(true).build())
            .setAllowNull(true)
            .setStorageRuntime()
            .build();
    
    private SipDeploymentServletDefinition() {
        super(PathElement.pathElement("servlet"), SipExtension.getResourceDescriptionResolver("deployment.servlet"));
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration registration) {

        registration.registerReadOnlyAttribute(SERVLET_NAME, null);
        registration.registerReadOnlyAttribute(SERVLET_CLASS, null);
        registration.registerReadOnlyAttribute(LOAD_ON_STARTUP, null);
        registration.registerMetric(MAX_REQUEST_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult, final ServletInfo servlet) {
                response.set(metricResult.getMaxRequestTime());
            }
        });
        registration.registerMetric(MIN_REQUEST_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult, final ServletInfo servlet) {
                response.set(metricResult.getMinRequestTime());
            }
        });
        registration.registerMetric(TOTAL_REQUEST_TIME, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult, final ServletInfo servlet) {
                response.set(metricResult.getTotalRequestTime());
            }
        });
        registration.registerMetric(REQUEST_COUNT, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult, final ServletInfo servlet) {
                response.set(metricResult.getTotalRequests());
            }
        });
        registration.registerMetric(SERVLET_MAPPINGS, new AbstractMetricsHandler() {
            @Override
            void handle(final ModelNode response, final String name, final MetricsHandler.MetricResult metricResult, final ServletInfo servlet) {
                for (String mapping : servlet.getMappings()) {
                    response.add(mapping);
                }
            }
        });
    }

   
    abstract static class AbstractMetricsHandler implements OperationStepHandler {

        abstract void handle(ModelNode response, String name, MetricsHandler.MetricResult metricResult, ServletInfo infos);

        private ModelNode modSubsystemToUndertow(ModelNode node) {        	
        	for (ModelNode n: node.asList()) {
        		if (n.hasDefined("subsystem")) {
        			n.get("subsystem").set("undertow");
        		}
        	}
        	return node;
        }
        
        @Override
        public void execute(final OperationContext context, final ModelNode operation) throws OperationFailedException {
        	ModelNode modifiedPathNode = modSubsystemToUndertow(operation.get(ModelDescriptionConstants.OP_ADDR)); 
            final PathAddress address = PathAddress.pathAddress(modifiedPathNode);

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
                        final ServletInfo servlet = deploymentInfo.getServlets().get(name);
                        final ModelNode response = new ModelNode();
                        MetricsHandler.MetricResult result = collector != null ? collector.getMetrics(name) : null;
                        if (result == null) {
                            response.set(0);
                        } else {
                            handle(response, name, result, servlet);
                        }
                        context.getResult().set(response);
                    }
                }
            }, OperationContext.Stage.RUNTIME);
        }
    }

}
