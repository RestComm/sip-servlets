package org.mobicents.servlet.management.client.deploy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface DeploymentService extends RemoteService{
	public static class Util {

		public static DeploymentServiceAsync getInstance() {

			DeploymentServiceAsync instance = (DeploymentServiceAsync) GWT
					.create(DeploymentService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}
	public static final String SERVICE_URI = "/DeploymentService";
	String[] getApplications(String directory);
	void deploy(String application);
	boolean isJBoss();
}
