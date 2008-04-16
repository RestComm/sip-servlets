/**
 * 
 */
package org.mobicents.servlet.management.client;


import org.mobicents.servlet.management.client.router.RequestColumnsContainer;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.gwt.components.client.Canvas;
import com.gwtext.client.widgets.Panel;

/**
 * @author root3
 *
 */
public class SipServletsManagement implements EntryPoint {

	public void onModuleLoad() {

	       RootPanel.get().add(new UserInterface());
	}

}
