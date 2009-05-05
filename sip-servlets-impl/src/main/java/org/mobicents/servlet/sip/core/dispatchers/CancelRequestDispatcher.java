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

import gov.nist.javax.sip.ServerTransactionExt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipSession.State;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;

/**
 * <p>
 * This class implements the logic for routing CANCEL requests along the
 * application path followed by the INVITE. <br/> CANCEL deserves a special
 * treatment since they are routed hop by hop.
 * </p>
 * 
 * <p>
 * Algorithm used : We can distinguish 2 cases here as per spec :
 * <ul>
 * <li>
 * Applications that acts as User Agent 11.2.3 Receiving CANCEL :
 * 
 * When a CANCEL is received for a request which has been passed to an
 * application, and the application has not responded yet or proxied the
 * original request, the container responds to the original request with a 487
 * (Request Terminated) and to the CANCEL with a 200 OK final response, and it
 * notifies the application by passing it a SipServletRequest object
 * representing the CANCEL request. The application should not attempt to
 * respond to a request after receiving a CANCEL for it. Neither should it
 * respond to the CANCEL notification. Clearly, there is a race condition
 * between the container generating the 487 response and the SIP servlet
 * generating its own response. This should be handled using standard Java
 * mechanisms for resolving race conditions. If the application wins, it will
 * not be notified that a CANCEL request was received. If the container wins and
 * the servlet tries to send a response before (or for that matter after) being
 * notified of the CANCEL, the container throws an IllegalStateException.</li>
 * 
 * <li>
 * Applications that acts as proxy : 
 * 
 * 10.2.6 Receiving CANCEL if the original
 * request has not been proxied yet the container responds to it with a 487
 * final response otherwise, all branches are cancelled, and response processing
 * continues as usual In either case, the application is subsequently invoked
 * with the CANCEL request. This is a notification only, as the server has
 * already responded to the CANCEL and cancelled outstanding branches as
 * appropriate. The race condition between the server sending the 487 response
 * and the application proxying the request is handled as in the UAS case as
 * discussed in section 11.2.3 Receiving CANCEL.</li>
 * </ul>
 * </p>
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class CancelRequestDispatcher extends RequestDispatcher {

	private static transient Logger logger = Logger.getLogger(CancelRequestDispatcher.class);
	
	public CancelRequestDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {
		super(sipApplicationDispatcher);
	}

	/**
	 * {@inheritDoc} 
	 */
	public void dispatchMessage(final SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipApplicationDispatcher.getSipNetworkInterfaceManager();
		final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
		if(logger.isInfoEnabled()) {
			logger.info("Routing of Cancel Request " + sipServletRequest);
		}
		
		final Request request = (Request) sipServletRequest.getMessage();		
		/*
		 * WARNING: routing of CANCEL is special because CANCEL does not contain Route headers as other requests related
		 * to the dialog. But still it has to be routed through the app path
		 * of the INVITE
		 */
		/* If there is a proxy with the request, let's try to send it directly there.
		 * This is needed because of CANCEL which is a subsequent request that might
		 * not have Routes. For example if the callee has'n responded the caller still
		 * doesn't know the route-record and just sends cancel to the outbound proxy.
		 */	
//		boolean proxyCancel = false;
		try {
			// First we need to send OK ASAP because of retransmissions both for 
			//proxy or app
			ServerTransaction cancelTransaction = 
				(ServerTransaction) sipServletRequest.getTransaction();
			SipServletResponseImpl cancelResponse = (SipServletResponseImpl) 
			sipServletRequest.createResponse(200, "Canceling");
			Response cancelJsipResponse = (Response) cancelResponse.getMessage();
			cancelTransaction.sendResponse(cancelJsipResponse);
		} catch (SipException e) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to send the ok to the CANCEL", e);
		} catch (InvalidArgumentException e) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to send the ok to the CANCEL", e);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("checking what to do with the CANCEL " + sipServletRequest);
		}				
		final Transaction inviteTransaction = ((ServerTransactionExt) sipServletRequest.getTransaction()).getCanceledInviteTransaction();
		final TransactionApplicationData inviteAppData = (TransactionApplicationData)inviteTransaction.getApplicationData();
		final SipServletRequestImpl inviteRequest = (SipServletRequestImpl)
			inviteAppData.getSipServletMessage();
		final MobicentsSipSession sipSession = inviteRequest.getSipSession();
		sipServletRequest.setSipSession(sipSession);
		DispatchTask dispatchTask = new DispatchTask(sipServletRequest, sipProvider) {

			public void dispatch() throws DispatcherException {
				if(logger.isDebugEnabled()) {
					logger.debug("message associated with the inviteAppData " +
							"of the CANCEL " + inviteRequest);
				}
				if(logger.isDebugEnabled()) {
					logger.debug("invite transaction associated with the inviteAppData " +
							"of the CANCEL " + inviteTransaction);
				}
				if(logger.isDebugEnabled()) {
					logger.debug("app data of the invite transaction associated with the inviteAppData " +
							"of the CANCEL " + inviteAppData);
				}	
				if(logger.isDebugEnabled()) {
					logger.debug("routing state of the INVITE request for the CANCEL = " + inviteRequest.getRoutingState());
				}
				Proxy proxy = sipSession.getProxy();
				if(proxy != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("proxying the CANCEL " + sipServletRequest);
					}			
					// Routing State : PROXY case				
					if(!RoutingState.PROXIED.equals(inviteRequest.getRoutingState())) {				
						// 10.2.6 if the original request has not been proxied yet the container 
						// responds to it with a 487 final response
						try {
							send487Response(inviteTransaction, inviteRequest);
						} catch(IllegalStateException iae) {
							logger.info("request already proxied, dropping the cancel");
							return;
						}
					} else {
						// otherwise, all branches are cancelled, and response processing continues as usual
						proxy.cancel();
					}
				} else if(RoutingState.FINAL_RESPONSE_SENT.equals(inviteRequest.getRoutingState())) {
					if(logger.isDebugEnabled()) {
						logger.debug("the final response has already been sent, nothing to do here");
					}
				} else {			
							
					if(logger.isDebugEnabled()) {
						logger.debug("invite transaction of the CANCEL " + inviteTransaction);
					}
					if(logger.isDebugEnabled()) {
						logger.debug("invite message : 1xx response was generated ? " + ((SipServletRequestImpl)inviteAppData.getSipServletMessage()).is1xxResponseGenerated());
					}
					if(logger.isDebugEnabled()) {
						logger.debug("invite message : Final response was generated ? " + ((SipServletRequestImpl)inviteAppData.getSipServletMessage()).isFinalResponseGenerated());
					}
		            
	            	if(logger.isDebugEnabled()) {
	    				logger.debug("replying 487 to INVITE cancelled");				
	    			}
	            	//otherwise it means that this is for the app
	            	try {
						send487Response(inviteTransaction, inviteRequest);
					} catch(IllegalStateException iae) {
						logger.info("request already proxied, dropping the cancel");
						return;
					}
					try{
						callServlet(sipServletRequest);
					} catch (ServletException e) {
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while routing the following CANCEL " + request, e);
					} catch (IOException e) {				
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected IO exception occured while routing the following CANCEL " + request, e);
					} 	
				} 	
			}			
		};
		// Execute CANCEL without waiting for previous requests because if we wait for an INVITE to complete
		// all responses will be already sent by the time the CANCEL is out of the queue.
		((SipApplicationDispatcherImpl)this.sipApplicationDispatcher).getAsynchronousExecutor().execute(dispatchTask);
	}

	/**
	 * @param inviteTransaction
	 * @param inviteRequest
	 */
	private final static void send487Response(Transaction inviteTransaction, SipServletRequestImpl inviteRequest) throws IllegalStateException, DispatcherException {
		SipServletResponseImpl inviteResponse = (SipServletResponseImpl) 
			inviteRequest.createResponse(Response.REQUEST_TERMINATED);		
		
		inviteRequest.setRoutingState(RoutingState.CANCELLED);
		//JSR 289 Section 6.2.1.1 Cancel Message Processing : since receiving a CANCEL request causes the UAS 
		// to respond to an ongoing INVITE transaction with a non-2XX (specifically, 487) response, the SipSession state 
		// normally becomes TERMINATED as a result of the non-2XX final response sent back to the UAC.
		inviteRequest.getSipSession().setState(State.TERMINATED);
		try {
			Response requestTerminatedResponse = (Response) inviteResponse.getMessage();
			((ServerTransaction)inviteTransaction).sendResponse(requestTerminatedResponse);				
		} catch (SipException e) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to send the 487 to the INVITE transaction corresponding to CANCEL", e);
		} catch (InvalidArgumentException e) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to send the 487 to the INVITE transaction corresponding to CANCEL", e);
		}		
	}
}
