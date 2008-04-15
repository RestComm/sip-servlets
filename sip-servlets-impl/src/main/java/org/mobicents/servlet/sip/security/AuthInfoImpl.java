package org.mobicents.servlet.sip.security;

import java.util.HashMap;

import javax.servlet.sip.AuthInfo;

public class AuthInfoImpl implements AuthInfo {

	private HashMap<String, AuthInfoEntry> realmToAuthInfo = new HashMap<String, AuthInfoEntry>();
	
	public void addAuthInfo(int statusCode, String realm, String userName, String password) {
		AuthInfoEntry authEntry = new AuthInfoEntry(statusCode, userName, password);
		realmToAuthInfo.put(realm, authEntry);
	}
	
	public AuthInfoEntry getAuthInfo(String realm)
	{
		return realmToAuthInfo.get(realm);
	}

}
