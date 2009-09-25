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
package org.mobicents.servlet.sip.alerting;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.slee.EventTypeID;
import javax.slee.connection.ExternalActivityHandle;
import javax.slee.connection.SleeConnection;
import javax.slee.connection.SleeConnectionFactory;

import org.apache.log4j.Logger;
import org.mobicents.slee.service.events.SmsAlertingCustomEvent;

public class SmsAlertServlet extends HttpServlet
{ 	
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SmsAlertServlet.class);
	private static final String EVENT_TYPE = "org.mobicents.slee.service.alerting.SEND_SMS";
	private static final String OK_BODY = "<HTML><BODY>Sms Alert Sent!</BODY></HTML>";
	
	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		logger.info("the SmsAlertServlet has been started");		
	}
    /**
     * Handle the HTTP POST method on which alert can be sent so that the app sends an sms based on that
     */
    public void doPost (HttpServletRequest request, HttpServletResponse response)
    		throws ServletException, IOException {
        String alertId = request.getParameter("alertId");
        String tel = request.getParameter("tel");
        String alertText = request.getParameter("alertText");        
        if(alertText == null || alertText.length() < 1) {
	        // Get the content of the request as the text to parse
	        byte[] content = new byte[request.getContentLength()];
	        request.getInputStream().read(content,0, request.getContentLength());        
	        alertText = new String(content); 	        
        }
        if(logger.isInfoEnabled()) {
        	logger.info("Got an alert : \n alertID : " + alertId + " \n tel : " + tel + " \n text : " +alertText);
        }
        // 
        try {
        	Properties jndiProps = new Properties();			
			Context initCtx = new InitialContext(jndiProps);
        	SleeConnectionFactory factory = (SleeConnectionFactory) initCtx.lookup("java:/MobicentsConnectionFactory");
        	
			SleeConnection conn1 = factory.getConnection();
			ExternalActivityHandle handle = conn1.createActivityHandle();

			EventTypeID requestType = conn1.getEventTypeID(
					EVENT_TYPE,
					"org.mobicents", "1.0");
			SmsAlertingCustomEvent smsAlertingCustomEvent = new SmsAlertingCustomEvent(alertId, tel, alertText);
			
			conn1.fireEvent(smsAlertingCustomEvent, requestType, handle, null);
			conn1.close();
		} catch (Exception e) {
			logger.error("unexpected exception while firing the event " + EVENT_TYPE + " into jslee", e);
		}	
        
        sendHttpResponse(response, OK_BODY);
    }
	/**
	 * @param response
	 * @throws IOException
	 */
	private void sendHttpResponse(HttpServletResponse response, String body)
			throws IOException {
		// Write the output html
    	PrintWriter	out;
        response.setContentType("text/html");
        out = response.getWriter();
        
        // Just redirect to the index
        out.println(body);
        out.close();
	}
}