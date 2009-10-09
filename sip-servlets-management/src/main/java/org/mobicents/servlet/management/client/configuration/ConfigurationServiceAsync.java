package org.mobicents.servlet.management.client.configuration;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {

	void setQueueSize(int queueSize, AsyncCallback<Void> callback);
	void getQueueSize(AsyncCallback<Integer> callback);
	void setMemoryThreshold(int memoryThreshold, AsyncCallback<Void> callback);
	void getMemoryThreshold(AsyncCallback<Integer> callback);
	void getConcurrencyControlMode(AsyncCallback<String> callback);
	void setConcurrencyControlMode(String mode, AsyncCallback<Void> callback);
	void getCongestionControlPolicy(AsyncCallback<String> callback);
	void setCongestionControlPolicy(String policy, AsyncCallback<Void> callback);
	void setCongestionControlCheckingInterval(long interval, AsyncCallback<Void> callback);
	void getCongestionControlCheckingInterval(AsyncCallback<Long> callback);
	void setBaseTimerInterval(int baseTimerInterval, AsyncCallback<Void> callback);
	void getBaseTimerInterval(AsyncCallback<Integer> callback);
}
