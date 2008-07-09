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
package org.mobicents.servlet.sip.core;

import gov.nist.javax.sip.stack.SIPServerTransaction;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.SipTargetedRequestInfo;
import javax.servlet.sip.ar.SipTargetedRequestType;
import javax.servlet.sip.ar.spi.SipApplicationRouterProvider;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;
import org.mobicents.servlet.sip.address.SipURIImpl;
import org.mobicents.servlet.sip.address.TelURLImpl;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletRequestReadOnly;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.router.ManageableApplicationRouter;
import org.mobicents.servlet.sip.security.SipSecurityUtils;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipServletMapping;

import sun.misc.Service;

/**
 * Implementation of the SipApplicationDispatcher interface.
 * Central point getting the sip messages from the different stacks for a Tomcat Service(Engine), 
 * translating jain sip SIP messages to sip servlets SIP messages, calling the application router 
 * to know which app is interested in the messages and
 * dispatching them to those sip applications interested in the messages.
 * @author Jean Deruelle 
 */
public class SipApplicationDispatcherImpl implements SipApplicationDispatcher, MBeanRegistration {	
	//the logger
	private static transient Log logger = LogFactory
			.getLog(SipApplicationDispatcherImpl.class);
	
	private enum SipSessionRoutingType {
		PREVIOUS_SESSION, CURRENT_SESSION;
	}
	
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
	
	public static final Set<String> nonInitialSipRequestMethods = new HashSet<String>();
	
	static {
		nonInitialSipRequestMethods.add("CANCEL");
		nonInitialSipRequestMethods.add("BYE");
		nonInitialSipRequestMethods.add("PRACK");
		nonInitialSipRequestMethods.add("ACK");
		nonInitialSipRequestMethods.add("UPDATE");
		nonInitialSipRequestMethods.add("INFO");
	};
	
//	private enum InitialRequestRouting {
//		CONTINUE, STOP;
//	}
	
	//the sip factory implementation, it is not clear if the sip factory should be the same instance
	//for all applications
	private SipFactoryImpl sipFactoryImpl = null;
	//the sip application router responsible for the routing logic of sip messages to
	//sip servlet applications
	private SipApplicationRouter sipApplicationRouter = null;
	//map of applications deployed
	private Map<String, SipContext> applicationDeployed = null;
	//List of host names managed by the container
	private List<String> hostNames = null;
	
	//application chains
//	private Map<String, SipContext> applicationChains = null;
	
	private SessionManagerUtil sessionManager = null;
	
	private Boolean started = Boolean.FALSE;

	private SipNetworkInterfaceManager sipNetworkInterfaceManager;
	
	/**
	 * 
	 */
	public SipApplicationDispatcherImpl() {
		applicationDeployed = new ConcurrentHashMap<String, SipContext>();
		sipFactoryImpl = new SipFactoryImpl(this);
		sessionManager = new SessionManagerUtil();
		hostNames = Collections.synchronizedList(new ArrayList<String>());
		sipNetworkInterfaceManager = new SipNetworkInterfaceManager();
	}
	
	/**
	 * {@inheritDoc} 
	 */
	public void init() throws LifecycleException {
		//load the sip application router from the javax.servlet.sip.ar.spi.SipApplicationRouterProvider system property
		//and initializes it if present
		String sipApplicationRouterProviderClassName = System.getProperty("javax.servlet.sip.ar.spi.SipApplicationRouterProvider");
		if(sipApplicationRouterProviderClassName != null && sipApplicationRouterProviderClassName.length() > 0) {
			if(logger.isInfoEnabled()) {
				logger.info("Using the javax.servlet.sip.ar.spi.SipApplicationRouterProvider system property to load the application router provider");
			}
			try {
				sipApplicationRouter = (SipApplicationRouter)
					Class.forName(sipApplicationRouterProviderClassName).newInstance();
			} catch (InstantiationException e) {
				throw new LifecycleException("Impossible to load the Sip Application Router",e);
			} catch (IllegalAccessException e) {
				throw new LifecycleException("Impossible to load the Sip Application Router",e);
			} catch (ClassNotFoundException e) {
				throw new LifecycleException("Impossible to load the Sip Application Router",e);
			} catch (ClassCastException e) {
				throw new LifecycleException("Sip Application Router defined does not implement " + SipApplicationRouter.class.getName(),e);
			}		
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("Using the Service Provider Framework to load the application router provider");
			}
			Iterator<SipApplicationRouterProvider> providers = Service.providers(SipApplicationRouterProvider.class);
			if(providers.hasNext()) {
				sipApplicationRouter = providers.next().getSipApplicationRouter();
			}
		}
		if(sipApplicationRouter == null) {
			throw new LifecycleException("No Sip Application Router Provider could be loaded. " +
					"No jar compliant with JSR 289 Section Section 15.4.2 could be found on the classpath " +
					"and no javax.servlet.sip.ar.spi.SipApplicationRouterProvider system property set");
		}
		if(logger.isInfoEnabled()) {
			logger.info("Using the following Application Router : " + sipApplicationRouter.getClass().getName());
		}
		sipApplicationRouter.init(new ArrayList<String>(applicationDeployed.keySet()));
		
