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

package org.mobicents.servlet.sip.core.session;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.mobicents.servlet.sip.core.MobicentsSipServlet;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class SipRequestDispatcher implements RequestDispatcher {

	private MobicentsSipServlet handler;

	public SipRequestDispatcher(MobicentsSipServlet sipServletImpl) {
		this.handler = sipServletImpl;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public void forward(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		if(request != null) {
			((SipServletRequest)request).getSession().setHandler(handler.getName());
		} else {
			((SipServletResponse)response).getSession().setHandler(handler.getName());
		}
		Servlet servlet = handler.allocate();
		servlet.service(request, response);
		handler.deallocate(servlet);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.RequestDispatcher#include(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	public void include(ServletRequest arg0, ServletResponse arg1)
			throws ServletException, IOException {
		//This method does nothing as per JSR 289 Section 6.2.5 The RequestDispatcher Interface
		//The include method has no meaning for SIP servlets.
		throw new ServletException("The include method has no meaning for SIP servlets.");
	}

}
