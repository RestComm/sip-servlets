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
					add(new Label("You can deploy additional examples from here. Note that you still have to configure the Application Router to make the applications active!"));

					add(new HTML("<br/>"));
					DeploymentEntry app1 = new DeploymentEntry("conference-demo.war");
					DeploymentEntry app2 = new DeploymentEntry("shopping-demo.ear");
					add(app1);
					add(app2);
					add(new HTML("<br/>"));
				} else {
					add(new Label("The additional examples are not available for Tomcat. It is recommended to use the JBoss version, which includes Mobicents Media Server and enterprise features required bu some of the examples."));
				}
			}
			
		});

	}
}
