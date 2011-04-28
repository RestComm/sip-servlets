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

package org.mobicents.servlet.sip.testsuite;

/**
 * Get this interface from the nist-sip IM
 * @author  olivier deruelle
 */
public interface ClientAuthenticationMethod {
    
    /**
     * Initialize the Client authentication method. This has to be
     * done outside the constructor.
     * @throws Exception if the parameters are not correct.
     */
    public void initialize(String realm,String userName,String uri,String nonce
    ,String password,String method,String cnonce,String algorithm) throws Exception;
    
    
    /**
     * generate the response
     * @returns null if the parameters given in the initialization are not
     * correct.
     */
    public String generateResponse();
    
}
