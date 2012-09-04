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
package org.mobicents.servlet.sip.pbx;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.pbx.db.DbLocationService;
import org.mobicents.servlet.sip.pbx.location.LocationService;

/**
 * @author Thomas Leseney
 */
public class PbxListener implements ServletContextListener {

	private static Log logger = LogFactory.getLog(PbxListener.class);
	
	public void contextInitialized(ServletContextEvent event) {
		try {
			LocationService locationService = new DbLocationService();
			locationService.start();
		
			event.getServletContext().setAttribute(Constants.LOCATION_SERVICE, locationService);
		
			logger.info("Initialized PBX context");
		} catch (Exception e) {
			logger.warn("Failed to initialize PBX context", e);
		}
	}
	
	public void contextDestroyed(ServletContextEvent event) {
		LocationService locationService = 
			(LocationService) event.getServletContext().getAttribute(Constants.LOCATION_SERVICE);
		if (locationService != null) {
			try {
				locationService.stop();
			} catch (Exception e) {
			}
		}
	}
}
