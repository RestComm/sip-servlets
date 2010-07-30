/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.web.tomcat.service.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jboss.logging.Logger;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

public final class ConvergedSessionReplicationContext
{
	protected static Logger logger = Logger.getLogger(ConvergedSessionReplicationContext.class);
	
   private static final ThreadLocal<ConvergedSessionReplicationContext> replicationContext = new ThreadLocal<ConvergedSessionReplicationContext>();
   private static final ThreadLocal<ConvergedSessionReplicationContext> sipReplicationContext = new ThreadLocal<ConvergedSessionReplicationContext>();
   
   private static final ConvergedSessionReplicationContext EMPTY = new ConvergedSessionReplicationContext();
   private static final ConvergedSessionReplicationContext EMPTY_SIP = new ConvergedSessionReplicationContext();
   
   private int webappCount;
   private int sipappCount;
//   private int activityCount;
//   private int sipActivityCount;
   private SnapshotManager soleManager;
   private SnapshotSipManager soleSipManager;
   private ClusteredSession<? extends OutgoingDistributableSessionData> soleSession;
   private final Set<ClusteredSipSession<? extends OutgoingDistributableSessionData>> sipSessions = new HashSet<ClusteredSipSession<? extends OutgoingDistributableSessionData>>();
   private final Set<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>> sipApplicationSessions = new HashSet<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>>();
   private Map<ClusteredSession<? extends OutgoingDistributableSessionData>, SnapshotManager> crossCtxSessions;
//   private Map crossCtxSipSessions;
//   private Map crossCtxSipApplicationSessions;
//   private Map expiredSessions;
//   private Map expiredSipSessions;
//   private Map expiredSipApplicationSessions;
   private Request outerRequest;
   private Response outerResponse;
//   private SipServletRequestImpl outerSipRequest;
//   private SipServletResponseImpl outerSipResponse;
   
   
   /**
    * Associate a SessionReplicationContext with the current thread, if
    * there isn't one already.  If there isn't one, associate the 
    * given request and response with the context.
    * <p/>
    * <strong>NOTE:</strong> Nested calls to this method and {@link #exitWebapp()}
    * are supported; once a context is established the number of calls to this
    * method and <code>exitWebapp()</code> are tracked.
    * 
    * @param request
    * @param response
    */
   public static void enterWebapp(Request request, Response response, boolean startCacheActivity)
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx == null)
      {
         ctx = new ConvergedSessionReplicationContext(request, response);
         replicationContext.set(ctx);
      }
      
      ctx.webappCount++;      
   }
   
   /**
    * Signals that the webapp is finished handling the request (and
    * therefore replication can begin.)
    * 
    * @return a SessionReplicationContext, from which information
    *         about any sessions needing replication can be obtained.
    *         Will not return <code>null</code>.
    */
   public static ConvergedSessionReplicationContext exitWebapp()
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx != null)
      {
         ctx.webappCount--;
         if (ctx.webappCount < 1)
         {
            // We've unwound any nested webapp calls, so we'll clean up and 
            // return the context to allow replication.  If all cache activity
            // is done as well, clear the ThreadLocal
            
            ctx.outerRequest = null;
            ctx.outerResponse = null;
            
            replicationContext.set(null);
            
            return ctx;
         }
      }
      
      // A nested valve called us. Just return an empty context
      return EMPTY;
      
   }
   
   /**
    * Associate a SessionReplicationContext with the current thread, if
    * there isn't one already.  If there isn't one, associate the 
    * given request and response with the context.
    * <p/>
    * <strong>NOTE:</strong> Nested calls to this method and {@link #exitWebapp()}
    * are supported; once a context is established the number of calls to this
    * method and <code>exitWebapp()</code> are tracked.
    * 
    * @param request
    * @param response
    */
   public static void enterSipapp(SipServletRequestImpl request, SipServletResponseImpl response, boolean startCacheActivity)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx == null)
      {
         ctx = new ConvergedSessionReplicationContext();
         sipReplicationContext.set(ctx);
      }
      
      ctx.sipappCount++;     
   }
   
   /**
    * Associate a SessionReplicationContext with the current thread, if
    * there isn't one already.  If there isn't one, associate the 
    * given request and response with the context.
    * <p/>
    * <strong>NOTE:</strong> Nested calls to this method and {@link #exitWebapp()}
    * are supported; once a context is established the number of calls to this
    * method and <code>exitWebapp()</code> are tracked.
    * 
    * @param request
    * @param response
    */
