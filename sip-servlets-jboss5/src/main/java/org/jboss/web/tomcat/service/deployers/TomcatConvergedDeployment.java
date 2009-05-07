/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
package org.jboss.web.tomcat.service.deployers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Iterator;
import java.util.zip.ZipFile;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.Loader;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.security.SecurityUtil;
import org.jboss.virtual.VirtualFile;
import org.jboss.web.WebApplication;
import org.jboss.web.tomcat.security.JaccContextValve;
import org.jboss.web.tomcat.security.RunAsListener;
import org.jboss.web.tomcat.security.SecurityAssociationValve;
import org.jboss.web.tomcat.security.SecurityContextEstablishmentValve;
import org.jboss.web.tomcat.service.TomcatConvergedSipInjectionContainer;
import org.jboss.web.tomcat.service.TomcatInjectionContainer;
import org.jboss.web.tomcat.service.session.AbstractJBossManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.ClusteringNotSupportedException;
import org.mobicents.servlet.sip.message.SipFactoryFacade;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig;

/**
 * A tomcat converged sip application deployment that will be able to deploy web applications, sip applications and converged sip/web applications.
 * 
 * It extends the TomcatDeployment JBoss 5 class so that the config class for the context becomes org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig
 * and that a ConvergedEncListener is set to the context to inject SipFactory, TimerService and SipSessionUtils into the private jndi of the context.
 * 
 * @author jean.deruelle@gmail.com
 * 
 */
public class TomcatConvergedDeployment extends TomcatDeployment {
	private static final Logger log = Logger
			.getLogger(TomcatConvergedDeployment.class);

	/**
	 * The name of the war level context configuration descriptor
	 */
	private static final String CONTEXT_CONFIG_FILE = "WEB-INF/context.xml";

	public static final String SIP_SUBCONTEXT = "sip";
	public static final String SIP_FACTORY_JNDI_NAME = "SipFactory";
	public static final String SIP_SESSIONS_UTIL_JNDI_NAME = "SipSessionsUtil";
	public static final String TIMER_SERVICE_JNDI_NAME = "TimerService";	
	
	protected DeployerConfig config;

	private final String[] javaVMs = { " jboss.management.local:J2EEServer=Local,j2eeType=JVM,name=localhost" };

	private final String serverName = "jboss";
	
	@Override
	public void init(Object containerConfig) throws Exception {
		super.init(containerConfig);
		this.config = (DeployerConfig) containerConfig;
	}

