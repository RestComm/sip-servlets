package org.mobicents.servlet.sip.conference.client.ui;

import org.mobicents.servlet.sip.conference.client.ServerConnection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AddSipPhoneForm extends HorizontalPanel{
	public AddSipPhoneForm(final String confName) {
		final Label status = new Label("");
		final TextBox urlText = new TextBox();
		add(urlText);
		Button addButton = new Button("Dial Sip Phone");
		addButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				String uri = urlText.getText();
				
				String name = makeShort(uri, 25);
				ServerConnection.Util.getInstance().joinSipPhone(

						name, confName, uri, new AsyncCallback<Void>() {

							public void onFailure(Throwable caught) {
								status.setText("Failure");
								
							}

							public void onSuccess(Void result) {
								
								
							}
							
						});
				
			}
			
		});
		add(addButton);
		add(status);
	}
	
	public static String makeShort(String str, int size) {
		if(str.length() <= size) return str;
		return ".." + str.substring(str.length()-size, str.length());
	}
}