//   public static void enterSipappAndBindSessions(SipServletRequestImpl request, SipServletResponseImpl response, SipManager manager, boolean startCacheActivity)
//   {
//      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
//      if (ctx == null)
//      {
//         ctx = new ConvergedSessionReplicationContext();
//         sipReplicationContext.set(ctx);
//      }
//      
//      ctx.sipappCount++;     
//      
//      ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusteredSipApplicationSession = null;
//      ClusteredSipSession<? extends OutgoingDistributableSessionData> clusteredSipSession = null; 
//      if(request != null) {
//    	  clusteredSipSession = (ClusteredSipSession<? extends OutgoingDistributableSessionData>)request.getSipSession();    	  
//      } else {
//    	  clusteredSipSession = (ClusteredSipSession<? extends OutgoingDistributableSessionData>)response.getSipSession();
//      }
//      
//      if(clusteredSipSession.getSipApplicationSession() instanceof org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade) {
//      clusteredSipApplicationSession = (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>) 
//      	((org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade)
//      			clusteredSipSession.getSipApplicationSession()).getMobicentstSipApplicationSession();
//      } else {
//    	  clusteredSipApplicationSession = (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>)clusteredSipSession.getSipApplicationSession();
//      }
//      
//      bindSipApplicationSession(clusteredSipApplicationSession, ((ClusteredSipManager<OutgoingDistributableSessionData>)manager).getSnapshotSipManager());
//      bindSipSession(clusteredSipSession, ((ClusteredSipManager<OutgoingDistributableSessionData>)manager).getSnapshotSipManager());
//   }
//   
//   public static void enterSipappAndBindSessions(SipApplicationSession appSession, SipManager manager, boolean startCacheActivity)
//   {
//      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
//      if (ctx == null)
//      {
//         ctx = new ConvergedSessionReplicationContext();
//         sipReplicationContext.set(ctx);
//      }
//      
//      ctx.sipappCount++;     
//      
//      ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> clusteredSipApplicationSession = null;
//      
//      if(appSession instanceof org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade) {
//      clusteredSipApplicationSession = (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>) 
//      	((org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade)
//      			appSession).getMobicentstSipApplicationSession();
//      } else {
//    	  clusteredSipApplicationSession = (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>)
//    	  	appSession;
//      }
//      
//      SnapshotSipManager snapshotSipManager = ((ClusteredSipManager<OutgoingDistributableSessionData>)manager).getSnapshotSipManager();
//      bindSipApplicationSession(clusteredSipApplicationSession, snapshotSipManager);
//      Iterator<?> sipSessionIterator = clusteredSipApplicationSession.getSessions(Protocol.SIP.toString());
//      while(sipSessionIterator.hasNext()) {
//    	  SipSession sipSession = (SipSession) sipSessionIterator.next();
//    	  ClusteredSipSession clusteredSipSession = null;
//    	  if(sipSession instanceof org.mobicents.servlet.sip.message.MobicentsSipSessionFacade) {
//    		  clusteredSipSession = (ClusteredSipSession) ((org.mobicents.servlet.sip.message.MobicentsSipSessionFacade)
//    				  sipSession).getMobicentsSipSession();
//    	  } else {
//    		  clusteredSipSession = (ClusteredSipSession) sipSession;
//    	  }
//    	  bindSipSession(clusteredSipSession, snapshotSipManager);
//      }
//    }
   
   /**
    * Signals that the webapp is finished handling the request (and
    * therefore replication can begin.)
    * 
    * @return a SessionReplicationContext, from which information
    *         about any sessions needing replication can be obtained.
    *         Will not return <code>null</code>.
    */
   public static ConvergedSessionReplicationContext exitSipapp()
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null)
      {
         ctx.sipappCount--;
         if (ctx.sipappCount < 1)
         {
            // We've unwound any nested webapp calls, so we'll clean up and 
            // return the context to allow replication.  If all cache activity
            // is done as well, clear the ThreadLocal
            
//            ctx.outerSipRequest = null;
//            ctx.outerSipResponse = null;
            
            sipReplicationContext.set(null);
            
            return ctx;
         }
      }
      
      // A nested valve called us. Just return an empty context
      return EMPTY_SIP;
      
   }
   
   public static void bindSession(ClusteredSession<? extends OutgoingDistributableSessionData> session, SnapshotManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx != null && ctx.webappCount > 0)
      {
         ctx.addReplicatableSession(session, manager);
      }
      /*else {
         We are past the part of the request cycle where we 
         track sessions for replication
      }*/
   }
   
   public static void bindSipSession(ClusteredSipSession<? extends OutgoingDistributableSessionData> session, SnapshotSipManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();      
      if (ctx != null && ctx.sipappCount > 0)
      {
         ctx.addReplicatableSipSession(session, manager);
      }
      /*else {
         We are past the part of the request cycle where we 
         track sessions for replication
      }*/
   }
   
   public static void bindSipApplicationSession(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session, SnapshotSipManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();      
      if (ctx != null && ctx.sipappCount > 0)
      {
         ctx.addReplicatableSipApplicationSession(session, manager);
      }
      /*else {
         We are past the part of the request cycle where we 
         track sessions for replication
      }*/
   }
   
   public static void sessionExpired(ClusteredSession<? extends OutgoingDistributableSessionData> session, String realId, SnapshotManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx != null && ctx.webappCount > 0)
      {
         ctx.sessionExpired(session, manager);
      }      
   }
   
   public static void sipSessionExpired(ClusteredSipSession<? extends OutgoingDistributableSessionData> session, SipSessionKey key, SnapshotSipManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null && ctx.sipappCount > 0)
      {
         ctx.sipSessionExpired(session, manager);
      }      
   }
   
   public static void sipApplicationSessionExpired(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session, SipApplicationSessionKey key, SnapshotSipManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null && ctx.sipappCount > 0)
      {
         ctx.sipApplicationSessionExpired(session, manager);
      }      
   }
   /**
    * Does nothing
    *
    * @return <code>false</code>
    * 
    * @deprecated Always returns false; will be removed in AS 6
    */
   @Deprecated
   public static boolean isSessionBoundAndExpired(String realId, SnapshotManager manager)
   {      
      return false;
   }
   /**
    * Does nothing
    *
    * @return <code>false</code>
    * 
    * @deprecated Always returns false; will be removed in AS 6
    */
   @Deprecated
   public static boolean isSipSessionBoundAndExpired(String key, SnapshotSipManager manager)
   {
	   return false;
   }
   /**
    * Does nothing
    *
    * @return <code>false</code>
    * 
    * @deprecated Always returns false; will be removed in AS 6
    */
   @Deprecated
   public static boolean isSipApplicationSessionBoundAndExpired(String key, SnapshotSipManager manager)
   {
	   return false;
   }
   
   /**
    * Does nothing
    * 
    * @deprecated Does nothing; will be removed in AS 6
    */
   @Deprecated
   public static void startCacheActivity()
   {
	   // no-op
   }
   
   /**
    * Does nothing
    * 
    * @deprecated Does nothing; will be removed in AS 6
    */
   @Deprecated
   public static void finishCacheActivity()
   {
	   // no-op
   }
   
   
   /**
    * Does nothing
    * 
    * @deprecated Does nothing; will be removed in AS 6
    */
   @Deprecated
   public static void startSipCacheActivity()
   {
	   // no-op
   }
   
   /**
    * Does nothing
    * 
    * @deprecated Does nothing; will be removed in AS 6
    */
   @Deprecated
   public static void finishSipCacheActivity()
   {
	   // no-op
   }
   /**
    * Returns whether there is a ConvergedSessionReplicationContext associated with
    * the current thread.
    * 
    * @return <code>true</code> if there is a context associated with the thread
    * 
    * @deprecated Will be removed in AS 6
    */
   @Deprecated
   public static boolean isLocallyActive()
   {
      return getCurrentContext() != null;
   }
   /**
    * Returns whether there is a ConvergedSessionReplicationContext associated with
    * the current thread.
    * 
    * @return <code>true</code> if there is a context associated with the thread
    * 
    * @deprecated Will be removed in AS 6
    */
   @Deprecated
   public static boolean isSipLocallyActive()
   {
      return getCurrentSipContext() != null;
   }
   
   public static Request getOriginalRequest()
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      return (ctx == null ? null : ctx.outerRequest);
   }
   
   public static Response getOriginalResponse()
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      return (ctx == null ? null : ctx.outerResponse);
   }
   
   private static ConvergedSessionReplicationContext getCurrentContext()
   {
      return replicationContext.get();
   }
   
   private static ConvergedSessionReplicationContext getCurrentSipContext()
   {
      return sipReplicationContext.get();
   }
   
   private ConvergedSessionReplicationContext(Request request, Response response) 
   {
      this.outerRequest = request;
      this.outerResponse = response;
   }
   
