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

package org.mobicents.servlet.management.client.router;

import org.mobicents.servlet.management.client.UserInterface;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RouterConfigurationPage extends Composite{
 
    
    public RouterConfigurationPage() {
    	VerticalPanel panel = new VerticalPanel();
    	panel.setWidth(UserInterface.WIDTH);
    	panel.setHeight(UserInterface.HEIGHT);
		panel.add(getContent());
		initWidget(panel);
    }

	
	private Widget getContent() {
		VerticalPanel panel = new VerticalPanel();
		RequestColumnsContainer container = new RequestColumnsContainer();
		container.setWidth(UserInterface.WIDTH);
		container.setHeight(UserInterface.HEIGHT);
		panel.add(new RouterToolbar(container));
		panel.add(container);
		panel.setTitle("Application Router");
		panel.setWidth(UserInterface.WIDTH); // Can't set to 100% because of drag and drop bug
		panel.setHeight(UserInterface.HEIGHT);
		return panel;
	}
}
