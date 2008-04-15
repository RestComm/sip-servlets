package org.mobicents.servlet.sip.testsuite;

import java.util.EventObject;
import java.util.HashMap;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import junit.framework.TestCase;



public abstract class ScenarioHarness extends TestCase implements SipListener {
	protected ProtocolObjects shootistProtocolObjects;

	protected ProtocolObjects shootmeProtocolObjects;

	protected static String transport = "udp";

	protected java.util.Map<SipProvider, SipListener> providerTable;

	// this flag determines whether the tested SIP Stack is shootist or shootme
	protected boolean testedImplFlag;
	
	private boolean autoDialog;

	public void setUp() throws Exception {
		
			this.shootistProtocolObjects = new ProtocolObjects(
					"shootist", "gov.nist", transport, autoDialog);

			this.shootmeProtocolObjects = new ProtocolObjects(
					"shootme" , "gov.nist",
					transport, autoDialog);
			/*
			 * if (!getImplementationPath().equals("gov.nist"))
			 * this.riProtocolObjects = new ProtocolObjects( super.getName(),
			 * super.getImplementationPath(), transport, true); else
			 * this.riProtocolObjects = tiProtocolObjects;
			 */

	}

	private SipListener getSipListener(EventObject sipEvent) {
		SipProvider source = (SipProvider) sipEvent.getSource();
		SipListener listener = (SipListener) providerTable.get(source);
		if (listener == null)
			throw new RuntimeException("Unexpected null listener");
		return listener;
	}

	public void processRequest(RequestEvent requestEvent) {
		getSipListener(requestEvent).processRequest(requestEvent);

	}

	public void processResponse(ResponseEvent responseEvent) {
		getSipListener(responseEvent).processResponse(responseEvent);

	}

	public void processTimeout(TimeoutEvent timeoutEvent) {
		getSipListener(timeoutEvent).processTimeout(timeoutEvent);
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		fail("unexpected exception");

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		getSipListener(transactionTerminatedEvent)
				.processTransactionTerminated(transactionTerminatedEvent);

	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		getSipListener(dialogTerminatedEvent).processDialogTerminated(
				dialogTerminatedEvent);

	}

	protected ScenarioHarness(String name, boolean autoDialog) {

		super(name);
		this.providerTable = new HashMap<SipProvider, SipListener>();

	}
	
	
	


}
