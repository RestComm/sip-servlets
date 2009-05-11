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

 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;

 import org.jboss.metatype.api.types.MetaType;
 import org.jboss.metatype.api.types.MapCompositeMetaType;
 import org.jboss.metatype.api.types.PropertiesMetaType;
 import org.jboss.metatype.api.values.MetaValue;

 import org.rhq.core.domain.configuration.PropertySimple;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyListToArrayValueAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyListToCollectionValueAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToMapCompositeValueSupportAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToTableValueAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertySimpleToSimpleValueAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToGenericValueAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertySimpleToEnumValueAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToCompositeValueSupportAdapter;
 import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToPropertiesValueAdapter;
 import org.jetbrains.annotations.Nullable;

 /**
 * @author Mark Spritzler
 */
public class PropertyAdapterFactory
{
    private static final Log LOG = LogFactory.getLog(PropertyAdapterFactory.class);

    public static PropertyAdapter getPropertyAdapter(MetaValue metaValue)
    {
        if (metaValue == null)
        {
            LOG.debug("The MetaValue passed in is null.");
            return null;
        }
        MetaType metaType = metaValue.getMetaType();
        return getPropertyAdapter(metaType);
    }

    public static PropertyAdapter getPropertyAdapter(MetaType metaType)
    {
        PropertyAdapter propertyAdapter = null;
        if (metaType.isSimple())
        {
            propertyAdapter = new PropertySimpleToSimpleValueAdapter();
        }
        else if (metaType.isGeneric())
        {
            propertyAdapter = new PropertyMapToGenericValueAdapter();
        }
        else if (metaType.isComposite())
        {
            if (metaType instanceof MapCompositeMetaType)
                propertyAdapter = new PropertyMapToMapCompositeValueSupportAdapter();
            else
                propertyAdapter = new PropertyMapToCompositeValueSupportAdapter();
        }
        else if (metaType.isTable())
        {
            propertyAdapter = new PropertyMapToTableValueAdapter();
        }
        else if (metaType.isCollection())
        {
            propertyAdapter = new PropertyListToCollectionValueAdapter();
        }
        else if (metaType.isArray())
        {
            propertyAdapter = new PropertyListToArrayValueAdapter();
        }
        else if (metaType.isEnum())
        {
            propertyAdapter = new PropertySimpleToEnumValueAdapter();
        }
        else if (metaType instanceof PropertiesMetaType)
        {
            propertyAdapter = new PropertyMapToPropertiesValueAdapter();
        }
        else
        {
            LOG.warn("Unsupported MetaType: " + metaType);
        }
        return propertyAdapter;
    }

    @Nullable
    public static PropertyAdapter getCustomPropertyAdapter(PropertySimple customProp)
    {
        if (customProp == null)
            return null;        
        String adapterClassName = customProp.getStringValue();
        PropertyAdapter propertyAdapter = null;
        try
        {
            Class adapterClass = Class.forName(adapterClassName);
            propertyAdapter = (PropertyAdapter) adapterClass.newInstance();
        }
        catch (Exception e)
        {
            LOG.error("Unable to create custom adapter class for " + customProp + ".", e);
        }
        return propertyAdapter;
    }
}
