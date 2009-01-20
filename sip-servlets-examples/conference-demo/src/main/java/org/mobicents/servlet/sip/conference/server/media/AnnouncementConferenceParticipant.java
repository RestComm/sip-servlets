package org.mobicents.servlet.sip.conference.server.media;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkListener;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

public class AnnouncementConferenceParticipant extends ConferenceParticipant {
	private static Log logger = LogFactory.getLog(AnnouncementConferenceParticipant.class);

	public final static String ANNOUNCEMENT_ENDPOINT_QUERY = "media/trunk/Announcement/$";
	
	private MsEndpoint endpoint;
	private MsSession session;
	private String fileUrl;
	private ConcurrentHashMap<Conference, MsLink> links = new ConcurrentHashMap<Conference, MsLink>();
	
	public AnnouncementConferenceParticipant(String name, String fileUrl, MsSession session) {
		this.session = session;
		this.fileUrl = fileUrl;
		this.name = name;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#getEndpoint()
	 */
	public MsEndpoint getEndpoint() {
		return endpoint;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#getSession()
	 */
	public MsSession getSession() {
		return session;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#join(org.mobicents.servlet.sip.conference.Conference)
	 */
	@SuppressWarnings("serial")
	public void join(Conference conference) {
		join(conference, MsLinkMode.FULL_DUPLEX);
	}
	
	private void join(final Conference conference, MsLinkMode mode) {
		final MsSession session = getSession();
		MsLink link = session.createLink(mode);
		link.addLinkListener(new MsLinkListener() {
			public void linkCreated(MsLinkEvent evt) {
				logger.info("link created " + evt);
				conference.setConferenceEndpoint(evt.getSource().getEndpoints()[0]);
				endpoint = evt.getSource().getEndpoints()[1];
			}

			public void linkConnected(MsLinkEvent evt) {
				logger.info("CONF-ANN link connected " + evt.getSource().getEndpoints()[0].getLocalName()
						+ "   " + evt.getSource().getEndpoints()[1].getLocalName());
				playOnLink(session, evt.getSource(), fileUrl, 1);
			}

			public void linkDisconnected(MsLinkEvent evt) {
				logger.info("link disconnected " + evt);
			}

			public void linkFailed(MsLinkEvent evt) {
				logger.info("link failed " + evt);
			}

			public void modeFullDuplex(MsLinkEvent evt) {
				logger.info("link mode full duplex" + evt);
			}

			public void modeHalfDuplex(MsLinkEvent evt) {
				logger.info("link mode half duplex" + evt);
			}
		});
		
		String confEndpoint = conference.getConferenceEndpointName();
		logger.info("Linking " + confEndpoint + " to ANN "  + conference.getKey());
		link.join(confEndpoint, ANNOUNCEMENT_ENDPOINT_QUERY);
		links.put(conference, link);
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#leave(org.mobicents.servlet.sip.conference.Conference)
	 */
	public void leave(Conference conference) {
		links.get(conference).release();
		links.remove(conference);
	}
	
	public static void playOnLink(MsSession session, MsLink link, String url, int endpointSide) {
		MsEventFactory eventFactory = session.getProvider().getEventFactory();
		MsPlayRequestedSignal play = null;
		play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		if(url.startsWith("http") || url.startsWith("file:") || url.startsWith("jar:")) {
			play.setURL(url);
		} else {
			play.setURL("file://" + url);
		}
		MsRequestedEvent onCompleted = null;
		MsRequestedEvent onFailed = null;

		onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);

		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[]{play};
		MsRequestedEvent[] requestedEvents = new MsRequestedEvent[]{onCompleted, onFailed};
		link.getEndpoints()[endpointSide].execute(requestedSignals, requestedEvents, link);
	}

	@Override
	public void kick(Conference conference) {
		leave(conference);
		
	}

	@Override
	public void mute(Conference conference) {
		links.get(conference).setMode(MsLinkMode.HALF_DUPLEX);
		//leave(conference);
		//join(conference, MsLinkMode.HALF_DUPLEX);
		muted = true;
	}

	@Override
	public void unmute(Conference conference) {
		links.get(conference).setMode(MsLinkMode.FULL_DUPLEX);
		//leave(conference);
		//join(conference, MsLinkMode.HALF_DUPLEX);
		muted = false;
	}

}
