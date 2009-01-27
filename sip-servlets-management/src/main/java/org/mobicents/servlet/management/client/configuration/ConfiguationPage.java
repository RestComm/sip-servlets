package org.mobicents.servlet.management.client.configuration;

import org.mobicents.servlet.management.client.router.Console;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.ComboBoxListenerAdapter;

public class ConfiguationPage extends Panel {
	private static Object[][] concurrencyControlModes = new Object[][]{  
		new Object[]{"None"},  
		new Object[]{"SipSession"},
		new Object[]{"SipApplicationSession"}
	};  
	
	private static Object[][] congestionControlPolicies = new Object[][]{  
		new Object[]{"DropMessage"},  
		new Object[]{"ErrorResponse"}
	};
	
	ComboBox ccms;
	ComboBox ccps;
	TextField queueSize;
	TextField memoryThreshold;
	
	private ComboBox makeCombo(Store store, String field, ComboBoxListenerAdapter listener, String defaultValue) {
		final ComboBox box;
		box = new ComboBox();  
		box.setForceSelection(true);  
		box.setStore(store);  
		box.setDisplayField(field);  
		box.setMode(ComboBox.LOCAL);  
		box.setTriggerAction(ComboBox.ALL);  
		box.setEmptyText("Select Value");  
		box.setValueField(field);
		box.setSelectOnFocus(true);  
		box.setEditable(false);
		box.setHideLabel(true);
		box.setWidth(160); 
		box.setHideTrigger(false);
		box.addListener(listener);
		box.setValue(defaultValue);
		return box;
	}
	
	private void addLabeledControl(String label, Widget component, Panel panel) {
		Panel regionLabel = new Panel();
		regionLabel.setPaddings(0, 0, 0, 1);
		regionLabel.setBorder(false);
		regionLabel.setHtml(label);
		panel.add(regionLabel);
		panel.add(component);
	}
	
	public ConfiguationPage() {
		final FormPanel formPanel = new FormPanel();  

		formPanel.setTitle("Concurrency and Congestion Control");  

		formPanel.setWidth(900);  
		formPanel.setFrame(true); 
		formPanel.setLabelWidth(75);

		// Create queue size text box
		queueSize = new TextField();  
		queueSize.setAllowBlank(false); 
		queueSize.setHideLabel(true);
		addLabeledControl("SIP Mesage Queue Size:", queueSize, formPanel);
		// Create memroy threshold size text box
		memoryThreshold = new TextField();  
		memoryThreshold.setAllowBlank(false); 
		memoryThreshold.setHideLabel(true);
		addLabeledControl("Memory Threshold:", memoryThreshold, formPanel);
		
		//Concurrency control modes selector
		final Store ccmsStore = new SimpleStore(new String[]{"ccms"}, concurrencyControlModes);  
		ccmsStore.load();  
		ccms = makeCombo(ccmsStore, "ccms", 
				new ComboBoxListenerAdapter() {  
			public void onSelect(ComboBox comboBox, com.gwtext.client.data.Record record, int index) {  
				System.out.println("Concurrency control::onSelect('" + record.getAsString("ccms") + "')");  
			}  
		}, (String)concurrencyControlModes[1][0]);
		addLabeledControl("Concurrency control mode:", ccms, formPanel);

		final Store ccpsStore = new SimpleStore(new String[]{"ccps"}, congestionControlPolicies);  
		ccpsStore.load();  
		ccps = makeCombo(ccpsStore, "ccps", 
				new ComboBoxListenerAdapter() {  
			public void onSelect(ComboBox comboBox, com.gwtext.client.data.Record record, int index) {  
				System.out.println("Congestion control::onSelect('" + record.getAsString("ccps") + "')");  
			}  
		}, (String)congestionControlPolicies[1][0]);
		addLabeledControl("Congestion control policy:", ccps, formPanel);
		
		//Save button
		Button save = new Button("Apply", new ButtonListenerAdapter(){

			public void onClick(Button button, EventObject e) {
				ConfigurationService.Util.getInstance().setConcurrencyControlMode(
						ccms.getValue(), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set concurreny control mode.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setCongestionControlPolicy(
						ccps.getValue(), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set congestion control policy.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setQueueSize(
						Integer.parseInt(queueSize.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set SIP message queue size.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
				
				ConfigurationService.Util.getInstance().setMemoryThreshold(
						Integer.parseInt(memoryThreshold.getValueAsString()), new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to set memory Threshold.");
							}

							public void onSuccess(Void result) {
								result = result;
							}
							
						});
			}
			
		});
		
		formPanel.add(save);

		add(formPanel);
		
		DeferredCommand.addCommand(new Command() {

			public void execute() {
				ConfigurationService.Util.getInstance().getQueueSize(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get SIP message queue size.");
					}

					public void onSuccess(Integer result) {
						queueSize.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getConcurrencyControlMode(
						new AsyncCallback<String>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to get concurreny control mode.");
							}

							public void onSuccess(String result) {
								ccms.setValue(result.toString());
							}
							
						});
				
				ConfigurationService.Util.getInstance().getMemoryThreshold(new AsyncCallback<Integer>() {

					public void onFailure(Throwable caught) {
						Console.error("Error while trying to get memory threshold.");
					}

					public void onSuccess(Integer result) {
						memoryThreshold.setValue(result.toString());
					}
					
				});
				
				ConfigurationService.Util.getInstance().getCongestionControlPolicy(
						new AsyncCallback<String>() {

							public void onFailure(Throwable caught) {
								Console.error("Error while trying to get congestion control policy.");
							}

							public void onSuccess(String result) {
								ccps.setValue(result.toString());
							}
							
						});
			}
			
		});
	}

}
