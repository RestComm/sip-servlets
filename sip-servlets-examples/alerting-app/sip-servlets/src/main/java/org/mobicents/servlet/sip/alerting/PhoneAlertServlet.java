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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.annotation.Resource;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
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
import org.mobicents.servlet.sip.alerting.util.DTMFListener;
import org.mobicents.servlet.sip.alerting.util.MMSUtil;

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
	        // Fix from Rafael Soares http://groups.google.com/group/mobicents-public/browse_thread/thread/16015a2e2c034806
	        // remove '\n' and ',' chars to avoid problems with TTS
	        alertText = alertText.replace('\n', ' ').replace(',', ' ');
	        if(logger.isInfoEnabled()) {
	        	logger.info("Got an alert : \n alertID : " + alertId + " \n tel : " + tel + " \n text : " +alertText);
	        }
        }
        alertText = "Hello Admin; the server 0.0.0.0:2999 you're monitoring is running out of memory";
		// Create app session and request
        SipApplicationSession appSession = 
        	((ConvergedHttpSession)request.getSession()).getApplicationSession();
        URI fromURI = sipFactory.createAddress(FROM).getURI();
        URI toURI = sipFactory.createAddress(tel).getURI();
        SipServletRequest sipServletRequest = sipFactory.createRequest(appSession, INVITE, fromURI, toURI);        
        sipServletRequest.setRequestURI(toURI);
		
        sipServletRequest.getSession().setAttribute("speechUri",
				java.net.URI.create("data:" + URLEncoder.encode("ts("+ alertText +")", "UTF-8")));
		try {
			// Media Server Control Creation
			MediaSession mediaSession = MMSUtil.getMsControl().createMediaSession();
			NetworkConnection connection = mediaSession
			.createNetworkConnection(NetworkConnection.BASIC);

			SdpPortManager sdpManag = connection.getSdpPortManager();
			sdpManag.generateSdpOffer();
			
			byte[] sdpOffer = null;
			int numTimes = 0;
			while(sdpOffer == null && numTimes<10) {
				sdpOffer = sdpManag.getMediaServerSessionDescription();
				Thread.sleep(500);
				numTimes++;
			}
			
			sipServletRequest.setContentLength(sdpOffer.length);
			sipServletRequest.setContent(sdpOffer, "application/sdp");	
			
			MediaGroup mg = mediaSession.createMediaGroup(MediaGroup.PLAYER_SIGNALDETECTOR);
			
			// storing helpful info for later during the call
			SipSession sipSession = sipServletRequest.getSession();
			sipSession.setAttribute("connection", connection);
			sipSession.setAttribute("alertId", alertId);
			sipSession.setAttribute("tel", tel);
			sipSession.setAttribute("alertText", alertText);
			sipSession.setAttribute("connection", connection);
			sipSession.setAttribute("mediaGroup", mg);
			sipSession.setAttribute("mediaSession", mediaSession);
			sipSession.setAttribute("feedbackUrl", (String)getServletContext().getAttribute("alert.feedback.url"));
			sipServletRequest.send();
			mg.getSignalDetector().addListener(new DTMFListener(alertId, (String)getServletContext().getAttribute("alert.feedback.url")));
			connection.join(Direction.DUPLEX, mg);
		} catch (Exception e) {
			// Should not happen
			logger.error("Unexpected exception while trying to send the SIP Request", e);
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