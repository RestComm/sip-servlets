package org.mobicents.servlet.sip.router;

import java.io.InputStream;

/**
 * This interface simply exposes methods to reconfigure an Application Router
 * dynamically.
 *
 */
public interface ManagebleApplicationRouter {
	
	/**
	 * This method will completely reconfigure the application router and
	 * clean the previous state accumulated in the AR.
	 * 
	 * @param configuration How the configuration variable will be
	 * interpreted is AR specific. For most cases a String should work
	 * just fine (dar file contents, xml files content, rule files, etc)
	 */
	void configure(Object configuration);
	
	/**
	 * This method will provide the configuration that is currently active
	 * in the AR. It can be parsed and visualized by the management application.
	 * 
	 * @return the current configuration (possibly a string)
	 */
	Object getCurrentConfiguration();
}
