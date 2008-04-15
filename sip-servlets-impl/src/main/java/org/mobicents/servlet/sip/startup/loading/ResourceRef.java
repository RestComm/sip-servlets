package org.mobicents.servlet.sip.startup.loading;

public class ResourceRef {
	public String description;
	public String resourceReferenceName;
	public String resourceType;
	public String resourceAuth;
	public String resourceSharingScope;
	public static final String RESOURCE_AUTH_APPLICATION = "Application";
	public static final String RESOURCE_AUTH_CONTAINER = "Container";
	public static final String RESOURCE_SHARING_SCOPE_SHAREABLE = "Shareable";
	public static final String RESOURCE_SHARING_SCOPE_UNSHAREABLE = "Unshareable";
}
