package org.mobicents.servlet.sip.conference.client.ui;

import org.mobicents.servlet.sip.conference.client.ServerConnection;
import org.mobicents.servlet.sip.conference.client.SipGwtConferenceConsole;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Conference extends VerticalPanel{

	private String conferenceName;
	
	private FlexTable participants = new FlexTable();
	
	public Conference(final String conferenceName) {
		this.conferenceName = conferenceName;
		setBorderWidth(0);
		participants.setWidth("500");
		participants.setBorderWidth(0);
		participants.setStyleName("participants");
		add(new Image("mssconf.png"));
		add(new HTML("</hr><b>To join the conference you can dial-in \"" 
				+ SipGwtConferenceConsole.CONFERENCE_NAME 
				+ "\" or join another sip phone by dialing it from the " +
						"form below, or you can play an announcement in the" +
						" conferece by typing the file name or URL of the " +
						"wav file (PCM, mono, 16 bits, 8KHz).</b></br></br>"));
		
		add(participants);
		add(new HTML("</br></br>"));
		add(new AddAnnouncementForm(conferenceName));
		add(new AddSipPhoneForm(conferenceName));
		final AsyncCallback<String[]> callback = new AsyncCallback<String[]>() {

			public void onFailure(Throwable caught) {
				
			}

			public void onSuccess(String[] result) {

				ServerConnection.Util.getInstance().getParticipants(
						conferenceName, true, this);

				populateParticipants(result);
			}

		};
		
		ServerConnection.Util.getInstance().getParticipants(
				conferenceName, false, callback);
	}
	
	private void populateParticipants(String[] result) {
		participants.clear();
		for(int q=0; q<result.length; q++) {
			final String user = result[q];
			
			Button kickLink = new Button();
			kickLink.setText("kick");
			kickLink.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ServerConnection.Util.getInstance().kick(user, conferenceName, new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							
						}

						public void onSuccess(Void result) {
							
						}
						
					});
				}
				
			});
			
			Button muteLink = new Button();
			muteLink.setText("mute");
			muteLink.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ServerConnection.Util.getInstance().mute(user, conferenceName, new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							
						}

						public void onSuccess(Void result) {
							
						}
						
					});
				}
				
			});
			Label numberLabel = new Label(new Integer(q).toString());
			numberLabel.setStyleName("numberLabel");
			participants.setWidget(q, 0, new Image("ajax-loader.gif"));
			Label userLabel = new Label(user);
			userLabel.setStyleName("userLabel");
			HorizontalPanel commands = new HorizontalPanel();
			commands.add(kickLink);
			commands.add(muteLink);
			participants.setWidget(q, 1, userLabel);
			participants.setWidget(q, 2, commands);
		}
	}
}
