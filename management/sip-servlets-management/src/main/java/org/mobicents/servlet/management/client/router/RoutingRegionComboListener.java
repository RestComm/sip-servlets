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