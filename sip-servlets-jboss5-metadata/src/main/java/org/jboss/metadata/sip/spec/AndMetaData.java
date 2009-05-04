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
package org.jboss.metadata.sip.spec;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class AndMetaData extends ConditionMetaData {
	private static final long serialVersionUID = 1;
	private List<ConditionMetaData> conditions;
	/**
	 * @param condition the condition to set
	 */
	@XmlElements( {
			@XmlElement(name = "and", type = AndMetaData.class),
			@XmlElement(name = "contains", type = ContainsMetaData.class),
			@XmlElement(name = "equal", type = EqualMetaData.class),
			@XmlElement(name = "exists", type = ExistsMetaData.class),
			@XmlElement(name = "not", type = NotMetaData.class),
			@XmlElement(name = "or", type = OrMetaData.class),
			@XmlElement(name = "subdomain-of", type = SubdomainOfMetaData.class)})
	public void setConditions(List<ConditionMetaData> conditions) {
		this.conditions = conditions;
	}
	/**
	 * @return the condition
	 */
	public List<ConditionMetaData> getConditions() {
		return conditions;
	}
}
