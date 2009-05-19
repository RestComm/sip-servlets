/*
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

import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipApplicationSession;

import org.mobicents.servlet.sip.message.MobicentsSipApplicationSessionFacade;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public interface MobicentsSipApplicationSession extends SipApplicationSession {
	public static final String SIP_APPLICATION_KEY_PARAM_NAME = "org.mobicents.servlet.sip.ApplicationSessionKey";
	
	void addHttpSession(HttpSession httpSession);
	
	boolean removeHttpSession(HttpSession httpSession);
	
	HttpSession findHttpSession(String sessionId);

	SipContext getSipContext();

	void onSipSessionReadyToInvalidate(MobicentsSipSession mobicentsSipSession);

	void addSipSession(MobicentsSipSession mobicentsSipSession);

	SipApplicationSessionKey getKey();

	void access();	

	boolean hasTimerListener();

	void addServletTimer(ServletTimer servletTimer);
	
	void removeServletTimer(ServletTimer servletTimer);

	void notifySipApplicationSessionListeners(SipApplicationSessionEventType expiration);

	boolean isExpired();
	
	String getCurrentRequestHandler();

	void setCurrentRequestHandler(String currentRequestHandler);
	
	void tryToInvalidate();
	
	Semaphore getSemaphore();
	
	MobicentsSipApplicationSessionFacade getSession();
	
	String getJvmRoute();
	void setJvmRoute(String jvmRoute);

}
