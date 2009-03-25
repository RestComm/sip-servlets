package org.mobicents.ipbx.session.call.model;

import java.util.HashSet;
import java.util.LinkedList;

import org.mobicents.ipbx.entity.CallState;
import org.mobicents.mscontrol.MsEndpoint;

public class Conference {
	private static final String CONFERENCE_ENDPOINT_NAME = "media/trunk/Conference/$";
	private String id;
	private String name;
	private MsEndpoint endpoint;
	private HashSet<CallParticipant> participants =
		new HashSet<CallParticipant>();
	
	public synchronized MsEndpoint getEndpoint() {
		return endpoint;
	}
	
	public synchronized String getEndpointName() {
		if(endpoint == null) {
			return CONFERENCE_ENDPOINT_NAME;
		} else {
			return endpoint.getLocalName();
		}
	}
	
	public synchronized void setEndpoint(MsEndpoint endpoint) {
		if(this.endpoint == null)
			this.endpoint = endpoint;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public synchronized void addParticipant(CallParticipant participant) {
		participants.add(participant);
	}
	
	public synchronized void removeParticipant(CallParticipant particpant) {
		participants.remove(particpant);
	}
	
	public synchronized CallParticipant[] getParticipants() {
		return participants.toArray(new CallParticipant[] {});
	}
	
	public synchronized CallParticipant[] getParticipants(CallState state) {
		CallParticipant[] ret = getParticipants();
		LinkedList<CallParticipant> filtered = new LinkedList<CallParticipant>();
		for(CallParticipant cp:ret) {
			if(state.equals(cp.getCallState())) {
				filtered.add(cp);
			}
		}
		return filtered.toArray(new CallParticipant[]{});
	}
	
	public synchronized CallParticipant[] getInactiveParticipants() {
		CallParticipant[] ret = getParticipants();
		LinkedList<CallParticipant> filtered = new LinkedList<CallParticipant>();
		for(CallParticipant cp:ret) {
			if(!CallState.CONNECTED.equals(cp.getCallState())) {
				filtered.add(cp);
			}
		}
		return filtered.toArray(new CallParticipant[]{});
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
