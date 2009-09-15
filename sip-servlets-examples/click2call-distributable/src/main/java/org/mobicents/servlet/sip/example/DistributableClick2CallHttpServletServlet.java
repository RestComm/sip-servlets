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
package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.sip.ConvergedHttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DistributableClick2CallHttpServletServlet extends HttpServlet
{ 	
	private static Log logger = LogFactory.getLog(DistributableClick2CallHttpServletServlet.class);
	private static final long serialVersionUID = 1L;
	@Resource
	private SipFactory sipFactory;	
	/**
	 * Handle the HTTP GET method by building a simple web page.
	 */
	public void doGet (HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException
	{
		String toAddr = request.getParameter("to");
		String fromAddr = request.getParameter("from");
		
		String bye = request.getParameter("bye");
		


		URI to = toAddr==null? null : sipFactory.createAddress(toAddr).getURI();
		URI from = fromAddr==null? null : sipFactory.createAddress(fromAddr).getURI();  
		
		
		SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();
		
		if(bye != null) {
			Iterator i = appSession.getSessions();
			while(i.hasNext()) {
				SipSession s = (SipSession) i.next();
				s.createRequest("BYE").send();
			}
			
		}
		
		appSession.setAttribute("setFromHttpServlet", appSession.getId());
		
		if(to != null && from != null)
		{

			SipServletRequest req = sipFactory.createRequest(appSession, "INVITE", from, to);

			// Set some attribute
			req.getSession().setAttribute("SecondPartyAddress", sipFactory.createAddress(fromAddr));

			logger.info("Sending request" + req);
			// Send the INVITE request            
			req.send();
		}

		// Write the output html
		PrintWriter	out;
		response.setContentType("text/html");
		out = response.getWriter();

		// Just redirect to the index
		out.println("<HTML><META HTTP-EQUIV=\"Refresh\"CONTENT=\"0; URL=index.jsp\"><HEAD><TITLE></HTML>");
		out.close();
	}
}