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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.attribute.EmsAttribute;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.plugins.jmx.ObjectNameQueryUtility;

/**
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class ConvergedSipStandaloneComponent extends StandaloneManagedDeploymentComponent
{   
    private final Log log = LogFactory.getLog(this.getClass());

    private static final String SERVLET_PREFIX = "Servlet.";
    private static final String SIP_SERVLET_PREFIX = "SipServlet.";
    public static final String CONTEXT_ROOT_CONFIG_PROP = "contextRoot";
    public static final String NAME_CONFIG_PROP = "name";
    public static final String FILE_NAME = "filename";
    public static final String JBOSS_WEB_NAME = "jbossWebName";
    public static final String RESPONSE_TIME_LOG_FILE_CONFIG_PROP = "responseTimeLogFile";
    public static final String RESPONSE_TIME_URL_EXCLUDES_CONFIG_PROP = "responseTimeUrlExcludes";
    public static final String RESPONSE_TIME_URL_TRANSFORMS_CONFIG_PROP = "responseTimeUrlTransforms";

    private static final String RESPONSE_TIME_METRIC = "ResponseTime";
    private static final String CONTEXT_ROOT_METRIC = "ContextRoot";

    private static final String MAX_SERVLET_TIME = "Servlet.MaxResponseTime";
    private static final String MIN_SERVLET_TIME = "Servlet.MinResponseTime";
    private static final String AVG_SERVLET_TIME = "Servlet.AvgResponseTime";
    private static final String NUM_SERVLET_REQUESTS = "Servlet.NumRequests";
    private static final String NUM_SERVLET_ERRORS = "Servlet.NumErrors";
    private static final String TOTAL_TIME = "Servlet.TotalTime";
    private static final String SERVLET_NAME_BASE_TEMPLATE = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=Servlet,name=%name%";
    private static final String SIP_SERVLET_NAME_BASE_TEMPLATE = "jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=SipServlet,name=%name%";
    
    private static final String SESSION_PREFIX = "Session.";
    private static final String SIP_SESSION_NAME_BASE_TEMPLATE = "jboss.web:host=%HOST%,type=SipManager,path=%PATH%";
    private static final String SIP_SESSION_PREFIX = "SipSession.";
    
    private static final String VHOST_PREFIX = "Vhost";
    public static final String VHOST_CONFIG_PROP = "vHost";

    public static final String ROOT_WEBAPP_CONTEXT_ROOT = "/";

//    private EmsBean jbossWebMBean;
//    private ResponseTimeLogParser logParser;
//    String vhost;
    private String contextRoot;
    
    ApplicationServerComponent applicationServerComponent; 
    
    @Override
    public void start(ResourceContext resourceContext) throws Exception {
    	super.start(resourceContext);
    	applicationServerComponent = (ApplicationServerComponent)resourceContext.getParentResourceComponent();
    	// -4 is for the extension .war to be removed
    	this.contextRoot = deploymentName.substring(0, deploymentName.length() - 5);
    	this.contextRoot = contextRoot.substring(contextRoot.lastIndexOf("/") + 1);
    	
    }
    
    // ------------ MeasurementFacet Implementation ------------

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) {
        super.getValues(report, requests);
        Set<MeasurementScheduleRequest> remainingSchedules = new LinkedHashSet<MeasurementScheduleRequest>();
        for (MeasurementScheduleRequest schedule : requests) {
            String metricName = schedule.getName();
            if (metricName.equals(RESPONSE_TIME_METRIC)) {
//                if (this.logParser != null) {
//                    try {
//                        CallTimeData callTimeData = new CallTimeData(schedule);
//                        this.logParser.parseLog(callTimeData);
//                        report.addData(callTimeData);
//                    } catch (Exception e) {
//                        log.error("Failed to retrieve HTTP call-time data.", e);
//                    }
//                } else {
//                    log.error("The '" + RESPONSE_TIME_METRIC + "' metric is enabled for WAR resource '"
//                        + getApplicationName() + "', but no value is defined for the '"
//                        + RESPONSE_TIME_LOG_FILE_CONFIG_PROP + "' connection property.");
//                    // TODO: Communicate this error back to the server for display in the GUI.
//                }
            } else if (metricName.equals(CONTEXT_ROOT_METRIC)) {
                MeasurementDataTrait trait = new MeasurementDataTrait(schedule, contextRoot);
                report.addData(trait);
            } else if (metricName.startsWith(SERVLET_PREFIX) || metricName.startsWith(SIP_SERVLET_PREFIX)) {
                Double value = getServletMetric(metricName);
                MeasurementDataNumeric metric = new MeasurementDataNumeric(schedule, value);
                report.addData(metric);
            } else if (metricName.startsWith(SESSION_PREFIX) || metricName.startsWith(SIP_SESSION_PREFIX)) {
                Double value = getSessionMetric(metricName);
                MeasurementDataNumeric metric = new MeasurementDataNumeric(schedule, value);
                report.addData(metric);
            } else if (metricName.startsWith(VHOST_PREFIX)) {
//                if (metricName.equals("Vhost.name")) {
//                    List<EmsBean> beans = getVHosts(contextRoot);
//                    String value = "";
//                    Iterator<EmsBean> iter = beans.iterator();
//                    while (iter.hasNext()) {
//                        EmsBean eBean = iter.next();
//                        value += eBean.getBeanName().getKeyProperty("host");
//                        if (iter.hasNext())
//                            value += ",";
//                    }
//                    MeasurementDataTrait trait = new MeasurementDataTrait(schedule, value);
//                    report.addData(trait);
//                }
            } else {
                remainingSchedules.add(schedule);
            }
        }
    }

    private Double getServletMetric(String metricName) {

        EmsConnection jmxConnection = applicationServerComponent.getEmsConnection();

        String servletNameBaseTemplate = SERVLET_NAME_BASE_TEMPLATE;
        if(metricName.startsWith(SIP_SERVLET_PREFIX)) {
        	servletNameBaseTemplate = SIP_SERVLET_NAME_BASE_TEMPLATE;
        }
        //FIXME : replace localhost with the real vhost
        String servletMBeanNames = servletNameBaseTemplate + ",WebModule=//localhost" 
                + applicationServerComponent.getContextPath(this.contextRoot);
        ObjectNameQueryUtility queryUtility = new ObjectNameQueryUtility(servletMBeanNames);
        List<EmsBean> mBeans = jmxConnection.queryBeans(queryUtility.getTranslatedQuery());

        long min = Long.MAX_VALUE;
        long max = 0;
        long processingTime = 0;
        int requestCount = 0;
        int errorCount = 0;
        Double result;

        for (EmsBean mBean : mBeans) {
            mBean.refreshAttributes();
            if (metricName.equals(MIN_SERVLET_TIME)) {
                EmsAttribute att = mBean.getAttribute("minTime");
                Long l = (Long) att.getValue();
                if (l < min)
                    min = l;
            } else if (metricName.equals(MAX_SERVLET_TIME)) {
                EmsAttribute att = mBean.getAttribute("maxTime");
                Long l = (Long) att.getValue();
                if (l > max)
                    max = l;
            } else if (metricName.equals(AVG_SERVLET_TIME)) {
                EmsAttribute att = mBean.getAttribute("processingTime");
                Long l = (Long) att.getValue();
                processingTime += l;
                att = mBean.getAttribute("requestCount");
                Integer i = (Integer) att.getValue();
                requestCount += i;
            } else if (metricName.equals(NUM_SERVLET_REQUESTS)) {
                EmsAttribute att = mBean.getAttribute("requestCount");
                Integer i = (Integer) att.getValue();
                requestCount += i;
            } else if (metricName.equals(NUM_SERVLET_ERRORS)) {
                EmsAttribute att = mBean.getAttribute("errorCount");
                Integer i = (Integer) att.getValue();
                errorCount += i;
            } else if (metricName.equals(TOTAL_TIME)) {
                EmsAttribute att = mBean.getAttribute("processingTime");
                Long l = (Long) att.getValue();
                processingTime += l;
            }
        }
        if (metricName.equals(AVG_SERVLET_TIME)) {
            result = (requestCount > 0) ? ((double) processingTime / (double) requestCount) : Double.NaN;
        } else if (metricName.equals(MIN_SERVLET_TIME)) {
            result = (min != Long.MAX_VALUE) ? (double) min : Double.NaN;
        } else if (metricName.equals(MAX_SERVLET_TIME)) {
            result = (max != 0) ? (double) max : Double.NaN;
        } else if (metricName.equals(NUM_SERVLET_ERRORS)) {
            result = (double) errorCount;
        } else if (metricName.equals(NUM_SERVLET_REQUESTS)) {
            result = (double) requestCount;
        } else if (metricName.equals(TOTAL_TIME)) {
            result = (double) processingTime;
        } else {
            // fallback
            result = Double.NaN;
        }

        return result;
    }
    
    private Double getSessionMetric(String metricName) {
        EmsConnection jmxConnection = applicationServerComponent.getEmsConnection();
        
        String servletMBeanNames = SIP_SESSION_NAME_BASE_TEMPLATE.replace("%PATH%",
        		applicationServerComponent.getContextPath(this.contextRoot));
        //FIXME : replace localhost with the real vhost
        servletMBeanNames = servletMBeanNames.replace("%HOST%", "localhost");
        ObjectNameQueryUtility queryUtility = new ObjectNameQueryUtility(servletMBeanNames);
        List<EmsBean> mBeans = jmxConnection.queryBeans(queryUtility.getTranslatedQuery());

        String property = metricName.substring(SIP_SESSION_PREFIX.length());

        Double ret = Double.NaN;

        if (mBeans.size() > 0) { // TODO flag error if != 1 ?
            EmsBean eBean = mBeans.get(0);
            eBean.refreshAttributes();
            EmsAttribute att = eBean.getAttribute(property);
            if (att != null) {
                Integer i = (Integer) att.getValue();
                ret = new Double(i);
            }

        }
        return ret;
    }
    
}
