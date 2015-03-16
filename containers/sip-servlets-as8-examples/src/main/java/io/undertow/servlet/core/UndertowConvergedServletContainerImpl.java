package io.undertow.servlet.core;

import io.undertow.servlet.api.UndertowConvergedServletContainer;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

public class UndertowConvergedServletContainerImpl extends ConvergedServletContainerImpl implements UndertowConvergedServletContainer{

    @Override
    public DeploymentManager addDeployment(final DeploymentInfo deployment) {
        DeploymentInfo dep = deployment.clone();
        
        DeploymentManager deploymentManager = new UndertowConvergedDeploymentManagerImpl(dep, this);
        deployments.put(dep.getDeploymentName(), deploymentManager);
        deploymentsByPath.put(dep.getContextPath(), deploymentManager);
        return deploymentManager;
    }

}
