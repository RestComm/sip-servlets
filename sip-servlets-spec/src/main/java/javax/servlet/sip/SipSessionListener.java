package javax.servlet.sip;
/**
 * Implementations of this interface are notified of changes to the list of active SipSessions in a SIP servlet application. To recieve notification events, the implementation class must be configured in the deployment descriptor for the SIP application.
 */
public interface SipSessionListener extends java.util.EventListener{
    /**
     * Notification that a SipSession was created.
     */
    void sessionCreated(javax.servlet.sip.SipSessionEvent se);

    /**
     * Notification that a SipSession was destroyed.
     */
    void sessionDestroyed(javax.servlet.sip.SipSessionEvent se);

}
