package org.rhq.plugins.mobicents.servlet.sip.jboss5;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.managed.api.ManagedDeployment;
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

public class ConvergedSipEmbeddedDiscoveryComponent extends EmbeddedManagedDeploymentDiscoveryComponent {
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected boolean accept(
			ManagedDeployment managedDeployment,
			ResourceDiscoveryContext<ApplicationServerComponent> resourceDiscoveryContext) {
		boolean isEmbedded = super.accept(managedDeployment, resourceDiscoveryContext);
    	if(!isEmbedded) {
    		return false;
    	}
    	String applicationName = managedDeployment.getSimpleName().substring(0,managedDeployment.getSimpleName().indexOf(".war"));    	
    	List<EmsBean> mBeans = resourceDiscoveryContext.getParentResourceComponent().getConvergedSipApplicationEmsBeans(applicationName);

    	List<EmsBean> parentMBeans = new ArrayList<EmsBean>();
		if(managedDeployment.getParent().getSimpleName().indexOf(".ear") != -1) {
			String parentApplicationName = managedDeployment.getParent().getSimpleName().substring(0,managedDeployment.getParent().getSimpleName().indexOf(".ear"));
			parentMBeans = resourceDiscoveryContext.getParentResourceComponent().getConvergedSipApplicationEmsBeans(parentApplicationName);
		}
		
		List<EmsBean> allBeans = new ArrayList<EmsBean>();
		allBeans.addAll(mBeans);
		allBeans.addAll(parentMBeans);
		
		if(allBeans.size() > 0) {
	    	return true;
	    } else {
	    	return false;
	    }		
	}
}
