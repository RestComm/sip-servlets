/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.BindingConfiguration;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.component.deployers.EEResourceReferenceProcessorRegistry;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.mobicents.metadata.sip.spec.SipMetaData;

/**
 * @author Stuart Douglas
 * @author josemrecio@gmail.com
 */
public class SipJndiBindingProcessor implements DeploymentUnitProcessor {

    static final String[] JNDI_BASE_FOR_SIP = {"java:", "java:app/", "java:comp/env/"};
    static final String SIP_PREFIX_JNDI = "sip/";
    static final String SIP_FACTORY_JNDI = "SipFactory";
    static final String SIP_SESSIONS_UTIL_JNDI = "SipSessionsUtil";
    static final String SIP_TIMER_SERVICE_JNDI = "TimerService";


    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.EAR, deploymentUnit)) {
            return;
        }
        // look for the sip application name to properly build the jndi name
        String sipApplicationName = null;
        for (final DeploymentUnit subDeployment : deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.SUB_DEPLOYMENTS)) {
            SipMetaData sipMetaData = subDeployment.getAttachment(SipMetaData.ATTACHMENT_KEY);
            if (sipMetaData != null) {
                sipApplicationName = sipMetaData.getApplicationName();
                break;
            }
        }
        if (sipApplicationName == null) {
            // not a sip deployment
            return;
        }
        for (final DeploymentUnit subDeployment : deploymentUnit.getAttachment(org.jboss.as.server.deployment.Attachments.SUB_DEPLOYMENTS)) {
            final EEResourceReferenceProcessorRegistry registry = subDeployment.getAttachment(Attachments.RESOURCE_REFERENCE_PROCESSOR_REGISTRY);
            // Add a EEResourceReferenceProcessor which handles @Resource references with no mappedName
            registry.registerResourceReferenceProcessor(new SipFactoryResourceProcessor(deploymentUnit));
            registry.registerResourceReferenceProcessor(new SipSessionsUtilResourceProcessor(deploymentUnit));
            registry.registerResourceReferenceProcessor(new SipTimerServiceResourceProcessor(deploymentUnit));

            // Create binding between jndi path and injection source
            final EEModuleDescription description = subDeployment.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
            if (description == null) {
                continue;
            }
            for (String jndiBaseSip: JNDI_BASE_FOR_SIP) {
                final String jndiBaseMapped = jndiBaseSip + SIP_PREFIX_JNDI + sipApplicationName + "/";
                description.getBindingConfigurations().add(new BindingConfiguration(jndiBaseMapped + SIP_FACTORY_JNDI, new SipFactoryInjectionSource(deploymentUnit)));
                description.getBindingConfigurations().add(new BindingConfiguration(jndiBaseMapped + SIP_SESSIONS_UTIL_JNDI, new SipSessionsUtilInjectionSource(deploymentUnit)));
                description.getBindingConfigurations().add(new BindingConfiguration(jndiBaseMapped + SIP_TIMER_SERVICE_JNDI, new SipTimerServiceInjectionSource(deploymentUnit)));
            }
        }
    }

    @Override
    public void undeploy(final DeploymentUnit context) {

    }

}
