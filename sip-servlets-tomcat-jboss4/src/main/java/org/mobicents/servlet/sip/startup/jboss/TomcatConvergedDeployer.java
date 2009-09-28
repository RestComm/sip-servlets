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

import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.OperationNotSupportedException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.metadata.WebMetaData;
import org.jboss.util.naming.NonSerializableFactory;
import org.jboss.util.naming.Util;
import org.jboss.web.AbstractWebContainer;
import org.jboss.web.WebApplication;
import org.jboss.web.tomcat.service.DeployerConfig;
import org.jboss.web.tomcat.service.TomcatDeployer;
import org.mobicents.servlet.sip.startup.SipHostConfig;
import org.mobicents.servlet.sip.startup.SipNamingContextListener;

/**
 * @author jean.deruelle
 *
 */
public class TomcatConvergedDeployer extends TomcatDeployer {
	
	private DeployerConfig config;
	protected String applicationName; 
	protected SipFactory sipFactoryFacade;
	protected TimerService timerService;
	protected SipSessionsUtil sipSessionsUtil;
	
	@Override
	public void init(Object containerConfig) throws Exception {
		super.init(containerConfig);
		this.config = (DeployerConfig) containerConfig;
	}
	
	@Override
	protected void performDeployInternal(String hostName,
		      WebApplication appInfo, String warUrl,
		      AbstractWebContainer.WebDescriptorParser webAppParser) throws Exception {
		
		super.performDeployInternal(hostName, appInfo, warUrl, webAppParser);

		if(log.isDebugEnabled()) {
			log.debug("Context class name : " + config.getContextClassName() + " for context " + appInfo.getMetaData().getContextRoot());
		}		
		if(config.getContextClassName().equals(SipHostConfig.SIP_CONTEXT_CLASS)) {
			String objectNameS = config.getCatalinaDomain()
	        + ":j2eeType=WebModule,name=//" +
	        ((hostName == null) ? "localhost" : hostName)
	        + appInfo.getMetaData().getContextRoot() + ",J2EEApplication=none,J2EEServer=none";
			
			ObjectName objectName = new ObjectName(objectNameS);						
			
			String applicationName = (String) server.invoke(objectName, "getApplicationName", new Object[]{}, new String[]{});
			SipFactory sipFactoryFacade = (SipFactory) server.invoke(objectName, "getSipFactoryFacade", new Object[]{}, new String[]{});
			TimerService timerService = (TimerService) server.invoke(objectName, "getTimerService", new Object[]{}, new String[]{});
			SipSessionsUtil sipSessionsUtil = (SipSessionsUtil) server.invoke(objectName, "getSipSessionsUtil", new Object[]{}, new String[]{});
					
			InitialContext iniCtx = new InitialContext();
			Context globalEnvCtx = (Context) iniCtx.lookup("java:/");
			Context sipSubcontext = Util.createSubcontext(globalEnvCtx,SipNamingContextListener.SIP_SUBCONTEXT);
			Context applicationNameSubcontext = Util.createSubcontext(sipSubcontext,applicationName);			
			NonSerializableFactory.rebind(applicationNameSubcontext,SipNamingContextListener.SIP_FACTORY_JNDI_NAME, sipFactoryFacade);
			NonSerializableFactory.rebind(applicationNameSubcontext, SipNamingContextListener.SIP_SESSIONS_UTIL_JNDI_NAME, sipSessionsUtil);
			NonSerializableFactory.rebind(applicationNameSubcontext,SipNamingContextListener.TIMER_SERVICE_JNDI_NAME, timerService);
			if(log.isDebugEnabled()) {
				log.debug("Sip Objects made available to global JNDI under following context : java:sip/" + applicationName + "/<ObjectName>");				
			}
			Thread currentThread = Thread.currentThread();
			ClassLoader currentLoader = currentThread.getContextClassLoader();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			ClassLoader parent = loader.getParent();
			while (parent != null) {
				parent = parent.getParent();
			}
			currentThread.setContextClassLoader(loader);
	        appInfo.getMetaData().setENCLoader(loader);	        
			Context envCtx = (Context) iniCtx.lookup("java:comp/env");
			currentThread.setContextClassLoader(currentLoader);
			sipSubcontext = Util.createSubcontext(envCtx,SipNamingContextListener.SIP_SUBCONTEXT);
			applicationNameSubcontext = Util.createSubcontext(sipSubcontext,applicationName);			
			NonSerializableFactory.rebind(applicationNameSubcontext,SipNamingContextListener.SIP_FACTORY_JNDI_NAME, sipFactoryFacade);
			NonSerializableFactory.rebind(applicationNameSubcontext, SipNamingContextListener.SIP_SESSIONS_UTIL_JNDI_NAME, sipSessionsUtil);
			NonSerializableFactory.rebind(applicationNameSubcontext,SipNamingContextListener.TIMER_SERVICE_JNDI_NAME, timerService);
			if(log.isDebugEnabled()) {
				log.debug("Sip Objects made available to global JNDI under following conetxt : java:comp/env/sip/" + applicationName + "/<ObjectName>");				
			}
		}
	}
	
	@Override
	protected void performUndeployInternal(String hostName, String warUrl,
			WebApplication appInfo) throws Exception {
		
		if(config.getContextClassName().equals(SipHostConfig.SIP_CONTEXT_CLASS)) {
			// Removing the SipFatcory, SipSessionsUtil and TimerService for the current context being undeployed 
			// from the global JNDI context for other JEE components 
			// TODO fix this to make them disappear from the private ENC and not the global JNDI context
			String objectNameS = config.getCatalinaDomain()
	        + ":j2eeType=WebModule,name=//" +
	        ((hostName == null) ? "localhost" : hostName)
	        + appInfo.getMetaData().getContextRoot() + ",J2EEApplication=none,J2EEServer=none";
			
			ObjectName objectName = new ObjectName(objectNameS);
			
			if(server.isRegistered(objectName)) {
				
				String applicationName = (String) server.invoke(objectName, "getApplicationName", new Object[]{}, new String[]{});
				
				try {
					InitialContext iniCtx = new InitialContext();
					Context applicationNameEnvCtx = (Context) iniCtx.lookup("java:/sip/" + applicationName);				
					Util.unbind(applicationNameEnvCtx,SipNamingContextListener.SIP_FACTORY_JNDI_NAME);
					Util.unbind(applicationNameEnvCtx, SipNamingContextListener.SIP_SESSIONS_UTIL_JNDI_NAME);
					Util.unbind(applicationNameEnvCtx,SipNamingContextListener.TIMER_SERVICE_JNDI_NAME);
					Context sipEnvCtx = (Context) iniCtx.lookup("java:/sip/");				
					Util.unbind(sipEnvCtx, applicationName);
				} catch (OperationNotSupportedException onse) {
					log.warn("Could not remove the JNDI context java:/sip/" + applicationName + ", cause " + onse.getMessage());
				} catch (NameNotFoundException nnfe) {
					log.warn("Could not remove the JNDI context java:/sip/" + applicationName + ", cause " + nnfe.getMessage());
				}
				
			}
		}
		
		super.performUndeployInternal(hostName, warUrl, appInfo);	
	}
}
