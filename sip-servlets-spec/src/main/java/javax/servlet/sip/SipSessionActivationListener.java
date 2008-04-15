package javax.servlet.sip;
/**
 * Objects that are bound to a session may listen to container events notifying them that sessions will be passivated and that session will be activated. A container that migrates session between VMs or persists sessions is required to notify all attributes bound to sessions implementing SipSessionActivationListener.
 */
public interface SipSessionActivationListener extends java.util.EventListener{
    /**
     * Notification that the session has just been activated.
     */
    void sessionDidActivate(javax.servlet.sip.SipSessionEvent se);

    /**
     * Notification that the session is about to be passivated.
     */
    void sessionWillPassivate(javax.servlet.sip.SipSessionEvent se);

}
