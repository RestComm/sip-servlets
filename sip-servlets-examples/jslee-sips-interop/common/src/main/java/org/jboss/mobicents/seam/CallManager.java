
package org.jboss.mobicents.seam;

import java.io.IOException;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
 

public interface CallManager {

	public String ANNOUNCEMENT_TYPE_OPENING ="PLAY_ANN_OPENING";	
	public String ANNOUNCEMENT_TYPE_CONFIRMATION ="PLAY_ANN_CONFIRM";
	
	void initMediaConnection(SipServletRequest request) throws IOException;
	
	void mediaConnectionCreated(String sdpContent) throws IOException;

	void playAnnouncement(SipServletMessage message, String announcementType); 

	void endCall(SipServletMessage message, boolean callerTerminating) throws IOException;

}
