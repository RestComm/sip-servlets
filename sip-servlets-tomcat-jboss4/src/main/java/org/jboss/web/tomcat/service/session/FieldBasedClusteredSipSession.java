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
package org.jboss.web.tomcat.service.session;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSessionActivationListener;

import org.apache.log4j.Logger;
import org.jboss.aspects.patterns.observable.Observer;
import org.jboss.aspects.patterns.observable.Subject;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.SipFactoryImpl;

/**
 * 
 * <p>
 * Implementation of a clustered sip session for the JBossCacheManager.
 * This class is based on the following Jboss class org.jboss.web.tomcat.service.session.FieldBasedClusteredSession JBOSS AS 4.2.2 Tag
 * 
 * The replication granularity
 * level is field based; that is, we replicate only the dirty field in a POJO that is part of
 * a session attribute. E.g., once a user do setAttribute("pojo", pojo), pojo will be monitored
 * automatically for field changes and accessing. It offers couple of advantages:
 * <ul>
 * <li>pojo.setName(), for example, will only replicate the name field in the pojo. And thus is more efficient.</li>
 * <li>If pojo has a complex object graph, we will handle that automtically providing that the
 * children object is also aspectized.</li>
 * </ul>
 * Note that in current version, all the attributes and its associated childre graph objects are
 * required to be aspectized. That is, you can't simply declare them as Serializable. This is restricted
 * because of the marshalling/unmarshalling issue.</p>
 *
 * <p>We use JBossCache for our internal, replicated data store.
 * The internal structure is like in JBossCache:
 * <pre>
 * /SIPSESSION
 *    /hostname
 *       /sip_application_name    (path + session id is unique)
 *          /sipappsessionid    Map(id, session)
 *          		  (VERSION_KEY, version)  // Used for version tracking. version is an Integer.
 *          	/sipsessionid   Map(id, session)
 *                    (VERSION_KEY, version)  // Used for version tracking. version is an Integer.
 *             			/ATTRIBUTE    Map(can be empty)
 *                			/pojo      Map(field name, field value) (pojo naming is by field.getName())
 *
 * </pre>
 * <p/>
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class FieldBasedClusteredSipSession extends JBossCacheClusteredSipSession implements Observer {
	private static transient final Logger logger = Logger.getLogger(AttributeBasedClusteredSipSession.class);
	/**
	 * Descriptive information describing this Session implementation.
	 */
   protected static final String info = "FieldBasedClusteredSipSession/1.0";
   
   protected transient Map attributes_ = Collections.synchronizedMap(new HashMap());

   protected FieldBasedClusteredSipSession(SipSessionKey key,
			SipFactoryImpl sipFactoryImpl,
			MobicentsSipApplicationSession mobicentsSipApplicationSession) {
		super(key, sipFactoryImpl, mobicentsSipApplicationSession);
	}

   // ----------------------------------------------- Overridden Public Methods


   /**
    * Override the superclass to additionally reset this class' fields.
    * <p>
    * <strong>NOTE:</strong> It is not anticipated that this method will be
    * called on a ClusteredSession, but we are overriding the method to be
    * thorough.
    * </p>
    */
//	   public void recycle()
//	   {
//	      super.recycle();
//
//	      attributes_.clear();
//	   }

   /**
    * Return a string representation of this object.
    */
   public String toString()
   {

      StringBuffer sb = new StringBuffer();
      sb.append("FieldBasedClusteredSipSession[");
      sb.append(super.toString());
      sb.append("]");
      return (sb.toString());

   }

   // The superclass version of processSessionRepl is fine; it will remove
   // the session metadata, and any attribute changes have been picked up
   // for replication as they were made; no need to do anything here
