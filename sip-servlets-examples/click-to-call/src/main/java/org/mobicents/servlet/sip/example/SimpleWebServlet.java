package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.sip.*;

public class SimpleWebServlet extends HttpServlet
{ 	

    /**
     * Handle the HTTP GET method by building a simple web page.
     */
    public void doGet (HttpServletRequest request,
            	   HttpServletResponse response)
    throws ServletException, IOException
    {
    	// Get the factory
       	SipFactory sf = (SipFactory) getServletContext().getAttribute(javax.servlet.sip.SipServlet.SIP_FACTORY);
        
        String toAddr = request.getParameter("to");
        String fromAddr = request.getParameter("from");
        URI to = sf.createAddress(toAddr).getURI();
        URI from = sf.createAddress(fromAddr).getURI();              

        // Create app session and request
//        SipApplicationSession appSession = 
//        	((ConvergedHttpSession)request.getSession()).getApplicationSession();
        SipApplicationSession appSession = 
        	sf.createApplicationSession();
        SipServletRequest req = sf.createRequest(appSession, "INVITE", from, to);
        
        // Set some attribute
        req.getSession().setAttribute("SecondPartyAddress", sf.createAddress(fromAddr));
        
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
        if(sf == null) out.println("</BR>Error: SipFactory is null");
        out.println("</BODY></HTML>");
        out.close();
    }
}