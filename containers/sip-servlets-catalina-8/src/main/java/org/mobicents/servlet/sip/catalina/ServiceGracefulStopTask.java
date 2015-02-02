/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.mobicents.servlet.sip.catalina;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardService;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipService;

/**
 * Task runnning at regular interval that checks whether or not there is still
 * active sip application running
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class ServiceGracefulStopTask implements Runnable {
	private static final Logger logger = Logger.getLogger(ServiceGracefulStopTask.class);		
	
	StandardService sipService;
	
	public ServiceGracefulStopTask(StandardService sipService) {
		this.sipService = sipService;
	}
	
	public void run() {
		int numberOfSipApplicationsDeployed = ((SipService)sipService).getSipApplicationDispatcher().findInstalledSipApplications().length;
		if(logger.isTraceEnabled()) {
			logger.trace("ServiceGracefulStopTask running, number of Sip Application still running " + numberOfSipApplicationsDeployed);
		}
		if(numberOfSipApplicationsDeployed <= 0) {
			try {
//				sipService.stop();
				((SipStandardService)sipService).getServer().stop();
			} catch (LifecycleException e) {
				logger.error("Couldn't gracefully stop service", e);
			}
		}
	}
}
