package org.mobicents.servlet.management.client.router;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RouterConfigurationPage extends Composite{
 
    
    public RouterConfigurationPage() {
    	VerticalPanel panel = new VerticalPanel();
		panel.add(getContent());
		initWidget(panel);
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
