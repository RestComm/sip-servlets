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
