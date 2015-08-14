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
package org.mobicents.as8.deployment;

import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.DeploymentInfoFacade;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

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
import org.mobicents.javax.servlet.sip.dns.DNSResolver;
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
 *         This class is based on the contents of org.mobicents.as7.deployment package from jboss-as7-mobicents project,
 *         re-implemented for jboss as8 (wildfly) by:
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

    public SIPWebContext createContextConfig(UndertowDeploymentInfoService deploymentInfoservice) throws ServletException {
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
    public boolean contextListenerStart() throws ServletException{
        boolean ok = super.contextListenerStart();

        ArrayList<EventListener> eventListeners = new ArrayList<EventListener>();
        eventListeners.add(super.sipListeners.getContainerListener());
        eventListeners.addAll(super.sipListeners.getProxyBranchListeners());
        eventListeners.addAll(super.sipListeners.getServletContextListeners());
        eventListeners.addAll(super.sipListeners.getSipApplicationSessionAttributeListeners());
        eventListeners.addAll(super.sipListeners.getSipApplicationSessionListeners());
        eventListeners.addAll(super.sipListeners.getSipConnectorListeners());
        eventListeners.addAll(super.sipListeners.getSipErrorListeners());
        eventListeners.addAll(super.sipListeners.getSipServletsListeners());
        eventListeners.addAll(super.sipListeners.getSipSessionAttributeListeners());
        eventListeners.addAll(super.sipListeners.getSipSessionListeners());
        eventListeners.add(super.sipListeners.getTimerListener());

        //inject resources:
        for (EventListener listener : eventListeners) {
            if(listener!=null) {
                Class listenerClass = listener.getClass();
                for (Field field : listenerClass.getDeclaredFields()) {
                    Annotation[] annotations = field.getAnnotations();
                    for(Annotation ann : annotations){
                        try {
                            field.setAccessible(true);
                            if (ann instanceof Resource && SipFactory.class.isAssignableFrom(field.getType())) {
                                field.set(listener, super.sipFactoryFacade);
                            }else if (ann instanceof Resource && SipSessionsUtil.class.isAssignableFrom(field.getType())){
                                field.set(listener, super.sipSessionsUtil);
                            }else if (ann instanceof Resource && DNSResolver.class.isAssignableFrom(field.getType())){
                                field.set(listener, super.getServletContext().getAttribute("org.mobicents.servlet.sip.DNS_RESOLVER"));
                            }else if (ann instanceof Resource && TimerService.class.isAssignableFrom(field.getType())){
                                field.set(listener, super.timerService);
                            }else if (ann instanceof EJB){
                                //jndi lookup:
                                String name = ((EJB)ann).name();
                                if(name == null || "".equals(name)){
                                    name = field.getType().getSimpleName();
                                }
                                //get deployment archive names:
                                String deployment =  deploymentUnit.getName().substring(0, deploymentUnit.getName().lastIndexOf("."));
                                DeploymentUnit parent = deploymentUnit.getParent();
                                while(parent!=null){
                                    deployment = parent.getName().substring(0, parent.getName().lastIndexOf(".")) + "/"+deployment;
                                    parent = parent.getParent();
                                }

                                Object ejb = InitialContext.doLookup("java:global/"+deployment+"/"+name);
                                field.set(listener, ejb);
                            }
                        } catch (IllegalArgumentException | IllegalAccessException | NamingException e) {
                            throw new ServletException("Exception occured while injecting resources!",e);
                        } finally {
                            field.setAccessible(false);
                        }
                    }
                }
            }
        }

        //call @PostConstruct methods:
        for(EventListener listener: eventListeners){
            if(listener != null){
                Method[] methods = listener.getClass().getDeclaredMethods();
                for(Method method : methods){
                    Annotation ann = method.getAnnotation(PostConstruct.class);
                    if(ann!=null){
                        try {
                            if(method.getParameterTypes().length == 0){
                                method.invoke(listener, new Object[0]);
                            }else{
                                throw new IllegalArgumentException("@PostContstruct annotated methods must have 0 parameters.");
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            throw new ServletException("Exception occured while calling @PostConstruct methods!",e);
                        }
                    }
                }
            }
        }
        return ok;
    }

    @Override
    public void start() throws ServletException {
        if (logger.isDebugEnabled()) {
            logger.debugf("Starting sip web context for deployment %s", deploymentUnit.getName());
        }
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit.getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);

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
        if (sipMetaData == null && sipAnnotationMetaData != null && sipAnnotationMetaData.isSipApplicationAnnotationPresent()) {
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

    @Override
    public void stop() throws ServletException{

        ArrayList<EventListener> listeners = new ArrayList<EventListener>();
        listeners.add(super.sipListeners.getContainerListener());
        listeners.addAll(super.sipListeners.getProxyBranchListeners());
        listeners.addAll(super.sipListeners.getServletContextListeners());
        listeners.addAll(super.sipListeners.getSipApplicationSessionAttributeListeners());
        listeners.addAll(super.sipListeners.getSipApplicationSessionListeners());
        listeners.addAll(super.sipListeners.getSipConnectorListeners());
        listeners.addAll(super.sipListeners.getSipErrorListeners());
        listeners.addAll(super.sipListeners.getSipServletsListeners());
        listeners.addAll(super.sipListeners.getSipSessionAttributeListeners());
        listeners.addAll(super.sipListeners.getSipSessionListeners());
        listeners.add(super.sipListeners.getTimerListener());

        //call @PreDestroy methods:
        for(EventListener listener: listeners){
            if(listener != null){
                Method[] methods = listener.getClass().getDeclaredMethods();
                for(Method method : methods){
                    Annotation ann = method.getAnnotation(PreDestroy.class);
                    if(ann!=null){
                        try {
                            if(method.getParameterTypes().length == 0){
                                method.invoke(listener, new Object[0]);
                            }else{
                                throw new IllegalArgumentException("@PreDestroy annotated methods must have 0 parameters.");
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            throw new ServletException("Exception occured while calling @PreDestroy methods!",e);
                        }
                    }
                }
            }
        }

        super.stop();
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
                                if (logger.isDebugEnabled())
                                    logger.debug("@SipListener already present: " + listenerMetaData.getListenerClass()
                                            + " from " + annotatedSipMetaDataKey);

                                found = true;
                            }
                        }
                        if (!found) {
                            if (logger.isDebugEnabled())
                                logger.debug("Added @SipListener: " + listenerMetaData.getListenerClass() + " from "
                                        + annotatedSipMetaDataKey);

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
                                    logger.debug("@SipServlet already present: " + servletMetaData.getServletClass() + " from "
                                            + annotatedSipMetaDataKey);

                                found = true;
                            }
                        }
                        if (!found) {
                            if (logger.isDebugEnabled())
                                logger.debug("Added @SipServlet: " + servletMetaData.getServletClass() + " from "
                                        + annotatedSipMetaDataKey);

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

    private void processMetaData(JBossWebMetaData mergedMetaData, SipMetaData sipMetaData) throws Exception {
        // processJBossWebMetaData(sharedJBossWebMetaData);
        // processWebMetaData(sharedJBossWebMetaData);
        JBossSipMetaDataMerger.merge((JBossConvergedSipMetaData) mergedMetaData, null, sipMetaData);
        sipJBossContextConfig.processSipMetaData((JBossConvergedSipMetaData) mergedMetaData, this);
    }

    private SipJBossContextConfig createContextConfig(DeploymentUnit deploymentUnit,
            UndertowDeploymentInfoService deploymentInfoservice) {
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
