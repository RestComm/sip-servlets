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
			if(dtmfNumber < participants.length && // make sure the dtmf is in range
					participants[dtmfNumber] != participant) {// make sure we dont end up talking to ourselves
				
				// Play the switched announcement to tell the current user than the switch is happening
				try {
					// It's not a vital operation, if it fails, we dont care
					String file = PbxConfiguration.getProperty("pbx.default.switched.announcement");
					IVRHelperManager.instance().getIVRHelper(participant.getSipSession())
					.playAnnouncementWithDtmf(file);
				} catch (Exception e) {
					// TODO: log it somewhere
				}
				
				// Put other users on hold
				for(int q=0; q<participants.length; q++) {
					if(participants[q] != participant && q != dtmfNumber) {
						currentWorkspaceState.putOnhold(participants[q]);
					}
				}
				
				// And just in case the callee is onhold from previous switch, unput him
				currentWorkspaceState.unputOnhold(participants[dtmfNumber]);
			}
		}
		if(dtmfNumber == 9) {
			for(int q=0; q<participants.length; q++) {
				currentWorkspaceState.unputOnhold(participants[q]);
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
