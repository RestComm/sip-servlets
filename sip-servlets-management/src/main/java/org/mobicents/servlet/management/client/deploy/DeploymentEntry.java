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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.HorizontalLayout;

public class DeploymentEntry extends Panel {
	public DeploymentEntry(final String file) {
		setLayout(new HorizontalLayout(10));
		add(new HTML("<div/>"));
		Panel left = new Panel();
		left.setWidth(300);
		left.add(new Label(file));
		add(left);
		Button deployButton = new Button("Deploy", new ButtonListenerAdapter() {
			public void onClick(Button button, EventObject e) {
				DeploymentService.Util.getInstance().deploy(file, new AsyncCallback<Void> () {

					public void onFailure(Throwable arg0) {
						Console.error("Could not deploy!");
					}

					public void onSuccess(Void arg0) {
						Console.info("Deployment successful: " + file);
					}
					
				});
			}
			
		});
		add(deployButton);
		
		Hyperlink link = new Hyperlink();
		final String currentHost = Window.Location.getProtocol() + "//"
        	+ Window.Location.getHost();
		
		// Remove extension
		String noExtension = file;
		int dotIndex = noExtension.lastIndexOf('.');
		if(dotIndex > 0) {
			noExtension = file.substring(0, dotIndex);
		}
		
		final String applicationUrl = currentHost + "/" + noExtension;
		
		add(new HTML("<a href=\"" + applicationUrl + "\">" + applicationUrl + "</a>"));
	}
}
