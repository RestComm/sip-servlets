package org.mobicents.servlet.sip.seam.entrypoint.media;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;
import org.mobicents.mscontrol.events.MsEventFactory;

@Name("eventFactory")
@Scope(ScopeType.APPLICATION)
@Startup
public class EventFactory {
	@Unwrap
	public MsEventFactory getFactroy() {
		return MsProviderContainer.msProvider.getEventFactory();
		
	}
}
