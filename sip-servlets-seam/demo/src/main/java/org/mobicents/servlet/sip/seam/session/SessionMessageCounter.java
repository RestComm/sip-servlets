package org.mobicents.servlet.sip.seam.session;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 *  Each Sip Session will have exactly one instance of this class.
 *
 */

@Name("sessionMessageCounter")
@Startup
@Scope(ScopeType.SESSION)
public class SessionMessageCounter {
	private int messages;
	
	public int getMessages() {
		return messages;
	}
	
	public void increment() {
		messages++;
	}
}
