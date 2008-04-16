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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsConnectionListener;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.MsSignalGenerator;
import org.mobicents.mscontrol.impl.MsProviderImpl;
import org.mobicents.mscontrol.signal.Announcement;

/**
 * This example shows a simple User agent that can playback audio.
 * @author Vladimir Ralev
 *
 */
public class MediaPlaybackServlet extends SipServlet {
	private static Log logger = LogFactory.getLog(MediaPlaybackServlet.class);
	
	
	public MediaPlaybackServlet() {
	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println("the simple sip servlet has been started");
		super.init(servletConfig);
	}
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
		inviteRequest = request;
	
		MsProviderImpl provider = new MsProviderImpl();
		MsSession session = provider.createSession();
		MsConnection connection = session.createNetworkConnection("media/trunk/Announcement/$");
		MediaConnectionListener listener = new MediaConnectionListener();
		listener.setInviteRequest(request);
		connection.addConnectionListener(listener);
		connection.modify("$", sdp);

	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got BYE request:\n" + request);
		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doResponse(SipServletResponse response)
			throws ServletException, IOException {

		logger.info("MediaPlaybackServlet: Got response:\n" + response);
		super.doResponse(response);
	}
}