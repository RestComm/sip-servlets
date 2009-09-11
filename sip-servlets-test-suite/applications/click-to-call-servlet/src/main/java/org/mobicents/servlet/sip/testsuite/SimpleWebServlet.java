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
package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
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
import javax.servlet.sip.SipApplicationSession.Protocol;

import org.apache.log4j.Logger;

public class SimpleWebServlet extends HttpServlet
{ 	
	private static transient Logger logger = Logger.getLogger(SimpleWebServlet.class);
	private SipFactory sipFactory;
	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		logger.info("the SimpleWebServlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.Click2DialApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
	}
    /**
     * Handle the HTTP GET method by building a simple web page.
     */
    public void doGet (HttpServletRequest request,
            	   HttpServletResponse response)
    throws ServletException, IOException
    {
        String toAddr = request.getParameter("to");
        String fromAddr = request.getParameter("from");
        String invalidateHttpSession = request.getParameter("invalidateHttpSession");
        
        URI to = sipFactory.createAddress(toAddr).getURI();
        URI from = sipFactory.createAddress(fromAddr).getURI();              

        // Create app session and request
        SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();
//        SipApplicationSession appSession = 
//        	sipFactory.createApplicationSession();
        if(!appSession.getSessions("HTTP").hasNext()) {
        	response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
        	return;
        }
        SipServletRequest req = sipFactory.createRequest(appSession, "INVITE", from, to);
        
        // Set some attribute
        req.getSession().setAttribute("SecondPartyAddress", sipFactory.createAddress(fromAddr));
        if(invalidateHttpSession != null) {
        	req.getSession().setAttribute("invalidateHttpSession", request.getSession().getId());
        }
        logger.info("Sending request" + req);
        // Send the INVITE request            
        req.send();
        
        // This hsould be at the end otherwise the response will have been committed and 
        // the session can't be accessed anymore 
        // Write some web page content
    	PrintWriter	out;
        response.setContentType("text/html");
        out = response.getWriter();
        out.println("<HTML><HEAD><TITLE>");
        out.println("Click to call - converger sip servlet");
        out.println("</TITLE></HEAD><BODY>");
        out.println("<H1>Click To Call Converged Demo Sip Servlet</H1>");
        out.println("<P>Calling from <b>" + fromAddr + "</b> to <b>" + toAddr + "</b>...");
        if(sipFactory == null) out.println("</BR>Error: SipFactory is null");
        out.println("</BODY></HTML>");
        out.close();
    }
}