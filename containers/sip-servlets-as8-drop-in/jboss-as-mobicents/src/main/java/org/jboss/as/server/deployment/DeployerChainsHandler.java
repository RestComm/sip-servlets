package org.jboss.as.server.deployment;

import java.util.List;

import org.jboss.msc.service.ServiceController;


public class DeployerChainsHandler {
    public enum Operations{
        REMOVE
    }
    
    
    public static boolean handleDeployerChains(final DeploymentPhaseContext phaseContext, Phase phase, Class<DeploymentUnitProcessor> deploymentUnitProcessorClass, Operations operation){
        boolean result=false;
        ServiceController<?> deployerChainServiceController = phaseContext.getServiceRegistry().getService(Services.JBOSS_DEPLOYMENT_CHAINS);
        DeployerChains chains = (DeployerChains)deployerChainServiceController.getValue();
        List<RegisteredDeploymentUnitProcessor> processors = chains.getChain(phase);
        
        for(RegisteredDeploymentUnitProcessor processor : processors){
            if(deploymentUnitProcessorClass == processor.getProcessor().getClass()){
                if(operation == Operations.REMOVE){
                    result = processors.remove(processor);
                    
                    break;
                }
            }
        }
        return result;
    }
}
