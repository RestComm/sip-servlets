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

import org.apache.catalina.Manager;
import org.mobicents.servlet.sip.core.SipManager;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public interface CatalinaSipManager extends SipManager, Manager {
	/**
	 * Stop the Context GraceFully, ie the context will stop only when  there is no outstanding SIP or HTTP Sessions
	 * @param timeToWait - the context will wait for the time specified in this parameter before forcefully killing
	 * the remaining sessions (HTTP and SIP) for each application deployed, if a negative value is provided the context 
	 * will wait until there is no remaining Session before shutting down
	 */
	void stopGracefully(long timeToWait);
	boolean isStoppingGracefully();
	
}
