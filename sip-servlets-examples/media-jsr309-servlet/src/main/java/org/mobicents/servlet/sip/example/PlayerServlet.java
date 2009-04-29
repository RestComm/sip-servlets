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
import java.util.Properties;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.networkconnection.NetworkConnectionConfig;
import javax.media.mscontrol.networkconnection.NetworkConnectionEvent;
import javax.media.mscontrol.resource.Error;
import javax.media.mscontrol.resource.MediaEventListener;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.apache.log4j.Logger;

/**
 * This example shows a simple usage of JSR 309.
 * 
 * @author amit bhayani
 * 
 */
public class PlayerServlet extends SipServlet {
	private static Logger logger = Logger.getLogger(PlayerServlet.class);

	protected MsControlFactory msControlFactory;

	/**
	 * The JSR309 Impl is over the MGCP Stack and hence IP Address/Port of MGCP
	 * Stack of Call Agent (CA) and Media Gateway (MGW) needs to be passed to
	 * get instance of MsControlFactory
	 * 
	 * Each Sip Servlet Application can have it's own unique MGCP Stack by
	 * providing different MGCP_STACK_NAME
	 */

	// Property key for the Unique MGCP stack name for this application
	public static final String MGCP_STACK_NAME = "mgcp.stack.name";

	// Property key for the IP address where CA MGCP Stack (SIP Servlet
	// Container) is bound
	public static final String MGCP_STACK_IP = "mgcp.stack.ip";

	// Property key for the port where CA MGCP Stack is bound
	public static final String MGCP_STACK_PORT = "mgcp.stack.port";

	// Property key for the IP address where MGW MGCP Stack (MMS) is bound
	public static final String MGCP_PEER_IP = "mgcp.stack.peer.ip";

	// Property key for the port where MGW MGCP Stack is bound
	public static final String MGCP_PEER_PORT = "mgcp.stack.peer.port";

	/**
	 * In this case MGW and CA are on same local host
	 */
	public static final String LOCAL_ADDRESS = "127.0.0.1";
	protected static final int CA_PORT = 2727;

	public static final String PEER_ADDRESS = "127.0.0.1";
	protected static final int MGW_PORT = 2427;

	public PlayerServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println("the simple sip servlet has been started");
		super.init(servletConfig);

		Properties property = new Properties();
		property.put(MGCP_STACK_NAME, "SipServlets");
		property.put(MGCP_PEER_IP, PEER_ADDRESS);
		property.put(MGCP_PEER_PORT, MGW_PORT);

		property.put(MGCP_STACK_IP, LOCAL_ADDRESS);
		property.put(MGCP_STACK_PORT, CA_PORT);

		try {
			// create the Media Session Factory
			msControlFactory = DriverManager.getDrivers().next().getFactory(
					property);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got request:\n"
				+ request.getMethod());
		SipServletResponse sipServletResponse = request
				.createResponse(SipServletResponse.SC_RINGING);
		sipServletResponse.send();

		SipSession sipSession = request.getSession();

		try {
			String user = request.getFrom().getDisplayName();
			if (user == null && request.getFrom().getURI() instanceof SipURI) {
				user = ((SipURI) request.getFrom().getURI()).getUser();
			}

			// Create new media session and store in SipSession
			MediaSession mediaSession = msControlFactory.createMediaSession();

			sipSession.setAttribute("MEDIA_SESSION", mediaSession);
			mediaSession.setAttribute("SIP_SESSION", sipSession);

			// Store INVITE so it can be responded to later
			sipSession.setAttribute("UNANSWERED_INVITE", request);

			// Create a new NetworkConnection and store in SipSession
			NetworkConnection conn = mediaSession
					.createContainer(NetworkConnectionConfig.c_Basic);

			NetworkConnectionListener ncListener = new NetworkConnectionListener();

			conn.addListener(ncListener);

			conn.modify(NetworkConnection.CHOOSE, request.getRawContent());

		} catch (MsControlException e) {
			logger.error(e);
			request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR)
					.send();
		}

	}

	private static String getBasename(String path) {
		int index = path.lastIndexOf(".");
		if (index == -1) {
			return path;
		} else {
			return path.substring(0, index);
		}
	}

	private static String getExtension(String path) {
		int index = path.lastIndexOf(".");
		if (index == -1) {
			return null;
		} else {
			return path.substring(index + 1);
		}
	}

	protected void terminate(SipSession sipSession, MediaSession mediaSession) {
		SipServletRequest bye = sipSession.createRequest("BYE");
		try {
			bye.send();
			// Clean up media session
			mediaSession.release();
			sipSession.removeAttribute("MEDIA_SESSION");
		} catch (Exception e1) {
			log("Terminating: Cannot send BYE: " + e1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got BYE request:\n" + request);
		
		MediaSession mediaSession = (MediaSession) request.getSession().getAttribute("MEDIA_SESSION");
		mediaSession.release();
		
		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
		// releasing the media connection

	}

	private class NetworkConnectionListener implements
			MediaEventListener<NetworkConnectionEvent> {

		public void onEvent(NetworkConnectionEvent event) {

			NetworkConnection conn = event.getSource();
			MediaSession mediaSession = event.getSource().getMediaSession();

			SipSession sipSession = (SipSession) mediaSession
					.getAttribute("SIP_SESSION");

			SipServletRequest inv = (SipServletRequest) sipSession
					.getAttribute("UNANSWERED_INVITE");
			sipSession.removeAttribute("UNANSWERED_INVITE");

			if (Error.e_OK.equals(event.getError())
					&& NetworkConnection.ev_Modify.equals(event.getEventType())) {
				SipServletResponse resp = inv
						.createResponse(SipServletResponse.SC_OK);
				try {
					byte[] sdp = conn.getLocalSessionDescription();

					resp.setContent(sdp, "application/sdp");
					// Send 200 OK
					resp.send();

					sipSession.setAttribute("NETWORK_CONNECTION", conn);

				} catch (Exception e) {
					logger.error(e);

					// Clean up
					sipSession.getApplicationSession().invalidate();
					mediaSession.release();
				}
			} else {
				try {
					if (NetworkConnection.e_ModifyOffersRejected.equals(event
							.getError())) {
						// Send 488 error response to INVITE
						inv.createResponse(
								SipServletResponse.SC_NOT_ACCEPTABLE_HERE)
								.send();
					} else if (NetworkConnection.e_ResourceNotAvailable
							.equals(event.getError())) {
						// Send 486 error response to INVITE
						inv.createResponse(SipServletResponse.SC_BUSY_HERE)
								.send();
					} else {
						// Some unknown error. Send 500 error response to INVITE
						inv.createResponse(
								SipServletResponse.SC_SERVER_INTERNAL_ERROR)
								.send();
					}
					// Clean up media session
					sipSession.removeAttribute("MEDIA_SESSION");
					mediaSession.release();
				} catch (Exception e) {
					logger.error(e);

					// Clean up
					sipSession.getApplicationSession().invalidate();
					mediaSession.release();
				}
			}
		}

	}
}