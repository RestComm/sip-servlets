package org.mobicents.io.undertow.servlet.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.mobicents.servlet.sip.startup.ConvergedServletContextImpl;

import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.core.DeploymentImpl;
import io.undertow.servlet.core.Lifecycle;
import io.undertow.servlet.core.ManagedServlets;

public class ConvergedDeploymentImpl extends DeploymentImpl{

	private final List<Lifecycle> lifecycleObjects = new ArrayList<>();
	private final ConvergedManagedServlets servlets;
    private volatile ConvergedServletContextImpl convergedServletContext;
    
    public ConvergedDeploymentImpl(DeploymentManager deploymentManager, final DeploymentInfo deploymentInfo, ServletContainer servletContainer) {
        super(deploymentManager,deploymentInfo,servletContainer);
        this.servlets = new ConvergedManagedServlets(this, super.getServletPaths());
    }

    public ManagedServlets getServlets() {
        return this.servlets;
    }

    public void setConvergedServletContext(final ConvergedServletContextImpl convergedServletContext) {
        this.convergedServletContext = convergedServletContext;
    }

    public ConvergedServletContextImpl getConvergedServletContext() {
        return convergedServletContext;
    }

    public void addLifecycleObjects(final Collection<Lifecycle> objects) {
        lifecycleObjects.addAll(objects);
    }

    public void addLifecycleObjects(final Lifecycle... objects) {
        lifecycleObjects.addAll(Arrays.asList(objects));
    }

    public List<Lifecycle> getLifecycleObjects() {
        return Collections.unmodifiableList(lifecycleObjects);
    }

    void destroy(){
        getApplicationListeners().contextDestroyed();
        getApplicationListeners().stop();
        if (convergedServletContext!=null){
            convergedServletContext.destroy();
        }
        convergedServletContext = null;
    }
}
