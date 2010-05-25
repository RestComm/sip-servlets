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

import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.IOException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.sip.SipSession.State;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
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
import org.mobicents.servlet.sip.proxy.ProxyImpl;
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
	private static final Logger logger = Logger.getLogger(ResponseDispatcher.class);
	
	public ResponseDispatcher() {}
	
//	public ResponseDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
//		super(sipApplicationDispatcher);
//	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMessage(final SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {		
		final SipFactoryImpl sipFactoryImpl = sipApplicationDispatcher.getSipFactory();
		final SipServletResponseImpl sipServletResponse = (SipServletResponseImpl) sipServletMessage;
		final Response response = sipServletResponse.getResponse();
		final ListIterator<ViaHeader> viaHeaders = response.getHeaders(ViaHeader.NAME);				
		final ViaHeader viaHeader = viaHeaders.next();
		if(logger.isDebugEnabled()) {
			logger.debug("viaHeader = " + viaHeader.toString());
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
				//
				ViaHeader nextViaHeader = null;
				if(viaHeaders.hasNext()) {
					nextViaHeader = viaHeaders.next();
				}
				checkInitialRemoteInformation(sipServletMessage, nextViaHeader);
			} //there is no client transaction associated with it, it means that this is a retransmission
			else if(dialog != null) {				
				applicationData = (TransactionApplicationData)dialog.getApplicationData();
				if(applicationData.getSipServletMessage() instanceof SipServletRequestImpl) {
					tmpOriginalRequest = (SipServletRequestImpl)applicationData.getSipServletMessage();
				}
				final ProxyBranchImpl proxyBranch = applicationData.getProxyBranch();
				if(proxyBranch == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("retransmission received for a non proxy application, dropping the response " + response);
					}
//					forwardResponseStatefully(sipServletResponse);
					return ;
				}
			} 
			final SipServletRequestImpl originalRequest = tmpOriginalRequest;
			sipServletResponse.setOriginalRequest(originalRequest);
			
			if(applicationData != null) {
				final String appNameNotDeployed = applicationData.getAppNotDeployed();
				if(appNameNotDeployed != null && appNameNotDeployed.length() > 0) {
					forwardResponseStatefully(sipServletResponse);
					return ;
				}
				final boolean noAppReturned = applicationData.isNoAppReturned();
				if(noAppReturned) {
					forwardResponseStatefully(sipServletResponse);
					return ;
				}
				final String modifier = applicationData.getModifier();
				if(modifier != null && modifier.length() > 0) {
					forwardResponseStatefully(sipServletResponse);
					return ;
				}		
			}
			final String branch = viaHeader.getBranch();
			String strippedBranchId = branch.substring(BRANCH_MAGIC_COOKIE.length());
			final String appId = strippedBranchId.substring(0, strippedBranchId.indexOf("_"));			
			strippedBranchId = strippedBranchId.substring(strippedBranchId.indexOf("_") + 1);
			final String appNameHashed = strippedBranchId.substring(0, strippedBranchId.indexOf("_"));			
			final String appName = sipApplicationDispatcher.getApplicationNameFromHash(appNameHashed);
			if(appName == null) {
				throw new DispatcherException("the via header branch " + branch + " for the response is missing the appname previsouly set by the container");
			}
			boolean inverted = false;
			if(dialog != null && dialog.isServer()) {
				inverted = true;
			}
			final SipContext sipContext = sipApplicationDispatcher.findSipApplication(appName);
			if(sipContext == null) {
				if(logger.isDebugEnabled()) {
					logger.debug("The application " + appName + " is not deployed anymore, fowarding response statefully to the next app in chain if any");
				}
				forwardResponseStatefully(sipServletResponse);
				return ;
			}
			final SipManager sipManager = (SipManager)sipContext.getManager();
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(appId, appName, response, inverted);
			if(logger.isDebugEnabled()) {
				logger.debug("Trying to find session with following session key " + sessionKey);
			}			
			MobicentsSipSession tmpSession = null;

			tmpSession = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, null);
			//needed in the case of RE-INVITE by example
			if(tmpSession == null) {
				sessionKey = SessionManagerUtil.getSipSessionKey(appId, appName, response, !inverted);
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
				if(logger.isDebugEnabled()) {
					logger.debug("Dropping the response since no active sip session has been found for it : " + response + ", it may already have been invalidated");
				}
				return ;
			} else {
				sipServletResponse.setSipSession(tmpSession);					
			}			
			
			if(logger.isDebugEnabled()) {
				logger.debug("route response on following session " + tmpSession.getId());
			}

			final MobicentsSipSession session = tmpSession;
			final TransactionApplicationData finalApplicationData = applicationData;						
			
			final DispatchTask dispatchTask = new DispatchTask(sipServletResponse, sipProvider) {

				public void dispatch() throws DispatcherException {
					sipContext.enterSipAppHa(true);
					try {
						try {														
							// we store the request only if the dialog is null and the method is not a dialog creating one
//							if(dialog == null) {
								session.setSessionCreatingTransactionRequest(sipServletResponse);
//							} else {
								session.setSessionCreatingDialog(dialog);
//							}
							if(originalRequest != null) {				
								originalRequest.setResponse(sipServletResponse);					
							}
							final int status = sipServletResponse.getStatus();
							// RFC 3265 : If a 200-class response matches such a SUBSCRIBE or REFER request,
							// it creates a new subscription and a new dialog.
							// Issue 1481 http://code.google.com/p/mobicents/issues/detail?id=1481
							// proxy should not add or remove subscription since there is no dialog associated with it
							if(Request.SUBSCRIBE.equals(sipServletResponse.getMethod()) && status >= 200 && status <= 300 && session.getProxy() == null) {					
								session.addSubscription(sipServletResponse);
							}
							// See if this is a response to a proxied request
							// We can not use session.getProxyBranch() because all branches belong to the same session
							// and the session.proxyBranch is overwritten each time there is activity on the branch.
							ProxyBranchImpl proxyBranch = null;
							if(finalApplicationData != null) {
								proxyBranch = finalApplicationData.getProxyBranch();
							}
							
							if(session.getProxy() != null) {
								
								// the final Application data is null meaning that the CTX was null, so it's a retransmission
								if(proxyBranch == null) {
									proxyBranch = session.getProxy().getFinalBranchForSubsequentRequests();	
								}
								
								if(proxyBranch == null) {
									if(logger.isDebugEnabled()) {
										logger.debug("Attempting to recover lost transaction app data for " + branch);
									}
									//String txid = ((ViaHeader)response.getHeader(ViaHeader.NAME)).getBranch();
									TransactionApplicationData tad = (TransactionApplicationData) 
										session.getProxy().getTransactionMap().get(branch);

									if(tad != null) {

										proxyBranch = tad.getProxyBranch();
										if(logger.isDebugEnabled()) {
											logger.debug("Sucessfully recovered app data for " + branch + " " + tad);
										}
									}
								}
								
								if(proxyBranch == null) {
									logger.warn("A proxy retransmission has arrived without knowing which proxybranch to use (tx data lost)");
									if(session.getProxy().getSupervised() && status != Response.TRYING) {
										callServlet(sipServletResponse);
									}
									forwardResponseStatefully(sipServletResponse);
								}
								

							} 
							if(proxyBranch != null) {
								sipServletResponse.setProxyBranch(proxyBranch);								
								// Update Session state
								session.updateStateOnResponse(sipServletResponse, true);
								proxyBranch.setResponse(sipServletResponse);

								final ProxyImpl proxy = (ProxyImpl) proxyBranch.getProxy();
								// Notfiy the servlet
								if(logger.isDebugEnabled()) {
									logger.debug("Is Supervised enabled for this proxy branch ? " + proxy.getSupervised());
								}
								if(proxy.getSupervised() && status != Response.TRYING) {
									callServlet(sipServletResponse);
								}
								if(status == 487 && proxy.allResponsesHaveArrived()) {
									session.setState(State.TERMINATED);
									session.setInvalidateWhenReady(true);
									if(logger.isDebugEnabled()) {
										logger.debug("Received 487 on a proxy branch and we are not waiting on other branches. Setting state to TERMINATED for " + this.toString());
									}
								}
								// Handle it at the branch
								proxyBranch.onResponse(sipServletResponse, status); 
								//we don't forward the response here since this has been done by the proxy
							}
							else {
								//if this is a trying response, the response is dropped
								if(Response.TRYING == status) {
									if(logger.isDebugEnabled()) {
										logger.debug("the response is dropped accordingly to JSR 289 " +
												"since this a 100 for a non proxy application");
									}
									return;
								}		
								// in non proxy case we drop the retransmissions
								if(sipServletResponse.getTransaction() == null && sipServletResponse.getDialog() == null) {
									if(logger.isDebugEnabled()) {
										logger.debug("the following response is dropped since there is no client transaction nor dialog for it : " + response);	
									}
									return;
								}
								// Update Session state
								session.updateStateOnResponse(sipServletResponse, true);

								callServlet(sipServletResponse);
								forwardResponseStatefully(sipServletResponse);
							}
						} catch (ServletException e) {				
							throw new DispatcherException("Unexpected servlet exception while processing the response : " + response, e);
							// Sends a 500 Internal server error and stops processing.				
							//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
						} catch (IOException e) {				
							throw new DispatcherException("Unexpected io exception while processing the response : " + response, e);
							// Sends a 500 Internal server error and stops processing.				
							//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
						} catch (Throwable e) {				
							throw new DispatcherException("Unexpected exception while processing response : " + response, e);
							// Sends a 500 Internal server error and stops processing.				
							//				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, clientTransaction, request, sipProvider);
						}
					} finally {
						sipContext.exitSipAppHa(null, sipServletResponse);
						sipContext.exitSipApp(session.getSipApplicationSession(), session);						
					}
				}
			};
			// we enter the sip app here, thus acuiring the semaphore on the session (if concurrency control is set) before the jain sip tx semaphore is released and ensuring that
			// the tx serialization is preserved
			sipContext.enterSipApp(session.getSipApplicationSession(), session);
			// if the flag is set we bypass the executor, the bypassExecutor flag should be made deprecated 
			if(sipApplicationDispatcher.isBypassResponseExecutor() || ConcurrencyControlMode.Transaction.equals((sipContext.getConcurrencyControlMode()))) {
				dispatchTask.dispatchAndHandleExceptions();
			} else {				
				getConcurrencyModelExecutorService(sipContext, sipServletMessage).execute(dispatchTask);				
			}
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
	private final void forwardResponseStatefully(final SipServletResponseImpl sipServletResponse) {
		final Response response = sipServletResponse.getResponse();
		final ListIterator<ViaHeader> viaHeadersLeft = response.getHeaders(ViaHeader.NAME);
		// we cannot remove the via header on the original response (and we don't to proactively clone the response for perf reasons)
		// otherwise it will make subsequent request creation fails (because JSIP dialog check the topmostviaHeader of the response)
		if(viaHeadersLeft.hasNext()) {
			viaHeadersLeft.next();
		}
		if(viaHeadersLeft.hasNext()) {
			final ClientTransaction clientTransaction = (ClientTransaction) sipServletResponse.getTransaction();
			final Dialog dialog = sipServletResponse.getDialog();
			final Response newResponse = (Response) response.clone();
			((SIPMessage)newResponse).setApplicationData(null);
			newResponse.removeFirst(ViaHeader.NAME);
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
			if(applicationData != null) {
				// non retransmission case
				final ServerTransaction serverTransaction = (ServerTransaction)
					applicationData.getTransaction();
				try {					
					serverTransaction.sendResponse(newResponse);
				} catch (SipException e) {
					logger.error("cannot forward the response statefully" , e);
				} catch (InvalidArgumentException e) {
					logger.error("cannot forward the response statefully" , e);
				}				
			} else {
				// retransmission case
				try {	
					String transport = JainSipUtils.findTransport(newResponse);
					SipProvider sipProvider = sipApplicationDispatcher.getSipNetworkInterfaceManager().findMatchingListeningPoint(
							transport, false).getSipProvider();
					sipProvider.sendResponse(newResponse);
				} catch (SipException e) {
					logger.error("cannot forward the response statelessly" , e);
				} 
			}
		} else {
			//B2BUA case we don't have to do anything here
			//no more via header B2BUA is the end point
			if(logger.isDebugEnabled()) {
				logger.debug("Not forwarding the response statefully. " +
						"It was either an endpoint or a B2BUA, ie an endpoint too " + response);
			}
		}			
	}
	
	/**
	 * This method checks if the initial remote information as specified by SIP Servlets 1.1 Section 15.7
	 * is available. If not we add it as headers only if the next via header is for the container 
	 * (so that this information stays within the container boundaries)
	 * @param sipServletMessage
	 * @param nextViaHeader
	 */
	public void checkInitialRemoteInformation(SipServletMessageImpl sipServletMessage, ViaHeader nextViaHeader) {		
		final SIPTransaction transaction = (SIPTransaction)sipServletMessage.getTransaction();
		String remoteAddr = transaction.getPeerAddress();
		int remotePort = transaction.getPeerPort();
		if(transaction.getPeerPacketSourceAddress() != null) {
			remoteAddr = transaction.getPeerPacketSourceAddress().getHostAddress();
			remotePort = transaction.getPeerPacketSourcePort();
		}		
		final String initialRemoteAddr = remoteAddr;
		final int initialRemotePort = remotePort;
		final String initialRemoteTransport = transaction.getTransport();
		final TransactionApplicationData transactionApplicationData = sipServletMessage.getTransactionApplicationData();
		// if the message comes from an external source we add the initial remote info from the transaction
		if(sipApplicationDispatcher.isExternal(initialRemoteAddr, initialRemotePort, initialRemoteTransport)) {
			transactionApplicationData.setInitialRemoteHostAddress(initialRemoteAddr);
			transactionApplicationData.setInitialRemotePort(initialRemotePort);
			transactionApplicationData.setInitialRemoteTransport(initialRemoteTransport);
			// there is no other way to pass the information to the next applications in chain  
			// (to avoid maintaining in memory information that would be to be clustered as well...)
			// than adding it as a custom information, we add it as headers only if 
			// the next via header is for the container
			if(nextViaHeader != null && !sipApplicationDispatcher.isViaHeaderExternal(nextViaHeader)) {
				sipServletMessage.setHeaderInternal(JainSipUtils.INITIAL_REMOTE_ADDR_HEADER_NAME, initialRemoteAddr, true); 
				sipServletMessage.setHeaderInternal(JainSipUtils.INITIAL_REMOTE_PORT_HEADER_NAME, "" + initialRemotePort, true);
				sipServletMessage.setHeaderInternal(JainSipUtils.INITIAL_REMOTE_TRANSPORT_HEADER_NAME, initialRemoteTransport, true);
			}
		} else {
			// if the message comes from an internal source we add the initial remote info from the previously added headers
			transactionApplicationData.setInitialRemoteHostAddress(sipServletMessage.getHeader(JainSipUtils.INITIAL_REMOTE_ADDR_HEADER_NAME));
			String remotePortHeader = sipServletMessage.getHeader(JainSipUtils.INITIAL_REMOTE_PORT_HEADER_NAME);
			int intRemotePort = -1;
			if(remotePortHeader != null) {
				intRemotePort = Integer.parseInt(remotePortHeader);
			}
			transactionApplicationData.setInitialRemotePort(intRemotePort);
			transactionApplicationData.setInitialRemoteTransport(sipServletMessage.getHeader(JainSipUtils.INITIAL_REMOTE_TRANSPORT_HEADER_NAME));
			if(nextViaHeader == null || sipApplicationDispatcher.isViaHeaderExternal(nextViaHeader)) {
				sipServletMessage.removeHeaderInternal(JainSipUtils.INITIAL_REMOTE_ADDR_HEADER_NAME, true); 
				sipServletMessage.removeHeaderInternal(JainSipUtils.INITIAL_REMOTE_PORT_HEADER_NAME, true);
				sipServletMessage.removeHeaderInternal(JainSipUtils.INITIAL_REMOTE_TRANSPORT_HEADER_NAME, true);
			}			
		}
	}
}
