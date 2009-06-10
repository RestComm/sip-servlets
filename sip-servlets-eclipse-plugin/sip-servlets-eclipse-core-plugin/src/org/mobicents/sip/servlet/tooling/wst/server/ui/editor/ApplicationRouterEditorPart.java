package org.mobicents.sip.servlet.tooling.wst.server.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;

public class ApplicationRouterEditorPart extends
		ServerEditorPart {
	private Browser browser;
	private Label startServerLabel;
	private Composite parent;

	private void loadMgmtConsole() {
		try {
			parent.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (startServerLabel != null) {
						startServerLabel.dispose();
						startServerLabel = null;
					}
					if(browser != null) {
						browser.dispose();
						browser = null;
					}
					browser = new Browser(parent, SWT.NONE);
					String host = getServer().getOriginal().getHost();
					int httpPort = 8080;
					ServerPort[] ports = getServer().getOriginal()
							.getServerPorts(null);
					for (ServerPort port : ports) {
						if (port.getProtocol().equalsIgnoreCase("http")) {
							httpPort = port.getPort();
						}
					}
					browser.setUrl("http://" + host + ":" + Integer.toString(httpPort)
							+ "/sip-servlets-management");
					parent.redraw();
					browser.redraw();
					browser.update();
					parent.update();
					
					// The following hack is need to redraw correctly the browser.
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					new Thread(){
						public void run() {
							parent.setSize(parent.getSize().x-1,parent.getSize().y);
							parent.getShell().redraw();
					}
					}.run();
					
				}

			});
		} catch (Exception e) {
			e = e;
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		try {
			this.parent = parent;
	         
	         int state = getServer().getOriginal().getServerState();
	         if(state == IServer.STATE_STARTED) {
	        	 loadMgmtConsole();
	         } else {
	        	 if(browser != null) browser.dispose();
	        	 startServerLabel = new Label(parent, SWT.NONE);
	        	 startServerLabel.setText("Sip Servlets management is only available when the server is running. Please start the server and the console will be loaded automatically.");
	        	 getServer().getOriginal().addServerListener(new IServerListener() {

					public void serverChanged(ServerEvent event) {
						if(event.getState() == IServer.STATE_STARTED) {
							loadMgmtConsole();
						}
						
					}
	        		 
	        	 });
	         }
	   } catch (SWTError e) {
	   }
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}

}
