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

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.spi.SipApplicationRouterProvider;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.web.tomcat.service.session.ConvergedSessionReplicationContext;
import org.jboss.web.tomcat.service.session.SnapshotSipManager;
import org.mobicents.servlet.sip.GenericUtils;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.dispatchers.DispatcherException;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcherFactory;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletRequestReadOnly;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.router.ManageableApplicationRouter;
import org.mobicents.servlet.sip.startup.SipContext;

import sun.misc.Service;

/**
 * Implementation of the SipApplicationDispatcher interface.
 * Central point getting the sip messages from the different stacks for a Tomcat Service(Engine), 
 * translating jain sip SIP messages to sip servlets SIP messages, creating a MessageRouter responsible 
 * for choosing which Dispatcher will be used for routing the message and  
 * dispatches the messages.
 * @author Jean Deruelle 
 */
public class SipApplicationDispatcherImpl implements SipApplicationDispatcher, MBeanRegistration {	
	//the logger
	private static transient Log logger = LogFactory
			.getLog(SipApplicationDispatcherImpl.class);
		
	//the sip factory implementation, it is not clear if the sip factory should be the same instance
	//for all applications
	private SipFactoryImpl sipFactoryImpl = null;
	//the sip application router responsible for the routing logic of sip messages to
	//sip servlet applications
	private SipApplicationRouter sipApplicationRouter = null;
	//map of applications deployed
	private Map<String, SipContext> applicationDeployed = null;
	//map hashes to app names
	private Map<String, String> mdToApplicationName = null;

	//List of host names managed by the container
	private List<String> hostNames = null;
	
	private SessionManagerUtil sessionManager = null;
	
	private Boolean started = Boolean.FALSE;

	private SipNetworkInterfaceManager sipNetworkInterfaceManager;
	
	/**
	 * 
	 */
	public SipApplicationDispatcherImpl() {
		applicationDeployed = new ConcurrentHashMap<String, SipContext>();
		mdToApplicationName = new ConcurrentHashMap<String, String>();
		sipFactoryImpl = new SipFactoryImpl(this);
		sessionManager = new SessionManagerUtil();
		hostNames = new CopyOnWriteArrayList<String>();
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
		sipApplicationRouter.init();
		sipApplicationRouter.applicationDeployed(new ArrayList<String>(applicationDeployed.keySet()));
		
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

		mdToApplicationName.put(GenericUtils.hashString(sipApplicationName), sipApplicationName);
		
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
		mdToApplicationName.remove(GenericUtils.hashString(sipApplicationName));
		if(logger.isInfoEnabled()) {
			logger.info("the following sip servlet application has been removed : " + sipApplicationName);
		}
		return sipContext;
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
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);				
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
			
			if(routeHeader == null && !isExternal(((javax.sip.address.SipURI)request.getRequestURI()).getHost(), ((javax.sip.address.SipURI)request.getRequestURI()).getPort(), ((javax.sip.address.SipURI)request.getRequestURI()).getTransportParam())) {
				ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
				String arText = toHeader.getTag();
				ApplicationRoutingHeaderComposer ar = new ApplicationRoutingHeaderComposer(this.mdToApplicationName, arText);
				
				javax.sip.address.SipURI localUri = JainSipUtils.createRecordRouteURI(
						sipFactoryImpl.getSipNetworkInterfaceManager(), 
						JainSipUtils.findTransport(request));
				if(arText != null) {
					localUri.setParameter(MessageDispatcher.RR_PARAM_APPLICATION_NAME, ar.getLast().getApplication());
					javax.sip.address.Address address = 
						SipFactories.addressFactory.createAddress(localUri);
					routeHeader = SipFactories.headerFactory.createRouteHeader(address);
				}
			}
			//Popping the router header if it's for the container as
			//specified in JSR 289 - Section 15.8
			if(!isRouteExternal(routeHeader)) {
				request.removeFirst(RouteHeader.NAME);
				sipServletRequest.setPoppedRoute(routeHeader);
			}							
			if(logger.isInfoEnabled()) {
				logger.info("Routing State " + sipServletRequest.getRoutingState());
			}
										
