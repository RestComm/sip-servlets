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
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.Header;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.web.tomcat.service.session.ConvergedSessionReplicationContext;
import org.jboss.web.tomcat.service.session.SnapshotSipManager;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipSessionRoutingType;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.core.session.SipStandardManager;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletRequestReadOnly;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipServletMapping;

/**
 * This class is responsible for implementing the logic for routing an initial request
 * according to the following sections of the JSR 289 specification Section 
 * 15.4.1 Procedure for Routing an Initial Request.
 * 15.11 Session Targeting 
 * 16 Mapping Requests To Servlets 
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class InitialRequestDispatcher extends RequestDispatcher {

	private static Log logger = LogFactory.getLog(InitialRequestDispatcher.class);
	private SipApplicationRouter sipApplicationRouter;
	
	public InitialRequestDispatcher(
			SipApplicationDispatcher sipApplicationDispatcher) {		
		super(sipApplicationDispatcher);
		this.sipApplicationRouter = sipApplicationDispatcher.getSipApplicationRouter();
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispatchMessage(SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException {
		final SipFactoryImpl sipFactoryImpl = sipApplicationDispatcher.getSipFactory();
		SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipServletMessage;
		if(logger.isInfoEnabled()) {
			logger.info("Routing of Initial Request " + sipServletRequest);
		}		
		final Request request = (Request) sipServletRequest.getMessage();
		
		javax.servlet.sip.Address poppedAddress = sipServletRequest.getPoppedRoute();
		if(logger.isInfoEnabled()) {
			logger.info("popped route : " + poppedAddress);
		}
		MobicentsSipSession sipSessionImpl = sipServletRequest.getSipSession();
		//set directive from popped route header if it is present			
		Serializable stateInfo = null;
		//If request is received from an external SIP entity, directive is set to NEW. 
		SipApplicationRoutingDirective sipApplicationRoutingDirective = SipApplicationRoutingDirective.NEW;
		if(poppedAddress != null) {
			// get the state info associated with the request because it means 
			// that is has been routed back to the container			
			String directive = poppedAddress.getParameter(ROUTE_PARAM_DIRECTIVE);
			if(directive != null && directive.length() > 0) {
				//If request is received from an application, directive is set either implicitly 
				//or explicitly by the application. 
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
				sipSessionImpl = sipContext.getSipManager().getSipSession(sipSessionKey, false, sipFactoryImpl, null);
				//If request is received from an application, and directive is CONTINUE or REVERSE, 
				//stateInfo is set to that of the original request that this request is associated with. 
				stateInfo = sipSessionImpl.getStateInfo();
				sipServletRequest.setSipSession(sipSessionImpl);
				if(logger.isInfoEnabled()) {
					logger.info("state info before the request has been routed back to container : " + stateInfo);
				}
			}
		} else if(sipSessionImpl != null) {
			stateInfo = sipSessionImpl.getStateInfo();
			sipApplicationRoutingDirective = sipServletRequest.getRoutingDirective();
			if(logger.isInfoEnabled()) {
				logger.info("previous state info : " + stateInfo);
			}
		}
		SipApplicationRoutingRegion routingRegion = null;
		if(sipSessionImpl != null) {
			routingRegion = sipSessionImpl.getRegion();
		}
		
		// 15.11.3 Target Session : Encode URI mechanism
		// Upon receiving an initial request for processing, a container MUST check the topmost Route header and 
		// Request-URI (in that order) to see if it contains an encoded URI. 
		// If it does, the container MUST use the encoded URI to locate the targeted SipApplicationSession object
		String targetedApplicationKey = sipServletRequest.getRequestURI().getParameter(MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME);
		SipTargetedRequestInfo targetedRequestInfo = retrieveTargetedApplication(targetedApplicationKey);
		if(targetedRequestInfo == null && poppedAddress != null) {
			targetedApplicationKey = poppedAddress.getURI().getParameter(MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME);
			targetedRequestInfo = retrieveTargetedApplication(targetedApplicationKey);
		}	
		
		// 15.4.1 Routing an Initial request Algorithm
		// 15.4.1 Procedure : point 1			
		
		//the application router shouldn't modify the request
		SipServletRequestReadOnly sipServletRequestReadOnly = new SipServletRequestReadOnly(sipServletRequest);
		
		SipApplicationRouterInfo applicationRouterInfo = 
			sipApplicationRouter.getNextApplication(
					sipServletRequestReadOnly, 
					routingRegion, 
					sipApplicationRoutingDirective,
					targetedRequestInfo,
					stateInfo);
		
		sipServletRequestReadOnly = null;
		
		// 15.4.1 Procedure : point 2
		if(checkRouteModifier(applicationRouterInfo, sipServletRequest)) {
			return;
		}
		// 15.4.1 Procedure : point 3
		if(applicationRouterInfo.getNextApplicationName() == null) {
			dispatchOutsideContainer(sipServletRequest);
		} else {
			dispatchInsideContainer(sipProvider, applicationRouterInfo, sipServletRequest, sipFactoryImpl);
		}		
	}

	/**
	 * Dispatch a request inside the container based on the information returned by the AR
	 * @param applicationRouterInfo information returned by the AR
	 * @param sipServletRequest the request to dispatch
	 * @param sipFactoryImpl the sip factory
	 * @throws DispatcherException a problem occured while dispatching the request
	 */
	private void dispatchInsideContainer(SipProvider sipProvider, SipApplicationRouterInfo applicationRouterInfo, SipServletRequestImpl sipServletRequest, SipFactoryImpl sipFactoryImpl) throws DispatcherException {
		if(logger.isInfoEnabled()) {
			logger.info("Dispatching the request event to " + applicationRouterInfo.getNextApplicationName());
		}
		final Request request = (Request) sipServletRequest.getMessage();
		sipServletRequest.setCurrentApplicationName(applicationRouterInfo.getNextApplicationName());
		SipContext sipContext = sipApplicationDispatcher.findSipApplication(applicationRouterInfo.getNextApplicationName());
		// follow the procedures of Chapter 16 to select a servlet from the application.									
		//no matching deployed apps
		if(sipContext == null) {
			// the app returned by the Application Router returned an app
			// that is not currently deployed, sends a 500 error response as was discussed 
			// in the JSR 289 EG, (for reference thread "Questions regarding AR" initiated by Uri Segev)
			// and stops processing.
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "No matching deployed application has been found !");
		}
		boolean isDistributable = sipContext.getDistributable();
		if(isDistributable) {
			ConvergedSessionReplicationContext.enterSipapp(sipServletRequest, null, true);
		}
		try {
			SipManager sipManager = (SipManager)sipContext.getManager();
			//sip appliation session association
			SipApplicationSessionKey sipApplicationSessionKey = makeAppSessionKey(
					sipContext, sipServletRequest, applicationRouterInfo.getNextApplicationName());
			MobicentsSipApplicationSession appSession = sipManager.getSipApplicationSession(
					sipApplicationSessionKey, true);
			//sip session association
			SipSessionKey sessionKey = SessionManagerUtil.getSipSessionKey(applicationRouterInfo.getNextApplicationName(), request, false);
			MobicentsSipSession sipSessionImpl = sipManager.getSipSession(sessionKey, true, sipFactoryImpl, appSession);
			sipSessionImpl.setSessionCreatingTransaction(sipServletRequest.getTransaction());
			sipServletRequest.setSipSession(sipSessionImpl);						
			
			// set the request's stateInfo to result.getStateInfo(), region to result.getRegion(), and URI to result.getSubscriberURI().			
			sipSessionImpl.setStateInfo(applicationRouterInfo.getStateInfo());
			sipSessionImpl.setRoutingRegion(applicationRouterInfo.getRoutingRegion());
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
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to parse the subscriber URI returned by the Application Router " 
						+ applicationRouterInfo.getSubscriberURI() +
						", please put one of DAR:<HeaderName> with Header containing a valid URI or an exlicit valid URI ", pe);
			} 									
			String sipSessionHandlerName = sipSessionImpl.getHandler();						
			if(sipSessionHandlerName == null || sipSessionHandlerName.length() < 1) {
				String mainServlet = sipContext.getMainServlet();
				if(mainServlet != null && mainServlet.length() > 0) {
					sipSessionHandlerName = mainServlet;				
				} else {
					SipServletMapping sipServletMapping = sipContext.findSipServletMappings(sipServletRequest);
					if(sipServletMapping == null) {
						logger.error("Sending 404 because no matching servlet found for this request ");
						sendErrorResponse(Response.NOT_FOUND, (ServerTransaction) sipServletRequest.getTransaction(), request, sipProvider);
						return;
					} else {
						sipSessionHandlerName = sipServletMapping.getServletName();
					}
				}
				try {
					sipSessionImpl.setHandler(sipSessionHandlerName);
				} catch (ServletException e) {
					// this should never happen
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while routing an initial request",e);
				} 
			}
			try {
				callServlet(sipServletRequest);
				if(logger.isInfoEnabled()) {
					logger.info("Request event dispatched to " + sipContext.getApplicationName());
				}
			} catch (ServletException e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected servlet exception occured while routing the following initial request " + request, e);
			} catch (IOException e) {				
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "An unexpected IO exception occured while routing the following initial request " + request, e);
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
						logger.info("Snapshot Sole Sip Session" + ctx.getSoleSipSession());
						logger.info("Snapshot Sole Sip App Session " + ctx.getSoleSipApplicationSession());
					}
					if (ctx.getSoleSnapshotManager() != null) {
						((SnapshotSipManager)ctx.getSoleSnapshotManager()).snapshot(
								ctx.getSoleSipSession());
						((SnapshotSipManager)ctx.getSoleSnapshotManager()).snapshot(
								ctx.getSoleSipApplicationSession());
					} 
				} finally {
					ConvergedSessionReplicationContext.finishSipCacheActivity();
				}
			}
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
				((SipStandardManager)sipContext.getSipManager()).dumpSipSessions();
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Routing State : " + sipServletRequest.getRoutingState() +
						"The Container hence continue routing the initial request.");
			}
			try {
				// the app that was called didn't do anything with the request
				// in any case we should route back to container statefully 
//				sipServletRequest.addAppCompositionRRHeader();
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
						if(!isAnotherDomain) return ;
					}
				}
				try {
					forwardRequestStatefully(sipServletRequest, SipSessionRoutingType.CURRENT_SESSION, SipRouteModifier.NO_ROUTE);
				} catch (Exception e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following subsequent request " + request, e);
				}
			} 
