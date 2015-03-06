package io.undertow.servlet.core;

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.ConvergedServletContainer;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;

public class ConvergedServletContainerImpl extends ServletContainerImpl implements ConvergedServletContainer{

    @Override
    public DeploymentManager addDeployment(final DeploymentInfo deployment) {
        DeploymentInfo dep = deployment.clone();
        
        DeploymentManager deploymentManager = new ConvergedDeploymentManagerImpl(dep, this);
        deployments.put(dep.getDeploymentName(), deploymentManager);
        deploymentsByPath.put(dep.getContextPath(), deploymentManager);
        return deploymentManager;
    }

}
