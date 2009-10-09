package org.mobicents.servlet.management.client.configuration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface ConfigurationService extends RemoteService {
	public static class Util {

		public static ConfigurationServiceAsync getInstance() {

			ConfigurationServiceAsync instance = (ConfigurationServiceAsync) GWT
					.create(ConfigurationService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}
	public static final String SERVICE_URI = "/ConfigurationService";
	
	void setQueueSize(int queueSize);
	int getQueueSize();
	public void setMemoryThreshold(int memoryThreshold);
	public int getMemoryThreshold();
	String getConcurrencyControlMode();
	void setConcurrencyControlMode(String mode);
	public String getCongestionControlPolicy();
	public void setCongestionControlPolicy(String congestionControlPolicy);
	public long getCongestionControlCheckingInterval();
	public void setCongestionControlCheckingInterval(long interval);
	void setBaseTimerInterval(int baseTimerInterval);
	int getBaseTimerInterval();
}
