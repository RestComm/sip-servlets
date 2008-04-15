/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.startup.jboss;

import java.io.IOException;
import java.net.URL;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.web.AbstractWebDeployer;
import org.jboss.web.tomcat.service.JBossWeb;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.SipHostConfig;

/**
 * Extending the JBossWeb implementation of AbstractWebContainer to be able to deploy
 * sip servlets archive. 
 * This class will check if the WEB-INF/sip.xml file can be found in the application
 * getting deployed, if that's the case it will use sip extended tomcat classes
 * for the application context 
 * 
 * @author Jean Deruelle
 *
 */
public class JBossConverged extends JBossWeb {						
		
	@Override
	public AbstractWebDeployer getDeployer(DeploymentInfo di) throws Exception {
		if(isSipServletApplication(di)) {
			String initialContextMBeanCode = getContextMBeanCode();
//			String initialManagerClass = getManagerClass();
			// default to sip standard context and sip specific manager 
			// if this is a sip servlet application
			setContextMBeanCode(SipHostConfig.SIP_CONTEXT_CLASS);
//			setManagerClass(SipStandardManager.class.getName());
			
			//getting the deployer
			AbstractWebDeployer deployer = super.getDeployer(di);
			
			// setting it back to default value for next potential deployements
			setContextMBeanCode(initialContextMBeanCode);
//			setManagerClass(initialManagerClass);
			
			return deployer;
		} else {
			return super.getDeployer(di);
		}
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
			return false;
		}
		
	}
}
