package org.mobicents.servlet.sip.example;

import java.util.HashMap;
import java.util.HashSet;

public class CallStatusContainer{
	public class Call
	{
		private String from;
		private String to;
		
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
	}
	
	private HashMap<Call,String> activeCalls = new HashMap<Call,String> ();
	
	public void addCall(String from, String to, String status) {
		activeCalls.put(new Call(from, to), status);
	}
	
	public void removeCall(String from, String to) {
		activeCalls.remove(new Call(from, to));
	}
	
	public String getStatus(String from, String to) {
		return activeCalls.get(new Call(from, to));
	}
}
