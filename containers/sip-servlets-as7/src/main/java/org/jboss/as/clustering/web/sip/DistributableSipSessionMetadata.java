package org.jboss.as.clustering.web.sip;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.logging.Logger;

/**
 * @author posfaig@gmail.com
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableSipSessionMetadata extends
		DistributableSessionMetadata implements Serializable {
	
	private transient static Logger logger = Logger.getLogger(DistributableSipSessionMetadata.class);
	
	// map to store meta data changes for replication.
	private transient Map<String, Object> metaData = new HashMap<String, Object>();
	
	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(Map<String, Object> metaData) {
		if (logger.isDebugEnabled()){
			logger.debug("setMetaData");
		}
		this.metaData = metaData;
	}

	/**
	 * @return the metaData
	 */
	public Map<String, Object> getMetaData() {
		if (logger.isDebugEnabled()){
			logger.debug("getMetaData");
		}
		return metaData;
	}
	
	/**
	 * @return shallow copy of the metaData map
	 */
	public Map<String, Object> getMetaDataCopy() {
		if (logger.isDebugEnabled()){
			logger.debug("getMetaDataCopy");
		}
		if (metaData == null){
			return null;
		}
		// this is just a shallow copy. depending on the type of entries, a deep copy might be better later on.
		return new HashMap<String, Object>(metaData);
	}
    
		
}
