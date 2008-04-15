package javax.servlet.sip;
/**
 * This listener interface can be implemented in order to get notifications of changes to the attribute lists of sessions within this SIP servlet application.
 */
public interface SipSessionAttributeListener extends java.util.EventListener{
    /**
     * Notification that an attribute has been added to a session. Called after the attribute is added.
     */
    void attributeAdded(javax.servlet.sip.SipSessionBindingEvent ev);

    /**
     * Notification that an attribute has been removed from a session. Called after the attribute is removed.
     */
    void attributeRemoved(javax.servlet.sip.SipSessionBindingEvent ev);

    /**
     * Notification that an attribute has been replaced in a session. Called after the attribute is replaced.
     */
    void attributeReplaced(javax.servlet.sip.SipSessionBindingEvent ev);

}
