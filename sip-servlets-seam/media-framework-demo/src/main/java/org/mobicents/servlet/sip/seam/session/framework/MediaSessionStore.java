package org.mobicents.servlet.sip.seam.session.framework;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsLink;

/**
 *  Each Sip Session will have exactly one instance of this class.
 *
 */

@Name("mediaSessionStore")
@Startup
@Scope(ScopeType.SESSION)
public class MediaSessionStore {
	private MsConnection msConnection;
	private MsLink msLink;
	
	public MsConnection getMsConnection() {
		return msConnection;
	}
	public MsLink getMsLink() {
		return msLink;
	}
	public void setMsConnection(MsConnection msConnection) {
		this.msConnection = msConnection;
	}
	public void setMsLink(MsLink msLink) {
		this.msLink = msLink;
	}
	

}