//   private ConvergedSessionReplicationContext(SipServletRequestImpl request, SipServletResponseImpl response) 
//   {
//      this.outerSipRequest = request;
//      this.outerSipResponse = response;
//   }
   
   private ConvergedSessionReplicationContext() {}

   /**
    * Gets a Map<SnapshotManager, ClusteredSession> of sessions that were accessed
    * during the course of a request.  Will only be non-null if 
    * {@link #bindSession(ClusteredSession, SnapshotManager)} was called
    * with more than one SnapshotManager (i.e the request crossed session
    * contexts.)
    */
   public Map<ClusteredSession<? extends OutgoingDistributableSessionData>, SnapshotManager> getCrossContextSessions()
   {
      return crossCtxSessions;
   }

   /**
    * Gets the SnapshotManager that was passed to  
    * {@link #bindSession(ClusteredSession, SnapshotManager)} if and only
    * if only one such SnapshotManager was passed. Returns <code>null</code>
    * otherwise, in which case a cross-context request is a possibility,
    * and {@link #getCrossContextSessions()} should be checked.
    */
   public SnapshotManager getSoleSnapshotManager()
   {
      return soleManager;
   }
   
   /**
    * Gets the SnapshotManager that was passed to  
    * {@link #bindSession(ClusteredSession, SnapshotManager)} if and only
    * if only one such SnapshotManager was passed. Returns <code>null</code>
    * otherwise, in which case a cross-context request is a possibility,
    * and {@link #getCrossContextSessions()} should be checked.
    */
   public SnapshotSipManager getSoleSnapshotSipManager()
   {
      return soleSipManager;
   }

   /**
    * Gets the ClusteredSession that was passed to  
    * {@link #bindSession(ClusteredSession, SnapshotManager)} if and only
    * if only one SnapshotManager was passed. Returns <code>null</code>
    * otherwise, in which case a cross-context request is a possibility,
    * and {@link #getCrossContextSessions()} should be checked.
    */
   public ClusteredSession<? extends OutgoingDistributableSessionData> getSoleSession()
   {
      return soleSession;
   }
   
   /**
    * Gets the ClusteredSession that was passed to  
    * {@link #bindSession(ClusteredSession, SnapshotManager)} if and only
    * if only one SnapshotManager was passed. Returns <code>null</code>
    * otherwise, in which case a cross-context request is a possibility,
    * and {@link #getCrossContextSessions()} should be checked.
    */
   public Set<ClusteredSipSession<? extends OutgoingDistributableSessionData>> getSipSessions()
   {
      return sipSessions;
   }
   
   /**
    * Gets the ClusteredSession that was passed to  
    * {@link #bindSession(ClusteredSession, SnapshotManager)} if and only
    * if only one SnapshotManager was passed. Returns <code>null</code>
    * otherwise, in which case a cross-context request is a possibility,
    * and {@link #getCrossContextSessions()} should be checked.
    */
   public Set<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>> getSipApplicationSessions()
   {
      return sipApplicationSessions;
   }
   
   private void addReplicatableSession(ClusteredSession<? extends OutgoingDistributableSessionData> session, SnapshotManager mgr)
   {
      if (crossCtxSessions != null)
      {
         crossCtxSessions.put(session, mgr);
      }
      else if (soleManager == null)
      {
         // First one bound
         soleManager = mgr;
         soleSession = session;
      }
      else if (!mgr.equals(soleManager))
      {
         // We have a cross-context call; need a Map for the sessions
         crossCtxSessions = new HashMap<ClusteredSession<? extends OutgoingDistributableSessionData>, SnapshotManager>();
         crossCtxSessions.put(soleSession, soleManager);
         crossCtxSessions.put(session, mgr);
         soleManager = null;
         soleSession = null;
      }
      else
      {
         soleSession = session;
      }
   }
   
   private void addReplicatableSipSession(
			ClusteredSipSession<? extends OutgoingDistributableSessionData> session,
			SnapshotSipManager mgr) {
		// if (crossCtxSipSessions != null)
		// {
		// crossCtxSipSessions.put(session, mgr);
		// }
		// else
	   if(logger.isDebugEnabled()){
		   logger.debug("Binding following sip session " + session.getKey());
	   }
		if (soleSipManager == null) {
			// First one bound
			soleSipManager = mgr;
			sipSessions.add(session);
		}
		// else if (!mgr.equals(soleManager))
		// {
		// // We have a cross-context call; need a Map for the sessions
		// crossCtxSipSessions = new HashMap();
		// crossCtxSipSessions.put(soleSipSession, soleManager);
		// crossCtxSipSessions.put(session, mgr);
		// soleManager = null;
		// soleSipSession = null;
		// }
		else {
			sipSessions.add(session);
		}
   }
   
   private void addReplicatableSipApplicationSession(
			ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session,
			SnapshotSipManager mgr) {
		// if (crossCtxSipApplicationSessions != null)
		// {
		// crossCtxSipApplicationSessions.put(session, mgr);
		// }
		// else
	   if(logger.isDebugEnabled()){
		   logger.debug("Binding following sip app session " + session.getKey());
	   }
		if (soleSipManager == null) {
			// First one bound
			soleSipManager = mgr;
			sipApplicationSessions.add(session);
		}
		// else if (!mgr.equals(soleManager))
		// {
		// // We have a cross-context call; need a Map for the sessions
		// crossCtxSipApplicationSessions = new HashMap();
		// crossCtxSipApplicationSessions.put(soleSipApplicationSession,
		// soleManager);
		// crossCtxSipApplicationSessions.put(session, mgr);
		// soleManager = null;
		// soleSipApplicationSession = null;
		// }
		else {
			sipApplicationSessions.add(session);
		}
	}
   
   private void sessionExpired(ClusteredSession<? extends OutgoingDistributableSessionData> session, SnapshotManager manager)
   {
      if (manager.equals(soleManager))
      {
         soleManager = null;
         soleSession = null;
      }
      else if (crossCtxSessions != null)
      {
         crossCtxSessions.remove(session);
      }
   }  
   
   private void sipSessionExpired(ClusteredSipSession<? extends OutgoingDistributableSessionData> session, SnapshotSipManager manager)
   {
      boolean store = manager.equals(soleSipManager);
      if (store)
      {
    	  if(logger.isDebugEnabled()){
    		  logger.debug("Unbinding following sip session " + session.getKey());
    	  }
    	  soleSipManager = null;
    	  sipSessions.remove(session);
      }      
   }
   
   private void sipApplicationSessionExpired(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session, SnapshotSipManager manager)
   {
      boolean store = manager.equals(soleSipManager);
      if (store)
      {
    	  if(logger.isDebugEnabled()){
    		  logger.debug("Unbinding following sip app session " + session.getKey());
    	  }
    	  soleSipManager = null;
    	  sipApplicationSessions.remove(session);
      }      
   }         
}
