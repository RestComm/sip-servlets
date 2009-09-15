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

import java.io.InputStream;
import java.net.URL;

import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 *
 */
public class DTMFUtils {
	
	private static Logger logger = Logger.getLogger(DTMFUtils.class);		
	
	/**
	 * Make the media server play a file given in parameter
	 * and add a listener so that when the media server is done playing the call is tear down 
	 * @param session the sip session used to tear down the call
	 * @param audioFile the file to play
	 */
	public static void playFileInResponseToDTMFInfo(SipSession session,
			String audioFile) {
		logger.info("playing " + audioFile + " in response to DTMF");
		MsConnection connection = (MsConnection)session.getAttribute("connection");
		MsLink link = (MsLink)session.getAttribute("link");
		MsEndpoint endpoint = link.getEndpoints()[0];
		MsEventFactory eventFactory = connection.getSession().getProvider().getEventFactory();		
		MediaResourceListener mediaResourceListener = new MediaResourceListener(session, link, connection);
		link.addNotificationListener(mediaResourceListener);

		// Let us request for Announcement Complete event or Failure
		// in case if it happens
		MsRequestedEvent onCompleted = null;
		MsRequestedEvent onFailed = null;

		onCompleted = eventFactory
				.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		onFailed = eventFactory
				.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);

		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
        play.setURL(audioFile);
		
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed };
							
        endpoint.execute(requestedSignals, requestedEvents, link);
		session.setAttribute("DTMFSession", DTMFListener.DTMF_SESSION_STOPPED);
		logger.info("played " + audioFile + " in response to DTMF");
	}

	public static void answerBack(String alertId, String signal, String feedbackUrl) {
		logger.info("Sending signal " + signal + " for alertId " + alertId + " to the alerting application  on URL " + feedbackUrl);
		String finalUrl = feedbackUrl + "?alertId=" + alertId + "&signal=" + signal;
		
		try {
			URL url = new URL(finalUrl);
			InputStream in = url.openConnection().getInputStream();
	
			byte[] buffer = new byte[in.available()];
			int len = in.read(buffer);
			String httpResponse = "";
			for (int q = 0; q < len; q++)
				httpResponse += (char) buffer[q];
			logger.info("Received the follwing HTTP response: " + httpResponse);
		} catch(Exception e) {
			logger.error("couldn't connect to " +  finalUrl + " : " + e.getMessage());
		}
	}
}
