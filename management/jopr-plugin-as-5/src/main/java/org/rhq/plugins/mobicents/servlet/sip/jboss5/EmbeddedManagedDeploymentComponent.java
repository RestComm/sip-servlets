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
package org.rhq.plugins.mobicents.servlet.sip.jboss5;

import java.util.Set;

import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.measurement.MeasurementDataTrait;

import org.jboss.profileservice.spi.NoSuchDeploymentException;

/**
 * @author Ian Springer
 */
public class EmbeddedManagedDeploymentComponent extends AbstractManagedDeploymentComponent
    implements MeasurementFacet
{
    public static final String CUSTOM_PARENT_TRAIT = "custom.parent";

    // ------------ MeasurementFacet Implementation ------------

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests)
            throws NoSuchDeploymentException
    {
        for (MeasurementScheduleRequest request : requests) {
            String metricName = request.getName();
            if (metricName.equals(CUSTOM_PARENT_TRAIT)) {
                String parentDeploymentName = getManagedDeployment().getParent().getName();
                MeasurementDataTrait trait = new MeasurementDataTrait(request, parentDeploymentName);
                report.addData(trait);
            }
        }
    }
}
