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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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

@WebServlet(urlPatterns={"/call"}, asyncSupported=true, loadOnStartup=1, name="Click2CallAsync")
public class SimpleWebServlet extends HttpServlet
{ 	
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SimpleWebServlet.class);
//	@Resource
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
			sipFactory = (SipFactory) envCtx.lookup("sip/ClickToCallAsyncApplication/SipFactory");
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
        			if (call != null) call.end();
        			calls.removeCall(call);
        			notifyStatus("Received Bye All request");
        		}
        	} else {
        		// Someone wants to end an established call, send byes and clean up
        		Call call = calls.getCall(fromAddr, toAddr);
        		call.end();
        		calls.removeCall(call);
        		notifyStatus("Received Bye request");
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
        	notifyStatus("Received Call request");
        }
        
        // Write the output html
    	PrintWriter	out;
        response.setContentType("text/html");
        out = response.getWriter();
        
        out.println("success");
        out.close();
    }
    
	/*
	 * Use the notifyStatus(String event) method in order to trigger an update at the client's page when needed for any new request.
	 * Here will be used for Bye or new Call requests.
	 */
	protected void notifyStatus(String event){
		BlockingQueue<String> eventsQueue = (LinkedBlockingQueue<String>)getServletContext().getAttribute("eventsQueue");
//		if (eventsQueue == null) eventsQueue = new LinkedBlockingQueue<String>();
//		getServletContext().setAttribute("eventsQueue", eventsQueue);
		
		try {
			eventsQueue.put(event);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
}