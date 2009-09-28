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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

import javax.management.ObjectName;
import javax.transaction.TransactionManager;

import org.apache.catalina.Context;
import org.jboss.aspects.patterns.observable.Observer;
import org.jboss.aspects.patterns.observable.Subject;
import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.aop.PojoCacheMBean;
import org.jboss.cache.buddyreplication.BuddyManager;
import org.jboss.cache.transaction.BatchModeTransactionManager;
import org.jboss.invocation.MarshalledValue;
import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.invocation.MarshalledValueOutputStream;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxyExt;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Implementation of a converged wrapper jboss cache service.
 * 
 * Based on JBossCacheService JBOSS AS 4.2.2 Tag 
 * I was forced to copy over most of the code since some things that needed to be adapted
 * were private 
 */
public class ConvergedJBossCacheService extends JBossCacheService
{   
   protected static Logger log_ = Logger.getLogger(ConvergedJBossCacheService.class);
   public static final String BUDDY_BACKUP = BuddyManager.BUDDY_BACKUP_SUBTREE;
   public static final Fqn BUDDY_BACKUP_FQN = BuddyManager.BUDDY_BACKUP_SUBTREE_FQN;
   public static final String SESSION = "JSESSION";
   public static final String SIPSESSION = "SIPSESSION";
   public static final String ATTRIBUTE = "ATTRIBUTE";
   // Needed for cache invalidation
   public static final String VERSION_KEY = "VERSION";
   static final String FQN_DELIMITER = "/";
   
   private PojoCacheMBean proxy_;
   
   // name of webapp's virtual host(JBAS-2194). 
   // Idea is host_name + web_app_path + session id is a unique combo.
   private String hostName_;
   // web app path (JBAS-1367 and JBAS-2194). 
   // Idea is host_name + web_app_path + session id is a unique combo.
   private String webAppPath_;
   // Idea is host_name + sip_application_name + session id is a unique combo.
   private String sipApplicationName;
   private TransactionManager tm;

   private JBossCacheSipManager manager_;
   private CacheListener cacheListener_;
   private SipCacheListener sipCacheListener_;
   private JBossCacheWrapper cacheWrapper_;
   
   // Do we have to marshall attributes ourself or can we let 
   // the TreeCache do it?
   private boolean useTreeCacheMarshalling_ = false;
   
   private WeakHashMap typeMap = new WeakHashMap();
   
   /**
    * Creates a JMX proxy PojoCacheMBean for the given object name.
    * 
    * @param objectName the object name
    * @return the proxy
    * @throws ClusteringNotSupportedException if there is a problem
    */
   private static PojoCacheMBean getPojoCacheMBean(String objectName)
      throws ClusteringNotSupportedException
   {
      try
      {
         ObjectName cacheServiceName = new ObjectName(objectName);
         // Create Proxy-Object for this service
         return (PojoCacheMBean) MBeanProxyExt.create(PojoCacheMBean.class,
                                                        cacheServiceName);
      }
      catch (Throwable t)
      {
         String str = "Could not access TreeCache service " + 
                     (objectName == null ? "<null>" : objectName) + 
                     " for Tomcat clustering";
         log_.debug(str);
         throw new ClusteringNotSupportedException(str, t);
      }  
   }
   
   public ConvergedJBossCacheService(PojoCacheMBean pojoCache)
   {
	  super(pojoCache);
      this.proxy_ = pojoCache;

      cacheWrapper_ = new JBossCacheWrapper(proxy_);
      
      useTreeCacheMarshalling_ = proxy_.getUseRegionBasedMarshalling();
   }
   
   public ConvergedJBossCacheService(String treeCacheObjectName) throws ClusteringNotSupportedException
   {
      this(getPojoCacheMBean(treeCacheObjectName));
   }

   public void start(ClassLoader tcl, JBossCacheSipManager manager)
   {
      manager_ = manager;
      
      Context webapp = (Context) manager_.getContainer();      
      String path = webapp.getName();
      if( path.length() == 0 || path.equals("/")) {
         // If this is root.
         webAppPath_ = "ROOT";
      } else if ( path.startsWith("/") ) {
         webAppPath_ = path.substring(1);
      } else {
         webAppPath_ = path;
      }
      // JBAS-3941 -- context path can be multi-level, but we don't
      // want that turning into a multilevel Fqn, so escape it
      // Use '?' which is illegal in a context path
      webAppPath_ = webAppPath_.replace('/', '?');
      log_.debug("Old and new web app path are: " +path + ", " +webAppPath_);
      
      String host = webapp.getParent().getName();
      if( host == null || host.length() == 0) {
         hostName_ = "localhost";
      }else {
         hostName_ = host;
      }
      log_.debug("Old and new virtual host name are: " + host + ", " + hostName_);
      

      // Listen for cache changes
      cacheListener_ = new CacheListener(cacheWrapper_, manager_, hostName_, webAppPath_);
      proxy_.addTreeCacheListener(cacheListener_);

      // register the tcl and bring over the state for the webapp
      Object[] objs = new Object[]{SESSION, hostName_, webAppPath_};
      Fqn pathFqn = new Fqn( objs );
      String fqnStr = pathFqn.toString();
      try {
         if(useTreeCacheMarshalling_)
         {
            log_.debug("UseMarshalling is true. We will register the fqn: " +
                        fqnStr + " with class loader" +tcl +
                        " and activate the webapp's Region");
            proxy_.registerClassLoader(fqnStr, tcl);
            proxy_.activateRegion(fqnStr);
         }
      } catch (Exception ex)
      {
         throw new RuntimeException("Can't register class loader", ex);
      }

      if(webapp instanceof SipContext) {    	  
    	  SipContext sipApp = (SipContext) webapp;
    	  //As per JSR 289, application name should be unique
    	  sipApplicationName = sipApp.getApplicationName();    	  
    	  // Listen for cache changes
          sipCacheListener_ = new SipCacheListener(cacheWrapper_, manager_, hostName_, sipApplicationName);
          proxy_.addTreeCacheListener(sipCacheListener_);

          // register the tcl and bring over the state for the webapp
          Object[] sipObjs = new Object[]{SIPSESSION, hostName_, sipApplicationName};
          Fqn sipPathFqn = new Fqn( sipObjs );
          String sipFqnStr = sipPathFqn.toString();
          log_.info("Adding jboss cache listener for sip application : " + sipApplicationName + " on following fqn : " + sipFqnStr);
          try {
             if(useTreeCacheMarshalling_)
             {
                log_.debug("UseMarshalling is true. We will register the fqn: " +
                            sipFqnStr + " with class loader" +tcl +
                            " and activate the sipapp's Region");
                proxy_.registerClassLoader(sipFqnStr, tcl);
                proxy_.activateRegion(sipFqnStr);
             }
          } catch (Exception ex)
          {
             throw new RuntimeException("Can't register class loader", ex);
          }  
      }
      // We require the cache tm to be BatchModeTransactionManager now.
      tm = proxy_.getTransactionManager();
      if( ! (tm instanceof BatchModeTransactionManager) )
      {
         throw new RuntimeException("JBossCacheService.start(): JBossCacheAop transaction manager is not type BatchModeTransactionManager." +
                 " Please check the tc5-cluster-service.xml TransactionManagerClassLookup field.");
      }
   }

