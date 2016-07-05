package org.jboss.as.web.session.sip;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipSession;

import org.jboss.as.clustering.web.sip.DistributedCacheConvergedSipManager;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;
import org.jboss.as.web.session.ClusteredSessionManager;
import org.jboss.as.web.session.notification.sip.ClusteredSipApplicationSessionNotificationPolicy;
import org.jboss.as.web.session.notification.sip.ClusteredSipSessionNotificationPolicy;
import org.mobicents.servlet.sip.catalina.CatalinaSipManager;
import org.mobicents.servlet.sip.core.session.DistributableSipManager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.restcomm.cluster.MobicentsCluster;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public interface ClusteredSipSessionManager<O extends OutgoingDistributableSessionData> extends ClusteredSessionManager, DistributableSipManager, CatalinaSipManager {
	/**
	 * Gets the policy for determining whether the servlet spec notifications
	 * related to sip session events are allowed to be emitted on the local cluster
	 * node.
	 */
	ClusteredSipSessionNotificationPolicy getSipSessionNotificationPolicy();
	
	/**
	 * Gets the policy for determining whether the servlet spec notifications
	 * related to sip application session events are allowed to be emitted on the local cluster
	 * node.
	 */
	ClusteredSipApplicationSessionNotificationPolicy getSipApplicationSessionNotificationPolicy();
	
	/**
	 * Remove the active session locally from the manager without replicating to
	 * the cluster. This can be useful when the session is expired, for example,
	 * where there is not need to propagate the expiration.
	 * 
	 * @param session
	 */
	public void removeLocal(SipSession session);
	
	/**
	 * Remove the active session locally from the manager without replicating to
	 * the cluster. This can be useful when the session is expired, for example,
	 * where there is not need to propagate the expiration.
	 * 
	 * @param session
	 */
	public void removeLocal(SipApplicationSession session);

	boolean storeSipSession(ClusteredSipSession<? extends OutgoingDistributableSessionData> session);

	boolean storeSipApplicationSession(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session);
	
	SnapshotSipManager getSnapshotSipManager();

	public DistributedCacheConvergedSipManager getDistributedCacheConvergedSipManager();

	
	MobicentsCluster getMobicentsCluster();

	void checkSipApplicationSessionPassivation(SipApplicationSessionKey key);
	void checkSipSessionPassivation(SipSessionKey key);
}

