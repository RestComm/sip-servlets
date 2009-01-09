package org.mobicents.servlet.management.client.deploy;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DeploymentServiceAsync {
	void getApplications(String directory, AsyncCallback<String[]> callback);
	void deploy(String file, AsyncCallback<Void> callback);
	void isJBoss(AsyncCallback<Boolean> callback);
}
