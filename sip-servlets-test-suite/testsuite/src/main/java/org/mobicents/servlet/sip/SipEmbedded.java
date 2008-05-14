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
package org.mobicents.servlet.sip;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.loader.StandardClassLoader;
import org.apache.catalina.security.SecurityClassLoad;
import org.apache.catalina.security.SecurityConfig;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tomcat.util.IntrospectionUtils;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.router.DefaultApplicationRouter;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipHostConfig;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.SipStandardEngine;
import org.mobicents.servlet.sip.startup.SipStandardService;

/**
 * This class is emulating an embedded tomcat configured with sip servlets extension
 * to allow deployment of sip servlets apps to it. It is for the test suite purposes only for now...
 * 
 *  @author Jean Deruelle
 *  @author Vladimir Ralev
 */
public class SipEmbedded {		
	private static Log log = LogFactory.getLog(SipEmbedded.class);

	private String loggingFilePath = null;
	
	private String darConfigurationFilePath = null;
	
	private String path = null;

	private SipStandardService sipStandardService = null;

	private StandardHost host = null;

	private String sipIPAdress = "127.0.0.1";

	/**
	 * Default Constructor
	 * 
	 */
	public SipEmbedded() {		
	}

	/**
	 * Basic Accessor setting the value of the context path
	 * 
	 * @param path -
	 *            the path
	 */
	public void setPath(String path) {

		this.path = path;
	}

	/**
	 * Basic Accessor returning the value of the context path
	 * 
	 * @return - the context path
	 */
	public String getPath() {

		return path;
	}		

	/**
	 * This method Starts the Tomcat server.
	 */
	public void startTomcat() throws Exception {
		
		// Set the home directory
		System.setProperty("CATALINA_HOME", getPath());
		System.setProperty("CATALINA_BASE", getPath());
		System.setProperty("catalina.home", getPath());
		System.setProperty("catalina.base", getPath());		
		//logging configuration
		System.setProperty("java.util.logging.config.file", loggingFilePath + "logging.properties");
		DOMConfigurator.configure(loggingFilePath + "log4j.xml");		
//		BasicConfigurator.configure();
//		PropertyConfigurator.configure(loggingFilePath);		
		//Those are for trying to make it work under mvn test command
		// don't know why but some jars aren't loaded
		setSecurityProtection();
		initDirs();
		initNaming();
		initClassLoaders();
		
		Thread.currentThread().setContextClassLoader(catalinaLoader);
        SecurityClassLoad.securityClassLoad(catalinaLoader);
        
		/*
		 * <Service className="org.mobicents.servlet.sip.startup.SipStandardService"
		 * darConfigurationFileLocation="file:///E:/sip-serv/sip-servlets-impl/docs/dar.properties"
		 * name="Catalina"
		 * sipApplicationDispatcherClassName="org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl"
		 * sipApplicationRouterClassName="org.mobicents.servlet.sip.router.DefaultApplicationRouter">
		 */		
		// Create an embedded server		
		sipStandardService = new SipStandardService();
		sipStandardService.setName("Catalina");		
		sipStandardService.setSipApplicationDispatcherClassName(SipApplicationDispatcherImpl.class.getName());
		sipStandardService.setSipApplicationRouterClassName(DefaultApplicationRouter.class.getName());		
		sipStandardService.setDarConfigurationFileLocation(darConfigurationFilePath);
		// Create an engine		
		Engine engine = new SipStandardEngine();
		engine.setName("Catalina");
		engine.setDefaultHost("localhost");
		engine.setService(sipStandardService);
		// Install the assembled container hierarchy
		sipStandardService.setContainer(engine);
		sipStandardService.init();
		// Create a default virtual host
//		host = (StandardHost) embedded.createHost("localhost", getPath() + "/webapps");
		host = new StandardHost();
        host.setAppBase(getPath() + "/webapps");
        host.setName("localhost");
		host.setConfigClass(StandardContext.class.getName());		
		host.setAppBase("webapps");
		host.addLifecycleListener(new SipHostConfig());
		host.setAutoDeploy(false);
		host.setDeployOnStartup(false);
		engine.addChild(host);		

		/*
		 * <Connector debugLog="../logs/debuglog.txt" ipAddress="0.0.0.0"
		 * logLevel="DEBUG" port="5070"
		 * protocol="org.mobicents.servlet.sip.startup.SipProtocolHandler"
		 * serverLog="../logs/serverlog.txt" signalingTransport="udp"
		 * sipPathName="gov.nist" sipStackName="SIP-Servlet-Tomcat-Server"/>
		 */
		Connector udpSipConnector = new Connector(
				SipProtocolHandler.class.getName());
		SipProtocolHandler udpProtocolHandler = (SipProtocolHandler) udpSipConnector
				.getProtocolHandler();
		udpProtocolHandler.setPort(5070);
		udpProtocolHandler.setDebugLog("../logs/debuglog.txt");
		udpProtocolHandler.setIpAddress(sipIPAdress);
		udpProtocolHandler.setLogLevel("DEBUG");
		udpProtocolHandler.setServerLog("../logs/serverlog.xml");
		udpProtocolHandler.setSignalingTransport("udp");
		udpProtocolHandler.setSipPathName("gov.nist");
		udpProtocolHandler.setSipStackName("SIP-Servlet-Tomcat-Server");
		udpProtocolHandler.setThreadPoolSize("64");
		udpProtocolHandler.setIsReentrantListener("true");

		sipStandardService.addConnector(udpSipConnector);
		//Filip Olsson : Issue 112, Adding tcp protocol
		Connector tcpSipConnector = new Connector(
			SipProtocolHandler.class.getName());
		SipProtocolHandler tcpProtocolHandler = (SipProtocolHandler) tcpSipConnector
			.getProtocolHandler();
		tcpProtocolHandler.setPort(5070);
		tcpProtocolHandler.setDebugLog("../logs/debuglog.txt");
		tcpProtocolHandler.setIpAddress(sipIPAdress);
		tcpProtocolHandler.setLogLevel("DEBUG");
		tcpProtocolHandler.setServerLog("../logs/serverlog.xml");
		tcpProtocolHandler.setSignalingTransport("tcp");
		tcpProtocolHandler.setSipPathName("gov.nist");
		tcpProtocolHandler.setSipStackName("SIP-Servlet-Tomcat-Server");
		tcpProtocolHandler.setThreadPoolSize("64");
		tcpProtocolHandler.setIsReentrantListener("true");

		sipStandardService.addConnector(tcpSipConnector);
		
		//HTTP connector
		Connector httpConnector = new Connector(
				Http11Protocol.class.getName());
		Http11Protocol httpProtocolHandler = (Http11Protocol) httpConnector
				.getProtocolHandler();		
		httpProtocolHandler.setPort(8080);		
		httpProtocolHandler.setDisableUploadTimeout(true);
		httpProtocolHandler.setMaxHttpHeaderSize(8192);
//		httpProtocolHandler.setMaxSpareThreads(75);
//		httpProtocolHandler.setMinSpareThreads(75);
		httpProtocolHandler.setMaxThreads(150);		

		sipStandardService.addConnector(httpConnector);
		// Start the embedded server
		sipStandardService.start();				
	}

