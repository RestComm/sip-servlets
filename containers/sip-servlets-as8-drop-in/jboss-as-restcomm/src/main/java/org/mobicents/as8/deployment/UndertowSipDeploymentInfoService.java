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

import io.undertow.servlet.api.DeploymentInfo;
import org.mobicents.io.undertow.servlet.api.DeploymentInfoFacade;
import org.mobicents.io.undertow.servlet.core.ConvergedSessionManagerFactory;

import javax.servlet.ServletException;

import org.jboss.as.server.ServerLogger;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.extension.undertow.Host;
import org.wildfly.extension.undertow.Server;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;

/**
 * @author kakonyi.istvan@alerant.hu
 */
public class UndertowSipDeploymentInfoService implements Service<UndertowSipDeploymentInfoService> {
    public static final ServiceName SERVICE_NAME = ServiceName.of("UndertowSipDeploymentInfoService");

    private InjectedValue<UndertowDeploymentInfoService> deploymentInfoService = new InjectedValue<UndertowDeploymentInfoService>();

    private DeploymentUnit deploymentUnit = null;
    private SIPWebContext webContext = null;
    private Server server = null;

    public UndertowSipDeploymentInfoService(DeploymentUnit deploymentUnit, Server server) throws DeploymentUnitProcessingException {
        this.deploymentUnit = deploymentUnit;
        this.server = server;

        // lets init sipWebContext:
        this.webContext = new SIPWebContext();
        try {
            this.webContext.addDeploymentUnit(this.deploymentUnit);

            this.webContext.initDispatcher();
            this.webContext.prepareServletContextServices();
            this.webContext.attachContext();
        } catch (ServletException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    @Override
    public UndertowSipDeploymentInfoService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowSipDeploymentInfoService.start()");
        SIPContextFactory factory = this.deploymentUnit.getAttachment(SIPContextFactory.ATTACHMENT);

        // lets add our custom sip session manager factory:
        if (this.deploymentInfoService != null && this.deploymentInfoService.getValue() != null
                && this.deploymentInfoService.getValue().getValue() != null) {
            DeploymentInfo info = this.deploymentInfoService.getValue().getValue();

            DeploymentInfoFacade facade = new DeploymentInfoFacade();
            try {
                facade.addDeploymentInfo(info);
                facade.setSessionManagerFactory(new ConvergedSessionManagerFactory());
                this.deploymentUnit.putAttachment(DeploymentInfoFacade.ATTACHMENT_KEY, facade);

                if(server!=null){
                    this.webContext.setWebServerListeners(server.getListeners());
                    for(Host host: server.getHosts()){
                        if(host!=null && host.getName().equals(info.getHostName())){
                            this.webContext.setHostOfDeployment(host);
                            break;
                        }
                    }
                }
            } catch (ServletException e) {
                throw new StartException(e);
            }
        } else {
            throw new StartException("DeploymentInfoService not properly initialized!");
        }

        try {
            this.webContext = factory.addDeplyomentUnitToContext(this.deploymentUnit, this.deploymentInfoService.getValue(),
                    webContext);
        } catch (ServletException e) {
            throw new StartException(e);
        }

        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowSipDeploymentInfoService.start() finished");
    }

    public InjectedValue<UndertowDeploymentInfoService> getDeploymentInfoServiceInjectedValue() {
        return deploymentInfoService;
    }

    @Override
    public void stop(StopContext context) {
    }
}
