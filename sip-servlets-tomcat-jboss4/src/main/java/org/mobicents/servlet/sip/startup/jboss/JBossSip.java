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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.security.jacc.PolicyContext;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployerExt;
import org.jboss.metadata.WebMetaData;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.server.Server;
import org.jboss.system.server.ServerImplMBean;
import org.jboss.web.AbstractWebDeployer;
import org.jboss.web.tomcat.security.HttpServletRequestPolicyContextHandler;
import org.jboss.web.tomcat.service.DeployerConfig;
import org.jboss.web.tomcat.service.JBossWebMBean;
import org.jboss.web.tomcat.service.session.SessionIDGenerator;
import org.mobicents.servlet.sip.annotations.SipApplicationAnnotationUtils;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.SipHostConfig;


/**
 * An implementation of the AbstractConvergedContainer for the Jakarta Tomcat5
 * servlet container. It has no code dependency on tomcat - only the new JMX
 * model is used.
 * <p/>
 * Tomcat5 is organized as a set of mbeans - just like jboss.
 *
 * @author Scott.Stark@jboss.org
 * @author Costin Manolache
 * @author Wonne.Keysers@realsoftware.be
 * @author Dimitris.Andreadis@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 57206 $
 * @see org.jboss.web.AbstractWebContainer
 */
