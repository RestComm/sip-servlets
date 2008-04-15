/**
 * 
 */
package org.mobicents.servlet.sip.core;

import gov.nist.javax.sip.stack.SIPServerTransaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationRouter;
import javax.servlet.sip.SipApplicationRouterInfo;
import javax.servlet.sip.SipApplicationRoutingDirective;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.session.SessionManager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Implementation of the SipApplicationDispatcher interface.
 * Central point getting the sip messages from the different stacks for a Tomcat Service(Engine), 
 * translating jain sip SIP messages to sip servlets SIP¨messages, calling the application router 
 * to know which app is interested in the messages and
 * dispatching them those sip applications interested in the messages. 
 */
public class SipApplicationDispatcherImpl implements SipApplicationDispatcher {
	//the logger
	private static transient Log logger = LogFactory
			.getLog(SipApplicationDispatcherImpl.class);
	//the sip application router responsible for the routing logic of sip messages to
	//sip servlet applications
	private SipApplicationRouter sipApplicationRouter = null;
	//map of applications deployed
	private Map<String, SipContext> applicationDeployed = null;
	
	private SessionManager sessionManager = new SessionManager();
	
	/**
	 * 
	 */
	public SipApplicationDispatcherImpl() {
		applicationDeployed = new HashMap<String, SipContext>();
	}
	
	/**
	 * {@inheritDoc} 
	 */
	public void init(String sipApplicationRouterClassName) throws LifecycleException {
		//load the sip application router from the class name specified in the server.xml file
		//and initializes it
		try {
			sipApplicationRouter = (SipApplicationRouter)
				Class.forName(sipApplicationRouterClassName).newInstance();
		} catch (InstantiationException e) {
			throw new LifecycleException("Impossible to load the Sip Application Router",e);
		} catch (IllegalAccessException e) {
			throw new LifecycleException("Impossible to load the Sip Application Router",e);
		} catch (ClassNotFoundException e) {
			throw new LifecycleException("Impossible to load the Sip Application Router",e);
		} catch (ClassCastException e) {
			throw new LifecycleException("Sip Application Router defined does not implement " + SipApplicationRouter.class.getName(),e);
		}		
		sipApplicationRouter.init();		
	}
	/**
	 * {@inheritDoc}
	 */
	public void start() {
		// TODO nothing to do here for now
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void stop() {
		sipApplicationRouter.destroy();		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addSipApplication(String sipApplicationName, SipContext sipApplication) {
		synchronized (applicationDeployed) {
			applicationDeployed.put(sipApplicationName, sipApplication);	
		}				
		logger.info("the following sip servlet application has been added : " + sipApplicationName);
	}
	/**
	 * {@inheritDoc}
	 */
	public SipContext removeSipApplication(String sipApplicationName) {
		SipContext sipContext;
		synchronized (applicationDeployed) {
			sipContext = applicationDeployed.remove(sipApplicationName);
		}
		logger.info("the following sip servlet application has been removed : " + sipApplicationName);
		return sipContext;
	}
	/**
	 * {@inheritDoc}
	 */	
	public void processDialogTerminated(DialogTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void processIOException(IOExceptionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void processRequest(RequestEvent requestEvent) {
		logger.info("Got a request event");
		
		SipSessionImpl session = sessionManager.getRequestSession(requestEvent);
		
		SipServletRequestImpl sipServletRequest = new SipServletRequestImpl(
				(SipProvider) requestEvent.getSource(),
				session,
				(SIPServerTransaction) session.getInitialTransaction());
		
		logger.info("Dispatching the request event");
		
		SipApplicationRouterInfo applicationRouterInfo = 
			sipApplicationRouter.getNextApplication(sipServletRequest, null, SipApplicationRoutingDirective.NEW, null);
		if(applicationRouterInfo.getNextApplicationName() == null) {
			//TODO sends an error message
		} else {
			SipContext sipContext = applicationDeployed.get(applicationRouterInfo);
			String mainServlet = sipContext.getMainServlet();
			Container container = sipContext.findChild(mainServlet);
			try {
				container.invoke(null, null);
			} catch (IOException e) {
				//TODO sends an error message
				logger.error("Problem during invocation of servlet "+ mainServlet,e);				
			} catch (ServletException e) {
				//TODO sends an error message
				logger.error("Problem during invocation of servlet "+ mainServlet,e);				
			}
		}
		logger.info("Request event dispatched");
	}
	/**
	 * {@inheritDoc}
	 */
	public void processResponse(ResponseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void processTimeout(TimeoutEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * {@inheritDoc}
	 */
	public void processTransactionTerminated(TransactionTerminatedEvent arg0) {
		// TODO Auto-generated method stub
		
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
}
