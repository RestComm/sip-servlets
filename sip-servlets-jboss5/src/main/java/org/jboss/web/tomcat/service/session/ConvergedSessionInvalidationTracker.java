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

package org.jboss.web.tomcat.service.session;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.catalina.Manager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class ConvergedSessionInvalidationTracker {
	private static final ThreadLocal<Map<Manager, String>> invalidatedSessions = new ThreadLocal<Map<Manager, String>>();
	private static final ThreadLocal<Map<Manager, MobicentsSipSessionKey>> invalidatedSipSessions = new ThreadLocal<Map<Manager, MobicentsSipSessionKey>>();
	private static final ThreadLocal<Map<Manager, MobicentsSipApplicationSessionKey>> invalidatedSipApplicationSessions = new ThreadLocal<Map<Manager, MobicentsSipApplicationSessionKey>>();
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
	
	public static void sipSessionInvalidated(MobicentsSipSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, MobicentsSipSessionKey> map = invalidatedSipSessions.get();
			if (map == null) {
				map = new WeakHashMap<Manager, MobicentsSipSessionKey>(2);
				invalidatedSipSessions.set(map);
			}
			map.put(manager, key);
		}
	}
	
	public static void sipApplicationSessionInvalidated(MobicentsSipApplicationSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, MobicentsSipApplicationSessionKey> map = invalidatedSipApplicationSessions.get();
			if (map == null) {
				map = new WeakHashMap<Manager, MobicentsSipApplicationSessionKey>(2);
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
	
	public static boolean isSipSessionInvalidated(MobicentsSipSessionKey key, Manager manager) {
		boolean result = false;
		Map<Manager, MobicentsSipSessionKey> map = invalidatedSipSessions.get();
		if (map != null) {
			result = key.equals(map.get(manager));
		}
		return result;
	}
	
	public static boolean isSipApplicationSessionInvalidated(MobicentsSipApplicationSessionKey key, Manager manager) {
		boolean result = false;
		Map<Manager, MobicentsSipApplicationSessionKey> map = invalidatedSipApplicationSessions.get();
		if (map != null) {
			result = key.equals(map.get(manager));
		}
		return result;
	}
}
