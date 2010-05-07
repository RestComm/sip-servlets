package org.mobicents.servlet.sip.conference.server.media;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.networkconnection.NetworkConnection;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PhoneConferenceParticipant extends ConferenceParticipant {
	private static Log logger = LogFactory.getLog(PhoneConferenceParticipant.class);
	
	private MediaGroup mg;
	private NetworkConnection nc;
	private MediaSession session;
	private SipServletMessage message;
	
	public PhoneConferenceParticipant(String name, NetworkConnection nc, MediaSession session, SipServletMessage message) {
		this.nc = nc;
		this.session = session;
		this.name = name;
		this.message = message;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#getEndpoint()
	 */
	public MediaGroup getMediaGroup() {
		return mg;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#getSession()
	 */
	public MediaSession getSession() {
		return session;
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#join(org.mobicents.servlet.sip.conference.Conference)
	 */
	@SuppressWarnings("serial")
	public void join(final Conference conference) {
		join(conference, Direction.DUPLEX);
	}
	
	private void join(final Conference conference, Direction direction) {
		//provider.addNotificationListener(this);
		try {
			nc.joinInitiate(direction, conference.getMixer(), null);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#leave(org.mobicents.servlet.sip.conference.Conference)
	 */
	public void leave(Conference conference) {
		try {
			nc.release();
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void kick(Conference conference) {
		leave(conference);
		try {
			message.getSession().createRequest("BYE").send();
		} catch (IOException e) {
			logger.error("Error", e);
		}
		
	}

	@Override
	public void mute(Conference conference) {
		try {
			nc.joinInitiate(Direction.RECV, conference.getMixer(), null);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
		muted = true;
	}

	@Override
	public void unmute(Conference conference) {
		try {
			nc.joinInitiate(Direction.DUPLEX, conference.getMixer(), null);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
		muted = false;
	}

}