public class JBossSip extends AbstractConvergedContainer
   implements JBossWebMBean, NotificationListener
{		
   // Constants -----------------------------------------------------
   public static final String NAME = "JBossSip";

   /**
    * Default value for property <code>cacheName</code>. This name will be used by JBossCache exclusively
    * for Tomcat clustering, e.g., session and sso replication.
    */
   public static final String DEFAULT_CACHE_NAME =
      "jboss.cache:service=TomcatClusteringCache";

   // XXX We could make this configurable - so it can support other containers
   // that provide JMX-based deployment.
   private String contextClassName =
	      "org.apache.catalina.core.StandardContext"; 
   
   /**
    * Configurable map of tomcat authenticators
    * Keyed in by the http auth method that gets 
    * plugged into the Context Config and then into the StandardContext 
    */
   private Properties authenticators = null;
   
   /**
    * Domain for tomcat5 mbeans
    */
   private String catalinaDomain = "Catalina";

   /**
    * ObjectName of a shared TreeCache used for clustered session replication
    * and clustered single-sign-on
    */
   private String cacheName = DEFAULT_CACHE_NAME;

   /**
    * The fully qualified name of the class that will be used for session
    * management if <tt>distributable</tt> is set to true.
    */
   protected String managerClass = "org.jboss.web.tomcat.service.session.JBossCacheManager";

   /**
    * With IntervalSnapshotManager use this interval (in ms)
    * for snapshotting
    */
   private int snapshotInterval = 1000;

   /**
    * Which snapshot mode should be used in clustered environment?
    * Default: instant
    */
   private String snapshotMode = "instant"; // instant or interval

   /**
    * Should the clustering code use a local cache for the sessions?
    */
   private boolean useLocalCache = true;

   /**
    * Maximum interval a clustered session should allow request to not
    * replicate the session timestamp. 
    */
   private int maxUnreplicatedInterval = WebMetaData.DEFAULT_MAX_UNREPLICATED_INTERVAL;
   
   /**
    * Whether we are using Apache MOD_JK(2) module or not
    */
   private boolean useJK = false;

   /**
    * A flag indicating if the JBoss Loader should be used
    */
   private boolean useJBossWebLoader = true;

   /**
    * JBAS-3358: Work directory shouldn't be deleted on Context Destroy
    */
   private boolean deleteWorkDirOnContextDestroy = false;
   
   /**
    * JBAS-2283: Provide custom header based auth support
    */
   private String httpHeaderForSSOAuth = null;
   private String sessionCookieForSSOAuth = null;
   
   /**
    * The server xml configuration file name
    */
   private String serverConfigFile = "server.xml";

   /**
    * Get the request attribute name under which the JAAS Subject is store
    */
   private String subjectAttributeName = null;
   
   /**
    * Flag indicating whether web-app specific context xmls may set the privileged flag.
    */
   private boolean allowSelfPrivilegedWebApps = false;
   
   /** The service used to flush authentication cache on session invalidation. */
   private JaasSecurityManagerServiceMBean secMgrService;
   
   /** */
   private String[] filteredPackages;
   
   /** Hold a proxy reference to myself, used when registering to MainDeployer */
   private SubDeployerExt thisProxy;

   public String getName()
   {
      return NAME;
   }


   public String getManagerClass()
   {
      return managerClass;
   }

   public void setManagerClass(String managerClass)
   {
      this.managerClass = managerClass;
   }


   public String getDomain()
   {
      return this.catalinaDomain;
   }
   public Properties getAuthenticators()
   {
      return this.authenticators; 
   }
   
   public void setAuthenticators(Properties prop)
   {
      this.authenticators = prop;
      log.debug("Passed set of authenticators=" + prop);
   }
   
   /**
    * The most important atteribute - defines the managed domain.
    * A catalina instance (engine) corresponds to a JMX domain, that's
    * how we know where to deploy webapps.
    *
    * @param catalinaDomain the domain portion of the JMX ObjectNames
    */
   public void setDomain(String catalinaDomain)
   {
      this.catalinaDomain = catalinaDomain;
   }

   public void setContextMBeanCode(String className)
   {
      this.contextClassName = className;
   }

   public String getContextMBeanCode()
   {
      return contextClassName;
   }

   /**
    * Set the snapshot interval in milliseconds for snapshot mode = interval
    */
   public void setSnapshotInterval(int interval)
   {
      this.snapshotInterval = interval;
   }

   /**
    * Get the snapshot interval
    */
   public int getSnapshotInterval()
   {
      return this.snapshotInterval;
   }

   /**
    * Set the snapshot mode. Currently supported: instant or interval
    */
   public void setSnapshotMode(String mode)
   {
      this.snapshotMode = mode;
   }

   /**
    * Get the snapshot mode
    */
   public String getSnapshotMode()
   {
      return this.snapshotMode;
   }

   /**
    * Gets the JMX object name of a shared TreeCache to be used for clustered
    * single-sign-on.
    *
    * @see #DEFAULT_CACHE_NAME
    * @see org.jboss.web.tomcat.service.sso.TreeCacheSSOClusterManager
    */
   public String getCacheName()
   {
      return cacheName;
   }

   /**
    * Gets the JMX object name of a shared TreeCache to be used for clustered
    * single-sign-on.
    * <p/>
    * <b>NOTE:</b> TreeCache must be deployed before this service.
    *
    * @see #DEFAULT_CACHE_NAME
    * @see org.jboss.web.tomcat.service.sso.TreeCacheSSOClusterManager
    */
   public void setCacheName(String cacheName)
   {
      this.cacheName = cacheName;
   }

   public boolean isUseLocalCache()
   {
      return useLocalCache;
   }

   public void setUseLocalCache(boolean useLocalCache)
   {
      this.useLocalCache = useLocalCache;
   }

   public boolean isUseJK()
   {
      return useJK;
   }

   public void setUseJK(boolean useJK)
   {
      this.useJK = useJK;
   } 
   
   public int getMaxUnreplicatedInterval()
   {
      return maxUnreplicatedInterval;
   }

   public void setMaxUnreplicatedInterval(int maxUnreplicatedInterval)
   {
      this.maxUnreplicatedInterval = maxUnreplicatedInterval;
   }

   public boolean getDeleteWorkDirOnContextDestroy()
   {
      return deleteWorkDirOnContextDestroy;
   }

   public void setDeleteWorkDirOnContextDestroy(boolean deleteFlag)
   {
      this.deleteWorkDirOnContextDestroy = deleteFlag;
   } 

   public String getHttpHeaderForSSOAuth()
   {
      return httpHeaderForSSOAuth;
   }

   public void setHttpHeaderForSSOAuth(String httpHeader)
   {
      this.httpHeaderForSSOAuth = httpHeader;
   }

   public String getSessionCookieForSSOAuth()
   {
      return sessionCookieForSSOAuth;
   }

   public void setSessionCookieForSSOAuth(String sessionC)
   {
      this.sessionCookieForSSOAuth = sessionC;
   }

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public void setSessionIdAlphabet(String sessionIdAlphabet)
   {
       SessionIDGenerator.getInstance().setSessionIdAlphabet(sessionIdAlphabet);
   }

   /**
    * The SessionIdAlphabet is the set of characters used to create a session Id
    */
   public String getSessionIdAlphabet()
   {
       return SessionIDGenerator.getInstance().getSessionIdAlphabet();
   }
   
   public boolean getUseJBossWebLoader()
   {
      return useJBossWebLoader;
   }

   public void setUseJBossWebLoader(boolean flag)
   {
      this.useJBossWebLoader = flag;
   }

   public String getConfigFile()
   {
      return serverConfigFile;
   }

   public void setConfigFile(String configFile)
   {
      this.serverConfigFile = configFile;
   }

   public String getSubjectAttributeName()
   {
      return this.subjectAttributeName;
   }

   public void setSubjectAttributeName(String name)
   {
      this.subjectAttributeName = name;
   }

   public boolean isAllowSelfPrivilegedWebApps()
   {
      return allowSelfPrivilegedWebApps;
   }

   public void setAllowSelfPrivilegedWebApps(boolean allowSelfPrivilegedWebApps)
   {
      this.allowSelfPrivilegedWebApps = allowSelfPrivilegedWebApps;
   }

   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr)
   {
      this.secMgrService = mgr;
   }

   public String[] getFilteredPackages()
   {
      return filteredPackages;
   }
   public void setFilteredPackages(String[] pkgs)
   {
      this.filteredPackages = pkgs;
   }

   public void startService()
      throws Exception
   {

      System.setProperty("catalina.ext.dirs",
         (System.getProperty("jboss.server.home.dir")
         + File.separator + "lib"));

      String objectNameS = catalinaDomain + ":type=server";
      ObjectName objectName = new ObjectName(objectNameS);

      // Set the modeler Registry MBeanServer to the that of the tomcat service
      Registry.getRegistry().setMBeanServer(server);

      Registry.getRegistry().registerComponent(Class.forName("org.apache.catalina.startup.Catalina").newInstance(), 
              objectName, "org.apache.catalina.startup.Catalina");

      server.setAttribute(objectName, new Attribute
         ("catalinaHome",
            System.getProperty("jboss.server.home.dir")));
      server.setAttribute(objectName, new Attribute
         ("configFile", serverConfigFile));
      server.setAttribute(objectName, new Attribute
         ("useNaming", Boolean.valueOf(false)));
      server.setAttribute(objectName, new Attribute
         ("useShutdownHook", Boolean.valueOf(false)));
      server.setAttribute(objectName, new Attribute
         ("await", Boolean.valueOf(false)));
      server.setAttribute(objectName, new Attribute
         ("redirectStreams", Boolean.valueOf(false)));

      server.invoke(objectName, "create", new Object[]{},
         new String[]{});

      server.invoke(objectName, "start", new Object[]{},
         new String[]{});

      // Configure any SingleSignOn valves      
      
      ObjectName ssoQuery = new ObjectName("*:type=Valve,*");
      Iterator iterator = server.queryMBeans(ssoQuery, null).iterator();
      while (iterator.hasNext())
      {
         ObjectName ssoObjectName =
            ((ObjectInstance) iterator.next()).getObjectName();
         String name = ssoObjectName.getKeyProperty("name");
         
         /* Ensure that the SingleSignOn valve requires that each
            request be reauthenticated to the security mgr. Should not
            be neccessary now that we cache the principal in the session.
         if ((name != null) && (name.indexOf("SingleSignOn") >= 0))
         {
            log.info("Turning on reauthentication of each request on " +
                     ssoObjectName);
            server.setAttribute(ssoObjectName, new Attribute
               ("requireReauthentication", Boolean.TRUE));
         }
         */
            
         // If the valve is a ClusteredSingleSignOn and we have a shared
         // TreeCache configured, configure the valve to use the shared one
         if (cacheName != null && "ClusteredSingleSignOn".equals(name))
         {
            String tcName = (String) server.getAttribute(ssoObjectName,
               "treeCacheName");
            tcName = (tcName != null ? tcName : DEFAULT_CACHE_NAME);
            ObjectName ssoCacheName = new ObjectName(tcName);
            // Only override if the valve's cacheName property was not
            // explicitly set in server.xml to a non-default value
            if (ssoCacheName.equals(new ObjectName(DEFAULT_CACHE_NAME)))
            {
               log.info("Setting the cache name to " + cacheName +
                  " on " + ssoObjectName);
               server.setAttribute(ssoObjectName,
                  new Attribute("treeCacheName", cacheName));
            }
         }
      }

      // Register the web container JACC PolicyContextHandlers
      HttpServletRequestPolicyContextHandler handler = new HttpServletRequestPolicyContextHandler();
      PolicyContext.registerHandler(HttpServletRequestPolicyContextHandler.WEB_REQUEST_KEY,
         handler, true);

      // The ServiceController used to control web app startup dependencies
      serviceController = (ServiceControllerMBean)
         MBeanProxyExt.create(ServiceControllerMBean.class, ServiceControllerMBean.OBJECT_NAME, server);

      // make a proxy to myself, so that calls from the MainDeployer
      // can go through the MBeanServer, so interceptors can be added
      thisProxy = (SubDeployerExt)
         MBeanProxyExt.create(SubDeployerExt.class, super.getServiceName(), super.getServer());

      // Register with the main deployer
      mainDeployer.addDeployer(thisProxy);

      // If we are hot-deployed *after* the overall server is started
      // we'll never receive Server.START_NOTIFICATION_TYPE, so check
      // with the Server and start the connectors immediately, if this is the case.
      // Otherwise register to receive the server start-up notification.
      Boolean started = (Boolean)server.getAttribute(ServerImplMBean.OBJECT_NAME, "Started");
      if (started.booleanValue() == true)
      {
         log.debug("Server '" + ServerImplMBean.OBJECT_NAME +
               "' already started, starting connectors now");         
         
         startConnectors();
      }
      else
      {
         // Register for notification of the overall server startup
         log.debug("Server '" + ServerImplMBean.OBJECT_NAME +
               "' not started, registering for start-up notification");
         
         server.addNotificationListener(ServerImplMBean.OBJECT_NAME, this, null, null);         
      }
   }


   public void stopService()
      throws Exception
   {

      String objectNameS = catalinaDomain + ":type=server";
      ObjectName objectName = new ObjectName(objectNameS);

      server.invoke(objectName, "stop", new Object[]{},
         new String[]{});

      server.invoke(objectName, "destroy", new Object[]{},
         new String[]{});

      server.unregisterMBean(objectName);

      MBeanServer server2 = server;

      // deregister with MainDeployer
      mainDeployer.removeDeployer(thisProxy);

      // Unregister any remaining jboss.web or Catalina MBeans
      ObjectName queryObjectName = new ObjectName
         (catalinaDomain + ":*");
      Iterator iterator =
         server2.queryMBeans(queryObjectName, null).iterator();
      while (iterator.hasNext())
      {
         ObjectInstance oi = (ObjectInstance) iterator.next();
         ObjectName toRemove = oi.getObjectName();
         // Exception: Don't unregister the service right now
         if (!"WebServer".equals(toRemove.getKeyProperty("service")))
         {
            if (server2.isRegistered(toRemove))
            {
               server2.unregisterMBean(toRemove);
            }
         }
      }
      queryObjectName = new ObjectName("Catalina:*");
      iterator = server2.queryMBeans(queryObjectName, null).iterator();
      while (iterator.hasNext())
      {
         ObjectInstance oi = (ObjectInstance) iterator.next();
         ObjectName name = oi.getObjectName();
         server2.unregisterMBean(name);
      }

   }

   public void startConnectors() throws Exception
   { 
      ObjectName service = new ObjectName(catalinaDomain + ":type=Service,serviceName=jboss.web");
      Object[] args = {};
      String[] sig = {};
      Connector[] connectors = (Connector[]) server.invoke(service,
         "findConnectors", args, sig); 
      for (int n = 0; n < connectors.length; n++)
      {
         Lifecycle lc = (Lifecycle) connectors[n];
         lc.start();
      }
      
      //There may be a need to start the connectors that are defined in
      //the multiple services in tomcat server.xml
      startAllConnectors();
      
      // start the sip application disptacher after the connectors have been started
      // so that serverl can act as UAC in servletInitialized callback
      ObjectName sipApplicationDispatcher = new ObjectName(catalinaDomain + ":type=SipApplicationDispatcher");      
      server.invoke(sipApplicationDispatcher,"start", args, sig);            
      
      // Notify listeners that connectors have started processing requests
      sendNotification(new Notification(TOMCAT_CONNECTORS_STARTED,
            this, getNextNotificationSequenceNumber()));
   }

   public void stopConnectors() throws Exception
   {
      ObjectName service = new ObjectName(catalinaDomain + ":type=Service,serviceName=jboss.web");
      Object[] args = {};
      String[] sig = {};
      Connector[] connectors = (Connector[]) server.invoke(service,
         "findConnectors", args, sig);
      for (int n = 0; n < connectors.length; n++)
      {
         Lifecycle lc = (Lifecycle) connectors[n];
         lc.stop();
      }
      //There may be a need to stop the connectors that are defined in
      //the multiple services in tomcat server.xml 
      stopAllConnectors();
      
      // stop the sip application disptacher 
      ObjectName sipApplicationDispatcher = new ObjectName(catalinaDomain + ":type=SipApplicationDispatcher");      
      server.invoke(sipApplicationDispatcher,"stop", args, sig);
   }

   public void handleNotification(Notification msg, Object handback)
   {
      String type = msg.getType();
      if (type.equals(Server.START_NOTIFICATION_TYPE))
      {
         log.debug("Saw " + type + " notification, starting connectors");
         try
         {
            startConnectors();
         }
         catch (Exception e)
         {
            log.warn("Failed to startConnectors", e);
         }
      }
   }

   public AbstractWebDeployer getDeployer(DeploymentInfo di) throws Exception
   {
      ClassLoader loader = di.ucl;
      Class deployerClass = loader.loadClass("org.mobicents.servlet.sip.startup.jboss.TomcatConvergedDeployer");
      AbstractWebDeployer deployer = (AbstractWebDeployer) deployerClass.newInstance();
      DeployerConfig config = new DeployerConfig();
      config.setDefaultSecurityDomain(this.defaultSecurityDomain);
      config.setSubjectAttributeName(this.subjectAttributeName);
      config.setServiceClassLoader(getClass().getClassLoader());
      config.setManagerClass(this.managerClass);
      config.setJava2ClassLoadingCompliance(this.java2ClassLoadingCompliance);
      config.setUnpackWars(this.unpackWars);
      config.setLenientEjbLink(this.lenientEjbLink);
      config.setCatalinaDomain(catalinaDomain);
      if(isSipServletApplication(di)) {
    	  config.setContextClassName(SipHostConfig.SIP_CONTEXT_CLASS);			
      }else {
    	  config.setContextClassName(contextClassName);
      }
      config.setServiceName(serviceName);
      config.setSnapshotInterval(this.snapshotInterval);
      config.setSnapshotMode(this.snapshotMode);
      config.setUseLocalCache(this.useLocalCache);
      config.setUseJK(this.useJK);
      config.setMaxUnreplicatedInterval(this.maxUnreplicatedInterval);
      config.setSubjectAttributeName(this.subjectAttributeName);
      config.setUseJBossWebLoader(this.useJBossWebLoader);
      config.setAllowSelfPrivilegedWebApps(this.allowSelfPrivilegedWebApps);
      config.setSecurityManagerService(this.secMgrService);
      config.setFilteredPackages(filteredPackages);
      deployer.setServer(server);
      deployer.init(config);
      
      return deployer;
   } 
   
   /**
    * Start all the connectors
    * 
    * @throws JMException
    * @throws LifecycleException
    */
   private void startAllConnectors() throws JMException, LifecycleException
   {
      /**
       * Not able to query the Catalina server for the services that it has
       * registered. A usuable solution is to query the MBean server for the
       * tomcat services.
       * http://www.jboss.com/index.html?module=bb&op=viewtopic&t=75353
       */
      ObjectName oname = new ObjectName("*:type=Service,*");
      Set services = server.queryMBeans(oname,null);
      Iterator iter = services.iterator();
      while(iter.hasNext())
      {
         ObjectInstance oi = (ObjectInstance)iter.next();
         ObjectName on = oi.getObjectName();  
         //Ignore jboss.web:*
         if(this.catalinaDomain.equals(on.getDomain()) )
               continue;
         String key = on.getKeyProperty("serviceName");
         if(key != null)
         {
            Connector[] connectors = (Connector[]) server.invoke(on,
                  "findConnectors", new Object[0], new String[0]); 
               for (int n = 0; n < connectors.length; n++)
               {
                  Lifecycle lc = (Lifecycle) connectors[n];
                  lc.start();
               }
         }
      }
   }
   
   /**
    * Stop all the connectors
    * 
    * @throws JMException
    * @throws LifecycleException
    */
   private void stopAllConnectors() throws JMException, LifecycleException
   { 
      ObjectName oname = new ObjectName("*:type=Service,*");
      Set services = server.queryMBeans(oname,null);
      Iterator iter = services.iterator();
      while(iter.hasNext())
      {
         ObjectInstance oi = (ObjectInstance)iter.next();
         ObjectName on = oi.getObjectName();  
         //Ignore jboss.web:*
         if(this.catalinaDomain.equals(on.getDomain()) )
               continue;
         String key = on.getKeyProperty("serviceName");
         if(key != null)
         {
            Connector[] connectors = (Connector[]) server.invoke(on,
                  "findConnectors", new Object[0], new String[0]); 
               for (int n = 0; n < connectors.length; n++)
               {
                  Lifecycle lc = (Lifecycle) connectors[n];
                  lc.stop();
               }
         }
      }
   }
   
   /***** Code added by jean.deruelle regarding the original jboss file  ********/
   
   public static final String SAR_SUFFIX = ".sar2";
   
   /** 
	 * The suffixes we accept, along with their relative order. 
	 * the accept method will return false if the sar2 doesn't contain a sip.xml file
	 */
	private static final String[] DEFAULT_ENHANCED_SUFFIXES = 
		new String[] { "600:" + SAR_SUFFIX};	
	
	/**
	 * 
	 */
	public JBossSip() {
		super();
		String[] enhancedSuffixes = getEnhancedSuffixes();
		String[] convergedEnhancedSuffixes = new String[enhancedSuffixes.length + DEFAULT_ENHANCED_SUFFIXES.length];
		System.arraycopy(enhancedSuffixes, 0, convergedEnhancedSuffixes, 0, enhancedSuffixes.length);
		System.arraycopy(DEFAULT_ENHANCED_SUFFIXES, 0, convergedEnhancedSuffixes, enhancedSuffixes.length, DEFAULT_ENHANCED_SUFFIXES.length);
		setEnhancedSuffixes(convergedEnhancedSuffixes);
	}		
   
   @Override   
   public boolean accepts(DeploymentInfo sdi) {		
		boolean accept = super.accepts(sdi);
		
		String urlPath = sdi.url.getPath();
        String shortName = sdi.shortName;
        boolean checkDir = sdi.isDirectory && !(sdi.isXML || sdi.isScript);
        //if this is a .sar2 file and it contains a sip.xml file then we accept it 
		if ((urlPath.endsWith(SAR_SUFFIX) || 
				(checkDir && shortName.endsWith(SAR_SUFFIX))
				|| urlPath.endsWith("war") || 
						(checkDir && shortName.endsWith("war")))&&
				isSipServletApplication(sdi)) {
			return true;
	    }
		
		return accept;
   }
   
   /**
	 * Check if the WEB-INF/sip.xml file can be found in the local class loader 
	 * of the service deployment info. If it is then it means that a sip servlet application
	 * is trying to be deployed
	 * @param di the service deployment info
	 * @return true if the service being deployed contains WEB-INF/sip.xml, false otherwise 
	 */
	public static boolean isSipServletApplication(DeploymentInfo di) {
		URL url = di.localCl.findResource(SipContext.APPLICATION_SIP_XML);
		if(url != null) {
			try {
				url.openStream();
				return true;
			} catch (IOException e) {
				return false;
			}		
		} else {
			File deploymentPath;
			try {
			  deploymentPath = new File(di.url.toURI());
			} catch(URISyntaxException e) {
			  deploymentPath = new File(di.url.getPath());
			}
			if(deploymentPath.isDirectory())
				return SipApplicationAnnotationUtils.findPackageInfoinDirectory(deploymentPath);
			else
				return SipApplicationAnnotationUtils.findPackageInfoInArchive(deploymentPath);
		}
	}
}
