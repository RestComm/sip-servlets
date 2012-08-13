/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.stack.SIPTransaction;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipSession.State;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipFactoryExt;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxyBranch;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

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
		final SipFactoryImpl sipFactoryImpl = (SipFactoryImpl) sipApplicationDispatcher.getSipFactory();
		final SipServletResponseImpl sipServletResponse = (SipServletResponseImpl) sipServletMessage;
		final Response response = sipServletResponse.getResponse();
		final ListIterator<ViaHeader> viaHeaders = response.getHeaders(ViaHeader.NAME);				
		final ViaHeader viaHeader = viaHeaders.next();
		final String branch = viaHeader.getBranch();
		if(logger.isDebugEnabled()) {
			logger.debug("viaHeader = " + viaHeader.toString());
			logger.debug("viaHeader branch = " + branch);
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
					// clearing the hops found by RFC 3263
					if(applicationData.getHops() != null) {
						applicationData.getHops().clear();
					}
				}
				//add the response for access from B2BUAHelper.getPendingMessages
				applicationData.addSipServletResponse(sipServletResponse);
				//
				ViaHeader nextViaHeader = null;
				if(viaHeaders.hasNext()) {					
					nextViaHeader = viaHeaders.next();
					if(logger.isDebugEnabled()) {
						logger.debug("nextViaHeader = " + nextViaHeader.toString());
						logger.debug("viaHeader branch = " + nextViaHeader.getBranch());
					}
				}
				checkInitialRemoteInformation(sipServletMessage, nextViaHeader);
			} //there is no client transaction associated with it, it means that this is a retransmission
			else if(dialog != null) {				
				applicationData = (TransactionApplicationData)dialog.getApplicationData();
				// Issue 1468 : application data can be null in case of forked response and max fork time property stack == 0
				if(applicationData != null) {
					if(applicationData.getSipServletMessage() instanceof SipServletRequestImpl) {
						tmpOriginalRequest = (SipServletRequestImpl)applicationData.getSipServletMessage();
					}
					// Retrans drop logic removed from here due to AR case Proxy-B2bua for http://code.google.com/p/mobicents/issues/detail?id=1986
				} else {
					// Issue 1468 : Dropping response in case of forked response and max fork time property stack == 0 to
					// Guard against exceptions that will arise later if we don't drop it since the support for it is not enabled
					if(logger.isDebugEnabled()) {
						logger.debug("application data is null, it means that this is a forked response, please enable stack property support for it through gov.nist.javax.sip.MAX_FORK_TIME_SECONDS");
						logger.debug("Dropping forked response " + response);
						return; 
					}
				}
			} 
			if(tmpOriginalRequest == null) {
				// our backup plan
				SIPTransaction tx = null;
				if(clientTransaction != null) {
					tx = (SIPTransaction) clientTransaction;
				} else {
					tx = (SIPTransaction) ((SIPTransactionStack)sipProvider.getSipStack()).findTransaction((SIPMessage) response, true);
				}
				if(tx != null) {
					tmpOriginalRequest = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest(tx.getRequest(), null, null, null, false);
				}
				
			}
			final SipServletRequestImpl originalRequest = tmpOriginalRequest;
			sipServletResponse.setOriginalRequest(originalRequest);
			
			if(applicationData != null) {
				final String appNameNotDeployed = applicationData.getAppNotDeployed();
				if(appNameNotDeployed != null && appNameNotDeployed.length() > 0) {
					if(logger.isDebugEnabled()) {
						logger.debug("appNameNotDeployed = " + appNameNotDeployed + " forwarding the response.");
					}
					forwardResponseStatefully(sipServletResponse);
					return ;
				}
				final boolean noAppReturned = applicationData.isNoAppReturned();
				if(noAppReturned) {
					if(logger.isDebugEnabled()) {
						logger.debug("isNoAppReturned forwarding the response.");
					}
					forwardResponseStatefully(sipServletResponse);
					return ;
				}
				final String modifier = applicationData.getModifier();
				if(modifier != null && modifier.length() > 0) {
					if(logger.isDebugEnabled()) {
						logger.debug("modifier = " + modifier + " forwarding the response.");
					}
					forwardResponseStatefully(sipServletResponse);
					return ;
				}		
			}
			String strippedBranchId = branch.substring(BRANCH_MAGIC_COOKIE.length());
			int indexOfUnderscore = strippedBranchId.indexOf("_");
			if(indexOfUnderscore == -1) {
				if(sipServletResponse.getStatus() == Response.TRYING) return;
				throw new DispatcherException("the via header branch " + branch + " for the response is wrong the response does not reuse the one from the original request");
			}
			final String appId = strippedBranchId.substring(0, indexOfUnderscore);
			indexOfUnderscore = strippedBranchId.indexOf("_");			
			if(indexOfUnderscore == -1) {
				throw new DispatcherException("the via header branch " + branch + " for the response is wrong the response does not reuse the one from the original request");
			}
			strippedBranchId = strippedBranchId.substring(indexOfUnderscore + 1);
			indexOfUnderscore = strippedBranchId.indexOf("_");
			if(indexOfUnderscore == -1) {
				throw new DispatcherException("the via header branch " + branch + " for the response is wrong the response does not reuse the one from the original request");
			}
			final String appNameHashed = strippedBranchId.substring(0, indexOfUnderscore);			
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
			final SipManager sipManager = sipContext.getSipManager();
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(appId, appName, response, inverted);
			if(logger.isDebugEnabled()) {
				logger.debug("Trying to find session with following session key " + sessionKey);
			}		
			final SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
					appName, 
					appId,
					null);
			
			MobicentsSipApplicationSession sipApplicationSession = null;
			// needed only for failover (early) dialog recovery
			if(sipManager instanceof DistributableSipManager) {
				sipApplicationSession = sipManager.getSipApplicationSession(sipApplicationSessionKey, false);
			}
			MobicentsSipSession tmpSession = null;

			tmpSession = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, sipApplicationSession);
			//needed in the case of RE-INVITE by example
			if(tmpSession == null) {
				sessionKey = SessionManagerUtil.getSipSessionKey(appId, appName, response, !inverted);
				if(logger.isDebugEnabled()) {
					logger.debug("Trying to find session with following session key " + sessionKey);
				}
				tmpSession = sipManager.getSipSession(sessionKey, false, sipFactoryImpl, sipApplicationSession);				
			}
			if(logger.isDebugEnabled()) {
				logger.debug("session found is " + tmpSession);
				if(tmpSession == null) {
					sipManager.dumpSipSessions();
				}
			}	
			
			if(tmpSession == null) {
				if(((SipFactoryExt)sipFactoryImpl).isRouteOrphanRequests()) {
					try {
						response.removeFirst(ViaHeader.NAME);
						SIPTransaction stx = (SIPTransaction) ((SIPTransactionStack)sipProvider.getSipStack()).findTransaction((SIPMessage) response, true);

						if(stx != null) {
							SipServletRequestImpl request = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest(stx.getRequest(), null, null, null, false);
							SipServletResponseImpl orphanResponse = (SipServletResponseImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletResponse(response, null, null, dialog, true, false);
							request.setOrphan(true);
							orphanResponse.setAppSessionId(appId);
							orphanResponse.setOriginalRequest(request);
							callServletForOrphanResponse(sipContext, orphanResponse);
							stx.sendMessage((SIPMessage) response);
						} else {
							if(sipServletResponse.getRequest() == null) {
								ToHeader toHeader = ((ToHeader)response.getHeader(ToHeader.NAME));
								FromHeader fromHeader = ((FromHeader)response.getHeader(FromHeader.NAME));
								javax.sip.address.URI uri = ((ToHeader)response.getHeader(ToHeader.NAME)).getAddress().getURI();
								CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
								CallIdHeader callid = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
								ViaHeader via = (ViaHeader) response.getHeader(ViaHeader.NAME);
								LinkedList<ViaHeader> vialist = new LinkedList<ViaHeader>();
								vialist.add(via);
								MaxForwardsHeader mf = sipFactoryImpl.getHeaderFactory().createMaxForwardsHeader(80);
								ContentTypeHeader cth = sipFactoryImpl.getHeaderFactory().createContentTypeHeader("orphan", "orphan");
								try {
									Request r = sipFactoryImpl.getMessageFactory().createRequest(uri, cseq.getMethod(), callid, cseq, fromHeader, toHeader, vialist, mf, cth, new byte[]{});
									SipServletRequestImpl req = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest(r, null, null, dialog, false);
									req.setOrphan(true);
									sipServletResponse.setOriginalRequest(req);
									sipServletResponse.setAppSessionId(appId);
									callServletForOrphanResponse(sipContext, sipServletResponse);
									sipProvider.sendResponse(response);
								} catch (Exception e) {
									if(logger.isDebugEnabled()) {
										logger.debug("failed sending artificial request for response ");
									}
								}
							}
						}
					} catch (Exception e) {
						logger.error("Problem routing orphaned response", e);
					}
				} else {
					if(logger.isDebugEnabled()) {
						logger.debug("Dropping the response since no active sip session has been found for it : " + response + ", it may already have been invalidated");
					}
				}
				return ;
			} else {
				// This piece of code move here due to Proxy-B2bua case for http://code.google.com/p/mobicents/issues/detail?id=1986
				if(tmpSession.getProxy() == null && sipServletResponse.isRetransmission()) {
					if(logger.isDebugEnabled()) {
						logger.debug("retransmission received for a non proxy application, dropping the response " + response);
					}
					return ;
				}
				sipServletResponse.setSipSession(tmpSession);					
			}			
			
			if(sipServletResponse.getRequest() == null && tmpSession.getProxy() != null) {
				for(ProxyBranch pbi:tmpSession.getProxy().getProxyBranches()) {
					ProxyBranchImpl pb = (ProxyBranchImpl) pbi;
					Request r = (Request) ((SipServletRequestImpl)pb.getRequest()).getMessage();
					ViaHeader via1 = (ViaHeader) r.getHeader(ViaHeader.NAME);
					ViaHeader via2 = (ViaHeader) response.getHeader(ViaHeader.NAME);
					if(via1.getBranch().equals(via2.getBranch())) {
						sipServletResponse.setOriginalRequest(((SipServletRequestImpl)pb.getRequest()));
						break;
					}
				}
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("route response on following session " + tmpSession.getId());
			}

			final MobicentsSipSession session = tmpSession;
			final TransactionApplicationData finalApplicationData = applicationData;						
			
			final DispatchTask dispatchTask = new DispatchTask(sipServletResponse, sipProvider) {

				public void dispatch() throws DispatcherException {
					final int status = sipServletResponse.getStatus();
					boolean batchStarted = false;
					if(status != Response.TRYING) {
						batchStarted = sipContext.enterSipAppHa(true);
					}
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
							MobicentsProxyBranch proxyBranch = null;
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

								final ProxyImpl proxy = (ProxyImpl) session.getProxy();
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
										logger.debug("Received 487 on a proxy branch and we are not waiting on other branches. Setting state to TERMINATED for session " + session);										
									}																		
								}
								// Issue http://code.google.com/p/mobicents/issues/detail?id=2616 : Proxy forwards 487 as well as 200 to caller
								// RFC 3261 Section 16.7 Response Processing. Step 5
								// After a final response has been sent on the server transaction, the following responses MUST be forwarded immediately:
						        //     -  Any 2xx response to an INVITE request
						        // A stateful proxy MUST NOT immediately forward any other responses
								if(sipServletResponse.getMethod().equals(Request.INVITE) && sipServletResponse.getRequest() != null 
										&& sipServletResponse.getRequest().isInitial() && proxy.getBestResponseSent() >= 200 && (status <= 200 || status >= 300)) {
									if(logger.isDebugEnabled()) {
										logger.debug("best final response sent " + proxy.getBestResponseSent() + ", response status " + status + " not forwarding response");
									}
									return;
								}
								// Handle it at the branch
								proxyBranch.onResponse(sipServletResponse, status); 
								//we don't forward the response here since this has been done by the proxy
							}
							else {
								// Fix for Issue 1689 : Send 100/INVITE immediately or not
								// This issue showed that retransmissions of 180 Ringing were passed
								// to the application so we make sure to drop them in case of UAC/UAS
								if(sipServletResponse.isRetransmission()) {
									if(logger.isDebugEnabled()) {
										logger.debug("the following response is dropped since this is a retransmission " + response);	
									}
									return;
								}
								//if this is a trying response, the response is dropped
								if(Response.TRYING == status) {
									// We will transition from INITIAL to EARLY here (not clear from the spec)
									// Figure 6-1 The SIP Dialog State Machine
									// and Figure 6-2 The SipSession State Machine
									session.updateStateOnResponse(sipServletResponse, true);
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
						// exitSipAppHa completes the replication task. It might block for a while if the state is too big
						// We should never call exitAipApp before exitSipAppHa, because exitSipApp releases the lock on the
						// Application of SipSession (concurrency control lock). If this happens a new request might arrive
						// and modify the state during Serialization or other non-thread safe operation in the serialization
						if(status != Response.TRYING) {
							sipContext.exitSipAppHa(null, sipServletResponse, batchStarted);
						}
						sipContext.exitSipApp(session.getSipApplicationSession(), session);						
					}
				}
			};
			// we enter the sip app here, thus acuiring the semaphore on the session (if concurrency control is set) before the jain sip tx semaphore is released and ensuring that
			// the tx serialization is preserved
			sipContext.enterSipApp(session.getSipApplicationSession(), session, false);
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
			((MessageExt)newResponse).setApplicationData(null);
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
			String remoteAddressHeader = sipServletMessage.getHeader(JainSipUtils.INITIAL_REMOTE_ADDR_HEADER_NAME);
			String remotePortHeader = sipServletMessage.getHeader(JainSipUtils.INITIAL_REMOTE_PORT_HEADER_NAME);
			String remoteTransportHeader = sipServletMessage.getHeader(JainSipUtils.INITIAL_REMOTE_TRANSPORT_HEADER_NAME);					
			int intRemotePort = -1;
			if(remotePortHeader != null) {
				intRemotePort = Integer.parseInt(remotePortHeader);
			}
			if(remoteAddressHeader == null && remoteTransportHeader == null && remotePortHeader == null) {
				transactionApplicationData.setInitialRemoteHostAddress(initialRemoteAddr);
				transactionApplicationData.setInitialRemotePort(initialRemotePort);
				transactionApplicationData.setInitialRemoteTransport(initialRemoteTransport);
			} else {
				transactionApplicationData.setInitialRemoteHostAddress(remoteAddressHeader);
				transactionApplicationData.setInitialRemotePort(intRemotePort);
				transactionApplicationData.setInitialRemoteTransport(remoteTransportHeader);
				if(nextViaHeader == null || sipApplicationDispatcher.isViaHeaderExternal(nextViaHeader)) {
					sipServletMessage.removeHeaderInternal(JainSipUtils.INITIAL_REMOTE_ADDR_HEADER_NAME, true); 
					sipServletMessage.removeHeaderInternal(JainSipUtils.INITIAL_REMOTE_PORT_HEADER_NAME, true);
					sipServletMessage.removeHeaderInternal(JainSipUtils.INITIAL_REMOTE_TRANSPORT_HEADER_NAME, true);
				}			
			}
		}	
	}
}
