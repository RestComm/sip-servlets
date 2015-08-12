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
package org.mobicents.as8.deployment;

import static org.wildfly.extension.undertow.UndertowMessages.MESSAGES;

import java.util.Collection;
import java.util.Collections;

import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.wildfly.extension.undertow.Server;
import org.wildfly.extension.undertow.UndertowService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;

/**
 * @author kakonyi.istvan@alerant.hu
 */
public class UndertowSipDeploymentProcessor implements DeploymentUnitProcessor {

    // copied from "protected" UndertowDeploymentProcessor.hostNameOfDeployment()
    public static String hostNameOfDeployment(final WarMetaData metaData, final String defaultHost) {
        Collection<String> hostNames = null;
        if (metaData.getMergedJBossWebMetaData() != null) {
            hostNames = metaData.getMergedJBossWebMetaData().getVirtualHosts();
        }
        if (hostNames == null || hostNames.isEmpty()) {
            hostNames = Collections.singleton(defaultHost);
        }
        String hostName = hostNames.iterator().next();
        if (hostName == null) {
            throw MESSAGES.nullHostName();
        }
        return hostName;
    }

    // copied from "protected" UndertowDeploymentProcessor.pathNameOfDeployment()
    public static String pathNameOfDeployment(final DeploymentUnit deploymentUnit, final JBossWebMetaData metaData) {
        String pathName;
        if (metaData.getContextRoot() == null) {
            final EEModuleDescription description = deploymentUnit
                    .getAttachment(org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION);
            if (description != null) {
                // if there is an EEModuleDescription we need to take into account that the module name may have been
                // overridden
                pathName = "/" + description.getModuleName();
            } else {
                pathName = "/" + deploymentUnit.getName().substring(0, deploymentUnit.getName().length() - 4);
            }
        } else {
            pathName = metaData.getContextRoot();
            if (pathName.length() > 0 && pathName.charAt(0) != '/') {
                pathName = "/" + pathName;
            }
        }
        return pathName;
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        if (sipMetaData == null) {
            SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit.getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);
            if (sipAnnotationMetaData == null || !sipAnnotationMetaData.isSipApplicationAnnotationPresent()) {
                // http://code.google.com/p/sipservlets/issues/detail?id=168
                // When no sip.xml but annotations only, Application is not recognized as SIP App by AS7

                // In case there is no metadata attached, it means this is not a sip deployment, so we can safely
                // ignore
                // it!
                return;
            }
        }

        final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        final JBossWebMetaData metaData = warMetaData.getMergedJBossWebMetaData();

        // lets find default server service:
        final ServiceName defaultServerServiceName = UndertowService.DEFAULT_SERVER;
        ServiceController<?> defaultServerServiceController = phaseContext.getServiceRegistry().getService(
                defaultServerServiceName);
        Server defaultServerService = (Server) defaultServerServiceController.getValue();

        final String defaultServer = defaultServerService.getName();
        final String defaultHost = defaultServerService.getDefaultHost();

        String hostName = UndertowSipDeploymentProcessor.hostNameOfDeployment(warMetaData, defaultHost);
        final String pathName = UndertowSipDeploymentProcessor.pathNameOfDeployment(deploymentUnit, metaData);

        String serverInstanceName = metaData.getServerInstanceName() == null ? defaultServer : metaData.getServerInstanceName();

        // lets find udertowdeployment service
        final ServiceName deploymentServiceName = UndertowService.deploymentServiceName(serverInstanceName, hostName, pathName);
        ServiceController<?> deploymentServiceController = phaseContext.getServiceRegistry().getService(deploymentServiceName);
        UndertowDeploymentService deploymentService = (UndertowDeploymentService) deploymentServiceController.getValue();

        // lets find udertowdeploymentinfo service
        final ServiceName deploymentInfoServiceName = deploymentServiceName.append(UndertowDeploymentInfoService.SERVICE_NAME);
        ServiceController<?> deploymentInfoServiceController = phaseContext.getServiceRegistry().getService(
                deploymentInfoServiceName);
        UndertowDeploymentInfoService deploymentInfoService = (UndertowDeploymentInfoService) deploymentInfoServiceController
                .getService();

        // lets inject undertowdeployment service to sip deploymentinfo service:
        final ServiceName sipDeploymentInfoServiceName = deploymentServiceName
                .append(UndertowSipDeploymentInfoService.SERVICE_NAME);
        ServiceController<?> sipDeplyomentInfoServiceController = phaseContext.getServiceRegistry().getService(
                sipDeploymentInfoServiceName);
        UndertowSipDeploymentInfoService sipDeplyomentInfoService = (UndertowSipDeploymentInfoService) sipDeplyomentInfoServiceController
                .getService();
        sipDeplyomentInfoService.getDeploymentInfoServiceInjectedValue().setValue(
                new ImmediateValue<UndertowDeploymentInfoService>(deploymentInfoService));

        // initiate undertowsipdeployment service:
        final ServiceName sipDeploymentServiceName = deploymentServiceName.append(UndertowSipDeploymentService.SERVICE_NAME);
        UndertowSipDeploymentService sipDeploymentService = new UndertowSipDeploymentService(deploymentService, deploymentUnit);
        ServiceBuilder<UndertowSipDeploymentService> sipDeploymentServiceBuilder = phaseContext.getServiceTarget().addService(
                sipDeploymentServiceName, sipDeploymentService);

        // this service depends on the base undertowdeployment service:
        sipDeploymentServiceBuilder.addDependency(deploymentServiceName);
        sipDeploymentServiceBuilder.install();
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
