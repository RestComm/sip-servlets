package org.jboss.as.clustering.web.sip;

import org.jboss.as.clustering.web.OutgoingDistributableSessionData;

/**
 * @author posfaig@gmail.com
 * @author jean.deruelle@gmail.com
 *
 */
public interface OutgoingDistributableSipSessionData extends OutgoingDistributableSessionData {
	String getSipSessionKey();
	String getSipApplicationSessionKey();
	void setSessionMetaDataDirty(boolean isSessionMetaDataDirty);
	boolean isSessionMetaDataDirty();
}