   public void stop()
   {
      proxy_.removeTreeCacheListener(cacheListener_);
      if(sipCacheListener_ != null) {
    	  proxy_.removeTreeCacheListener(sipCacheListener_);
      }

      // Construct the fqn
      Object[] objs = new Object[]{SESSION, hostName_, webAppPath_};
      Fqn pathFqn = new Fqn( objs );

      String fqnStr = pathFqn.toString();
      if(useTreeCacheMarshalling_)
      {
            log_.debug("UseMarshalling is true. We will inactivate the fqn: " +
                       fqnStr + " and un-register its classloader");
            
         try {
            proxy_.inactivateRegion(fqnStr);
            proxy_.unregisterClassLoader(fqnStr);          
         }
         catch (Exception e) 
         {
            log_.error("Exception during inactivation of webapp region " + fqnStr + 
                        " or un-registration of its class loader", e);
         }
      }

      // remove session data
      cacheWrapper_.evictSubtree(pathFqn);
      
      if(sipApplicationName != null) {
	      // Construct the fqn
	      Object[] sipObjs = new Object[]{SIPSESSION, hostName_, sipApplicationName};
	      Fqn sipPathFqn = new Fqn( sipObjs );
	
	      String sipFqnStr = sipPathFqn.toString();
	      if(useTreeCacheMarshalling_)
	      {
	            log_.debug("UseMarshalling is true. We will inactivate the fqn: " +
	            		sipFqnStr + " and un-register its classloader");
	            
	         try {
	            proxy_.inactivateRegion(sipFqnStr);
	            proxy_.unregisterClassLoader(sipFqnStr);          
	         }
	         catch (Exception e) 
	         {
	            log_.error("Exception during inactivation of sipapp region " + sipFqnStr + 
	                        " or un-registration of its class loader", e);
	         }
	      }
	
	      // remove session data
	      cacheWrapper_.evictSubtree(pathFqn);
      }
   }

   /**
    * Get specfically the BatchModeTransactionManager.
    */
   public TransactionManager getTransactionManager()
   {
      return tm;
   }
   
   
   /**
    * Gets whether TreeCache-based marshalling is available
    */
   public boolean isMarshallingAvailable()
   {
      return useTreeCacheMarshalling_;
   }

   /**
    * Loads any serialized data in the cache into the given session
    * using its <code>readExternal</code> method.
    *
    * @return the session passed as <code>toLoad</code>, or
    *         <code>null</code> if the cache had no data stored
    *         under the given session id.
    */
   public ClusteredSession loadSession(String realId, ClusteredSession toLoad)
   {
	   if(log_.isDebugEnabled()) {
		   log_.debug("loading session from the cache " + realId);
	   }
	   Fqn fqn = getSessionFqn(realId);
   
      
	   Object sessionData = cacheWrapper_.get(fqn, realId, true);
      
	   if (sessionData == null) {
		   // Requested session is no longer in the cache; return null
		   return null;
	   }
      
	   boolean firstLoad = (toLoad.getVersion() == 0);
      
//      if (useTreeCacheMarshalling_)
//      {
//         toLoad.update((ClusteredSession) sessionData);
//      }
//      else
//      {
       byte[] sessionBytes = (byte[]) sessionData;
         
       // Swap in/out the webapp classloader so we can deserialize
       // attributes whose classes are only available to the webapp
       ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
       Thread.currentThread().setContextClassLoader(manager_.getWebappClassLoader());
       try {
    	   ByteArrayInputStream bais = new ByteArrayInputStream(sessionBytes);
    	   // Use MarshalledValueInputStream instead of superclass ObjectInputStream
    	   // or else there are problems finding classes with scoped loaders
    	   MarshalledValueInputStream input = new MarshalledValueInputStream(bais);
    	   toLoad.readExternal(input);
    	   input.close();
       } catch (Exception e) {
    	   log_.error("loadSession(): id: " + realId + " exception occurred during deserialization", e);
    	   return null;
       } finally {
    	   Thread.currentThread().setContextClassLoader(prevTCL);
       }
//      }
      
       // The internal version of the serialized session may be less than the
       // real one due to not replicating metadata.  If our listener hasn't 
       // been keeping the outdatedVersion of the session up to date because
       // the session has never been loaded into the JBCManager cache, we 
       // need to fix the version
       if (firstLoad) {         
    	   Integer ver = (Integer) cacheWrapper_.get(fqn, VERSION_KEY);
    	   if (ver != null)
    		   toLoad.setVersion(ver.intValue());
       }
      
       return toLoad;
   }
   
   /**
    * Loads any serialized data in the cache into the given session
    * using its <code>readExternal</code> method.
    *
    * @return the session passed as <code>toLoad</code>, or
    *         <code>null</code> if the cache had no data stored
    *         under the given session id.
    */
   public Object getSipSessionData(String id, String realId)
   {
	  if(log_.isDebugEnabled()) {
		  log_.debug("getting sip session data from the cache " + id + " / " + realId);
	  }
      Fqn fqn = getSipSessionFqn(id, realId);
   
      return cacheWrapper_.get(fqn, realId, true);            
   }
   
   /**
    * Loads any serialized data in the cache into the given session
    * using its <code>readExternal</code> method.
    *
    * @return the session passed as <code>toLoad</code>, or
    *         <code>null</code> if the cache had no data stored
    *         under the given session id.
    */
   public ClusteredSipSession loadSipSession(String id, ClusteredSipSession toLoad, Object sessionData)
   {
	   String realId = toLoad.getId();
	  if(log_.isDebugEnabled()) {
		  log_.debug("loading sip session from the cache " + id + " / " + realId);
	  }
     
      boolean firstLoad = (toLoad.getVersion() == 0);
      
//      if (useTreeCacheMarshalling_)
//      {
//         toLoad.update((ClusteredSession) sessionData);
//      }
//      else
//      {
         byte[] sessionBytes = (byte[]) sessionData;
         
         // Swap in/out the webapp classloader so we can deserialize
         // attributes whose classes are only available to the webapp
         ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(manager_.getWebappClassLoader());
         try
         {
            ByteArrayInputStream bais = new ByteArrayInputStream(sessionBytes);
            // Use MarshalledValueInputStream instead of superclass ObjectInputStream
            // or else there are problems finding classes with scoped loaders
            MarshalledValueInputStream input = new MarshalledValueInputStream(bais);
            toLoad.readExternal(input);
            input.close();
         }
         catch (Exception e)
         {
            log_.error("loadSession(): id: " + realId + " exception occurred during deserialization", e);
            return null;
         }
         finally {
            Thread.currentThread().setContextClassLoader(prevTCL);
         }
//      }
      
      // The internal version of the serialized session may be less than the
      // real one due to not replicating metadata.  If our listener hasn't 
      // been keeping the outdatedVersion of the session up to date because
      // the session has never been loaded into the JBCManager cache, we 
      // need to fix the version
      if (firstLoad)
      {         
    	 Fqn fqn = getSipSessionFqn(id, realId);
         Integer ver = (Integer) cacheWrapper_.get(fqn, VERSION_KEY);
         if (ver != null)
            toLoad.setVersion(ver.intValue());
      }
      
      return toLoad;
   }

