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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServletResponse;
import javax.sip.ObjectInUseException;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.Transaction;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.DispatcherException;
import org.mobicents.servlet.sip.core.MobicentsSipServlet;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.descriptor.MobicentsSipServletMapping;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.message.SipServletMessageImpl;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.message.TransactionApplicationData;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class MessageDispatcher {

	private static final Logger logger = Logger.getLogger(MessageDispatcher.class);
	
	public static final String ROUTE_PARAM_DIRECTIVE = "directive";
	
	public static final String ROUTE_PARAM_REGION_LABEL= "region_label";
	public static final String ROUTE_PARAM_REGION_TYPE= "region_type";
	
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
	 * since we generate our own branch ids, this is the constant for the magic cookie 
	 */
	public static final String BRANCH_MAGIC_COOKIE = "z9hG4bK";
	/* 
	 * This parameter is to know the application id
	 */
	public static final String APP_ID = "app_id";

	/*
	 * Those parameters is to indicate to the SIP Load Balancer, from which node comes from the request
	 * so that it can stick the Call Id to this node and correctly route the subsequent requests. 
	 */
	public static final String ROUTE_PARAM_NODE_HOST = "node_host";

	public static final String ROUTE_PARAM_NODE_PORT = "node_port";
	
	
	protected SipApplicationDispatcher sipApplicationDispatcher = null;
	
	/**
	 * From RFC 5626:
	 * 
	 * "The "ob" parameter is a SIP URI parameter that has a different meaning
	 * depending on context. In a Path header field value, it is used by the
	 * first edge proxy to indicate that a flow token was added to the URI. In a
	 * Contact or Route header field value, it indicates that the UA would like
	 * other requests in the same dialog to be routed over the same flow."
	 * 
	 */
	public static final String SIP_OUTBOUND_PARAM_OB = "ob";
	public static final String SIP_OUTBOUND_PARAM_REG_ID = "reg-id";	
		
	public MessageDispatcher() {}
	
//	public MessageDispatcher(SipApplicationDispatcher sipApplicationDispatcher) {
//		this.sipApplicationDispatcher = sipApplicationDispatcher;
//	}
	
	public static void sendErrorResponse(int errorCode, SipServletRequestImpl sipServletRequest, SipProvider sipProvider) {
		MessageDispatcher.sendErrorResponse(errorCode, (ServerTransaction) sipServletRequest.getTransaction(), (Request) sipServletRequest.getMessage(), sipProvider);
		if(sipServletRequest.getSipSession() != null) {
			sipServletRequest.getSipSession().updateStateOnResponse((SipServletResponseImpl)sipServletRequest.createResponse(SipServletResponseImpl.SC_SERVER_INTERNAL_ERROR), false);
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
			Response response=SipFactoryImpl.messageFactory.createResponse
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
			java.lang.Thread.currentThread().setContextClassLoader(sipContext.getSipContextClassLoader());		
			
			Class methodDeclaringClass = appKeyMethod.getDeclaringClass();
			MobicentsSipServlet sipServletImpl = sipContext.findSipServletByClassName(methodDeclaringClass.getCanonicalName());			
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
//			if(appGeneratedKey == null) {
				// JSR 289 Section 18.2.5 @SipApplicationKey Annotation , The container should treat a "null" return 
				// or an invalid session id as a failure to obtain a key from the application. 
				// It is recommended that the container create a new SipApplicationSession for the incoming request in such a case.
//				throw new IllegalStateException("SipApplicationKey annotated method shoud not return null");				
//			}
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
	
	public static void callServletForOrphanRequest(SipContext sipContext, SipServletRequestImpl request) throws DispatcherException, ServletException, IOException {
		String servletHandler = sipContext.getServletHandler();		
		//Fix for Issue 1200 : if we are not in a main-servlet servlet selection type, the mapping discovery should always be performed 
		String mainServlet = servletHandler;
		if(sipContext.isMainServlet() && mainServlet != null && mainServlet.length() > 0) {
			servletHandler = mainServlet;				
		} else {
			MobicentsSipServletMapping sipServletMapping = sipContext.findSipServletMappings(request);
			if(sipServletMapping == null && sipContext.getSipRubyController() == null) {
				logger.error("Sending 404 because no matching servlet found for this request ");
				throw new DispatcherException(Response.NOT_FOUND);
			} else if(sipServletMapping != null) {							
				servletHandler = sipServletMapping.getServletName();
				if(logger.isDebugEnabled()) {
					logger.debug("servlet mapping Handler Name= " + servletHandler);
				}
			}
		}
		if(logger.isDebugEnabled()) {
			logger.debug("current request Handler " + servletHandler + " for orphan Request = " + request);
		}
		final MobicentsSipServlet sipServletImpl = sipContext.findSipServletByName(servletHandler);
		if(sipServletImpl != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Dispatching orphan request " + request.toString() + 
					" to servlet " + servletHandler);
			}
			final Servlet servlet = sipServletImpl.allocate();
			// JBoss-specific CL issue:
			// This is needed because usually the classloader here is some UnifiedClassLoader3,
			// which has no idea where the servlet ENC is. We will use the classloader of the
			// servlet class, which is the WebAppClassLoader, and has ENC fully loaded with
			// with java:comp/env/security (manager, context etc)
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			
			try {
				sipContext.enterSipContext();				
				try {
					if(logger.isDebugEnabled()) {
						logger.debug("Invoking instance " + servlet);
					}
					servlet.service(request, null);
				} finally {								
					sipServletImpl.deallocate(servlet);					
				}
			} finally {				
				sipContext.exitSipContext(oldClassLoader);
			}
		}
	}
	
	public static void callServletForOrphanResponse(SipContext sipContext, SipServletResponse response) throws DispatcherException, ServletException, IOException {
		String servletHandler = sipContext.getServletHandler();		
		//Fix for Issue 1200 : if we are not in a main-servlet servlet selection type, the mapping discovery should always be performed 
		String mainServlet = servletHandler;
		
		SipServletResponseImpl responseExt = (SipServletResponseImpl) response;
		responseExt.setOrphan(true);
		responseExt.setCurrentApplicationName(sipContext.getApplicationName());
		if(sipContext.isMainServlet() && mainServlet != null && mainServlet.length() > 0) {
			servletHandler = mainServlet;				
		} else {
			if(logger.isDebugEnabled()) {
				logger.debug("servlet mapping Handler Name= " + servletHandler);
			}

		}
		if(logger.isDebugEnabled()) {
			logger.debug("current request Handler " + servletHandler + " for orphan Request = " + response);
		}
		final MobicentsSipServlet sipServletImpl = sipContext.findSipServletByName(servletHandler);
		if(sipServletImpl != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Dispatching orphan request " + response.toString() + 
					" to servlet " + servletHandler);
			}
			final Servlet servlet = sipServletImpl.allocate();
			// JBoss-specific CL issue:
			// This is needed because usually the classloader here is some UnifiedClassLoader3,
			// which has no idea where the servlet ENC is. We will use the classloader of the
			// servlet class, which is the WebAppClassLoader, and has ENC fully loaded with
			// with java:comp/env/security (manager, context etc)
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			
			try {
				sipContext.enterSipContext();	
				try {
					if(logger.isDebugEnabled()) {
						logger.debug("Invoking instance " + servlet);
					}
					
					if(response.getStatus() > Response.TRYING) {
						servlet.service(null, response);
					}
				} finally {								
					sipServletImpl.deallocate(servlet);					
				}
			} finally {
				sipContext.exitSipContext(oldClassLoader);
			}
		}
	}
	
	public static void callServlet(MobicentsSipServletRequest request) throws ServletException, IOException {
		final MobicentsSipSession session = request.getSipSession();		
		final String sessionHandler = session.getHandler();
		final MobicentsSipApplicationSession sipApplicationSessionImpl = session.getSipApplicationSession();
		final SipContext sipContext = sipApplicationSessionImpl.getSipContext();
		final MobicentsSipServlet sipServletImpl = sipContext.findSipServletByName(sessionHandler);
		if(sipServletImpl != null) {
			if(logger.isDebugEnabled()) {
				logger.debug("Dispatching request " + request.toString() + 
					" to following App/servlet => " + session.getKey().getApplicationName()+ 
					"/" + session.getHandler() + " on following sip session " + session.getId());
			}
			final Servlet servlet = sipServletImpl.allocate();
			// JBoss-specific CL issue:
			// This is needed because usually the classloader here is some UnifiedClassLoader3,
			// which has no idea where the servlet ENC is. We will use the classloader of the
			// servlet class, which is the WebAppClassLoader, and has ENC fully loaded with
			// with java:comp/env/security (manager, context etc)
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			
			try {
				sipContext.enterSipContext();	
			
				if(!securityCheck(request)) return;				

				if(logger.isDebugEnabled()) {
					logger.debug("Invoking instance " + servlet);
				}
				
				try {
					servlet.service(request, null);
				} finally {			
					sipServletImpl.deallocate(servlet);
				}
			} finally {
				sipContext.exitSipContext(oldClassLoader);
			}
		} else if(sipContext.getSipRubyController() != null) {
			//handling the ruby case
			if(logger.isDebugEnabled()) {
				logger.debug("Dispatching request " + request.toString() + 
					" to following App/ruby controller => " + request.getSipSession().getKey().getApplicationName()+ 
					"/" + sipContext.getSipRubyController().getName());
			}
			
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				final ClassLoader cl = sipContext.getSipContextClassLoader();
				Thread.currentThread().setContextClassLoader(cl);
			
				sipContext.getSipRubyController().routeSipMessageToRubyApp(sipContext.getServletContext(), request);
			} finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}
		} else {
			logger.error("no handler found for sip session " + session.getKey() + " and request " + request);
			// Issue 1493 : under some race condition the transaction might not be added to the sip session
			// and in this particular condition no response will be generated back to the UA (we don't want to since this 
			// can be a retransmission here) so the stack will hold the ref to the transaction, so terminating the transaction
			// if it is present and cleaning up all related data
            Transaction transaction = request.getTransaction();
            if(transaction != null) {
                TransactionApplicationData tad = (TransactionApplicationData) transaction.getApplicationData();
                if(tad != null) {
                    tad.cleanUp();
                }
                transaction.setApplicationData(null);

                try {
                    transaction.terminate();
                } catch (ObjectInUseException e) {
                    logger.error("transaction " + transaction.getBranchId() + " for request " + request + " couldn't be terminated");
                }
            }

            request.setSipSession(null);
		}
	}
	
	public static void callServlet(MobicentsSipServletResponse response) throws ServletException, IOException {		
		final MobicentsSipSession session = response.getSipSession();
		
		final String sessionHandler = session.getHandler();
		final MobicentsSipApplicationSession sipApplicationSessionImpl = session.getSipApplicationSession();
		final SipContext sipContext = sipApplicationSessionImpl.getSipContext();
		final MobicentsSipServlet sipServletImpl = (MobicentsSipServlet) sipContext.findSipServletByName(sessionHandler);
		
		if(sipServletImpl == null || sipServletImpl.isUnavailable()) {
			if(sipContext.getSipRubyController() != null) {
				//handling the ruby case	
				if(logger.isDebugEnabled()) {
					logger.debug("Dispatching response " + response.toString() + 
						" to following App/ruby controller => " + response.getSipSession().getKey().getApplicationName()+ 
						"/" + sipContext.getSipRubyController().getName());
				}
				final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			
				try {
					final ClassLoader cl = sipContext.getSipContextClassLoader();
					Thread.currentThread().setContextClassLoader(cl);
				
					sipContext.getSipRubyController().routeSipMessageToRubyApp(sipContext.getServletContext(), response);
				} finally {
					Thread.currentThread().setContextClassLoader(oldClassLoader);
				}
			} else {
				logger.warn(sessionHandler + " is unavailable, dropping response " + response);
			}
		} else {
			final Servlet servlet = sipServletImpl.allocate();
			if(logger.isDebugEnabled()) {
				logger.debug("Dispatching response " + response.toString() + 
					" to following App/servlet => " + session.getKey().getApplicationName()+ 
					"/" + session.getHandler() + " on following sip session " + session.getId());
			}
			final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				sipContext.enterSipContext();	
			
				try {				
					servlet.service(null, response);
				} finally {
					sipServletImpl.deallocate(servlet);
				}
			} finally {
				sipContext.exitSipContext(oldClassLoader);
			}
		}
				
	}
	
	public static boolean securityCheck(MobicentsSipServletRequest request)
	{
		MobicentsSipApplicationSession appSession = (MobicentsSipApplicationSession) request.getApplicationSession();
		SipContext sipStandardContext = appSession.getSipContext();
		boolean authorized = sipStandardContext.authorize(request);
		
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
			return this.sipApplicationDispatcher.getAsynchronousExecutor();
	}
}
