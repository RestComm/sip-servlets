package org.mobicents.ipbx.session.call.logging;

import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("sipMessageLog")
@Scope(ScopeType.APPLICATION)
@Startup
public class SipMessageLog {
	private HashMap<SipSession, LinkedList<String>> messages =
		new HashMap<SipSession, LinkedList<String>>();
	
	@In(required=false) SipSession sipSession;
	private synchronized void logSipMessage(String message) {
		
	}

	public void log(SipServletMessage message){
		logSipMessage(message.toString());
	}
	
	public HashMap<SipSession, LinkedList<String>> getMessages() {
		return messages;
	}
}