			MessageDispatcherFactory.getRequestDispatcher(sipServletRequest, this).
				dispatchMessage(sipProvider, sipServletRequest);
		} catch (DispatcherException e) {
			logger.error("Unexpected exception while processing request " + request,e);
			// Sends an error response if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
			if(!Request.ACK.equalsIgnoreCase(request.getMethod())) {
				MessageDispatcher.sendErrorResponse(e.getErrorCode(), transaction, request, sipProvider);
			}
			return;
		} catch (Throwable e) {
			logger.error("Unexpected exception while processing request " + request,e);
			// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
			if(!Request.ACK.equalsIgnoreCase(request.getMethod())) {
				MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, transaction, request, sipProvider);
			}
			return;
		}
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
		
		ClientTransaction clientTransaction = responseEvent.getClientTransaction();
		Dialog dialog = responseEvent.getDialog();
		// Transate the response to SipServletResponse
		SipServletResponseImpl sipServletResponse = new SipServletResponseImpl(
				response, 
				sipFactoryImpl,
				clientTransaction, 
				null, 
				dialog);
		
		try {
			MessageDispatcherFactory.getResponseDispatcher(sipServletResponse, this).
				dispatchMessage(null, sipServletResponse);
		} catch (Throwable e) {
			logger.error("An unexpected exception happened while routing the response " +  response, e);
			return;
		}
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
		MobicentsSipSession sipSessionImpl = sipServletMessageImpl.getSipSession();
		tryToInvalidateSession(sipSessionImpl);
	}

	/**
	 * @param sipSessionImpl
	 */
	private void tryToInvalidateSession(MobicentsSipSession sipSessionImpl) {
		SipContext sipContext = findSipApplication(sipSessionImpl.getKey().getApplicationName());
		boolean isDistributable = sipContext.getDistributable();
		if(isDistributable) {
			ConvergedSessionReplicationContext.enterSipapp(null, null, true);
		}
		try {
			if(sipSessionImpl.isReadyToInvalidate()) {
				sipSessionImpl.onTerminatedState();
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
								ctx.getSoleSipSession());
						((SnapshotSipManager)ctx.getSoleSnapshotManager()).snapshot(
								ctx.getSoleSipApplicationSession());
					} 
				} finally {
					ConvergedSessionReplicationContext.finishSipCacheActivity();
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(TimeoutEvent timeoutEvent) {
		Transaction transaction = null;
		if(timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		if(logger.isInfoEnabled()) {
			logger.info("transaction " + transaction + " terminated => " + transaction.getRequest().toString());
		}
		
		TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
		if(tad != null) {
			SipServletMessageImpl sipServletMessage = tad.getSipServletMessage();
			MobicentsSipSession sipSession = sipServletMessage.getSipSession();
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
			tryToInvalidateSession(sipSession);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
	 */
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
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
		if(tad != null) {
			MobicentsSipSession sipSessionImpl = tad.getSipServletMessage().getSipSession();
			if(sipSessionImpl == null) {
				if(logger.isInfoEnabled()) {
					logger.info("no sip session were returned for this transaction " + transaction);
				}
			} else {
				if(logger.isInfoEnabled()) {
					logger.info("Invalidating sip session " + sipSessionImpl.getId() + " if ready to invalidate " + sipSessionImpl.isReadyToInvalidate());
				}
				sipSessionImpl.removeOngoingTransaction(transaction);
				tryToInvalidateSession(sipSessionImpl);
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("TransactionApplicationData not available on the following request " + transaction.getRequest().toString());
			}
		}
	}

	public Map<String, String> getMdToApplicationName() {
		return mdToApplicationName;
	}
	
	/**
	 * Check if the route is external
	 * @param routeHeader the route to check 
	 * @return true if the route is external, false otherwise
	 */
	public final boolean isRouteExternal(RouteHeader routeHeader) {
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
	public final boolean isViaHeaderExternal(ViaHeader viaHeader) {
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
	public final boolean isExternal(String host, int port, String transport) {
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
	public SipFactoryImpl getSipFactory() {
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
		return hostNames;
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
