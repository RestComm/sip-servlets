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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipApplicationSession.Protocol;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;

public final class ConvergedSessionReplicationContext
{
   private static final ThreadLocal replicationContext = new ThreadLocal();
   private static final ThreadLocal sipReplicationContext = new ThreadLocal();
   
   private static final ConvergedSessionReplicationContext EMPTY = new ConvergedSessionReplicationContext();
   private static final ConvergedSessionReplicationContext EMPTY_SIP = new ConvergedSessionReplicationContext();
   
   private int webappCount;
   private int sipappCount;
   private int activityCount;
   private int sipActivityCount;
   private SnapshotManager soleManager;
   private ClusteredSession soleSession;
   private final Set<ClusteredSipSession> sipSessions = new HashSet<ClusteredSipSession>();
   private final Set<ClusteredSipApplicationSession> sipApplicationSessions = new HashSet<ClusteredSipApplicationSession>();;
   private Map crossCtxSessions;
//   private Map crossCtxSipSessions;
//   private Map crossCtxSipApplicationSessions;
   private Map expiredSessions;
   private Map expiredSipSessions;
   private Map expiredSipApplicationSessions;
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
      if (startCacheActivity)
         ctx.activityCount++;
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
            
            if (ctx.activityCount < 1)
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
      if (startCacheActivity)
         ctx.sipActivityCount++;
   }
   
//   /**
//    * Associate a SessionReplicationContext with the current thread, if
//    * there isn't one already.  If there isn't one, associate the 
//    * given request and response with the context.
//    * <p/>
//    * <strong>NOTE:</strong> Nested calls to this method and {@link #exitWebapp()}
//    * are supported; once a context is established the number of calls to this
//    * method and <code>exitWebapp()</code> are tracked.
//    * 
//    * @param request
//    * @param response
//    */
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
//      if (startCacheActivity)
//         ctx.sipActivityCount++;
//      
//      ClusteredSipApplicationSession clusteredSipApplicationSession = null;
//      ClusteredSipSession clusteredSipSession = null; 
//      if(request != null) {
//    	  clusteredSipSession = (ClusteredSipSession)request.getSipSession();    	  
//      } else {
//    	  clusteredSipSession = (ClusteredSipSession)response.getSipSession();
//      }
//      
//      if(clusteredSipSession.getSipApplicationSession() instanceof org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade) {
//    	  clusteredSipApplicationSession = (ClusteredSipApplicationSession) 
//      		((org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade)
//      			clusteredSipSession.getSipApplicationSession()).getMobicentstSipApplicationSession();
//      } else {
//    	  clusteredSipApplicationSession = (ClusteredSipApplicationSession)clusteredSipSession.getSipApplicationSession();
//      }
//      
//      bindSipApplicationSession(clusteredSipApplicationSession, ((JBossCacheSipManager)manager).getSnapshotManager());
//      bindSipSession(clusteredSipSession, ((JBossCacheSipManager)manager).getSnapshotManager());
//   }
//   
//   public static void enterSipappAndBindSessions(SipApplicationSession appSession, SipManager manager, boolean startCacheActivity)
//   {
//      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
//      if (ctx == null)
//      {
//         ctx = new ConvergedSessionReplicationContext((Request)null, null);
//         sipReplicationContext.set(ctx);
//      }
//      
//      ctx.sipappCount++;
//      if (startCacheActivity)
//         ctx.sipActivityCount++;
//      
//      ClusteredSipApplicationSession clusteredSipApplicationSession = null;
//      
//      if(appSession instanceof org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade) {
//    	  clusteredSipApplicationSession = (ClusteredSipApplicationSession) 
//      		((org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade)
//      				appSession).getMobicentstSipApplicationSession();
//      } else {
//    	  clusteredSipApplicationSession = (ClusteredSipApplicationSession)appSession;
//      }
//
//      SnapshotManager snapshotManager = ((JBossCacheSipManager)manager).getSnapshotManager();
//      
//      bindSipApplicationSession(clusteredSipApplicationSession, snapshotManager);
//      
//      // We also bind all SIP sessions here
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
//    	  bindSipSession(clusteredSipSession, snapshotManager);
//      }
//   }
   
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
            
