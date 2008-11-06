package org.mobicents.servlet.management.client.configuration;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {

	void setQueueSize(int queueSize, AsyncCallback callback);
	void getQueueSize(AsyncCallback callback);
	void getConcurrencyControlMode(AsyncCallback callback);
	void setConcurrencyControlMode(String mode, AsyncCallback callback);
}
