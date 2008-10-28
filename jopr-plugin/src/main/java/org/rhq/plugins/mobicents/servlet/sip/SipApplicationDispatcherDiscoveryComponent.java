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
import org.mc4j.ems.connection.bean.EmsBean;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.plugins.jmx.MBeanResourceComponent;
import org.rhq.plugins.jmx.MBeanResourceDiscoveryComponent;

/**
 * Discover JBossCache instances. The only way to detect them are to
 * look for "*:cache-interceptor=CacheMgmtInterceptor,*" or "*:treecache-interceptor=CacheMgmtInterceptor,*"
 * This is done in {@link MBeanResourceDiscoveryComponent}. We postprocess the result here to 
 * get the base MBean name (without the property for detection) to be able to use this later and also
 * for display purposes, as this is the MBean name the user set up in his MBean.
 * 
 * @author Heiko W. Rupp
 */
public class SipApplicationDispatcherDiscoveryComponent implements ResourceDiscoveryComponent<MBeanResourceComponent> {

    private static final Log log = LogFactory.getLog(SipApplicationDispatcherDiscoveryComponent.class);

    public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext<MBeanResourceComponent> context) {

       EmsBean statsBean = context.getParentResourceComponent().getEmsBean();
       
        log.debug("statsBean " + statsBean);
        
        Set<DiscoveredResourceDetails> results = new HashSet<DiscoveredResourceDetails>();
//        Set<DiscoveredResourceDetails> results = new HashSet<DiscoveredResourceDetails>(discovered.size());

//        log.debug("results " + results);
//        // Normalize the base object names from the key
//        for (DiscoveredResourceDetails detail : discovered) {
//        	String key = detail.getResourceKey();
//        	log.info("resource key " + key);        	
//          
//            try {
//                ObjectName on = ObjectName.getInstance(key);
////                key = on.getDomain();
////                key += ":";
////                Set<String> propKeys = on.getKeyPropertyList().keySet();
////                for (String prop : propKeys) {
////                    if (!(prop.contains("cache"))) {
////                        key += prop + "=" + on.getKeyProperty(prop);
////                        key += ",";
////                    }
////                }
////                if (key.endsWith(","))
////                    key = key.substring(0, key.length() - 1);
////                if (log.isDebugEnabled())
////                    log.debug("Translated " + detail.getResourceKey() + " to " + key);
//                detail.setResourceKey(key);
//                detail.setResourceName(key);
//                String descr = "Sip Application Dispatcher at key " + key;
//                detail.setResourceDescription(descr);
//
//                Configuration pluginConfiguration = detail.getPluginConfiguration();
//                PropertySimple onProp = pluginConfiguration.getSimple("objectName");
//                onProp.setStringValue(key);
//                
//                results.add(detail);
//            } catch (MalformedObjectNameException e) {
//                log.warn("Invalid obectname : " + key);
//            }
//        }

        return results;
    }
}
