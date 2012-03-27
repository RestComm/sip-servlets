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

 import java.io.Serializable;

 import org.rhq.core.domain.configuration.PropertySimple;
 import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;
 import org.rhq.plugins.jbossas5.adapter.api.AbstractPropertySimpleAdapter;
 import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;

 import org.jboss.metatype.api.types.MetaType;
 import org.jboss.metatype.api.types.SimpleMetaType;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.SimpleValue;
 import org.jboss.metatype.api.values.SimpleValueSupport;

/**
 * This class provides code that maps back and forth between a {@link PropertySimple} and
 * a {@link SimpleValueSupport}.
 * 
 * @author Ian Springer
 * @author Mark Spritzler
 */
public class PropertySimpleToSimpleValueAdapter extends AbstractPropertySimpleAdapter implements PropertyAdapter<PropertySimple, PropertyDefinitionSimple>
{
    public void populatePropertyFromMetaValue(PropertySimple propSimple, MetaValue metaValue, PropertyDefinitionSimple propDefSimple)
    {
        Object value = (metaValue != null) ? ((SimpleValue) metaValue).getValue() : null;
        propSimple.setValue(value);
    }

    public MetaValue convertToMetaValue(PropertySimple propSimple, PropertyDefinitionSimple propDefSimple, MetaType metaType)
    {
        SimpleValue simpleValue = new SimpleValueSupport((SimpleMetaType) metaType, null);
        populateMetaValueFromProperty(propSimple, simpleValue, propDefSimple);                    
        return simpleValue;
    }

    protected void setInnerValue(String propSimpleValue, MetaValue metaValue, PropertyDefinitionSimple propDefSimple)
    {
        SimpleValueSupport simpleValueSupport = (SimpleValueSupport) metaValue;
        if (propSimpleValue == null) {
            // A null value is the easiest case - just set the SimpleMetaValue's inner value to null.
            simpleValueSupport.setValue(null);
            return;
        }
        // String value is non-null, so we can massage it into the proper type for the SimpleMetaValue's inner value.
        SimpleMetaType simpleMetaType = simpleValueSupport.getMetaType();
        Serializable innerValue;
        if (simpleMetaType.equals(SimpleMetaType.STRING) || simpleMetaType.equals(SimpleMetaType.NAMEDOBJECT))
            innerValue = propSimpleValue;
        else if (simpleMetaType.equals(SimpleMetaType.BOOLEAN) || simpleMetaType.equals(SimpleMetaType.BOOLEAN_PRIMITIVE))
            innerValue = Boolean.valueOf(propSimpleValue);
        else if (simpleMetaType.equals(SimpleMetaType.BYTE) || simpleMetaType.equals(SimpleMetaType.BYTE_PRIMITIVE))
            innerValue = Byte.valueOf(propSimpleValue);
        else if (simpleMetaType.equals(SimpleMetaType.CHARACTER) || simpleMetaType.equals(SimpleMetaType.CHARACTER_PRIMITIVE)) {
            if (propSimpleValue.length() != 1)
                throw new IllegalStateException("String value '" + propSimpleValue + " cannot be converted to a character.");
            innerValue = propSimpleValue.charAt(0);
        } else if (simpleMetaType.equals(SimpleMetaType.DOUBLE) || simpleMetaType.equals(SimpleMetaType.DOUBLE_PRIMITIVE))
            innerValue = Double.valueOf(propSimpleValue);
        else if (simpleMetaType.equals(SimpleMetaType.FLOAT) || simpleMetaType.equals(SimpleMetaType.FLOAT_PRIMITIVE))
            innerValue = Float.valueOf(propSimpleValue);
        else if (simpleMetaType.equals(SimpleMetaType.INTEGER) || simpleMetaType.equals(SimpleMetaType.INTEGER_PRIMITIVE))
            innerValue = Integer.valueOf(propSimpleValue);
        else if (simpleMetaType.equals(SimpleMetaType.LONG) || simpleMetaType.equals(SimpleMetaType.LONG_PRIMITIVE))
            innerValue = Long.valueOf(propSimpleValue);
        else if (simpleMetaType.equals(SimpleMetaType.SHORT) || simpleMetaType.equals(SimpleMetaType.SHORT_PRIMITIVE))
            innerValue = Short.valueOf(propSimpleValue);
        else
            throw new IllegalStateException("Unsupported MetaType: " + simpleMetaType);
        simpleValueSupport.setValue(innerValue);
    }
}
