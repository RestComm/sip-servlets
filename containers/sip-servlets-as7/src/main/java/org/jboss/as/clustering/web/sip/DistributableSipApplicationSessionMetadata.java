package org.jboss.as.clustering.web.sip;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.logging.Logger;


/**
 * @author posfaig@gmail.com
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableSipApplicationSessionMetadata extends
		DistributableSessionMetadata implements Externalizable {
	
	private transient static Logger logger = Logger.getLogger(DistributableSipApplicationSessionMetadata.class);
	
	private transient boolean sipSessionsMapModified;
	private transient boolean httpSessionsMapModified;
	private transient boolean servletTimersMapModified;

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
	 * @param sipSessionsMapModified the sipSessionsMapModified to set
	 */
	public void setSipSessionsMapModified(boolean sipSessionsMapModified) {
		if (logger.isDebugEnabled()){
			logger.debug("setSipSessionsMapModified - sipSessionsMapModified=" + sipSessionsMapModified);
		}
		this.sipSessionsMapModified = sipSessionsMapModified;
	}

	/**
	 * @return the sipSessionsMapModified
	 */
	public boolean isSipSessionsMapModified() {
		if (logger.isDebugEnabled()){
			logger.debug("isSipSessionsMapModified - return=" + sipSessionsMapModified);
		}
		return sipSessionsMapModified;
	}

	/**
	 * @param httpSessionsMapModified the httpSessionsMapModified to set
	 */
	public void setHttpSessionsMapModified(boolean httpSessionsMapModified) {
		if (logger.isDebugEnabled()){
			logger.debug("setHttpSessionsMapModified - httpSessionsMapModified=" + httpSessionsMapModified);
		}
		this.httpSessionsMapModified = httpSessionsMapModified;
	}

	/**
	 * @return the httpSessionsMapModified
	 */
	public boolean isHttpSessionsMapModified() {
		if (logger.isDebugEnabled()){
			logger.debug("isHttpSessionsMapModified - return=" + httpSessionsMapModified);
		}
		return httpSessionsMapModified;
	}

	/**
	 * @param servletTimersMapModified the servletTimersMapModified to set
	 */
	public void setServletTimersMapModified(boolean servletTimersMapModified) {
		if (logger.isDebugEnabled()){
			logger.debug("setServletTimersMapModified - servletTimersMapModified=" + servletTimersMapModified);
		}
		this.servletTimersMapModified = servletTimersMapModified;
	}

	/**
	 * @return the servletTimersMapModified
	 */
	public boolean isServletTimersMapModified() {
		if (logger.isDebugEnabled()){
			logger.debug("isServletTimersMapModified - return=" + servletTimersMapModified);
		}
		return servletTimersMapModified;
	}
	
	@Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        super.readExternal(in);
    }
}

