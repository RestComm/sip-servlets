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
package org.jboss.mobicents.seam.listeners;

import java.io.IOException;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.impl.common.events.EventID;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.MsResourceListener;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class MediaResourceListener implements MsResourceListener {
	private static Log logger = LogFactory.getLog(MediaResourceListener.class);
	private SipSession session;
	private MsConnection connection;
	
	/**
	 * @param session
	 * @param connection
	 */
	public MediaResourceListener(SipSession session, MsConnection connection) {
		super();
		this.session = session;
		this.connection = connection;
	}

	/* (non-Javadoc)
	 * @see org.mobicents.mscontrol.MsResourceListener#update(org.mobicents.mscontrol.MsNotifyEvent)
	 */	
	public void update(MsNotifyEvent event) {
		logger.info("event ID " + event.getEventID());
		logger.info("event cause " + event.getCause());
		logger.info("event message " + event.getMessage());
		//  
		if(session != null && connection != null && event.getEventID() == EventID.COMPLETE) {						
			try {
				SipServletRequest byeRequest = session.createRequest("BYE");				
				byeRequest.send();																	
			} catch (IOException e) {
				logger.error("Unexpected error while sending the BYE request", e);				
			}
			connection.release();			
		}
	}

	public void resourceCreated(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void resourceInvalid(MsNotifyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
