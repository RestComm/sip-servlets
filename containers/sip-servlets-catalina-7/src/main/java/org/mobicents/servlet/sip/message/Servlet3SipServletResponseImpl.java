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
package org.mobicents.servlet.sip.message;

import javax.sip.Dialog;
import javax.sip.Transaction;
import javax.sip.message.Response;

import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class Servlet3SipServletResponseImpl extends SipServletResponseImpl
		implements MobicentsSipServletResponse {

	/**
	 * 
	 */
	public Servlet3SipServletResponseImpl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param response
	 * @param sipFactoryImpl
	 * @param transaction
	 * @param session
	 * @param dialog
	 * @param hasBeenReceived
	 * @param isRetransmission
	 */
	public Servlet3SipServletResponseImpl(Response response,
			SipFactoryImpl sipFactoryImpl, Transaction transaction,
			MobicentsSipSession session, Dialog dialog,
			boolean hasBeenReceived, boolean isRetransmission) {
		super(response, sipFactoryImpl, transaction, session, dialog,
				hasBeenReceived, isRetransmission);
	}

}
