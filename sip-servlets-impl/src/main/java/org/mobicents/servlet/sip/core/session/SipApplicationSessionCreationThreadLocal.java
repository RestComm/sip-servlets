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

package org.mobicents.servlet.sip.core.session;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.mobicents.servlet.sip.core.SipContext;

/**
 * Information related to the application sessions created in the context of a thread when it is passed to the application
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipApplicationSessionCreationThreadLocal {
	Set<MobicentsSipApplicationSession> sipApplicationSessions = new CopyOnWriteArraySet<MobicentsSipApplicationSession>();
	
	public Set<MobicentsSipApplicationSession> getSipApplicationSessions() {
		return sipApplicationSessions;
	}
        
        private static ThreadLocal<SipApplicationSessionCreationThreadLocal> sessionsTH = new ThreadLocal();
        
        public static ThreadLocal<SipApplicationSessionCreationThreadLocal> getTHRef() {
            return sessionsTH;
        }
        
        public static SipContext lookupContext() {
            SipContext ctx = null;
            SipApplicationSessionCreationThreadLocal get = sessionsTH.get();
            if (get != null) {
                Iterator<MobicentsSipApplicationSession> iterator = get.getSipApplicationSessions().iterator();
                if (iterator.hasNext()) {
                    ctx = iterator.next().getSipContext();
                }
            }
            return ctx;
        }
}
