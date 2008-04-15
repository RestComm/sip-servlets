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
package org.mobicents.servlet.sip.startup;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardEngine;

/**
 * Sip Servlet implementation of the <code>Engine</code> interface.  
 * This class inherits from the Tomcat StandardEngine. <br>
 * This class will allow the sip application dispatcher of the service to
 * know what are the host names currently deployed on the server
 * 
 * @author Jean Deruelle
 */
public class SipStandardEngine extends StandardEngine implements Engine {
	@Override
	public void addChild(Container child) {				
		if(child instanceof Host) {
			final Host host = (Host) child;
			String hostName = host.getName();
			String[] aliases = host.findAliases();
			if(getService() instanceof SipService) {
				final SipService sipService = (SipService) getService();
				sipService.getSipApplicationDispatcher().addHostName(hostName);
				for (String alias : aliases) {
					sipService.getSipApplicationDispatcher().addHostName(alias);
				}
			}
		}
		super.addChild(child);
	}
	
	@Override
	public void removeChild(Container child) {
		if(child instanceof Host) {
			final Host host = (Host) child;
			String hostName = host.getName();
			String[] aliases = host.findAliases();
			if(getService() instanceof SipService) {
				final SipService sipService = (SipService) getService();
				sipService.getSipApplicationDispatcher().removeHostName(hostName);
				for (String alias : aliases) {
					sipService.getSipApplicationDispatcher().removeHostName(alias);
				}
			}
		}
		super.removeChild(child);		
	}
}
