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
package org.mobicents.servlet.sip.monitoring.util;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsNotificationListener;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class MediaResourceListener implements MsNotificationListener {
	private static Logger logger = Logger.getLogger(MediaResourceListener.class);
	private SipSession session;
	private MsLink link;
	private MsConnection connection;
	
	/**
	 * @param session
	 * @param connection 
	 * @param connection
	 */
	public MediaResourceListener(SipSession session, MsLink link, MsConnection connection) {
		super();
		this.session = session;
		this.link = link;
		this.connection = connection;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.mscontrol.MsResourceListener#update(org.mobicents.mscontrol.MsNotifyEvent)
	 */	
	public void update(MsNotifyEvent event) {
		logger.info("resource updated : event FQN " + event.getEventID().getFqn());
		if(session != null && link != null && event.getEventID().equals(MsAnnouncement.COMPLETED)) {						
			try {
				SipServletRequest byeRequest = session.createRequest("BYE");				
				byeRequest.send();																	
			} catch (IOException e) {
				logger.error("Unexpected error while sending the BYE request", e);				
			}
			link.release();		
			connection.release();
		}
	}

	public void resourceCreated(MsNotifyEvent event) {
		logger.info("resource created : event FQN " + event.getEventID().getFqn());
	}

	public void resourceInvalid(MsNotifyEvent event) {
		logger.info("resource invalid : event FQN " + event.getEventID().getFqn());
	}
}
