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
package org.mobicents.servlet.sip.monitoring;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
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
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.servlet.sip.monitoring.util.MediaConnectionListener;
import org.mobicents.servlet.sip.monitoring.util.TTSUtils;

public class PhoneAlertServlet extends HttpServlet
{ 	
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(PhoneAlertServlet.class);
	private static final String INVITE = "INVITE";
	private static final String FROM = "sip:mss-jopr-alertin-app@mobicents.org";
	private static final String OK_BODY = "<HTML><BODY>Phone Alert Sent!</BODY></HTML>";
	
	@Resource
	private SipFactory sipFactory;	

	@Override
	public void init(ServletConfig config) throws ServletException {		
		super.init(config);
		logger.info("the PhoneAlertServlet has been started");		
	}
    /**
     * Handle the HTTP POST method on which alert can be sent so that the app initiates phone calls based on that
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
	        if(logger.isInfoEnabled()) {
	        	logger.info("Got an alert : \n alertID : " + alertId + " \n tel : " + tel + " \n text : " +alertText);
	        }
        }
        // TTS creation
        String pathToAudioDirectory = (String)getServletContext().getAttribute("audioFilePath");
        String fileName= pathToAudioDirectory + System.nanoTime() + "-monitoring-alert-" + alertId;
//        String fileName= System.nanoTime() + "-monitoring-alert-" + alertId;
        try {
			TTSUtils.buildAudio(alertText, fileName);
			fileName = fileName+ ".wav";
			logger.info("Speech created at " + fileName);
		} catch (Exception e) {
			logger.error("Unexpected exception while creating the text to speech file", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			String errorBody = "<HTML><BODY>Phone Alert not Sent, could not generate the Text to Speech file !</BODY></HTML>";
			sendHttpResponse(response, errorBody);
			return;
		}		                
        
		// Create app session and request
        SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();
        URI fromURI = sipFactory.createAddress(FROM).getURI();
        URI toURI = sipFactory.createAddress(tel).getURI();
        SipServletRequest sipServletRequest = sipFactory.createRequest(appSession, INVITE, fromURI, toURI);        
        sipServletRequest.setRequestURI(toURI);
		
		try {
			// Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection(MediaConnectionListener.PR_JNDI_NAME);
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
			connection.addConnectionListener(listener);
			
			// storing helpful info for later during the call
			SipSession sipSession = sipServletRequest.getSession();
			sipSession.setAttribute("connection", connection);
			sipSession.setAttribute("alertId", alertId);
			sipSession.setAttribute("tel", tel);
			sipSession.setAttribute("alertText", alertText);
			sipSession.setAttribute("fileName", fileName);
			sipSession.setAttribute("feedbackUrl", (String)getServletContext().getAttribute("alert.feedback.url"));
			
			if(logger.isInfoEnabled()) {
				logger.info("waiting to get the SDP from Media Server before sending the INVITE to " + tel);
			}
			connection.modify("$", null);
		} catch (ClassNotFoundException e) {
			// Should not happen
			logger.error("Unexpected exception while trying to get the Media Peer Factory", e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			String errorBody = "<HTML><BODY>Phone Alert not Sent, could not find Media Libraries !</BODY></HTML>";
			sendHttpResponse(response, errorBody);
			return;
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