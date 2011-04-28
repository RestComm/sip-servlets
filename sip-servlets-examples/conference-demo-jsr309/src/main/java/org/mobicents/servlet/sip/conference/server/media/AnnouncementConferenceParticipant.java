/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

package org.mobicents.servlet.sip.conference.server.media;

import java.net.URI;

import javax.media.mscontrol.MediaSession;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.join.Joinable.Direction;
import javax.media.mscontrol.mediagroup.MediaGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.conference.server.MsControlObjects;

public class AnnouncementConferenceParticipant extends ConferenceParticipant {
	private static Log logger = LogFactory.getLog(AnnouncementConferenceParticipant.class);
	private String url;
	private MediaGroup mg;
	private MediaSession session;
	
	public AnnouncementConferenceParticipant(String name, String url) {
		try {
			this.name = name;
			this.url = url;
			this.session = MsControlObjects.msControlFactory.createMediaSession();
			this.mg = this.session.createMediaGroup(MediaGroup.PLAYER);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
		
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
		try {
			Thread.sleep(800);
			mg.getPlayer().play(URI.create(url), null, null);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	
	private void join(final Conference conference, Direction direction) {
		//provider.addNotificationListener(this);
		try {
			mg.joinInitiate(direction, conference.getMixer(), null);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.conference.ConferenceLeg#leave(org.mobicents.servlet.sip.conference.Conference)
	 */
	public void leave(Conference conference) {
		try {
			mg.release();
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void kick(Conference conference) {
		leave(conference);
		
	}

	@Override
	public void mute(Conference conference) {
		try {
			mg.joinInitiate(Direction.RECV, conference.getMixer(), null);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
		muted = true;
	}

	@Override
	public void unmute(Conference conference) {
		try {
			mg.joinInitiate(Direction.DUPLEX, conference.getMixer(), null);
		} catch (MsControlException e) {
			logger.error("Error", e);
		}
		muted = false;
	}
}
