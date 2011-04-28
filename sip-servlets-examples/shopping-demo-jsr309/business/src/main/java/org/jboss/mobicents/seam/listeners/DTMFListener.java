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

package org.jboss.mobicents.seam.listeners;
import javax.media.mscontrol.MediaEventListener;
import javax.media.mscontrol.MsControlException;
import javax.media.mscontrol.mediagroup.MediaGroup;
import javax.media.mscontrol.mediagroup.signals.SignalDetectorEvent;
import javax.servlet.sip.SipSession;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.util.DTMFUtils;

public class DTMFListener implements MediaEventListener<SignalDetectorEvent>{
	public static final int DTMF_SESSION_STARTED = 1;
	public static final int DTMF_SESSION_STOPPED = 2;
	
	private static Logger logger = Logger.getLogger(DTMFListener.class);
	
	MediaGroup mg;
	SipSession session;
	private String pathToAudioDirectory;

	public DTMFListener(MediaGroup mg, SipSession session, String pathToAudioDirectory) {
		this.mg = mg;
		this.pathToAudioDirectory = pathToAudioDirectory;
		this.session = session;
	}


	public void onEvent(SignalDetectorEvent arg0) {
		String signal = arg0.getSignalString();
		if(session.getAttribute("orderApproval") != null) {
			if(session.getAttribute("adminApproval") != null) {
				logger.info("admin approval in progress.");
				DTMFUtils.adminApproval(session, signal, pathToAudioDirectory);
			} else {
				logger.info("customer approval in progress.");
				DTMFUtils.orderApproval(session, signal, pathToAudioDirectory);
			}
		} else if(session.getAttribute("deliveryDate") != null) {
			logger.info("delivery date update in progress.");
			if(!DTMFUtils.updateDeliveryDate(session, signal)) {				
				try {
					mg.getSignalDetector().receiveSignals(1, null, null, null);
				} catch (MsControlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
	}
}
