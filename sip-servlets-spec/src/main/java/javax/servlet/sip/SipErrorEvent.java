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

package javax.servlet.sip;
/**
 * Events of this type are sent to objects implementing the SipErrorListener 
 * interface when an error occurs which is related to the applications processing 
 * of a SIP transaction.
 */
public class SipErrorEvent extends java.util.EventObject{
	private SipServletRequest sipServletRequest = null;
	private SipServletResponse sipServletResponse = null;
    /**
     * Constructs a new SipErrorEvent.
     * @param request the request the error relates to
     * @param response - the response the error relates to
     */
    public SipErrorEvent(javax.servlet.sip.SipServletRequest request, javax.servlet.sip.SipServletResponse response){
    	super(request);
        this.sipServletRequest = request;
        this.sipServletResponse = response;
    }

    /**
     * Returns the request object associated with this SipErrorEvent.
     */
    public javax.servlet.sip.SipServletRequest getRequest(){
        return sipServletRequest; 
    }

    /**
     * Returns the response object associated with this SipErrorEvent.
     */
    public javax.servlet.sip.SipServletResponse getResponse(){
        return sipServletResponse; 
    }

}
