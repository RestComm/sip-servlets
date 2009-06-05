package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.net.URI;

import javax.media.mscontrol.JoinEvent;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.StatusEvent;
import javax.media.mscontrol.StatusEventListener;
import javax.media.mscontrol.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.MediaGroupConfig;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.Error;
import javax.media.mscontrol.resource.MediaEventListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;
import org.mobicents.javax.media.mscontrol.MediaSessionImpl;

/**
 * 
 * @author amit bhayani
 * 
 */
public class PromptAndRecordServlet extends PlayerServlet implements
		TimerListener {

	private static Logger logger = Logger
			.getLogger(PromptAndRecordServlet.class);

	private final static String WELCOME_MSG = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/record_welcome.wav";

	private static final int RECORDING_DELAY = 30000;

	private final static String RECORDER = "test.wav";
	private final String RECORDED_FILE = "file://"
			+ System.getProperty("jboss.server.data.dir") + "/" + RECORDER;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	@Override
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		SipSession sipSession = req.getSession();

		MediaSessionImpl ms = (MediaSessionImpl) sipSession
				.getAttribute("MEDIA_SESSION");
		try {
			MediaGroup mg = ms
					.createMediaGroup(MediaGroupConfig.c_PlayerRecorderSignalDetector);
			mg.addListener(new MyStatusEventListener());

			NetworkConnection nc = (NetworkConnection) sipSession
					.getAttribute("NETWORK_CONNECTION");
			mg.joinInitiate(Direction.DUPLEX, nc, this);

		} catch (MsControlException e) {
			logger.error(e);
			// Clean up media session
			terminate(sipSession, ms);
		}
	}

	public void timeout(ServletTimer servletTimer) {

		SipSession sipSession = servletTimer.getApplicationSession()
				.getSipSession((String) servletTimer.getInfo());

		System.out.println("Timer fired");
		// sipSession.setAttribute("MEDIA_GROUP", mg);

	}

	private class MyStatusEventListener implements StatusEventListener {

		public void onEvent(StatusEvent event) {

			MediaGroup mg = (MediaGroup) event.getSource();
			if (event.getError().equals(Error.e_OK)
					&& JoinEvent.ev_Joined.equals(event.getEventType())) {
				// NC Joined to MG

				try {
					Player player = mg.getPlayer();
					player.addListener(new PlayerListener());

					URI prompt = URI.create(WELCOME_MSG);

					player.play(prompt, null, null);

				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (event.getError().equals(Error.e_OK)
					&& JoinEvent.ev_Unjoined.equals(event.getEventType())) {
				if (logger.isDebugEnabled()) {
					logger.debug("Un-Joined MG and NC");
				}
			} else {
				logger.error("Joining of MG and NC failed");
			}
		}

	}

	private class PlayerListener implements MediaEventListener<PlayerEvent> {

		public void onEvent(PlayerEvent event) {

			Player player = event.getSource();
			MediaGroup mg = player.getContainer();
			if (!isBye) {
				if (Error.e_OK.equals(event.getError())
						&& Player.ev_PlayComplete.equals(event.getEventType())) {

					MediaSession mediaSession = event.getSource()
							.getMediaSession();

					SipSession sipSession = (SipSession) mediaSession
							.getAttribute("SIP_SESSION");

					sipSession.setAttribute("MEDIA_GROUP", mg);

					SipApplicationSession sipAppSession = sipSession
							.getApplicationSession();

					try {
						Recorder recoredr = mg.getRecorder();
						URI prompt = URI.create(RECORDER);
						recoredr.record(prompt, null, null);

						// TODO : Set timer

						TimerService timer = (TimerService) getServletContext()
								.getAttribute(TIMER_SERVICE);
						timer.createTimer(sipAppSession, RECORDING_DELAY,
								false, sipSession.getId());

					} catch (MsControlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					logger.error("Player didn't complete successfully ");
				}
			}

		}

	}
}
