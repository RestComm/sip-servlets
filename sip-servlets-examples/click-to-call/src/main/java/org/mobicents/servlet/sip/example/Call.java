package org.mobicents.servlet.sip.example;

import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.sip.SipSession;


public class Call
{
	private String from;
	private String to;
	private String status;
	private HashSet<SipSession> sessions = new HashSet<SipSession>();

	public Call(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public boolean equals(Object a) {
		Call other = (Call) a;
		if(other.from.equals(from) && other.to.equals(to)) return true;
		return false;
	}

	public int hashCode() {
		return from.hashCode()^to.hashCode();
	}

	public void addSession(SipSession session) {
		this.sessions.add(session);
	}

	public void end() {
		Iterator it = this.sessions.iterator();
		while(it.hasNext()) {
			SipSession session = (SipSession) it.next();
			try {
				session.createRequest("BYE").send();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}