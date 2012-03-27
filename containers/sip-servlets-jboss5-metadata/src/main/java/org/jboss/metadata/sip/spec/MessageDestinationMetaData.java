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

import org.jboss.metadata.javaee.support.MergeableMappedMetaData;
import org.jboss.metadata.javaee.support.NamedMetaDataWithDescriptionGroup;
import org.jboss.xb.annotations.JBossXmlNsPrefix;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class MessageDestinationMetaData extends
		NamedMetaDataWithDescriptionGroup implements
		MergeableMappedMetaData<MessageDestinationMetaData> {
	/** The serialVersionUID */
	private static final long serialVersionUID = 2129990191983873784L;

	/** The mapped name */
	private String mappedName;

	/**
	 * Create a new MessageDestinationMetaData.
	 */
	public MessageDestinationMetaData() {
		// For serialization
	}

	/**
	 * Get the messageDestinationName.
	 * 
	 * @return the messageDestinationName.
	 */
	public String getMessageDestinationName() {
		return getName();
	}

	/**
	 * Set the messageDestinationName.
	 * 
	 * @param messageDestinationName
	 *            the messageDestinationName.
	 * @throws IllegalArgumentException
	 *             for a null messageDestinationName
	 */
	@XmlElement(name="message-destination-name")
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setMessageDestinationName(String messageDestinationName) {
		setName(messageDestinationName);
	}

	/**
	 * Get the mappedName.
	 * 
	 * @return the mappedName.
	 */
	public String getMappedName() {
		return mappedName;
	}

	/**
	 * Set the mappedName.
	 * 
	 * @param mappedName
	 *            the mappedName.
	 * @throws IllegalArgumentException
	 *             for a null mappedName
	 */
	@XmlElement(name="mapped-name", required = false)	
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setMappedName(String mappedName) {
		if (mappedName == null)
			throw new IllegalArgumentException("Null mappedName");
		this.mappedName = mappedName;
	}

	/**
	 * Get the jndiName.
	 * 
	 * @return the jndiName.
	 */
	public String getJndiName() {
		return getMappedName();
	}

	/**
	 * Set the jndiName.
	 * 
	 * @param jndiName
	 *            the jndiName.
	 * @throws IllegalArgumentException
	 *             for a null jndiName
	 */
	@XmlElement(name="jndi-name", required = false)
	@JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	public void setJndiName(String jndiName) {
		setMappedName(jndiName);
	}

	public MessageDestinationMetaData merge(MessageDestinationMetaData original) {
		MessageDestinationMetaData merged = new MessageDestinationMetaData();
		merged.merge(this, original);
		return merged;
	}

	/**
	 * Merge the contents of override with original into this.
	 * 
	 * @param override
	 *            data which overrides original
	 * @param original
	 *            the original data
	 */
	public void merge(MessageDestinationMetaData override,
			MessageDestinationMetaData original) {
		super.merge(override, original);
		if (override != null && override.mappedName != null)
			setMappedName(override.mappedName);
		else if (original.mappedName != null)
			setMappedName(original.mappedName);
	}
}
