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
 * Causes applications to be notified of various error conditions occurring during regular SIP transaction processing.
 */
public interface SipErrorListener extends java.util.EventListener{
    /**
     * Invoked by the servlet container to notify an application that no ACK was received for an INVITE transaction for which a final response has been sent upstream.
     * This method is invoked for UAS applications only and not for applications that proxied the INVITE.
     */
    void noAckReceived(javax.servlet.sip.SipErrorEvent ee);

    /**
     * Invoked by the servlet container for applications acting as a UAS when no PRACK was received for a previously sent reliable provisional response. It is then up to the application to generate the 5xx response reccommended by RFC 3262 for the INVITE transaction. The original INVITE request as well as the unacknowledged reliable response is available from the SipErrorEvent argument.
     */
    void noPrackReceived(javax.servlet.sip.SipErrorEvent ee);

}
