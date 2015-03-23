package org.mobicents.as8.deployment;

import io.undertow.servlet.core.ManagedFilter;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.core.ManagedServlets;
import io.undertow.servlet.handlers.ServletHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.mobicents.servlet.sip.undertow.SipServletImpl;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;

public class UndertowSipDeploymentService implements Service<UndertowSipDeploymentService> {
    public static final ServiceName SERVICE_NAME = ServiceName.of("UndertowSipDeploymentService");

    private UndertowDeploymentService deploymentService = null;
    private UndertowDeploymentInfoService deploymentInfoService = null;
    private DeploymentUnit deploymentUnit = null;

    public UndertowSipDeploymentService(UndertowDeploymentService deploymentService,
            UndertowDeploymentInfoService deploymentInfoService, DeploymentUnit deploymentUnit) {
        this.deploymentInfoService = deploymentInfoService;
        this.deploymentService = deploymentService;
        this.deploymentUnit = deploymentUnit;
    }

    @Override
    public UndertowSipDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    @Override
    public void start(StartContext context) throws StartException {
        System.out.println("UndertowSipDeploymentService.start()");
        SIPWebContext sipWebContext = (SIPWebContext) this.deploymentService.getDeployment();

        SIPContextFactory factory = this.deploymentUnit.getAttachment(SIPContextFactory.ATTACHMENT);
        factory.addDeplyomentUnitToContext(this.deploymentUnit, this.deploymentInfoService, sipWebContext);

        try {
            Module module = this.deploymentUnit.getAttachment(Attachments.MODULE);
            sipWebContext.addSipContextClassLoader(module.getClassLoader());
            sipWebContext.init();
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

            this.createServlets(sipWebContext.getServlets(), sipServlets, sipWebContext);

        } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("UndertowSipDeploymentService.start() finished");
    }

    @Override
    public void stop(StopContext context) {
        SIPWebContext sipWebContext = (SIPWebContext) this.deploymentService.getDeployment();
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
        if (context.getDeploymentInfo().isEagerFilterInit()) {
            for (ManagedFilter filter : context.getFilters().getFilters().values()) {
                filter.createFilter();
            }
        }
    }
}
