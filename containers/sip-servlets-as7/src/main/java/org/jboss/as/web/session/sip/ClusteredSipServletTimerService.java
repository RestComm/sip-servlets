package org.jboss.as.web.session.sip;

import javax.servlet.sip.ServletTimer;

import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.timers.SipServletTimerService;

/**
 * @author jean.deruelle@gmail.com
 * @author andras.kokuti@ext.alerant.hu
 *
 */
public interface ClusteredSipServletTimerService extends SipServletTimerService {

	ServletTimer rescheduleTimerLocally(MobicentsSipApplicationSession sipApplicationSession, String timerId);
	void cancel(String id);
} 
