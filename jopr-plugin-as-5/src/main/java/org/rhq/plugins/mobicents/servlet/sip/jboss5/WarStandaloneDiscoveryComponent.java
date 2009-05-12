package org.rhq.plugins.mobicents.servlet.sip.jboss5;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.managed.api.ManagedDeployment;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class WarStandaloneDiscoveryComponent extends StandaloneManagedDeploymentDiscoveryComponent {
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected boolean accept(
			ManagedDeployment managedDeployment,
			ResourceDiscoveryContext<ApplicationServerComponent> resourceDiscoveryContext) {
		boolean isEmbedded = !super.accept(managedDeployment, resourceDiscoveryContext);
    	if(isEmbedded) {
    		return false;
    	}
    	String applicationName = managedDeployment.getSimpleName().substring(0,managedDeployment.getSimpleName().indexOf(".war"));
		List<EmsBean> mBeans = resourceDiscoveryContext.getParentResourceComponent().getWebApplicationEmsBeans(applicationName);
		if(mBeans.size() > 0) {
	    	return true;
	    } else {
	    	return false;
	    }
//    		log.warn(managedDeployment.getName());
//    		log.warn(managedDeployment.getSimpleName());    		
//	    	log.warn("Children");
//	    	for(ManagedDeployment managedDeploymenttemp : managedDeployment.getChildren()){ 
//	    		log.warn("Child : " + managedDeploymenttemp.getName());
//	    	}
//	    	log.warn("Components");
//	    	for(Entry<String, ManagedComponent> component : managedDeployment.getComponents().entrySet()){ 
//	    		log.warn("ManagedComponent : key=" + component.getKey() +" ,value=" + component.getValue().getName());
//	    	}
//	    	log.warn("ManagedObjectNames");
//	    	for(String managedObjectName : managedDeployment.getManagedObjectNames()){ 
//	    		log.warn("ManagedObjectName : " + managedObjectName );
//	    	}
//	    	log.warn("Properties");
//	    	for(Entry<String, ManagedProperty> property : managedDeployment.getProperties().entrySet()){ 
//	    		log.warn("ManagedProperty : key=" + property.getKey() +" ,value=" + property.getValue().getName());
//	    	}    	    
	}
}
