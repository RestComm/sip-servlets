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
 * Allows SIP servlet applications to set timers in order to receive notifications on timer expiration. Applications receive such notifications through an implementation of the TimerListener interface. Applications using timers must implement this interface and declare it as listener in the SIP deployment descriptor.
 * SIP servlet containers are requried to make a TimerService instance available to applications through a ServletContext attribute with name javax.servlet.sip.TimerService.
 * See Also:TimerListener, SipApplicationSession.getTimers()
 */
public interface TimerService{
    /**
     * Creates a one-time ServletTimer and schedules it to expire after the specified delay.
     */
    javax.servlet.sip.ServletTimer createTimer(javax.servlet.sip.SipApplicationSession appSession, long delay, boolean isPersistent, java.io.Serializable info);

    /**
     * Creates a repeating ServletTimer and schedules it to expire after the specified delay and then again at approximately regular intervals.
     * The ServletTimer is rescheduled to expire in either a fixed-delay or fixed-rate manner as specified by the fixedDelay argument.
     * The semantics are the same as for Timer:
     * In fixed-delay execution, each execution is scheduled relative to the actual execution time of the previous execution. If an execution is delayed for any reason (such as garbage collection or other background activity), subsequent executions will be delayed as well. In the long run, the frequency of execution will generally be slightly lower than the reciprocal of the specified period (assuming the system clock underlying Object.wait(long) is accurate).
     * In fixed-rate execution, each execution is scheduled relative to the scheduled execution time of the initial execution. If an execution is delayed for any reason (such as garbage collection or other background activity), two or more executions will occur in rapid succession to "catch up." In the long run, the frequency of execution will be exactly the reciprocal of the specified period (assuming the system clock underlying Object.wait(long) is accurate).
     */
    javax.servlet.sip.ServletTimer createTimer(javax.servlet.sip.SipApplicationSession appSession, long delay, long period, boolean fixedDelay, boolean isPersistent, java.io.Serializable info);

}
