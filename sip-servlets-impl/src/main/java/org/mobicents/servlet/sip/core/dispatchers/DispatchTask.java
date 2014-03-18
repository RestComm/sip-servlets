/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.core.dispatchers;

import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;

/**
 * Generic class that allows async execution of tasks in one of the executors depending
 * on what session isolation you need. Error handling is also done here.
 * 
 * @author Vladimir Ralev
 *
 */
public abstract class DispatchTask implements Runnable {
	
	private static final Logger logger = Logger.getLogger(DispatchTask.class);
	
	protected SipServletMessageImpl sipServletMessage;
	protected SipProvider sipProvider;
	
	public DispatchTask(SipServletMessageImpl sipServletMessage, SipProvider sipProvider) {
		this.sipProvider = sipProvider;
		this.sipServletMessage = sipServletMessage;
	}

	abstract public void dispatch() throws DispatcherException;

	public void run() {
		dispatchAndHandleExceptions();
	}

	public void dispatchAndHandleExceptions () {
		try {
			dispatch();
		} catch (Throwable t) {
			logger.error("Unexpected exception while processing message " + sipServletMessage, t);
			
			if(sipServletMessage instanceof SipServletRequestImpl) {
				SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
				if(!Request.ACK.equalsIgnoreCase(sipServletRequest.getMethod()) &&
						!Request.PRACK.equalsIgnoreCase(sipServletRequest.getMethod())) {
					MessageDispatcher.sendErrorResponse(sipServletRequest.getSipSession().getSipApplicationSession().getSipContext().getSipApplicationDispatcher(), Response.SERVER_INTERNAL_ERROR, sipServletRequest, sipProvider);					
				}
			}
		}		
	}
	
}
