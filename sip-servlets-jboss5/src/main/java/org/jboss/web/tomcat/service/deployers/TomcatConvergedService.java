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
package org.jboss.web.tomcat.service.deployers;

import javax.management.ObjectName;

/**
 * A Converged Service extending JBoss 5 TomcatService to start the sip application dispatcher after the connectors have been started 
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class TomcatConvergedService extends TomcatService {
	
	@Override
	public void startConnectors() throws Exception {
		super.startConnectors();
		Object[] args = {};
	    String[] sig = {};
		// start the sip application disptacher after the connectors have been started
		// so that servlets can act as UAC in servletInitialized callback
		ObjectName sipApplicationDispatcher = new ObjectName(getTomcatDeployer().getDomain() + ":type=SipApplicationDispatcher");      
		server.invoke(sipApplicationDispatcher,"start", args, sig);      
	}
	
	@Override
	protected void stopService() throws Exception {
		Object[] args = {};
	    String[] sig = {};
		// stop the sip application disptacher 
		ObjectName sipApplicationDispatcher = new ObjectName(getTomcatDeployer().getDomain() + ":type=SipApplicationDispatcher");      
		server.invoke(sipApplicationDispatcher,"stop", args, sig);
	
		super.stopService();		
	}
}
