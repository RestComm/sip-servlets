package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.net.URI;

import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.JoinEvent;
import javax.media.mscontrol.join.JoinEventListener;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.Player;
import javax.media.mscontrol.mediagroup.PlayerEvent;
import javax.media.mscontrol.mediagroup.Recorder;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerListener;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;

/**
 * 
 * @author amit bhayani
 * 
 */
public class PromptAndRecordServlet extends PlayerServlet implements
		TimerListener {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger
			.getLogger(PromptAndRecordServlet.class);

	private final static String WELCOME_MSG = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/record_welcome.wav";

	private static final int RECORDING_DELAY = 30000;

	private final static String RECORDER = "test.wav";

	// private final String RECORDED_FILE = "file://" +
	// System.getProperty("jboss.server.data.dir") + "/" + RECORDER;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	@Override
	protected void doAck(SipServletRequest req) throws ServletException,
			IOException {
		SipSession sipSession = req.getSession();

		MediaSession ms = (MediaSession) sipSession
				.getAttribute("MEDIA_SESSION");
		try {
			MediaGroup mg = ms
					.createMediaGroup(MediaGroup.PLAYER_RECORDER_SIGNALDETECTOR);
			mg.addListener(new MyJoinEventListener());

			NetworkConnection nc = (NetworkConnection) sipSession
					.getAttribute("NETWORK_CONNECTION");
			mg.joinInitiate(Direction.DUPLEX, nc, this);

		} catch (MsControlException e) {
			logger.error(e);
			// Clean up media session
			terminate(sipSession, ms);
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		MediaGroup mediaGroup = (MediaGroup) request.getSession().getAttribute(
				"MEDIA_GROUP");
		if (mediaGroup != null) {
			logger.info("Bye received, stopping the recording");
			try {
				mediaGroup.getRecorder().stop();
			} catch (MsControlException e) {
				logger.info("recording couldn't be stopped", e);
			}
		}
		super.doBye(request);
	}

	public void timeout(ServletTimer servletTimer) {

		String sessionId = (String) servletTimer.getInfo();
		logger.info("Timer fired on sip session " + sessionId);
		SipSession sipSession = servletTimer.getApplicationSession()
				.getSipSession(sessionId);

		if (sipSession != null) {
			MediaGroup mediaGroup = (MediaGroup) sipSession
					.getAttribute("MEDIA_GROUP");
			if (mediaGroup != null) {
				logger.info("Timer fired, stopping the recording");
				try {
					mediaGroup.getRecorder().stop();
				} catch (MsControlException e) {
					logger.info("recording couldn't be stopped", e);
				}
			}
		} else {
			logger
					.info("the session has not been found, it may have been already invalidated");
		}
	}

	private class MyJoinEventListener implements JoinEventListener {

		public void onEvent(javax.media.mscontrol.join.JoinEvent event) {

			MediaGroup mg = (MediaGroup) event.getThisJoinable();
			if (event.isSuccessful()) {

				if (JoinEvent.JOINED == event.getEventType()) {
					// NC Joined to MG

					try {
						Player player = mg.getPlayer();
						player.addListener(new PlayerListener());

						URI prompt = URI.create(WELCOME_MSG);

						player.play(prompt, null, null);

					} catch (MsControlException e) {
						logger.error(e);
					}
				} else if (JoinEvent.UNJOINED == event.getEventType()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Un-Joined MG and NC");
					}
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
				if (event.isSuccessful()
						&& (PlayerEvent.PLAY_COMPLETED == event.getEventType())) {

					MediaSession mediaSession = event.getSource()
							.getMediaSession();

					SipSession sipSession = (SipSession) mediaSession
							.getAttribute("SIP_SESSION");

					sipSession.setAttribute("MEDIA_GROUP", mg);

					SipApplicationSession sipAppSession = sipSession
							.getApplicationSession();

					try {
						Recorder recoredr = mg.getRecorder();
						logger.info("recording the user at " + RECORDER);
						URI prompt = URI.create(RECORDER);

						recoredr.record(prompt, null, null);

						TimerService timer = (TimerService) getServletContext()
								.getAttribute(TIMER_SERVICE);
						timer.createTimer(sipAppSession, RECORDING_DELAY,
								false, sipSession.getId());

					} catch (MsControlException e) {
						logger.error("An unexpected error happened ", e);
					}
				} else {
					logger.error("Player didn't complete successfully ");
				}
			}

		}

	}
}
