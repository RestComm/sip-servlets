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

import org.jboss.as.server.deployment.AttachmentKey;
import org.mobicents.servlet.sip.undertow.SipStandardService;

/**
 * The web server.
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.as7 package from jboss-as7-mobicents project, re-implemented for
 *         jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public interface SipServer {

    AttachmentKey<SipServer> ATTACHMENT_KEY = AttachmentKey.create(SipServer.class);

    /**
     * Add a connector.
     *
     * @param connector the connector
     */
    void addConnector(SipConnectorListener connector);

    /**
     * Remove connector.
     *
     * @param connector the connector
     */
    void removeConnector(SipConnectorListener connector);

    /**
     * Add a virtual host.
     *
     * @param host the virtual host
     */
    // FIXME: void addHost(Host host);

    /**
     * Remove a virtual host.
     *
     * @param host the virtual host
     */
    // FIXME: void removeHost(Host host);

    /**
     * return the server (StandardServer)
     */
    // FIXME: Server getServer();

    /**
     * return the service (StandardService)
     */
    SipStandardService getService();

}