//            if (ctx.sipActivityCount < 1)
//               sipReplicationContext.set(null);
            
            return ctx;
         }
      }
      
      // A nested valve called us. Just return an empty context
      return EMPTY_SIP;
      
   }
   
   public static void bindSession(ClusteredSession session, SnapshotManager manager)
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
   
   public static void bindSipSession(ClusteredSipSession session, SnapshotManager manager)
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
   
   public static void bindSipApplicationSession(ClusteredSipApplicationSession session, SnapshotManager manager)
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
   
   public static void sessionExpired(ClusteredSession session, String realId, SnapshotManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx != null && ctx.webappCount > 0)
      {
         ctx.addExpiredSession(session, manager);
      }      
   }
   
   public static void sipSessionExpired(ClusteredSipSession session, String realId, SnapshotManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null && ctx.sipappCount > 0)
      {
         ctx.addExpiredSipSession(session, manager);
      }      
   }
   
   public static void sipApplicationSessionExpired(ClusteredSipApplicationSession session, String realId, SnapshotManager manager)
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null && ctx.sipappCount > 0)
      {
         ctx.addExpiredSipApplicationSession(session, manager);
      }      
   }
   
   public static boolean isSessionBoundAndExpired(String realId, SnapshotManager manager)
   {
      boolean result = false;
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx != null)
      {
         result = ctx.isSessionExpired(realId, manager);
      }
      return result;
   }
   
   public static boolean isSipSessionBoundAndExpired(String key, SnapshotManager manager)
   {
      boolean result = false;
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null)
      {
         result = ctx.isSipSessionExpired(key, manager);
      }
      return result;
   }
   
   public static boolean isSipApplicationSessionBoundAndExpired(String key, SnapshotManager manager)
   {
      boolean result = false;
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null)
      {
         result = ctx.isSipApplicationSessionExpired(key, manager);
      }
      return result;
   }
   
   /**
    * Marks the current thread as actively processing the given session.
    * If the thread has already been so marked, increases a counter
    * so a subsequent call to finishLocalActivity does not remove
    * the association (allowing nested calls).
    */
   public static void startCacheActivity()
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx == null)
      {
         ctx = new ConvergedSessionReplicationContext();
         replicationContext.set(ctx);
      }
      
      ctx.activityCount++;
   }
   
   /**
    * Marks the completion of activity on a given session.  Should be called
    * once for each invocation of {@link #startCacheActivity()}.
    */
   public static void finishCacheActivity()
   {
      ConvergedSessionReplicationContext ctx = getCurrentContext();
      if (ctx != null)
      {
         ctx.activityCount--;
         if (ctx.activityCount < 1 && ctx.webappCount < 1)
         {
            replicationContext.set(null);
         }
      }
   }
   
   
   /**
    * Marks the current thread as actively processing the given session.
    * If the thread has already been so marked, increases a counter
    * so a subsequent call to finishLocalActivity does not remove
    * the association (allowing nested calls).
    */
   public static void startSipCacheActivity()
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx == null)
      {
         ctx = new ConvergedSessionReplicationContext();
         sipReplicationContext.set(ctx);
      }
      
      ctx.sipActivityCount++;
   }
   
   /**
    * Marks the completion of activity on a given session.  Should be called
    * once for each invocation of {@link #startCacheActivity()}.
    */
   public static void finishSipCacheActivity()
   {
      ConvergedSessionReplicationContext ctx = getCurrentSipContext();
      if (ctx != null)
      {
         ctx.sipActivityCount--;
         if (ctx.sipActivityCount < 1 && ctx.sipappCount < 1)
         {
        	ctx.sipSessions.clear();
        	ctx.sipApplicationSessions.clear();
            sipReplicationContext.set(null);            
         }
      }
   }
   
   public static boolean isLocallyActive()
   {
      return getCurrentContext() != null;
   }
   
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
      return (ConvergedSessionReplicationContext) replicationContext.get();
   }
   
   private static ConvergedSessionReplicationContext getCurrentSipContext()
   {
      return (ConvergedSessionReplicationContext) sipReplicationContext.get();
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
   public Map getCrossContextSessions()
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
    * Gets the ClusteredSession that was passed to  
    * {@link #bindSession(ClusteredSession, SnapshotManager)} if and only
    * if only one SnapshotManager was passed. Returns <code>null</code>
    * otherwise, in which case a cross-context request is a possibility,
    * and {@link #getCrossContextSessions()} should be checked.
    */
   public ClusteredSession getSoleSession()
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
   public Set<ClusteredSipSession> getSipSessions()
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
   public Set<ClusteredSipApplicationSession> getSipApplicationSessions()
   {
      return sipApplicationSessions;
   }
   
   private void addReplicatableSession(ClusteredSession session, SnapshotManager mgr)
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
         crossCtxSessions = new HashMap();
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
   
   private void addReplicatableSipSession(ClusteredSipSession session, SnapshotManager mgr)
   {
//      if (crossCtxSipSessions != null)
//      {
//         crossCtxSipSessions.put(session, mgr);
//      }
//      else 
    	  if (soleManager == null)
      {
         // First one bound
         soleManager = mgr;
         sipSessions.add(session);
      }
//      else if (!mgr.equals(soleManager))
//      {
//         // We have a cross-context call; need a Map for the sessions
//         crossCtxSipSessions = new HashMap();
//         crossCtxSipSessions.put(soleSipSession, soleManager);
//         crossCtxSipSessions.put(session, mgr);
//         soleManager = null;
//         soleSipSession = null;
//      }
      else
      {
    	  sipSessions.add(session);
      }
   }
   
   private void addReplicatableSipApplicationSession(ClusteredSipApplicationSession session, SnapshotManager mgr)
   {
//      if (crossCtxSipApplicationSessions != null)
//      {
//         crossCtxSipApplicationSessions.put(session, mgr);
//      }
//      else 
    	  if (soleManager == null)
      {
         // First one bound
         soleManager = mgr;
         sipApplicationSessions.add(session);
      }
//      else if (!mgr.equals(soleManager))
//      {
//         // We have a cross-context call; need a Map for the sessions
//         crossCtxSipApplicationSessions = new HashMap();
//         crossCtxSipApplicationSessions.put(soleSipApplicationSession, soleManager);
//         crossCtxSipApplicationSessions.put(session, mgr);
//         soleManager = null;
//         soleSipApplicationSession = null;
//      }
      else
      {
    	  sipApplicationSessions.add(session);
      }
   }
   
   
   private void addExpiredSession(ClusteredSession session, SnapshotManager manager)
   {
      boolean store = manager.equals(soleManager);
      if (store)
      {
         soleManager = null;
         soleSession = null;
      }
      else if (crossCtxSessions != null)
      {
         // Only store the session if it was previously in our map
         store = (crossCtxSessions.remove(session) != null);
      }
      
      if (store)
      {
         if (this.expiredSessions == null)
         {
            expiredSessions = new HashMap();
         }
         expiredSessions.put(manager, session.getRealId());      
      }
   }
   
   private void addExpiredSipSession(ClusteredSipSession session, SnapshotManager manager)
   {
      boolean store = manager.equals(soleManager);
      if (store)
      {
         soleManager = null;
         sipSessions.remove(session);
      }
      else if (crossCtxSessions != null)
      {
         // Only store the session if it was previously in our map
         store = (crossCtxSessions.remove(session) != null);
      }
      
      if (store)
      {
         if (this.expiredSipSessions == null)
         {
            expiredSipSessions = new HashMap();
         }
         expiredSipSessions.put(manager, session.getId());      
      }
   }
   
   private void addExpiredSipApplicationSession(ClusteredSipApplicationSession session, SnapshotManager manager)
   {
      boolean store = manager.equals(soleManager);
      if (store)
      {
         soleManager = null;
         sipApplicationSessions.remove(session);
      }
      else if (crossCtxSessions != null)
      {
         // Only store the session if it was previously in our map
         store = (crossCtxSessions.remove(session) != null);
      }
      
      if (store)
      {
         if (this.expiredSipApplicationSessions == null)
         {
        	 expiredSipApplicationSessions = new HashMap();
         }
         expiredSipApplicationSessions.put(manager, session.getId());      
      }
   }
   
   private boolean isSessionExpired(String realId, SnapshotManager manager)
   {
      boolean result = false;
      if (expiredSessions != null)
      {
         result = realId.equals(expiredSessions.get(manager));
      }      
      return result;
   }

   private boolean isSipSessionExpired(String key, SnapshotManager manager)
   {
      boolean result = false;
      if (expiredSipSessions != null)
      {
         result = key.equals(expiredSipSessions.get(manager));
      }      
      return result;
   }

   
   private boolean isSipApplicationSessionExpired(String key, SnapshotManager manager)
   {
      boolean result = false;
      if (expiredSipApplicationSessions != null)
      {
         result = key.equals(expiredSipApplicationSessions.get(manager));
      }      
      return result;
   }

   
}
