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
package org.mobicents.javax.servlet.sip;

/**
 * Interface Extension that adds extra features to the JSR 289 Proxy interface.</br>
 * It adds the following capabilities : 
 * 
 * <ul>
 * 		<li>
 * 			Allows for applications to set a timeout on 1xx responses as JSR 289 defines a timeout only for final responses.
 * 		</li>
 * </ul>
 * 
 * 
 * @author jean.deruelle@gmail.com
 * @since 1.3
 */
public interface ProxyExt {
	/**
	 * This is the amount of time, in seconds, the container waits for an informational response when proxying.</br>
	 * <ul>
	 * 	<li> 
	 * 		If the proxy is sequential, when the timer expires and no 1xx response nor final response has been received,
	 * 		the container CANCELs the current branch and proxies to the next element in the target set.
	 * 	</li>
	 *  <li> 
	 * 		If the proxy is parallel, then this acts as the upper limit for the entire proxy operation resulting in equivalent of invoking cancel()
	 * 		if the the proxy did not complete during this time, which means that neither an informational response nor a final response was not sent upstream. 
	 * 	</li>
	 * </ul>
	 * @param timeout new search 1xx timeout in seconds
	 * @throws IllegalArgumentException if the container cannot set the value as requested because it is too high, too low or negative
	 * @since 1.3
	 */
	public void setProxy1xxTimeout(int timeout);
	/**
	 * The current value of the overall proxy 1xx timeout value. This is measured in seconds.
	 * @return current value of proxy timeout in seconds.
	 * @since 1.3
	 */
	public int getProxy1xxTimeout();
}
