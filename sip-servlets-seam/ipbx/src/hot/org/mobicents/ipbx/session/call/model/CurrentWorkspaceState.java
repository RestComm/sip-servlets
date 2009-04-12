package org.mobicents.ipbx.session.call.model;

import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;

import org.ajax4jsf.event.PushEventListener;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.entity.CallState;
import org.mobicents.ipbx.entity.User;
import org.mobicents.servlet.sip.seam.media.framework.IVRHelperManager;
import org.mobicents.ipbx.session.configuration.PbxConfiguration;
import org.mobicents.mscontrol.MsLinkMode;

@Name("currentWorkspaceState")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class CurrentWorkspaceState {
	@Logger 
	private static Log log;
	
	@In(value="sessionUser") User sessionUser;
	
	private Conference conference;
	
	//using a global push listener instead of 3 listener because browsers can't handle more than 2 simulteanous connections 
	private PushEventListener globalListener;
	
	private HashSet<CallParticipant> incomingCalls = new HashSet<CallParticipant>();
	private HashSet<CallParticipant> ongoingCalls = new HashSet<CallParticipant>();
	private HashSet<CallParticipant> outgoingCalls = new HashSet<CallParticipant>();
	
	public CallParticipant[] getIncomingCalls() {
		return incomingCalls.toArray(new CallParticipant[]{});
	}
	public CallParticipant[] getOngoingCalls() {
		return ongoingCalls.toArray(new CallParticipant[]{});
	}
	public CallParticipant[] getOutgoingCalls() {
		return outgoingCalls.toArray(new CallParticipant[]{});
	}
	
	@Unwrap
	public CurrentWorkspaceState getState() {
		CurrentWorkspaceState cus = WorkspaceStateManager.instance().getWorkspace(sessionUser.getName());
		return cus;
	}
	
	public void endCall(CallParticipant participant) {
		endCall(participant, true);
	}
	
	public void endCall(CallParticipant participant, boolean sendBye) {
		endCall(participant, sendBye, true);
	}
	// Ends a call with another participant while keeping everyone else in the conf
	public void endCall(CallParticipant participant, boolean sendBye, boolean disconnectOthers) {
		try {
			removeCall(participant);
			
			Conference conf = participant.getConference();
			
			// Already closed by someone
			if(conf == null) return;
			
			CallParticipant[] ps = conf.getParticipants();
			for(CallParticipant cp : ps) {
				if(cp.getName() != null) {
					WorkspaceStateManager.instance().getWorkspace(cp.getName()).removeCall(participant);
				}
			}
			participant.setConference(null);
			participant.setCallState(CallState.DISCONNECTED);
			if(sendBye) {
				participant.getInitialRequest().getSession().createRequest("BYE").send();
			}
			participant.setInitialRequest(null);

			// If there is only one other participant, attempt to disconnect him
			if(ps.length == 1 && disconnectOthers) {
				try {
					CallParticipant other = ps[0];
					String name = other.getName();
					
					// We can't use the injected callmanager here for some reason !! TODO: FIXME
					CurrentWorkspaceState cus = WorkspaceStateManager.instance().getWorkspace(name);
					try {
						cus.endCall(other, true);
					} catch (Exception e) {}
					try {
						other.getInitialRequest().createCancel().send();
					} catch (Exception e) {}
				} catch (Exception e) {
					e=e;
				}
			}
			
			if(participant.getMsLink() != null) {
				participant.getMsLink().release();
			}
			if(participant.getMsConnection() != null) {
				participant.getMsConnection().release();
			}

		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	// Mute an active participant
	public void mute(CallParticipant participant) {
		participant.setMuted(true);
		participant.getMsLink().setMode(MsLinkMode.HALF_DUPLEX);
		makeStatusDirty();
		String ann = PbxConfiguration.getProperty("pbx.default.muted.announcement");
		IVRHelperManager.instance().getIVRHelper(participant.getSipSession()).playAnnouncementWithDtmf(ann);
	}
	
	// Unmute an active participant
	public void unmute(CallParticipant participant) {
		participant.setMuted(false);
		participant.getMsLink().setMode(MsLinkMode.FULL_DUPLEX);
		makeStatusDirty();
		String ann = PbxConfiguration.getProperty("pbx.default.unmuted.announcement");
		IVRHelperManager.instance().getIVRHelper(participant.getSipSession()).playAnnouncementWithDtmf(ann);
	}
	
	// Put on-hold an active participant
	public void putOnhold(CallParticipant participant) {
		if(!participant.isOnhold()) {
			participant.setOnhold(true);
			participant.getMsLink().setMode(MsLinkMode.HALF_DUPLEX);
			makeStatusDirty();
			String ann = PbxConfiguration.getProperty("pbx.default.onhold.announcement");
			IVRHelperManager.instance().getIVRHelper(participant.getSipSession()).playAnnouncementWithDtmf(ann);
			Events.instance().raiseEvent("onHold", participant);
		}
	}

	// Restore user from on-hold as active participant
	public void unputOnhold(CallParticipant participant) {
		if(participant.isOnhold()) {
			participant.setOnhold(false);
			participant.getMsLink().setMode(MsLinkMode.FULL_DUPLEX);
			IVRHelperManager.instance().getIVRHelper(participant.getSipSession()).endAll();
			IVRHelperManager.instance().getIVRHelper(participant.getSipSession()).detectDtmf();
			makeStatusDirty();
			Events.instance().raiseEvent("unonHold", participant);
		}
	}
	
	// Cancel an outgoing call
	public void cancel(CallParticipant participant) {
		try {
			endCall(participant);
		} catch (Exception e) {}
	}
	
	// Simply removed a call from the GUI, sip/media release is done elsewhere
	public void removeCall(CallParticipant participant) {
		log.info("Removing " + participant.getUri() + " from " + sessionUser);
		incomingCalls.remove(participant);
		outgoingCalls.remove(participant);
		ongoingCalls.remove(participant);
		makeStatusDirty();
	}
	
	// Reject an incoming call
	public void reject(CallParticipant participant) {
		endCall(participant, true);
		// TODO: analyze who is that guy calling and cancel it
	}
	
	// Update the UI when a call is active
	public void setOngoing(CallParticipant participant) {
		incomingCalls.remove(participant);
		outgoingCalls.remove(participant);
		ongoingCalls.add(participant);
		this.conference = participant.getConference();
		makeStatusDirty();
	}
	
	// Update the UI when there is an incoming call
	public void setIncoming(CallParticipant participant) {
		incomingCalls.add(participant);
		this.conference = participant.getConference();
		makeStatusDirty();
	}
	
	// Update the UI for outgoing call
	public void setOutgoing(CallParticipant participant) {
		outgoingCalls.add(participant);
		this.conference = participant.getConference();
		makeStatusDirty();
	}
	
	// These methods are about what is the status of the calls now
	public boolean hasOngoingCalls() {
		return ongoingCalls.size() > 0;
	}
	
	public boolean hasIncomingCalls() {
		return incomingCalls.size() > 0;
	}
	
	public boolean hasOutgoingCalls() {
		return outgoingCalls.size() > 0;
	}
	
	public boolean anythingGoingOn() {
		return hasOngoingCalls() || hasOutgoingCalls() || hasIncomingCalls();
	}
	
	public void makeStatusDirty() {
		if(this.globalListener != null) {
			this.globalListener.onEvent(new EventObject(this));
		}
	}
	
	public void makeHistoryDirty() {
		if(this.globalListener != null) {
			this.globalListener.onEvent(new EventObject(this));
		}
	}
	
	public void makeRegistrationsDirty() {
		if(this.globalListener != null) {
			this.globalListener.onEvent(new EventObject(this));
		}
	}
	
	public void addGlobalListener(EventListener listener) {
		synchronized (listener) {
			if (this.globalListener != listener) {
				this.globalListener = (PushEventListener) listener;
			}
		}
	}
	public Conference getConference() {
		return conference;
	}
	
	public void setConference(Conference conference) {
		this.conference = conference;
	}
	

}
