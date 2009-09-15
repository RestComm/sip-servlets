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
package org.mobicents.servlet.sip.conference.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.servlet.sip.conference.server.media.ConferenceCenter;

/*
 * This example shows a simple User agent that can playback audio.
 * @author Vladimir Ralev
 *
 */
@javax.servlet.sip.annotation.SipServlet
public class ConferenceServlet extends SipServlet {
	private static Log logger = LogFactory.getLog(ConferenceServlet.class);
	private static final long serialVersionUID = 1L;
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";
	
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
			MsProvider provider = ConferenceCenter.getInstance().getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection(PR_JNDI_NAME);
			ConferenceConnectionListener listener = new ConferenceConnectionListener(request);
			connection.addConnectionListener(listener);
			connection.modify("$", sdp);

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
				String sdp = new String(sdpBytes); 
				try {
					MsProvider provider = ConferenceCenter.getInstance().getProvider();
					MsSession session = provider.createSession();
					MsConnection connection = session.createNetworkConnection(PR_JNDI_NAME);
					ConferenceConnectionListener listener = 
						new ConferenceConnectionListener(response);
					connection.addConnectionListener(listener);
					connection.modify("$", sdp);

				} catch (Exception e) {
					logger.error(e);
				}

			}
		}
		super.doResponse(response);
	}
}