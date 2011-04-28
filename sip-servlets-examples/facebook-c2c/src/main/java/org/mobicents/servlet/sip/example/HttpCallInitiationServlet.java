/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
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

import org.apache.log4j.Logger;

public class HttpCallInitiationServlet extends HttpServlet
{ 	
	private static Logger logger = Logger.getLogger(HttpCallInitiationServlet.class);
	private SipFactory sipFactory;
	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		logger.info("the servlet has been started");
		try { 			
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			sipFactory = (SipFactory) envCtx.lookup("sip/FacebookClickToCallSip/SipFactory");
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
    	String access = request.getParameter("access");
    	String callAuthCode = getServletContext().getInitParameter("call.code");
    	if (callAuthCode.equals(access)) {

			String toAddr = "sip:+" + request.getParameter("to").trim() + "@"
					+ getServletContext().getInitParameter("domain.name");

			String fromAddr = "sip:"
					+ getServletContext().getInitParameter("user1") + "@"
					+ getServletContext().getInitParameter("domain.name");

			String secondParty = "sip:+" + request.getParameter("from").trim()
					+ "@" + getServletContext().getInitParameter("domain.name");

			URI to = toAddr == null ? null : sipFactory.createAddress(toAddr)
					.getURI();
			URI from = fromAddr == null ? null : sipFactory.createAddress(
					fromAddr).getURI();

			// Create app session and request
			SipApplicationSession appSession =
			// ((ConvergedHttpSession)request.getSession()).getApplicationSession();
			sipFactory.createApplicationSession();

			SipServletRequest req = sipFactory.createRequest(appSession,
					"INVITE", from, to);

			// Set some attribute
			req.getSession().setAttribute("SecondPartyAddress",
					sipFactory.createAddress(secondParty));
			req.getSession().setAttribute("FromAddr",
					sipFactory.createAddress(fromAddr));
			req.getSession().setAttribute("user",
					getServletContext().getInitParameter("user1"));
			req.getSession().setAttribute("pass",
					getServletContext().getInitParameter("pass1"));

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