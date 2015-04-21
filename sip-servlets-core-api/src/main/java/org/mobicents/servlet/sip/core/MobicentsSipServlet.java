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
	 * Retrieves the load on starup value of the sip servlet
	 * @return
	 */
	int getLoadOnStartup();
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