	@Override
	protected void performDeployInternal(
			WebApplication webApp, String hostName, String warUrlStr) throws Exception {

		JBossWebMetaData metaData = webApp.getMetaData();
		String ctxPath = metaData.getContextRoot();
		if (ctxPath.equals("/") || ctxPath.equals("/ROOT")
				|| ctxPath.equals("")) {
			log.debug("deploy root context=" + ctxPath);
			ctxPath = "/";
			metaData.setContextRoot(ctxPath);
		}

		log.info("deploy, ctxPath=" + ctxPath);

		URL warUrl = new URL(warUrlStr);

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		metaData.setContextLoader(loader);

		StandardContext context = (StandardContext) Class.forName(
				config.getContextClassName()).newInstance();

		DeploymentUnit depUnit = webApp.getDeploymentUnit();
		TomcatConvergedSipInjectionContainer injectionContainer = new TomcatConvergedSipInjectionContainer(
				webApp, depUnit, context,
				getPersistenceUnitDependencyResolver());		

		Loader webLoader = depUnit.getAttachment(
				Loader.class);
		if (webLoader == null)
			webLoader = getWebLoader(depUnit, metaData,
					loader, warUrl, injectionContainer);

		webApp.setName(warUrl.getPath());
		webApp.setClassLoader(loader);
		webApp.setURL(warUrl);

		String objectNameS = config.getCatalinaDomain()
				+ ":j2eeType=WebModule,name=//"
				+ ((hostName == null) ? "localhost" : hostName) + ctxPath
				+ ",J2EEApplication=none,J2EEServer=none";

		ObjectName objectName = new ObjectName(objectNameS);

		if (Registry.getRegistry(null, null).getMBeanServer().isRegistered(
				objectName))
			throw new DeploymentException(
					"Web mapping already exists for deployment URL " + warUrlStr);

		Registry.getRegistry(null, null).registerComponent(context, objectName,
				config.getContextClassName());

		context.setConfigFile(CONTEXT_CONFIG_FILE);
		context.setInstanceManager(injectionContainer);
		context.setDefaultContextXml("context.xml");
		context.setDefaultWebXml("conf/web.xml");
		context.setPublicId(metaData.getPublicID());
		
		String docBase = depUnit.getAttachment("org.jboss.web.explicitDocBase", String.class);
	    if (docBase == null)
	    	docBase = warUrl.getFile();

	    context.setDocBase(docBase);
	      
		// If there is an alt-dd set it
		if (metaData.getAlternativeDD() != null) {
			log.debug("Setting altDDName to: " + metaData.getAlternativeDD());
			context.setAltDDName(metaData.getAlternativeDD());
		}
		context.setJavaVMs(javaVMs);
		context.setServer(serverName);
		context.setSaveConfig(false);

		if (webLoader != null) {
			context.setLoader(webLoader);
		} else {
			context.setParentClassLoader(loader);
		}
		context.setDelegate(webApp.getJava2ClassLoadingCompliance());

		// Javac compatibility whenever possible
		String[] jspCP = getCompileClasspath(loader);
		StringBuffer classpath = new StringBuffer();
		for (int u = 0; u < jspCP.length; u++) {
			String repository = jspCP[u];
			if (repository == null)
				continue;
			if (repository.startsWith("file://"))
				repository = repository.substring(7);
			else if (repository.startsWith("file:"))
				repository = repository.substring(5);
			else
				continue;
			if (repository == null)
				continue;
			// ok it is a file. Make sure that is is a directory or jar file
			File fp = new File(repository);
			if (!fp.isDirectory()) {
				// if it is not a directory, try to open it as a zipfile.
				try {
					// avoid opening .xml files
					if (fp.getName().toLowerCase().endsWith(".xml"))
						continue;

					ZipFile zip = new ZipFile(fp);
					zip.close();
				} catch (IOException e) {
					continue;
				}

			}
			if (u > 0)
				classpath.append(File.pathSeparator);
			classpath.append(repository);
		}
		context.setCompilerClasspath(classpath.toString());

		// Set the session cookies flag according to metadata
		switch (metaData.getSessionCookies()) {
		case JBossWebMetaData.SESSION_COOKIES_ENABLED:
			context.setCookies(true);
			log.debug("Enabling session cookies");
			break;
		case JBossWebMetaData.SESSION_COOKIES_DISABLED:
			context.setCookies(false);
			log.debug("Disabling session cookies");
			break;
		default:
			log.debug("Using session cookies default setting");
		}

		String metaDataSecurityDomain = metaData.getSecurityDomain();
		if (metaDataSecurityDomain != null)
			metaDataSecurityDomain = metaDataSecurityDomain.trim();

		// TODO : add the security valve again with SecurityActions enabled. It was commented due to IllegalAccessError, it is a regression from regular JBoss 5
		// Add a valve to establish security context
//		SecurityContextEstablishmentValve scevalve = new SecurityContextEstablishmentValve(
//				metaDataSecurityDomain, SecurityUtil
//						.unprefixSecurityDomain(config
//								.getDefaultSecurityDomain()), SecurityActions
//						.loadClass(config.getSecurityContextClassName()),
//				getSecurityManagement());
		SecurityContextEstablishmentValve scevalve = new SecurityContextEstablishmentValve(
				metaDataSecurityDomain, SecurityUtil
						.unprefixSecurityDomain(config
								.getDefaultSecurityDomain()), Class.forName(config.getSecurityContextClassName()),
				getSecurityManagement());
		context.addValve(scevalve);

		// Add a valve to estalish the JACC context before authorization valves
		Certificate[] certs = null;
		CodeSource cs = new CodeSource(warUrl, certs);
		JaccContextValve jaccValve = new JaccContextValve(metaData, cs);
		context.addValve(jaccValve);		
		
		// Set listener
		context.setConfigClass("org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig");
//		context.setConfigClass("org.jboss.web.tomcat.service.deployers.JBossContextConfig");
		context.addLifecycleListener(new ConvergedEncListener(hostName, loader, webLoader, injectionContainer, webApp));

		// Pass the metadata to the RunAsListener via a thread local
		RunAsListener.metaDataLocal.set(metaData);
		SipJBossContextConfig.metaDataLocal.set(metaData);
		SipJBossContextConfig.metaDataShared.set(config.getSharedMetaData());
		SipJBossContextConfig.deployerConfig.set(config);

		SipJBossContextConfig.kernelLocal.set(kernel);
		SipJBossContextConfig.deploymentUnitLocal.set(unit);
		try {
			// Start it
			context.start();			
			// Build the ENC
			injectSipUtilitiesIntoEJBs(context, metaData);
		} catch (Exception e) {
			context.destroy();
			DeploymentException.rethrowAsDeploymentException("URL " + warUrlStr
					+ " deployment failed", e);
		} finally {
			RunAsListener.metaDataLocal.set(null);
			SipJBossContextConfig.metaDataLocal.set(null);
			SipJBossContextConfig.metaDataShared.set(null);
			SipJBossContextConfig.deployerConfig.set(null);

			SipJBossContextConfig.kernelLocal.set(null);
			SipJBossContextConfig.deploymentUnitLocal.set(null);
		}
		if (context.getState() != 1) {
			context.destroy();
			throw new DeploymentException("URL " + warUrlStr
					+ " deployment failed");
		}

		// Clustering
		if (metaData.getDistributable() != null) {
			// Try to initate clustering, fallback to standard if no clustering
			// is
			// available
			try {
				AbstractJBossManager manager = null;
				String managerClassName = config.getManagerClass();
				Class managerClass = Thread.currentThread()
						.getContextClassLoader().loadClass(managerClassName);
				manager = (AbstractJBossManager) managerClass.newInstance();
				String name = "//"
						+ ((hostName == null) ? "localhost" : hostName)
						+ ctxPath;
				manager.init(name, metaData);

				server.setAttribute(objectName, new Attribute("manager",
						manager));

				log.debug("Enabled clustering support for ctxPath=" + ctxPath);
			} catch (ClusteringNotSupportedException e) {
				// JBAS-3513 Just log a WARN, not an ERROR
				log
						.warn("Failed to setup clustering, clustering disabled. ClusteringNotSupportedException: "
								+ e.getMessage());
			} catch (NoClassDefFoundError ncdf) {
				// JBAS-3513 Just log a WARN, not an ERROR
				log.debug("Classes needed for clustered webapp unavailable",
						ncdf);
				log
						.warn("Failed to setup clustering, clustering disabled. NoClassDefFoundError: "
								+ ncdf.getMessage());
			} catch (Throwable t) {
				// TODO consider letting this through and fail the deployment
				log
						.error(
								"Failed to setup clustering, clustering disabled. Exception: ",
								t);
			}
		}

		/*
		 * Add security association valve after the authorization valves so that
		 * the authenticated user may be associated with the request
		 * thread/session.
		 */
		SecurityAssociationValve valve = new SecurityAssociationValve(metaData,
				config.getSecurityManagerService());
		valve.setSubjectAttributeName(config.getSubjectAttributeName());
		server.invoke(objectName, "addValve", new Object[] { valve },
				new String[] { "org.apache.catalina.Valve" });

		/*
		 * TODO: Retrieve the state, and throw an exception in case of a failure
		 * Integer state = (Integer) server.getAttribute(objectName, "state");
		 * if (state.intValue() != 1) { throw new DeploymentException("URL " +
		 * warUrl + " deployment failed"); }
		 */

		webApp.setAppData(objectName);

		/*
		 * TODO: Create mbeans for the servlets ObjectName servletQuery = new
		 * ObjectName (config.getCatalinaDomain() +
		 * ":j2eeType=Servlet,WebModule=" + objectName.getKeyProperty("name") +
		 * ",*"); Iterator iterator = server.queryMBeans(servletQuery,
		 * null).iterator(); while (iterator.hasNext()) {
		 * di.mbeans.add(((ObjectInstance)iterator.next()).getObjectName()); }
		 */

		log.debug("Initialized: " + webApp + " " + objectName);
	}

