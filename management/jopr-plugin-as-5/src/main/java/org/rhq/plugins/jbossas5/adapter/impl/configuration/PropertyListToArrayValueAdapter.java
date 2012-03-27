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
package org.rhq.plugins.jbossas5.adapter.impl.configuration;

import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.ArrayValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.MetaValueFactory;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionList;
import org.rhq.plugins.jbossas5.adapter.api.AbstractPropertyListAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapterFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Spritzler
 */
public class PropertyListToArrayValueAdapter extends AbstractPropertyListAdapter implements PropertyAdapter<PropertyList, PropertyDefinitionList>
{
    //@todo need to implement this like the other List to Collection, but not until there is an actual property that needs this
    public void populateMetaValueFromProperty(PropertyList property, MetaValue metaValue, PropertyDefinitionList propertyDefinition)
    {
        PropertyDefinition memberDefinition = propertyDefinition.getMemberDefinition();
        List<Property> properties = property.getList();
        if (metaValue != null)
        {
            ArrayValueSupport valueSupport = (ArrayValueSupport) metaValue;
            MetaType listMetaType = valueSupport.getMetaType().getElementType();
            List<MetaValue> values = new ArrayList<MetaValue>(properties.size());
            for (Property propertyWithinList : properties)
            {
                MetaValue value = MetaValueFactory.getInstance().create(null);
                PropertyAdapter propertyAdapter = PropertyAdapterFactory.getPropertyAdapter(listMetaType);
                propertyAdapter.populateMetaValueFromProperty(propertyWithinList, value, memberDefinition);
                values.add(value);
            }
            valueSupport.setValue(values.toArray());
        }
    }

    //@todo need to implement this like the other List to Collection, but not until there is an actual property that needs this
    public MetaValue convertToMetaValue(PropertyList property, PropertyDefinitionList propertyDefinition, MetaType type)
    {
        return null;
    }

    public void populatePropertyFromMetaValue(PropertyList property, MetaValue metaValue, PropertyDefinitionList propertyDefinition)
    {
        PropertyDefinition memberDefinition = propertyDefinition.getMemberDefinition();
        List<Property> properties = property.getList();

        // Since we want to load the PropertyList with fresh values, we want it cleared out
        properties.clear();

        if (metaValue != null)
        {
            ArrayValueSupport valueSupport = (ArrayValueSupport) metaValue;
            MetaType listMetaType = valueSupport.getMetaType().getElementType();
            MetaValue[] metaValues = (MetaValue[]) valueSupport.getValue();
            PropertyAdapter propertyAdapter = PropertyAdapterFactory.getPropertyAdapter(listMetaType);
            for (MetaValue value : metaValues)
            {
                Property propertyToAddToList = propertyAdapter.convertToProperty(value, memberDefinition);
                properties.add(propertyToAddToList);
            }
        }
    }
}
