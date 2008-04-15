package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleWebServlet extends HttpServlet
{ 	
	private static Log logger = LogFactory.getLog(SimpleWebServlet.class);
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
			sipFactory = (SipFactory) envCtx.lookup("sip/SipFactory");
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
        String bye = request.getParameter("bye");

        URI to = sipFactory.createAddress(toAddr).getURI();
        URI from = sipFactory.createAddress(fromAddr).getURI();    

        CallStatusContainer calls = (CallStatusContainer) getServletContext().getAttribute("activeCalls");

        // Create app session and request
        SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();

        if(bye != null) {
        	// Someone wants to end an established call, send byes and clean up
        	SipServletRequest bye1 = sipFactory.createRequest(appSession, "BYE", from, to);
        	SipServletRequest bye2 = sipFactory.createRequest(appSession, "BYE", to, from);
        	bye1.send();
        	bye2.send();
        	calls.removeCall(from.toString(), to.toString());
        	calls.removeCall(to.toString(), from.toString());
        } else {
        	if(calls == null) {
        		calls = new CallStatusContainer();
        		getServletContext().setAttribute("activeCalls", calls);
        	}
        	
        	// Add the call in the active calls
        	calls.addCall(fromAddr, toAddr, "FFFF00");

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