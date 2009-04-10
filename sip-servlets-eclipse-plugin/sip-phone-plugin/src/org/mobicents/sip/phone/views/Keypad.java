package org.mobicents.sip.phone.views;

import java.net.URL;
import java.util.Iterator;

import net.java.sip.communicator.service.audionotifier.AudioNotifierService;
import net.java.sip.communicator.service.protocol.CallParticipant;
import net.java.sip.communicator.service.protocol.DTMFTone;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetDTMF;
import net.java.sip.communicator.util.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.mobicents.sip.phone.SipPhoneActivator;

public class Keypad extends Composite{
	
	private static final Logger logger = Logger.getLogger(Keypad.class);
	
	private String[] dtmfFiles = new String[] {
			"one_1.wav",
			"two_2.wav",
			"three_3.wav",
			"four_4.wav",
			"five_5.wav",
			"six_6.wav",
			"seven_7.wav",
			"eight_8.wav",
			"nine_9.wav"
	};
	
	private Listener buttonListener;
	private CallParticipant remoteSide;
	
	public Keypad(Composite parent, int style) {
		super(parent, style);
		
		buttonListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				if(event.widget instanceof Button) {
					Button b = (Button) event.widget;
					String digit = b.getText();
					sendDtmfTone(new DTMFTone(digit));
					AudioNotifierService audio = SipCommunicatorOSGIBootstrap.getAudioNotifier();
					int buttonNumber = -1;
					try {
						buttonNumber = Integer.parseInt(digit);
					} catch (Throwable t) {}
					if(buttonNumber >= 0) {
						URL url = SipPhoneActivator.getDefault().getBundle().getEntry(
								"/resources/sounds/" + dtmfFiles[buttonNumber+1]);
						audio.createAudio(url).play();
					}
					logger.info(digit);
				}
				
			}
			
		};
		
		Composite group = new Group(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		group.setLayout(gridLayout);
		addButton(group, "1");
		addButton(group, "2");
		addButton(group, "3");
		addButton(group, "4");
		addButton(group, "5");
		addButton(group, "6");
		addButton(group, "7");
		addButton(group, "8");
		addButton(group, "9");
		addButton(group, "#");
		addButton(group, "0");
		addButton(group, "*");

	}
	
	private void sendDtmfTone(DTMFTone dtmfTone)
    {
        if(remoteSide != null) {
                CallParticipant participant
                    = remoteSide;

                if (participant.getProtocolProvider()
                    .getOperationSet(OperationSetDTMF.class) != null)
                {
                    OperationSetDTMF dtmfOpSet
                        = (OperationSetDTMF) participant.getProtocolProvider()
                            .getOperationSet(OperationSetDTMF.class);

                    try {
						dtmfOpSet.sendDTMF(participant, dtmfTone);
					} catch (Exception e) {
						logger.error(e);
					}
                }
            }
        
    }
	
	public Button addButton(Composite parent, String text) {
		Button b = new Button(parent, SWT.PUSH);
		b.setText(text);
		b.addListener(SWT.Selection, buttonListener);
		return b;
	}

	public CallParticipant getRemoteSide() {
		return remoteSide;
	}

	public void setRemoteSide(CallParticipant remoteSide) {
		this.remoteSide = remoteSide;
	}

}
