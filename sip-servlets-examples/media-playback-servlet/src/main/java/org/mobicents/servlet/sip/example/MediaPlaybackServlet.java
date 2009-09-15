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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
/**
 * This example shows a simple User agent that can playback audio.
 * @author Vladimir Ralev
 *
 */
public class MediaPlaybackServlet extends SipServlet {
	private static Logger logger = Logger.getLogger(MediaPlaybackServlet.class);
	private static final long serialVersionUID = 1L;
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";
	
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
		try {			
			String user = request.getFrom().getDisplayName();
			if(user == null && request.getFrom().getURI() instanceof SipURI) {
				user = ((SipURI)request.getFrom().getURI()).getUser();
			}
			buildAudio("Hey " + user +
					". This is Mobicents Sip Servlets.", "speech.wav");
			MsPeer peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection(PR_JNDI_NAME);
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(request);
			connection.addConnectionListener(listener);
			connection.modify("$", sdp);
			
			request.getSession().setAttribute("connection", connection);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void buildAudio(String text, String filename) throws Exception {
		VoiceManager mgr = VoiceManager.getInstance();
		Voice voice = mgr.getVoice("kevin");
		voice.allocate();
		File speech = new File(filename);
		SingleFileAudioPlayer player = new SingleFileAudioPlayer(getBasename(speech.getAbsolutePath()), getAudioType(filename));
		voice.setAudioPlayer(player);
		voice.startBatch();
		boolean ok = voice.speak(text);
		voice.endBatch();
		player.close();
		voice.deallocate();
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
	
	private static AudioFileFormat.Type getAudioType(String file) {
		AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
		String extension = getExtension(file);

		for (int i = 0; i < types.length; i++) {
			if (types[i].getExtension().equals(extension)) {
				return types[i];
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("MediaPlaybackServlet: Got BYE request:\n" + request);
		SipServletResponse sipServletResponse = request.createResponse(200);
		sipServletResponse.send();
		// releasing the media connection
		MsConnection connection = (MsConnection) sipServletResponse.getSession().getAttribute("connection");
		connection.release();
		MsLink link = (MsLink) sipServletResponse.getSession().getAttribute("link");
		link.release();
	}
}