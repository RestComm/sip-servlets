/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.tomcat.service.session.distributedcache.spi;

import java.util.HashMap;
import java.util.Map;


/**
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableSipApplicationSessionMetadata extends
		DistributableSessionMetadata {
	private transient boolean sipSessionsMapModified;
	private transient boolean httpSessionsMapModified;

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
}
