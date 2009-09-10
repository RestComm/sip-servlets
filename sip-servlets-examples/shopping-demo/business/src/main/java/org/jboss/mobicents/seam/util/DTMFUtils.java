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
package org.jboss.mobicents.seam.util;

import java.io.File;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.actions.OrderManager;
import org.jboss.mobicents.seam.listeners.DTMFListener;
import org.jboss.mobicents.seam.listeners.MediaResourceListener;
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
	
	public static void adminApproval(SipSession session, String signal, String pathToAudioDirectory) {			
		
		if("1".equalsIgnoreCase(signal)) {
			// Order Approved
			logger.info("Order approved !");
			String audioFile = pathToAudioDirectory + "OrderApproved.wav";					
			
			playFileInResponseToDTMFInfo(session, audioFile);					
		} else if("2".equalsIgnoreCase(signal)) {
			// Order Rejected
			logger.info("Order rejected !");
			String audioFile = pathToAudioDirectory + "OrderCancelled.wav";					
			
			playFileInResponseToDTMFInfo(session, audioFile);
		}
	}

	public static void orderApproval(SipSession session, String signal, String pathToAudioDirectory) {
		long orderId = (Long) session.getAttribute("orderId");
		
		if("2".equalsIgnoreCase(signal)) {
			// Order cancelled
			logger.info("Order " + orderId + " cancelled !");
			String audioFile = pathToAudioDirectory + "OrderCancelled.wav";					
			
			playFileInResponseToDTMFInfo(session, audioFile);
			try {
				InitialContext ctx = new InitialContext();
				OrderManager orderManager = (OrderManager) ctx.lookup("shopping-demo/OrderManagerBean/remote");
				orderManager.cancelOrder(orderId);
			} catch (NamingException e) {
				logger.error("An exception occured while retrieving the EJB OrderManager",e);
			}
		} else {
			// Order Confirmed
			logger.info("Order " + orderId + " confirmed !");
			String audioFile = pathToAudioDirectory + "OrderApproved.wav";					
			
			playFileInResponseToDTMFInfo(session, audioFile);
			try {
				InitialContext ctx = new InitialContext();
				OrderManager orderManager = (OrderManager) ctx.lookup("shopping-demo/OrderManagerBean/remote");
				orderManager.confirmOrder(orderId);
			} catch (NamingException e) {
				logger.error("An exception occured while retrieving the EJB OrderManager",e);
			}					
		}
	}

	public static boolean updateDeliveryDate(SipSession session, String signal) {
		int cause = -1;
		try {
			cause = Integer.parseInt(signal);
		} catch (java.lang.NumberFormatException e) {
			return false;
		}
		MsLink link = (MsLink)session.getAttribute("link");
		synchronized(session) {
			String dateAndTime = (String) session.getAttribute("dateAndTime");
			if(dateAndTime == null) {
				dateAndTime = "";
			}
	
			switch (cause) {
			case 0:
				dateAndTime = dateAndTime + "0";
				break;
			case 1:
				dateAndTime = dateAndTime + "1";
				break;
			case 2:
				dateAndTime = dateAndTime + "2";
				break;
			case 3:
				dateAndTime = dateAndTime + "3";
				break;
			case 4:
				dateAndTime = dateAndTime + "4";
				break;
			case 5:
				dateAndTime = dateAndTime + "5";
				break;
			case 6:
				dateAndTime = dateAndTime + "6";
				break;
			case 7:
				dateAndTime = dateAndTime + "7";
				break;
			case 8:
				dateAndTime = dateAndTime + "8";
				break;
			case 9:
				dateAndTime = dateAndTime + "9";
				break;
			default:
				break;
			}
	
			// TODO: Add logic to check if date and time is valid. We assume that
			// user is well educated and will always punch right date and time
	
			if (dateAndTime.length() == 10) {			
				
				char[] c = dateAndTime.toCharArray();
	
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer.append("You have selected delivery date to be ");
	
				String date = "" + c[0] + c[1];
				int iDate = (new Integer(date)).intValue();
				stringBuffer.append(iDate);
	
				String month = "" + c[2] + c[3];
				int iMonth = (new Integer(month)).intValue();
	
				String year = "" + c[4] + c[5];
				int iYear = (new Integer(year)).intValue();
	
				String hour = "" + c[6] + c[7];
				int iHour = (new Integer(hour)).intValue();
	
				String min = "" + c[8] + c[9];
				int iMin = (new Integer(min)).intValue();
	
				switch (iMonth) {
				case 1:
					month = "January";
					break;
				case 2:
					month = "February";
					break;
				case 3:
					month = "March";
					break;
				case 4:
					month = "April";
					break;
				case 5:
					month = "May";
					break;
				case 6:
					month = "June";
					break;
				case 7:
					month = "July";
					break;
				case 8:
					month = "August";
					break;
				case 9:
					month = "September";
					break;
				case 10:
					month = "October";
					break;
				case 11:
					month = "November";
					break;
				case 12:
					month = "December";
					break;
				default:
					break;
				}
				stringBuffer.append(" of ");
				stringBuffer.append(month);
				stringBuffer.append(" ");
				stringBuffer.append(2000 + iYear);
				stringBuffer.append(" at ");
				stringBuffer.append(iHour);
				stringBuffer.append(" hour and ");
				stringBuffer.append(iMin);
				stringBuffer.append(" minute. Thank you. Bye.");
	
				java.sql.Timestamp timeStamp = new java.sql.Timestamp(
						(iYear + 100), iMonth - 1, iDate, iHour, iMin, 0, 0);
				
				try {
					InitialContext ctx = new InitialContext();
					OrderManager orderManager = (OrderManager) ctx.lookup("shopping-demo/OrderManagerBean/remote");
					orderManager.setDeliveryDate(session.getAttribute("orderId"), timeStamp);
				} catch (NamingException e) {
					logger.error("An exception occured while retrieving the EJB OrderManager",e);
				}				
				logger.info(stringBuffer.toString());
				try {
					TTSUtils.buildAudio(stringBuffer.toString(), "deliveryDate.wav");
					MsEndpoint endpoint = link.getEndpoints()[0];
					MsEventFactory eventFactory = link.getSession()
							.getProvider().getEventFactory();
					java.io.File speech = new File("deliveryDate.wav");
					logger.info("Playing delivery date summary : " + "file://" + speech.getAbsolutePath());
					MediaResourceListener mediaResourceListener = new MediaResourceListener(session, link, (MsConnection)session.getAttribute("connection"));
					link.addNotificationListener(mediaResourceListener);

					// Let us request for Announcement Complete event or Failure
					// in case if it happens
					MsRequestedEvent onCompleted = eventFactory
							.createRequestedEvent(MsAnnouncement.COMPLETED);
					onCompleted.setEventAction(MsEventAction.NOTIFY);

					MsRequestedEvent onFailed = eventFactory
							.createRequestedEvent(MsAnnouncement.FAILED);
					onFailed.setEventAction(MsEventAction.NOTIFY);

					MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
			        play.setURL("file://"+ speech.getAbsolutePath());
					
					MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
			        MsRequestedEvent[] requestedEvents = new MsRequestedEvent[] { onCompleted, onFailed };
										
			        endpoint.execute(requestedSignals, requestedEvents, link);
					logger.info("delivery Date summary played. not waiting for DTMF anymore");
					return true;
				} catch (Exception e) {
					logger.error("An unexpected exception occured while generating the deliveryDate tts file");
					return true;
				}							
			} else {
				session.setAttribute("dateAndTime", dateAndTime);
				return false;
			}
		}	
	}
	
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
}
