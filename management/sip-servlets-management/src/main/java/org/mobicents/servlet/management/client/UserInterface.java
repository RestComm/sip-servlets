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

package org.mobicents.servlet.management.client;

import org.mobicents.servlet.management.client.configuration.ConfiguationPage;
import org.mobicents.servlet.management.client.deploy.DeployExamplesPage;
import org.mobicents.servlet.management.client.router.RouterConfigurationPage;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;

public class UserInterface extends VerticalPanel{

	public static final String WIDTH = "2500px";
	public static final String HEIGHT = "1500px";
	
	public UserInterface() {
		this.setWidth(UserInterface.WIDTH);

		TabPanel tabPanel = new TabPanel();
		tabPanel.add(getRouterConfigPage());
		tabPanel.add(getConfigurationPage());
		//tabPanel.add(getDeploymentPage());
		tabPanel.setResizeTabs(true);  
		tabPanel.setMinTabWidth(115);  
		tabPanel.setTabWidth(135);  
		tabPanel.setEnableTabScroll(true);
		tabPanel.setActiveTab(0);
		tabPanel.setWidth(UserInterface.WIDTH);
		add(getHeader());
		add(tabPanel);
	}
	
	private Widget getRouterConfigPage() {
		Panel routerPagePanel = new Panel();
		
		RouterConfigurationPage routerPage = new RouterConfigurationPage();
		
		routerPagePanel.setTitle("Router Configuration");
		routerPagePanel.setShadow(true);
		routerPagePanel.add(routerPage);
		routerPagePanel.setIconCls("tab-icon-router");
		return routerPagePanel;
	}
	
	private Widget getHeader() {
		HorizontalPanel header = new HorizontalPanel();
		Image logo = new Image("images/logo.jpg");
		header.add(logo);
		header.setBorderWidth(0);
		header.setWidth(UserInterface.WIDTH);
		return header;
	}
	
	private Widget getConfigurationPage() {
		Panel configPanel = new Panel();
		ConfiguationPage config = new ConfiguationPage();
		configPanel.add(config);
		configPanel.setTitle("Server Settings");
		configPanel.setIconCls("tab-icon-settings");
		return configPanel;
	}
	
	private Widget getDeploymentPage() {
		Panel deployPanel = new Panel();
		deployPanel.setTitle("Deploy Applications");
		DeployExamplesPage page = new DeployExamplesPage();
		deployPanel.add(page);
		deployPanel.setIconCls("tab-icon-deploy");
		return deployPanel;
	}
	

}
