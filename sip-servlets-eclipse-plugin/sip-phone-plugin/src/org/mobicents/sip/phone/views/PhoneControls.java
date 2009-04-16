package org.mobicents.sip.phone.views;

import java.util.Hashtable;

import net.java.sip.communicator.impl.protocol.sip.ProtocolProviderServiceSipImpl;
import net.java.sip.communicator.impl.protocol.sip.SipActivator;
import net.java.sip.communicator.impl.protocol.sip.SipStackSharing;
import net.java.sip.communicator.service.protocol.Call;
import net.java.sip.communicator.service.protocol.CallParticipant;
import net.java.sip.communicator.service.protocol.CallState;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicTelephony;
import net.java.sip.communicator.service.protocol.ProtocolProviderService;
import net.java.sip.communicator.service.protocol.RegistrationState;
import net.java.sip.communicator.service.protocol.event.CallChangeEvent;
import net.java.sip.communicator.service.protocol.event.CallChangeListener;
import net.java.sip.communicator.service.protocol.event.CallParticipantEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeEvent;
import net.java.sip.communicator.service.protocol.event.RegistrationStateChangeListener;
import net.java.sip.communicator.util.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

public class PhoneControls extends Composite{
	
	private static final Logger logger = Logger.getLogger(PhoneControls.class);
	
	private SipPhoneView sipPhoneView;

	public PhoneControls(Composite parent, int style) {
		super(parent, style);
		Composite group = new Composite(parent, SWT.NONE);
		//group.setText("Call control");
		GridLayout grid = new GridLayout(2, false);
		group.setLayout(grid);
		
		GridData cellWidthGridHint = new GridData();
		cellWidthGridHint.grabExcessHorizontalSpace = true;
		cellWidthGridHint.minimumWidth = 180;
		new Label(group, SWT.NONE).setText("Server");
		final Text server = new Text(group, SWT.BORDER);
		server.setText("127.0.0.1");
		server.setLayoutData(cellWidthGridHint);
		
		new Label(group, SWT.NONE).setText("Server Port");
		final Text port = new Text(group, SWT.BORDER);
		port.setText("5080");
		port.setLayoutData(cellWidthGridHint);
		
		new Label(group, SWT.NONE).setText("Local SIP Port");
		final Text sipPort = new Text(group, SWT.BORDER);
		sipPort.setText("5060");
		sipPort.setLayoutData(cellWidthGridHint);
		
		new Label(group, SWT.NONE).setText("Dial URI");
		final Text uri = new Text(group, SWT.BORDER);
		uri.setText("sip:server@127.0.0.1:5080");
		uri.setLayoutData(cellWidthGridHint);

		
		final Button registerButton = new Button(group, SWT.NONE);
		//registerButton.setLayoutData(new RowData(100, 50)); // Eclipse crashes wow
		final Button callButton = new Button(group, SWT.NONE);
		final Label callState = new Label(group, SWT.NONE);
		callState.setText("Not registered");
		callButton.setEnabled(false);
		//callButton.setLayoutData(new RowData(100, 50));

		registerButton.setText("Register    ");
		
		// Register button behaviour
		registerButton.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				SipCommunicatorOSGIBootstrap sipCommunicator = sipPhoneView.getSipCommunicator();
				ProtocolProviderService service = sipCommunicator.getProtocolProviderService();
				if(event.widget == registerButton) {

					try {
						// If we are already registered at the server, pressing this button again
						// must unregister us
						if(service != null && service.isRegistered()) {
							service.unregister();
						} else {
							// Otherwsie proceed with regular registration
							Hashtable props = SipCommunicatorOSGIBootstrap.createSipAccountProperties("user", "display", "pass",
									server.getText(), port.getText(), server.getText(), port.getText());
							
							// Reset the service
							if(service != null) {
								service.shutdown();
								service = null;
								System.gc();
							}
							
							// If successful do new initialization
							if(service == null) {
								SipActivator.getConfigurationService().
			                    setProperty(SipStackSharing.PREFERRED_CLEAR_PORT_PROPERTY_NAME, sipPort.getText());
								sipCommunicator.setUp(props);
								service = sipCommunicator.getProtocolProviderService();
							}
							
							service.addRegistrationStateChangeListener(
									new RegistrationStateChangeListener() {

										@Override
										public void registrationStateChanged(final RegistrationStateChangeEvent evt) {
											getDisplay().asyncExec(new Runnable() {
												public void run() {
													callState.setText(evt.getNewState().getStateName());
													if(evt.getNewState().equals(RegistrationState.REGISTERED)) {
														callButton.setEnabled(true);
														registerButton.setText("Unregister");
													} else if(evt.getNewState().equals(RegistrationState.UNREGISTERED)){
														registerButton.setText("Register");
														callButton.setEnabled(false);
													}
												}
											});
										}
									});
							service.register(new SecurityAuthorityImpl("pass".toCharArray()));
						}

					} catch (OperationFailedException e) {
						logger.error(e);
					}
				}
			}

		});
		
		
		// Call button behaviour
		callButton.setText("Call");
		callButton.addListener(SWT.Selection, new Listener() {

			CallParticipant remoteSide;
			@Override
			public void handleEvent(Event event) {
				try {
					OperationSetBasicTelephony telephony
					= (OperationSetBasicTelephony) sipPhoneView.getSipCommunicator().getProtocolProviderService().getOperationSet(
							OperationSetBasicTelephony.class);
					if(remoteSide != null) {
						telephony.hangupCallParticipant(remoteSide);
					} else {
						String address = uri.getText();

						Call sipCall = telephony.createCall(address);
						sipCall.addCallChangeListener(new CallChangeListener() {

							@Override
							public void callParticipantAdded(
									final CallParticipantEvent evt) {
							}

							@Override
							public void callParticipantRemoved(
									final CallParticipantEvent evt) {
							}

							@Override
							public void callStateChanged(final CallChangeEvent evt) {
								getDisplay().asyncExec(new Runnable() {

									@Override
									public void run() {

										logger.info("callStateChanged");
										Object obj = evt.getNewValue();
										if(obj instanceof CallState) {
											CallState cs = (CallState) obj;
											if(!cs.equals(CallState.CALL_ENDED)) {
												logger.info("Establishing a call");
												remoteSide = evt.getSourceCall().getCallParticipants().next();
												sipPhoneView.getKeypad().setRemoteSide(remoteSide);
												callButton.setText("End");
											} else {
												logger.info("Ending call");
												callButton.setText("Call");
												remoteSide = null;
												sipPhoneView.getKeypad().setRemoteSide(null);
											}
											callState.setText(cs.getStateString());
										}
									}
								});
							}
						});
					}
				}
				catch (Exception e) {
					logger.error(e);
				}
			}
		});
	}

	public SipPhoneView getSipPhoneView() {
		return sipPhoneView;
	}

	public void setSipPhoneView(SipPhoneView sipPhoneView) {
		this.sipPhoneView = sipPhoneView;
	}

}
