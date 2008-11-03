/*
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
package org.mobicents.servlet.sip.startup.jboss;

import org.jboss.web.AbstractWebContainerMBean;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;

/**
 * An implementation of the AbstractWebContainer for the Jakarta Tomcat5
 * servlet container. It has no code dependency on tomcat - only the new JMX
 * model is used.
 * <p/>
 * Tomcat5 is organized as a set of mbeans - just like jboss.
 *
 * @author Scott.Stark@jboss.org
 * @author Costin Manolache
 * @version $Revision: 57206 $
 * @see AbstractWebContainerMBean
 */
public interface JBossSipMBean extends AbstractWebContainerMBean
{
   /** JMX notification type to signal after-start connector event */ 
   public final String TOMCAT_CONNECTORS_STARTED  = "jboss.tomcat.connectors.started";
   
   /**
    * @return the jmx domain for the tomcat management mbeans
    */
   public String getDomain();

   /**
    * The most important attribute - defines the managed domain.
    * A catalina instance (engine) corresponds to a JMX domain, that's
    * how we know where to deploy webapps.
    *
    * @param domainName the jmx domain under which tc registers
    */
   public void setDomain(String domainName);

   /**
    * Set the snapshot mode in a clustered environment
    */
   public void setSnapshotMode(String mode);

   /**
    * Get the snapshot mode in a clustered environment
    */
   public String getSnapshotMode();

   /**
    * Set the snapshot interval in ms for the interval snapshot mode
    */
   public void setSnapshotInterval(int interval);

   /**
    * Get the snapshot interval
    */
   public int getSnapshotInterval();

   /**
    * Get the clustering code cache behaviour
    */
   public boolean isUseLocalCache();

   /**
    * Set the clustering code cache behaviour
    */
   public void setUseLocalCache(boolean useLocalCache);

   /**
    * Get the clustering code failover behaviour whether MOD_JK(2) is used or not.
    */
   public boolean isUseJK();

   /**
    * Set the clustering code failover behaviour whether MOD_JK(2) is used or not.
    */
   public void setUseJK(boolean useJK);

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public void setSessionIdAlphabet(String sessionIdAlphabet);

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public String getSessionIdAlphabet();
   
   /**
    * Gets the JMX object name of a shared TreeCache to be used for clustered
    * single-sign-on.
    *
    * @see org.jboss.web.tomcat.service.sso.TreeCacheSSOClusterManager
    */
   public String getCacheName();

   /**
    * Gets the JMX object name of a shared TreeCache to be used for clustered
    * single-sign-on.
    * <p/>
    * <b>NOTE:</b> TreeCache must be deployed before this service.
    *
    * @see org.jboss.web.tomcat.service.sso.TreeCacheSSOClusterManager
    */
   public void setCacheName(String cacheName);

   /**
    * Get the maximum interval between requests, in seconds, after which a
    * request will trigger replication of the session's metadata regardless
    * of whether the request has otherwise made the session dirty. Such 
    * replication ensures that other nodes in the cluster are aware of a 
    * relatively recent value for the session's timestamp and won't incorrectly
    * expire an unreplicated session upon failover.
    * <p/>
    * Default value is {@link #DEFAULT_MAX_UNREPLICATED_INTERVAL}.
    * <p/>
    * The cost of the metadata replication depends on the configured
    * {@link #setReplicationGranularityString(String) replication granularity}.
    * With <code>SESSION</code>, the sesssion's attribute map is replicated 
    * along with the metadata, so it can be fairly costly.  With other 
    * granularities, the metadata object is replicated separately from the
    * attributes and only contains a String, and a few longs, ints and booleans.
    * 
    * @return the maximum interval since last replication after which a request
    *         will trigger session metadata replication. A value of 
    *         <code>0</code> means replicate metadata on every request; a value 
    *         of <code>-1</code> means never replicate metadata unless the 
    *         session is otherwise dirty.
    */
   public int getMaxUnreplicatedInterval();

   /**
    * Sets the maximum interval between requests, in seconds, after which a
    * request will trigger replication of the session's metadata regardless
    * of whether the request has otherwise made the session dirty.
    * 
    * @param  maxUnreplicatedInterval  
    *         the maximum interval since last replication after which a request
    *         will trigger session metadata replication. A value of 
    *         <code>0</code> means replicate metadata on every request; a value 
    *         of <code>-1</code> means never replicate metadata unless the 
    *         session is otherwise dirty.
    */
   public void setMaxUnreplicatedInterval(int maxUnreplicatedInterval);
   
   /**
    * Get the JBoss UCL use flag
    */
   public boolean getUseJBossWebLoader();

   /**
    * Set the JBoss UCL use flag
    */
   public void setUseJBossWebLoader(boolean flag);

   public String getManagerClass();

   public void setManagerClass(String managerClass);

   /** */
   public String getContextMBeanCode();

   /** */
   public void setContextMBeanCode(String className);

   /**
    * Get the name of the external tomcat server configuration file.
    *
    * @return the config file name, server.xml for example
    */
   public String getConfigFile();

   /**
    * Set the name of the external tomcat server configuration file.
    *
    * @param configFile - the config file name, server.xml for example
    */
   public void setConfigFile(String configFile);

   /**
    * Get the request attribute name under which the JAAS Subject is store
    */
   public String getSubjectAttributeName();

   /**
    * Set the request attribute name under which the JAAS Subject will be
    * stored when running with a security mgr that supports JAAS. If this is
    * empty then the Subject will not be store in the request.
    *
    * @param name the HttpServletRequest attribute name to store the Subject
    */
   public void setSubjectAttributeName(String name);

   /**
    * Start all connectors of the Domain + ":type=Service,serviceName=jboss.web"
    * service.
    *
    * @throws Exception
    */
   public void startConnectors() throws Exception;

   /**
    * Stop all connectors of the Domain + ":type=Service,serviceName=jboss.web"
    * service.
    *
    * @throws Exception
    */
   public void stopConnectors() throws Exception;

   /**
    * Get whether web-apps are able to control the privileged flag
    */
   public boolean isAllowSelfPrivilegedWebApps();

   /**
    * Set whether web-apps are able to control the privileged flag
    */
   public void setAllowSelfPrivilegedWebApps(boolean flag);

   /** Set the SecurityManagerService binding. This is used to flush any
    * associated authentication cache on session invalidation.
    * @param mgr the JaasSecurityManagerServiceMBean
    */ 
   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr);

   /**
    * 
    * @return
    */ 
   public String[] getFilteredPackages();
   /**
    * 
    * @param pkgs
    */ 
   public void setFilteredPackages(String[] pkgs);
}