	/**
	 * If we are deploying a sip servlet application, 
	 * check the deployment unit in which we are to check if there is any EJB JAR present. 
	 * For each EJB Container present in the EJB-JAR, get the private JNDI context (comp/env),
	 * create the sip servlets JNDI Tree (sip/<appname>/SipFactory & sip/'appname'/TimerService & sip/'appname'/SipSessionsUtil)  
	 * and inject the SIP utilities (TimerService, SipFactory and SipSessionsUtil).
	 * @param context the sip context from which the Sip Utilities will be fecthed
	 * @param metaData the meta Data from which we get the application Name to create the JNDI tree
	 */
	private void injectSipUtilitiesIntoEJBs(StandardContext context, JBossWebMetaData metaData) {
		if(context instanceof SipContext && metaData instanceof JBossConvergedSipMetaData) {				
			JBossConvergedSipMetaData convergedMetaData = (JBossConvergedSipMetaData) metaData;
			SipContext sipContext = (SipContext) context;
			
			DeploymentUnit parent = unit.getTopLevel();
			Iterator<DeploymentUnit> it = parent.getChildren()
					.iterator();
			while (it.hasNext()) {
				DeploymentUnit deploymentUnit = (DeploymentUnit) it.next();
				Ejb3Deployment ejbDeployment = (Ejb3Deployment) deploymentUnit
						.getAttachment(Ejb3Deployment.class);
				if(ejbDeployment != null) {
					if(log.isInfoEnabled()) {
						log.info("Ejb Jar in which to inject SipUtilities " + deploymentUnit.getName());
					}
					for(Object container : ejbDeployment.getEjbContainers().values()) {
						EJBContainer ejbContainer = (EJBContainer) container;
						if(log.isDebugEnabled()) {
							log.debug("Ejb Container in which to inject SipUtilities " + ejbContainer);
						}
						try {
							Context envCtx = (Context)ejbContainer.getEnc().lookup("env");
							Context sipSubcontext = envCtx.createSubcontext(SIP_SUBCONTEXT);
							Context applicationNameSubcontext = sipSubcontext.createSubcontext(convergedMetaData.getApplicationName());						
							
							SipFactoryFacade sipFactoryFacade = (SipFactoryFacade) sipContext.getSipFactoryFacade();
							TimerService timerService = (TimerService) sipContext.getTimerService();
							SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) sipContext.getSipSessionsUtil();
							
							NonSerializableFactory.rebind(
										applicationNameSubcontext,
										SIP_FACTORY_JNDI_NAME,
										sipFactoryFacade);
							 
							NonSerializableFactory
									.rebind(
											applicationNameSubcontext,
											SIP_SESSIONS_UTIL_JNDI_NAME,
											sipSessionsUtil);
							NonSerializableFactory
									.rebind(
											applicationNameSubcontext,
											TIMER_SERVICE_JNDI_NAME,
											timerService);
							if (log.isDebugEnabled()) {
								log
										.debug("Sip Objects made available to global JNDI under following conetxt : java:comp/env/sip/"
												+ convergedMetaData.getApplicationName() + "/<ObjectName>");
							}
						} catch (NamingException e) {
							log.error("Unexpected exception while trying to inject Sip Utilities into following EJB Container : " + ejbContainer, e);
						}
					}
				}
			}
		}
	}

	public class ConvergedEncListener extends EncListener
	{
		protected String hostName;
		
		public ConvergedEncListener(String hostName, ClassLoader loader, Loader webLoader,TomcatInjectionContainer tomcatInjectionContainer, WebApplication webApp) {
			super(loader, webLoader, tomcatInjectionContainer, webApp);
			this.hostName = hostName;
		}
		
		public void lifecycleEvent(LifecycleEvent event) {
			super.lifecycleEvent(event);
			if (event.getType().equals(StandardContext.AFTER_START_EVENT)) {
				JBossConvergedSipMetaData convergedMetaData = (JBossConvergedSipMetaData) metaData ;
				Thread currentThread = Thread.currentThread();
	            ClassLoader currentLoader = currentThread.getContextClassLoader();
				currentThread.setContextClassLoader(webLoader.getClassLoader());
				try {
					InitialContext iniCtx = new InitialContext();
		            Context envCtx = (Context) iniCtx.lookup("java:comp/env");		            																
					Context sipSubcontext = envCtx.createSubcontext(SIP_SUBCONTEXT);
					Context applicationNameSubcontext = sipSubcontext.createSubcontext(convergedMetaData.getApplicationName());						
					
					if(event.getSource() instanceof SipContext) {
						SipContext sipContext = (SipContext) event.getSource();
						SipFactoryFacade sipFactoryFacade = (SipFactoryFacade) sipContext.getSipFactoryFacade();
						TimerService timerService = (TimerService) sipContext.getTimerService();
						SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) sipContext.getSipSessionsUtil();
						
						NonSerializableFactory.rebind(
									applicationNameSubcontext,
									SIP_FACTORY_JNDI_NAME,
									sipFactoryFacade);
						 
						NonSerializableFactory
								.rebind(
										applicationNameSubcontext,
										SIP_SESSIONS_UTIL_JNDI_NAME,
										sipSessionsUtil);
						NonSerializableFactory
								.rebind(
										applicationNameSubcontext,
										TIMER_SERVICE_JNDI_NAME,
										timerService);
						if (log.isDebugEnabled()) {
							log
									.debug("Sip Objects made available to global JNDI under following conetxt : java:comp/env/sip/"
											+ convergedMetaData.getApplicationName() + "/<ObjectName>");
						}
					}
	            }
	            catch (Throwable t) {
	               log.error("ENC setup failed", t);
	               throw new RuntimeException(t);
	            }
	            finally {
	               currentThread.setContextClassLoader(currentLoader);		               		              
	            }		            
			}
		}
	}
}
