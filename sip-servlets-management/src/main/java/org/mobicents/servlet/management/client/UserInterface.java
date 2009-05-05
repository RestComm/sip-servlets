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
		tabPanel.add(getDeploymentPage());
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
