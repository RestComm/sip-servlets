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
package org.mobicents.servlet.sip.startup;

import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardEngine;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;

/**
 * Sip Servlet implementation of the <code>Engine</code> interface.  
 * This class inherits from the Tomcat StandardEngine. <br>
 * This class will allow the sip application dispatcher of the service to
 * know what are the host names currently deployed on the server
 * 
 * @author Jean Deruelle
 */
public class SipStandardEngine extends StandardEngine {
	
	private static final long serialVersionUID = 1L;
	/**
     * The descriptive information string for this implementation.
     */
    private static final String info =
        "org.mobicents.servlet.sip.startup.SipStandardEngine/1.0";
	
	@Override
	public void addChild(Container child) {				
		if(child instanceof Host) {
			final Host host = (Host) child;
			String hostName = host.getName();
			String[] aliases = host.findAliases();			
			if(getService() instanceof SipService) {
				final SipService sipService = (SipService) getService();
				SipApplicationDispatcher sipApplicationDispatcher = sipService.getSipApplicationDispatcher();
				if(sipApplicationDispatcher != null) {
					sipApplicationDispatcher.addHostName(hostName);
					for (String alias : aliases) {
						sipService.getSipApplicationDispatcher().addHostName(alias);
					}
				}
			}
			//FIXME : ugly hack to cope with lack of extensibility  in jboss as 5 Tomcat Service
			if("org.jboss.web.tomcat.service.deployers.JBossContextConfig".equals(host.getConfigClass())) {
				host.setConfigClass("org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig");
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
				SipApplicationDispatcher sipApplicationDispatcher = sipService.getSipApplicationDispatcher();
				if(sipApplicationDispatcher != null) {
					sipApplicationDispatcher.removeHostName(hostName);
					for (String alias : aliases) {
						sipService.getSipApplicationDispatcher().removeHostName(alias);
					}
				}
			}
		}
		super.removeChild(child);		
	}
	
	@Override
	/**
	 * Used for JBoss AS 5 to add the hostnames and aliases to the sip application dispatcher
	 * since when addChild is called the sip application dispatcher is still null or getService might still return null
	 */
	public void start() throws LifecycleException {		
		super.start();
		if(getService() instanceof SipService) {
			final SipService sipService = (SipService) getService();
			SipApplicationDispatcher sipApplicationDispatcher = sipService.getSipApplicationDispatcher();
			if(sipApplicationDispatcher != null) {
				for (Object child: children.values()) {
					Host host = (Host) child;
					String hostName = host.getName();
					String[] aliases = host.findAliases();
					sipApplicationDispatcher.addHostName(hostName);
					for (String alias : aliases) {
						sipService.getSipApplicationDispatcher().addHostName(alias);
					}
				}
			}
		}
	}
}
