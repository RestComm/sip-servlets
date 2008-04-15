/**
 * 
 */
package org.mobicents.servlet.sip.core;

import java.util.Map;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import org.apache.catalina.Wrapper;

/**
 * @author DRAGAN
 *
 */
public class SipApplicationDispatcherImpl implements SipApplicationDispatcher {

	/**
	 * 
	 */
	public SipApplicationDispatcherImpl() {}

	public void init() {
		// TODO Auto-generated method stub
		
	}

	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void processRequest(RequestEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void processResponse(ResponseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void processTimeout(TimeoutEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void addSipApplication(String sipApplication, Map<String, ? extends Wrapper> sipApplicationServlets) {
		// TODO Auto-generated method stub
		
	}

	public void removeSipApplication(String sipApplication) {
		// TODO Auto-generated method stub
		
	}

}
