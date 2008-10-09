package org.mobicents.servlet.sip.core.dispatchers;

import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;

public abstract class DispatchTask implements Runnable {
	
	private static transient Log logger = LogFactory
	.getLog(DispatchTask.class);
	
	private SipServletMessageImpl sipServletMessage;
	private SipProvider sipProvider;
	
	public DispatchTask(SipServletMessageImpl sipServletMessage, SipProvider sipProvider) {
		this.sipProvider = sipProvider;
		this.sipServletMessage = sipServletMessage;
	}

	abstract public void dispatch() throws DispatcherException;

	public void run() {
		try {
			dispatch();
		} catch (Throwable t) {
			logger.error("Unexpected exception while processing message " + sipServletMessage, t);
			
			if(sipServletMessage instanceof SipServletRequestImpl) {
				SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
				if(!Request.ACK.equalsIgnoreCase(sipServletRequest.getMethod())) {
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, (ServerTransaction) sipServletRequest.getTransaction(), (Request) sipServletRequest.getMessage(), sipProvider);
				}
			}

		}

	}

}
