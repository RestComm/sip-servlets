/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.plugins.mobicents.servlet.sip.jboss5;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedProperty;

/**
 * @author Ian Springer
 */
public class StandaloneManagedDeploymentDiscoveryComponent extends ManagedDeploymentDiscoveryComponent
{
	private final Log log = LogFactory.getLog(this.getClass());    
	
    protected boolean accept(ManagedDeployment managedDeployment)
    {
    	boolean isEmbedded = false;
    	if(managedDeployment.getParent() != null) {
    		isEmbedded = true;
    	}
    	
    	if(!isEmbedded){
    	
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
	        return true;
    	} else {
    		return false;
    	}
    }
}
