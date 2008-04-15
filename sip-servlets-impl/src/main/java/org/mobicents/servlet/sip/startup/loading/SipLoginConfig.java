package org.mobicents.servlet.sip.startup.loading;

import org.apache.catalina.deploy.LoginConfig;
/**
 * 
 *
 */
public class SipLoginConfig extends LoginConfig {
	public static final String BASIC_AUTHENTICATION_METHOD = "BASIC";
	public static final String DIGEST_AUTHENTICATION_METHOD = "DIGEST";
	public static final String CLIENT_CERT_AUTHENTICATION_METHOD = "CLIENT_CERT";
}
