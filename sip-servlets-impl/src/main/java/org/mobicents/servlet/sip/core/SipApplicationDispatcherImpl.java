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

package org.mobicents.servlet.sip.core;

import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.DialogTimeoutEvent.Reason;
import gov.nist.javax.sip.IOExceptionEventExt;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.message.MessageExt;

import java.io.IOException;
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
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.spi.ServiceRegistry;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.spi.SipApplicationRouterProvider;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.Parameters;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.ext.javax.sip.dns.DNSServerLocator;
import org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingListener;
import org.mobicents.ha.javax.sip.SipLoadBalancer;
import org.mobicents.javax.servlet.CongestionControlEvent;
import org.mobicents.javax.servlet.CongestionControlPolicy;
import org.mobicents.javax.servlet.ContainerListener;
import org.mobicents.servlet.sip.GenericUtils;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.address.AddressImpl;
import org.mobicents.servlet.sip.address.AddressImpl.ModifiableRule;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.b2bua.MobicentsB2BUAHelper;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcher;
import org.mobicents.servlet.sip.core.dispatchers.MessageDispatcherFactory;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.proxy.MobicentsProxy;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.listener.SipConnectorListener;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;
import org.mobicents.servlet.sip.proxy.ProxyBranchImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;
import org.mobicents.servlet.sip.router.ManageableApplicationRouter;

/**
 * Implementation of the SipApplicationDispatcher interface.
 * Central point getting the sip messages from the different stacks for a Tomcat Service(Engine), 
 * translating jain sip SIP messages to sip servlets SIP messages, creating a MessageRouter responsible 
 * for choosing which Dispatcher will be used for routing the message and  
 * dispatches the messages.
 * @author Jean Deruelle 
 */
public class SipApplicationDispatcherImpl implements SipApplicationDispatcher, SipApplicationDispatcherImplMBean, MBeanRegistration, LoadBalancerHeartBeatingListener {
	
	//list of methods supported by the AR
	private static final String[] METHODS_SUPPORTED = 
		{"REGISTER", "INVITE", "ACK", "BYE", "CANCEL", "MESSAGE", "INFO", "SUBSCRIBE", "NOTIFY", "UPDATE", "PUBLISH", "REFER", "PRACK", "OPTIONS"};
	
	// List of sip extensions supported by the container	
	private static final String[] EXTENSIONS_SUPPORTED = 
		{"MESSAGE", "INFO", "SUBSCRIBE", "NOTIFY", "UPDATE", "PUBLISH", "REFER", "PRACK", "100rel", "STUN", "path", "join", "outbound", "from-change"};
	// List of sip rfcs supported by the container
	private static final String[] RFC_SUPPORTED = 
		{"3261", "3428", "2976", "3265", "3311", "3903", "3515", "3262", "3489", "3327", "3911", "5626", "4916"};

