package javax.servlet.sip;
/**
 * Listener interface implemented by SIP servlet applications using timers.
 * The application specifies an implementation of this interface in a listener element of the SIP deployment descriptor. There may be at most one TimerListener defined.
 * See Also:TimerService
 */
public interface TimerListener extends java.util.EventListener{
    /**
     * Notifies the listener that the specified timer has expired.
     */
    void timeout(javax.servlet.sip.ServletTimer timer);

}
