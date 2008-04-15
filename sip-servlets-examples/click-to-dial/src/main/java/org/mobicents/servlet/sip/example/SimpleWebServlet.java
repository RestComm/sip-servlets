package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.sip.SipFactory;

public class SimpleWebServlet extends HttpServlet
{ 
    /**
     * Handle the HTTP GET method by building a simple web page.
     */
    public void doGet (HttpServletRequest request,
            	   HttpServletResponse response)
    throws ServletException, IOException
    {
    	PrintWriter	out;
    	String title = "Click To Call Converged Demo Sip Servlet";
    	SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(javax.servlet.sip.SipServlet.SIP_FACTORY);
		
    	// set content type and other response header fields first
        response.setContentType("text/html");

        // then write the data of the response
        out = response.getWriter();

        out.println("<HTML><HEAD><TITLE>");
        out.println(title);
        out.println("</TITLE></HEAD><BODY>");
        out.println("<H1>" + title + "</H1>");
        out.println("<P>Calling...");
        if(sipFactory == null) out.println("</BR>Error: SipFactory is null");
        out.println("</BODY></HTML>");
        out.close();
    }
}