package javax.servlet.sip;
/**
 * Causes an object to be notified when it is bound to or unbound from a SipApplicationSession. The object is notified by an SipApplicationSessionBindingEvent object. This may be as a result of a servlet programmer explicitly unbinding an attribute from an application session, due to an application session being invalidated, or due to an application session timing out.
 * Since: 1.1 See Also:SipApplicationSession, SipApplicationSessionBindingEvent
 */
public interface SipApplicationSessionBindingListener extends java.util.EventListener{
    /**
     * Notifies the object that it is being bound to an application session and identifies the application session.
     */
    void valueBound(javax.servlet.sip.SipApplicationSessionBindingEvent event);

    /**
     * Notifies the object that it is being unbound from an application session and identifies the application session.
     */
    void valueUnbound(javax.servlet.sip.SipApplicationSessionBindingEvent event);

}
