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
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.aop.Advised;
import org.jboss.web.tomcat.service.session.distributedcache.spi.DistributableSessionMetadata;
import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * 
 * <p>
 * Implementation of a clustered sip application session for the JBossCacheManager.
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
 * <p/>
 *
 * @author Ben Wang
 * @author Brian Stansberry
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class FieldBasedClusteredSipApplicationSession extends ClusteredSipApplicationSession<OutgoingDistributableSessionData> {
	private static transient final Logger logger = Logger.getLogger(AttributeBasedClusteredSipApplicationSession.class);
	/**
	 * Descriptive information describing this Session implementation.
	 */
   protected static final String info = "FieldBasedClusteredSipApplicationSession/1.0";
   
   protected FieldBasedClusteredSipApplicationSession(SipApplicationSessionKey key,
			SipContext sipContext, boolean useJK) {
		super(key, sipContext, useJK);
	}

// ----------------------------------------------- Overridden Public Methods

   @Override
   public String getInfo()
   {
      return (info);
   }


   // --------------------------------------------- Overridden Protected Methods

   @Override
   protected OutgoingDistributableSessionData getOutgoingSessionData()
   {
      DistributableSessionMetadata metadata = isSessionMetadataDirty() ? getSessionMetadata() : null;
      Long timestamp = metadata != null || isSessionAttributeMapDirty() || getMustReplicateTimestamp() ? Long.valueOf(getSessionTimestamp()) : null;
      return new OutgoingDistributableSessionDataImpl(getRealId(), getVersion(), timestamp, metadata);
   }

   /**
    * Overrides the superclass to treat classes implementing Subject
    * as "immutable", since as an Observer we will detect any changes
    * to those types.
    */
   @Override
   protected boolean isMutable(Object attribute)
   {
      boolean pojo = (attribute instanceof Advised);
      boolean mutable = (!pojo && super.isMutable(attribute));
      return mutable;
   }

   @Override
   protected Object removeAttributeInternal(String name, boolean localCall, boolean localOnly)
   {
      // Remove it from the underlying store
      if (localCall && !replicationExcludes.contains(name))
      { 
         if (localOnly)         
            getDistributedCacheManager().removeAttributeLocal(getRealId(), name);      
         else
            getDistributedCacheManager().removeAttribute(getRealId(), name); 
         
         sessionAttributesDirty();
      }
      return getAttributesInternal().remove(name);
   }
   
   /**
    * Overrides the superclass to allow instrumented classes and
    * non-serializable Collections and Maps.
    */
   @Override
   protected boolean canAttributeBeReplicated(Object attribute)
   {
      return (attribute == null || Util.checkPojoType(attribute));
   }

   /**
    * This is the hook for setAttribute. Note that in this FieldBasedClusteredSession using aop,
    * user should not call setAttribute call too often since this will re-connect the attribute with the internal
    * cache (and this is expensive).
    * @param key
    * @param value
    * @return Object
    */
   @Override
   protected Object setAttributeInternal(String key, Object value)
   {
      if (!replicationExcludes.contains(key))
      {   
         String myRealId = getRealId();
         getDistributedCacheManager().putAttribute(myRealId, key, value);
   
         // Special case for Collection classes.
         if( value instanceof Map || value instanceof Collection)
         {
            // We need to obtain the proxy first.
            value = getDistributedCacheManager().getAttribute(myRealId, key);
         }

         // Only mark session dirty if we can replicate the attribute
         sessionAttributesDirty();
      }
      
      // Still need to put it in the map to track locally.
      return getAttributesInternal().put(key, value);
   }
}
