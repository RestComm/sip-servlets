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
import java.util.Properties;

import javax.annotation.Resource;
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
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;

public class SimpleWebServlet extends HttpServlet
{ 	
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SimpleWebServlet.class);
	private SipFactory sipFactory;
	@Resource
	private SipFactory sipFactory2;
	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		logger.info("the SimpleWebServlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.example.SimpleApplication/SipFactory");
			logger.info("Sip Factory ref from JNDI : " + sipFactory);
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
		logger.info("Sip Factory ref from annotation : " + sipFactory2);
		logger.info("Sip Factory ref from serv let context : " + getServletContext().getAttribute(SipServlet.SIP_FACTORY));
		logger.info("init param from servlet context : " + getServletContext().getInitParameter("foo"));
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
        String bye = request.getParameter("bye");

        URI to = toAddr==null? null : sipFactory.createAddress(toAddr).getURI();
        URI from = fromAddr==null? null : sipFactory.createAddress(fromAddr).getURI();    

        CallStatusContainer calls = (CallStatusContainer) getServletContext().getAttribute("activeCalls");

        // Create app session and request
        SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();

        if(bye != null) {
        	if(bye.equals("all")) {
        		Iterator it = (Iterator) appSession.getSessions("sip");
        		while(it.hasNext()) {
        			SipSession session = (SipSession) it.next();
        			Call call = (Call) session.getAttribute("call");
        			call.end();
        			calls.removeCall(call);
        		}
        	} else {
        		// Someone wants to end an established call, send byes and clean up
        		Call call = calls.getCall(fromAddr, toAddr);
        		call.end();
        		calls.removeCall(call);
        	}
        } else {
        	if(calls == null) {
        		calls = new CallStatusContainer();
        		getServletContext().setAttribute("activeCalls", calls);
        	}
        	
        	// Add the call in the active calls
        	Call call = calls.addCall(fromAddr, toAddr, "FFFF00");

        	SipServletRequest req = sipFactory.createRequest(appSession, "INVITE", from, to);

        	// Set some attribute
        	req.getSession().setAttribute("SecondPartyAddress", sipFactory.createAddress(fromAddr));
        	req.getSession().setAttribute("call", call);
        	
        	// This session will be used to send BYE
        	call.addSession(req.getSession());
        	
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