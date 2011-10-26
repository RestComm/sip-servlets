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

import java.util.List;

import javax.servlet.ServletContextListener;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.TimerListener;

import org.mobicents.javax.servlet.ContainerListener;
import org.mobicents.javax.servlet.sip.ProxyBranchListener;
import org.mobicents.servlet.sip.listener.SipConnectorListener;

/**
 * Holds the listeners for a given {@link SipContext}
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface SipListeners {

	List<SipApplicationSessionListener> getSipApplicationSessionListeners();
	List<SipApplicationSessionAttributeListener> getSipApplicationSessionAttributeListeners();
	List<SipSessionAttributeListener> getSipSessionAttributeListeners();
	List<SipSessionListener> getSipSessionListeners();
	List<SipErrorListener> getSipErrorListeners();
	List<ProxyBranchListener> getProxyBranchListeners();
	TimerListener getTimerListener();
	List<ServletContextListener> getServletContextListeners();
	boolean loadListeners(String[] findSipApplicationListeners,
			ClassLoader loader);
	void clean();
	void deallocateServletsActingAsListeners();
	List<SipServletListener> getSipServletsListeners();
	List<SipConnectorListener> getSipConnectorListeners();
	ContainerListener getContainerListener();

}
