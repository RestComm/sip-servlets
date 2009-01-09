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
