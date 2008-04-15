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
