package org.jboss.as.clustering.web.sip;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.as.clustering.web.DistributableSessionMetadata;

/**
 * @author posfaig@gmail.com
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableSipSessionMetadata extends
		DistributableSessionMetadata {
	// map to store meta data changes for replication.
	private transient Map<String, Object> metaData = new ConcurrentHashMap<String, Object>();

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	/**
	 * @return the metaData
	 */
	public Map<String, Object> getMetaData() {
		return metaData;
	}
		
}
