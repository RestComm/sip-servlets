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

package org.jboss.web.tomcat.service.session.distributedcache.impl.jbc;

import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.NodeActivated;
import org.jboss.cache.notifications.event.NodeActivatedEvent;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableConvergedSessionManager;
import org.jboss.web.tomcat.service.session.distributedcache.spi.LocalDistributableSessionManager;

/**
 * Listener for JBoss Cache activation events.  Triggers updates of
 * the passivation counter.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 64678 $
 */
@org.jboss.cache.notifications.annotation.CacheListener
public class SipPassivationListener extends CacheListenerBase
{
	private String sipApplicationNameHashed;
	
   SipPassivationListener(LocalDistributableSessionManager manager, String contextHostPath, String sipApplicationNameHashed)
   {      
      super(manager, contextHostPath);
      this.sipApplicationNameHashed = sipApplicationNameHashed;
   }
   
   // NOTE: Don't track passivation from here -- we know in JBossCacheManager
   // when we trigger a passivation. Avoid spurious listener callbacks to
   // webapps that aren't interested.
   
//   @NodePassivated
//   public void nodePassivated(NodePassivatedEvent event)
//   {
//      Fqn fqn = event.getFqn();
//      if (isFqnForOurWebapp(fqn, isBuddyFqn(fqn)))
//      {
//         manager_.sessionPassivated();
//      }
//   }
   
   // We do want activation callbacks, as JBossCacheManager can't readily
   // track whether a cache read is going to result in an activation
   
   @NodeActivated
   public void nodeActivated(NodeActivatedEvent event)
   {
      @SuppressWarnings("unchecked")
      Fqn<String> fqn = event.getFqn();
      boolean isBuddy = isBuddyFqn(fqn);      
      if (isFqnForOurSipapp(fqn, isBuddy))
      {
    	  String sessId = null;
			if (SipCacheListener.isFqnSessionRootSized(fqn.size(), isBuddy) 
		            && SipCacheListener.isFqnSipApplicationSessionRootSized(fqn.size(), isBuddy)) {
				sessId = SipCacheListener.getSipApplicationSessionIdFromFqn(fqn, isBuddy);
				((LocalDistributableConvergedSessionManager) manager_)
						.sipApplicationSessionActivated();
			} else {
				sessId = SipCacheListener.getSipSessionIdFromFqn(fqn, isBuddy);
				((LocalDistributableConvergedSessionManager) manager_)
						.sipSessionActivated();
			}   
    	  
         manager_.sessionActivated();
      }
      
   }
   
   protected boolean isFqnForOurSipapp(Fqn<String> fqn, boolean isBuddy)
   {   
      try
      {
         if (sipApplicationNameHashed.equals(fqn.get(isBuddy ? SipCacheListener.BUDDY_BACKUP_ROOT_OWNER_SIZE + SipCacheListener.SIPAPPNAME_FQN_INDEX : SipCacheListener.SIPAPPNAME_FQN_INDEX))
               && AbstractJBossCacheService.SESSION.equals(fqn.get(isBuddy ? SipCacheListener.BUDDY_BACKUP_ROOT_OWNER_SIZE + SipCacheListener.SIPSESSION_FQN_INDEX : SipCacheListener.SIPSESSION_FQN_INDEX)))
            return true;
      }
      catch (IndexOutOfBoundsException e)
      {
         // can't be ours; too small; just fall through
      }
   
      return false;
   }      
}