package javax.servlet.sip;
/**
 * Causes an object to be notified when it is bound to or unbound from a SipSession. The object is notified by an SipSessionBindingEvent object. This may be as a result of a servlet programmer explicitly unbinding an attribute from a session, due to a session being invalidated, or due to a session timing out.
 * See Also:SipSession, SipSessionBindingEvent
 */
public interface SipSessionBindingListener extends java.util.EventListener{
    /**
     * Notifies the object that it is being bound to a session and identifies the session.
     */
    void valueBound(javax.servlet.sip.SipSessionBindingEvent event);

    /**
     * Notifies the object that it is being unbound from a session and identifies the session.
     */
    void valueUnbound(javax.servlet.sip.SipSessionBindingEvent event);

}
