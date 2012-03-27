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
package org.mobicents.servlet.sip.message;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sip.Dialog;
import javax.sip.Transaction;
import javax.sip.message.Request;

import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class Servlet3SipServletRequestImpl extends SipServletRequestImpl
		implements MobicentsSipServletRequest {

	/**
	 * 
	 */
	public Servlet3SipServletRequestImpl() {
	}

	/**
	 * @param request
	 * @param sipFactoryImpl
	 * @param sipSession
	 * @param transaction
	 * @param dialog
	 * @param createDialog
	 */
	public Servlet3SipServletRequestImpl(Request request,
			SipFactoryImpl sipFactoryImpl, MobicentsSipSession sipSession,
			Transaction transaction, Dialog dialog, boolean createDialog) {
		super(request, sipFactoryImpl, sipSession, transaction, dialog,
				createDialog);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map<String, String[]> getParameterMap() {
		// JSR 289 Section 5.6.1 Parameters :
		// For initial requests where a preloaded Route header specified the application to be invoked, the parameters are those of the SIP or SIPS URI in that Route header.
		// For initial requests where the application is invoked the parameters are those present on the request URI, 
		// if this is a SIP or a SIPS URI. For other URI schemes, the parameter set is undefined.
		// For subsequent requests in a dialog, the parameters presented to the application  are those that the application itself 
		// set on the Record-Route header for the initial request or response (see 10.4 Record-Route Parameters). 
		// These will typically be the URI parameters of the top Route header field but if the upstream SIP element is a 
		// "strict router" they may be returned in the request URI (see RFC 3261). 
		// It is the containers responsibility to recognize whether the upstream element is a strict router and determine the right parameter set accordingly.
		HashMap<String, String[]> retval = new HashMap<String, String[]>();
		if(this.getPoppedRoute() != null) {
			Iterator<String> parameterNamesIt =  this.getPoppedRoute().getURI().getParameterNames();
			while (parameterNamesIt.hasNext()) {
				String parameterName = parameterNamesIt.next();
				String [] paramsArray = {this.getPoppedRoute().getURI().getParameter(parameterName)}; //Get the parameter map value to String[]
				retval.put(parameterName, paramsArray); 
			}
		} else {
			Iterator<String> parameterNamesIt =  this.getRequestURI().getParameterNames();
			while (parameterNamesIt.hasNext()) {
				String parameterName = parameterNamesIt.next();
				String [] paramsArray = {this.getPoppedRoute().getURI().getParameter(parameterName)}; //Get the parameter map value to String[]
				retval.put(parameterName, paramsArray);
			}
		}		
		
		return retval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAsyncContext()
	 */
	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getDispatcherType()
	 */
	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServletContext()
	 */
	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isAsyncStarted()
	 */
	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isAsyncSupported()
	 */
	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#startAsync()
	 */
	@Override
	public AsyncContext startAsync() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#startAsync(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
