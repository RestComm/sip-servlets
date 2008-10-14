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
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.web.tomcat.service.session.ConvergedSessionReplicationContext;
import org.jboss.web.tomcat.service.session.SnapshotSipManager;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * This class is responsible for routing and dispatching responses to applications according to JSR 289 Section 
 * 15.6 Responses, Subsequent Requests and Application Path 
 * 
 * It uses via header parameters that were previously set by the container to know which app has to be called 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ResponseDispatcher extends MessageDispatcher {
	private static Log logger = LogFactory.getLog(ResponseDispatcher.class);
	
	public ResponseDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
		super(sipApplicationDispatcher);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMessage(final SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {		
		final SipFactoryImpl sipFactoryImpl = sipApplicationDispatcher.getSipFactory();
		final SipServletResponseImpl sipServletResponse = (SipServletResponseImpl) sipServletMessage;
		final Response response = sipServletResponse.getResponse();
		final ListIterator<ViaHeader> viaHeaders = response.getHeaders(ViaHeader.NAME);				
		final ViaHeader viaHeader = viaHeaders.next();
		if(logger.isInfoEnabled()) {
			logger.info("viaHeader = " + viaHeader.toString());
		}
		//response meant for the container
		if(!sipApplicationDispatcher.isViaHeaderExternal(viaHeader)) {
			final ClientTransaction clientTransaction = (ClientTransaction) sipServletResponse.getTransaction();
			final Dialog dialog = sipServletResponse.getDialog();
			
			TransactionApplicationData applicationData = null;
			SipServletRequestImpl tmpOriginalRequest = null;
			if(clientTransaction != null) {
				applicationData = (TransactionApplicationData)clientTransaction.getApplicationData();
				if(applicationData.getSipServletMessage() instanceof SipServletRequestImpl) {
					tmpOriginalRequest = (SipServletRequestImpl)applicationData.getSipServletMessage();
				}
				//add the response for access from B2BUAHelper.getPendingMessages
				applicationData.addSipServletResponse(sipServletResponse);
			} //there is no client transaction associated with it, it means that this is a retransmission
			else if(dialog != null) {				
				applicationData = (TransactionApplicationData)dialog.getApplicationData();
				if(applicationData.getSipServletMessage() instanceof SipServletRequestImpl) {
					tmpOriginalRequest = (SipServletRequestImpl)applicationData.getSipServletMessage();
				}
				ProxyBranchImpl proxyBranch = applicationData.getProxyBranch();
				if(proxyBranch == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("retransmission received for a non proxy application, dropping the response " + response);
					}
					forwardResponseStatefully(sipServletResponse);
					return ;
				}
			}
			final SipServletRequestImpl originalRequest = tmpOriginalRequest;
			sipServletResponse.setOriginalRequest(originalRequest);
			
			String appNameNotDeployed = viaHeader.getParameter(APP_NOT_DEPLOYED);
			if(appNameNotDeployed != null && appNameNotDeployed.length() > 0) {
				forwardResponseStatefully(sipServletResponse);
				return ;
			}
			String noAppReturned = viaHeader.getParameter(NO_APP_RETURNED);
			if(noAppReturned != null && noAppReturned.length() > 0) {
				forwardResponseStatefully(sipServletResponse);
				return ;
			}
			String modifier = viaHeader.getParameter(MODIFIER);
			if(modifier != null && modifier.length() > 0) {
				forwardResponseStatefully(sipServletResponse);
				return ;
			}
			String appName = viaHeader.getParameter(RR_PARAM_APPLICATION_NAME); 
			boolean inverted = false;
			if(dialog != null && dialog.isServer()) {
				inverted = true;
			}
			final SipContext sipContext = sipApplicationDispatcher.findSipApplication(appName);
			final SipManager sipManager = (SipManager)sipContext.getManager();
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(appName, response, inverted);
			if(logger.isDebugEnabled()) {
				logger.debug("Trying to find session with following session key " + sessionKey);
			}			
			MobicentsSipSession tmpSession = null;
			try {
				tmpSession = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, null);
				//needed in the case of RE-INVITE by example
				if(tmpSession == null) {
					sessionKey = SessionManagerUtil.getSipSessionKey(appName, response, !inverted);
					if(logger.isDebugEnabled()) {
						logger.debug("Trying to find session with following session key " + sessionKey);
					}
					tmpSession = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, null);				
				}
				if(logger.isDebugEnabled()) {
					logger.debug("session found is " + tmpSession);
					if(tmpSession == null) {
						sipManager.dumpSipSessions();
					}
				}					
				
				if(tmpSession == null) {
					logger.error("Dropping the response since no active sip session has been found for it : " + response);
					return ;
				} else {
					sipServletResponse.setSipSession(tmpSession);					
				}			
				
				if(logger.isInfoEnabled()) {
					logger.info("route response on following session " + tmpSession.getId());
				}
			} catch (Exception e) {
				logger.error("unexpected exception happened while trying to get the sessions" ,e);
				// Ignore for now, it will fail something later and throw DispatcherException
			}
			final MobicentsSipSession session = tmpSession;
			final TransactionApplicationData finalApplicationData = applicationData;
			DispatchTask dispatchTask = new DispatchTask(sipServletResponse, sipProvider) {

				public void dispatch() throws DispatcherException {
					final boolean isDistributable = sipContext.getDistributable();
					if(isDistributable) {
						ConvergedSessionReplicationContext.enterSipappAndBindSessions(null, sipServletResponse, sipManager, true);
					} 
					try {
						try {
							session.setSessionCreatingTransaction(clientTransaction);
							session.setSessionCreatingDialog(dialog);
							if(originalRequest != null) {				
								originalRequest.setResponse(sipServletResponse);					
							}
							// RFC 3265 : If a 200-class response matches such a SUBSCRIBE or REFER request,
							// it creates a new subscription and a new dialog.
							if(Request.SUBSCRIBE.equals(sipServletResponse.getMethod()) && sipServletResponse.getStatus() >= 200 && sipServletResponse.getStatus() <= 300) {					
								session.addSubscription(sipServletResponse);
							}
							// See if this is a response to a proxied request
							// We can not use session.getProxyBranch() because all branches belong to the same session
							// and the session.proxyBranch is overwritten each time there is activity on the branch.
							ProxyBranchImpl proxyBranch = null;
							if(finalApplicationData != null) proxyBranch = finalApplicationData.getProxyBranch();
							if(proxyBranch != null) {
								sipServletResponse.setProxyBranch(proxyBranch);								
								// Update Session state
								session.updateStateOnResponse(sipServletResponse, true);
								proxyBranch.setResponse(sipServletResponse);

								// Notfiy the servlet
								if(logger.isDebugEnabled()) {
									logger.debug("Is Supervised enabled for this proxy branch ? " + proxyBranch.getProxy().getSupervised());
								}
								if(proxyBranch.getProxy().getSupervised()) {
									callServlet(sipServletResponse);
								}
								// Handle it at the branch
								proxyBranch.onResponse(sipServletResponse); 
								//we don't forward the response here since this has been done by the proxy
							}
							else {
								// Update Session state
								session.updateStateOnResponse(sipServletResponse, true);

								callServlet(sipServletResponse);
								forwardResponseStatefully(sipServletResponse);
							}
						} catch (ServletException e) {				
							logger.error("Unexpected servlet exception while processing the response : " + response, e);
							// Sends a 500 Internal server error and stops processing.				
							//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
						} catch (IOException e) {				
							logger.error("Unexpected io exception while processing the response : " + response, e);
							// Sends a 500 Internal server error and stops processing.				
							//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
						} catch (Throwable e) {				
							logger.error("Unexpected exception while processing response : " + response, e);
							// Sends a 500 Internal server error and stops processing.				
							//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
						}
					} finally {
						if (isDistributable) {
							if(logger.isInfoEnabled()) {
								logger.info("We are now after the servlet invocation, We replicate no matter what");
							}
							try {
								ConvergedSessionReplicationContext ctx = ConvergedSessionReplicationContext
								.exitSipapp();
								if(logger.isInfoEnabled()) {
									logger.info("Snapshot Manager " + ctx.getSoleSnapshotManager());
								}
								if (ctx.getSoleSnapshotManager() != null) {									
									((SnapshotSipManager)ctx.getSoleSnapshotManager()).snapshot(
											ctx.getSoleSipApplicationSession());
									((SnapshotSipManager)ctx.getSoleSnapshotManager()).snapshot(
											ctx.getSoleSipSession());
								} 
							} finally {
								ConvergedSessionReplicationContext.finishSipCacheActivity();
							}
						}
					}
				}
			};
			getConcurrencyModelExecutorService(sipServletMessage).execute(dispatchTask);
		} else {
			// No sessions here and no servlets called, no need for asynchronicity
			forwardResponseStatefully(sipServletResponse);
		}
	}
	
	/**
	 * this method is called when 
	 * a B2BUA got the response so we don't have anything to do here 
	 * or an app that didn't do anything with it
	 * or when handling the request an app had to be called but wasn't deployed
	 * the topmost via header is stripped from the response and the response is forwarded statefully
	 * @param sipServletResponse
	 */
	private final static void forwardResponseStatefully(SipServletResponseImpl sipServletResponse) {
		final ClientTransaction clientTransaction = (ClientTransaction) sipServletResponse.getTransaction();
		final Dialog dialog = sipServletResponse.getDialog();
		
		Response newResponse = (Response) sipServletResponse.getResponse().clone();
		newResponse.removeFirst(ViaHeader.NAME);
		ListIterator<ViaHeader> viaHeadersLeft = newResponse.getHeaders(ViaHeader.NAME);
		if(viaHeadersLeft.hasNext()) {
			//forward it statefully
			//TODO should decrease the max forward header to avoid infinite loop
			if(logger.isDebugEnabled()) {
				logger.debug("forwarding the response statefully " + newResponse);
			}
			TransactionApplicationData applicationData = null; 
			if(clientTransaction != null) {
				applicationData = (TransactionApplicationData)clientTransaction.getApplicationData();
				if(logger.isDebugEnabled()) {
					logger.debug("ctx application Data " + applicationData);
				}
			}
			//there is no client transaction associated with it, it means that this is a retransmission
			else if(dialog != null){	
				applicationData = (TransactionApplicationData)dialog.getApplicationData();
				if(logger.isDebugEnabled()) {
					logger.debug("dialog application data " + applicationData);
				}
			}								
			ServerTransaction serverTransaction = (ServerTransaction)
				applicationData.getTransaction();
			try {					
				serverTransaction.sendResponse(newResponse);
			} catch (SipException e) {
				logger.error("cannot forward the response statefully" , e);
			} catch (InvalidArgumentException e) {
				logger.error("cannot forward the response statefully" , e);
			}				
		} else {
			//B2BUA case we don't have to do anything here
			//no more via header B2BUA is the end point
			if(logger.isDebugEnabled()) {
				logger.debug("Not forwarding the response statefully. " +
						"It was either an endpoint or a B2BUA, ie an endpoint too " + newResponse);
			}
		}			
	}
}
