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
package org.jboss.web.tomcat.service.session;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.catalina.Manager;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class ConvergedSessionInvalidationTracker {
	private static final ThreadLocal<Map<Manager, String>> invalidatedSessions = new ThreadLocal<Map<Manager, String>>();
	private static final ThreadLocal<Map<Manager, SipSessionKey>> invalidatedSipSessions = new ThreadLocal<Map<Manager, SipSessionKey>>();
	private static final ThreadLocal<Map<Manager, SipApplicationSessionKey>> invalidatedSipApplicationSessions = new ThreadLocal<Map<Manager, SipApplicationSessionKey>>();
	private static final ThreadLocal<Boolean> suspended = new ThreadLocal<Boolean>();

	public static void suspend() {
		suspended.set(Boolean.TRUE);
	}

	public static void resume() {
		suspended.set(null);
	}

	public static void sessionInvalidated(String id, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, String> map = invalidatedSessions.get();
			if (map == null) {
				map = new WeakHashMap<Manager, String>(2);
				invalidatedSessions.set(map);
			}
			map.put(manager, id);
		}
	}
	
	public static void sipSessionInvalidated(SipSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, SipSessionKey> map = invalidatedSipSessions.get();
			if (map == null) {
				map = new WeakHashMap<Manager, SipSessionKey>(2);
				invalidatedSipSessions.set(map);
			}
			map.put(manager, key);
		}
	}
	
	public static void sipApplicationSessionInvalidated(SipApplicationSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, SipApplicationSessionKey> map = invalidatedSipApplicationSessions.get();
			if (map == null) {
				map = new WeakHashMap<Manager, SipApplicationSessionKey>(2);
				invalidatedSipApplicationSessions.set(map);
			}
			map.put(manager, key);
		}
	}

	public static void clearInvalidatedSession(String id, Manager manager) {
		Map<Manager, String> map = invalidatedSessions.get();
		if (map != null) {
			map.remove(manager);
		}
	}

	public static boolean isSessionInvalidated(String id, Manager manager) {
		boolean result = false;
		Map<Manager, String> map = invalidatedSessions.get();
		if (map != null) {
			result = id.equals(map.get(manager));
		}
		return result;
	}
}
