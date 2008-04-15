package org.mobicents.servlet.sip.startup.loading;

import java.util.Collection;

public class SecurityConstraint {
	public String displayName;
	public Collection resourceCollections;
	public boolean proxyAuthentication;
	public AuthorizationConstraint authorizationConstraint;
	public UserDataConstraint userDataConstraint;
}
