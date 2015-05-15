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

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.DeploymentInfo;

import java.lang.reflect.Field;

import org.jboss.as.server.ServerLogger;
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

    private static final String DEPLOYMENTINFOFIELDNAME = "deploymentInfo";

    private InjectedValue<UndertowDeploymentInfoService> deploymentInfoService = new InjectedValue<UndertowDeploymentInfoService>();

    @Override
    public UndertowDeploymentInfoReflectionService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start()");
        DeploymentInfo info = this.deploymentInfoService.getValue().getValue();
        ConvergedDeploymentInfo convergedInfo = new ConvergedDeploymentInfo(info);

        Class<? extends UndertowDeploymentInfoService> serviceClass = deploymentInfoService.getValue().getClass();
        for (Field field : serviceClass.getDeclaredFields()) {
            if (UndertowDeploymentInfoReflectionService.DEPLOYMENTINFOFIELDNAME.equals(field.getName())) {
                field.setAccessible(true);
                try {
                    field.set(deploymentInfoService.getValue(), convergedInfo);
                    /*
                     * this.deploymentService.getValue().getDeploymentInfoInjectedValue().setValue( new
                     * ImmediateValue<DeploymentInfo>(convergedInfo));
                     */
                    ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowDeploymentInfoReflectionService.start() deploymentInfo injected:"
                            + convergedInfo);
                } catch (IllegalArgumentException e) {
                    ServerLogger.DEPLOYMENT_LOGGER.error(e.getMessage(),e);
                } catch (IllegalAccessException e) {
                    ServerLogger.DEPLOYMENT_LOGGER.error(e.getMessage(),e);
                }
                field.setAccessible(false);
                break;
            }
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
