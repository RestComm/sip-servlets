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

import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.message.MessageExt;

import java.text.ParseException;

import javax.servlet.sip.ar.SipRouteModifier;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.core.MobicentsExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.SipSessionRoutingType;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class RequestDispatcher extends MessageDispatcher {

	private static final Logger logger = Logger.getLogger(RequestDispatcher.class);
	
	public RequestDispatcher() {}
	
//	public RequestDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
//		super(sipApplicationDispatcher);
//	}

	/**
	 * Forward statefully a request whether it is initial or subsequent
	 * and keep track of the transactions used in application data of each transaction
	 * @param sipServletRequest the sip servlet request to forward statefully
	 * @param sipRouteModifier the route modifier returned by the AR when receiving the request
	 * @throws ParseException 
	 * @throws TransactionUnavailableException
	 * @throws SipException
	 * @throws InvalidArgumentException 
	 */
	protected final void forwardRequestStatefully(SipServletRequestImpl sipServletRequest, SipSessionRoutingType sipSessionRoutingType, SipRouteModifier sipRouteModifier) throws Exception {
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipApplicationDispatcher.getSipNetworkInterfaceManager();
		Request clonedRequest = (Request)sipServletRequest.getMessage().clone();		
		
		//Add via header
		String transport = JainSipUtils.findTransport(clonedRequest);
		((MessageExt)clonedRequest).setApplicationData(null);
		MobicentsSipSession session = sipServletRequest.getSipSession();
		String outboundInterface = null;
		if(session != null) {
			outboundInterface = session.getOutboundInterface();
		}
		ViaHeader viaHeader = JainSipUtils.createViaHeader(
				sipNetworkInterfaceManager, clonedRequest, null, outboundInterface);		
		
		// used for response routing, stored later in the tx app data so that it interoperates with UA/proxies not handling correctly via params 
		String appNotDeployed = null;
		boolean noAppReturned = false;
		String modifier = null;
		
		if(session != null) {			
			if(SipSessionRoutingType.CURRENT_SESSION.equals(sipSessionRoutingType)) {
				//an app was found or an app was returned by the AR but not found
				String handlerName = session.getHandler();
				if(handlerName != null) {
					final String branch = JainSipUtils.createBranch(session.getSipApplicationSession().getKey().getId(),  sipApplicationDispatcher.getHashFromApplicationName(session.getKey().getApplicationName()));
					viaHeader.setBranch(branch);
				} else {				
					// if the handler name is null it means that the app returned by the AR was not deployed
					// and couldn't be called, 
					// we specify it so that on response handling this app can be skipped
					final String branch = JainSipUtils.createBranch(session.getSipApplicationSession().getKey().getId(),  sipApplicationDispatcher.getHashFromApplicationName(session.getKey().getApplicationName()));
					viaHeader.setBranch(branch);
					appNotDeployed = session.getKey().getApplicationName();					
				}			
				clonedRequest.addHeader(viaHeader);
			} else {				
				if(SipRouteModifier.NO_ROUTE.equals(sipRouteModifier)) {
					// no app was returned by the AR 				
					noAppReturned = true;
				} else {
					modifier = sipRouteModifier.toString();
				}
				clonedRequest.addHeader(viaHeader);
			}
		} else {
			if(SipRouteModifier.NO_ROUTE.equals(sipRouteModifier)) {
				// no app was returned by the AR and no application was selected before
				// if the handler name is null it means that the app returned by the AR was not deployed
				// and couldn't be called, 
				// we specify it so that on response handling this app can be skipped
				noAppReturned = true;
			} else {
				modifier = sipRouteModifier.toString();
			}
			clonedRequest.addHeader(viaHeader);	 			
		}
		MobicentsExtendedListeningPoint extendedListeningPoint = 
			sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		if(logger.isDebugEnabled()) {
			logger.debug("Matching listening point found " 
					+ extendedListeningPoint);
		}
		SipProvider sipProvider = extendedListeningPoint.getSipProvider();		
		ServerTransaction serverTransaction = (ServerTransaction) sipServletRequest.getTransaction();
		Dialog dialog = sipServletRequest.getDialog();
		
		
		//decrease the Max Forward Header
		MaxForwardsHeader maxForwardsHeader = (MaxForwardsHeader) clonedRequest
			.getHeader(MaxForwardsHeader.NAME);
		if (maxForwardsHeader == null) {
			maxForwardsHeader = SipFactoryImpl.headerFactory.createMaxForwardsHeader(JainSipUtils.MAX_FORWARD_HEADER_VALUE);
			clonedRequest.addHeader(maxForwardsHeader);
		} else {
			if(maxForwardsHeader.getMaxForwards() - 1 > 0) {
				maxForwardsHeader.setMaxForwards(maxForwardsHeader.getMaxForwards() - 1);
			} else {
				//Max forward header equals to 0, thus sending too many hops response
				sendErrorResponse(Response.TOO_MANY_HOPS, sipServletRequest, sipProvider);
				return;
			}
		}		
		
		if(logger.isDebugEnabled()) {
			if(SipRouteModifier.NO_ROUTE.equals(sipRouteModifier)) {
				logger.debug("Routing Back to the container the following request " 
					+ clonedRequest);
			} else {
				logger.debug("Routing externally the following request " 
						+ clonedRequest);
			}
		}	
		if(logger.isDebugEnabled()) {
			logger.debug("Dialog existing " + (dialog != null));
		}
		if(dialog == null) {			
			Transaction transaction = ((TransactionApplicationData)
					serverTransaction.getApplicationData()).getSipServletMessage().getTransaction();
			if(transaction == null || transaction instanceof ServerTransaction) {
				ClientTransaction ctx = sipProvider.getNewClientTransaction(clonedRequest);
				JainSipUtils.setTransactionTimers((TransactionExt) ctx, sipApplicationDispatcher);
				//keeping the server transaction in the client transaction's application data
				TransactionApplicationData appData = new TransactionApplicationData(sipServletRequest);					
				appData.setTransaction(serverTransaction);
				appData.setNoAppReturned(noAppReturned);
				appData.setAppNotDeployed(appNotDeployed);
				appData.setModifier(modifier);
				ctx.setApplicationData(appData);				
				//keeping the client transaction in the server transaction's application data
				((TransactionApplicationData)serverTransaction.getApplicationData()).setTransaction(ctx);
				if(logger.isInfoEnabled()) {
					logger.info("Sending the request through a new client transaction " + clonedRequest);
				}	
				try {
					ctx.sendRequest();
				} catch(SipException e) {
					JainSipUtils.terminateTransaction(ctx);
					throw e;
				}
			} else {				
				TransactionApplicationData appData = (TransactionApplicationData) transaction.getApplicationData();
				if(appData == null) {
					appData = new TransactionApplicationData(sipServletRequest);
					appData.setTransaction(transaction);
				}
				appData.setNoAppReturned(noAppReturned);
				appData.setAppNotDeployed(appNotDeployed);
				appData.setModifier(modifier);
				//keeping the client transaction in the server transaction's application data
				((TransactionApplicationData)serverTransaction.getApplicationData()).setTransaction(transaction);
				if(logger.isInfoEnabled()) {
					logger.info("Sending the request through the existing transaction " + clonedRequest);
				}
				((ClientTransaction)transaction).sendRequest();
			}
		} else if ( clonedRequest.getMethod().equals("ACK") ) {
			if(logger.isInfoEnabled()) {
				logger.info("Sending the ACK through the dialog " + clonedRequest);
			}
            dialog.sendAck(clonedRequest);
		} else {
			Request dialogRequest=
				dialog.createRequest(clonedRequest.getMethod());
	        Object content=clonedRequest.getContent();
	        if (content!=null) {
	        	ContentTypeHeader contentTypeHeader= (ContentTypeHeader)
	        		clonedRequest.getHeader(ContentTypeHeader.NAME);
	            if (contentTypeHeader!=null) {
	            	dialogRequest.setContent(content,contentTypeHeader);
	        	}
	        }
            // Copy all the headers from the original request to the 
            // dialog created request:	
	        // => not needed already done by the clone method
//            ListIterator<String> headerNamesListIterator=clonedRequest.getHeaderNames();
//            while (headerNamesListIterator.hasNext()) {
//                 String name=headerNamesListIterator.next();
//                 Header header=dialogRequest.getHeader(name);
//                 if (header==null  ) {
//                    ListIterator<Header> li=clonedRequest.getHeaders(name);
//                    if (li!=null) {
//                        while (li.hasNext() ) {
//                            Header  h = li.next();
//                            dialogRequest.addHeader(h);
//                        }
//                    }
//                 }
//                 else {
//                     if ( header instanceof ViaHeader) {
//                         ListIterator<Header> li= clonedRequest.getHeaders(name);
//                         if (li!=null) {
//                             dialogRequest.removeHeader(name);
//                             Vector v=new Vector();
//                             while (li.hasNext() ) {
//                                 Header  h=li.next();
//                                 v.addElement(h);
//                             }
//                             for (int k=(v.size()-1);k>=0;k--) {
//                                 Header  h=(Header)v.elementAt(k);
//                                 dialogRequest.addHeader(h);
//                             }
//                         }
//                     }
//                 }
//            }       
            	                    
            ClientTransaction clientTransaction =
            	sipProvider.getNewClientTransaction(dialogRequest);
            JainSipUtils.setTransactionTimers((TransactionExt) clientTransaction, sipApplicationDispatcher);
            //keeping the server transaction in the client transaction's application data
			TransactionApplicationData appData = new TransactionApplicationData(sipServletRequest);
			appData.setNoAppReturned(noAppReturned);
			appData.setAppNotDeployed(appNotDeployed);
			appData.setModifier(modifier);
			appData.setTransaction(serverTransaction);
			clientTransaction.setApplicationData(appData);
			//keeping the client transaction in the server transaction's application data
			((TransactionApplicationData)serverTransaction.getApplicationData()).setTransaction(clientTransaction);
			dialog.setApplicationData(appData);
			if(logger.isInfoEnabled()) {
				logger.info("Sending the request through the dialog " + clonedRequest);
			}
            dialog.sendRequest(clientTransaction);	        
		}
	}
}
