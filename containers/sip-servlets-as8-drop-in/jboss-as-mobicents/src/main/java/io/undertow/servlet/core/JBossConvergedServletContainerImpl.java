package io.undertow.servlet.core;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.JBossConvergedServletContainer;
import io.undertow.servlet.core.ConvergedServletContainerImpl;

public class JBossConvergedServletContainerImpl extends ConvergedServletContainerImpl implements JBossConvergedServletContainer
{
    @Override
    public DeploymentManager addDeployment(final DeploymentInfo deployment) {
        DeploymentInfo dep = deployment.clone();
        
        DeploymentManager deploymentManager = new JBossConvergedDeploymentManagerImpl(dep, this);
        deployments.put(dep.getDeploymentName(), deploymentManager);
        deploymentsByPath.put(dep.getContextPath(), deploymentManager);
        return deploymentManager;
    }
}
