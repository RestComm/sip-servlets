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

import javax.sip.*;
import javax.sip.message.*;
import javax.sip.header.*;
import javax.sip.address.*;

public interface AuthenticationMethod {

	/**
	 * Get the authentication scheme
	 */
	public String getScheme();

	/**
	 * Initialize the authentication method. This has to be done outside the
	 * constructor as the constructor is generic (created from the class name
	 * specified in the authentication method).
	 */
	public void initialize();

	/**
	 * Get the authentication realm.
	 */
	public String getRealm(String resource);

	/**
	 * get the authentication domain.
	 */
	public String getDomain();

	/**
	 * Get the authentication Algorithm
	 */
	public String getAlgorithm();

	/**
	 * Generate the challenge string.
	 */
	public String generateNonce();

	/**
	 * Check the response and answer true if authentication succeeds. Not all of
	 * these fields are relevant for every method - a basic scheme may simply do
	 * a username password check.
	 * 
	 * @param username
	 *            is the username and password.
	 * @param authorizationHeader
	 *            is the authorization header from the SIP request.
	 * @param requestLine
	 *            is the RequestLine from the SIP Request.
	 */
	public boolean doAuthenticate(String username,
			AuthorizationHeader authorizationHeader, Request request);

}
