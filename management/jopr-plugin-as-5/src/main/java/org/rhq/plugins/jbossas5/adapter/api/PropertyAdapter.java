 /*
  * Jopr Management Platform
  * Copyright (C) 2005-2009 Red Hat, Inc.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License, version 2, as
  * published by the Free Software Foundation, and/or the GNU Lesser
  * General Public License, version 2.1, also as published by the Free
  * Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License and the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License
  * and the GNU Lesser General Public License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
package org.rhq.plugins.jbossas5.adapter.api;

import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;

/**
 * An adapter that can convert back and forth between a specific type of {@link MetaValue} and a specific
 * type of {@link Property}.
 *
 * @author Mark Spritzler
 * @author Ian Springer
 */ 
public interface PropertyAdapter<P extends Property, D extends PropertyDefinition>
{
    /**
     * Given a {@link Property} and its {@link PropertyDefinition}, as well as a {@link MetaValue}, populate that
     * MetaValue so it corresponds to the Property. If the Property is a list or a map, all descendant Property's should
     * be added as descendants of the MetaValue.
     *
     * @param property the property to be copied
     * @param metaValue the MetaValue to be populated; should not be null
     * @param propertyDefinition the property's definition
     */
    public void populateMetaValueFromProperty(P property, MetaValue metaValue, D propertyDefinition);

    /**
     * Given a {@link Property} and its {@link PropertyDefinition}, create and return a corresponding {@link MetaValue}
     * with the specified {@link MetaType}. If the Property is a list or a map, all descendant Property's should
     * be represented as descendants of the returned MetaValue. Generally this method can simply create an empty
     * MetaValue object and then delegate the population of the guts of the object to
     * {@link #populateMetaValueFromProperty(Property, MetaValue, PropertyDefinition)}.
     *
     * @param property the property to be converted
     * @param propertyDefinition the property's definition
     * @param metaType the type of MetaValue that should be created and returned
     * @return the MetaValue representation of the given Property
     */
    public MetaValue convertToMetaValue(P property, D propertyDefinition, MetaType metaType);

    /**
     * Given a {@link Property} and its {@link PropertyDefinition}, as well as a {@link MetaValue}, populate the
     * Property so it corresponds to the MetaValue. If the MetaValue is a from of list or map, all descendant
     * MetaValue's should be added as descendants of the Property.
     *
     * @param property the property to be populated; should not be null
     * @param metaValue the MetaValue to be copied
     * @param propertyDefinition the property's definition
     */
    public void populatePropertyFromMetaValue(P property, MetaValue metaValue, D propertyDefinition);

    /**
     * Given a {@link MetaValue}, create and return a corresponding {@link Property}
     * with the specified {@link PropertyDefinition}. If the MetaValue is a form of list or map, all descendant
     * MetaValue's should be represented as descendants of the returned Property. Generally this method can simply
     * create an empty Property object and then delegate the population of the guts of the object to
     * {@link #populatePropertyFromMetaValue(Property, MetaValue, PropertyDefinition)}.
     *
     * @param metaValue the metaValue to be converted
     * @param propertyDefinition the definition of the property to be created and returned
     * @return the Property representation of the given MetaValue
     */
    public P convertToProperty(MetaValue metaValue, D propertyDefinition);
}
