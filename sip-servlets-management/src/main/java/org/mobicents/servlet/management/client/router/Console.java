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

import com.gwtext.client.widgets.Window;

public class Console extends Window{
	private static Console instance = null;
	private static String log = "<b>Console initialized</b><hr/><br/>";
	
	public static Console getInstance() {
		if(instance == null) {
			instance = new Console();
			instance.setTitle("Console");  
			instance.setClosable(true);  
			instance.setWidth(600);  
			instance.setHeight(350);  
			instance.setPlain(true);  
			instance.setAutoScroll(true);
			instance.setCloseAction(Window.HIDE);
			instance.setHtml(log);
		}
		return instance;
	}
	
	public static void println(String html) {
		log += html + "<br/>";
		instance.setHtml(log);
	}
	
	public static void error(Object object) {
		instance.show(); // Pop the window on error.
		println("<b>[ERROR] " + object.toString() + "</b>");
		instance.setVisible(true);
		instance.setActive();
	}
	
	public static void warn(Object object) {
		instance.show(); // Pop the window on warning.
		println("[WARN] " + object.toString());
		instance.setVisible(true);
		instance.setActive();
	}
	
	public static void info(Object object) {
		println("[INFO] " + object.toString());
	}	
	
	public static void fatal(Object object) {
		instance.show(); // Pop the window on fatal error.
		println("[FATAL] " + object.toString());
		instance.setVisible(true);
		instance.setActive();
	}
}
