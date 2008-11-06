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
