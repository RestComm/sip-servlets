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

import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.core.ManagedFilter;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.core.ManagedServlets;
import io.undertow.servlet.handlers.ServletHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.jboss.as.server.ServerLogger;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.mobicents.servlet.sip.undertow.SipServletImpl;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;

/**
 *@author kakonyi.istvan@alerant.hu
 */
public class UndertowSipDeploymentService implements Service<UndertowSipDeploymentService> {
    public static final ServiceName SERVICE_NAME = ServiceName.of("UndertowSipDeploymentService");

    private UndertowDeploymentService deploymentService = null;
    private DeploymentUnit deploymentUnit = null;

    public UndertowSipDeploymentService(UndertowDeploymentService deploymentService, DeploymentUnit deploymentUnit) {
        this.deploymentService = deploymentService;
        this.deploymentUnit = deploymentUnit;
    }

    @Override
    public UndertowSipDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowSipDeploymentService.start()");

        Deployment deployment = this.deploymentService.getDeployment();
        SIPWebContext sipWebContext = this.deploymentUnit.getAttachment(SIPWebContext.ATTACHMENT_KEY);

        try {
            Module module = this.deploymentUnit.getAttachment(Attachments.MODULE);
            sipWebContext.init(deployment, module.getClassLoader());
            sipWebContext.start();
            sipWebContext.contextListenerStart();

            // lets init http servlets only here to be able to do injections provided by the SIPWebContext
            // sipServlets initialized earlier in context.start() to be able to notify its contextlisteners
            List<SipServletImpl> sipServlets = new ArrayList<SipServletImpl>();
            /*
             * for (String servletName
             * :((ConvergedDeploymentInfo)context.getDeploymentInfo()).getSipServlets().keySet()) { SipServletImpl
             * managedServlet = (SipServletImpl) context.getChildrenMap().get(servletName);
             * sipServlets.add(managedServlet); }
             */

            this.createServlets(sipWebContext.getDeployment().getServlets(), sipServlets, sipWebContext);

        } catch (ServletException e) {
            ServerLogger.DEPLOYMENT_LOGGER.error(e.getMessage(), e);
        }
        ServerLogger.DEPLOYMENT_LOGGER.debug("UndertowSipDeploymentService.start() finished");
    }

    @Override
    public void stop(StopContext context) {
        SIPWebContext sipWebContext = this.deploymentUnit.getAttachment(SIPWebContext.ATTACHMENT_KEY);
        try {
            if (sipWebContext != null) {
                sipWebContext.stop();
                sipWebContext.listenerStop();
            }
        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void createServlets(ManagedServlets servlets, List<SipServletImpl> sipServlets, SIPWebContext context)
            throws ServletException {
        final TreeMap<Integer, List<ManagedServlet>> loadOnStartup = new TreeMap<>();
        for (Map.Entry<String, ServletHandler> entry : servlets.getServletHandlers().entrySet()) {
            ManagedServlet servlet = entry.getValue().getManagedServlet();
            Integer loadOnStartupNumber = servlet.getServletInfo().getLoadOnStartup();
            if (loadOnStartupNumber != null) {
                if (loadOnStartupNumber < 0) {
                    continue;
                }
                List<ManagedServlet> list = loadOnStartup.get(loadOnStartupNumber);
                if (list == null) {
                    loadOnStartup.put(loadOnStartupNumber, list = new ArrayList<>());
                }
                list.add(servlet);
            }
        }

        for (SipServletImpl servlet : sipServlets) {
            Integer loadOnStartupNumber = servlet.getServletInfo().getLoadOnStartup();
            if (loadOnStartupNumber != null) {
                if (loadOnStartupNumber < 0) {
                    continue;
                }
                List<ManagedServlet> list = loadOnStartup.get(loadOnStartupNumber);
                if (list == null) {
                    loadOnStartup.put(loadOnStartupNumber, list = new ArrayList<>());
                }
                list.add(servlet);
            }
        }

        for (Map.Entry<Integer, List<ManagedServlet>> load : loadOnStartup.entrySet()) {
            for (ManagedServlet servlet : load.getValue()) {
                servlet.createServlet();
            }
        }
        if (context.getDeploymentInfoFacade().getDeploymentInfo().isEagerFilterInit()) {
            for (ManagedFilter filter : context.getDeployment().getFilters().getFilters().values()) {
                filter.createFilter();
            }
        }
    }
}
