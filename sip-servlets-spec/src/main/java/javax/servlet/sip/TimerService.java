/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
