package org.mobicents.servlet.sip.startup.loading;

public class UserDataConstraint {
	public String description;
	public Object transportGuarantee;
	public static final String NONE_TRANSPORT_GUARANTEE = "NONE";
	public static final String INTEGRAL_TRANSPORT_GUARANTEE = "INTEGRAL";
	public static final String CONFIDENTIAL_TRANSPORT_GUARANTEE = "CONFIDENTIAL";
}
