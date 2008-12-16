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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.annotation.SipApplication;

import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.sip.jboss.JBoss50ConvergedSipMetaData;
import org.jboss.metadata.sip.spec.Sip11MetaData;
import org.jboss.metadata.web.jboss.JBoss50WebMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.Web25MetaData;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.web.deployers.AbstractWarDeployment;
import org.jboss.web.tomcat.service.deployers.DeployerConfig;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.SipHostConfig;

/**
 * @author jean.deruelle
 *
 */
public class TomcatConvergedDeployer extends org.jboss.web.tomcat.service.deployers.TomcatDeployer {
	private static final Logger log = Logger.getLogger(TomcatConvergedDeployer.class);
	
	JBoss50ConvergedSipMetaData sharedConvergedMetaData;
	/**
	 * Domain for tomcat6 mbeans
	 */
    private String catalinaDomain = "Catalina";
	   
    /** The web app context implementation class */
    private String contextClassName = "org.apache.catalina.core.StandardContext";
    /** The service used to flush authentication cache on session invalidation. */
    private JaasSecurityManagerServiceMBean secMgrService;
    /** The JBoss Security Manager Wrapper */
    private String securityManagement;
    /** FQN of the SecurityContext Class */
    private String securityContextClassName;
    private String policyRegistrationName;
    
//	private DeployerConfig config;
//	protected String applicationName; 
//	protected SipFactory sipFactoryFacade;
//	protected TimerService timerService;
//	protected SipSessionsUtil sipSessionsUtil;
	
	/**
    * Unmarshall factory used for parsing shared web.xml.
    */
   private static final UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
	
	/**
	 * Start the deployer. This sets up the tomcat core.
	 */
   	@Override
   	public void start() throws Exception {
		super.start();

		// Parse shared web.xml
		Unmarshaller unmarshaller = factory.newUnmarshaller();
		URL webXml = this.getClass().getClassLoader().getResource("web.xml");
		if (webXml == null) {
			webXml = this.getClass().getClassLoader().getResource("conf/web.xml");			
		}
		if (webXml == null)
			throw new IllegalStateException(
					"Unable to find shared web.xml or conf/web.xml");
		SchemaBinding schema = JBossXBBuilder.build(Web25MetaData.class);
		Web25MetaData confWebMD = (Web25MetaData) unmarshaller.unmarshal(webXml
				.toString(), schema);
		
		// Parse shared sip.xml
		URL sipXml = this.getClass().getClassLoader().getResource("sip.xml");
		if (sipXml == null) {
			sipXml = this.getClass().getClassLoader().getResource("conf/sip.xml");
		}
		sharedConvergedMetaData = new JBoss50ConvergedSipMetaData();
		if (sipXml != null) {
			schema = JBossXBBuilder.build(Sip11MetaData.class);
			Sip11MetaData confSipMD = (Sip11MetaData) unmarshaller.unmarshal(sipXml
					.toString(), schema);			
			sharedConvergedMetaData.merge(confWebMD, confSipMD);
		} else {
			sharedConvergedMetaData.merge(null, confWebMD);
		}
	}	   

