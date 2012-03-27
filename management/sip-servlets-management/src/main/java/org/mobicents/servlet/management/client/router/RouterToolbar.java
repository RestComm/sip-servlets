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

package org.mobicents.servlet.management.client.router;

import org.mobicents.servlet.management.client.UserInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.ToolbarTextItem;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

public class RouterToolbar extends Toolbar {
	public RouterToolbar(final RequestColumnsContainer container) {
		addItem(new ToolbarTextItem("Default Application Router Configuration"));
		addSpacer();addSpacer();addSeparator();addSpacer();addSpacer();
		
		final Window sourceCodeWindow = new Window();
		final Window helpWindow = new Window();
		
		// Initialize help window
		Frame helpFrame = new Frame("help/index.html");
		helpWindow.setTitle("Default Application Router Help");  
		helpWindow.setClosable(true);  
		helpWindow.setWidth(600);  
		helpWindow.setHeight(350);  
		helpWindow.setPlain(true);  
		helpWindow.setAutoScroll(false);
		helpWindow.setLayout(new FitLayout());
		helpWindow.add(helpFrame);
		helpWindow.setCloseAction(Window.HIDE);
		
		
		//Set up the source code button - display DAR config
		ToolbarButton showDARTextButton = new ToolbarButton("View source", new ButtonListenerAdapter() {
			public void onClick(Button button, EventObject e) {
				String sourceCode = container.getDARText();
				sourceCodeWindow.setTitle("Default Application Router Source");  
				sourceCodeWindow.setClosable(true);  
				sourceCodeWindow.setWidth(600);  
				sourceCodeWindow.setHeight(350);  
				sourceCodeWindow.setPlain(true);  
				sourceCodeWindow.setAutoScroll(true);
				sourceCodeWindow.setHtml("<pre>" + sourceCode + "</pre>");
				sourceCodeWindow.setCloseAction(Window.HIDE);
				sourceCodeWindow.show();
			}
			
		});
		showDARTextButton.setIconCls("tab-icon-source");
		
		// Set up the Save button - saves the DAR configuration
		ToolbarButton saveButton = new ToolbarButton("Save", new ButtonListenerAdapter() {  
			public void onClick(Button button, EventObject e) {
				save();
				System.out.println("Source code:" + container.getDARText());

			}
			
			public void save() {

				DARConfigurationService.Util.getInstance().configure(container.getDARText(), new AsyncCallback(){

					public void onFailure(Throwable arg0) {
						Console.error("Could not save the AR data: " + arg0.getMessage());
					}

					public void onSuccess(Object arg0) {
						Console.info("Successfully persisted new AR configuration.");
						
					}
					
				});
			}
			
		});
		saveButton.setIconCls("tab-icon-save");
		
		// Pop the console window
		ToolbarButton logErrorButton = new ToolbarButton("Log and errors", new ButtonListenerAdapter() {  
			public void onClick(Button button, EventObject e) {
				Console.getInstance().show();
			}
			
		});
		logErrorButton.setIconCls("tab-icon-log");
		
		// Help button
		ToolbarButton helpButton = new ToolbarButton("Help", new ButtonListenerAdapter() {  
			public void onClick(Button button, EventObject e) {

				helpWindow.show();
			}
			
		});
		helpButton.setIconCls("tab-icon-help");
		
		addButton(showDARTextButton);
		addSeparator();
		addButton(saveButton);
		addSeparator();
		addButton(logErrorButton);
		addSeparator();
		addButton(helpButton);
		this.setWidth(UserInterface.WIDTH);
		this.setHeight(25);
		
	}
}
