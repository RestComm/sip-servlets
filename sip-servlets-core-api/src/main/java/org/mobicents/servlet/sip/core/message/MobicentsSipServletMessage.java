/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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

package org.mobicents.servlet.sip.core.message;

import javax.servlet.sip.SipServletMessage;
import javax.sip.Transaction;
import javax.sip.message.Message;

import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;

/**
 * Extension to the Sip Servlet Message interface from JSR 289
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public interface MobicentsSipServletMessage extends SipServletMessage {
	MobicentsSipSession getSipSession();
	void setSipSession(MobicentsSipSession sipSession);
	
	Transaction getTransaction();
	void setTransaction(Transaction transaction);
	
	MobicentsTransactionApplicationData getTransactionApplicationData();
	
	Message getMessage();

	// https://code.google.com/p/sipservlets/issues/detail?id=21
	boolean isMessageSent();
	
	MobicentsSipApplicationSession getSipApplicationSession(boolean create);
}
