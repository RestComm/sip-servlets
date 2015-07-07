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
import io.undertow.servlet.api.DeploymentInfoFacade;

import java.util.ArrayList;

import javax.servlet.ServletException;

import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.logging.Logger;
import org.jboss.metadata.merge.web.jboss.JBossWebMetaDataMerger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.mobicents.as8.SipServer;
import org.mobicents.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.mobicents.metadata.sip.merge.JBossSipMetaDataMerger;
import org.mobicents.metadata.sip.spec.ProxyConfigMetaData;
import org.mobicents.metadata.sip.spec.Sip11MetaData;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.metadata.sip.spec.SipServletSelectionMetaData;
import org.mobicents.metadata.sip.spec.SipServletsMetaData;
import org.mobicents.servlet.sip.core.SipService;
import org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig;
import org.mobicents.servlet.sip.undertow.SipContextImpl;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;

/**
 * The SIP specific implementation of the jboss-web {@code StandardContext}.
 *
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 *
 * This class is based on the contents of org.mobicents.as7.deployment package from jboss-as7-mobicents project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class SIPWebContext extends SipContextImpl {

    static AttachmentKey<SIPWebContext> ATTACHMENT_KEY = AttachmentKey.create(SIPWebContext.class);

    private static final Logger logger = Logger.getLogger(SIPWebContext.class);

    private DeploymentUnit deploymentUnit;
    private SipJBossContextConfig sipJBossContextConfig;

    // default constructor:
    public SIPWebContext() {
    }

    public SIPWebContext addDeploymentUnit(DeploymentUnit du) throws ServletException {
        if (this.deploymentUnit == null) {
            if (du != null) {
                this.deploymentUnit = du;
            } else {
                throw new ServletException("Cannot set deploymentUnit to null!");
            }
        }
        return this;
    }

    public SIPWebContext createContextConfig(UndertowDeploymentInfoService deploymentInfoservice)
            throws ServletException {
        if (this.deploymentUnit == null) {
            throw new ServletException("deploymentUnit not set, call SIPWebContext.addDeploymentUnit() first");
        }
        if (this.sipJBossContextConfig == null) {
            this.sipJBossContextConfig = createContextConfig(this.deploymentUnit, deploymentInfoservice);
        }
        return this;
    }

    public SIPWebContext attachContext() throws ServletException {
        if (this.deploymentUnit == null) {
            throw new ServletException("deploymentUnit not set, call SIPWebContext.addDeploymentUnit() first");
        }

        // attach context to top-level deploymentUnit so it can be used to get context resources (SipFactory, etc.)
        final DeploymentUnit anchorDu = getSipContextAnchorDu(this.deploymentUnit);
        if (anchorDu != null) {
            if (logger.isDebugEnabled())
                logger.debug("Attaching SIPWebContext " + this + " to " + anchorDu.getName());
            anchorDu.putAttachment(SIPWebContext.ATTACHMENT_KEY, this);
        } else {
            logger.error("Can't attach SIPWebContext " + this + " to " + this.deploymentUnit.getName()
                    + " - This is probably a bug");
        }

        return this;
    }

    public void postProcessContext(DeploymentUnit deploymentUnit) {
    }

    public void initDispatcher() {
        SipServer sipServer = deploymentUnit.getAttachment(SipServer.ATTACHMENT_KEY);
        if (sipServer.getService() instanceof SipService) {
            super.sipApplicationDispatcher = ((SipService) sipServer.getService()).getSipApplicationDispatcher();
        }
    }

    @Override
    public void init(Deployment deployment, ClassLoader sipContextClassLoader) throws ServletException {
        super.deploymentInfoFacade = deploymentUnit.getAttachment(DeploymentInfoFacade.ATTACHMENT_KEY);

        super.init(deployment, sipContextClassLoader);
    }

    @Override
    public void start() throws ServletException {
        if (logger.isDebugEnabled()) {
            logger.debugf("Starting sip web context for deployment %s", deploymentUnit.getName());
        }
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit
                .getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);

        JBossWebMetaData mergedMetaData = null;
        mergedMetaData = new JBossConvergedSipMetaData();
        final WarMetaData warMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY);
        final JBossWebMetaData override = warMetaData.getJBossWebMetaData();
        final WebMetaData original = null;
        JBossWebMetaDataMerger.merge(mergedMetaData, override, original);

        if (logger.isDebugEnabled()) {
            logger.debugf("security domain " + mergedMetaData.getSecurityDomain() + " for deployment %s",
                    deploymentUnit.getName());
        }
        if (sipMetaData == null && sipAnnotationMetaData != null
                && sipAnnotationMetaData.isSipApplicationAnnotationPresent()) {
            // http://code.google.com/p/sipservlets/issues/detail?id=168
            // When no sip.xml but annotations only, Application is not recognized as SIP App by AS7
            logger.debugf("sip meta data is null, creating a new one");
            sipMetaData = new Sip11MetaData();
        }
        augmentAnnotations(mergedMetaData, sipMetaData, sipAnnotationMetaData);
        try {
            processMetaData(mergedMetaData, sipMetaData);
        } catch (Exception e) {
            throw new ServletException("An unexpected exception happened while parsing sip meta data from "
                    + deploymentUnit.getName(), e);
        }

        super.start();
    }

    private void augmentAnnotations(JBossWebMetaData mergedMetaData, SipMetaData sipMetaData,
            SipAnnotationMetaData sipAnnotationMetaData) throws ServletException {
        if (logger.isDebugEnabled()) {
            if (sipAnnotationMetaData != null) {
                SipMetaData annotatedSipMetaData = sipAnnotationMetaData.get("classes");
                if (annotatedSipMetaData.getListeners() != null) {
                    for (ListenerMetaData listenerMetaData : annotatedSipMetaData.getListeners()) {
                        if (logger.isDebugEnabled())
                            logger.debug("@SipListener: " + listenerMetaData.getListenerClass());
                    }
                }
                if (annotatedSipMetaData.getSipServlets() != null) {
                    for (ServletMetaData sipServletMetaData : annotatedSipMetaData.getSipServlets()) {
                        if (logger.isDebugEnabled())
                            logger.debug("@SipServlet: " + sipServletMetaData.getServletClass());
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
                    for (ServletMetaData check : sipMetaData.getSipServlets()) {
                        logger.debug("SipServlet: " + check.getServletClass());
                    }
                }
                logger.debug("</Before clumsy augmentation>");
            }
            // FIXME: josemrecio - clumsy annotation augmentation, this should be done by SipAnnotationMergedView or
            // similar
            // FIXME: josemrecio - SipAnnotation is supported, full merge is needed (e.g. main servlet selection) but
            // not done yet
            {
                if (sipAnnotationMetaData != null) {
                    SipMetaData annotatedMetaData = sipAnnotationMetaData.get("classes");
                    SipMetaData annotatedSipMetaData = (SipMetaData) annotatedMetaData;

                    // @SipApplication processing
                    // existing sipMetaData overrides annotations
                    {
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
                                sipMetaData.getProxyConfig().setProxyTimeout(
                                        annotatedSipMetaData.getProxyConfig().getProxyTimeout());
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
                            } else if (sipMetaData.getApplicationName().compareTo(
                                    annotatedSipMetaData.getApplicationName()) != 0) {
                                throw (new ServletException("Sip application name mismatch: "
                                        + sipMetaData.getApplicationName() + " (from sip.xml) vs "
                                        + annotatedSipMetaData.getApplicationName() + " (from annotations)"));
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
                        for (ListenerMetaData listenerMetaData : annotatedSipMetaData.getListeners()) {
                            boolean found = false;
                            for (ListenerMetaData check : sipMetaData.getListeners()) {
                                if (check.getListenerClass().equals(listenerMetaData.getListenerClass())) {
                                    if (logger.isDebugEnabled())
                                        logger.debug("@SipListener already present: "
                                                + listenerMetaData.getListenerClass());
                                    found = true;
                                }
                            }
                            if (!found) {
                                if (logger.isDebugEnabled())
                                    logger.debug("Added @SipListener: " + listenerMetaData.getListenerClass());
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
                                    if (logger.isDebugEnabled())
                                        logger.debug("@SipServlet already present: "
                                                + servletMetaData.getServletClass());
                                    found = true;
                                }
                            }
                            if (!found) {
                                if (logger.isDebugEnabled())
                                    logger.debug("Added @SipServlet: " + servletMetaData.getServletClass());
                                sipMetaData.getSipServlets().add(servletMetaData);
                            }
                        }
                    }
                    if (annotatedMetaData.getSipApplicationKeyMethodInfo() != null) {
                        sipMetaData.setSipApplicationKeyMethodInfo(annotatedMetaData.getSipApplicationKeyMethodInfo());
                    }
                    if (annotatedMetaData.getConcurrencyControlMode() != null) {
                        if (sipMetaData.getConcurrencyControlMode() == null) {
                            sipMetaData.setConcurrencyControlMode(annotatedMetaData.getConcurrencyControlMode());
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
                        logger.debug("SipServlet: " + check.getName() + " - class: " + check.getServletClass());
                    }
                }
                logger.debug("</After clumsy augmentation>");
            }
            JBossSipMetaDataMerger.merge((JBossConvergedSipMetaData) mergedMetaData, null, sipMetaData);
        }
    }

    private void processMetaData(JBossWebMetaData mergedMetaData, SipMetaData sipMetaData) throws Exception {
        // processJBossWebMetaData(sharedJBossWebMetaData);
        // processWebMetaData(sharedJBossWebMetaData);
        JBossSipMetaDataMerger.merge((JBossConvergedSipMetaData) mergedMetaData, null, sipMetaData);
        sipJBossContextConfig.processSipMetaData((JBossConvergedSipMetaData) mergedMetaData, this);
    }

    private SipJBossContextConfig createContextConfig(DeploymentUnit deploymentUnit, UndertowDeploymentInfoService deploymentInfoservice) {
        SipJBossContextConfig config = new SipJBossContextConfig(deploymentUnit, deploymentInfoservice);
        // FIXME: kakonyii: sipContext.addLifecycleListener(config);
        return config;
    }

    // returns the anchor deployment unit that will have attached a SIPWebContext
    public static DeploymentUnit getSipContextAnchorDu(final DeploymentUnit du) {
        // attach context to top-level deploymentUnit so it can be used to get context resources (SipFactory, etc.)
        DeploymentUnit parentDu = du.getParent();
        if (parentDu == null) {
            // this is a war only deployment
            return du;
        } else if (DeploymentTypeMarker.isType(DeploymentType.EAR, parentDu)) {
            return parentDu;
        } else {
            logger.error("Can't find proper anchor deployment unit for " + du.getName() + " - This is probably a bug");
            return null;
        }
    }

}