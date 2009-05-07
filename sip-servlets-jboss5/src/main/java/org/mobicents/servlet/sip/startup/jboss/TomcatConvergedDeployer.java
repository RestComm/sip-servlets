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
import java.util.Collection;
import java.util.List;

import javax.management.ObjectName;
import javax.servlet.sip.annotation.SipApplication;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.annotations.AnnotationEnvironment;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.logging.Logger;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.web.jboss.JBoss50WebMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.Web25MetaData;
import org.jboss.security.plugins.JaasSecurityManagerServiceMBean;
import org.jboss.sip.deployers.ConvergedSipModule;
import org.jboss.system.metadata.ServiceAttributeMetaData;
import org.jboss.system.metadata.ServiceConstructorMetaData;
import org.jboss.system.metadata.ServiceDependencyMetaData;
import org.jboss.system.metadata.ServiceInjectionValueMetaData;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.web.deployers.AbstractWarDeployer;
import org.jboss.web.deployers.AbstractWarDeployment;
import org.jboss.web.tomcat.service.deployers.DeployerConfig;
import org.jboss.web.tomcat.service.deployers.TomcatConvergedDeployment;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.SipHostConfig;

/**
 * Extends the JBoss 5 TomcatDeployer to be able to deploy sip applications and converged web/sip applications 
 * 
 * @author jean.deruelle
 *
 */
public class TomcatConvergedDeployer extends org.jboss.web.tomcat.service.deployers.TomcatDeployer {
	private static final Logger log = Logger.getLogger(TomcatConvergedDeployer.class);
	
   /**
    * Shared metaData.
    */
   private JBossWebMetaData sharedMetaData = null;
   
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
    	
	/**
    * Unmarshall factory used for parsing shared web.xml.
    */
   private static final UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
	
   public TomcatConvergedDeployer() {
		super();
   }
   
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
		
	      sharedMetaData = new JBoss50WebMetaData();
	      sharedMetaData.merge(null, confWebMD);
		
