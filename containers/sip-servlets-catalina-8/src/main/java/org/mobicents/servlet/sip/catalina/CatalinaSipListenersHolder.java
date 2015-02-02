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
package org.mobicents.servlet.sip.catalina;

import java.util.EventListener;

import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.catalina.annotations.SipInstanceManager;
import org.mobicents.servlet.sip.core.MobicentsSipServlet;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class CatalinaSipListenersHolder extends SipListenersHolder {
	private static final Logger logger = Logger.getLogger(CatalinaSipListenersHolder.class);
	
	public CatalinaSipListenersHolder(SipContext sipContext) {
		super(sipContext);
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.core.session.SipListenersHolder#loadListeners(java.lang.String[], java.lang.ClassLoader)
	 */
	@Override
	public boolean loadListeners(String[] listeners, ClassLoader classLoader) {
		// Instantiate all the listeners
		for (String className : listeners) {			
			try {
				Class listenerClass = Class.forName(className, false, classLoader);
				EventListener listener = (EventListener) listenerClass.newInstance();
				
				SipInstanceManager sipInstanceManager = ((CatalinaSipContext)sipContext).getSipInstanceManager();
				sipInstanceManager.processAnnotations(listener, sipInstanceManager.getInjectionMap(listenerClass.getName()));
				
				MobicentsSipServlet sipServletImpl = (MobicentsSipServlet)sipContext.findSipServletByClassName(className);
				if(sipServletImpl != null) {
					listener = (EventListener) sipServletImpl.allocate();
					listenerServlets.put(listener, sipServletImpl);
				} else {
					SipServlet servlet = (SipServlet) listenerClass.getAnnotation(SipServlet.class);
					if (servlet != null) {						
						sipServletImpl = (MobicentsSipServlet)sipContext.findSipServletByName(servlet.name());
						if(sipServletImpl != null) {
							listener = (EventListener) sipServletImpl.allocate();
							listenerServlets.put(listener, sipServletImpl);
						}
					}					
				}				             
				addListenerToBunch(listener);				
			} catch (Exception e) {
				logger.fatal("Cannot instantiate listener class " + className,
						e);
				return false;
			}
		}
		return true;
	}

}