	/**
	 * This method Stops the Tomcat server.
	 */
	public void stopTomcat() throws Exception {
	 		
		// Stop the embedded server
		sipStandardService.stop();
	}

	/**
	 * Deploy a context to the embedded tomcat container
	 * @param contextPath the context Path of the context to deploy
	 */
	public boolean deployContext(String docBase, String name, String path) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(docBase);
		context.setName(name);
		context.setPath(path);
		context.setParent(host);
		context.addLifecycleListener(new SipContextConfig());
		context.setManager(new SipStandardManager());
		host.addChild(context);
		return context.getAvailable();			
	}
	
	public boolean deployContext(SipStandardContext context) {
		context.setParent(host);
		host.addChild(context);
		return context.getAvailable();	
	}
	
	public void undeployContext(Container context) {
		host.removeChild(context);
	}

	public String getDarConfigurationFilePath() {
		return darConfigurationFilePath;
	}

	public void setDarConfigurationFilePath(String darConfigurationFilePath) {
		this.darConfigurationFilePath = darConfigurationFilePath;
	}

	public String getLoggingFilePath() {
		return loggingFilePath;
	}

	public void setLoggingFilePath(String loggingFilePath) {
		this.loggingFilePath = loggingFilePath;
	}
	
	
	/**
     * Is naming enabled ?
     */
    protected boolean useNaming = true;
    protected static final String CATALINA_HOME_TOKEN = "${catalina.home}";
    protected static final String CATALINA_BASE_TOKEN = "${catalina.base}";

    protected static final Integer IS_DIR = new Integer(0);
    protected static final Integer IS_JAR = new Integer(1);
    protected static final Integer IS_GLOB = new Integer(2);
    protected static final Integer IS_URL = new Integer(3);
    
    protected ClassLoader commonLoader = null;
    protected ClassLoader catalinaLoader = null;
    protected ClassLoader sharedLoader = null;
	
	private void initClassLoaders() throws Exception {
        
            commonLoader = createClassLoader("common", null);
            if( commonLoader == null ) {
                // no config file, default to this loader - we might be in a 'single' env.
                commonLoader=this.getClass().getClassLoader();
            }
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);        
    }


    private ClassLoader createClassLoader(String name, ClassLoader parent)
        throws Exception {

        String value = CatalinaProperties.getProperty(name + ".loader");
        if ((value == null) || (value.equals("")))
            return parent;

        ArrayList repositoryLocations = new ArrayList();
        ArrayList repositoryTypes = new ArrayList();
        int i;
 
        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        while (tokenizer.hasMoreElements()) {
            String repository = tokenizer.nextToken();

            // Local repository
            boolean replace = false;
            String before = repository;
            while ((i=repository.indexOf(CATALINA_HOME_TOKEN))>=0) {
                replace=true;
                if (i>0) {
                repository = repository.substring(0,i) + getCatalinaHome() 
                    + repository.substring(i+CATALINA_HOME_TOKEN.length());
                } else {
                    repository = getCatalinaHome() 
                        + repository.substring(CATALINA_HOME_TOKEN.length());
                }
            }
            while ((i=repository.indexOf(CATALINA_BASE_TOKEN))>=0) {
                replace=true;
                if (i>0) {
                repository = repository.substring(0,i) + getCatalinaBase() 
                    + repository.substring(i+CATALINA_BASE_TOKEN.length());
                } else {
                    repository = getCatalinaBase() 
                        + repository.substring(CATALINA_BASE_TOKEN.length());
                }
            }
            if (replace && log.isDebugEnabled())
                log.debug("Expanded " + before + " to " + replace);

            // Check for a JAR URL repository
            try {
                URL url=new URL(repository);
                repositoryLocations.add(repository);
                repositoryTypes.add(IS_URL);
                continue;
            } catch (MalformedURLException e) {
                // Ignore
            }

            if (repository.endsWith("*.jar")) {
                repository = repository.substring
                    (0, repository.length() - "*.jar".length());
                repositoryLocations.add(repository);
                repositoryTypes.add(IS_GLOB);
            } else if (repository.endsWith(".jar")) {
                repositoryLocations.add(repository);
                repositoryTypes.add(IS_JAR);
            } else {
                repositoryLocations.add(repository);
                repositoryTypes.add(IS_DIR);
            }
        }

        String[] locations = (String[]) repositoryLocations.toArray(new String[0]);
        Integer[] types = (Integer[]) repositoryTypes.toArray(new Integer[0]);
 
        ClassLoader classLoader = createClassLoader
            (locations, types, parent);

        // Retrieving MBean server
        MBeanServer mBeanServer = null;
        if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
            mBeanServer =
                (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
        } else {
            mBeanServer = MBeanServerFactory.createMBeanServer();
        }

        // Register the server classloader
        ObjectName objectName =
            new ObjectName("Catalina:type=ServerClassLoader,name=" + name);
        if(!mBeanServer.isRegistered(objectName))
        	mBeanServer.registerMBean(classLoader, objectName);

        return classLoader;

    }

    /**
     * Get the value of the catalina.home environment variable.
     */
    public static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
    public static String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }
	
    
    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param locations Array of strings containing class directories, jar files,
     *  jar directories or URLS that should be added to the repositories of
     *  the class loader. The type is given by the member of param types.
     * @param types Array of types for the members of param locations.
     *  Possible values are IS_DIR (class directory), IS_JAR (single jar file),
     *  IS_GLOB (directory of jar files) and IS_URL (URL).
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(String locations[],
                                                Integer types[],
                                                ClassLoader parent)
        throws Exception {

        if (log.isDebugEnabled())
            log.debug("Creating new class loader");

        // Construct the "class path" for this class loader
        ArrayList list = new ArrayList();

        if (locations != null && types != null && locations.length == types.length) {
            for (int i = 0; i < locations.length; i++)  {
                String location = locations[i];
                if ( types[i] == IS_URL ) {
                    URL url = new URL(location);
                    if (log.isDebugEnabled())
                        log.debug("  Including URL " + url);
                    list.add(url);
                } else if ( types[i] == IS_DIR ) {
                    File directory = new File(location);
                    directory = new File(directory.getCanonicalPath());
                    if (!directory.exists() || !directory.isDirectory() ||
                        !directory.canRead())
                         continue;
                    URL url = directory.toURL();
                    if (log.isDebugEnabled())
                        log.debug("  Including directory " + url);
                    list.add(url);
                } else if ( types[i] == IS_JAR ) {
                    File file=new File(location);
                    file = new File(file.getCanonicalPath());
                    if (!file.exists() || !file.canRead())
                        continue;
                    URL url = file.toURL();
                    if (log.isDebugEnabled())
                        log.debug("  Including jar file " + url);
                    list.add(url);
                } else if ( types[i] == IS_GLOB ) {
                    File directory=new File(location);
                    if (!directory.exists() || !directory.isDirectory() ||
                        !directory.canRead())
                        continue;
                    if (log.isDebugEnabled())
                        log.debug("  Including directory glob "
                            + directory.getAbsolutePath());
                    String filenames[] = directory.list();
                    for (int j = 0; j < filenames.length; j++) {
                        String filename = filenames[j].toLowerCase();
                        if (!filename.endsWith(".jar"))
                            continue;
                        File file = new File(directory, filenames[j]);
                        file = new File(file.getCanonicalPath());
                        if (!file.exists() || !file.canRead())
                            continue;
                        if (log.isDebugEnabled())
                            log.debug("    Including glob jar file "
                                + file.getAbsolutePath());
                        URL url = file.toURL();
                        list.add(url);
                    }
                }
            }
        }

        // Construct the class loader itself
        URL[] array = (URL[]) list.toArray(new URL[list.size()]);
        if (log.isDebugEnabled())
            for (int i = 0; i < array.length; i++) {
                log.debug("  location " + i + " is " + array[i]);
            }
        StandardClassLoader classLoader = null;
        if (parent == null)
            classLoader = new StandardClassLoader(array);
        else
            classLoader = new StandardClassLoader(array, parent);
        return (classLoader);

    }

    /**
     * Set the security package access/protection.
     */
    protected void setSecurityProtection(){
        SecurityConfig securityConfig = SecurityConfig.newInstance();
        securityConfig.setPackageDefinition();
        securityConfig.setPackageAccess();
    }
	
	/** Initialize naming - this should only enable java:env and root naming.
     * If tomcat is embeded in an application that already defines those -
     * it shouldn't do it.
     *
     * XXX The 2 should be separated, you may want to enable java: but not
     * the initial context and the reverse
     * XXX Can we "guess" - i.e. lookup java: and if something is returned assume
     * false ?
     * XXX We have a major problem with the current setting for java: url
     */
    protected void initNaming() {
        // Setting additional variables
        if (!useNaming) {
            log.info( "Catalina naming disabled");
            System.setProperty("catalina.useNaming", "false");
        } else {
            System.setProperty("catalina.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue =
                System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = value + ":" + oldValue;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            if( log.isDebugEnabled() )
                log.debug("Setting naming prefix=" + value);
            value = System.getProperty
                (javax.naming.Context.INITIAL_CONTEXT_FACTORY);
            if (value == null) {
                System.setProperty
                    (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                     "org.apache.naming.java.javaURLContextFactory");
            } else {
                log.debug( "INITIAL_CONTEXT_FACTORY alread set " + value );
            }
        }
    }


    protected void initDirs() {

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome == null) {
            // Backwards compatibility patch for J2EE RI 1.3
            String j2eeHome = System.getProperty("com.sun.enterprise.home");
            if (j2eeHome != null) {
                catalinaHome=System.getProperty("com.sun.enterprise.home");
            } else if (System.getProperty("catalina.base") != null) {
                catalinaHome = System.getProperty("catalina.base");
            } else {
                // Use IntrospectionUtils and guess the dir
                catalinaHome = IntrospectionUtils.guessInstall
                    ("catalina.home", "catalina.base", "catalina.jar");
                if (catalinaHome == null) {
                    catalinaHome = IntrospectionUtils.guessInstall
                        ("tomcat.install", "catalina.home", "tomcat.jar");
                }
            }
        }
        // last resort - for minimal/embedded cases. 
        if(catalinaHome==null) {
            catalinaHome=System.getProperty("user.dir");
        }
        if (catalinaHome != null) {
            File home = new File(catalinaHome);
            if (!home.isAbsolute()) {
                try {
                    catalinaHome = home.getCanonicalPath();
                } catch (IOException e) {
                    catalinaHome = home.getAbsolutePath();
                }
            }
            System.setProperty("catalina.home", catalinaHome);
        }

        if (System.getProperty("catalina.base") == null) {
            System.setProperty("catalina.base",
                               catalinaHome);
        } else {
            String catalinaBase = System.getProperty("catalina.base");
            File base = new File(catalinaBase);
            if (!base.isAbsolute()) {
                try {
                    catalinaBase = base.getCanonicalPath();
                } catch (IOException e) {
                    catalinaBase = base.getAbsolutePath();
                }
            }
            System.setProperty("catalina.base", catalinaBase);
        }
        
        String temp = System.getProperty("java.io.tmpdir");
        if (temp == null || (!(new File(temp)).exists())
                || (!(new File(temp)).isDirectory())) {
            log.error("no temp directory"+ temp);
        }

    }

	public String getSipIPAdress() {
		return sipIPAdress;
	}

	public void setSipIPAdress(String sipIPAdress) {
		this.sipIPAdress = sipIPAdress;
	}
}
