package org.mobicents.servlet.management.client.router;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface DARConfigurationService extends RemoteService {

	public static final String SERVICE_URI = "/DARConfigurationService";

	public static class Util {

		public static DARConfigurationServiceAsync getInstance() {

			DARConfigurationServiceAsync instance = (DARConfigurationServiceAsync) GWT
					.create(DARConfigurationService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}
	
	public void configure(String config);
	public String[] getApplications();
	public DARRoute[] getConfiguration();

}
