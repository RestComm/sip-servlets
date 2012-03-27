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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.plugins.jbossas.EmbeddedWarDiscoveryComponent;
import org.rhq.plugins.jbossas.JBossASServerComponent;
import org.rhq.plugins.jbossas.util.WarDeploymentInformation;
import org.rhq.plugins.jmx.MBeanResourceComponent;

/**
 * Provides helper methods that are used by both {@link WarDiscoveryComponent} and {@link EmbeddedWarDiscoveryComponent}
 *
 * Original file copied over from jopr jboss as plugin trunk rev 26
 * It has been copied because it uses ConvergedDeploymentUtility and 
 * since methods in this class are static they couldn't be extended by inheritance
 *
 * @author Ian Springer
 * @author Heiko W. Rupp
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class ConvergedWarDiscoveryHelper {
    private static final Log LOG = LogFactory.getLog(ConvergedWarDiscoveryHelper.class);

    public static final String JBOSS_WEB_MBEAN_NAME_TEMPLATE = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=%name%";    
    private static final String ROOT_WEBAPP_RT_LOG_FILE_NAME_BASE = "ROOT";
    private static final String RT_LOG_FILE_NAME_SUFFIX = "_rt.log";

    // regex for web-services container-generated war filenames,
    // e.g. rhq.ear-on-enterprise-server-ejb.ejb346088.war
    private static final String WEB_SERVICES_EJB_WAR_FILE_NAME_REGEX = "\\.(ejb|jar)\\d+\\.war$";

    // precompile the pattern for optimal performance
    private static final Pattern WEB_SERVICES_EJB_WAR_FILE_NAME_PATTERN = Pattern
        .compile(WEB_SERVICES_EJB_WAR_FILE_NAME_REGEX);
    private static final String ROOT_WEBAPP_RESOURCE_NAME = "ROOT.war";

    private ConvergedWarDiscoveryHelper() {
    }

    public static Set<DiscoveredResourceDetails> initPluginConfigurations(
        JBossASServerComponent parentJBossASComponent, Set<DiscoveredResourceDetails> warResources) {

        EmsConnection jmxConnection = parentJBossASComponent.getEmsConnection();
        File configPath = parentJBossASComponent.getConfigurationPath();
        File logDir = new File(configPath, "log");
        File rtLogDir = new File(logDir, "rt");

        List<String> objectNames = new ArrayList<String>();
        
        // Create a List of objectNames that will be passed to DeploymentUtility.
        for (DiscoveredResourceDetails resourceDetails : warResources) {        	
            Configuration warConfig = resourceDetails.getPluginConfiguration();
            PropertySimple objectNameProperty = warConfig.getSimple(MBeanResourceComponent.OBJECT_NAME_PROP);
            objectNames.add(objectNameProperty.getStringValue());
        }

        Map<String, List<WarDeploymentInformation>> deploymentInformations = ConvergedDeploymentUtility
            .getConvergedWarDeploymentInformation(jmxConnection, objectNames);

        Set<DiscoveredResourceDetails> resultingResources = new HashSet<DiscoveredResourceDetails>();        
        
        for (Iterator<DiscoveredResourceDetails> warResourcesIterator = warResources.iterator(); warResourcesIterator
            .hasNext();) {
            DiscoveredResourceDetails discoResDetail = warResourcesIterator.next();
            Configuration warConfig = discoResDetail.getPluginConfiguration();
            PropertySimple objectNameProperty = warConfig.getSimple(MBeanResourceComponent.OBJECT_NAME_PROP);
            List<WarDeploymentInformation> deploymentInfoList = null;

            if (deploymentInformations != null) {
                deploymentInfoList = deploymentInformations.get(objectNameProperty.getStringValue());
            }

            if (deploymentInfoList != null) {
                /*
                 * Loop over the list elements and create new resources for all the vhosts
                 * that are not localhost. This is needed so that the majority of installed
                 * webapps will just be found with their existing key in case of an update.
                 */
                for (WarDeploymentInformation info : deploymentInfoList) {                	
                    String vhost = info.getVHost();
                    LOG.debug("vHost " + vhost);
                    if ("localhost".equals(vhost)) {
                        initPluginConfiguration(info, rtLogDir, warResourcesIterator, discoResDetail);
                        resultingResources.add(discoResDetail);
                    } else {
                        DiscoveredResourceDetails myDetail;
                        String key = discoResDetail.getResourceKey();
                        key += ",vhost=" + vhost;

                        Configuration configClone;
                        try {
                            configClone = discoResDetail.getPluginConfiguration().clone();
                        } catch (CloneNotSupportedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            configClone = new Configuration();
                        }

                        myDetail = new DiscoveredResourceDetails(discoResDetail.getResourceType(), key, discoResDetail
                            .getResourceName(), discoResDetail.getResourceVersion(), discoResDetail
                            .getResourceDescription()
                            + " on (" + vhost + ")", configClone, discoResDetail.getProcessInfo());

                        initPluginConfiguration(info, rtLogDir, warResourcesIterator, myDetail);
                        resultingResources.add(myDetail); // the cloned one
                    }
                }
            } else {
                // WAR has no associated context root, so remove it from the list of discovered resources...
                // @todo Might not want to call remove, because, as an example, in EC, the EC war file would end up being removed because this happens before it is completely deployed
                warResourcesIterator.remove();

                if (!discoResDetail.getResourceName().equals(ROOT_WEBAPP_RESOURCE_NAME)) {
                    LOG
                        .debug("The deployed WAR '"
                            + discoResDetail.getResourceName()
                            + "' does not have a jboss.web MBean (i.e. context root) associated with it; it will not be added to inventory.");
                }
            }
        }
        return resultingResources;
    }

    public static void setDeploymentInformation(Configuration pluginConfig,
        WarDeploymentInformation deploymentInformation) {
        // JBNADM-3420 - These are being set incorrectly. Specifically, the incorrect file name is causing invalid
        // file names on windows and thus breaking WAR updates.
        pluginConfig.put(new PropertySimple(ConvergedWarComponent.FILE_NAME, deploymentInformation.getFileName()));
        pluginConfig.put(new PropertySimple(ConvergedWarComponent.JBOSS_WEB_NAME, deploymentInformation
            .getJbossWebModuleMBeanObjectName()));
        pluginConfig.put(new PropertySimple(ConvergedWarComponent.CONTEXT_ROOT_CONFIG_PROP, deploymentInformation
            .getContextRoot()));
        pluginConfig.put(new PropertySimple(ConvergedWarComponent.VHOST_CONFIG_PROP, deploymentInformation.getVHost()));
    }

    public static String getContextPath(String contextRoot) {
        return ((contextRoot.equals(ConvergedWarComponent.ROOT_WEBAPP_CONTEXT_ROOT))
                ? "/" : "/" + contextRoot);
    }
    
    private static void initPluginConfiguration(WarDeploymentInformation deploymentInformation, File rtLogDir,
        Iterator<DiscoveredResourceDetails> warResourcesIterator, DiscoveredResourceDetails resource) {
        Configuration pluginConfig = resource.getPluginConfiguration();
        String warFileName = resource.getResourceName();
        String contextRoot = deploymentInformation.getContextRoot();
        int length = contextRoot.length();
        if (length > 0 && (contextRoot.charAt(length - 1) == '.') // e.g. "//localhost/rhq-rhq-enterprise-server-ejb."
            && WEB_SERVICES_EJB_WAR_FILE_NAME_PATTERN.matcher(warFileName).find()) {
            // It's a web-services container-generated war - we don't want to auto-discover
            // these, since they're not explicitly deployed by the user and their names change
            // on every redeploy of the parent ear (see http://jira.jboss.com/jira/browse/JBNADM-2728).
            // So remove it from the list of discovered resources and move on...
            warResourcesIterator.remove();
            return;
        }

        ConvergedWarDiscoveryHelper.setDeploymentInformation(pluginConfig, deploymentInformation);

        // Set the default value for the 'responseTimeLogFile' plugin config prop.
        // We do it here because the filename is derived from the context root.
        String rtLogFileNameBase = (contextRoot.equals(ConvergedWarComponent.ROOT_WEBAPP_CONTEXT_ROOT)) 
                ? ROOT_WEBAPP_RT_LOG_FILE_NAME_BASE : contextRoot;
        String vHost = deploymentInformation.getVHost();
        if ("localhost".equals(vHost))
            vHost = "";
        else
            vHost = vHost + "_";
        String rtLogFileName = vHost + rtLogFileNameBase + RT_LOG_FILE_NAME_SUFFIX;
        File rtLogFile = new File(rtLogDir, rtLogFileName);
        pluginConfig.put(new PropertySimple(ConvergedWarComponent.RESPONSE_TIME_LOG_FILE_CONFIG_PROP, rtLogFile));
    }
}