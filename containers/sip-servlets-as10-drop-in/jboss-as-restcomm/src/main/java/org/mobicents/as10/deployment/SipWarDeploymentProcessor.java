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
package org.mobicents.as10.deployment;

import static org.mobicents.as10.SipMessages.MESSAGES;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import org.jboss.as.controller.PathElement;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentResourceSupport;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.logging.ServerLogger;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.metadata.merge.web.jboss.JBossWebMetaDataMerger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.vfs.VirtualFile;
import org.mobicents.as10.SipDeploymentDefinition;
import org.mobicents.as10.SipExtension;
import org.mobicents.as10.SipSubsystemServices;
import org.mobicents.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.mobicents.metadata.sip.merge.JBossSipMetaDataMerger;
import org.mobicents.metadata.sip.spec.ProxyConfigMetaData;
import org.mobicents.metadata.sip.spec.Sip11MetaData;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.metadata.sip.spec.SipServletSelectionMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;

/**
 * {@code DeploymentUnitProcessor} creating the actual deployment services. For SIP this is only required to setup management
 * points
 *
 * @author Emanuel Muckenhuber
 * @author Anil.Saldhana@redhat.com
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.as7.deployment package from jboss-as7-mobicents project,
 *         re-implemented for jboss as10 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 * @author balogh.gabor@alerant.hu
 */
public class SipWarDeploymentProcessor implements DeploymentUnitProcessor {
	
    private static final Logger logger = Logger.getLogger(SipWarDeploymentProcessor.class);

