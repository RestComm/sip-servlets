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
import org.wildfly.extension.undertow.UndertowService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;

/**
 *@author kakonyi.istvan@alerant.hu
 */
public class UndertowSipDeploymentProcessor implements DeploymentUnitProcessor {

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
            SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit
                    .getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);
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
        // TODO:
        final String defaultHost = "default-host";
        final String defaultServer = "default-server";

        String hostName = /* TODO:UndertowDeploymentProcessor. */hostNameOfDeployment(warMetaData, defaultHost);
        final String pathName = /* TODO:UndertowDeploymentProcessor. */pathNameOfDeployment(deploymentUnit, metaData);

        String serverInstanceName = metaData.getServerInstanceName() == null ? defaultServer : metaData
                .getServerInstanceName();

        // lets find udertowdeployment service
        final ServiceName deploymentServiceName = UndertowService.deploymentServiceName(serverInstanceName, hostName,
                pathName);
        ServiceController<?> deploymentServiceController = phaseContext.getServiceRegistry().getService(
                deploymentServiceName);
        UndertowDeploymentService deploymentService = (UndertowDeploymentService) deploymentServiceController
                .getValue();

        // lets find udertowdeploymentinfo service
        final ServiceName deploymentInfoServiceName = deploymentServiceName
                .append(UndertowDeploymentInfoService.SERVICE_NAME);
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
        final ServiceName sipDeploymentServiceName = deploymentServiceName
                .append(UndertowSipDeploymentService.SERVICE_NAME);
        UndertowSipDeploymentService sipDeploymentService = new UndertowSipDeploymentService(deploymentService,
                deploymentUnit);
        ServiceBuilder<UndertowSipDeploymentService> sipDeploymentServiceBuilder = phaseContext.getServiceTarget()
                .addService(sipDeploymentServiceName, sipDeploymentService);
        // this service depends on the base undertowdeployment service:
        sipDeploymentServiceBuilder.addDependency(deploymentServiceName);
        sipDeploymentServiceBuilder.install();

    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}