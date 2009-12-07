/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.servlet.sip.startup;

import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.HostConfig;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class SipStandardHost extends StandardHost {
	private boolean initialized=false;
	
	@Override
	public void init() {
		 if( initialized ) return;
	        initialized=true;
	        
		super.init();
		LifecycleListener[] listeners = lifecycle.findLifecycleListeners();
		for (int i = 0; i < listeners.length; i++) {
			if(listeners[i] instanceof HostConfig) {
				SipHostConfig sipHostConfig = new SipHostConfig();
				listeners[i] = sipHostConfig;
			}
		}			
	}
}
