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
package org.mobicents.servlet.sip.pbx.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.sip.SipFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.pbx.Constants;
import org.mobicents.servlet.sip.pbx.location.Binding;
import org.mobicents.servlet.sip.pbx.location.LocationService;

/**
 * @author Thomas Leseney
 */
public class DisplayServlet extends HttpServlet
{ 	
	private static Log logger = LogFactory.getLog(DisplayServlet.class);
	private SipFactory sipFactory;
	private LocationService locationService;
	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		locationService = (LocationService) getServletContext().getAttribute(Constants.LOCATION_SERVICE);
	}

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       
    	PrintWriter	out;
        response.setContentType("text/html");
        out = response.getWriter();
        out.println("<html><body>");
        
        if (locationService == null) {
        	out.println("<h2>Location Service not started</h2>");
        } else {
        	out.println("<h2>Registered Users</h2>");
        	locationService.beginTransaction();
        	List<Binding> bindings = locationService.getAllBindings();
        	locationService.commitTransaction();
        	
        	out.println("<table border=\"1\" cellspacing=\"0\"><th>AOR</th><th>Contact</th><th>Expiration Time</th>");
			
        	for (Binding binding : bindings) {
        		out.println("<tr>");
        		out.println("<td>" + binding.getAor() + "</td>");
        		out.println("<td>" + binding.getContact() + "</td>");
        		out.println("<td>" + new Date(binding.getExpirationTime()) + "</td>");
        		out.println("</tr>");
        	}
        	out.println("</table>");
        }
        out.close();
    }
}