		if( oname == null ) {
			try {				
                oname=new ObjectName(domain + ":type=SipApplicationDispatcher");                
                Registry.getRegistry(null, null)
                    .registerComponent(this, oname, null);
                if(logger.isInfoEnabled()) {
                	logger.info("Sip Application dispatcher registered under following name " + oname);
                }
            } catch (Exception e) {
                logger.error("Impossible to register the Sip Application dispatcher in domain" + domain, e);
            }
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public void start() {
		synchronized (started) {
			if(started) {
				return;
			}
			started = Boolean.TRUE;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("Sip Application Dispatcher started");
		}
		// outbound interfaces set here and not in sipstandardcontext because
		// depending on jboss or tomcat context can be started before or after
		// connectors
		resetOutboundInterfaces();
		//JSR 289 Section 2.1.1 Step 4.If present invoke SipServletListener.servletInitialized() on each of initialized Servlet's listeners.
		for (SipContext sipContext : applicationDeployed.values()) {
			notifySipServletsListeners(sipContext);
		}		
	}
	
	/**
	 * Notifies the sip servlet listeners that the servlet has been initialized
	 * and that it is ready for service
	 * @param sipContext the sip context of the application where the listeners reside.
	 * @return true if all listeners have been notified correctly
	 */
	private static boolean notifySipServletsListeners(SipContext sipContext) {
		boolean ok = true;
		SipListenersHolder sipListenersHolder = sipContext.getListeners();
		List<SipServletListener> sipServletListeners = sipListenersHolder.getSipServletsListeners();
		if(logger.isDebugEnabled()) {
			logger.debug(sipServletListeners.size() + " SipServletListener to notify of servlet initialization");
		}
		Container[] children = sipContext.findChildren();
		if(logger.isDebugEnabled()) {
			logger.debug(children.length + " container to notify of servlet initialization");
		}
		for (Container container : children) {
			if(logger.isDebugEnabled()) {
				logger.debug("container " + container.getName() + ", class : " + container.getClass().getName());
			}
			if(container instanceof Wrapper) {			
				Wrapper wrapper = (Wrapper) container;
				Servlet sipServlet = null;
				try {
					sipServlet = wrapper.allocate();
					if(sipServlet instanceof SipServlet) {
						SipServletContextEvent sipServletContextEvent = 
							new SipServletContextEvent(sipContext.getServletContext(), (SipServlet)sipServlet);
						for (SipServletListener sipServletListener : sipServletListeners) {					
							sipServletListener.servletInitialized(sipServletContextEvent);					
						}
					}					
				} catch (ServletException e) {
					logger.error("Cannot allocate the servlet "+ wrapper.getServletClass() +" for notifying the listener " +
							"that it has been initialized", e);
					ok = false; 
				} catch (Throwable e) {
					logger.error("An error occured when initializing the servlet " + wrapper.getServletClass(), e);
					ok = false; 
				} 
				try {
					if(sipServlet != null) {
						wrapper.deallocate(sipServlet);
					}
				} catch (ServletException e) {
		            logger.error("Deallocate exception for servlet" + wrapper.getName(), e);
		            ok = false;
				} catch (Throwable e) {
					logger.error("Deallocate exception for servlet" + wrapper.getName(), e);
		            ok = false;
				}
			}
		}			
		return ok;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		synchronized (started) {
			if(!started) {
				return;
			}
			started = Boolean.FALSE;
		}
		sipApplicationRouter.destroy();		
		if(oname != null) {
			Registry.getRegistry(null, null).unregisterComponent(oname);
		}		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addSipApplication(String sipApplicationName, SipContext sipApplication) {
		if(logger.isDebugEnabled()) {
			logger.debug("Adding the following sip servlet application " + sipApplicationName + ", SipContext=" + sipApplication);
		}
		applicationDeployed.put(sipApplicationName, sipApplication);
		List<String> newlyApplicationsDeployed = new ArrayList<String>();
		newlyApplicationsDeployed.add(sipApplicationName);
		sipApplicationRouter.applicationDeployed(newlyApplicationsDeployed);
		//if the ApplicationDispatcher is started, notification is sent that the servlets are ready for service
		//otherwise the notification will be delayed until the ApplicationDispatcher has started
		synchronized (started) {
			if(started) {
				notifySipServletsListeners(sipApplication);
			}
		}
		if(logger.isInfoEnabled()) {
			logger.info("the following sip servlet application has been added : " + sipApplicationName);
		}
	}
	/**
	 * {@inheritDoc}
	 */
	public SipContext removeSipApplication(String sipApplicationName) {
		SipContext sipContext = applicationDeployed.remove(sipApplicationName);
		List<String> applicationsUndeployed = new ArrayList<String>();
		applicationsUndeployed.add(sipApplicationName);
		sipApplicationRouter.applicationUndeployed(applicationsUndeployed);
		((SipManager)sipContext.getManager()).removeAllSessions();
		if(logger.isInfoEnabled()) {
			logger.info("the following sip servlet application has been removed : " + sipApplicationName);
		}
		return sipContext;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
	 */
	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		// TODO FIXME
		if(logger.isInfoEnabled()) {
			logger.info("Dialog Terminated => " + dialogTerminatedEvent.getDialog().getCallId().getCallId());
		}
		Dialog dialog = dialogTerminatedEvent.getDialog();		
		TransactionApplicationData tad = (TransactionApplicationData) dialog.getApplicationData();
		SipServletMessageImpl sipServletMessageImpl = tad.getSipServletMessage();
		SipSessionImpl sipSessionImpl = sipServletMessageImpl.getSipSession();
		/*
 			TODO: FIXME: Review this. We can't remove sesions that still have transactions.
			Although the jsip stacktransaction might have ended, the message that ended it
			is not yet delivered to the container and the servlet at this time.
        */
//		if(!sipSessionImpl.hasOngoingTransaction()) {
//			sessionManager.removeSipSession(sipSessionImpl.getKey());
//		}
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
	 */
	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestEvent) {		
		SipProvider sipProvider = (SipProvider)requestEvent.getSource();
		ServerTransaction transaction =  requestEvent.getServerTransaction();
		Request request = requestEvent.getRequest();
		try {
			if(logger.isInfoEnabled()) {
				logger.info("Got a request event "  + request.toString());
			}
			if (!Request.ACK.equals(request.getMethod()) && transaction == null ) {
				try {
					//folsson fix : Workaround broken Cisco 7940/7912
				    if(request.getHeader(MaxForwardsHeader.NAME)==null){
					    request.setHeader(SipFactories.headerFactory.createMaxForwardsHeader(70));
					}
					transaction = sipProvider.getNewServerTransaction(request);
				} catch ( TransactionUnavailableException tae) {
					// Sends a 500 Internal server error and stops processing.				
					JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);				
	                return;
				} catch ( TransactionAlreadyExistsException taex ) {
					// Already processed this request so just return.
					return;				
				} 
			} 	
			if(logger.isInfoEnabled()) {
				logger.info("ServerTx ref "  + transaction);
				logger.info("Dialog ref "  + requestEvent.getDialog());
			}
			
			SipServletRequestImpl sipServletRequest = new SipServletRequestImpl(
						request,
						sipFactoryImpl,
						null,
						transaction,
						requestEvent.getDialog(),
						JainSipUtils.dialogCreatingMethods.contains(request.getMethod()));						
			
			// Check if the request is meant for me. If so, strip the topmost
			// Route header.
			RouteHeader routeHeader = (RouteHeader) request
					.getHeader(RouteHeader.NAME);
			//Popping the router header if it's for the container as
			//specified in JSR 289 - Section 15.8
			if(! isRouteExternal(routeHeader)) {
				request.removeFirst(RouteHeader.NAME);
				sipServletRequest.setPoppedRoute(routeHeader);
			}							
			if(logger.isInfoEnabled()) {
				logger.info("Routing State " + sipServletRequest.getRoutingState());
			}
										
			if(sipServletRequest.isInitial()) {
				if(logger.isInfoEnabled()) {
					logger.info("Routing of Initial Request " + request);
				}
				routeInitialRequest(sipProvider, sipServletRequest);							
			} else {
				if(logger.isInfoEnabled()) {
					logger.info("Routing of Subsequent Request " + request);
				}
				if(sipServletRequest.getMethod().equals(Request.CANCEL)) {
					routeCancel(sipProvider, sipServletRequest);
				} else {
					routeSubsequentRequest(sipProvider, sipServletRequest);
				}
			}
		} catch (Throwable e) {
			logger.error("Unexpected exception while processing request",e);
			// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
			if(!Request.ACK.equalsIgnoreCase(request.getMethod())) {
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			}
			return;
		}
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
	private void forwardStatefully(SipServletRequestImpl sipServletRequest, SipSessionRoutingType sipSessionRoutingType, SipRouteModifier sipRouteModifier) throws ParseException,
			TransactionUnavailableException, SipException, InvalidArgumentException {
		
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
					viaHeader.setParameter(SipApplicationDispatcherImpl.RR_PARAM_APPLICATION_NAME,
							sipServletRequest.getSipSession().getKey().getApplicationName());
					viaHeader.setParameter(SipApplicationDispatcherImpl.RR_PARAM_HANDLER_NAME,
							sipServletRequest.getSipSession().getHandler());					
				} else {				
					// if the handler name is null it means that the app returned by the AR was not deployed
					// and couldn't be called, 
					// we specify it so that on response handling this app can be skipped
					viaHeader.setParameter(SipApplicationDispatcherImpl.APP_NOT_DEPLOYED,
							sipServletRequest.getSipSession().getKey().getApplicationName());					
				}			
				clonedRequest.addHeader(viaHeader);
			} else {				
				if(SipRouteModifier.NO_ROUTE.equals(sipRouteModifier)) {
					// no app was returned by the AR 				
					viaHeader.setParameter(SipApplicationDispatcherImpl.NO_APP_RETURNED,
						"noappreturned");
				} else {
					viaHeader.setParameter(SipApplicationDispatcherImpl.MODIFIER,
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
				viaHeader.setParameter(SipApplicationDispatcherImpl.NO_APP_RETURNED,
						"noappreturned");
			} else {
				viaHeader.setParameter(SipApplicationDispatcherImpl.MODIFIER,
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
	 * @param sipProvider
	 * @param transaction
	 * @param request
	 * @param sipServletRequest
	 * @throws InvalidArgumentException 
	 * @throws SipException 
	 * @throws ParseException 
	 * @throws TransactionUnavailableException 
	 */
	private boolean routeSubsequentRequest(SipProvider sipProvider, SipServletRequestImpl sipServletRequest) throws TransactionUnavailableException, ParseException, SipException, InvalidArgumentException {
		ServerTransaction transaction = (ServerTransaction) sipServletRequest.getTransaction();
		Request request = (Request) sipServletRequest.getMessage();
		Dialog dialog = sipServletRequest.getDialog();
		
		javax.servlet.sip.Address poppedAddress = sipServletRequest.getPoppedRoute();
				
		if(poppedAddress==null){
			if(Request.ACK.equals(request.getMethod())) {
				//Means that this is an ACK to a container generated error response, so we can drop it
				if(logger.isDebugEnabled()) {
					logger.debug("The popped route is null for an ACK, this is an ACK to a container generated error response, so it is dropped");
				}
				return false;
			} else {
				throw new IllegalArgumentException("The popped route shouldn't be null for not proxied requests.");
			}
		}
		//Extract information from the Record Route Header		
		String applicationName = poppedAddress.getParameter(RR_PARAM_APPLICATION_NAME);
		String handlerName = poppedAddress.getParameter(RR_PARAM_HANDLER_NAME);
		String finalResponse = poppedAddress.getParameter(FINAL_RESPONSE);
		String generatedApplicationKey = poppedAddress.getParameter(GENERATED_APP_KEY);
		if(generatedApplicationKey != null) {
			generatedApplicationKey = RFC2396UrlDecoder.decode(generatedApplicationKey);
		}
		if(applicationName == null || applicationName.length() < 1 || 
				handlerName == null || handlerName.length() < 1) {
			throw new IllegalArgumentException("cannot find the application to handle this subsequent request " +
					"in this popped routed header " + poppedAddress);
		}
		boolean inverted = false;
		if(dialog != null && !dialog.isServer()) {
			inverted = true;
		}
		
		SipContext sipContext = this.applicationDeployed.get(applicationName);
		if(sipContext == null) {
			throw new IllegalArgumentException("cannot find the application to handle this subsequent request " +
					"in this popped routed header " + poppedAddress);
		}
		SipManager sipManager = (SipManager)sipContext.getManager();
		SipApplicationSessionKey sipApplicationSessionKey = null;
		if(generatedApplicationKey != null && generatedApplicationKey.length() > 0) {
			sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
					applicationName, 
					generatedApplicationKey,
					true);
		} else {
			sipApplicationSessionKey = makeAppSessionKey(
				sipContext, sipServletRequest, applicationName);
		}
		SipApplicationSessionImpl sipApplicationSession = sipManager.getSipApplicationSession(sipApplicationSessionKey, false);
		if(sipApplicationSession == null) {
			logger.error("Cannot find the corresponding sip application session to this subsequent request " + request +
					" with the following popped route header " + sipServletRequest.getPoppedRoute());
			sipManager.dumpSipApplicationSessions();
			// Sends a 500 Internal server error and stops processing.				
			JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			return false;
		}
		
		SipSessionKey key = SessionManagerUtil.getSipSessionKey(applicationName, request, inverted);
		SipSessionImpl sipSession = sipManager.getSipSession(key, false, sipFactoryImpl, sipApplicationSession);
		
		// Added by Vladimir because the inversion detection on proxied requests doesn't work
		if(sipSession == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Cannot find the corresponding sip session with key " + key + " to this subsequent request " + request +
						" with the following popped route header " + sipServletRequest.getPoppedRoute() + ". Trying inverted.");
			}
			key = SessionManagerUtil.getSipSessionKey(applicationName, request, !inverted);
			sipSession = sipManager.getSipSession(key, false, sipFactoryImpl, sipApplicationSession);
		}
		
		if(sipSession == null) {			
			logger.error("Cannot find the corresponding sip session with key " + key + " to this subsequent request " + request +
					" with the following popped route header " + sipServletRequest.getPoppedRoute());
			sipManager.dumpSipSessions();
			// Sends a 500 Internal server error and stops processing.				
			JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			return false;
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("Inverted try worked. sip session found : " + sipSession.getId());
			}
		}
		sipServletRequest.setSipSession(sipSession);			
		// JSR 289 Section 6.2.1 :
		// any state transition caused by the reception of a SIP message, 
		// the state change must be accomplished by the container before calling 
		// the service() method of any SipServlet to handle the incoming message.
		sipSession.updateStateOnSubsequentRequest(sipServletRequest, true);
		
		try {
			// See if the subsequent request should go directly to the proxy
			if(sipServletRequest.getSipSession().getProxyBranch() != null) {
				ProxyBranchImpl proxyBranch = sipServletRequest.getSipSession().getProxyBranch();
				if(proxyBranch.getProxy().getSupervised()) {
					callServlet(sipServletRequest);
				}
				proxyBranch.proxySubsequentRequest(sipServletRequest);
			}
			// If it's not for a proxy then it's just an AR, so go to the next application
			else {
				callServlet(sipServletRequest);				
			}
		} catch (ServletException e) {				
			logger.error("An unexpected servlet exception occured while processing the following subsequent request " + request, e);
			// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
			if(!Request.ACK.equalsIgnoreCase(request.getMethod())) {
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			}
			return false;
		} catch (IOException e) {				
			logger.error("An unexpected IO exception occured while processing the following subsequent request " + request, e);
			// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
			if(!Request.ACK.equalsIgnoreCase(request.getMethod())) {
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			}
			return false;
		} 
//		finally {
//			if(sipSession.isReadyToInvalidate()) {
//				sipSession.onTerminatedState();
//			}
//		}
		//if a final response has been sent, or if the request has 
		//been proxied or relayed we stop routing the request
		RoutingState routingState = sipServletRequest.getRoutingState();
		if(RoutingState.FINAL_RESPONSE_SENT.equals(routingState) ||
				RoutingState.PROXIED.equals(routingState) ||
				RoutingState.RELAYED.equals(routingState) ||
				RoutingState.CANCELLED.equals(routingState)) {
			if(logger.isDebugEnabled()) {
				logger.debug("Routing State : " + sipServletRequest.getRoutingState() +
						"The Container hence stops routing the subsequent request.");
			}
			return false;
		} 
		else {					
			if(finalResponse != null && finalResponse.length() > 0) {
				if(logger.isInfoEnabled()) {
					logger.info("Subsequent Request reached the servlet application " +
						"that sent a final response, stop routing the subsequent request " + request.toString());
				}
				return false;
			}
			// Check if the request is meant for me. 
			RouteHeader routeHeader = (RouteHeader) request
					.getHeader(RouteHeader.NAME);
			if(routeHeader == null || isRouteExternal(routeHeader)) {
				// no route header or external, send outside the container
				// FIXME send it statefully
				if(dialog == null && transaction == null) {
					try{
						sipProvider.sendRequest((Request)request.clone());
						if(logger.isInfoEnabled()) {
							logger.info("Subsequent Request dispatched outside the container" + request.toString());
						}
					} catch (Exception ex) {			
						throw new IllegalStateException("Error sending request",ex);
					}	
				} else {
					forwardStatefully(sipServletRequest, SipSessionRoutingType.CURRENT_SESSION, SipRouteModifier.ROUTE);	
				}
				return false;
			} else {		
				//route header is meant for the container hence we continue
				forwardStatefully(sipServletRequest, SipSessionRoutingType.CURRENT_SESSION, SipRouteModifier.NO_ROUTE);
				return true;
			}
		}		
	}

	/**
	 * This method allows to route the CANCEL request along the application path 
	 * followed by the INVITE.
	 * We can distinguish cases here as per spec :
	 * Applications that acts as User Agent or B2BUA  
	 * 11.2.3 Receiving CANCEL 
	 * 
	 * When a CANCEL is received for a request which has been passed to an application, 
	 * and the application has not responded yet or proxied the original request, 
	 * the container responds to the original request with a 487 (Request Terminated) 
	 * and to the CANCEL with a 200 OK final response, and it notifies the application 
	 * by passing it a SipServletRequest object representing the CANCEL request. 
	 * The application should not attempt to respond to a request after receiving a CANCEL for it. 
	 * Neither should it respond to the CANCEL notification. 
	 * Clearly, there is a race condition between the container generating the 487 response 
	 * and the SIP servlet generating its own response. 
	 * This should be handled using standard Java mechanisms for resolving race conditions. 
	 * If the application wins, it will not be notified that a CANCEL request was received. 
	 * If the container wins and the servlet tries to send a response before 
	 * (or for that matter after) being notified of the CANCEL, 
	 * the container throws an IllegalStateException.
	 * 
	 * Applications that acts as proxy : 
	 * 10.2.6 Receiving CANCEL 
	 * if the original request has not been proxied yet the container responds 
	 * to it with a 487 final response otherwise, all branches are cancelled, 
	 * and response processing continues as usual
	 * In either case, the application is subsequently invoked with the CANCEL request. 
	 * This is a notification only, as the server has already responded to the CANCEL 
	 * and cancelled outstanding branches as appropriate. 
	 * The race condition between the server sending the 487 response and 
	 * the application proxying the request is handled as in the UAS case as 
	 * discussed in section 11.2.3 Receiving CANCEL.
	 * 
	 * @param sipProvider
	 * @param sipServletRequest
	 * @param transaction
	 * @param request
	 * @param poppedAddress
	 * @throws InvalidArgumentException 
	 * @throws SipException 
	 * @throws ParseException 
	 * @throws TransactionUnavailableException 
	 */
	private boolean routeCancel(SipProvider sipProvider, SipServletRequestImpl sipServletRequest) throws TransactionUnavailableException, ParseException, SipException, InvalidArgumentException {
		
		ServerTransaction transaction = (ServerTransaction) sipServletRequest.getTransaction();
		Request request = (Request) sipServletRequest.getMessage();		
		Dialog dialog = transaction.getDialog();
		/*
		 * WARNING: routing of CANCEL is special because CANCEL does not contain Route headers as other requests related
		 * to the dialog. But still it has to be routed through the app path
		 * of the INVITE
		 */
		/* If there is a proxy with the request, let's try to send it directly there.
		 * This is needed because of CANCEL which is a subsequent request that might
		 * not have Routes. For example if the callee has'n responded the caller still
		 * doesn't know the route-record and just sends cancel to the outbound proxy.
		 */	
//		boolean proxyCancel = false;
		try {
			// First we need to send OK ASAP because of retransmissions both for 
			//proxy or app
			ServerTransaction cancelTransaction = 
				(ServerTransaction) sipServletRequest.getTransaction();
			SipServletResponseImpl cancelResponse = (SipServletResponseImpl) 
			sipServletRequest.createResponse(200, "Canceling");
			Response cancelJsipResponse = (Response) cancelResponse.getMessage();
			cancelTransaction.sendResponse(cancelJsipResponse);
		} catch (SipException e) {
			logger.error("Impossible to send the ok to the CANCEL",e);
			JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			return false;
		} catch (InvalidArgumentException e) {
			logger.error("Impossible to send the ok to the CANCEL",e);
			JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			return false;
		}
		if(logger.isDebugEnabled()) {
			logger.debug("checking what to do with the CANCEL " + sipServletRequest);
		}				
//		TransactionApplicationData dialogAppData = (TransactionApplicationData) dialog.getApplicationData();
		Transaction inviteTransaction = ((SIPServerTransaction) sipServletRequest.getTransaction()).getCanceledInviteTransaction();
		TransactionApplicationData dialogAppData = (TransactionApplicationData)inviteTransaction.getApplicationData();
		SipServletRequestImpl inviteRequest = (SipServletRequestImpl)
			dialogAppData.getSipServletMessage();
		if(logger.isDebugEnabled()) {
			logger.debug("message associated with the dialogAppData " +
					"of the CANCEL " + inviteRequest);
		}
//		Transaction inviteTransaction = inviteRequest.getTransaction();
		if(logger.isDebugEnabled()) {
			logger.debug("invite transaction associated with the dialogAppData " +
					"of the CANCEL " + inviteTransaction);
//			logger.debug(dialog.isServer());
		}
		TransactionApplicationData inviteAppData = (TransactionApplicationData) 
			inviteTransaction.getApplicationData();
		if(logger.isDebugEnabled()) {
			logger.debug("app data of the invite transaction associated with the dialogAppData " +
					"of the CANCEL " + inviteAppData);
		}		
		if(inviteAppData.getProxy() != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("proxying the CANCEL " + sipServletRequest);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("CANCEL a proxied request state = " + inviteRequest.getRoutingState());
			}
			// Routing State : PROXY case
			SipServletResponseImpl inviteResponse = (SipServletResponseImpl) 
				inviteRequest.createResponse(Response.REQUEST_TERMINATED);			
			if(!RoutingState.PROXIED.equals(inviteRequest.getRoutingState())) {				
				// 10.2.6 if the original request has not been proxied yet the container 
				// responds to it with a 487 final response
				try {					
					inviteRequest.setRoutingState(RoutingState.CANCELLED);
				} catch (IllegalStateException e) {
					logger.info("request already proxied, dropping the cancel");
					return false;
				}
				try {
					Response requestTerminatedResponse = (Response) inviteResponse.getMessage();
					((ServerTransaction)inviteTransaction).sendResponse(requestTerminatedResponse);				
				} catch (SipException e) {
					logger.error("Impossible to send the 487 to the INVITE transaction corresponding to CANCEL",e);
					JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				} catch (InvalidArgumentException e) {
					logger.error("Impossible to send the ok 487 to the INVITE transaction corresponding to CANCEL",e);
					JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				}
			} else {
				// otherwise, all branches are cancelled, and response processing continues as usual
				inviteAppData.getProxy().cancel();
			}
			return true;
		} else if(RoutingState.RELAYED.equals(inviteRequest.getRoutingState())) {
			if(logger.isDebugEnabled()) {
				logger.debug("Relaying the CANCEL " + sipServletRequest);
			}
			// Routing State : RELAYED - B2BUA case
			SipServletResponseImpl inviteResponse = (SipServletResponseImpl) 
				inviteRequest.createResponse(Response.REQUEST_TERMINATED);
			try {
				inviteRequest.setRoutingState(RoutingState.CANCELLED);
			} catch (IllegalStateException e) {
				logger.info("Final response already sent, dropping the cancel");
				return false;
			}
			try {
				Response requestTerminatedResponse = (Response) inviteResponse.getMessage();
				((ServerTransaction)inviteTransaction).sendResponse(requestTerminatedResponse);
			} catch (SipException e) {
				logger.error("Impossible to send the 487 to the INVITE transaction corresponding to CANCEL",e);
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			} catch (InvalidArgumentException e) {
				logger.error("Impossible to send the ok 487 to the INVITE transaction corresponding to CANCEL",e);
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			}
			//Forwarding the cancel on the other B2BUA side
			if(inviteRequest.getLinkedRequest() != null) {					
				SipServletRequestImpl cancelRequest = (SipServletRequestImpl)
					inviteRequest.getLinkedRequest().createCancel();
				cancelRequest.send();
				return true;
			} else {
				SipSessionImpl sipSession = inviteRequest.getSipSession();
				sipServletRequest.setSipSession(sipSession);
				Wrapper servletWrapper = (Wrapper) applicationDeployed.get(sipSession.getKey().getApplicationName()).findChild(sipSession.getHandler());
				try{
					Servlet servlet = servletWrapper.allocate();
					servlet.service(sipServletRequest, null);
					return true;
				} catch (ServletException e) {				
					logger.error("An unexpected servlet exception occured while routing the CANCEL", e);
					// Sends a 500 Internal server error and stops processing.				
//						JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				} catch (IOException e) {				
					logger.error("An unexpected IO exception occured while routing the CANCEL", e);
					// Sends a 500 Internal server error and stops processing.				
//						JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				}
			}
		} else if(RoutingState.FINAL_RESPONSE_SENT.equals(inviteRequest.getRoutingState())) {
			if(logger.isDebugEnabled()) {
				logger.debug("the final response has already been sent, nothing to do here");
			}
			return true;
		} else if(RoutingState.INITIAL.equals(inviteRequest.getRoutingState()) ||
				RoutingState.SUBSEQUENT.equals(inviteRequest.getRoutingState())) {
			if(logger.isDebugEnabled()) {
				logger.debug("the app didn't do anything with the request forwarding the cancel on the other tx");				
			}
			javax.servlet.sip.Address poppedAddress = sipServletRequest.getPoppedRoute();
			
			if(poppedAddress==null){
				throw new IllegalArgumentException("The popped route shouldn't be null for not proxied requests.");
			}
			
			Request clonedRequest = (Request) request.clone();			
            // Branch Id will be assigned by the stack.
			String transport = JainSipUtils.findTransport(clonedRequest);		
			ViaHeader viaHeader = JainSipUtils.createViaHeader(
					sipNetworkInterfaceManager, transport, null);                                
        
            // Cancel is hop by hop so remove all other via headers.
            clonedRequest.removeHeader(ViaHeader.NAME);                
            clonedRequest.addHeader(viaHeader);           
            ClientTransaction clientTransaction = (ClientTransaction)inviteAppData.getTransaction();
            Request cancelRequest = clientTransaction.createCancel();
            sipProvider.getNewClientTransaction(cancelRequest).sendRequest();
			return true;
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("the initial request isn't in a good routing state ");				
			}
			throw new IllegalStateException("Initial request for CANCEL is in "+ sipServletRequest.getRoutingState() +" Routing state");
		}
	}
	
