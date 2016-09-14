/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

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
 * @author jean.deruelle@gmail.com
 * @author posfai.gergely@ext.alerant.hu
 *
 */
public class DistributableSipApplicationSessionMetadata extends
		DistributableSessionMetadata implements Serializable {
	
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

