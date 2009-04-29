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
import javax.media.mscontrol.mediagroup.signals.SignalDetector;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.media.mscontrol.resource.Error;
import javax.media.mscontrol.resource.MediaEventListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;

/**
 * 
 * @author amit bhayani
 * 
 */
public class PromptAndCollectServlet extends PlayerServlet {

	private static Logger logger = Logger
			.getLogger(PromptAndCollectServlet.class);

	private final static String WELCOME_MSG = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/welcome.wav";
	private final static String DTMF_0 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf0.wav";
	private final static String DTMF_1 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf1.wav";
	private final static String DTMF_2 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf2.wav";
	private final static String DTMF_3 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf3.wav";
	private final static String DTMF_4 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf4.wav";
	private final static String DTMF_5 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf5.wav";
	private final static String DTMF_6 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf6.wav";
	private final static String DTMF_7 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf7.wav";
	private final static String DTMF_8 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf8.wav";
	private final static String DTMF_9 = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/dtmf9.wav";
	private final static String STAR = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/star.wav";
	private final static String POUND = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/pound.wav";
	private final static String A = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/A.wav";
	private final static String B = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/B.wav";
	private final static String C = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/C.wav";
	private final static String D = "http://"
			+ System.getProperty("jboss.bind.address", "127.0.0.1")
			+ ":8080/media-jsr309-servlet/audio/D.wav";

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
					.createContainer(MediaGroupConfig.c_PlayerSignalDetector);
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

			} else {
				logger.error("Joining of MG and NC failed");
			}
		}

	}

	private class PlayerListener implements MediaEventListener<PlayerEvent> {

		public void onEvent(PlayerEvent event) {

			Player player = event.getSource();
			MediaGroup mg = player.getContainer();
			if (Error.e_OK.equals(event.getError())
					&& Player.ev_PlayComplete.equals(event.getEventType())) {
				try {
					SignalDetector sg = mg.getSignalDetector();
					sg.addListener(new SignalDetectorListener());
					sg.receiveSignals(1, null, null, null);
				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				logger.error("Player didn't complete successfully ");
			}

		}

	}

	class SignalDetectorListener implements
			MediaEventListener<SignalDetectorEvent> {

		public void onEvent(SignalDetectorEvent event) {
			MediaGroup mg = (MediaGroup) event.getSource().getContainer();
			if (Error.e_OK.equals(event.getError())
					&& SignalDetector.ev_ReceiveSignals.equals(event
							.getEventType())) {
				String seq = event.getSignalString();

				URI prompt = null;
				try {
					Player player = mg.getPlayer();

					if (seq.equals("0")) {
						prompt = URI.create(DTMF_0);
					} else if (seq.equals("1")) {
						prompt = URI.create(DTMF_1);
					} else if (seq.equals("2")) {
						prompt = URI.create(DTMF_2);
					} else if (seq.equals("3")) {
						prompt = URI.create(DTMF_3);
					} else if (seq.equals("4")) {
						prompt = URI.create(DTMF_4);
					} else if (seq.equals("5")) {
						prompt = URI.create(DTMF_5);
					} else if (seq.equals("6")) {
						prompt = URI.create(DTMF_6);
					} else if (seq.equals("7")) {
						prompt = URI.create(DTMF_7);
					} else if (seq.equals("8")) {
						prompt = URI.create(DTMF_8);
					} else if (seq.equals("9")) {
						prompt = URI.create(DTMF_9);
					} else if (seq.equals("#")) {
						prompt = URI.create(POUND);
					} else if (seq.equals("*")) {
						prompt = URI.create(STAR);
					} else if (seq.equals("A")) {
						prompt = URI.create(A);
					} else if (seq.equals("B")) {
						prompt = URI.create(B);
					} else if (seq.equals("C")) {
						prompt = URI.create(C);
					} else if (seq.equals("D")) {
						prompt = URI.create(D);
					} else {
						logger.error("This DigitMap is not recognized " + seq);
						return;
					}

					player.play(prompt, null, null);

				} catch (MsControlException e) {
					e.printStackTrace();
				}

			} else {
				logger.error("DTMF detection failed ");
			}

		}

	}

}
