/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
