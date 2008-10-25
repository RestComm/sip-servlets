package org.mobicents.servlet.sip.conference.server;

import javax.annotation.Resource;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;

import org.mobicents.servlet.sip.conference.client.ConferenceService;
import org.mobicents.servlet.sip.conference.server.media.AnnouncementConferenceParticipant;
import org.mobicents.servlet.sip.conference.server.media.Conference;
import org.mobicents.servlet.sip.conference.server.media.ConferenceCenter;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConferenceServiceImpl extends RemoteServiceServlet implements ConferenceService{

	@Resource
	private static SipFactory sipFactory;
	
	public String[] getParticipants(String conference, Boolean refresh) {
		Conference conf = ConferenceCenter.getInstance().getConference(conference);
		if(refresh) {
			try {
				synchronized (conf) {
					conf.wait(30000);
				}
			} catch (InterruptedException e) {
				return new String[] {};
			}
		}
		return conf.getParticipantNames();
	}

	public void joinAnnouncement(String name, String conference, String url) {
		ConferenceCenter center = ConferenceCenter.getInstance();
		AnnouncementConferenceParticipant ann = new AnnouncementConferenceParticipant(
				name, url, center.getProvider().createSession());
		center.getConference(conference).joinParticipant(ann);
		
	}

	public void kick(String user, String conference) {
		ConferenceCenter.getInstance().getConference(conference).kick(user);
	}

	public void mute(String user, String conference) {
		ConferenceCenter.getInstance().getConference(conference).mute(user);
	}

	public void joinSipPhone(String namr, String conference, String url) {
		try {
			SipApplicationSession appSession = sipFactory.createApplicationSession();
			Address from = sipFactory.createAddress("sip:sip-servlets-conference@sip-servlets.com");
			Address to = sipFactory.createAddress(url);
			SipServletRequest request = sipFactory.createRequest(appSession, "INVITE", from, to);
			request.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
