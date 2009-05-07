 /*
  * Jopr Management Platform
  * Copyright (C) 2005-2008 Red Hat, Inc.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License, version 2, as
  * published by the Free Software Foundation, and/or the GNU Lesser
  * General Public License, version 2.1, also as published by the Free
  * Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License and the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License
  * and the GNU Lesser General Public License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
package org.rhq.plugins.mobicents.servlet.sip;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jbossas.JBossASServerComponent;
import org.rhq.plugins.jmx.MBeanResourceDiscoveryComponent;

/**
 * JON plugin discovery component for finding SAR/WARs that are nested inside of an EAR.
 *
 * Original file *NOT* copied over from jopr jboss as plugin trunk rev 26 because it was failing with a classcastexception
 * 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class EmbeddedConvergedWarDiscoveryComponent extends MBeanResourceDiscoveryComponent {
    // ResourceDiscoveryComponent Implementation  --------------------------------------------
	private static final Log log = LogFactory.getLog(EmbeddedConvergedWarDiscoveryComponent.class);
	
    @Override
    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext context) {
    	//discovering ear kind of app
    	Set<DiscoveredResourceDetails> resources = super.discoverResources(context);
    	
    	Set<DiscoveredResourceDetails> resourceDetails = new HashSet<DiscoveredResourceDetails>();
    	log.debug("resources " + resources.size());
    	//For each EAR app, try to discover if it contains a sip application (Manager will be of type SipManager)
    	for (DiscoveredResourceDetails discoveredResourceDetails : resources) {
            // Generate object name based on the parent EAR with the following format.
            //   jboss.management.local:J2EEApplication=rhq.ear,J2EEServer=Local,j2eeType=WebModule,name=on-portal.war    	
        	JBossASServerComponent parentJBossASComponent = (JBossASServerComponent) context.getParentResourceComponent();    	
        	    	
//            ApplicationResourceComponent parentEarComponent = (ApplicationResourceComponent) context
//                .getParentResourceComponent();
            String parentEar = discoveredResourceDetails.getResourceName();

            String objectName = "jboss.management.local:J2EEApplication=" + parentEar
                + ",J2EEServer=Local,j2eeType=WebModule,name=%name%";
     
            log.debug("discoveredResourceDetails Object name to dicsover the converged sar/war embedded resource " + 
            		objectName);
            // Stuff the object name into the default plugin configuration to look like any other JMX discovery
            // where the objectName is read from the default plugin configuration
            Configuration defaultPluginConfiguration = context.getDefaultPluginConfiguration();
            defaultPluginConfiguration.put(new PropertySimple(MBeanResourceDiscoveryComponent.PROPERTY_OBJECT_NAME,
                objectName));

            // Call the base MBean discovery method to perform the actual discovery
           resourceDetails = super.performDiscovery(defaultPluginConfiguration,
            		parentJBossASComponent, context.getResourceType());
           log.debug("resourceDetails size" + resourceDetails.size()); 
           resourceDetails = ConvergedWarDiscoveryHelper.initPluginConfigurations(parentJBossASComponent, resourceDetails);
           log.debug("resourceDetails final size" + resourceDetails.size());
            // 2) Then the stuff specific to embedded WARs...
//            String parentEarFullFileName = parentEarComponent.getFileName() + File.separator;
//            for (DiscoveredResourceDetails resource : resourceDetails) {
//                Configuration pluginConfiguration = resource.getPluginConfiguration();
//                pluginConfiguration.put(new PropertySimple(ConvergedWarComponent.FILE_NAME, parentEarFullFileName
//                    + resource.getResourceName()));
//            }            
		}
    	
    	return resourceDetails;
    }
}