	private static final String[] RESPONSES_PER_CLASS_OF_SC = 
		{"1XX", "2XX", "3XX", "4XX", "5XX", "6XX", "7XX", "8XX", "9XX"};
	
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
			if(gatherStatistics) {
				for (SipContext sipContext : applicationDeployed.values()) {
					sipContext.getSipManager().updateStats();
				}
			}
			//TODO wait for JDK 6 new OperatingSystemMXBean
//			analyzeCPU();
		}
	} 
	
	//the logger
	private static final Logger logger = Logger.getLogger(SipApplicationDispatcherImpl.class);
	
	// ref back to the sip service
	private SipService sipService = null;
	//the sip factory implementation
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
	
	private Boolean started = Boolean.FALSE;
	private Lock statusLock = new ReentrantLock();

	protected SipStack sipStack;
	private SipNetworkInterfaceManager sipNetworkInterfaceManager;
	private DNSServerLocator dnsServerLocator;
	
	// stats
	private boolean gatherStatistics = true;
	private static AtomicLong requestsProcessed = new AtomicLong(0);
	private static AtomicLong responsesProcessed = new AtomicLong(0);
	static final Map<String, AtomicLong> requestsProcessedByMethod = new ConcurrentHashMap<String, AtomicLong>();
	static {
		for (String method : METHODS_SUPPORTED) {
			requestsProcessedByMethod.put(method, new AtomicLong(0));
		}
	}
	static final Map<String, AtomicLong> responsesProcessedByStatusCode = new ConcurrentHashMap<String, AtomicLong>();
	static {
		for (String classOfSc : RESPONSES_PER_CLASS_OF_SC) {
			responsesProcessedByStatusCode.put(classOfSc, new AtomicLong(0));
		}
	}
	// congestion control
	private boolean memoryToHigh = false;	
	private double maxMemory;
	private int memoryThreshold;
	private int backToNormalMemoryThreshold;
	private boolean rejectSipMessages = false;
	private long congestionControlCheckingInterval; //30 sec		
	protected transient CongestionControlTimerTask congestionControlTimerTask;
	protected transient ScheduledFuture congestionControlTimerFuture;
	private CongestionControlPolicy congestionControlPolicy;
	private int numberOfMessagesInQueue;
	private double percentageOfMemoryUsed;	
	private int queueSize;
	private int backToNormalQueueSize;
	//used for the congestion control mechanism
	private ScheduledThreadPoolExecutor congestionControlThreadPool = null;
	
	// configuration
	private boolean bypassResponseExecutor = true;
	private boolean bypassRequestExecutor = true;			
	private int baseTimerInterval = 500; // base timer interval for jain sip tx
	private int t2Interval = 4000; // t2 timer interval for jain sip tx
	private int t4Interval = 5000; // t4 timer interval for jain sip tx
	private int timerDInterval = 32000; // timer D interval for jain sip tx
	private ConcurrencyControlMode concurrencyControlMode;
	
	// This executor is used for async things that don't need to wait on session executors, like CANCEL requests
	// or when the container is configured to execute every request ASAP without waiting on locks (no concurrency control)
	private ThreadPoolExecutor asynchronousExecutor = null;
	
	// fatcory for dispatching SIP messages
	private MessageDispatcherFactory messageDispatcherFactory;
	
    //the balancers names to send heartbeat to and our health info
	private Set<SipLoadBalancer> sipLoadBalancers = new CopyOnWriteArraySet<SipLoadBalancer>();
	
	/**
	 * 
	 */
	public SipApplicationDispatcherImpl() {
		applicationDeployed = new ConcurrentHashMap<String, SipContext>();
		mdToApplicationName = new ConcurrentHashMap<String, String>();
		applicationNameToMd = new ConcurrentHashMap<String, String>();
		sipFactoryImpl = new SipFactoryImpl(this);
		hostNames = new CopyOnWriteArraySet<String>();
		sipNetworkInterfaceManager = new SipNetworkInterfaceManagerImpl(this);
		maxMemory = Runtime.getRuntime().maxMemory() / (double) 1024;
		congestionControlPolicy = CongestionControlPolicy.ErrorResponse;
	}
	
	/**
	 * {@inheritDoc} 
	 */
	public void init() throws IllegalArgumentException {
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
				throw new IllegalArgumentException("Impossible to load the Sip Application Router",e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Impossible to load the Sip Application Router",e);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Impossible to load the Sip Application Router",e);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Sip Application Router defined does not implement " + SipApplicationRouterProvider.class.getName(),e);
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
			throw new IllegalArgumentException("No Sip Application Router Provider could be loaded. " +
					"No jar compliant with JSR 289 Section Section 15.4.2 could be found on the classpath " +
					"and no javax.servlet.sip.ar.spi.SipApplicationRouterProvider system property set");
		}
		if(logger.isInfoEnabled()) {
			logger.info(this + " Using the following Application Router instance: " + sipApplicationRouter);
		}
		sipApplicationRouter.init();
		sipApplicationRouter.applicationDeployed(new ArrayList<String>(applicationDeployed.keySet()));
		
		if( oname == null ) {
			try {				
                oname=new ObjectName(domain + ":type=SipApplicationDispatcher");                
                ((MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0)).registerMBean(this, oname);
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
		congestionControlThreadPool = new ScheduledThreadPoolExecutor(2,
				new ThreadPoolExecutor.CallerRunsPolicy());
		congestionControlThreadPool.prestartAllCoreThreads();	
		logger.info("AsynchronousThreadPoolExecutor size is " + sipService.getDispatcherThreadPoolSize());		
		asynchronousExecutor = new ThreadPoolExecutor(sipService.getDispatcherThreadPoolSize(), 64, 90, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
		            private int threadCount = 0;
		
		            public Thread newThread(Runnable pRunnable) {
		            	Thread thread = new Thread(pRunnable, String.format("%s-%d",
		                                "MSS-Executor-Thread", threadCount++));
		            	thread.setPriority(((SipStackImpl)sipStack).getThreadPriority());
		            	return thread;
		            }
        });		
		asynchronousExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler(){

			public void rejectedExecution(Runnable r,
					ThreadPoolExecutor executor) {
				logger.warn("Executor job was rejected " + r.toString());
				
			}
			
		});
	}
	/**
	 * {@inheritDoc}
	 */
	public void start() {		
		statusLock.lock();
		try {
			if(started) {
				return;
			}
			started = Boolean.TRUE;
		} finally {
			statusLock.unlock();
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
		Version.printVersion();
		// outbound interfaces set here and not in sipstandardcontext because
		// depending on jboss or tomcat context can be started before or after
		// connectors
		resetOutboundInterfaces();
		
		// Starting the SIP Stack right before we notify the apps that the container is ready
		// to serve and dispatch SIP Messages. 
		// In addition, the LB Heartbeat will be started only when apps are ready
		try {
			startSipStack();
		} catch (Exception e) {
			throw new IllegalStateException("The SIP Stack couldn't be started " , e);
		}
		
		//JSR 289 Section 2.1.1 Step 4.If present invoke SipServletListener.servletInitialized() on each of initialized Servlet's listeners.
		for (SipContext sipContext : applicationDeployed.values()) {
			sipContext.notifySipContextListeners(new SipContextEventImpl(SipContextEventType.SERVLET_INITIALIZED, null));
		}
		
		if(logger.isInfoEnabled()) {
			logger.info("SipApplicationDispatcher Started");
		}	
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		statusLock.lock();
		try {
			if(!started) {
				return;
			}
			started = Boolean.FALSE;
		} finally {
			statusLock.unlock();
		}
		congestionControlThreadPool.shutdownNow();
		asynchronousExecutor.shutdownNow();						
		sipApplicationRouter.destroy();
		
		stopSipStack();				
		
		if(oname != null) {
			try {
				((MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0)).unregisterMBean(oname);
			} catch (Exception e) {
				logger.error("Impossible to register the Sip Application dispatcher in domain" + domain, e);
			}
		}		
		if(logger.isInfoEnabled()) {
			logger.info("SipApplicationDispatcher Stopped");
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
		// Issue 1417 http://code.google.com/p/mobicents/issues/detail?id=1417
		// Deploy 2 applications with the same app-name should fail
		SipContext app = applicationDeployed.get(sipApplicationName);
		if(app != null) {
			logger.error("An application with the app name " + sipApplicationName + " is already deployed under the following context " + app.getPath());
			throw new IllegalStateException("An application with the app name " + sipApplicationName + " is already deployed under the following context " + app.getPath());
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
		statusLock.lock();
		try {
			if(started) {
				sipApplication.notifySipContextListeners(new SipContextEventImpl(SipContextEventType.SERVLET_INITIALIZED, null));
			}
		} finally {
			statusLock.unlock();
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
			sipContext.getSipManager().removeAllSessions();
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
		if(event instanceof IOExceptionEventExt  && ((IOExceptionEventExt)event).getReason() == gov.nist.javax.sip.IOExceptionEventExt.Reason.KeepAliveTimeout) {
			IOExceptionEventExt keepAliveTimeout = ((IOExceptionEventExt)event);
			
			SipConnector connector = findSipConnector(
	                keepAliveTimeout.getLocalHost(),
	                keepAliveTimeout.getLocalPort(),
	                keepAliveTimeout.getTransport());								
			
			if(connector != null) {
		        for (SipContext sipContext : applicationDeployed.values()) {
		        	final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();	
		        	final ClassLoader cl = sipContext.getSipContextClassLoader();
					Thread.currentThread().setContextClassLoader(cl);
					try {	
			            for (SipConnectorListener connectorListener : sipContext.getListeners().getSipConnectorListeners()) {
			            	try {	
				                connectorListener.onKeepAliveTimeout(connector,
				                        keepAliveTimeout.getPeerHost(),
				                        keepAliveTimeout.getPeerPort());
			            	} catch (Throwable t) {
								logger.error("SipErrorListener threw exception", t);
							}
			            }
					} finally {				
						Thread.currentThread().setContextClassLoader(oldClassLoader);
					}
		        }
//		        return;
			}
		}
		if(dnsServerLocator != null && event.getSource() instanceof ClientTransaction) {			
			ClientTransaction ioExceptionTx = (ClientTransaction) event.getSource();
			if(ioExceptionTx.getApplicationData() != null) {
				SipServletMessageImpl sipServletMessageImpl = ((TransactionApplicationData)ioExceptionTx.getApplicationData()).getSipServletMessage();
				if(sipServletMessageImpl != null && sipServletMessageImpl instanceof SipServletRequestImpl) {
					if(logger.isDebugEnabled()) {
						logger.debug("An IOException occured on " + event.getHost() + ":" + event.getPort() + "/" + event.getTransport() + " for source " + event.getSource() + ", trying to visit next hop as per RFC3263");
					}
					if(((SipServletRequestImpl)sipServletMessageImpl).visitNextHop()) {
						return;
					}
				}
			}
		} 
		logger.error("An IOException occured on " + event.getHost() + ":" + event.getPort() + "/" + event.getTransport() + " for source " + event.getSource());
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
			if(numberOfMessagesInQueue  < backToNormalQueueSize) {
				String message = "Number of pending messages in the queues : " + numberOfMessagesInQueue + " < to the back to normal queue Size : " + backToNormalQueueSize;
				logger.warn(message + " => stopping to reject requests");
				rejectSipMessages = false;
				final CongestionControlEvent congestionControlEvent = new CongestionControlEvent(
						org.mobicents.javax.servlet.CongestionControlEvent.Reason.Queue, message);		
				callbackCongestionControlListener(rejectSipMessages, congestionControlEvent);
			}
		} else {
			if(numberOfMessagesInQueue > queueSize) {
				String message = "Number of pending messages in the queues : " + numberOfMessagesInQueue + " > to the queue Size : " + queueSize;
				logger.warn(message + " => starting to reject requests");
				rejectSipMessages = true;
				final CongestionControlEvent congestionControlEvent = new CongestionControlEvent(
						org.mobicents.javax.servlet.CongestionControlEvent.Reason.Queue, message);		
				callbackCongestionControlListener(rejectSipMessages, congestionControlEvent);
			}
		}
	}
	
	private void analyzeMemory() {
		Runtime runtime = Runtime.getRuntime();  
		   
		double allocatedMemory = runtime.totalMemory() / (double) 1024;  
		double freeMemory = runtime.freeMemory() / (double) 1024;
		
		double totalFreeMemory = freeMemory + (maxMemory - allocatedMemory);
		this.percentageOfMemoryUsed=  (((double)100) - ((totalFreeMemory / maxMemory) * ((double)100)));

		if(memoryToHigh) {
			if(percentageOfMemoryUsed < backToNormalMemoryThreshold) {
				String message = "Memory used: " + percentageOfMemoryUsed + "% < to the back to normal memory threshold : " + backToNormalMemoryThreshold+ "%";
				logger.warn(message + " => stopping to reject requests");
				memoryToHigh = false;		
				final CongestionControlEvent congestionControlEvent = new CongestionControlEvent(
						org.mobicents.javax.servlet.CongestionControlEvent.Reason.Memory, message);		
				callbackCongestionControlListener(memoryToHigh, congestionControlEvent);			
			}
		} else {
			if(percentageOfMemoryUsed > memoryThreshold) {
				String message = "Memory used: " + percentageOfMemoryUsed + "% > to the memory threshold : " + memoryThreshold+ "%";
				logger.warn(message + " => starting to reject requests");
				memoryToHigh = true;
				final CongestionControlEvent congestionControlEvent = new CongestionControlEvent(
						org.mobicents.javax.servlet.CongestionControlEvent.Reason.Memory, message);		
				callbackCongestionControlListener(memoryToHigh, congestionControlEvent);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestEvent) {			
		final SipProvider sipProvider = (SipProvider)requestEvent.getSource();
		ServerTransaction requestTransaction =  requestEvent.getServerTransaction();
		final Dialog dialog = requestEvent.getDialog();
		final Request request = requestEvent.getRequest();
		// self routing makes the application data cloned, so we make sure to nullify it
		((MessageExt)request).setApplicationData(null);
		final String requestMethod = request.getMethod();
		
		final RouteHeader routeHeader = (RouteHeader) request
				.getHeader(RouteHeader.NAME);
		
		// congestion control is done here only if we drop messages to avoid generating STX 
		if(CongestionControlPolicy.DropMessage.equals(congestionControlPolicy) && controlCongestion(request, null, dialog, routeHeader, sipProvider)) {
			return;
		}
		
		if((rejectSipMessages || memoryToHigh) && CongestionControlPolicy.DropMessage.equals(congestionControlPolicy)) {
			String method = requestEvent.getRequest().getMethod();
			boolean goodMethod = method.equals(Request.ACK) || method.equals(Request.PRACK) || method.equals(Request.BYE) || method.equals(Request.CANCEL) || method.equals(Request.UPDATE) || method.equals(Request.INFO);
			if(logger.isDebugEnabled()) {
				logger.debug("congestion control good method " + goodMethod + ", dialog "  + dialog + " routeHeader " + routeHeader);
			}
			if(!goodMethod) {
				if(dialog == null && (routeHeader == null || ((Parameters)routeHeader.getAddress().getURI()).getParameter(MessageDispatcher.RR_PARAM_PROXY_APP) == null)) { 
					logger.error("dropping request, memory is too high or too many messages present in queues");
					return;
				}
			}
		}	
		
		try {
			if(logger.isDebugEnabled()) {
				logger.debug("sipApplicationDispatcher " + this + ", Got a request event "  + request.toString());
			}			
			if (!Request.ACK.equals(requestMethod) && requestTransaction == null ) {
				try {
					//folsson fix : Workaround broken Cisco 7940/7912
				    if(request.getHeader(MaxForwardsHeader.NAME) == null){
					    request.setHeader(SipFactoryImpl.headerFactory.createMaxForwardsHeader(70));
					}
				    requestTransaction = sipProvider.getNewServerTransaction(request);
				    JainSipUtils.setTransactionTimers(((TransactionExt)requestTransaction), this);				    
				} catch ( TransactionUnavailableException tae) {
					logger.error("cannot get a new Server transaction for this request " + request, tae);
					// Sends a 500 Internal server error and stops processing.				
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, requestTransaction, request, sipProvider);				
	                return;
				} catch ( TransactionAlreadyExistsException taex ) {
					// This is a retransmission so just return.
					return;				
				} 
			} 	
			final ServerTransaction transaction = requestTransaction;
			
			if(logger.isDebugEnabled()) {
				logger.debug("ServerTx ref "  + transaction);				
				logger.debug("Dialog ref "  + dialog);				
			}
			
			final SipServletRequestImpl sipServletRequest = (SipServletRequestImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletRequest(
						request,
						null,
						transaction,
						dialog,
						JainSipUtils.DIALOG_CREATING_METHODS.contains(requestMethod));			
			updateRequestStatistics(request);
			// Check if the request is meant for me. If so, strip the topmost
			// Route header.
			
			//Popping the router header if it's for the container as
			//specified in JSR 289 - Section 15.8
			if(!isRouteExternal(routeHeader)) {
				request.removeFirst(RouteHeader.NAME);
				sipServletRequest.setPoppedRoute(routeHeader);
				final Parameters poppedAddress = (Parameters)routeHeader.getAddress().getURI();
				if(poppedAddress.getParameter(MessageDispatcher.RR_PARAM_PROXY_APP) != null || 
						// Issue 2850 :	Use Request-URI custom Mobicents parameters to route request for misbehaving agents, workaround for Cisco-SIPGateway/IOS-12.x user agent 
						(request.getRequestURI() instanceof javax.sip.address.SipURI && ((Parameters)request.getRequestURI()).getParameter(MessageDispatcher.RR_PARAM_PROXY_APP) != null)) {
					if(logger.isDebugEnabled()) {
						logger.debug("the request is for a proxy application, thus it is a subsequent request ");
					}
					sipServletRequest.setRoutingState(RoutingState.SUBSEQUENT);
				}
				if(transaction != null) {
					TransactionApplicationData transactionApplicationData = (TransactionApplicationData)transaction.getApplicationData();
					if(transactionApplicationData != null && transactionApplicationData.getInitialPoppedRoute() == null) {				
						transactionApplicationData.setInitialPoppedRoute(new AddressImpl(routeHeader.getAddress(), null, ModifiableRule.NotModifiable));
					}
				}
			}							
			if(logger.isDebugEnabled()) {
				logger.debug("Routing State " + sipServletRequest.getRoutingState());
			}
			
			try {
				// congestion control is done here so that the STX is created and a response can be generated back
				// and that 
				if(controlCongestion(request, sipServletRequest, dialog, routeHeader, sipProvider)) {
					return;
				}
				messageDispatcherFactory.getRequestDispatcher(sipServletRequest, this).
					dispatchMessage(sipProvider, sipServletRequest);
			} catch (DispatcherException e) {
				logger.error("Unexpected exception while processing request " + request,e);
				// Sends an error response if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
				if(!Request.ACK.equalsIgnoreCase(requestMethod)) {
					MessageDispatcher.sendErrorResponse(e.getErrorCode(), sipServletRequest, sipProvider);
				}
				return;
			} catch (Throwable e) {
				logger.error("Unexpected exception while processing request " + request,e);
				// Sends a 500 Internal server error if the subsequent request is not an ACK (otherwise it violates RF3261) and stops processing.				
				if(!Request.ACK.equalsIgnoreCase(requestMethod)) {
					MessageDispatcher.sendErrorResponse(Response.SERVER_INTERNAL_ERROR, sipServletRequest, sipProvider);
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
	
	private void callbackCongestionControlListener(boolean triggered, CongestionControlEvent congestionControlEvent) {
		for (SipContext sipContext : applicationDeployed.values()) {
			final ContainerListener containerListener = 
				sipContext.getListeners().getContainerListener();
								
			if(containerListener != null) {
				final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();						
				try {				
					final ClassLoader cl = sipContext.getSipContextClassLoader();
					Thread.currentThread().setContextClassLoader(cl);
											
					try {				
						if(triggered) {
							containerListener.onCongestionControlStarted(congestionControlEvent);
						} else {
							containerListener.onCongestionControlStopped(congestionControlEvent);
						}
					} catch (Throwable t) {
						logger.error("ContainerListener threw exception", t);
					}
				} finally {				
					Thread.currentThread().setContextClassLoader(oldClassLoader);
				}
			}
		}
	}

	private boolean controlCongestion(Request request, SipServletRequestImpl sipServletRequest, Dialog dialog, RouteHeader routeHeader, SipProvider sipProvider) {
		if(rejectSipMessages || memoryToHigh) {
			String method = request.getMethod();
			boolean goodMethod = method.equals(Request.ACK) || method.equals(Request.PRACK) || method.equals(Request.BYE) || method.equals(Request.CANCEL) || method.equals(Request.UPDATE) || method.equals(Request.INFO);
			if(logger.isDebugEnabled()) {
				logger.debug("congestion control good method " + goodMethod + ", dialog "  + dialog + " routeHeader " + routeHeader);
			}
			if(!goodMethod) {
				if(dialog == null && (routeHeader == null || ((Parameters)routeHeader.getAddress().getURI()).getParameter(MessageDispatcher.RR_PARAM_PROXY_APP) == null)) {
					if(CongestionControlPolicy.DropMessage.equals(congestionControlPolicy)) {
						logger.error("dropping request, memory is too high or too many messages present in queues");
						return true;
					}
					SipServletResponse sipServletResponse = null;
					String message = null;
					if(rejectSipMessages) {
						message = "Number of pending messages in the queues : " + numberOfMessagesInQueue + " > to the queue Size : " + queueSize;
					} else if (memoryToHigh) {
						message = "Memory used: " + percentageOfMemoryUsed + "% > to the memory threshold : " + memoryThreshold + "%";
					}
					final CongestionControlEvent congestionControlEvent = new CongestionControlEvent(
							org.mobicents.javax.servlet.CongestionControlEvent.Reason.Memory, message);
					
					for (SipContext sipContext : applicationDeployed.values()) {
						final ContainerListener containerListener = 
							sipContext.getListeners().getContainerListener();
						
						if(containerListener != null) {
							final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();						
							try {				
								final ClassLoader cl = sipContext.getSipContextClassLoader();
								Thread.currentThread().setContextClassLoader(cl);
															
								try {				
									sipServletResponse = containerListener.onRequestThrottled(sipServletRequest, congestionControlEvent);
								} catch (Throwable t) {
									logger.error("ContainerListener threw exception", t);
								}
							} finally {				
								Thread.currentThread().setContextClassLoader(oldClassLoader);
							}
							
							if(sipServletResponse != null) {
								// container listener generated a response, we send it
								try{
									((ServerTransaction)sipServletRequest.getTransaction()).sendResponse(((SipServletResponseImpl)sipServletResponse).getResponse());
//									sipServletResponse.send();
								} catch (Exception e) {
									logger.error("Problem while sending the error response " + sipServletResponse + " to the following request "
											+ request.toString(), e);
								}
								return true;
							}
						}
					}
					// no application implements the container listener or the container listener didn't generate any responses so we send back a generic one.
					if(sipServletResponse == null) {
						MessageDispatcher.sendErrorResponse(Response.SERVICE_UNAVAILABLE, (ServerTransaction) sipServletRequest.getTransaction(), request, sipProvider);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * @param requestMethod
	 */
	private void updateRequestStatistics(final Request request) {
		if(gatherStatistics) {
			requestsProcessed.incrementAndGet();
			final String method = request.getMethod();
			final AtomicLong requestsProcessed = requestsProcessedByMethod.get(method);
			if(requestsProcessed == null) {
				requestsProcessedByMethod.put(method, new AtomicLong());
			} else {
				requestsProcessed.incrementAndGet();
			}
		}
	}
	
	/**
	 * @param requestMethod
	 */
	private void updateResponseStatistics(final Response response) {
		if(gatherStatistics) {
			responsesProcessed.incrementAndGet();	
			final int statusCode = response.getStatusCode();
			int statusCodeDiv = statusCode / 100;
			switch (statusCodeDiv) {
				case 1:
					responsesProcessedByStatusCode.get("1XX").incrementAndGet();
					break;
				case 2:
					responsesProcessedByStatusCode.get("2XX").incrementAndGet();
					break;
				case 3:
					responsesProcessedByStatusCode.get("3XX").incrementAndGet();
					break;
				case 4:
					responsesProcessedByStatusCode.get("4XX").incrementAndGet();
					break;
				case 5:
					responsesProcessedByStatusCode.get("5XX").incrementAndGet();
					break;
				case 6:
					responsesProcessedByStatusCode.get("6XX").incrementAndGet();
					break;
				case 7:
					responsesProcessedByStatusCode.get("7XX").incrementAndGet();
					break;
				case 8:
					responsesProcessedByStatusCode.get("8XX").incrementAndGet();
					break;
				case 9:
					responsesProcessedByStatusCode.get("9XX").incrementAndGet();
					break;
			}			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public void processResponse(ResponseEvent responseEvent) {
		final ResponseEventExt responseEventExt = (ResponseEventExt) responseEvent;		
		final Response response = responseEventExt.getResponse();
		
		if(logger.isDebugEnabled()) {
			logger.debug("Response " + response.toString());
		}
		
		final CSeqHeader cSeqHeader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
		//if this is a response to a cancel, the response is dropped
		if(Request.CANCEL.equalsIgnoreCase(cSeqHeader.getMethod())) {
			if(logger.isDebugEnabled()) {
				logger.debug("the response is dropped accordingly to JSR 289 " +
						"since this a response to a CANCEL");
			}
			return;
		}
		// self routing makes the application data cloned, so we make sure to nullify it
		((MessageExt)response).setApplicationData(null);
		
		updateResponseStatistics(response);
		ClientTransaction clientTransaction = responseEventExt.getClientTransaction();		
		final Dialog dialog = responseEventExt.getDialog();
		final boolean isForkedResponse = responseEventExt.isForkedResponse();
		final boolean isRetransmission = responseEventExt.isRetransmission();
		final ClientTransactionExt originalTransaction = responseEventExt.getOriginalTransaction();
		if(logger.isDebugEnabled()) {
			logger.debug("is Forked Response " + isForkedResponse);
			logger.debug("is Retransmission " + isRetransmission);
			logger.debug("Client Transaction " + clientTransaction);
			logger.debug("Original Transaction " + originalTransaction);
			logger.debug("Dialog " + dialog);
		}
		// Issue 1468 : Handling forking 
		if(isForkedResponse && originalTransaction != null && !responseEventExt.isRetransmission()) {
			final Dialog defaultDialog = originalTransaction.getDefaultDialog();
			final Dialog orginalTransactionDialog = originalTransaction.getDialog();
			if(logger.isDebugEnabled()) {				
				logger.debug("Original Transaction Dialog " + orginalTransactionDialog);
				logger.debug("Original Transaction Default Dialog " + defaultDialog);
			}
			clientTransaction = originalTransaction;			
		}
		
		// Transate the response to SipServletResponse
		final SipServletResponseImpl sipServletResponse = (SipServletResponseImpl) sipFactoryImpl.getMobicentsSipServletMessageFactory().createSipServletResponse(				
				response, 
				clientTransaction, 
				null, 
				dialog,
				true,
				isRetransmission);
		try {		
			messageDispatcherFactory.getResponseDispatcher(sipServletResponse, this).
				dispatchMessage(((SipProvider)responseEvent.getSource()), sipServletResponse);
		} catch (Throwable e) {
			logger.error("An unexpected exception happened while routing the response " +  sipServletResponse, e);
			return;
		}
	}	

	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
	 */
	public void processDialogTerminated(final DialogTerminatedEvent dialogTerminatedEvent) {
		final Dialog dialog = dialogTerminatedEvent.getDialog();		
		if(logger.isDebugEnabled()) {
			logger.debug("Dialog Terminated => dialog Id : " + dialogTerminatedEvent.getDialog().getDialogId());
		}
		
		getAsynchronousExecutor().execute(new Runnable() {
			public void run() {			
				try {
					boolean appDataFound = false;
					TransactionApplicationData dialogAppData = (TransactionApplicationData) dialog.getApplicationData();
					TransactionApplicationData txAppData = null; 
					if(dialogAppData != null) {						
						if(dialogAppData.getSipServletMessage() == null) {
							Transaction transaction = dialogAppData.getTransaction();
							if(transaction != null && transaction.getApplicationData() != null) {
								txAppData = (TransactionApplicationData) transaction.getApplicationData();
								txAppData.cleanUp();
							}
						} else {
							SipServletMessageImpl sipServletMessageImpl = dialogAppData.getSipServletMessage();
							MobicentsSipSessionKey sipSessionKey = sipServletMessageImpl.getSipSessionKey();
							tryToInvalidateSession(sipSessionKey, false);				
						}
						dialogAppData.cleanUp();
						// since the stack doesn't nullify the app data, we need to do it to let go of the refs					
						dialog.setApplicationData(null);
					}		
					if(!appDataFound && logger.isDebugEnabled()) {
						logger.debug("no application data for this dialog " + dialog.getDialogId());
					}
				} catch (Exception e) {
					logger.error("Problem handling dialog termination", e);
				}
			}
		});

	}

	/**
	 * @param sipSessionImpl
	 */
	private void tryToInvalidateSession(MobicentsSipSessionKey sipSessionKey, boolean invalidateProxySession) {
		//the key can be null if the application already invalidated the session
		if(sipSessionKey != null) {
			SipContext sipContext = findSipApplication(sipSessionKey.getApplicationName());
			//the context can be null if the server is being shutdown
			if(sipContext != null) {
				final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
				try {
					final ClassLoader cl = sipContext.getSipContextClassLoader();
					Thread.currentThread().setContextClassLoader(cl);
					final SipManager sipManager = sipContext.getSipManager();					
					final SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
							sipSessionKey.getApplicationName(), 
							sipSessionKey.getApplicationSessionId());
					
					MobicentsSipSession sipSessionImpl = null;
					MobicentsSipApplicationSession sipApplicationSession = null;
					if(sipManager instanceof DistributableSipManager) {
						// we check only locally if the sessions are present, no need to check in the cache since
						// what triggered this method call was either a transaction or a dialog terminating or timeout
						// so the sessions should be present locally, if they are not it means that it has already been invalidated
						// perf optimization and fix for Issue 1688 : MSS HA on AS5 : Version is null Exception occurs sometimes
						DistributableSipManager distributableSipManager = (DistributableSipManager) sipManager;
						sipApplicationSession = distributableSipManager.getSipApplicationSession(sipApplicationSessionKey, false, true);
						sipSessionImpl = distributableSipManager.getSipSession(sipSessionKey, false, sipFactoryImpl, sipApplicationSession, true);						
					} else {						
						sipApplicationSession = sipManager.getSipApplicationSession(sipApplicationSessionKey, false);
						sipSessionImpl = sipManager.getSipSession(sipSessionKey, false, sipFactoryImpl, sipApplicationSession);
					}									
															
					if(sipSessionImpl != null) {
						final MobicentsProxy proxy = sipSessionImpl.getProxy();
						if(!invalidateProxySession && 
								(proxy == null || (proxy != null && 
										((proxy.getFinalBranchForSubsequentRequests() != null && !proxy.getFinalBranchForSubsequentRequests().getRecordRoute()) ||
										proxy.isTerminationSent()))))  {
							if(logger.isDebugEnabled()) {
								logger.debug("try to Invalidate Proxy session if it is non record routing or termination has been sent " + sipSessionKey);
							}
							invalidateProxySession = true;
						}
						// If this is a client transaction no need to invalidate proxy session http://code.google.com/p/mobicents/issues/detail?id=1024
						if(!invalidateProxySession) {
							if(logger.isDebugEnabled()) {
								logger.debug("don't Invalidate Proxy session");
							}
							return;
						} 
						boolean batchStarted = false;
						try {
							sipContext.enterSipApp(sipApplicationSession, sipSessionImpl, false);
							batchStarted = sipContext.enterSipAppHa(true);
							if(logger.isDebugEnabled()) {
								logger.debug("sip session " + sipSessionKey + " is valid ? :" + sipSessionImpl.isValidInternal());
								if(sipSessionImpl.isValidInternal()) {
									logger.debug("Sip session " + sipSessionKey + " is ready to be invalidated ? :" + sipSessionImpl.isReadyToInvalidate());
								}
							}
							if(sipSessionImpl.isValidInternal() && sipSessionImpl.isReadyToInvalidate()) {														
								sipSessionImpl.onTerminatedState();							
							}
						} finally {
							sipContext.exitSipAppHa(null, null, batchStarted);
							sipContext.exitSipApp(sipApplicationSession, sipSessionImpl);
						}
					} else {
						if(logger.isDebugEnabled()) {
							logger.debug("sip session already invalidated" + sipSessionKey);
						}
					}															
					if(sipApplicationSession != null) {												
						try {
							sipContext.enterSipApp(sipApplicationSession, null, false);
							if(logger.isDebugEnabled()) {
								logger.debug("sip app session " + sipApplicationSession.getKey() + " is valid ? :" + sipApplicationSession.isValidInternal());
								if(sipApplicationSession.isValidInternal()) {
									logger.debug("Sip app session " + sipApplicationSession.getKey() + " is ready to be invalidated ? :" + sipApplicationSession.isReadyToInvalidate());
								}
							}												
							if(sipApplicationSession.isValidInternal() && sipApplicationSession.isReadyToInvalidate()) {								
								sipApplicationSession.tryToInvalidate();
							}
						} finally {
							sipContext.exitSipApp(sipApplicationSession, null);
						}							
					}

				} finally {
					Thread.currentThread().setContextClassLoader(oldClassLoader);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see gov.nist.javax.sip.SipListenerExt#processDialogTimeout(gov.nist.javax.sip.DialogTimeoutEvent)
	 */
	public void processDialogTimeout(final DialogTimeoutEvent timeoutEvent) {
		final Dialog dialog = timeoutEvent.getDialog();
		if(logger.isDebugEnabled()) {
			logger.info("dialog timeout " + dialog + " reason => " + timeoutEvent.getReason());
		}	
		if(timeoutEvent.getReason() == Reason.AckNotReceived) {
			final TransactionApplicationData tad = (TransactionApplicationData) dialog.getApplicationData();
			if(tad != null && tad.getSipServletMessage() != null) {
				getAsynchronousExecutor().execute(new Runnable() {
					public void run() {			
						if(logger.isDebugEnabled()) {
							logger.info("Running process dialog timeout " + dialog + " reason => " + timeoutEvent.getReason());
						}	
						try {
							final SipServletMessageImpl sipServletMessage = tad.getSipServletMessage();
							final MobicentsSipSessionKey sipSessionKey = sipServletMessage.getSipSessionKey();
							final MobicentsSipSession sipSession = sipServletMessage.getSipSession();
							if(sipSession != null) {
								SipContext sipContext = findSipApplication(sipSessionKey.getApplicationName());					
								//the context can be null if the server is being shutdown
								if(sipContext != null) {
									MobicentsSipApplicationSession sipApplicationSession = sipSession.getSipApplicationSession();
									try {
										sipContext.enterSipApp(sipApplicationSession, sipSession, false);
										checkForAckNotReceived(sipServletMessage);
										checkForPrackNotReceived(sipServletMessage);
									} finally {
										sipContext.exitSipApp(sipApplicationSession, sipSession);
									}
									// Issue 1822 http://code.google.com/p/mobicents/issues/detail?id=1822
									// don't delete the dialog so that the app can send the BYE even after the noAckReceived has been called
									//								dialog.delete();
									tryToInvalidateSession(sipSessionKey, false);						
								}					
							}
							tad.cleanUp();	
							dialog.setApplicationData(null);
						} catch (Exception e) {
							logger.error("Problem handling dialog timeout", e);
						}
					}
				});
			} else{
				dialog.setApplicationData(null);
			}
		} else {
			dialog.setApplicationData(null);
		}
	}
	public SipConnector findSipConnector(String ipAddress, int port, String transport){
        MobicentsExtendedListeningPoint extendedListeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(ipAddress, port, transport);
        if(extendedListeningPoint != null) {
        	return extendedListeningPoint.getSipConnector();
        }
        return null;
    }
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(final TimeoutEvent timeoutEvent) {
		Transaction eventTransaction = null;
		if(timeoutEvent.isServerTransaction()) {
			eventTransaction = timeoutEvent.getServerTransaction();
		} else {
			eventTransaction = timeoutEvent.getClientTransaction();
		}
		final Transaction transaction = eventTransaction;
		if(logger.isDebugEnabled()) {
			logger.debug("transaction " + transaction + " timed out => " + transaction.getRequest().toString());
		}

		final TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
		if(tad != null && tad.getSipServletMessage() != null) {

			getAsynchronousExecutor().execute(new Runnable() {
				public void run() {
					try {
						if(logger.isDebugEnabled()) {
							logger.debug("transaction " + transaction + " timed out => " + transaction.getRequest().toString());
						}
						SipServletMessageImpl sipServletMessage = tad.getSipServletMessage();
						MobicentsSipSessionKey sipSessionKey = sipServletMessage.getSipSessionKey();
						MobicentsSipSession sipSession = sipServletMessage.getSipSession();					
						boolean appNotifiedOfPrackNotReceived = false;
						// session can be null if a message was sent outside of the container by the container itself during Initial request dispatching
						// but the external host doesn't send any response so we call out to the application only if the session is not null					
						if(sipSession != null) {
							SipContext sipContext = findSipApplication(sipSessionKey.getApplicationName());					
							//the context can be null if the server is being shutdown
							if(sipContext != null) {
								MobicentsSipApplicationSession sipApplicationSession = sipSession.getSipApplicationSession();
								try {
									sipContext.enterSipApp(sipApplicationSession, sipSession, false);
									MobicentsB2BUAHelper b2buaHelperImpl = sipSession.getB2buaHelper();

									if(b2buaHelperImpl != null && tad.getSipServletMessage() instanceof SipServletRequestImpl) {
										b2buaHelperImpl.unlinkOriginalRequestInternal((SipServletRequestImpl)tad.getSipServletMessage(), false);
									}
									// naoki : Fix for Issue 1618 http://code.google.com/p/mobicents/issues/detail?id=1618 on Timeout don't do the 408 processing for Server Transactions
									if(sipServletMessage instanceof SipServletRequestImpl && !timeoutEvent.isServerTransaction()) {
										try {
											ProxyBranchImpl proxyBranchImpl = tad.getProxyBranch();
											if(proxyBranchImpl != null) {
												ProxyImpl proxy = (ProxyImpl) proxyBranchImpl.getProxy();
												if(proxy.getFinalBranchForSubsequentRequests() != null) {
													tad.cleanUp();				
													transaction.setApplicationData(null);
													return;
												}
											}
											SipServletRequestImpl sipServletRequestImpl = (SipServletRequestImpl) sipServletMessage;
											if(sipServletRequestImpl.visitNextHop()) {
												return;
											}
											sipServletMessage.setTransaction(transaction);
											SipServletResponseImpl response = (SipServletResponseImpl) sipServletRequestImpl.createResponse(408, null, false);
											// Fix for Issue 1734
											sipServletRequestImpl.setResponse(response);

											MessageDispatcher.callServlet(response);
											if(tad.getProxyBranch() != null) {
												tad.getProxyBranch().setResponse(response);
												tad.getProxyBranch().onResponse(response, response.getStatus());
											}
											sipSession.updateStateOnResponse(response, true);
										} catch (Throwable t) {
											logger.error("Failed to deliver 408 response on transaction timeout" + transaction, t);
										}
									}
									// Guard only invite tx should check that, otherwise proxy might become null http://code.google.com/p/mobicents/issues/detail?id=2350
									// Should check only Server Tx for ack or prack not received http://code.google.com/p/mobicents/issues/detail?id=2525
									if(Request.INVITE.equals(sipServletMessage.getMethod()) && timeoutEvent.isServerTransaction()) {
										checkForAckNotReceived(sipServletMessage);
										appNotifiedOfPrackNotReceived = checkForPrackNotReceived(sipServletMessage);
									}
								} finally {
									sipSession.removeOngoingTransaction(transaction);
									sipSession.setRequestsPending(0);
									sipContext.exitSipApp(sipApplicationSession, sipSession);
								}
								// don't invalidate here because if the application sends a final response on the noPrack received
								// the ACK to this final response won't be able to get routed since the sip session would have been invalidated
								if(!appNotifiedOfPrackNotReceived) {
									tryToInvalidateSession(sipSessionKey, false);
								}
							}
						}
						// don't clean up for the same reason we don't invalidate the sip session right above
						tad.cleanUp();				
						transaction.setApplicationData(null);
					} catch (Exception e) {
						logger.error("Problem handling timeout", e);
					}
				}
			});
		}
	}
	
	private boolean checkForAckNotReceived(SipServletMessageImpl sipServletMessage) {
		//notifying SipErrorListener that no ACK has been received for a UAS only
		final MobicentsSipSession sipSession = sipServletMessage.getSipSession();
		final SipServletResponseImpl lastFinalResponse = (SipServletResponseImpl)
			((SipServletRequestImpl)sipServletMessage).getLastFinalResponse();
		final MobicentsProxy proxy = sipSession.getProxy();
		if(logger.isDebugEnabled()) {
			logger.debug("checkForAckNotReceived : request " + sipServletMessage + " last Final Response " + lastFinalResponse);
		}		
		boolean notifiedApplication = false;
		if(sipServletMessage instanceof SipServletRequestImpl && Request.INVITE.equals(sipServletMessage.getMethod()) &&
				proxy == null &&
				lastFinalResponse != null) {
			final SipContext sipContext = sipSession.getSipApplicationSession().getSipContext();
			final List<SipErrorListener> sipErrorListeners = 
				sipContext.getListeners().getSipErrorListeners();
									
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();						
			try {				
				final ClassLoader cl = sipContext.getSipContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				
				final SipErrorEvent sipErrorEvent = new SipErrorEvent(
						(SipServletRequest)sipServletMessage, 
						lastFinalResponse);
				
				for (SipErrorListener sipErrorListener : sipErrorListeners) {
					try {				
						notifiedApplication = true;
						sipErrorListener.noAckReceived(sipErrorEvent);
					} catch (Throwable t) {
						logger.error("SipErrorListener threw exception", t);
					}
				}
			} finally {				
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}
			final Dialog dialog = sipSession.getSessionCreatingDialog();
			if(!notifiedApplication && sipSession.getProxy() == null &&
					// Fix for http://code.google.com/p/mobicents/issues/detail?id=2525 BYE is being sent to a not yet established dialog
					dialog != null && dialog.getState() != null && !dialog.getState().equals(DialogState.TERMINATED)) {
				// Issue 1822 http://code.google.com/p/mobicents/issues/detail?id=1822
				// RFC 3261 Section 13.3.1.4 The INVITE is Accepted
				// "If the server retransmits the 2xx response for 64*T1 seconds without receiving an ACK, 
				// the dialog is confirmed, but the session SHOULD be terminated.  
				// This is accomplished with a BYE, as described in Section 15."
				SipServletRequest bye = sipSession.createRequest(Request.BYE);
				if(logger.isDebugEnabled()) {
					logger.debug("no applications called for ACK not received, sending BYE " + bye);
				}
				try {
					bye.send();
				} catch (IOException e) {
					logger.error("Couldn't send the BYE " + bye, e);
				}
			}
		}
		return notifiedApplication;
	}
	
	private boolean checkForPrackNotReceived(SipServletMessageImpl sipServletMessage) {
		//notifying SipErrorListener that no ACK has been received for a UAS only
		final MobicentsSipSession sipSession = sipServletMessage.getSipSession();
		SipServletResponseImpl lastInfoResponse = (SipServletResponseImpl)
			((SipServletRequestImpl)sipServletMessage).getLastInformationalResponse();
		final MobicentsProxy proxy = sipSession.getProxy();
		if(logger.isDebugEnabled()) {
			logger.debug("checkForPrackNotReceived : last Informational Response " + lastInfoResponse);
		}
		boolean notifiedApplication = false;
		if(sipServletMessage instanceof SipServletRequestImpl && Request.INVITE.equals(sipServletMessage.getMethod()) &&
				proxy == null &&
				 lastInfoResponse != null) {
			final SipContext sipContext = sipSession.getSipApplicationSession().getSipContext();
			final List<SipErrorListener> sipErrorListeners = 
				sipContext.getListeners().getSipErrorListeners();
			
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			try {				
				final ClassLoader cl = sipContext.getSipContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				
				final SipErrorEvent sipErrorEvent = new SipErrorEvent(
						(SipServletRequest)sipServletMessage, 
						lastInfoResponse);
				for (SipErrorListener sipErrorListener : sipErrorListeners) {
					try {				
						notifiedApplication = true;
						sipErrorListener.noPrackReceived(sipErrorEvent);
					} catch (Throwable t) {
						logger.error("SipErrorListener threw exception", t);
					}
				}
			} finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}			
		}
		return notifiedApplication;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
	 */
	public void processTransactionTerminated(final TransactionTerminatedEvent transactionTerminatedEvent) {
		Transaction eventTransaction = null;
		if(transactionTerminatedEvent.isServerTransaction()) {
			eventTransaction = transactionTerminatedEvent.getServerTransaction();
		} else {
			eventTransaction = transactionTerminatedEvent.getClientTransaction();
		}
		final Transaction transaction = eventTransaction;
		if(logger.isDebugEnabled()) {
			logger.info("transaction " + transaction + " terminated => " + transaction.getRequest().toString());
		}		
		
		final TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
		if(tad != null && tad.getSipServletMessage() != null) {
			getAsynchronousExecutor().execute(new Runnable() {
				public void run() {
					try {
						SipServletMessageImpl sipServletMessageImpl = tad.getSipServletMessage();
						MobicentsSipSessionKey sipSessionKey = sipServletMessageImpl.getSipSessionKey();
						MobicentsSipSession sipSession = sipServletMessageImpl.getSipSession();
						MobicentsB2BUAHelper b2buaHelperImpl = null;
						if(sipSession != null) {
							b2buaHelperImpl = sipSession.getB2buaHelper();
						}
						if(sipSessionKey == null) {
							if(logger.isDebugEnabled()) {
								logger.debug("no sip session were returned for this key " + sipServletMessageImpl.getSipSessionKey() + " and message " + sipServletMessageImpl);
							}
						}

						if(tad.getProxyBranch() != null) {
							tad.getProxyBranch().removeTransaction(transaction.getBranchId());
						}

						// Issue 1333 : B2buaHelper.getPendingMessages(linkedSession, UAMode.UAC) returns empty list
						// don't remove the transaction on terminated state for INVITE Tx because it won't be possible
						// to create the ACK on second leg for B2BUA apps
						if(sipSession != null) {
							boolean removeTx = true;
							if(b2buaHelperImpl != null && transaction instanceof ClientTransaction && Request.INVITE.equals(sipServletMessageImpl.getMethod())) {
								removeTx = false;
							}							
							SipContext sipContext = findSipApplication(sipSessionKey.getApplicationName());					
							//the context can be null if the server is being shutdown
							if(sipContext != null) {
								MobicentsSipApplicationSession sipApplicationSession = sipSession.getSipApplicationSession();
								try {
									sipContext.enterSipApp(sipApplicationSession, sipSession, false);
									if(b2buaHelperImpl != null && tad.getSipServletMessage() instanceof SipServletRequestImpl) {
										b2buaHelperImpl.unlinkOriginalRequestInternal((SipServletRequestImpl)tad.getSipServletMessage(), false);
									}
									if(removeTx) {
										sipSession.removeOngoingTransaction(transaction);
										tad.cleanUp();
										// Issue 1468 : to handle forking, we shouldn't cleanup the app data since it is needed for the forked responses
										boolean nullifyAppData = true;					
										if(((SipStackImpl)((SipProvider)transactionTerminatedEvent.getSource()).getSipStack()).getMaxForkTime() > 0 && Request.INVITE.equals(sipServletMessageImpl.getMethod())) {
											nullifyAppData = false;
										}
										if(nullifyAppData) {
											transaction.setApplicationData(null);
										}
									} else {
										if(logger.isDebugEnabled()) {
											logger.debug("Transaction " + transaction + " not removed from session " + sipSessionKey + " because the B2BUA might still need it to create the ACK");
										}
									}
								} finally {
									sipContext.exitSipApp(sipApplicationSession, sipSession);
								}
							}
							
							// If it is a client transaction, do not kill the proxy session http://code.google.com/p/mobicents/issues/detail?id=1024
							tryToInvalidateSession(sipSessionKey, transactionTerminatedEvent.isServerTransaction());				

						}
					} catch (Exception e) {
						logger.error("Problem handling transaction termination", e);
					}
				}
			});
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("TransactionApplicationData not available on the following request " + transaction.getRequest().toString());
			}
			if(tad != null) {
				tad.cleanUp();
			}
			transaction.setApplicationData(null);
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
		MobicentsExtendedListeningPoint listeningPoint = sipNetworkInterfaceManager.findMatchingListeningPoint(host, port, transport);		
		if((hostNames.contains(host) || hostNames.contains(host+":" + port) || listeningPoint != null)) {
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
	public MobicentsSipFactory getSipFactory() {
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
		if(dnsServerLocator != null) {
			dnsServerLocator.addLocalHostName(hostName);
		}
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
		if(dnsServerLocator != null) {
			dnsServerLocator.removeLocalHostName(hostName);
		}
	}

	/**
	 * 
	 */
	public SipApplicationRouterInfo getNextInterestedApplication(
			MobicentsSipServletRequest sipServletRequest) {
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
				final Address routeAddress = SipFactoryImpl.addressFactory.createAddress(routes[0]);
				final RouteHeader applicationRouterInfoRouteHeader = SipFactoryImpl.headerFactory.createRouteHeader(routeAddress);									
				if(isRouteExternal(applicationRouterInfoRouteHeader)) {				
					// push all of the routes on the Route header stack of the request and 
					// send the request externally
					for (int i = routes.length-1 ; i >= 0; i--) {
						RouteHeader routeHeader = (RouteHeader) SipFactoryImpl.headerFactory.createHeader(RouteHeader.NAME, routes[i]);
						URI routeURI = routeHeader.getAddress().getURI();
						if(routeURI.isSipURI()) {
							((javax.sip.address.SipURI)routeURI).setLrParam();
						}
						request.addHeader(routeHeader);
					}				
				}
			} else if (SipRouteModifier.ROUTE_BACK.equals(sipRouteModifier)) {
				// Push container Route, pick up the first outbound interface
				final SipURI sipURI = getOutboundInterfaces().get(0);
				sipURI.setParameter("modifier", "route_back");
				Header routeHeader = SipFactoryImpl.headerFactory.createHeader(RouteHeader.NAME, sipURI.toString());
				request.addHeader(routeHeader);
				// push all of the routes on the Route header stack of the request and 
				// send the request externally
				for (int i = routes.length-1 ; i >= 0; i--) {
					routeHeader = SipFactoryImpl.headerFactory.createHeader(RouteHeader.NAME, routes[i]);
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
//	private Serializable serializeStateInfo(Serializable stateInfo) {
//		ByteArrayOutputStream baos = null;
//		ObjectOutputStream out = null;
//		ByteArrayInputStream bais = null;
//		ObjectInputStream in = null;
//		
//		try{
//			baos = new ByteArrayOutputStream();
//			out = new ObjectOutputStream(baos);
//			out.writeObject(stateInfo);					
//			bais = new ByteArrayInputStream(baos.toByteArray());
//			in =new ObjectInputStream(bais);
//			return (Serializable)in.readObject();			
//		} catch (IOException e) {
//			logger.error("Impossible to serialize the state info", e);
//			return stateInfo;
//		} catch (ClassNotFoundException e) {
//			logger.error("Impossible to serialize the state info", e);
//			return stateInfo;
//		} finally {
//			try {
//				if(out != null) {
//					out.close();
//				}
//				if(in != null) {
//					in.close();
//				}
//				if(baos != null) {
//					baos.close();
//				}
//				if(bais != null) {
//					bais.close();
//				}
//			} catch (IOException e) {
//				logger.error("Impossible to close the streams after serializing state info", e);
//			}
//		}
//	}

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
	
	public DNSServerLocator getDNSServerLocator() {		
		return dnsServerLocator;
	}

	public void setDNSServerLocator(DNSServerLocator dnsServerLocator) {
		this.dnsServerLocator = dnsServerLocator;
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
	
	public Serializable retrieveApplicationRouterConfigurationString() {
		if(this.sipApplicationRouter instanceof ManageableApplicationRouter) {
			ManageableApplicationRouter router = (ManageableApplicationRouter) this.sipApplicationRouter;
			return (Serializable) router.getCurrentConfiguration();
		} else {
			throw new RuntimeException("This application router is not manageable");
		}
	}
	
	public void updateApplicationRouterConfiguration(Serializable configuration) {
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
			logger.info("Container wide Concurrency Control set to " + concurrencyControlMode);
		}
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
		if(logger.isInfoEnabled()) {
			logger.info("Queue Size set to " + queueSize);
		}
	}
	
	public void setConcurrencyControlModeByName(String concurrencyControlMode) {
		this.concurrencyControlMode = ConcurrencyControlMode.valueOf(concurrencyControlMode);
		if(logger.isInfoEnabled()) {
			logger.info("Container wide Concurrency Control set to " + concurrencyControlMode);
		}
	}	

	/**
	 * @return the requestsProcessed
	 */
	public long getRequestsProcessed() {
		return requestsProcessed.get();
	}
	
	/**
	 * @return the requestsProcessedByMethod
	 */
	public Map<String, AtomicLong> getRequestsProcessedByMethod() {		
		return requestsProcessedByMethod;
	}

	/**
	 * @return the responsesProcessedByStatusCode
	 */
	public Map<String, AtomicLong> getResponsesProcessedByStatusCode() {		
		return responsesProcessedByStatusCode;
	}
	
	/**
	 * @return the requestsProcessed
	 */
	public long getRequestsProcessedByMethod(String method) {
		AtomicLong requestsProcessed = requestsProcessedByMethod.get(method);
		if(requestsProcessed != null) {
			return requestsProcessed.get();
		}
		return 0;
	}
	
	public long getResponsesProcessedByStatusCode(String statusCode) {
		AtomicLong responsesProcessed = responsesProcessedByStatusCode.get(statusCode);
		if(responsesProcessed != null) {
			return responsesProcessed.get();
		}
		return 0;
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
		if(congestionControlCheckingInterval != this.congestionControlCheckingInterval) {
			this.congestionControlCheckingInterval = congestionControlCheckingInterval;
			statusLock.lock();
			try {
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
			} finally {
				statusLock.unlock();
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
		if(logger.isInfoEnabled()) {
			logger.info("Congestion Control policy set to " + this.congestionControlPolicy.toString());
		}
	}

	
	public void setCongestionControlPolicyByName(String congestionControlPolicy) {
		this.congestionControlPolicy = CongestionControlPolicy.valueOf(congestionControlPolicy);
		if(logger.isInfoEnabled()) {
			logger.info("Congestion Control policy set to " + this.congestionControlPolicy.toString());
		}
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
		if(logger.isInfoEnabled()) {
			logger.info("Memory threshold set to " + this.memoryThreshold +"%");
		}
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
		if(logger.isInfoEnabled()) {
			logger.info("Bypass Request Executor enabled ?" + this.bypassRequestExecutor);
		}
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
		if(logger.isInfoEnabled()) {
			logger.info("Bypass Response Executor enabled ?" + this.bypassResponseExecutor);
		}
	}

	/**
	 * @return the bypassResponseExecutor
	 */
	public boolean isBypassResponseExecutor() {
		return bypassResponseExecutor;
	}

	/**
	 * @param baseTimerInterval the baseTimerInterval to set
	 */
	public void setBaseTimerInterval(int baseTimerInterval) {
		if(baseTimerInterval < 1) {
			throw new IllegalArgumentException("It's forbidden to set the Base Timer Interval to a non positive value");
		}
		this.baseTimerInterval = baseTimerInterval;
		if(logger.isInfoEnabled()) {
			logger.info("SIP Base Timer Interval set to " + this.baseTimerInterval +"ms");
		}
	}

	/**
	 * @return the baseTimerInterval
	 */
	public int getBaseTimerInterval() {
		return baseTimerInterval;
	}
	
	/**
	 * @param t2Interval the t2Interval to set
	 */
	public void setT2Interval(int t2Interval) {
		if(t2Interval < 1) {
			throw new IllegalArgumentException("It's forbidden to set the SIP Timer T2 Interval to a non positive value");
		}
		this.t2Interval = t2Interval;
		if(logger.isInfoEnabled()) {
			logger.info("SIP Timer T2 Interval set to " + this.t2Interval +"ms");
		}
	}


	/**
	 * @return the t2Interval
	 */
	public int getT2Interval() {
		return t2Interval;
	}


	/**
	 * @param t4Interval the t4Interval to set
	 */
	public void setT4Interval(int t4Interval) {
		if(t4Interval < 1) {
			throw new IllegalArgumentException("It's forbidden to set the SIP Timer T4 Interval to a non positive value");
		}
		this.t4Interval = t4Interval;
		if(logger.isInfoEnabled()) {
			logger.info("SIP Timer T4 Interval set to " + this.t4Interval +"ms");
		}
	}


	/**
	 * @return the t4Interval
	 */
	public int getT4Interval() {
		return t4Interval;
	}


	/**
	 * @param timerDInterval the timerDInterval to set
	 */
	public void setTimerDInterval(int timerDInterval) {
		if(timerDInterval < 1) {
			throw new IllegalArgumentException("It's forbidden to set the SIP Timer TD Interval to a non positive value");
		}
		if(timerDInterval < 32000) {
			throw new IllegalArgumentException("It's forbidden to set the SIP Timer TD Interval to a value lower than 32s");
		}
		this.timerDInterval = timerDInterval;
		if(logger.isInfoEnabled()) {
			logger.info("SIP Timer D Interval set to " + this.timerDInterval +"ms");
		}
	}


	/**
	 * @return the timerDInterval
	 */
	public int getTimerDInterval() {
		return timerDInterval;
	}

	public String[] getExtensionsSupported() {
		return EXTENSIONS_SUPPORTED;
	}

	public String[] getRfcSupported() {
		return RFC_SUPPORTED;
	}

	public void loadBalancerAdded(SipLoadBalancer sipLoadBalancer) {
		sipLoadBalancers.add(sipLoadBalancer);
		if(sipFactoryImpl.getLoadBalancerToUse() == null) {
			sipFactoryImpl.setLoadBalancerToUse(sipLoadBalancer);
		}
	}

	public void loadBalancerRemoved(SipLoadBalancer sipLoadBalancer) {
		sipLoadBalancers.remove(sipLoadBalancer);
		if(sipFactoryImpl.getLoadBalancerToUse() != null && 
				sipFactoryImpl.getLoadBalancerToUse().equals(sipLoadBalancer)) {
			if(sipLoadBalancers.size() > 0) {
				sipFactoryImpl.setLoadBalancerToUse(sipLoadBalancers.iterator().next());
			} else {
				sipFactoryImpl.setLoadBalancerToUse(null);
			}
		}
	}	
	
	/**
	 * @param info
	 */
	public void sendSwitchoverInstruction(String fromJvmRoute, String toJvmRoute) {
		if(logger.isDebugEnabled()) {
			logger.debug("switching over from " + fromJvmRoute + " to " + toJvmRoute);
		}
		if(fromJvmRoute == null || toJvmRoute == null) {
			return;
		}
		for(SipLoadBalancer  sipLoadBalancer : sipLoadBalancers) {
			sipLoadBalancer.switchover(fromJvmRoute, toJvmRoute);			
		}		
	}

	/**
	 * @param skipStatistics the skipStatistics to set
	 */
	public void setGatherStatistics(boolean skipStatistics) {
		this.gatherStatistics = skipStatistics;
		if(logger.isInfoEnabled()) {
			logger.info("Gathering Statistics set to " + skipStatistics);
		}
	}

	/**
	 * @return the skipStatistics
	 */
	public boolean isGatherStatistics() {
		return gatherStatistics;
	}
	
	/**
	 * PRESENT TO ACCOMODATE JOPR. NEED TO FILE A BUG ON THIS
	 * @return the skipStatistics
	 */
	public boolean getGatherStatistics() {
		return gatherStatistics;
	}

	/**
	 * @param backToNormalPercentageOfMemoryUsed the backToNormalPercentageOfMemoryUsed to set
	 */
	public void setBackToNormalMemoryThreshold(
			int backToNormalMemoryThreshold) {
		this.backToNormalMemoryThreshold = backToNormalMemoryThreshold;
		if(logger.isInfoEnabled()) {
			logger.info("Back To Normal Memory threshold set to " + backToNormalMemoryThreshold +"%");
		}
	}

	/**
	 * @return the backToNormalPercentageOfMemoryUsed
	 */
	public int getBackToNormalMemoryThreshold() {
		return backToNormalMemoryThreshold;
	}

	/**
	 * @param backToNormalQueueSize the backToNormalQueueSize to set
	 */
	public void setBackToNormalQueueSize(int backToNormalQueueSize) {
		this.backToNormalQueueSize = backToNormalQueueSize;
		if(logger.isInfoEnabled()) {
			logger.info("Back To Normal Queue Size set to " + backToNormalQueueSize);
		}
	}

	/**
	 * @return the backToNormalQueueSize
	 */
	public int getBackToNormalQueueSize() {
		return backToNormalQueueSize;
	}

	public SipStack getSipStack() {
		return sipStack;
	}

	public void setSipStack(SipStack sipStack) {
		this.sipStack = sipStack;		
	}
	
	protected void startSipStack() throws SipException {
		// stopping the sip stack	
		if(sipStack != null) {
			sipStack.start();
			if(logger.isInfoEnabled()) {
				logger.info("SIP stack started");
			}
		}
	}
	
	protected void stopSipStack() {
		// stopping the sip stack	
		if(sipStack != null) {
			sipStack.stop();
			sipStack = null;
			if(logger.isInfoEnabled()) {
				logger.info("SIP stack stopped");
			}
		}
	}

	public String getVersion() {
		return Version.getVersion();
	}

	@Override
	public SipService getSipService() {
		return sipService;
	}

	@Override
	public void setSipService(SipService sipService) {
		this.sipService = sipService;
	}
}
