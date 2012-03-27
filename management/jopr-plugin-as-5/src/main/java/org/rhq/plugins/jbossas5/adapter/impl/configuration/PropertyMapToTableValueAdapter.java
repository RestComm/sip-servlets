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

 import java.util.Collection;
 import java.util.Map;

 import org.jboss.metatype.api.types.MetaType;
 import org.jboss.metatype.api.values.CompositeValue;
 import org.jboss.metatype.api.values.CompositeValueSupport;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.SimpleValueSupport;
 import org.jboss.metatype.api.values.TableValueSupport;

 import org.rhq.core.domain.configuration.Property;
 import org.rhq.core.domain.configuration.PropertyMap;
 import org.rhq.core.domain.configuration.definition.PropertyDefinition;
 import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
 import org.rhq.plugins.jbossas5.adapter.api.AbstractPropertyMapAdapter;
 import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
 import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapterFactory;

/**
 * @author Mark Spritzler
 */
public class PropertyMapToTableValueAdapter extends AbstractPropertyMapAdapter implements PropertyAdapter<PropertyMap, PropertyDefinitionMap>
{
    public PropertyMap convertToProperty(MetaValue metaValue, PropertyDefinitionMap propertyDefinition)
    {
        PropertyMap property = new PropertyMap();
        populatePropertyFromMetaValue(property, metaValue, propertyDefinition);
        return property;
    }

    //@todo need to implement this like the other Map to Composite, but not until there is an actual property that needs this
    public void populateMetaValueFromProperty(PropertyMap property, MetaValue metaValue, PropertyDefinitionMap propertyDefinition)
    {
        if (metaValue != null)
        {
            TableValueSupport tableValueSupport = (TableValueSupport) metaValue;
            Map<String, PropertyDefinition> map = propertyDefinition.getPropertyDefinitions();
            Map<String, Property> properties = property.getMap();
            for (String key : map.keySet())
            {
                PropertyDefinition definition = map.get(key);
                MetaValue[] getKey = new MetaValue[]{SimpleValueSupport.wrap(key)};
                MetaValue value = tableValueSupport.get(getKey);
                Property innerProperty = properties.get(key);
                PropertyAdapter adapter = PropertyAdapterFactory.getPropertyAdapter(value);
                adapter.populateMetaValueFromProperty(innerProperty, value, definition);
            }
        }
    }

    //@todo need to implement this like the other Map to Composite, but not until there is an actual property that needs this
    public MetaValue convertToMetaValue(PropertyMap property, PropertyDefinitionMap propertyDefinition, MetaType type)
    {
        return null;
    }

    public void populatePropertyFromMetaValue(PropertyMap property, MetaValue metaValue, PropertyDefinitionMap propertyDefinition)
    {
        // Not important at this moment to implement as there sin't a need for this mapping yet.
        if (metaValue != null)
        {
            TableValueSupport valueSupport = (TableValueSupport) metaValue;
            Collection<CompositeValue> values = valueSupport.values();
            for (CompositeValue value : values)
            {
                CompositeValueSupport support = (CompositeValueSupport) value;
            }
        }
    }
}
