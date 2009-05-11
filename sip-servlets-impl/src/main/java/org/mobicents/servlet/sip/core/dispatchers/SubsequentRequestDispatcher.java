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

import gov.nist.javax.sip.header.extensions.JoinHeader;
import gov.nist.javax.sip.header.extensions.ReplacesHeader;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.sip.ProxyBranch;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.Parameters;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * This class is responsible for routing and dispatching subsequent request to applications according to JSR 289 Section 
 * 15.6 Responses, Subsequent Requests and Application Path 
 * 
 * It uses route header parameters that were previously set by the container on 
 * record route headers to know which app has to be called 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SubsequentRequestDispatcher extends RequestDispatcher {

	private static transient Logger logger = Logger.getLogger(SubsequentRequestDispatcher.class);
	
	public SubsequentRequestDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {
		super(sipApplicationDispatcher);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void dispatchMessage(final SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {
		final SipFactoryImpl sipFactoryImpl = sipApplicationDispatcher.getSipFactory();
		final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
		if(logger.isDebugEnabled()) {
			logger.debug("Routing of Subsequent Request " + sipServletRequest);
		}	
		
		final ServerTransaction transaction = (ServerTransaction) sipServletRequest.getTransaction();
		final Request request = (Request) sipServletRequest.getMessage();
		final Dialog dialog = sipServletRequest.getDialog();				
				
		if(sipServletRequest.getPoppedRouteHeader() ==null){
			if(Request.ACK.equals(request.getMethod())) {
				//Means that this is an ACK to a container generated error response, so we can drop it
				if(logger.isDebugEnabled()) {
					logger.debug("The popped route is null for an ACK, this is an ACK to a container generated error response, so it is dropped");
				}
				return ;
			} else {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "The popped route shouldn't be null for not proxied requests.");
			}
		}
		Parameters poppedAddress = (Parameters)sipServletRequest.getPoppedRouteHeader().getAddress().getURI();
		//Extract information from the Route Header		
		String applicationNameHashed = poppedAddress.getParameter(RR_PARAM_APPLICATION_NAME);	
		final String finalResponse = poppedAddress.getParameter(FINAL_RESPONSE);
		String applicationId = poppedAddress.getParameter(APP_ID);
		String generatedApplicationKey = poppedAddress.getParameter(GENERATED_APP_KEY);
		boolean isAppGenerated = false;
		if(generatedApplicationKey != null) {
			applicationId = RFC2396UrlDecoder.decode(generatedApplicationKey);
			isAppGenerated = true;
		}
		if(applicationNameHashed == null || applicationNameHashed.length() < 1) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot find the application to handle this subsequent request " +
					"in this popped routed header " + poppedAddress);
		}
		String applicationName = sipApplicationDispatcher.getApplicationNameFromHash(applicationNameHashed);
		boolean inverted = false;
		if(dialog != null && !dialog.isServer()) {
			inverted = true;
		}
		
		final SipContext sipContext = sipApplicationDispatcher.findSipApplication(applicationName);
		if(sipContext == null) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot find the application to handle this subsequent request " +
					"in this popped routed header " + poppedAddress);
		}
		final SipManager sipManager = (SipManager)sipContext.getManager();		
		SipApplicationSessionKey sipApplicationSessionKey = null;
		if(applicationId != null && applicationId.length() > 0) {
			sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
					applicationName, 
					applicationId,
					isAppGenerated);
		} else {
			sipApplicationSessionKey = makeAppSessionKey(
				sipContext, sipServletRequest, applicationName);
		}		
		
		MobicentsSipSession tmpSipSession = null;
		MobicentsSipApplicationSession sipApplicationSession = sipManager.getSipApplicationSession(sipApplicationSessionKey, false);
		if(sipApplicationSession == null) {
			sipManager.dumpSipApplicationSessions();
			//trying the join or replaces matching sip app sessions
			SipApplicationSessionKey joinSipApplicationSessionKey = sipContext.getSipSessionsUtil().getCorrespondingSipApplicationSession(sipApplicationSessionKey, JoinHeader.NAME);
			SipApplicationSessionKey replacesSipApplicationSessionKey = sipContext.getSipSessionsUtil().getCorrespondingSipApplicationSession(sipApplicationSessionKey, ReplacesHeader.NAME);
			if(joinSipApplicationSessionKey != null) {
				sipApplicationSession = sipManager.getSipApplicationSession(joinSipApplicationSessionKey, false);
				sipApplicationSessionKey = joinSipApplicationSessionKey;
			} else if(replacesSipApplicationSessionKey != null) {
				sipApplicationSession = sipManager.getSipApplicationSession(replacesSipApplicationSessionKey, false);
				sipApplicationSessionKey = replacesSipApplicationSessionKey;
			}
			if(sipApplicationSession == null) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Cannot find the corresponding sip application session to this subsequent request " + request +
					" with the following popped route header " + sipServletRequest.getPoppedRoute());
			}
		}
		
		SipSessionKey key = SessionManagerUtil.getSipSessionKey(applicationName, request, inverted);
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
			key = SessionManagerUtil.getSipSessionKey(applicationName, request, !inverted);
			tmpSipSession = sipManager.getSipSession(key, false, sipFactoryImpl, sipApplicationSession);
		}
		
		if(tmpSipSession == null) {
			sipManager.dumpSipSessions();
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Cannot find the corresponding sip session with key " + key + " to this subsequent request " + request +
					" with the following popped route header " + sipServletRequest.getPoppedRoute());
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Inverted try worked. sip session found : " + tmpSipSession.getId());
			}
		}			
		
		final MobicentsSipSession sipSession = tmpSipSession;
		sipServletRequest.setSipSession(sipSession);
		
		DispatchTask dispatchTask = new DispatchTask(sipServletRequest, sipProvider) {

			public void dispatch() throws DispatcherException {
				sipContext.enterSipApp(sipServletRequest, null, sipManager, true, true);
				try {
					sipSession.setSessionCreatingTransaction(sipServletRequest.getTransaction());
					// JSR 289 Section 6.2.1 :
					// any state transition caused by the reception of a SIP message, 
					// the state change must be accomplished by the container before calling 
					// the service() method of any SipServlet to handle the incoming message.
					sipSession.updateStateOnSubsequentRequest(sipServletRequest, true);
					try {
						// RFC 3265 : If a matching NOTIFY request contains a "Subscription-State" of "active" or "pending", it creates
						// a new subscription and a new dialog (unless they have already been
						// created by a matching response, as described above).
						SubscriptionStateHeader subscriptionStateHeader = (SubscriptionStateHeader) 
							sipServletRequest.getMessage().getHeader(SubscriptionStateHeader.NAME);		
						if(Request.NOTIFY.equals(sipServletRequest.getMethod()) && 
										(subscriptionStateHeader != null && 
												SubscriptionStateHeader.ACTIVE.equalsIgnoreCase(subscriptionStateHeader.getState()) ||
												SubscriptionStateHeader.PENDING.equalsIgnoreCase(subscriptionStateHeader.getState()))) {					
							sipSession.addSubscription(sipServletRequest);
						}
						// A subscription is destroyed when a notifier sends a NOTIFY request
						// with a "Subscription-State" of "terminated".			
						if(Request.NOTIFY.equals(sipServletRequest.getMethod()) && 
										(subscriptionStateHeader != null && 
												SubscriptionStateHeader.TERMINATED.equalsIgnoreCase(subscriptionStateHeader.getState()))) {
							sipSession.removeSubscription(sipServletRequest);
						}
								
						// See if the subsequent request should go directly to the proxy
						ProxyImpl proxy = sipSession.getProxy();
						if(proxy != null) {
							if(proxy.getFinalBranchForSubsequentRequests() != null) {
								ProxyBranchImpl proxyBranch = sipSession.getProxy().getFinalBranchForSubsequentRequests();
								proxy.setAckReceived(sipServletRequest.getMethod().equalsIgnoreCase(Request.ACK));
								proxy.setOriginalRequest(sipServletRequest);
								callServlet(sipServletRequest);
								proxyBranch.proxySubsequentRequest(sipServletRequest);
							} else if(sipServletRequest.getMethod().equals(Request.PRACK)) {
								callServlet(sipServletRequest);
								List<ProxyBranch> branches = sipSession.getProxy().getProxyBranches();
								for(ProxyBranch pb : branches) {
									ProxyBranchImpl proxyBranch = (ProxyBranchImpl) pb;
									if(proxyBranch.isWaitingForPrack()) {
										proxyBranch.proxyRequestTxStateful(sipServletRequest, true);
										proxyBranch.setWaitingForPrack(false);
									}
								}
							}
						}
						// If it's not for a proxy then it's just an AR, so go to the next application
						else {							
							callServlet(sipServletRequest);				
						}
					} catch (ServletException e) {
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
					} catch (SipException e) {
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
					} catch (IOException e) {				
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while processing the following subsequent request " + request, e);
					} 	 
				} finally {
					sipContext.exitSipApp(sipServletRequest, null);
				}
				//nothing more needs to be done, either the app acted as UA, PROXY or B2BUA. in any case we stop routing	
			}
		};
		getConcurrencyModelExecutorService(sipContext, sipServletMessage).execute(dispatchTask);
	}	

}
