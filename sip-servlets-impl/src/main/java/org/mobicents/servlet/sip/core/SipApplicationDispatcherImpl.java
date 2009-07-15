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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.spi.ServiceRegistry;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
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
import javax.sip.header.Parameters;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.LifecycleException;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.modeler.Registry;
import org.mobicents.servlet.sip.GenericUtils;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.dispatchers.DispatcherException;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcherFactory;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.router.ManageableApplicationRouter;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Implementation of the SipApplicationDispatcher interface.
 * Central point getting the sip messages from the different stacks for a Tomcat Service(Engine), 
 * translating jain sip SIP messages to sip servlets SIP messages, creating a MessageRouter responsible 
 * for choosing which Dispatcher will be used for routing the message and  
 * dispatches the messages.
 * @author Jean Deruelle 
 */
public class SipApplicationDispatcherImpl implements SipApplicationDispatcher, MBeanRegistration {
	
	/**
	 * Timer task that will gather information about congestion control 
	 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
	 */
	public class CongestionControlTimerTask implements Runnable {		
		
		public void run() {
			if(logger.isDebugEnabled()) {
				logger.debug("CongestionControlTimerTask now running ");
			}
			analyzeQueueCongestionState();
			analyzeMemory();
			//TODO wait for JDK 6 new OperatingSystemMXBean
//			analyzeCPU();
		}
	} 
	
	//the logger
	private static transient Logger logger = Logger.getLogger(SipApplicationDispatcherImpl.class);
		
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
	//map app names to hashes
	private Map<String, String> applicationNameToMd = null;

	//List of host names managed by the container
	private Set<String> hostNames = null;

	//30 sec
	private long congestionControlCheckingInterval;		
	
	protected transient CongestionControlTimerTask congestionControlTimerTask;
	
	protected transient ScheduledFuture congestionControlTimerFuture;
	
	private Boolean started = Boolean.FALSE;

	private SipNetworkInterfaceManager sipNetworkInterfaceManager;
	
	private ConcurrencyControlMode concurrencyControlMode;
	
	private AtomicLong requestsProcessed = new AtomicLong(0);
	
	private AtomicLong responsesProcessed = new AtomicLong(0);
	
	private boolean rejectSipMessages = false;
	
	private boolean bypassResponseExecutor = false;
	private boolean bypassRequestExecutor = false;
	
	private boolean memoryToHigh = false;	
	
	private double maxMemory;
	
	private int memoryThreshold;
	
	private CongestionControlPolicy congestionControlPolicy;
	
	int queueSize;
	
	private int numberOfMessagesInQueue;
	
	private double percentageOfMemoryUsed;
	
	// This executor is used for async things that don't need to wait on session executors, like CANCEL requests
	// or when the container is configured to execute every request ASAP without waiting on locks (no concurrency control)
	private ThreadPoolExecutor asynchronousExecutor = new ThreadPoolExecutor(4, 32, 90, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());
	
	//used for the congestion control mechanism
	private ScheduledThreadPoolExecutor congestionControlThreadPool = null;
	
	// fatcory for dispatching SIP messages
	private MessageDispatcherFactory messageDispatcherFactory;
	