    public SipWarDeploymentProcessor() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final WarMetaData metaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        if (metaData == null) {
            return;
        }
        JBossConvergedSipMetaData mergedMetaData  =  mergeSipMetaDataAndSipAnnMetaData(deploymentUnit);
        processDeployment(deploymentUnit, phaseContext.getServiceTarget(), mergedMetaData);
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
        // TODO
    }
    
    //merge DD and annotation meta data and store in WarMetaData.mergedJBossWebMetaData based on SIPWebContext code
    private JBossConvergedSipMetaData mergeSipMetaDataAndSipAnnMetaData(final DeploymentUnit deploymentUnit) {
    	final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit.getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);
        
        JBossConvergedSipMetaData mergedMetaData = new JBossConvergedSipMetaData();
        final JBossWebMetaData override = warMetaData.getJBossWebMetaData();
        final WebMetaData original = null;
        JBossWebMetaDataMerger.merge(mergedMetaData, override, original);

        if (sipMetaData == null && sipAnnotationMetaData != null && sipAnnotationMetaData.isSipApplicationAnnotationPresent()) {
            // http://code.google.com/p/sipservlets/issues/detail?id=168
            // When no sip.xml but annotations only, Application is not recognized as SIP App by AS7
            sipMetaData = new Sip11MetaData();
        }
        if (sipMetaData != null) {
	        try {
				augmentAnnotations(mergedMetaData, sipMetaData, sipAnnotationMetaData);
			} catch (ServletException e) {
				logger.error("Error while augmenting annotation: " + e.getMessage(), e);
			}
        } else {
        	logger.debug("Deployment does not consist SIP application.");
        }
        return mergedMetaData;
    }
    
    private void augmentAnnotations(JBossWebMetaData mergedMetaData, SipMetaData sipMetaData,
            SipAnnotationMetaData sipAnnotationMetaData) throws ServletException {
        // https://github.com/Mobicents/sip-servlets/issues/68 iterating through all entry set and not only classes directory
        Set<Entry<String, SipMetaData>> annotationsEntrySet = sipAnnotationMetaData.entrySet();
        if (logger.isDebugEnabled()) {
            logger.debug("sipAnnotationMetaData " + sipAnnotationMetaData);
            if (sipAnnotationMetaData != null) {
                for (Entry<String, SipMetaData> annotationEntry : annotationsEntrySet) {
                    String annotatedSipMetaDataKey = annotationEntry.getKey();
                    SipMetaData annotatedSipMetaData = annotationEntry.getValue();
                    logger.debug("sipAnnotationMetaDataKey " + annotatedSipMetaDataKey + " value " + annotatedSipMetaData);
                    if (annotatedSipMetaData.getListeners() != null) {
                        for (ListenerMetaData listenerMetaData : annotatedSipMetaData.getListeners()) {
                            if (logger.isDebugEnabled())
                                logger.debug("@SipListener: " + listenerMetaData.getListenerClass() + " in "
                                        + annotatedSipMetaDataKey);
                        }
                    }
                    if (annotatedSipMetaData.getSipServlets() != null) {
                        for (ServletMetaData sipServletMetaData : annotatedSipMetaData.getSipServlets()) {
                            if (logger.isDebugEnabled())
                                logger.debug("@SipServlet: " + sipServletMetaData.getServletClass() + " in "
                                        + annotatedSipMetaDataKey);
                        }
                    }
                }
            }
        }
        // merging sipMetaData and clumsy sip annotation processing
        if (logger.isDebugEnabled()) {
            logger.debug("<Before clumsy augmentation>");
            if (sipMetaData.getListeners() != null) {
                logger.debug("Listeners: " + sipMetaData.getListeners().size());
                for (ListenerMetaData check : sipMetaData.getListeners()) {
                    logger.debug("Listener: " + check.getListenerClass());
                }
            }
            if (sipMetaData.getSipServlets() != null) {
                logger.debug("SipServlets: " + sipMetaData.getSipServlets().size());
                for (ServletMetaData check : sipMetaData.getSipServlets()) {
                    logger.debug("SipServlet: " + check.getName() + " - class: " + check.getServletClass()
                            + " - load-on-startup: " + check.getLoadOnStartup());
                }
            }
            logger.debug("</Before clumsy augmentation>");
        }
        // FIXME: josemrecio - clumsy annotation augmentation, this should be done by SipAnnotationMergedView or
        // similar
        // FIXME: josemrecio - SipAnnotation is supported, full merge is needed (e.g. main servlet selection) but
        // not done yet
        if (sipAnnotationMetaData != null) {
            for (Entry<String, SipMetaData> annotationEntry : annotationsEntrySet) {
                String annotatedSipMetaDataKey = annotationEntry.getKey();

                SipMetaData annotatedSipMetaData = annotationEntry.getValue();

                // @SipApplication processing
                // existing sipMetaData overrides annotations

                // main servlet
                if (annotatedSipMetaData.getServletSelection() != null
                        && annotatedSipMetaData.getServletSelection().getMainServlet() != null) {

                    if (sipMetaData.getServletSelection() == null) {
                        sipMetaData.setServletSelection(new SipServletSelectionMetaData());
                        sipMetaData.getServletSelection().setMainServlet(
                                annotatedSipMetaData.getServletSelection().getMainServlet());

                    }
                }
                // proxy timeout
                if (annotatedSipMetaData.getProxyConfig() != null
                        && annotatedSipMetaData.getProxyConfig().getProxyTimeout() != 0) {

                    if (sipMetaData.getProxyConfig() == null) {
                        sipMetaData.setProxyConfig(new ProxyConfigMetaData());
                        sipMetaData.getProxyConfig().setProxyTimeout(annotatedSipMetaData.getProxyConfig().getProxyTimeout());

                    }
                }
                // session timeout
                if (annotatedSipMetaData.getSessionConfig() != null
                        && annotatedSipMetaData.getSessionConfig().getSessionTimeout() != 0) {

                    if (sipMetaData.getSessionConfig() == null) {
                        sipMetaData.setSessionConfig(new SessionConfigMetaData());
                        sipMetaData.getSessionConfig().setSessionTimeout(
                                annotatedSipMetaData.getSessionConfig().getSessionTimeout());

                    }
                }
                // application name
                if (annotatedSipMetaData.getApplicationName() != null) {
                    if (sipMetaData.getApplicationName() == null) {
                        sipMetaData.setApplicationName(annotatedSipMetaData.getApplicationName());
                    } else if (sipMetaData.getApplicationName().compareTo(annotatedSipMetaData.getApplicationName()) != 0) {
                        throw (new ServletException("Sip application name mismatch: " + sipMetaData.getApplicationName()
                                + " (from sip.xml) vs " + annotatedSipMetaData.getApplicationName() + " from annotations "
                                + annotatedSipMetaDataKey));

                    }
                }
                // description
                if (annotatedSipMetaData.getDescriptionGroup() != null) {
                    if (sipMetaData.getDescriptionGroup() == null) {
                        sipMetaData.setDescriptionGroup(annotatedSipMetaData.getDescriptionGroup());
                    }
                }
                // distributable
                // TODO: josemrecio - distributable not supported yet

                if (annotatedSipMetaData.getListeners() != null) {
                    if (sipMetaData.getListeners() == null) {
                        sipMetaData.setListeners(new ArrayList<ListenerMetaData>());
                    }
                    for (ListenerMetaData listenerMetaData : annotatedSipMetaData.getListeners()) {
                        boolean found = false;
                        for (ListenerMetaData check : sipMetaData.getListeners()) {
                            if (check.getListenerClass().equals(listenerMetaData.getListenerClass())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            sipMetaData.getListeners().add(listenerMetaData);
                        }
                    }
                }
                if (annotatedSipMetaData.getSipServlets() != null) {
                    if (sipMetaData.getSipServlets() == null) {
                        sipMetaData.setSipServlets(new SipServletsMetaData());
                    }
                    for (ServletMetaData servletMetaData : annotatedSipMetaData.getSipServlets()) {
                        boolean found = false;
                        for (ServletMetaData check : sipMetaData.getSipServlets()) {
                            if (check.getServletClass().equals(servletMetaData.getServletClass())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            sipMetaData.getSipServlets().add(servletMetaData);
                        }
                    }
                }
                if (annotatedSipMetaData.getSipApplicationKeyMethodInfo() != null) {
                    sipMetaData.setSipApplicationKeyMethodInfo(annotatedSipMetaData.getSipApplicationKeyMethodInfo());
                }
                if (annotatedSipMetaData.getConcurrencyControlMode() != null) {
                    if (sipMetaData.getConcurrencyControlMode() == null) {
                        sipMetaData.setConcurrencyControlMode(annotatedSipMetaData.getConcurrencyControlMode());
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("<After clumsy augmentation>");

            if (sipMetaData.getListeners() != null) {
                logger.debug("Listeners: " + sipMetaData.getListeners().size());
                for (ListenerMetaData check : sipMetaData.getListeners()) {
                    logger.debug("Listener: " + check.getListenerClass());
                }
            }
            if (sipMetaData.getSipServlets() != null) {
                logger.debug("SipServlets: " + sipMetaData.getSipServlets().size());
                for (ServletMetaData check : sipMetaData.getSipServlets()) {
                    logger.debug("SipServlet: " + check.getName() + " - class: " + check.getServletClass()
                            + " - load-on-startup: " + check.getLoadOnStartup());

                }
            }
            logger.debug("</After clumsy augmentation>");
        }
        JBossSipMetaDataMerger.merge((JBossConvergedSipMetaData) mergedMetaData, null, sipMetaData);
    } 

    protected void processDeployment(final DeploymentUnit deploymentUnit, final ServiceTarget serviceTarget, final JBossConvergedSipMetaData mergedSipMetaData)
            throws DeploymentUnitProcessingException {
        final VirtualFile deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
        final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        if (module == null) {
            throw new DeploymentUnitProcessingException(MESSAGES.failedToResolveModule(deploymentRoot));
        }
        
        if (mergedSipMetaData != null && mergedSipMetaData.getApplicationName() != null) {
            final String appNameMgmt = mergedSipMetaData.getApplicationName();
            final ServiceName deploymentServiceName = SipSubsystemServices.deploymentServiceName(appNameMgmt);
            try {
                final SipDeploymentService sipDeploymentService = new SipDeploymentService(deploymentUnit);
                ServiceBuilder<?> builder = serviceTarget.addService(deploymentServiceName, sipDeploymentService);

                // TODO: when distributable is implemented
                // if (sipMetaData.getDistributable() != null) {
                // DistributedCacheManagerFactoryService factoryService = new DistributedCacheManagerFactoryService();
                // DistributedCacheManagerFactory factory = factoryService.getValue();
                // if (factory != null) {
                // ServiceName factoryServiceName = deploymentServiceName.append("session");
                // builder.addDependency(DependencyType.OPTIONAL, factoryServiceName, DistributedCacheManagerFactory.class,
                // config.getDistributedCacheManagerFactoryInjector());
                //
                // ServiceBuilder<DistributedCacheManagerFactory> factoryBuilder = serviceTarget.addService(factoryServiceName,
                // factoryService);
                // boolean enabled = factory.addDeploymentDependencies(deploymentServiceName,
                // deploymentUnit.getServiceRegistry(), serviceTarget, factoryBuilder, metaData);
                // factoryBuilder.setInitialMode(enabled ? ServiceController.Mode.ON_DEMAND :
                // ServiceController.Mode.NEVER).install();
                // }
                // }
                // add dependency to sip deployment service
                builder.addDependency(deploymentServiceName);
                builder.setInitialMode(Mode.ACTIVE).install();
            } catch (ServiceRegistryException e) {
                throw new DeploymentUnitProcessingException(MESSAGES.failedToAddSipDeployment(), e);
            }

            // Process sip related mgmt information
            final DeploymentResourceSupport  deploymentResourceSupport = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_RESOURCE_SUPPORT);
   		    final ModelNode node = deploymentResourceSupport.getDeploymentSubsystemModel(SipExtension.SUBSYSTEM_NAME);
            node.get(SipDeploymentDefinition.APP_NAME.getName()).set("".equals(appNameMgmt) ? "/" : appNameMgmt);
            processManagement(deploymentUnit, mergedSipMetaData);
        } else {

        }
    }
    
    void processManagement(final DeploymentUnit unit, final JBossConvergedSipMetaData sipMetaData) {
        if (sipMetaData.getSipServlets() != null) {
            for (final ServletMetaData servlet : sipMetaData.getSipServlets()) {
                try {
                    final String name = servlet.getName().replace(' ', '_');
                    final DeploymentResourceSupport  deploymentResourceSupport = unit.getAttachment(Attachments.DEPLOYMENT_RESOURCE_SUPPORT);
           		    final ModelNode node = deploymentResourceSupport.getDeploymentSubModel(SipExtension.SUBSYSTEM_NAME, PathElement.pathElement("servlet", name));
           		    node.get("servlet-class").set(servlet.getServletClass());		                  
           		    node.get("servlet-name").set(servlet.getServletName());		                                 
                    node.get("load-on-startup").set(servlet.getLoadOnStartupInt());
                } catch (Exception e) {
                	ServerLogger.DEPLOYMENT_LOGGER.error("SipWarDeployment.processManagement() error=" + e.getMessage(), e);
                    // Should a failure in creating the mgmt view also make to the deployment to fail?
                    continue;
                }
            }
        }
    }
}
