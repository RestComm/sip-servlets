/**
 * 
 */
package org.mobicents.servlet.management.client;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author root3
 *
 */
public class SipServletsManagement implements EntryPoint {

	public void onModuleLoad() {

	       RootPanel.get().add(new UserInterface());
	}

}
