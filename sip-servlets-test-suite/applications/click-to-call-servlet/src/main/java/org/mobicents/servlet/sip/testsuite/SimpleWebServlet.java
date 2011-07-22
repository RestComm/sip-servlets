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

package org.mobicents.servlet.sip.testsuite;

import gov.nist.core.ServerLogger;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.RawMessageChannel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;
import javax.servlet.sip.SipApplicationSession.Protocol;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipApplicationSessionAsynchronousWork;
import org.mobicents.javax.servlet.sip.SipApplicationSessionExt;
import org.mobicents.javax.servlet.sip.SipSessionAsynchronousWork;
import org.mobicents.javax.servlet.sip.SipSessionExt;

public class SimpleWebServlet extends HttpServlet { 	
	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(SimpleWebServlet.class);
	private static final String CONTENT_TYPE = "text/plain;charset=UTF-8";
	
	@Resource
	private SipFactory sipFactory;
	@Resource
	private SipSessionsUtil sipSessionsUtil;
	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		logger.info("the SimpleWebServlet has been started");
		try { 			
			SipFactory contextFactory = (SipFactory) config.getServletContext().getAttribute(SipServlet.SIP_FACTORY);
			if(contextFactory == null) {
				throw new IllegalStateException("The Sip Factory should be available in init method");
			} else {
				logger.info("Sip Factory ref from Servlet Context : " + contextFactory);				
			}
			// Getting the Sip factory from the JNDI Context
			Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			SipFactory jndiSipFactory = (SipFactory) envCtx.lookup("sip/org.mobicents.servlet.sip.testsuite.Click2DialApplication/SipFactory");
			if(jndiSipFactory == null) {
				throw new IllegalStateException("The Sip Factory from JNDI should be available in init method");
			} else {
				logger.info("Sip Factory ref from JNDI : " + jndiSipFactory);
			}
		} catch (NamingException e) {
			throw new ServletException("Uh oh -- JNDI problem !", e);
		}
		if(sipFactory == null) {
			throw new IllegalStateException("The Sip Factory from Annotations should be available in init method");
		} else {
			logger.info("Sip Factory ref from Annotations : " + sipFactory);								
		}
	}
    /**
     * Handle the HTTP GET method by building a simple web page.
     */
    public void doGet (HttpServletRequest request,
            	   HttpServletResponse response)
    throws ServletException, IOException
    {
    	
    	if(request.getParameter("expirationTime") != null) {
    		PrintWriter	out;
            response.setContentType("text/html");
            out = response.getWriter();
            out.println("" + ((javax.servlet.sip.ConvergedHttpSession)request.getSession()).getApplicationSession().getExpirationTime() );
            out.close();
            return;
    	}
    	
        String toAddr = request.getParameter("to");
        String fromAddr = request.getParameter("from");
        String invalidateHttpSession = request.getParameter("invalidateHttpSession");
        String asyncWorkMode = request.getParameter("asyncWorkMode");
        String asyncWorkSasId = request.getParameter("asyncWorkSasId");
        if(asyncWorkMode != null && asyncWorkSasId != null) {
        	doAsyncWork(asyncWorkMode, asyncWorkSasId, response);
        	return;
        }
        
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
        out.println("</BODY></HTML>");
        out.close();
    }
	private void doAsyncWork(String asyncWorkMode, String asyncWorkSasId, HttpServletResponse response) throws IOException {			
		if(asyncWorkMode.equals("Thread")) {			
			Runnable processMessageTask = new Runnable() {
                public void run() {
                	SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();                	
                	String response = "testThread";					
                	sipApplicationSession.setAttribute(response, "true");
					sendMessage(sipApplicationSession, sipFactory, response);
                }
            };
            Executors.newFixedThreadPool(4).execute(processMessageTask);            
		} else if(asyncWorkMode.equals("SipSession")) {
			final SipApplicationSession sipApplicationSession = sipSessionsUtil.getApplicationSessionById(asyncWorkSasId);
			Iterator<SipSession> sipSessionIterator = (Iterator<SipSession>) sipApplicationSession.getSessions(Protocol.SIP.toString());
			SipSession sipSession = sipSessionIterator.next();
			((SipSessionExt)sipSession).scheduleAsynchronousWork(new SipSessionAsynchronousWork() {
				private static final long serialVersionUID = 1L;
	
				public void doAsynchronousWork(SipSession sipSession) {				
					String content = "web";
					sipSession.setAttribute("mutable", content);
					logger.info("doAsynchronousWork beforeSleep " + content);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String mutableAttr = (String) sipSession.getAttribute("mutable");
					logger.info("doAsyncWork afterSleep " + mutableAttr + " vs " + content);
					String response = "OK";
					if(!content.equals(mutableAttr))
						response = "KO";
					
					sendMessage(sipApplicationSession, sipFactory, response);
				}
			});
			
		} else {
			final SipApplicationSession sipApplicationSession = sipSessionsUtil.getApplicationSessionById(asyncWorkSasId);
			((SipApplicationSessionExt)sipApplicationSession).scheduleAsynchronousWork(new SipApplicationSessionAsynchronousWork() {
				private static final long serialVersionUID = 1L;
	
				public void doAsynchronousWork(SipApplicationSession sipApplicationSession) {				
					String content = "web";
					sipApplicationSession.setAttribute("mutable", content);
					logger.info("doAsynchronousWork beforeSleep " + content);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String mutableAttr = (String) sipApplicationSession.getAttribute("mutable");
					logger.info("doAsyncWork afterSleep " + mutableAttr + " vs " + content);
					String response = "OK";
					if(!content.equals(mutableAttr))
						response = "KO";
					
					sendMessage(sipApplicationSession, sipFactory, response);
				}
			});
		}
		PrintWriter	out;
        response.setContentType("text/html");
        out = response.getWriter();
        out.println("<HTML><HEAD><TITLE>");
        out.println("Click to call - converger sip servlet");
        out.println("</TITLE></HEAD><BODY>");
        out.println("OK");        
        out.println("</BODY></HTML>");
        out.close();
	}
	
	/**
	 * @param sipApplicationSession
	 * @param storedFactory
	 */
	private static void sendMessage(SipApplicationSession sipApplicationSession,
			SipFactory storedFactory, String content) {
		try {
			SipServletRequest sipServletRequest = storedFactory.createRequest(
					sipApplicationSession, 
					"MESSAGE", 
					"sip:sender@sip-servlets.com", 
					"sip:receiver@sip-servlets.com");
			SipURI sipUri=storedFactory.createSipURI("receiver", "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":5080");
			sipServletRequest.setRequestURI(sipUri);
			sipServletRequest.setContentLength(content.length());
			sipServletRequest.setContent(content, CONTENT_TYPE);
			sipServletRequest.send();
		} catch (ServletParseException e) {
			logger.error("Exception occured while parsing the addresses",e);
		} catch (IOException e) {
			logger.error("Exception occured while sending the request",e);			
		}
	}
}