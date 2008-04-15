package org.mobicents.servlet.sip.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.sip.SipSession;

public class CallStatusContainer{
	
	private HashSet<Call> activeCalls = new HashSet<Call> ();
	
	public Call addCall(String from, String to, String status) {
		Call call = new Call(from, to);
		call.setStatus(status);
		activeCalls.add(call);
		return call;
	}
	
	public void removeCall(String from, String to) {
		activeCalls.remove(new Call(from, to));
	}
	
	public void removeCall(Call call) {
		activeCalls.remove(call);
	}
	
	public Call getCall(String from, String to) {
		Iterator<Call> it = activeCalls.iterator();
		while(it.hasNext()) {
			Call call = it.next();
			if(call.getFrom().equals(from) && call.getTo().equals(to))
				return call;
		}
		return null;
	}
	
	public String getStatus(String from, String to) {
		return getCall(from,to).getStatus();
	}
}
