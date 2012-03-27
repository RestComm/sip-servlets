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

import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.operation.OperationDefinition;
import org.jetbrains.annotations.Nullable;

/**
 * @author Ian Springer
 */
public class ResourceTypeUtils
{
    /**
     * TODO
     * @param resourceType
     * @param metricName
     * @return
     */
    @Nullable
    public static MeasurementDefinition getMeasurementDefinition(ResourceType resourceType, String metricName) {
        Set<MeasurementDefinition> metricDefinitions = resourceType.getMetricDefinitions();
        for (MeasurementDefinition metricDefinition : metricDefinitions)
        {
            if (metricDefinition.getName().equals(metricName))
                return metricDefinition;
        }
        return null;
    }

    /**
     * TODO
     * @param resourceType
     * @param operationName
     * @return
     */
    @Nullable
    public static OperationDefinition getOperationDefinition(ResourceType resourceType, String operationName) {
        Set<OperationDefinition> operationDefinitions = resourceType.getOperationDefinitions();
        for (OperationDefinition operationDefinition : operationDefinitions)
        {
            if (operationDefinition.getName().equals(operationName))
                return operationDefinition;
        }
        return null;
    }

    private ResourceTypeUtils()
    {
    }
}
