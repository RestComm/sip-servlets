package org.mobicents.servlet.management.client.router;


import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;

public class RoutingRegionComboListener extends ComboBoxListenerAdapter{  
	ApplicationRouteNodeEditor editor;
	public RoutingRegionComboListener(ApplicationRouteNodeEditor editor) {
		this.editor = editor;
	}
	
	public void onSelect(ComboBox comboBox, com.gwtext.client.data.Record record, int index) {  
		System.out.println("Routing region::onSelect('" + record.getAsString("region") + "')");
		//editor.setRoutingRegion(record.getAsString("region"));
	}  
}