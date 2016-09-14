/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.web.session.sip;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.catalina.Manager;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionKey;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;

/**
 * @author jean.deruelle@gmail.com
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class ConvergedSessionInvalidationTracker {
	
	private static final Object MAP_VALUE = new Object();
	
	private static final ThreadLocal<Map<Manager, Map<String,Object>>> invalidatedSessions = new ThreadLocal<Map<Manager, Map<String,Object>>>();
	private static final ThreadLocal<Map<Manager, Map<MobicentsSipSessionKey,Object>>> invalidatedSipSessions = new ThreadLocal<Map<Manager, Map<MobicentsSipSessionKey,Object>>>();
	private static final ThreadLocal<Map<Manager, Map<MobicentsSipApplicationSessionKey,Object>>> invalidatedSipApplicationSessions = new ThreadLocal<Map<Manager,Map<MobicentsSipApplicationSessionKey,Object>>>();
	private static final ThreadLocal<Boolean> suspended = new ThreadLocal<Boolean>();

	public static void suspend() {
		suspended.set(Boolean.TRUE);
	}

	public static void resume() {
		suspended.set(null);
	}

	public static void sessionInvalidated(String id, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, Map<String,Object>> map = invalidatedSessions.get();
			if (map == null) {
				map = new HashMap<Manager, Map<String,Object>>(2);						
				invalidatedSessions.set(map);
			}
			Map<String,Object> managerMap = map.get(manager);
			if(managerMap == null) {
				managerMap = new WeakHashMap<String,Object>();
				map.put(manager, managerMap);
			}
			managerMap.put(id, MAP_VALUE);
		}
	}
	
	public static void sipSessionInvalidated(MobicentsSipSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, Map<MobicentsSipSessionKey,Object>> map = invalidatedSipSessions.get();
			if (map == null) {
				map = new HashMap<Manager, Map<MobicentsSipSessionKey,Object>>(2);						
				invalidatedSipSessions.set(map);
			}
			Map<MobicentsSipSessionKey,Object> managerMap = map.get(manager);
			if(managerMap == null) {
				managerMap = new WeakHashMap<MobicentsSipSessionKey,Object>();
				map.put(manager, managerMap);
			}
			managerMap.put(key, MAP_VALUE);
		}
	}
	
	public static void sipApplicationSessionInvalidated(MobicentsSipApplicationSessionKey key, Manager manager) {
		if (Boolean.TRUE != suspended.get()) {
			Map<Manager, Map<MobicentsSipApplicationSessionKey,Object>> map = invalidatedSipApplicationSessions.get();
			if (map == null) {
				map = new HashMap<Manager, Map<MobicentsSipApplicationSessionKey,Object>>(2);						
				invalidatedSipApplicationSessions.set(map);
			}
			Map<MobicentsSipApplicationSessionKey,Object> managerMap = map.get(manager);
			if(managerMap == null) {
				managerMap = new WeakHashMap<MobicentsSipApplicationSessionKey,Object>();
				map.put(manager, managerMap);
			}
			managerMap.put(key, MAP_VALUE);
		}
	}

	public static void clearInvalidatedSession(String id, Manager manager) {
		Map<Manager, Map<String,Object>> map = invalidatedSessions.get();
		if (map != null) {
			Map<String,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				managerMap.remove(id);
			}
		}		
	}

	public static boolean isSessionInvalidated(String id, Manager manager) {
		boolean result = false;
		Map<Manager, Map<String,Object>> map = invalidatedSessions.get();
		if (map != null) {
			Map<String,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				result = managerMap.containsKey(id);
			}
		}		
		return result;
	}
	
	public static boolean isSipSessionInvalidated(MobicentsSipSessionKey key, Manager manager) {
		boolean result = false;
		Map<Manager, Map<MobicentsSipSessionKey,Object>> map = invalidatedSipSessions.get();
		if (map != null) {
			Map<MobicentsSipSessionKey,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				result = managerMap.containsKey(key);
			}
		}		
		return result;
	}
	
	public static boolean isSipApplicationSessionInvalidated(MobicentsSipApplicationSessionKey key, Manager manager) {
		boolean result = false;
		Map<Manager, Map<MobicentsSipApplicationSessionKey,Object>> map = invalidatedSipApplicationSessions.get();
		if (map != null) {
			Map<MobicentsSipApplicationSessionKey,Object> managerMap = map.get(manager);
			if (managerMap != null) {
				result = managerMap.containsKey(key);
			}
		}		
		return result;
	}
}
