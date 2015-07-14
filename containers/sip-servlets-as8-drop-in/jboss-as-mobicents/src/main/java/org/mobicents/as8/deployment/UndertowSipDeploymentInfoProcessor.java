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

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.as.web.common.WebComponentDescription;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.wildfly.extension.undertow.Server;
import org.wildfly.extension.undertow.UndertowService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;
/**
 *@author kakonyi.istvan@alerant.hu
 */
public class UndertowSipDeploymentInfoProcessor implements DeploymentUnitProcessor{

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

        //lets find default server service:
        final ServiceName defaultServerServiceName = UndertowService.DEFAULT_SERVER;
        final ServiceController<?> defaultServerServiceController = phaseContext.getServiceRegistry().getService(defaultServerServiceName);
        final Server defaultServerService = (Server) defaultServerServiceController.getValue();

        final String defaultHost = defaultServerService.getDefaultHost();
        final String defaultServer = defaultServerService.getName();

        final String hostName = UndertowSipDeploymentProcessor.hostNameOfDeployment(warMetaData, defaultHost);
        final String pathName = UndertowSipDeploymentProcessor.pathNameOfDeployment(deploymentUnit, metaData);

        final String serverInstanceName = metaData.getServerInstanceName() == null ? defaultServer : metaData.getServerInstanceName();
        final ServiceName deploymentServiceName = UndertowService.deploymentServiceName(serverInstanceName, hostName, pathName);
        final ServiceName deploymentInfoServiceName = deploymentServiceName.append(UndertowDeploymentInfoService.SERVICE_NAME);

        //instantiate injector service
        final UndertowSipDeploymentInfoService sipDeploymentInfoService = new UndertowSipDeploymentInfoService(deploymentUnit);
        final ServiceName sipDeploymentInfoServiceName = deploymentServiceName.append(UndertowSipDeploymentInfoService.SERVICE_NAME);
        //lets earn that deploymentService will depend on this service:
        phaseContext.getDeploymentUnit().getAttachment(WebComponentDescription.WEB_COMPONENTS).add(sipDeploymentInfoServiceName);

        //make injector service to be dependent from deploymentInfoService:
        final ServiceBuilder<UndertowSipDeploymentInfoService> infoInjectorBuilder = phaseContext.getServiceTarget().addService(sipDeploymentInfoServiceName, sipDeploymentInfoService);
        infoInjectorBuilder.addDependency(deploymentInfoServiceName);
        infoInjectorBuilder.install();
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

}
