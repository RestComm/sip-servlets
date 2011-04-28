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
		urlText.setText("http://mobicents.googlecode.com/svn/branches/servers/media/1.x.y/examples/mms-demo/web/src/main/webapp/audio/cnfannouncement.wav");
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