   /**
    * Loads any serialized data in the cache into the given session
    * using its <code>readExternal</code> method.
    *
    * @return the session passed as <code>toLoad</code>, or
    *         <code>null</code> if the cache had no data stored
    *         under the given session id.
    */
   public Object getSipApplicationSessionData(String realId)
   {
	   if(log_.isDebugEnabled()) {
		   log_.debug("loading sip app session from the cache " + realId);
	   }
	   Fqn fqn = getSipApplicationSessionFqn(realId);
   
	   return cacheWrapper_.get(fqn, realId, true);
   }

   /**
    * Loads any serialized data in the cache into the given session
    * using its <code>readExternal</code> method.
    *
    * @return the session passed as <code>toLoad</code>, or
    *         <code>null</code> if the cache had no data stored
    *         under the given session id.
    */
   public ClusteredSipApplicationSession loadSipApplicationSession(ClusteredSipApplicationSession toLoad, Object sessionData)
   {	   
	   	boolean firstLoad = (toLoad.getVersion() == 0);
      
//      if (useTreeCacheMarshalling_)
//      {
//         toLoad.update((ClusteredSession) sessionData);
//      }
//      else
//      {
         byte[] sessionBytes = (byte[]) sessionData;
         
         // Swap in/out the webapp classloader so we can deserialize
         // attributes whose classes are only available to the webapp
         ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(manager_.getWebappClassLoader());
         try
         {
            ByteArrayInputStream bais = new ByteArrayInputStream(sessionBytes);
            // Use MarshalledValueInputStream instead of superclass ObjectInputStream
            // or else there are problems finding classes with scoped loaders
            MarshalledValueInputStream input = new MarshalledValueInputStream(bais);
            toLoad.readExternal(input);
            input.close();
         }
         catch (Exception e)
         {
            log_.error("loadSession(): id: " + toLoad.getId() + " exception occurred during deserialization", e);
            return null;
         }
         finally {
            Thread.currentThread().setContextClassLoader(prevTCL);
         }
//      }
      
      // The internal version of the serialized session may be less than the
      // real one due to not replicating metadata.  If our listener hasn't 
      // been keeping the outdatedVersion of the session up to date because
      // the session has never been loaded into the JBCManager cache, we 
      // need to fix the version
      if (firstLoad)
      {         
    	  Fqn fqn = getSipApplicationSessionFqn(toLoad.getId());
    	  Integer ver = (Integer) cacheWrapper_.get(fqn, VERSION_KEY);
    	  if (ver != null)
            toLoad.setVersion(ver.intValue());
      }
      
      return toLoad;
   }


   public void putSession(String realId, ClusteredSession session)
   {
      Fqn fqn = getSessionFqn(realId);
      
      if (session.getReplicateSessionBody())
      {
         Map map = new HashMap();
//         if (useTreeCacheMarshalling_)
//            map.put(realId, session);
//         else
            map.put(realId, externalizeSession(session));
         // Put in (VERSION_KEY, version) after the real put for cache invalidation
         map.put(VERSION_KEY, new Integer(session.getVersion()));
         cacheWrapper_.put(fqn, map);
      }
      else
      {
         // Invalidate the remote caches
         cacheWrapper_.put(fqn, VERSION_KEY, new Integer(session.getVersion()));
      }
   }
   
   public void putSipSession(String realId, ClusteredSipSession session)
   {
      Fqn fqn = getSipSessionFqn(session.getSipApplicationSession().getId(), realId);
      
      if (session.getReplicateSessionBody())
      {
         Map map = new HashMap();
//         if (useTreeCacheMarshalling_)
//            map.put(realId, session);
//         else
            map.put(realId, externalizeSipSession(session));
         // Put in (VERSION_KEY, version) after the real put for cache invalidation
         map.put(VERSION_KEY, new Integer(session.getVersion()));
         cacheWrapper_.put(fqn, map);
      }
      else
      {
         // Invalidate the remote caches
         cacheWrapper_.put(fqn, VERSION_KEY, new Integer(session.getVersion()));
      }
   }
   
   public void putSipApplicationSession(String realId, ClusteredSipApplicationSession session)
   {
      Fqn fqn = getSipApplicationSessionFqn(realId);
      
      if (session.getReplicateSessionBody())
      {
         Map map = new HashMap();
//         if (useTreeCacheMarshalling_)
//            map.put(realId, session);
//         else
            map.put(realId, externalizeSipApplicationSession(session));
         // Put in (VERSION_KEY, version) after the real put for cache invalidation
         map.put(VERSION_KEY, new Integer(session.getVersion()));
         cacheWrapper_.put(fqn, map);
      }
      else
      {
         // Invalidate the remote caches
         cacheWrapper_.put(fqn, VERSION_KEY, new Integer(session.getVersion()));
      }
   }

   public void removeSession(String realId)
   {
      Fqn fqn = getSessionFqn(realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove session from distributed store. Fqn: " + fqn);
      }
      //Object obj = getUnMarshalledValue(cacheWrapper_.remove(fqn, realId));
      cacheWrapper_.remove(fqn, realId); 
      // This needs to go after object removal to support correct cache invalidation.
//      _remove(fqn, VERSION_KEY);
      // Let just remove the whole thing (including the fqn)
      cacheWrapper_.remove(fqn);
      //return obj;
   }
   public void removeSipSession(String sipApplicationSessionId, String realId)
   {
      Fqn fqn = getSipSessionFqn(sipApplicationSessionId, realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove session from distributed store. Fqn: " + fqn);
      }
      //Object obj = getUnMarshalledValue(cacheWrapper_.remove(fqn, realId));
      cacheWrapper_.remove(fqn, realId); 
      // This needs to go after object removal to support correct cache invalidation.
//      _remove(fqn, VERSION_KEY);
      // Let just remove the whole thing (including the fqn)
      cacheWrapper_.remove(fqn);
      //return obj;
   }
   public void removeSipApplicationSession(String realId)
   {
      Fqn fqn = getSipApplicationSessionFqn(realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove session from distributed store. Fqn: " + fqn);
      }
      //Object obj = getUnMarshalledValue(cacheWrapper_.remove(fqn, realId));
      cacheWrapper_.remove(fqn, realId); 
      // This needs to go after object removal to support correct cache invalidation.
//      _remove(fqn, VERSION_KEY);
      // Let just remove the whole thing (including the fqn)
      cacheWrapper_.remove(fqn);
      //return obj;
   }

   public void removeSessionLocal(String realId)
   {
      Fqn fqn = getSessionFqn(realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove session from my own distributed store only. Fqn: " + fqn);
      }
      cacheWrapper_.evictSubtree(fqn);
   }
   
   public void removeSipSessionLocal(String id, String realId)
   {
      Fqn fqn = getSipSessionFqn(id, realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove session from my own distributed store only. Fqn: " + fqn);
      }
      cacheWrapper_.evictSubtree(fqn);
   }
   
