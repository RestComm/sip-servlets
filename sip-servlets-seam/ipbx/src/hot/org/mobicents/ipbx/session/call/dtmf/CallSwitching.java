package org.mobicents.ipbx.session.call.dtmf;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.session.call.framework.IVRHelper;
import org.mobicents.ipbx.session.call.framework.IVRHelperManager;
import org.mobicents.ipbx.session.call.model.CallParticipant;
import org.mobicents.ipbx.session.call.model.CurrentWorkspaceState;
import org.mobicents.ipbx.session.call.model.WorkspaceStateManager;
import org.mobicents.ipbx.session.configuration.PbxConfiguration;

@Name("callSwitching")
@Scope(ScopeType.STATELESS)
public class CallSwitching {
	@Logger 
	private static Log log;
	@In SipSession sipSession;
	@In IVRHelper ivrHelper;
	
	@Observer("DTMF")
	public void doDTMF(String digit) {
		log.info(digit);
		int dtmfNumber = 1000;
		if(digit.charAt(0)>='0' && digit.charAt(0)<='9') {
			dtmfNumber = Integer.parseInt(digit);
		}
		CallParticipant participant = 
			(CallParticipant) sipSession.getAttribute("participant");
		CurrentWorkspaceState currentWorkspaceState = 
			WorkspaceStateManager.instance().getWorkspace(participant.getName());
		CallParticipant[] participants = currentWorkspaceState.getOngoingCalls();
		
		if(participants.length>2) {
			// This operation makes sense only when there are more than two participants
			if(dtmfNumber < participants.length && // make sure we are not puting ourselves on hold
					participants[dtmfNumber].getUri().equals(participant.getUri())) {
				for(int q=0; q<participants.length; q++) {
					if(q!=dtmfNumber) {
						currentWorkspaceState.putOnhold(participants[q]);
					}
				}
				currentWorkspaceState.unputOnhold(participants[dtmfNumber]);
			}
		}
		if(dtmfNumber == 0) {
			for(int q=0; q<participants.length; q++) {
				if(q!=dtmfNumber) {
					currentWorkspaceState.unputOnhold(participants[q]);
				}
			}
		}
	}
	
	@Observer("announcementComplete")
	public void doAnnouncementComplete() {
		CallParticipant participant = 
			(CallParticipant) sipSession.getAttribute("participant");
		if(participant.isOnhold()) { 
			// If we are onhold keep repeating, so the user can't hear the conversation
			// from the conference.
			String ann = PbxConfiguration.getProperty("pbx.default.onhold.announcement");
			IVRHelperManager.instance().getIVRHelper(sipSession).playAnnouncementWithDtmf(ann);
		}
	}

}