//   public synchronized void processSessionRepl()
//   {
//	      super.processSessionRepl();
//   }

   public void removeMyself()
   {
      // This is a shortcut to remove session and it's child attributes.
      // Note that there is no need to remove attribute first since caller 
      // will do that already.
      proxy_.removeSipSession(sipApplicationSession.getId(), getId());
   }

   public void removeMyselfLocal()
   {
      // Need to evict attribute first before session to clean up everything.
      // Note that there is no need to remove attributes first since caller 
      // will do that already.
      // BRIAN -- the attributes *are* already evicted, but we leave the
      // removePojosLocal call here in order to evict the ATTRIBUTE node.  
      // Otherwise empty nodes for the session root and child ATTRIBUTE will 
      // remain in the tree and screw up our list of session names.
      proxy_.removeSipSessionPojosLocal(sipApplicationSession.getId(), getId());
      proxy_.removeSipSessionLocal(sipApplicationSession.getId(), getId());
   }

   // ------------------------------------------------ JBoss internal abstract method

   /**
    * Populate the attributes stored in the distributed store to the local 
    * transient map. Add ourself as an Observer to newly found attributes and 
    * remove ourself as an Observer to existing attributes that are no longer
    * in the distributed store.
    */
   protected void populateAttributes()
   {
      // Preserve any local attributes that were excluded from replication
      Map excluded = removeExcludedAttributes(attributes_);
      
      Set keys = proxy_.getSipSessionPojoKeys(sipApplicationSession.getId(), getId());
      Set oldKeys = new HashSet(attributes_.keySet());
      
      // Since we are going to touch each attribute, might as well
      // check if we have any HttpSessionActivationListener
      boolean hasListener = false;
      
      if (keys != null)
      {
         oldKeys.removeAll(keys); // only keys that no longer exist are left

         for (Iterator it = keys.iterator(); it.hasNext(); )
         {
            String name = (String) it.next();
            
            Object oldAttrib = null;
            Object newAttrib = proxy_.getSipSessionPojo(sipApplicationSession.getId(), getId(), name);
            if (newAttrib != null)
            {
               oldAttrib = attributes_.put(name, newAttrib);
            
               if (oldAttrib != newAttrib)
               {
                  // Need to observe this pojo as well
                  // for any modification events.
                  proxy_.addObserver(this, newAttrib);
                  
                  // Stop observing the old pojo
                  proxy_.removeObserver(this, oldAttrib); // null pojo OK :)
               }
               
               // Check if we have a listener
               if (newAttrib instanceof HttpSessionActivationListener)
                  hasListener = true;
            }
            else
            {
               // This shouldn't happen -- if we had a key, newAttrib s/b not null
               
               oldAttrib = attributes_.remove(name);
               // Stop observing this pojo
               proxy_.removeObserver(this, oldAttrib); // null pojo OK :)                 
               
            }
         }
      }
      
      hasActivationListener = hasListener ? Boolean.TRUE : Boolean.FALSE;
      
      // Cycle through remaining old keys and remove them 
      // and also remove ourself as Observer
      for (Iterator it = oldKeys.iterator(); it.hasNext(); )
      {
         Object oldAttrib = attributes_.remove(it.next());
         proxy_.removeObserver(this, oldAttrib); 
      }
      
      // Restore any excluded attributes
      if (excluded != null)
    	  getAttributeMap().putAll(excluded);
   }
 
   protected Object getJBossInternalAttribute(String name)
   {
      // Check the local map first.
      Object result = attributes_.get(name);
      
      // NOTE -- we no longer check with the store.  Attributes are only
      // loaded from store during populateAttributes() call at beginning
      // of request when we notice we are outdated.

      // Do dirty check even if result is null, as w/ SET_AND_GET null
      // still makes us dirty (ensures timely replication w/o using ACCESS)
      if (isGetDirty(result))
      {
         sessionAttributesDirty();
      }
      
      return result;
   }

   /**
    * Overrides the superclass to treat classes implementing Subject
    * as "immutable", since as an Observer we will detect any changes
    * to those types.
    */
   protected boolean isMutable(Object attribute)
   {
      boolean pojo = (attribute instanceof Subject);
      boolean mutable = (!pojo && super.isMutable(attribute));
      return mutable;
   }

   protected Object removeJBossInternalAttribute(String name, boolean localCall, boolean localOnly)
   {
      // Remove it from the underlying store
      if (localCall && !replicationExcludes.contains(name))
      { 
         if (localOnly)         
            proxy_.removeSipSessionPojoLocal(sipApplicationSession.getId(), getId(), name);      
         else
            proxy_.removeSipSessionPojo(sipApplicationSession.getId(), getId(), name); 
         
         sessionAttributesDirty();
      }
      Object result = attributes_.remove(name);
      if(result == null)
      {
         logger.warn("removeJBossInternalAttribute(): null value to remove with key: "+ name);
         return null;
      }
      proxy_.removeObserver(this, result);
         
      return result;
   }

   protected Map getJBossInternalAttributes()
   {
      return attributes_;
   }

   protected Set getJBossInternalKeys()
   {
      return attributes_.keySet();
   }

   /**
    * Method inherited from Tomcat. Return zero-length based string if not found.
    */
   protected String[] keys()
   {
      return ((String[]) getJBossInternalKeys().toArray(new String[0]));
   }

   /**
    * Overrides the superclass to allow instrumented classes and
    * non-serializable Collections and Maps.
    */
   protected boolean canAttributeBeReplicated(Object attribute)
   {
      return (Util.checkPojoType(attribute));
   }

   /**
    * This is the hook for setAttribute. Note that in this FieldBasedClusteredSession using aop,
    * user should not call setAttribute call too often since this will re-connect the attribute with the internal
    * cache (and this is expensive).
    * @param key
    * @param value
    * @return Object
    */
   protected Object setJBossInternalAttribute(String key, Object value)
   {
      Object oldVal = null;
      if (!replicationExcludes.contains(key))
      {   
         oldVal = proxy_.setSipSessionPojo(sipApplicationSession.getId(), getId(), key, value);
         if(oldVal != null)
         {  // We are done with the old one.
            proxy_.removeObserver(this, oldVal);
         }
   
         if(value != null)
         {
            // Special case for Collection classes.
            if( value instanceof Map || value instanceof Collection)
            {
               // We need to obtain the proxy first.
               value = proxy_.getSipSessionPojo(sipApplicationSession.getId(), getId(), key);
            }

            // Need to use obj since it can return as a proxy.
            proxy_.addObserver(this, value);
         }

         // Only mark session dirty if we can replicate the attribute
         sessionAttributesDirty();
      }
      
      // Still need to put it in the map to track locally.
      oldVal = attributes_.put(key, value);
      
      return oldVal;
   }

   /**
    * Call back handler for the aop Subject/Observer pattern. 
    * We subscribe to the event of field write and mark ourself dirty.
    * 
    * @param subject  the object we are Observing
    */
   public void fireChange(Subject subject)
   {
      // Currently we don't care who is modified, we will simply mark session is dirty for replication purpose.
      if(logger.isTraceEnabled())
      {
         logger.trace("fireChange(): subject has changed: " +subject);
      }
      sessionAttributesDirty();
   }
	
}
