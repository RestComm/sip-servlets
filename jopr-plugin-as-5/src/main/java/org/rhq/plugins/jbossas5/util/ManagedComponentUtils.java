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
package org.rhq.plugins.jbossas5.util;

import java.util.Set;
import java.util.EnumSet;
import java.io.Serializable;

import org.rhq.plugins.jbossas5.factory.ProfileServiceFactory;
import org.jetbrains.annotations.NotNull;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.metatype.api.values.EnumValue;
import org.jboss.metatype.api.types.MetaType;

/**
 * @author Ian Springer
 */
public class ManagedComponentUtils
{
    public static ManagedComponent getManagedComponent(ComponentType componentType, String componentName)
    {
        Set<ManagedComponent> components = getManagedComponents(componentType);
        for (ManagedComponent component : components)
        {
            if (component.getName().equals(componentName))
                return component;
        }
        return null;
    }

    public static ManagedComponent getSingletonManagedComponent(ComponentType componentType)
    {
        Set<ManagedComponent> components = getManagedComponents(componentType);
        if (components.size() != 1)
            throw new IllegalStateException("Found more than one component of type " + componentType + ": "
                    + components);
        @SuppressWarnings({"UnnecessaryLocalVariable"})
        ManagedComponent component = components.iterator().next();
        return component;
    }

    public static Serializable getSimplePropertyValue(ManagedComponent component, String propertyName)
    {
        ManagedProperty property = component.getProperty(propertyName);
        MetaType metaType = property.getMetaType();
        Serializable value;
        if (metaType.isSimple()) {
            SimpleValue simpleValue = (SimpleValue)property.getValue();
            value = (simpleValue != null) ? simpleValue.getValue() : null;
        } else if (metaType.isEnum()) {
            EnumValue enumValue = (EnumValue)property.getValue();
            value = (enumValue != null) ? enumValue.getValue() : null;
        } else {
            throw new IllegalStateException("Type of [" + property + "] is not simple or enum.");
        }
        return value;
    }

    @NotNull
    public static EnumSet<ViewUse> getViewUses(ManagedProperty managedProperty)
    {
        EnumSet<ViewUse> viewUses = EnumSet.noneOf(ViewUse.class);
        for (ViewUse viewUse : ViewUse.values()) {
           if (managedProperty.hasViewUse(viewUse))
               viewUses.add(viewUse);
        }
        return viewUses;
    }

    @NotNull
    private static Set<ManagedComponent> getManagedComponents(ComponentType componentType)
    {
        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
        Set<ManagedComponent> components;
        try
        {
            components = managementView.getComponentsForType(componentType);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
        return components;
    }
}