   public void removeSipApplicationSessionLocal(String realId)
   {
      Fqn fqn = getSipApplicationSessionFqn(realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove session from my own distributed store only. Fqn: " + fqn);
      }
      cacheWrapper_.evictSubtree(fqn);
   }

   public void removeSessionLocal(String realId, String dataOwner)
   {
      if (dataOwner == null)
      {
         removeSessionLocal(realId);
      }
      else
      {         
         Fqn fqn = getSessionFqn(realId, dataOwner);
         if (log_.isDebugEnabled())
         {
            log_.debug("Remove session from my own distributed store only. Fqn: " + fqn);
         }
         cacheWrapper_.evictSubtree(fqn);
      }
   }

   public boolean exists(String realId)
   {
      Fqn fqn = getSessionFqn(realId);
      return proxy_.exists(fqn);
   }

   public Object getAttribute(String realId, String key)
   {
      Fqn fqn = getAttributeFqn(realId);
      return getUnMarshalledValue(cacheWrapper_.get(fqn, key));
   }

   public Object getSipApplicationSessionAttribute(String realId, String key)
   {
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      return getUnMarshalledValue(cacheWrapper_.get(fqn, key));
   }

   public Object getSipSessionAttribute(String id, String realId, String key)
   {
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      return getUnMarshalledValue(cacheWrapper_.get(fqn, key));
   }
   
   public void putAttribute(String realId, String key, Object value)
   {
      Fqn fqn = getAttributeFqn(realId);
      cacheWrapper_.put(fqn, key, getMarshalledValue(value));
   }
   
   public void putSipApplicationSessionAttribute(String realId, String key, Object value)
   {
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      cacheWrapper_.put(fqn, key, getMarshalledValue(value));
   }
   
   public void putSipSessionAttribute(String id, String realId, String key, Object value)
   {
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      cacheWrapper_.put(fqn, key, getMarshalledValue(value));
   }

   public void putAttribute(String realId, Map map)
   {
      // Duplicate the map with marshalled values
      Map marshalled = new HashMap(map.size());
      Set entries = map.entrySet();
      for (Iterator it = entries.iterator(); it.hasNext(); )
      {
         Map.Entry entry = (Map.Entry) it.next();
         marshalled.put(entry.getKey(), getMarshalledValue(entry.getValue()));
      }
      
      Fqn fqn = getAttributeFqn(realId);
      cacheWrapper_.put(fqn, marshalled);
      
   }

   public void putSipApplicationSessionAttribute(String realId, Map map)
   {
      // Duplicate the map with marshalled values
      Map marshalled = new HashMap(map.size());
      Set entries = map.entrySet();
      for (Iterator it = entries.iterator(); it.hasNext(); )
      {
         Map.Entry entry = (Map.Entry) it.next();
         marshalled.put(entry.getKey(), getMarshalledValue(entry.getValue()));
      }
      
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      cacheWrapper_.put(fqn, marshalled);
      
   }

   
   public void putSipSessionAttribute(String id, String realId, Map map)
   {
      // Duplicate the map with marshalled values
      Map marshalled = new HashMap(map.size());
      Set entries = map.entrySet();
      for (Iterator it = entries.iterator(); it.hasNext(); )
      {
         Map.Entry entry = (Map.Entry) it.next();
         marshalled.put(entry.getKey(), getMarshalledValue(entry.getValue()));
      }
      
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      cacheWrapper_.put(fqn, marshalled);
      
   }

   public void removeAttributes(String realId)
   {
      Fqn fqn = getAttributeFqn(realId);
      cacheWrapper_.remove(fqn);
   }

   public Object removeAttribute(String realId, String key)
   {
      Fqn fqn = getAttributeFqn(realId);
      if (log_.isTraceEnabled())
      {
         log_.trace("Remove attribute from distributed store. Fqn: " + fqn + " key: " + key);
      }
      return getUnMarshalledValue(cacheWrapper_.remove(fqn, key));
   }
   
   public Object removeSipApplicationSessionAttribute(String realId, String key)
   {
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      if (log_.isTraceEnabled())
      {
         log_.trace("Remove attribute from distributed store. Fqn: " + fqn + " key: " + key);
      }
      return getUnMarshalledValue(cacheWrapper_.remove(fqn, key));
   }
   
   public Object removeSipSessionAttribute(String id, String realId, String key)
   {
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      if (log_.isTraceEnabled())
      {
         log_.trace("Remove attribute from distributed store. Fqn: " + fqn + " key: " + key);
      }
      return getUnMarshalledValue(cacheWrapper_.remove(fqn, key));
   }

   public void removeAttributesLocal(String realId)
   {
      Fqn fqn = getAttributeFqn(realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove attributes from my own distributed store only. Fqn: " + fqn);
      }
      cacheWrapper_.evict(fqn);
   }
   
