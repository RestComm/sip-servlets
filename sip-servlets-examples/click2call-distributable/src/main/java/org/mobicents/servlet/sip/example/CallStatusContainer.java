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

package org.mobicents.servlet.sip.example;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

public class CallStatusContainer implements Serializable {
	
	private HashSet<Call> activeCalls = new HashSet<Call> ();
	
	public Call addCall(String from, String to, String status) {
		Call call = new Call(from, to);
		call.setStatus(status);
		activeCalls.add(call);
		return call;
	}
	
	public void removeCall(String from, String to) {
		activeCalls.remove(new Call(from, to));
	}
	
	public void removeCall(Call call) {
		activeCalls.remove(call);
	}
	
	public Call getCall(String from, String to) {
		Iterator<Call> it = activeCalls.iterator();
		while(it.hasNext()) {
			Call call = it.next();
			if(call.getFrom().equals(from) && call.getTo().equals(to))
				return call;
		}
		return null;
	}
	
	public String getStatus(String from, String to) {
		Call call = getCall(from,to);
		if(call != null) {
			return call.getStatus();
		} else {
			return null;
		}
		
	}
}
