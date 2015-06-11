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

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentInfoFacade;
import io.undertow.servlet.core.ConvergedSessionManagerFactory;

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
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;
/**
 *@author alerant.appngin@gmail.com
 */
public class UndertowDeploymentInfoReflectionService implements Service<UndertowDeploymentInfoReflectionService> {
    public static final ServiceName SERVICE_NAME = ServiceName.of("UndertowDeploymentInfoReflectionService");

    private InjectedValue<UndertowDeploymentInfoService> deploymentInfoService = new InjectedValue<UndertowDeploymentInfoService>();

    private DeploymentUnit deploymentUnit = null;
    private SIPWebContext webContext = null;
    
    public UndertowDeploymentInfoReflectionService(DeploymentUnit deploymentUnit) throws DeploymentUnitProcessingException{
    	this.deploymentUnit = deploymentUnit;

        //lets init sipWebContext:
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
    public UndertowDeploymentInfoReflectionService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start()");
        SIPContextFactory factory = this.deploymentUnit.getAttachment(SIPContextFactory.ATTACHMENT);

        //lets add our custom sip session manager factory:
        if(this.deploymentInfoService!=null && this.deploymentInfoService.getValue()!=null && this.deploymentInfoService.getValue().getValue()!=null){
            DeploymentInfo info =  this.deploymentInfoService.getValue().getValue();
            
            DeploymentInfoFacade facade = new DeploymentInfoFacade();
            try {
                facade.addDeploymentInfo(info);
                facade.setSessionManagerFactory(new ConvergedSessionManagerFactory());
                this.deploymentUnit.putAttachment(DeploymentInfoFacade.ATTACHMENT_KEY, facade);
            } catch (ServletException e) {
                throw new StartException(e);  
            }
        }else{
            throw new StartException("DeploymentInfoService not properly initialized!");  
        }
        
        try {
			this.webContext = factory.addDeplyomentUnitToContext(this.deploymentUnit, this.deploymentInfoService.getValue(), webContext);
		} catch (ServletException e) {
			throw new StartException(e);  
		}

        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start() finished");
    }

    public InjectedValue<UndertowDeploymentInfoService> getDeploymentInfoServiceInjectedValue() {
        return deploymentInfoService;
    }

    @Override
    public void stop(StopContext context) {
        // TODO Auto-generated method stub
    }
}
