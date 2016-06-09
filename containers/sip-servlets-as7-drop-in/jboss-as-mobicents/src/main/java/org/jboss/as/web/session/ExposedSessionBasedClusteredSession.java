package org.jboss.as.web.session;

import org.jboss.as.clustering.web.OutgoingSessionGranularitySessionData;

public class ExposedSessionBasedClusteredSession extends SessionBasedClusteredSession {
	
	public ExposedSessionBasedClusteredSession(ClusteredSessionManager<OutgoingSessionGranularitySessionData> manager) {
		super(manager);
	}
	
}
