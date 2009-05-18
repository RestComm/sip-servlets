package org.jboss.web.tomcat.service.session.distributedcache.impl.jbc;

import org.jboss.cache.Fqn;
import org.jboss.cache.notifications.annotation.NodeActivated;
import org.jboss.cache.notifications.event.NodeActivatedEvent;
import org.jboss.web.tomcat.service.session.ClusteredSipManager;
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
	private String sipApplicationName;
	
   SipPassivationListener(LocalDistributableSessionManager manager, String contextHostPath, String sipApplicationName)
   {      
      super(manager, contextHostPath);
      this.sipApplicationName = sipApplicationName;
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
				((ClusteredSipManager) manager_)
						.sipApplicationSessionActivated();
			} else {
				sessId = SipCacheListener.getSipSessionIdFromFqn(fqn, isBuddy);
				((ClusteredSipManager) manager_)
						.sipSessionActivated();
			}   
    	  
         manager_.sessionActivated();
      }
      
   }
   
   protected boolean isFqnForOurSipapp(Fqn<String> fqn, boolean isBuddy)
   {   
      try
      {
         if (sipApplicationName.equals(fqn.get(isBuddy ? SipCacheListener.BUDDY_BACKUP_ROOT_OWNER_SIZE + SipCacheListener.SIPAPPNAME_FQN_INDEX : SipCacheListener.SIPAPPNAME_FQN_INDEX))
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