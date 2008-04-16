package org.mobicents.servlet.management.client.router;

import java.util.HashMap;


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DARConfigurationServiceAsync {

	public void configure(String config, AsyncCallback callback);
	public void getApplications(AsyncCallback callback);
	public void getConfiguration(AsyncCallback callback);

}
