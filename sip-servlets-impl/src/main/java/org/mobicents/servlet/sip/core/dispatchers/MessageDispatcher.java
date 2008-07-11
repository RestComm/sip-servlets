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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ListIterator;
import java.util.Vector;

import javax.servlet.sip.ar.SipRouteModifier;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.core.SipSessionRoutingType;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletRequestReadOnly;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class MessageDispatcher {

	private static Log logger = LogFactory.getLog(MessageDispatcher.class);
	
	public static final String ROUTE_PARAM_DIRECTIVE = "directive";
	
	public static final String ROUTE_PARAM_PREV_APPLICATION_NAME = "previousappname";
	/* 
	 * This parameter is to know which app handled the request 
	 */
	public static final String RR_PARAM_APPLICATION_NAME = "appname";
	/* 
	 * This parameter is to know which servlet handled the request 
	 */
	public static final String RR_PARAM_HANDLER_NAME = "handler";
	/* 
	 * This parameter is to know if a servlet application sent a final response
	 */
	public static final String FINAL_RESPONSE = "final_response";
	/* 
	 * This parameter is to know if a servlet application has generated its own application key
	 */
	public static final String GENERATED_APP_KEY = "gen_app_key";
	/* 
	 * This parameter is to know when an app was not deployed and couldn't handle the request
	 * used so that the response doesn't try to call the app not deployed
	 */
	public static final String APP_NOT_DEPLOYED = "appnotdeployed";
	/* 
	 * This parameter is to know when no app was returned by the AR when doing the initial selection process
	 * used so that the response is forwarded externally directly
	 */
	public static final String NO_APP_RETURNED = "noappreturned";
	/*
	 * This parameter is to know when the AR returned an external route instead of an app
	 */
	public static final String MODIFIER = "modifier";
	
	protected SipApplicationDispatcher sipApplicationDispatcher = null;
		
	public MessageDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
		this.sipApplicationDispatcher = sipApplicationDispatcher;
	}
	
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
	public final void forwardStatefully(SipServletRequestImpl sipServletRequest, SipSessionRoutingType sipSessionRoutingType, SipRouteModifier sipRouteModifier) throws ParseException,
			TransactionUnavailableException, SipException, InvalidArgumentException {
		final SipNetworkInterfaceManager sipNetworkInterfaceManager = sipApplicationDispatcher.getSipNetworkInterfaceManager();
		Request clonedRequest = (Request)sipServletRequest.getMessage().clone();		
		
		//Add via header
		String transport = JainSipUtils.findTransport(clonedRequest);		
		ViaHeader viaHeader = JainSipUtils.createViaHeader(
				sipNetworkInterfaceManager, transport, null);
		SipSessionImpl session = sipServletRequest.getSipSession();
		
		if(session != null) {			
			if(SipSessionRoutingType.CURRENT_SESSION.equals(sipSessionRoutingType)) {
				//an app was found or an app was returned by the AR but not found
				String handlerName = session.getHandler();
				if(handlerName != null) { 
					viaHeader.setParameter(RR_PARAM_APPLICATION_NAME,
							sipServletRequest.getSipSession().getKey().getApplicationName());
					viaHeader.setParameter(RR_PARAM_HANDLER_NAME,
							sipServletRequest.getSipSession().getHandler());					
				} else {				
					// if the handler name is null it means that the app returned by the AR was not deployed
					// and couldn't be called, 
					// we specify it so that on response handling this app can be skipped
					viaHeader.setParameter(APP_NOT_DEPLOYED,
							sipServletRequest.getSipSession().getKey().getApplicationName());					
				}			
				clonedRequest.addHeader(viaHeader);
			} else {				
				if(SipRouteModifier.NO_ROUTE.equals(sipRouteModifier)) {
					// no app was returned by the AR 				
					viaHeader.setParameter(NO_APP_RETURNED,
						"noappreturned");
				} else {
					viaHeader.setParameter(MODIFIER,
							sipRouteModifier.toString());
				}
				clonedRequest.addHeader(viaHeader);
			}
		} else {
			if(SipRouteModifier.NO_ROUTE.equals(sipRouteModifier)) {
				// no app was returned by the AR and no application was selected before
				// if the handler name is null it means that the app returned by the AR was not deployed
				// and couldn't be called, 
				// we specify it so that on response handling this app can be skipped
				viaHeader.setParameter(NO_APP_RETURNED,
						"noappreturned");
			} else {
				viaHeader.setParameter(MODIFIER,
						sipRouteModifier.toString());
			}
			clonedRequest.addHeader(viaHeader);	 			
		}		
		//decrease the Max Forward Header
		MaxForwardsHeader mf = (MaxForwardsHeader) clonedRequest
			.getHeader(MaxForwardsHeader.NAME);
		if (mf == null) {
			mf = SipFactories.headerFactory.createMaxForwardsHeader(JainSipUtils.MAX_FORWARD_HEADER_VALUE);
			clonedRequest.addHeader(mf);
		} else {
			//TODO send error meesage for loop if maxforward < 0
			mf.setMaxForwards(mf.getMaxForwards() - 1);
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
		ExtendedListeningPoint extendedListeningPoint = 
			sipNetworkInterfaceManager.findMatchingListeningPoint(transport, false);
		if(logger.isDebugEnabled()) {
			logger.debug("Matching listening point found " 
					+ extendedListeningPoint);
		}
		SipProvider sipProvider = extendedListeningPoint.getSipProvider();		
		ServerTransaction serverTransaction = (ServerTransaction) sipServletRequest.getTransaction();
		Dialog dialog = sipServletRequest.getDialog();
		if(logger.isDebugEnabled()) {
			logger.debug("Dialog existing " 
					+ dialog != null);
		}
		if(dialog == null) {			
			Transaction transaction = ((TransactionApplicationData)
					serverTransaction.getApplicationData()).getSipServletMessage().getTransaction();
			if(transaction == null || transaction instanceof ServerTransaction) {
				ClientTransaction ctx = sipProvider.getNewClientTransaction(clonedRequest);
				//keeping the server transaction in the client transaction's application data
				TransactionApplicationData appData = new TransactionApplicationData(sipServletRequest);					
				appData.setTransaction(serverTransaction);
				ctx.setApplicationData(appData);				
				//keeping the client transaction in the server transaction's application data
				((TransactionApplicationData)serverTransaction.getApplicationData()).setTransaction(ctx);
				if(logger.isInfoEnabled()) {
					logger.info("Sending the request through a new client transaction " + clonedRequest);
				}
				ctx.sendRequest();												
			} else {
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
            ListIterator<String> headerNamesListIterator=clonedRequest.getHeaderNames();
            while (headerNamesListIterator.hasNext()) {
                 String name=headerNamesListIterator.next();
                 Header header=dialogRequest.getHeader(name);
                 if (header==null  ) {
                    ListIterator<Header> li=clonedRequest.getHeaders(name);
                    if (li!=null) {
                        while (li.hasNext() ) {
                            Header  h = li.next();
                            dialogRequest.addHeader(h);
                        }
                    }
                 }
                 else {
                     if ( header instanceof ViaHeader) {
                         ListIterator<Header> li= clonedRequest.getHeaders(name);
                         if (li!=null) {
                             dialogRequest.removeHeader(name);
                             Vector v=new Vector();
                             while (li.hasNext() ) {
                                 Header  h=li.next();
                                 v.addElement(h);
                             }
                             for (int k=(v.size()-1);k>=0;k--) {
                                 Header  h=(Header)v.elementAt(k);
                                 dialogRequest.addHeader(h);
                             }
                         }
                     }
                 }
            }       
            	                    
            ClientTransaction clientTransaction =
			sipProvider.getNewClientTransaction(dialogRequest);
            //keeping the server transaction in the client transaction's application data
			TransactionApplicationData appData = new TransactionApplicationData(sipServletRequest);					
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
	
	/**
	 * 
	 * @param errorCode
	 * @param transaction
	 * @param request
	 * @param sipProvider
	 */
	public static void sendErrorResponse(int errorCode,
			ServerTransaction transaction, Request request,
			SipProvider sipProvider) {
		try{
			Response response=SipFactories.messageFactory.createResponse
	        	(errorCode,request);			
	        if (transaction!=null) {
	        	transaction.sendResponse(response);
	        } else { 
	        	sipProvider.sendResponse(response);
	        }
		} catch (Exception e) {
			logger.error("Problem while sending the error response to the following request "
					+ request.toString(), e);
		}
	}
	
	protected static SipApplicationSessionKey makeAppSessionKey(SipContext sipContext, SipServletRequestImpl sipServletRequestImpl, String applicationName) {
		String id = null;
		boolean isAppGeneratedKey = false;
		Request request = (Request) sipServletRequestImpl.getMessage();
		Method appKeyMethod = null;
		// Session Key Based Targeted Mechanism is oly for Initial Requests 
		// see JSR 289 Section 15.11.2 Session Key Based Targeting Mechanism 
		if(sipServletRequestImpl.isInitial() && sipContext != null) {
			appKeyMethod = sipContext.getSipApplicationKeyMethod();
		}
		if(appKeyMethod != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("For request target to application " + sipContext.getApplicationName() + 
						", using the following annotated method to generate the application key " + appKeyMethod);
			}
			SipServletRequestReadOnly readOnlyRequest = new SipServletRequestReadOnly(sipServletRequestImpl);
			try {
				id = (String) appKeyMethod.invoke(null, new Object[] {readOnlyRequest});
				isAppGeneratedKey = true;
			} catch (IllegalArgumentException e) {
				logger.error("Couldn't invoke the app session key annotated method !", e);
			} catch (IllegalAccessException e) {
				logger.error("Couldn't invoke the app session key annotated method !", e);
			} catch (InvocationTargetException e) {
				logger.error("Couldn't invoke the app session key annotated method !", e);
			} finally {
				readOnlyRequest = null;
			}
			if(id == null) {
				throw new IllegalStateException("SipApplicationKey annotated method shoud not return null");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("For request target to application " + sipContext.getApplicationName() + 
						", following annotated method " + appKeyMethod + " generated the application key : " + id);
			}
		} else {
			id = ((CallIdHeader)request.getHeader((CallIdHeader.NAME))).getCallId();
		}
		SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
				applicationName, 
				id,
				isAppGeneratedKey);
		return sipApplicationSessionKey;
	}
	/**
	 * Responsible for routing and dispatching a SIP message to the correct application
	 * 
	 * @param sipProvider use the sipProvider to route the message if needed, can be null
	 * @param sipServletMessage the SIP message to route and dispatch
	 * @return true if we should continue routing to other applications, false otherwise
	 * @throws Exception if anything wrong happens
	 */
	public abstract boolean dispatchMessage(SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws Exception;
}
