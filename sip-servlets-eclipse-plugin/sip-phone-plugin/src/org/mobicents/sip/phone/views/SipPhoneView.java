package org.mobicents.sip.phone.views;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.mobicents.sip.phone.SipPhoneActivator;
import org.osgi.framework.ServiceReference;




public class SipPhoneView extends ViewPart {

	private VisualizationService vis;
	private SipCommunicatorOSGIBootstrap sipCommunicator;
	private Keypad keypad;

	public void createPartControl(final Composite parent) {
		sipCommunicator = new SipCommunicatorOSGIBootstrap(SipPhoneActivator.getDefault().getBundle().getBundleContext());
		Composite main = new Composite(parent, SWT.NONE);
		GridLayout verticalLayout = new GridLayout();
		verticalLayout.numColumns = 1;
		verticalLayout.verticalSpacing = 1;
		verticalLayout.horizontalSpacing = 1;
		main.setLayout(verticalLayout);
		Composite upper = new Composite(main, SWT.NONE);
		Group lower = new Group(main, SWT.NONE);
		
		RowLayout upperRowLayout = new RowLayout();
		upperRowLayout.justify = false;
		upper.setLayout(upperRowLayout);
		vis = new VisualizationCanvas(upper, SWT.NO_REDRAW_RESIZE, 500, 100, 130);
		
		RowLayout lowerRowLayout = new RowLayout();
		lowerRowLayout.justify = false;
		lowerRowLayout.wrap = false;
		lowerRowLayout.marginLeft = 0;
		lowerRowLayout.marginRight = 0;
		lowerRowLayout.spacing = 0;
		lower.setLayout(lowerRowLayout);
		
		PhoneControls phoneControls = new PhoneControls(lower, SWT.NONE);
		phoneControls.setSipPhoneView(this);
		keypad = new Keypad(lower, SWT.NONE);
		
		// TODO: Add properties so we can support multiple instances
		SipPhoneActivator.getDefault().getBundle().getBundleContext().registerService(
				VisualizationService.class.getName(), vis, null);
		
		//phoneControls.setLayoutData(new RowData(150, 310));
	}
	
	public static VisualizationService getVisualizationCanvas() {
		ServiceReference ref = SipPhoneActivator.getDefault().getBundle().getBundleContext().getServiceReference(
				VisualizationService.class.getName());
		return (VisualizationService)SipPhoneActivator.getDefault().getBundle().getBundleContext().getService(ref);
	}
	
	@Override
	public void setFocus() {
	}

	public VisualizationService getVis() {
		return vis;
	}

	public SipCommunicatorOSGIBootstrap getSipCommunicator() {
		return sipCommunicator;
	}

	public void setVis(VisualizationService vis) {
		this.vis = vis;
	}

	public void setSipCommunicator(SipCommunicatorOSGIBootstrap sipCommunicator) {
		this.sipCommunicator = sipCommunicator;
	}


}