   public void removeSipApplicationSessionAttributesLocal(String realId)
   {
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove attributes from my own distributed store only. Fqn: " + fqn);
      }
      cacheWrapper_.evict(fqn);
   }
   
   public void removeSipSessionAttributesLocal(String id, String realId)
   {
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      if (log_.isDebugEnabled())
      {
         log_.debug("Remove attributes from my own distributed store only. Fqn: " + fqn);
      }
      cacheWrapper_.evict(fqn);
   }

   /**
    * Obtain the keys associated with this fqn. Note that it is not the fqn children.
    *
    */
   public Set getAttributeKeys(String realId)
   {
      Set keys = null;
      Fqn fqn = getAttributeFqn(realId);
      try
      {
         keys = proxy_.getKeys(fqn);
      }
      catch (CacheException e)
      {
         log_.error("getAttributeKeys(): Exception getting keys for session " + realId, e);
      }
      
      return keys;
   }

   /**
    * Return all attributes associated with this session id.
    *
    * @param realId the session id with any jvmRoute removed
    * @return the attributes, or any empty Map if none are found.
    */
   public Map getAttributes(String realId)
   {
      if (realId == null || realId.length() == 0) return new HashMap();
      
      Map map = new HashMap();
      Set set = getAttributeKeys(realId);
      if(set != null)
      {
         for (Iterator it = set.iterator(); it.hasNext();)
         {
            String key = (String) it.next();
            Object value = getAttribute(realId, key);
            map.put(key, value);
         }
      }
      return map;
   }
   
   /**
    * Return all attributes associated with this session id.
    *
    * @param realId the session id with any jvmRoute removed
    * @return the attributes, or any empty Map if none are found.
    */
   public Map getSipApplicationSessionAttributes(String realId)
   {
      if (realId == null || realId.length() == 0) return new HashMap();
      
      Map map = new HashMap();
      Set set = getAttributeKeys(realId);
      if(set != null)
      {
         for (Iterator it = set.iterator(); it.hasNext();)
         {
            String key = (String) it.next();
            Object value = getSipApplicationSessionAttribute(realId, key);
            map.put(key, value);
         }
      }
      return map;
   }
   
   /**
    * Return all attributes associated with this session id.
    *
    * @param realId the session id with any jvmRoute removed
    * @return the attributes, or any empty Map if none are found.
    */
   public Map getSipSessionAttributes(String id, String realId)
   {
      if (realId == null || realId.length() == 0) return new HashMap();
      
      Map map = new HashMap();
      Set set = getAttributeKeys(realId);
      if(set != null)
      {
         for (Iterator it = set.iterator(); it.hasNext();)
         {
            String key = (String) it.next();
            Object value = getSipSessionAttribute(id, realId, key);
            map.put(key, value);
         }
      }
      return map;
   }

   /**
    * Gets the ids of all sessions in the underlying cache.
    *
    * @return Set containing all of the session ids of sessions in the cache
    *         (with any jvmRoute removed) or <code>null</code> if there
    *         are no sessions in the cache.
    */
   public Map getSessionIds() throws CacheException
   {
      Map result = new HashMap();
      Set owners = proxy_.getChildrenNames(BUDDY_BACKUP_FQN);
      if (owners != null)
      {
         for (Iterator it = owners.iterator(); it.hasNext();)
         {
            Object owner = it.next();
            Set ids = proxy_.getChildrenNames(getWebappFqn(owner));
            storeSessionOwners(ids, owner, result);
         }
      }
      storeSessionOwners(proxy_.getChildrenNames(getWebappFqn()), null, result);

      return result;
   }
   
   /**
    * Gets the ids of all sessions in the underlying cache.
    *
    * @return Set containing all of the session ids of sessions in the cache
    *         (with any jvmRoute removed) or <code>null</code> if there
    *         are no sessions in the cache.
    */
   public Map getSipSessionIds(Set sipApplicationSessionIds, Object owner) throws CacheException
   {
	   Map result = new HashMap();
	  for (Object object : sipApplicationSessionIds) {
          Set ids = proxy_.getChildrenNames(getSipApplicationSessionFqn((String)object, (String)owner));
          storeSessionOwners(ids, owner, result);
	      
	  }
	  return result;
      
   }
   
   /**
    * Gets the ids of all sessions in the underlying cache.
    *
    * @return Set containing all of the session ids of sessions in the cache
    *         (with any jvmRoute removed) or <code>null</code> if there
    *         are no sessions in the cache.
    */
   public Map getSipApplicationSessionIds() throws CacheException
   {
      Map result = new HashMap();
      Set owners = proxy_.getChildrenNames(BUDDY_BACKUP_FQN);
      if (owners != null)
      {
         for (Iterator it = owners.iterator(); it.hasNext();)
         {
            Object owner = it.next();
            Set ids = proxy_.getChildrenNames(getSipappFqn(owner));
            storeSessionOwners(ids, owner, result);
            getSipSessionIds(ids, owner);
         }
      }
      storeSessionOwners(proxy_.getChildrenNames(getSipappFqn()), null, result);

      return result;
   }
   
   private void storeSessionOwners(Set ids, Object owner, Map map)
   {
      if (ids != null)
      {
         for (Iterator it = ids.iterator(); it.hasNext();)
         {
            map.put(it.next(), owner);
         }
      }
   }

   /**
    * store the pojo instance in the cache. Note that this is for the aop cache.
    * THe pojo needs to be "aspectized".
    * 
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @param pojo
    */
   public Object setPojo(String realId, String key, Object pojo)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("setPojo(): session id: " + realId + " key: " + key + 
                    " object: " + pojo.toString());
      }
      // Construct the fqn.
      Fqn fqn = getFieldFqn(realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startCacheActivity();
         return proxy_.putObject(fqn, pojo);
      } catch (CacheException e) {
         throw new RuntimeException("JBossCacheService: exception occurred in cache setPojo ... ", e);
      }
      finally {
         ConvergedSessionReplicationContext.finishCacheActivity();
      }
   }
   
   /**
    * store the pojo instance in the cache. Note that this is for the aop cache.
    * THe pojo needs to be "aspectized".
    * 
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @param pojo
    */
   public Object setSipApplicationSessionPojo(String realId, String key, Object pojo)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("setPojo(): sipa app session id: " + realId + " key: " + key + 
                    " object: " + pojo.toString());
      }
      // Construct the fqn.
      Fqn fqn = getSipApplicationSessionFieldFqn(realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         return proxy_.putObject(fqn, pojo);
      } catch (CacheException e) {
         throw new RuntimeException("JBossCacheService: exception occurred in cache setSipApplicationSessionPojo ... ", e);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }
   
   /**
    * store the pojo instance in the cache. Note that this is for the aop cache.
    * THe pojo needs to be "aspectized".
    * 
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @param pojo
    */
   public Object setSipSessionPojo(String id, String realId, String key, Object pojo)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("setPojo(): sip session id: " + realId + " key: " + key + 
                    " object: " + pojo.toString());
      }
      // Construct the fqn.
      Fqn fqn = getSipSessionFieldFqn(id, realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         return proxy_.putObject(fqn, pojo);
      } catch (CacheException e) {
         throw new RuntimeException("JBossCacheService: exception occurred in cache setSipSessionPojo ... ", e);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }

   /**
    * Remove pojo from the underlying cache store.
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @return pojo that just removed. Null if there none.
    */
   public Object removePojo(String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("removePojo(): session id: " +realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getFieldFqn(realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startCacheActivity();
         return proxy_.removeObject(fqn);
      } catch (CacheException e) {
         throw new RuntimeException("JBossCacheService: exception occurred in cache removePojo ... ", e);
      }
      finally {
         ConvergedSessionReplicationContext.finishCacheActivity();
      }
   }
   
   /**
    * Remove pojo from the underlying cache store.
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @return pojo that just removed. Null if there none.
    */
   public Object removeSipApplicationSessionPojo(String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("removePojo(): sip app session id: " +realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getSipApplicationSessionFieldFqn(realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         return proxy_.removeObject(fqn);
      } catch (CacheException e) {
         throw new RuntimeException("JBossCacheService: exception occurred in cache removeSipApplicationSessionPojo ... ", e);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }
   
   /**
    * Remove pojo from the underlying cache store.
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @return pojo that just removed. Null if there none.
    */
   public Object removeSipSessionPojo(String id, String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("removePojo(): sip session id: " +realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getSipSessionFieldFqn(id, realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         return proxy_.removeObject(fqn);
      } catch (CacheException e) {
         throw new RuntimeException("JBossCacheService: exception occurred in cache removeSipSessionPojo ... ", e);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }

   /**
    * Remove all the pojos from the underlying cache store locally 
    * without replication.
    * 
    * @param realId the session id with any jvmRoute removed
    */
   public void removePojosLocal(String realId)
   {
      if(log_.isDebugEnabled())
      {
         log_.debug("removePojoLocal(): session id: " +realId);
      }
      // Construct the fqn.
      Fqn fqn = getAttributeFqn(realId);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startCacheActivity();
         cacheWrapper_.evictSubtree(fqn);
      }
      finally {
         ConvergedSessionReplicationContext.finishCacheActivity();
      }
   }
   /**
    * Remove all the pojos from the underlying cache store locally 
    * without replication.
    * 
    * @param realId the session id with any jvmRoute removed
    */
   public void removeSipSessionPojosLocal(String id, String realId)
   {
      if(log_.isDebugEnabled())
      {
         log_.debug("removeSipSessionPojosLocal(): session id: " +realId);
      }
      // Construct the fqn.
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         cacheWrapper_.evictSubtree(fqn);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }

   /**
    * Remove all the pojos from the underlying cache store locally 
    * without replication.
    * 
    * @param realId the session id with any jvmRoute removed
    */
   public void removeSipApplicationSessionPojosLocal(String realId)
   {
      if(log_.isDebugEnabled())
      {
         log_.debug("removeSipApplicationSessionPojosLocal(): session id: " +realId);
      }
      // Construct the fqn.
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         cacheWrapper_.evictSubtree(fqn);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }

   /**
    * Remove all the pojos from the underlying cache store locally 
    * without replication.
    * 
    * @param realId the session id with any jvmRoute removed
    */
   public void removePojoLocal(String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("removePojoLocal(): session id: " + realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getFieldFqn(realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startCacheActivity();
         cacheWrapper_.evictSubtree(fqn);
      }
      finally {
         ConvergedSessionReplicationContext.finishCacheActivity();
      }
   }
   
   /**
    * Remove all the pojos from the underlying cache store locally 
    * without replication.
    * 
    * @param realId the session id with any jvmRoute removed
    */
   public void removeSipApplicationSessionPojoLocal(String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("removeSipApplicationPojoLocal(): sip app session id: " + realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getSipApplicationSessionFieldFqn(realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         cacheWrapper_.evictSubtree(fqn);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }
   
   /**
    * Remove all the pojos from the underlying cache store locally 
    * without replication.
    * 
    * @param realId the session id with any jvmRoute removed
    */
   public void removeSipSessionPojoLocal(String id, String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("removeSipSessionPojoLocal(): sip session id: " + realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getSipSessionFieldFqn(id, realId, key);
      try {
         // Ignore any cache notifications that our own work generates 
         ConvergedSessionReplicationContext.startSipCacheActivity();
         cacheWrapper_.evictSubtree(fqn);
      }
      finally {
         ConvergedSessionReplicationContext.finishSipCacheActivity();
      }
   }
   
   public Set getPojoKeys(String realId)
   {
      Set keys = null;
      Fqn fqn = getAttributeFqn(realId);
      try
      {
         keys = proxy_.getChildrenNames(fqn);
      }
      catch (CacheException e)
      {
         log_.error("getPojoKeys(): Exception getting keys for session " + realId, e);
      }
      
      return keys;      
   }
   
   public Set getSipApplicationSessionPojoKeys(String realId)
   {
      Set keys = null;
      Fqn fqn = getSipApplicationSessionAttributeFqn(realId);
      try
      {
         keys = proxy_.getChildrenNames(fqn);
      }
      catch (CacheException e)
      {
         log_.error("getPojoKeys(): Exception getting keys for session " + realId, e);
      }
      
      return keys;      
   }
   
   public Set getSipSessionPojoKeys(String id, String realId)
   {
      Set keys = null;
      Fqn fqn = getSipSessionAttributeFqn(id, realId);
      try
      {
         keys = proxy_.getChildrenNames(fqn);
      }
      catch (CacheException e)
      {
         log_.error("getPojoKeys(): Exception getting keys for session " + realId, e);
      }
      
      return keys;      
   }
   

   /**
    *
    * @param realId the session id with any jvmRoute removed
    * @param key    the attribute key
    * @return Pojo that is associated with the attribute
    */
   public Object getPojo(String realId, String key)
   {
      if(log_.isTraceEnabled())
      {
         log_.trace("getPojo(): session id: " +realId + " key: " +key);
      }
      // Construct the fqn.
      Fqn fqn = getFieldFqn(realId, key);
      
      try 
      {
         return proxy_.getObject(fqn);
      } 
      catch (CacheException e) 
      {
         throw new RuntimeException("JBossCacheService: exception occurred in cache getPojo ... ", e);
      }
   }
   
   /**
   *
   * @param realId the session id with any jvmRoute removed
   * @param key    the attribute key
   * @return Pojo that is associated with the attribute
   */
  public Object getSipApplicationSessionPojo(String realId, String key)
  {
     if(log_.isTraceEnabled())
     {
        log_.trace("getSipApplicationSessionPojo(): sip app session id: " +realId + " key: " +key);
     }
     // Construct the fqn.
     Fqn fqn = getSipApplicationSessionFieldFqn(realId, key);
     
     try 
     {
        return proxy_.getObject(fqn);
     } 
     catch (CacheException e) 
     {
        throw new RuntimeException("JBossCacheService: exception occurred in cache getSipApplicationPojo ... ", e);
     }
  }
  
  /**
  *
  * @param realId the session id with any jvmRoute removed
  * @param key    the attribute key
  * @return Pojo that is associated with the attribute
  */
 public Object getSipSessionPojo(String id, String realId, String key)
 {
    if(log_.isTraceEnabled())
    {
       log_.trace("getSipSessionPojo(): sip session id: " +realId + " key: " +key);
    }
    // Construct the fqn.
    Fqn fqn = getSipSessionFieldFqn(id, realId, key);
    
    try 
    {
       return proxy_.getObject(fqn);
    } 
    catch (CacheException e) 
    {
       throw new RuntimeException("JBossCacheService: exception occurred in cache getSipSessionPojo ... ", e);
    }
 }

   /**
    * Recursively adds session as observer to the pojo graph. Assumes the 
    * whole object graph has Subject "introduction" declared. If a portion
    * of the graph isn't a Subject, the recursion does not continue below
    * that part of the graph.
    *  
    * @param session  the session
    * @param pojo     the pojo.  Can be <code>null</code>.
    */
   public void addObserver(Observer session, Object pojo)
   {
      addObserver(session, pojo, new HashSet());
   }
   
   private void addObserver(Observer session, Object pojo, Set processed)
   {
      if ( pojo instanceof Collection )
      {
         Collection col = (Collection)pojo;
         for (Iterator i = col.iterator(); i.hasNext();) {
            // If not a managed pojo, will return anyway
            addObserver(session, i.next(), processed);
         }

         return;
      } 
      else if (pojo instanceof Map)
      {
         for (Iterator i = ((Map)pojo).entrySet().iterator(); i.hasNext();) 
         {
            Map.Entry entry = (Map.Entry) i.next();

            // Walk thru key and value
            addObserver(session, entry.getKey(), processed);
            addObserver(session, entry.getValue(), processed);
         }

         return;
      }

      if(! (pojo instanceof Subject) )
      {
         return;  // No need to add observer since it is primitive.
      }

      Subject subject = (Subject)pojo;
      subject.addObserver(session);
      
      if(log_.isTraceEnabled())
      {
         log_.trace("addObserver(): session: " +session + " pojo name: " +pojo.getClass().getName());
      }
      
      // Examine each field of the type and its superclasses to see if
      // we need to add the observer to the pojo held by that field
      // Traverse recursively
      
      // First identify and cache the names of all the class' 
      // non-immediate fields 
      Class type = pojo.getClass();      
      Set complexFields = (Set) typeMap.get(type);
      if (complexFields == null)
      {
         complexFields = Util.parseComplexFields(type);
         typeMap.put(type, complexFields);
      }
      
      if (complexFields.size() == 0)
         return;

      // Store a ref to the pojo to avoid cyclic additions
      processed.add(pojo);
      
      for (Iterator iter = complexFields.iterator(); iter.hasNext();)
      {
         String fieldName = (String) iter.next();
         Class curType = type;
         while (curType != null)
         {
            try
            {
               Field field = curType.getDeclaredField(fieldName);
               boolean accessible = field.isAccessible();
               Object value = null;
               try 
               {
                  field.setAccessible(true);
                  
                  value=field.get(pojo);
                  // Continue recursively unless we've already handled this value
                  if (value != null && !processed.contains(value))
                     addObserver(session, value, processed);
                  break;
               }
               catch(IllegalAccessException e) 
               {
                  throw new RuntimeException("field access failed", e);
               }
               finally
               {
                  field.setAccessible(accessible);
               }
            }
            catch (NoSuchFieldException e)
            {
               // Check if the field is declared in a superclass
               curType = curType.getSuperclass();
               if (curType == null)
                  throw new RuntimeException("Field "  + fieldName + 
                        " does not exist", e);
            }
         }
      }
   }

   /**
    * Recursively removes session as observer to the pojo graph. Assumes the 
    * whole object graph has Subject "introduction" declared. If a portion
    * of the graph isn't a Subject, the recursion does not continue below
    * that part of the graph.
    *  
    * @param session  the session
    * @param pojo the pojo to stop observing.  Can be <code>null</code>.
    */
   public void removeObserver(Observer session, Object pojo)
   {
      removeObserver(session, pojo, new HashSet());
   }
   
   private void removeObserver(Observer session, Object pojo, Set stack)
   {
      if ( pojo instanceof Collection )
      {
         Collection col = (Collection)pojo;
         for (Iterator i = col.iterator(); i.hasNext();) {
            Object obj = i.next();
            // If not a managed pojo, will return anyway
            removeObserver(session, obj, stack);
         }
   
         return;
      } 
      else if (pojo instanceof Map)
      {
         Map map = (Map)pojo;
         for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Object value = map.get(key);
   
            // Walk thru key and value
            removeObserver(session, key, stack);
            removeObserver(session, value, stack);
         }
   
         return;
      }
      // BRIAN 3/14 changed this from checking Advised to checking Subject
      // since that is what we cast to below
      if(! (pojo instanceof Subject) )
      {
         return;  // No need to add observer since it is primitive.
      }

      Subject subject = (Subject)pojo;
      subject.removeObserver(session);
      if(log_.isTraceEnabled())
      {
         log_.trace("removeObserver(): session: " +session + " pojo name: " +pojo.getClass().getName());
      }
      
      // Examine each field of the type and its superclasses to see if
      // we need to remove the observer from the pojo held by that field
      // Traverse recursively
      
      // First identify and cache the names of all the class' 
      // non-immediate fields 
      Class type = pojo.getClass();
      Set complexFields = (Set) typeMap.get(type);
      if (complexFields == null)
      {
         complexFields = Util.parseComplexFields(type);
         typeMap.put(type, complexFields);
      }

      if (complexFields.size() == 0)
         return;
      
      // Store a ref to the pojo to avoid cyclic removals
      stack.add(pojo);
      
      for (Iterator iter = complexFields.iterator(); iter.hasNext();)
      {
         String fieldName = (String) iter.next();
         Class curType = type;
         while (curType != null)
         {
            try
            {
               Field field = curType.getDeclaredField(fieldName);
               boolean accessible = field.isAccessible();
               Object value = null;
               try 
               {
                  field.setAccessible(true);
                  
                  value=field.get(pojo);
                  // Continue recursively unless we've already handled this value
                  if (value != null && !stack.contains(value))
                     removeObserver(session, value, stack);
                  break;
               }
               catch(IllegalAccessException e) 
               {
                  throw new RuntimeException("field access failed", e);
               }
               finally
               {
                  field.setAccessible(accessible);
               }
            }
            catch (NoSuchFieldException e)
            {
               // Check if the field is declared in a superclass
               curType = curType.getSuperclass();
               if (curType == null)
                  throw new RuntimeException("Field "  + fieldName + 
                        " does not exist", e);
            }
         }
      }
   }

   private Fqn getFieldFqn(String id, String key)
   {
      // /SESSION/id/ATTR/key
      // Guard against string with delimiter.
      List list = new ArrayList(6);
      list.add(SESSION);
      list.add(hostName_);
      list.add(webAppPath_);
      list.add(id);
      list.add(ATTRIBUTE);
      breakKeys(key, list);
      return new Fqn(list);
   }
   
   private Fqn getSipApplicationSessionFieldFqn(String id, String key)
   {
      // /SIPSESSION/id/ATTR/key
      // Guard against string with delimiter.
      List list = new ArrayList(6);
      list.add(SIPSESSION);
      list.add(hostName_);
      list.add(sipApplicationName);
      list.add(id);
      list.add(ATTRIBUTE);
      breakKeys(key, list);
      return new Fqn(list);
   }
   
   private Fqn getSipSessionFieldFqn(String id, String sessionId, String key)
   {
      // /SIPSESSION/id/sessionid/ATTR/key
      // Guard against string with delimiter.
      List list = new ArrayList(6);
      list.add(SIPSESSION);
      list.add(hostName_);
      list.add(sipApplicationName);
      list.add(id);
      list.add(sessionId);
      list.add(ATTRIBUTE);
      breakKeys(key, list);
      return new Fqn(list);
   }

   private void breakKeys(String key, List list)
   {
      StringTokenizer token = new StringTokenizer(key, FQN_DELIMITER);
      while(token.hasMoreTokens())
      {
         list.add(token.nextToken());
      }
   }

   private Fqn getWebappFqn()
   {
      // /SESSION/hostname/webAppPath
      Object[] objs = new Object[]{SESSION, hostName_, webAppPath_};
      return new Fqn(objs);
   }
   
   private Fqn getSipappFqn()
   {
      // /SIPSESSION/hostname/sipApplicationName
      Object[] objs = new Object[]{SIPSESSION, hostName_, sipApplicationName};
      return new Fqn(objs);
   }
   
   private Fqn getWebappFqn(Object dataOwner)
   {
      if (dataOwner == null)
         return getWebappFqn();
      
      // /SESSION/hostname/webAppPath
      Object[] objs = new Object[]{BUDDY_BACKUP, dataOwner, SESSION, hostName_, webAppPath_};
      return new Fqn(objs);
   }
   
   private Fqn getSipappFqn(Object dataOwner)
   {
      if (dataOwner == null)
         return getSipappFqn();
      
      // /SIPSESSION/hostname/sipApplicationName
      Object[] objs = new Object[]{BUDDY_BACKUP, dataOwner, SIPSESSION, hostName_, sipApplicationName};
      return new Fqn(objs);
   }
   
   private Fqn getSessionFqn(String id)
   {
      // /SESSION/hostname/webAppPath/id
      Object[] objs = new Object[]{SESSION, hostName_, webAppPath_, id};
      return new Fqn(objs);
   }
   
   private Fqn getSipApplicationSessionFqn(String id)
   {
      // /SIPSESSION/hostname/sipApplicationName/id
      Object[] objs = new Object[]{SIPSESSION, hostName_, sipApplicationName, id};
      return new Fqn(objs);
   }
   
   private Fqn getSipSessionFqn(String id, String sessionId)
   {
      // /SIPSESSION/hostname/sipApplicationName/id/sessionId
      Object[] objs = new Object[]{SIPSESSION, hostName_, sipApplicationName, id, sessionId};
      return new Fqn(objs);
   }

   private Fqn getSessionFqn(String id, String dataOwner)
   {
      // /_BUDDY_BACKUP_/dataOwner/SESSION/hostname/webAppPath/id
      Object[] objs = new Object[]{BUDDY_BACKUP, dataOwner, SESSION, hostName_, webAppPath_, id};
      return new Fqn(objs);
   }
   
   private Fqn getSipApplicationSessionFqn(String id, String dataOwner)
   {
      // /_BUDDY_BACKUP_/dataOwner/SIPSESSION/hostname/sipApplicationName/id
      Object[] objs = new Object[]{BUDDY_BACKUP, dataOwner, SIPSESSION, hostName_, sipApplicationName, id};
      return new Fqn(objs);
   }
   
   private Fqn getSipSessionFqn(String id, String sessionId, String dataOwner)
   {
      // /_BUDDY_BACKUP_/dataOwner/SIPSESSION/hostname/sipApplicationName/id/sessionId
      Object[] objs = new Object[]{BUDDY_BACKUP, dataOwner, SIPSESSION, hostName_, sipApplicationName, id, sessionId};
      return new Fqn(objs);
   }

   private Fqn getAttributeFqn(String id)
   {
      // /SESSION/hostName/webAppPath/id/ATTR
      Object[] objs = new Object[]{SESSION, hostName_, webAppPath_, id, ATTRIBUTE};
      return new Fqn(objs);
   }
   
   private Fqn getSipApplicationSessionAttributeFqn(String id)
   {
      // /SIPSESSION/hostName/sipapplicationname/id/ATTR
      Object[] objs = new Object[]{SIPSESSION, hostName_, sipApplicationName, id, ATTRIBUTE};
      return new Fqn(objs);
   }
   
   private Fqn getSipSessionAttributeFqn(String id, String sessionId)
   {
      // /SIPSESSION/hostName/sipapplicationname/id/sessionid/ATTR
      Object[] objs = new Object[]{SIPSESSION, hostName_, sipApplicationName, id, sessionId, ATTRIBUTE};
      return new Fqn(objs);
   }

   private Object getMarshalledValue(Object value)
   {
      // JBAS-2920.  For now, continue using MarshalledValue, as 
      // it allows lazy deserialization of the attribute on remote nodes
      // For Branch_4_0 this is what we have to do anyway for backwards
      // compatibility. For HEAD we'll follow suit for now.
      // TODO consider only using MV for complex objects (i.e. not primitives)
      // and Strings longer than X.
      
//      if (useTreeCacheMarshalling_)
//      {
//         return value;
//      }
//      else
//      {
         try
         {
            MarshalledValue mv = new MarshalledValue(value);
            if (log_.isTraceEnabled())
            {
               log_.trace("marshalled object to size " + mv.size() + " bytes");
            }
            return mv;
         }
         catch (IOException e)
         {
            log_.error("IOException occurred marshalling value ", e);
            return null;
         }
//      }
      }

   private Object getUnMarshalledValue(Object mv)
   {
      // JBAS-2920.  For now, continue using MarshalledValue, as 
      // it allows lazy deserialization of the attribute on remote nodes
      // For Branch_4_0 this is what we have to do anyway for backwards
      // compatibility. For HEAD we'll follow suit for now.
//      if (useTreeCacheMarshalling_)
//      {
//         return mv;
//      }
//      else
//      {
         if (mv == null) return null;
         // Swap in/out the tcl for this web app. Needed only for un marshalling.
         ClassLoader prevTCL = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(manager_.getWebappClassLoader());
         try
         {
            return ((MarshalledValue) mv).get();
         }
         catch (IOException e)
         {
            log_.error("IOException occurred unmarshalling value ", e);
            return null;
         }
         catch (ClassNotFoundException e)
         {
            log_.error("ClassNotFoundException occurred unmarshalling value ", e);
            return null;
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(prevTCL);
         }
//      }
      }

   private byte[] externalizeSession(ClusteredSession session)
   {      
      try
      {
         // Write the contents of session to a byte array and store that
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         // Use MarshalledValueOutputStream instead of superclass ObjectOutputStream
         // or else there are problems finding classes with scoped loaders
         MarshalledValueOutputStream oos = new MarshalledValueOutputStream(baos);
         session.writeExternal(oos);
         oos.close(); // flushes bytes to baos
         
         byte[] bytes = baos.toByteArray();
         
         if (log_.isDebugEnabled())
         {
            log_.debug("marshalled object to size " + bytes.length + " bytes");
         }

         return bytes;
      }
      catch (Exception e)
      {
         log_.error("externalizeSession(): exception occurred externalizing session " + session, e);
         return null;
      }
      
   }
   
   private byte[] externalizeSipSession(ClusteredSipSession session)
   {      
      try
      {
         // Write the contents of session to a byte array and store that
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         // Use MarshalledValueOutputStream instead of superclass ObjectOutputStream
         // or else there are problems finding classes with scoped loaders
         MarshalledValueOutputStream oos = new MarshalledValueOutputStream(baos);
         session.writeExternal(oos);
         oos.close(); // flushes bytes to baos
         
         byte[] bytes = baos.toByteArray();
         
         if (log_.isDebugEnabled())
         {
            log_.debug("marshalled object to size " + bytes.length + " bytes");
         }

         return bytes;
      }
      catch (Exception e)
      {
         log_.error("externalizeSession(): exception occurred externalizing session " + session, e);
         return null;
      }
      
   }
   
   private byte[] externalizeSipApplicationSession(ClusteredSipApplicationSession session)
   {      
      try
      {
         // Write the contents of session to a byte array and store that
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         // Use MarshalledValueOutputStream instead of superclass ObjectOutputStream
         // or else there are problems finding classes with scoped loaders
         MarshalledValueOutputStream oos = new MarshalledValueOutputStream(baos);
         session.writeExternal(oos);
         oos.close(); // flushes bytes to baos
         
         byte[] bytes = baos.toByteArray();
         
         if (log_.isTraceEnabled())
         {
            log_.trace("marshalled object to size " + bytes.length + " bytes");
         }

         return bytes;
      }
      catch (Exception e)
      {
         log_.error("externalizeSession(): exception occurred externalizing session " + session, e);
         return null;
      }
      
   }

}
