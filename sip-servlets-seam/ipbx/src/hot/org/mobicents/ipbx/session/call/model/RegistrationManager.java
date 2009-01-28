package org.mobicents.ipbx.session.call.model;

import java.util.HashMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.mobicents.ipbx.entity.Registration;

@Name("registrationManager")
@Scope(ScopeType.APPLICATION)
@Startup
public class RegistrationManager {
	private HashMap<Registration, CallParticipant> registrations =
		new HashMap<Registration, CallParticipant>();
	
	public void set(Registration registration, CallParticipant participant) {
		registrations.put(registration, participant);
	}
	
	public void remove(Registration registration) {
		registrations.remove(registration);
	}
}
