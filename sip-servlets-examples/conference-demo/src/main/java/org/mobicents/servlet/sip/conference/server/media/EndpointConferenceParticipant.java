package org.mobicents.servlet.sip.conference.server.media;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkListener;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsSession;


public class EndpointConferenceParticipant extends ConferenceParticipant {
	private static Log logger = LogFactory.getLog(EndpointConferenceParticipant.class);
	
	private MsEndpoint endpoint;
	private MsSession session;
	private MsConnection uaPrConnection;
	private SipServletMessage message;
	private ConcurrentHashMap<Conference, MsLink> links = new ConcurrentHashMap<Conference, MsLink>();
	
	public EndpointConferenceParticipant(String name, MsEndpoint endpoint, MsSession session, SipServletMessage message, MsConnection connection) {
		this.endpoint = endpoint;
		this.session = session;
		this.name = name;
		this.message = message;
		this.uaPrConnection = connection;
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
	public void join(final Conference conference) {
		join(conference, MsLinkMode.FULL_DUPLEX);
	}
	
	private void join(final Conference conference, MsLinkMode mode) {
		MsEndpoint endpoint = getEndpoint();
		//provider.addNotificationListener(this);
		final MsSession session = getSession();
		final MsLink link = session.createLink(mode);
		link.addLinkListener(new MsLinkListener() {
			public void linkCreated(MsLinkEvent evt) {
				logger.info("link created " + evt);
			}

			public void linkConnected(MsLinkEvent evt) {
				logger.info("PR-CONF link connected " +link.getEndpoints()[0].getLocalName() +
						" " + link.getEndpoints()[1].getLocalName());
				conference.setConferenceEndpoint(link.getEndpoints()[0]);
				//AnnouncementConferenceParticipant.playOnLink(session, link, "/home/vralev/control/mobicents/servers/media/examples/mms-demo/web/src/main/webapp/audio/cuckoo.wav", 0);
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
		
		String log = "Linking " + endpoint.getLocalName() + " to CONF" + conference.getKey();
		logger.info(log);
		
		String confEndpoint = conference.getConferenceEndpointName();
		link.join(confEndpoint, endpoint.getLocalName());
		links.put(conference, link);
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#leave(org.mobicents.servlet.sip.conference.Conference)
	 */
	public void leave(Conference conference) {
		try {
			links.get(conference).release();
			uaPrConnection.release();
		} catch (Exception e) {
			logger.error(e);
		}
		links.remove(conference);
	}

	@Override
	public void kick(Conference conference) {
		leave(conference);
		try {
			message.getSession().createRequest("BYE").send();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void mute(Conference conference) {
		links.get(conference).setMode(MsLinkMode.HALF_DUPLEX);
		muted = true;
	}

	@Override
	public void unmute(Conference conference) {
		links.get(conference).setMode(MsLinkMode.FULL_DUPLEX);
		muted = false;
	}

}
