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
import java.io.Serializable;
import java.text.ParseException;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.SipTargetedRequestInfo;
import javax.servlet.sip.ar.SipTargetedRequestType;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.Header;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.SipSessionRoutingType;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletRequestReadOnly;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipServletMapping;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class InitialRequestDispatcher extends RequestDispatcher {

	private static Log logger = LogFactory.getLog(InitialRequestDispatcher.class);
	private SipApplicationRouter sipApplicationRouter;
	
	public InitialRequestDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher, SipApplicationRouter sipApplicationRouter) {		
		super(sipApplicationDispatcher);
		this.sipApplicationRouter = sipApplicationRouter;
	}

	/**
	 * 
	 * @param sipProvider
	 * @param transaction
	 * @param request
	 * @param session
	 * @param sipServletRequest
	 * @throws ParseException 
	 * @throws InvalidArgumentException 
	 * @throws SipException 
	 * @throws TransactionUnavailableException 
	 */
	public boolean routeInitialRequest(SipProvider sipProvider, SipServletRequestImpl sipServletRequest) throws ParseException, TransactionUnavailableException, SipException, InvalidArgumentException {
		final SipFactoryImpl sipFactoryImpl = (SipFactoryImpl) sipApplicationDispatcher.getSipFactory();

		ServerTransaction transaction = (ServerTransaction) sipServletRequest.getTransaction();
		Request request = (Request) sipServletRequest.getMessage();
		
		javax.servlet.sip.Address poppedAddress = sipServletRequest.getPoppedRoute();
		if(logger.isInfoEnabled()) {
			logger.info("popped route : " + poppedAddress);
		}
		//set directive from popped route header if it is present			
		Serializable stateInfo = null;
		SipApplicationRoutingDirective sipApplicationRoutingDirective = SipApplicationRoutingDirective.NEW;;
		if(poppedAddress != null) {
			// get the state info associated with the request because it means 
			// that is has been routed back to the container			
			String directive = poppedAddress.getParameter(ROUTE_PARAM_DIRECTIVE);
			if(directive != null && directive.length() > 0) {
				if(logger.isInfoEnabled()) {
					logger.info("directive before the request has been routed back to container : " + directive);
				}
				sipApplicationRoutingDirective = SipApplicationRoutingDirective.valueOf(
						SipApplicationRoutingDirective.class, directive);
				String previousAppName = poppedAddress.getParameter(ROUTE_PARAM_PREV_APPLICATION_NAME);
				if(logger.isInfoEnabled()) {
					logger.info("application name before the request has been routed back to container : " + previousAppName);
				}
				SipContext sipContext = sipApplicationDispatcher.findSipApplication(previousAppName);				
				SipSessionKey sipSessionKey = SessionManagerUtil.getSipSessionKey(previousAppName, request, false);
				SipSessionImpl sipSession = ((SipManager)sipContext.getManager()).getSipSession(sipSessionKey, false, sipFactoryImpl, null);
				stateInfo = sipSession.getStateInfo();
				sipServletRequest.setSipSession(sipSession);
				if(logger.isInfoEnabled()) {
					logger.info("state info before the request has been routed back to container : " + stateInfo);
				}
			}
		} else if(sipServletRequest.getSipSession() != null) {
			stateInfo = sipServletRequest.getSipSession().getStateInfo();
			sipApplicationRoutingDirective = sipServletRequest.getRoutingDirective();
			if(logger.isInfoEnabled()) {
				logger.info("previous state info : " + stateInfo);
			}
		}
		
		// 15.4.1 Routing an Initial request Algorithm
		// 15.4.1 Procedure : point 1		
		SipApplicationRoutingRegion routingRegion = null;
		if(sipServletRequest.getSipSession() != null) {
			routingRegion = sipServletRequest.getSipSession().getRegion();
		}
		
		// 15.11.3 Target Session : Encode URI mechanism
		// Upon receiving an initial request for processing, a container MUST check the topmost Route header and 
		// Request-URI (in that order) to see if it contains an encoded URI. 
		// If it does, the container MUST use the encoded URI to locate the targeted SipApplicationSession object
		SipTargetedRequestInfo targetedRequestInfo = null;
		String targetedApplicationKey = sipServletRequest.getRequestURI().getParameter(SipApplicationSessionImpl.SIP_APPLICATION_KEY_PARAM_NAME);
		targetedRequestInfo = retrieveTargetedApplication(targetedApplicationKey);
		if(targetedRequestInfo == null && poppedAddress != null) {
			targetedApplicationKey = poppedAddress.getURI().getParameter(SipApplicationSessionImpl.SIP_APPLICATION_KEY_PARAM_NAME);
			targetedRequestInfo = retrieveTargetedApplication(targetedApplicationKey);
		}	
		
		SipServletRequestReadOnly sipServletRequestReadOnly = new SipServletRequestReadOnly(sipServletRequest);
		
		SipApplicationRouterInfo applicationRouterInfo = 
			sipApplicationRouter.getNextApplication(
					sipServletRequestReadOnly, 
					routingRegion, 
					sipApplicationRoutingDirective,
					targetedRequestInfo,
					stateInfo);
		
		sipServletRequestReadOnly= null;
		
		// 15.4.1 Procedure : point 2
		SipRouteModifier sipRouteModifier = applicationRouterInfo.getRouteModifier();
		if(logger.isDebugEnabled()) {
			logger.debug("the AR returned the following sip route modifier" + sipRouteModifier);
		}
		if(sipRouteModifier != null) {
			String[] route = applicationRouterInfo.getRoutes();
			switch(sipRouteModifier) {
				// ROUTE modifier indicates that SipApplicationRouterInfo.getRoute() returns a valid route,
				// it is up to container to decide whether it is external or internal.
				case ROUTE :					
					Address routeAddress = null; 
					RouteHeader applicationRouterInfoRouteHeader = null;
					try {
						routeAddress = SipFactories.addressFactory.createAddress(route[0]);
						applicationRouterInfoRouteHeader = SipFactories.headerFactory.createRouteHeader(routeAddress);
					} catch (ParseException e) {
						logger.error("Impossible to parse the route returned by the application router " +
								"into a compliant address",e);							
						// the AR returned an empty string route or a bad route
						// this shouldn't happen if the route modifier is ROUTE, processing is stopped
						//and a 500 is sent
						sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
						return false;
					}						
					if(sipApplicationDispatcher.isRouteExternal(applicationRouterInfoRouteHeader)) {						
						// push all of the routes on the Route header stack of the request and 
						// send the request externally
						for (int i = route.length-1 ; i >= 0; i--) {
							Header routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, route[i]);
							request.addHeader(routeHeader);
						}
						if(logger.isDebugEnabled()) {
							logger.debug("Routing the request externally " + sipServletRequest );
						}
						request.setRequestURI(SipFactories.addressFactory.createURI(route[0]));
						forwardStatefully(sipServletRequest, null, sipRouteModifier);
						return false;
					} else {
						// the container MUST make the route available to the applications 
						// via the SipServletRequest.getPoppedRoute() method.							
						sipServletRequest.setPoppedRoute(applicationRouterInfoRouteHeader);
						break;
					}				
				// NO_ROUTE indicates that application router is not returning any route 
				// and the SipApplicationRouterInfo.getRoute() value if any should be disregarded.	
				case NO_ROUTE :
					//nothing to do here
					//disregard the result.getRoute() and proceed. Specifically the SipServletRequest.getPoppedRoute() 
					//returns the same value as before the invocation of application router.
					break;
				// ROUTE_BACK directs the container to push its own route before 
				// pushing the external routes obtained from SipApplicationRouterInfo.getRoutes(). 
				case ROUTE_BACK :
					// Push container Route, pick up the first outbound interface
					SipURI sipURI = sipApplicationDispatcher.getOutboundInterfaces().get(0);
					sipURI.setParameter("modifier", "route_back");
					Header routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, sipURI.toString());
					request.addHeader(routeHeader);
					// push all of the routes on the Route header stack of the request and 
					// send the request externally
					for (int i = route.length-1 ; i >= 0; i--) {
						routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, route[i]);
						request.addHeader(routeHeader);
					}
					if(logger.isDebugEnabled()) {
						logger.debug("Routing the request externally " + sipServletRequest );
					}
					forwardStatefully(sipServletRequest, null, sipRouteModifier);
					return false;					
			}
		}
		// 15.4.1 Procedure : point 3
		if(applicationRouterInfo.getNextApplicationName() == null) {
			if(logger.isInfoEnabled()) {
				logger.info("Dispatching the request event outside the container");
			}
			//check if the request point to another domain
			if(request.getRequestURI() instanceof TelURL) {
				logger.error("cannot dispatch a request with a tel url request uri outside the container ");
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			}
			javax.sip.address.SipURI sipRequestUri = (javax.sip.address.SipURI)request.getRequestURI();
			
			String host = sipRequestUri.getHost();
			int port = sipRequestUri.getPort();
			String transport = JainSipUtils.findTransport(request);
			boolean isAnotherDomain = sipApplicationDispatcher.isExternal(host, port, transport);			
			ListIterator<String> routeHeaders = sipServletRequest.getHeaders(RouteHeader.NAME);				
			if(isAnotherDomain || routeHeaders.hasNext()) {
				forwardStatefully(sipServletRequest, SipSessionRoutingType.PREVIOUS_SESSION, SipRouteModifier.NO_ROUTE);
				return false;
			} else {
				// the Request-URI does not point to another domain, and there is no Route header, 
				// the container should not send the request as it will cause a loop. 
				// Instead, the container must reject the request with 404 Not Found final response with no Retry-After header.					 			
				sendErrorResponse(Response.NOT_FOUND, transaction, request, sipProvider);
				return false;
			}
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("Dispatching the request event to " + applicationRouterInfo.getNextApplicationName());
			}
			sipServletRequest.setCurrentApplicationName(applicationRouterInfo.getNextApplicationName());
			SipContext sipContext = sipApplicationDispatcher.findSipApplication(applicationRouterInfo.getNextApplicationName());
			// follow the procedures of Chapter 16 to select a servlet from the application.									
			//no matching deployed apps
			if(sipContext == null) {
				logger.error("No matching deployed application has been found !");
				// the app returned by the Application Router returned an app
				// that is not currently deployed, sends a 500 error response as was discussed 
				// in the JSR 289 EG, (for reference thread "Questions regarding AR" initiated by Uri Segev)
				// and stops processing.				
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			}
			SipManager sipManager = (SipManager)sipContext.getManager();
			//sip appliation session association
			SipApplicationSessionKey sipApplicationSessionKey = makeAppSessionKey(
					sipContext, sipServletRequest, applicationRouterInfo.getNextApplicationName());
			SipApplicationSessionImpl appSession = sipManager.getSipApplicationSession(
					sipApplicationSessionKey, true);
			//sip session association
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(applicationRouterInfo.getNextApplicationName(), request, false);
			SipSessionImpl sipSession = sipManager.getSipSession(sessionKey, true, sipFactoryImpl, appSession);
			sipSession.setSessionCreatingTransaction(transaction);
			sipServletRequest.setSipSession(sipSession);						
			
			// set the request's stateInfo to result.getStateInfo(), region to result.getRegion(), and URI to result.getSubscriberURI().			
			sipServletRequest.getSipSession().setStateInfo(applicationRouterInfo.getStateInfo());
			sipServletRequest.getSipSession().setRoutingRegion(applicationRouterInfo.getRoutingRegion());
			sipServletRequest.setRoutingRegion(applicationRouterInfo.getRoutingRegion());		
			try {
				URI subscriberUri = SipFactories.addressFactory.createURI(applicationRouterInfo.getSubscriberURI());				
				if(subscriberUri instanceof javax.sip.address.SipURI) {
					javax.servlet.sip.URI uri = new SipURIImpl((javax.sip.address.SipURI)subscriberUri);
					sipServletRequest.setRequestURI(uri);
					sipServletRequest.setSubscriberURI(uri);
				} else if (subscriberUri instanceof javax.sip.address.TelURL) {
					javax.servlet.sip.URI uri = new TelURLImpl((javax.sip.address.TelURL)subscriberUri);
					sipServletRequest.setRequestURI(uri);
					sipServletRequest.setSubscriberURI(uri);
				}
			} catch (ParseException pe) {					
				logger.error("Impossible to parse the subscriber URI returned by the Application Router " 
						+ applicationRouterInfo.getSubscriberURI() +
						", please put one of DAR:<HeaderName> with Header containing a valid URI or an exlicit valid URI ", pe);
				// Sends a 500 Internal server error and stops processing.				
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			} 									
			String sipSessionHandlerName = sipServletRequest.getSipSession().getHandler();						
			if(sipSessionHandlerName == null || sipSessionHandlerName.length() < 1) {
				String mainServlet = sipContext.getMainServlet();
				if(mainServlet != null && mainServlet.length() > 0) {
					sipSessionHandlerName = mainServlet;				
				} else {
					SipServletMapping sipServletMapping = sipContext.findSipServletMappings(sipServletRequest);
					if(sipServletMapping == null) {
						logger.error("Sending 404 because no matching servlet found for this request " + request);
						// Sends a 404 and stops processing.				
						sendErrorResponse(Response.NOT_FOUND, transaction, request, sipProvider);
						return false;
					} else {
						sipSessionHandlerName = sipServletMapping.getServletName();
					}
				}
				try {
					sipServletRequest.getSipSession().setHandler(sipSessionHandlerName);
				} catch (ServletException e) {
					// this should never happen
					logger.error("An unexpected servlet exception occured while routing an initial request",e);
					// Sends a 500 Internal server error and stops processing.				
					sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				} 
			}
			try {
				SipApplicationDispatcherImpl.callServlet(sipServletRequest);
				if(logger.isInfoEnabled()) {
					logger.info("Request event dispatched to " + sipContext.getApplicationName());
				}
			} catch (ServletException e) {				
				logger.error("An unexpected servlet exception occured while routing an initial request", e);
				// Sends a 500 Internal server error and stops processing.				
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			} catch (IOException e) {				
				logger.error("An unexpected IO exception occured while routing an initial request", e);
				// Sends a 500 Internal server error and stops processing.				
				sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			} 			
			//if a final response has been sent, or if the request has 
			//been proxied or relayed we stop routing the request
			RoutingState routingState = sipServletRequest.getRoutingState();			
			if(RoutingState.FINAL_RESPONSE_SENT.equals(routingState) ||
					RoutingState.PROXIED.equals(routingState) ||
					RoutingState.RELAYED.equals(routingState) ||
					RoutingState.CANCELLED.equals(routingState)) {
				if(logger.isDebugEnabled()) {
					logger.debug("Routing State : " + sipServletRequest.getRoutingState() +
							"The Container hence stops routing the initial request.");
				}
				return false;
			} else {
				if(logger.isDebugEnabled()) {
					logger.debug("Routing State : " + sipServletRequest.getRoutingState() +
							"The Container hence continue routing the initial request.");
				}
				try {
					// the app that was called didn't do anything with the request
					// in any case we should route back to container statefully 
					sipServletRequest.addAppCompositionRRHeader();
					SipApplicationRouterInfo routerInfo = sipApplicationDispatcher.getNextInterestedApplication(sipServletRequest);
					if(routerInfo.getNextApplicationName() != null) {
						if(logger.isDebugEnabled()) {
							logger.debug("routing back to the container " +
									"since the following app is interested " + routerInfo.getNextApplicationName());
						}
						//add a route header to direct the request back to the container 
						//to check if there is any other apps interested in it
						sipServletRequest.addInfoForRoutingBackToContainer(applicationRouterInfo.getNextApplicationName());
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("routing outside the container " +
									"since no more apps are is interested.");
							
							// If a servlet does not generate final response the routing process
							// will continue (non-terminating routing state). This code stops
							// routing these requests.
							javax.sip.address.SipURI sipRequestUri = (javax.sip.address.SipURI)request.getRequestURI();
							String host = sipRequestUri.getHost();
							int port = sipRequestUri.getPort();
							String transport = JainSipUtils.findTransport(request);
							boolean isAnotherDomain = sipApplicationDispatcher.isExternal(host, port, transport);
							if(!isAnotherDomain) return false;
						}
					}
					forwardStatefully(sipServletRequest, SipSessionRoutingType.CURRENT_SESSION, SipRouteModifier.NO_ROUTE);
					return true;
				} catch (SipException e) {				
					logger.error("an exception has occured when trying to forward statefully", e);
					// Sends a 500 Internal server error and stops processing.				
					sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				}
			}
		}		
	}

	/**
	 * Section 15.11.3 Encode URI Mechanism
	 * The container MUST use the encoded URI to locate the targeted SipApplicationSession object. 
	 * If a valid SipApplicationSession is found, the container must determine 
	 * the name of the application that owns the SipApplicationSession object.
	 * 
	 * @param targetedApplicationKey the encoded URI parameter
	 * @return null if no corresponding SipApplicationSession could be found or 
	 * if the param is null, the SipTargetedRequestInfo completed otherwise
	 */
	private final SipTargetedRequestInfo retrieveTargetedApplication(
			String targetedApplicationKey) {
		if( targetedApplicationKey != null && 
				targetedApplicationKey.length() > 0) {
			SipApplicationSessionKey targetedApplicationSessionKey;
			try {
				targetedApplicationSessionKey = SessionManagerUtil.parseSipApplicationSessionKey(targetedApplicationKey);
			} catch (ParseException e) {
				logger.error("Couldn't parse the targeted application key " + targetedApplicationKey, e);
				return null;
			}
			SipContext sipContext = sipApplicationDispatcher.findSipApplication(targetedApplicationSessionKey.getApplicationName());
			if(sipContext != null && ((SipManager)sipContext.getManager()).getSipApplicationSession(targetedApplicationSessionKey, false) != null) {
				return new SipTargetedRequestInfo(SipTargetedRequestType.ENCODED_URI, targetedApplicationSessionKey.getApplicationName());
			}				
		}
		return null;
	}
}
