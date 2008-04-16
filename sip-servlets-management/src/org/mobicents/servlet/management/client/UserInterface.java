package org.mobicents.servlet.management.client;

import org.mobicents.servlet.management.client.router.RouterConfigurationPage;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.TabPanel;

public class UserInterface extends VerticalPanel{

	public UserInterface() {
		this.setWidth("100%");
		RouterConfigurationPage routerPage = new RouterConfigurationPage();
		routerPage.setTitle("Router Configuration");
//		TabPanel tabPanel = new TabPanel();
//		tabPanel.add(routerPage);
//		tabPanel.setMinTabWidth(115);  
//		tabPanel.setTabWidth(135);
		add(getHeader());
		add(routerPage);
	}
	
	private Widget getHeader() {
		HorizontalPanel header = new HorizontalPanel();
		Image logo = new Image("images/logo.jpg");
		header.add(logo);
		header.setBorderWidth(0);
		header.setWidth("100%");
		return header;
	}

}
