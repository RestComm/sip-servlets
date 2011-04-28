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

package javax.servlet.sip.ar;

import java.io.Serializable;

/**
 * A class that represents the application routing region. It uses the predefined regions in the Enum SipApplicationRoutingRegionType and also allows for implementations to have additional or new regions if it is so required. This could be useful in non telephony domains where the concept of of a caller and callee is not applicable.
 * Since: 1.1
 */
public class SipApplicationRoutingRegion implements Serializable {
	private SipApplicationRoutingRegionType sipApplicationRoutingRegionType = null;
	private String label = null;
    /**
     * The NEUTRAL region contains applications that do not service a specific subscriber.
     */
    public static final javax.servlet.sip.ar.SipApplicationRoutingRegion NEUTRAL_REGION = new SipApplicationRoutingRegion("NEUTRAL", SipApplicationRoutingRegionType.NEUTRAL);

    /**
     * The ORIGINATING region contains applications that service the caller.
     */
    public static final javax.servlet.sip.ar.SipApplicationRoutingRegion ORIGINATING_REGION= new SipApplicationRoutingRegion("ORIGINATING", SipApplicationRoutingRegionType.ORIGINATING);

    /**
     * The TERMINATING region contains applications that service the callee.
     */
    public static final javax.servlet.sip.ar.SipApplicationRoutingRegion TERMINATING_REGION=new SipApplicationRoutingRegion("TERMINATING", SipApplicationRoutingRegionType.TERMINATING);

    /**
     * Deployer may define new routing region by constructing a new SipApplicationRoutingRegion object. The SipApplicationRoutingRegionType may be null in cases when a custom region is defined.
     */
    public SipApplicationRoutingRegion(java.lang.String label, javax.servlet.sip.ar.SipApplicationRoutingRegionType type){
         this.label = label;
         this.sipApplicationRoutingRegionType = type;
    }

    /**
     * Each routing region has a String label.
     */
    public java.lang.String getLabel(){
        return label;
    }

    /**
     * Each routing region is either ORIGINATING, TERMINATING, or NEUTRAL type.
     */
    public final javax.servlet.sip.ar.SipApplicationRoutingRegionType getType(){
        return sipApplicationRoutingRegionType;
    }

    /**
     * {@inheritDoc}
     */
    public java.lang.String toString(){
        return label; 
    }

}
