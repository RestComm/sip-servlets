package org.mobicents.servlet.management.client.router;

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
		
		ToolbarButton logErrorButton = new ToolbarButton("Log and errors", new ButtonListenerAdapter() {  
			public void onClick(Button button, EventObject e) {
				Console.getInstance().show();
			}
			
		});
		
		
		ToolbarButton helpButton = new ToolbarButton("Help", new ButtonListenerAdapter() {  
			public void onClick(Button button, EventObject e) {
				Frame helpFrame = new Frame("help/index.html");
				helpWindow.setTitle("Default Application Router Help");  
				helpWindow.setClosable(true);  
				helpWindow.setWidth(600);  
				helpWindow.setHeight(350);  
				helpWindow.setPlain(true);  
				helpWindow.setAutoScroll(true);
				helpWindow.setLayout(new FitLayout());
				helpWindow.add(helpFrame);
				helpWindow.setCloseAction(Window.HIDE);
				helpWindow.show();
			}
			
		});
		
		addButton(showDARTextButton);
		addSeparator();
		addButton(saveButton);
		addSeparator();
		addButton(logErrorButton);
		addSeparator();
		addButton(helpButton);
		
	}
}
