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

package org.mobicents.servlet.sip.conference.client;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Vladimir Ralev
 *
 */
public interface ConferenceServiceAsync {
    public void getParticipants(String conference, Boolean refresh, AsyncCallback<ParticipantInfo[]> callback);
    public void joinAnnouncement(String name, String conference, String url, AsyncCallback<Void> callback);
    public void joinSipPhone(String namr, String conference, String url, AsyncCallback<Void> callback);
    public void kick(String user, String conference, AsyncCallback<Void> callback);
    public void mute(String user, String conference, AsyncCallback<Void> callback);
    public void unmute(String user, String conference, AsyncCallback<Void> callback);
 }
