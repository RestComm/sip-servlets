package org.mobicents.servlet.management.client.deploy;

import org.mobicents.servlet.management.client.router.Console;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Label;

public class DeployExamplesPage extends Panel {
	public DeployExamplesPage() {
		DeploymentService.Util.getInstance().isJBoss(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable arg0) {
				Console.error("Error while communicating with Server");
				
			}

			public void onSuccess(Boolean isJBoss) {
				//setCls("x-panel-mc");
				if(isJBoss) {
					DeploymentService.Util.getInstance().getApplications("examples", new AsyncCallback<String[]>() {

						public void onFailure(Throwable arg0) {
							Console.error("Couldn't get a list of applications");
							
						}

						public void onSuccess(String[] apps) {
						
							add(new HTML("<br/><h2>You can deploy additional examples from here. Note that you still have to configure the Application Router to make the applications active!</h2>"));
							add(new HTML("<br/>"));
							for(String app : apps) {
								DeploymentEntry entry = new DeploymentEntry(app);
								add(entry);
							}
							add(new HTML("<br/>"));
							
						}
						
					});
				} else {
					add(new Label("The additional examples are not available for Tomcat. It is recommended to use the JBoss version, which includes Mobicents Media Server and enterprise features required bu some of the examples."));
				}
			}
			
		});

	}
}
