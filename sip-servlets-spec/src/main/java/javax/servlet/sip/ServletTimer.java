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
     * Get the information associated with the timer at the time of creation.
     */
    java.io.Serializable getInfo();

    /**
     * Returns the scheduled expiration time of the most recent actual expiration of this timer.
     * This method is typically invoked from within TimerListener.timerFired to determine whether the timer callback was sufficiently timely to warrant performing the scheduled activity: public void run() { if (System.currentTimeMillis() - scheduledExecutionTime() >= MAX_TARDINESS) return; // Too late; skip this execution. // Perform the task }
     * This method is typically not used in conjunction with fixed-delay execution repeating tasks, as their scheduled execution times are allowed to drift over time, and so are not terribly significant.
     */
    long scheduledExecutionTime();

}
