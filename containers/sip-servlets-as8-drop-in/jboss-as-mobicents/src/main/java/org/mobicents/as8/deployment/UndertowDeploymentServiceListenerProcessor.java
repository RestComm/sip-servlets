package org.mobicents.as8.deployment;

import static org.wildfly.extension.undertow.UndertowMessages.MESSAGES;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;

import io.undertow.servlet.api.ConvergedDeploymentInfo;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.core.ManagedFilter;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.core.ManagedServlets;
import io.undertow.servlet.handlers.ServletHandler;

import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.modules.Module;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.value.ImmediateValue;
import org.mobicents.as8.ServletContainerServiceListener;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.servlet.sip.undertow.SipServletImpl;
import org.wildfly.extension.undertow.UndertowService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentService;

@SuppressWarnings("deprecation")
public class UndertowDeploymentServiceListenerProcessor implements DeploymentUnitProcessor {

    static String hostNameOfDeployment(final WarMetaData metaData, final String defaultHost) {
        Collection<String> hostNames = null;
        if (metaData.getMergedJBossWebMetaData() != null) {
            hostNames = metaData.getMergedJBossWebMetaData().getVirtualHosts();
        }
        if (hostNames == null || hostNames.isEmpty()) {
            hostNames = Collections.singleton(defaultHost);
        }
        String hostName = hostNames.iterator().next();
        if (hostName == null) {
            throw MESSAGES.nullHostName();
        }
        return hostName;
    }

    static String pathNameOfDeployment(final DeploymentUnit deploymentUnit, final JBossWebMetaData metaData) {
        String pathName;
        if (metaData.getContextRoot() == null) {
            final EEModuleDescription description = deploymentUnit
                    .getAttachment(org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION);
            if (description != null) {
                // if there is an EEModuleDescription we need to take into account that the module name may have been
                // overridden
                pathName = "/" + description.getModuleName();
            } else {
                pathName = "/" + deploymentUnit.getName().substring(0, deploymentUnit.getName().length() - 4);
            }
        } else {
            pathName = metaData.getContextRoot();
            if (pathName.length() > 0 && pathName.charAt(0) != '/') {
                pathName = "/" + pathName;
            }
        }
        return pathName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        ServletContainerServiceListener servletContainerServiceListener = ServletContainerServiceListener.newInstance();
        // lets wait till ServletContainerServiceListener change the container for us:
        servletContainerServiceListener.acquireSemaphore();
        // after the change we should release the semaphore:
        servletContainerServiceListener.releaseSemaphore();

        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        if (sipMetaData == null) {
            SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit
                    .getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);
            if (sipAnnotationMetaData == null || !sipAnnotationMetaData.isSipApplicationAnnotationPresent()) {
                // http://code.google.com/p/sipservlets/issues/detail?id=168
                // When no sip.xml but annotations only, Application is not recognized as SIP App by AS7

                // In case there is no metadata attached, it means this is not a sip deployment, so we can safely
                // ignore
                // it!
                return;
            }
        }

        final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        final JBossWebMetaData metaData = warMetaData.getMergedJBossWebMetaData();
        // TODO:
        final String defaultHost = "default-host";
        final String defaultServer = "default-server";

        String hostName = /* TODO:UndertowDeploymentProcessor. */hostNameOfDeployment(warMetaData, defaultHost);
        final String pathName = /* TODO:UndertowDeploymentProcessor. */pathNameOfDeployment(deploymentUnit, metaData);

        String serverInstanceName = metaData.getServerInstanceName() == null ? defaultServer : metaData
                .getServerInstanceName();

        final ServiceName deploymentServiceName = UndertowService.deploymentServiceName(serverInstanceName, hostName,
                pathName);
        ServiceController<?> deploymentServiceController = phaseContext.getServiceRegistry().getService(
                deploymentServiceName);
        UndertowDeploymentService deploymentService = (UndertowDeploymentService) deploymentServiceController
                .getValue();

        final ServiceName deploymentInfoServiceName = deploymentServiceName
                .append(UndertowDeploymentInfoService.SERVICE_NAME);
        ServiceController<?> deploymentInfoServiceController = phaseContext.getServiceRegistry().getService(
                deploymentInfoServiceName);
        UndertowDeploymentInfoService deplyomentInfoService = (UndertowDeploymentInfoService) deploymentInfoServiceController
                .getService();

        UndertowDeploymentServiceListener deploymentListener = new UndertowDeploymentServiceListener(deploymentService, deplyomentInfoService,
                deploymentUnit);
        deploymentServiceController.addListener(deploymentListener);
        
        UndertowDeploymentInfoServiceListener deploymentInfoListener = new UndertowDeploymentInfoServiceListener(
                deplyomentInfoService, deploymentServiceController, deploymentUnit);
        deploymentInfoServiceController.addListener(deploymentInfoListener);
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

    @SuppressWarnings({ "rawtypes" })
    class UndertowDeploymentInfoServiceListener extends AbstractServiceListener implements ServiceListener {
        private static final String DEPLOYMENTINFOFIELDNAME = "deploymentInfo";

        private UndertowDeploymentInfoService deploymentInfoservice = null;
        private UndertowDeploymentService deploymentService = null;
        //private ServiceController<?> deploymentServiceController = null;

        private DeploymentUnit deploymentUnit = null;

