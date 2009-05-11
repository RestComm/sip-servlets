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
package org.rhq.plugins.jbossas5.adapter.impl.configuration.custom;

import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionList;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.plugins.jbossas5.adapter.api.AbstractPropertyListAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapterFactory;
import org.rhq.plugins.jbossas5.adapter.impl.configuration.PropertyMapToMapCompositeValueSupportAdapter;
import org.rhq.plugins.jbossas5.util.ConversionUtils;

import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.CompositeValue;
import org.jboss.metatype.api.values.CompositeValueSupport;
import org.jboss.metatype.api.values.MapCompositeValueSupport;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.metatype.plugins.types.MutableCompositeMetaType;

/**
 * This class provides code that maps back and forth between a {@link PropertyList} of {@link PropertyMap}s and
 * a {@link MapCompositeValueSupport} with the key being a JMS security role name and the value being a
 * {@link CompositeValueSupport}s representing the role's read/write/create permissions. Here's what the
 * PropertyList looks like:
 *  
 * <code>
 *   &lt;c:list-property name="securityConfig" min="2" max="4"
 *                    displayName="Security Configurations"
 *                    description="This element specifies a XML fragment which describes the access control list to be
 *                                 used by the SecurityManager to authorize client operations against the destination.
 *                                 The content model is the same as for the SecurityManager SecurityConf attribute."&gt;
 *       &lt;c:map-property name="role"
 *                       displayName="Security Configuration Attributes"
 *                       description="These are the attributes that define the role name, and if the role is allowed to
 *                                    read, write or create Messages on this Queue."&gt;
 *           &lt;c:simple-property name="name"
 *                              description="Name of the Security Role. e.g. Guest"
 *                              summary="true" required="true"/&gt;
 *           &lt;c:simple-property name="read"
 *                              description="Is this role allowed to read messages?"
 *                               summary="true" required="false" type="boolean"/&gt;
 *           &lt;c:simple-property name="write"
 *                              description="Is this role allowed to write messages?"
 *                              summary="true" required="false" type="boolean"/&gt;
 *           &lt;c:simple-property name="create"
 *                              description="Is this role allowed to create messages?"
 *                              summary="true" required="false" type="boolean"/&gt;
 *       &lt;/c:map-property&gt;
 *   &lt;/c:list-property&gt;
 * </code>
 *
 * @author Ian Springer
 */
public class JMSSecurityConfigAdapter extends AbstractPropertyListAdapter
{
    public MetaValue convertToMetaValue(PropertyList propertyList, PropertyDefinitionList propertyDefinitionList,
                                        MetaType metaType)
    {
        MapCompositeMetaType securityConfigCompositeMetaType = (MapCompositeMetaType)metaType;
        MapCompositeValueSupport securityConfigCompositeValue =
                new MapCompositeValueSupport(securityConfigCompositeMetaType.getValueType());
        populateMetaValueFromProperty(propertyList, securityConfigCompositeValue, propertyDefinitionList);
        return securityConfigCompositeValue;
    }

    public void populateMetaValueFromProperty(PropertyList propertyList, MetaValue metaValue,
                                              PropertyDefinitionList propertyDefinitionList)
    {
        MapCompositeValueSupport securityConfigCompositeValue = (MapCompositeValueSupport)metaValue;
        PropertyDefinitionMap memberPropDefMap = (PropertyDefinitionMap)propertyDefinitionList.getMemberDefinition();        
        for (Property memberProperty : propertyList.getList()) {
            PropertyMap memberPropMap = (PropertyMap)memberProperty;
            String roleName = memberPropMap.getSimple("name").getStringValue();
            CompositeValueSupport roleCompositeValue = (CompositeValueSupport)createCompositeValue(memberPropDefMap);
            populateMetaValueFromProperty(memberPropMap, roleCompositeValue, memberPropDefMap);
            securityConfigCompositeValue.put(roleName, roleCompositeValue);
        }
    }

    public void populatePropertyFromMetaValue(PropertyList propertyList, MetaValue metaValue,
                                              PropertyDefinitionList propertyDefinitionList)
    {
        CompositeValue compositeValue = (CompositeValue)metaValue;
        CompositeMetaType compositeMetaType = compositeValue.getMetaType();
        PropertyDefinitionMap memberPropertyDefinitionMap = (PropertyDefinitionMap)propertyDefinitionList.getMemberDefinition();
        PropertyMapToMapCompositeValueSupportAdapter mapToMapCompositeValueAdapter = new PropertyMapToMapCompositeValueSupportAdapter();
        for (String memberName : compositeMetaType.itemSet()) {
            MetaValue memberMetaValue = compositeValue.get(memberName);
            PropertyMap memberPropertyMap = mapToMapCompositeValueAdapter.convertToProperty(memberMetaValue, memberPropertyDefinitionMap);
            memberPropertyMap.put(new PropertySimple("name", memberName)); // add a simple for the role name to the map
            propertyList.add(memberPropertyMap);
        }
    }

    // NOTE: We can't just leverage PropertyMapToCompositeValueSupportAdapter, because we have to skip the "name" map member.
    private CompositeValue createCompositeValue(PropertyDefinitionMap propDefMap) {
        String name = (propDefMap != null) ?
                propDefMap.getName() : "CompositeMetaType";
        String desc = (propDefMap != null && propDefMap.getDescription() != null) ?
                propDefMap.getDescription() : "none";
        MutableCompositeMetaType compositeMetaType = new MutableCompositeMetaType(name, desc);
        if (propDefMap != null) {
            for (PropertyDefinition mapMemberPropDef : propDefMap.getPropertyDefinitions().values()) {
                if (mapMemberPropDef.getName().equals("name"))
                    continue;
                String mapMemberDesc = (propDefMap.getDescription() != null) ? propDefMap.getDescription() : "none";
                MetaType mapMemberMetaType = ConversionUtils.convertPropertyDefinitionToMetaType(mapMemberPropDef);
                compositeMetaType.addItem(mapMemberPropDef.getName(), mapMemberDesc, mapMemberMetaType);
            }
        }
        return new CompositeValueSupport(compositeMetaType);
    }

    // NOTE: We can't just leverage PropertyMapToCompositeValueSupportAdapter, because we have to skip the "name" map member.
    private void populateMetaValueFromProperty(PropertyMap propMap, MetaValue metaValue, PropertyDefinitionMap propDefMap) {
        CompositeValueSupport compositeValue = (CompositeValueSupport)metaValue;
        for (String mapMemberPropName : propMap.getMap().keySet()) {
            if (mapMemberPropName.equals("name"))
                continue;
            Property mapMemberProp = propMap.get(mapMemberPropName);
            PropertyDefinition mapMemberPropDef = propDefMap.get(mapMemberPropName);
            MetaType mapMemberMetaType = compositeValue.getMetaType().getType(mapMemberPropName);
            if (mapMemberMetaType == null) {
            	// this will occur when new map properties are added since they are not present
            	// in the original metaValue which we are using
            	mapMemberMetaType = SimpleMetaType.STRING;
            }
            PropertyAdapter adapter = PropertyAdapterFactory.getPropertyAdapter(mapMemberMetaType);
            MetaValue mapMemberMetaValue = adapter.convertToMetaValue(mapMemberProp, mapMemberPropDef, mapMemberMetaType);
            compositeValue.set(mapMemberPropName, mapMemberMetaValue);
        }
    }
}
