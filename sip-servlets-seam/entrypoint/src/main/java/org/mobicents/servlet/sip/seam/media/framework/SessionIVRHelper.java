package org.mobicents.servlet.sip.seam.media.framework;

import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Unwrap;
import org.mobicents.servlet.sip.seam.entrypoint.media.MediaController;

@Name("ivrHelper")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class SessionIVRHelper {
	@In(required=false) SipSession sipSession;
	@In(required=false) MediaController mediaController;

	@Unwrap
	public IVRHelper findInstance() {
		// If this is not a SIP session
		if(sipSession == null) {
			return null;
		}
		return new IVRHelper(sipSession, IVRHelperManager.instance().getMediaSessionStore(sipSession),
			mediaController);
	}
}
