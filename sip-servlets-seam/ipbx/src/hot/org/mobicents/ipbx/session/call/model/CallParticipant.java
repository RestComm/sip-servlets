package org.mobicents.ipbx.session.call.model;

import java.util.HashSet;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.mobicents.ipbx.entity.Binding;
import org.mobicents.ipbx.entity.CallState;
import org.mobicents.ipbx.entity.PstnGatewayAccount;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;

@Name("callParticipant")
@Scope(ScopeType.STATELESS)
public class CallParticipant {
	private MsConnection msConnection;
	private MsLink msLink;
	private String name;
	private String uri;
	private Conference conference;
	private CallState callState;
	private SipServletRequest initialRequest;
	private Registration registration;
	private boolean initiator;
	private boolean muted;
	private boolean onhold;
	private Binding binding;
	private PstnGatewayAccount pstnGatewayAccount;
	
	public SipServletRequest getInitialRequest() {
		return initialRequest;
	}
	
	public void setInitialRequest(SipServletRequest request) {
		initialRequest = request;
	}

	CallParticipant() {
	}
	
	public SipSession getSipSession() {
		if(initialRequest != null)
			return initialRequest.getSession();
		return null;
	}
	public CallState getCallState() {
		return callState;
	}
	public void setCallState(CallState callState) {
		this.callState = callState;
	}
	public Conference getConference() {
		return conference;
	}
	public void setConference(Conference conference) {
		if(conference == null) {
			if(this.conference != null) {				
				this.conference.removeParticipant(this);
				this.conference = conference;
			}
		} else {
			this.conference = conference;
			this.conference.addParticipant(this);
		}		
	}
	public MsConnection getMsConnection() {
		return msConnection;
	}
	public void setMsConnection(MsConnection msConnection) {
		this.msConnection = msConnection;
	}
	public MsLink getMsLink() {
		return msLink;
	}
	public void setMsLink(MsLink msLink) {
		this.msLink = msLink;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public MsEndpoint getPrEndpoint() {
		if(msConnection != null) {
			return msConnection.getEndpoint();
		}
		return null;
	}

	public Registration getRegistration() {
		return registration;
	}

	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public boolean isInitiator() {
		return initiator;
	}

	public void setInitiator(boolean initiator) {
		this.initiator = initiator;
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public PstnGatewayAccount getPstnGatewayAccount() {
		return pstnGatewayAccount;
	}

	public void setPstnGatewayAccount(PstnGatewayAccount pstnGatewayAccount) {
		this.pstnGatewayAccount = pstnGatewayAccount;
	}
	
	public String toString() {
		return "[uri=" + uri + ", conf=" + conference.getId() + ", name=" + name + "]";
	}

	public Binding getBinding() {
		return binding;
	}

	public void setBinding(Binding binding) {
		this.binding = binding;
	}

	public boolean isOnhold() {
		return onhold;
	}

	public void setOnhold(boolean onhold) {
		this.onhold = onhold;
	}
}
