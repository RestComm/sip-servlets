/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.mobicents.as7.deployment;

import java.util.ArrayList;

import org.apache.catalina.LifecycleException;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.merge.web.jboss.JBossWebMetaDataMerger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.mobicents.as7.SipServer;
import org.mobicents.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.mobicents.metadata.sip.merge.JBossSipMetaDataMerger;
import org.mobicents.metadata.sip.spec.ProxyConfigMetaData;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.metadata.sip.spec.SipServletSelectionMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig;

/**
 * The SIP specific implementation of the jboss-web {@code StandardContext}.
 *
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 */
public class SIPWebContext extends SipStandardContext {

    static AttachmentKey<SIPWebContext> ATTACHMENT = AttachmentKey.create(SIPWebContext.class);

    private static final Logger logger = Logger.getLogger(SIPWebContext.class);

    private final DeploymentUnit deploymentUnit;
    private SipJBossContextConfig sipJBossContextConfig;

    public SIPWebContext(DeploymentUnit du) {
        super();
        deploymentUnit = du;
        sipJBossContextConfig = createContextConfig(this, deploymentUnit);
        // if this an ear deployment, attach this to the parent deploymentUnit so it can be used to inject context resources (SipFactory, etc.)
        DeploymentUnit parentDu = deploymentUnit.getParent();
        if (parentDu == null) {
            // this is a war only deployment
            return;
        }
        if (DeploymentTypeMarker.isType(DeploymentType.EAR, parentDu)) {
            if (logger.isDebugEnabled()) logger.debug("Attaching SIPWebContext " + this + " to " + parentDu.getName());
            parentDu.putAttachment(SIPWebContext.ATTACHMENT, this);
        }
    }

    public void postProcessContext(DeploymentUnit deploymentUnit) {
    }

    @Override
    public void init() throws Exception {
        SipServer sipServer = deploymentUnit.getAttachment(SipServer.ATTACHMENT_KEY);
        if (sipServer.getService() instanceof SipService) {
            super.sipApplicationDispatcher = ((SipService)sipServer.getService()).getSipApplicationDispatcher();
        }
        super.init();
    }

    @Override
    public void start() throws LifecycleException {
        log.infof("Starting sip web context for deployment %s", deploymentUnit.getName());
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit.getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);

        JBossWebMetaData mergedMetaData = null;
        mergedMetaData = new JBossConvergedSipMetaData();
        final JBossWebMetaData override = null;
        final WebMetaData original = null;
        JBossWebMetaDataMerger.merge(mergedMetaData, override, original);

        augmentAnnotations(mergedMetaData, sipMetaData, sipAnnotationMetaData);
        processMetaData(mergedMetaData, sipMetaData);

