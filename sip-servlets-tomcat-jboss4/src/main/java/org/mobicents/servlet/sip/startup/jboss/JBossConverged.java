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

import org.jboss.deployment.DeploymentInfo;
import org.jboss.web.AbstractWebDeployer;
import org.jboss.web.tomcat.service.JBossWeb;
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
		return JBossSip.isSipServletApplication(di);
		
	}
}
