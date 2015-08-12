/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.as8;

import org.jboss.msc.service.ServiceName;

/**
 * Service name constants.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 * @author Emanuel Muckenhuber
 *
 *         This class is based on the contents of org.mobicents.as7 package from jboss-as7-mobicents project, re-implemented for
 *         jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public final class SipSubsystemServices {

    /** The base name for jboss.sip services. */
    public static final ServiceName JBOSS_SIP = ServiceName.JBOSS.append("sip");
    /** The jboss.sip server name, there can only be one. */
    public static final ServiceName JBOSS_SIP_SERVER = JBOSS_SIP.append("server");
    /** The base name for jboss.sip connector services. */
    public static final ServiceName JBOSS_SIP_CONNECTOR = JBOSS_SIP.append("connector");
    /** The base name for jboss.sip deployments. */
    static final ServiceName JBOSS_SIP_DEPLOYMENT_BASE = JBOSS_SIP.append("deployment");

    public static ServiceName deploymentServiceName(final String appName) {
        return JBOSS_SIP_DEPLOYMENT_BASE.append("".equals(appName) ? "/" : appName);
    }

    private SipSubsystemServices() {
    }
}
