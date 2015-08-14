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

import java.util.List;

import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.mobicents.as8.SipConnectorService;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
/**
 * @author kakonyi.istvan@alerant.hu
 */
public class UndertowSipConnectorActivateProcessor implements DeploymentUnitProcessor {

    @SuppressWarnings("unchecked")
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

        //this service will start up and activate sip connector(s) AFTER all sip deployments finished in order to prevent the sip server to handle incoming messages before sip applications ready:
        final UndertowSipConnectorActivateService sipConnectorActivateService = new UndertowSipConnectorActivateService();
        final ServiceName sipConnectorActivateServiceName = UndertowSipConnectorActivateService.SERVICE_NAME;

        final ServiceBuilder<UndertowSipConnectorActivateService> sipConnectorActivateServiceBuilder = phaseContext.getServiceTarget()
                .addService(sipConnectorActivateServiceName, sipConnectorActivateService);

        List<ServiceName> serviceNames = phaseContext.getServiceRegistry().getServiceNames();
        for(ServiceName serviceName: serviceNames){
            ServiceController<?> serviceController = phaseContext.getServiceRegistry().getService(serviceName);
            if(serviceController.getService() instanceof UndertowSipDeploymentService){
                //don't start connnector service activator until all sip deployments finished:
                sipConnectorActivateServiceBuilder.addDependency(serviceName);
            }
            else if(serviceController.getService() instanceof SipConnectorService) {
                //set connector service controller to activator service:
                sipConnectorActivateService.addServiceController((ServiceController<SipConnectorService>)serviceController);
            }
        }
        sipConnectorActivateServiceBuilder.install();

    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

}
