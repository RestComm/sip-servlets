/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.mobicents.servlet.sip.core;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

/**
 * Represents a Sip Servlet object
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsSipServlet {
	/**
	 * Retrieves the name of the sip servlet
	 * @return
	 */
	String getName();
	/**
	 * Allocate and return an instance of the Sip Servlet
	 * @return an instance of the Sip Servlet
	 * @throws ServletException if something went wrong during the allocation
	 */
	Servlet allocate() throws ServletException;
	/**
	 * DeAllocate the given Sip Servlet
	 * @throws ServletException if something went wrong during the deallocation
	 */
	void deallocate(Servlet servlet) throws ServletException;
	/**
	 * Check whehter or not the servlet is available 
	 * @return
	 */
	boolean isUnavailable();

}
