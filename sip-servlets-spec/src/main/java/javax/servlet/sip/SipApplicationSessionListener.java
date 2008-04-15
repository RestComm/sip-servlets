package javax.servlet.sip;
/**
 * Implementations of this interface can receive notifications about invalidated and/or activated SipApplicationSession objects in the SIP application they are part of. To receive notification events, the implementation class must be configured in the deployment descriptor for the servlet application.
 */
public interface SipApplicationSessionListener extends java.util.EventListener{
    /**
     * Notification that a session was created.
     */
    void sessionCreated(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that a session was invalidated. Either it timed out or it was explicitly invalidated. It is not possible to extend the application sessions lifetime.
     */
    void sessionDestroyed(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that the application session has just been activated.
     */
    void sessionDidActivate(javax.servlet.sip.SipApplicationSessionEvent se);

    /**
     * Notification that an application session has expired. The application may request an extension of the lifetime of the application session by invoking
     * .
     */
    void sessionExpired(javax.servlet.sip.SipApplicationSessionEvent ev);

    /**
     * Notification that the application session is about to be passivated.
     */
    void sessionWillPassivate(javax.servlet.sip.SipApplicationSessionEvent se);

}
