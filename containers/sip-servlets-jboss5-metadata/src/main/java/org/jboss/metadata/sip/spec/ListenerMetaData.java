/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.jboss.metadata.sip.spec;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.metadata.javaee.support.IdMetaDataImplWithDescriptionGroup;
import org.jboss.xb.annotations.JBossXmlNsPrefix;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class ListenerMetaData extends IdMetaDataImplWithDescriptionGroup {
	private static final long serialVersionUID = 1;

	private String listenerClass;

	public String getListenerClass() {
		return listenerClass;
	}
	@XmlElement(name="listener-class")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setListenerClass(String listenerClass) {
		this.listenerClass = listenerClass;
	}

	public String toString() {
		StringBuilder tmp = new StringBuilder("ListenerMetaData(id=");
		tmp.append(getId());
		tmp.append(",listenerClass=");
		tmp.append(listenerClass);
		tmp.append(')');
		return tmp.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((listenerClass == null) ? 0 : listenerClass.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListenerMetaData other = (ListenerMetaData) obj;
		if (listenerClass == null) {
			if (other.listenerClass != null)
				return false;
		} else if (!listenerClass.equals(other.listenerClass))
			return false;
		return true;
	}
	
	
}
