package org.jboss.as.clustering.web.sip;

import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.as.clustering.web.SessionIdFactory;

/**
 * Callback interface to allow the distributed caching layer to invoke upon the
 * local session manager.
 * 
 * @author posfaig@gmail.com
 * @author Brian Stansberry
 * @version $Revision: $
 */
public interface LocalDistributableConvergedSessionManager extends SessionIdFactory {
	void notifyRemoteSipApplicationSessionInvalidation(String sessId);

	void notifyRemoteSipSessionInvalidation(String sipAppSessionId,
			String sipSessionId);

	void notifySipApplicationSessionLocalAttributeModification(String sessId);

	void notifySipSessionLocalAttributeModification(String sipAppSessionId,
			String sipSessionId);

	boolean sipApplicationSessionChangedInDistributedCache(String realId,
			String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	boolean sipSessionChangedInDistributedCache(String sipAppSessionId,
			String sipSessionId, String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	void sipApplicationSessionActivated();

	void sipSessionActivated();
}
