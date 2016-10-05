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

import java.lang.reflect.Field;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.startup.StaticServiceHolder;
import org.mobicents.servlet.sip.utils.NamingThreadFactory;

/**
 * @author jean.deruelle@gmail.com
 * @author kakonyi.istvan@alerant.hu
 *
*/
public class DefaultProxyTimerService extends ScheduledThreadPoolExecutor implements ProxyTimerService{
    private static final Logger logger = Logger.getLogger(DefaultProxyTimerService.class
            .getName());

    // Counts the number of cancelled tasks
    protected static volatile int numCancelled = 0;

    public static final int SCHEDULER_THREAD_POOL_DEFAULT_SIZE = 4;

    public DefaultProxyTimerService(String applicationName) {
        super(SCHEDULER_THREAD_POOL_DEFAULT_SIZE, new NamingThreadFactory(applicationName + "_sip_default_proxy_timer_service"));
        schedulePurgeTaskIfNeeded();
    }

    public DefaultProxyTimerService(int corePoolSize) {
        super(corePoolSize);
        schedulePurgeTaskIfNeeded();
    }
    
    public DefaultProxyTimerService(String applicationName, int corePoolSize) {
        super(corePoolSize, new NamingThreadFactory(applicationName + "_sip_default_proxy_timer_service"));
        schedulePurgeTaskIfNeeded();
    }

    /**
     * @param corePoolSize
     * @param threadFactory
     */
    public DefaultProxyTimerService(int corePoolSize,
            ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
        schedulePurgeTaskIfNeeded();
    }

    /**
     * @param corePoolSize
     * @param handler
     */
    public DefaultProxyTimerService(int corePoolSize,
            RejectedExecutionHandler handler) {
        super(corePoolSize, handler);
        schedulePurgeTaskIfNeeded();
    }

    /**
     * @param corePoolSize
     * @param threadFactory
     * @param handler
     */
    public DefaultProxyTimerService(int corePoolSize,
            ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, threadFactory, handler);
        schedulePurgeTaskIfNeeded();
    }

    private void schedulePurgeTaskIfNeeded() {
        int purgePeriod = StaticServiceHolder.sipStandardService.getCanceledTimerTasksPurgePeriod();
        if(purgePeriod > 0) {
            Runnable r = new Runnable() {
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
            scheduleWithFixedDelay(r, purgePeriod, purgePeriod, TimeUnit.MINUTES);
        }
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#cancel(org.mobicents.servlet.sip.proxy.ProxyBranchTimerTask)
     */
    public void cancel(TimerTask task) {
        //CANCEL needs to remove the shceduled timer see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6602600
        //to improve perf

        ScheduledFuture<?> future = null;
        Object[] array = super.getQueue().toArray();
        for(int i = 0; i<array.length; i++) {
            if(array[i] instanceof ScheduledFuture<?>){
                future = (ScheduledFuture<?>)array[i];
                try {

                    Field callableField = future.getClass().getDeclaredField("callable");
                    callableField.setAccessible(true);
                    Object callable = callableField.get(future);
                    callableField.setAccessible(false);

                    if(callable!=null){
                        Field taskField = callable.getClass().getDeclaredField("task");
                        taskField.setAccessible(true);
                        TimerTask scheduledTask = null;
                        if(taskField.get(callable) instanceof TimerTask){
                             scheduledTask = (TimerTask) taskField.get(callable);
                        }
                        taskField.setAccessible(false);

                        if(scheduledTask!=null && scheduledTask.equals(task)){
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Couldn't clean the timer from the JVM GC ", e);
                }
            }
        }

        if(future != null) {
            boolean removed = super.remove((Runnable) future);
            if(logger.isDebugEnabled()) {
                logger.debug("expiration timer on sip proxy task" + task + " removed : " + removed);
            }
            boolean cancelled = future.cancel(true);
            if(logger.isDebugEnabled()) {
                logger.debug("expiration timer on sip proxy task" + task + " Cancelled : " + cancelled);
            }

            future = null;
            // Purge is expensive when called frequently, only call it every now and then.
            // We do not sync the numCancelled variable. We dont care about correctness of
            // the number, and we will still call purge rought once on every 25 cancels.
            numCancelled++;
            if(numCancelled % 100 == 0) {
                super.purge();
            }
        } else {
            if(logger.isDebugEnabled()) {
                logger.debug("expiration timer future is null, thus cannot be Cancelled");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#isStarted()
     */
    public boolean isStarted() {
        return super.isTerminated();
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#schedule(org.mobicents.servlet.sip.proxy.ProxyBranchTimerTask, long)
     */
    public void schedule(TimerTask task, long delay) {
        super.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#stop()
     */
    public void stop() {
        super.shutdownNow();
        if(logger.isInfoEnabled()) {
            logger.info("Stopped timer service "+ this);
        }
    }

    /* (non-Javadoc)
     * @see org.mobicents.servlet.sip.core.timers.ProxyTimerService#start()
     */
    public void start() {
        prestartAllCoreThreads();
        if(logger.isInfoEnabled()) {
            logger.info("Started timer service "+ this);
        }
    }
}
