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
package org.mobicents.servlet.sip.message;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionEventType;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * The purpose of this class is to be a facade to the real sip application session as well as a 
 * serializable session class that can be put as a session attribute in other sessions or even its own session. 
 * Basically instead of replicating the whole attribute map, we will replicate the id, then on the remote side we will
 * read the ID and look it up in the remote session manager.
 * 
 * @author vralev
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsSipApplicationSessionFacade implements
		MobicentsSipApplicationSession, Externalizable {

	private static final long serialVersionUID = 1L;
	private static transient Logger logger = Logger.getLogger(MobicentsSipApplicationSessionFacade.class);
	private MobicentsSipApplicationSession sipApplicationSession;

	public MobicentsSipApplicationSessionFacade() {
	}
	
	public MobicentsSipApplicationSession getMobicentstSipApplicationSession() {
		return this.sipApplicationSession;
	}

	public MobicentsSipApplicationSessionFacade(
			MobicentsSipApplicationSession sipApplicationSession) {
		this.sipApplicationSession = sipApplicationSession;
	}

	public void access() {
		this.sipApplicationSession.access();
	}

	public void addHttpSession(HttpSession httpSession) {
		this.sipApplicationSession.addHttpSession(httpSession);
	}

	public void addServletTimer(ServletTimer servletTimer) {
		this.sipApplicationSession.addServletTimer(servletTimer);
	}

	public void addSipSession(MobicentsSipSession mobicentsSipSession) {
		this.sipApplicationSession.addSipSession(mobicentsSipSession);
	}

	public HttpSession findHttpSession(String id) {
		return this.sipApplicationSession.findHttpSession(id);
	}

	public String getCurrentRequestHandler() {
		return this.sipApplicationSession.getCurrentRequestHandler();
	}

	public SipApplicationSessionKey getKey() {
		return this.sipApplicationSession.getKey();
	}

	public Semaphore getSemaphore() {
		return this.sipApplicationSession.getSemaphore();
	}

	public SipContext getSipContext() {
		return this.sipApplicationSession.getSipContext();
	}

	public boolean hasTimerListener() {
		return this.sipApplicationSession.hasTimerListener();
	}

	public boolean isExpired() {
		return this.sipApplicationSession.isExpired();
	}

	public void notifySipApplicationSessionListeners(
			SipApplicationSessionEventType expiration) {
		this.sipApplicationSession
				.notifySipApplicationSessionListeners(expiration);
	}

	public void onSipSessionReadyToInvalidate(
			MobicentsSipSession mobicentsSipSession) {
		this.sipApplicationSession
				.onSipSessionReadyToInvalidate(mobicentsSipSession);
	}

	public boolean removeHttpSession(HttpSession httpSession) {
		return this.sipApplicationSession.removeHttpSession(httpSession);
	}

	public void removeServletTimer(ServletTimer servletTimer) {
		this.sipApplicationSession.removeServletTimer(servletTimer);
	}

	public void setCurrentRequestHandler(String currentRequestHandler) {
		this.sipApplicationSession
				.setCurrentRequestHandler(currentRequestHandler);
	}

	public void tryToInvalidate() {
		this.sipApplicationSession.tryToInvalidate();
	}

	public void encodeURI(URI uri) {
		this.sipApplicationSession.encodeURI(uri);
	}

	public URL encodeURL(URL url) {
		return this.sipApplicationSession.encodeURL(url);
	}

	public String getApplicationName() {
		return this.sipApplicationSession.getApplicationName();
	}

	public Object getAttribute(String name) {
		return this.sipApplicationSession.getAttribute(name);
	}

	public Iterator<String> getAttributeNames() {
		return this.sipApplicationSession.getAttributeNames();
	}

	public long getCreationTime() {
		return this.sipApplicationSession.getCreationTime();
	}

	public long getExpirationTime() {
		return this.sipApplicationSession.getExpirationTime();
	}

	public String getId() {
		return this.sipApplicationSession.getId();
	}

	public boolean getInvalidateWhenReady() {
		return this.sipApplicationSession.getInvalidateWhenReady();
	}

	public long getLastAccessedTime() {
		return this.sipApplicationSession.getLastAccessedTime();
	}

	public Object getSession(String id, Protocol protocol) {
		return this.sipApplicationSession.getSession(id, protocol);
	}

	public Iterator<?> getSessions() {
		return this.sipApplicationSession.getSessions();
	}

	public Iterator<?> getSessions(String protocol) {
		return this.sipApplicationSession.getSessions(protocol);
	}

	public SipSession getSipSession(String id) {
		return this.sipApplicationSession.getSipSession(id);
	}

	public ServletTimer getTimer(String id) {
		return this.sipApplicationSession.getTimer(id);
	}

	public Collection<ServletTimer> getTimers() {
		return this.sipApplicationSession.getTimers();
	}

	public void invalidate() {
		this.sipApplicationSession.invalidate();
	}

	public boolean isReadyToInvalidate() {
		return this.sipApplicationSession.isReadyToInvalidate();
	}

	public boolean isValid() {
		return this.sipApplicationSession.isValid();
	}

	public void removeAttribute(String name) {
		this.sipApplicationSession.removeAttribute(name);
	}

	public void setAttribute(String name, Object attribute) {
		this.sipApplicationSession.setAttribute(name, attribute);
	}

	public int setExpires(int deltaMinutes) {
		return this.sipApplicationSession.setExpires(deltaMinutes);
	}

	public void setInvalidateWhenReady(boolean invalidateWhenReady) {
		this.sipApplicationSession.setInvalidateWhenReady(invalidateWhenReady);
	}

	public MobicentsSipApplicationSessionFacade getSession() {
		return sipApplicationSession.getSession();
	}

	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException {
		String sipApplicationSessionId = arg0.readUTF();
		SipApplicationSessionKey key = null;
		try {
			key = SessionManagerUtil.parseSipApplicationSessionKey(sipApplicationSessionId);
		} catch (ParseException e) {
			logger.error("Couldn't parse the following sip application session key " + sipApplicationSessionId, e);
			throw new RuntimeException(e);
		}
		SipContext sipContext = StaticServiceHolder.sipStandardService
				.getSipApplicationDispatcher().findSipApplication(key.getApplicationName());
		
		this.sipApplicationSession = ((SipManager) sipContext.getManager()).getSipApplicationSession(
				key, false);
	}

	public void writeExternal(ObjectOutput arg0) throws IOException {
		arg0.writeUTF(sipApplicationSession.getId());
	}

	public String getJvmRoute() {
		return this.sipApplicationSession.getJvmRoute();
	}

	public void setJvmRoute(String jvmRoute) {
		this.sipApplicationSession.setJvmRoute(jvmRoute);
	}

	@Override
	public boolean equals(Object obj) {
		return this.sipApplicationSession.equals(obj);
	}

	@Override
	public int hashCode() {
		return this.sipApplicationSession.hashCode();
	}

	@Override
	public String toString() {
		return this.sipApplicationSession.toString();
	}

}
