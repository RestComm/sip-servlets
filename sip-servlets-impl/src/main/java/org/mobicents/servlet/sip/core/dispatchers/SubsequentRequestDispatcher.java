/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;
import gov.nist.javax.sip.message.MessageExt;
import gov.nist.javax.sip.message.RequestExt;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import gov.nist.javax.sip.stack.SIPTransaction;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServletResponse;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.Parameters;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.SipFactoryExt;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.address.URIImpl;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.ApplicationRoutingHeaderComposer;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.core.MobicentsSipFactory;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.SipManager;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxy;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxyBranch;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * This class is responsible for routing and dispatching subsequent request to applications according to JSR 289 Section 
 * 15.6 Responses, Subsequent Requests and Application Path 
 * 
 * It uses route header parameters for proxy apps or to tag parameter for UAS/B2BUA apps 
 * that were previously set by the container on 
 * record route headers or to tag to know which app has to be called 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SubsequentRequestDispatcher extends RequestDispatcher {

	private static final Logger logger = Logger.getLogger(SubsequentRequestDispatcher.class);
	
	public SubsequentRequestDispatcher() {}
	
//	public SubsequentRequestDispatcher(
//			SipApplicationDispatcher sipApplicationDispatcher) {
//		super(sipApplicationDispatcher);
//	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMessage(final SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {
		final MobicentsSipFactory sipFactoryImpl = sipApplicationDispatcher.getSipFactory();
		final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
		if(logger.isDebugEnabled()) {
			logger.debug("Routing of Subsequent Request " + sipServletRequest);
		}	
				
		final Request request = (Request) sipServletRequest.getMessage();
		final Dialog dialog = sipServletRequest.getDialog();		
		final RouteHeader poppedRouteHeader = sipServletRequest.getPoppedRouteHeader();
		final String method = request.getMethod();
		
		String applicationName = null; 
		String applicationId = null;		
		if(poppedRouteHeader != null){
			final Parameters poppedAddress = (Parameters)poppedRouteHeader.getAddress().getURI();
			// Extract information from the Route Header		
			final String applicationNameHashed = poppedAddress.getParameter(RR_PARAM_APPLICATION_NAME);
			if(applicationNameHashed != null && applicationNameHashed.length() > 0) {				
				applicationName = sipApplicationDispatcher.getApplicationNameFromHash(applicationNameHashed);
				applicationId = poppedAddress.getParameter(APP_ID);				
			}
		} 
		if(applicationId == null) {
			final ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
			final String arText = toHeader.getTag();
			try {
				final String[] tuple = ApplicationRoutingHeaderComposer.getAppNameAndSessionId(sipApplicationDispatcher, arText);
				applicationName = tuple[0];
				applicationId = tuple[1];
			} catch(IllegalArgumentException e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, e);
			}
			if(applicationId == null && applicationName == null) {
				//gvag Issue 2337 & 2327
				javax.sip.address.URI requestURI = request.getRequestURI();
				// Issue 2850 :	Use Request-URI custom Mobicents parameters to route request for misbehaving agents, workaround for Cisco-SIPGateway/IOS-12.x user agent
				if(request.getRequestURI() instanceof javax.sip.address.SipURI) {
					final String applicationNameHashed = ((Parameters)requestURI).getParameter(RR_PARAM_APPLICATION_NAME);
					if(applicationNameHashed != null && applicationNameHashed.length() > 0) {				
						applicationName = sipApplicationDispatcher.getApplicationNameFromHash(applicationNameHashed);
						applicationId = ((Parameters)requestURI).getParameter(APP_ID);
					}
				}
				if(applicationId == null && applicationName == null) {
					boolean isAnotherDomain = false;
					
					if(requestURI.isSipURI()){
						final String host = ((SipURI) requestURI).getHost();
						final int port = ((SipURI) requestURI).getPort();
						final String transport = JainSipUtils.findTransport(request);
						isAnotherDomain = sipApplicationDispatcher.isExternal(host, port, transport);
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("The Request URI " + requestURI + " is not a SIP URI and the Route Header was null or didn't contain information about an application to call (which would be incorrect) so we assume the request is an ACK for a container generated error response or misrouted");
						}	
					}
					
					// Issue 823 (http://code.google.com/p/mobicents/issues/detail?id=823) : 
					// Container should proxy statelessly subsequent requests not targeted at itself
					if(isAnotherDomain) {	
						if(Request.ACK.equals(method) && sipServletRequest.getTransaction() != null && ((SIPServerTransaction)sipServletRequest.getTransaction()).getLastResponseStatusCode() >= 300) {
							// Issue 2213 (http://code.google.com/p/mobicents/issues/detail?id=2213) :
							// ACK for final error response are proxied statelessly for proxy applications
							//Means that this is an ACK to a container generated error response, so we can drop it
							if(logger.isDebugEnabled()) {
								logger.debug("The popped Route, application Id and name are null for an ACK, and this is an ACK for an error response, so it is dropped");
							}				
							return ;
						} 
						// Some UA are misbehaving and don't follow the non record proxy so they sent subsequent requests to the container (due to oubound proxy set probably) instead of directly to the UA
						// so we proxy statelessly those requests
						if(logger.isDebugEnabled()) {
							logger.debug("No application found to handle this request " + request + " with the following popped route header " + poppedRouteHeader + " so forwarding statelessly to the outside since it is not targeted at the container");
						}
						try {
							sipProvider.sendRequest(request);
						} catch (SipException e) {
							throw new DispatcherException("cannot proxy statelessly outside of the container the following request " + request, e);
						}
						return;
					} else {
						if(Request.ACK.equals(method)) {
							//Means that this is an ACK to a container generated error response, so we can drop it
							if(logger.isDebugEnabled()) {
								logger.debug("The popped Route, application Id and name are null for an ACK, so this is an ACK to a container generated error response, so it is dropped");
							}				
							return ;
						} else {
							if(poppedRouteHeader != null) {
								throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot find the application to handle this subsequent request " + request +
									"in this popped routed header " + poppedRouteHeader);
							} else {
								throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot find the application to handle this subsequent request " + request);
							}
						}
					}
				}
			} 
		}		
		
		boolean inverted = false;
		if(dialog != null && !dialog.isServer()) {
			inverted = true;
		}
		
		final SipContext sipContext = sipApplicationDispatcher.findSipApplication(applicationName);
		if(sipContext == null) {
			if(poppedRouteHeader != null) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot find the application to handle this subsequent request " + request +
					"in this popped routed header " + poppedRouteHeader);
			} else {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot find the application to handle this subsequent request " + request);
			}
		}
		final SipManager sipManager = sipContext.getSipManager();		
		final MobicentsSipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
				applicationName, 
				applicationId);
	
		MobicentsSipSession tmpSipSession = null;
		MobicentsSipApplicationSession sipApplicationSession = sipManager.getSipApplicationSession(sipApplicationSessionKey, false);
		if(sipApplicationSession == null) {
			if(logger.isDebugEnabled()) {
				sipManager.dumpSipApplicationSessions();
			}
			//trying the join or replaces matching sip app sessions
			final MobicentsSipApplicationSessionKey joinSipApplicationSessionKey = sipContext.getSipSessionsUtil().getCorrespondingSipApplicationSession(sipApplicationSessionKey, JoinHeader.NAME);
			final MobicentsSipApplicationSessionKey replacesSipApplicationSessionKey = sipContext.getSipSessionsUtil().getCorrespondingSipApplicationSession(sipApplicationSessionKey, ReplacesHeader.NAME);
			if(joinSipApplicationSessionKey != null) {
				sipApplicationSession = sipManager.getSipApplicationSession(joinSipApplicationSessionKey, false);
			} else if(replacesSipApplicationSessionKey != null) {
				sipApplicationSession = sipManager.getSipApplicationSession(replacesSipApplicationSessionKey, false);
			}
		}
		// Orphaned requests are routed from here
		boolean routeOrphanRequests = ((SipFactoryExt)sipContext.getSipFactoryFacade()).isRouteOrphanRequests();
		if(sipApplicationSession == null || sipApplicationSession.isOrphan()) {			
			if(logger.isDebugEnabled()) {
				logger.debug("routeOrphanRequests = " + routeOrphanRequests + " for context " + sipContext.getApplicationName() + " appSession=" + sipApplicationSession);
			}
			if(!routeOrphanRequests) {
				if(poppedRouteHeader != null) {
					throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "Cannot find the corresponding sip application session to this subsequent request " + request +
							" with the following popped route header " + sipServletRequest.getPoppedRoute() + ", it may already have been invalidated or timed out");
				} else {
					throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "Cannot find the corresponding sip application session to this subsequent request " + request +
							", it may already have been invalidated or timed out");					
				}
			} else {
				handleOrphanRequest(sipProvider, sipServletRequest, applicationId, sipContext);
				return;				
			}
		}

		if(StaticServiceHolder.sipStandardService.isHttpFollowsSip()) {
			String jvmRoute = StaticServiceHolder.sipStandardService.getJvmRoute();
			if(jvmRoute != null) {
				sipApplicationSession.setJvmRoute(jvmRoute);
			}
		}
		
		SipSessionKey key = SessionManagerUtil.getSipSessionKey(sipApplicationSession.getKey().getId(), applicationName, request, inverted);
		if(logger.isDebugEnabled()) {
			logger.debug("Trying to find the corresponding sip session with key " + key + " to this subsequent request " + request +
					" with the following popped route header " + sipServletRequest.getPoppedRoute());
		}
		tmpSipSession = sipManager.getSipSession(key, false, sipFactoryImpl, sipApplicationSession);
		
		// Added by Vladimir because the inversion detection on proxied requests doesn't work
		if(tmpSipSession == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Cannot find the corresponding sip session with key " + key + " to this subsequent request " + request +
						" with the following popped route header " + sipServletRequest.getPoppedRoute() + ". Trying inverted.");
			}
			key = SessionManagerUtil.getSipSessionKey(sipApplicationSession.getKey().getId(), applicationName, request, !inverted);
			tmpSipSession = sipManager.getSipSession(key, false, sipFactoryImpl, sipApplicationSession);
		}
		
		if(tmpSipSession == null) {
			sipManager.dumpSipSessions();
			if(logger.isDebugEnabled()) {
				logger.debug("routeOrphanRequests = " + routeOrphanRequests + " for context " + sipContext.getApplicationName() + " appSessionId=" + applicationId);
			}
			if(!routeOrphanRequests) {
				if(poppedRouteHeader != null) {
					throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "Cannot find the corresponding sip session to this subsequent request " + request +
							" with the following popped route header " + sipServletRequest.getPoppedRoute() + ", it may already have been invalidated or timed out");
				} else {
					throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "Cannot find the corresponding sip session to this subsequent request " + request +
							", it may already have been invalidated or timed out");					
				}
			} else {
				handleOrphanRequest(sipProvider, sipServletRequest, applicationId, sipContext);
				return; 
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Inverted try worked. sip session found : " + tmpSipSession.getId());
			}
		}			
		
		final MobicentsSipSession sipSession = tmpSipSession;
		sipServletRequest.setSipSession(sipSession);		
		
		final SubsequentDispatchTask dispatchTask = new SubsequentDispatchTask(sipServletRequest, sipProvider);
		// we enter the sip app here, thus acuiring the semaphore on the session (if concurrency control is set) before the jain sip tx semaphore is released and ensuring that
		// the tx serialization is preserved		
		sipContext.enterSipApp(sipApplicationSession, sipSession, false);
		// Issue 2886 : http://code.google.com/p/mobicents/issues/detail?id=2886 ACK is bound out of replication context
		// we need to enter the serialization here because validateCSeq below can set the CSeq so we need to replicate it
		final boolean batchStarted = sipContext.enterSipAppHa(true);
		
		// Issue 1714 && Issue 2886 : moving the requests pending within the serialization block 
		if(request.getMethod().equals(Request.ACK)) {
			sipSession.setRequestsPending(sipSession.getRequestsPending() - 1);
		} else if(request.getMethod().equals(Request.INVITE)){
			if(logger.isDebugEnabled()) {
				logger.debug("INVITE requests pending " + sipSession.getRequestsPending());
			}
			if(StaticServiceHolder.sipStandardService.isDialogPendingRequestChecking() && 
					sipSession.getProxy() == null && sipSession.getRequestsPending() > 0) {
				try {
					sipServletRequest.createResponse(491).send();
					return;
				} catch (IOException e) {
					logger.error("Problem sending 491 response to " + sipServletRequest, e);
				} finally {
					sipContext.exitSipApp(sipApplicationSession, sipSession);
				}
			}
			sipSession.setRequestsPending(sipSession.getRequestsPending() + 1);
		}
		// Issue 1714 : do the validation after lock acquisition to avoid conccurency on CSeq validation 
		// if a concurrency control mode is used
		
		// BEGIN validation delegated to the applicationas per JSIP patch for http://code.google.com/p/mobicents/issues/detail?id=766
		boolean orphan = sipSession.isOrphan() || sipApplicationSession.isOrphan();
		sipServletRequest.setOrphan(orphan);
		if(sipSession.getProxy() == null && !orphan) {
			boolean isValid = sipSession.validateCSeq(sipServletRequest);
			if(!isValid) {
				// Issue 2886 : http://code.google.com/p/mobicents/issues/detail?id=2886 ACK is bound out of replication context
				// we need to exit the serialization here because validateCSeq above can set the CSeq so we need to replicate it
				sipContext.exitSipAppHa(sipServletRequest, null, batchStarted);
				// Issue 1714 release the lock if we don't call the app
				sipContext.exitSipApp(sipApplicationSession, sipSession);				
				return;
			}
		}
		// END of validation for http://code.google.com/p/mobicents/issues/detail?id=766
		
		// if the flag is set we bypass the executor. This flag should be made deprecated 
		if(sipApplicationDispatcher.isBypassRequestExecutor() || ConcurrencyControlMode.Transaction.equals((sipContext.getConcurrencyControlMode()))) {
			dispatchTask.setBatchStarted(batchStarted);
			dispatchTask.dispatchAndHandleExceptions();
		} else {
			// Issue 2886 : http://code.google.com/p/mobicents/issues/detail?id=2886 ACK is bound out of replication context
			// we need to exitSipAppHa because a new thread will be started here 
			sipContext.exitSipAppHa(sipServletRequest, null, batchStarted);
			if(logger.isDebugEnabled()) {
				logger.debug("We are just before executor with sipAppSession=" + sipApplicationSession + " and sipSession=" + sipSession + " for " + sipServletMessage);
			}
			getConcurrencyModelExecutorService(sipContext, sipServletMessage).execute(dispatchTask);
			if(logger.isDebugEnabled()) {
				logger.debug("We are just after executor with sipAppSession=" + sipApplicationSession + " and sipSession=" + sipSession + " for " + sipServletMessage);
			}
		}
	}	
	
	/*
	 * http://code.google.com/p/mobicents/issues/detail?id=2547
	 * Allows to route subsequent requests statelessly to proxy applications to 
	 * improve perf and mem usage.
	 */
	private static void handleOrphanRequest(final SipProvider sipProvider,
			final SipServletRequestImpl sipServletRequest,
			String applicationId, final SipContext sipContext)
			throws DispatcherException {
		final String applicationName = sipContext.getApplicationName();
		final Request request = (Request) sipServletRequest.getMessage();
		
		// Making sure to nullify those ref so that if there is a race condition as in 
		// http://code.google.com/p/mobicents/issues/detail?id=2937
		// we return null instead of the invalidated sip application session
		sipServletRequest.setSipSession(null);
		sipServletRequest.setSipSessionKey(null);		
		sipServletRequest.setOrphan(true);
		sipServletRequest.setAppSessionId(applicationId);
		sipServletRequest.setCurrentApplicationName(applicationName);
		try {
			MessageDispatcher.callServletForOrphanRequest(sipContext, sipServletRequest);
			try {
				String transport = JainSipUtils.findTransport(request);
				SipConnector connector = StaticServiceHolder.sipStandardService.findSipConnector(transport);
				
				String branch = ((ViaHeader)sipServletRequest.getMessage().getHeader(ViaHeader.NAME)).getBranch();
				ViaHeader via = JainSipUtils.createViaHeader(sipContext.getSipApplicationDispatcher().getSipNetworkInterfaceManager(), request, 
						JainSipUtils.createBranch("orphan", 
								sipContext.getSipApplicationDispatcher().getHashFromApplicationName(applicationName),
								Integer.toString(branch.hashCode()) + branch.substring(branch.length()/2)), 
								null) ;
				if(connector.isUseStaticAddress()) {
					try {
						via.setHost(connector.getStaticServerAddress());
						via.setPort(connector.getStaticServerPort());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					
				}
				sipServletRequest.getMessage().addHeader(via);

				sipProvider.sendRequest((Request) sipServletRequest.getMessage());
			} catch (SipException e) {
				logger.error("Error routing orphaned request" ,e);
			}
			return;
		} catch (ServletException e) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
		} catch (IOException e) {				
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
		}
	}	

	public static class SubsequentDispatchTask extends DispatchTask {
		boolean batchStarted = false;
		
		SubsequentDispatchTask(SipServletRequestImpl sipServletRequest, SipProvider sipProvider) {
			super(sipServletRequest, sipProvider);
		}
		
		public void setBatchStarted(boolean batchStarted) {
			this.batchStarted = batchStarted;
		}

		public void dispatch() throws DispatcherException {
			final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl)sipServletMessage;
			final MobicentsSipSession sipSession = sipServletRequest.getSipSession();
			final MobicentsSipApplicationSession appSession = sipSession.getSipApplicationSession();
			final SipContext sipContext = appSession.getSipContext();
			final Request request = (Request) sipServletRequest.getMessage();
			
			if(!sipContext.getSipApplicationDispatcher().isBypassRequestExecutor()) {
				// Issue 2886 : http://code.google.com/p/mobicents/issues/detail?id=2886 ACK is bound out of replication context
				// we need to enterSipAppHa because was started in another thread so we need to bind to this thread
				batchStarted = sipContext.enterSipAppHa(true);
			}		
			
			// Issue 2937 : http://code.google.com/p/mobicents/issues/detail?id=2937
			// Susbequent Requests whose session is invalidated are still routed to the application
			if(!appSession.isValidInternal()) {
				if(appSession.isOrphan() || ((SipFactoryExt)sipContext.getSipFactoryFacade()).isRouteOrphanRequests()) {
					handleOrphanRequest(sipProvider, sipServletRequest, appSession.getId(), sipContext);
				} else {
					throw new DispatcherException(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST, "The corresponding sip application session to this subsequent request " + request +
						" has been invalidated or timed out.");	
				}
			}
			
			final String requestMethod = sipServletRequest.getMethod();
			try {
				if(!Request.ACK.equalsIgnoreCase(requestMethod)) {
					// Issue 1494 : http://code.google.com/p/mobicents/issues/detail?id=1494
                    // Only the first ACK makes it up to the application
					// resetting the creating transaction request to the current one is needed to
					// avoid a null final response on a reinvite while checking for the ACK below
					if(!Request.PRACK.equalsIgnoreCase(requestMethod)) {
						sipSession.setSessionCreatingTransactionRequest(sipServletRequest);
					}
					sipSession.addOngoingTransaction(sipServletRequest.getTransaction());
				}				
				try {
					// RFC 3265 : If a matching NOTIFY request contains a "Subscription-State" of "active" or "pending", it creates
					// a new subscription and a new dialog (unless they have already been
					// created by a matching response, as described above).	
					// Issue 1481 http://code.google.com/p/mobicents/issues/detail?id=1481
					// proxy should not add or remove subscription since there is no dialog associated with it
					if(Request.NOTIFY.equals(requestMethod) && sipSession.getProxy() == null) {
						final SubscriptionStateHeader subscriptionStateHeader = (SubscriptionStateHeader) 
							sipServletRequest.getMessage().getHeader(SubscriptionStateHeader.NAME);
					 						
						if (subscriptionStateHeader != null && 
											(SubscriptionStateHeader.ACTIVE.equalsIgnoreCase(subscriptionStateHeader.getState()) ||
											SubscriptionStateHeader.PENDING.equalsIgnoreCase(subscriptionStateHeader.getState()))) {					
							sipSession.addSubscription(sipServletRequest);
						}
					}						
					
					// Issue 1401 http://code.google.com/p/mobicents/issues/detail?id=1401
					// JSR 289 Section 11.2.2 Receiving ACK : 
					// "Applications are not notified of incoming ACKs for non-2xx final responses to INVITE."
					boolean callServlet = true;
					if(Request.ACK.equalsIgnoreCase(requestMethod)) {
						final Set<Transaction> ongoingTransactions = sipSession.getOngoingTransactions();
						// Issue 1837 http://code.google.com/p/mobicents/issues/detail?id=1837
						// ACK was received by JAIN-SIP but was not routed to application
						// we need to go through the set of all ongoing tx since and INFO request can be received before a reINVITE tx has been completed
						if(ongoingTransactions != null) {
							if(logger.isDebugEnabled()) {
                                logger.debug("going through all Stx to check if we need to pass the ACK up to the application");
                            }
							for (Transaction transaction: ongoingTransactions) {
								if(logger.isDebugEnabled()) {
                                    logger.debug(" tx to check "+ transaction);
                                }
								if (transaction instanceof ServerTransaction && ((SIPTransaction)transaction).getMethod().equals(Request.INVITE)) {
									if(logger.isDebugEnabled()) {
	                                    logger.debug("Stx found "+ transaction.getBranchId() +" to check if we need to pass the ACK up to the application");
	                                }
									final TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
									if(tad != null) {
										final SipServletMessageImpl sipServletMessage = tad.getSipServletMessage();
										if(sipServletMessage != null && sipServletMessage instanceof SipServletRequestImpl && 
												((MessageExt)request).getCSeqHeader().getSeqNumber() == ((MessageExt)sipServletMessage.getMessage()).getCSeqHeader().getSeqNumber()) {
											SipServletRequestImpl correspondingInviteRequest = (SipServletRequestImpl)sipServletMessage;
											final SipServletResponse lastFinalResponse = correspondingInviteRequest.getLastFinalResponse();
											if(logger.isDebugEnabled()) {
				                                logger.debug("last final response " + lastFinalResponse + " for original request " + correspondingInviteRequest);
				                            }
											
										    if(lastFinalResponse != null && lastFinalResponse.getStatus() >= 300) {
										    	callServlet = false;
			    								if(logger.isDebugEnabled()) {
			    									logger.debug("not calling the servlet since this is an ACK for a final error response");
			    								}
			    								break;
										    }
										    // Issue 1494 : http://code.google.com/p/mobicents/issues/detail?id=1494
			                                // Only the first ACK makes it up to the application, in case the sip stack 
										    // generates an error response the last final response will be null
			                                // for the corresponding ACK and it should not be passed to the app
										    if(lastFinalResponse == null) {
			                                    if(logger.isDebugEnabled()) {
			                                        logger.debug("not calling the servlet since this is an ACK for a null last final response, which means the ACK was for a sip stack generated error response");
			                                    }
			                                    callServlet = false;	
			                                    break;
										    }							 
										}										
									}
								}
							}
						}							
					}
					
					// See if the subsequent request should go directly to the proxy
					final MobicentsProxy proxy = sipSession.getProxy();
					if(proxy != null) {
						MobicentsProxyBranch finalBranch = proxy.getFinalBranchForSubsequentRequests();
						
						boolean isPrack = requestMethod.equalsIgnoreCase(Request.PRACK);
						boolean isUpdate = requestMethod.equalsIgnoreCase(Request.UPDATE);
						// JSR 289 Section 6.2.1 :
						// any state transition caused by the reception of a SIP message, 
						// the state change must be accomplished by the container before calling 
						// the service() method of any SipServlet to handle the incoming message.
						sipSession.updateStateOnSubsequentRequest(sipServletRequest, true);
						if(finalBranch != null) {								
							proxy.setAckReceived(requestMethod.equalsIgnoreCase(Request.ACK));
							checkRequestURIForNonCompliantAgents(finalBranch, request);							
							proxy.setOriginalRequest(sipServletRequest);
							// if(!isAckRetranmission) { // We should pass the ack retrans (implied by 10.2.4.1 Handling 2xx Responses to INVITE)
							// emmartins: JSR 289 10.2.8 - ACKs for non-2xx final responses are just dropped 
							if(callServlet) {
								callServlet(sipServletRequest);
								finalBranch.proxySubsequentRequest(sipServletRequest);
							}
						} else if(isPrack || isUpdate) {
							callServlet(sipServletRequest);
							final List<ProxyBranch> branches = proxy.getProxyBranches();
							for(ProxyBranch pb : branches) {
								final ProxyBranchImpl proxyBranch = (ProxyBranchImpl) pb;
								if(proxyBranch.isWaitingForPrack() && isPrack) {
									proxyBranch.proxyDialogStateless(sipServletRequest);
									proxyBranch.setWaitingForPrack(false);
								} else {
									//Issue: https://code.google.com/p/mobicents/issues/detail?id=2264
									if(logger.isDebugEnabled()){
										logger.debug("Checking request's To header tag against proxyBranch's From tag and To tag" +
												"in order to identify the proxyBranch for this request ");
									}
									String requestToTag = ((RequestExt)request).getToHeader().getTag();

									SipServletResponseImpl proxyResponse = (SipServletResponseImpl)(proxyBranch.getResponse());									
									String proxyToTag = ((MessageExt)proxyResponse.getMessage()).getToHeader().getTag();
									String proxyFromTag = ((MessageExt)proxyResponse.getMessage()).getFromHeader().getTag();

									if (proxyToTag.equals(requestToTag) || proxyFromTag.equals(requestToTag) ) { 
										finalBranch = proxyBranch;
										checkRequestURIForNonCompliantAgents(finalBranch, request);
										finalBranch.proxySubsequentRequest(sipServletRequest);
									} 
								}
							}
							if (finalBranch == null && isUpdate){
								logger.warn("Final branch is null, enable debug for more information.");
								if(logger.isDebugEnabled()) {
									logger.debug("Final branch is null, this will probably result in a lost call or request. Here is the request:\n" + request, new
											RuntimeException("Final branch is null"));
								}
							}
						} else {
							// this can happen on a branch that timed out and the proxy sent a 408
							// This would be the ACK to the 408 
							if(!Request.ACK.equalsIgnoreCase(requestMethod)) {
								logger.warn("Final branch is null, enable debug for more information.");
								if(logger.isDebugEnabled()) {
									logger.debug("Final branch is null, this will probably result in a lost call or request. Here is the request:\n" + request, new
											RuntimeException("Final branch is null"));
								}
							} else {
								if(logger.isDebugEnabled()) {
									logger.debug("Final branch is null, Here is the request:\n" + request);
								}
							}
							
						}
					}
					// If it's not for a proxy then it's just an AR, so go to the next application
					else {	
						
						// JSR 289 Section 6.2.1 :
						// any state transition caused by the reception of a SIP message, 
						// the state change must be accomplished by the container before calling 
						// the service() method of any SipServlet to handle the incoming message.
						sipSession.updateStateOnSubsequentRequest(sipServletRequest, true);
						if(callServlet) {
							callServlet(sipServletRequest);
						}
					}						
				} catch (ServletException e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
				} catch (SipException e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
				} catch (IOException e) {				
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
				} 	 
			} finally {
				// A subscription is destroyed when a notifier sends a NOTIFY request
				// with a "Subscription-State" of "terminated".		
				// Issue 1481 http://code.google.com/p/mobicents/issues/detail?id=1481
				// proxy should not add or remove subscription since there is no dialog associated with it
				if(Request.NOTIFY.equals(requestMethod) && sipSession.getProxy() == null) {
					final SubscriptionStateHeader subscriptionStateHeader = (SubscriptionStateHeader) 
						sipServletRequest.getMessage().getHeader(SubscriptionStateHeader.NAME);
				
					if (subscriptionStateHeader != null && 
										SubscriptionStateHeader.TERMINATED.equalsIgnoreCase(subscriptionStateHeader.getState())) {
						sipSession.removeSubscription(sipServletRequest);
					}
				}
				
				// exitSipAppHa completes the replication task. It might block for a while if the state is too big
				// We should never call exitAipApp before exitSipAppHa, because exitSipApp releases the lock on the
				// Application of SipSession (concurrency control lock). If this happens a new request might arrive
				// and modify the state during Serialization or other non-thread safe operation in the serialization
				sipContext.exitSipAppHa(sipServletRequest, null, batchStarted);
				sipContext.exitSipApp(sipSession.getSipApplicationSession(), sipSession);				
			}
			//nothing more needs to be done, either the app acted as UA, PROXY or B2BUA. in any case we stop routing	
		}
		
		// Issue 2850 :	Use Request-URI custom Mobicents parameters to route request for misbehaving agents, workaround for Cisco-SIPGateway/IOS-12.x user agent
		private void checkRequestURIForNonCompliantAgents(MobicentsProxyBranch finalBranch, Request request) {
			URI requestURI = request.getRequestURI();
			if(request.getRequestURI() instanceof javax.sip.address.SipURI && ((Parameters)requestURI).getParameter(MessageDispatcher.RR_PARAM_PROXY_APP) != null && requestURI instanceof SipURI) {								
				final String host = ((SipURI) requestURI).getHost();
				final int port = ((SipURI) requestURI).getPort();
				final String transport = JainSipUtils.findTransport(request);
				boolean isAnotherDomain = StaticServiceHolder.sipStandardService.getSipApplicationDispatcher().isExternal(host, port, transport);
				if(!isAnotherDomain) {
					if(logger.isDebugEnabled()) {
						logger.debug("Non Compliant Agent targeting Mobicents directly, Changing the request URI from " + requestURI + " to " + finalBranch.getTargetURI() + " to avoid going in a loop");
					}
					request.setRequestURI(((URIImpl)finalBranch.getTargetURI()).getURI());
				}								
			}
		}
	}
	
}
