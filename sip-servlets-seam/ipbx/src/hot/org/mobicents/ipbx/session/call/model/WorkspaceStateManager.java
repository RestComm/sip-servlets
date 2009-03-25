package org.mobicents.ipbx.session.call.model;

import java.util.HashMap;

public class WorkspaceStateManager {
	private static WorkspaceStateManager workspaceStateManager;
	
	private HashMap<String, CurrentWorkspaceState> currentWorkspaceStates =
		new HashMap<String, CurrentWorkspaceState>();
	
	public synchronized CurrentWorkspaceState getWorkspace(String username) {
		if(currentWorkspaceStates.get(username) == null) {
			CurrentWorkspaceState state = new CurrentWorkspaceState();
			currentWorkspaceStates.put(username, state);
		}
		return currentWorkspaceStates.get(username);
	}
	
	public static synchronized WorkspaceStateManager instance() {
		if(workspaceStateManager == null) {
			workspaceStateManager = new WorkspaceStateManager();
		}
		return workspaceStateManager;
	}

}
