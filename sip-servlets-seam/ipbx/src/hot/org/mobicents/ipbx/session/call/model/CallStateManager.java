package org.mobicents.ipbx.session.call.model;

import java.util.HashMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

@Name("callStateManager")
@Scope(ScopeType.APPLICATION)
@Startup
public class CallStateManager {
	private HashMap<String, CurrentUserState> currentUserStates =
		new HashMap<String, CurrentUserState>();
	
	public synchronized CurrentUserState getCurrentState(String username) {
		if(currentUserStates.get(username) == null) {
			CurrentUserState state = new CurrentUserState();
			currentUserStates.put(username, state);
		}
		return currentUserStates.get(username);
	}
}
