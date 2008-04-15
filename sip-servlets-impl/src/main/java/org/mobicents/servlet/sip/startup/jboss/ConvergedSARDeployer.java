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

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SARDeployer;

/**
 * @author Jean Deruelle
 *
 */
public class ConvergedSARDeployer extends SARDeployer {
	
	private static final String SAR_SUFFIX = ".sar2";

	@Override
	public boolean accepts(DeploymentInfo sdi) {		
		boolean accept = super.accepts(sdi);
		
		String urlPath = sdi.url.getPath();
        String shortName = sdi.shortName;
        boolean checkDir = sdi.isDirectory && !(sdi.isXML || sdi.isScript);
		//if this is a .sar file and it contains a sip.xml file then we accept it 
		if ((urlPath.endsWith(SAR_SUFFIX) || 
				(checkDir && shortName.endsWith(SAR_SUFFIX))) &&
				JBossConverged.isSipServletApplication(sdi)) {
			return false;
	    }
		
		return accept;
	}
}
