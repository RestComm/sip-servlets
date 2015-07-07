/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.undertow;

/**
 * Exception occuring when something goes wrong with the deployment of the sip application
 *
 * @author jean.deruelle@gmail.com
 *
 * This class is based on org.mobicents.servlet.sip.catalina.SipDeploymentException from sip-servlet-as7 project, re-implemented for jboss as8 (wildfly) by:
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