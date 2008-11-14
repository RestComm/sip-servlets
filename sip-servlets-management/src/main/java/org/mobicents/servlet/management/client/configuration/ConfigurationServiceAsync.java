package org.mobicents.servlet.management.client.configuration;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {

	void setQueueSize(int queueSize, AsyncCallback<Void> callback);
	void getQueueSize(AsyncCallback<Integer> callback);
	void getConcurrencyControlMode(AsyncCallback<String> callback);
	void setConcurrencyControlMode(String mode, AsyncCallback<Void> callback);
}
