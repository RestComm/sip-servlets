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

import io.undertow.servlet.api.Deployment;

import javax.servlet.ServletException;

import org.jboss.as.server.ServerLogger;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;

/**
 *@author kakonyi.istvan@alerant.hu
 */
public class UndertowSipDeploymentService implements Service<UndertowSipDeploymentService> {
    public static final ServiceName SERVICE_NAME = ServiceName.of("UndertowSipDeploymentService");

    private UndertowDeploymentService deploymentService = null;
    private DeploymentUnit deploymentUnit = null;

    public UndertowSipDeploymentService(UndertowDeploymentService deploymentService, DeploymentUnit deploymentUnit) {
        this.deploymentService = deploymentService;
        this.deploymentUnit = deploymentUnit;
    }

    @Override
    public UndertowSipDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowSipDeploymentService.start()");

        Deployment deployment = this.deploymentService.getDeployment();

        DeploymentUnit anchorDu = SIPWebContext.getSipContextAnchorDu(this.deploymentUnit);
        SIPWebContext sipWebContext = anchorDu.getAttachment(SIPWebContext.ATTACHMENT_KEY);

        try {
            Module module = this.deploymentUnit.getAttachment(Attachments.MODULE);
            sipWebContext.init(deployment, module.getClassLoader());
            sipWebContext.start();
            sipWebContext.contextListenerStart();

        } catch (ServletException e) {
            ServerLogger.DEPLOYMENT_LOGGER.error(e.getMessage(), e);
        }
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowSipDeploymentService.start() finished");
    }

    @Override
    public void stop(StopContext context) {
        DeploymentUnit anchorDu = SIPWebContext.getSipContextAnchorDu(this.deploymentUnit);
        SIPWebContext sipWebContext = anchorDu.getAttachment(SIPWebContext.ATTACHMENT_KEY);

        try {
            if (sipWebContext != null) {
                sipWebContext.stop();
                sipWebContext.listenerStop();
            }
        } catch (ServletException e) {
            ServerLogger.DEPLOYMENT_LOGGER.error("UndertowSipDeploymentService.stop() ends with error: "+e.getMessage(), e);
        }
    }
}
