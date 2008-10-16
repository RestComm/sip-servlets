package org.mobicents.servlet.management.client.router;

import org.mobicents.servlet.management.client.UserInterface;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RouterConfigurationPage extends Composite{
 
    
    public RouterConfigurationPage() {
    	VerticalPanel panel = new VerticalPanel();
    	panel.setWidth(UserInterface.WIDTH);
    	panel.setHeight(UserInterface.HEIGHT);
		panel.add(getContent());
		initWidget(panel);
    }

	
	private Widget getContent() {
		VerticalPanel panel = new VerticalPanel();
		RequestColumnsContainer container = new RequestColumnsContainer();
		container.setWidth(UserInterface.WIDTH);
		container.setHeight(UserInterface.HEIGHT);
		panel.add(new RouterToolbar(container));
		panel.add(container);
		panel.setTitle("Application Router");
		panel.setWidth(UserInterface.WIDTH); // Can't set to 100% because of drag and drop bug
		panel.setHeight(UserInterface.HEIGHT);
		return panel;
	}
}
