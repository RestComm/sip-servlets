package org.mobicents.servlet.sip.conference.client.ui;

import org.mobicents.servlet.sip.conference.client.ServerConnection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class AddAnnouncementForm extends HorizontalPanel {
	public AddAnnouncementForm(final String confName) {
		final Label status = new Label("");
		final TextBox urlText = new TextBox();
		add(urlText);
		Button addButton = new Button("Play file");
		addButton.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				//String url = "file:///home/vralev/control/mobicents/servers/media/examples/mms-demo/web/src/main/webapp/audio/cuckoo.wav";//urlText.getText();
				String url = urlText.getText();
				
				String name = makeShort(url, 25);
				ServerConnection.Util.getInstance().joinAnnouncement(

						name, confName, url, new AsyncCallback<Void>() {

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
