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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;

/**
 * @author jean.deruelle@gmail.com
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class ProxyTimerServiceImpl extends Timer implements ProxyTimerService {

    private static final Logger logger = Logger.getLogger(ProxyTimerServiceImpl.class
            .getName());
    private AtomicBoolean started = new AtomicBoolean(false);

    // Counts the number of cancelled tasks
    private static volatile int numCancelled = 0;

    public ProxyTimerServiceImpl(String applicationName){
        super(applicationName + "_sip_standard_proxy_timer_service");
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
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#cancel(org.mobicents.servlet.sip.proxy.ProxyBranchTimerTask)
     */
    public void cancel(TimerTask task) {
        task.cancel();

        // Purge is expensive when called frequently, only call it every now and then.
        // We do not sync the numCancelled variable. We dont care about correctness of
        // the number, and we will still call purge rought once on every 25 cancels.
        numCancelled++;
        if(numCancelled % 100 == 0) {
            super.purge();
        }
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#isStarted()
     */
    public boolean isStarted() {
        return started.get();
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#schedule(org.mobicents.servlet.sip.proxy.ProxyBranchTimerTask, long)
     */
    public void schedule(TimerTask task, long delay) {
        super.schedule(task, delay);
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#stop()
     */
    public void stop() {
    	if(logger.isDebugEnabled()) {
            logger.debug("stop");
        }
        started.set(false);
        super.cancel();
        if(logger.isDebugEnabled()) {
            logger.debug("Stopped proxy timer service "+ this);
        }
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#start()
     */
    public void start() {
    	if(logger.isDebugEnabled()) {
            logger.debug("start");
        }
        started.set(true);
        if(logger.isDebugEnabled()) {
            logger.debug("Started proxy timer service "+ this);
        }
    }
}