		// Parse shared sip.xml
//		URL sipXml = this.getClass().getClassLoader().getResource("sip.xml");
//		if (sipXml == null) {
//			sipXml = this.getClass().getClassLoader().getResource("conf/sip.xml");
//		}
//		sharedConvergedMetaData = new JBoss50ConvergedSipMetaData();
//		if (sipXml != null) {
//			schema = JBossXBBuilder.build(Sip11MetaData.class);
//			Sip11MetaData confSipMD = (Sip11MetaData) unmarshaller.unmarshal(sipXml
//					.toString(), schema);			
//			sharedConvergedMetaData.merge(confWebMD, confSipMD);
//		} else {
//			sharedConvergedMetaData.merge(null, confWebMD);
//		}
	}	   
   	
   	@Override
   	public void deploy(DeploymentUnit unit, JBossWebMetaData metaData)
   			throws DeploymentException {   
   		JBossConvergedSipMetaData convergedMetaData = (JBossConvergedSipMetaData) unit.getAttachment(JBossConvergedSipMetaData.class);
   		if(convergedMetaData == null) {
   			super.deploy(unit, metaData);
   		} else {
   			super.deploy(unit, convergedMetaData);
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
	public AbstractWarDeployment getDeployment(DeploymentUnit unit,
			JBossWebMetaData metaData) throws Exception {						

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
		String className = (getDeploymentClass() == null) ? "org.jboss.web.tomcat.service.deployers.TomcatDeployment"
				: getDeploymentClass();
		//if the application is a sip servlet application or converged one we use the TomcatConvergedDeployment to be able to deploy it 
		// in accordance with sip servlets spec
		if(isSipServletApplication(unit, metaData)) { 
			className = (getDeploymentClass() == null) ? "org.jboss.web.tomcat.service.deployers.TomcatConvergedDeployment"
					: getDeploymentClass();
			config.setContextClassName(SipHostConfig.SIP_CONTEXT_CLASS);			
		}else {			
			config.setContextClassName(contextClassName);
		}
		AbstractWarDeployment convergedDeployment = (AbstractWarDeployment) (getClass()
				.getClassLoader().loadClass(className)).newInstance();
		
		config.setServiceName(null);
		config.setSubjectAttributeName(this.getSubjectAttributeName());
		config.setUseJBossWebLoader(this.getUseJBossWebLoader());
		config.setAllowSelfPrivilegedWebApps(this
				.isAllowSelfPrivilegedWebApps());
		config.setSecurityManagerService(this.secMgrService);
		config.setFilteredPackages(getFilteredPackages());
		config.setSharedMetaData(sharedMetaData);
		config.setDeleteWorkDirs(getDeleteWorkDirOnContextDestroy());

		config.setSecurityContextClassName(securityContextClassName);
		convergedDeployment.setSecurityManagementName(this.securityManagement);
		convergedDeployment.setPolicyRegistrationName(this.policyRegistrationName);
		
		// Add a dependency on the webserver itself
		List<String> depends = metaData.getDepends();
		if (depends == null) {
			depends = new ArrayList<String>();
			metaData.setDepends(depends);
		}
		depends.add(TOMCAT_SERVICE_NAME.getCanonicalName());		

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
	 * of the service deployment info or if the SipApplication annotation is present. 
	 * If it is then it means that a sip servlet application is trying to be deployed
	 * 
	 * @param unit
	 *            the service deployment info
	 * @param metaData 
	 * @return true if the service being deployed contains WEB-INF/sip.xml or a SipApplication annotation,
	 *         false otherwise
	 */
	public static boolean isSipServletApplication(DeploymentUnit unit, JBossWebMetaData metaData) {
		boolean isSipApplication = false;
		
		if(metaData instanceof JBossConvergedSipMetaData) {
			//this can happen for ruby app
			JBossConvergedSipMetaData convergedSipMetaData = (JBossConvergedSipMetaData) metaData;
			if(convergedSipMetaData.getApplicationName() != null) {
				isSipApplication = true;
			}
		}
		if(!isSipApplication) {
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
		}
		
		if(log.isInfoEnabled()) {
			log.info(unit.getName() + " is a sip servlet application ? " + isSipApplication);
		}
		return isSipApplication;
	}
	
	@Override
	protected void deployWebModule(DeploymentUnit unit,
			JBossWebMetaData metaData, AbstractWarDeployment deployment)
			throws Exception {		

		log.debug("deploy Module: " + unit.getName());
		try {
			ServiceMetaData webModule = new ServiceMetaData();
			String name = getObjectName(metaData);
			ObjectName objectName = new ObjectName(name);
			webModule.setObjectName(objectName);
			//specify the correct class for converged or pure sip applications
			webModule.setCode(ConvergedSipModule.class.getName());
			// WebModule(DeploymentUnit, AbstractWarDeployer,
			// AbstractWarDeployment)
			ServiceConstructorMetaData constructor = new ServiceConstructorMetaData();
			constructor.setSignature(new String[] {
					VFSDeploymentUnit.class.getName(),
					AbstractWarDeployer.class.getName(),
					AbstractWarDeployment.class.getName() });
			constructor
					.setParameters(new Object[] { unit, this, deployment });
			webModule.setConstructor(constructor);

			List<ServiceAttributeMetaData> attrs = new ArrayList<ServiceAttributeMetaData>();

			ServiceAttributeMetaData attr = new ServiceAttributeMetaData();
			attr.setName("SecurityManagement");
			ServiceInjectionValueMetaData injectionValue = new ServiceInjectionValueMetaData(
					deployment.getSecurityManagementName());
			attr.setValue(injectionValue);
			attrs.add(attr);

			ServiceAttributeMetaData attrPR = new ServiceAttributeMetaData();
			attrPR.setName("PolicyRegistration");
			ServiceInjectionValueMetaData injectionValuePR = new ServiceInjectionValueMetaData(
					deployment.getPolicyRegistrationName());
			attrPR.setValue(injectionValuePR);
			attrs.add(attrPR);

			ServiceAttributeMetaData attrKernel = new ServiceAttributeMetaData();
			attrKernel.setName("Kernel");
			ServiceInjectionValueMetaData injectionValueKernel = new ServiceInjectionValueMetaData(
					KernelConstants.KERNEL_NAME);
			attrKernel.setValue(injectionValueKernel);
			attrs.add(attrKernel);

			webModule.setAttributes(attrs);

			// Dependencies...Still have old jmx names here
			Collection<String> depends = metaData.getDepends();
			List<ServiceDependencyMetaData> dependencies = new ArrayList<ServiceDependencyMetaData>();
			if (depends != null && depends.isEmpty() == false) {
				if (log.isTraceEnabled())
					log.trace(name + " has dependencies: " + depends);

				for (String iDependOn : depends) {
					ServiceDependencyMetaData sdmd = new ServiceDependencyMetaData();
					sdmd.setIDependOn(iDependOn);
					dependencies.add(sdmd);
				}
			}
			webModule.setDependencies(dependencies);

			// Here's where a bit of magic happens. By attaching the
			// ServiceMetaData
			// to the deployment, we now make the deployment "relevant" to
			// deployers that use ServiceMetaData as an input (e.g. the
			// org.jboss.system.deployers.ServiceDeployer). Those deployers
			// can now take over deploying the web module.

			unit.addAttachment("WarServiceMetaData", webModule,
					ServiceMetaData.class);
		} catch (Exception e) {
			throw DeploymentException.rethrowAsDeploymentException(
					"Error creating rar deployment " + unit.getName(), e);
		}		
	}
}
