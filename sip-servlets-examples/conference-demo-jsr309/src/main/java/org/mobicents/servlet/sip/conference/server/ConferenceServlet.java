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

package org.mobicents.servlet.sip.conference.server;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.SdpPortManager;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.javax.media.mscontrol.spi.DriverImpl;
import org.mobicents.servlet.sip.conference.server.media.ConferenceCenter;

/*
 * This example shows a simple User agent that can playback audio.
 * @author Vladimir Ralev
 *
 */
@javax.servlet.sip.annotation.SipServlet
@javax.servlet.sip.annotation.SipListener
public class ConferenceServlet extends SipServlet implements SipServletListener {
	private static Log logger = LogFactory.getLog(ConferenceServlet.class);
	private static final long serialVersionUID = 1L;
	
	private static final String MS_CONTROL_FACTORY = "MsControlFactory";
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";
	
	// Property key for the Unique MGCP stack name for this application 
    public static final String MGCP_STACK_NAME = "mgcp.stack.name"; 
    // Property key for the IP address where CA MGCP Stack (SIP Servlet 
    // Container) is bound 
    public static final String MGCP_STACK_IP = "mgcp.server.address"; 
    // Property key for the port where CA MGCP Stack is bound 
    public static final String MGCP_STACK_PORT = "mgcp.local.port"; 
    // Property key for the IP address where MGW MGCP Stack (MMS) is bound 
    public static final String MGCP_PEER_IP = "mgcp.bind.address"; 
    // Property key for the port where MGW MGCP Stack is bound 
    public static final String MGCP_PEER_PORT = "mgcp.server.port"; 
	/**
	 * In this case MGW and CA are on same local host
	 */
	public static final String LOCAL_ADDRESS = System.getProperty(
			"jboss.bind.address", "127.0.0.1");
	protected static final String CA_PORT = "2921";

	public static final String PEER_ADDRESS = System.getProperty(
			"jboss.bind.address", "127.0.0.1");
	protected static final String MGW_PORT = "2427";
	
	private static MediaSession mediaSession;
	
	/**
	 * {@inheritDoc}
	 */
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got request:\n"
				+ request.getMethod());
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();
		Object sdpObj = request.getContent();
		byte[] sdpBytes = (byte[]) sdpObj;
		String sdp = new String(sdpBytes); 
		try {
			
			SipSession sipSession = request.getSession();
			MsControlObjects.msControlFactory = (MsControlFactory) getServletContext().getAttribute(MS_CONTROL_FACTORY);
//			MediaSession mediaSession = (MediaSession) MsControlObjects.msControlFactory
//			.createMediaSession();

			sipSession.setAttribute("MEDIA_SESSION", mediaSession);
			mediaSession.setAttribute("SIP_SESSION", sipSession);

			// Store INVITE so it can be responded to later
			sipSession.setAttribute("UNANSWERED_INVITE", request);

			// Create a new NetworkConnection and store in SipSession
			NetworkConnection conn = mediaSession
			.createNetworkConnection(NetworkConnection.BASIC);

			SdpPortManager sdpManag = conn.getSdpPortManager();

			ConferenceConnectionListener ncListener = new ConferenceConnectionListener(request);

			sdpManag.addListener(ncListener);

			byte[] sdpOffer = request.getRawContent();

			sdpManag.processSdpOffer(sdpOffer);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got BYE request:\n" + request);
		SipURI from = (SipURI) request.getFrom().getURI();
		SipURI to = (SipURI) request.getTo().getURI();
		ConferenceCenter.getInstance().getConference(to.getUser()).removeParticipant(from.toString());
		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		logger.info("Got response:\n" + response);
		if(response.getRequest().getMethod().equalsIgnoreCase("INVITE")) {
			if(response.getStatus() == 200) {
				Object sdpObj = response.getContent();
				byte[] sdpBytes = (byte[]) sdpObj;
				try {
					ConferenceConnectionListener listener = 
						new ConferenceConnectionListener(response);
//					MediaSession mediaSession = MsControlObjects.msControlFactory.createMediaSession();
					NetworkConnection conn = mediaSession
					.createNetworkConnection(NetworkConnection.BASIC);

					SdpPortManager sdpManag = conn.getSdpPortManager();
					sdpManag.addListener(listener);
					sdpManag.processSdpOffer(sdpBytes);
				} catch (Exception e) {
					logger.error(e);
				}

			}
		}
		super.doResponse(response);
	}
	
	public void contextInitialized(SipServletContextEvent event) {

	}
	
	Properties properties = null;
	
	public void contextDestroyed(ServletContextEvent event) {
		Iterator<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasNext()) {
			Driver driver = drivers.next();
			DriverManager.deregisterDriver(driver);
			DriverImpl impl = (DriverImpl) driver;
			impl.shutdown();
		}
	}

	public void servletInitialized(SipServletContextEvent event) {
		if(event.getServletContext().getAttribute(MS_CONTROL_FACTORY) == null) {
			DriverImpl d = new DriverImpl();
			properties = new Properties();
			properties.setProperty(MGCP_STACK_NAME, "SipServlets");
			properties.setProperty(MGCP_PEER_IP, PEER_ADDRESS);
			properties.setProperty(MGCP_PEER_PORT, MGW_PORT);
	
			properties.setProperty(MGCP_STACK_IP, LOCAL_ADDRESS);
			properties.setProperty(MGCP_STACK_PORT, CA_PORT);
	
			try {
				// create the Media Session Factory
				final MsControlFactory msControlFactory = new DriverImpl().getFactory(properties); 
				MsControlObjects.msControlFactory = msControlFactory;
				event.getServletContext().setAttribute(MS_CONTROL_FACTORY, msControlFactory);
				mediaSession = MsControlObjects.msControlFactory.createMediaSession();
				logger.info("started MGCP Stack on " + LOCAL_ADDRESS + "and port " + CA_PORT + " obj: " + MsControlObjects.msControlFactory);
			} catch (Exception e) {
				logger.error("couldn't start the underlying MGCP Stack", e);
			}
		} else {
			logger.info("MGCP Stack already started on " + LOCAL_ADDRESS + "and port " + CA_PORT);
		}
	}

}