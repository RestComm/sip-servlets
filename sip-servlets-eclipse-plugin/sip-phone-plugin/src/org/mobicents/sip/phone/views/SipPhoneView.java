package org.mobicents.sip.phone.views;


import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.mobicents.sip.phone.SipPhoneActivator;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;




public class SipPhoneView extends ViewPart {

	private VisualizationService outVisualization;
	private VisualizationService inVisualization;
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
		Composite lower = new Composite(main, SWT.NONE);
		
		RowLayout upperRowLayout = new RowLayout();
		upperRowLayout.justify = false;
		upper.setLayout(upperRowLayout);
		
		outVisualization = new VisualizationCanvas(upper, SWT.NO_REDRAW_RESIZE, 250, 80, 90);
		inVisualization = new VisualizationCanvas(upper, SWT.NO_REDRAW_RESIZE, 250, 80, 90);
		
		RowLayout lowerRowLayout = new RowLayout();
		lowerRowLayout.justify = true;
		lowerRowLayout.wrap = false;
		lowerRowLayout.marginLeft = 0;
		lowerRowLayout.marginRight = 0;
		lowerRowLayout.spacing = 0;
		
		lower.setLayout(lowerRowLayout);
		
		PhoneControls phoneControls = new PhoneControls(lower, SWT.NONE);
		phoneControls.setSipPhoneView(this);
		keypad = new Keypad(lower, SWT.NONE);
		
		Dictionary propertiesOut = new Properties();
		propertiesOut.put("TYPE", "OUT");
		SipPhoneActivator.getDefault().getBundle().getBundleContext().registerService(
				VisualizationService.class.getName(), outVisualization, propertiesOut);
		
		Dictionary propertiesIn = new Properties();
		propertiesIn.put("TYPE", "IN");
		SipPhoneActivator.getDefault().getBundle().getBundleContext().registerService(
				VisualizationService.class.getName(), inVisualization, propertiesIn);
		
		//phoneControls.setLayoutData(new RowData(150, 310));
	}
	
	public static VisualizationService getVisualizationService(String filter) {
		ServiceReference ref = null;
		try {
			ref = SipPhoneActivator.getDefault().getBundle().getBundleContext().getServiceReferences(
					VisualizationService.class.getName(), filter)[0];
		} catch (InvalidSyntaxException e) {
		}
		return (VisualizationService)SipPhoneActivator.getDefault().getBundle().getBundleContext().getService(ref);
	}
	
	
	@Override
	public void setFocus() {
	}

	public SipCommunicatorOSGIBootstrap getSipCommunicator() {
		return sipCommunicator;
	}


	public void setSipCommunicator(SipCommunicatorOSGIBootstrap sipCommunicator) {
		this.sipCommunicator = sipCommunicator;
	}

	public Keypad getKeypad() {
		return keypad;
	}


}