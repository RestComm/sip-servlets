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

package org.mobicents.servlet.sip.alerting;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class RedOxygenSmsAlertServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(RedOxygenSmsAlertServlet.class);
	private static final String RESPONSE_BEGIN = "<HTML><BODY>";
	private static final String RESPONSE_END = "</BODY></HTML>";		

	private static final String RED_OXYGEN_URL = "http://sms1.redoxygen.net/sms.dll?Action=SendSMS";
	private static final String HTTP_METHOD = "POST";
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.info("the SmsAlertServlet has been started");
	}

	/**
	 * Handle the HTTP POST method on which alert can be sent so that the app
	 * sends an sms based on that
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String accountId  = getServletContext().getInitParameter("accountId"); // Put your AccountId here
		String email      = getServletContext().getInitParameter("email"); // Put your Email address here (Used for authentication and replies)
		String password   = getServletContext().getInitParameter("password");  // Put your Password here				
		
		String alertId = request.getParameter("alertId");
		String tel = request.getParameter("tel");
		String alertText = request.getParameter("alertText");        
        if(alertText == null || alertText.length() < 1) {
	        // Get the content of the request as the text to parse
	        byte[] content = new byte[request.getContentLength()];
	        request.getInputStream().read(content,0, request.getContentLength());        
	        alertText = new String(content); 	        
        }
        alertId = URLEncoder.encode(alertId, "UTF-8");
//        tel = URLEncoder.encode(tel, "UTF-8");
//        alertText = URLEncoder.encode(alertText, "UTF-8");        
		
		Integer nResult;
		StringBuffer strResponse = new StringBuffer();
		String sData;

		try {
			sData = ("AccountId=" + URLEncoder.encode(accountId, "UTF-8"));
//			sData += ("&Email=" + URLEncoder.encode(email, "UTF-8"));
			sData += ("&Email=" + email);
			sData += ("&Password=" + URLEncoder.encode(password, "UTF-8"));
			sData += ("&Recipient=" + URLEncoder.encode(tel, "UTF-8"));
			sData += ("&Message=" + URLEncoder.encode(alertText, "UTF-8"));

			if(logger.isInfoEnabled()) {        	
	        	logger.info("Got an alert : \n alertID : " + alertId + " \n tel : " + tel + " \n text : " +alertText);
	        	logger.info("Red Oxygen : \n Account Id : " + accountId + " \n email : " + email + " \n password : " + password);
	        	logger.info("Red Oxygen URL : \n URL : " + RED_OXYGEN_URL + " \n data : " + sData);
	        }
	        
			
			URL urlObject = new URL(RED_OXYGEN_URL);

			HttpURLConnection con = (HttpURLConnection) urlObject.openConnection();
			con.setRequestMethod(HTTP_METHOD);
			con.setDoInput(true);
			con.setDoOutput(true);

			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			out.writeBytes(sData);
			out.flush();
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con
					.getInputStream()));

			String inputLine;
			StringBuffer responseBuffer = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				responseBuffer = responseBuffer.append(inputLine);
				responseBuffer = responseBuffer.append("\n\n\n");
			}

			strResponse.replace(0, 0, responseBuffer.toString());

			String sResultCode = strResponse.substring(0, 4);
			nResult = new Integer(sResultCode);

			in.close();
		} catch (Exception e) {			
			logger.error("Exception caught sending SMS",e);
			nResult = -2;
		}		

		sendHttpResponse(response, RESPONSE_BEGIN + "result : " + nResult + "<br/>response : " + strResponse + RESPONSE_END);
	}

	/**
	 * @param response
	 * @throws IOException
	 */
	private void sendHttpResponse(HttpServletResponse response, String body)
			throws IOException {
		// Write the output html
		PrintWriter out;
		response.setContentType("text/html");
		out = response.getWriter();

		// Just redirect to the index
		out.println(body);
		out.close();
	}
}