	private SipApplicationSessionKey makeAppSessionKey(SipContext sipContext, SipServletRequestImpl sipServletRequestImpl, String applicationName) {
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
	private boolean routeInitialRequest(SipProvider sipProvider, SipServletRequestImpl sipServletRequest) throws ParseException, TransactionUnavailableException, SipException, InvalidArgumentException {		
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
				SipContext sipContext = applicationDeployed.get(previousAppName);				
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
						JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
						return false;
					}						
					if(isRouteExternal(applicationRouterInfoRouteHeader)) {						
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
					SipURI sipURI = getOutboundInterfaces().get(0);
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
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			}
			javax.sip.address.SipURI sipRequestUri = (javax.sip.address.SipURI)request.getRequestURI();
			
			String host = sipRequestUri.getHost();
			int port = sipRequestUri.getPort();
			String transport = JainSipUtils.findTransport(request);
			boolean isAnotherDomain = isExternal(host, port, transport);			
			ListIterator<String> routeHeaders = sipServletRequest.getHeaders(RouteHeader.NAME);				
			if(isAnotherDomain || routeHeaders.hasNext()) {
				forwardStatefully(sipServletRequest, SipSessionRoutingType.PREVIOUS_SESSION, SipRouteModifier.NO_ROUTE);
				return false;
			} else {
				// the Request-URI does not point to another domain, and there is no Route header, 
				// the container should not send the request as it will cause a loop. 
				// Instead, the container must reject the request with 404 Not Found final response with no Retry-After header.					 			
				JainSipUtils.sendErrorResponse(Response.NOT_FOUND, transaction, request, sipProvider);
				return false;
			}
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("Dispatching the request event to " + applicationRouterInfo.getNextApplicationName());
			}
			sipServletRequest.setCurrentApplicationName(applicationRouterInfo.getNextApplicationName());
			SipContext sipContext = applicationDeployed.get(applicationRouterInfo.getNextApplicationName());
			// follow the procedures of Chapter 16 to select a servlet from the application.									
			//no matching deployed apps
			if(sipContext == null) {
				logger.error("No matching deployed application has been found !");
				// the app returned by the Application Router returned an app
				// that is not currently deployed, sends a 500 error response as was discussed 
				// in the JSR 289 EG, (for reference thread "Questions regarding AR" initiated by Uri Segev)
				// and stops processing.				
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
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
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
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
						JainSipUtils.sendErrorResponse(Response.NOT_FOUND, transaction, request, sipProvider);
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
					JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
					return false;
				} 
			}
			try {
				callServlet(sipServletRequest);
				if(logger.isInfoEnabled()) {
					logger.info("Request event dispatched to " + sipContext.getApplicationName());
				}
			} catch (ServletException e) {				
				logger.error("An unexpected servlet exception occured while routing an initial request", e);
				// Sends a 500 Internal server error and stops processing.				
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
				return false;
			} catch (IOException e) {				
				logger.error("An unexpected IO exception occured while routing an initial request", e);
				// Sends a 500 Internal server error and stops processing.				
				JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
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
					SipApplicationRouterInfo routerInfo = getNextInterestedApplication(sipServletRequest);
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
							boolean isAnotherDomain = isExternal(host, port, transport);
							if(!isAnotherDomain) return false;
						}
					}
					forwardStatefully(sipServletRequest, SipSessionRoutingType.CURRENT_SESSION, SipRouteModifier.NO_ROUTE);
					return true;
				} catch (SipException e) {				
					logger.error("an exception has occured when trying to forward statefully", e);
					// Sends a 500 Internal server error and stops processing.				
					JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
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
	private SipTargetedRequestInfo retrieveTargetedApplication(
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
			SipContext sipContext = applicationDeployed.get(targetedApplicationSessionKey.getApplicationName());
			if(sipContext != null && ((SipManager)sipContext.getManager()).getSipApplicationSession(targetedApplicationSessionKey, false) != null) {
				return new SipTargetedRequestInfo(SipTargetedRequestType.ENCODED_URI, targetedApplicationSessionKey.getApplicationName());
			}				
		}
		return null;
	}

	/**
	 * Check if the route is external
	 * @param routeHeader the route to check 
	 * @return true if the route is external, false otherwise
	 */
	public boolean isRouteExternal(RouteHeader routeHeader) {
		if (routeHeader != null) {
			javax.sip.address.SipURI routeUri = (javax.sip.address.SipURI) routeHeader.getAddress().getURI();

			String routeTransport = routeUri.getTransportParam();
			if(routeTransport == null) {
				routeTransport = ListeningPoint.UDP;
			}					
			return isExternal(routeUri.getHost(), routeUri.getPort(), routeTransport);						
		}		
		return true;
	}
	
	/**
	 * Check if the via header is external
	 * @param viaHeader the via header to check 
	 * @return true if the via header is external, false otherwise
	 */
	public boolean isViaHeaderExternal(ViaHeader viaHeader) {
		if (viaHeader != null) {			
			return isExternal(viaHeader.getHost(), viaHeader.getPort(), viaHeader.getTransport());
		}
		return true;
	}
	
	/**
	 * Check whether or not the triplet host, port and transport are corresponding to an interface
	 * @param host can be hostname or ipaddress
	 * @param port port number
	 * @param transport transport used
	 * @return true if the triplet host, port and transport are corresponding to an interface
	 * false otherwise
	 */
	private boolean isExternal(String host, int port, String transport) {
		boolean isExternal = true;
		ExtendedListeningPoint listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(host, port, transport);		
		if((hostNames.contains(host) || listeningPoint != null)) {
			if(logger.isDebugEnabled()) {
				logger.debug("hostNames.contains(host)=" + 
						hostNames.contains(host) +
						" | listeningPoint found = " +
						listeningPoint);
			}
			isExternal = false;
		}		
		if(logger.isDebugEnabled()) {
			logger.debug("the triplet host/port/transport : " + 
					host + "/" +
					port + "/" +
					transport + " is external : " + isExternal);
		}
		return isExternal;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public void processResponse(ResponseEvent responseEvent) {
		if(logger.isInfoEnabled()) {
			logger.info("Response " + responseEvent.getResponse().toString());
		}
		Response response = responseEvent.getResponse();
		CSeqHeader cSeqHeader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
		//if this is a response to a cancel, the response is dropped
		if(Request.CANCEL.equalsIgnoreCase(cSeqHeader.getMethod())) {
			if(logger.isDebugEnabled()) {
				logger.debug("the response is dropped accordingly to JSR 289 " +
						"since this a response to a CANCEL");
			}
			return;
		}
		//if this is a trying response, the response is dropped
		if(Response.TRYING == response.getStatusCode()) {
			if(logger.isDebugEnabled()) {
				logger.debug("the response is dropped accordingly to JSR 289 " +
						"since this a 100");
			}
			return;
		}
		if(responseEvent.getClientTransaction() == null && responseEvent.getDialog() == null) {
			if(logger.isDebugEnabled()) {
				logger.debug("the following response is dropped since there is no client transaction nor dialog for it : " + response);	
			}
			return;
		}
		boolean continueRouting = routeResponse(responseEvent);		
		// if continue routing is to true it means that
		// a B2BUA got it so we don't have anything to do here 
		// or an app that didn't do anything with it
		// or a when handling the request an app had to be called but wasn't deployed
		// we have to strip the topmost via header and forward it statefully		
		if (continueRouting) {			
			Response newResponse = (Response) response.clone();
			newResponse.removeFirst(ViaHeader.NAME);
			ListIterator<ViaHeader> viaHeadersLeft = newResponse.getHeaders(ViaHeader.NAME);
			if(viaHeadersLeft.hasNext()) {
				//forward it statefully
				//TODO should decrease the max forward header to avoid infinite loop
				if(logger.isDebugEnabled()) {
					logger.debug("forwarding the response statefully " + newResponse);
				}
				TransactionApplicationData applicationData = null; 
				if(responseEvent.getClientTransaction() != null) {
					applicationData = (TransactionApplicationData)responseEvent.getClientTransaction().getApplicationData();
					if(logger.isDebugEnabled()) {
						logger.debug("ctx application Data " + applicationData);
					}
				}
				//there is no client transaction associated with it, it means that this is a retransmission
				else if(responseEvent.getDialog() != null){	
					applicationData = (TransactionApplicationData)responseEvent.getDialog().getApplicationData();
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

	/**
	 * Method for routing responses to apps. This uses via header to know which 
	 * app has to be called 
	 * @param responseEvent the jain sip response received
	 * @return true if we need to continue routing
	 */
	private boolean routeResponse(ResponseEvent responseEvent) {
		Response response = responseEvent.getResponse();
		ListIterator<ViaHeader> viaHeaders = response.getHeaders(ViaHeader.NAME);				
		ViaHeader viaHeader = viaHeaders.next();
		if(logger.isInfoEnabled()) {
			logger.info("viaHeader = " + viaHeader.toString());
		}
		//response meant for the container
		if(!isViaHeaderExternal(viaHeader)) {
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
			String handlerName = viaHeader.getParameter(RR_PARAM_HANDLER_NAME);
			boolean inverted = false;
			if(dialog != null && dialog.isServer()) {
				inverted = true;
			}
			SipContext sipContext = applicationDeployed.get(appName);
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
						callServlet(sipServletResponse);
					}
					return false;
				}
				else {
					// Update Session state
					session.updateStateOnResponse(sipServletResponse, true);

					callServlet(sipServletResponse);
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
	
	public static void callServlet(SipServletRequestImpl request) throws ServletException, IOException {
		SipSessionImpl session = request.getSipSession();
		if(logger.isInfoEnabled()) {
			logger.info("Dispatching request " + request.toString() + 
				" to following App/servlet => " + session.getKey().getApplicationName()+ 
				"/" + session.getHandler());
		}
		String sessionHandler = session.getHandler();
		SipApplicationSessionImpl sipApplicationSessionImpl = session.getSipApplicationSession();
		SipContext sipContext = sipApplicationSessionImpl.getSipContext();
		Wrapper sipServletImpl = (Wrapper) sipContext.findChild(sessionHandler);
		Servlet servlet = sipServletImpl.allocate();		
		
		// JBoss-specific CL issue:
		// This is needed because usually the classloader here is some UnifiedClassLoader3,
		// which has no idea where the servlet ENC is. We will use the classloader of the
		// servlet class, which is the WebAppClassLoader, and has ENC fully loaded with
		// with java:comp/env/security (manager, context etc)
		ClassLoader cl = servlet.getClass().getClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		
		if(!securityCheck(request)) return;
	    
		try {
			servlet.service(request, null);
		} finally {		
			sipServletImpl.deallocate(servlet);
		}
		
		// We invalidate here just after the servlet is called because before that the session would be unavailable
		// to the user code. TODO: FIXME: This event should occur after all transations are complete.
		// if(session.isReadyToInvalidate() && session.getInvalidateWhenReady()) session.invalidate();
	}
	
	public static void callServlet(SipServletResponseImpl response) throws ServletException, IOException {		
		SipSessionImpl session = response.getSipSession();
		if(logger.isInfoEnabled()) {
			logger.info("Dispatching response " + response.toString() + 
				" to following App/servlet => " + session.getKey().getApplicationName()+ 
				"/" + session.getHandler());
		}
		
		Container container = ((SipApplicationSessionImpl)session.getApplicationSession()).getSipContext().findChild(session.getHandler());
		Wrapper sipServletImpl = (Wrapper) container;
		
		if(sipServletImpl.isUnavailable()) {
			logger.warn(sipServletImpl.getName()+ " is unavailable, dropping response " + response);
		} else {
			Servlet servlet = sipServletImpl.allocate();
			try {
				servlet.service(null, response);
			} finally {
				sipServletImpl.deallocate(servlet);
			}
		}
				
	}
	
	public static boolean securityCheck(SipServletRequestImpl request)
	{
		SipApplicationSessionImpl appSession = (SipApplicationSessionImpl) request.getApplicationSession();
		SipContext sipStandardContext = appSession.getSipContext();
		boolean authorized = SipSecurityUtils.authorize(sipStandardContext, request);
		
		// This will propagate the identity for the thread and all called components
		// TODO: FIXME: This would introduce a dependency on JBoss
		// SecurityAssociation.setPrincipal(request.getUserPrincipal());
		
		return authorized;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(TimeoutEvent timeoutEvent) {
		// TODO: FIX ME
		if(timeoutEvent.isServerTransaction()) {
			if(logger.isInfoEnabled()) {
				logger.info("timeout => " + timeoutEvent.getServerTransaction().getRequest().toString());
			}
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("timeout => " + timeoutEvent.getClientTransaction().getRequest().toString());
			}
		}
		
		Transaction transaction = null;
		if(timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();		
		SipServletMessageImpl sipServletMessage = tad.getSipServletMessage();
		SipSessionImpl sipSession = sipServletMessage.getSipSession();
		sipSession.removeOngoingTransaction(transaction);
		//notifying SipErrorListener that no ACK has been received for a UAS only
		if(sipServletMessage instanceof SipServletRequestImpl &&
				tad.getProxy() == null &&
				((SipServletRequestImpl)sipServletMessage).getLastFinalResponse() != null) {
			List<SipErrorListener> sipErrorListeners = 
				sipSession.getSipApplicationSession().getSipContext().getListeners().getSipErrorListeners();			
			
			SipErrorEvent sipErrorEvent = new SipErrorEvent(
					(SipServletRequest)sipServletMessage, 
					((SipServletRequestImpl)sipServletMessage).getLastFinalResponse());
			for (SipErrorListener sipErrorListener : sipErrorListeners) {
				try {					
					sipErrorListener.noAckReceived(sipErrorEvent);
				} catch (Throwable t) {
					logger.error("SipErrorListener threw exception", t);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
	 */
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		// TODO: FIX ME		
		Transaction transaction = null;
		if(transactionTerminatedEvent.isServerTransaction()) {
			transaction = transactionTerminatedEvent.getServerTransaction();
		} else {
			transaction = transactionTerminatedEvent.getClientTransaction();
		}
		if(logger.isInfoEnabled()) {
			logger.info("transaction " + transaction + " terminated => " + transaction.getRequest().toString());
		}
		
		TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();		
		SipSessionImpl sipSessionImpl = tad.getSipServletMessage().getSipSession();
		if(sipSessionImpl == null) {
			if(logger.isInfoEnabled()) {
				logger.info("no sip session were returned for this transaction " + transaction);
			}
		} else {
			sipSessionImpl.removeOngoingTransaction(transaction);
			if(sipSessionImpl.isReadyToInvalidate()) {
				sipSessionImpl.onTerminatedState();
			}
		}
	}

	/**
	 * @return the sipApplicationRouter
	 */
	public SipApplicationRouter getSipApplicationRouter() {
		return sipApplicationRouter;
	}

	/**
	 * @param sipApplicationRouter the sipApplicationRouter to set
	 */
	public void setSipApplicationRouter(SipApplicationRouter sipApplicationRouter) {
		this.sipApplicationRouter = sipApplicationRouter;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#getSipNetworkInterfaceManager()
	 */
	public SipNetworkInterfaceManager getSipNetworkInterfaceManager() {
		return this.sipNetworkInterfaceManager;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#getSipFactory()
	 */
	public SipFactory getSipFactory() {
		return sipFactoryImpl;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#getOutboundInterfaces()
	 */
	public List<SipURI> getOutboundInterfaces() {
		return sipNetworkInterfaceManager.getOutboundInterfaces();
	}		
	
	/**
	 * set the outbound interfaces on all servlet context of applications deployed
	 */
	private void resetOutboundInterfaces() {
		List<SipURI> outboundInterfaces = sipNetworkInterfaceManager.getOutboundInterfaces();
		for (SipContext sipContext : applicationDeployed.values()) {
			sipContext.getServletContext().setAttribute(javax.servlet.sip.SipServlet.OUTBOUND_INTERFACES,
					outboundInterfaces);				
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#addHostName(java.lang.String)
	 */
	public void addHostName(String hostName) {
		if(logger.isDebugEnabled()) {
			logger.debug(this);
			logger.debug("Adding hostname "+ hostName);
		}
		hostNames.add(hostName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#findHostNames()
	 */
	public List<String> findHostNames() {		
		return Collections.unmodifiableList(hostNames);
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#removeHostName(java.lang.String)
	 */
	public void removeHostName(String hostName) {
		if(logger.isDebugEnabled()) {
			logger.debug("Removing hostname "+ hostName);
		}
		hostNames.remove(hostName);
	}

	/**
	 * @return the sessionManager
	 */
	public SessionManagerUtil getSessionManager() {
		return sessionManager;
	}

	/**
	 * 
	 */
	public SipApplicationRouterInfo getNextInterestedApplication(
			SipServletRequestImpl sipServletRequest) {
		SipApplicationRoutingRegion routingRegion = null;
		Serializable stateInfo = null;
		if(sipServletRequest.getSipSession() != null) {
			routingRegion = sipServletRequest.getSipSession().getRegion();
			stateInfo =sipServletRequest.getSipSession().getStateInfo(); 
		}
		Request request = (Request) sipServletRequest.getMessage();
		
		SipServletRequestReadOnly sipServletRequestReadOnly = new SipServletRequestReadOnly(sipServletRequest);
		SipApplicationRouterInfo applicationRouterInfo = sipApplicationRouter.getNextApplication(
			sipServletRequestReadOnly, 
			routingRegion, 
			sipServletRequest.getRoutingDirective(), 
			null,
			stateInfo);
		sipServletRequestReadOnly = null;
		// 15.4.1 Procedure : point 2
		SipRouteModifier sipRouteModifier = applicationRouterInfo.getRouteModifier();
		String[] routes = applicationRouterInfo.getRoutes();		
		try {
			// ROUTE modifier indicates that SipApplicationRouterInfo.getRoute() returns a valid route,
			// it is up to container to decide whether it is external or internal.
			if(SipRouteModifier.ROUTE.equals(sipRouteModifier)) {						
				Address routeAddress = null; 
				RouteHeader applicationRouterInfoRouteHeader = null;
				
				routeAddress = SipFactories.addressFactory.createAddress(routes[0]);
				applicationRouterInfoRouteHeader = SipFactories.headerFactory.createRouteHeader(routeAddress);									
				if(isRouteExternal(applicationRouterInfoRouteHeader)) {				
					// push all of the routes on the Route header stack of the request and 
					// send the request externally
					for (int i = routes.length-1 ; i >= 0; i--) {
						Header routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, routes[i]);
						request.addHeader(routeHeader);
					}				
				}
			} else if (SipRouteModifier.ROUTE_BACK.equals(sipRouteModifier)) {
				// Push container Route, pick up the first outbound interface
				SipURI sipURI = getOutboundInterfaces().get(0);
				sipURI.setParameter("modifier", "route_back");
				Header routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, sipURI.toString());
				request.addHeader(routeHeader);
				// push all of the routes on the Route header stack of the request and 
				// send the request externally
				for (int i = routes.length-1 ; i >= 0; i--) {
					routeHeader = SipFactories.headerFactory.createHeader(RouteHeader.NAME, routes[i]);
					request.addHeader(routeHeader);
				}
			}
		} catch (ParseException e) {
			logger.error("Impossible to parse the route returned by the application router " +
					"into a compliant address",e);							
			// the AR returned an empty string route or a bad route
			// this shouldn't happen if the route modifier is ROUTE, processing is stopped
			//and a 500 is sent
//					JainSipUtils.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
//					return false;
		}
		return applicationRouterInfo;
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#findSipApplications()
	 */
	public Iterator<SipContext> findSipApplications() {
		return applicationDeployed.values().iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.SipApplicationDispatcher#findSipApplication(java.lang.String)
	 */
	public SipContext findSipApplication(String applicationName) {
		return applicationDeployed.get(applicationName);
	}

	// -------------------- JMX and Registration  --------------------
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
	
	/*
	 * (non-Javadoc)
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {}

	/*
	 * (non-Javadoc)
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean registrationDone) {}

	/*
	 * (non-Javadoc)
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {}

	/*
	 * (non-Javadoc)
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name)
			throws Exception {
		oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
	}
	
	/* Exposed methods for the management console. Some of these duplicate existing methods, but
	 * with JMX friendly types.
	 */
	
	public String[] findInstalledSipApplications() {
		Iterator<SipContext> apps = findSipApplications();
		ArrayList<String> appList = new ArrayList<String>();
		while(apps.hasNext()){
			SipContext ctx = apps.next();
			appList.add(ctx.getApplicationName());
		}
		String[] ret = new String[appList.size()];
		for(int q=0; q<appList.size(); q++) ret[q] = appList.get(q);
		return ret;
	}
	
	public Object retrieveApplicationRouterConfiguration() {
		if(this.sipApplicationRouter instanceof ManageableApplicationRouter) {
			ManageableApplicationRouter router = (ManageableApplicationRouter) this.sipApplicationRouter;
			return router.getCurrentConfiguration();
		} else {
			throw new RuntimeException("This application router is not manageable");
		}
	}
	
	public void updateApplicationRouterConfiguration(Object configuration) {
		if(this.sipApplicationRouter instanceof ManageableApplicationRouter) {
			ManageableApplicationRouter router = (ManageableApplicationRouter) this.sipApplicationRouter;
			router.configure(configuration);
		} else {
			throw new RuntimeException("This application router is not manageable");
		}
	}
}
