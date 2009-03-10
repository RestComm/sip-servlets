package org.mobicents.sip.phone.views;

import net.java.sip.communicator.service.audionotifier.AudioNotifierService;
import net.java.sip.communicator.util.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

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
	
	public Keypad(Composite parent, int style) {
		super(parent, style);
		
		buttonListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				if(event.widget instanceof Button) {
					Button b = (Button) event.widget;
					String digit = b.getText();
					AudioNotifierService audio = SipCommunicatorOSGIBootstrap.getAudioNotifier();
					int d = -1;
					try {
						d = Integer.parseInt(digit);
					} catch (Throwable t) {}
					if(d>=0) {
						
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
	
	public Button addButton(Composite parent, String text) {
		Button b = new Button(parent, SWT.PUSH);
		b.setText(text);
		b.addListener(SWT.Selection, buttonListener);
		return b;
	}

}
