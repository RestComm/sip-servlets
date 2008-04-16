package org.mobicents.servlet.management.client;

import org.mobicents.servlet.management.client.router.RequestColumnsContainer;
import org.mobicents.servlet.management.client.router.RouterToolbar;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class UserInterface extends VerticalPanel{

	public UserInterface() {
		this.setWidth("100%");
		add(getHeader());
		add(getContent());
	}
	
	private Widget getHeader() {
		HorizontalPanel header = new HorizontalPanel();
		Image logo = new Image("images/logo.jpg");
		header.add(logo);
		header.setBorderWidth(0);
		header.setWidth("100%");
		return header;
	}
	
	private Widget getContent() {
		VerticalPanel panel = new VerticalPanel();
		RequestColumnsContainer container = new RequestColumnsContainer();
		panel.add(new RouterToolbar(container));
		panel.add(new HTML("<br/>"));
		panel.add(container);
		return panel;
	}
}
