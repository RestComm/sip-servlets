/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.plugins.jbossas5.adapter.impl.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.plugins.ManagedObjectImpl;
import org.jboss.managed.plugins.ManagedPropertyImpl;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.GenericMetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.values.GenericValueSupport;

import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapterFactory;
import org.rhq.plugins.jbossas5.adapter.api.AbstractPropertyMapAdapter;
import org.rhq.plugins.jbossas5.util.ConversionUtils;

/**
 * This class provides code that maps back and forth between a {@link PropertyMap} and
 * a {@link GenericValue}. The GenericValue's value is assumed to be a {@link ManagedObject}.
 *
 * @author Ian Springer
 */
public class PropertyMapToGenericValueAdapter extends AbstractPropertyMapAdapter
        implements PropertyAdapter<PropertyMap, PropertyDefinitionMap> {
    private final Log log = LogFactory.getLog(this.getClass());

    public void populateMetaValueFromProperty(PropertyMap propMap, MetaValue metaValue, PropertyDefinitionMap propDefMap) {
        GenericValue genericValue = (GenericValue)metaValue;
        if (!(genericValue.getValue() instanceof ManagedObject)) {
            log.error("GenericValue's value [" + genericValue.getValue() + "] is not a ManagedObject - not supported!");
            return;
        }
        ManagedObject managedObject = (ManagedObject)genericValue.getValue();
        for(String propName : propMap.getMap().keySet()) {
            Property mapMemberProp = propMap.get(propName);
            ManagedProperty managedProp = managedObject.getProperty(propName);
            MetaType metaType = managedProp.getMetaType();
            PropertyAdapter propertyAdapter = PropertyAdapterFactory.getPropertyAdapter(metaType);
            PropertyDefinition mapMemberPropDef = propDefMap.get(propName);
            if (managedProp.getValue() == null)
            {
                MetaValue managedPropMetaValue = propertyAdapter.convertToMetaValue(mapMemberProp, mapMemberPropDef, metaType);
                managedProp.setValue(managedPropMetaValue);
            }
            else
            {
                MetaValue managedPropMetaValue = (MetaValue)managedProp.getValue();
                propertyAdapter.populateMetaValueFromProperty(mapMemberProp, managedPropMetaValue, mapMemberPropDef);
            }                      
        }
    }

    public MetaValue convertToMetaValue(PropertyMap propMap, PropertyDefinitionMap propDefMap, MetaType metaType) {
        //GenericMetaType genericMetaType = (GenericMetaType)metaType;
        ManagedObjectImpl managedObject = new ManagedObjectImpl(propDefMap.getName());
        for (PropertyDefinition mapMemberPropDef : propDefMap.getPropertyDefinitions().values()) {
            ManagedPropertyImpl managedProp = new ManagedPropertyImpl(mapMemberPropDef.getName());
            MetaType managedPropMetaType = ConversionUtils.convertPropertyDefinitionToMetaType(mapMemberPropDef);
            managedProp.setMetaType(managedPropMetaType);
            managedProp.setManagedObject(managedObject);
            managedObject.getProperties().put(managedProp.getName(), managedProp);
        }
        GenericValue genericValue = new GenericValueSupport(new GenericMetaType(propDefMap.getName(),
                propDefMap.getDescription()), managedObject);
        populateMetaValueFromProperty(propMap, genericValue, propDefMap);
        return genericValue;
    }

    public void populatePropertyFromMetaValue(PropertyMap propMap, MetaValue metaValue, PropertyDefinitionMap propDefMap) {
        GenericValue genericValue = (GenericValue)metaValue;
        ManagedObject managedObject = (ManagedObject)genericValue.getValue();
        for (String propName : propDefMap.getPropertyDefinitions().keySet()) {
            ManagedProperty managedProp = managedObject.getProperty(propName);
            if (managedProp != null) {
                MetaType metaType = managedProp.getMetaType();
                Object value;
                if (metaType.isSimple()) {
                    SimpleValue simpleValue = (SimpleValue)managedProp.getValue();
                    value = simpleValue.getValue();
                } else if (metaType.isEnum()) {
                    EnumValue enumValue = (EnumValue)managedProp.getValue();
                    value = enumValue.getValue();
                } else {
                    log.error("Nested ManagedProperty's value [" + managedProp.getValue()
                            + "] is not a SimpleValue or EnumValue - unsupported!");
                    continue;
                }
                propMap.put(new PropertySimple(propName, value));
            }
        }
    }
}
