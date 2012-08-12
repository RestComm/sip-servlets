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

package org.mobicents.as7.deployment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.ee.component.EEApplicationClasses;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.web.deployment.WebAttachments;
import org.jboss.as.web.deployment.component.ComponentInstantiator;
import org.jboss.as.web.deployment.component.WebComponentDescription;
import org.jboss.as.web.deployment.component.WebComponentInstantiator;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.mobicents.metadata.sip.spec.SipAnnotationMetaData;
import org.mobicents.metadata.sip.spec.SipMetaData;

/**
 * Processor that figures out what type of component a servlet/listener is, and registers the appropriate metadata.
 * The different types are:
 * <ul>
 * <li>Managed Bean - If the servlet is annotated with the <code>ManagedBean</code> annotation</li>
 * <li>CDI Bean - If the servlet is deployed in a bean archive</li>
 * <li>EE Component - If this is an EE deployment and the servlet is not one of the above</li>
 * <li>Normal Servlet - If the EE subsystem is disabled</li>
 * </ul>
 * <p/>
 * For ManagedBean Servlets no action is necessary at this stage, as the servlet is already registered as a component.
 * For CDI and EE components a component definition is added to the deployment.
 * <p/>
 * For now we are just using managed bean components as servlets. We may need a custom component type in future.
 */
public class SipComponentProcessor implements DeploymentUnitProcessor {

//    /**
//     * Tags in these packages do not need to be computerized
//     */
//    private static final String[] BUILTIN_TAGLIBS = {"org.apache.taglibs.standard", "com.sun.faces.taglib.jsf_core",  "com.sun.faces.ext.taglib", "com.sun.faces.taglib.html_basic",};

//    /**
//     * Dotname for AsyncListener, which can be injected dynamically.
//     */
//    private static final DotName ASYNC_LISTENER_INTERFACE = DotName.createSimple(AsyncListener.class.getName());

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return;
        }

        final Map<String, ComponentDescription> componentByClass = new HashMap<String, ComponentDescription>();
        //final Map<String, ComponentInstantiator> webComponents = new HashMap<String, ComponentInstantiator>();
        Map<String, ComponentInstantiator> webComponents = deploymentUnit.getAttachment(WebAttachments.WEB_COMPONENT_INSTANTIATORS);
        if (webComponents == null) {
        	webComponents = new HashMap<String, ComponentInstantiator>();
        	deploymentUnit.putAttachment(WebAttachments.WEB_COMPONENT_INSTANTIATORS, webComponents);
        }
        final EEModuleDescription moduleDescription = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
        final EEApplicationClasses applicationClassesDescription = deploymentUnit.getAttachment(Attachments.EE_APPLICATION_CLASSES_DESCRIPTION);
        final CompositeIndex compositeIndex = deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (moduleDescription == null) {
            return; //not an ee deployment
        }
        for (ComponentDescription component : moduleDescription.getComponentDescriptions()) {
            componentByClass.put(component.getComponentClassName(), component);
        }

        final SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        final SipAnnotationMetaData sipAnnotationMetaData = deploymentUnit.getAttachment(SipAnnotationMetaData.ATTACHMENT_KEY);
        if (sipMetaData == null && sipAnnotationMetaData == null) {
        	// not a sip deployment
        	return;
        }
        final Set<String> classes = getAllComponentClasses(deploymentUnit, compositeIndex, sipMetaData, sipAnnotationMetaData);
        for (String clazz : classes) {
            if (clazz == null || clazz.trim().isEmpty()) {
                continue;
            }
            ComponentDescription description = componentByClass.get(clazz);
            if (description != null) {
//                //for now just make sure it has a single view
//                //this will generally be a managed bean, but it could also be an EJB
//                //TODO: make sure the component is a managed bean
//                if (!(description.getViews().size() == 1)) {
//                    throw MESSAGES.wrongComponentType(clazz);
//                }
//                ManagedBeanComponentInstantiator instantiator = new ManagedBeanComponentInstantiator(deploymentUnit, description);
//                webComponents.put(clazz, instantiator);
            } else {
                description = new WebComponentDescription(clazz, clazz, moduleDescription, deploymentUnit.getServiceName(), applicationClassesDescription);
                moduleDescription.addComponent(description);
                webComponents.put(clazz, new WebComponentInstantiator(deploymentUnit, description));
            }
        }
        //deploymentUnit.putAttachment(WebAttachments.WEB_COMPONENT_INSTANTIATORS, webComponents);
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }

    /**
     * Gets all classes that are eligible for injection etc
     *
     * @param metaData
     * @return
     */
    private Set<String> getAllComponentClasses(DeploymentUnit deploymentUnit, CompositeIndex index, SipMetaData sipMetaData, SipAnnotationMetaData sipAnnotationMetaData) {
        final Set<String> classes = new HashSet<String>();
        if (sipAnnotationMetaData != null)  {
            for (Map.Entry<String, SipMetaData> metaData : sipAnnotationMetaData.entrySet()) {
                getAllComponentClasses(metaData.getValue(), classes);
            }
        }
//        if (metaData.getAnnotationsMetaData() != null)
//            for (Map.Entry<String, WebMetaData> webMetaData : metaData.getAnnotationsMetaData().entrySet()) {
//                getAllComponentClasses(webMetaData.getValue(), classes);
//            }
//        if (metaData.getSharedWebMetaData() != null)
//            getAllComponentClasses(metaData.getSharedWebMetaData(), classes);
//        if (metaData.getWebFragmentsMetaData() != null)
//            for (Map.Entry<String, WebFragmentMetaData> webMetaData : metaData.getWebFragmentsMetaData().entrySet()) {
//                getAllComponentClasses(webMetaData.getValue(), classes);
//            }
        if (sipMetaData != null) {
          getAllComponentClasses(sipMetaData, classes);        	
        }
//        if (metaData.getWebMetaData() != null)
//            getAllComponentClasses(metaData.getWebMetaData(), classes);
//        if (tldsMetaData == null)
//            return classes;
//        if (tldsMetaData.getSharedTlds(deploymentUnit) != null)
//            for (TldMetaData tldMetaData : tldsMetaData.getSharedTlds(deploymentUnit)) {
//                getAllComponentClasses(tldMetaData, classes);
//            }
//        if (tldsMetaData.getTlds() != null)
//            for (Map.Entry<String, TldMetaData> tldMetaData : tldsMetaData.getTlds().entrySet()) {
//                getAllComponentClasses(tldMetaData.getValue(), classes);
//            }
//        getAllAsyncListenerClasses(index, classes);
        return classes;
    }

    private void getAllComponentClasses(SipMetaData sipMetaData, Set<String> classes) {
        if (sipMetaData.getSipServlets() != null) {
            for (ServletMetaData servlet : sipMetaData.getSipServlets()) {
                if (servlet.getServletClass() != null) {
                    classes.add(servlet.getServletClass());
                }
            }
        }
        if (sipMetaData.getListeners() != null) {
            for (ListenerMetaData listener : sipMetaData.getListeners()) {
                classes.add(listener.getListenerClass());
            }
        }
    }
}
