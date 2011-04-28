/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.sip.conference.client.ui;

import org.mobicents.servlet.sip.conference.client.ParticipantInfo;
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
import com.gwtext.client.util.Format;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;

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
		final AsyncCallback<ParticipantInfo[]> callback = new AsyncCallback<ParticipantInfo[]>() {

			public void onFailure(Throwable caught) {
				
			}

			public void onSuccess(ParticipantInfo[] result) {

				ServerConnection.Util.getInstance().getParticipants(
						conferenceName, true, this);

				populateParticipants(result);
			}

		};

		ServerConnection.Util.getInstance().getParticipants(
				conferenceName, false, callback);
	}

	private void populateParticipants(ParticipantInfo[] result) {
		participants.clear();
		for(int q=0; q<result.length; q++) {
			final String user = result[q].name;
			
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
			
			Button unmuteLink = new Button();
			unmuteLink.setText("unmute");
			unmuteLink.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					ServerConnection.Util.getInstance().unmute(user, conferenceName, new AsyncCallback<Void>() {

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
			if(!result[q].muted) {
				commands.add(muteLink);
			} else {
				commands.add(unmuteLink);
			}
			participants.setWidget(q, 1, userLabel);
			participants.setWidget(q, 2, commands);
		}
	}
}