//			catch (SipException e) {
//				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "an exception has occured when trying to forward statefully", e);
//			} 
			catch (ParseException e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, e);					
			}	
		}
	}

	/**
	 * Dispatch a request outside the container
	 * @param sipServletRequest request
	 * @throws DispatcherException problem occured when dispatching the request outside the container
	 */
	private void dispatchOutsideContainer(SipServletRequestImpl sipServletRequest) throws DispatcherException {
		final Request request = (Request) sipServletRequest.getMessage();
		if(logger.isInfoEnabled()) {
			logger.info("Dispatching the request event outside the container");
		}
		//check if the request point to another domain
		if(request.getRequestURI() instanceof TelURL) {
			throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "cannot dispatch a request with a tel url request uri outside the container ");
		}
		javax.sip.address.SipURI sipRequestUri = (javax.sip.address.SipURI)request.getRequestURI();
		
		String host = sipRequestUri.getHost();
		int port = sipRequestUri.getPort();
		String transport = JainSipUtils.findTransport(request);
		boolean isAnotherDomain = sipApplicationDispatcher.isExternal(host, port, transport);			
		ListIterator<String> routeHeaders = sipServletRequest.getHeaders(RouteHeader.NAME);				
		if(isAnotherDomain || routeHeaders.hasNext()) {
			try {
				forwardRequestStatefully(sipServletRequest, SipSessionRoutingType.PREVIOUS_SESSION, SipRouteModifier.NO_ROUTE);
			} catch (Exception e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following subsequent request " + request, e);
			}
		} else {
			// the Request-URI does not point to another domain, and there is no Route header, 
			// the container should not send the request as it will cause a loop. 
			// Instead, the container must reject the request with 404 Not Found final response with no Retry-After header.
			throw new DispatcherException(Response.NOT_FOUND, "the Request-URI does not point to another domain, and there is no Route header," + 
					"the container should not send the request as it will cause a loop. " +
					"Instead, the container must reject the request with 404 Not Found final response with no Retry-After header.");				
		}
	}

	/**
	 * JSR 289 Section 15.4.1 Procedure : point 2
	 * Check the result.getRouteModifier() :
	 * 	If result.getRouteModifier() is ROUTE, then get the routes using result.getRoutes()
	 * 		If the first returned route is external (does not belong to this container) then 
	 * 		push all of the routes on the Route header stack of the request and send the request externally. 
	 * 		Note that the first returned route becomes the top route header of the request.
	 * 	
	 * 		If the first returned route is internal then the container MUST make it available 
	 * 		to the applications via the SipServletRequest.getPoppedRoute() method and ignore the remaining ones, if any. 
	 * 		This allows the AR modify the popped route before passing it to the application.
	 * 
	 * If result.getRouteModifier() is ROUTE_BACK then push a route back to the container followed 
	 * by the external routes obtained from result.getRoutes() and send the request externally. 
	 * Note that the container route SHOULD include AR state encoded as a route parameter in order for the AR to continue processing the application chain once the request returns back to the container.
	 * 
	 * If result.getRouteModifier() is NO_ROUTE then disregard the result.getRoutes() and proceed.
	 * 
	 * @param applicationRouterInfo the applicationRouterInfo returned by the AR
	 * @param sipServletRequest the request
	 * @return true if the request has been routed outside, false otherwise
	 * @throws DispatcherException a proble occured while checking the information returned by the AR
	 */
	private boolean checkRouteModifier(SipApplicationRouterInfo applicationRouterInfo, SipServletRequestImpl sipServletRequest) throws DispatcherException {
		Request request = (Request) sipServletRequest.getMessage();
		SipRouteModifier sipRouteModifier = applicationRouterInfo.getRouteModifier();
		if(logger.isDebugEnabled()) {
			logger.debug("the AR returned the following sip route modifier" + sipRouteModifier);
		}
		if(sipRouteModifier != null) {
			String[] routes = applicationRouterInfo.getRoutes();
			switch(sipRouteModifier) {
				// ROUTE modifier indicates that SipApplicationRouterInfo.getRoute() returns a valid route,
				// it is up to container to decide whether it is external or internal.
				case ROUTE :					
					Address routeAddress = null; 
					RouteHeader applicationRouterInfoRouteHeader = null;
					try {
						routeAddress = SipFactories.addressFactory.createAddress(routes[0]);
						applicationRouterInfoRouteHeader = SipFactories.headerFactory.createRouteHeader(routeAddress);										
						if(sipApplicationDispatcher.isRouteExternal(applicationRouterInfoRouteHeader)) {						
							// push all of the routes on the Route header stack of the request and 
							// send the request externally
							for (int i = routes.length-1 ; i >= 0; i--) {
								routeAddress = SipFactories.addressFactory.createAddress(routes[i]);
								Header routeHeader = SipFactories.headerFactory.createRouteHeader(routeAddress);
								// Fix for Issue 280 provided by eelcoc
								try {
									request.addFirst(routeHeader);
								} catch (SipException e) {
									throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to add a route header to route externally the following initial request " + request, e);
								}
							}
							if(logger.isDebugEnabled()) {
								logger.debug("Routing the request externally " + sipServletRequest );
							}
							request.setRequestURI(SipFactories.addressFactory.createURI(routes[0]));
							try {
								forwardRequestStatefully(sipServletRequest, null, sipRouteModifier);
								return true;
							} catch (Exception e) {
								throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following initial request " + request, e);
							}							
						} else {
							// the container MUST make the route available to the applications 
							// via the SipServletRequest.getPoppedRoute() method.							
							sipServletRequest.setPoppedRoute(applicationRouterInfoRouteHeader);
							break;
						}
					} catch (ParseException e) {
						// the AR returned an empty string route or a bad route
						// this shouldn't happen if the route modifier is ROUTE, processing is stopped
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to parse the route returned by the application router " +
								"into a compliant address" ,e);																						
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
					try {
						// Push container Route, pick up the first outbound interface
						SipURI sipURI = sipApplicationDispatcher.getOutboundInterfaces().get(0);
						sipURI.setParameter("modifier", "route_back");
						Header routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, sipURI.toString());
						// Fix for Issue 280 provided by eelcoc
						try {
							request.addFirst(routeHeader);
							// push all of the routes on the Route header stack of the request and 
							// send the request externally
							for (int i = routes.length-1 ; i >= 0; i--) {
								routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, routes[i]);
								request.addFirst(routeHeader);
							}
						} catch (SipException e) {
							throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to add a route header to route externally the following initial request " + request, e);
						}
						if(logger.isDebugEnabled()) {
							logger.debug("Routing the request externally " + sipServletRequest );
						}					
						try {
							forwardRequestStatefully(sipServletRequest, null, sipRouteModifier);
							return true;
						} catch (Exception e) {
							throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Unexpected Exception while trying to forward statefully the following subsequent request " + request, e);
						}										
					} catch (ParseException e) {
						// the AR returned an empty string route or a bad route
						// this shouldn't happen if the route modifier is ROUTE, processing is stopped
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Impossible to parse the route returned by the application router " +
								"into a compliant address" ,e);							
					}	
			}			
		}
		return false;
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
