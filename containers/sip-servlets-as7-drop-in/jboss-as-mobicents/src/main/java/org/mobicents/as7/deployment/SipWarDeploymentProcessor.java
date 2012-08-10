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

import static org.mobicents.as7.SipMessages.MESSAGES;

import org.jboss.as.controller.PathElement;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.dmr.ModelNode;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.vfs.VirtualFile;
import org.mobicents.as7.SipDeploymentDefinition;
import org.mobicents.as7.SipSubsystemServices;
import org.mobicents.metadata.sip.spec.SipMetaData;


/**
 * {@code DeploymentUnitProcessor} creating the actual deployment services.
 * For SIP this is only required to setup management points 
 *
 * @author Emanuel Muckenhuber
 * @author Anil.Saldhana@redhat.com
 * @author josemrecio@gmail.com
 */
public class SipWarDeploymentProcessor implements DeploymentUnitProcessor {

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
        processDeployment(deploymentUnit, phaseContext.getServiceTarget());
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
    	// TODO
    }

    protected void processDeployment(final DeploymentUnit deploymentUnit,
                                     final ServiceTarget serviceTarget) throws DeploymentUnitProcessingException {
        final VirtualFile deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
        final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        if (module == null) {
            throw new DeploymentUnitProcessingException(MESSAGES.failedToResolveModule(deploymentRoot));
        }
        final SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        if(sipMetaData != null) {
	        final String appNameMgmt = sipMetaData.getApplicationName();
	    	final ServiceName deploymentServiceName = SipSubsystemServices.deploymentServiceName(appNameMgmt);
	        try {
	        	final SipDeploymentService sipDeploymentService = new SipDeploymentService(deploymentUnit);
	        	ServiceBuilder<?> builder = serviceTarget
	        			.addService(deploymentServiceName, sipDeploymentService);
	
	//        	TODO: when distributable is implemented
	//        	if (sipMetaData.getDistributable() != null) {
	//        		DistributedCacheManagerFactoryService factoryService = new DistributedCacheManagerFactoryService();
	//        		DistributedCacheManagerFactory factory = factoryService.getValue();
	//        		if (factory != null) {
	//        			ServiceName factoryServiceName = deploymentServiceName.append("session");
	//        			builder.addDependency(DependencyType.OPTIONAL, factoryServiceName, DistributedCacheManagerFactory.class, config.getDistributedCacheManagerFactoryInjector());
	//
	//        			ServiceBuilder<DistributedCacheManagerFactory> factoryBuilder = serviceTarget.addService(factoryServiceName, factoryService);
	//        			boolean enabled = factory.addDeploymentDependencies(deploymentServiceName, deploymentUnit.getServiceRegistry(), serviceTarget, factoryBuilder, metaData);
	//        			factoryBuilder.setInitialMode(enabled ? ServiceController.Mode.ON_DEMAND : ServiceController.Mode.NEVER).install();
	//        		}
	//        	}
	            // add dependency to sip deployment service
	            builder.addDependency(deploymentServiceName);
	            builder.setInitialMode(Mode.ACTIVE).install();
	        } catch (ServiceRegistryException e) {
	        	throw new DeploymentUnitProcessingException(MESSAGES.failedToAddSipDeployment(), e);
	        }        

	        // Process sip related mgmt information
	        final ModelNode node = deploymentUnit.getDeploymentSubsystemModel("sip");
	        node.get(SipDeploymentDefinition.APP_NAME.getName()).set("".equals(appNameMgmt) ? "/" : appNameMgmt);
	        processManagement(deploymentUnit, sipMetaData);
        } else {
        	
        }
    }

    void processManagement(final DeploymentUnit unit, final SipMetaData sipMetaData) {
    	if(sipMetaData.getSipServlets() != null) {
	        for (final ServletMetaData servlet : sipMetaData.getSipServlets()) {
	            try {
	                final String name = servlet.getName().replace(' ', '_');
	                final ModelNode node = unit.createDeploymentSubModel("sip", PathElement.pathElement("servlet", name));
	                node.get("servlet-class").set(servlet.getServletClass());
	                node.get("servlet-name").set(servlet.getServletName());
	            } catch (Exception e) {
	                // Should a failure in creating the mgmt view also make to the deployment to fail?
	                continue;
	            }
	        }
    	}
    }
}

