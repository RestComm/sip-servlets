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

import java.util.Map;

import org.mobicents.servlet.sip.undertow.SipServletImpl;

import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.core.DeploymentImpl;
import io.undertow.servlet.core.ManagedServlets;
import io.undertow.servlet.handlers.ServletHandler;
import io.undertow.servlet.handlers.ServletPathMatches;
import io.undertow.util.CopyOnWriteMap;

public class ConvergedManagedServlets extends ManagedServlets{

    private final ConvergedDeploymentImpl deployment;
    private final ServletPathMatches servletPaths;

    private final Map<String, ServletHandler> managedServletMap = new CopyOnWriteMap<>();

    public ConvergedManagedServlets(final DeploymentImpl deployment, final ServletPathMatches servletPaths) {
        super(deployment,servletPaths);
        this.deployment = (ConvergedDeploymentImpl) deployment;
        this.servletPaths = servletPaths;
    }

    public ServletHandler addServlet(final ServletInfo servletInfo) {
        SipServletImpl managedServlet = new SipServletImpl(servletInfo, ((ConvergedDeploymentImpl) deployment).getConvergedServletContext());
        ServletHandler servletHandler = new ServletHandler(managedServlet);
        managedServletMap.put(servletInfo.getName(), servletHandler);
        deployment.addLifecycleObjects(managedServlet);
        this.servletPaths.invalidate();

        return servletHandler;
    }

}

