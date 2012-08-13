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

import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.BindingConfiguration;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.component.deployers.EEResourceReferenceProcessorRegistry;
import org.jboss.as.server.deployment.AttachmentList;
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

    static final String[] JNDI_BASE_FOR_SIP = {"java:comp/env/", "java:app/"};
    static final String SIP_PREFIX_JNDI = "sip/";
    static final String SIP_FACTORY_JNDI = "SipFactory";
    static final String SIP_SESSIONS_UTIL_JNDI = "SipSessionsUtil";
    static final String SIP_TIMER_SERVICE_JNDI = "TimerService";

    @Override
    public void deploy(final DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        final DeploymentUnit parentDU = deploymentUnit.getParent();

        // look for the sip application name to properly build the jndi name
        String sipApplicationName = null;
        SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        if (sipMetaData != null) {
        	sipApplicationName = sipMetaData.getApplicationName();
        }        	
        else if (parentDU != null){
            AttachmentList<DeploymentUnit> subDeploymentList = parentDU.getAttachment(org.jboss.as.server.deployment.Attachments.SUB_DEPLOYMENTS);
            for (final DeploymentUnit subDeployment : subDeploymentList) {
            	sipMetaData = subDeployment.getAttachment(SipMetaData.ATTACHMENT_KEY);
            	if (sipMetaData != null) {
            		sipApplicationName = sipMetaData.getApplicationName();
            		break;
            	}
            }        	
        }
        if (sipApplicationName == null) {
        	// not a sip deployment
        	return;
        }
        // anchorDU will have a SIPWebContext.ATTACHMENT so jndi lookups can be resolved to context resources
        final DeploymentUnit anchorDU = SIPWebContext.getSipContextAnchorDu(deploymentUnit);
        setupSipJndiBindingsPerDeploymentUnit(anchorDU, deploymentUnit, sipApplicationName);
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
    	// TODO: do we need to explictly remove jndi bindings?

    }

    private static void setupSipJndiBindingsPerDeploymentUnit(final DeploymentUnit anchorDU, final DeploymentUnit subDU, final String sipApplicationName) {
		final EEResourceReferenceProcessorRegistry registry = subDU.getAttachment(Attachments.RESOURCE_REFERENCE_PROCESSOR_REGISTRY);
		// Add a EEResourceReferenceProcessor which handles @Resource references with no mappedName
		registry.registerResourceReferenceProcessor(new SipFactoryResourceProcessor(anchorDU));
		registry.registerResourceReferenceProcessor(new SipSessionsUtilResourceProcessor(anchorDU));
		registry.registerResourceReferenceProcessor(new SipTimerServiceResourceProcessor(anchorDU));

		// Create binding between jndi path and injection source
		final EEModuleDescription description = subDU.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
		if (description == null) {
			return;
		}
		// Add bindings proper
		for (String jndiBaseSip: JNDI_BASE_FOR_SIP) {
			final String jndiBaseMapped = jndiBaseSip + SIP_PREFIX_JNDI + sipApplicationName + "/";
			description.getBindingConfigurations().add(new BindingConfiguration(jndiBaseMapped + SIP_FACTORY_JNDI, new SipFactoryInjectionSource(anchorDU)));
			description.getBindingConfigurations().add(new BindingConfiguration(jndiBaseMapped + SIP_SESSIONS_UTIL_JNDI, new SipSessionsUtilInjectionSource(anchorDU)));
			description.getBindingConfigurations().add(new BindingConfiguration(jndiBaseMapped + SIP_TIMER_SERVICE_JNDI, new SipTimerServiceInjectionSource(anchorDU)));
		}
	}
}