   /**
	 * Create a tomcat war deployment bean for the deployment unit/metaData.
	 * 
	 * @param unit
	 *            - the current web app deployment unit
	 * @param metaData
	 *            - the parsed metdata for the web app deployment
	 * @return TomcatDeployment instnace
	 */
	@Override
	public AbstractWarDeployment getDeployment(VFSDeploymentUnit unit,
			JBossWebMetaData metaData) throws Exception {
		
		String className = (getDeploymentClass() == null) ? "org.jboss.web.tomcat.service.deployers.TomcatConvergedDeployment"
				: getDeploymentClass();
		AbstractWarDeployment convergedDeployment = (AbstractWarDeployment) (getClass()
				.getClassLoader().loadClass(className)).newInstance();

		DeployerConfig config = new DeployerConfig();
		config.setDefaultSecurityDomain(this.defaultSecurityDomain);
		config.setSubjectAttributeName(this.getSubjectAttributeName());
		config
				.setServiceClassLoader((getServiceClassLoader() == null) ? getClass()
						.getClassLoader()
						: getServiceClassLoader());
		config.setManagerClass(this.managerClass);
		config.setJava2ClassLoadingCompliance(this.java2ClassLoadingCompliance);
		config.setUnpackWars(this.unpackWars);
		config.setLenientEjbLink(this.lenientEjbLink);
		config.setCatalinaDomain(catalinaDomain);
		if(isSipServletApplication(unit)) {
			config.setContextClassName(SipHostConfig.SIP_CONTEXT_CLASS);			
		}else {
			config.setContextClassName(contextClassName);
		}
		config.setServiceName(null);
		config.setSubjectAttributeName(this.getSubjectAttributeName());
		config.setUseJBossWebLoader(this.getUseJBossWebLoader());
		config.setAllowSelfPrivilegedWebApps(this
				.isAllowSelfPrivilegedWebApps());
		config.setSecurityManagerService(this.secMgrService);
		config.setFilteredPackages(getFilteredPackages());
		config.setSharedMetaData(sharedConvergedMetaData);
		config.setDeleteWorkDirs(getDeleteWorkDirOnContextDestroy());

		config.setSecurityContextClassName(securityContextClassName);
		convergedDeployment.setSecurityManagementName(this.securityManagement);
		convergedDeployment.setPolicyRegistrationName(this.policyRegistrationName);
		
		// Add a dependency on the webserver itself
		List<String> depends = metaData.getDepends();
		if (depends == null)
			depends = new ArrayList<String>();
		depends.add(TOMCAT_SERVICE_NAME.getCanonicalName());
		metaData.setDepends(depends);

		convergedDeployment.setServer(getServer());
		convergedDeployment.init(config);

		return convergedDeployment;
	}
	
   public void setSecurityManagerService(JaasSecurityManagerServiceMBean mgr)
   {
      this.secMgrService = mgr;
   }

   public void setSecurityManagementName(String securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   public void setSecurityContextClassName(String securityContextClassName)
   {
      this.securityContextClassName = securityContextClassName;
   }
   
   public void setPolicyRegistrationName(String policyRegistration)
   {
      this.policyRegistrationName = policyRegistration;
   }

   /**
    * The most important atteribute - defines the managed domain. A catalina instance (engine) corresponds to a JMX
    * domain, that's how we know where to deploy webapps.
    * 
    * @param catalinaDomain the domain portion of the JMX ObjectNames
    */
   public void setDomain(String catalinaDomain)
   {
      this.catalinaDomain = catalinaDomain;
   }
   
   public String getDomain()
   {
      return this.catalinaDomain;
   }
   
   public void setContextMBeanCode(String className)
   {
      this.contextClassName = className;
   }
   
   public String getContextMBeanCode()
   {
      return this.contextClassName;
   }
   
   /**
	 * Check if the WEB-INF/sip.xml file can be found in the local class loader
	 * of the service deployment info. If it is then it means that a sip servlet
	 * application is trying to be deployed
	 * 
	 * @param di
	 *            the service deployment info
	 * @return true if the service being deployed contains WEB-INF/sip.xml,
	 *         false otherwise
	 */
	public static boolean isSipServletApplication(VFSDeploymentUnit unit) {
		boolean isSipApplication = false;
		URL url = unit.getResourceClassLoader().getResource(SipContext.APPLICATION_SIP_XML);
		if (url != null) {
			try {
				url.openStream();
				isSipApplication = true;
			} catch (IOException e) {
				isSipApplication= false;
			}
		} else {
			AnnotationEnvironment env = unit.getAttachment(AnnotationEnvironment.class);
			if(env != null) {
				isSipApplication = env.hasClassAnnotatedWith(SipApplication.class);
			}		    
		}
		if(log.isInfoEnabled()) {
			log.info(unit.getName() + " is a sip servlet application ? " + isSipApplication);
		}
		return isSipApplication;
	}
	
	@Override
	protected void deployWebModule(VFSDeploymentUnit unit,
			JBossWebMetaData metaData, AbstractWarDeployment deployment)
			throws Exception {	
		super.deployWebModule(unit, metaData, deployment);
	}
}
