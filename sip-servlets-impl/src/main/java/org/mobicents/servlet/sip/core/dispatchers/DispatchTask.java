/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.core.dispatchers;

import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
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
	
	private static transient Logger logger = Logger.getLogger(DispatchTask.class);
	
	private SipServletMessageImpl sipServletMessage;
	private SipProvider sipProvider;
	
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
				if(!Request.ACK.equalsIgnoreCase(sipServletRequest.getMethod()) ||
						!Request.PRACK.equalsIgnoreCase(sipServletRequest.getMethod())) {
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, (ServerTransaction) sipServletRequest.getTransaction(), (Request) sipServletRequest.getMessage(), sipProvider);
				}
			}
		}		
	}
	
}