        super.start();
    }

    private void augmentAnnotations(JBossWebMetaData mergedMetaData, SipMetaData sipMetaData, SipAnnotationMetaData sipAnnotationMetaData) throws LifecycleException {
        if (logger.isDebugEnabled()) {
            if (sipAnnotationMetaData != null) {
                SipMetaData annotatedSipMetaData = sipAnnotationMetaData.get("classes");
                if (annotatedSipMetaData.getListeners() != null) {
                    for (ListenerMetaData listenerMetaData: annotatedSipMetaData.getListeners()) {
                        if (logger.isDebugEnabled()) logger.debug("@SipListener: " + listenerMetaData.getListenerClass());
                    }
                }
                if (annotatedSipMetaData.getSipServlets() != null) {
                    for (ServletMetaData sipServletMetaData: annotatedSipMetaData.getSipServlets()) {
                        if (logger.isDebugEnabled()) logger.debug("@SipServlet: " + sipServletMetaData.getServletClass());
                    }
                }
            }
        }
        // merging sipMetaData and clumsy sip annotation processing
        {
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
                    for (ServletMetaData check: sipMetaData.getSipServlets()) {
                        logger.debug("SipServlet: " + check.getServletClass());
                    }
                }
                logger.debug("</Before clumsy augmentation>");
            }
            // FIXME: josemrecio - clumsy annotation augmentation, this should be done by SipAnnotationMergedView or similar
            // FIXME: josemrecio - SipAnnotation is supported, full merge is needed (e.g. main servlet selection) but not done yet
            {
                if (sipAnnotationMetaData != null) {
                    SipMetaData annotatedMetaData = sipAnnotationMetaData.get("classes");
                    SipMetaData annotatedSipMetaData = (SipMetaData) annotatedMetaData;

                    // @SipApplication processing
                    // existing sipMetaData overrides annotations
                    {
                        // main servlet
                        if (annotatedSipMetaData.getServletSelection() != null && annotatedSipMetaData.getServletSelection().getMainServlet() != null) {
                            if (sipMetaData.getServletSelection() == null) {
                                sipMetaData.setServletSelection(new SipServletSelectionMetaData());
                                sipMetaData.getServletSelection().setMainServlet(annotatedSipMetaData.getServletSelection().getMainServlet());
                            }
                        }
                        // proxy timeout
                        if (annotatedSipMetaData.getProxyConfig() != null && annotatedSipMetaData.getProxyConfig().getProxyTimeout() != 0) {
                            if (sipMetaData.getProxyConfig() == null) {
                                sipMetaData.setProxyConfig(new ProxyConfigMetaData());
                                sipMetaData.getProxyConfig().setProxyTimeout(annotatedSipMetaData.getProxyConfig().getProxyTimeout());
                            }
                        }
                        // session timeout
                        if (annotatedSipMetaData.getSessionConfig() != null && annotatedSipMetaData.getSessionConfig().getSessionTimeout() != 0) {
                            if (sipMetaData.getSessionConfig() == null) {
                                sipMetaData.setSessionConfig(new SessionConfigMetaData());
                                sipMetaData.getSessionConfig().setSessionTimeout(annotatedSipMetaData.getSessionConfig().getSessionTimeout());
                            }
                        }
                        // application name
                        if (annotatedSipMetaData.getApplicationName() != null) {
                            if (sipMetaData.getApplicationName() == null) {
                                sipMetaData.setApplicationName(annotatedSipMetaData.getApplicationName());
                            }
                            else if (sipMetaData.getApplicationName().compareTo(annotatedSipMetaData.getApplicationName()) != 0) {
                                throw (new LifecycleException("Sip application name mismatch: " + sipMetaData.getApplicationName() + " (from sip.xml) vs " + annotatedSipMetaData.getApplicationName()+ " (from annotations)"));
                            }
                        }
                        // description
                        if (annotatedSipMetaData.getDescriptionGroup() != null) {
                            if (sipMetaData.getDescriptionGroup() == null) {
                                sipMetaData.setDescriptionGroup(annotatedMetaData.getDescriptionGroup());
                            }
                        }
                        // distributable
                        // TODO: josemrecio - distributable not supported yet
                    }

                    if (annotatedSipMetaData.getListeners() != null) {
                        if (sipMetaData.getListeners() == null) {
                            sipMetaData.setListeners(new ArrayList<ListenerMetaData>());
                        }
                        for (ListenerMetaData listenerMetaData: annotatedSipMetaData.getListeners()) {
                            boolean found = false;
                            for (ListenerMetaData check : sipMetaData.getListeners()) {
                                if (check.getListenerClass().equals(listenerMetaData.getListenerClass())) {
                                    if (logger.isDebugEnabled()) logger.debug("@SipListener already present: " + listenerMetaData.getListenerClass());
                                    found = true;
                                }
                            }
                            if (!found) {
                                if (logger.isDebugEnabled()) logger.debug("Added @SipListener: " + listenerMetaData.getListenerClass());
                                sipMetaData.getListeners().add(listenerMetaData);
                            }
                        }
                    }
                    if (annotatedSipMetaData.getSipServlets() != null) {
                        if (sipMetaData.getSipServlets() == null) {
                            sipMetaData.setSipServlets(new SipServletsMetaData());
                        }
                        for (ServletMetaData servletMetaData: annotatedSipMetaData.getSipServlets()) {
                            boolean found = false;
                            for (ServletMetaData check : sipMetaData.getSipServlets()) {
                                if (check.getServletClass().equals(servletMetaData.getServletClass())) {
                                    if (logger.isDebugEnabled()) logger.debug("@SipServlet already present: " + servletMetaData.getServletClass());
                                    found = true;
                                }
                            }
                            if (!found) {
                                if (logger.isDebugEnabled()) logger.debug("Added @SipServlet: " + servletMetaData.getServletClass());
                                sipMetaData.getSipServlets().add(servletMetaData);
                            }
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
                    for (ServletMetaData check: sipMetaData.getSipServlets()) {
                        logger.debug("SipServlet: " + check.getName() + " - class: " + check.getServletClass());
                    }
                }
                logger.debug("</After clumsy augmentation>");
            }
            JBossSipMetaDataMerger.merge((JBossConvergedSipMetaData)mergedMetaData, null, sipMetaData);
        }
    }

    private void processMetaData(JBossWebMetaData mergedMetaData, SipMetaData sipMetaData) {
        //processJBossWebMetaData(sharedJBossWebMetaData);
        //processWebMetaData(sharedJBossWebMetaData);
        JBossSipMetaDataMerger.merge((JBossConvergedSipMetaData)mergedMetaData, null, sipMetaData);
        sipJBossContextConfig.processSipMetaData((JBossConvergedSipMetaData)mergedMetaData);
    }

    private SipJBossContextConfig createContextConfig(SipStandardContext sipContext, DeploymentUnit deploymentUnit) {
        SipJBossContextConfig config = new SipJBossContextConfig(deploymentUnit);
        sipContext.addLifecycleListener(config);
        return config;
    }

}
