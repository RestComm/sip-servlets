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

package org.mobicents.servlet.sip.core.timers;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * @author jean.deruelle@gmail.com
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class StandardSipApplicationSessionTimerService extends
        Timer implements
        SipApplicationSessionTimerService {

    private static final Logger logger = Logger.getLogger(StandardSipApplicationSessionTimerService.class
            .getName());

    // Counts the number of cancelled tasks
    private static volatile int numCancelled = 0;

    private AtomicBoolean started = new AtomicBoolean(false);
    /**
     * @param corePoolSize
     */
    public StandardSipApplicationSessionTimerService(String applicationName) {
        super(applicationName + "_sip_standard_sas_timer_service");
        schedulePurgeTaskIfNeeded();
    }

    private void schedulePurgeTaskIfNeeded() {
        int purgePeriod = StaticServiceHolder.sipStandardService.getCanceledTimerTasksPurgePeriod();
        if(purgePeriod > 0) {
            TimerTask t = new TimerTask() {

                @Override
                public void run() {
                    try {
                        if(logger.isDebugEnabled()) {
                            logger.debug("Purging canceled timer tasks...");
                        }
                        purge();
                        if(logger.isDebugEnabled()) {
                            logger.debug("Purging canceled timer tasks completed.");
                        }
                    }
                    catch (Exception e) {
                        logger.error("failed to execute purge",e);
                    }
                }
            };
            //purgePeriod originally set in minutes, but needed in millis:
            purgePeriod = purgePeriod * 60 * 1000;

            super.scheduleAtFixedRate(t, purgePeriod, purgePeriod);
        }
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerFactory#createSipApplicationSessionTimerTask()
     */
    public SipApplicationSessionTimerTask createSipApplicationSessionTimerTask(MobicentsSipApplicationSession sipApplicationSession) {
        return new StandardSasTimerTask(sipApplicationSession);
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#cancel(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask)
     */
    public boolean cancel(SipApplicationSessionTimerTask expirationTimerTask) {
        boolean cancelled = ((StandardSasTimerTask)expirationTimerTask).cancel();
        // Purge is expensive when called frequently, only call it every now and then.
        // We do not sync the numCancelled variable. We dont care about correctness of
        // the number, and we will still call purge rought once on every 25 cancels.
        numCancelled++;
        if(numCancelled % 100 == 0) {
            super.purge();
        }
        return cancelled;
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.startup.SipApplicationSessionTimerService#schedule(org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerTask, long, java.util.concurrent.TimeUnit)
     */
    public SipApplicationSessionTimerTask schedule(
            SipApplicationSessionTimerTask expirationTimerTask, long delay,
            TimeUnit unit) {
        if(logger.isDebugEnabled()) {
            logger.debug("Scheduling sip application session "+ expirationTimerTask.getSipApplicationSession().getKey() +" to expire in " + (delay / (double) 1000 / (double) 60) + " minutes");
        }
        super.schedule(((StandardSasTimerTask)expirationTimerTask), delay);
        return expirationTimerTask;
    }

    public void stop() {
        started.set(false);
        super.cancel();
        if(logger.isDebugEnabled()) {
            logger.debug("Stopped timer service "+ this);
        }
    }

    public void start() {
        started.set(true);
        if(logger.isDebugEnabled()) {
            logger.debug("Started timer service "+ this);
        }
    }

    public boolean isStarted() {
        return started.get();
    }

}
