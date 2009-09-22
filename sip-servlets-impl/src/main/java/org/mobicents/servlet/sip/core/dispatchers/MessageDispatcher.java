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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.catalina.Wrapper;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.security.SipSecurityUtils;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class MessageDispatcher {

	private static transient Logger logger = Logger.getLogger(MessageDispatcher.class);
	
	public static final String ROUTE_PARAM_DIRECTIVE = "directive";
	
	public static final String ROUTE_PARAM_PREV_APPLICATION_NAME = "previousappname";
	
	public static final String ROUTE_PARAM_PREV_APP_ID = "previousappid";
	/* 
	 * This parameter is to know which app handled the request 
	 */
	public static final String RR_PARAM_APPLICATION_NAME = "appname";
	/* 
	 * This parameter is to know if a record routing proxy app forwarded the request
	 * It will help us to determine if the next request coming from either side is a subsequent request
	 */
	public static final String RR_PARAM_PROXY_APP = "proxy";
	/* 
	 * This parameter is to know if a servlet application sent a final response
	 */
//	public static final String FINAL_RESPONSE = "final_response";
	/* 
	 * This parameter is to know if a servlet application has generated its own application key
	 */
	public static final String GENERATED_APP_KEY = "gen_app_key";
	/* 
	 * This parameter is to know the application id
	 */
	public static final String APP_ID = "app_id";
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

	/*
	 * Those parameters is to indicate to the SIP Load Balancer, from which node comes from the request
	 * so that it can stick the Call Id to this node and correctly route the subsequent requests. 
	 */
	public static final String ROUTE_PARAM_NODE_HOST = "node_host";

	public static final String ROUTE_PARAM_NODE_PORT = "node_port";
	
	
	protected SipApplicationDispatcher sipApplicationDispatcher = null;
		
	public MessageDispatcher() {}
	
//	public MessageDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
//		this.sipApplicationDispatcher = sipApplicationDispatcher;
//	}
	
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
	
	protected static SipApplicationSessionKey makeAppSessionKey(SipContext sipContext, SipServletRequestImpl sipServletRequestImpl, String applicationName) throws DispatcherException {
		String appGeneratedKey = null;
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
			sipServletRequestImpl.setReadOnly(true);
			Servlet servlet = null;
			// Set the Class Loader to the same that loaded the servlet class to avoid http://code.google.com/p/mobicents/issues/detail?id=700
			ClassLoader oldLoader = java.lang.Thread.currentThread().getContextClassLoader();
			java.lang.Thread.currentThread().setContextClassLoader(sipContext.getLoader().getClassLoader());		
			
			Class methodDeclaringClass = appKeyMethod.getDeclaringClass();
			Wrapper sipServletImpl = (Wrapper) sipContext.findChildrenByClassName(methodDeclaringClass.getCanonicalName());			
			if(sipServletImpl != null) {				
				try {
					servlet = sipServletImpl.allocate();
					// http://code.google.com/p/mobicents/issues/detail?id=700 : 
					// we get the method from servlet class anew because the original method might have been loaded by a different class loader  
					Method newMethod = servlet.getClass().getMethod(appKeyMethod.getName(), appKeyMethod.getParameterTypes());
					appKeyMethod = newMethod;
												
					if(logger.isDebugEnabled()) {
						logger.debug("Invoking the application key method " + appKeyMethod.getName() + 
								", on the following servlet " + methodDeclaringClass.getCanonicalName());
					}
				} catch (ServletException e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Couldn't allocate the sip servlet to invoke the key annotated method !" ,e);
				} catch (SecurityException e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Couldn't allocate the sip servlet to invoke the key annotated method !" ,e);
				} catch (NoSuchMethodException e) {
					throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Couldn't allocate the sip servlet to invoke the key annotated method !" ,e);
				} finally {
					java.lang.Thread.currentThread().setContextClassLoader(oldLoader);		
				}
			}
			try {			
				appGeneratedKey = (String) appKeyMethod.invoke(servlet, new Object[] {sipServletRequestImpl});
			} catch (IllegalArgumentException e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Couldn't invoke the app session key annotated method !" ,e);		
			} catch (IllegalAccessException e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Couldn't invoke the app session key annotated method !" ,e);
			} catch (InvocationTargetException e) {
				throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "A Problem occured while invoking the app session key annotated method !" ,e);
			} finally {
				sipServletRequestImpl.setReadOnly(false);
				if(sipServletImpl != null) {
					try {
						sipServletImpl.deallocate(servlet);
					} catch (ServletException e) {
						throw new DispatcherException(Response.SERVER_INTERNAL_ERROR, "Couldn't deallocate the sip servlet to invoke the key annotated method !" ,e);
					}
				}
				java.lang.Thread.currentThread().setContextClassLoader(oldLoader);		
			}
			if(appGeneratedKey == null) {
				//JSR 289 Section 18.2.5 @SipApplicationKey Annotation , The container should treat a "null" return 
				//or an invalid session id as a failure to obtain a key from the application. 
				//It is recommended that the container create a new SipApplicationSession for the incoming request in such a case.
//				throw new IllegalStateException("SipApplicationKey annotated method shoud not return null");				
			}
			if(logger.isDebugEnabled()) {
				logger.debug("For request target to application " + sipContext.getApplicationName() + 
						", following annotated method " + appKeyMethod + " generated the application key : " + appGeneratedKey);
			}
		}
		SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
				applicationName, 
				null);
		sipApplicationSessionKey.setAppGeneratedKey(appGeneratedKey);
		return sipApplicationSessionKey;
	}
	
	public static void callServlet(SipServletRequestImpl request) throws ServletException, IOException {
		final MobicentsSipSession session = request.getSipSession();		
		final String sessionHandler = session.getHandler();
		final MobicentsSipApplicationSession sipApplicationSessionImpl = session.getSipApplicationSession();
		final SipContext sipContext = sipApplicationSessionImpl.getSipContext();
		final Wrapper sipServletImpl = (Wrapper) sipContext.findChild(sessionHandler);
		if(sipServletImpl != null) {
			if(logger.isInfoEnabled()) {
				logger.info("Dispatching request " + request.toString() + 
					" to following App/servlet => " + session.getKey().getApplicationName()+ 
					"/" + session.getHandler() + " on following sip session " + session.getId());
			}
			final Servlet servlet = sipServletImpl.allocate();
			// JBoss-specific CL issue:
			// This is needed because usually the classloader here is some UnifiedClassLoader3,
			// which has no idea where the servlet ENC is. We will use the classloader of the
			// servlet class, which is the WebAppClassLoader, and has ENC fully loaded with
			// with java:comp/env/security (manager, context etc)
			ClassLoader cl = servlet.getClass().getClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			
			if(!securityCheck(request)) return;

			if(logger.isDebugEnabled()) {
				logger.debug("Invoking instance " + servlet);
			}
			
			try {
				servlet.service(request, null);
			} finally {			
				sipServletImpl.deallocate(servlet);
			}
		} else if(sipContext.getSipRubyController() != null) {
			//handling the ruby case
			if(logger.isInfoEnabled()) {
				logger.info("Dispatching request " + request.toString() + 
					" to following App/ruby controller => " + request.getSipSession().getKey().getApplicationName()+ 
					"/" + sipContext.getSipRubyController().getName());
			}
			ClassLoader cl = sipContext.getLoader().getClassLoader();
			Thread.currentThread().setContextClassLoader(cl);
			
			sipContext.getSipRubyController().routeSipMessageToRubyApp(sipContext.getServletContext(), request);
		} else {
			logger.error("no handler found for sip session " + session.getKey() + " and request " + request);
		}
	}
	
	public static void callServlet(SipServletResponseImpl response) throws ServletException, IOException {		
		final MobicentsSipSession session = response.getSipSession();
		
		final String sessionHandler = session.getHandler();
		final MobicentsSipApplicationSession sipApplicationSessionImpl = session.getSipApplicationSession();
		final SipContext sipContext = sipApplicationSessionImpl.getSipContext();
		final Wrapper sipServletImpl = (Wrapper) sipContext.findChild(sessionHandler);
		
		if(sipServletImpl == null || sipServletImpl.isUnavailable()) {
			if(sipContext.getSipRubyController() != null) {
				//handling the ruby case	
				if(logger.isInfoEnabled()) {
					logger.info("Dispatching response " + response.toString() + 
						" to following App/ruby controller => " + response.getSipSession().getKey().getApplicationName()+ 
						"/" + sipContext.getSipRubyController().getName());
				}
				ClassLoader cl = sipContext.getLoader().getClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
				
				sipContext.getSipRubyController().routeSipMessageToRubyApp(sipContext.getServletContext(), response);
			} else {
				logger.warn(sessionHandler + " is unavailable, dropping response " + response);
			}
		} else {
			final Servlet servlet = sipServletImpl.allocate();
			if(logger.isInfoEnabled()) {
				logger.info("Dispatching response " + response.toString() + 
					" to following App/servlet => " + session.getKey().getApplicationName()+ 
					"/" + session.getHandler() + " on following sip session " + session.getId());
			}
			try {				
				servlet.service(null, response);
			} finally {
				sipServletImpl.deallocate(servlet);
			}
		}
				
	}
	
	public static boolean securityCheck(SipServletRequestImpl request)
	{
		MobicentsSipApplicationSession appSession = (MobicentsSipApplicationSession) request.getApplicationSession();
		SipContext sipStandardContext = appSession.getSipContext();
		boolean authorized = SipSecurityUtils.authorize(sipStandardContext, request);
		
		// This will propagate the identity for the thread and all called components
		// TODO: FIXME: This would introduce a dependency on JBoss
		// SecurityAssociation.setPrincipal(request.getUserPrincipal());
		
		return authorized;
	}
	
	/**
	 * Responsible for routing and dispatching a SIP message to the correct application
	 * 
	 * @param sipProvider use the sipProvider to route the message if needed, can be null
	 * @param sipServletMessage the SIP message to route and dispatch
	 * @return true if we should continue routing to other applications, false otherwise
	 * @throws Exception if anything wrong happens
	 */
	public abstract void dispatchMessage(SipProvider sipProvider, SipServletMessageImpl sipServletMessage) throws DispatcherException;
	
	/**
	 * This method return an ExecutorService depending on the current concurrency strategy. It can return the
	 * executor of a sip session, app session or just threadpool executor which doesn't limit concurrent processing
	 * of requests per app or sip session.
	 * Since 0.8.1 it always return threadpool executor which doesn't limit concurrent processing since concurrency is achieved through semaphore
	 * 
	 * @param sipServletMessage the request you put here must have app and sip session associated
	 * @return
	 */
	public final ExecutorService getConcurrencyModelExecutorService(
			SipContext sipContext, SipServletMessageImpl sipServletMessage) {
			return ((SipApplicationDispatcherImpl) this.sipApplicationDispatcher)
					.getAsynchronousExecutor();
	}
}
