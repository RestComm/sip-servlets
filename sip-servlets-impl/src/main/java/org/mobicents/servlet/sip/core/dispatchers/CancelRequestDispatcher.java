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

import gov.nist.javax.sip.stack.SIPServerTransaction;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.Wrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class CancelRequestDispatcher extends RequestDispatcher {

	private static Log logger = LogFactory.getLog(CancelRequestDispatcher.class);
	
	public CancelRequestDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {
		super(sipApplicationDispatcher);
	}

	/**
	 * This method allows to route the CANCEL request along the application path 
	 * followed by the INVITE.
	 * We can distinguish cases here as per spec :
	 * Applications that acts as User Agent or B2BUA  
	 * 11.2.3 Receiving CANCEL 
	 * 
	 * When a CANCEL is received for a request which has been passed to an application, 
	 * and the application has not responded yet or proxied the original request, 
	 * the container responds to the original request with a 487 (Request Terminated) 
	 * and to the CANCEL with a 200 OK final response, and it notifies the application 
	 * by passing it a SipServletRequest object representing the CANCEL request. 
	 * The application should not attempt to respond to a request after receiving a CANCEL for it. 
	 * Neither should it respond to the CANCEL notification. 
	 * Clearly, there is a race condition between the container generating the 487 response 
	 * and the SIP servlet generating its own response. 
	 * This should be handled using standard Java mechanisms for resolving race conditions. 
	 * If the application wins, it will not be notified that a CANCEL request was received. 
	 * If the container wins and the servlet tries to send a response before 
	 * (or for that matter after) being notified of the CANCEL, 
	 * the container throws an IllegalStateException.
	 * 
	 * Applications that acts as proxy : 
	 * 10.2.6 Receiving CANCEL 
	 * if the original request has not been proxied yet the container responds 
	 * to it with a 487 final response otherwise, all branches are cancelled, 
	 * and response processing continues as usual
	 * In either case, the application is subsequently invoked with the CANCEL request. 
	 * This is a notification only, as the server has already responded to the CANCEL 
	 * and cancelled outstanding branches as appropriate. 
	 * The race condition between the server sending the 487 response and 
	 * the application proxying the request is handled as in the UAS case as 
	 * discussed in section 11.2.3 Receiving CANCEL.
	 * 
	 * @param sipProvider
	 * @param sipServletRequest
	 * @param transaction
	 * @param request
	 * @param poppedAddress
	 * @throws InvalidArgumentException 
	 * @throws SipException 
	 * @throws ParseException 
	 * @throws TransactionUnavailableException 
	 */
	public boolean routeCancel(SipProvider sipProvider, SipServletRequestImpl sipServletRequest) throws TransactionUnavailableException, ParseException, SipException, InvalidArgumentException {
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipApplicationDispatcher.getSipNetworkInterfaceManager();
		
		ServerTransaction transaction = (ServerTransaction) sipServletRequest.getTransaction();
		Request request = (Request) sipServletRequest.getMessage();		
		Dialog dialog = transaction.getDialog();
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
			logger.error("Impossible to send the ok to the CANCEL",e);
			sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			return false;
		} catch (InvalidArgumentException e) {
			logger.error("Impossible to send the ok to the CANCEL",e);
			sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			return false;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("checking what to do with the CANCEL " + sipServletRequest);
		}				
//		TransactionApplicationData dialogAppData = (TransactionApplicationData) dialog.getApplicationData();
		Transaction inviteTransaction = ((SIPServerTransaction) sipServletRequest.getTransaction()).getCanceledInviteTransaction();
		TransactionApplicationData dialogAppData = (TransactionApplicationData)inviteTransaction.getApplicationData();
		SipServletRequestImpl inviteRequest = (SipServletRequestImpl)
			dialogAppData.getSipServletMessage();
		if(logger.isDebugEnabled()) {
			logger.debug("message associated with the dialogAppData " +
					"of the CANCEL " + inviteRequest);
		}
//		Transaction inviteTransaction = inviteRequest.getTransaction();
		if(logger.isDebugEnabled()) {
			logger.debug("invite transaction associated with the dialogAppData " +
					"of the CANCEL " + inviteTransaction);
//			logger.debug(dialog.isServer());
		}
		TransactionApplicationData inviteAppData = (TransactionApplicationData) 
			inviteTransaction.getApplicationData();
		if(logger.isDebugEnabled()) {
			logger.debug("app data of the invite transaction associated with the dialogAppData " +
					"of the CANCEL " + inviteAppData);
		}		
		if(inviteAppData.getProxy() != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("proxying the CANCEL " + sipServletRequest);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("CANCEL a proxied request state = " + inviteRequest.getRoutingState());
			}
			// Routing State : PROXY case
			SipServletResponseImpl inviteResponse = (SipServletResponseImpl) 
				inviteRequest.createResponse(Response.REQUEST_TERMINATED);			
			if(!RoutingState.PROXIED.equals(inviteRequest.getRoutingState())) {				
				// 10.2.6 if the original request has not been proxied yet the container 
				// responds to it with a 487 final response
				try {					
					inviteRequest.setRoutingState(RoutingState.CANCELLED);
				} catch (IllegalStateException e) {
					logger.info("request already proxied, dropping the cancel");
					return false;
				}
				try {
					Response requestTerminatedResponse = (Response) inviteResponse.getMessage();
					((ServerTransaction)inviteTransaction).sendResponse(requestTerminatedResponse);				
				} catch (SipException e) {
					logger.error("Impossible to send the 487 to the INVITE transaction corresponding to CANCEL",e);
					sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				} catch (InvalidArgumentException e) {
					logger.error("Impossible to send the ok 487 to the INVITE transaction corresponding to CANCEL",e);
					sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				}
			} else {
				// otherwise, all branches are cancelled, and response processing continues as usual
				inviteAppData.getProxy().cancel();
			}
			return true;
		} else if(RoutingState.RELAYED.equals(inviteRequest.getRoutingState())) {
			if(logger.isDebugEnabled()) {
				logger.debug("Relaying the CANCEL " + sipServletRequest);
			}
			// Routing State : RELAYED - B2BUA case
			SipServletResponseImpl inviteResponse = (SipServletResponseImpl) 
				inviteRequest.createResponse(Response.REQUEST_TERMINATED);
			try {
				inviteRequest.setRoutingState(RoutingState.CANCELLED);
			} catch (IllegalStateException e) {
				logger.info("Final response already sent, dropping the cancel");
				return false;
			}
			try {
				Response requestTerminatedResponse = (Response) inviteResponse.getMessage();
				((ServerTransaction)inviteTransaction).sendResponse(requestTerminatedResponse);
			} catch (SipException e) {
				logger.error("Impossible to send the 487 to the INVITE transaction corresponding to CANCEL",e);
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			} catch (InvalidArgumentException e) {
				logger.error("Impossible to send the ok 487 to the INVITE transaction corresponding to CANCEL",e);
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			}
			//Forwarding the cancel on the other B2BUA side
			if(inviteRequest.getLinkedRequest() != null) {					
				SipServletRequestImpl cancelRequest = (SipServletRequestImpl)
					inviteRequest.getLinkedRequest().createCancel();
				cancelRequest.send();
				return true;
			} else {
				SipSessionImpl sipSession = inviteRequest.getSipSession();
				sipServletRequest.setSipSession(sipSession);
				Wrapper servletWrapper = (Wrapper) sipApplicationDispatcher.findSipApplication(sipSession.getKey().getApplicationName()).findChild(sipSession.getHandler());
				try{
					Servlet servlet = servletWrapper.allocate();
					servlet.service(sipServletRequest, null);
					return true;
				} catch (ServletException e) {				
					logger.error("An unexpected servlet exception occured while routing the CANCEL", e);
					// Sends a 500 Internal server error and stops processing.				
//						JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				} catch (IOException e) {				
					logger.error("An unexpected IO exception occured while routing the CANCEL", e);
					// Sends a 500 Internal server error and stops processing.				
//						JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				}
			}
		} else if(RoutingState.FINAL_RESPONSE_SENT.equals(inviteRequest.getRoutingState())) {
			if(logger.isDebugEnabled()) {
				logger.debug("the final response has already been sent, nothing to do here");
			}
			return true;
		} else if(RoutingState.INITIAL.equals(inviteRequest.getRoutingState()) ||
				RoutingState.SUBSEQUENT.equals(inviteRequest.getRoutingState())) {
			if(logger.isDebugEnabled()) {
				logger.debug("the app didn't do anything with the request forwarding the cancel on the other tx");				
			}
			javax.servlet.sip.Address poppedAddress = sipServletRequest.getPoppedRoute();
			
			if(poppedAddress==null){
				throw new IllegalArgumentException("The popped route shouldn't be null for not proxied requests.");
			}
			
			Request clonedRequest = (Request) request.clone();			
            // Branch Id will be assigned by the stack.
			String transport = JainSipUtils.findTransport(clonedRequest);		
			ViaHeader viaHeader = JainSipUtils.createViaHeader(
					sipNetworkInterfaceManager, transport, null);                                
        
            // Cancel is hop by hop so remove all other via headers.
            clonedRequest.removeHeader(ViaHeader.NAME);                
            clonedRequest.addHeader(viaHeader);           
            ClientTransaction clientTransaction = (ClientTransaction)inviteAppData.getTransaction();
            Request cancelRequest = clientTransaction.createCancel();
            sipProvider.getNewClientTransaction(cancelRequest).sendRequest();
			return true;
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("the initial request isn't in a good routing state ");				
			}
			throw new IllegalStateException("Initial request for CANCEL is in "+ sipServletRequest.getRoutingState() +" Routing state");
		}
	}
}
