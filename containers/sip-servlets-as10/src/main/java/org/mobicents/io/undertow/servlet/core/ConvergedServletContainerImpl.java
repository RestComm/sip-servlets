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
package org.mobicents.io.undertow.servlet.core;

import java.lang.reflect.Field;
import java.util.Map;

import org.mobicents.io.undertow.servlet.api.ConvergedServletContainer;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.core.ServletContainerImpl;
/**
 * This class extends io.undertow.servlet.core.ServletContainerImpl to override its addDeployment() method.
 * This method creates ConvergedDeploymentManagerImpl which uses ConvergedServletContextImpl.
 *
 * @author kakonyi.istvan@alerant.hu
 */
public class ConvergedServletContainerImpl extends ServletContainerImpl implements ConvergedServletContainer{

    @Override
    public DeploymentManager addDeployment(final DeploymentInfo deployment) {
        final DeploymentInfo dep = deployment.clone();
        DeploymentManager deploymentManager = new ConvergedDeploymentManagerImpl(dep, this);

        //accessing parent fields using reflection:
        try{
            Field deploymentsField = ServletContainerImpl.class.getDeclaredField("deployments");
            deploymentsField.setAccessible(true);
            Map<String, DeploymentManager> deployments = (Map<String, DeploymentManager>) deploymentsField.get(this);
            deployments.put(dep.getDeploymentName(), deploymentManager);
            deploymentsField.setAccessible(false);

            Field deploymentsByPathField = ServletContainerImpl.class.getDeclaredField("deploymentsByPath");
            deploymentsByPathField.setAccessible(true);
            Map<String, DeploymentManager> deploymentsByPath = (Map<String, DeploymentManager>) deploymentsByPathField.get(this);
            deploymentsByPath.put(dep.getContextPath(), deploymentManager);
            deploymentsByPathField.setAccessible(false);
        }catch(NoSuchFieldException | IllegalAccessException e){
            throw new RuntimeException(e);
        }
        return deploymentManager;
    }
}