	/**
	 * 
	 */
	public SipApplicationDispatcherImpl() {
		applicationDeployed = new ConcurrentHashMap<String, SipContext>();
		mdToApplicationName = new ConcurrentHashMap<String, String>();
		applicationNameToMd = new ConcurrentHashMap<String, String>();
		sipFactoryImpl = new SipFactoryImpl(this);
		hostNames = new CopyOnWriteArraySet<String>();
		sipNetworkInterfaceManager = new SipNetworkInterfaceManager(this);
		maxMemory = Runtime.getRuntime().maxMemory() / 1024;
		congestionControlPolicy = CongestionControlPolicy.ErrorResponse;
		congestionControlThreadPool = new ScheduledThreadPoolExecutor(2,
				new ThreadPoolExecutor.CallerRunsPolicy());
		congestionControlThreadPool.prestartAllCoreThreads();		
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
				sipApplicationRouter = ((SipApplicationRouterProvider)
					Class.forName(sipApplicationRouterProviderClassName).newInstance()).getSipApplicationRouter();
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
			//TODO when moving to JDK 6, use the official http://java.sun.com/javase/6/docs/api/java/util/ServiceLoader.html instead			
			//http://grep.codeconsult.ch/2007/10/31/the-java-service-provider-spec-and-sunmiscservice/
			Iterator<SipApplicationRouterProvider> providers = ServiceRegistry.lookupProviders(SipApplicationRouterProvider.class);
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
		if(logger.isInfoEnabled()) {
			logger.info("bypassRequestExecutor ? " + bypassRequestExecutor);
			logger.info("bypassResponseExecutor ? " + bypassResponseExecutor);
		}
		messageDispatcherFactory = new MessageDispatcherFactory(this);
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
		congestionControlTimerTask = new CongestionControlTimerTask();
		if(congestionControlTimerFuture == null && congestionControlCheckingInterval > 0) { 
				congestionControlTimerFuture = congestionControlThreadPool.scheduleWithFixedDelay(congestionControlTimerTask, congestionControlCheckingInterval, congestionControlCheckingInterval, TimeUnit.MILLISECONDS);
		 	if(logger.isInfoEnabled()) {
		 		logger.info("Congestion control background task started and checking every " + congestionControlCheckingInterval + " milliseconds.");
		 	}
		} else {
			if(logger.isInfoEnabled()) {
		 		logger.info("No Congestion control background task started since the checking interval is equals to " + congestionControlCheckingInterval + " milliseconds.");
		 	}
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
			sipContext.notifySipServletsListeners();
		}		
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
		if(sipApplicationName == null) {
			throw new IllegalArgumentException("Something when wrong while initializing a sip servlets or converged application ");
		}
		if(sipApplication == null) {
			throw new IllegalArgumentException("Something when wrong while initializing the following application " + sipApplicationName);
		}
		
		//if the application has not set any concurrency control mode, we default to the container wide one
		if(sipApplication.getConcurrencyControlMode() == null) {			
			sipApplication.setConcurrencyControlMode(concurrencyControlMode);
			if(logger.isInfoEnabled()) {
				logger.info("No concurrency control mode for application " + sipApplicationName + " , defaulting to the container wide one : " + concurrencyControlMode);
			}
		} else {
			if(logger.isInfoEnabled()) {
				logger.info("Concurrency control mode for application " + sipApplicationName + " is " + sipApplication.getConcurrencyControlMode());
			}
		}
		sipApplication.getServletContext().setAttribute(ConcurrencyControlMode.class.getCanonicalName(), sipApplication.getConcurrencyControlMode());		
		
		applicationDeployed.put(sipApplicationName, sipApplication);

		String hash = GenericUtils.hashString(sipApplicationName);
		mdToApplicationName.put(hash, sipApplicationName);
		applicationNameToMd.put(sipApplicationName, hash);
		
		List<String> newlyApplicationsDeployed = new ArrayList<String>();
		newlyApplicationsDeployed.add(sipApplicationName);
		sipApplicationRouter.applicationDeployed(newlyApplicationsDeployed);
		//if the ApplicationDispatcher is started, notification is sent that the servlets are ready for service
		//otherwise the notification will be delayed until the ApplicationDispatcher has started
		synchronized (started) {
			if(started) {
				sipApplication.notifySipServletsListeners();
			}
		}
		if(logger.isInfoEnabled()) {
			logger.info("the following sip servlet application has been added : " + sipApplicationName);
		}
		if(logger.isInfoEnabled()) {
			logger.info("It contains the following Sip Servlets : ");
			for(String servletName : sipApplication.getChildrenMap().keySet()) {
				logger.info("SipApplicationName : " + sipApplicationName + "/ServletName : " + servletName);
			}
			if(sipApplication.getSipRubyController() != null) {
				logger.info("It contains the following Sip Ruby Controller : " + sipApplication.getSipRubyController());
			}
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
		if(sipContext != null) {
			((SipManager)sipContext.getManager()).removeAllSessions();
		}
		String hash = GenericUtils.hashString(sipApplicationName);
		mdToApplicationName.remove(hash);
		applicationNameToMd.remove(sipApplicationName);
		if(logger.isInfoEnabled()) {
			logger.info("the following sip servlet application has been removed : " + sipApplicationName);
		}
		return sipContext;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
	 */
	public void processIOException(IOExceptionEvent event) {
		logger.error("An IOException occured on " + event.getHost() + ":" + event.getPort() + "/" + event.getTransport() + " for provider " + event.getSource());
	}
	
	/*
	 * Gives the number of pending messages in all queues for all concurrency control modes.
	 */
	public int getNumberOfPendingMessages() {
		return this.asynchronousExecutor.getQueue().size();
//		int size = 0;
//		Iterator<SipContext> applicationsIterator = this.applicationDeployed
//				.values().iterator();
//		boolean noneModeAlreadyCounted = false;
//		while (applicationsIterator.hasNext()) {
//			SipContext context = applicationsIterator.next();
//			SipManager manager = (SipManager) context
//					.getManager();
//			if(context.getConcurrencyControlMode().equals(
//					ConcurrencyControlMode.None) && !noneModeAlreadyCounted) {
//				size = this.asynchronousExecutor.getQueue().size();
//				noneModeAlreadyCounted = true;
//			} else if (context.getConcurrencyControlMode().equals(
//					ConcurrencyControlMode.SipApplicationSession)) {
//				Iterator<MobicentsSipApplicationSession> sessionIterator = manager
//						.getAllSipApplicationSessions();
//				while (sessionIterator.hasNext()) {
//					size += sessionIterator.next().getExecutorService()
//							.getQueue().size();
//				}
//			} else if (context.getConcurrencyControlMode().equals(
//					ConcurrencyControlMode.SipSession)) {
//				Iterator<MobicentsSipSession> sessionIterator = manager
//						.getAllSipSessions();
//				while (sessionIterator.hasNext()) {
//					size += sessionIterator.next().getExecutorService()
//							.getQueue().size();
//				}
//			}
//		}
//		
//		return size;
	}
	
	private void analyzeQueueCongestionState() {
		this.numberOfMessagesInQueue = getNumberOfPendingMessages();
		if(rejectSipMessages) {
			if(numberOfMessagesInQueue  <queueSize) {
				logger.warn("number of pending messages in the queues : " + numberOfMessagesInQueue + " < to the queue Size : " + queueSize + " => stopping to reject requests");
				rejectSipMessages = false;
			}
		} else {
			if(numberOfMessagesInQueue > queueSize) {
				logger.warn("number of pending messages in the queues : " + numberOfMessagesInQueue + " > to the queue Size : " + queueSize + " => starting to reject requests");
				rejectSipMessages = true;
			}
		}
	}
	
	private void analyzeMemory() {
		Runtime runtime = Runtime.getRuntime();  
		   
		double allocatedMemory = runtime.totalMemory() / 1024;  
		double freeMemory = runtime.freeMemory() / 1024;
		
		double totalFreeMemory = freeMemory + (maxMemory - allocatedMemory);
		this.percentageOfMemoryUsed=  (100 - ((totalFreeMemory / maxMemory) * 100));

		if(memoryToHigh) {
			if(percentageOfMemoryUsed < memoryThreshold) {
				logger.warn("Memory used: " + percentageOfMemoryUsed + "% < to the memory threshold : " + memoryThreshold + " => stopping to reject requests");
				memoryToHigh = false;				
			}
		} else {
			if(percentageOfMemoryUsed > memoryThreshold) {
				logger.warn("Memory used: " + percentageOfMemoryUsed + "% > to the memory threshold : " + memoryThreshold + " => starting to reject requests");
				memoryToHigh = true;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestEvent) {
		if((rejectSipMessages || memoryToHigh) && CongestionControlPolicy.DropMessage.equals(congestionControlPolicy)) {
			logger.error("dropping request, memory is too high or too many messages present in queues");
			return;
		}	
		final SipProvider sipProvider = (SipProvider)requestEvent.getSource();
		ServerTransaction requestTransaction =  requestEvent.getServerTransaction();
		final Dialog dialog = requestEvent.getDialog();
		final Request request = requestEvent.getRequest();
		final String requestMethod = request.getMethod();
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("Got a request event "  + request.toString());
			}			
			if (!Request.ACK.equals(requestMethod) && requestTransaction == null ) {
				try {
					//folsson fix : Workaround broken Cisco 7940/7912
				    if(request.getHeader(MaxForwardsHeader.NAME) == null){
					    request.setHeader(SipFactories.headerFactory.createMaxForwardsHeader(70));
					}
				    requestTransaction = sipProvider.getNewServerTransaction(request);					
				} catch ( TransactionUnavailableException tae) {
					logger.error("cannot get a new Server transaction for this request " + request, tae);
					// Sends a 500 Internal server error and stops processing.				
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, requestTransaction, request, sipProvider);				
	                return;
				} catch ( TransactionAlreadyExistsException taex ) {
					// Already processed this request so just return.
					return;				
				} 
			} 	
			final ServerTransaction transaction = requestTransaction;
			
			if(logger.isDebugEnabled()) {
				logger.debug("ServerTx ref "  + transaction);				
				logger.debug("Dialog ref "  + dialog);				
			}
			
			final SipServletRequestImpl sipServletRequest = new SipServletRequestImpl(
						request,
						sipFactoryImpl,
						null,
						transaction,
						dialog,
						JainSipUtils.dialogCreatingMethods.contains(requestMethod));
			requestsProcessed.incrementAndGet();	
			// Check if the request is meant for me. If so, strip the topmost
			// Route header.
			final RouteHeader routeHeader = (RouteHeader) request
					.getHeader(RouteHeader.NAME);
			
			//Popping the router header if it's for the container as
			//specified in JSR 289 - Section 15.8
			if(!isRouteExternal(routeHeader)) {
				request.removeFirst(RouteHeader.NAME);
				sipServletRequest.setPoppedRoute(routeHeader);
				final Parameters poppedAddress = (Parameters)routeHeader.getAddress().getURI();
				if(poppedAddress.getParameter(MessageDispatcher.RR_PARAM_PROXY_APP) != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("the request is for a proxy application, thus it is a subsequent request ");
					}
					sipServletRequest.setRoutingState(RoutingState.SUBSEQUENT);
				}
				if(transaction != null) {
					TransactionApplicationData transactionApplicationData = (TransactionApplicationData)transaction.getApplicationData();
					if(transactionApplicationData != null && transactionApplicationData.getInitialPoppedRoute() == null) {				
						transactionApplicationData.setInitialPoppedRoute(new AddressImpl(routeHeader.getAddress(), null, false));
					}
				}
			}							
			if(logger.isDebugEnabled()) {
				logger.debug("Routing State " + sipServletRequest.getRoutingState());
			}
			
			try {
				if(rejectSipMessages || memoryToHigh) {
					if(!Request.ACK.equals(requestMethod) && !Request.PRACK.equals(requestMethod)) {
						MessageDispatcher.sendErrorResponse(Response.SERVICE_UNAVAILABLE, (ServerTransaction) sipServletRequest.getTransaction(), (Request) sipServletRequest.getMessage(), sipProvider);
					}
				}
				messageDispatcherFactory.getRequestDispatcher(sipServletRequest, this).
					dispatchMessage(sipProvider, sipServletRequest);
			} catch (DispatcherException e) {
				logger.error("Unexpected exception while processing request " + request,e);
				// Sends an error response if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
				if(!Request.ACK.equalsIgnoreCase(requestMethod)) {
					MessageDispatcher.sendErrorResponse(e.getErrorCode(), (ServerTransaction) sipServletRequest.getTransaction(), (Request) sipServletRequest.getMessage(), sipProvider);
				}
				return;
			} catch (Throwable e) {
				logger.error("Unexpected exception while processing request " + request,e);
				// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
				if(!Request.ACK.equalsIgnoreCase(requestMethod)) {
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, (ServerTransaction) sipServletRequest.getTransaction(), (Request) sipServletRequest.getMessage(), sipProvider);
				}
				return;
			}
		} catch (Throwable e) {
			logger.error("Unexpected exception while processing request " + request,e);
			// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
			if(!Request.ACK.equalsIgnoreCase(request.getMethod())) {
				MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, requestTransaction, request, sipProvider);
			}
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public void processResponse(ResponseEvent responseEvent) {
		if((rejectSipMessages || memoryToHigh) && CongestionControlPolicy.DropMessage.equals(congestionControlPolicy)) {
			logger.error("dropping response, memory is too high or too many messages present in queues");
			return;
		}
		if(logger.isInfoEnabled()) {
			logger.info("Response " + responseEvent.getResponse().toString());
		}
		final Response response = responseEvent.getResponse();
		final CSeqHeader cSeqHeader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
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
		responsesProcessed.incrementAndGet();
		final ClientTransaction clientTransaction = responseEvent.getClientTransaction();		
		final Dialog dialog = responseEvent.getDialog();
		// Transate the response to SipServletResponse
		final SipServletResponseImpl sipServletResponse = new SipServletResponseImpl(
				response, 
				sipFactoryImpl,
				clientTransaction, 
				null, 
				dialog);
		try {		
			messageDispatcherFactory.getResponseDispatcher(sipServletResponse, this).
				dispatchMessage(null, sipServletResponse);
		} catch (Throwable e) {
			logger.error("An unexpected exception happened while routing the response " +  sipServletResponse, e);
			return;
		}
	}	

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
	 */
	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		if(logger.isInfoEnabled()) {
			logger.info("Dialog Terminated => " + dialogTerminatedEvent.getDialog().getCallId().getCallId());
		}
		Dialog dialog = dialogTerminatedEvent.getDialog();		
		TransactionApplicationData tad = (TransactionApplicationData) dialog.getApplicationData();
		if(tad != null) {
			SipServletMessageImpl sipServletMessageImpl = tad.getSipServletMessage();
			MobicentsSipSession sipSessionImpl = sipServletMessageImpl.getSipSession();
			tryToInvalidateSession(sipSessionImpl);
		} else {
			logger.warn("no application data for this dialog " + dialogTerminatedEvent.getDialog().getDialogId());
		}
	}

	/**
	 * @param sipSessionImpl
	 */
	private void tryToInvalidateSession(MobicentsSipSession sipSessionImpl) {
		//the key can be null if the application already invalidated the session
		if(sipSessionImpl.getKey() != null) {
			SipContext sipContext = findSipApplication(sipSessionImpl.getKey().getApplicationName());
			//the context can be null if the server is being shutdown
			if(sipContext != null) {		
				sipContext.enterSipApp(null, null, null, true, false);
				try {
					if(logger.isInfoEnabled()) {
						logger.info("session " + sipSessionImpl.getId() + " is valid ? :" + sipSessionImpl.isValid());
						if(sipSessionImpl.isValid()) {
							logger.info("Sip session " + sipSessionImpl.getId() + " is ready to be invalidated ? :" + sipSessionImpl.isReadyToInvalidate());
						}
					}
					
					if(sipSessionImpl.isValid() && sipSessionImpl.isReadyToInvalidate()) {				
						sipSessionImpl.onTerminatedState();
					}
					MobicentsSipApplicationSession sipApplicationSession = sipSessionImpl.getSipApplicationSession();
					if(sipApplicationSession != null && sipApplicationSession.isValid() && sipApplicationSession.isReadyToInvalidate()) {
						sipApplicationSession.tryToInvalidate();
					}
				} finally {
					sipContext.exitSipApp(null, null);
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
			logger.info("transaction " + transaction + " timed out => " + transaction.getRequest().toString());
		}
		
		TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
		if(tad != null) {
			SipServletMessageImpl sipServletMessage = tad.getSipServletMessage();
			MobicentsSipSession sipSession = sipServletMessage.getSipSession();
			if(sipSession != null) {
				sipSession.removeOngoingTransaction(transaction);			
				//notifying SipErrorListener that no ACK has been received for a UAS only
				SipServletResponseImpl lastFinalResponse = (SipServletResponseImpl)
					((SipServletRequestImpl)sipServletMessage).getLastInformationalResponse();
				if(logger.isInfoEnabled()) {
					logger.info("last Final Response" + lastFinalResponse);
				}
				ProxyImpl proxy = sipSession.getProxy();
				if(sipServletMessage instanceof SipServletRequestImpl &&
						proxy == null &&
						lastFinalResponse != null) {
					List<SipErrorListener> sipErrorListeners = 
						sipSession.getSipApplicationSession().getSipContext().getListeners().getSipErrorListeners();			
					
					SipErrorEvent sipErrorEvent = new SipErrorEvent(
							(SipServletRequest)sipServletMessage, 
							lastFinalResponse);
					for (SipErrorListener sipErrorListener : sipErrorListeners) {
						try {					
							sipErrorListener.noAckReceived(sipErrorEvent);
						} catch (Throwable t) {
							logger.error("SipErrorListener threw exception", t);
						}
					}
				}
				SipServletResponseImpl lastInfoResponse = (SipServletResponseImpl)
					((SipServletRequestImpl)sipServletMessage).getLastInformationalResponse();
				if(logger.isInfoEnabled()) {
					logger.info("last Informational Response" + lastInfoResponse);
				}
				if(sipServletMessage instanceof SipServletRequestImpl &&
						proxy == null &&
						 lastInfoResponse != null) {
					List<SipErrorListener> sipErrorListeners = 
						sipSession.getSipApplicationSession().getSipContext().getListeners().getSipErrorListeners();			
					
					SipErrorEvent sipErrorEvent = new SipErrorEvent(
							(SipServletRequest)sipServletMessage, 
							lastInfoResponse);
					for (SipErrorListener sipErrorListener : sipErrorListeners) {
						try {					
							sipErrorListener.noPrackReceived(sipErrorEvent);
						} catch (Throwable t) {
							logger.error("SipErrorListener threw exception", t);
						}
					}
				}
				tryToInvalidateSession(sipSession);
			}
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
			SipServletMessageImpl sipServletMessageImpl = tad.getSipServletMessage();
			MobicentsSipSession sipSessionImpl = sipServletMessageImpl.getSipSession();
			if(sipSessionImpl == null) {
//				if(logger.isInfoEnabled()) {
					logger.warn("no sip session were returned for this transaction " + transaction + " and message " + sipServletMessageImpl);
//				}
			} else {
				if(sipSessionImpl.getKey() != null) {
					if(logger.isInfoEnabled()) {
						logger.info("sip session " + sipSessionImpl.getId() + " returned for this transaction " + transaction);
					}
					tryToInvalidateSession(sipSessionImpl);
				} else {
					if(logger.isInfoEnabled()) {
						logger.info("sip session already invalidate by the app for message " + sipServletMessageImpl);
					}
				}
				
//				sipSessionImpl.removeOngoingTransaction(transaction);
			}
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("TransactionApplicationData not available on the following request " + transaction.getRequest().toString());
			}
		}
	}

	public String getApplicationNameFromHash(String hash) {
		return mdToApplicationName.get(hash);
	}
	
	public String getHashFromApplicationName(String appName) {
		return applicationNameToMd.get(appName);
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
	public Set<String> findHostNames() {		
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
	 * 
	 */
	public SipApplicationRouterInfo getNextInterestedApplication(
			SipServletRequestImpl sipServletRequest) {
		SipApplicationRoutingRegion routingRegion = null;
		Serializable stateInfo = null;
		if(sipServletRequest.getSipSession() != null) {
			routingRegion = sipServletRequest.getSipSession().getRegionInternal();
			stateInfo = sipServletRequest.getSipSession().getStateInfo();
		}
		final Request request = (Request) sipServletRequest.getMessage();
		
		sipServletRequest.setReadOnly(true);
		SipApplicationRouterInfo applicationRouterInfo = sipApplicationRouter.getNextApplication(
			sipServletRequest, 
			routingRegion, 
			sipServletRequest.getRoutingDirective(), 
			null,
			stateInfo);
		sipServletRequest.setReadOnly(false);
		// 15.4.1 Procedure : point 2
		final SipRouteModifier sipRouteModifier = applicationRouterInfo.getRouteModifier();
		final String[] routes = applicationRouterInfo.getRoutes();		
		try {
			// ROUTE modifier indicates that SipApplicationRouterInfo.getRoute() returns a valid route,
			// it is up to container to decide whether it is external or internal.
			if(SipRouteModifier.ROUTE.equals(sipRouteModifier)) {						
				final Address routeAddress = SipFactories.addressFactory.createAddress(routes[0]);
				final RouteHeader applicationRouterInfoRouteHeader = SipFactories.headerFactory.createRouteHeader(routeAddress);									
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
				final SipURI sipURI = getOutboundInterfaces().get(0);
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

	public ThreadPoolExecutor getAsynchronousExecutor() {
		return asynchronousExecutor;
	}

	/**
	 * Serialize the state info in memory and deserialize it and return the new object. 
	 * Since there is no clone method this is the only way to get the same object with a new reference 
	 * @param stateInfo the state info to serialize
	 * @return the state info serialized and deserialized
	 */
	private Serializable serializeStateInfo(Serializable stateInfo) {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream out = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream in = null;
		
		try{
			baos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(baos);
			out.writeObject(stateInfo);					
			bais = new ByteArrayInputStream(baos.toByteArray());
			in =new ObjectInputStream(bais);
			return (Serializable)in.readObject();			
		} catch (IOException e) {
			logger.error("Impossible to serialize the state info", e);
			return stateInfo;
		} catch (ClassNotFoundException e) {
			logger.error("Impossible to serialize the state info", e);
			return stateInfo;
		} finally {
			try {
				if(out != null) {
					out.close();
				}
				if(in != null) {
					in.close();
				}
				if(baos != null) {
					baos.close();
				}
				if(bais != null) {
					bais.close();
				}
			} catch (IOException e) {
				logger.error("Impossible to close the streams after serializing state info", e);
			}
		}
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

	public ConcurrencyControlMode getConcurrencyControlMode() {
		return concurrencyControlMode;
	}

	public void setConcurrencyControlMode(ConcurrencyControlMode concurrencyControlMode) {
		this.concurrencyControlMode = concurrencyControlMode;
		if(logger.isInfoEnabled()) {
			logger.info("Container wide Concurrency Control set to " + concurrencyControlMode.toString());
		}
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
	
	public void setConcurrencyControlModeByName(String concurrencyControlMode) {
		this.concurrencyControlMode = ConcurrencyControlMode.valueOf(concurrencyControlMode);
	}	

	/**
	 * @return the requestsProcessed
	 */
	public long getRequestsProcessed() {
		return requestsProcessed.get();
	}
	
	/**
	 * @return the requestsProcessed
	 */
	public long getResponsesProcessed() {
		return responsesProcessed.get();
	}

	/**
	 * @param congestionControlCheckingInterval the congestionControlCheckingInterval to set
	 */
	public void setCongestionControlCheckingInterval(
			long congestionControlCheckingInterval) {
		this.congestionControlCheckingInterval = congestionControlCheckingInterval;
		if(started) {
			if(congestionControlTimerFuture != null) {
				congestionControlTimerFuture.cancel(false);
			}
			if(congestionControlCheckingInterval > 0) {
				congestionControlTimerFuture = congestionControlThreadPool.scheduleWithFixedDelay(congestionControlTimerTask, congestionControlCheckingInterval, congestionControlCheckingInterval, TimeUnit.MILLISECONDS);
				if(logger.isInfoEnabled()) {
			 		logger.info("Congestion control background task modified to check every " + congestionControlCheckingInterval + " milliseconds.");
			 	}
			} else {
				if(logger.isInfoEnabled()) {
			 		logger.info("No Congestion control background task started since the checking interval is equals to " + congestionControlCheckingInterval + " milliseconds.");
			 	}
			}
		}
	}

	/**
	 * @return the congestionControlCheckingInterval
	 */
	public long getCongestionControlCheckingInterval() {
		return congestionControlCheckingInterval;
	}

	/**
	 * @param congestionControlPolicy the congestionControlPolicy to set
	 */
	public void setCongestionControlPolicy(CongestionControlPolicy congestionControlPolicy) {
		this.congestionControlPolicy = congestionControlPolicy;
	}

	
	public void setCongestionControlPolicyByName(String congestionControlPolicy) {
		this.congestionControlPolicy = CongestionControlPolicy.valueOf(congestionControlPolicy);
	}	

	/**
	 * @return the congestionControlPolicy
	 */
	public CongestionControlPolicy getCongestionControlPolicy() {
		return congestionControlPolicy;
	}

	/**
	 * @param memoryThreshold the memoryThreshold to set
	 */
	public void setMemoryThreshold(int memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}

	/**
	 * @return the memoryThreshold
	 */
	public int getMemoryThreshold() {
		return memoryThreshold;
	}
	
	/**
	 * @return the numberOfMessagesInQueue
	 */
	public int getNumberOfMessagesInQueue() {
		return numberOfMessagesInQueue;
	}

	/**
	 * @return the percentageOfMemoryUsed
	 */
	public double getPercentageOfMemoryUsed() {
		return percentageOfMemoryUsed;
	}

	/**
	 * @param bypassRequestExecutor the bypassRequestExecutor to set
	 */
	public void setBypassRequestExecutor(boolean bypassRequestExecutor) {
		this.bypassRequestExecutor = bypassRequestExecutor;
	}

	/**
	 * @return the bypassRequestExecutor
	 */
	public boolean isBypassRequestExecutor() {
		return bypassRequestExecutor;
	}

	/**
	 * @param bypassResponseExecutor the bypassResponseExecutor to set
	 */
	public void setBypassResponseExecutor(boolean bypassResponseExecutor) {
		this.bypassResponseExecutor = bypassResponseExecutor;
	}

	/**
	 * @return the bypassResponseExecutor
	 */
	public boolean isBypassResponseExecutor() {
		return bypassResponseExecutor;
	}
}
