package org.jboss.as.clustering.web.sip;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.clustering.web.DistributableSessionMetadata;


/**
 * @author posfaig@gmail.com
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableSipApplicationSessionMetadata extends
		DistributableSessionMetadata {
	private transient boolean sipSessionsMapModified;
	private transient boolean httpSessionsMapModified;
	private transient boolean servletTimersMapModified;

	// map to store meta data changes for replication.
	private transient Map<String, Object> metaData = new HashMap<String, Object>();


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

	/**
	 * @param sipSessionsMapModified the sipSessionsMapModified to set
	 */
	public void setSipSessionsMapModified(boolean sipSessionsMapModified) {
		this.sipSessionsMapModified = sipSessionsMapModified;
	}

	/**
	 * @return the sipSessionsMapModified
	 */
	public boolean isSipSessionsMapModified() {
		return sipSessionsMapModified;
	}

	/**
	 * @param httpSessionsMapModified the httpSessionsMapModified to set
	 */
	public void setHttpSessionsMapModified(boolean httpSessionsMapModified) {
		this.httpSessionsMapModified = httpSessionsMapModified;
	}

	/**
	 * @return the httpSessionsMapModified
	 */
	public boolean isHttpSessionsMapModified() {
		return httpSessionsMapModified;
	}

	/**
	 * @param servletTimersMapModified the servletTimersMapModified to set
	 */
	public void setServletTimersMapModified(boolean servletTimersMapModified) {
		this.servletTimersMapModified = servletTimersMapModified;
	}

	/**
	 * @return the servletTimersMapModified
	 */
	public boolean isServletTimersMapModified() {
		return servletTimersMapModified;
	}
}

