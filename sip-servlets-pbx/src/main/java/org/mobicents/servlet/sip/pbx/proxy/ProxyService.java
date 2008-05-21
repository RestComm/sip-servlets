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
package org.mobicents.servlet.sip.pbx.proxy;

import java.io.IOException;
import java.util.List;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.pbx.URIUtil;
import org.mobicents.servlet.sip.pbx.location.Binding;
import org.mobicents.servlet.sip.pbx.location.LocationService;

/**
 * @author Thomas Leseney
 */
public class ProxyService {

	private static Log logger = LogFactory.getLog(ProxyService.class);
	
	private SipFactory sipFactory;
	private LocationService locationService;
	
	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}
	
	public void setSipFactory(SipFactory sipFactory) {
		this.sipFactory = sipFactory;
	}
	
	public void doRequest(SipServletRequest request) throws IOException {
		
		if (!request.isInitial()) {
			return;
		}
		
		try {
			String aor = URIUtil.toCanonical(request.getRequestURI());
			
			logger.debug("Initial request (" + request.getMethod() + ") for AOR: " + aor);
			locationService.beginTransaction();
			
			List<Binding> bindings = locationService.getBindings(aor);
			
			if (bindings != null && bindings.size() > 0) {
				Binding binding = (Binding) bindings.get(0);
				URI target = sipFactory.createURI(binding.getContact());
				
				logger.debug("Proxying request to: " + target);
				Proxy proxy = request.getProxy();
				proxy.setRecordRoute(true);
				proxy.proxyTo(target);
			} else {
				logger.debug("No binding found for AOR: " + aor);
				request.createResponse(SipServletResponse.SC_NOT_FOUND).send();
			}
		} catch (Exception e) {
			logger.warn("Exception while proxying request", e);
			request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR,
					e.getMessage()).send();
		} finally {
			locationService.commitTransaction();
		}
	}
}
