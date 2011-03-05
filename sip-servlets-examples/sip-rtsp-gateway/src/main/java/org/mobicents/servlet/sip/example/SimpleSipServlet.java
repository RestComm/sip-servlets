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
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.SipSession.State;

import net.java.stun4j.stack.StunStack;

import org.apache.log4j.Logger;

/**
 * This example shows a simple User agent that can any accept call and reply to BYE or initiate BYE 
 * depending on the sender.
 * 
 * @author Jean Deruelle
 *
 */
public class SimpleSipServlet extends SipServlet{
	private static Logger logger = Logger.getLogger(SimpleSipServlet.class);
	public SimpleSipServlet() {
	}
	
	protected void doRegister(SipServletRequest request) throws ServletException,
	IOException {
		request.createResponse(200).send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {
		
		if(logger.isInfoEnabled()) {
			logger.info("Simple Servlet: Got request:\n"
				+ request.getMethod());
		}
		
		RTSPStack rtsp = new RTSPStack();
		
		// Try to take RTSP URI from the SIP request "To" header
		String rtspParameter = request.getTo().getURI().getParameter("rtsp");
		if(rtspParameter == null) {
			rtspParameter = request.getRequestURI().getParameter("rtsp");
		}
		if(rtspParameter != null) {
			rtsp.uri = "rtsp://" + rtspParameter;
		}
		request.getSession().setAttribute("rtsp", rtsp);
		
		String callerSipPhoneDSP = new String(
				(byte[])request.getContent());
		
		// Extract the RTP video port number from the SDP
		String videoSdp = callerSipPhoneDSP.substring(
				callerSipPhoneDSP.indexOf("video ") + "video ".length());
		String videoPort = videoSdp.split(" ")[0];
		Integer videoPortInt = Integer.parseInt(videoPort);
		videoPort = videoPortInt.toString() + "-" + (videoPortInt+1);
		
		// Extract the RTP audio port number from the SDP
		String audioSdp = callerSipPhoneDSP.substring(
				callerSipPhoneDSP.indexOf("audio ") + "audio ".length());
		String audioPort = audioSdp.split(" ")[0];
		Integer audioPortInt = Integer.parseInt(audioPort);
		audioPort = audioPortInt.toString() + "-" + (audioPortInt+1);
		
		// Format the port information in RTSP SDP way ("RTPPORT-RTCPPORT")
		rtsp.audioPort = audioPortInt + "-" + (audioPortInt+1);
		rtsp.videoPort = videoPortInt + "-" + (videoPortInt+1);
		
		// Make the caller hear the ringback tone
		SipServletResponse sipServletResponse = request.createResponse(
				SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		
		if(logger.isInfoEnabled()) {
			logger.info("Extracted audio and video ports: " + audioPort + "/" + videoPort);
		}
		rtsp.init();
		
		// Wait until we receive the SDP from the RTSP server
		try{
			for(int q=0;q<5000;) {
				Thread.sleep(q+=200);
				if(rtsp.sdp != null) break;
			}
		} catch (Exception e) {
			throw new RuntimeException("Problem retrieving SDP from RTSP server", e);
		}
		sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		/*
		//rtsp.sdp.replaceAll("a=rtpmap:99", "a=rtpmap:55");
		rtsp.sdp = rtsp.sdp.replaceAll("99","9");
		rtsp.sdp = rtsp.sdp.replaceAll("98","115");
		rtsp.sdp = rtsp.sdp.replaceAll("a=rtpmap:9 AMR/8000/1","a=rtpmap:9 G722/8000");
		rtsp.sdp = rtsp.sdp.replaceAll("a=rtpmap:98 H263-2000/90000","a=rtpmap:115 H263-1998/90000");
		*/
		sipServletResponse.setContent(rtsp.sdp, "application/sdp");
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		if(logger.isInfoEnabled()) {
			logger.info("Got BYE request:\n" + request);
		}
		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
		RTSPStack rtsp = (RTSPStack) request.getSession().getAttribute("rtsp");
		try {
			rtsp.sendTeardown();
			Thread.sleep(1000);
			rtsp.destroy();
		} catch (Exception e) {
			logger.error("Problem cleaning up RTSP stack", e);
		}
		
	}
}