        public UndertowDeploymentInfoServiceListener(UndertowDeploymentInfoService deploymentInfoService,
                ServiceController<?> deploymentServiceController, DeploymentUnit deploymentUnit) {
            this.deploymentInfoservice = deploymentInfoService;
            this.deploymentUnit = deploymentUnit;
            //this.deploymentServiceController = deploymentServiceController;
            this.deploymentService = (UndertowDeploymentService) deploymentServiceController.getValue();

        }

        @Override
        public void transition(final ServiceController controller, final ServiceController.Transition transition) {
            //System.out.println("UndertowDeploymentInfoServiceListener.transition():" + transition);
            if (transition == ServiceController.Transition.STARTING_to_UP) {

                while (deploymentInfoservice.getValue() == null) {
                    // TODO timeout
                }

                DeploymentInfo info = this.deploymentInfoservice.getValue();
                ConvergedDeploymentInfo convergedInfo = new ConvergedDeploymentInfo(info);

                Class serviceClass = deploymentInfoservice.getClass();
                for (Field field : serviceClass.getDeclaredFields()) {
                    if (UndertowDeploymentInfoServiceListener.DEPLOYMENTINFOFIELDNAME.equals(field.getName())) {
                        field.setAccessible(true);
                        try {
                            field.set(deploymentInfoservice, convergedInfo);
                            this.deploymentService.getDeploymentInfoInjectedValue().setValue(
                                    new ImmediateValue<DeploymentInfo>(convergedInfo));
                            //System.out.println("UndertowDeploymentInfoServiceListener.transition() deploymentInfo injected!");
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        field.setAccessible(false);
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes" })
    class UndertowDeploymentServiceListener extends AbstractServiceListener implements ServiceListener {
        private UndertowDeploymentService deploymentService = null;
        private UndertowDeploymentInfoService deploymentInfoservice = null;
        private DeploymentUnit deploymentUnit = null;

        public UndertowDeploymentServiceListener(UndertowDeploymentService deploymentService, UndertowDeploymentInfoService deploymentInfoservice, DeploymentUnit deploymentUnit) {
            this.deploymentService = deploymentService;
            this.deploymentUnit = deploymentUnit;
            this.deploymentInfoservice = deploymentInfoservice;

        }

        private void createServlets(ManagedServlets servlets, List<SipServletImpl> sipServlets, SIPWebContext context) throws ServletException{
            final TreeMap<Integer, List<ManagedServlet>> loadOnStartup = new TreeMap<>();
            for(Map.Entry<String, ServletHandler> entry: servlets.getServletHandlers().entrySet()) {
                ManagedServlet servlet = entry.getValue().getManagedServlet();
                Integer loadOnStartupNumber = servlet.getServletInfo().getLoadOnStartup();
                if(loadOnStartupNumber != null) {
                    if(loadOnStartupNumber < 0) {
                        continue;
                    }
                    List<ManagedServlet> list = loadOnStartup.get(loadOnStartupNumber);
                    if(list == null) {
                        loadOnStartup.put(loadOnStartupNumber, list = new ArrayList<>());
                    }
                    list.add(servlet);
                }
            }

            for(SipServletImpl servlet: sipServlets) {
                Integer loadOnStartupNumber = servlet.getServletInfo().getLoadOnStartup();
                if(loadOnStartupNumber != null) {
                    if(loadOnStartupNumber < 0) {
                        continue;
                    }
                    List<ManagedServlet> list = loadOnStartup.get(loadOnStartupNumber);
                    if(list == null) {
                        loadOnStartup.put(loadOnStartupNumber, list = new ArrayList<>());
                    }
                    list.add(servlet);
                }
            }
            
            for(Map.Entry<Integer, List<ManagedServlet>> load : loadOnStartup.entrySet()) {
                for(ManagedServlet servlet : load.getValue()) {
                    servlet.createServlet();
                }
            }
            if (context.getDeploymentInfo().isEagerFilterInit()){
                for(ManagedFilter filter: context.getFilters().getFilters().values()) {
                    filter.createFilter();
                }
            }
        }
        
        @Override
        public void transition(final ServiceController controller, final ServiceController.Transition transition) {
            if (transition == ServiceController.Transition.STARTING_to_UP) {
                SIPWebContext context = (SIPWebContext) this.deploymentService.getDeployment();

                SIPContextFactory factory = this.deploymentUnit.getAttachment(SIPContextFactory.ATTACHMENT);
                factory.addDeplyomentUnitToContext(deploymentUnit, deploymentInfoservice, context);

                try {
                    Module module = deploymentUnit.getAttachment(Attachments.MODULE);
                    context.addSipContextClassLoader(module.getClassLoader());
                    context.init();
                    context.start();
                    context.contextListenerStart();
                    
                    //lets init servlets only here to be able to do injections provided by the SIPWebContext
                    //sipSErvlets initialized earlier in context.start() to be able to notify its contextlisteners
                    List<SipServletImpl> sipServlets= new ArrayList<SipServletImpl>();
                    /*for (String servletName :((ConvergedDeploymentInfo)context.getDeploymentInfo()).getSipServlets().keySet()) 
                    {
                        SipServletImpl managedServlet = (SipServletImpl) context.getChildrenMap().get(servletName);
                        sipServlets.add(managedServlet);
                    }*/

                    this.createServlets(context.getServlets(),sipServlets, context);
                    
                } catch (ServletException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (transition == ServiceController.Transition.STOPPING_to_DOWN) {
                SIPWebContext context = (SIPWebContext) this.deploymentService.getDeployment();
                try {
                    if(context!=null){
                        context.stop();
                        context.listenerStop();
                    }
                } catch (ServletException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
