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

import org.apache.catalina.core.StandardContext;
import org.jboss.as.clustering.web.DistributedCacheManagerFactory;
import org.jboss.as.clustering.web.DistributedCacheManagerFactoryService;
import org.jboss.as.clustering.web.infinispan.sip.DistributedCacheConvergedSipManagerFactory;
import org.jboss.as.clustering.web.sip.DistributedConvergedCacheManagerFactoryService;
import org.jboss.as.controller.PathElement;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.WebSubsystemServices;
import org.jboss.as.web.deployment.JBossContextConfig;
import org.jboss.as.web.deployment.WarMetaData;
import org.jboss.as.web.ext.WebContextFactory;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.modules.Module;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.vfs.VirtualFile;
import org.mobicents.as7.SipDeploymentDefinition;
import org.mobicents.as7.SipSubsystemServices;
import org.mobicents.metadata.sip.spec.SipMetaData;
import org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.value.InjectedValue;

/**
 * {@code DeploymentUnitProcessor} creating the actual deployment services.
 * For SIP this is only required to setup management points 
 *
 * @author Emanuel Muckenhuber
 * @author Anil.Saldhana@redhat.com
 * @author josemrecio@gmail.com
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
        processDeployment(deploymentUnit, phaseContext.getServiceTarget(), metaData);
    }

    @Override
    public void undeploy(final DeploymentUnit context) {
    	// TODO
    }

    protected void processDeployment(final DeploymentUnit deploymentUnit,
                                     final ServiceTarget serviceTarget,
                                     WarMetaData warMetaData) throws DeploymentUnitProcessingException {
        
    	final VirtualFile deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot();
        final Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        if (module == null) {
            throw new DeploymentUnitProcessingException(MESSAGES.failedToResolveModule(deploymentRoot));
        }
        final SipMetaData sipMetaData = deploymentUnit.getAttachment(SipMetaData.ATTACHMENT_KEY);
        if(sipMetaData != null) {
	        final String appNameMgmt = sipMetaData.getApplicationName();
	    	final ServiceName deploymentServiceName = SipSubsystemServices.deploymentServiceName(appNameMgmt);
	    	
	    	if (logger.isDebugEnabled()){
	    		if (serviceTarget.getDependencies() != null){
	    			logger.debug("processDeployment - serviceTarget.getDependencies()");
	    			for (ServiceName sn: serviceTarget.getDependencies()){
	    				logger.debug("processDeployment - dependency: " + sn.toString());
	    			}
	    		}
        		logger.debug("processDeployment - deploymentServiceName.getSimpleName() = " + deploymentServiceName.getSimpleName());
        		logger.debug("processDeployment - deploymentServiceName.getCanonicalName() = " + deploymentServiceName.getCanonicalName());
        		logger.debug("processDeployment - deploymentServiceName.toString() = " + deploymentServiceName.toString());
        		logger.debug("processDeployment - deploymentServiceName.getParent() = " + deploymentServiceName.getParent());
        	}
	    	
	        try {
	        	final SipDeploymentService sipDeploymentService = new SipDeploymentService(deploymentUnit);
	        	ServiceBuilder<?> builder = serviceTarget
	        			.addService(deploymentServiceName, sipDeploymentService);
	
	        	if (logger.isDebugEnabled()){
	        		logger.debug("processDeployment - start distributable stuff");
	        	}
	        	
	        	
	//        	TODO: when distributable is implemented
	        	if (sipMetaData.getDistributable() != null) {
	        		
	        		
	        		//--------------
	            	final JBossWebMetaData metaData = warMetaData.getMergedJBossWebMetaData();
	            	if (logger.isDebugEnabled()){
	            		logger.debug("processDeployment - is jbosswebmetadata distributable?: " + (metaData.getDistributable() != null));
	            	}
	            	//WebContextFactory contextFactory = deploymentUnit.getAttachment(WebContextFactory.ATTACHMENT);
	            	//if (contextFactory == null) {
	            	//	if (logger.isDebugEnabled()){
	                //		logger.debug("processDeployment - contextFactory is null, using WebContextFactory.DEFAULT");
	                //	}
	                //    contextFactory = WebContextFactory.DEFAULT;
	                //}
	            	
	            	// Create the context
	            	//final SIPWebContext webContext = (SIPWebContext)contextFactory.createContext(deploymentUnit);
	            	final SIPWebContext webContext = deploymentUnit.getAttachment(SIPWebContext.ATTACHMENT);
	            	if (logger.isDebugEnabled()){
	            		if (webContext == null){
	            			logger.error("processDeployment - sip web context is null");
	            		} else {
	            			logger.debug("processDeployment - sip web context picked up successfully");
	            		}
	            	}
	                //final JBossContextConfig config = new JBossContextConfig(deploymentUnit, this.service);
	                final SipJBossContextConfig config = webContext.getSipJBossContextConfig();
	                config.getDistributedCacheManagerFactoryInjector();
	                //--------------
	                
	                if (logger.isDebugEnabled()){
		        		logger.debug("processDeployment - distributable");
		        	}
	        		
	        		//DistributedCacheManagerFactoryService factoryService = new DistributedCacheManagerFactoryService();
	        		//DistributedCacheManagerFactory factory = factoryService.getValue();
	        		
	        		DistributedCacheManagerFactoryService factoryService = new DistributedConvergedCacheManagerFactoryService();
	        		final DistributedCacheManagerFactory factory = factoryService.getValue();
	        		
	        		if (factory != null) {
	        			if (logger.isDebugEnabled()){
	    	        		logger.debug("processDeployment - factory not null");
	    	        	}
	        			
	        			ServiceName factoryServiceName = deploymentServiceName.append("session");
	        			if (logger.isDebugEnabled()){
	    	        		logger.debug("processDeployment - factoryServiceName.getSimpleName() = " + factoryServiceName.getSimpleName());
	    	        		logger.debug("processDeployment - factoryServiceName.getCanonicalName() = " + factoryServiceName.getCanonicalName());
	    	        		logger.debug("processDeployment - factoryServiceName.toString() = " + factoryServiceName.toString());
	    	        		logger.debug("processDeployment - factoryServiceName.getParent() = " + factoryServiceName.getParent());
	    	        	}
	        			
	        			//----------
	        			if (logger.isDebugEnabled()){
	    	        		logger.debug("processDeployment - addInjection");
	    	        	}
	        			//builder.addInjection(config.getDistributedCacheManagerFactoryInjector(), (DistributedCacheConvergedSipManagerFactory)factory);
	        			//----------
	        			
	        			if (logger.isDebugEnabled()){
	    	        		logger.debug("processDeployment - addDependency");
	    	        	}
	        			builder.addDependency(DependencyType.REQUIRED, //DependencyType.OPTIONAL, 
	        					factoryServiceName, 
	        					DistributedCacheConvergedSipManagerFactory.class, 
	        					//DistributedCacheManagerFactory.class, 
	        					config.getDistributedCacheManagerFactoryInjector());
	
	        			ServiceBuilder<DistributedCacheManagerFactory> factoryBuilder = serviceTarget.addService(factoryServiceName, factoryService);
	        			
	        			
	        			if (logger.isDebugEnabled()){
	    	        		logger.debug("processDeployment - addInjection to factoryBuilder");
	    	        	}
	        			
	        			boolean enabled = factory.addDeploymentDependencies(deploymentServiceName, deploymentUnit.getServiceRegistry(), serviceTarget, factoryBuilder, metaData);
	        			if (logger.isDebugEnabled()){
	    	        		logger.debug("processDeployment - install factoryBuilder... enabled = " + enabled);
	    	        	}
	        			factoryBuilder.setInitialMode(enabled ? ServiceController.Mode.ACTIVE : ServiceController.Mode.NEVER).install();
	        			//factoryBuilder.setInitialMode(enabled ? ServiceController.Mode.ON_DEMAND : ServiceController.Mode.NEVER).install();
	        		}
	        	}
	        // TODO - VEGE, eddig tart a distributable-s resz
	        	
	        	
	        	
	        	// add dependency to sip deployment service
	        	if (logger.isDebugEnabled()){
	        		logger.debug("processDeployment - add dependency to sip subsystem services (" + deploymentServiceName + ")");
	        	}
	            builder.addDependency(deploymentServiceName);
	            if (logger.isDebugEnabled()){
	        		logger.debug("processDeployment - install");
	        	}
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
	                node.get("load-on-startup").set(servlet.getLoadOnStartup());
	            } catch (Exception e) {
	                // Should a failure in creating the mgmt view also make to the deployment to fail?
	                continue;
	            }
	        }
    	}
    }
}

