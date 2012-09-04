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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.pbx.location.LocationService;
import org.mobicents.servlet.sip.pbx.proxy.ProxyService;
import org.mobicents.servlet.sip.pbx.registrar.RegistrarService;

/**
 * @author Thomas Leseney 
 */
public class PbxServlet extends SipServlet {

	private static Log logger = LogFactory.getLog(PbxServlet.class);
	
	private RegistrarService registrar;
	private ProxyService proxy;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
		LocationService locationService = (LocationService) getServletContext().getAttribute(Constants.LOCATION_SERVICE);
		
		if (locationService == null) {
			logger.error("No location service in context");
			throw new UnavailableException("no location service");
		}
		
		registrar = new RegistrarService();
		registrar.setLocationService(locationService);
		registrar.setSipFactory(sipFactory);
		
		proxy = new ProxyService();
		proxy.setLocationService(locationService);
		proxy.setSipFactory(sipFactory);
		
		logger.info("PbxServlet initialized");
	}
	
	@Override 
	public void doRegister(SipServletRequest register) throws ServletException, IOException {
		registrar.doRegister(register);
	}
	
	@Override
	public void doInvite(SipServletRequest request) throws ServletException, IOException {
		proxy.doRequest(request);
	}
	
}
