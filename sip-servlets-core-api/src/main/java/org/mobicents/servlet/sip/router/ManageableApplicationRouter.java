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

package org.mobicents.servlet.sip.router;

import java.util.List;
import java.util.Map;

import javax.servlet.sip.ar.SipApplicationRouterInfo;


/**
 * This interface simply exposes methods to reconfigure an Application Router
 * dynamically.
 *
 */
public interface ManageableApplicationRouter {
	
	/**
	 * This method will completely reconfigure the application router and
	 * clean the previous state accumulated in the AR.
	 * 
	 * @param configuration How the configuration variable will be
	 * interpreted is AR specific. For most cases a String should work
	 * just fine (dar file contents, xml files content, rule files, etc)
	 */
	void configure(Object configuration);
	
	/**
	 * This method will provide the configuration that is currently active
	 * in the AR. It can be parsed and visualized by the management application.
	 * 
	 * @return the current configuration (possibly a string)
	 */
	Object getCurrentConfiguration();
	
	/**
	 * This method will provide the configuration that is currently active
	 * in the AR. It can be parsed and visualized by the management application.
	 * 
	 * @return the current configuration (possibly a string)
	 */
	Map<String, List<? extends SipApplicationRouterInfo>> getConfiguration();
}
