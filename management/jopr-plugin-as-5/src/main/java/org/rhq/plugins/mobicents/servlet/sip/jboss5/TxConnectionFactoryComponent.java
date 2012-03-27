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
import java.util.HashSet;

import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.managed.api.ManagedProperty;

/**
 * A Resource component for JBoss AS 5 Tx Connection Factories.
 * 
 * @author Ian Springer
 */
public class TxConnectionFactoryComponent extends ManagedComponentComponent
{
    private final Log log = LogFactory.getLog(this.getClass());
    
    @Override
    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) throws Exception
    {
        Set<MeasurementScheduleRequest> uncollectedMetrics = new HashSet();
        for (MeasurementScheduleRequest request : metrics)
        {
            try {
                if (request.getName().equals("custom.transactionType")) {
                    ManagedProperty xaTransactionProp = getManagedProperties().get("xa-transaction");
                    SimpleValue xaTransactionMetaValue = (SimpleValue)xaTransactionProp.getValue();
                    Boolean xaTransactionValue = (xaTransactionMetaValue != null)
                            ? (Boolean)xaTransactionMetaValue.getValue() : null;
                    boolean isXa = (xaTransactionValue != null && xaTransactionValue);
                    String transactionType = (isXa) ? "XA" : "Local";
                    report.addData(new MeasurementDataTrait(request, transactionType));
                } else {
                    uncollectedMetrics.add(request);
                }
            } catch (Exception e) {
                log.error("Failed to collect metric for " + request, e);
            }
        }
        super.getValues(report, uncollectedMetrics);
    }
}
