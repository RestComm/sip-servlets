package org.mobicents.servlet.management.client.router;

import org.mobicents.servlet.management.client.UserInterface;
import org.mobicents.servlet.management.client.dnd.NoInsertAtEndIndexedDropController;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.IndexedDropController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;

public final class RequestColumnsContainer extends AbsolutePanel {

	private static final String[] COLUMNS = new String[] {"ALL", "INVITE", "REGISTER", "SUBSCRIBE", "OPTIONS", "MESSAGE", "NOTIFY", "PUBLISH", "REFER" };

	private static final String CSS_SSM = "ssm";

	private static final String CSS_SSM_COLUMN_COMPOSITE = "ssm-column-composite";

	private static final String CSS_CONTAINER = "ssm-container";

	private static final String CSS_HEADING = "ssm-heading";

	private static final String CSS_WIDGET = "ssm-widget";
	
	private static final String CSS_TITLE = "ssm-title-background";

	private static final int SPACING = 0;

	private VerticalPanel[] routeColumns;

	public RequestColumnsContainer() {
		init();
	}
	
	private Widget makeTitle(String text) {
		Label title = new Label(text);
		title.setPixelSize(221, 20);
		title.addStyleName(CSS_HEADING);
		return title;
	}
	private Widget buildTitles() {
		HorizontalPanel titles = new HorizontalPanel();
		titles.setStyleName(CSS_TITLE);
		titles.setSpacing(SPACING);
		Label spacerLabel = new Label("");
		spacerLabel.setPixelSize(14, 7);
		titles.add(spacerLabel);
		
		for(int col=0; col<COLUMNS.length; col++) {
			titles.add(makeTitle(COLUMNS[col]));
		}
		
		return titles;
	}
	
	private void populateRouterNodes(DARRoute[] routes) {
		AbsolutePanel boundaryPanel = this;
		boundaryPanel.add(buildTitles());
		boundaryPanel.setSize(UserInterface.WIDTH, UserInterface.HEIGHT);
		addStyleName(CSS_SSM);
		routeColumns = new VerticalPanel[COLUMNS.length];
		PickupDragController columnDragController = new PickupDragController(boundaryPanel, false);
		columnDragController.setBehaviorMultipleSelection(false);

		final PickupDragController widgetDragController = new PickupDragController(boundaryPanel, false);
		widgetDragController.setBehaviorMultipleSelection(false);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.addStyleName(CSS_CONTAINER);
		horizontalPanel.setSpacing(SPACING);
		boundaryPanel.add(horizontalPanel);

		IndexedDropController columnDropController = new IndexedDropController(horizontalPanel);
		columnDragController.registerDropController(columnDropController);

		for (int col = 0; col < COLUMNS.length; col++) {

			VerticalPanel columnCompositePanel = new VerticalPanel();
			columnCompositePanel.addStyleName(CSS_SSM_COLUMN_COMPOSITE);

			final VerticalPanel verticalPanel = new VerticalPanel();
			routeColumns[col] = verticalPanel;
			verticalPanel.addStyleName(CSS_CONTAINER);
			verticalPanel.setSpacing(SPACING);
			horizontalPanel.add(columnCompositePanel);

			NoInsertAtEndIndexedDropController widgetDropController = new NoInsertAtEndIndexedDropController(
					verticalPanel);
			widgetDragController.registerDropController(widgetDropController);

			HTML groupDragHandle = new HTML("<div class='group-drag-handle'/>");
			columnCompositePanel.add(groupDragHandle);
			columnCompositePanel.add(verticalPanel);

			columnDragController.makeDraggable(columnCompositePanel, groupDragHandle);
			
			for(int q=0; q<routes.length; q++) {
				if(COLUMNS[col].equals(routes[q].getRequest())) {
					for(int w=0; w<routes[q].getNodes().length; w++) {
						ApplicationRouteNodeEditor widget = new ApplicationRouteNodeEditor(routes[q].getNodes()[w]);
						verticalPanel.add(widget);
						widgetDragController.makeDraggable(widget, widget.getDragHandle());
					}
				}
			}

			Label spacerLabel = new Label("");
			spacerLabel.setPixelSize(199, 50);
			verticalPanel.add(spacerLabel);
			Button addApplicationButton = new Button("Add application", new ButtonListenerAdapter() {  
				public void onClick(Button button, EventObject e) {
					ApplicationRouteNodeEditor widget = new ApplicationRouteNodeEditor();
					verticalPanel.insert(widget, verticalPanel.getWidgetCount()-1);
					widgetDragController.makeDraggable(widget, widget.getDragHandle());
				}
				
			});
			addApplicationButton.setWidth("100%");
			columnCompositePanel.add(addApplicationButton);
		}
		
		
	}
	
	public String getDARText() {
		String source = "";
		for(int col=0; col<routeColumns.length; col++) {
			int count = routeColumns[col].getWidgetCount();
			boolean empty = true;
			String routeText = "";
			for(int row=0; row<count; row++) {	
				Widget widget = routeColumns[col].getWidget(row);
				if(widget instanceof ApplicationRouteNodeEditor) {
					empty = false;
					routeText += widget.toString() + ",";
				}
			}
			if(!empty) {
				routeText = routeText.substring(0, routeText.length() - 1);
				source += COLUMNS[col] + ":" + routeText + "\n";
			}
		}
		return source;
	}
	
	public VerticalPanel[] getColumns() {
		return routeColumns;
	}
	
	private void init() {
		DARConfigurationServiceAsync darConfigService = DARConfigurationService.Util.getInstance();
		darConfigService.getConfiguration(new AsyncCallback() {

			public void onFailure(Throwable arg0) {
				Console.error("Failed to parse AR configuration");
				
			}

			public void onSuccess(Object configObj) {
				
				DARRoute[] routes = (DARRoute[]) configObj;
				populateRouterNodes(routes);
				Console.info("AR configiuration parsed succesfully");
			}
			
		});
	}
	
}
