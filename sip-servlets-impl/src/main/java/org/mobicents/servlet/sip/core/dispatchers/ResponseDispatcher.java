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

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.header.ViaHeader;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ResponseDispatcher extends MessageDispatcher {
	private static Log logger = LogFactory.getLog(ResponseDispatcher.class);
	
	public ResponseDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
		super(sipApplicationDispatcher);
	}

	/**
	 * Method for routing responses to apps. This uses via header to know which 
	 * app has to be called 
	 * @param responseEvent the jain sip response received
	 * @return true if we need to continue routing
	 */
	public boolean routeResponse(ResponseEvent responseEvent) {
		final SipFactoryImpl sipFactoryImpl = (SipFactoryImpl) sipApplicationDispatcher.getSipFactory();
		
		Response response = responseEvent.getResponse();
		ListIterator<ViaHeader> viaHeaders = response.getHeaders(ViaHeader.NAME);				
		ViaHeader viaHeader = viaHeaders.next();
		if(logger.isInfoEnabled()) {
			logger.info("viaHeader = " + viaHeader.toString());
		}
		//response meant for the container
		if(!sipApplicationDispatcher.isViaHeaderExternal(viaHeader)) {
			ClientTransaction clientTransaction = responseEvent.getClientTransaction();
			Dialog dialog = responseEvent.getDialog();
			
			TransactionApplicationData applicationData = null;
			SipServletRequestImpl originalRequest = null;
			if(clientTransaction != null) {
				applicationData = (TransactionApplicationData)clientTransaction.getApplicationData();
				if(applicationData.getSipServletMessage() instanceof SipServletRequestImpl) {
					originalRequest = (SipServletRequestImpl)applicationData.getSipServletMessage();
				}
			} //there is no client transaction associated with it, it means that this is a retransmission
			else if(dialog != null) {	
				applicationData = (TransactionApplicationData)dialog.getApplicationData();
				ProxyBranchImpl proxyBranch = applicationData.getProxyBranch();
				if(proxyBranch == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("retransmission received for a non proxy application, dropping the response " + response);
					}
					return true;
				}
			}
			
			String appNameNotDeployed = viaHeader.getParameter(APP_NOT_DEPLOYED);
			if(appNameNotDeployed != null && appNameNotDeployed.length() > 0) {
				return true;
			}
			String noAppReturned = viaHeader.getParameter(NO_APP_RETURNED);
			if(noAppReturned != null && noAppReturned.length() > 0) {
				return true;
			}
			String modifier = viaHeader.getParameter(MODIFIER);
			if(modifier != null && modifier.length() > 0) {
				return true;
			}
			String appName = viaHeader.getParameter(RR_PARAM_APPLICATION_NAME); 
			boolean inverted = false;
			if(dialog != null && dialog.isServer()) {
				inverted = true;
			}
			SipContext sipContext = sipApplicationDispatcher.findSipApplication(appName);
			SipManager sipManager = (SipManager)sipContext.getManager();
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(appName, response, inverted);
			if(logger.isDebugEnabled()) {
				logger.debug("Trying to find session with following session key " + sessionKey);
			}
			SipSessionImpl session = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, null);
			//needed in the case of RE-INVITE by example
			if(session == null) {
				sessionKey = SessionManagerUtil.getSipSessionKey(appName, response, !inverted);
				if(logger.isDebugEnabled()) {
					logger.debug("Trying to find session with following session key " + sessionKey);
				}
				session = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, null);				
			}
			if(logger.isDebugEnabled()) {
				logger.debug("session found is " + session);
				if(session == null) {
					sipManager.dumpSipSessions();
				}
			}	
			if(logger.isInfoEnabled()) {
				logger.info("route response on following session " + sessionKey);
			}
			
			if(session == null) {
				logger.error("Dropping the response since no active sip session has been found for it : " + response);
				return false;
			}
			
			// Transate the repsponse to SipServletResponse
			SipServletResponseImpl sipServletResponse = new SipServletResponseImpl(
					response, 
					sipFactoryImpl,
					clientTransaction, 
					session, 
					dialog, 
					originalRequest);				
			
			try {
				// See if this is a response to a proxied request
				if(originalRequest != null) {				
					originalRequest.setLastFinalResponse(sipServletResponse);					
				}
								
				// We can not use session.getProxyBranch() because all branches belong to the same session
				// and the session.proxyBranch is overwritten each time there is activity on the branch.				
				ProxyBranchImpl proxyBranch = applicationData.getProxyBranch();
				if(proxyBranch != null) {
					sipServletResponse.setProxyBranch(proxyBranch);
					// Update Session state
					session.updateStateOnResponse(sipServletResponse, true);
					
					// Handle it at the branch
					proxyBranch.onResponse(sipServletResponse); 
					
					// Notfiy the servlet
					if(logger.isDebugEnabled()) {
						logger.debug("Is Supervised enabled for this proxy branch ? " + proxyBranch.getProxy().getSupervised());
					}
					if(proxyBranch.getProxy().getSupervised()) {
						SipApplicationDispatcherImpl.callServlet(sipServletResponse);
					}
					return false;
				}
				else {
					// Update Session state
					session.updateStateOnResponse(sipServletResponse, true);

					SipApplicationDispatcherImpl.callServlet(sipServletResponse);
				}
			} catch (ServletException e) {				
				logger.error("Unexpected servlet exception while processing the response : " + response, e);
				// Sends a 500 Internal server error and stops processing.				
	//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
				return false;
			} catch (IOException e) {				
				logger.error("Unexpected io exception while processing the response : " + response, e);
				// Sends a 500 Internal server error and stops processing.				
	//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
				return false;
			} catch (Throwable e) {				
				logger.error("Unexpected exception while processing response : " + response, e);
				// Sends a 500 Internal server error and stops processing.				
	//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
				return false;
			}
//			finally {
//				if(session.isReadyToInvalidate()) {
//					session.onTerminatedState();
//				}
//			}
		}
		return true;		
	}
}
