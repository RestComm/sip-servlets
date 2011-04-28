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

package javax.servlet.sip;
/**
 * Created by the TimerService for servlet applications wishing to schedule future tasks.
 * See Also:TimerService, TimerListener
 */
public interface ServletTimer{
    /**
     * Cancels this timer. If the task has been scheduled for one-time execution and has not yet expired, or has not yet been scheduled, it will never run. If the task has been scheduled for repeated execution, it will never expire again.
     * Note that calling this method on a repeating ServletTimer from within the timerFired method of a TimerListener absolutely guarantees that the timer will not fire again (unless rescheduled).
     * This method may be called repeatedly; the second and subsequent calls have no effect.
     */
    void cancel();

    /**
     * Returns the application session associated with this ServletTimer.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession();

    /**
     * Returns a string containing the unique identifier assigned to this timer task. 
     * The identifier is assigned by the servlet container and is implementation dependent.
     * @return a string specifying the identifier assigned to this session
     * @since 1.1
     */
    java.lang.String getId();
    
    /**
     * Get the information associated with the timer at the time of creation.
     */
    java.io.Serializable getInfo();

    /**
     * Returns the scheduled expiration time of the most recent actual expiration of this timer.
     * This method is typically invoked from within TimerListener.timerFired to determine whether the timer callback was sufficiently timely to warrant performing the scheduled activity: public void run() { if (System.currentTimeMillis() - scheduledExecutionTime() >= MAX_TARDINESS) return; // Too late; skip this execution. // Perform the task }
     * This method is typically not used in conjunction with fixed-delay execution repeating tasks, as their scheduled execution times are allowed to drift over time, and so are not terribly significant.
     */
    long scheduledExecutionTime();

    /**
     * Get the number of milliseconds that will elapse before the next scheduled timer expiration.
     * For a one-time timer that has already expired (i.e., current time > scheduled expiry time) 
     * this method will return the time remaining as a negative value. 
     * @return the number of milliseconds that will elapse before the next scheduled timer expiration. 
     */
    long getTimeRemaining();
}
