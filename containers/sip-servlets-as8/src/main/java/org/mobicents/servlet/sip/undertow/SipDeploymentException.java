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

package org.mobicents.servlet.sip.undertow;

/**
 * Exception occuring when something goes wrong with the deployment of the sip application
 *
 * @author jean.deruelle@gmail.com
 *
 *         This class is based on org.mobicents.servlet.sip.catalina.SipDeploymentException from sip-servlet-as7 project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipDeploymentException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public SipDeploymentException() {
        super();
    }

    /**
     * @param message
     */
    public SipDeploymentException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public SipDeploymentException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public SipDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

}
