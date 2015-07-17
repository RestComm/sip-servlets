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
package io.undertow.servlet.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;
import org.jboss.as.server.deployment.AttachmentKey;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.MobicentsSipServlet;
import org.mobicents.servlet.sip.core.descriptor.MobicentsSipServletMapping;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.ruby.SipRubyController;

/**
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class DeploymentInfoFacade implements Cloneable{
    public static AttachmentKey<DeploymentInfoFacade> ATTACHMENT_KEY = AttachmentKey.create(DeploymentInfoFacade.class);

    private DeploymentInfo deploymentInfo;

    private static final Logger logger = Logger.getLogger(DeploymentInfoFacade.class);

    private final Map<String, ServletInfo> sipServlets = new HashMap<>();

    // sip-xml meta:
    protected String applicationName;
    protected String description;
    protected String smallIcon;
    protected String largeIcon;
    protected int proxyTimeout;
    protected int sipApplicationSessionTimeout;
    // Issue 1200 this is needed to be able to give a default servlet handler if we are not in main-servlet servlet
    // selection case
    // by example when creating a new sip application session from a factory from an http servlet
    private String servletHandler;
    protected boolean isMainServlet;
    private String mainServlet;
    protected transient MobicentsSipLoginConfig sipLoginConfig;
    protected transient Method sipApplicationKeyMethod;
    protected ConcurrencyControlMode concurrencyControlMode;
    protected transient List<String> sipApplicationListeners = new CopyOnWriteArrayList<String>();
    protected transient List<MobicentsSipServletMapping> sipServletMappings = new ArrayList<MobicentsSipServletMapping>();
    private transient SipRubyController rubyController;
    protected transient Map<String, MobicentsSipServlet> childrenMap;
    protected transient Map<String, MobicentsSipServlet> childrenMapByClassName;

    //default constructor:
    public DeploymentInfoFacade(){}

    public void addDeploymentInfo(DeploymentInfo info) throws ServletException{
        if(this.deploymentInfo==null){
            if(info!=null){
                this.deploymentInfo=info;
            }else{
                throw new ServletException("Cannot set deploymentInfo to null!");
            }
        }else{
            throw new ServletException("DeploymentInfo already set!");
        }
    }

    public DeploymentInfoFacade addSipServlets(final ServletInfo... servlets) {
        for (final ServletInfo servlet : servlets) {
            sipServlets.put(servlet.getName(), servlet);
            return this;
        }
        return this;
    }

    public Map<String, ServletInfo> getSipServlets() {
        return Collections.unmodifiableMap(sipServlets);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSmallIcon() {
        return smallIcon;
    }

    public void setSmallIcon(String smallIcon) {
        this.smallIcon = smallIcon;
    }

    public String getLargeIcon() {
        return largeIcon;
    }

    public void setLargeIcon(String largeIcon) {
        this.largeIcon = largeIcon;
    }

    public int getProxyTimeout() {
        return proxyTimeout;
    }

    public void setProxyTimeout(int proxyTimeout) {
        this.proxyTimeout = proxyTimeout;
    }

    public int getSipApplicationSessionTimeout() {
        return sipApplicationSessionTimeout;
    }

    public void setSipApplicationSessionTimeout(int sipApplicationSessionTimeout) {
        this.sipApplicationSessionTimeout = sipApplicationSessionTimeout;
    }

    public String getServletHandler() {
        return servletHandler;
    }

    public void setServletHandler(String servletHandler) {
        this.servletHandler = servletHandler;
    }

    public boolean isMainServlet() {
        return isMainServlet;
    }

    public void setMainServlet(boolean isMainServlet) {
        this.isMainServlet = isMainServlet;
    }

    public String getMainServlet() {
        return mainServlet;
    }

    public void setMainServlet(String mainServlet) {
        this.mainServlet = mainServlet;
    }

    public void addSipApplicationListener(String listener) {
        sipApplicationListeners.add(listener);
        // FIXME:fireContainerEvent("addSipApplicationListener", listener);
    }

    public void removeSipApplicationListener(String listener) {
        sipApplicationListeners.remove(listener);

        // Inform interested listeners
        // FIXME:fireContainerEvent("removeSipApplicationListener", listener);
    }

    public void addSipServletMapping(MobicentsSipServletMapping sipServletMapping) {
        sipServletMappings.add(sipServletMapping);
        isMainServlet = false;
        if (servletHandler == null) {
            servletHandler = sipServletMapping.getServletName();
        }
    }

    public List<MobicentsSipServletMapping> findSipServletMappings() {
        return sipServletMappings;
    }

    public MobicentsSipServletMapping findSipServletMappings(SipServletRequest sipServletRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking sip Servlet Mapping for following request : " + sipServletRequest);
        }
        for (MobicentsSipServletMapping sipServletMapping : sipServletMappings) {
            if (sipServletMapping.getMatchingRule().matches(sipServletRequest)) {
                return sipServletMapping;
            } else {
                logger.debug("Following mapping rule didn't match : servletName => "
                        + sipServletMapping.getServletName() + " | expression = "
                        + sipServletMapping.getMatchingRule().getExpression());
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void removeSipServletMapping(MobicentsSipServletMapping sipServletMapping) {
        sipServletMappings.remove(sipServletMapping);
    }

    public String[] findSipApplicationListeners() {
        return sipApplicationListeners.toArray(new String[sipApplicationListeners.size()]);
    }

    public Method getSipApplicationKeyMethod() {
        return sipApplicationKeyMethod;
    }

    public void setSipApplicationKeyMethod(Method sipApplicationKeyMethod) {
        this.sipApplicationKeyMethod = sipApplicationKeyMethod;
    }

    public ConcurrencyControlMode getConcurrencyControlMode() {
        return concurrencyControlMode;
    }

    public void setConcurrencyControlMode(ConcurrencyControlMode concurrencyControlMode) {
        this.concurrencyControlMode = concurrencyControlMode;
    }

    public MobicentsSipLoginConfig getSipLoginConfig() {
        return sipLoginConfig;
    }

    public void setSipLoginConfig(MobicentsSipLoginConfig sipLoginConfig) {
        this.sipLoginConfig = sipLoginConfig;
    }

    public SipRubyController getRubyController() {
        return rubyController;
    }

    public void setRubyController(SipRubyController rubyController) {
        this.rubyController = rubyController;
    }

    public Map<String, MobicentsSipServlet> getChildrenMap() {
        return childrenMap;
    }

    public void setChildrenMap(Map<String, MobicentsSipServlet> childrenMap) {
        this.childrenMap = childrenMap;
    }

    public Map<String, MobicentsSipServlet> getChildrenMapByClassName() {
        return childrenMapByClassName;
    }

    public void setChildrenMapByClassName(Map<String, MobicentsSipServlet> childrenMapByClassName) {
        this.childrenMapByClassName = childrenMapByClassName;
    }

    public SessionManagerFactory getSessionManagerFactory() {
        return this.deploymentInfo.getSessionManagerFactory();
    }

    public DeploymentInfoFacade setSessionManagerFactory(final SessionManagerFactory sessionManagerFactory) {
        this.deploymentInfo.setSessionManagerFactory(sessionManagerFactory);
        return this;
    }

    public List<String> getSipApplicationListeners() {
        return sipApplicationListeners;
    }

    public List<MobicentsSipServletMapping> getSipServletMappings() {
        return sipServletMappings;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }
}