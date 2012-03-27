/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.rhq.plugins.mobicents.servlet.sip;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;

import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.plugins.jmx.MBeanResourceComponent;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class SipApplicationDispatcherComponent extends MBeanResourceComponent implements MeasurementFacet{
	private static final String NB_RESPONSES_PROCESSED_BY_SC_ATTRIBUTE_NAME = "responsesProcessedByStatusCode";
	private static final String NB_REQUESTS_PROCESSED_BY_METHOD_ATTRIBUTE_NAME = "requestsProcessedByMethod";
	private static final CharSequence REQUEST_METHOD = "numberOfRequests";
	private static final CharSequence RESPONSE_SC = "numberOfResponses";
	
	@Override
	public void getValues(MeasurementReport report, Set requests) {
		super.getValues(report, requests);
//		dumpEmsInformation();
		Map<String, AtomicLong> requestsProcessedByMethod = null;
		Map<String, AtomicLong> responsesProcessedBySc = null;
		try {
			EmsAttribute requestAttribute = getEmsBean().getAttribute(NB_REQUESTS_PROCESSED_BY_METHOD_ATTRIBUTE_NAME);
			requestsProcessedByMethod = requestsProcessedByMethod = (Map<String, AtomicLong>) requestAttribute.refresh();
		} catch (Throwable e) {
			log.error("An unexpected exception occured while trying to access the Sip Balancer requestsProcessedByMethod", e);
		}
		try {
			EmsAttribute responseAttribute = getEmsBean().getAttribute(NB_RESPONSES_PROCESSED_BY_SC_ATTRIBUTE_NAME);
			responsesProcessedBySc = (Map<String, AtomicLong>) responseAttribute.refresh();
		} catch (Throwable e) {
			log.error("An unexpected exception occured while trying to access the Sip Balancer responsesProcessedByStatusCode", e);
		}
		Set<MeasurementScheduleRequest> remainingSchedules = new LinkedHashSet<MeasurementScheduleRequest>();
        for (MeasurementScheduleRequest schedule : (Set<MeasurementScheduleRequest>)requests) {
            String metricName = schedule.getName();
            log.debug("trying to retrieve value for metric " + metricName);
            if (metricName.contains(REQUEST_METHOD)) {
        		try {
        			if(requestsProcessedByMethod != null) {
		        		String method = metricName.substring(REQUEST_METHOD.length());
		        		log.debug("method is " + method);
		        		AtomicLong requestsProcessed = requestsProcessedByMethod.get(method);
		            	Double value = Double.valueOf((double) requestsProcessed.get());
		            	log.debug("value for metric " + metricName + " is " + value);
		                MeasurementDataNumeric metric = new MeasurementDataNumeric(schedule, value);
		                report.addData(metric);
        			} else {
        				log.error(NB_REQUESTS_PROCESSED_BY_METHOD_ATTRIBUTE_NAME + " attribute is not available on the mbean");
        			}
        		} catch (Throwable e) {
        			log.error("An unexpected exception occured while trying to access the metric " + metricName + " from the requestProcessedByMethod", e);
        		}
            } else if (metricName.contains(RESPONSE_SC)) {
        		try {
        			if(responsesProcessedBySc != null) {
		        		String statusCode = metricName.substring(RESPONSE_SC.length());
		        		log.debug("statusCode is " + statusCode);
		        		AtomicLong responsesProcessed = responsesProcessedBySc.get(statusCode);
		        		Double value = Double.valueOf((double) responsesProcessed.get());
		        		log.debug("value for metric " + metricName + " is " + value);
		                MeasurementDataNumeric metric = new MeasurementDataNumeric(schedule, value);
		                report.addData(metric);
        			} else {
        				log.error(NB_RESPONSES_PROCESSED_BY_SC_ATTRIBUTE_NAME + " attribute is not available on the mbean");
        			}
        		} catch (Throwable e) {
        			log.error("An unexpected exception occured while trying to access the metric " + metricName + " from the responsesProcessedByStatusCode", e);
        		}
            } else {
            	remainingSchedules.add(schedule);
            }
        }		
	}
	
	public void dumpEmsInformation() {
		log.debug("Ems Attributes");
		SortedSet<EmsAttribute> attributes = getEmsBean().getAttributes();
		for (EmsAttribute emsAttribute : attributes) {
			log.debug("Ems Attribute: " + emsAttribute.getName() + " type = " + emsAttribute.getType());
		}
		log.debug("Ems Operations");
		SortedSet<EmsOperation> operations = getEmsBean().getOperations();
		for (EmsOperation emsOperation : operations) {
			log.debug("Ems Operation: " + emsOperation.getName() + " type = " + emsOperation.getParameters());
		}
	}

}
