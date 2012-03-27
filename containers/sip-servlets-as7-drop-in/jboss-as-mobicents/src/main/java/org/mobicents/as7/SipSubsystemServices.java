/**
 *
 */
package org.mobicents.as7;

import org.jboss.msc.service.ServiceName;

/**
 * Service name constants.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 * @author Emanuel Muckenhuber
 */
public final class SipSubsystemServices {

    /** The base name for jboss.sip services. */
    public static final ServiceName JBOSS_SIP = ServiceName.JBOSS.append("sip");
    /** The jboss.sip server name, there can only be one. */
    public static final ServiceName JBOSS_SIP_SERVER = JBOSS_SIP.append("server");
    /** The base name for jboss.sip connector services. */
    public static final ServiceName JBOSS_SIP_CONNECTOR = JBOSS_SIP.append("connector");
    /** The base name for jboss.sip host services. */
    public static final ServiceName JBOSS_SIP_HOST = JBOSS_SIP.append("host");
    /** The base name for jboss.sip deployments. */
    static final ServiceName JBOSS_SIP_DEPLOYMENT_BASE = JBOSS_SIP.append("deployment");

    public static ServiceName deploymentServiceName(final String virtualHost, final String contextPath) {
        return JBOSS_SIP_DEPLOYMENT_BASE.append(virtualHost).append("".equals(contextPath) ? "/" : contextPath);
    }

    private SipSubsystemServices() {
    }
}
