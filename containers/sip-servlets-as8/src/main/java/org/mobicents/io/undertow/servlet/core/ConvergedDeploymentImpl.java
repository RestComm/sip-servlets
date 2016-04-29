/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
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
